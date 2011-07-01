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

import org.apache.commons.lang.ObjectUtils;
import org.nuclos.server.dblayer.DbException;

public class DbColumn extends DbTableArtifact {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final DbColumnType	columnType;
	private final DbNullable nullable;
	private final Object defaultValue;

	public DbColumn(String tableName, String columnName, DbColumnType columnType, DbNullable nullable, Object defaultValue) {
		super(tableName, columnName);
		this.columnType = columnType;
		this.nullable = nullable;
		this.defaultValue = defaultValue;
	}
	
	public String getColumnName() {
		return getSimpleName();
	}
	
	public Object getDefaultValue() {
		return this.defaultValue;
	}
	
	public DbColumnType getColumnType() {
		return columnType;
	}
	
	public DbNullable getNullable() {
		return nullable;
	}
	
	@Override
	public String getArtifactName() {
		return getTableName() + "." + getColumnName();
	}

	@Override
	protected boolean isUnchanged(DbArtifact a) {
		DbColumn other = (DbColumn) a;
		return ObjectUtils.equals(getColumnType(), other.getColumnType())
			&& ObjectUtils.equals(getNullable(), other.getNullable());
	}
	
	@Override
	public <T> T accept(DbArtifactVisitor<T> visitor) throws DbException {
		return visitor.visitColumn(this);
	}
	
	// TODO: types
}
