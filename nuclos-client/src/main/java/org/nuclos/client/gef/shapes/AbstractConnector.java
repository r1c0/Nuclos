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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.nuclos.client.gef.Connectable;
import org.nuclos.client.gef.Shape;

/**
 * Abstract connector.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */

public abstract class AbstractConnector extends AbstractShape {

	public static final int STARTPOINT = 1;
	public static final int ENDPOINT = 2;
	public static final int HIT_NONE = 0;
	public static final int HIT_STARTPOINT = 1;
	public static final int HIT_ENDPOINT = 2;
	public static final int HIT_LINE = 4;

	protected ConnectionPoint srcConnection;
	protected ConnectionPoint dstConnection;
	protected Point2D startPoint = new Point2D.Double();
	protected Point2D endPoint = new Point2D.Double();
	double dCPIndicatorSize = 6d;
	protected Color paint = Color.BLACK;
	protected Stroke stroke = new BasicStroke(1f);

	public AbstractConnector() {
		this((ConnectionPoint) null, (ConnectionPoint) null);
		Point2D p = new Point2D.Double();
		setStartPoint(p);
		setEndPoint(p);
	}

	/**
	 *
	 * @param src
	 * @param dst
	 */
	public AbstractConnector(ConnectionPoint src, ConnectionPoint dst) {
		srcConnection = src;
		dstConnection = dst;

		if (src != null) {
			Shape srcShape = src.getTargetShape();
			setStartPoint(srcShape.getConnectionPoint(src.getTargetPoint()));
			((Connectable) srcShape).addConnector(this);
		}
		if (dst != null) {
			Shape dstShape = dst.getTargetShape();
			setEndPoint(dstShape.getConnectionPoint(dst.getTargetPoint()));
			((Connectable) dstShape).addConnector(this);
		}
	}

	/**
	 *
	 * @param startPoint
	 * @param endPoint
	 */
	public AbstractConnector(Point2D startPoint, Point2D endPoint) {
		setStartPoint(startPoint);
		setEndPoint(endPoint);
	}

	/**
	 *
	 * @return
	 */
	public ConnectionPoint getSourceConnection() {
		return srcConnection;
	}

	/**
	 *
	 * @param srcConnection
	 */
	public void setSourceConnection(ConnectionPoint srcConnection) {
		if (this.srcConnection != null && this.srcConnection.getTargetShape() != null) {
			((Connectable) this.srcConnection.getTargetShape()).removeConnector(this);
		}
		if (srcConnection != null) {
			setStartPoint(srcConnection.getTargetShape().getConnectionPoint(srcConnection.getTargetPoint()));
			((Connectable) srcConnection.getTargetShape()).addConnector(this);
		}
		this.srcConnection = srcConnection;
	}

	/**
	 *
	 * @return
	 */
	public ConnectionPoint getDestinationConnection() {
		return dstConnection;
	}

	/**
	 *
	 * @param dstConnection
	 */
	public void setDestinationConnection(ConnectionPoint dstConnection) {
		if (this.dstConnection != null && this.dstConnection.getTargetShape() != null) {
			((Connectable) this.dstConnection.getTargetShape()).removeConnector(this);
		}
		if (dstConnection != null) {
			setEndPoint(dstConnection.getTargetShape().getConnectionPoint(dstConnection.getTargetPoint()));
			((Connectable) dstConnection.getTargetShape()).addConnector(this);
		}
		this.dstConnection = dstConnection;
	}

	/**
	 *
	 * @return
	 */
	public Point2D getStartPoint() {
		return startPoint;
	}

	/**
	 *
	 * @param startPoint
	 */
	public void setStartPoint(Point2D startPoint) {
		this.startPoint.setLocation(startPoint);
		computeDimension();
	}

	/**
	 *
	 * @return
	 */
	public Point2D getEndPoint() {
		return endPoint;
	}

	/**
	 *
	 * @param endPoint
	 */
	public void setEndPoint(Point2D endPoint) {
		this.endPoint.setLocation(endPoint);
		computeDimension();
	}

	/**
	 *
	 * @param p
	 * @return
	 */
	@Override
	public boolean isPointInside(Point2D p) {
		Line2D shape = new Line2D.Double(startPoint, endPoint);
		double dist = shape.ptSegDist(p);
		return (dist <= 8d);
	}

	/**
	 *
	 * @param r
	 * @return
	 */
	@Override
	public boolean isInside(Rectangle2D r) {
		return r.contains(startPoint) && r.contains(endPoint);
	}

	/**
	 *
	 * @param p
	 * @return
	 */
	public int hitTest(Point2D p) {
		int iWhich = HIT_NONE;

		if (p.distance(endPoint) <= 9d) {
			iWhich = HIT_ENDPOINT;
		}
		else if (p.distance(startPoint) <= 9d) {
			iWhich = HIT_STARTPOINT;
		}
		else if (isPointInside(p)) {
			iWhich = HIT_LINE;
		}
		return iWhich;
	}

