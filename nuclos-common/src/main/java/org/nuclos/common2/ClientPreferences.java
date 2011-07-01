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
package org.nuclos.common2;

import org.nuclos.common.ApplicationProperties;

/**
 * Singleton for Nucleus client related user preferences.
 * Note that system preferences are not supported in Nucleus currently.
 * <b>DON'T MOVE THIS CLASS (org.nuclos.client.NucleusClientPreferences) TO ANOTHER PACKAGE WITHOUT ADJUSTING <code>getUserPreferences()</code></b>!
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class ClientPreferences {
	/**
	 * the one and only instance of NucleusClientPreferences
	 */

	private static final String PREFS_KEY_APPLICATIONVERSION = "applicationVersion";

	private ClientPreferences() {
	}

	public static java.util.prefs.Preferences getUserPreferences() {
		// Note that the Singleton is not really necessary, but we need to call getInstance().
		/** @todo use application name instead of "nucleus" */
		return java.util.prefs.Preferences.userRoot().node("org/nuclos/client");
	}

	/**
	 * @return the version of the application stored in the preferences. This is the version the
	 * current user launched most recently.
	 */
	public static String getMostRecentlyLaunchedVersion() {
		return getUserPreferences().get(PREFS_KEY_APPLICATIONVERSION, "0.0.0");
	}

	/**
	 * @return Is the currently launched version a new version for the current user?
	 */
	public static boolean isNewVersion() {
		return !getMostRecentlyLaunchedVersion().equals(ApplicationProperties.getInstance().getCurrentVersion().getVersionNumber());
	}
}	// class NucleusClientPreferences
