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
package org.nuclos.server.dblayer.statements;

import java.sql.SQLException;
import java.util.Map;


/**
 * A DB operation which represents an insert statement. 
 */
public class DbInsertStatement extends DbTableStatement {

	private final Map<String, Object> values;
	
	public DbInsertStatement(String tableName, Map<String, Object> values) {
		super(tableName);
		this.values = values;
	}
	
	public Map<String, Object> getColumnValues() {
		return values;
	}

	@Override
	public <T> T accept(DbStatementVisitor<T> visitor) throws SQLException {
		return visitor.visitInsert(this);
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("table=").append(getTableName());
		result.append(", values=").append(values);
		result.append("]");
		return result.toString();
	}

}
