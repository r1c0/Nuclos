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
 * Oval shape.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
package org.nuclos.client.gef.shapes;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class OvalShape extends AbstractShape {
	protected double centerX;
	protected double centerY;
	protected double horizontalRadius;
	protected double verticalRadius;

	/**
	 *
	 * @param centerX
	 * @param centerY
	 * @param horzRadius
	 * @param vertRadius
	 */
	public OvalShape(double centerX, double centerY, double horzRadius, double vertRadius) {
		this.centerX = centerX;
		this.centerY = centerY;
		this.horizontalRadius = horzRadius;
		this.verticalRadius = vertRadius;

		this.dimension.setRect(centerX - horzRadius / 2, centerY - vertRadius / 2,
				horzRadius * 2, vertRadius * 2);
	}

	/**
	 *
	 * @param p
	 * @return
	 */
	@Override
	public boolean isPointInside(Point2D p) {
		Ellipse2D shape = new Ellipse2D.Double(dimension.getX(), dimension.getY(),
				dimension.getWidth(), dimension.getHeight());
		return shape.contains(p);
	}

	/**
	 *
	 * @param gfx
	 */
	@Override
	public void paint(Graphics2D gfx) {
		Ellipse2D shape = new Ellipse2D.Double(dimension.getX(), dimension.getY(),
				dimension.getWidth(), dimension.getHeight());
		gfx.setPaint(bgColor);
		gfx.fill(shape);
		if (paintBorder) {
			Stroke s = new BasicStroke((float) borderSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
			gfx.setStroke(s);
			gfx.setPaint(borderColor);
			gfx.draw(shape);
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

		this.centerX = dimension.getX() + dimension.getWidth() / 2;
		this.centerY = dimension.getY() + dimension.getHeight() / 2;
		this.horizontalRadius = dimension.getWidth() / 2;
		this.verticalRadius = dimension.getHeight() / 2;
	}
}
