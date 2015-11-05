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
package org.jboss.tools.openshift.cdk.server.core.internal.adapter;

import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.SERVER_PASSWORD;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.SERVER_USERNAME;

import java.io.UnsupportedEncodingException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;

public class CDKServer extends ServerDelegate {

	public static final String CDK_SERVER_TYPE = "org.jboss.tools.openshift.cdk.server.type";
	public static final String PROP_FOLDER = "org.jboss.tools.openshift.cdk.server.core.internal.adapter.FOLDER";
	
	
	public static final String CREDENTIAL_OPTION_KEY = "org.jboss.tools.openshift.cdk.server.core.internal.adapter.credential.mode";

	public static final int CREDENTIAL_OPTION_PROMPT = 0;
	public static final int CREDENTIAL_OPTION_HARD_CODED = 1;
	public static final int CREDENTIAL_OPTION_ENV_VAR = 2;
	

	public static final String[] CREDENTIAL_OPTION_VISIBLE = new String[]{
			"Prompt",
			"Hard-coded in Vagrantfile",
			"* Passed via Environment Variables", 
	};
	public static final int[] CREDENTIAL_OPTION_VALS = new int[]{
			CREDENTIAL_OPTION_PROMPT, CREDENTIAL_OPTION_HARD_CODED,  CREDENTIAL_OPTION_ENV_VAR
	};
	
	
	public CDKServer() {
	}

	@Override
	public IStatus canModifyModules(IModule[] add, IModule[] remove) {
		return Status.CANCEL_STATUS;
	}

	@Override
	public IModule[] getChildModules(IModule[] module) {
		return new IModule[0];
	}

	@Override
	public IModule[] getRootModules(IModule module) throws CoreException {
		return new IModule[0];
	}

	@Override
	public void modifyModules(IModule[] add, IModule[] remove, IProgressMonitor monitor) throws CoreException {
	}

	
	public int getCredentialType() {
		return getAttribute(CREDENTIAL_OPTION_KEY, CREDENTIAL_OPTION_PROMPT);
	}
	
	public void setCredentialType(int type) {
		setAttribute(CREDENTIAL_OPTION_KEY, type);
	}
	
	public String getUsername() {
		return getAttribute(SERVER_USERNAME, (String)null);
	}
	
	
	public void setUsername(String name) {
		setAttribute(SERVER_USERNAME, name);
	}
	
	public String getPassword() {
		String s = ServerUtil.getFromSecureStorage(getServer(), SERVER_PASSWORD);
		if( s == null )
			return getAttribute(SERVER_PASSWORD, (String)null);
		return s;
	}
	
	public void setPassword(String pass) {
		try {
			ServerUtil.storeInSecureStorage(getServer(), SERVER_PASSWORD, pass);
        } catch (StorageException e) {
        	JBossServerCorePlugin.log(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Could not save password for server in secure storage.", e)); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
        	JBossServerCorePlugin.log(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Could not save password for server in secure storage.", e)); //$NON-NLS-1$	
        }
	}
	
	
}
