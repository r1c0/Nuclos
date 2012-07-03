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
	SYBASE("sybase", "SQL Anywhere"),
	DB2("db2", "IBM DB2");

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
		case DB2:
			return "com.ibm.db2.jcc.DB2Driver";
		}
		throw new IllegalStateException();
	}

	public Integer getDefaultPort() {
		switch (this) {
		case ORACLE:     return 1521;
		case MSSQL:      return 1433;
		case POSTGRESQL: return 5432;
		case SYBASE:     return 2638;
		case DB2:    	 return 50000;
		}
		throw new IllegalStateException();
	}

	public String buildJdbcConnectionString(Properties props) {
		String server = props.getProperty("database.server");
		String port = props.getProperty("database.port", "" + getDefaultPort());
		String database = props.getProperty("database.name");
		String schema = props.getProperty("database.schema");
		if (server == null || database == null)
			return null;
		return buildJdbcConnectionString(server, port, database, schema);
	}

	public String buildJdbcConnectionString(String server, String port, String database, String schema) {
		Object[] srvPrtDb = { server, port, database, schema };
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
		case DB2:
			// jdbc:db2://<server>:<port>/<database>:currentSchema=<schema>;
			return String.format("jdbc:db2://%s:%s/%s:currentSchema=%s;", srvPrtDb);
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
		case DB2:
			pattern = Pattern.compile("jdbc:db2://([^:]+):(\\d+)/(.+):currentSchema=(.+);");
			break;
		}
		Matcher matcher = pattern.matcher(jdbcUrl);
		if (!matcher.matches()) {
			return false;
		}
		props.put("database.server", matcher.group(1));
		props.put("database.port", group(matcher, 2, getDefaultPort().toString()));
		props.put("database.name", group(matcher, 3, ""));
		//props.put("database.schema", group(matcher, 4, ""));
		return true;
	}
	
	private static String group(Matcher m, int i, String default0) {
		String result = null;
		try {
			result = m.group(i);
			if (result == null) {
				result = default0;
			}
		}
		catch (IndexOutOfBoundsException e) {
			result = default0;
		}
		return result;
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
