/*******************************************************************************
 * Copyright (c) 2015-2016 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.openshift.internal.ui.wizard.newapp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.jboss.tools.openshift.core.ICommonAttributes;
import org.jboss.tools.openshift.core.OpenShiftAPIAnnotations;
import org.jboss.tools.openshift.core.connection.Connection;
import org.jboss.tools.openshift.core.connection.ConnectionsRegistryUtil;
import org.jboss.tools.openshift.internal.ui.OpenShiftUIActivator;
import org.jboss.tools.openshift.internal.ui.treeitem.IModelFactory;
import org.jboss.tools.openshift.internal.ui.treeitem.ObservableTreeItem;
import org.jboss.tools.openshift.internal.ui.wizard.newapp.fromimage.ImageStreamApplicationSource;
import org.jboss.tools.openshift.internal.ui.wizard.newapp.fromtemplate.TemplateApplicationSource;

import com.openshift.restclient.OpenShiftException;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.capability.CapabilityVisitor;
import com.openshift.restclient.capability.resources.IProjectTemplateList;
import com.openshift.restclient.model.IImageStream;
import com.openshift.restclient.model.IProject;
import com.openshift.restclient.model.image.ITagReference;
import com.openshift.restclient.model.template.ITemplate;
import com.openshift.restclient.utils.ResourceStatus;

/**
 * @author Andre Dietisheim
 * @author Jeff Maury
 */
public class ApplicationSourceTreeItems implements IModelFactory, ICommonAttributes {

	private static final String BUILDER_TAG = "builder";
	public static final ApplicationSourceTreeItems INSTANCE = new ApplicationSourceTreeItems();

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> List<T> createChildren(Object parent) {
		if (parent instanceof Connection) {
		    Connection connection = (Connection) parent;
		    return (List)connection.getResources(ResourceKind.PROJECT,
                    resource -> !ResourceStatus.TERMINATING.equals(((IProject)resource).getStatus()));
		} else if (parent instanceof IProject) {
			IProject project = (IProject) parent;
			Connection conn = ConnectionsRegistryUtil.getConnectionFor(project);
			Collection appSources = loadTemplates(project, conn);
			appSources.addAll(loadImageStreams(project, conn));
			return new ArrayList<>(appSources);
		}
		return Collections.emptyList();
	}

	private Collection<IApplicationSource> loadImageStreams(IProject project, Connection conn) {
		final Collection<IImageStream> streams = conn.getResources(ResourceKind.IMAGE_STREAM, project.getNamespaceName());
		try {
			if (StringUtils.isNotBlank(conn.getClusterNamespace())) {
				Collection<IImageStream> commonStreams = conn.getResources(ResourceKind.IMAGE_STREAM,
						(String) conn.getClusterNamespace());
				commonStreams.stream().filter(s -> !streams.contains(s)).forEach(streams::add);
			}
		} catch (OpenShiftException e) {
			OpenShiftUIActivator.log(IStatus.ERROR, e.getLocalizedMessage(), e);
		}

		Collection<IApplicationSource> sources = new ArrayList<>();
		for (IImageStream is : streams) {
			List<ITagReference> tags = is.getTags().stream()
					.filter(t -> t.isAnnotatedWith(OpenShiftAPIAnnotations.TAGS) && ArrayUtils
							.contains(t.getAnnotation(OpenShiftAPIAnnotations.TAGS).split(","), BUILDER_TAG))
					.collect(Collectors.toList());
			if (!tags.isEmpty()) {
				tags.forEach(t -> sources.add(new ImageStreamApplicationSource(is, t)));
			}
		}
		return sources;
	}

	public List<ObservableTreeItem> create(Collection<?> openShiftObjects) {
		if (openShiftObjects == null) {
			return Collections.emptyList();
		}
		List<ObservableTreeItem> items = new ArrayList<>();
		for (Object openShiftObject : openShiftObjects) {
			ObservableTreeItem item = create(openShiftObject);
			if (item != null) {
				items.add(item);
			}
		}
		return items;
	}

	@Override
	public ObservableTreeItem create(Object object) {
		return new ObservableTreeItem(object, this);
	}

	private Collection<IApplicationSource> loadTemplates(IProject project, final Connection conn) {
		return project.accept(new CapabilityVisitor<IProjectTemplateList, Collection<IApplicationSource>>() {

			@Override
			public Collection<IApplicationSource> visit(IProjectTemplateList capability) {
				final Collection<ITemplate> templates = capability.getTemplates();
				if (StringUtils.isNotBlank(conn.getClusterNamespace())) {
					try {
						Collection<ITemplate> commonTemplates = capability
								.getCommonTemplates(conn.getClusterNamespace());
						commonTemplates.stream().filter(t -> !templates.contains(t)).forEach(templates::add);
					} catch (OpenShiftException e) {
						OpenShiftUIActivator.log(IStatus.ERROR, e.getLocalizedMessage(), e);
					}
				}
				return templates.stream().map(TemplateApplicationSource::new).collect(Collectors.toList());
			}
		}, Collections.emptyList());
	}
}
