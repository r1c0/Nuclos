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
package org.nuclos.server.dblayer.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.server.dblayer.IPreparedStringExecutor;
import org.nuclos.server.dblayer.expression.DbNull;
import org.nuclos.server.dblayer.impl.util.PreparedString;
import org.nuclos.server.dblayer.incubator.DbExecutor;
import org.nuclos.server.dblayer.incubator.DbExecutor.ConnectionRunner;

/**
 * @author Thomas Pasch
 * @since Nuclos 3.2.0
 */
public class StandardPreparedStringExecutor implements IPreparedStringExecutor {
	
    private static final Logger LOG = Logger.getLogger(StandardPreparedStringExecutor.class);

	protected final DbExecutor dbExecutor;
	
	public StandardPreparedStringExecutor(DbExecutor dbExecutor) {
		if (dbExecutor == null) throw new NullPointerException();
		this.dbExecutor = dbExecutor;
	}

	/**
	 * @deprecated
	 */
	@Override
    public Integer executePreparedStatements(List<PreparedString> pss) throws SQLException {
        int result = 0;
        for (PreparedString ps : pss)
            result += executePreparedStatement(ps);
        return result;
    }

	@Override
    public int executePreparedStatement(final PreparedString ps) throws SQLException {
        if (!ps.hasParameters()) {
            String sql = ps.toString();
            logSql("execute SQL statement", sql, null);
            return dbExecutor.executeUpdate(sql);
        } else {
            return dbExecutor.execute(new ConnectionRunner<Integer>() {
                @Override
                public Integer perform(Connection conn) throws SQLException {
                    String sql = ps.toString();
                    Object[] params = ps.getParameters();

                    logSql("execute SQL statement", sql, params);

                    PreparedStatement stmt = conn.prepareStatement(sql);
                    try {
                        dbExecutor.prepareStatementParameters(stmt, params);
                        return stmt.executeUpdate();
                    } catch (SQLException e) {
                		LOG.error("executePreparedStatement failed with " + e.toString() + ":\n\t" + sql + "\n\t" + Arrays.asList(params));
                    	throw e;
                    } finally {
                        stmt.close();
                    }
                }
            });
        }
    }

    protected void logSql(String text, String sql, Object[] parameters) {
    	StringBuilder sb = new StringBuilder(text);
    	sb.append(" <[").append(sql.toString()).append("]>");
    	if (parameters != null && parameters.length > 0) {
    		sb.append(" with parameters [");
    		for (int i = 0, n = parameters.length; i < n; i++) {
    			if (i > 0) sb.append(", ");
    			Object param = parameters[i];
				sb.append(param);
				if (param != null) {
					Class<?> type = param.getClass();
					if (type != DbNull.class) {
						sb.append(" (").append(param.getClass().getName()).append(")");
					}
				}
    		}
    		sb.append("]");
    	}
        LOG.debug(sb.toString());
    }
}
