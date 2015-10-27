package org.jboss.tools.openshift.vagrant.core.internal.server;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.internal.terminal.control.impl.ITerminalControlForText;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalService;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller2;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;
import org.jboss.tools.openshift.vagrant.ui.internal.util.TerminalUtility;

public class VagrantLaunch implements ILaunchConfigurationDelegate2 {

	private class CustomDone implements ITerminalService.Done {
		private IStatus stat = null;
		public void done(IStatus status) {
			System.out.println("done called");
			this.stat = status;
		}
		public IStatus getStatus() {
			return stat;
		}
	}
	
	
	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		final IServer s = ServerUtil.getServer(configuration);
		final VagrantServerBehaviour beh = (VagrantServerBehaviour)s.loadAdapter(VagrantServerBehaviour.class, new NullProgressMonitor());
		beh.setServerStarting();
		
		final Map<String, Object> props = TerminalUtility.getPropertiesForServer(s);		
		final CustomDone customDone = new CustomDone();
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				openView(props, customDone);
			}
		});
		
		
		// Wait for done
		while(customDone.getStatus() == null ) {
			try {
				Thread.sleep(200);
			} catch(InterruptedException ie) {
				// TODO
			}
		}
		
		
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				ITerminalControlForText control = TerminalUtility.findTerminalControl(props);
				if( control != null ) {
					OutputStream os = control.getOutputStream();
					try {
						os.write("vagrant up\n".getBytes());
					} catch(IOException ioe) {
						ioe.printStackTrace();
					}
					
					launchPoller(beh);
				}
			}
		});
		
	}
	
	
	private void launchPoller(VagrantServerBehaviour beh) {
		// delay the launch of polling until the cmd vagrant up has been actually run. 
		try {
			Thread.sleep(1500);
		} catch(InterruptedException ie) {
			// ignore
		}
		PollThreadUtils.pollServer(beh.getServer(), IServerStatePoller2.SERVER_UP, new VagrantPoller());
	}
	
	
	private void openView(Map<String, Object> props, ITerminalService.Done d) {
		TerminalUtility.openConsole(props, d);
	}
	

	@Override
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean finalLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		// TODO Auto-generated method stub
		return true;
	}

}
