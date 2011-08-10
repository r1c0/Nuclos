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

import java.util.ArrayList;
import java.util.List;

public class DbBatchStatement extends DbStatement {

	private final boolean failFirst;
	private final List<DbStatement> statements;
	
	public DbBatchStatement() {
		this(true);
	}
	
	public DbBatchStatement(boolean failFirst) {
		this.failFirst = failFirst;
		this.statements = new ArrayList<DbStatement>();
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("failFirst=").append(failFirst);
		result.append(", statements=").append(statements);
		result.append("]");
		return result.toString();
	}

	public boolean isFailFirst() {
		return failFirst;
	}
	
	public void add(DbBuildableStatement stmt) {
		statements.add(stmt.build());
	}
	
	public List<DbStatement> getStatements() {
		return statements;
	}
	
	@Override
	public <T> T accept(DbStatementVisitor<T> visitor) {
		return visitor.visitBatch(this);
	}
}
