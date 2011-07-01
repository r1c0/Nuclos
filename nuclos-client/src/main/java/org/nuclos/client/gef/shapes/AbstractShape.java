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
/*
 * Created on 12.07.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.nuclos.client.gef.shapes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

import org.nuclos.client.gef.Connectable;
import org.nuclos.client.gef.Shape;
import org.nuclos.client.gef.ShapeViewer;
import org.nuclos.client.gef.layout.Extents2D;
import org.nuclos.client.gef.layout.Insets2D;

/**
 * @author bsander
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class AbstractShape implements Serializable, Cloneable, Shape, Connectable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *	Unique system generated shape id
	 */
	protected static int iShapeId;

	protected ShapeViewer view = null;

	protected Color bgColor;
	protected Color connectionPointColor;
	protected Color resizePointColor;
	protected Color borderColor;
	protected double borderSize;
	protected boolean paintBorder;

	protected Rectangle2D dimension;
	protected boolean bSelected;
	protected Point2D[] connections = null;
	protected boolean[] connectionsEnabled = new boolean[CONNECTION_COUNT];
	protected ArrayList<AbstractConnector> connectors = new ArrayList<AbstractConnector>();
	protected double connectionSnapRadius;
	protected Point2D[] resizers = null;
	protected double resizerSnapRadius;
	protected boolean bConnectable;
	protected boolean bConnectionPointsVisible;
	protected boolean bResizePointsVisible;
	protected boolean bVisible;
	protected boolean bSelectable;
	protected boolean bMoveable;
	protected boolean bResizeable;

	protected Insets2D insets = new Insets2D(0d, 0d, 0d, 0d);
	protected Extents2D preferredSize = new Extents2D(0d, 0d);
	protected Extents2D minimumSize = new Extents2D(0d, 0d);
	protected Extents2D maximumSize = new Extents2D(Double.MAX_VALUE, Double.MAX_VALUE);

	protected int iId;

	/**
	 *
	 */
	public AbstractShape() {
		for (int i = 0; i < CONNECTION_COUNT; connectionsEnabled[i++] = true) {
			;
		}
		bgColor = Color.BLUE;
		connectionPointColor = Color.RED;
		connectionPointColor = Color.BLACK;
		dimension = new Rectangle2D.Double(0d, 0d, 0d, 0d);
		borderColor = Color.BLACK;
		borderSize = 1.0d;
		paintBorder = true;
		bConnectable = false;
		bConnectionPointsVisible = false;
		connectionSnapRadius = 14.0d;
		resizerSnapRadius = 6.0d;
		bMoveable = true;
		bResizeable = false;
		bSelectable = true;
		iId = iShapeId++;
	}

	@Override
	public void afterCreate() {
	}

	@Override
	public void beforeDelete() {
		Object[] aConnectors = connectors.toArray();
		for (int i = 0; i < aConnectors.length; i++) {
			AbstractConnector connector = (AbstractConnector) aConnectors[i];
			ConnectionPoint srcCp = connector.getSourceConnection();
			ConnectionPoint dstCp = connector.getDestinationConnection();

			if (srcCp != null && srcCp.getTargetShape() == this) {
				connector.setSourceConnection(null);
			}
			if (dstCp != null && dstCp.getTargetShape() == this) {
				connector.setDestinationConnection(null);
			}
		}
	}

	/**
	 *
	 * @param gfx
	 */
	@Override
	public void paint(Graphics2D gfx) {
		if (bConnectionPointsVisible) {
			paintConnectionPoints(gfx);
		}
		if (bResizePointsVisible) {
			paintResizePoints(gfx);
		}
		if (bSelected && bResizePointsVisible) {
			paintResizePoints(gfx);
		}
	}

	/**
	 *
	 * @param gfx
	 */
	@Override
	public void paintConnectionPoints(Graphics2D gfx) {
		Ellipse2D area = new Ellipse2D.Double();
		double dSize = 5d;

		for (int i = 0; connections != null && i < connections.length; i++) {
			if (!connectionsEnabled[i]) {
				continue;
			}
			Point2D p = connections[i];
			area.setFrame(p.getX() - dSize / 2d, p.getY() - dSize / 2d, dSize, dSize);
			gfx.setPaint(connectionPointColor);
			gfx.fill(area);
		}
	}

	/**
	 *
	 * @param gfx
	 */
	public void paintResizePoints(Graphics2D gfx) {
		Rectangle2D area = new Rectangle2D.Double();
		double dSize = 5d;

		for (int i = 0; resizers != null && i < resizers.length; i++) {
			Point2D p = resizers[i];
			area.setFrame(p.getX() - dSize / 2d, p.getY() - dSize / 2d, dSize, dSize);
			gfx.setPaint(resizePointColor);
			gfx.fill(area);
		}
	}

	/**
	 *
	 * @return the id of the shape
	 */
	@Override
	public int getId() {
		return iId;
	}

	/**
	 *
	 * @return the fill color of the shape
	 */
	@Override
	public Color getColor() {
		return bgColor;
	}

	/**
	 *
	 * @param color
	 */
	@Override
	public void setColor(Color color) {
		this.bgColor = color;
	}

	/**
	 *
	 * @return the color of the connection point
	 */
	public Color getConnectionPointColor() {
		return connectionPointColor;
	}

	/**
	 *
	 * @param connectionPointColor
	 */
	public void setConnectionPointColor(Color connectionPointColor) {
		this.connectionPointColor = connectionPointColor;
	}

	@Override
	public int getConnectionPointCount() {
		int iResult = 0;
		if (connections != null) {
			iResult = connections.length;
		}
		return iResult;
	}

	/**
	 *
	 * @return Rectangle2D the size of the shape
	 */
	@Override
	public Rectangle2D getDimension() {
		return dimension;
	}

	/**
	 *
	 * @param dimension
	 */
	@Override
	public void setDimension(Rectangle2D dimension) {
		if (view != null) {
			this.dimension.setRect(Math.max((Math.min(dimension.getX(), view.getWidth() - dimension.getWidth())), 0d),
					Math.max((Math.min(dimension.getY(), view.getHeight() - dimension.getHeight())), 0d),
					Math.max(
							Math.min(dimension.getWidth(), (maximumSize.getWidth() > 0d ? maximumSize.getWidth() : dimension.getWidth())),
							minimumSize.getWidth()),
					Math.max(
							Math.min(dimension.getHeight(),
									(maximumSize.getHeight() > 0d ? maximumSize.getHeight() : dimension.getHeight())),
							minimumSize.getHeight()));
		}
		else {
			this.dimension.setRect(Math.max(dimension.getX(), 0d),
					Math.max(dimension.getY(), 0d),
					Math.max(
							Math.min(dimension.getWidth(), (maximumSize.getWidth() > 0d ? maximumSize.getWidth() : dimension.getWidth())),
							minimumSize.getWidth()),
					Math.max(
							Math.min(dimension.getHeight(),
									(maximumSize.getHeight() > 0d ? maximumSize.getHeight() : dimension.getHeight())),
							minimumSize.getHeight()));
		}
		if (bResizeable) {
			layoutResizers();
		}
		if (bConnectable) {
			layoutConnections();
			for (Iterator<AbstractConnector> i = connectors.iterator(); i.hasNext();) {
				AbstractConnector connector = i.next();
				if (connector.getSourceConnection() != null && connector.getSourceConnection().getTargetShape().equals(this)) {
					int iConnectionPoint = connector.getSourceConnection().getTargetPoint();
					if (iConnectionPoint < 0) {
						try {
							Logger.getLogger(this.getClass()).error(
									"Startpunkt ist f\u00e4lschlicherweise < 0 (" + iConnectionPoint + ", setDimension())");
						}
						catch (Exception e) {
							e.printStackTrace();
						}
						iConnectionPoint = 0;
					}
					connector.setStartPoint(connections[iConnectionPoint]);
				}
				else
				if (connector.getDestinationConnection() != null && connector.getDestinationConnection().getTargetShape().equals(this))
				{
					int iConnectionPoint = connector.getDestinationConnection().getTargetPoint();
					if (iConnectionPoint < 0) {
						try {
							Logger.getLogger(this.getClass()).error(
									"Endpunkt ist f\u00e4lschlicherweise < 0 (" + iConnectionPoint + ", setDimension())");
						}
						catch (Exception e) {
							e.printStackTrace();
						}
						iConnectionPoint = 0;
					}
					connector.setEndPoint(connections[iConnectionPoint]);
				}
			}
		}
	}

	/**
	 *
	 * @return the position of the upper left corner of the shape
	 */
	@Override
	public Point2D getLocation() {
		return new Point2D.Double(dimension.getX(), dimension.getY());
	}

	/**
	 *
	 * @param p
	 */
	@Override
	public void setLocation(Point2D p) {
		Rectangle2D newRect = new Rectangle2D.Double();
		newRect.setRect(p.getX(), p.getY(), dimension.getWidth(), dimension.getHeight());
		setDimension(newRect);
	}

	/**
	 *
	 * @param dX
	 * @param dY
	 */
	@Override
	public void setLocation(double dX, double dY) {
		Rectangle2D newRect = new Rectangle2D.Double();
		newRect.setRect(dX, dY, dimension.getWidth(), dimension.getHeight());
		setDimension(newRect);
	}

	/**
	 *
	 */
	private void layoutConnections() {
		connections[CONNECTION_NW].setLocation(dimension.getX(), dimension.getY());
		connections[CONNECTION_NNW].setLocation(dimension.getX() + dimension.getWidth() / 4d, dimension.getY());
		connections[CONNECTION_N].setLocation(dimension.getX() + dimension.getWidth() / 2d, dimension.getY());
		connections[CONNECTION_NNE].setLocation(dimension.getX() + dimension.getWidth() * 3d / 4d, dimension.getY());
		connections[CONNECTION_NE].setLocation(dimension.getX() + dimension.getWidth(), dimension.getY());
		connections[CONNECTION_ENE].setLocation(dimension.getX() + dimension.getWidth(),
				dimension.getY() + dimension.getHeight() / 4d);
		connections[CONNECTION_E].setLocation(dimension.getX() + dimension.getWidth(),
				dimension.getY() + dimension.getHeight() / 2d);
		connections[CONNECTION_ESE].setLocation(dimension.getX() + dimension.getWidth(),
				dimension.getY() + dimension.getHeight() * 3d / 4d);
		connections[CONNECTION_SE].setLocation(dimension.getX() + dimension.getWidth(),
				dimension.getY() + dimension.getHeight());
		connections[CONNECTION_SSE].setLocation(dimension.getX() + dimension.getWidth() * 3d / 4d,
				dimension.getY() + dimension.getHeight());
		connections[CONNECTION_S].setLocation(dimension.getX() + dimension.getWidth() / 2d,
				dimension.getY() + dimension.getHeight());
		connections[CONNECTION_SSW].setLocation(dimension.getX() + dimension.getWidth() / 4d,
				dimension.getY() + dimension.getHeight());
		connections[CONNECTION_SW].setLocation(dimension.getX(), dimension.getY() + dimension.getHeight());
		connections[CONNECTION_WSW].setLocation(dimension.getX(), dimension.getY() + dimension.getHeight() * 3d / 4d);
		connections[CONNECTION_W].setLocation(dimension.getX(), dimension.getY() + dimension.getHeight() / 2d);
		connections[CONNECTION_WNW].setLocation(dimension.getX(), dimension.getY() + dimension.getHeight() / 4d);
		connections[CONNECTION_CENTER].setLocation(dimension.getX() + dimension.getWidth() / 2d,
				dimension.getY() + dimension.getHeight() / 2d);
	}

	/**
	 *
	 */
	private void layoutResizers() {
		resizers[RESIZE_NW].setLocation(dimension.getX(), dimension.getY());
		resizers[RESIZE_N].setLocation(dimension.getX() + dimension.getWidth() / 2d, dimension.getY());
		resizers[RESIZE_NE].setLocation(dimension.getX() + dimension.getWidth(), dimension.getY());
		resizers[RESIZE_E].setLocation(dimension.getX() + dimension.getWidth(),
				dimension.getY() + dimension.getHeight() / 2d);
		resizers[RESIZE_SE].setLocation(dimension.getX() + dimension.getWidth(), dimension.getY() + dimension.getHeight());
		resizers[RESIZE_S].setLocation(dimension.getX() + dimension.getWidth() / 2d,
				dimension.getY() + dimension.getHeight());
		resizers[RESIZE_SW].setLocation(dimension.getX(), dimension.getY() + dimension.getHeight());
		resizers[RESIZE_W].setLocation(dimension.getX(), dimension.getY() + dimension.getHeight() / 2d);
	}

	/**
	 *
	 * @param p
	 * @return true if the point is within the shape
	 */
	@Override
	public boolean isPointInside(Point2D p) {
		boolean bResult = false;

		if (bConnectable || bResizeable) {
			Rectangle2D area = new Rectangle2D.Double(dimension.getX() - connectionSnapRadius / 2d,
					dimension.getY() - connectionSnapRadius / 2d,
					dimension.getWidth() + connectionSnapRadius,
					dimension.getHeight() + connectionSnapRadius);
			bResult = area.contains(p);
		}
		else {
			Rectangle2D area = new Rectangle2D.Double(dimension.getX(), dimension.getY(),
					dimension.getWidth(), dimension.getHeight());
			bResult = area.contains(p);
		}
		return bResult;
	}

	/**
	 *
	 * @param r
	 * @return true if the given rectangle is completely within the shape
	 */
	@Override
	public boolean isInside(Rectangle2D r) {
		Rectangle2D dim = new Rectangle2D.Double(dimension.getX(), dimension.getY(),
				dimension.getWidth(), dimension.getHeight());
		Rectangle2D rz = new Rectangle2D.Double(r.getX(), r.getY(),
				r.getWidth(), r.getHeight());
		return rz.contains(dim);
	}

	/**
	 *
	 * @param dX
	 * @param dY
	 */
	@Override
	public boolean move(double dX, double dY) {
		boolean bResult = false;

		Rectangle2D bounds = dimension.getBounds2D();
		Rectangle2D newBounds = new Rectangle2D.Double(bounds.getX() + dX, bounds.getY() + dY,
				bounds.getWidth(), bounds.getHeight());
		if (newBounds.getX() >= 0d && newBounds.getY() >= 0d) {
			setDimension(newBounds);
			bResult = true;
		}
		return bResult;
	}

	/**
	 *
	 * @param p
	 */
	@Override
	public boolean move(Point2D p) {
		return move(p.getX(), p.getY());
	}

	/**
	 *
	 * @param dX
	 * @param dY
	 */
	@Override
	public boolean resize(double dX, double dY) {
		boolean bResult = false;

		Rectangle2D bounds = dimension.getBounds2D();
		Rectangle2D newBounds = new Rectangle2D.Double(bounds.getX(), bounds.getY(),
				bounds.getWidth() + dX, bounds.getHeight() + dY);
		if (newBounds.getWidth() >= (minimumSize.getWidth() > 0 ? minimumSize.getWidth() : Double.MIN_VALUE) ||
				newBounds.getHeight() >= (minimumSize.getHeight() > 0 ? minimumSize.getHeight() : Double.MIN_VALUE)) {
			setDimension(newBounds);
			bResult = true;
		}
		return bResult;
	}

	/**
	 *
	 * @param p
	 */
	@Override
	public boolean resize(Point2D p) {
		return resize(p.getX(), p.getY());
	}

	/**
	 *
	 * @param newSize
	 */
	@Override
	public void setSize(Extents2D newSize) {
		Rectangle2D bounds = dimension.getBounds2D();
		Rectangle2D newBounds = new Rectangle2D.Double(bounds.getX(), bounds.getY(),
				newSize.getWidth(), newSize.getHeight());
		setDimension(newBounds);
	}

	/**
	 *
	 * @return the size of the shape
	 */
	@Override
	public Extents2D getSize() {
		return new Extents2D(dimension.getWidth(), dimension.getHeight());
	}

	/**
	 *
	 * @return the position of the shape on the x-axis
	 */
	@Override
	public double getX() {
		return dimension.getX();
	}

	/**
	 *
	 * @return the position of the shape on the y-axis
	 */
	@Override
	public double getY() {
		return dimension.getY();
	}

	/**
	 *
	 * @return the width of the shape
	 */
	@Override
	public double getWidth() {
		return dimension.getWidth();
	}

	/**
	 *
	 * @return the height of the shape
	 */
	@Override
	public double getHeight() {
		return dimension.getHeight();
	}

	/**
	 *
	 * @return true if the shape is selected
	 */
	@Override
	public boolean isSelected() {
		return bSelected;
	}

	/**
	 *
	 * @param value
	 */
	@Override
	public void setSelection(boolean value) {
		bSelected = value;
	}

	/**
	 *
	 * @return the border line color of the shape
	 */
	public Color getBorderColor() {
		return borderColor;
	}

	/**
	 *
	 * @param borderColor
	 */
	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	/**
	 *
	 * @return the width of the border line
	 */
	public double getBorderSize() {
		return borderSize;
	}

	/**
	 *
	 * @param borderSize
	 */
	public void setBorderSize(double borderSize) {
		this.borderSize = borderSize;
	}

	/**
	 *
	 * @return true if the border is painted
	 */
	public boolean isBorderPainted() {
		return paintBorder;
	}

	/**
	 *
	 * @param paintBorder
	 */
	public void setPaintBorder(boolean paintBorder) {
		this.paintBorder = paintBorder;
	}

	/**
	 *
	 * @return Point2D array of all connection points
	 */
	@Override
	public Point2D[] getConnectionPoints() {
		return connections;
	}

	/**
	 *
	 * @return the radius around a connection point where the mouse is "catched" when dragging a connection
	 */
	@Override
	public double getConnectionSnapRadius() {
		return connectionSnapRadius;
	}

	/**
	 *
	 * @param connectionSnapRadius
	 */
	@Override
	public void setConnectionSnapRadius(double connectionSnapRadius) {
		this.connectionSnapRadius = connectionSnapRadius;
	}

	/**
	 *
	 * @param index
	 * @return the connection point with the specified index
	 */
	@Override
	public Point2D getConnectionPoint(int index) {
		Point2D result = new Point2D.Double(-1D, -1D);

		if (!bConnectable) {
			return result;
		}

		try {
			result.setLocation(connections[index]);
		}
		catch (ArrayIndexOutOfBoundsException e) {
		}
		return result;
	}

	/**
	 *
	 * @return true if the shape can be connected to another
	 */
	@Override
	public boolean isConnectable() {
		return bConnectable;
	}

	/**
	 *
	 * @param value
	 */
	@Override
	public void setConnectable(boolean value) {
		bConnectable = value;
		if (bConnectable) {
			connections = new Point2D.Double[CONNECTION_COUNT];
			for (int i = 0; i < CONNECTION_COUNT; connections[i++] = new Point2D.Double()) {
				;
			}
			layoutConnections();
		}
		else {
			connections = null;
		}
	}

	/**
	 *
	 * @return true if the connection points are shown
	 */
	@Override
	public boolean getConnectionPointsVisible() {
		return bConnectionPointsVisible;
	}

	/**
	 *
	 * @param bConnectionPointsVisible
	 */
	@Override
	public void setConnectionPointsVisible(boolean bConnectionPointsVisible) {
		this.bConnectionPointsVisible = bConnectionPointsVisible & bConnectable;
	}

	/**
	 *
	 * @return true if the resize points are shown
	 */
	@Override
	public boolean getResizePointsVisible() {
		return bResizePointsVisible;
	}

	/**
	 *
	 * @param bResizePointsVisible
	 */
	@Override
	public void setResizePointsVisible(boolean bResizePointsVisible) {
		this.bResizePointsVisible = bResizePointsVisible & bResizeable;
	}

	/**
	 *
	 * @param pIn the point to check
	 * @param pOut is fileed with the location of the connection point, if pIn is inside it
	 * @return the index of the connection point, when pIn is inside one, -1 else
	 */
	@Override
	public int isInsideConnector(Point2D pIn, Point2D pOut) {
		int iResult = -1;
		if (bConnectable) {
			for (int i = 0; i < CONNECTION_COUNT; i++) {
				Rectangle2D r = new Rectangle2D.Double(connections[i].getX() - connectionSnapRadius / 2d,
						connections[i].getY() - connectionSnapRadius / 2d,
						connectionSnapRadius, connectionSnapRadius);
				if (connectionsEnabled[i] && r.contains(pIn)) {
					pOut.setLocation(connections[i]);
					iResult = i;
					break;
				}
			}
		}
		return iResult;
	}

	/**
	 *
	 * @param pIn the point to check
	 * @return the index of the resizer point, when pIn is inside one, -1 else
	 */
	@Override
	public int isInsideResizer(Point2D pIn) {
		int iResult = -1;
		if (bResizeable) {
			for (int i = 0; i < RESIZE_COUNT; i++) {
				Rectangle2D r = new Rectangle2D.Double(resizers[i].getX() - resizerSnapRadius / 2d,
						resizers[i].getY() - resizerSnapRadius / 2d,
						resizerSnapRadius, resizerSnapRadius);
				if (r.contains(pIn)) {
					iResult = i;
					break;
				}
			}
		}
		return iResult;
	}

	/**
	 *
	 * @param connector
	 */
	@Override
	public void addConnector(AbstractConnector connector) {
		connectors.add(connector);
	}

	/**
	 *
	 * @param connector
	 */
	@Override
	public void removeConnector(AbstractConnector connector) {
		connectors.remove(connector);
	}

	/**
	 *
	 * @param s
	 */
	public boolean equals(AbstractShape s) {
		return this.iId == s.iId;
	}

	/**
	 *
	 */
	public static void initShapeId() {
		iShapeId = 1;
	}

	/**
	 *
	 * @param view
	 */
	@Override
	public void setView(ShapeViewer view) {
		this.view = view;
	}

	/**
	 *
	 * @return the view of the shape
	 */
	@Override
	public ShapeViewer getView() {
		return view;
	}

	@Override
	public boolean isVisible() {
		return bVisible;
	}

	@Override
	public void setVisible(boolean bVisible) {
		this.bVisible = bVisible;
	}

	@Override
	public Insets2D getInsets() {
		return insets;
	}

	@Override
	public void setInsets(Insets2D insets) {
		this.insets.left = insets.left;
		this.insets.top = insets.top;
		this.insets.right = insets.right;
		this.insets.bottom = insets.bottom;
	}

	@Override
	public Extents2D getPreferredSize() {
		return preferredSize;
	}

	@Override
	public void setPreferredSize(Extents2D preferredSize) {
		this.preferredSize.setSize(preferredSize);
	}

	public void setPreferredSize(double dW, double dH) {
		this.preferredSize.width = dW;
		this.preferredSize.height = dH;
	}

	@Override
	public Extents2D getMinimumSize() {
		return minimumSize;
	}

	@Override
	public void setMinimumSize(Extents2D minimumSize) {
		this.minimumSize.setSize(minimumSize);
	}

	public void setMinimumSize(double dW, double dH) {
		this.minimumSize.width = dW;
		this.minimumSize.height = dH;
	}

	@Override
	public Extents2D getMaximumSize() {
		return maximumSize;
	}

	@Override
	public void setMaximumSize(Extents2D maximumSize) {
		this.maximumSize.setSize(maximumSize);
	}

	public void setMaximumSize(double dW, double dH) {
		this.maximumSize.width = dW;
		this.maximumSize.height = dH;
	}

	@Override
	public boolean isMoveable() {
		return bMoveable;
	}

	@Override
	public void setMoveable(boolean bMoveable) {
		this.bMoveable = bMoveable;
	}

	@Override
	public boolean isResizeable() {
		return bResizeable;
	}

	@Override
	public void setResizeable(boolean bResizeable) {
		this.bResizeable = bResizeable;
		if (bResizeable) {
			resizers = new Point2D.Double[RESIZE_COUNT];
			for (int i = 0; i < RESIZE_COUNT; resizers[i++] = new Point2D.Double()) {
				;
			}
			layoutResizers();
		}
		else {
			resizers = null;
		}
	}

	@Override
	public boolean isSelectable() {
		return bSelectable;
	}

	@Override
	public void setSelectable(boolean bSelectable) {
		this.bSelectable = bSelectable;
	}
}
