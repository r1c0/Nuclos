// Copyright (C) 2010 Novabit Informationssysteme GmbH
//
// This file is part of Nuclos.
//
// Nuclos is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Nuclos is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with Nuclos. If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.installer.mode.wizard;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.nuclos.installer.ConfigContext;
import org.nuclos.installer.Constants;
import org.nuclos.installer.L10n;
import org.nuclos.installer.unpack.GenericUnpacker;
import org.pietschy.wizard.InvalidStateException;

/**
 * Collect server configuration
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 */
public class ServerConfigurationWizardStep extends AbstractWizardStep implements Constants {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txtJavaHomePath = new JTextField();
	private JButton btnJavaHomeSelect = new JButton();

	private JTextField txtInstance = new JTextField();
	private JTextField txtPort = new JTextField();

	private ButtonGroup group = new ButtonGroup();
	private JRadioButton optHttp = new JRadioButton();
	private JRadioButton optHttps = new JRadioButton();

	private JTextField txtHttpsPort = new JTextField();
	private JTextField txtHttpsKeystoreFile = new JTextField();
	private JButton btnHttpsKeystoreSelect = new JButton();
	private JPasswordField txtKeystorePassword1 = new JPasswordField();
	private JPasswordField txtKeystorePassword2 = new JPasswordField();
	private JTextField txtShutdownPort = new JTextField();
	private JTextField txtMemory = new JTextField();

	private JCheckBox chkLaunch = new JCheckBox();

	private static double layout[][] = { { 20.0, 200.0, TableLayout.FILL, 100.0 }, // Columns
			{ 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, TableLayout.FILL } }; // Rows

	public ServerConfigurationWizardStep() {
		super(L10n.getMessage("gui.wizard.server.title"), L10n.getMessage("gui.wizard.server.description"));

		TableLayout layout = new TableLayout(this.layout);
		layout.setVGap(5);
		layout.setHGap(5);
		this.setLayout(layout);

		JLabel label = new JLabel();
		label.setText(L10n.getMessage("gui.wizard.server.javahomepath.label"));
		this.add(label, "0,0, 1,0");

		this.txtJavaHomePath.getDocument().addDocumentListener(this);
		this.add(txtJavaHomePath, "2,0");

		btnJavaHomeSelect.setText(L10n.getMessage("filechooser.browse"));
		btnJavaHomeSelect.addActionListener(new SelectJavaHomeActionListener());
		this.add(btnJavaHomeSelect, "3,0");

		label = new JLabel();
		label.setText(L10n.getMessage("gui.wizard.server.instance.label"));
		this.add(label, "0,1, 1,1");

		this.txtInstance.getDocument().addDocumentListener(this);
		this.txtInstance.setEnabled(!ConfigContext.isUpdate());
		this.add(txtInstance, "2,1");

		optHttp.addActionListener(this);
		this.add(optHttp, "0,2");

		label = new JLabel();
		label.setText(L10n.getMessage("gui.wizard.server.port.label"));
		this.add(label, "1,2");

		this.txtPort.getDocument().addDocumentListener(this);
		this.add(txtPort, "2,2");

		optHttps.addActionListener(this);
		this.add(optHttps, "0,3");

		label = new JLabel();
		label.setText(L10n.getMessage("gui.wizard.server.httpsport.label"));
		this.add(label, "1,3");

		this.txtHttpsPort.getDocument().addDocumentListener(this);
		this.add(txtHttpsPort, "2,3");

		label = new JLabel();
		label.setText(L10n.getMessage("gui.wizard.server.httpskeystorefile.label"));
		this.add(label, "1,4");

		this.txtHttpsKeystoreFile.getDocument().addDocumentListener(this);
		this.add(txtHttpsKeystoreFile, "2,4");

		btnHttpsKeystoreSelect.setText(L10n.getMessage("filechooser.browse"));
		btnHttpsKeystoreSelect.addActionListener(new SelectKeyStoreActionListener());
		this.add(btnHttpsKeystoreSelect, "3,4");

		label = new JLabel();
		label.setText(L10n.getMessage("gui.wizard.server.httpskeystorepassword1.label"));
		this.add(label, "1,5");

		this.txtKeystorePassword1.getDocument().addDocumentListener(this);
		this.add(txtKeystorePassword1, "2,5");

		label = new JLabel();
		label.setText(L10n.getMessage("gui.wizard.server.httpskeystorepassword1.label"));
		this.add(label, "1,6");

		this.txtKeystorePassword2.getDocument().addDocumentListener(this);
		this.add(txtKeystorePassword2, "2,6");

		label = new JLabel();
		label.setText(L10n.getMessage("gui.wizard.server.shutdownport.label"));
		this.add(label, "0,7, 1,7");

		this.txtShutdownPort.getDocument().addDocumentListener(this);
		this.add(txtShutdownPort, "2,7");

		label = new JLabel();
		label.setText(L10n.getMessage("gui.wizard.server.memory.label"));
		this.add(label, "0,8, 1,8");

		this.txtMemory.getDocument().addDocumentListener(this);
		this.add(txtMemory, "2,8");

		chkLaunch.setText(L10n.getMessage("gui.wizard.server.launch.label"));
		this.chkLaunch.addActionListener(this);
		this.add(chkLaunch, "0,9 2,9");

		group.add(optHttp);
		group.add(optHttps);
	}

