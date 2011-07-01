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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import org.nuclos.client.gef.math.Vector2d;

/**
 * Arrow connector.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class ArrowConnector extends AbstractConnector {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Point2D pRightArrow = new Point2D.Double();
	protected Point2D pLeftArrow = new Point2D.Double();
	protected float dArrowWidth = 12f;
	protected float dArrowHeight = 4.5f;
	protected double dArrowAngle = 0d;
	GeneralPath leftArrow = null;
	GeneralPath rightArrow = null;

	/**
	 *
	 */
	public ArrowConnector() {
		super();
		initPath();
	}

	/**
	 *
	 * @param startPoint
	 * @param endPoint
	 */
	public ArrowConnector(Point2D startPoint, Point2D endPoint) {
		super(startPoint, endPoint);
		initPath();
		computeArrow();
	}

	/**
	 *
	 * @param src
	 * @param dst
	 */
	public ArrowConnector(ConnectionPoint src, ConnectionPoint dst) {
		super(src, dst);
		initPath();
		computeArrow();
	}

	protected void initPath() {
		rightArrow = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		rightArrow.moveTo(0f, 0f);
		rightArrow.lineTo(-dArrowWidth, dArrowHeight);
		rightArrow.lineTo(-dArrowWidth, -dArrowHeight);
		rightArrow.closePath();

		leftArrow = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		leftArrow.moveTo(0f, 0f);
		leftArrow.lineTo(dArrowWidth, dArrowHeight);
		leftArrow.lineTo(dArrowWidth, -dArrowHeight);
		leftArrow.closePath();
	}

	@Override
	public void setStartPoint(Point2D startPoint) {
		super.setStartPoint(startPoint);
		computeArrow();
	}

	@Override
	public void setEndPoint(Point2D endPoint) {
		super.setEndPoint(endPoint);
		computeArrow();
	}

	public void computeArrow() {
		if (view == null) {
			return;
		}

		double viewHeight = getView().getHeight();
		if (viewHeight <= 0d) {
			viewHeight = 1024d;
		}
		Vector2d xAxis = new Vector2d(1d, 0d);
		Point2D pStart = new Point2D.Double(startPoint.getX(), Math.abs(startPoint.getY() - viewHeight));
		Point2D pEnd = new Point2D.Double(endPoint.getX(), Math.abs(endPoint.getY() - viewHeight));

		Vector2d connector;
		if (endPoint.getY() < startPoint.getY()) {
			connector = Vector2d.vecSubtract(pStart, pEnd);
		}
		else {
			connector = Vector2d.vecSubtract(pEnd, pStart);
		}

		dArrowAngle = Vector2d.getAngle(connector, xAxis);
	}

	@Override
	public void paint(Graphics2D gfx) {
		super.paint(gfx);
		if (dArrowAngle >= 0d && dArrowAngle <= Math.PI) {
			AffineTransform oldTransform = gfx.getTransform();
			gfx.setPaint(Color.BLACK);
			AffineTransform at = new AffineTransform(oldTransform);
			at.concatenate(AffineTransform.getTranslateInstance(endPoint.getX(), endPoint.getY()));
			at.concatenate(AffineTransform.getRotateInstance(dArrowAngle));
			gfx.setTransform(at);
			gfx.fill(endPoint.getY() < startPoint.getY() ? leftArrow : rightArrow);
			gfx.setTransform(oldTransform);
		}
	}
}
