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
import org.nuclos.server.dblayer.IPart;
import org.nuclos.server.dblayer.IPreparedStringExecutor;
import org.nuclos.server.dblayer.impl.util.PreparedString;

/**
 * @author Thomas Pasch
 * @since Nuclos 3.2.0
 */
public class PartImpl implements IPart {
	
	private static final Logger LOG = Logger.getLogger(PartImpl.class);

	private final NextPartHandling nextPartHandling;
	
	private final EBatchType batchType;
	
	private List<PreparedString> statements;
	
	public PartImpl(List<PreparedString> statements, EBatchType batchType, NextPartHandling nextPartHandling) {
		this.statements = statements;
		this.batchType = batchType;
		this.nextPartHandling = nextPartHandling;
	}

	public PartImpl(PreparedString statement, EBatchType batchType, NextPartHandling nextPartHandling) {
		this(CollectionUtils.newOneElementArrayList(statement), batchType, nextPartHandling);
	}
	
	@Override
	public boolean process(DalCallResult result, IPreparedStringExecutor ex) {
		boolean succeeded = true;
		for (PreparedString ps : statements) {
			succeeded = false;
			try {
				final int changes = ex.executePreparedStatement(ps);
				succeeded = true;
				result.addToNumberOfDbChanges(changes);
			} catch (SQLException e) {
				succeeded = false;
				if (!batchType.equals(EBatchType.FAIL_NEVER_IGNORE_EXCEPTION)) {
					result.addBusinessException(null, Collections.singletonList(ps.toString()), e);
				} else {
					LOG.info("Ignored exception: " + e + " while executing " + ps);
				}
				switch (batchType) {
				case FAIL_EARLY:
					result.throwFirstException();
					break;
				case FAIL_LATE:
				case FAIL_NEVER:
				case FAIL_NEVER_IGNORE_EXCEPTION:
					break;
				default:
					throw new IllegalArgumentException(batchType.toString());
				}
			}
		}
		return succeeded;
	}
	
	@Override
	public EBatchType getBatchType() {
		return batchType;
	}

	@Override
	public NextPartHandling getNextPartHandling() {
		return nextPartHandling;
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[type=").append(batchType);
		result.append(", next=").append(nextPartHandling).append(":\n");
		for (PreparedString ps: statements) {
			result.append(ps).append("\n");
		}
		result.append("] // end of part ");
		return result.toString();
	}

}
