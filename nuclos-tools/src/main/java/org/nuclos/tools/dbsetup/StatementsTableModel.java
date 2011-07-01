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

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.nuclos.server.dblayer.statements.DbStatement;
import org.nuclos.server.dblayer.util.StatementToStringVisitor;

@SuppressWarnings("serial")
public class StatementsTableModel extends AbstractTableModel {

	private DbStatement[] statements;
	private Object[] success;

	public StatementsTableModel() {
		setStatements(null);
	}
	
	public void setStatements(List<DbStatement> setupStatements) {
		if (setupStatements != null) {
			this.statements = setupStatements.toArray(new DbStatement[0]);
		} else {
			this.statements = new DbStatement[0];
		}
		this.success = new Object[statements.length];
		fireTableDataChanged();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		DbStatement stmt = statements[rowIndex];
		switch (columnIndex) {
		case 0:
			return rowIndex + 1;
		case 1:
			return stmt.accept(new StatementToStringVisitor());
		case 2:
			return success[rowIndex];
		}
		return null;
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return Integer.class;
		case 1:
			return String.class;
		case 2:
			return Object.class;
		}
		return Object.class;
	}
	
	@Override
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return "#";
		case 1:
			return "Statement";
		case 2:
			return "Success";
		}
		return null;
	}

	public DbStatement[] getStatements() {
		return statements;
	}
	
	public Object[] getSuccess() {
		return success;
	}
	
	@Override
	public int getRowCount() {
		return statements.length;
	}

	@Override
	public int getColumnCount() {
		return 3;
	}
	
	public DbStatement getStatement(int row) {
		return statements[row];
	}
	
	public Object getSuccess(int row) {
		return success[row];
	}
	
	public void setSuccess(int i, Object value) {
		success[i] = value;
		fireTableRowsUpdated(i, i);
	}
}