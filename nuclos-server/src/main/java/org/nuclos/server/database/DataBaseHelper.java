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
package org.nuclos.server.database;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.VersionNumber;
import org.nuclos.common.collection.Pair;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.autosync.AutoDbSetup;
import org.nuclos.server.common.NuclosSystemParameters;
import org.nuclos.server.common.ServerProperties;
import org.nuclos.server.dblayer.DbAccess;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.DbType;
import org.nuclos.server.dblayer.statements.DbBuildableStatement;
import org.nuclos.server.dblayer.statements.DbStatement;
import org.nuclos.server.dblayer.structure.DbTableType;
import org.nuclos.server.dblayer.util.StatementToStringVisitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * Class containing general static helper functions.<br>
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:martin.weber@novabit.de">martin.weber</a>
 * @version 01.00.00
 */
@Component
@DependsOn("nuclosRemoteRollback")
public class DataBaseHelper {

	private static final Logger LOG = Logger.getLogger(DataBaseHelper.class);

	public static final String DEFAULT_SEQUENCE = "IDFACTORY";
	
	private static DataBaseHelper INSTANCE;
	
	//
	
	private DataSource dataSource;

	// 
	
	private DbAccess defaultDbAccess;

	DataBaseHelper() {
		INSTANCE = this;
	}
	
