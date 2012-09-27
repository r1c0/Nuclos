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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosDateTime;
import org.nuclos.common.NuclosPassword;
import org.nuclos.common.NuclosScript;
import org.nuclos.common2.DateUtils;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.XStreamSupport;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.expression.DbCurrentDate;
import org.nuclos.server.dblayer.expression.DbCurrentDateTime;
import org.nuclos.server.dblayer.expression.DbId;
import org.nuclos.server.dblayer.expression.DbIncrement;
import org.nuclos.server.dblayer.expression.DbNull;
import org.nuclos.server.dblayer.impl.util.JDBCType;
import org.nuclos.server.dblayer.incubator.DbExecutor;
import org.nuclos.server.dblayer.util.ServerCryptUtil;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/** This class is an implementation class, only use in exceptional cases! */
public abstract class DataSourceExecutor implements DbExecutor {

	private static final Logger log = Logger.getLogger(DataSourceExecutor.class);
	
	private final DataSource dataSource;
	private final String username;
	private final String password; 

	/**
	 * @deprecated
	 */
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
	public <T> T execute(ConnectionRunner<T> runner) throws SQLException {
		final Connection conn = getConnection();
		try {
			return runner.perform(conn);
		} finally {
			DataSourceUtils.releaseConnection(conn, dataSource);
		}
	}

	@Override
	public <T> T executeQuery(final String sql, final ResultSetRunner<T> runner) throws SQLException {
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
                } catch (SQLException e) {
            		log.error("SQL query failed with " + e.toString() + ":\n\t" + sql);
                	throw e;
				} finally {
					stmt.close();
				}
			}
		});
	}

	@Override
	public int executeUpdate(final String sql) throws SQLException {
		return execute(new ConnectionRunner<Integer>() {
			@Override
			public Integer perform(Connection conn) throws SQLException {
				Statement stmt = conn.createStatement();
				try {
					return stmt.executeUpdate(sql);
                } catch (SQLException e) {
            		log.error("SQL update failed with " + e.toString() + ":\n\t" + sql);
                	throw e;
				} finally {
					stmt.close();
				}
			}
		});
	}
	
	@Override
    public final int prepareStatementParameters(PreparedStatement stmt, Object[] values) throws SQLException {
        return prepareStatementParameters(stmt, 1, Arrays.asList(values));
    }

	@Override
    public final int prepareStatementParameters(PreparedStatement stmt, int index, Iterable<Object> values) throws SQLException {
        java.util.Date now = new java.util.Date();
        for (Object param : values) {
            if (param == null) {
                // Note, db null values have to be wrapped as DbNull object
                throw new IllegalArgumentException("Missing prepared statement parameter #" + index);
            }
            Class<?> javaType = param.getClass();
            if (javaType == DbIncrement.class) {
                // TODO: this behavior matches buildUpdateString but a better handling is required
                continue;
            } else if (javaType == DbId.class) {
                DbId dbId = (DbId) param;
                dbId.setIdValue(getNextId(((DbId) param).getSequenceName()));
                param = dbId.getIdValue();
                javaType = param.getClass();
            } else if (javaType == DbCurrentDate.class) {
                param = DateUtils.getPureDate(now);
                javaType = param.getClass();
            } else if (javaType == DbCurrentDateTime.class) {
                param = new Timestamp(now.getTime());
                javaType = param.getClass();
            } else if (javaType == DbNull.class) {
                javaType = ((DbNull<?>) param).getJavaType();
                if (javaType == java.util.Date.class)
                    javaType = java.sql.Date.class;
                param = null;
            }
            setStatementParameter(stmt, index++, param, javaType);
        }
        return index;
    }

    public void setStatementParameter(PreparedStatement stmt, int index, Object value, Class<?> javaType) throws SQLException {
        if (value instanceof String) {
            stmt.setString(index, (String) value);
        } else if (value instanceof NuclosPassword) {
            stmt.setString(index, ServerCryptUtil.encrypt(((NuclosPassword) value).getValue()));
        } else if (value instanceof Integer) {
            stmt.setInt(index, (Integer) value);
        } else if (value instanceof Long) {
            stmt.setLong(index, (Long) value);
        } else if (value instanceof Boolean) {
            stmt.setBoolean(index, (Boolean) value);
        } else if (value instanceof Double) {
            stmt.setDouble(index, (Double) value);
        } else if (value instanceof BigDecimal) {
            stmt.setBigDecimal(index, (BigDecimal) value);
        } else if (value instanceof java.sql.Timestamp) {
            stmt.setTimestamp(index, (java.sql.Timestamp) value);
        } else if (value instanceof Date) {
            if (javaType == InternalTimestamp.class) {
                stmt.setTimestamp(index, new java.sql.Timestamp(((InternalTimestamp) value).getTime()));
            } else if (javaType == NuclosDateTime.class) {
                stmt.setTimestamp(index, new java.sql.Timestamp(((NuclosDateTime) value).getTime()));
            } else {
                stmt.setDate(index, new java.sql.Date(((Date) value).getTime()));
            }
        } else if (value instanceof byte[]) {
            stmt.setBytes(index, (byte[]) value);
        } else if (value == null) {
            stmt.setNull(index, getPreferredSqlTypeFor(javaType));
        } else if (value instanceof NuclosScript) {
        	final XStream xstream = XStreamSupport.getInstance().getXStreamUtf8();
            stmt.setString(index, xstream.toXML(value));
        }else {
            throw new SQLException("Java type " + javaType + " cannot be mapped to DB type");
        }
    }

    /**
     * @deprecated
     */
	public void release() {
	}
	
	protected Connection getConnection() throws SQLException {
		if (dataSource != null) {			
			return DataSourceUtils.getConnection(dataSource);			
		}
		return null;
	}

	@Override
    public final int getPreferredSqlTypeFor(Class<?> javaType) throws DbException {
        if (javaType == java.util.Date.class) {
            javaType = java.sql.Date.class;
        } else if (javaType == InternalTimestamp.class || javaType == NuclosDateTime.class) {
            javaType = java.sql.Timestamp.class;
        } else if (javaType == NuclosPassword.class) {
        	javaType = java.lang.String.class;
        } else if (javaType == NuclosScript.class) {
        	javaType = java.lang.String.class;
        }
        return JDBCType.getJDCBTypesForObjectType(javaType)[0].getSqlType();
    }

}
