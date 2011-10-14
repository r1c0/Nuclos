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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.nuclos.server.dblayer.structure.DbArtifact;
import org.nuclos.server.dblayer.structure.DbCallable;
import org.nuclos.server.dblayer.structure.DbCallableType;
import org.nuclos.server.dblayer.structure.DbColumn;
import org.nuclos.server.dblayer.structure.DbColumnType;
import org.nuclos.server.dblayer.structure.DbIndex;
import org.nuclos.server.dblayer.structure.DbNullable;
import org.nuclos.server.dblayer.structure.DbSequence;
import org.nuclos.server.dblayer.structure.DbTable;
import org.nuclos.server.dblayer.structure.DbTableArtifact;
import org.nuclos.server.dblayer.structure.DbTableType;
import org.nuclos.server.dblayer.structure.DbColumnType.DbGenericType;
import org.nuclos.server.dblayer.structure.DbConstraint.DbForeignKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbPrimaryKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbUniqueConstraint;


/**
 * A schema extractor which extracts the schema via JDBC metadata API.
 *
 * Subclasses can override some methods in order to use a particular database API
 * to extract schema information.
 */
public abstract class MetaDataSchemaExtractor {

	protected final Logger log = Logger.getLogger(this.getClass());

	protected Connection connection;
	protected DatabaseMetaData metaData;
	protected String catalog;
	protected String schema;

	protected boolean supportsJDBC4getFunction = true;

	public MetaDataSchemaExtractor setup(Connection connection, String catalog, String schema) throws SQLException {
		this.catalog = catalog;
		this.schema = schema;
		this.connection = connection;
		initMetaData();
		return this;
	}

	protected void initMetaData() throws SQLException {
		this.metaData = connection.getMetaData();
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("catalog=").append(catalog);
		result.append(", schema=").append(schema);
		result.append("]");
		return result.toString();
	}

	public Map<String, Object> getMetaDataInfo() throws SQLException {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("databaseProductName", metaData.getDatabaseProductName());
		map.put("databaseProductVersion", metaData.getDatabaseProductVersion());
		map.put("databaseMajorVersion", metaData.getDatabaseMajorVersion());
		map.put("databaseMinorVersion", metaData.getDatabaseMinorVersion());
		map.put("database URL", metaData.getURL());
		map.put("userName", metaData.getUserName());
		map.put("driverName", metaData.getDriverName());
		map.put("driverVersion", metaData.getDriverVersion());
		map.put("driverMajorVersion", metaData.getDriverMajorVersion());
		map.put("driverMinorVersion", metaData.getDriverMinorVersion());
		map.put("JDBCMajorVersion", metaData.getJDBCMajorVersion());
		map.put("JDBCMinorVersion", metaData.getJDBCMinorVersion());
		map.put("defaultTransactionIsolation", metaData.getDefaultTransactionIsolation());
		map.put("extraNameCharacters", metaData.getExtraNameCharacters());
		map.put("identifierQuoteString", metaData.getIdentifierQuoteString());
		map.put("maxBinaryLiteralLength", metaData.getMaxBinaryLiteralLength());
		map.put("maxCatalogNameLength", metaData.getMaxCatalogNameLength());
		map.put("maxCharLiteralLength", metaData.getMaxCharLiteralLength());
		map.put("maxConnections", metaData.getMaxConnections());
		map.put("maxTablesInSelect", metaData.getMaxTablesInSelect());
		map.put("procedureTerm", metaData.getProcedureTerm());
		return map;
	}

	public Set<String> getTableNames(DbTableType tableType) throws SQLException {
		Set<String> tableNames = new LinkedHashSet<String>();
		ResultSet rs = metaData.getTables(catalog, schema, null, new String[]{ tableType.name() });
		try {
			while (rs.next())
				tableNames.add(rs.getString("TABLE_NAME"));
		} finally {
			rs.close();
		}
		return tableNames;
	}

	/**
	 * Retrieve table metadata.
	 * Implementation takes care of converting tablename to upper or lower case according to meta information.
	 *
	 * @param tableName the tablename (mixed case)
	 * @return table metadata
	 * @throws SQLException
	 */
	public DbTable getTableMetaData(String tableName) throws SQLException {
		if (metaData.storesLowerCaseIdentifiers()) {
			tableName = tableName.toLowerCase();
		}
		else if (metaData.storesUpperCaseIdentifiers()) {
			tableName = tableName.toUpperCase();
		}
		ResultSet rs = metaData.getTables(catalog, schema, tableName, new String[] {"TABLE", "VIEW"});
		DbTableType tableType;
		String remarks;
		try {
			if (!rs.next())
				return null;
			tableType = DbTableType.valueOf(rs.getString("TABLE_TYPE"));
			remarks = rs.getString("REMARKS");
		} finally {
			rs.close();
		}

		Collection<DbTableArtifact> tableArtifacts = getTableArtifacts(tableName, tableType);
		DbTable table = new DbTable(tableName, tableArtifacts);
		table.setComment(remarks);
		return table;
	}

