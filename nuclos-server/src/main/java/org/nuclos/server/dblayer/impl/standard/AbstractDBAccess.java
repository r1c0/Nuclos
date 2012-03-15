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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.nuclos.server.dblayer.DbAccess;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.IBatch;
import org.nuclos.server.dblayer.impl.BatchImpl;
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
	
	protected static final IBatch[] EMPTY_IBATCH_ARRAY = new IBatch[0];

	@Override
	public IBatch getBatchFor(DbStatement stmt) throws SQLException {
		return stmt.build().accept(sqlForStatementVisitor);
	}

	protected final IBatch getSqlForCreate(DbArtifact artifact) {
		return artifact.accept(sqlForCreateVisitor);
	}

	protected final IBatch getSqlForDrop(DbArtifact artifact) {
		return artifact.accept(sqlForDropVisitor);
	}

    public abstract String getSqlForConcat(String x, String y);
    
    public abstract String getSqlForConcat(List<String> l);

	protected abstract IBatch getSqlForInsert(DbInsertStatement insertStmt);

	protected abstract IBatch getSqlForDelete(DbDeleteStatement deleteStmt);

	protected abstract IBatch getSqlForUpdate(DbUpdateStatement updateStmt);

	protected abstract IBatch getSqlForCreateTable(DbTable table);

	protected abstract IBatch getSqlForCreateColumn(DbColumn column) throws SQLException;

	protected abstract IBatch getSqlForCreatePrimaryKey(DbPrimaryKeyConstraint constraint);

	protected abstract IBatch getSqlForCreateForeignKey(DbForeignKeyConstraint constraint);

	protected abstract IBatch getSqlForCreateUniqueConstraint(DbUniqueConstraint constraint);

	protected abstract IBatch getSqlForCreateIndex(DbIndex index);

	protected abstract IBatch getSqlForCreateSimpleView(DbSimpleView view);

	protected abstract IBatch getSqlForCreateSequence(DbSequence callable);

	protected abstract IBatch getSqlForCreateCallable(DbCallable sequence);	

	protected abstract IBatch getSqlForAlterTableColumn(DbColumn column1, DbColumn column2) throws SQLException;
	
	protected abstract IBatch getSqlForAlterTableNotNullColumn(DbColumn column);

	protected abstract IBatch getSqlForAlterSequence(DbSequence sequence1, DbSequence sequence2);

	protected abstract IBatch getSqlForDropTable(DbTable table);

	protected abstract IBatch getSqlForDropColumn(DbColumn column);

	protected abstract IBatch getSqlForDropPrimaryKey(DbPrimaryKeyConstraint constraint);

	protected abstract IBatch getSqlForDropForeignKey(DbForeignKeyConstraint constraint);

	protected abstract IBatch getSqlForDropUniqueConstraint(DbUniqueConstraint constraint);

	protected abstract IBatch getSqlForDropIndex(DbIndex index);

	protected abstract IBatch getSqlForDropSimpleView(DbSimpleView view);

	protected abstract IBatch getSqlForAlterSimpleView(DbSimpleView oldView, DbSimpleView newView);
	
	protected abstract IBatch getSqlForDropSequence(DbSequence sequence);

	protected abstract IBatch getSqlForDropCallable(DbCallable callable);	

	private final DbStatementVisitor<IBatch> sqlForStatementVisitor = new DbStatementVisitor<IBatch>() {

		@Override
		public IBatch visitStructureChange(DbStructureChange structureChange) throws SQLException {
			DbArtifact artifact1 = structureChange.getArtifact1();
			DbArtifact artifact2 = structureChange.getArtifact2();
			final IBatch result;
			switch (structureChange.getType()) {
			case CREATE:
				result = artifact2.accept(sqlForCreateVisitor);
				break;
			case DROP:
				result = artifact1.accept(sqlForDropVisitor);
				break;
			case MODIFY:
				if (artifact1 instanceof DbColumn && artifact2 instanceof DbColumn) {
					result = getSqlForAlterTableColumn((DbColumn) artifact1, (DbColumn) artifact2);
				} else if (artifact1 instanceof DbColumn && artifact2 instanceof DbColumn) {
					result = getSqlForAlterSequence((DbSequence) artifact1, (DbSequence) artifact2);
				} else if (artifact1 instanceof DbSimpleView && artifact2 instanceof DbSimpleView) {
					result = getSqlForAlterSimpleView((DbSimpleView) artifact1, (DbSimpleView) artifact2);
				} else {
					result = BatchImpl.concat(
						artifact1.accept(sqlForDropVisitor),
						artifact2.accept(sqlForCreateVisitor));
				}
				break;
			default:
				throw new IllegalArgumentException();
			}
			return result;
		}

		@Override
		public IBatch visitInsert(DbInsertStatement insert) {
			return getSqlForInsert(insert);
		}

		@Override
		public IBatch visitUpdate(DbUpdateStatement update) {
			return getSqlForUpdate(update);
		}		

		@Override
		public IBatch visitDelete(DbDeleteStatement delete) {
			return getSqlForDelete(delete);
		}

		@Override
		public IBatch visitPlain(DbPlainStatement command) {
			return BatchImpl.simpleBatch(new PreparedString(command.getSql()));
		}

		@Override
		public IBatch visitBatch(DbBatchStatement batch) throws SQLException {
			ArrayList<IBatch> list = new ArrayList<IBatch>();
			for (DbStatement stmt : batch.getStatements()) {
				list.add(stmt.accept(this));
			}
			return BatchImpl.concat(list.toArray(EMPTY_IBATCH_ARRAY)); 			
		}
	};

	private final DbArtifactVisitor<IBatch> sqlForCreateVisitor = new DbArtifactVisitor<IBatch>() {
		@Override
		public IBatch visitTable(DbTable table) throws DbException {
			return getSqlForCreateTable(table);
		}

		@Override
		public IBatch visitColumn(DbColumn column) {
			try {
				return getSqlForCreateColumn(column);
			} catch (SQLException e) {
				throw wrapSQLException(null, "visitColumn fails on " + column, e);
			}
		}

		@Override
		public IBatch visitPrimaryKeyConstraint(DbPrimaryKeyConstraint constraint) {
			return getSqlForCreatePrimaryKey(constraint);
		}

		@Override
		public IBatch visitForeignKeyConstraint(DbForeignKeyConstraint constraint) {
			return getSqlForCreateForeignKey(constraint);
		}

		@Override
		public IBatch visitUniqueConstraint(DbUniqueConstraint constraint) {
			return getSqlForCreateUniqueConstraint(constraint);
		}

		@Override
		public IBatch visitIndex(DbIndex index) throws DbException {
			return getSqlForCreateIndex(index);
		}

		@Override
		public IBatch visitView(DbSimpleView view) throws DbException {
			return getSqlForCreateSimpleView(view);
		}

		@Override
		public IBatch visitSequence(DbSequence sequence) throws DbException {
			return getSqlForCreateSequence(sequence);
		}

		@Override
		public IBatch visitCallable(DbCallable callable) throws DbException {
			return getSqlForCreateCallable(callable);
		}
	};

	private final DbArtifactVisitor<IBatch> sqlForDropVisitor = new DbArtifactVisitor<IBatch>() {  
		
		@Override
		public IBatch visitTable(DbTable table) throws DbException {
			return getSqlForDropTable(table);
		}

		@Override
		public IBatch visitColumn(DbColumn column) {
			return getSqlForDropColumn(column);
		}

		@Override
		public IBatch visitPrimaryKeyConstraint(DbPrimaryKeyConstraint constraint) {
			return getSqlForDropPrimaryKey(constraint);
		}

		@Override
		public IBatch visitForeignKeyConstraint(DbForeignKeyConstraint constraint) {
			return getSqlForDropForeignKey(constraint);
		}

		@Override
		public IBatch visitUniqueConstraint(DbUniqueConstraint constraint) {
			return getSqlForDropUniqueConstraint(constraint);
		}

		@Override
		public IBatch visitIndex(DbIndex index) throws DbException {
			return getSqlForDropIndex(index);
		}

		@Override
		public IBatch visitView(DbSimpleView view) throws DbException {
			return getSqlForDropSimpleView(view);
		}

		@Override
		public IBatch visitSequence(DbSequence sequence) throws DbException {
			return getSqlForDropSequence(sequence);
		}

		@Override
		public IBatch visitCallable(DbCallable callable) throws DbException {
			return getSqlForDropCallable(callable);
		}
	};	
}
