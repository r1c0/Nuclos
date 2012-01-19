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
package org.nuclos.common2.communication;

import org.nuclos.common2.communication.exception.CommonCommunicationException;

/**
 * Interface for communicator classes.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version	00.01.000
 */
public interface Communicator {

	/**
	 * check addresses format for validity
	 * @param sAddress address to check format on
	 * @return true or false
	 */
	public boolean isValid(String sAddress);

	/**
	 * send a message to a recipient as sender
	 * @param sSender sender of message
	 * @param sRecipients recipients of message
	 * @param sSubject subject of message
	 * @param sMessage message content
	 * @throws CommonCommunicationException
	 */
	public void sendMessage(String sAuth, String sSender, String[] sRecipients, String sSubject, String sMessage) throws CommonCommunicationException;
}
