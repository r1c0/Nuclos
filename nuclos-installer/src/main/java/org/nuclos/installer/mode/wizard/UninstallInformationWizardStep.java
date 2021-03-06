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

import java.io.IOException;

import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.log4j.Logger;
import org.nuclos.installer.ConfigContext;
import org.nuclos.installer.L10n;
import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.WizardModel;

/**
 * Show and accept Nuclos license.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 */
public class UninstallInformationWizardStep extends AbstractWizardStep {

	private static final Logger LOG = Logger.getLogger(UninstallInformationWizardStep.class);

	private JEditorPane pane = new JEditorPane();

	private JCheckBox chkRemoveDataAndLogs = new JCheckBox();

	private static double layout[][] = {
        { TableLayout.FILL }, // Columns
        { TableLayout.FILL, 20.0 } // Rows
    };

	public UninstallInformationWizardStep() {
		super(L10n.getMessage("gui.wizard.uninstall.title"), L10n.getMessage("gui.wizard.uninstall.description"));
	}

	@Override
	public void init(WizardModel arg0) {
		super.init(arg0);
		TableLayout layout = new TableLayout(this.layout);
        layout.setVGap(5);
        layout.setHGap(5);
        this.setLayout(layout);

        pane.setEditable(false);
        pane.setContentType("text/html");
        pane.setEditorKit(new HTMLEditorKit());
        try {
        	pane.setText(L10n.getLocalizedHtml("org/nuclos/installer/resource/nuclos-uninstall", ConfigContext.getCurrentConfig()));
		}
        catch(IOException e) {
			throw new RuntimeException(e);
		}
		pane.addHyperlinkListener(new DefaultHyperLinkListener());

        this.add(pane, "0,0");

        this.chkRemoveDataAndLogs.setText(L10n.getMessage("gui.wizard.uninstall.removedataandlogs.label"));
        this.chkRemoveDataAndLogs.addActionListener(this);
        this.add(chkRemoveDataAndLogs, "0,1");
	}

	@Override
	public void prepare() {
		modelToView(UNINSTALL_REMOVEDATAANDLOGS, chkRemoveDataAndLogs);
	}

	@Override
	public void applyState() throws InvalidStateException {
		viewToModel(UNINSTALL_REMOVEDATAANDLOGS, chkRemoveDataAndLogs);
	}

	@Override
	protected void updateState() { }

	@Override
	public boolean isComplete() {
		return true;
	}
}
