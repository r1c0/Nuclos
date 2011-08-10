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

import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dblayer.JoinType;

public class DbFrom implements Serializable {
	
	private final DbQuery<?> query;
	private final String tableName;
	private String alias;
	private Set<DbJoin> joins;
	
	// private final MetaDataProvider mdProv = MetaDataServerProvider.getInstance();
	
	DbFrom(DbQuery<?> query, String tableName) {
		this.query = query;
		this.tableName = tableName;
		this.joins = new LinkedHashSet<DbJoin>();
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("table=").append(tableName);
		result.append(", alias=").append(alias);
		result.append(", joins=").append(joins);
		result.append(", query=").append(query);
		result.append("]");
		return result.toString();
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
	
	public DbJoin join(String tableName, JoinType joinType) {
		DbJoin join = new DbJoin(query, this, joinType, tableName);
		joins.add(join);
		return join;
	}
	
	/**
	 * An alternative to join().alias().on() for usability would be very nice...
	public DbJoin join(EntityMetaDataVO joinEntity, JoinType joinType) {
		DbJoin join = new DbJoin(query, this, joinType, joinEntity.getDbEntity());
		joins.add(join);

		final EntityFieldMetaDataVO ref = mdProv.getRefField(entity.getEntity(), joinEntity.getEntity());
		String foreignEntityField = ref.getForeignEntityField();
		// TODO: ???
		if (foreignEntityField == null) {
			foreignEntityField = "INTID";
		}
		
		return join.alias(joinEntity.getEntity()).on(foreignEntityField, ref.getField());
	}
	 */
	
	public DbJoin innerJoin(String tableName) {
		return join(tableName, JoinType.INNER);
	}
	
	public DbJoin leftJoin(String tableName) {
		return join(tableName, JoinType.LEFT);
	}
	
	public <T> DbColumnExpression<T> column(String columnName, Class<T> javaClass) {
		return new DbColumnExpression<T>(this, columnName, javaClass);
	}
	
	DbQuery<?> getQuery() {
		return query;
	}
	
	/**
	 * An alternative to column() for usability.
	 */
	public <T> DbColumnExpression<T> field(EntityFieldMetaDataVO field) {
		if (field.getPivotInfo() != null) {
			throw new IllegalArgumentException(field.toString());
		}
		try {
			return new DbColumnExpression<T>(this, field.getDbColumn(), (Class<T>) Class.forName(field.getDataType()));
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e.toString());
		}
	}
	
	@Deprecated
	/** @deprecated Only use case-sensitive columns if needed. */
	public <T> DbColumnExpression<T> columnCaseSensitive(String columnName, Class<T> javaClass) {
		return new DbColumnExpression<T>(this, columnName, javaClass, true);
	}
}
