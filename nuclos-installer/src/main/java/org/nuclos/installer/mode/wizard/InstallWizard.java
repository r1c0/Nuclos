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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.nuclos.installer.ConfigContext;
import org.nuclos.installer.Constants;
import org.nuclos.installer.util.ProcessCommand;
import org.pietschy.wizard.ButtonBar;
import org.pietschy.wizard.Wizard;
import org.pietschy.wizard.WizardModel;

public class InstallWizard extends Wizard implements PropertyChangeListener {

	private static final Logger LOG = Logger.getLogger(InstallWizard.class);
	
	// 
	
	private final boolean isPrivileged;

	public InstallWizard(WizardModel model, boolean isPrivileged) {
		super(model);
		this.isPrivileged = isPrivileged;
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
				final URI uri;
				try {
					uri = new URI("http://localhost:" + ConfigContext.getProperty(Constants.HTTP_PORT) + "/"
							+ ConfigContext.getProperty(Constants.NUCLOS_INSTANCE));
				}
				catch (URISyntaxException e) {
					throw new IllegalArgumentException("Wrong URL", e);
				}
				LOG.info("URL to open Nuclos is '" + uri + "'");
				if (fws.isOpenWebstart()) {
					if (Desktop.isDesktopSupported()) {
			            Desktop desktop = Desktop.getDesktop();
			            try {
							desktop.browse(uri);
							LOG.info("Browser has started with URL " + uri);
						} catch (Exception ex) {
							LOG.warn("Browser start failed", ex);
							// Fallback
							try {
								final Process p = new ProcessCommand().browse(uri, isPrivileged);
								if (p == null) {
									model.getCallback().info("error.open.webstart");
								}
								LOG.info("Fallback browser has started with URL " + uri);
							}
							catch (IOException e) {
								LOG.warn("Fallback browser start failed", e);
								model.getCallback().info("error.open.webstart");
							}
						}
			        }
					else {
						LOG.warn("Can't start browser; java Desktop API is not supported");
						// Fallback
						try {
							final Process p = new ProcessCommand().browse(uri, isPrivileged);
							if (p == null) {
								model.getCallback().info("error.open.webstart");
							}
							LOG.info("Fallback browser has started with URL " + uri);
						}
						catch (IOException e) {
							LOG.warn("Fallback browser start failed", e);
							model.getCallback().info("error.open.webstart");
						}
					}
				}
				else {
					LOG.info("Browser start is not requested by user");
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
