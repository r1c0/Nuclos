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
package org.nuclos.common.database.query.definition;

import java.io.Serializable;
import java.sql.Types;

public enum DataType implements Serializable {
	
	VARCHAR(Types.VARCHAR, "VARCHAR", "VARCHAR2"),
	NUMERIC(Types.NUMERIC, "NUMERIC", "NUMBER"),
	INTEGER(Types.INTEGER, "INTEGER"),
	BLOB(Types.BLOB, "BLOB"),
	CLOB(Types.CLOB, "CLOB"),
	VARBINARY(Types.VARBINARY, "VARBINARY"),
	LONG_RAW(Types.LONGVARBINARY, "LONG RAW"),
	// TODO_AUTOSYNC: check date types
	DATE(Types.TIMESTAMP, "DATE"),
	TIMESTAMP(Types.TIMESTAMP, "TIMESTAMP", "DATETIME"),
	;
	
	public static DataType findByName(String name) {
		return findByName(name, false);
	}
	
	public static DataType findByName(String name, boolean legacy) {
		for (DataType dataType : DataType.values()) {
			if (name.equals(dataType.name))
				return dataType;
			if (legacy) {
				for (String legacyName : dataType.legacyNames) {
					if (name.equals(legacyName))
						return dataType;
				}
			}
		}
		return null;
	}
	
	private final String name;
	private final String[] legacyNames;
	
	private DataType(int type, String name, String...legacyNames) {
		this.name = name;
		this.legacyNames = legacyNames;
	}
	
	public void show() {
		System.out.println("DataType: " + name);
	}

	public String getTypeName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public boolean isSameType(DataType other) {
		return this == other;
	}
}
