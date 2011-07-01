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
package org.nuclos.client.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.BevelBorder;

/**
 * A simple 1 line bevel border.
 * Adapted from javax.swing.border.BevelBorder, which is a 2 line border (and looks ugly).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public final class BevelBorder1 extends javax.swing.border.AbstractBorder {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Raised bevel type.
	 */
	public static final int RAISED = BevelBorder.RAISED;

	/**
	 * Lowered bevel type.
	 */
	public static final int LOWERED = BevelBorder.LOWERED;

	/**
	 * the bevel type
	 */
	private final int iBevelType;

	/**
	 * the highlight color
	 */
	private final Color colorHighlight;

	/**
	 * the shadow color
	 */
	private final Color colorShadow;

	/**
	 * Creates a bevel border with the specified type and whose
	 * colors will be derived from the background color of the
	 * component passed into the paintBorder method.
	 * @param iBevelType the type of bevel for the border
	 * @precondition bevelTypeIsValid(iBevelType)
	 */
	public BevelBorder1(int iBevelType) {
		this(iBevelType, null, null);
	}

	/**
	 * Creates a bevel border with the specified type, highlight and
	 * shadow colors.
	 * @param iBevelType the type of bevel for the border
	 * @param colorHighlight the color to use for the bevel highlight. May be null.
	 * @param colorShadow the color to use for the bevel shadow. May be null.
	 */
	public BevelBorder1(int iBevelType, Color colorHighlight, Color colorShadow) {
		if (!bevelTypeIsValid(iBevelType)) {
			throw new IllegalArgumentException("iBevelType");
		}
		this.iBevelType = iBevelType;
		this.colorHighlight = colorHighlight;
		this.colorShadow = colorShadow;
	}

	/**
	 * defines the valid bevel types.
	 * @return is bevel type iBevelType valid?
	 */
	public static boolean bevelTypeIsValid(int iBevelType) {
		return (iBevelType == RAISED || iBevelType == LOWERED);
	}

	/**
	 * Paints the border for the specified component with the specified
	 * position and size.
	 * @param c the component for which this border is being painted
	 * @param g the paint graphics
	 * @param x the x position of the painted border
	 * @param y the y position of the painted border
	 * @param width the width of the painted border
	 * @param height the height of the painted border
	 */
	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		switch (iBevelType) {
			case RAISED:
				paintRaisedBevel(c, g, x, y, width, height);
				break;

			case LOWERED:
				paintLoweredBevel(c, g, x, y, width, height);
				break;

			default:
				throw new IllegalStateException("iBevelType");
		}  // switch
	}

	/**
	 * Returns the insets of the border.
	 * @param c the component for which this border insets value applies. Ignored.
	 */
	@Override
	public Insets getBorderInsets(Component c) {
		final Insets insets = new Insets(1, 1, 1, 1);

		return insets;
	}

	/**
	 * Reinitialize the insets parameter with this Border's current Insets.
	 * @param c the component for which this border insets value applies. Ignored.
	 * @param insets the object to be reinitialized.
	 */
	@Override
	public Insets getBorderInsets(Component c, Insets insets) {
		if (insets == null) {
			throw new IllegalArgumentException("insets");
		}
		insets.left = insets.top = insets.right = insets.bottom = 1;

		return insets;
	}

	/**
	 * Returns the highlight color of the bevel border.
	 * @precondition (colorHighlight == null) --> (comp != null)
	 */
	public final Color getHighlightColor(Component comp) {
		if (!(!(colorHighlight == null) || comp != null)) {
			throw new IllegalArgumentException("comp");
		}

		return (colorHighlight != null) ? colorHighlight : comp.getBackground().brighter();
	}

	/**
	 * Returns the inner shadow color of the bevel border.
	 * @precondition (colorShadow == null) --> (comp != null)
	 */
	public Color getShadowColor(Component comp) {
		if (!(!(colorShadow == null) || comp != null)) {
			throw new IllegalArgumentException("comp");
		}

		return (colorShadow != null) ? colorShadow : comp.getBackground().darker();
	}

	/**
	 * Returns the type of the bevel border.
	 */
	public int getBevelType() {
		return iBevelType;
	}

	/**
	 * Returns whether or not the border is opaque.
	 */
	@Override
	public boolean isBorderOpaque() {
		return true;
	}

	void paintRaisedBevel(Component c, Graphics g, int x, int y, int width, int height) {
		Color oldColor = g.getColor();
		int h = height;
		int w = width;

		g.translate(x, y);

		g.setColor(getHighlightColor(c));
		g.drawLine(0, 0, 0, h - 1);
		g.drawLine(1, 0, w - 1, 0);

		g.setColor(getShadowColor(c));
		g.drawLine(1, h - 1, w - 1, h - 1);
		g.drawLine(w - 1, 1, w - 1, h - 2);

		g.translate(-x, -y);
		g.setColor(oldColor);
	}

	void paintLoweredBevel(Component c, Graphics g, int x, int y, int width, int height) {
		Color oldColor = g.getColor();
		int h = height;
		int w = width;

		g.translate(x, y);

		g.setColor(getShadowColor(c));
		g.drawLine(0, 0, 0, h - 1);
		g.drawLine(1, 0, w - 1, 0);

		g.setColor(getHighlightColor(c));
		g.drawLine(1, h - 1, w - 1, h - 1);
		g.drawLine(w - 1, 1, w - 1, h - 2);

		g.translate(-x, -y);
		g.setColor(oldColor);
	}

}
