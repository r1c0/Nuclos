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

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLEditorKit;

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
public class SummaryWizardStep extends AbstractWizardStep {

	private JEditorPane pane = new JEditorPane();

	private static double layout[][] = {
        { TableLayout.FILL }, // Columns
        { TableLayout.FILL } // Rows
    };

	public SummaryWizardStep() {
		super(L10n.getMessage("gui.wizard.summary.title"), L10n.getMessage("gui.wizard.summary.description"));
	}

	@Override
	public void init(WizardModel model) {
		super.init(model);
		TableLayout layout = new TableLayout(this.layout);
        layout.setVGap(5);
        layout.setHGap(5);
        this.setLayout(layout);

        pane.setEditable(false);
        pane.setContentType("text/html");
        pane.setEditorKit(new HTMLEditorKit());

		final JScrollPane scrlpane = new JScrollPane();
		scrlpane.getViewport().add(pane, null);
		this.add(scrlpane, "0,0");
	}

	@Override
	public void prepare() {
		setComplete(true);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					if ("use".equals(ConfigContext.getProperty(DATABASE_SETUP))) {
						pane.setText(L10n.getLocalizedHtml("org/nuclos/installer/resource/summary-default", ConfigContext.getCurrentConfig()));
					}
					else if ("setup".equals(ConfigContext.getProperty(DATABASE_SETUP))) {
						pane.setText(L10n.getLocalizedHtml("org/nuclos/installer/resource/summary-dbsetup", ConfigContext.getCurrentConfig()));
					}
					else if ("install".equals(ConfigContext.getProperty(DATABASE_SETUP))) {
						pane.setText(L10n.getLocalizedHtml("org/nuclos/installer/resource/summary-dbinstall", ConfigContext.getCurrentConfig()));
					}
				}
				catch(IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	@Override
	protected void updateState() { }

	@Override
	public void applyState() throws InvalidStateException { }
}
