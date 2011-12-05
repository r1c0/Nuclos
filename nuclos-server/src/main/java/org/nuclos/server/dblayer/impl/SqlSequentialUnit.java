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
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.server.dblayer.EBatchType;
import org.nuclos.server.dblayer.IPreparedStringExecutor;
import org.nuclos.server.dblayer.IUnit;
import org.nuclos.server.dblayer.impl.util.PreparedString;

/**
 * @author Thomas Pasch
 * @since Nuclos 3.2.0
 */
public class SqlSequentialUnit implements IUnit {
	
    private static final Logger LOG = Logger.getLogger(SqlSequentialUnit.class);

	private final List<PreparedString> sequence;
	
	public SqlSequentialUnit(List<PreparedString> sequence) {
		if (sequence == null) throw new NullPointerException();
		this.sequence = sequence;
	}
	
	public SqlSequentialUnit(PreparedString sequence) {
		this(CollectionUtils.newOneElementArrayList(sequence));
	}	

	@Override
	public DalCallResult process(IPreparedStringExecutor ex, EBatchType type) {
		final boolean debug = LOG.isDebugEnabled();
		final DalCallResult result = new DalCallResult();
		if (debug) LOG.debug("begin process of " + this);
		for (PreparedString ps: sequence) {
			try {
				final int changes = ex.executePreparedStatement(ps);
				result.addToNumberOfDbChanges(changes);
			} catch (SQLException e) {
				if (!type.equals(EBatchType.FAIL_NEVER_IGNORE_EXCEPTION)) {
					result.addBusinessException(null, Collections.singletonList(ps.toString()), e);
				}
				else {
					if (debug) LOG.info("Ignored exception: " + e + " while executing " + ps);
				}
				switch (type)  {
					case FAIL_EARLY:
						result.throwFirstException();
						break;
					case FAIL_LATE:
					case FAIL_NEVER:
					case FAIL_NEVER_IGNORE_EXCEPTION:
						break;
					default:
						throw new IllegalArgumentException(type.toString());
				}
			}
		}
		if (debug) LOG.debug("end batch process with result: " + result);
		if (type.equals(EBatchType.FAIL_LATE)) {
			result.throwFirstException();
		}
		return result;
	}

	@Override
	public void toString(StringBuilder result, String lineIndentPrefix) {
		result.append(getClass().getName()).append(":[\n");
		for (PreparedString ps: sequence) {
			result.append(lineIndentPrefix).append(ps).append("\n");
		}
		result.append("] // end of unit");
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		toString(result, "");
		return result.toString();
	}

}
