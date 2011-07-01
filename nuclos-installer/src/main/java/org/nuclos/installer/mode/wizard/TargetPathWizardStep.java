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
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.nuclos.installer.ConfigContext;
import org.nuclos.installer.Constants;
import org.nuclos.installer.InstallException;
import org.nuclos.installer.L10n;
import org.pietschy.wizard.InvalidStateException;

/**
 * Set target installation path
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 */
public class TargetPathWizardStep extends AbstractWizardStep implements ActionListener, Constants {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txtTargetPath = new JTextField();
	private JButton btnSelectPath = new JButton();

	private static double layout[][] = {
        { TableLayout.FILL, 100.0 }, // Columns
        { 20.0, 20.0, TableLayout.FILL } };// Rows

	public TargetPathWizardStep() {
		super(L10n.getMessage("gui.wizard.path.title"), L10n.getMessage("gui.wizard.path.description"));

		TableLayout layout = new TableLayout(this.layout);
        layout.setVGap(5);
        layout.setHGap(5);
        this.setLayout(layout);

        JLabel label = new JLabel();
        label.setText(L10n.getMessage("gui.wizard.path.select.label"));
        this.add(label, "0,0, 1,0");

        this.txtTargetPath.getDocument().addDocumentListener(this);
		this.add(txtTargetPath, "0,1");

		btnSelectPath.setText(L10n.getMessage("filechooser.browse"));
		btnSelectPath.addActionListener(this);
        this.add(btnSelectPath, "1,1");
	}

	@Override
	public void prepare() {
		txtTargetPath.setText(ConfigContext.getCurrentConfig().getProperty(NUCLOS_HOME));
	}

	@Override
	protected void updateState() {
		if (txtTargetPath.getText() != null) {
			ConfigContext.getCurrentConfig().setProperty(NUCLOS_HOME, txtTargetPath.getText());
			setComplete(true);
		}
		else {
			setComplete(false);
		}
	}

	@Override
	public void applyState() throws InvalidStateException {
		validate(NUCLOS_HOME, txtTargetPath.getText());
		if (ConfigContext.isUpdate()) {
			getModel().getCallback().info("info.update.backup");
			try {
				getModel().getUnpacker().shutdown(getModel().getCallback());
			} catch (InstallException e) {
				throw new InvalidStateException(e.getLocalizedMessage());
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle(L10n.getMessage("gui.wizard.path.filechooser.title"));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int returnVal = chooser.showOpenDialog(this);

        //Process the results.
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            txtTargetPath.setText(file.getAbsolutePath());
        }
	}
}
