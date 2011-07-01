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

public class CommandInformationMessage extends CommandMessage {
	public static final int CMD_INFO_SHUTDOWN = 2;
	private String info;
	private boolean needLogout = true;
	
	public CommandInformationMessage(int iCommand) {
		super(iCommand);
	}

	public CommandInformationMessage(int iCommand, String info) {
		super(iCommand);
		this.info = info;
	}
	
	public CommandInformationMessage(int iCommand, String info, boolean needLogout) {
		super(iCommand);
		this.info = info;
		this.needLogout = needLogout;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public boolean isNeedLogout() {
		return needLogout;
	}

	public void setNeedLogout(boolean needLogout) {
		this.needLogout = needLogout;
	}
}
