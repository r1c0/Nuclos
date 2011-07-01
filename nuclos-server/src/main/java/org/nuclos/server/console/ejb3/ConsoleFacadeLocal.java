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
package org.nuclos.server.console.ejb3;

import javax.ejb.Local;

import org.nuclos.common.Priority;

@Local
public interface ConsoleFacadeLocal {

	/**
	 *
	 * @param sMessage the message to send
	 * @param sUser the receiver of this message (all users if null)
	 * @param priority
	 * @param sAuthor the author of the message
	 */
	public abstract void sendClientNotification(String sMessage, String sUser,
		Priority priority, String sAuthor);

}
