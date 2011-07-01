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
package org.nuclos.server.dblayer.impl.standard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.server.dblayer.DbAccess;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.impl.util.PreparedString;
import org.nuclos.server.dblayer.statements.DbBatchStatement;
import org.nuclos.server.dblayer.statements.DbDeleteStatement;
import org.nuclos.server.dblayer.statements.DbInsertStatement;
import org.nuclos.server.dblayer.statements.DbPlainStatement;
import org.nuclos.server.dblayer.statements.DbStatement;
import org.nuclos.server.dblayer.statements.DbStatementVisitor;
import org.nuclos.server.dblayer.statements.DbStructureChange;
import org.nuclos.server.dblayer.statements.DbUpdateStatement;
import org.nuclos.server.dblayer.structure.DbArtifact;
import org.nuclos.server.dblayer.structure.DbArtifactVisitor;
import org.nuclos.server.dblayer.structure.DbCallable;
import org.nuclos.server.dblayer.structure.DbColumn;
import org.nuclos.server.dblayer.structure.DbConstraint.DbForeignKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbPrimaryKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbUniqueConstraint;
import org.nuclos.server.dblayer.structure.DbIndex;
import org.nuclos.server.dblayer.structure.DbSequence;
import org.nuclos.server.dblayer.structure.DbSimpleView;
import org.nuclos.server.dblayer.structure.DbTable;


/**
 * Abstract base class for {@link DbAccess} which should simplify an implementation.
 */
public abstract class AbstractDBAccess extends DbAccess {

	@Override
	public List<PreparedString> getPreparedSqlFor(DbStatement stmt) throws DbException {
		return stmt.build().accept(sqlForStatementVisitor);
	}

	protected final List<String> getSqlForCreate(DbArtifact artifact) {
		return artifact.accept(sqlForCreateVisitor);
	}

	protected final List<String> getSqlForDrop(DbArtifact artifact) {
		return artifact.accept(sqlForDropVisitor);
	}

	protected abstract List<PreparedString> getSqlForInsert(DbInsertStatement insertStmt);

	protected abstract List<PreparedString> getSqlForDelete(DbDeleteStatement deleteStmt);

	protected abstract List<PreparedString> getSqlForUpdate(DbUpdateStatement updateStmt);

	protected abstract List<String> getSqlForCreateTable(DbTable table);

	protected abstract List<String> getSqlForCreateColumn(DbColumn column);

	protected abstract List<String> getSqlForCreatePrimaryKey(DbPrimaryKeyConstraint constraint);

	protected abstract List<String> getSqlForCreateForeignKey(DbForeignKeyConstraint constraint);

	protected abstract List<String> getSqlForCreateUniqueConstraint(DbUniqueConstraint constraint);

	protected abstract List<String> getSqlForCreateIndex(DbIndex index);

	protected abstract List<String> getSqlForCreateSimpleView(DbSimpleView view);

	protected abstract List<String> getSqlForCreateSequence(DbSequence callable);

	protected abstract List<String> getSqlForCreateCallable(DbCallable sequence);	

	protected abstract List<String> getSqlForAlterTableColumn(DbColumn column1, DbColumn column2);
	
	protected abstract List<String> getSqlForAlterTableNotNullColumn(DbColumn column);

	protected abstract List<String> getSqlForAlterSequence(DbSequence sequence1, DbSequence sequence2);

	protected abstract List<String> getSqlForDropTable(DbTable table);

	protected abstract List<String> getSqlForDropColumn(DbColumn column);

	protected abstract List<String> getSqlForDropPrimaryKey(DbPrimaryKeyConstraint constraint);

	protected abstract List<String> getSqlForDropForeignKey(DbForeignKeyConstraint constraint);

	protected abstract List<String> getSqlForDropUniqueConstraint(DbUniqueConstraint constraint);

	protected abstract List<String> getSqlForDropIndex(DbIndex index);

	protected abstract List<String> getSqlForDropSimpleView(DbSimpleView view);

	protected abstract List<String> getSqlForDropSequence(DbSequence sequence);

	protected abstract List<String> getSqlForDropCallable(DbCallable callable);	

