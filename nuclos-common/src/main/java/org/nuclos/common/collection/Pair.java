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
package org.nuclos.common.collection;

import java.io.Serializable;

import org.nuclos.common2.LangUtils;

/**
 * A generic Pair class.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars Rueckemann</a>
 * @version 01.00.00
 */
public class Pair<X, Y> implements Serializable {

	private static final long serialVersionUID = 1461169617313551192L;

	public X x;
	public Y y;
	
	public static <X, Y> Pair<X, Y> makePair(X x, Y y) {
		return new Pair<X, Y>(x, y);
	}

	public Pair() {
		this(null, null);
	}

	public Pair(X x, Y y) {
		this.x = x;
		this.y = y;
	}

	public X getX() {
		return x;
	}

	public void setX(X x) {
		this.x = x;
	}

	public Y getY() {
		return y;
	}

	public void setY(Y y) {
		this.y = y;
	}

	@Override
	public boolean equals(Object o) {
		return (this == o) || ((o != null) && (o instanceof Pair) && this.equals((Pair<?, ?>) o));
	}

	public boolean equals(Pair<?, ?> that) {
		return LangUtils.equals(this.x, that.x) && LangUtils.equals(this.y, that.y);
	}

	@Override
	public int hashCode() {
		return LangUtils.hashCode(x) ^ LangUtils.hashCode(y);
	}

	@Override
	public String toString() {
		return "Pair(" + x + ", " + y + ")";
	}

}	// class Pair
