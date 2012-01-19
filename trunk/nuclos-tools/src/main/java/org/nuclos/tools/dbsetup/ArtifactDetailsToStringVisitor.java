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

package org.nuclos.tools.dbsetup;

import org.nuclos.common2.StringUtils;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.structure.DbArtifactVisitor;
import org.nuclos.server.dblayer.structure.DbCallable;
import org.nuclos.server.dblayer.structure.DbColumn;
import org.nuclos.server.dblayer.structure.DbColumnType;
import org.nuclos.server.dblayer.structure.DbIndex;
import org.nuclos.server.dblayer.structure.DbSequence;
import org.nuclos.server.dblayer.structure.DbSimpleView;
import org.nuclos.server.dblayer.structure.DbTable;
import org.nuclos.server.dblayer.structure.DbConstraint.DbForeignKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbPrimaryKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbUniqueConstraint;

public class ArtifactDetailsToStringVisitor implements DbArtifactVisitor<String> {

	@Override
	public String visitCallable(DbCallable dbCallable) throws DbException {
		return null;
	}

	@Override
	public String visitColumn(DbColumn column) throws DbException {
		DbColumnType columnType = column.getColumnType();
		StringBuilder sb = new StringBuilder();
		sb.append(columnType.getGenericType());
		if (columnType.getTypeName() != null)
			sb.append(" ").append(columnType.getTypeName());
		sb.append(" (").append(StringUtils.join(", ", columnType.getParameters())).append(")");
		sb.append(" ").append(column.getNullable());
		return sb.toString();
	}

	@Override
	public String visitForeignKeyConstraint(DbForeignKeyConstraint constraint) throws DbException {
		return String.format("(%s) references %s (%s) %s",
			StringUtils.join(", ", constraint.getColumnNames()),
			constraint.getReferencedTableName(),
			StringUtils.join(", ", constraint.getReferencedColumnNames()),
			constraint.isOnDeleteCascade() ? "ON DELETE CASCADE" : "");
	}

	@Override
	public String visitIndex(DbIndex index) throws DbException {
		return String.format("(%s)", StringUtils.join(", ", index.getColumnNames()));
	}

	@Override
	public String visitPrimaryKeyConstraint(DbPrimaryKeyConstraint constraint) throws DbException {
		return String.format("(%s)", StringUtils.join(", ", constraint.getColumnNames()));
	}

	@Override
	public String visitSequence(DbSequence sequence) throws DbException {
		return String.format("start with %d", sequence.getStartWith());
	}

	@Override
	public String visitTable(DbTable table) throws DbException {
		return null;
	}

	@Override
	public String visitUniqueConstraint(DbUniqueConstraint constraint) throws DbException {
		return String.format("(%s)", StringUtils.join(", ", constraint.getColumnNames()));
	}

	@Override
	public String visitView(DbSimpleView view) throws DbException {
		return null;
	}
}
