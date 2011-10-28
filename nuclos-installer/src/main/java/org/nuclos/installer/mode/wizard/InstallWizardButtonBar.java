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

import javax.swing.JButton;

import org.pietschy.wizard.ButtonBar;
import org.pietschy.wizard.Wizard;

/**
* <br>
* Created by Novabit Informationssysteme GmbH <br>
* Please visit <a href="http://www.novabit.de">www.novabit.de</a>
*/
public class InstallWizardButtonBar extends ButtonBar {

	public InstallWizardButtonBar(Wizard wizard) {
		super(wizard);
	}

	@Override
	protected void layoutButtons(JButton helpButton, final JButton previousButton, JButton nextButton, JButton lastButton, JButton finishButton, JButton cancelButton, JButton closeButton) {
		super.layoutButtons(helpButton, previousButton, nextButton, lastButton, finishButton, cancelButton, closeButton);
		lastButton.setVisible(false);
		finishButton.setVisible(false);
		helpButton.setVisible(false);
	}
}
