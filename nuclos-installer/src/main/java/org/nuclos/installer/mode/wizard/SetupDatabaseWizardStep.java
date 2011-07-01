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

import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.nuclos.installer.ConfigContext;
import org.nuclos.installer.Constants;
import org.nuclos.installer.L10n;
import org.nuclos.installer.database.PostgresDbSetup;
import org.pietschy.wizard.InvalidStateException;

/**
 * Set target installation path
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 */
public class SetupDatabaseWizardStep extends AbstractWizardStep implements ActionListener, Constants {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JTextField txtHost = new JTextField();

	private JTextField txtPort = new JTextField();

	private JTextField txtPgUser = new JTextField();

	private JPasswordField txtPgPassword1 = new JPasswordField();
	private JPasswordField txtPgPassword2 = new JPasswordField();

	private JTextField txtDbName = new JTextField();

	private JTextField txtDbUsername = new JTextField();

	private JPasswordField txtDbPassword1 = new JPasswordField();
	private JPasswordField txtDbPassword2 = new JPasswordField();

	private JTextField txtSchema = new JTextField();

	private JTextField txtTablespace = new JTextField();

	private JTextField txtPgTablespacePath = new JTextField();

	private static double layout[][] = {
        { 200.0, TableLayout.FILL, 100.0 }, // Columns
        { 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, TableLayout.FILL } };// Rows

	public SetupDatabaseWizardStep() {
		super(L10n.getMessage("gui.wizard.setupdb.title"), L10n.getMessage("gui.wizard.setupdb.description"));

		TableLayout layout = new TableLayout(this.layout);
        layout.setVGap(5);
        layout.setHGap(5);
        this.setLayout(layout);

        JLabel label = new JLabel();
        label.setText(L10n.getMessage("gui.wizard.setupdb.host.label"));
        this.add(label, "0,0");

        txtHost.getDocument().addDocumentListener(this);
        this.add(txtHost, "1,0");

        label = new JLabel();
        label.setText(L10n.getMessage("gui.wizard.setupdb.port.label"));
        this.add(label, "0,1");

        txtPort.getDocument().addDocumentListener(this);
        this.add(txtPort, "1,1");

        label = new JLabel();
        label.setText(L10n.getMessage("gui.wizard.setupdb.superuser.label"));
        this.add(label, "0,2");

        txtPgUser.getDocument().addDocumentListener(this);
        this.add(txtPgUser, "1,2");

        label = new JLabel();
        label.setText(L10n.getMessage("gui.wizard.installdb.pgpassword1.label"));
        this.add(label, "0,3");

        txtPgPassword1.getDocument().addDocumentListener(this);
        this.add(txtPgPassword1, "1,3");

        label = new JLabel();
        label.setText(L10n.getMessage("gui.wizard.installdb.pgpassword2.label"));
        this.add(label, "0,4");

        txtPgPassword2.getDocument().addDocumentListener(this);
        this.add(txtPgPassword2, "1,4");

        label = new JLabel();
        label.setText(L10n.getMessage("gui.wizard.installdb.dbname.label"));
        this.add(label, "0,5");

        txtDbName.getDocument().addDocumentListener(this);
        this.add(txtDbName, "1,5");

        label = new JLabel();
        label.setText(L10n.getMessage("gui.wizard.installdb.dbuser.label"));
        this.add(label, "0,6");

        txtDbUsername.getDocument().addDocumentListener(this);
        this.add(txtDbUsername, "1,6");

        label = new JLabel();
        label.setText(L10n.getMessage("gui.wizard.installdb.dbpassword1.label"));
        this.add(label, "0,7");

        txtDbPassword1.getDocument().addDocumentListener(this);
        this.add(txtDbPassword1, "1,7");

        label = new JLabel();
        label.setText(L10n.getMessage("gui.wizard.installdb.dbpassword2.label"));
        this.add(label, "0,8");

        txtDbPassword2.getDocument().addDocumentListener(this);
        this.add(txtDbPassword2, "1,8");

        label = new JLabel();
        label.setText(L10n.getMessage("gui.wizard.installdb.schema.label"));
        this.add(label, "0,9");

        txtSchema.getDocument().addDocumentListener(this);
        this.add(txtSchema, "1,9");

        label = new JLabel();
        label.setText(L10n.getMessage("gui.wizard.installdb.tablespace.label"));
        this.add(label, "0,10");

        txtTablespace.getDocument().addDocumentListener(this);
        this.add(txtTablespace, "1,10");

        label = new JLabel();
        label.setText(L10n.getMessage("gui.wizard.installdb.pgtablespacepath.label"));
        this.add(label, "0,11");

        this.txtPgTablespacePath.getDocument().addDocumentListener(this);
		this.add(txtPgTablespacePath, "1,11");
	}

	@Override
	public void prepare() {
		modelToView(DATABASE_SERVER, txtHost);
		modelToView(DATABASE_PORT, txtPort);
		modelToView(POSTGRES_SUPERUSER, txtPgUser);
		modelToView(POSTGRES_SUPERPWD, txtPgPassword1);
		modelToView(POSTGRES_SUPERPWD, txtPgPassword2);
		modelToView(DATABASE_NAME, txtDbName);
		modelToView(DATABASE_USERNAME, txtDbUsername);
		modelToView(DATABASE_PASSWORD, txtDbPassword1);
		modelToView(DATABASE_PASSWORD, txtDbPassword2);
		modelToView(DATABASE_SCHEMA, txtSchema);
		modelToView(DATABASE_TABLESPACE, txtTablespace);
		modelToView(POSTGRES_TABLESPACEPATH, txtPgTablespacePath);
		this.setComplete(true);
	}

	@Override
	protected void updateState() {
		this.setComplete(true);
	}

	@Override
	public void applyState() throws InvalidStateException {
		viewToModel(DATABASE_SERVER, txtHost);
		viewToModel(DATABASE_PORT, txtPort);
		viewToModel(POSTGRES_SUPERUSER, txtPgUser);
		validatePasswordEquality(txtPgPassword1, txtPgPassword2, "gui.wizard.installdb.pgpassword1.label");
		viewToModel(POSTGRES_SUPERPWD, txtPgPassword1);
		viewToModel(DATABASE_NAME, txtDbName);
		viewToModel(DATABASE_USERNAME, txtDbUsername);
		validatePasswordEquality(txtDbPassword1, txtDbPassword2, "gui.wizard.installdb.dbpassword1.label");
		viewToModel(DATABASE_PASSWORD, txtDbPassword1);
		viewToModel(DATABASE_SCHEMA, txtSchema);
		viewToModel(DATABASE_TABLESPACE, txtTablespace);
		viewToModel(POSTGRES_TABLESPACEPATH, txtPgTablespacePath);

		PostgresDbSetup dbSetup = new PostgresDbSetup();
		String superuser = ConfigContext.getProperty(POSTGRES_SUPERUSER);
		String superpwd = ConfigContext.getProperty(Constants.POSTGRES_SUPERPWD);

		try {
			dbSetup.init(ConfigContext.getCurrentConfig(), superuser, superpwd);
			dbSetup.checkConnection();
		}
		catch (Exception ex) {
			throw new InvalidStateException(L10n.getMessage("validation.invalid.pg.connection"));
		}
	}
}
