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

import java.awt.Cursor;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.SwingConstants;

/**
 * Utility class with some geometry methods.
 */
public class GeomUtils implements SwingConstants {

	private GeomUtils() {
	}
	
	public static Rectangle absoluteRect(int x, int y, int x2, int y2) {
		return new Rectangle(x, y, x2 - x, y2 - y);
	}
	
	public static void normalizeRect(Rectangle rect) {
		if (rect.width < 0) {
			rect.width = -rect.width;
			rect.x -= rect.width;
		}
		if (rect.height < 0) {
			rect.height = -rect.height;
			rect.y -= rect.height;
		}
	}

	/**
	 * If the rect contains the given point, returns the compass direction if the point
	 * is on an edge/corner as specified by the given insets.
	 * If the rect does not contain the given point, it returns -1.
	 */
	public static int findInsetDirection(Rectangle rect, Point p, Insets insets) {
		if (!rect.contains(p))
			return -1;
		int y = p.y - rect.y, v = CENTER;
		if (y < insets.top) {
			v = NORTH;
		} else if (rect.height - y < insets.bottom) {
			v = SOUTH;
		}
		int x = p.x - rect.x, h = CENTER;
		if (x < insets.left) {
			h = WEST;
		} else if (rect.width - x < insets.right) {
			h = EAST;
		}
		return vectorDir(v, h);
	}

	public static void resize(Rectangle rect, int direction, Point p, Point p2) {
		int dy = p2.y - p.y;
		switch (verticalDir(direction)) {
		case NORTH:
			rect.y += dy;
			rect.height -= dy;
			break;
		case SOUTH:
			rect.height += dy;
			break;
		}
		int dx = p2.x - p.x;
		switch (horizontalDir(direction)) {
		case WEST:
			rect.x += dx;
			rect.width -= dy;
			break;
		case EAST:
			rect.width += dx;
			break;
		}
	}
	
	/**
	 * Extracts the horizontal component of the direction.
	 * @return WEST, EAST or CENTER.
	 */
	public static int horizontalDir(int direction) {
		checkDirection(direction);
		return HORIZONTAL_COMPONENT[direction];
	}

	/**
	 * Extracts the vertical component of the direction
	 * @return NORTH, SOUTH or CENTER
	 */
	public static int verticalDir(int direction) {
		checkDirection(direction);
		return VERTICAL_COMPONENT[direction];
	}

	public static int vectorDir(int vert, int horiz) {
		int v = verticalDir(vert);
		int h = horizontalDir(horiz);
		if (vert != v && horiz != h) {
			throw new IllegalArgumentException("Invalid directions");
		}
		return VECTOR_LOOKUP[h + v % 3];
	}

	/**
	 * Converts a Swing direction constant into a string, useful for debugging.
	 */
	public static String directionToString(int direction) {
		if (direction < 0 || direction > 8) {
			return null;
		}
		return DIRECTION_STRING[direction];
	}
	
	public static int getResizeCursor(int direction) {
		checkDirection(direction);
		switch (direction) {
			case NORTH:      return Cursor.N_RESIZE_CURSOR;
			case NORTH_EAST: return Cursor.NE_RESIZE_CURSOR;
			case EAST:       return Cursor.E_RESIZE_CURSOR;
			case SOUTH_EAST: return Cursor.SE_RESIZE_CURSOR;
			case SOUTH:      return Cursor.S_RESIZE_CURSOR;
			case SOUTH_WEST: return Cursor.SW_RESIZE_CURSOR;
			case WEST:       return Cursor.W_RESIZE_CURSOR;
			case NORTH_WEST: return Cursor.NW_RESIZE_CURSOR;
		}
		return -1;
	}
	
	private static void checkDirection(int direction) {
		if (direction < 0 || direction > 8) {
			throw new IllegalArgumentException("Argument (" + direction + ") must be a Swing direction constant");
		}
	}

	// To unterstand the arrays and direction arithmetics, here the values for the directional constants:
	//  NW=8   N=1   NE=2
	//  W=7     0     E=3
	//  SW=6   S=5   SE=4

	private static final int VERTICAL_COMPONENT[]   = {0, 1, 1, 0, 5, 5, 5, 0, 1};

	private static final int HORIZONTAL_COMPONENT[] = {0, 0, 3, 3, 3, 0, 7, 7, 7};

	/** Lookup table with combined v/h directions using {@code h+v%3} as index. */
	private static final int VECTOR_LOOKUP[] = {0, 1, 5, 3, 2, 4, -1, 7, 8, 6};

	private static final String DIRECTION_STRING[] = {
		"CENTER", "NORTH", "NORTH_EAST",
		"EAST", "SOUTH_EAST", "SOUTH",
		"SOUTH_WEST", "WEST", "NORTH_WEST"
	};

}
