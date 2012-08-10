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

import java.sql.SQLException;

import org.nuclos.common.Priority;
import org.nuclos.common2.exception.CommonBusinessException;

public interface ConsoleFacadeRemote {

	/**
	 *
	 * @param sMessage the message to send
	 * @param sUser the receiver of this message (all users if null)
	 * @param priority
	 * @param sAuthor the author of the message
	 */
	void sendClientNotification(String sMessage, String sUser,
		Priority priority, String sAuthor);

	/**
	 * end all the clients of sUser
	 * @param sUser if null for all users
	 */
	void killSession(String sUser);

	/**
	 * check for VIEWS and FUNCTIONS which are invalid and compile them
	 */
	void compileInvalidDbObjects() throws SQLException;

	/**
	 * invalidateAllServerSide Caches
	 */
	String invalidateAllCaches();

	/**
	 * get Infomation about the database in use
	 */
	String getDatabaseInformationAsHtml();

	/**
	 * get the system properties of the server
	 */
	String getSystemPropertiesAsHtml();
	
	/**
	 * 
	 * @param sCommand
	 * @throws CommonBusinessException
	 */
	void executeCommand(String sCommand) throws CommonBusinessException;
	
	String[] rebuildConstraints();

}
