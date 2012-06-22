//Copyright (C) 2010  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.installer.mode.wizard;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.nuclos.installer.Constants;
import org.nuclos.installer.L10n;
import org.pietschy.wizard.InvalidStateException;

/**
 * Configuration of existing database connection.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 */
public class UseDatabaseWizardStep extends AbstractWizardStep implements ActionListener, Constants {

	private JComboBox cmbAdapter = new JComboBox();

	private JTextField txtDriverJarPath = new JTextField();
	private JButton btnDriverJarSelect = new JButton();

	private JTextField txtHost = new JTextField();

	private JTextField txtPort = new JTextField();

	private JTextField txtDbName = new JTextField();

	private JTextField txtDbUsername = new JTextField();

	private JPasswordField txtDbPassword1 = new JPasswordField();
	private JPasswordField txtDbPassword2 = new JPasswordField();

	private JTextField txtSchema = new JTextField();

	private JTextField txtTablespace = new JTextField();
	private JTextField txtTablespaceIndex = new JTextField();

	private static double layout[][] = {
        { 200.0, TableLayout.FILL, 100.0 }, // Columns
        { 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, TableLayout.FILL } };// Rows

	public UseDatabaseWizardStep() {
		super(L10n.getMessage("gui.wizard.usedb.title"), L10n.getMessage("gui.wizard.usedb.description"));

		TableLayout layout = new TableLayout(this.layout);
        layout.setVGap(5);
        layout.setHGap(5);
        this.setLayout(layout);

        JLabel label = new JLabel();
        label.setText(L10n.getMessage("gui.wizard.usedb.adapter.label"));
        this.add(label, "0,0");

        cmbAdapter.addItem(new ComboBoxItem("postgresql", "PostgreSQL (8.4+)"));
        cmbAdapter.addItem(new ComboBoxItem("oracle", "Oracle (10+)"));
        cmbAdapter.addItem(new ComboBoxItem("mssql", "Microsft SQL Server (2005+)"));
        cmbAdapter.addItem(new ComboBoxItem("sybase", "Sybase SQL Anywhere (10+)"));
        cmbAdapter.addItem(new ComboBoxItem("db2", "IBM DB2 (10+)"));
        cmbAdapter.addActionListener(this);
        this.add(cmbAdapter, "1,0");

        label = new JLabel();
        label.setText(L10n.getMessage("gui.wizard.usedb.driverjar.label"));
        this.add(label, "0,1");

        this.txtDriverJarPath.getDocument().addDocumentListener(this);
		this.add(txtDriverJarPath, "1,1");

		btnDriverJarSelect.setText(L10n.getMessage("filechooser.browse"));
		btnDriverJarSelect.addActionListener(new SelectJarActionListener());
        this.add(btnDriverJarSelect, "2,1");

        label = new JLabel();
        label.setText(L10n.getMessage("gui.wizard.usedb.host.label"));
        this.add(label, "0,2");

        txtHost.getDocument().addDocumentListener(this);
        this.add(txtHost, "1,2");

        label = new JLabel();
        label.setText(L10n.getMessage("gui.wizard.usedb.port.label"));
        this.add(label, "0,3");

        txtPort.getDocument().addDocumentListener(this);
        this.add(txtPort, "1,3");

        label = new JLabel();
        label.setText(L10n.getMessage("gui.wizard.usedb.dbname.label"));
        this.add(label, "0,4");

        txtDbName.getDocument().addDocumentListener(this);
        this.add(txtDbName, "1,4");

        label = new JLabel();
        label.setText(L10n.getMessage("gui.wizard.usedb.dbuser.label"));
        this.add(label, "0,5");

        txtDbUsername.getDocument().addDocumentListener(this);
        this.add(txtDbUsername, "1,5");

        label = new JLabel();
        label.setText(L10n.getMessage("gui.wizard.usedb.dbpassword1.label"));
        this.add(label, "0,6");

        txtDbPassword1.getDocument().addDocumentListener(this);
        this.add(txtDbPassword1, "1,6");

        label = new JLabel();
        label.setText(L10n.getMessage("gui.wizard.usedb.dbpassword2.label"));
        this.add(label, "0,7");

        txtDbPassword1.getDocument().addDocumentListener(this);
        this.add(txtDbPassword2, "1,7");

        label = new JLabel();
        label.setText(L10n.getMessage("gui.wizard.usedb.schema.label"));
        this.add(label, "0,8");

        txtSchema.getDocument().addDocumentListener(this);
        this.add(txtSchema, "1,8");

        label = new JLabel();
        label.setText(L10n.getMessage("gui.wizard.usedb.tablespace.label"));
        this.add(label, "0,9");

        txtTablespace.getDocument().addDocumentListener(this);
        this.add(txtTablespace, "1,9");

        label = new JLabel();
        label.setText(L10n.getMessage("gui.wizard.usedb.tablespaceindex.label"));
        this.add(label, "0,10");

        txtTablespaceIndex.getDocument().addDocumentListener(this);
        this.add(txtTablespaceIndex, "1,10");
	}

	@Override
	public void prepare() {
		modelToView(DATABASE_ADAPTER, cmbAdapter);
		modelToView(DATABASE_DRIVERJAR, txtDriverJarPath);
		modelToView(DATABASE_SERVER, txtHost);
		modelToView(DATABASE_PORT, txtPort);
		modelToView(DATABASE_NAME, txtDbName);
		modelToView(DATABASE_USERNAME, txtDbUsername);
		modelToView(DATABASE_PASSWORD, txtDbPassword1);
		modelToView(DATABASE_PASSWORD, txtDbPassword2);
		modelToView(DATABASE_SCHEMA, txtSchema);
		modelToView(DATABASE_TABLESPACE, txtTablespace);
		modelToView(DATABASE_TABLESPACEINDEX, txtTablespaceIndex);
		updateState();
	}

	@Override
	protected void updateState() {
		String adapter = ((ComboBoxItem)cmbAdapter.getSelectedItem()).value;
		if ("postgresql".equals(adapter)) {
			txtDriverJarPath.setEnabled(false);
			btnDriverJarSelect.setEnabled(false);
		}
		else {
			txtDriverJarPath.setEnabled(true);
			btnDriverJarSelect.setEnabled(true);
		}
		this.setComplete(true);
	}

	@Override
	public void applyState() throws InvalidStateException {
		viewToModel(DATABASE_ADAPTER, cmbAdapter);
		viewToModel(DATABASE_DRIVERJAR, txtDriverJarPath);
		viewToModel(DATABASE_SERVER, txtHost);
		viewToModel(DATABASE_PORT, txtPort);
		viewToModel(DATABASE_NAME, txtDbName);
		viewToModel(DATABASE_USERNAME, txtDbUsername);
		validatePasswordEquality(txtDbPassword1, txtDbPassword2, "gui.wizard.usedb.dbpassword1.label");
		viewToModel(DATABASE_PASSWORD, txtDbPassword1);
		viewToModel(DATABASE_PASSWORD, txtDbPassword2);
		viewToModel(DATABASE_SCHEMA, txtSchema);
		viewToModel(DATABASE_TABLESPACE, txtTablespace);
		viewToModel(DATABASE_TABLESPACEINDEX, txtTablespaceIndex);
	}

	private class SelectJarActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new java.io.File("."));
			chooser.setDialogTitle(L10n.getMessage("gui.wizard.path.filechooser.title"));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

	        int returnVal = chooser.showOpenDialog(UseDatabaseWizardStep.this);

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	        	File file = chooser.getSelectedFile();
	        	txtDriverJarPath.setText(file.getAbsolutePath());
	        }
		}
	}
}
