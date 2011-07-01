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

import org.nuclos.server.dblayer.structure.DbArtifact;

/**
 * A DB operation which represents a structural change. 
 */
public class DbStructureChange extends DbStatement {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static enum Type {
		DROP(1),
		CREATE(2),
		MODIFY(3);
		
		private final int	flags;

		private Type(int flags) {
			this.flags = flags;
		}
		
		private void check(DbArtifact artifact1, DbArtifact artifact2) {
			int f = (artifact1 != null ? 1 : 0) | (artifact2 != null ? 2 : 0);
			if (flags != f)
				throw new IllegalArgumentException("Illegal parameters for type " + this);
		}
	}
	
	private final Type type;
	private final DbArtifact artifact1;
	private final DbArtifact artifact2;
	
	public DbStructureChange(Type type, DbArtifact artifact) {
		this(type, type == Type.DROP ? artifact : null, type == Type.CREATE ? artifact : null);
	}
	
	public DbStructureChange(Type type, DbArtifact artifact1, DbArtifact artifact2) {
		type.check(artifact1, artifact2);
		if (artifact1 != null && artifact2 != null && !artifact1.isSameType(artifact2))
			throw new IllegalArgumentException();
		this.type = type;
		this.artifact1 = artifact1;
		this.artifact2 = artifact2;
	}
	
	public DbArtifact getArtifact1() {
		return artifact1;
	}
	
	public DbArtifact getArtifact2() {
		return artifact2;		
	}
	
	public Type getType() {
		return type;
	}
	
	@Override
	public <T> T accept(DbStatementVisitor<T> visitor) {
		return visitor.visitStructureChange(this);
	}
}
