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

package org.nuclos.server.dblayer.util;

import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.structure.DbArtifactVisitor;
import org.nuclos.server.dblayer.structure.DbCallable;
import org.nuclos.server.dblayer.structure.DbColumn;
import org.nuclos.server.dblayer.structure.DbIndex;
import org.nuclos.server.dblayer.structure.DbSequence;
import org.nuclos.server.dblayer.structure.DbSimpleView;
import org.nuclos.server.dblayer.structure.DbTable;
import org.nuclos.server.dblayer.structure.DbConstraint.DbForeignKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbPrimaryKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbUniqueConstraint;

public class ArtifactToStringVisitor implements DbArtifactVisitor<String> {

	@Override
	public String visitCallable(DbCallable dbCallable) throws DbException {
		return makeName(dbCallable.getType().toString().toLowerCase(), dbCallable.getCallableName());
	}

	@Override
	public String visitColumn(DbColumn column) throws DbException {
		return makeName("column", column.getTableName() + "." + column.getColumnName());
	}

	@Override
	public String visitForeignKeyConstraint(DbForeignKeyConstraint constraint) throws DbException {
		return makeName("foreign key constraint", constraint.getTableName() + "." + constraint.getConstraintName());
	}

	@Override
	public String visitIndex(DbIndex index) throws DbException {
		return makeName("index", index.getTableName() + "." + index.getIndexName());
	}

	@Override
	public String visitPrimaryKeyConstraint(DbPrimaryKeyConstraint constraint) throws DbException {
		return makeName("primary key", constraint.getTableName() + "." + constraint.getConstraintName());
	}

	@Override
	public String visitSequence(DbSequence sequence) throws DbException {
		return makeName("sequence", sequence.getSequenceName());
	}

	@Override
	public String visitTable(DbTable table) throws DbException {
		return makeName("table", table.getTableName());
	}

	@Override
	public String visitUniqueConstraint(DbUniqueConstraint constraint) throws DbException {
		return makeName("unique constraint", constraint.getTableName() + "." + constraint.getConstraintName());
	}

	@Override
	public String visitView(DbSimpleView view) throws DbException {
		return makeName("simple view", view.getViewName());
	}

	protected String makeName(String kind, String name) {
		return kind + " " + name;
	}
}
