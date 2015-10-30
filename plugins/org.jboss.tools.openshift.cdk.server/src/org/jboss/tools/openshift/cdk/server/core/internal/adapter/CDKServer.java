package org.jboss.tools.openshift.cdk.server.core.internal.adapter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.ServerDelegate;

public class CDKServer extends ServerDelegate {

	public static final String CDK_SERVER_TYPE = "org.jboss.tools.openshift.cdk.server.type";
	public static final String PROP_FOLDER = "org.jboss.tools.openshift.cdk.server.core.internal.adapter.FOLDER";
	
	
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

}
