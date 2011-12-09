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
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

import de.javasoft.plaf.synthetica.SyntheticaBlackMoonLookAndFeel;

public class NuclosSyntheticaThemeableLookAndFeel extends SyntheticaBlackMoonLookAndFeel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Map<String, String> mapNuclosThemes = new HashMap<String, String>();
	
	private static String NUCLOS_THEME;
	
	public NuclosSyntheticaThemeableLookAndFeel() throws ParseException {
		super();
	}

	@Override
	protected void loadCustomXML() throws ParseException {
		if (NUCLOS_THEME != null) {
			if (getClass().getResource(mapNuclosThemes.get(NUCLOS_THEME)) != null) {
				loadXMLConfig(mapNuclosThemes.get(NUCLOS_THEME));
			}
		}
	}

	/**
	 * 
	 * @return loaded nuclos theme name
	 */
	static String getNuclosTheme() {
		return NUCLOS_THEME;
	}

	/**
	 * 
	 * @param nuclos theme name
	 */
	void setNuclosTheme(String nuclosTheme) {
		NUCLOS_THEME = null;
		if (nuclosTheme != null) {
			nuclosTheme = nuclosTheme.trim();
			if (mapNuclosThemes.get(nuclosTheme) != null) {
				NUCLOS_THEME = nuclosTheme;
			}
		} 
		setWindowsDecorated(false);
		setLookAndFeel(getClass().getName(), true, true);
		updateNuclosThemeSettings();
	}
	
	/**
	 * update NuclosThemeSettings from nuclosTheme.xml
	 * e.g. NuclosInactiveField background
	 */
	private void updateNuclosThemeSettings() {
		NuclosThemeSettings.setDefaults();
		
		Color color;
		
		color = getDefaultColor("Panel.background");
		if (color != null) NuclosThemeSettings.BACKGROUND_PANEL = color;
			
		color = getDefaultColor("Nuclos.rootPane.background");
		if (color != null) NuclosThemeSettings.BACKGROUND_ROOTPANE = color;
		
		color = getDefaultColor("Nuclos.background3");
		if (color != null) NuclosThemeSettings.BACKGROUND_COLOR3 = color; 
		
		color = getDefaultColor("Nuclos.background4");
		if (color != null) NuclosThemeSettings.BACKGROUND_COLOR4 = color; 
		
		color = getDefaultColor("Nuclos.background5");
		if (color != null) NuclosThemeSettings.BACKGROUND_COLOR5 = color; 
		
		color = getDefaultColor("Nuclos.fieldInactive.background");
		if (color != null) NuclosThemeSettings.BACKGROUND_INACTIVEFIELD = color; 
		
		color = getDefaultColor("Nuclos.rowInactive.background");
		if (color != null) NuclosThemeSettings.BACKGROUND_INACTIVEROW = color; 
		
		color = getDefaultColor("Nuclos.columnInactive.background");
		if (color != null) NuclosThemeSettings.BACKGROUND_INACTIVECOLUMN = color; 
		
		color = getDefaultColor("Nuclos.columnInactiveSelected.background");
		if (color != null) NuclosThemeSettings.BACKGROUND_INACTIVESELECTEDCOLUMN = color; 
		
		color = getDefaultColor("Nuclos.bubble.background");
		if (color != null) NuclosThemeSettings.BUBBLE_FILL_COLOR = color; 
		
		color = getDefaultColor("Nuclos.bubble.border");
		if (color != null) NuclosThemeSettings.BUBBLE_BORDER_COLOR = color; 
	}
	
	static boolean registerNuclosTheme(String themeName, String pathToXml) {
		if (themeName != null && pathToXml != null) {
			pathToXml = pathToXml.trim();
			if (!pathToXml.startsWith("/")) {
				pathToXml = "/" + pathToXml;
			}
			if (NuclosSyntheticaThemeableLookAndFeel.class.getResource(pathToXml) != null) {
				mapNuclosThemes.put(themeName.trim(), pathToXml);
				return true;
			}
		}
		return false;
	}
	
	static Set<String> getNuclosThemes() {
		return new HashSet<String>(mapNuclosThemes.keySet());
	}
	
	private Color getDefaultColor(String key) {
		final Color defaultColor = UIManager.getColor(key);
		if (defaultColor == null) {
			return defaultColor;
		}
		
		if (defaultColor instanceof ColorUIResource) {
			ColorUIResource c = (ColorUIResource) defaultColor;
			return new Color(c.getRed(), c.getGreen(), c.getBlue());
		} else {
			return defaultColor;
		}
	}
}
