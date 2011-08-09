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

import org.nuclos.common.collection.Pair;
import org.nuclos.common.dblayer.JoinType;

public class DbJoin extends DbFrom {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final DbFrom left;
	private final JoinType joinType;
	private Pair<String, String> on;
	
	DbJoin(DbQuery<?> query, DbFrom left, JoinType joinType, String tableName) {
		super(query, tableName);
		this.left = left;
		this.joinType = joinType;
	}
	
	public DbFrom getParent() {
		return left;
	}

	public DbJoin on(String baseTableColumn, String joinedTableColumn) {
		if (on != null) {
			throw new IllegalStateException("Join criteria already set");
		}
		this.on = Pair.makePair(baseTableColumn, joinedTableColumn);
		return this;
	}
	
	public JoinType getJoinType() {
		return joinType;
	}
	
	public Pair<String, String> getOn() {
		return on;
	}
	
	@Override
	public DbJoin alias(String alias) {
		return (DbJoin) super.alias(alias);
	}
}
