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
package org.nuclos.common;

import java.io.Serializable;

/**
 * CommandMessage is used to be send as a JMS message to the client.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars Rueckemann</a>
 * @version 01.00.00
 */
public class CommandMessage implements Serializable {

	public static final int CMD_SHUTDOWN = 1;

	private int iCommand;

	public CommandMessage(int iCommand) {
		this.setCommand(iCommand);
	}

	/**
	 * @return Returns the iCommand.
	 */
	public int getCommand() {
		return iCommand;
	}

	/**
	 * @param command The iCommand to set.
	 */
	public void setCommand(int command) {
		iCommand = command;
	}
	
	@Override
	public String toString() {
		return "CommandMessage[" + iCommand + "]";
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof CommandMessage)) return false;
		final CommandMessage o = (CommandMessage) other;
		return iCommand == o.iCommand;
	}
	
	@Override
	public int hashCode() {
		return 267261 + iCommand;
	}
	
}
