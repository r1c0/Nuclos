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

import java.util.Map;


/**
 * A DB operation which represents a  insert statement. 
 */
public class DbDeleteStatement extends DbTableStatement {

	private final Map<String, Object> conditions;
	
	public DbDeleteStatement(String tableName, Map<String, Object> conditions) {
		super(tableName);
		this.conditions = conditions;
	}
	
	public Map<String, Object> getConditions() {
		return conditions;
	}

	@Override
	public <T> T accept(DbStatementVisitor<T> visitor) {
		return visitor.visitDelete(this);
	}
}
