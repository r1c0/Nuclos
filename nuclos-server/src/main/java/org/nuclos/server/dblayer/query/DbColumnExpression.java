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

/**
 * TODO: Is there really any difference between DbSelections and DbColumnExpressions?
 *   What does it mean a column and/or table alias here?
 */
public class DbColumnExpression<T> extends DbExpression<T> {

	private String tableAlias;

	private final String columnName;

	DbColumnExpression(String tableAlias, DbFrom fromTable, String columnName, Class<T> javaType) {
		this(tableAlias, fromTable, columnName, javaType, false);
	}

	DbColumnExpression(String tableAlias, DbFrom fromTable, String columnName, Class<T> javaType, boolean caseSensitive) {
		super(fromTable.getQuery().getBuilder(), javaType, caseSensitive
			? PreparedStringBuilder.concat(tableAlias, ".", "\"", columnName, "\"")
			: PreparedStringBuilder.concat(tableAlias, ".", columnName));
		if (fromTable.getAlias() == null) {
			throw new IllegalArgumentException("Table alias in DbFrom must not be null on table " + fromTable.getTableName());
		}
		this.tableAlias = tableAlias;
		this.columnName = columnName;
	}

	public final DbExpression<T> tableAlias(String tableAlias) {
		if (this.tableAlias != null) throw new IllegalArgumentException(
				"Tried to alter table alias from " + this.tableAlias + " to " + tableAlias);
		this.tableAlias = tableAlias;
		return this;
	}

	public final String getTableAlias() {
		return tableAlias;
	}

	public final String getColumnName() {
		return columnName;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("tableAlias=").append(getTableAlias());
		result.append(", Alias=").append(getAlias());
		result.append(", type=").append(getJavaType());
		result.append(", column=").append(columnName);
		final PreparedStringBuilder psb = getSqlString();
		if (psb != null) {
			result.append(", sql=").append(psb);
			result.append(", frozen=").append(psb.isFrozen());
		}
		result.append("]");
		return result.toString();
	}

}
