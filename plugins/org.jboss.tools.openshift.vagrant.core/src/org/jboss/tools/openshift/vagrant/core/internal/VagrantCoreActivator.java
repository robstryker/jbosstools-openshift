package org.jboss.tools.openshift.vagrant.core.internal;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleContext;

public class VagrantCoreActivator extends Plugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.openshift.vagrant.core"; //$NON-NLS-1$

	// The shared instance
	private static VagrantCoreActivator plugin;
	
	/**
	 * The constructor
	 */
	public VagrantCoreActivator() {
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
	public static VagrantCoreActivator getDefault() {
		return plugin;
	}


}
