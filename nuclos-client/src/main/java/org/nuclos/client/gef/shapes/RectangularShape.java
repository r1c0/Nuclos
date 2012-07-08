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
/**
 * Rectangular shape.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:boris.sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */

package org.nuclos.client.gef.shapes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

public class RectangularShape extends AbstractShape {

	protected double left;
	protected double top;
	protected double width;
	protected double height;

	/**
	 *
	 * @param left
	 * @param top
	 * @param width
	 * @param height
	 */
	public RectangularShape(double left, double top, double width, double height) {
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;

		setDimension(new Rectangle2D.Double(left, top, width, height));
		bgColor = Color.GRAY;
	}
	
	protected Color getBackground() {
		return bgColor;
	}

	/**
	 *
	 * @param gfx
	 */
	@Override
	public void paint(Graphics2D gfx) {
//		Color second = getSecondBackgroundColor();
//		if (second != null) {
//			gfx.setPaint(new GradientPaint(new Double(left).intValue(), new Double(top+height/2).intValue(), bgColor, new Double(left).intValue(), new Double(top+height).intValue(), second));
//		} else {
			gfx.setPaint(getBackground());
//		}
		gfx.fill(dimension);
		if (paintBorder) {
			Stroke borderStroke = new BasicStroke((float) borderSize);
			gfx.setStroke(borderStroke);
			if (bSelected) {
				gfx.setColor(Color.BLUE);
			}
			else {
				gfx.setColor(borderColor);
			}
			gfx.draw(dimension);
		}
		super.paint(gfx);
	}

	/**
	 *
	 * @param dimension
	 */
	@Override
	public void setDimension(Rectangle2D dimension) {
		super.setDimension(dimension);
		left = dimension.getX();
		top = dimension.getY();
		width = dimension.getWidth();
		height = dimension.getHeight();
	}
}