	public Collection<DbArtifact> getAllMetaData() throws SQLException {
		Collection<DbArtifact> artifacts = new ArrayList<DbArtifact>();
		for (String tableName : getTableNames(DbTableType.TABLE)) {
			DbTable table = getTableMetaData(tableName);
			if (table != null)
				artifacts.add(table);
		}
		artifacts.addAll(getCallables());
		artifacts.addAll(getSequences());
		artifacts.addAll(getCustomArtifacts());
		return artifacts;
	}

	protected Collection<DbTableArtifact> getTableArtifacts(String tableName, DbTableType tableType) throws SQLException {
		Map<String, DbTableArtifact> tableArtifacts = new LinkedHashMap<String, DbTableArtifact>();
		addAll(tableArtifacts, getColumns(tableName));
		if (tableType == DbTableType.TABLE) {
			// Note that we first add the indices because these contain also constraints. After that
			// we add the constraint objects, so that these will replace the former.
			addAll(tableArtifacts, getIndices(tableName));
			DbPrimaryKeyConstraint primaryKey = getPrimaryKey(tableName);
			if (primaryKey != null)
				tableArtifacts.put(primaryKey.getArtifactName(), primaryKey);
			addAll(tableArtifacts, getForeignKeys(tableName));
		}
		return tableArtifacts.values();
	}

	protected Collection<DbColumn> getColumns(String tableName) throws SQLException {
		List<DbColumn> columns = new ArrayList<DbColumn>();
		ResultSet rs = metaData.getColumns(catalog, schema, tableName, null);
		try{
			while (rs.next()) {
				String columnName  = rs.getString("COLUMN_NAME");
				DbNullable nullable = NULLABLE[rs.getInt("NULLABLE")];
				DbColumnType type = getColumnType(rs);
				DbColumn column = new DbColumn(tableName, columnName, type, nullable, null);
				columns.add(column);
			}
		} finally {
			rs.close();
		}
		return columns;
	}

	protected DbPrimaryKeyConstraint getPrimaryKey(String tableName) throws SQLException {
		ResultSet rs = metaData.getPrimaryKeys(catalog, schema, tableName);
		try {
			if (rs.next()) {
				String pkName = rs.getString("PK_NAME");
				Map<Integer, String> pks = new TreeMap<Integer, String>();
				do {
					pks.put(rs.getInt("KEY_SEQ"), rs.getString("COLUMN_NAME"));
				} while (rs.next());
				return new DbPrimaryKeyConstraint(tableName, pkName, new ArrayList<String>(pks.values()));
			} else {
				return null;
			}
		} finally {
			rs.close();
		}
	}

	protected Collection<DbForeignKeyConstraint> getForeignKeys(String tableName) throws SQLException {
		List<DbForeignKeyConstraint> foreignKeys = new ArrayList<DbForeignKeyConstraint>();
		ResultSet rs = metaData.getImportedKeys(catalog, schema, tableName);
		try {
			if (rs.next()) {
				String fkName = null, refTable = null, refKey = null;
				List<String> fkColumns = null, refColumns = null;
				boolean onDeleteCascade = false;
				int nextKeySeq = 0;
				do {
					boolean sameSchema =
						ObjectUtils.equals(rs.getString("PKTABLE_CAT"), rs.getString("FKTABLE_CAT")) &&
						ObjectUtils.equals(rs.getString("PKTABLE_SCHEM"), rs.getString("FKTABLE_SCHEM"));
					if (!sameSchema)
						continue;
					int keySeq = rs.getInt("KEY_SEQ");
					if (nextKeySeq != keySeq) {
						if (nextKeySeq > 0)
							foreignKeys.add(new DbForeignKeyConstraint(tableName, fkName, fkColumns, refTable, refKey, refColumns, onDeleteCascade));

						fkName = rs.getString("FK_NAME");
						refTable = rs.getString("PKTABLE_NAME");
						refKey = rs.getString("PK_NAME");
						onDeleteCascade = false;
						int deleteRule = rs.getShort("DELETE_RULE");
						if (rs.wasNull())
							deleteRule = -1;
						if (deleteRule == DatabaseMetaData.importedKeyCascade) {
							onDeleteCascade = true;
						}
						fkColumns = new ArrayList<String>();
						refColumns = new ArrayList<String>();
					}

					fkColumns.add(rs.getString("FKCOLUMN_NAME"));
					refColumns.add(rs.getString("PKCOLUMN_NAME"));
					nextKeySeq = keySeq + 1;
				} while (rs.next());
				foreignKeys.add(new DbForeignKeyConstraint(tableName, fkName, fkColumns, refTable, refKey, refColumns, onDeleteCascade));
			}
		} finally {
			rs.close();
		}
		return foreignKeys;
	}

