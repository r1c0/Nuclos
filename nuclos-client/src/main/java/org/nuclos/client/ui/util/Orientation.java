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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.SwingConstants;

import org.nuclos.common.collection.Pair;

/**
 * A type-safe enum for the Swing orientations HORIZONTAL and VERTICAL.
 */
public enum Orientation {
	HORIZONTAL(SwingConstants.HORIZONTAL),
	VERTICAL(SwingConstants.VERTICAL);
	
	private final int value;
	
	private Orientation(int constant) {
		this.value = constant;
	}
	
	public boolean isHorizontal() {
		return value == SwingConstants.HORIZONTAL;
	}
	
	public boolean isVertical() {
		return value == SwingConstants.VERTICAL;
	}
	
	public Orientation opposite() {
		return isHorizontal() ? VERTICAL : HORIZONTAL;
	}
	
	public int swingConstant() {
		return value;
	}
	
	public int coordFrom(Point pt) {
		return isHorizontal() ? pt.x : pt.y;
	}
	
	public int coordFrom(Rectangle rect) {
		return isHorizontal() ? rect.x : rect.y;
	}
	
	public int extentFrom(Dimension dim) {
		return isHorizontal() ? dim.width : dim.height;
	}
	
	public int extentFrom(Rectangle rect) {
		return isHorizontal() ? rect.width : rect.height;
	}
	
	public Range rangeFrom(Rectangle rect) {
		return rangeFrom(rect, null);
	}
	
	public Range rangeFrom(Rectangle rect, Range range) {
		if (range == null) {
			range = new Range();
		}
		if (isHorizontal()) {
			range.coord = rect.x;
			range.extent = rect.width;
		} else {
			range.coord = rect.y;
			range.extent = rect.height;
		}
		return range;
	}
	
	public Point updateCoord(Point pt, int value) {
		if (isHorizontal()) pt.x = value; else pt.y = value;
		return pt;
	}
	
	public Rectangle updateCoord(Rectangle rect, int value) {
		if (isHorizontal()) rect.x = value; else rect.y = value;
		return rect;
	}
	
	public Dimension updateExtent(Dimension dim, int value) {
		if (isHorizontal()) dim.width = value; else dim.height = value;
		return dim;
	}

	public Rectangle updateExtent(Rectangle rect, int value) {
		if (isHorizontal()) rect.width = value; else rect.height = value;
		return rect;
	}
	
	public Rectangle updateCoordExtent(Rectangle rect, int coord, int extent) {
		 return updateExtent(updateCoord(rect, coord), extent);
	}
	
	public Rectangle updateRange(Rectangle rect, Range range) {
		return updateExtent(updateCoord(rect, range.coord), range.extent);
	}
	
	public <T> T select(T horiz, T vert) {
		return isHorizontal() ? horiz : vert;
	}

	public <T> T select(Pair<T, T> pair) {
		return isHorizontal() ? pair.x : pair.y;
	}

	public int select(int horiz, int vert) {
		return isHorizontal() ? horiz : vert;
	}

	/**
	 * Check the compass direction with respect to this orientation.  If the
	 * orientation is horizontal, west directions return -1 and east directions
	 * return +1; if the orientation is vertical, north directions return -1 and
	 * south directions return +1. For other directions, it returns 0.
	 */
	public int testCompassDirection(int direction) {
		switch (direction) {
		case SwingConstants.NORTH_WEST: return -1;
		case SwingConstants.NORTH:      return isHorizontal() ? 0 : -1;
		case SwingConstants.NORTH_EAST: return isHorizontal() ? +1 : -1;
		case SwingConstants.WEST:       return isHorizontal() ? -1 : 0;
		case SwingConstants.CENTER:     return 0;
		case SwingConstants.EAST:       return isHorizontal() ? +1 : 0;
		case SwingConstants.SOUTH_WEST: return isHorizontal() ? -1 : +1;
		case SwingConstants.SOUTH:      return isHorizontal() ? 0 : +1;
		case SwingConstants.SOUTH_EAST: return isHorizontal() ? +1 : -1;
		default: throw new IllegalArgumentException("Argument must be Swing direction constant");
		}
	}
	
	public int normalizeCompassDirection(int direction) {
		return isHorizontal() ? GeomUtils.horizontalDir(direction) : GeomUtils.verticalDir(direction);
	}

	public static Orientation fromSwingConstant(int orientation) {
		switch (orientation) {
		case SwingConstants.HORIZONTAL:
			return HORIZONTAL;
		case SwingConstants.VERTICAL:
			return VERTICAL;
		default:
			throw new IllegalArgumentException("Value must be Swing constant HORIZONTAL or VERTICAL");
		}
	}
}
