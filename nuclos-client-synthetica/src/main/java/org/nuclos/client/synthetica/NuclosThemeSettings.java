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
package org.nuclos.client.synthetica;

import java.awt.Color;
import java.awt.Dimension;

public class NuclosThemeSettings {

	public static final Dimension TOOLBAR_BUTTON_SIZE = new Dimension(24,24);

	public static final Dimension TOOLBAR_SEPARATOR_H_MINIMUM = new Dimension(8, 24);
	public static final Dimension TOOLBAR_SEPARATOR_V_MINIMUM = new Dimension(24, 8);
	
	public static final Color ICON_BLUE = new Color(97, 171, 215);
	public static final Color ICON_BLUE_LIGHTER = new Color(130, 200, 255);

	/**
	 * Default for all panels:
	 * e.g. Starttab, Desktop, Entities
	 */
	public static Color BACKGROUND_PANEL;
	/**
	 * Background of frame
	 */
	public static Color BACKGROUND_ROOTPANE;
	/**
	 * Background color 3:
	 * e.g. status bar
	 */
	public static Color BACKGROUND_COLOR3;
	/**
	 * Background color 4:
	 * e.g. status bar spot
	 */
	public static Color BACKGROUND_COLOR4;
	/**
	 * Background color 5:
	 * e.g. status bar text selection
	 */
	public static Color BACKGROUND_COLOR5;
	/**
	 * Background of inactive/ not editable fields
	 */
	public static Color BACKGROUND_INACTIVEROW;
	/**
	 * Background of inactive/ not editable rows
	 */
	public static Color BACKGROUND_INACTIVEFIELD;
	/**
	 * Background of inactive/ not editable columns
	 */
	public static Color BACKGROUND_INACTIVECOLUMN;
	/**
	 * Background of inactive/ not editable selected columns
	 */
	public static Color BACKGROUND_INACTIVESELECTEDCOLUMN;
	/**
	 * Fill color for bubble notifications
	 */
	public static Color BUBBLE_FILL_COLOR;
	/**
	 * Border color for bubble notifications
	 */
	public static Color BUBBLE_BORDER_COLOR;
	
	public static void setDefaults() {
		BACKGROUND_PANEL = new Color(242, 243, 246);
		BACKGROUND_ROOTPANE = new Color(72, 76, 83);
		
		BACKGROUND_COLOR3 = new Color(109, 115, 126);
		BACKGROUND_COLOR4 = new Color(169, 184, 212);
		BACKGROUND_COLOR5 = new Color(71, 80, 94);

		BACKGROUND_INACTIVEROW = new Color(232, 233, 236);
		BACKGROUND_INACTIVEFIELD = new Color(232, 233, 236);
		BACKGROUND_INACTIVECOLUMN = new Color(232, 233, 236);
		BACKGROUND_INACTIVESELECTEDCOLUMN = new Color(99, 105, 114);
		
		BUBBLE_FILL_COLOR = new Color(255, 255, 160);
		BUBBLE_BORDER_COLOR = new Color(50, 50, 50);
	}
	
	static {
		setDefaults();
	}

}