	protected Collection<DbTableArtifact> getIndices(String tableName) throws SQLException {
		List<DbTableArtifact> indices = new ArrayList<DbTableArtifact>();
		ResultSet rs = metaData.getIndexInfo(catalog, schema, tableName, false, false);
		try {
			if (rs.next()) {
				String indexName = null;
				List<String> columns = null;
				int nextOrdinal = 0;
				boolean unique = false;
				do {
					int ordinal = rs.getInt("ORDINAL_POSITION");
					if (ordinal == 0)
						continue;
					if (nextOrdinal != ordinal) {
						if (nextOrdinal > 0)
							indices.add(unique ? new DbUniqueConstraint(tableName, indexName, columns) : new DbIndex(tableName, indexName, columns));

						indexName = rs.getString("INDEX_NAME");
						unique = ! rs.getBoolean("NON_UNIQUE");
						columns = new ArrayList<String>();
					}

					columns.add(rs.getString("COLUMN_NAME"));
					nextOrdinal = ordinal + 1;
				} while (rs.next());
				if (nextOrdinal != 0)
					indices.add(unique ? new DbUniqueConstraint(tableName, indexName, columns) : new DbIndex(tableName, indexName, columns));
			}
		} finally {
			rs.close();
		}
		return indices;
	}

	protected Set<String> getCallableNames() throws SQLException {
		return getCallables(false).keySet();
	}

	public abstract Map<String, String> getDatabaseParameters() throws SQLException;

	protected abstract Collection<DbSequence> getSequences() throws SQLException;

	protected Collection<DbCallable> getCallables() throws SQLException {
		return getCallables(true).values();
	}

	protected Map<String, DbCallable> getCallables(boolean withCode) throws SQLException {
		Map<String, DbCallable> callables = new LinkedHashMap<String, DbCallable>();
		ResultSet rs;
		rs = metaData.getProcedures(catalog, schema, null);
		try {
			while(rs.next()) {
				String name = normalizeCallableName(rs.getString("PROCEDURE_NAME"));
				String comment = rs.getString("REMARKS");
				DbCallable callable = new DbCallable(DbCallableType.PROCEDURE, name, null);
				callable.setComment(comment);
				callables.put(name, callable);
			}
		} finally {
			rs.close();
		}
		if (supportsJDBC4getFunction) {
			try {
				rs = metaData.getFunctions(catalog, schema, null);
				try {
					while(rs.next()) {
						String name = normalizeCallableName(rs.getString("FUNCTION_NAME"));
						String comment = rs.getString("REMARKS");
						DbCallable callable = new DbCallable(DbCallableType.FUNCTION, name, null);
						callable.setComment(comment);
						callables.put(name, callable);
					}
				} finally {
					rs.close();
				}
			} catch (AbstractMethodError ex) {
				// getFunctions() is new JDBC 4 api and not supported by all drivers
				log.warn("JDBC driver does not support getFunctions() properly; use solely getProcedures()");
				supportsJDBC4getFunction = false;
			}
		}
		if (withCode) {
			for (DbCallable callable : callables.values()) {
				callable.setCode(getCode(callable));
			}
		}
		return callables;
	}

	protected String normalizeCallableName(String name) {
		return name;
	}

	protected String getCode(DbArtifact artifact) throws SQLException {
		return null;
	}

	protected Collection<DbArtifact> getCustomArtifacts() throws SQLException {
		return Collections.emptySet();
	}

	protected DbColumnType getColumnType(ResultSet rs) throws SQLException {
		int sqlType = rs.getInt("DATA_TYPE");
		String typeName = rs.getString("TYPE_NAME");
		Integer length = 0;
		Integer precision = rs.getInt("COLUMN_SIZE");
		Integer scale = rs.getInt("DECIMAL_DIGITS");
		if (rs.wasNull()) {
			length = precision;
			scale = precision = null;
		}
		DbGenericType genericType = getDbGenericType(sqlType, typeName);
		return new DbColumnType(genericType, typeName, length, precision, scale);
	}

	protected abstract DbGenericType getDbGenericType(int sqlType, String typeName);

	protected <T extends DbArtifact> void addAll(Map<String, T> nameMap, Collection<? extends T> artifacts) {
		nameMap.putAll(DbArtifact.makeNameMap(artifacts));
	}

	private static DbNullable[] NULLABLE = { DbNullable.NOT_NULL, DbNullable.NULL, null };
}
