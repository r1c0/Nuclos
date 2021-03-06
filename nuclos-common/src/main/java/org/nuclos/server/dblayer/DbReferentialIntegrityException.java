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
package org.nuclos.server.dblayer;

import java.sql.SQLException;


/**
 * DbException class thrown when an operation fails due to an foreign key constraint.
 */
public class DbReferentialIntegrityException extends DbException {
	
	private final String schemaName;
	private final String constraintName;

	public DbReferentialIntegrityException(Long id, String schemaName, String constraintName, SQLException ex) {
		super(id, "schema=" + schemaName + " constraint=" + constraintName, ex);
		this.schemaName = schemaName;
		this.constraintName = constraintName;
	}
	
	public String getSchemaName() {
		return schemaName;
	}
	
	public String getConstraintName() {
		return constraintName;
	}
}
