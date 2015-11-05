/******************************************************************************* 
 * Copyright (c) 2015 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.openshift.cdk.server.core.internal;

import org.eclipse.wst.server.core.IServer;

// TODO - allow customization of this location
public class CDKConstantUtility {
	private static final String VAGRANT_LOCATION_LINUX = "/usr/bin/vagrant";

	public static String getVagrantLocation(IServer server) {
		return VAGRANT_LOCATION_LINUX;
	}
}
