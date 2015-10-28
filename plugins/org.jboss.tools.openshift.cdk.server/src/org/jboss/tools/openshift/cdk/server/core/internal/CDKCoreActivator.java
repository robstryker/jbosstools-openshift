package org.jboss.tools.openshift.cdk.server.core.internal;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleContext;

public class CDKCoreActivator extends Plugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.openshift.cdk.server.core"; //$NON-NLS-1$

	// The shared instance
	private static CDKCoreActivator plugin;
	
	/**
	 * The constructor
	 */
	public CDKCoreActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static CDKCoreActivator getDefault() {
		return plugin;
	}


}
