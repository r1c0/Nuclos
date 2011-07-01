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

package org.nuclos.installer.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.nuclos.installer.Constants;
import org.nuclos.installer.L10n;

public class PostgresDbSetup extends DbSetup implements Constants {

	private String server;
	private String port;
	private String databaseName;

	private String userName;
	private String userPassword;
	private String tablespaceLocation;
	private String tablespaceName;

	private String superName;
	private String superPassword;

	private String schemaName;

	private List<SetupActionImpl> actions;

	public PostgresDbSetup() {
	}

	public void init(Properties props, String superuser, String superpwd) {
		actions = new ArrayList<SetupActionImpl>();

		this.superName = superuser;
		this.superPassword = superpwd;

		server = props.getProperty("database.server");
		port = props.getProperty("database.port");
		databaseName = props.getProperty("database.name");

		userName = props.getProperty("database.username");
		userPassword = props.getProperty("database.password");
		tablespaceLocation = props.getProperty(POSTGRES_TABLESPACEPATH);
		if (tablespaceLocation != null && !tablespaceLocation.isEmpty()) {
			tablespaceLocation = tablespaceLocation.replace('\\', '/');
		}
		tablespaceName = props.getProperty("database.tablespace");
		schemaName = props.getProperty("database.schema");

		if (isUnset(tablespaceName)) {
			tablespaceName = "pg_default";
		}
		if (isUnset(schemaName)) {
			schemaName = userName;
		}
		if (isUnset(databaseName)) {
			databaseName = "postgres";
		}
	}

	@Override
	public void prepare() {
		actions = new ArrayList<SetupActionImpl>();
		if (superPassword.isEmpty() || userPassword.isEmpty()) {
			actions.add(new SetupActionImpl(Kind.ERROR, "Empty password"));
		}

		try {
			Connection superConn = openConnection("postgres", superName, superPassword);
			try {
				boolean databaseExists = executeCheck(superConn, "SELECT 1 FROM pg_database WHERE datname = " + nameLit(databaseName));
				boolean setupSchema;
				if (databaseExists) {
					// Database already exists.
					actions.add(new SetupActionImpl(Kind.WARN, "dbsetup.warn.dbalreadyexists", databaseName));

					if (databaseName.equalsIgnoreCase("postgres")) {
						actions.add(new SetupActionImpl(Kind.WARN, "dbsetup.warn.managementdb"));
					}
					// Check if schema already exists
					try {
						Connection conn = openConnection(databaseName, userName, userPassword);
						try {
							boolean schemaExists = executeCheck(conn, "SELECT * FROM pg_namespace WHERE nspname = " + nameLit(schemaName));
							// boolean plpgsql = executeCheck(conn, "SELECT * FROM pg_language WHERE lanname = " + nameLit("plpgsql"));
							if (schemaExists) {
								actions.add(new SetupActionImpl(Kind.WARN, "dbsetup.warn.schemaexists", schemaName));
							}
							setupSchema = !schemaExists;
						} finally {
							conn.close();
						}
					} catch (SQLException ex) {
						actions.add(createErrorFromSQLException(ex));
						setupSchema = false;
					}
				} else {
					// Database doesn't exist

					// Query pg_roles or pg_user? Since the corresponding step is CREATE ROLE, pg_roles seems more appropriate
					String userOid = executeQuery1(superConn, "SELECT oid FROM pg_roles WHERE rolname = " + nameLit(userName));
					if (userOid != null) {
						// TODO: Test login and check it is a login role and that the password matches
						actions.add(new SetupActionImpl(Kind.WARN, "dbsetup.warn.roleexists", userName));
					} else {
						actions.add(new SetupActionImpl(Kind.SQL, "Create role", new RoleSqlStatements()));
					}

					boolean tablespaceExists =  executeCheck(superConn, "SELECT 1 FROM pg_tablespace WHERE spcname = " + nameLit(tablespaceName));
					String[] s = executeQuery(superConn, "SELECT spclocation FROM pg_tablespace WHERE spcname = " + nameLit(tablespaceName));
					tablespaceExists = (s != null);
					if (tablespaceExists) {
						actions.add(new SetupActionImpl(Kind.WARN, "dbsetup.warn.tablespaceexists", tablespaceName));
					} else {
						actions.add(new SetupActionImpl(Kind.SQL, "Create tablespace", new TablespaceSqlStatements()));
					}
					setupSchema = true;
					actions.add(new SetupActionImpl(Kind.SQL, "Datenbank", new DatabaseSqlStatements()));
					actions.add(new SetupActionImpl(Kind.SQL, "PL/PGSQL-Support", new PlPgSupportSqlStatements()));
				}
				// Note: Schema statements will be performed by the real user (not the superuser)
				if (setupSchema) {
					actions.add(new SetupActionImpl(Kind.SQL, "Schema", new SchemaSqlStatements()));
				}
			} finally {
				superConn.close();
			}
		} catch (SQLException ex) {
			actions.add(createErrorFromSQLException(ex));
			ex.printStackTrace();
		}
		System.err.println(actions);
	}

