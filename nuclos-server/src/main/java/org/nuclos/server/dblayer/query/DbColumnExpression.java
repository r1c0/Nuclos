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
package org.nuclos.server.dblayer.query;

import org.nuclos.server.dblayer.impl.util.PreparedStringBuilder;

public class DbColumnExpression<T> extends DbExpression<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final DbFrom fromTable;
	private final String columnName;

	DbColumnExpression(DbFrom fromTable, String columnName, Class<T> javaType) {
		this(fromTable, columnName, javaType, false);
	}
	
	DbColumnExpression(DbFrom fromTable, String columnName, Class<T> javaType, boolean caseSensitive) {
		super(fromTable.query.getBuilder(), javaType, caseSensitive
			? PreparedStringBuilder.concat(fromTable.getAlias(), ".", "\"", columnName, "\"")
			: PreparedStringBuilder.concat(fromTable.getAlias(), ".", columnName));
		this.fromTable = fromTable;
		this.columnName = columnName;
	}
	
	public DbFrom getFrom() {
		return fromTable;
	}
	
	public String getColumnName() {
		return columnName;
	}
}
