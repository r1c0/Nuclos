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
package org.nuclos.client.datasource.querybuilder.shapes.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import org.nuclos.client.datasource.querybuilder.QueryBuilderIcons;

public class TableHeader extends JLabel {
	public TableHeader(String title) {
		super(title);
		setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
		setFont(new Font("Tahoma", Font.TRUETYPE_FONT | Font.BOLD, 12));
		setVerticalAlignment(JLabel.CENTER);
		setIcon(QueryBuilderIcons.iconTable16);
		setIconTextGap(2);
		setBackground(Color.LIGHT_GRAY);
	}
	
	@Override
	public void paint(Graphics g) {
		// Workaround for Bug in Synthetica-L&F which
		// doesn't paint the background correctly.
		g.setColor(getBackground());
		Dimension d = getSize();
		g.fillRect(0, 0, d.width, d.height);
		super.paint(g);
	}

	public void setSelection(boolean value) {
		if (value) {
			setBackground(Color.GRAY);
			setForeground(Color.WHITE);
		}
		else {
			setBackground(Color.LIGHT_GRAY);
			setForeground(Color.BLACK);
		}
		repaint();
	}
}
