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

/**
 * Sequence.
 */
public class DbSequence extends DbArtifact {

	private final long startWith;
	
	public DbSequence(String name, Long startWith) {
		super(name);
		this.startWith = startWith;
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("start=").append(startWith);
		result.append(", name=").append(getSimpleName());
		result.append("]");
		return result.toString();
	}

	public String getSequenceName() {
		return getSimpleName();
	}
	
	public long getStartWith() {
		return startWith;
	}
	
	@Override
	protected boolean isUnchanged(DbArtifact a) {
		DbSequence other = (DbSequence) a;
		return getStartWith() == other.getStartWith();
	}
	
	@Override
	public <T> T accept(DbArtifactVisitor<T> visitor) throws DbException {
		return visitor.visitSequence(this);
	}
}
