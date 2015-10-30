package org.jboss.tools.openshift.cdk.server.core.internal.adapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller2;
import org.jboss.ide.eclipse.as.core.server.IServerStatePollerType;

public class VagrantPoller implements IServerStatePoller2 {
	private IServer server;
	private boolean canceled, done;
	private boolean state;
	private boolean expectedState;
	private PollingException aborted = null;

	@Override
	public IServer getServer() {
		return server;
	}


	@Override
	public void beginPolling(IServer server, boolean expectedState) throws PollingException {
		this.server = server;
		this.canceled = done = false;
		this.expectedState = expectedState;
		this.state = !expectedState;
		launchThread();
	}
	protected void launchThread() {
		Thread t = new Thread(new Runnable(){
			public void run() {
				pollerRun();
			}
		}, "Vagrant Poller"); //$NON-NLS-1$
		t.start();
	}
	

	private synchronized void setStateInternal(boolean done, boolean state) {
		this.done = done;
		this.state = state;
	}
	
	private void pollerRun() {
		setStateInternal(false, state);
		while(aborted == null && !canceled && !done) {
			boolean up = onePing(server);
			if( up == expectedState ) {
				setStateInternal(true, expectedState);
			}
//			try {
//				Thread.sleep(100);
//			} catch(InterruptedException ie) {} // ignore
		}
	}

	public synchronized boolean isComplete() throws PollingException, RequiresInfoException {
		return done;
	}

	public synchronized boolean getState() throws PollingException, RequiresInfoException {
		return state;
	}

	public void cleanup() {
	}

	public synchronized void cancel(int type) {
		canceled = true;
	}

	public int getTimeoutBehavior() {
		return TIMEOUT_BEHAVIOR_FAIL;
	}

	@Override
	public List<String> getRequiredProperties() {
		// TODO Auto-generated method stub
		return null;
	}


	public IStatus getCurrentStateSynchronous(IServer server) {
		boolean b = onePing(server);
		Status s;
		if( b ) {
			s = new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, "Vagrant Instance is Up");
		} else {
			s = new Status(IStatus.INFO, JBossServerCorePlugin.PLUGIN_ID, "Vagrant Instance is not up");
		}
		return s;
	}
	
	private File getWorkingDirectory(IServer s) throws PollingException {
		String str = s.getAttribute(CDKServer.PROP_FOLDER, (String)null);
		if( str != null && new File(str).exists()) {
			return new File(str);
		}
		throw  new PollingException("Working Directory not found: " + str);
	}
	
	private boolean onePing(IServer server) {
	    try {
	        String line;
	    	Process p = null;
	    	List<String> args = new ArrayList<String>();
	    	String vagrantCmdLoc = "/usr/bin/vagrant";
	    	args.add(vagrantCmdLoc);
	    	args.add("status");
	    	args.add("--machine-readable");
	    	ProcessBuilder pb = new ProcessBuilder(args);
	    	try {
		    	File fDir = getWorkingDirectory(server);
		    	pb.directory(fDir);
		    	p = pb.start();
		        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
		        StringBuffer sb = new StringBuffer();
	  	        while ((line = input.readLine()) != null) {
	  	        	sb.append(line);
	  	        	sb.append("\n");
	  	        }
	  	        input.close();
	  	        
	  	        // Evaluate the output
	  	        boolean result = parseOutput(sb.toString());
	  	        return result;
	    	} catch(PollingException pe) {
	    		aborted = pe;
	    	} catch(IOException ioe) {
	    		// TODO
	    		ioe.printStackTrace();
	    	}
	      }
	      catch (Exception err) {
	        err.printStackTrace();
	      }
		return true;
	}
	
	private class VagrantStatus {
		static final String PROVIDER_NAME = "provider-name";
		static final String STATE = "state";
		static final String STATE_HUMAN_SHORT = "state-human-short";
		static final String STATE_HUMAN_LONG = "state-human-long";
		
		static final String STATE_RUNNING = "running";
		static final String STATE_SHUTOFF = "shutoff";
		
		private HashMap<String, String> kv;
		private String id;
		public VagrantStatus(String vmId) {
			this.id = vmId;
			this.kv = new HashMap<String, String>();
		}
		public void setProperty(String k, String v) {
			kv.put(k, v);
		}
		public String getState() {
			return kv.get(STATE);
		}
	}
	
	
	private boolean parseOutput(String s) {
		HashMap<String, VagrantStatus> status = new HashMap<String, VagrantStatus>();
		String[] byLine = s.split("\n");
		for( int i = 0; i < byLine.length; i++ ) {
			String[] csv = byLine[i].split(",");
			String timestamp = csv[0];
			String vmId = csv[1];
			if( vmId != null && !vmId.isEmpty() ) {
				VagrantStatus vs = status.get(vmId);
				if( vs == null ) {
					vs = new VagrantStatus(vmId);
					status.put(vmId, vs);
				}
				String k = csv[2];
				String v = csv[3];
				if( k != null ) {
					vs.setProperty(k,v);
				}
			} else {
				return false;
			}
		}
		
		Collection<VagrantStatus> stats = status.values();
		Iterator<VagrantStatus> i = stats.iterator();
		while(i.hasNext()) {
			if( !VagrantStatus.STATE_RUNNING.equals(i.next().getState())) {
				return false;
			}
		}
		
		return true;
	}
	

	@Override
	public void provideCredentials(Properties credentials) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public IServerStatePollerType getPollerType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPollerType(IServerStatePollerType type) {
		// TODO Auto-generated method stub
		
	}

}
