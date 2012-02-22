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
package org.nuclos.client.datasource.querybuilder.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.nuclos.common2.SpringLocaleDelegate;

/**
 * @todo enter class description.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class LabelPanel extends JPanel {
	
	private String[] labels = {
			SpringLocaleDelegate.getInstance().getMessage("LabelPanel.8","Tabelle"), 
			SpringLocaleDelegate.getInstance().getMessage("LabelPanel.7","Spalte"), 
			SpringLocaleDelegate.getInstance().getMessage("LabelPanel.1","Alias"), 
			SpringLocaleDelegate.getInstance().getMessage("LabelPanel.5","Sichtbar"), 
			SpringLocaleDelegate.getInstance().getMessage("LabelPanel.3","Gruppierung"), 
			SpringLocaleDelegate.getInstance().getMessage("LabelPanel.6","Sortierung"), 
			SpringLocaleDelegate.getInstance().getMessage("LabelPanel.2","Bedingung"), 
			SpringLocaleDelegate.getInstance().getMessage("LabelPanel.4","oder")};
	
	private ColumnSelectionTable table;
	
	public LabelPanel(ColumnSelectionTable table) {
		this.table = table;

		setLayout(null);
		for (int i = 0; i < labels.length; i++) {
			JLabel label = new JLabel(labels[i]);
			label.setPreferredSize(new Dimension(100, 14));
			add(label);
		}
	}
	
	@Override
	public void doLayout() {
		setPreferredSize(new Dimension(80, table.getHeight()));
		for (int i = 0; i < getComponentCount(); i++) {
			Component comp = getComponent(i);
			Rectangle cellRect = table.getCellRect(i, 0, true);
			comp.setBounds(new Rectangle(cellRect.x + 4, cellRect.y, 80, cellRect.height));
		}
	}
}
