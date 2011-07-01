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

import org.nuclos.client.gef.Shape;

/**
 * Connection point.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class ConnectionPoint {
	protected Shape targetShape;
	protected int iTargetPoint;

	/**
	 *
	 */
	public ConnectionPoint() {
		this(null, -1);
	}

	/**
	 *
	 * @param targetShape
	 * @param iTargetPoint
	 */
	public ConnectionPoint(Shape targetShape, int iTargetPoint) {
		this.targetShape = targetShape;
		this.iTargetPoint = iTargetPoint;
	}

	/**
	 *
	 * @return
	 */
	public Shape getTargetShape() {
		return targetShape;
	}

	/**
	 *
	 * @param targetShape
	 */
	public void setTargetShape(Shape targetShape) {
		this.targetShape = targetShape;
	}

	/**
	 *
	 * @return
	 */
	public int getTargetPoint() {
		return iTargetPoint;
	}

	/**
	 *
	 * @param iTargetPoint
	 */
	public void setTargetPoint(int iTargetPoint) {
		this.iTargetPoint = iTargetPoint;
	}

	/**
	 *
	 * @param p
	 * @return
	 */
	public boolean equals(ConnectionPoint p) {
		return p != null && p.targetShape == this.targetShape && p.iTargetPoint == this.iTargetPoint;
	}
}
