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

import org.nuclos.installer.mode.Installer;
import org.nuclos.installer.unpack.Unpacker;
import org.pietschy.wizard.models.StaticModel;

/**
 * Model for the install wizard.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 */
public class UninstallWizardModel extends StaticModel implements InstallerWizardModel {

	private final Unpacker unpacker;
	private final Installer callback;
	private final RemoveWizardStep removeWizardStep;

	public RemoveWizardStep getRemoveWizardStep() {
		return removeWizardStep;
	}

	public UninstallWizardModel(Unpacker unpacker, Installer callback) {
		super();
		this.unpacker = unpacker;
		this.callback = callback;
		this.add(new UninstallInformationWizardStep());
		this.removeWizardStep = new RemoveWizardStep();
		this.add(this.removeWizardStep);
		this.add(new UninstallFinishWizardStep());
	}

	@Override
	public Unpacker getUnpacker() {
		return this.unpacker;
	}

	@Override
	public Installer getCallback() {
		return this.callback;
	}

	@Override
	public boolean isLastVisible() {
		return false;
	}

	@Override
	public boolean isPreviousAvailable() {
		if (getActiveStep() instanceof UnpackWizardStep) {
			return false;
		}
		else {
			return super.isPreviousAvailable();
		}
	}
}
