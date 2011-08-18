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

	private String columnAlias;
	
	private final String columnName;

	DbColumnExpression(String alias, DbFrom fromTable, String columnName, Class<T> javaType) {
		this(alias, fromTable, columnName, javaType, false);
	}
	
	DbColumnExpression(String tableAlias, DbFrom fromTable, String columnName, Class<T> javaType, boolean caseSensitive) {
		super(fromTable.getQuery().getBuilder(), javaType, tableAlias, caseSensitive
			? PreparedStringBuilder.concat(fromTable.getAlias(), ".", "\"", columnName, "\"")
			: PreparedStringBuilder.concat(fromTable.getAlias(), ".", columnName));
		if (fromTable.getAlias() == null) {
			throw new IllegalArgumentException("Table alias in DbFrom must not be null on table " + fromTable.getTableName());
		}
		this.columnName = columnName;
	}
	
	public final DbExpression<T> columnAlias(String columnAlias) {
		if (this.columnAlias != null) throw new IllegalArgumentException(
				"Tried to change column alias from " + this.columnAlias + " to " + columnAlias);
		this.columnAlias = columnAlias;
		return this;
	}
	
	public final String getColumnAlias() {
		return columnAlias;
	}
	
	public final String getColumnName() {
		return columnName;
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("tableAlias=").append(getTableAlias());
		result.append("columnAlias=").append(getColumnAlias());
		result.append(", type=").append(getJavaType());
		result.append(", column=").append(columnName);
		result.append("]");
		return result.toString();
	}

}
