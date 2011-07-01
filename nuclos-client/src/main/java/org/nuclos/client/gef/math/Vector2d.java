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
package org.nuclos.client.gef.math;

import java.awt.geom.Point2D;

/**
 * 2D vector.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class Vector2d {
	double dX;
	double dY;

	/**
	 * Creates a null vector
	 */
	public Vector2d() {
		dX = dY = 0d;
	}

	/**
	 * Creates a vector to point dX/dY
	 * @param dX
	 * @param dY
	 */
	public Vector2d(double dX, double dY) {
		this.dX = dX;
		this.dY = dY;
	}

	/**
	 * Copy constructor
	 * @param v
	 */
	public Vector2d(Vector2d v) {
		this.dX = v.dX;
		this.dY = v.dY;
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

	/**
	 * Returns a result vector by computing v1 - v2
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static Vector2d vecSubtract(Vector2d v1, Vector2d v2) {
		return new Vector2d(v1.dX - v2.dX, v1.dY - v2.dY);
	}

	/**
	 * Returns a result vector by computing p1 - p2
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static Vector2d vecSubtract(Point2D p1, Point2D p2) {
		return new Vector2d(p1.getX() - p2.getX(), p1.getY() - p2.getY());
	}

	/**
	 * Returns a result vector by computing v1 + v2
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static Vector2d vecAdd(Vector2d v1, Vector2d v2) {
		return new Vector2d(v1.dX + v2.dX, v1.dY + v2.dY);
	}

	/**
	 * Scales a vector by a double value
	 * @param dValue
	 * @return
	 */
	public Vector2d scale(double dValue) {
		dX *= dValue;
		dY *= dValue;
		return this;
	}

	/**
	 * Returns the length of a vector
	 * @return
	 */
	public double length() {
		return Math.sqrt(dX * dX + dY * dY);
	}

	/**
	 * Rotates a vector by a given angle
	 * @param dValue
	 * @return
	 */
	public Vector2d rotate(double dValue) {
		dX = Math.cos(dValue) * dX + Math.sin(dValue) * dY;
		dY = -Math.sin(dValue) * dX + Math.cos(dValue) * dY;
		return this;
	}

	public static double scalarProduct(Vector2d v1, Vector2d v2) {
		return v1.getX() * v2.getX() + v1.getY() * v2.getY();
	}

	@Override
	public String toString() {
		return "(" + dX + "|" + dY + ")";
	}

	public static double getAngle(Vector2d v1, Vector2d v2) {
		return Math.acos(scalarProduct(v1, v2) / (v1.length() * v2.length()));
	}
}
