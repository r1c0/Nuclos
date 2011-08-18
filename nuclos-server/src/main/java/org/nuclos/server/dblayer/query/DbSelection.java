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

import java.util.List;


public abstract class DbSelection<T> {

	private String tableAlias;
	private DbQueryBuilder builder;
	private Class<? extends T> javaType;
	
	DbSelection(DbQueryBuilder builder, Class<? extends T> javaType) {
		this.builder = builder;
		this.javaType = javaType;
	}

	DbSelection(DbQueryBuilder builder, Class<? extends T> javaType, String tableAlias) {
		this.builder = builder;
		this.javaType = javaType;
		this.tableAlias = tableAlias;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("tableAlias=").append(tableAlias);
		result.append(", type=").append(javaType);
		result.append("]");
		return result.toString();
	}

	public final Class<? extends T> getJavaType() {
		return javaType;
	}
		
	public final String getTableAlias() {
		return tableAlias;
	}
		
	public final DbSelection<T> alias(String alias) {
		if (this.tableAlias != null)
			throw new IllegalStateException();
		return replaceAlias(alias);
	}
	
	final DbSelection<T> replaceAlias(String alias) {
		this.tableAlias = alias;
		return this;
	}

	public boolean isCompoundSelection() {
		return false;
	}
	
	public List<DbSelection<?>> getCompoundSelectionItems() {
		throw new IllegalStateException();
	}
	
	final DbQueryBuilder getBuilder() {
		return builder;
	}
}
