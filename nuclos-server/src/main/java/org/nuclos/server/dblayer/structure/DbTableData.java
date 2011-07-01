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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.nuclos.common.collection.Pair;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.dblayer.statements.DbBatchStatement;
import org.nuclos.server.dblayer.statements.DbInsertStatement;
import org.nuclos.server.dblayer.statements.DbStatement;
import org.nuclos.server.dblayer.structure.DbColumnType.DbGenericType;

/**
 * Initial data of a table.
 */
public class DbTableData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String tableName;
	private final List<Pair<String, DbGenericType>> columns;
	private final List<List<Object>> data;

	public DbTableData(String tableName, List<Pair<String, DbGenericType>> columns, List<List<Object>> data) {
		this.tableName = tableName;
		this.columns = columns;
		this.data = data;
	}

	public String getTableName() {
		return tableName;
	}
	
	public List<Pair<String, DbGenericType>> getColumns() {
		return columns;
	}
	
	public List<List<Object>> getData() {
		return data;
	}
	
	public List<DbStatement> getStatements(boolean deleteAll) {
		List<DbStatement> statements = new ArrayList<DbStatement>();
		String tableName = getTableName();
		if (deleteAll) {
			statements.add(DbStatementUtils.deleteAllFrom(tableName));
		}
		DbBatchStatement insertBatch = new DbBatchStatement();
		for (List<Object> row : data) {
			Map<String, Object> values = new LinkedHashMap<String, Object>();
			for (int i = 0; i < columns.size(); i++) {
				values.put(columns.get(i).x, row.get(i));
			}
			insertBatch.add(new DbInsertStatement(tableName, values));  
		}
		statements.add(insertBatch);
		return statements;
	}
}
