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

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.dblayer.impl.Base32;
import org.nuclos.server.dblayer.impl.DataSourceExecutor;
import org.nuclos.server.dblayer.impl.util.PreparedString;
import org.nuclos.server.dblayer.incubator.DbExecutor;
import org.nuclos.server.dblayer.incubator.DbExecutor.ConnectionRunner;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.dblayer.statements.DbBuildableStatement;
import org.nuclos.server.dblayer.statements.DbStatement;
import org.nuclos.server.dblayer.statements.DbStructureChange;
import org.nuclos.server.dblayer.structure.DbArtifact;
import org.nuclos.server.dblayer.structure.DbColumnType;
import org.nuclos.server.dblayer.structure.DbTable;
import org.nuclos.server.dblayer.structure.DbTableType;
import org.nuclos.server.dblayer.util.StatementToStringVisitor;
import org.nuclos.server.report.valueobject.ResultVO;


public abstract class DbAccess {

	public static final String SCHEMA = "schema";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String TABLESPACE = "tablespace";
	public static final String TABLESPACE_INDEX = "tablespace.index";
	public static final String STRUCTURE_CHANGELOG_DIR = "structure.changelog.dir";

	protected final Logger log = Logger.getLogger(this.getClass());

	protected DbType type;
	protected String catalog = null;
	protected String schema;
	protected DataSource dataSource;
	protected DbExecutor executor;
	protected Map<String, String> config;
	protected File structureChangeLogDir;
	
	protected DbAccess() {	
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("type=").append(type);
		result.append(", schema=").append(schema);
		result.append(", catalog=").append(catalog);
		result.append(", ds=").append(dataSource);
		result.append(", ex=").append(executor);
		result.append("]");
		return result.toString();
	}

	public void init(DbType type, DataSource dataSource, Map<String, String> config) {
		this.type = type;
		this.config = new HashMap<String, String>(config);
		this.executor = new DataSourceExecutor(dataSource, config.get(USERNAME), config.get(PASSWORD)); 
		/*
		{
			@Override
			protected DbException wrapSQLException(String message, SQLException ex) {
				return DbAccess.this.wrapSQLException(message, ex);
			}
		};
		 */
		if (config.containsKey(STRUCTURE_CHANGELOG_DIR)) {
			structureChangeLogDir = new File(config.get(STRUCTURE_CHANGELOG_DIR));
		}
		this.schema = resolveSchema(config);
	}

	protected String resolveSchema(Map<String, String> config) {
		final String givenSchema;
		if (!StringUtils.looksEmpty(config.get(SCHEMA))) {
			givenSchema = config.get(SCHEMA);
		} else {
			givenSchema = config.get(USERNAME);
		}
		try {
			String resolvedSchema = executor.execute(new ConnectionRunner<String>() {
				@Override
				public String perform(Connection conn) throws SQLException {
					String resolvedSchemaName = null;
					ResultSet rs = conn.getMetaData().getSchemas();
					try {
						while (rs.next()) {
							String dbSchemaName = rs.getString("TABLE_SCHEM");
							if (givenSchema.equalsIgnoreCase(dbSchemaName)) {
								resolvedSchemaName = dbSchemaName;
								if (givenSchema.equals(dbSchemaName)) {
									break;
								}
							}
						}
						return resolvedSchemaName;
	                } catch (SQLException e) {
	            		log.error("resolveSchema failed with " + e.toString() + ":\n\t" + givenSchema);
	                	throw e;
					} finally {
						rs.close();
					}
				}
			});
			if (resolvedSchema != null) {
				log.info(String.format("Schema name resolved to '%s'", resolvedSchema));
				return resolvedSchema;
			} else {
				log.error(String.format("Schema name '%s' not found", givenSchema));
				return givenSchema;
			}
		} catch (SQLException e) {
			log.warn("Exception during resolving schema names", e);
			return givenSchema;
		}
	}

	public DbType getDbType() {
		return type;
	}

	public String getSchemaName() {
		return schema;
	}

	/**
	 * Returns the configuration in use with username and password erased. 
	 * @return
	 */
	public Map<String, String> getConfig() {
		HashMap<String, String> configCopy = new HashMap<String, String>(config);
		configCopy.remove(USERNAME);
		configCopy.remove(PASSWORD);
		return configCopy;
	}

	public void close() {
	}

	//
	// Statements
	//

	public int execute(DbBuildableStatement statement) throws DbException {
		return execute(Collections.singletonList(statement));
	}

	/**
	 * Executes the given database operations.
	 * <p>
	 * Note: This method may be overridden in order to support batch execution.
	 * It is up to the implementation to find consecutive commands which
	 * can be executed in as batch statements.
	 */
	public abstract int execute(List<? extends DbBuildableStatement> statements) throws DbException;

	public final int execute(DbBuildableStatement statement1, DbBuildableStatement...statements) throws DbException {
		return execute(CollectionUtils.asList(statement1, statements));
	}

	//
	// Queries
	//

	public abstract Long getNextId(String sequenceName) throws SQLException;

	public abstract DbQueryBuilder getQueryBuilder();

	public abstract <T> List<T> executeQuery(DbQuery<? extends T> query) throws DbException;

