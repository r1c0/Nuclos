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
package org.nuclos.server.dblayer;

import java.io.Serializable;

public final class DbIdent implements Serializable, Comparable<DbIdent> {

	public static DbIdent makeIdent(String name) {
		return new DbIdent(name);
	}
	
	private final String	name;

	private DbIdent(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (obj == this) {
			return true;
		} else if (obj instanceof DbIdent) {
			return name.equals(((DbIdent) obj).name);
		}
		return false;
	}
	
	@Override
	public int compareTo(DbIdent other) {
		return name.compareTo(other.name);
	}
	
	@Override
	public String toString() {
		return name;
	}
}
