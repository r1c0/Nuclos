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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.nuclos.server.dblayer.impl.mssql.MSSQLDBAccess;
import org.nuclos.server.dblayer.impl.oracle.OracleDBAccess;
import org.nuclos.server.dblayer.impl.postgresql.PostgreSQLDBAccess;
import org.nuclos.server.dblayer.impl.sybase.SybaseDbAccess;
import org.nuclos.server.dblayer.impl.db2.DB2DBAccess;


public enum DbType {
	ORACLE("Oracle"),
	MSSQL("Microsoft SQL Server"),
	POSTGRESQL("PostgreSQL"),
	SYBASE("SQL Anywhere"),
	DB2("IBM DB2");
	
	private String productName;
	
	private DbType(String productName) {
		this.productName = productName;
	}
	
	public DbAccess createDbAccess(DataSource dataSource, Map<String, String> config) {
		DbAccess dbAccess;
		switch (this) {
		case ORACLE:
			dbAccess = new OracleDBAccess();
			break;
		case POSTGRESQL:
			dbAccess = new PostgreSQLDBAccess();
			break;
		case MSSQL:
			dbAccess = new MSSQLDBAccess();
			break;
		case SYBASE:
			dbAccess = new SybaseDbAccess();
			break;
		case DB2:
			dbAccess = new DB2DBAccess();
			break;
		default:
			throw new UnsupportedOperationException("Unsupported database " + this);
		}
		dbAccess.init(this, dataSource, config);
		return dbAccess;
	}
	
	public static DbType getFromMetaData(Connection conn) throws SQLException {
		DatabaseMetaData metaData = conn.getMetaData();
		String productName = metaData.getDatabaseProductName();
		// String productVersion = metaData.getDatabaseProductVersion();
		for (DbType db : DbType.values()) {
			if (db.productName.equals(productName))
				return db;
		}
		return null;
	}
	
	public static DbType getFromMetaData(DataSource dataSource) throws SQLException {
		Connection conn = dataSource.getConnection();
		try {
			return getFromMetaData(conn);
		} finally {
			conn.close();
		}
	}
	
	public static DbType getFromName(String name) {
		for (DbType type : DbType.values()) {
			if (name.equalsIgnoreCase(type.name())) {
				return type;
			}
		}
		return null;
	}
}
