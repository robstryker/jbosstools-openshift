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
package org.jboss.tools.openshift.cdk.server.ui.internal;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.wtp.ui.editor.ServerWorkingCopyPropertyComboCommand;
import org.jboss.ide.eclipse.as.wtp.ui.editor.ServerWorkingCopyPropertyCommand;
import org.jboss.tools.openshift.cdk.server.core.internal.adapter.CDKServer;

/*
 * I would love to remake this class to use a credentialing framework
 * So users can select the username and password from a central location
 */
public class CDKCredentialSection extends ServerEditorSection {

	public CDKCredentialSection() {
		// TODO Auto-generated constructor stub
	}
	private static String PASSWORD_NOT_LOADED = "***jbt****"; //$NON-NLS-1$
	
	private ModifyListener nameModifyListener, passModifyListener;
	private SelectionListener comboListener;
	
	private Text nameText, passText;
	private Combo credentialType;
	
	private String passwordString;
	private boolean passwordChanged = false;
	
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
	}
	
	public void createSection(Composite parent) {
		super.createSection(parent);
		CDKServer cdkServer = (CDKServer)server.getOriginal().loadAdapter(CDKServer.class, new NullProgressMonitor());
		
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		
		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE|ExpandableComposite.EXPANDED|ExpandableComposite.TITLE_BAR);
		section.setText("Credentials");
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
		
		Composite composite = toolkit.createComposite(section);

		composite.setLayout(new GridLayout(2, false));
		Label explanation = toolkit.createLabel(composite, 
				"Set the Red Hat credentialing method for starting the CDK.\n" + 
				"If you choose to pass credentials through environment variables,\n" + 
				"the `SUB_USERNAME` and `SUB_PASSWORD` variables will be used.");
		GridData d = new GridData(); d.horizontalSpan = 2;
		explanation.setLayoutData(d);
		
		
		credentialType = new Combo(composite, SWT.READ_ONLY);
		credentialType.setItems(CDKServer.CREDENTIAL_OPTION_VISIBLE);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		credentialType.setLayoutData(gd);
		credentialType.select(cdkServer.getCredentialType());
		
		
		Label username = toolkit.createLabel(composite, "Username: ");
		username.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		String n = cdkServer.getUsername();
		passwordString = PASSWORD_NOT_LOADED;
		nameText = toolkit.createText(composite, n); 
		Label password = toolkit.createLabel(composite, "Password: ");
		password.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		passText = toolkit.createText(composite, passwordString, SWT.SINGLE | SWT.PASSWORD);
		
		d = new GridData(); d.grabExcessHorizontalSpace = true; d.widthHint = 100;
		nameText.setLayoutData(d);
		d = new GridData(); d.grabExcessHorizontalSpace = true; d.widthHint = 100;
		passText.setLayoutData(d);
		
		comboListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				execute(new SetCredentialCommand(server));
			}
		};
		
		nameModifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				execute(new SetUserCommand(server));
			}
		};
		
		passModifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				execute(new SetPassCommand(server));
			}
		};
		credentialType.addSelectionListener(comboListener);
		nameText.addModifyListener(nameModifyListener);
		passText.addModifyListener(passModifyListener);
		
		enableUserPass();
		
		
		toolkit.paintBordersFor(composite);
		section.setClient(composite);
	}

	private void enableUserPass() {
		int sel = credentialType.getSelectionIndex();
		boolean enable = sel == CDKServer.CREDENTIAL_OPTION_ENV_VAR;
		nameText.setEnabled(enable);
		passText.setEnabled(enable);
	}

	
	public class SetCredentialCommand extends ServerWorkingCopyPropertyComboCommand {
		public SetCredentialCommand(IServerWorkingCopy server) {
			super(server, "Set Credential Method", credentialType, new Integer(credentialType.getSelectionIndex()).toString(), CDKServer.CREDENTIAL_OPTION_KEY, comboListener);
		}
		public void execute() {
			super.execute();
			enableUserPass();
		}
		
		public void undo() {
			super.undo();
			enableUserPass();
		}
		public IStatus redo() {
			IStatus red = super.redo();
			enableUserPass();
			return red;
		}
	}
	
	public class SetUserCommand extends ServerWorkingCopyPropertyCommand {
		public SetUserCommand(IServerWorkingCopy server) {
			super(server, "Change Username", nameText, nameText.getText(), 
					IJBossToolingConstants.SERVER_USERNAME, nameModifyListener);
		}
	}
	
	public class SetPassCommand extends ServerWorkingCopyPropertyCommand {
		public SetPassCommand(IServerWorkingCopy server) {
			super(server, "Change Password", passText, passText.getText(), 
					IJBossToolingConstants.SERVER_PASSWORD, passModifyListener);
			oldVal = passwordString;
		}
		
		public void execute() {
			passwordString = newVal;
			passwordChanged = !PASSWORD_NOT_LOADED.equals(passwordString);
		}
		
		public void undo() {
			passwordString = oldVal;
			text.removeModifyListener(listener);
			text.setText(oldVal);
			text.addModifyListener(listener);
			passwordChanged = !PASSWORD_NOT_LOADED.equals(passwordString);
		}
		public IStatus redo(IProgressMonitor monitor, IAdaptable adapt) {
			execute();
			return Status.OK_STATUS;
		}
	}

	/**
	 * Allow a section an opportunity to respond to a doSave request on the editor.
	 * @param monitor the progress monitor for the save operation.
	 */
	public void doSave(IProgressMonitor monitor) {
		if( passwordChanged ) {
			CDKServer cdk = (CDKServer)server.getOriginal().loadAdapter(CDKServer.class, new NullProgressMonitor());
			cdk.setPassword(passwordString);
			monitor.worked(100);
			passwordChanged = false;
		}
	}

}
