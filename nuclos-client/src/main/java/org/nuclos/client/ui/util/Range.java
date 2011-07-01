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

package org.nuclos.client.ui.util;

import java.io.Serializable;

public class Range implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int coord;

	public int extent;

	public Range() {
	}

	public Range(int coord, int extent) {
		this.coord = coord;
		this.extent = extent;
	}

	public int getCoord() {
		return coord;
	}

	public void setCoord(int coord) {
		this.coord = coord;
	}

	public int getExtent() {
		return extent;
	}

	public void setExtent(int extent) {
		this.extent = extent;
	}

	@Override
	public int hashCode() {
		return coord ^ extent;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Range) {
			Range r = (Range) obj;
			return (coord == r.coord) && (extent == r.extent);
		}
		return false;
	}

	@Override
	public String toString() {
		return getClass().getName() + "[coord=" + coord + ",extent=" + extent + "]";
	}
}
