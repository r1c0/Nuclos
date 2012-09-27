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

import static org.nuclos.common.collection.CollectionUtils.getFirst;
import static org.nuclos.common.collection.CollectionUtils.transform;
import static org.nuclos.common2.StringUtils.join;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosDateTime;
import org.nuclos.common.NuclosPassword;
import org.nuclos.common.NuclosScript;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.collection.TransformerUtils;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.XStreamSupport;
import org.nuclos.server.dblayer.DbAccess;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.DbIdent;
import org.nuclos.server.dblayer.DbInvalidResultSizeException;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.EBatchType;
import org.nuclos.server.dblayer.IBatch;
import org.nuclos.server.dblayer.IPreparedStringExecutor;
import org.nuclos.server.dblayer.expression.DbIncrement;
import org.nuclos.server.dblayer.expression.DbNull;
import org.nuclos.server.dblayer.impl.BatchImpl;
import org.nuclos.server.dblayer.impl.LogOnlyPreparedStringExecutor;
import org.nuclos.server.dblayer.impl.SQLUtils2;
import org.nuclos.server.dblayer.impl.StandardPreparedStringExecutor;
import org.nuclos.server.dblayer.impl.util.DbTupleImpl;
import org.nuclos.server.dblayer.impl.util.DbTupleImpl.DbTupleElementImpl;
import org.nuclos.server.dblayer.impl.util.PreparedString;
import org.nuclos.server.dblayer.impl.util.PreparedStringBuilder;
import org.nuclos.server.dblayer.incubator.DbExecutor.ConnectionRunner;
import org.nuclos.server.dblayer.incubator.DbExecutor.ResultSetRunner;
import org.nuclos.server.dblayer.query.DbExpression;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbJoin;
import org.nuclos.server.dblayer.query.DbOrder;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.dblayer.query.DbSelection;
import org.nuclos.server.dblayer.statements.AbstractDbStatementVisitor;
import org.nuclos.server.dblayer.statements.DbBatchStatement;
import org.nuclos.server.dblayer.statements.DbBuildableStatement;
import org.nuclos.server.dblayer.statements.DbDeleteStatement;
import org.nuclos.server.dblayer.statements.DbInsertStatement;
import org.nuclos.server.dblayer.statements.DbPlainStatement;
import org.nuclos.server.dblayer.statements.DbStatement;
import org.nuclos.server.dblayer.statements.DbStatementVisitor;
import org.nuclos.server.dblayer.statements.DbStructureChange;
import org.nuclos.server.dblayer.statements.DbUpdateStatement;
import org.nuclos.server.dblayer.structure.DbArtifact;
import org.nuclos.server.dblayer.structure.DbCallable;
import org.nuclos.server.dblayer.structure.DbColumn;
import org.nuclos.server.dblayer.structure.DbColumnType;
import org.nuclos.server.dblayer.structure.DbColumnType.DbGenericType;
import org.nuclos.server.dblayer.structure.DbConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbForeignKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbPrimaryKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbUniqueConstraint;
import org.nuclos.server.dblayer.structure.DbIndex;
import org.nuclos.server.dblayer.structure.DbNullable;
import org.nuclos.server.dblayer.structure.DbReference;
import org.nuclos.server.dblayer.structure.DbSequence;
import org.nuclos.server.dblayer.structure.DbSimpleView;
import org.nuclos.server.dblayer.structure.DbSimpleView.DbSimpleViewColumn;
import org.nuclos.server.dblayer.structure.DbTable;
import org.nuclos.server.dblayer.structure.DbTableArtifact;
import org.nuclos.server.dblayer.structure.DbTableType;
import org.nuclos.server.dblayer.util.ServerCryptUtil;
import org.nuclos.server.dblayer.util.StatementToStringVisitor;
import org.nuclos.server.report.valueobject.ResultColumnVO;
import org.nuclos.server.report.valueobject.ResultVO;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public abstract class StandardSqlDBAccess extends AbstractDBAccess {

    public static final String MAX_ROWS_HINT = "maxRows";
    public static final String QUERY_TIMEOUT_HINT = "queryTimeout";

    private static final Logger LOG = Logger.getLogger(StandardSqlDBAccess.class);

    protected StandardSqlDBAccess() {
    }

	/**
	 * @deprecated Use an IBatch for executing structural DB changes.
	 */
	@Override
	public int execute(List<? extends DbBuildableStatement> statements) throws DbException {
		int result = 0;
		DbStatementVisitor<Integer> visitor = createCommandVisitor();
		for (DbBuildableStatement statement : statements) {
			try {
				result += statement.build().accept(visitor);
			} catch (SQLException e) {
				throw wrapSQLException(null, "execute DbBuildableStatement " + statement + " failed: " + e.toString(), e);
			}
		}
		return result;
	}

    @Override
    public <T> List<T> executeQuery(DbQuery<? extends T> query) throws DbException {
        return executeQuery(query, TransformerUtils.<T>id());
    }

    @Override
    public <T> T executePlainQuery(String sql, int maxRows, ResultSetRunner<T> runner) throws DbException {
        Map<String, Object> hints = null;
        if (maxRows != -1)
            hints = Collections.<String, Object>singletonMap(MAX_ROWS_HINT, maxRows);
        return executeQuery(sql, hints, runner);
    }

    @Override
    public ResultVO executePlainQueryAsResultVO(String sql, int maxRows) throws DbException {
        return executePlainQuery(sql, maxRows, new ResultSetRunner<ResultVO>() {
            @Override
            public ResultVO perform(ResultSet rs) throws SQLException {
                ResultVO result = new ResultVO();
                ResultSetMetaData metadata = rs.getMetaData();

                Class<?>[] javaTypes = new Class<?>[metadata.getColumnCount()];
                for (int i = 0; i < metadata.getColumnCount(); i++) {
                    ResultColumnVO column = new ResultColumnVO();
                    column.setColumnLabel(metadata.getColumnLabel(i + 1));

                    DbGenericType type = getDbGenericType(metadata.getColumnType(i + 1), metadata.getColumnTypeName(i + 1));
                    if (type != null) {
                    	Class<?> javaType = type.getPreferredJavaType();
                    	// override java type here @todo this is not the right place.
                    	if (type == DbGenericType.NUMERIC) {
                    		if (metadata.getScale(i+1) == 0)
                    			javaType = Integer.class;
                    		else
                    			javaType = Double.class;
                    	}
                        column.setColumnClassName(javaType.getName());
                        javaTypes[i] = javaType;
                    } else {
                        column.setColumnClassName(metadata.getColumnClassName(i + 1));
                        javaTypes[i] = Object.class;
                    }
                    result.addColumn(column);
                }
                while (rs.next()) {
                    final Object[] values = new Object[javaTypes.length];
                    for (int i = 0; i < values.length; i++) {
                        values[i] = getResultSetValue(rs, i + 1, javaTypes[i]);
                    }
                    result.addRow(values);
                }
                return result;
            }
        });
    }

    @Override
    public <T, R> List<R> executeQuery(DbQuery<T> query, Transformer<? super T, R> transformer) throws DbException {
        StandardQueryBuilder queryBuilder = (StandardQueryBuilder) query.getBuilder();
        PreparedString ps = queryBuilder.getPreparedString(query);
        // logSql("execute SQL query", ps.toString(), ps.getParameters());
        // Create runner
        ResultSetRunner<List<R>> runner = queryBuilder.createListResultSetRunner(query, transformer);
        return executeQuery(ps.toString(), null, runner, ps.getParameters());
    }

    @Override
    public <T> T executeQuerySingleResult(DbQuery<? extends T> query) throws DbException, DbInvalidResultSizeException {
        List<T> result = executeQuery(query.maxResults(2));
        if (result.size() != 1) {
            throw new DbInvalidResultSizeException(null, "Invalid result set size", result.size());
        }
        return result.get(0);
    }

    protected <T> T executeQuery(final String sql, final Map<String, Object> hints, final ResultSetRunner<T> runner, final Object...parameters) throws DbException {
    	if (LOG.isDebugEnabled()) {
    		LOG.debug("Query to execute:\n\t" + sql + "\n\t" + Arrays.asList(parameters));
    	}
        try {
			return executor.execute(new ConnectionRunner<T>() {
			    @Override
			    public T perform(Connection conn) throws SQLException {
			        PreparedStatement stmt = conn.prepareStatement(sql);
			        if (hints != null) {
			            setStatementHints(stmt, hints);
			        }
			        try {
			            executor.prepareStatementParameters(stmt, parameters);
			            ResultSet rs = stmt.executeQuery();
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
		} catch (SQLException e) {
    		LOG.error("SQL query failed with " + e.toString() + ":\n\t" + sql + "\n\t" + Arrays.asList(parameters));
    		throw wrapSQLException(null, "executeQuery(" + sql + ") failed", e);
		}
    }

    protected final void setStatementHints(Statement stmt, Map<String, Object> hints) throws SQLException {
        if (hints != null) {
            for (Map.Entry<String, Object> e : hints.entrySet()) {
                setStatementHint(stmt, e.getKey(), e.getValue());
            }
        }
    }

    protected boolean setStatementHint(Statement stmt, String hint, Object value) throws SQLException {
        if (MAX_ROWS_HINT.equals(hint)) {
            stmt.setMaxRows((Integer) value);
            return true;
        } else if (QUERY_TIMEOUT_HINT.equals(hint)) {
            stmt.setQueryTimeout((Integer) value);
            return true;
        }
        return false;
    }

    @Override
    public DalCallResult executeBatch(final IBatch batch, EBatchType type) {
    	return batch.process(getPreparedStringExecutor(), type);
    }

    @Override
    public List<String> getStatementsForLogging(final IBatch batch) {
    	final LogOnlyPreparedStringExecutor ex = new LogOnlyPreparedStringExecutor();
    	// Sometimes (e.g. for creating a virtual entity), there is no SQL to execute. (tp)
    	if (batch != null)
    		batch.process(ex, EBatchType.FAIL_NEVER);
    	return ex.getStatements();
    }

    @Override
    public <T> T executeFunction(String name, Class<T> resultType, Object...args) throws DbException {
        return executeCallable(name, resultType, args);
    }

    @Override
    public void executeProcedure(String name, Object...args) throws DbException {
        executeCallable(name, Void.class, args);
    }

    protected <T> T executeCallable(final String name, final Class<T> resultType, final Object...args) throws DbException {
        final boolean hasOut = resultType != Void.class;
        final String call = String.format("{%scall %s(%s)}",
                hasOut ? "? = " : "", name,
                        StringUtils.join(", ", CollectionUtils.replicate("?", args.length)));
        try {
			return executor.execute(new ConnectionRunner<T>() {
			    @Override
			    public T perform(Connection conn) throws SQLException {
			        final CallableStatement stmt = conn.prepareCall(call);
			        try {
			            executor.prepareStatementParameters(stmt, hasOut ? 2 : 1, Arrays.asList(args));
			            if (hasOut)
			                stmt.registerOutParameter(1, executor.getPreferredSqlTypeFor(resultType));
			            stmt.execute();
			            if (hasOut) {
			                return getCallableResultValue(stmt, 1, resultType);
			            } else {
			                return null;
			            }
			        } finally {
			            stmt.close();
			        }
			    }
			});
		} catch (SQLException e) {
    		LOG.error("executeCallable failed with " + e.toString() + ":\n\t" + call + "\n\t" + Arrays.asList(args));
    		throw wrapSQLException(null, "executeCallable failed", e);
		}
    }

	protected List<String> getColumns(final String tableOrView) {
		try {
			return executor.execute(new ConnectionRunner<List<String>>() {
				@Override
				public List<String> perform(Connection conn) throws SQLException {
					final DatabaseMetaData meta = conn.getMetaData();
					// we NEED  toLowerCase() here, at least for PostgreSQL (tp)
					final ResultSet columns = meta.getColumns(catalog, schema, tableOrView.toLowerCase(), "%");
					final List<String> result = new ArrayList<String>();
					while (columns.next()) {
						result.add(columns.getString("COLUMN_NAME"));
					}
					if (result.size() == 0) {
						throw new IllegalArgumentException("Table " + tableOrView + " with no columns?");
					}
					return result;
				}
			});
		} catch (SQLException e) {
			LOG.error("getColumns failed with " + e.toString());
			throw wrapSQLException(null, "getColumns failed", e);
		}
	}

    protected List<DbSimpleViewColumn> ensureNaturalSequence(DbSimpleView view, List<DbSimpleViewColumn> columns) {
    	final int size = columns.size();
    	final List<DbSimpleViewColumn> result = new ArrayList<DbSimpleView.DbSimpleViewColumn>(size);
    	final List<String> natural = getColumns(view.getViewName());

    	COLUMNS:
    	for (String n: natural) {
    		final Iterator<DbSimpleViewColumn> it = columns.iterator();
    		while (it.hasNext()) {
    			final DbSimpleViewColumn c = it.next();
    			if (c.getColumnName().equalsIgnoreCase(n)) {
    				// http://support.novabit.de/browse/NUCLOSINT-1356
    				// it.remove();
    				result.add(c);
    				continue COLUMNS;
    			}
    		}
    		// We only get here if we can't find the column
    		throw new IllegalStateException("Can't find column " + n + " of " + view + " in " + columns);
    	}
    	return result;
    }

	protected boolean existsTableOrView(final String tableOrView) {
		try {
			return executor.execute(new ConnectionRunner<Boolean>() {
				@Override
				public Boolean perform(Connection conn) throws SQLException {
					final DatabaseMetaData meta = conn.getMetaData();
					// we NEED  toLowerCase() here, at least for PostgreSQL (tp)
					final ResultSet columns = meta.getTables(catalog, schema, tableOrView.toLowerCase(), null);
					return Boolean.valueOf(columns.next());
				}
			});
		} catch (SQLException e) {
			LOG.error("existsTableOrView failed with " + e.toString());
			throw wrapSQLException(null, "existsTableOrView failed", e);
		}
	}

    @Override
    public Set<String> getTableNames(final DbTableType tableType) throws DbException {
        try {
			return executor.execute(new ConnectionRunner<Set<String>>() {
			    @Override
			    public Set<String> perform(Connection conn) throws SQLException {
		    		return getMetaDataExtractor().setup(conn, catalog, schema).getTableNames(tableType);
			    }
			});
		} catch (SQLException e) {
    		LOG.error("getTableNames failed with " + e.toString() + ":\n\tcatalog: " + catalog + " schema: " + schema
    				+ " table: " + tableType);
    		throw wrapSQLException(null, "getTableNames failed", e);
		}
    }

    @Override
    public DbTable getTableMetaData(final String tableName) throws DbException {
        try {
			return executor.execute(new ConnectionRunner<DbTable>() {
			    @Override
			    public DbTable perform(Connection conn) throws SQLException {
		    		return getMetaDataExtractor().setup(conn, catalog, schema).getTableMetaData(tableName);
			    }
			});
		} catch (SQLException e) {
    		LOG.error("getTableMetaData failed with " + e.toString() + ":\n\tcatalog: " + catalog + " schema: " + schema
    				+ " table: " + tableName);
    		throw wrapSQLException(null, "getTableMetaData(" + tableName + ") failed", e);
		}
    }

    @Override
    public Set<String> getCallableNames() throws DbException {
        try {
			return executor.execute(new ConnectionRunner<Set<String>>() {
			    @Override
			    public Set<String> perform(Connection conn) throws SQLException {
		    		return getMetaDataExtractor().setup(conn, catalog, schema).getCallableNames();
			    }
			});
		} catch (SQLException e) {
    		LOG.error("getCallableNames failed with " + e.toString() + ":\n\tcatalog: " + catalog + " schema: " + schema);
    		throw wrapSQLException(null, "getCallableNames failed", e);
		}
    }

    @Override
    public Collection<DbArtifact> getAllMetaData() throws DbException {
        try {
			return executor.execute(new ConnectionRunner<Collection<DbArtifact>>() {
			    @Override
			    public Collection<DbArtifact> perform(Connection conn) throws SQLException {
		    		return getMetaDataExtractor().setup(conn, catalog, schema).getAllMetaData();
			    }
			});
		} catch (SQLException e) {
    		LOG.error("getAllMetaData failed with " + e.toString() + ":\n\tcatalog: " + catalog + " schema: " + schema);
			throw wrapSQLException(null, "getAllMetaData fails", e);
		}
    }

    @Override
    public Map<String, Object> getMetaDataInfo() throws DbException {
        try {
			return executor.execute(new ConnectionRunner<Map<String, Object>>() {
			    @Override
			    public Map<String, Object> perform(Connection conn) throws SQLException {
			    	return getMetaDataExtractor().setup(conn, catalog, schema).getMetaDataInfo();
			    }
			});
		} catch (SQLException e) {
    		LOG.error("getMetaDataInfo failed with " + e.toString() + ":\n\tcatalog: " + catalog + " schema: " + schema);
			throw wrapSQLException(null, "getMetaDataInfo fails", e);
		}
    }

    @Override
    public Map<String, String> getDatabaseParameters() throws DbException {
        try {
			return executor.execute(new ConnectionRunner<Map<String, String>>() {
			    @Override
			    public Map<String, String> perform(Connection conn) throws SQLException {
			    	return getMetaDataExtractor().setup(conn, catalog, schema).getDatabaseParameters();
			    }
			});
		} catch (SQLException e) {
    		LOG.error("getDatabaseParameters failed with " + e.toString() + ":\n\tcatalog: " + catalog + " schema: " + schema);
			throw wrapSQLException(null, "getDatabaseParameters fails", e);
		}
    }

    protected static <T> T getResultSetValue(ResultSet rs, int index, Class<T> javaType) throws SQLException {
        Object value;
        if (javaType == String.class) {
            value = rs.getString(index);
        } else if (javaType == NuclosPassword.class) {
            value = new NuclosPassword(ServerCryptUtil.decrypt(rs.getString(index)));
        } else if (javaType == Double.class){
            value = rs.getDouble(index);
        } else if (javaType == Long.class){
            value = rs.getLong(index);
        } else if (javaType == Integer.class){
            value = rs.getInt(index);
        } else if (javaType == Boolean.class) {
            value = rs.getBoolean(index);
        } else if (javaType == BigDecimal.class) {
            value = rs.getBigDecimal(index);
        } else if (javaType == java.util.Date.class || javaType == java.sql.Date.class) {
            value = rs.getDate(index);
        } else if (javaType == InternalTimestamp.class) {
            value = InternalTimestamp.toInternalTimestamp(rs.getTimestamp(index));
        } else if (javaType == NuclosDateTime.class) {
            value = NuclosDateTime.toNuclosDateTime(rs.getTimestamp(index));
        } else if (javaType == byte[].class) {
            value = rs.getBytes(index);
        } else if (javaType == Object.class) {
            value = rs.getObject(index);
        } else if (javaType == NuclosScript.class) {
        	String xml = rs.getString(index);
        	if (StringUtils.isNullOrEmpty(xml)) {
        		value = null;
        	}
        	else {
        		final XStream xstream = XStreamSupport.getInstance().getXStreamUtf8();
        		value = xstream.fromXML(rs.getString(index));
        	}
        } else {
            throw new IllegalArgumentException("Class " + javaType + " not supported by readField");
        }
        return rs.wasNull() ? null : javaType.cast(value);
    }

    protected <T> T getCallableResultValue(CallableStatement stmt, int index, Class<T> javaType) throws SQLException {
        Object value;
        if(javaType == String.class) {
            value = stmt.getString(index);
        } else if (javaType == NuclosPassword.class) {
            value = new NuclosPassword(ServerCryptUtil.decrypt(stmt.getString(index)));
        } else if(javaType == Double.class){
            value = stmt.getDouble(index);
        } else if(javaType == Long.class){
            value = stmt.getLong(index);
        } else if(javaType == Integer.class){
            value = stmt.getInt(index);
        } 	else if(javaType == Boolean.class) {
            value = stmt.getBoolean(index);
        } 	else if(javaType == BigDecimal.class) {
            value = stmt.getBigDecimal(index);
        } 	else if(javaType == java.util.Date.class) {
            value = stmt.getDate(index);
        } 	else if(javaType == byte[].class) {
            value = stmt.getBytes(index);
        } 	else if (javaType == NuclosScript.class) {
    		final XStream xstream = XStreamSupport.getInstance().getXStreamUtf8();
            value = xstream.fromXML(stmt.getString(index));
        }   else {
            throw new IllegalArgumentException("Class " + javaType + " not supported by readField");
        }
        return stmt.wasNull() ? null : javaType.cast(value);
    }

    protected static DbGenericType getDbGenericType(int sqlType, String typeName) {
        switch (sqlType) {
        case Types.VARCHAR:
        case Types.NVARCHAR:
        case Types.NCHAR:
        case Types.CHAR:
            return DbGenericType.VARCHAR;
        case Types.NUMERIC:
        case Types.DECIMAL:
            return DbGenericType.NUMERIC;
        case Types.BIT:
        case Types.BOOLEAN:
            return DbGenericType.BOOLEAN;
        case Types.DATE:
            return DbGenericType.DATE;
        case Types.BLOB:
        case Types.VARBINARY:
        case Types.BINARY:
        case Types.LONGVARBINARY:
            return DbGenericType.BLOB;
        case Types.CLOB:
        case Types.LONGVARCHAR:
            return DbGenericType.CLOB;
        case Types.TIMESTAMP:
            return DbGenericType.DATETIME;
        default:
            return null;
        }
    }

    protected String getName(String name) {
        return makeIdent(name);
    }

    protected String getQualifiedName(String name) {
        return makeIdent(getSchemaName()) + "." + makeIdent(name);
    }

    protected String makeIdent(String name) {
        return name;
    }

    @Override
    protected DbException wrapSQLException(Long id, String message, SQLException ex) {
        return new DbException(id, message, ex);
    }

	/**
	 * @deprecated Use an IBatch for executing structural DB changes.
	 */
    protected DbStatementVisitor<Integer> createCommandVisitor() {
        return new StatementVisitor();
    }

    protected IPreparedStringExecutor getPreparedStringExecutor() {
    	return new StandardPreparedStringExecutor(executor);
    }

    protected abstract MetaDataSchemaExtractor getMetaDataExtractor();

    @Override
    public boolean checkSyntax(final String sql) {
        try {
			executor.execute(new ConnectionRunner<Void>() {
			    @Override
			    public Void perform(Connection conn) throws SQLException {
			        final Statement stmt = conn.createStatement();
			        try {
			            setStatementHint(stmt, MAX_ROWS_HINT, 1);
			            setStatementHint(stmt, QUERY_TIMEOUT_HINT, 300);
			            stmt.execute(sql);
			        } finally {
			            stmt.close();
			        }
			        return null;
			    }
			});
		} catch (SQLException e) {
    		LOG.error("checkSyntax failed with " + e.toString() + ":\n\t" + sql);
			throw wrapSQLException(null, "checkSyntax fails on '" + sql + "'", e);
		}
        return true;
    }

    public static abstract class StandardQueryBuilder extends DbQueryBuilder {

    	private final StandardSqlDBAccess dbAccess;

    	protected StandardQueryBuilder(StandardSqlDBAccess dbAccess) {
    		if (dbAccess == null) {
    			throw new NullPointerException();
    		}
    		this.dbAccess = dbAccess;
    	}

    	@Override
    	public StandardSqlDBAccess getDBAccess() {
    		return dbAccess;
    	}

        protected <T, R> ResultSetRunner<List<R>> createListResultSetRunner(final DbQuery<? extends T> query, Transformer<? super T, R> transformer)
        		throws DbException {
            final List<? extends DbSelection<? extends T>> selections = query.getSelections();
            final DbTupleElementImpl<T>[] elements = new DbTupleElementImpl[selections.size()];
            for (int i = 0; i < selections.size(); i++) {
                DbSelection<? extends T> selection = selections.get(i);
                elements[i] = new DbTupleElementImpl<T>((DbSelection<T>) selection);
            }
            Transformer<Object[], ? extends Object> internalTransformer;
            if (query.getResultType() == Object[].class) {
                internalTransformer = TransformerUtils.id();
            } else if (query.getResultType() == DbTuple.class) {
                internalTransformer = new Transformer<Object[], DbTuple>() {
                    @Override
                    public DbTuple transform(Object[] values) {
                        return new DbTupleImpl(elements, values);
                    }
                };
            } else if (query.getResultType().isAssignableFrom(elements[0].getJavaType())) {
                internalTransformer = new Transformer<Object[], T>() {
                    @Override
                    public T transform(Object[] values) { return (T) values[0]; };
                };
            } else {
                throw new DbException("Query does not return result of type " + query.getResultType());
            }
            if (transformer == TransformerUtils.id()) {
                return new DefaultResultSetRunner(elements, internalTransformer);
            } else {
                return new DefaultResultSetRunner<R>(elements, TransformerUtils.chained(internalTransformer, (Transformer<Object, R>) transformer));
            }
        }

        protected final PreparedString getPreparedString(DbQuery<?> query) {
            return buildPreparedString(query).toPreparedString(null);
        }

        @Override
        protected PreparedStringBuilder buildPreparedString(DbQuery<?> query) {
            PreparedStringBuilder ps = new PreparedStringBuilder();
            prepareSelect(ps, query);
            appendSelection(ps, query.getSelections());
            postprocessSelect(ps, query);
            ps.append(" FROM ");
            appendFrom(ps, query.getRoots());
            if (query.getRestriction() != null) {
                ps.append(" WHERE ").append(getPreparedString(query.getRestriction()));
            }
            if (!query.getGroupList().isEmpty()) {
                String sep = " GROUP BY ";
                for (DbExpression<?> grouping : query.getGroupList()) {
                    ps.append(sep);
                    ps.append(getPreparedString(grouping));
                    sep = ", ";
                }
            }
            if (query.getGroupRestriction() != null) {
                ps.append(" HAVING ").append(getPreparedString(query.getGroupRestriction()));
            }
            prepareOrderBy(ps, query);
            return ps;
        }

        protected void prepareOrderBy(PreparedStringBuilder ps, DbQuery<?> query) {
        	if (!query.getOrderList().isEmpty()) {
                String sep = " ORDER BY ";
                for (DbOrder order : query.getOrderList()) {
                    ps.append(sep);
                    ps.append(getPreparedString(order.getExpression()));
                    ps.append(order.isAscending() ? " ASC" : " DESC");
                    sep = ", ";
                }
            }
        }

        protected void prepareSelect(PreparedStringBuilder ps, DbQuery<?> query) {
            ps.append("SELECT ");
            if (query.isDistinct())
                ps.append("DISTINCT ");
        }

        protected void postprocessSelect(PreparedStringBuilder ps, DbQuery<?> query) {

        }

        private void appendSelection(PreparedStringBuilder ps, List<? extends DbSelection<?>> selections) {
        	for (Iterator<? extends DbSelection<?>> it = selections.iterator(); it.hasNext();) {
        		final DbSelection<?> s = it.next();
        		ps.append(s.getSqlColumnExpr());
        		if (it.hasNext()) {
        			ps.append(",");
        		}
        		ps.append(" ");
        	}
        }

        protected void appendFrom(PreparedStringBuilder ps, Set<DbFrom> roots) {
            if (roots.isEmpty())
                throw new RuntimeException("No root for query" + this);

            int index = 0;
            for (DbFrom root : roots) {
                if (index++ > 0)
                    ps.append(", ");
                ps.append(root.getTableName() + " " + /* Oracle don't like it " AS " + */ root.getAlias());
                for (DbJoin join : getAllJoins(root)) {
                    switch (join.getJoinType()) {
                    case INNER:
                        ps.append(" INNER JOIN ");
                        break;
                    case LEFT:
                        ps.append(" LEFT OUTER JOIN ");
                        break;
                    case RIGHT:
                        ps.append(" RIGHT OUTER JOIN ");
                        break;
                    default:
                        throw new IllegalArgumentException("Join type " + join.getJoinType() + " not supported");
                    }
                    ps.append(join.getTableName() + " " + join.getAlias());
                    ps.append(" ON (");
                    /*
                    ps.append(join.getParent().getAlias() + "." + join.getOn().x);
                    ps.append(" = ");
                    ps.append(join.getAlias() + "." + join.getOn().y);
                    */
                    ps.append(join.getOn().getSqlString());
                    ps.append(")");
                }
            };
        }

        protected Set<DbJoin> getAllJoins(DbFrom root) {
            return collectAllJoinsImpl(root, new LinkedHashSet<DbJoin>());
        }

        private Set<DbJoin> collectAllJoinsImpl(DbFrom parent, Set<DbJoin> set) {
            for (DbJoin join : parent.getJoins()) {
                if (set.add(join))
                    collectAllJoinsImpl(join, set);
            }
            return set;
        }
    }

	/**
	 * @deprecated Use an IBatch for executing structural DB changes.
	 */
    public class StatementVisitor implements DbStatementVisitor<Integer> {

    	private final EBatchType type;

    	public StatementVisitor() {
    		type = EBatchType.FAIL_LATE;
    	}

        @Override
        public Integer visitInsert(final DbInsertStatement insertStmt) throws SQLException {
            return executeBatch(getBatchFor(insertStmt), type).getNumberOfDbChanges();
        }

        @Override
        public Integer visitDelete(final DbDeleteStatement deleteStmt) throws SQLException {
            return executeBatch(getBatchFor(deleteStmt), type).getNumberOfDbChanges();
        }

        @Override
        public Integer visitUpdate(final DbUpdateStatement updateStmt) throws SQLException {
            return executeBatch(getBatchFor(updateStmt), type).getNumberOfDbChanges();
        }

        @Override
        public Integer visitStructureChange(DbStructureChange command) throws SQLException {
            int result = -1;
            String message = "Unknown error";
            final IBatch batch;
            try {
            	batch = getBatchFor(command);
            	// sometimes (i.e. for creating virtual entities, there is no SQL to execute, hence...
            	if (batch != null) {
            		result = executeBatch(batch, type).getNumberOfDbChanges();
            	}
        		message = "Success";
            } catch (SQLException e) {
                message = "Error: " + e;
                throw e;
            } finally {
                logStructureChange(command, message);
            }
            return result;
        }

        /**
         * @deprecated
         */
    	private void logStructureChange(DbStructureChange command, String result) {
    		if (structureChangeLogDir == null)
    			return;
    		try {
    			Date date = new Date();
    			synchronized (DbAccess.class) {
    				if (!structureChangeLogDir.exists()) {
    					structureChangeLogDir.mkdirs();
    				}
    				PrintWriter w = new PrintWriter(new BufferedWriter(
    					new FileWriter(new File(structureChangeLogDir, String.format("dbchanges-%tF.log", date)), true)));
    				try {
    					w.println(String.format("---------- %1$tFT%1$tT ----------------------------", date));
    					w.println("-- " + command.accept(new StatementToStringVisitor()));
    					int i = 0;
    					final IBatch batch = getBatchFor(command);
    					for (String ps : getStatementsForLogging(batch)) {
    						w.println(ps);
    						if (i++ > 0)
    							w.println();
    					}
    					if (result != null)
    						w.println("-- => " + result);
    					w.println();
    					w.flush();
    				} finally {
    					w.close();
    				}
    			}
    		} catch (Exception e) {
    			LOG.debug(e);
    			LOG.error("Exception during structure change logging: " + e);
    		}
    	}

        @Override
        public Integer visitPlain(DbPlainStatement command) {
            try {
				return executeBatch(getBatchFor(command), type).getNumberOfDbChanges();
			} catch (SQLException e) {
				throw wrapSQLException(null, "visitPlain fails on " + command, e);
			}
        }

        @Override
        public Integer visitBatch(DbBatchStatement batch) throws SQLException {
            // TODO: reuse PreparedStatement if two consecutive statements are compatible
            int result = 0;
            final List<SQLException> exceptions = new ArrayList<SQLException>();
            for (DbStatement stmt : batch.getStatements()) {
                try {
                    result += stmt.accept(this);
                } catch (SQLException e) {
                	LOG.error("visitBatch failed on " + stmt, e);
                    if (batch.isFailFirst()) {
                        throw e;
                    }
                    else {
                        exceptions.add(e);
                    }
                }
            }
            if (!exceptions.isEmpty()) {
                throw exceptions.get(0);
            }
            return result;
        }
    }

    protected String getColumnSpecForAlterTableColumn(DbColumn column, DbColumn oldColumn) {
        return getColumnSpec(column, column.getNullable() != oldColumn.getNullable());
    }

    protected String getColumnSpec(DbColumn column, boolean withNullable) {
        if (withNullable) {
            return String.format("%s %s %s", column.getColumnName(), getDataType(column.getColumnType()), column.getNullable());
        } else {
            return String.format("%s %s", column.getColumnName(), getDataType(column.getColumnType()));
        }
    }

    protected String getColumnSpecNullable(DbColumn column) {
    	return String.format("%s %s %s", column.getColumnName(), getDataType(column.getColumnType()), DbNullable.NULL);
    }

    protected String getUsingIndex(DbConstraint constraint) {
        return "USING INDEX " + getTablespaceSuffix(constraint);
    }

    protected String getTablespaceSuffix(DbArtifact artifact) {
        return "";
    }

    @Override
    protected IBatch getSqlForInsert(DbInsertStatement insertStmt) {
        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
            insertStmt.getTableName(),
            StringUtils.join(", ", insertStmt.getColumnValues().keySet()),
            StringUtils.join(", ", CollectionUtils.replicate("?", insertStmt.getColumnValues().size())));
        Object[] params = insertStmt.getColumnValues().values().toArray();
        return BatchImpl.simpleBatch(new PreparedString(sql, params));
    }

    @Override
    public IBatch getSqlForDelete(DbDeleteStatement deleteStmt) {
        String sql = "DELETE FROM " + deleteStmt.getTableName();
        if (deleteStmt.getConditions() != null) {
            sql = sql + " WHERE " + buildWhereString(deleteStmt.getConditions());
        }
        Object[] params = deleteStmt.getConditions().values().toArray();
        return BatchImpl.simpleBatch(new PreparedString(sql, params));
    }

    @Override
    public IBatch getSqlForUpdate(DbUpdateStatement updateStmt) {
        return BatchImpl.simpleBatch(getPreparedStringForUpdate(updateStmt));
    }

	protected PreparedString getPreparedStringForUpdate(DbUpdateStatement updateStmt) {
		String sql = String.format("UPDATE %s SET %s WHERE %s", updateStmt.getTableName(),
				buildUpdateString(updateStmt.getColumnValues()), buildWhereString(updateStmt.getConditions()));

		Object[] params = CollectionUtils.concat(updateStmt.getColumnValues().values(),
				updateStmt.getConditions().values()).toArray();
		return new PreparedString(sql, params);
	}

    private String buildWhereString(Collection<String> names) {
        if (names.isEmpty())
            return "1=1";
        return StringUtils.join(" AND ", CollectionUtils.transform(names, new Transformer<String, String>() {
            @Override
            public String transform(String c) { return c + " = ?"; }
        }));
    }

    private String buildWhereString(final Map<String, Object> mpConditions) {
        if (mpConditions.isEmpty())
            return "1=1";

        return StringUtils.join(" AND ", CollectionUtils.transform(new HashSet<String>(mpConditions.keySet()), new Transformer<String, String>() {
            @Override
            public String transform(String c) {
            	if(mpConditions.get(c) instanceof DbNull<?>) {
            		mpConditions.remove(c);
            		return c + " is null ";
            	}
            	else
            		return c + " = ?";
            	}
        }));
    }

    private String buildUpdateString(Map<String, Object> conditions) {
        return StringUtils.join(", ", CollectionUtils.transform(conditions.entrySet(), new Transformer<Map.Entry<String, Object>, String>() {
            @Override
            public String transform(Map.Entry<String, Object> e) {
                String c = e.getKey();
                if (e.getValue().getClass() == DbIncrement.class) {
                    return c + " = " + c + " + 1";
                }
                return c + " = ?";
            }
        }));
    }

    @Override
    protected IBatch getSqlForCreateTable(DbTable table) {
        List<PreparedString> list = new ArrayList<PreparedString>();
        List<DbColumn> columns = table.getTableArtifacts(DbColumn.class);
		if (!table.isVirtual()) {
			list.add(PreparedString.format("CREATE TABLE %s(\n%s\n) %s", getQualifiedName(table.getTableName()),
					join(",\n", transform(columns, new Transformer<DbColumn, String>() {
						@Override
						public String transform(DbColumn column) {
							return getColumnSpec(column, true);
						}
					})),
					getTablespaceSuffix(table)));
		}
		final IBatch result = BatchImpl.simpleBatch(list);
        for (DbTableArtifact tableArtifact : table.getTableArtifacts()) {
            if (!columns.contains(tableArtifact))
                result.append(getSqlForCreate(tableArtifact));
        }
        return result;
    }

    @Override
	protected abstract IBatch getSqlForAlterTableNotNullColumn(DbColumn column);

    @Override
    protected IBatch getSqlForCreateColumn(DbColumn column) throws SQLException {
    	final PreparedString ps = PreparedString.format("ALTER TABLE %s ADD %s",
            getQualifiedName(column.getTableName()),
            getColumnSpec(column, true));

    	final IBatch result;
    	if(column.getDefaultValue() != null && column.getNullable().equals(DbNullable.NOT_NULL)) {
    		PreparedString ps2 = PreparedString.format("ALTER TABLE %s ADD %s",
                getQualifiedName(column.getTableName()),
                getColumnSpecNullable(column));

    		result = BatchImpl.simpleBatch(ps2);
    		result.append(getSqlForUpdateNotNullColumn(column));
    		result.append(getSqlForAlterTableNotNullColumn(column));
    	}
    	else {
    		result = BatchImpl.simpleBatch(ps);
    	}
    	return result;
    }

	protected IBatch getSqlForUpdateNotNullColumn(final DbColumn column) throws SQLException {
		final DbUpdateStatement stmt = DbStatementUtils.getDbUpdateStatementWhereFieldIsNull(
				getQualifiedName(column.getTableName()), column.getColumnName(), column.getDefaultValue());

		final PreparedString sPlainUpdate = stmt.build().accept(new AbstractDbStatementVisitor<PreparedString>() {
			@Override
			public PreparedString visitUpdate(DbUpdateStatement update) {
				String sUpdate = getPreparedStringForUpdate(stmt).toString();
				for(Object obj : update.getColumnValues().values()) {
					sUpdate = org.apache.commons.lang.StringUtils.replace(sUpdate, "?", "'"+obj.toString()+"'");
				}
				return new PreparedString(sUpdate);
			}
		});
		return BatchImpl.simpleBatch(sPlainUpdate);
	}

    @Override
    protected IBatch getSqlForCreatePrimaryKey(DbPrimaryKeyConstraint constraint) {
        return BatchImpl.simpleBatch(PreparedString.format("ALTER TABLE %s ADD CONSTRAINT %s PRIMARY KEY (%s) %s",
            getQualifiedName(constraint.getTableName()),
            constraint.getConstraintName(),
            join(",", constraint.getColumnNames()),
            getUsingIndex(constraint)));
    }

    @Override
    protected IBatch getSqlForCreateForeignKey(DbForeignKeyConstraint constraint) {
    	String sql = String.format("ALTER TABLE %s ADD CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s (%s)",
            getQualifiedName(constraint.getTableName()),
            constraint.getConstraintName(),
            join(",", constraint.getColumnNames()),
            getQualifiedName(constraint.getReferencedTableName()),
            join(",", constraint.getReferencedColumnNames()));
        if (constraint.isOnDeleteCascade()) {
        	sql += " ON DELETE CASCADE";
        }
        return BatchImpl.simpleBatch(new PreparedString(sql));
    }

    @Override
    protected IBatch getSqlForCreateUniqueConstraint(DbUniqueConstraint constraint) {
        return BatchImpl.simpleBatch(PreparedString.format("ALTER TABLE %s ADD CONSTRAINT %s UNIQUE (%s) %s",
            getQualifiedName(constraint.getTableName()),
            constraint.getConstraintName(),
            join(",", constraint.getColumnNames()),
            getUsingIndex(constraint)));
    }

    @Override
    protected abstract IBatch getSqlForCreateIndex(DbIndex index);

    /**
     * @deprecated Views has always been problematic (especially with PostgreSQL).
     * 	Avoid whenever possible.
     */
    @Override
    protected IBatch getSqlForCreateSimpleView(DbSimpleView view) throws DbException {
    	return BatchImpl.simpleBatch(_getSqlForCreateSimpleView("CREATE VIEW", view, ""));
    }

    protected PreparedString _getSqlForCreateSimpleView(String prefix, DbSimpleView view, String suffix) throws DbException {
    	final TableAliasForSimpleView aliasFactory = new TableAliasForSimpleView();
        final StringBuilder fromClause = new StringBuilder();
        fromClause.append(getName(view.getTableName()) + " " + SystemFields.BASE_ALIAS);

        int fkIndex = 1;
        for (DbSimpleViewColumn vc : view.getReferencingViewColumns()) {
        	final String tableAlias = aliasFactory.getTableAlias(vc);
            final DbReference fk = vc.getReference();
            fromClause.append(String.format("\nLEFT OUTER JOIN %s %s ON (%s)",
                getName(fk.getReferencedTableName()),
                tableAlias,
				join(" AND ", transform(fk.getReferences(), new Transformer<Pair<String, String>, String>() {
					@Override
					public String transform(Pair<String, String> p) {
						return String.format("t.%s=%s.%s", p.x, tableAlias, p.y);
					}
				}))));
        }
        return PreparedString.format("%s %s(\n%s\n) AS SELECT\n%s\nFROM\n%s %s",
        	prefix,
            getQualifiedName(view.getViewName()),
            join(",\n", view.getViewColumnNames()),
            join(",\n", transform(view.getViewColumns(), new Transformer<DbSimpleViewColumn, String>() {
                // int fkIndex = 1;
                @Override
                public String transform(DbSimpleViewColumn vc) {
                	String sql;
                    switch (vc.getViewColumnType()) {
                    case TABLE:
                        // ignore column type (must be the same as the original column)
                        return String.format("t.%s", vc.getColumnName());
                    case FOREIGN_REFERENCE:
                        // int fkN = fkIndex++;
                        sql = null;
                        for (Object obj : vc.getViewPattern()) {
                            String sql2;
                            if (obj instanceof DbIdent) {
                                sql2 = String.format("%s.%s", aliasFactory.getTableAlias(vc), ((DbIdent) obj).getName());
                            } else {
                                String s = (String) obj;
                                if (s.isEmpty())
                                    continue;
                                sql2 = "'" + SQLUtils2.escape(s) +"'";
                            }
                            sql = (sql != null) ? getSqlForConcat(sql, sql2) : sql2;
                        }
                        sql = getSqlForCast(sql, vc.getColumnType());
                        return getSqlForNullCheck(String.format("t.%s", getFirst(vc.getReference().getReferences()).x), sql);
                    case FUNCTION:
                        sql = String.format("%s(%s)",
                            getFunctionNameForUseInView(vc.getFunctionName()),
                            StringUtils.join(",", CollectionUtils.transform(vc.getArgColumns(),
                                new Transformer<String, String>() {
                                @Override public String transform(String c) { return "t." + c; }
                            })));
                        return getSqlForCast(sql, vc.getColumnType());
                    default:
                        throw new DbException("Unsupported view column type " + vc.getViewColumnType());
                    }
                }
            })),
            fromClause,
            suffix);
    }

    protected String getFunctionNameForUseInView(String name) {
        return name;
    }

	@Override
	public String getSqlForConcat(String x, String y) {
		// or JDBC escape syntax? String.format("{fn concat(%s,%s)}", x, y);
		return String.format("CONCAT(%s,%s)", x, y);
	}

	@Override
	public String getSqlForConcat(List<String> l) {
		if (l == null || l.isEmpty()) {
			throw new IllegalArgumentException();
		}
		final Iterator<String> it = l.iterator();
		String result = it.next();
		while (it.hasNext()) {
			result = getSqlForConcat(result, it.next());
		}
		return result;
	}

    public String getSqlForCast(String x, DbColumnType columnType) {
        return String.format("CAST(%s AS %s)", x, getDataType(columnType));
    }

    public String getSqlForNullCheck(String x, String y) {
        return String.format("CASE WHEN %s IS NOT NULL THEN %s END", x, y);
    }

    public String getSqlForSubstituteNull(String expression, String substitute) {
        return String.format("CASE WHEN %s IS NOT NULL THEN %s ELSE %s END", expression, expression, substitute);
    }

    @Override
    protected IBatch getSqlForCreateSequence(DbSequence sequence) {
        return BatchImpl.simpleBatch(PreparedString.format(
            "CREATE SEQUENCE %s INCREMENT BY 1 MINVALUE 1 MAXVALUE 999999999 START WITH %d NO CYCLE",
            getQualifiedName(sequence.getSequenceName()), sequence.getStartWith()));
    }

    @Override
    protected IBatch getSqlForCreateCallable(DbCallable callable) throws DbException {
        String code = callable.getCode();
        if (code == null)
            throw new DbException("No code for callable " + callable.getCallableName());
        Pattern pattern = Pattern.compile(
            String.format("\\s*(CREATE\\s+(OR\\s+REPLACE\\s+)?)?%s\\s+%s\\s*(?=\\W)",
                callable.getType(), callable.getCallableName()),
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(code);
        boolean success = matcher.lookingAt();
        if (!success)
            throw new DbException("Cannot interpret header for callable " + callable.getCallableName());
        return BatchImpl.simpleBatch(PreparedString.format("CREATE %s %s %s",
            callable.getType(),
            getQualifiedName(callable.getCallableName()),
            code.substring(matcher.group().length())));
    }

    @Override
    protected abstract IBatch getSqlForAlterTableColumn(DbColumn column1, DbColumn column2) throws SQLException;

    @Override
    protected IBatch getSqlForAlterSequence(DbSequence sequence1, DbSequence sequence2) {
        long restartWith = Math.max(sequence1.getStartWith(), sequence2.getStartWith());
        return BatchImpl.simpleBatch(PreparedString.format("ALTER SEQUENCE %s RESTART WITH %s",
            getQualifiedName(sequence2.getSequenceName()),
            restartWith));
    }

    @Override
    protected IBatch getSqlForDropTable(DbTable table) {
    	if (!table.isVirtual()) {
	        return BatchImpl.simpleBatch(PreparedString.format("DROP TABLE %s",
	            getQualifiedName(table.getTableName())));
    	}
    	else {
    		return null;
    	}
    }

    @Override
    protected IBatch getSqlForDropColumn(DbColumn column) {
        return BatchImpl.simpleBatch(PreparedString.format("ALTER TABLE %s DROP COLUMN %s",
            getQualifiedName(column.getTableName()),
            column.getColumnName()));
    }

    @Override
    protected IBatch getSqlForDropPrimaryKey(DbPrimaryKeyConstraint constraint) {
        return BatchImpl.simpleBatch(PreparedString.format("ALTER TABLE %s DROP CONSTRAINT %s",
            getQualifiedName(constraint.getTableName()),
            constraint.getConstraintName()));
    }

    @Override
    protected IBatch getSqlForDropForeignKey(DbForeignKeyConstraint constraint) {
        return BatchImpl.simpleBatch(PreparedString.format("ALTER TABLE %s DROP CONSTRAINT %s",
            getQualifiedName(constraint.getTableName()),
            constraint.getConstraintName()));
    }

    @Override
    protected IBatch getSqlForDropUniqueConstraint(DbUniqueConstraint constraint) {
        return BatchImpl.simpleBatch(PreparedString.format("ALTER TABLE %s DROP CONSTRAINT %s",
            getQualifiedName(constraint.getTableName()),
            constraint.getConstraintName()));
    }

    @Override
    protected IBatch getSqlForDropIndex(DbIndex index) {
        return BatchImpl.simpleBatch(PreparedString.format("DROP INDEX %s",
            getQualifiedName(index.getIndexName())));
    }

    @Override
    protected IBatch getSqlForDropSimpleView(DbSimpleView view) {
        return BatchImpl.simpleBatch(PreparedString.format("DROP VIEW %s",
            getQualifiedName(view.getViewName())));
    }

    @Override
    protected IBatch getSqlForDropSequence(DbSequence sequence) {
        return BatchImpl.simpleBatch(PreparedString.format("DROP SEQUENCE %s",
            getQualifiedName(sequence.getSequenceName())));
    }

    @Override
    protected IBatch getSqlForDropCallable(DbCallable callable) {
        return BatchImpl.simpleBatch(PreparedString.format("DROP %s %s",
            callable.getType(),
            getQualifiedName(callable.getCallableName())));
    }

    public static class DefaultResultSetRunner<T> implements ResultSetRunner<List<T>> {

        DbTupleElementImpl<?>[] elements;
        Transformer<Object[], T> transformer;

        public DefaultResultSetRunner(DbTupleElementImpl<?>[] elements, Transformer<Object[], T> transformer) {
            this.elements = elements;
            this.transformer = transformer;
        }

        @Override
        public List<T> perform(ResultSet rs) throws SQLException {
            List<T> result = new ArrayList<T>();
            while (rs.next()) {
                result.add(transform(extractRow(rs)));
            }
            return result;
        }

        protected Object[] extractRow(ResultSet rs) throws SQLException {
            int columnCount = elements.length;
            Object[] values = new Object[columnCount];
            for (int i = 0; i < columnCount; i++) {
                values[i] = getResultSetValue(rs, i + 1, elements[i].getJavaType());
            }
            return values;
        }

        protected T transform(Object[] row) {
            return transformer.transform(row);
        }
    }

}
