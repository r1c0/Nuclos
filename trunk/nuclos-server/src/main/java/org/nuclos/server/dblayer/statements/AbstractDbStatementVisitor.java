package org.nuclos.server.dblayer.statements;

import java.sql.SQLException;

public class AbstractDbStatementVisitor<T> implements DbStatementVisitor<T> {

	protected AbstractDbStatementVisitor() {
	}

	@Override
	public T visitStructureChange(DbStructureChange structureChange) throws SQLException {
		return null;
	}

	@Override
	public T visitInsert(DbInsertStatement insert) throws SQLException {
		return null;
	}

	@Override
	public T visitUpdate(DbUpdateStatement update) throws SQLException {
		return null;
	}

	@Override
	public T visitDelete(DbDeleteStatement delete) throws SQLException {
		return null;
	}

	@Override
	public T visitPlain(DbPlainStatement command) throws SQLException {
		return null;
	}

	@Override
	public T visitBatch(DbBatchStatement batch) throws SQLException {
		return null;
	}
	
}
