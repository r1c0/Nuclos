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

import javax.annotation.PostConstruct;
import javax.ejb.Remote;

import org.nuclos.common.Priority;
import org.nuclos.common2.exception.CommonBusinessException;

@Remote
public interface ConsoleFacadeRemote {

	@PostConstruct
	public abstract void postConstruct();

	/**
	 *
	 * @param sMessage the message to send
	 * @param sUser the receiver of this message (all users if null)
	 * @param priority
	 * @param sAuthor the author of the message
	 */
	public abstract void sendClientNotification(String sMessage, String sUser,
		Priority priority, String sAuthor);

	/**
	 * end all the clients of sUser
	 * @param sUser if null for all users
	 */
	public abstract void killSession(String sUser);

	/**
	 * check for VIEWS and FUNCTIONS which are invalid and compile them
	 */
	public abstract void compileInvalidDbObjects() throws SQLException;

	/**
	 * finds attribute values which should be assigned to a value list entry and creates a script to assign them if possible
	 * @return the number of bad attribute values found
	 */
//	public abstract int updateAttributeValueListAssignment(
//		final String sOutputFileName);

	/**
	 * invalidateAllServerSide Caches
	 */
	public abstract String invalidateAllCaches();

	/**
	 * get Infomation about the database in use
	 */
	public abstract String getDatabaseInformationAsHtml();

	/**
	 * get the system properties of the server
	 */
	public abstract String getSystemPropertiesAsHtml();
	
	/**
	 * 
	 * @param sCommand
	 * @throws CommonBusinessException
	 */
	public void executeCommand(String sCommand) throws CommonBusinessException;

}
