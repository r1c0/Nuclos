//Copyright (C) 2011  Novabit Informationssysteme GmbH
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.nuclos.server.dblayer.impl.DataSourceExecutor;

/**
 * @author Thomas Pasch
 * @since Nuclos 3.2.0
 */
public class TransactSqlExecutor extends DataSourceExecutor {
	
	public TransactSqlExecutor(DataSource dataSource, String username, String password) {
		super(dataSource, username, password);
	}

	@Override
	public Long getNextId(String sequenceName) throws SQLException {
		return executeQuery("EXECUTE " + sequenceName, new ResultSetRunner<Long>() {
			@Override
			public Long perform(ResultSet rs) throws SQLException {
				return rs.next() ? rs.getLong(1) : null;
			}
		});
	}

	@Override
	public void setStatementParameter(PreparedStatement stmt, int index, Object value, Class<?> javaType) throws SQLException {
		if (javaType == Boolean.class) {
			javaType = Integer.class;
			if (value != null)
				value = ((Boolean) value).booleanValue() ? 1 : 0;
		}
		super.setStatementParameter(stmt, index, value, javaType);
	}

}
