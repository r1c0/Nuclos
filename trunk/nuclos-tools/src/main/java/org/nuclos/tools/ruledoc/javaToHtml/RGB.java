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
package org.nuclos.tools.ruledoc.javaToHtml;

/**
 * A color representation similar to  java.awt.Color, but more lightweight, since it does not rquire GUI
 * libraries.
 *
 */
public class RGB {
	public static final RGB MAGENTA = new RGB(255, 0, 255);
	public static final RGB GREEN = new RGB(0, 255, 0);
	public static final RGB BLACK = new RGB(0, 0, 0);
	public static final RGB RED = new RGB(255, 0, 0);
	public static final RGB WHITE = new RGB(255, 255, 255);
	public static final RGB ORANGE = new RGB(255, 200, 0);
	public final static RGB CYAN = new RGB(0, 255, 255);
	public final static RGB BLUE = new RGB(0, 0, 255);
	public final static RGB LIGHT_GRAY = new RGB(192, 192, 192);
	public final static RGB GRAY = new RGB(128, 128, 128);
	public final static RGB DARK_GRAY = new RGB(64, 64, 64);
	public final static RGB YELLOW = new RGB(255, 255, 0);
	public final static RGB PINK = new RGB(255, 175, 175);

	private int red;
	private int green;
	private int blue;

	public RGB(int red, int green, int blue) {
		assertColorValueRange(red, green, blue);
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	private static void assertColorValueRange(int red, int green, int blue) {
		boolean rangeError = false;
		String badComponentString = "";
		if (red < 0 || red > 255) {
			rangeError = true;
			badComponentString = badComponentString + " Red";
		}
		if (green < 0 || green > 255) {
			rangeError = true;
			badComponentString = badComponentString + " Green";
		}
		if (blue < 0 || blue > 255) {
			rangeError = true;
			badComponentString = badComponentString + " Blue";
		}
		if (rangeError == true) {
			throw new IllegalArgumentException("Color parameter outside of expected range:"
					+ badComponentString);
		}
	}

	public int getRed() {
		return red;
	}

	public int getGreen() {
		return green;
	}

	public int getBlue() {
		return blue;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof RGB)) {
			return false;
		}
		RGB other = (RGB) obj;
		return other.getRed() == getRed()
				&& other.getGreen() == getGreen()
				&& other.getBlue() == getBlue();
	}

	@Override
	public int hashCode() {
		return ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);
	}

	/**
	 * Returns a string containing a concise, human-readable
	 * description of the receiver.
	 *
	 * @return a string representation of the <code>RGB</code>
	 */
	@Override
	public String toString() {
		return "RGB {" + red + ", " + green + ", " + blue + "}"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
}