	public static DataBaseHelper getInstance() {
		if (INSTANCE.defaultDbAccess == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}
	
	@Autowired
	void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@PostConstruct
	final void init() {
		try {
			Map<String, String> config = new HashMap<String, String>();
			for (Map.Entry<?, ?> p : ServerProperties.loadProperties(ServerProperties.JNDI_SERVER_PROPERTIES).entrySet()) {
				if (p.getKey().toString().startsWith("database."))
					config.put(p.getKey().toString().substring(9), p.getValue().toString());
			}

			if (!config.containsKey(DbAccess.SCHEMA)) {
				throw new NuclosFatalException("Missing database.schema specification");
			}

			File structureChangelogDir = NuclosSystemParameters.getDirectory(NuclosSystemParameters.DATABASE_STRUCTURE_CHANGE_LOG_PATH);
			if (structureChangelogDir != null) {
				String path = structureChangelogDir.getAbsolutePath();
				LOG.info("Set structure change log directory to " + path);
				config.put(DbAccess.STRUCTURE_CHANGELOG_DIR, path);
			}

			// DataSource dataSource = NuclosDataSources.getDefaultDS();
			defaultDbAccess = getDbAccessFor(dataSource, config);

			AutoDbSetup autoSetup = new AutoDbSetup(defaultDbAccess);
			Pair<String, Date> version = null;
			try {
				version = autoSetup.determineLastVersion();
			} catch (Exception e) {
			}

			boolean doAutoSetup = "true".equals(config.get("autosetup"));

			LOG.info("Nuclos auto-setup is " + (doAutoSetup ? "enabled" : "disabled"));
			if (doAutoSetup) {
				boolean installed = autoSetup.checkIsInstalled();
				if (version != null) {
					LOG.info(String.format("Nuclos version found in schema %s: %s (%s)", defaultDbAccess.getSchemaName(), version.x, version.y));
					if (!installed) {
						LOG.warn("Cannot detect Nuclos schema via table existence");
					}
					installed = true;

					// Check that the installed version not newer than this Nuclos server version
					String thisVersion = ApplicationProperties.getInstance().getNuclosVersion().getSchemaVersion();
					if (VersionNumber.compare(version.x, thisVersion) > 0) {
						throw new NuclosFatalException(String.format("Found schema is newer than this Nuclos server: %s > %s", version.x, thisVersion));
					}
				}

				if (!installed) {
					LOG.info("No Nuclos installation found in schema " + defaultDbAccess.getSchemaName());
					List<DbStatement> statements = autoSetup.getSetupStatements();
					executeSetupStatements("Auto-Setup", statements);
				} else {
					LOG.info("Existing Nuclos installation found in schema " + defaultDbAccess.getSchemaName());
					if (version != null) {
						List<DbStatement> statements = autoSetup.getUpdateStatementsSince(version.x);
						if (statements != null) {
							executeSetupStatements("Auto-Update", statements);
						}
					}
				}
			}
		} catch (Exception ex) {
			LOG.fatal("Error initializing Nuclos datasource/database access", ex);
			throw new NuclosFatalException("Error initializing Nuclos datasource/database access: " + ex, ex);
		}
	}

	private void executeSetupStatements(String type, List<DbStatement> stmts) throws Exception {
		LOG.info("Starting " + type);
		LOG.info(type + ": " + stmts.size() + " statement(s) to execute...");

		int stmtNo = 1;
		String stmtDescription = null;
		StatementToStringVisitor toStringVisitor = new StatementToStringVisitor();
		try {
			for (DbStatement stmt : stmts) {
				stmtDescription = type + " #" + stmtNo;
				// this is intentionally a separate assignment; if something went
				// wrong inside the visitor, the number is already set
				stmtDescription = stmtDescription + ": " + stmt.accept(toStringVisitor);
				LOG.info(stmtDescription + "...");
				defaultDbAccess.execute(stmt);
				stmtNo++;
			}
			LOG.info(type + " finished successfully");
		} catch (Exception e) {
			String message = "Error during " + stmtDescription + ": " + e.toString();
			LOG.fatal("Error during " + stmtDescription + ": " + e.toString(), e);
			throw new NuclosFatalException(message, e);
		}
	}

	public DbAccess getDbAccess() {
		return defaultDbAccess;
	}

	public static DbAccess getDbAccessFor(DataSource dataSource, Map<String, String> config) {
		DbType dbType;
		String typeId = config.get("adapter");
		if (config.containsKey("type"))
			typeId = config.get("type");
		if (typeId != null && !typeId.isEmpty()) {
			dbType = DbType.getFromName(typeId);
			if (dbType == null) {
				throw new NuclosFatalException("Unsupported database type " + typeId);
			}
		} else {
			try {
				dbType = DbType.getFromMetaData(dataSource);
				LOG.info("Auto-determine database type: " + dbType);
			} catch (SQLException ex) {
				throw new NuclosFatalException("Error while determining database vendor and version", ex);
			}
			if (dbType == null) {
				throw new NuclosFatalException("Cannot determine database vendor and version");
			}
		}
		return dbType.createDbAccess(dataSource, config);
	}

	public int execute(DbBuildableStatement command) throws DbException {
		return getDbAccess().execute(command);
	}

	/**
	 * @return a connection from the given datasource. Must be closed by the caller in a finally block.
	 * @precondition datasource != null
	 */
	public Connection getConnection(DataSource datasource) {
		if (datasource == null) {
			throw new NullArgumentException("datasource");
		}
		try {
			return datasource.getConnection();
		}
		catch (SQLException ex) {
			throw new CommonFatalException("Connection to datasource could not be initialized.", ex);
		}
	}

	public Integer getNextIdAsInteger(String sSequenceName) {
		try {
			return getDbAccess().getDbExecutor().getNextId(sSequenceName).intValue();
		}
		catch (SQLException e) {
			// Wrap in non-check exception for convenience.
			throw new IllegalStateException(e);
		}
	}

	public boolean isTableAvailable(String sTable) {
		if (getDbAccess().getTableNames(DbTableType.TABLE).contains(sTable))
			return true;
		if (getDbAccess().getTableNames(DbTableType.TABLE).contains(sTable.toLowerCase()))
			return true;

		return false;
	}

	public boolean isViewAvailable(String sTable) {
		if (getDbAccess().getTableNames(DbTableType.VIEW).contains(sTable))
			return true;
		if (getDbAccess().getTableNames(DbTableType.VIEW).contains(sTable.toLowerCase()))
			return true;

		return false;
	}

	public boolean isObjectAvailable(String sObjectName) {
		return isTableAvailable(sObjectName) ||isViewAvailable(sObjectName);
	}
	
} // class DataBaseHelper
