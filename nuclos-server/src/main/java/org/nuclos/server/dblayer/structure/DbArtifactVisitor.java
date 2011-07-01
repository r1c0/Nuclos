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
import org.nuclos.server.dblayer.structure.DbConstraint.DbForeignKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbPrimaryKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbUniqueConstraint;

public interface DbArtifactVisitor<T> {

	public T visitTable(DbTable table) throws DbException;

	public T visitColumn(DbColumn column) throws DbException;

	public T visitPrimaryKeyConstraint(DbPrimaryKeyConstraint constraint) throws DbException;

	public T visitUniqueConstraint(DbUniqueConstraint constraint) throws DbException;

	public T visitForeignKeyConstraint(DbForeignKeyConstraint constraint) throws DbException;

	public T visitIndex(DbIndex index) throws DbException;

	public T visitView(DbSimpleView view) throws DbException;
	
	public T visitSequence(DbSequence sequence) throws DbException;

	public T visitCallable(DbCallable dbCallable) throws DbException;
}
