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
package org.nuclos.server.dblayer.structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.PredicateUtils;
import org.nuclos.server.dblayer.DbException;

public class DbTable extends DbArtifact {
	
	private final List<DbTableArtifact> tableArtifacts;
	
	public DbTable(String tableName, DbTableArtifact...tableArtifacts) {
		this(tableName, Arrays.asList(tableArtifacts));
	}
	
	public DbTable(String tableName, Collection<? extends DbTableArtifact> tableArtifacts) {
		super(tableName);
		for (DbTableArtifact tableArtifact : tableArtifacts) {
			if (tableArtifact.getTableName() == null) {
				tableArtifact.setTableName(tableName);
			} else if (!tableName.equals(tableArtifact.getTableName())) {
				throw new IllegalArgumentException("Table artifact " + tableArtifact + " belongs to different table");
			}
		}
		this.tableArtifacts = new ArrayList<DbTableArtifact>(tableArtifacts);
		Collections.sort(this.tableArtifacts, DbArtifact.COMPARATOR);
	}
	
	public String getTableName() {
		return getSimpleName();
	}
	
	public List<DbTableArtifact> getTableArtifacts() {
		return tableArtifacts;
	}
	
	public <T extends DbTableArtifact> List<T> getTableArtifacts(Class<T> clazz) {
		return CollectionUtils.selectInstancesOf(tableArtifacts, clazz);
	}
	
	public List<DbColumn> getTableColumns() {
		return getTableArtifacts(DbColumn.class);
	}
	
	public Pair<DbTable, List<DbTableArtifact>> flatten() {
		return flatten(false);
	}
	
	public Pair<DbTable, List<DbTableArtifact>> flatten(boolean flattenColumns) {
		Predicate<DbTableArtifact> predicate; 
		if (flattenColumns) {
			predicate = PredicateUtils.alwaysFalse();
		} else {			
			predicate = PredicateUtils.<DbTableArtifact>isInstanceOf(DbColumn.class);
		}
		Pair<List<DbTableArtifact>,List<DbTableArtifact>> p = CollectionUtils.split(tableArtifacts, predicate);
		return Pair.makePair(new DbTable(getTableName(), p.x), p.y);
	}
	
	@Override
	protected boolean isUnchanged(DbArtifact a) {
		DbTable other = (DbTable) a;
		Map<String, DbTableArtifact> map = DbArtifact.makeNameMap(getTableArtifacts());
		Map<String, DbTableArtifact> otherMap = DbArtifact.makeNameMap(other.getTableArtifacts());
		return map.equals(otherMap);
	}
	
	@Override
	public <T> T accept(DbArtifactVisitor<T> visitor) throws DbException {
		return visitor.visitTable(this);
	}
}
