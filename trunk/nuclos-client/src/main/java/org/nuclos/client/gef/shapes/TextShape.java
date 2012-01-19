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
package org.nuclos.client.gef.shapes;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

/**
 * Text shape.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class TextShape extends RectangularShape {

	protected String sText;
	protected AttributedString attrString;
	protected Font textFont = new Font("Arial", Font.PLAIN, 10);

	public TextShape() {
		this("", 0d, 0d, 0d, 0d);
	}

	/**
	 *
	 * @param sText
	 */
	public TextShape(String sText) {
		this(sText, 0d, 0d, 0d, 0d);
	}

	/**
	 *
	 * @param dX
	 * @param dY
	 * @param dWidth
	 * @param dHeight
	 */
	public TextShape(String sText, double dX, double dY, double dWidth, double dHeight) {
		super(dX, dY, dWidth, dHeight);
		setText(sText);
	}

	/**
	 *
	 * @return
	 */
	public String getText() {
		return sText;
	}

	/**
	 *
	 * @param sText
	 */
	public void setText(String sText) {
		this.sText = sText;
	}

	/**
	 *
	 * @return
	 */
	public Font getTextFont() {
		return textFont;
	}

	/**
	 *
	 * @param textFont
	 */
	public void setTextFont(Font textFont) {
		this.textFont = textFont;
	}

	/**
	 *
	 * @param gfx
	 */
	@Override
	public void paint(Graphics2D gfx) {
		double fX = 0d, fY = getY() + 4d;
		Shape oldClip = gfx.getClip();

		super.paint(gfx);
		if (sText.length() == 0) {
			return;
		}
		gfx.setClip(dimension);
		gfx.setFont(textFont);
		attrString = new AttributedString(sText);
		if (sText.length() > 0) {
			attrString.addAttribute(TextAttribute.FONT, textFont);
		}

		FontRenderContext frc = gfx.getFontRenderContext();
		AttributedCharacterIterator paragraph = attrString.getIterator();
		LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(paragraph, frc);

		lineMeasurer.setPosition(0);
		while (lineMeasurer.getPosition() < paragraph.getEndIndex()) {
			TextLayout layout = lineMeasurer.nextLayout((int) getWidth());
			// Move y-coordinate by the ascent of the layout.
			fY += layout.getAscent();

			// Compute pen x position.  If the paragraph is
			// right-to-left, we want to align the TextLayouts
			// to the right edge of the panel.
			if (layout.isLeftToRight()) {
				fX = getX() + 4;
			}
			else {
				fX = getX() + getWidth() - layout.getAdvance() + 4;
			}

			// Draw the TextLayout at (drawPosX, drawPosY).
			layout.draw(gfx, (float) fX, (float) fY);

			// Move y-coordinate in preparation for next layout.
			fY += layout.getDescent() + layout.getLeading();
		}
		gfx.setClip(oldClip);
	}
}
