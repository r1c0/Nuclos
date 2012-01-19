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

import java.awt.geom.Point2D;

import org.nuclos.client.gef.shapes.AbstractConnector;

/**
 * Connactable interface.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public interface Connectable {
	/**
	 *
	 */
	public static final int CONNECTION_NW = 0;
	/**
	 *
	 */
	public static final int CONNECTION_NNW = 1;
	/**
	 *
	 */
	public static final int CONNECTION_N = 2;
	/**
	 *
	 */
	public static final int CONNECTION_NNE = 3;
	/**
	 *
	 */
	public static final int CONNECTION_NE = 4;
	/**
	 *
	 */
	public static final int CONNECTION_ENE = 5;
	/**
	 *
	 */
	public static final int CONNECTION_E = 6;
	/**
	 *
	 */
	public static final int CONNECTION_ESE = 7;
	/**
	 *
	 */
	public static final int CONNECTION_SE = 8;
	/**
	 *
	 */
	public static final int CONNECTION_SSE = 9;
	/**
	 *
	 */
	public static final int CONNECTION_S = 10;
	/**
	 *
	 */
	public static final int CONNECTION_SSW = 11;
	/**
	 *
	 */
	public static final int CONNECTION_SW = 12;
	/**
	 *
	 */
	public static final int CONNECTION_WSW = 13;
	/**
	 *
	 */
	public static final int CONNECTION_W = 14;
	/**
	 *
	 */
	public static final int CONNECTION_WNW = 15;
	/**
	 *
	 */
	public static final int CONNECTION_CENTER = 16;
	/**
	 *
	 */
	public static final int CONNECTION_COUNT = 17;

	/**
	 *
	 * @return
	 */
	public Point2D[] getConnectionPoints();

	/**
	 *
	 * @return
	 */
	public double getConnectionSnapRadius();

	/**
	 *
	 * @param connectionSnapRadius
	 */
	public void setConnectionSnapRadius(double connectionSnapRadius);

	/**
	 *
	 * @param index
	 * @return
	 */
	public Point2D getConnectionPoint(int index);

	/**
	 *
	 * @return
	 */
	public boolean isConnectable();

	/**
	 *
	 * @param pIn
	 * @param pOut
	 * @return
	 */
	public int isInsideConnector(Point2D pIn, Point2D pOut);

	/**
	 *
	 * @param value
	 */
	public void setConnectable(boolean value);

	/**
	 *
	 * @param connection
	 */
	public void addConnector(AbstractConnector connection);

	/**
	 *
	 * @param connection
	 */
	public void removeConnector(AbstractConnector connection);

}
