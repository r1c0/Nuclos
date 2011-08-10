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

import org.nuclos.server.dblayer.DbException;

public class DbCallable extends DbArtifact {

	private DbCallableType type;
	private String code;
	
	public DbCallable(DbCallableType type, String callableName, String code) {
		super(callableName);
		this.type = type;
		this.code = code;
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("type=").append(type);
		result.append(", name=").append(getSimpleName());
		result.append(", code=").append(code);
		result.append("]");
		return result.toString();
	}

	public DbCallableType getType() {
		return type;
	}
	
	public String getCallableName() {
		return getSimpleName();
	}	
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	@Override
	protected boolean isUnchanged(DbArtifact a) {
		DbCallable other = (DbCallable) a;
		return getCode() != null && other.getCode() != null && getCode().trim().equals(other.getCode().trim());
	}
		
	@Override
	public <T> T accept(DbArtifactVisitor<T> visitor) throws DbException {
		return visitor.visitCallable(this);
	}
}
