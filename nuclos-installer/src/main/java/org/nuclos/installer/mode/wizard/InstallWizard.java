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

import java.awt.Desktop;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;

import org.nuclos.installer.ConfigContext;
import org.nuclos.installer.Constants;
import org.pietschy.wizard.ButtonBar;
import org.pietschy.wizard.Wizard;
import org.pietschy.wizard.WizardModel;

public class InstallWizard extends Wizard implements PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InstallWizard(WizardModel model) {
		super(model);
		getModel().addPropertyChangeListener("activeStep", this);
	}

	@Override
	public boolean isOverviewVisible() {
		return true;
	}

	@Override
	protected ButtonBar createButtonBar() {
		return new InstallWizardButtonBar(this);
	}

	@Override
	public void cancel() {
		if (getModel() instanceof InstallerWizardModel) {
			((InstallerWizardModel)getModel()).getCallback().cancel();
		}
		else {
			super.cancel();
		}
	}

	@Override
	public void close() {
		if (getModel() instanceof InstallerWizardModel) {
			InstallerWizardModel model = (InstallerWizardModel)getModel();
			if (getModel().getActiveStep() instanceof FinishWizardStep) {
				FinishWizardStep fws = (FinishWizardStep)getModel().getActiveStep();
				if (fws.isOpenWebstart()) {
					if (Desktop.isDesktopSupported()) {
			            Desktop desktop = Desktop.getDesktop();
			            try {
							desktop.browse(new URI("http://localhost:" + ConfigContext.getProperty(Constants.HTTP_PORT) + "/"
									+ ConfigContext.getProperty(Constants.NUCLOS_INSTANCE)));
						} catch (Exception ex) {
							model.getCallback().info("error.open.webstart");
						}
			        }
				}
			}
			model.getCallback().close();
		}
		else {
			super.close();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getNewValue() != null && evt.getNewValue() instanceof FinishWizardStep) {
			showCloseButton();
		}
	}
}
