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
package org.nuclos.client.login;

/**
 * Event occuring on a user login.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public class LoginEvent extends java.util.EventObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String sUserName;
	private final String sServerName;

	public LoginEvent(Object source) {
		this(source, null, null);
	}

	public LoginEvent(Object source, String sUserName, String sServerName) {
		super(source);
		this.sUserName = sUserName;
		this.sServerName = sServerName;
	}

	/**
	 * @return the name of the authenticated user in case of a successful login.
	 */
	public String getAuthenticatedUserName() {
		return sUserName;
	}

	public String getConnectedServerName() {
		return sServerName;
	}

}	// class LoginEvent
