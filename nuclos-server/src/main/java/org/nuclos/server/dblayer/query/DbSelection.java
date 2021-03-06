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


public abstract class DbSelection<T> {

	private final DbQueryBuilder builder;

	private final Class<? extends T> javaType;

	private String alias;

	DbSelection(DbQueryBuilder builder, Class<? extends T> javaType) {
		this(builder, javaType, null);
	}

	DbSelection(DbQueryBuilder builder, Class<? extends T> javaType, String alias) {
		this.builder = builder;
		this.javaType = javaType;
		this.alias = alias;
	}

	public String getSqlColumnExpr() {
		throw new IllegalStateException("No column alias in " + this);
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("alias=").append(alias);
		result.append(", type=").append(javaType);
		result.append("]");
		return result.toString();
	}

	public final Class<? extends T> getJavaType() {
		return javaType;
	}

	public DbSelection<T> alias(String alias) {
		this.alias = alias;
		return this;
	}

	public String getAlias() {
		return this.alias;
	}

	final DbQueryBuilder getBuilder() {
		return builder;
	}
}
