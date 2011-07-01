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
package org.nuclos.client.wizard.model;

public class UserRights {
	
	String sGroup;
   String sRight;
	
	public UserRights(String sGroup, String sRight) {
		super();
		this.sGroup = sGroup;
		this.sRight = sRight;
	}

	public String getGroup() {
		return sGroup;
	}

	public void setGroup(String group) {
		this.sGroup = group;
	}

	public String getRight() {
		return sRight;
	}

	public void setRight(String right) {
		this.sRight = right;
	}


}
