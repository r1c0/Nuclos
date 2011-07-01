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
package org.nuclos.server.dblayer;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.nuclos.common2.InternalTimestamp;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dblayer.expression.DbNull;
import org.nuclos.server.dblayer.statements.DbBuildableStatement;
import org.nuclos.server.dblayer.statements.DbDeleteStatement;
import org.nuclos.server.dblayer.statements.DbInsertStatement;
import org.nuclos.server.dblayer.statements.DbUpdateStatement;

public class DbStatementUtils {

	public static interface SpecifiyCondition {
		
		public DbBuildableStatement where(String column1, Object value1, Object...varargs);
	}
	
	// TODO: merge with SchemaUtils?
	
	public static DbInsertStatement insertInto(String table, String column1, Object value1, Object...varargs) {
		return new DbInsertStatement(table, makeMap(column1, value1, varargs));
	}
	
	public static DbDeleteStatement deleteAllFrom(String table) {
		return new DbDeleteStatement(table, Collections.<String, Object>emptyMap());
	}

	public static DbDeleteStatement deleteFrom(String table, String column1, Object value1, Object...varargs) {
		return new DbDeleteStatement(table, makeMap(column1, value1, varargs));
	}
	
	public static SpecifiyCondition updateValues(final String table, String column1, Object value1, Object...varargs) {
		final Map<String, Object> values = makeMap(column1, value1, varargs);
		return new SpecifiyCondition() {
			@Override
			public DbBuildableStatement where(String column1, Object value1, Object ... varargs) {
				return new DbUpdateStatement(table, values, makeMap(column1, value1, varargs));
			}
		};
	}
	
	public static DbUpdateStatement getDbUpdateStatementWhereFieldIsNull(String tablename, String field, Object value) {
		Map<String, Object> columnValueMap = new HashMap<String, Object>();
		Map<String, Object> columnConditionMap = new HashMap<String, Object>();
		DbNull<?> dbNull = null;
		if(value instanceof String){
			dbNull = new DbNull<String>(String.class);
		}
		else if(value instanceof Boolean) {
			dbNull = new DbNull<Boolean>(Boolean.class);
		}
		else if(value instanceof Integer) {
			dbNull = new DbNull<Integer>(Integer.class);
		}
		else if(value instanceof java.util.Date) {
			dbNull = new DbNull<InternalTimestamp>(InternalTimestamp.class);
		}
		else {
			dbNull = DbNull.forType(DalUtils.getDbType(value.getClass()));
		}
		
		columnConditionMap.put(field, dbNull);
		columnValueMap.put(field, value);
		DbUpdateStatement update = new DbUpdateStatement(tablename, columnValueMap, columnConditionMap);		
		return update;			
	}
	
	private static Map<String, Object> makeMap(String column1, Object value1, Object...varargs) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put(column1, value1);
		for (int i = 0; i < varargs.length; i += 2)
			map.put((String) varargs[i], varargs[i+1]);
		return map;
	}}