	@Override
	public void prepare() {
		modelToView(JAVA_HOME, txtJavaHomePath);
		modelToView(NUCLOS_INSTANCE, txtInstance);
		optHttp.setSelected("true".equals(ConfigContext.getProperty(HTTP_ENABLED)));
		modelToView(HTTP_PORT, txtPort);
		optHttps.setSelected("true".equals(ConfigContext.getProperty(HTTPS_ENABLED)));
		modelToView(HTTPS_PORT, txtHttpsPort);
		modelToView(HTTPS_KEYSTORE_FILE, txtHttpsKeystoreFile);
		modelToView(HTTPS_KEYSTORE_PASSWORD, txtKeystorePassword1);
		modelToView(HTTPS_KEYSTORE_PASSWORD, txtKeystorePassword2);
		modelToView(SHUTDOWN_PORT, txtShutdownPort);
		modelToView(HEAP_SIZE, txtMemory);
		modelToView(LAUNCH_STARTUP, chkLaunch);
		this.chkLaunch.setEnabled(!(getModel().getUnpacker() instanceof GenericUnpacker));
	}

	@Override
	protected void updateState() {
		txtPort.setEnabled(optHttp.isSelected());
		txtHttpsPort.setEnabled(optHttps.isSelected());
		txtHttpsKeystoreFile.setEnabled(optHttps.isSelected());
		btnHttpsKeystoreSelect.setEnabled(optHttps.isSelected());
		txtKeystorePassword1.setEnabled(optHttps.isSelected());
		txtKeystorePassword2.setEnabled(optHttps.isSelected());
		setComplete(true);
	}

	@Override
	public void applyState() throws InvalidStateException {
		viewToModel(JAVA_HOME, txtJavaHomePath);
		viewToModel(NUCLOS_INSTANCE, txtInstance);
		ConfigContext.setProperty(HTTP_ENABLED, optHttp.isSelected() ? "true" : "false");
		viewToModel(HTTP_PORT, txtPort);
		ConfigContext.setProperty(HTTPS_ENABLED, optHttps.isSelected() ? "true" : "false");
		viewToModel(HTTPS_PORT, txtHttpsPort);
		viewToModel(HTTPS_KEYSTORE_FILE, txtHttpsKeystoreFile);
		if (optHttps.isSelected()) {
			validatePasswordEquality(txtKeystorePassword1, txtKeystorePassword2, "gui.wizard.server.httpskeystorepassword1.label");
		}
		viewToModel(HTTPS_KEYSTORE_PASSWORD, txtKeystorePassword1);
		viewToModel(SHUTDOWN_PORT, txtShutdownPort);
		viewToModel(HEAP_SIZE, txtMemory);
		viewToModel(LAUNCH_STARTUP, chkLaunch);
	}

	private class SelectJavaHomeActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new java.io.File("."));
			chooser.setDialogTitle(L10n.getMessage("gui.wizard.path.filechooser.title"));
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			int returnVal = chooser.showOpenDialog(ServerConfigurationWizardStep.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				txtJavaHomePath.setText(file.getAbsolutePath());
			}
		}
	}

	private class SelectKeyStoreActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new java.io.File("."));
			chooser.setDialogTitle(L10n.getMessage("gui.wizard.path.filechooser.title"));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			int returnVal = chooser.showOpenDialog(ServerConfigurationWizardStep.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				txtHttpsKeystoreFile.setText(file.getAbsolutePath());
			}
		}
	}
}
