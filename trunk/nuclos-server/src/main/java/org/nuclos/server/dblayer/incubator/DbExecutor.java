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
package org.nuclos.server.dblayer.incubator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.nuclos.server.dblayer.DbException;

/**
 * Generic low-level interface for performing actions on the database.  
 */
public interface DbExecutor {

	public static interface ConnectionRunner<T> {

		/**
		 * Performs a generic action on the given connection. The connection should closed
		 * by this method.
		 * @throws SQLException
		 */
		T perform(Connection conn) throws SQLException;
	}

	public static interface ResultSetRunner<T> {

		/**
		 * Performs a generic action on the given result set. The result should not closed by
		 * this method. 
		 * @throws SQLException
		 */
		T perform(ResultSet rs) throws SQLException;
	}

	int executeUpdate(String sql) throws SQLException;

	<T> T executeQuery(String sql, ResultSetRunner<T> runner) throws SQLException;

	<T> T execute(ConnectionRunner<T> runner) throws SQLException;
	
	// 

	int prepareStatementParameters(PreparedStatement stmt, Object[] values) throws SQLException;

    int prepareStatementParameters(PreparedStatement stmt, int index, Iterable<Object> values) throws SQLException;
    
    void setStatementParameter(PreparedStatement stmt, int index, Object value, Class<?> javaType) throws SQLException;

    Long getNextId(String sequenceName) throws SQLException;
    
    int getPreferredSqlTypeFor(Class<?> javaType) throws DbException;
    
    /**
     * @deprecated
     */
	void release();
	
}
