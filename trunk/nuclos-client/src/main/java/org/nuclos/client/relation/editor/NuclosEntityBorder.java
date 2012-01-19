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
package org.nuclos.client.relation.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.io.Serializable;

import javax.swing.border.Border;

/**
 * Border for Statemodel and EntityRelationship
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:marc.finke@nuclos.de">Marc Finke</a>
 * @version 01.00.00
 */

public class NuclosEntityBorder implements Border, Serializable {

	private Insets insets;

	private final static NuclosEntityBorder instance = new NuclosEntityBorder();
	
	public static NuclosEntityBorder getInstance()	{
		return instance;
	}

	/*
	 * Create only one instance
	 */
	private NuclosEntityBorder() {
		insets = new Insets(0, 0, 2, 2);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.border.Border#getBorderInsets(java.awt.Component)
	 */
	@Override
	public Insets getBorderInsets(Component c) {
		return insets;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.border.Border#isBorderOpaque()
	 */
	@Override
	public boolean isBorderOpaque() {
		return false;
	}

	/**
	 * paint the border
	 */
	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
		Color bg = c.getBackground();

		if (c.getParent() != null)	{
			bg = c.getParent().getBackground();
		}

		if (bg != null) {
			Color mid = bg.darker();
			Color edge = createEdgeColor(mid, bg);

			g.setColor(bg);
			g.drawLine(0, h - 2, w, h - 2);
			g.drawLine(0, h - 1, w, h - 1);
			g.drawLine(w - 2, 0, w - 2, h);
			g.drawLine(w - 1, 0, w - 1, h);

			g.setColor(mid);
			g.drawLine(1, h - 2, w - 2, h - 2);
			g.drawLine(w - 2, 1, w - 2, h - 2);

			g.setColor(edge);
			g.drawLine(2, h - 1, w - 2, h - 1);
			g.drawLine(w - 1, 2, w - 1, h - 2);
		}
	}

	/**
	 * create color of two
	 * @param c1
	 * @param c2
	 * @return
	 */
	private static Color createEdgeColor(Color c1, Color c2) {
		int red = c1.getRed() + (c2.getRed() - c1.getRed()) / 2;
		int green = c1.getGreen() + (c2.getGreen() - c1.getGreen()) / 2;
		int blue = c1.getBlue() + (c2.getBlue() - c1.getBlue()) / 2;
		return new Color(red, green, blue);
	}
	
}
