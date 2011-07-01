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
package org.nuclos.client.gef;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.nuclos.client.gef.layout.Extents2D;
import org.nuclos.client.gef.layout.Insets2D;

/**
 * Shape interface
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */

public interface Shape {

	/**
	 *
	 */
	public static final int RESIZE_NW = 0;
	/**
	 *
	 */
	public static final int RESIZE_N = 1;
	/**
	 *
	 */
	public static final int RESIZE_NE = 2;
	/**
	 *
	 */
	public static final int RESIZE_E = 3;
	/**
	 *
	 */
	public static final int RESIZE_SE = 4;
	/**
	 *
	 */
	public static final int RESIZE_S = 5;
	/**
	 *
	 */
	public static final int RESIZE_SW = 6;
	/**
	 *
	 */
	public static final int RESIZE_W = 7;
	/**
	 *
	 */
	public static final int RESIZE_COUNT = 8;

	/**
	 * Perform special actions after the shape has been created.
	 */
	public void afterCreate();

	/**
	 * Perform special actions before the shape will be created.
	 */
	public void beforeDelete();

	/**
	 *
	 * @return
	 */
	public Color getColor();

	/**
	 *
	 * @param color
	 */
	public void setColor(Color color);

	/**
	 *
	 * @return
	 */
	public Rectangle2D getDimension();

	/**
	 *
	 * @param dimension
	 */
	public void setDimension(Rectangle2D dimension);

	/**
	 *
	 * @return
	 */
	public Point2D getLocation();

	/**
	 *
	 * @param p
	 */
	public void setLocation(Point2D p);

	/**
	 *
	 * @param dX
	 * @param dY
	 */
	public void setLocation(double dX, double dY);

	/**
	 *
	 * @param p
	 * @return
	 */
	public boolean isPointInside(Point2D p);

	/**
	 *
	 * @param r
	 * @return
	 */
	public boolean isInside(Rectangle2D r);

	/**
	 *
	 * @param pIn
	 * @param pOut
	 * @return
	 */
	public int isInsideConnector(Point2D pIn, Point2D pOut);

	/**
	 *
	 * @param pIn
	 * @return
	 */
	public int isInsideResizer(Point2D pIn);

	/**
	 *
	 * @return
	 */
	public boolean isSelected();

	/**
	 *
	 * @param value
	 */
	public void setSelection(boolean value);

	/**
	 *
	 * @param gfx
	 */
	public void paint(Graphics2D gfx);

	/**
	 *
	 * @param gfx
	 */
	public void paintConnectionPoints(Graphics2D gfx);

	/**
	 *
	 * @param p
	 * @return
	 */
	public boolean move(Point2D p);

	/**
	 *
	 * @param p
	 * @return
	 */
	public boolean resize(Point2D p);

	/**
	 *
	 * @param dX
	 * @param dY
	 * @return
	 */
	public boolean move(double dX, double dY);

	/**
	 *
	 * @param dX
	 * @param dY
	 * @return
	 */
	public boolean resize(double dX, double dY);

	/**
	 *
	 * @param size
	 */
	public void setSize(Extents2D size);

	/**
	 *
	 */
	public Extents2D getSize();

	/**
	 *
	 * @return
	 */
	public double getX();

	/**
	 *
	 * @return
	 */
	public double getY();

	/**
	 *
	 * @return
	 */
	public double getWidth();

	/**
	 *
	 * @return
	 */
	public double getHeight();

	/**
	 *
	 * @param value
	 */
	public void setConnectionPointsVisible(boolean value);

	/**
	 *
	 * @return
	 */
	public boolean getConnectionPointsVisible();

	/**
	 *
	 * @param value
	 */
	public void setResizePointsVisible(boolean value);

	/**
	 *
	 * @return
	 */
	public boolean getResizePointsVisible();

	/**
	 *
	 * @return
	 */
	public boolean isConnectable();

	/**
	 *
	 * @param value
	 */
	public void setConnectable(boolean value);

	/**
	 *
	 * @param point
	 * @return
	 */
	public Point2D getConnectionPoint(int point);

	/**
	 *
	 * @return
	 */
	public int getConnectionPointCount();

	/**
	 *
	 * @param view
	 */
	public void setView(ShapeViewer view);

	/**
	 *
	 * @return
	 */
	public ShapeViewer getView();

	/**
	 *
	 * @return
	 */
	public boolean isVisible();

	/**
	 *
	 * @param bVisible
	 */
	public void setVisible(boolean bVisible);

	/**
	 *
	 */
	public boolean isSelectable();

	/**
	 *
	 * @param bSelectable
	 */
	public void setSelectable(boolean bSelectable);

	/**
	 *
	 * @param bValue
	 */
	public void setMoveable(boolean bValue);

	/**
	 *
	 * @return
	 */
	public boolean isMoveable();

	/**
	 *
	 * @param bValue
	 */
	public void setResizeable(boolean bValue);

	/**
	 *
	 * @return
	 */
	public boolean isResizeable();

	/**
	 *
	 * @return
	 */
	public Insets2D getInsets();

	/**
	 *
	 * @param insets
	 */
	public void setInsets(Insets2D insets);

	/**
	 *
	 * @return
	 */
	public Extents2D getPreferredSize();

	/**
	 *
	 * @param d
	 */
	public void setPreferredSize(Extents2D d);

	/**
	 *
	 * @return
	 */
	public Extents2D getMinimumSize();

	/**
	 *
	 * @param d
	 */
	public void setMinimumSize(Extents2D d);

	/**
	 *
	 * @return
	 */
	public Extents2D getMaximumSize();

	/**
	 *
	 * @param d
	 */
	public void setMaximumSize(Extents2D d);

	public int getId();
}