	private final DbStatementVisitor<List<PreparedString>> sqlForStatementVisitor = new DbStatementVisitor<List<PreparedString>>() {

		@Override
		public List<PreparedString> visitStructureChange(DbStructureChange structureChange) {
			DbArtifact artifact1 = structureChange.getArtifact1();
			DbArtifact artifact2 = structureChange.getArtifact2();
			List<String> sqls;
			switch (structureChange.getType()) {
			case CREATE:
				sqls = artifact2.accept(sqlForCreateVisitor);
				break;
			case DROP:
				sqls = artifact1.accept(sqlForDropVisitor);
				break;
			case MODIFY:
				if (artifact1 instanceof DbColumn && artifact2 instanceof DbColumn) {
					sqls = getSqlForAlterTableColumn((DbColumn) artifact1, (DbColumn) artifact2);
				} else if (artifact1 instanceof DbColumn && artifact2 instanceof DbColumn) {
					sqls = getSqlForAlterSequence((DbSequence) artifact1, (DbSequence) artifact2);
				} else {
					sqls = CollectionUtils.concat(
						artifact1.accept(sqlForDropVisitor),
						artifact2.accept(sqlForCreateVisitor));
				}
				break;
			default:
				throw new IllegalArgumentException();
			}
			List<PreparedString> list = new ArrayList<PreparedString>();
			for (String sql : sqls) {
				list.add(new PreparedString(sql));
			}
			return list;				
		}

		@Override
		public List<PreparedString> visitInsert(DbInsertStatement insert) {
			return getSqlForInsert(insert);
		}

		@Override
		public List<PreparedString> visitUpdate(DbUpdateStatement update) {
			return getSqlForUpdate(update);
		}		

		@Override
		public List<PreparedString> visitDelete(DbDeleteStatement delete) {
			return getSqlForDelete(delete);
		}

		@Override
		public List<PreparedString> visitPlain(DbPlainStatement command) {
			return Collections.singletonList(new PreparedString(command.getSql()));
		}

		@Override
		public List<PreparedString> visitBatch(DbBatchStatement batch) {
			List<PreparedString> list = new ArrayList<PreparedString>();
			for (DbStatement stmt : batch.getStatements()) {
				list.addAll(stmt.accept(this));
			}
			return list;			
		}
	};

	private final DbArtifactVisitor<List<String>> sqlForCreateVisitor = new DbArtifactVisitor<List<String>>() {
		@Override
		public List<String> visitTable(DbTable table) throws DbException {
			return getSqlForCreateTable(table);
		}

		@Override
		public List<String> visitColumn(DbColumn column) {
			return getSqlForCreateColumn(column);
		}

		@Override
		public List<String> visitPrimaryKeyConstraint(DbPrimaryKeyConstraint constraint) {
			return getSqlForCreatePrimaryKey(constraint);
		}

		@Override
		public List<String> visitForeignKeyConstraint(DbForeignKeyConstraint constraint) {
			return getSqlForCreateForeignKey(constraint);
		}

		@Override
		public List<String> visitUniqueConstraint(DbUniqueConstraint constraint) {
			return getSqlForCreateUniqueConstraint(constraint);
		}

		@Override
		public List<String> visitIndex(DbIndex index) throws DbException {
			return getSqlForCreateIndex(index);
		}

		@Override
		public List<String> visitView(DbSimpleView view) throws DbException {
			return getSqlForCreateSimpleView(view);
		}

		@Override
		public List<String> visitSequence(DbSequence sequence) throws DbException {
			return getSqlForCreateSequence(sequence);
		}

		@Override
		public List<String> visitCallable(DbCallable callable) throws DbException {
			return getSqlForCreateCallable(callable);
		}
	};

	private final DbArtifactVisitor<List<String>> sqlForDropVisitor = new DbArtifactVisitor<List<String>>() {   		
		@Override
		public List<String> visitTable(DbTable table) throws DbException {
			return getSqlForDropTable(table);
		}

		@Override
		public List<String> visitColumn(DbColumn column) {
			return getSqlForDropColumn(column);
		}

		@Override
		public List<String> visitPrimaryKeyConstraint(DbPrimaryKeyConstraint constraint) {
			return getSqlForDropPrimaryKey(constraint);
		}

		@Override
		public List<String> visitForeignKeyConstraint(DbForeignKeyConstraint constraint) {
			return getSqlForDropForeignKey(constraint);
		}

		@Override
		public List<String> visitUniqueConstraint(DbUniqueConstraint constraint) {
			return getSqlForDropUniqueConstraint(constraint);
		}

		@Override
		public List<String> visitIndex(DbIndex index) throws DbException {
			return getSqlForDropIndex(index);
		}

		@Override
		public List<String> visitView(DbSimpleView view) throws DbException {
			return getSqlForDropSimpleView(view);
		}

		@Override
		public List<String> visitSequence(DbSequence sequence) throws DbException {
			return getSqlForDropSequence(sequence);
		}

		@Override
		public List<String> visitCallable(DbCallable callable) throws DbException {
			return getSqlForDropCallable(callable);
		}
	};	
}
