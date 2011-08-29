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

import org.nuclos.common.dblayer.JoinType;

public class DbJoin extends DbFrom {

	private final DbFrom left;
	private final JoinType joinType;
	
	// Old implementation, bad because it does not support non-equi joins.
	// private Pair<String, String> on;
	
	private DbCondition on;
	
	DbJoin(DbQuery<?> query, DbFrom left, JoinType joinType, String tableName) {
		super(query, tableName);
		this.left = left;
		this.joinType = joinType;
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("from=").append(left);
		result.append(", type=").append(joinType);
		result.append(", on=").append(on);
		result.append("]");
		return result.toString();
	}

	public DbFrom getParent() {
		return left;
	}

	/**
	 * Simple equi-join.
	 */
	public DbJoin on(String baseTableColumn, String joinedTableColumn, Class<?> type) {
		if (on != null) {
			throw new IllegalStateException("Join criteria already set");
		}
		final DbQueryBuilder b = left.getQuery().getBuilder();
		this.on = b.equal(left.baseColumn(baseTableColumn, type), baseColumn(joinedTableColumn, type));
		return this;
	}
	
	/**
	 * Simple equi-join with additional constraints.
	 */
	public DbJoin onAnd(String baseTableColumn, String joinedTableColumn, Class<?> type, DbCondition...cond) {
		if (on != null) {
			throw new IllegalStateException("Join criteria already set");
		}
		final DbQueryBuilder b = left.getQuery().getBuilder();
		this.on = b.and2(b.equal(left.baseColumn(baseTableColumn, type), baseColumn(joinedTableColumn, type)), cond);
		return this;
	}
	
	/**
	 * Non equi-join.
	 */
	public DbJoin onAnd(DbCondition...cond) {
		if (on != null) {
			throw new IllegalStateException("Join criteria already set");
		}
		final DbQueryBuilder b = left.getQuery().getBuilder();
		this.on = b.and(cond);
		return this;
	}
	
	public JoinType getJoinType() {
		return joinType;
	}
	
	public DbCondition getOn() {
		return on;
	}
	
	@Override
	public DbJoin alias(String alias) {
		return (DbJoin) super.alias(alias);
	}
}
