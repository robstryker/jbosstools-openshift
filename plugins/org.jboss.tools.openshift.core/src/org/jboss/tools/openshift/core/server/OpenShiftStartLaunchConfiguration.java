/*******************************************************************************
 * Copyright (c) 2015 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.jboss.tools.openshift.core.server;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;

/**
 * @author Andre Dietisheim
 */
public class OpenShiftStartLaunchConfiguration 
	extends AbstractJavaLaunchConfigurationDelegate 
	implements ILaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) 
			throws CoreException {
		IServer server = ServerUtil.getServer(configuration);
		OpenShiftServerBehaviour osb = (OpenShiftServerBehaviour)server.loadAdapter(OpenShiftServerBehaviour.class, null);
		osb.setServerStarting();
		if( "run".equals(mode)) {
			int remotePort = getDebugPortFromConfig(server);
			if( remotePort != -1 ) {
				// TODO should we also cancel the existing remote deugger?  
				// Code not stubbed for that... 
				
				
				unmapPortForwarding(server, remotePort);
				removeDebugPortFromDeploymentConfig(server);
				waitForPodRestart(server);
			}
			osb.setServerStarted();
		} else if( "debug".equals(mode)) {
			if( remotePodIsJava(server)) {
				int remotePort = getDebugPortFromConfig(server);
				if( remotePort == -1 ) {
					remotePort = addDebugPortToDeploymentConfig(server);
					if( remotePort != -1 ) {
						waitForPodRestart(server);
						int localPort = mapPortForwarding(server, remotePort);
						if( localPort != -1 ) {
							attachRemoteDebugger(server, localPort);
						} else {
							// server was successfully launched in debug mode, but mapping of port forwarding failed (?)
							// not sure if we should set to stopped here. 
						}
					} else {
						// Was not able to set up a remote port
						osb.setServerStopped();
					}
				}
				osb.setServerStarted();
			}
		}
	}
	
	private boolean remotePodIsJava(IServer server) {
		// TODO
		return true;
	}
	
	/**
	 * Read the debug port from the deployment config, or -1 if not found
	 * @param server
	 * @return
	 */
	private int getDebugPortFromConfig(IServer server) {
		// TODO
		return -1;
	}

	/**
	 * Unmap the port forwarding. 
	 * @param server
	 * @param port
	 * @return true on success, false otherwise
	 */
	private boolean unmapPortForwarding(IServer server, int port) {
		// TODO 
		return true;
	}
	
	/**
	 * Remove the debug port section from the deployment configuration
	 * @param server
	 * @return
	 */
	private boolean removeDebugPortFromDeploymentConfig(IServer server) {
		// TODO
		return true;
	}
	
	/**
	 * Synchronous wait
	 * @param server
	 */
	private void waitForPodRestart(IServer server) {
		// TODO
	}
	
	/**
	 * Add a debug port to the deployment config. 
	 * Return what port is designated as the debug port, or -1 if failed
	 * @param server
	 * @return
	 */
	private int addDebugPortToDeploymentConfig(IServer server) {
		// TODO
		return -1;
	}
	
	
	/**
	 * Map the remote port to a local port. 
	 * Return the local port in use, or -1 if failed
	 * @param server
	 * @param remotePort
	 * @return
	 */
	private int mapPortForwarding(IServer server, int remotePort) {
		// TODO
		return -1;
	}
	
	private void attachRemoteDebugger(IServer server, int localDebugPort) throws CoreException {
		String REMOTE_JAVA = "org.eclipse.jdt.launching.remoteJavaApplication";
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(REMOTE_JAVA);
		ILaunchConfigurationWorkingCopy config = type.newInstance(null, server.getName());
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, false); 		
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_CONNECTOR,  IJavaLaunchConfigurationConstants.ID_SOCKET_ATTACH_VM_CONNECTOR);

		Map<String,String> attrMap = new HashMap<String,String>();
		attrMap.put("port", Integer.toString(localDebugPort));
		attrMap.put("hostname", "localhost");
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CONNECT_MAP, attrMap);
		config.launch("run", new NullProgressMonitor());
	}
}
