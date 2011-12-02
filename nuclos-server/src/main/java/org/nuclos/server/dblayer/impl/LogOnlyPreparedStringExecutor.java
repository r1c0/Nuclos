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
package org.nuclos.server.dblayer.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.nuclos.server.dblayer.IPreparedStringExecutor;
import org.nuclos.server.dblayer.impl.util.PreparedString;

/**
 * @author Thomas Pasch
 * @since Nuclos 3.2.0
 */
public class LogOnlyPreparedStringExecutor implements IPreparedStringExecutor {
	
	private final List<String> statements = new ArrayList<String>();
	
	public LogOnlyPreparedStringExecutor() {
	}
	
	public List<String> getStatements() {
		return statements;
	}

	/**
	 * @deprecated
	 */
	@Override
	public Integer executePreparedStatements(List<PreparedString> pss) throws SQLException {
		for (PreparedString ps: pss) {
			executePreparedStatement(ps);
		}
		return 0;
	}

	@Override
	public int executePreparedStatement(PreparedString ps) throws SQLException {
		statements.add(ps.toString());
		return 0;
	}

}
