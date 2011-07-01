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

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import org.nuclos.server.dblayer.query.DbJoin.JoinType;

public class DbFrom implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final DbQuery<?> query;
	private final String tableName;
	private String alias;
	private Set<DbJoin> joins;
	
	DbFrom(DbQuery<?> query, String tableName) {
		this.query = query;
		this.tableName = tableName;
		this.joins = new LinkedHashSet<DbJoin>();
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public String getAlias() {
		return alias;
	}
	
	public DbFrom alias(String alias) {
		query.registerAlias(this, alias);
		this.alias = alias;
		return this;
	}
	
	public Set<DbJoin> getJoins() {
		return joins;
	}
	
	public DbJoin join(String tableName, DbJoin.JoinType joinType) {
		DbJoin join = new DbJoin(query, this, joinType, tableName);
		joins.add(join);
		return join;
	}
	
	public DbJoin innerJoin(String tableName) {
		return join(tableName, JoinType.INNER);
	}
	
	public DbJoin leftJoin(String tableName) {
		return join(tableName, JoinType.LEFT);
	}
	
	public <T> DbColumnExpression<T> column(String columnName, Class<T> javaClass) {
		return new DbColumnExpression<T>(this, columnName, javaClass);
	}
	
	@Deprecated
	/** @deprecated Only use case-sensitive columns if needed. */
	public <T> DbColumnExpression<T> columnCaseSensitive(String columnName, Class<T> javaClass) {
		return new DbColumnExpression<T>(this, columnName, javaClass, true);
	}
}
