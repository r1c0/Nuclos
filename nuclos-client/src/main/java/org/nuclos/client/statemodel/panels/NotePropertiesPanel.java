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
package org.nuclos.client.statemodel.panels;

import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.client.statemodel.models.NotePropertiesPanelModel;
import javax.swing.*;
import java.awt.*;

/**
 * Panel containing the properties of a note.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph Radig</a>
 * @version 01.00.00
 */
public class NotePropertiesPanel extends JPanel {

	private final NotePropertiesPanelModel model = new NotePropertiesPanelModel();

	public NotePropertiesPanel() {
		super(new BorderLayout(4, 4));

		this.init();
	}

	private void init() {
		final JPanel pnlBg = new JPanel();
		pnlBg.setLayout(new GridBagLayout());

		final JLabel labText = new JLabel(CommonLocaleDelegate.getInstance().getMessage(
				"NotePropertiesPanel.2", "Text"));
		labText.setAlignmentY((float) 0.0);
		labText.setHorizontalAlignment(SwingConstants.LEADING);
		labText.setHorizontalTextPosition(SwingConstants.TRAILING);
		labText.setVerticalAlignment(SwingConstants.CENTER);
		labText.setVerticalTextPosition(SwingConstants.CENTER);

		final JTextArea taText = new JTextArea();
		taText.setAlignmentX((float) 0.0);
		taText.setAlignmentY((float) 0.0);
		taText.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
		taText.setText("");
		taText.setDocument(model.docText);
		taText.setLineWrap(true);

		final JScrollPane scrlpn = new JScrollPane();
		scrlpn.getViewport().add(taText);
		scrlpn.setAutoscrolls(true);
		scrlpn.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrlpn.setPreferredSize(new Dimension(100, 300));

		this.setAlignmentX((float) 0.0);
		this.setAlignmentY((float) 0.0);
		pnlBg.add(new JLabel(CommonLocaleDelegate.getInstance().getMessage(
				"NotePropertiesPanel.1", "Bemerkung")),
				new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(2, 2, 2, 0), 0, 0));
		pnlBg.add(labText,
				new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
						new Insets(3, 2, 0, 0), 0, 0));
		pnlBg.add(scrlpn,
				new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
						new Insets(2, 4, 0, 2), 0, 0));
		this.add(pnlBg, BorderLayout.NORTH);
	}

	public NotePropertiesPanelModel getModel() {
		return model;
	}

}	// class NotePropertiesPanel
