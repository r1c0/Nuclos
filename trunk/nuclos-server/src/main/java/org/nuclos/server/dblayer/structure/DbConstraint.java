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
import java.util.List;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.server.dblayer.DbException;

public abstract class DbConstraint extends DbTableArtifact implements DbTableColumnGroup {

	private final List<String>	columns;

	DbConstraint(String tableName, String constraintName, List<String> columns) {
		super(tableName, constraintName);
		this.columns = new ArrayList<String>(columns);
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("columns=").append(columns);
		result.append(", name=").append(getSimpleName());
		result.append("]");
		return result.toString();
	}

	public String getConstraintName() {
		return super.getSimpleName();
	}
	
	@Override
	public List<String> getColumnNames() {
		return columns;
	}
	
	public static class DbPrimaryKeyConstraint extends DbConstraint {
	
		public DbPrimaryKeyConstraint(String tableName, String constraintName, List<String> columns) {
			super(tableName, constraintName, columns);
		}

		public DbPrimaryKeyConstraint(String tableName, String constraintName, String...columns) {
			this(tableName, constraintName, Arrays.asList(columns));
		}
		
		@Override
			protected boolean isUnchanged(DbArtifact a) {
			DbPrimaryKeyConstraint other = (DbPrimaryKeyConstraint) a;
			return getColumnNames().equals(other.getColumnNames());
		}
		
		@Override
		public <T> T accept(DbArtifactVisitor<T> visitor) throws DbException {
			return visitor.visitPrimaryKeyConstraint(this);
		}
	}

	public static class DbUniqueConstraint extends DbConstraint {
		
		public DbUniqueConstraint(String tableName, String constraintName, List<String> columns) {
			super(tableName, constraintName, columns);
		}
		
		@Override
			protected boolean isUnchanged(DbArtifact a) {
			DbUniqueConstraint other = (DbUniqueConstraint) a;
			return getColumnNames().equals(other.getColumnNames());
		}

		@Override
		public <T> T accept(DbArtifactVisitor<T> visitor) throws DbException {
			return visitor.visitUniqueConstraint(this);
		}
	}
	
	public static class DbForeignKeyConstraint extends DbConstraint implements DbReference {
		
		private final String referencedTable;
		private final String referencedConstraint;
		private final List<String> referencedColumns;
		private final boolean onDeleteCascade;

		public DbForeignKeyConstraint(String tableName, String constraintName, List<String> columns, String referencedTable, String referencedConstraint, List<String> referencedColumns, boolean onDeleteCascade) {
			super(tableName, constraintName, columns);
			this.referencedTable = referencedTable;
			this.referencedConstraint = referencedConstraint;
			this.referencedColumns = new ArrayList<String>(referencedColumns);
			if (columns.size() != referencedColumns.size()) {
				throw new IllegalArgumentException(columns.size() + " column(s) but " + referencedColumns.size() + " referenced column(s) specified");
			}
			this.onDeleteCascade = onDeleteCascade;
		}
		
		@Override
		public String toString() {
			final StringBuilder result = new StringBuilder();
			result.append(getClass().getName()).append("[");
			result.append("columns=").append(getColumnNames());
			result.append(", name=").append(getSimpleName());
			result.append(", refTable=").append(referencedTable);
			result.append(", refColumns=").append(referencedColumns);
			result.append(", refConstr=").append(referencedConstraint);
			result.append(", onDeleteCascade=").append(onDeleteCascade);
			result.append("]");
			return result.toString();
		}

		@Override
		public String getReferencedTableName() {
			return referencedTable;
		}
		
		public String getReferencedConstraintName() {
			return referencedConstraint;
		}		
		
		@Override
		public List<String> getReferencedColumnNames() {
			return referencedColumns;
		}
		
		@Override
		public List<Pair<String, String>> getReferences() {
			return CollectionUtils.zip(getColumnNames(), getReferencedColumnNames());
		}
		
		public boolean isOnDeleteCascade() {
			return onDeleteCascade;
		}
		
		@Override
		protected boolean isUnchanged(DbArtifact a) {
			DbForeignKeyConstraint other = (DbForeignKeyConstraint) a;
			return getReferencedTableName().equals(other.getReferencedTableName())
				&& getReferences().equals(other.getReferences())
				&& (isOnDeleteCascade() == other.isOnDeleteCascade());
		}
		
		@Override
		public <T> T accept(DbArtifactVisitor<T> visitor) throws DbException {
			return visitor.visitForeignKeyConstraint(this);
		}
	}	
}
