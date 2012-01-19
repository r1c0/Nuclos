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
package org.nuclos.server.dblayer.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.statements.DbStructureChange;
import org.nuclos.server.dblayer.statements.DbStructureChange.Type;
import org.nuclos.server.dblayer.structure.DbArtifact;
import org.nuclos.server.dblayer.structure.DbColumn;
import org.nuclos.server.dblayer.structure.DbConstraint.DbForeignKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbUniqueConstraint;
import org.nuclos.server.dblayer.structure.DbIndex;
import org.nuclos.server.dblayer.structure.DbSequence;
import org.nuclos.server.dblayer.structure.DbSimpleView;
import org.nuclos.server.dblayer.structure.DbTable;
import org.nuclos.server.dblayer.structure.DbTableColumnGroup;

public class SchemaUtils {
	
	private SchemaUtils() {
		// Never invoked.
	}

	public static List<DbStructureChange> create(DbArtifact artifact) {
		return create(Collections.singleton(artifact));
	}
	
	public static List<DbStructureChange> drop(DbArtifact artifact) {
		return drop(Collections.singleton(artifact));
	}

	public static List<DbStructureChange> modify(DbArtifact artifact1, DbArtifact artifact2) {
		return modify(Collections.singleton(artifact1), Collections.singleton(artifact2));
	}
	
	public static List<DbStructureChange> create(Collection<? extends DbArtifact> schema) throws DbException {
		List<DbArtifact> flatten = DbArtifact.flatten(schema, false);
		Collections.sort(flatten, DbArtifact.COMPARATOR);
		return CollectionUtils.transform(flatten, new Transformer<DbArtifact, DbStructureChange>() {
			@Override
			public DbStructureChange transform(DbArtifact artifact) {
				return new DbStructureChange(Type.CREATE, artifact);
			}
		});
	}	

	public static List<DbStructureChange> drop(Collection<? extends DbArtifact> schema) throws DbException {
		List<DbArtifact> flatten = DbArtifact.flatten(schema, false);
		Collections.sort(flatten, Collections.reverseOrder(DbArtifact.COMPARATOR));
		return CollectionUtils.transform(flatten, new Transformer<DbArtifact, DbStructureChange>() {
			@Override
			public DbStructureChange transform(DbArtifact artifact) {
				return new DbStructureChange(Type.DROP, artifact);
			}
		});
	}	

	public static List<DbStructureChange> modify(Collection<? extends DbArtifact> schema1, Collection<? extends DbArtifact> schema2) {
		List<DbStructureChange> changes = new ArrayList<DbStructureChange>();
		
		Map<String, DbArtifact> flattenedMap1 = DbArtifact.makeNameMap(DbArtifact.flatten(schema1, false));
		Map<String, DbArtifact> flattenedMap2 = DbArtifact.makeNameMap(DbArtifact.flatten(schema2, false));
		
		Map<String, DbArtifact> dropMap = new HashMap<String, DbArtifact>(flattenedMap1);
		dropMap.keySet().removeAll(flattenedMap2.keySet());
		Map<String, DbArtifact> createMap = new HashMap<String, DbArtifact>(flattenedMap2);
		createMap.keySet().removeAll(flattenedMap1.keySet());
		
		flattenedMap1.keySet().removeAll(dropMap.keySet());
		flattenedMap2.keySet().removeAll(createMap.keySet());
		
		// Now we have 4 maps: 
		// - dropMap with all artifacts in schema1 which are not in schema2 (obsolete artifacts)
		// - createMap with all artifacts in schema2 which are not in schema1 (new artifacts)
		// - flattenedMap1 and flattenedMap2 with artifacts in both
		for (String name : flattenedMap1.keySet()) {
			DbArtifact a1 = flattenedMap1.get(name);
			DbArtifact a2 = flattenedMap2.get(name);
			
			if (a1.equals(a2))
				continue;

			if (a1.isSameType(a2)) {
				if (a1 instanceof DbColumn) {
					changes.add(new DbStructureChange(Type.MODIFY, a1, a2));
					continue;
				} else if (a1 instanceof DbTable) {
					changes.addAll(modify(((DbTable) a1).getTableArtifacts(), ((DbTable) a2).getTableArtifacts()));
					continue;
				} else if (a1 instanceof DbSimpleView) {
					changes.add(new DbStructureChange(Type.MODIFY, a1, a2));
					continue;
				} else if (a1 instanceof DbSequence) {
					changes.add(new DbStructureChange(Type.MODIFY, a1, a2));
					continue;
				}
				// NOTE: If you add a special case for constraints or indices are here, you must also update
				// the code below that handles column-dependant artifacts.
			}	

			// Fallback: treat all other changes as drop/create sequence
			dropMap.put(name, a1);
			createMap.put(name, a2);
		}		

		// NUCLOSINT-714: We are now checking the column changes to drop/re-create dependant 
		// table artifacts (i.e. foreign key constraints, unique constraint and indices which 
		// include the concerned column)
		for (DbStructureChange change : changes) {
			if (change.getType() == Type.MODIFY && change.getArtifact1() instanceof DbColumn) {
				dropMap.putAll(findColumnDependantArtifacts((DbColumn) change.getArtifact1(), flattenedMap1));
				createMap.putAll(findColumnDependantArtifacts((DbColumn) change.getArtifact2(), flattenedMap2));
			}
		}
		
		changes.addAll(0, drop(dropMap.values()));
		changes.addAll(create(createMap.values()));
		
		return changes;
	}
	
	private static Map<String, DbArtifact> findColumnDependantArtifacts(DbColumn column, Map<String, DbArtifact> artifacts) {
		Map<String, DbArtifact> result = new HashMap<String, DbArtifact>();
		for (Map.Entry<String, DbArtifact> e : artifacts.entrySet()) {
			DbArtifact a = e.getValue();
			if (a instanceof DbForeignKeyConstraint || a instanceof DbUniqueConstraint || a instanceof DbIndex) {
				DbTableColumnGroup columnGroup = (DbTableColumnGroup) a;
				boolean dependant = columnGroup.getTableName().equals(column.getTableName())
						&& columnGroup.getColumnNames().contains(column.getColumnName());
				if (dependant) {
					result.put(e.getKey(), e.getValue());
				}
			}
		}
		return result;
	}
}
