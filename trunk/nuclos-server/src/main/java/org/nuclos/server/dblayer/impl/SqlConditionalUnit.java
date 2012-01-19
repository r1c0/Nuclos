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

import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.server.dblayer.EBatchType;
import org.nuclos.server.dblayer.IPart;
import org.nuclos.server.dblayer.IPart.NextPartHandling;
import org.nuclos.server.dblayer.IPreparedStringExecutor;
import org.nuclos.server.dblayer.IUnit;

/**
 * @author Thomas Pasch
 * @since Nuclos 3.2.0
 */
public class SqlConditionalUnit implements IUnit {

	private static final Logger LOG = Logger.getLogger(SqlSequentialUnit.class);

	private final List<IPart> parts;

	public SqlConditionalUnit(List<IPart> parts) {
		if (parts == null)
			throw new NullPointerException();
		this.parts = parts;
	}

	public SqlConditionalUnit(IPart part) {
		this(CollectionUtils.newOneElementArrayList(part));
	}

	@Override
	public DalCallResult process(IPreparedStringExecutor ex, EBatchType type) {
		final boolean debug = LOG.isDebugEnabled();
		final DalCallResult result = new DalCallResult();
		if (debug)
			LOG.debug("begin process of " + this);
		
		NextPartHandling nextPartHandling = NextPartHandling.ALWAYS;
		boolean succeeded = true;
		for (IPart p : parts) {
			if (debug) 
				LOG.debug("last part succeeded: " + succeeded + " next part handling: " + nextPartHandling);
			switch (nextPartHandling) {
			case ONLY_IF_THIS_FAILS:
				if (succeeded) continue;
				break;
			case ONLY_IF_THIS_SUCCEEDS:
				if (!succeeded) continue;
			case ALWAYS:
				break;
			default:
				throw new IllegalStateException(nextPartHandling.toString());
			}
			
			succeeded = p.process(result, ex);
			nextPartHandling = p.getNextPartHandling();
		}
		if (debug)
			LOG.debug("end batch process with result: " + result);
		if (type.equals(EBatchType.FAIL_LATE)) {
			result.throwFirstException();
		}
		return result;
	}

	@Override
	public void toString(StringBuilder result, String lineIndentPrefix) {
		result.append(getClass().getName()).append(":[\n");
		for (IPart p : parts) {
			result.append(lineIndentPrefix).append(p).append("\n");
		}
		result.append("] // end of unit ");
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		toString(result, "");
		return result.toString();
	}

}
