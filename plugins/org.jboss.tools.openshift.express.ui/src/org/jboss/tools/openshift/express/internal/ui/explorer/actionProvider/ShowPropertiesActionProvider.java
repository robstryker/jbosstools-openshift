/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.openshift.express.internal.ui.explorer.actionProvider;

import org.jboss.tools.openshift.express.internal.ui.action.ShowPropertiesAction;

/**
 * @author Xavier Coulon
 */
public class ShowPropertiesActionProvider extends AbstractOpenShiftExplorerViewerActionProvider {

	public ShowPropertiesActionProvider() {
		super(new ShowPropertiesAction(), "group.properties");
	}

}
