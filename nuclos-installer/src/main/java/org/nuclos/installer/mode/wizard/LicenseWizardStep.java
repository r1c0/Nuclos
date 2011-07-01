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

import javax.swing.ButtonGroup;
import javax.swing.JEditorPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLEditorKit;

import org.nuclos.installer.L10n;
import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.WizardModel;

/**
 * Show and accept Nuclos license.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 */
public class LicenseWizardStep extends AbstractWizardStep {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JEditorPane pane = new JEditorPane();

	private JRadioButton accept = new JRadioButton();
	private JRadioButton decline = new JRadioButton();

	private ButtonGroup group = new ButtonGroup();

	private static double layout[][] = {
        { TableLayout.FILL }, // Columns
        { TableLayout.FILL , 20.0, 20.0} // Rows
    };

	public LicenseWizardStep() {
		super(L10n.getMessage("gui.wizard.license.title"), L10n.getMessage("gui.wizard.license.description"));
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
        try {
			pane.setPage(getClass().getClassLoader().getResource("org/nuclos/installer/resource/nuclos-license.html"));
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
		pane.addHyperlinkListener(new DefaultHyperLinkListener());

		final JScrollPane scrlpane = new JScrollPane();
		scrlpane.getViewport().add(pane, null);
		this.add(scrlpane, "0,0");

		accept.setText(L10n.getMessage("gui.wizard.license.agree"));
		accept.addActionListener(this);
		decline.setText(L10n.getMessage("gui.wizard.license.decline"));
		decline.addActionListener(this);
		decline.setSelected(true);

		group.add(accept);
		group.add(decline);

		this.add(accept, "0,1");
		this.add(decline, "0,2");
	}

	@Override
	public void prepare() {
		super.prepare();
	}


	@Override
	public void applyState() throws InvalidStateException { }

	@Override
	protected void updateState() {
		setComplete(accept.isSelected());
	}

}
