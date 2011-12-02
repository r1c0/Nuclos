//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.server.dblayer.impl.postgresql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.nuclos.server.dblayer.impl.SQLUtils2;
import org.nuclos.server.dblayer.impl.StandardPreparedStringExecutor;
import org.nuclos.server.dblayer.impl.util.PreparedString;
import org.nuclos.server.dblayer.incubator.DbExecutor;
import org.nuclos.server.dblayer.incubator.DbExecutor.ConnectionRunner;
import org.postgresql.jdbc4.Jdbc4Connection;

/**
 * @author Thomas Pasch
 * @since Nuclos 3.2.0
 */
public class PostgreSQLPreparedStringExecutor extends StandardPreparedStringExecutor {

    private static final Logger LOG = Logger.getLogger(PostgreSQLPreparedStringExecutor.class);
    
	private final boolean autoSavepoint;
	
    public PostgreSQLPreparedStringExecutor(DbExecutor dbExecutor, boolean autoSavepoint) {
    	super(dbExecutor);
    	this.autoSavepoint = autoSavepoint;
    }

	@Override
	public int executePreparedStatement(final PreparedString ps) throws SQLException {
		return dbExecutor.execute(new ConnectionRunner<Integer>() {
			@Override
			public Integer perform(Connection conn) throws SQLException {
				String sql = ps.toString();
				Object[] params = ps.getParameters();

				logSql("execute SQL statement", sql, params);

				Jdbc4Connection pgsqlConn;
				if (useSavepoint(conn)
						&& (pgsqlConn = SQLUtils2.unwrap(conn, org.postgresql.jdbc4.Jdbc4Connection.class)) != null) {
					LOG.trace("Create savepoint");
					Savepoint savepoint = pgsqlConn.setSavepoint();
					try {
						return performImpl(conn, sql, params);
					} catch (SQLException e) {
						LOG.warn("Restore to savepoint because SQL failed with " + e.toString() + ":\n\t" + sql
								+ "\n\t" + Arrays.asList(params));
						pgsqlConn.rollback(savepoint);
						throw e;
					} finally {
						LOG.trace("Release savepoint");
						pgsqlConn.releaseSavepoint(savepoint);
					}
				} else {
					return performImpl(conn, sql, params);
				}
			}

			private Integer performImpl(Connection conn, String sql, Object[] params) throws SQLException {
				PreparedStatement stmt = conn.prepareStatement(sql);
				try {
					dbExecutor.prepareStatementParameters(stmt, params);
					return stmt.executeUpdate();
				} finally {
					stmt.close();
				}
			}
		});
	}

	private boolean useSavepoint(Connection conn) throws SQLException {
		return autoSavepoint && !conn.getAutoCommit()
				&& (conn.getTransactionIsolation() != Connection.TRANSACTION_NONE);
	}
}
