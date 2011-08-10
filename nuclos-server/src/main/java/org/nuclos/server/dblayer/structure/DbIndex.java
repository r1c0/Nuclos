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
import java.util.List;

import org.nuclos.server.dblayer.DbException;

public class DbIndex extends DbTableArtifact implements DbTableColumnGroup {

	private final List<String>	columns;

	public DbIndex(String tableName, String constraintName, List<String> columns) {
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

	public String getIndexName() {
		return getSimpleName();
	}	
	
	@Override
	public List<String> getColumnNames() {
		return columns;
	}
	
	@Override
		protected boolean isUnchanged(DbArtifact a) {
		DbIndex other = (DbIndex) a;
		return getColumnNames().equals(other.getColumnNames());
	}
		
	@Override
	public <T> T accept(DbArtifactVisitor<T> visitor) throws DbException {
		return visitor.visitIndex(this);
	}
}
