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
package org.nuclos.server.dblayer.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.incubator.DbExecutor;
import org.springframework.jdbc.datasource.DataSourceUtils;

/** This class is an implementation class, only use in exceptional cases! */
public class DataSourceExecutor implements DbExecutor {

	private static final Logger log = Logger.getLogger(DataSourceExecutor.class);
	
	private final DataSource dataSource;
	private final String username;
	private final String password; 

	public DataSourceExecutor(DataSource dataSource) {
		this(dataSource, null, null);
	}
	
	public DataSourceExecutor(DataSource dataSource, String username, String password) {
		this.dataSource = dataSource;
		this.username = username;
		this.password = password;
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("ds=").append(dataSource);
		result.append(", user=").append(username);
		result.append("]");
		return result.toString();
	}

	@Override
	public <T> T execute(ConnectionRunner<T> runner) throws DbException {
		try {
			Connection conn = getConnection();
			try {
				return runner.perform(conn);
			} finally {
				DataSourceUtils.releaseConnection(conn, dataSource);
			}
		} catch (SQLException e) {
			log.error("SQL exception", e);
			throw wrapSQLException(e);
		} catch (Exception e) {
			log.error(e);
			throw new DbException(e.toString(), e);
		}
	}

	@Override
	public <T> T executeQuery(final String sql, final ResultSetRunner<T> runner) throws DbException {
		return execute(new ConnectionRunner<T>() {
			@Override
			public T perform(Connection conn) throws SQLException {
				Statement stmt = conn.createStatement();
				try {
					ResultSet rs = stmt.executeQuery(sql);
					try {
						return runner.perform(rs);
					} finally {
						rs.close();
					}
				} finally {
					stmt.close();
				}
			}
		});
	}

	@Override
	public int executeUpdate(final String sql) throws DbException {
		return execute(new ConnectionRunner<Integer>() {
			@Override
			public Integer perform(Connection conn) throws SQLException {
				Statement stmt = conn.createStatement();
				try {
					return stmt.executeUpdate(sql);
				} finally {
					stmt.close();
				}
			}
		});
	}
	
	public void release() throws DbException {
	}
	
	protected Connection getConnection() throws SQLException {
		if (dataSource != null) {			
			return DataSourceUtils.getConnection(dataSource);			
		}
		return null;
	}
	
	protected DbException wrapSQLException(SQLException ex) {
		return new DbException(ex);
	}
}
