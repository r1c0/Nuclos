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
import org.pietschy.wizard.models.MultiPathModel;
import org.pietschy.wizard.models.Path;

/**
 * Model for the install wizard.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 */
public class InstallWizardModel extends MultiPathModel implements InstallerWizardModel {

	private final Unpacker unpacker;
	private final Installer callback;

	public InstallWizardModel(Path path, Unpacker unpacker, Installer callback) {
		super(path);
		this.unpacker = unpacker;
		this.callback = callback;
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
	public boolean isNextAvailable() {
		return super.isNextAvailable();
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
