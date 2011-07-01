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
package org.nuclos.client.main;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.Map;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

import org.nuclos.common.collection.CollectionUtils;

/**
 * Utility class to switch a locale in the swing-ui (which is not supported
 * officially, as the internal tables are filled once from the active resource
 * bundle and cannot be updated).
 * <p>
 * Thus, this class identifies the ideal "supported" locale, loads the resource
 * bundles for basic and windows look and feel, and copies the map into the ui
 * defaults.
 * <p>
 * As the underlying classes are officially marked as "internal", sun will eat
 * your pants if they catch you using this class.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:marc.jackisch@novabit.de">Marc Jackisch</a>
 * @version 01.00.00
 */
public class SwingLocaleSwitcher {
	private static List<String> supported =  Arrays.asList(
				"de", "es", "fr", "it", "ja", "ko", "sv",
				"zh_CN", "zh_HK", "zh_TW"
				);
	private static List<String> classBaseNames = Arrays.asList(
				"com.sun.swing.internal.plaf.basic.resources.basic",
				"com.sun.java.swing.plaf.windows.resources.windows"
				);
	
	/**
	 * Switch the locale to the given locale, including swing-resources for the
	 * windows look and feel
	 * @param locale the new locale
	 */
	public static void setLocale(Locale locale) {
		Locale.setDefault(locale);
		
		// Find the "best match" in the supported locales array and generate
		// the class suffix
		String sup = null;
		if(supported.contains(locale.getLanguage() + "_" + locale.getCountry()))
			sup = locale.getLanguage() + "_" + locale.getCountry();
		if(supported.contains(locale.getLanguage()))
			sup = locale.getLanguage();
		String suffix = sup == null ? "" : "_" + sup;
		
		// Load classes, copy resources
		Map<String, Object> m = new HashMap<String, Object>();
		for(String className : classBaseNames) {
			try {
				ListResourceBundle resources = (ListResourceBundle) Class.forName(className + suffix).newInstance();
				for(String k : CollectionUtils.asIterable(resources.getKeys()))
					m.put(k, resources.getObject(k));
			}
			catch(InstantiationException e) {
				e.printStackTrace();
			}
			catch(IllegalAccessException e) {
				e.printStackTrace();
			}
			catch(ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		UIDefaults uiDefs = UIManager.getDefaults();
		UIManager.getDefaults().setDefaultLocale(locale);
		uiDefs.putAll(m);
	}
}
