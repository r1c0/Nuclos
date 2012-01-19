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

package org.nuclos.tools.dbsetup;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

public class SimpleDataSource implements DataSource {

	private int	timeout;
	private PrintWriter out;
	private String	url;
	private Properties	info;

	public SimpleDataSource(String url) {
		this.url = url;
		this.info = new Properties();
		
	}
	public SimpleDataSource(String url, String user, String password) {
		this(url);
		info.put("user", user);
		info.put("password", password);
	}
	
	public SimpleDataSource(String url, Map<String, String> map) {
		this(url);
		this.info.putAll(map);
	}
	
	@Override
	public Connection getConnection() throws SQLException {
		return prepareConnection(DriverManager.getConnection(url, info));
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		Properties localInfo = new Properties(info);
		localInfo.put("user", username);
		localInfo.put("password", password);
		return prepareConnection(DriverManager.getConnection(url, localInfo));
	}

	protected Connection prepareConnection(Connection connection) throws SQLException {
		String newConnectionSql = info.getProperty("new.connection.sql");
		if (newConnectionSql != null) {
			Statement stmt = connection.createStatement();
			try {
				stmt.execute(newConnectionSql);
			} finally {
				stmt.close();
			}
		}
		return connection;
	}
	
	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return out;
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return timeout;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		this.out = out;
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		this.timeout = seconds;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new SQLException();
	}
}