	public void checkConnection() throws SQLException {
		Connection superConn = null;
		try {
			superConn = openConnection("postgres", superName, superPassword);
		}
		finally {
			if (superConn != null && !superConn.isClosed()) {
				superConn.close();
			}
		}
	}

	@Override
	public void run(Callback callback) throws SQLException {
		Connection superConn = openConnection("postgres", superName, superPassword);
		boolean success = false;
		int index = 0;
		try {
			for (; index < actions.size(); index++) {
				SetupActionImpl action = actions.get(index);
				SqlStatements sqlStmts = action.action;
				if (sqlStmts != null)
					sqlStmts.setup(superConn);
			}
			success = true;
		} catch (SQLException ex) {
			callback.message(createErrorFromSQLException(ex));
		} catch (RuntimeException ex) {
			callback.message(new SetupActionImpl(Kind.ERROR, ex.getLocalizedMessage()));
		} finally {
			if (!success) {
				for (; index >= 0; index--) {
					SqlStatements action = actions.get(index).action;
					if (action != null)
						action.rollback(superConn);
				}
			}
			superConn.close();
		}
	}


	@Override
	public List<? extends SetupAction> getActions() {
		return actions;
	}

	private SetupActionImpl createErrorFromSQLException(SQLException ex) {
		// TODO: String state = ex.getSQLState();
		// For state values, see: http://www.postgresql.org/docs/9.0/interactive/errcodes-appendix.html
		// 08004 -> Verbindung verweigert (Port etc.)
		// 28P01 -> Authentifizierung (name/password)
		return new SetupActionImpl(Kind.ERROR, ex.getLocalizedMessage());
	}

	private class SetupActionImpl implements SetupAction {

		private final Kind kind;
		private final String text;
		private final SqlStatements action;
		private final Object[] args;

		public SetupActionImpl(Kind kind, String text, Object...args) {
			this(kind, text, null, args);
		}

		public SetupActionImpl(Kind kind, String text, SqlStatements action, Object...args) {
			this.kind = kind;
			this.text = text;
			this.action = action;
			this.args = args;
		}

		@Override
		public Kind getKind() {
			return kind;
		}

		@Override
		public String getMessage() {
			return L10n.getMessage(text, args);
		}

		@Override
		public String toString() {
			return kind + ": " + text;
		}
	}

	private interface SqlStatements {

		public void setup(Connection superConn) throws SQLException;

		/** Note: Rollback must not called if this statement failed */
		public void rollback(Connection superConn) throws SQLException;
	}

	private class RoleSqlStatements implements SqlStatements {

		@Override
		public void setup(Connection superConn) throws SQLException {
			executeStmt(superConn,
				"CREATE ROLE %s LOGIN UNENCRYPTED PASSWORD '%s' NOSUPERUSER NOCREATEDB NOCREATEROLE",
				userName, escape(userPassword));
		}