	/**
	 *
	 * @param gfx
	 */
	@Override
	public void paint(Graphics2D gfx) {
		Line2D shape = new Line2D.Double(startPoint, endPoint);
		if (bSelected) {
			gfx.setPaint(Color.BLUE);
		}
		else {
			gfx.setPaint(paint);
		}
		gfx.setStroke(stroke);
		gfx.draw(shape);

		if (bSelected) {
			Ellipse2D el = new Ellipse2D.Double();
			el.setFrame(startPoint.getX() - dCPIndicatorSize / 2d, startPoint.getY() - dCPIndicatorSize / 2d,
					dCPIndicatorSize, dCPIndicatorSize);
			gfx.setPaint(srcConnection != null ? Color.GREEN : Color.RED);
			gfx.setStroke(new BasicStroke(1f));
			gfx.fill(el);
			gfx.setPaint(Color.BLACK);
			gfx.draw(el);
			el.setFrame(endPoint.getX() - dCPIndicatorSize / 2d, endPoint.getY() - dCPIndicatorSize / 2d,
					dCPIndicatorSize, dCPIndicatorSize);
			gfx.setPaint(dstConnection != null ? Color.GREEN : Color.RED);
			gfx.setStroke(new BasicStroke(1f));
			gfx.fill(el);
			gfx.setPaint(Color.BLACK);
			gfx.draw(el);
		}
	}

	/**
	 *
	 * @param iWhich
	 * @param p
	 */
	public void movePoint(int iWhich, Point2D p) {
		Point2D newPoint = new Point2D.Double();
		switch (iWhich) {
			case STARTPOINT:
				newPoint.setLocation(startPoint.getX() + p.getX(), startPoint.getY() + p.getY());
				setStartPoint(newPoint);
				break;
			case ENDPOINT:
				newPoint.setLocation(endPoint.getX() + p.getX(), endPoint.getY() + p.getY());
				setEndPoint(newPoint);
				break;
		}
	}

	/**
	 *
	 * @param p
	 */
	@Override
	public boolean move(Point2D p) {
		Point2D newPoint = new Point2D.Double();
		newPoint.setLocation(startPoint.getX() + p.getX(), startPoint.getY() + p.getY());
		setStartPoint(newPoint);
		newPoint.setLocation(endPoint.getX() + p.getX(), endPoint.getY() + p.getY());
		setEndPoint(newPoint);
		return true;
	}

	/**
	 *
	 * @param p
	 */
	@Override
	public boolean resize(Point2D p) {
		return false;
	}

	public void incTargetPoint(ConnectionPoint cp) {
		cp.setTargetPoint(getNextTargetPoint(cp));
		if (cp.equals(srcConnection)) {
			setStartPoint(srcConnection.getTargetShape().getConnectionPoint(srcConnection.getTargetPoint()));
		}
		else {
			setEndPoint(dstConnection.getTargetShape().getConnectionPoint(dstConnection.getTargetPoint()));
		}
	}

	public void decTargetPoint(ConnectionPoint cp) {
		cp.setTargetPoint(getPrevTargetPoint(cp));
		if (cp.equals(srcConnection)) {
			setStartPoint(srcConnection.getTargetShape().getConnectionPoint(srcConnection.getTargetPoint()));
		}
		else {
			setEndPoint(dstConnection.getTargetShape().getConnectionPoint(dstConnection.getTargetPoint()));
		}
	}

	protected int getNextTargetPoint(ConnectionPoint cp) {
		int iResult = 0;
		if (cp.getTargetPoint() < cp.getTargetShape().getConnectionPointCount() - 1) {
			iResult = cp.getTargetPoint() + 1;
		}
		return iResult;
	}

	protected int getPrevTargetPoint(ConnectionPoint cp) {
		int iResult = cp.getTargetShape().getConnectionPointCount() - 1;
		if (cp.getTargetPoint() > 0) {
			iResult = cp.getTargetPoint() - 1;
		}
		return iResult;
	}

	protected void computeDimension() {
		double dMinX = Math.min(startPoint.getX(), endPoint.getX());
		double dMaxX = Math.max(startPoint.getX(), endPoint.getX());
		double dMinY = Math.min(startPoint.getY(), endPoint.getY());
		double dMaxY = Math.max(startPoint.getY(), endPoint.getY());
		dimension.setRect(dMinX, dMinY, dMaxX - dMinX, dMaxY - dMinY);
	}

	public boolean isConnectionAllowed(Shape shape, int connectorPoint, int connectionPoint) {
		boolean bResult = true;

		switch (connectorPoint) {
			case STARTPOINT:
				if (dstConnection != null && dstConnection.getTargetShape() != null &&
						dstConnection.getTargetShape().getId() == shape.getId()) {
					bResult = false;
				}
				break;
			case ENDPOINT:
				if (srcConnection != null && srcConnection.getTargetShape() != null &&
						srcConnection.getTargetShape().getId() == shape.getId()) {
					bResult = false;
				}
				break;
		}
		return bResult;
	}

	public void removeConnections() {
		AbstractShape shape = null;
		if (srcConnection != null && srcConnection.getTargetShape() != null) {
			shape = (AbstractShape) srcConnection.getTargetShape();
			shape.removeConnector(this);
			srcConnection = null;
		}
		if (dstConnection != null && dstConnection.getTargetShape() != null) {
			shape = (AbstractShape) dstConnection.getTargetShape();
			shape.removeConnector(this);
			dstConnection = null;
		}
	}

	@Override
	public void beforeDelete() {
		removeConnections();
	}

	public void setPaint(Color paint) {
		this.paint = paint;
	}

	public void setStroke(Stroke stroke) {
		this.stroke = stroke;
	}
}
