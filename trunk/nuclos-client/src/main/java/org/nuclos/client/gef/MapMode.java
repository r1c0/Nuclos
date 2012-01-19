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
/**
 * Map mode.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */

package org.nuclos.client.gef;

import java.awt.Toolkit;

public class MapMode {
	/**
	 * Pixel coordinate mode
	 */
	public static final int MM_PIXEL = 1;
	/**
	 * all coordinates are defined in millimeters
	 */
	public static final int MM_HIMETRIC = 2;
	/**
	 * Screen resolution
	 */
	public static int DPI = Toolkit.getDefaultToolkit().getScreenResolution();

	protected int mode;

	/**
	 * Default Constructor uses pixel mode mapping
	 */
	public MapMode() {
		mode = MM_PIXEL;
	}

	/**
	 * Converts a pixel component to mm
	 * @param value
	 * @return
	 */
	public static double convertPixel2Metric(double value) {
		return value * DPI / 25.4d;
	}

	/**
	 * Converts a mm component to pixel
	 * @param value
	 * @return
	 */
	public static double convertMetric2Pixel(double value) {
		return value / 25.4d * DPI;
	}

	/**
	 * Gets the selected map mode
	 * @return The current map mode
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * Sets the map mode
	 * @param newMode
	 */
	public void setMode(int newMode) {
		mode = newMode;
	}
}
