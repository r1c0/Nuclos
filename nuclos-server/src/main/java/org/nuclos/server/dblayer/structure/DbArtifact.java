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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.ComparatorUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.structure.DbConstraint.DbForeignKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbPrimaryKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbUniqueConstraint;

/**
 * Abstrat base class for any kind of database artifact.
 * 
 * Artifacts are tables, columns, constraints (primary key, foreign key, unique) as well
 * as indices.
 *
 */
public abstract class DbArtifact implements Serializable {

	public static Comparator<DbArtifact> COMPARATOR = ComparatorUtils.byClassComparator(
		DbSequence.class,
		DbTable.class,	DbColumn.class,
		DbPrimaryKeyConstraint.class, DbForeignKeyConstraint.class, DbUniqueConstraint.class,
		DbIndex.class,
		DbSimpleView.class,
		DbCallable.class);
	
	private final String name;
	private String comment;
	private Map<String, String> hints;
	
	DbArtifact(String name) {
		this.name = name;
	}
	
	public String getSimpleName() {
		return name;
	}
	
	public String getArtifactName() {
		return name;
	}
	
	/**
	 * Returns a remark about this artifact.
	 */
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getHint(String hint) {
		return (hints != null) ? hints.get(hint) : null;
	}
	
	public void setHint(String hint, String value) {
		if (value != null) {
			if (hints == null) {
				hints = new HashMap<String, String>();
			}
			hints.put(hint, value);
		} else if (hints != null) {
			hints.remove(hint);
		}
	}
	
	/**
	 * Returns a list of hints which may be used for database specific tuning. 
	 */
	public Map<String, String> getHints() {
		return (hints != null) ? hints : Collections.<String, String>emptyMap();
	}
	
	public boolean isSameType(DbArtifact other) {
		return getClass() == other.getClass();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		DbArtifact a = (DbArtifact) obj;
		return getArtifactName().equals(a.getArtifactName()) && isSameType(a) && isUnchanged(a);
	}
	
	@Override
	public int hashCode() {
		return getArtifactName().hashCode();
	}
	
	@Override
	public String toString() {
		return super.toString() + "[artifactName=" + getArtifactName() + "]";
	}
	
	/**
	 * Checks whether this artifact is "equivalent" to the given artifact.
	 * @throws ClassCastException if the other artifact is a different
	 * type
	 */
	public boolean isAltered(DbArtifact a) {
		return ! isUnchanged(a);
	}

	protected abstract boolean isUnchanged(DbArtifact a) throws ClassCastException;
	
	public abstract <T> T accept(DbArtifactVisitor<T> visitor) throws DbException;
	
	public static void acceptAll(Collection<? extends DbArtifact> artifacts, DbArtifactVisitor<?> visitor) throws DbException {
		for (DbArtifact artifact : artifacts)
			artifact.accept(visitor);
	}
	
   /**
    * Flattens the schema, so that all table definitions are simple
    * @param artifacts
    * @return
    */
   public static List<DbArtifact> flatten(Collection<? extends DbArtifact> artifacts, final boolean flattenColumns) {
      List<DbArtifact> list = CollectionUtils.concatTransform(artifacts, new FlatteningVisitior(flattenColumns));
      Collections.sort(list, DbArtifact.COMPARATOR);
      return list;
   }
   
   public static <A extends DbArtifact> Map<String, A> makeNameMap(Collection<? extends A> artifacts) {
   	// Not using CollectionUtils.transformIntoMap because we want to preserve the order (LinkedHashMap)
   	Map<String, A> nameMap = new LinkedHashMap<String, A>();
   	for (A a : artifacts)
   		nameMap.put(a.getArtifactName(), a);
   	return nameMap;
   }	

   public static <A extends DbArtifact> Map<String, A> makeSimpleNameMap(Collection<? extends A> artifacts) {
   	// Not using CollectionUtils.transformIntoMap because we want to preserve the order (LinkedHashMap)
   	Map<String, A> nameMap = new LinkedHashMap<String, A>();
   	for (A a : artifacts)
   		nameMap.put(a.getSimpleName(), a);
   	return nameMap;
   }	

	private static final class FlatteningVisitior extends AbstractDbArtifactVisitor<Collection<DbArtifact>> {
		
		private final boolean flattenColumns;

		private FlatteningVisitior(boolean flattenColumns) {
			this.flattenColumns = flattenColumns;
		}

		@Override
		public Collection<DbArtifact> visitTable(DbTable table) throws DbException {
		   Pair<DbTable, List<DbTableArtifact>> t = table.flatten(flattenColumns);
		   Collection<DbArtifact> flatten = new ArrayList<DbArtifact>();
		   flatten.add(t.x);
		   flatten.addAll(t.y);
		   return flatten;
		}

		@Override
		protected Collection<DbArtifact> fallback(DbArtifact artifact) throws DbException {
		   return Collections.singleton(artifact);
		}
	}
}
