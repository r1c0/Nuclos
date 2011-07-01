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
package org.nuclos.server.dbtransfer;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * These are meant to be a replacement for direct use of JDBC.
 * Closing resultsets/statements/connections and SQL related exceptions are handled here.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
@Deprecated
class NuclosSQLUtils {
	private static final Logger log = Logger.getLogger(NuclosSQLUtils.class);

	private NuclosSQLUtils() {
	}

	/**
	 * @return a connection from the given datasource. Must be closed by the caller in a finally block.
	 * @precondition datasource != null
	 */
	public static Connection getConnection(DataSource datasource) {
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

	@Deprecated
	public static int executeSQLLegacyUpdate(Connection conn, String sql){
		try {
			final Statement stmt = conn.createStatement();
			log.debug("BEGIN executing SQL: " + sql);
			int result = stmt.executeUpdate(sql);
			log.debug("END executing SQL");
			
			if (stmt != null) {
				stmt.close();
			}
			
			return result;
		}
		catch (SQLException ex) {
			throw new CommonFatalException("Invalid SQL statement: " + sql, ex);
		}
	}
}	// class NuclosSQLUtils
