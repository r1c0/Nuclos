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
package org.nuclos.common2.exception;

/**
 * <code>Exception</code> that might occur while reading from or writing to <code>java.util.prefs.Preferences</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class PreferencesException extends CommonBusinessException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PreferencesException(String sMessage) {
		super(sMessage);
	}

	public PreferencesException(String sMessage, Throwable tCause) {
		super(sMessage, tCause);
	}

	public PreferencesException(Throwable tCause) {
		super("preferences.error", tCause);//"Beim Lesen/Schreiben der Benutzereinstellungen ist ein Fehler aufgetreten.", tCause);
	}

}  // class PreferencesException
