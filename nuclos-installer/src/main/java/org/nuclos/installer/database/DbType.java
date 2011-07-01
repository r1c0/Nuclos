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

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum DbType {

	ORACLE("oracle", "Oracle"),
	MSSQL("mssql", "Microsoft SQL Server"),
	POSTGRESQL("postgresql", "PostgreSQL"),
	SYBASE("sybase", "SQL Anywhere");

	private final String adapter;

	private DbType(String adapter, String displayName) {
		this.adapter = adapter;
	}

	public String getAdapterName() {
		return adapter;
	}

	public String getDriverClassName() {
		switch (this) {
		case ORACLE:
			// TODO: better "oracle.jdbc.driver"
			return "oracle.jdbc.driver.OracleDriver";
		case MSSQL:
			return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		case POSTGRESQL:
			return "org.postgresql.Driver";
		case SYBASE:
			return "com.sybase.jdbc3.jdbc.SybDriver";
		}
		throw new IllegalStateException();
	}

	public Integer getDefaultPort() {
		switch (this) {
		case ORACLE:     return 1521;
		case MSSQL:      return 1433;
		case POSTGRESQL: return 5432;
		case SYBASE:     return 2638;
		}
		throw new IllegalStateException();
	}

	public String buildJdbcConnectionString(Properties props) {
		String server = props.getProperty("database.server");
		String port = props.getProperty("database.port", "" + getDefaultPort());
		String database = props.getProperty("database.name");
		if (server == null || database == null)
			return null;
		return buildJdbcConnectionString(server, port, database);
	}

	public String buildJdbcConnectionString(String server, String port, String database) {
		Object[] srvPrtDb = { server, port, database };
		switch (this) {
		case ORACLE:
			// jdbc:oracle:thin:@<server>:<port>:<instance-name>
			return String.format("jdbc:oracle:thin:@%s:%s:%s", srvPrtDb);
		case MSSQL:
			// jdbc:sqlserver://localhost:1433;DatabaseName=<database>
			return String.format("jdbc:sqlserver://%s:%s;DatabaseName=%s", srvPrtDb);
		case POSTGRESQL:
			// jdbc:postgresql://localhost:5432/<database>
			return String.format("jdbc:postgresql://%s:%s/%s", srvPrtDb);
		case SYBASE:
			// jdbc:sybase:Tds:<server>:<port>/<instance-name>
			return String.format("jdbc:sybase:Tds:%s:%s/%s", srvPrtDb);
		}
		throw new IllegalStateException();
	}

	public boolean parseJdbcConnectionString(String jdbcUrl, Properties props) {
		if (jdbcUrl == null)
			return false;
		Pattern pattern = null;
		switch (this) {
		case ORACLE:
			pattern = Pattern.compile("jdbc:oracle:thin:@([^:]+):(\\d+)[/|:](.+)");
			break;
		case MSSQL:
			pattern = Pattern.compile("jdbc:sqlserver://([^:]+):(\\d+);DatabaseName=(.+)");
			break;
		case POSTGRESQL:
			pattern = Pattern.compile("jdbc:postgresql://([^:]+):(\\d+)/(.*)");
			break;
		case SYBASE:
			pattern = Pattern.compile("jdbc:sybase:Tds:([^:]+):(\\d+)/(.*)");
			break;
		}
		Matcher matcher = pattern.matcher(jdbcUrl);
		if (!matcher.matches()) {
			return false;
		}
		props.put("database.server", matcher.group(1));
		props.put("database.port", matcher.group(2) != null ? matcher.group(2) : getDefaultPort());
		props.put("database.name", matcher.group(3) != null ? matcher.group(3) : "");
		return true;
	}

	public static DbType findType(String adapterName) {
		if (adapterName == null)
			return null;
		for (DbType type : DbType.values()) {
			if (adapterName.equals(type.getAdapterName())) {
				return type;
			}
		}
		return null;
	}
}
