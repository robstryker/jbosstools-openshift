package org.jboss.tools.openshift.cdk.server.core.internal.adapter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.control.impl.ITerminalControlForText;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalService;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller2;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllerEnvironment;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ISubsystemController;
import org.jboss.tools.as.core.server.controllable.IDeployableServerBehaviorProperties;
import org.jboss.tools.openshift.cdk.server.ui.internal.util.TerminalUtility;

public class CDKServerBehaviour extends ControllableServerBehavior implements IControllableServerBehavior {
	protected final Object serverStateLock = new Object();
	public CDKServerBehaviour() {
	}


	public void setServerStarting() {
		synchronized(serverStateLock) {
			setServerState(IServer.STATE_STARTING);
		}
	}

	public void setServerStarted() {
		synchronized(serverStateLock) {
			setServerState(IServer.STATE_STARTED);
		}
	}

	public void setServerStopping() {
		synchronized(serverStateLock) {
			setServerState(IServer.STATE_STOPPING);
		}
	}
	
	public void setServerStopped() {
		synchronized(serverStateLock) {
			setServerState(IServer.STATE_STOPPED);
		}
	}
	
	private void cancelPoller() {
		// Server is in process of starting or stopping... 
		Object pt = getSharedData(IDeployableServerBehaviorProperties.POLL_THREAD);
		if( pt instanceof PollThread ) {
			((PollThread) pt).cancel();
		}
	}
	
	
	@Override
	public void stop(boolean force) {
		setServerStopping();
		
		IStatus state = PollThreadUtils.isServerStarted(getServer(), new VagrantPoller());
		boolean started = state.isOK();
		boolean requiresCancelation = true;  // Bug in poller, assume always need to cancel existing startup
		if( !started ) {
			if( state.getSeverity() == IStatus.ERROR ) {
				// server is stopped, cancel the poller
				cancelPoller();
				setServerStopped();
				return;
			}
			// server is either starting or stopping, so cancel the transition
			requiresCancelation = true;
		}
		
		
		final ITerminalControlForText[] control = new ITerminalControlForText[1];
		control[0] = null;
		final Map<String, Object> props = TerminalUtility.getPropertiesForServer(getServer());
		Display.getDefault().syncExec(new Runnable(){
			public void run() {
				control[0] = TerminalUtility.findTerminalControl(props);
			}
		});
		
		final CustomDone cd = new CustomDone();
		if( control[0] == null ) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					TerminalUtility.openConsole(props, cd);
				}
			});
			// Wait for done
			while(cd.getStatus() == null ) {
				try {
					Thread.sleep(200);
				} catch(InterruptedException ie) {
					// TODO
				}
			}
			Display.getDefault().syncExec(new Runnable(){
				public void run() {
					control[0] = TerminalUtility.findTerminalControl(props);
				}
			});
		}
		
		if( control[0] != null ) {
			OutputStream os = control[0].getOutputStream();
			try {
				if( requiresCancelation ) {
					os.write(new byte[]{3}); // Hitting ctrl+c in case the startup has stalled
					try {
						Thread.sleep(300); // delay since it requires 2 ctrl+c to cancel properly
					} catch(InterruptedException ie) {}
					os.write(new byte[]{3}); // Hitting ctrl+c in case the startup has stalled
				}
				os.write("\nvagrant halt\n".getBytes());
				os.flush();
				try {
					Thread.sleep(1500);
				} catch(InterruptedException ie) {
					// ignore
				}
				launchShutdownPoller();
			} catch(IOException ioe) {
				ioe.printStackTrace();
			}
		} else {
			setServerStarted();
		}
	}
	
	private void launchShutdownPoller() {
		PollThreadUtils.pollServer(getServer(), IServerStatePoller2.SERVER_DOWN, new VagrantPoller());
	}
	
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

	
	/*
	 * Implementing IControllableServerBehaviour just to make sure poll thread utils work for this
	 */
	protected HashMap<String, Object> sharedData = new HashMap<String, Object>();
	public synchronized Object getSharedData(String key) {
		return sharedData.get(key);
	}
	
	public synchronized void putSharedData(String key, Object o) {
		sharedData.put(key, o);
	}

	
	// Unused IControllableServerBehaviour methods below
	
	@Override
	public ISubsystemController getController(String system) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ISubsystemController getController(String system, ControllerEnvironment env) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ISubsystemController getWorkingCopyController(String system, IServerWorkingCopy wc) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public IStatus canPublish() {
		return Status.CANCEL_STATUS;
	}
	
	public IStatus canStart(String launchMode) {
		return Status.OK_STATUS;
	}
	
	public IStatus canStop() {
		return Status.OK_STATUS;
	}
}