		@Override
		public void rollback(Connection superConn) throws SQLException {
			executeStmt(superConn, "DROP ROLE %s", userName);
		}
	}

	private class TablespaceSqlStatements implements SqlStatements {

		@Override
		public void setup(Connection superConn) throws SQLException {
			executeStmt(superConn,
				"CREATE TABLESPACE %s LOCATION '%s'", tablespaceName, escape(tablespaceLocation));
		}

		@Override
		public void rollback(Connection superConn) throws SQLException {
			executeStmt(superConn, "DROP TABLESPACE %s", tablespaceName);
		}
	}

	private class DatabaseSqlStatements implements SqlStatements {

		@Override
		public void setup(Connection superConn) throws SQLException {
			executeStmt(superConn,
				"CREATE DATABASE %s WITH OWNER = %s ENCODING = 'UTF-8' TABLESPACE = %s",
				databaseName, userName, tablespaceName);
		}

		@Override
		public void rollback(Connection superConn) throws SQLException {
			executeStmt(superConn, "DROP DATABASE %s", databaseName);
		}
	}


	private class PlPgSupportSqlStatements implements SqlStatements {

		@Override
		public void setup(Connection ignoreSuperConn) throws SQLException {
			Connection conn = openConnection(databaseName, superName, superPassword);
			try {
				if (!executeCheck(conn, "SELECT 1 FROM pg_language where lanname = 'plpgsql'")) {
					executeStmt(conn, "CREATE LANGUAGE plpgsql");
				}
			} finally {
				conn.close();
			}
		}

		@Override
		public void rollback(Connection superConn) throws SQLException {
			// Nothing to do...
		}
	}

	private class SchemaSqlStatements implements SqlStatements {
		@Override
		public void setup(Connection ignoreSuperConn) throws SQLException {
			Connection conn = openConnection(databaseName, userName, userPassword);
			try {
				executeStmt(conn,
					"CREATE SCHEMA %s AUTHORIZATION %s", schemaName, userName);
			} finally {
				conn.close();
			}
		}

		@Override
		public void rollback(Connection ignoreSuperConn) throws SQLException {
			Connection conn = openConnection(databaseName, userName, userPassword);
			try {
				executeStmt(conn, "DROP SCHEMA %s", schemaName);
			} finally {
				conn.close();
			}
		}
	}

	private Connection openConnection(String database, String username, String password) throws SQLException {
		Properties props = new Properties();
		props.put("user", username);
		props.put("password", password);
		String jdbcUrl = DbType.POSTGRESQL.buildJdbcConnectionString(server, port, database);
		return DriverManager.getConnection(jdbcUrl, props);
	}

	private static boolean executeStmt(Connection conn, String fmt, Object...args) throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			String sql = String.format(fmt, args);
			System.err.println(sql);
			return stmt.execute(sql);
		} finally {
			stmt.close();
		}
	}

	private static String[] executeQuery(Connection conn, String fmt, Object...args) throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery(String.format(fmt, args));
			try {
				String[] result = null;
				if (rs.next()) {
					result = new String[ rs.getMetaData().getColumnCount()];
					for (int i = 0, n = result.length; i < n; i++) {
						result[i] = rs.getString(i + 1);
					}
				}
				return result;
			} finally {
				rs.close();
			}
		} finally {
			stmt.close();
		}
	}

	private static String executeQuery1(Connection conn, String fmt, Object...args) throws SQLException {
		String[] result = executeQuery(conn, fmt, args);
		return (result != null) ? result[0] : null;
	}

	private static boolean executeCheck(Connection conn, String fmt, Object...args) throws SQLException {
		return executeQuery(conn, fmt, args) != null;
	}

	private static String escape(String text) {
		return (text != null) ? text.replace("'", "''") : "";
	}

	private static String nameLit(String text) {
		return String.format("LOWER('%s')", escape(text));
	}

	private static boolean isUnset(String str) {
		return str == null || str.trim().isEmpty();
	}
}