	public abstract <T, R> List<R> executeQuery(DbQuery<T> query, Transformer<? super T, R> transformer) throws DbException;

	/**
	 * Executes a query which returns exactly one row. 
	 * @throws DbInvalidResultSizeException if no or more than one result rows are returned
	 */
	public abstract <T> T executeQuerySingleResult(DbQuery<? extends T> query) throws DbException;

	public <T, R> R executeQuerySingleResult(DbQuery<T> query, Transformer<? super T, R> transformer) throws DbException {
		return transformer.transform(executeQuerySingleResult(query));
	}

	//   public abstract List<DbTuple> executePlainQuery(String sql, int maxRows) throws DbException;

	public abstract ResultVO executePlainQueryAsResultVO(String sql, int maxRows) throws DbException;

	public abstract <T> T executeFunction(String functionName, Class<T> result, Object...args) throws DbException;

	public abstract void executeProcedure(String procedureName, Object...args) throws DbException;

	//
	// Useful informational functions (e.g. for debugging) 
	//

	public abstract List<PreparedString> getPreparedSqlFor(DbStatement stmt) throws SQLException;

	//
	// Database schema metadata
	//

	public abstract Set<String> getTableNames(DbTableType tableType) throws DbException;

	public abstract Set<String> getCallableNames() throws DbException;

	public abstract DbTable getTableMetaData(String tableName) throws DbException;

	public abstract Collection<DbArtifact> getAllMetaData() throws DbException;

	public abstract Map<String, Object> getMetaDataInfo() throws DbException;

	public abstract Map<String, String> getDatabaseParameters() throws DbException;

	/** Tries to (re)validate all invalid database objects. */
	public abstract boolean validateObjects() throws SQLException;

	/** Tries to validate the given sql string. */
	public abstract boolean checkSyntax(String sql);

	/**
	 * Invalidates all assumptions about the database made by the access layer. 
	 * Should be called after the database has been (structurally) modified
	 * by another process or thread.
	 */ 
	public void invalidate() throws DbException {
		// Currently, we do nothing...
	}

	/**
	 * Tries to disable all constraints and triggers in the database temporarily,
	 * then executes the given {@link Runnable}'s run method, and finally tries 
	 * to re-enable the all disabled constraints and triggers again.
	 */
	public abstract void runWithDisabledChecksAndTriggers(Runnable runnable) throws SQLException;

	protected abstract String getDataType(DbColumnType columnType);

	protected abstract DbException wrapSQLException(Long id, String message, SQLException ex);

	protected String getConfigParameter(String name, String defaultValue) {
		String value = StringUtils.nullIfEmpty(config.get(name));
		return (value != null) ? value : defaultValue;
	}

	private static final Charset UTF_8 = Charset.forName("UTF-8");

	/**
	 * Generate a name from a given base name and, optionally, from some additional 
	 * affixes.  This generated name must obey name limitations of the database. 
	 */
	public abstract String generateName(String base, String...affixes);

	/**
	 * Generate a name from a given base name and, optionally, from some additional 
	 * affixes with at most {@code max} characters.
	 * 
	 * The generated name consists of the base name + '{@code _}' + a Base32-encoded
	 * hash of the base name and the affixes.  If this generated string exceeds 
	 * {@code max} characters, the base name part is truncated.
	 *  
	 * The hash itself is the first 30 bits of the SHA-1 digest generated from
	 * the UTF-8 encoded base name and affixes separated by a NUL byte (30 bits
	 * corresponds to 6 characters in Base32).  
	 */
	public static String generateName(int max, String base, String...affixes) {
		if (affixes.length == 0 && base.length() <= max)
			return base;
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch(NoSuchAlgorithmException e) {
			throw new CommonFatalException("java.security.MessageDigest does not support SHA-1");
		}
		md.update(base.getBytes(UTF_8));
		for (String s : affixes) {
			md.update(new byte[1]);
			md.update(s.getBytes(UTF_8));
		}
		byte[] digest = md.digest();

		StringBuilder sb = new StringBuilder(base);
		if (sb.length() > max - 7) {
			sb.setLength(max - 8); sb.append('_');
		}
		sb.append('_').append(toBase32(digest), 0, 6);
		return sb.toString();
	}

	private static String toBase32(byte[] b) {
		return Base32.encode(b);
	}

	protected void logStructureChange(DbStructureChange command, String result) {
		if (structureChangeLogDir == null)
			return;
		try {
			Date date = new Date();
			synchronized (DbAccess.class) {
				if (!structureChangeLogDir.exists()) {
					structureChangeLogDir.mkdirs();
				}
				PrintWriter w = new PrintWriter(
					new FileWriter(new File(structureChangeLogDir, String.format("dbchanges-%tF.log", date)), true));
				try {
					w.println(String.format("---------- %1$tFT%1$tT ----------------------------", date));
					w.println("-- " + command.accept(new StatementToStringVisitor()));
					int i = 0;
					for (PreparedString ps : getPreparedSqlFor(command)) {
						w.println(ps.toString());
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
			log.debug(e);
			log.error("Exception during structure change logging: " + e);
		}
	}
	
	public abstract String getSelectSqlForColumn(String table, DbColumnType columnType, List<?> viewPattern);
	
	public String getWildcardLikeSearchChar() {
		return "%";
	}
}
