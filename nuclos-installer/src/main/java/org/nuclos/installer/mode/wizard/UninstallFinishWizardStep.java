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

import javax.swing.JLabel;

import org.nuclos.installer.L10n;
import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.WizardModel;

/**
 * Finish step.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 */
public class UninstallFinishWizardStep extends AbstractWizardStep {

	private static double layout[][] = {
        { 20.0, TableLayout.FILL }, // Columns
        { 20.0, 20.0, TableLayout.FILL } // Rows
    };

	public UninstallFinishWizardStep() {
		super(L10n.getMessage("gui.wizard.finish.title"), L10n.getMessage("gui.wizard.finish.description"));
	}

	@Override
	public void init(WizardModel model) {
		super.init(model);
		TableLayout layout = new TableLayout(this.layout);
        layout.setVGap(5);
        layout.setHGap(5);
        this.setLayout(layout);

        JLabel label = new JLabel();
        label.setText(L10n.getMessage("uninstallation.finished"));
		this.add(label, "0,0 1,0");
	}

	@Override
	public void prepare() {
		super.prepare();
	}

	@Override
	public void applyState() throws InvalidStateException { }

	@Override
	protected void updateState() { }

}
