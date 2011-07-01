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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.DbIdent;


/**
 * This artifact represents a Nuclos generated view, i.e. a view which maps...  
 * 
 * Note that this artifact is a table artifact, i.e. it belongs to its base table.
 */
public class DbSimpleView extends DbTableArtifact {

	public static enum DbSimpleViewColumnType {
		TABLE,
		FOREIGN_REFERENCE,
		FUNCTION;
	}
	
	public static class DbSimpleViewColumn implements Serializable {

		private final DbSimpleViewColumnType type;
		private final String columnName;
		private final DbColumnType columnType;
		private DbReference reference;
		private List<?> viewPattern;
		private String functionName;
		private String[] argColumns;
		
		public DbSimpleViewColumn(String columnName) {
			this.type = DbSimpleViewColumnType.TABLE;
			this.columnName = columnName;
			this.columnType = null;
			this.reference = null;
			this.viewPattern = null;
		}

		public DbSimpleViewColumn(String columnName, DbColumnType columnType, String functionName, String...argColumns) {
			this.type = DbSimpleViewColumnType.FUNCTION;
			this.columnName = columnName;
			this.columnType = columnType;
			this.functionName = functionName;
			this.argColumns = argColumns;
		}
		
		public DbSimpleViewColumn(String columnName, DbColumnType columnType, DbReference reference, List<?> viewPattern) {
			this.type = DbSimpleViewColumnType.FOREIGN_REFERENCE;
			this.columnName = columnName;
			this.columnType = columnType;
			this.reference = reference;
			this.viewPattern = viewPattern;
			if (viewPattern.isEmpty()) {
				throw new IllegalArgumentException("Empty view pattern");
			}
			for (Object obj : viewPattern) {
				if (!(obj instanceof String || obj instanceof DbIdent)) {
					throw new IllegalArgumentException("View pattern list contains illegal entry");
				}
			}
		}
		
		public DbSimpleViewColumnType getViewColumnType() {
			return type;
		}
		
		public String getColumnName() {
			return columnName;
		}
		
		public DbColumnType getColumnType() {
			return columnType;
		}
		
		public DbReference getReference() {
			return reference;
		}
		
		public List<?> getViewPattern() {
			return viewPattern;
		}		
		
		public String getFunctionName() {
			return functionName;
		}
		
		public List<String> getArgColumns() {
			return Arrays.asList(argColumns);
		}
		
		@Override
		public int hashCode() {
			return columnName.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof DbSimpleViewColumn) {
				DbSimpleViewColumn other = (DbSimpleViewColumn) obj;
				DbReference otherReference = other.getReference();
				return ObjectUtils.equals(columnName, other.columnName)
					// TODO: For backward compatibility (Nuclos < 2.6.1), ignore if columnType is null in order
					// to avoid regeneration of all system views (may lead to some problems, see NUCLOSINT-679).
					&& (columnType == null || other.columnType == null || ObjectUtils.equals(columnType, other.columnType))
					&& ((reference == null && otherReference == null)
						|| (reference != null && otherReference != null
							&& reference.getReferencedTableName().equals(otherReference.getReferencedTableName())
							&& reference.getTableName().equals(otherReference.getTableName())
							&& reference.getReferences().equals(otherReference.getReferences())))
					&& ObjectUtils.equals(viewPattern, other.viewPattern);
			}
			return false;
		}
	}
	
	private final List<DbSimpleViewColumn> viewColumns;
	
	public DbSimpleView(String tableName, String viewName, List<DbSimpleViewColumn> viewColumns) {
		super(tableName, viewName);
		this.viewColumns = viewColumns;
		for (DbSimpleViewColumn vc : viewColumns) {
			if (vc.getReference() != null && !vc.getReference().getTableName().equals(getTableName())) {
				throw new IllegalArgumentException(String.format("Column %s of view %s is a reference from a different table",
					vc.getColumnName(), getViewName()));
			}
		}
	}
	
	public String getViewName() {
		return getSimpleName();
	}
	
	public List<DbSimpleViewColumn> getViewColumns() {
		return viewColumns;
	}

	public List<String> getViewColumnNames() {
		return CollectionUtils.transform(viewColumns, new Transformer<DbSimpleViewColumn, String>() {
			@Override public String transform(DbSimpleViewColumn c) { return c.getColumnName(); }
		});		
	}
	
	public List<DbSimpleViewColumn> getReferencingViewColumns() {
		return CollectionUtils.applyFilter(viewColumns, new Predicate<DbSimpleViewColumn>() {
			@Override public boolean evaluate(DbSimpleViewColumn c) { return c.getReference() != null; }
		});
	}
	
	@Override
	protected boolean isUnchanged(DbArtifact a) {
		DbSimpleView other = (DbSimpleView) a;
		return viewColumns.equals(other.viewColumns);
	}
	
	@Override
	public <T> T accept(DbArtifactVisitor<T> visitor) throws DbException {
		return visitor.visitView(this);
	}
}
