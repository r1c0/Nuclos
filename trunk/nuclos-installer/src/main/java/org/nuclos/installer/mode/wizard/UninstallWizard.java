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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;

import org.pietschy.wizard.ButtonBar;
import org.pietschy.wizard.Wizard;
import org.pietschy.wizard.WizardModel;

public class UninstallWizard extends Wizard implements PropertyChangeListener {

	private final JFrame frame;

	public UninstallWizard(WizardModel model, JFrame frame) {
		super(model);
		this.frame = frame;
		getModel().addPropertyChangeListener("activeStep", this);
		setOverviewVisible(false);
	}

	@Override
	protected ButtonBar createButtonBar() {
		return new InstallWizardButtonBar(this);
	}

	@Override
	public void cancel() {
		frame.setVisible(false);
		System.exit(0);
	}

	@Override
	public void close() {
		frame.setVisible(false);
		System.exit(0);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getNewValue() != null && evt.getNewValue() instanceof UninstallFinishWizardStep) {
			showCloseButton();
		}
	}
}
