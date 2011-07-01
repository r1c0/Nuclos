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
package org.nuclos.server.statemodel.valueobject;

import java.io.Serializable;

/**
 * State layout.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris@novabit.de">Boris</a>
 * @version 01.00.00
 */
public class StateLayout implements Serializable {
	private static final long serialVersionUID = 7488185297337827494L;

	private double dX;
	private double dY;
	private double dW;
	private double dH;

	public StateLayout() {
		dX = dY = dW = dH = 0d;
	}

	public StateLayout(double dX, double dY, double dWidth, double dHeight) {
		this.dX = dX;
		this.dY = dY;
		this.dW = dWidth;
		this.dH = dHeight;
	}

	public double getX() {
		return dX;
	}

	public void setX(double dX) {
		this.dX = dX;
	}

	public double getY() {
		return dY;
	}

	public void setY(double dY) {
		this.dY = dY;
	}

	public double getWidth() {
		return dW;
	}

	public void setWidth(double dWidth) {
		this.dW = dWidth;
	}

	public double getHeight() {
		return dH;
	}

	public void setHeight(double dHeight) {
		this.dH = dHeight;
	}

	@Override
	public boolean equals(Object o) {
		StateLayout sl = (StateLayout) o;

		return
				this.dX == sl.getX() &&
						this.dY == sl.getY() &&
						this.dW == sl.getWidth() &&
						this.dH == sl.getHeight();
	}

}	// class StateLayout
