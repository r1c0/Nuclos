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

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.nuclos.installer.ConfigContext;
import org.nuclos.installer.InstallException;
import org.nuclos.installer.L10n;
import org.pietschy.wizard.InvalidStateException;


public class ClientConfigurationWizardStep extends AbstractWizardStep {

	private JCheckBox chkSingleInstance = new JCheckBox();

	private static double	layout[][]			= {
		{ 20.0, TableLayout.FILL, 100.0 }, // Columns
		{ 20.0, 20.0, 20.0, 20.0, 20.0, TableLayout.FILL } };		// Rows

	public ClientConfigurationWizardStep() {
		super(L10n.getMessage("gui.wizard.client.title"), L10n.getMessage("gui.wizard.client.description"));

		TableLayout layout = new TableLayout(this.layout);
		layout.setVGap(5);
		layout.setHGap(5);
		this.setLayout(layout);

		JLabel label = new JLabel();
		label.setText(L10n.getMessage("gui.wizard.client.singleinstance.label"));
		this.add(label, "1,0");

		this.chkSingleInstance.addActionListener(this);
		this.add(chkSingleInstance, "0,0");
	}

	@Override
	public void prepare() {
		modelToView(CLIENT_SINGLEINSTANCE, chkSingleInstance);
		setComplete(true);
	}

	@Override
	protected void updateState() {
		setComplete(true);
	}

	@Override
	public void applyState() throws InvalidStateException {
		viewToModel(CLIENT_SINGLEINSTANCE, chkSingleInstance);
		try {
			ConfigContext.getCurrentConfig().setDerivedProperties();
			ConfigContext.getCurrentConfig().verify();
			setComplete(true);
		}
		catch(InstallException e) {
			throw new InvalidStateException(e.getLocalizedMessage());
		}
	}

}
