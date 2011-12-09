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
import org.nuclos.server.dblayer.IPreparedStringExecutor;
import org.nuclos.server.dblayer.impl.util.PreparedString;

/**
 * @author Thomas Pasch
 * @since Nuclos 3.2.0
 */
public class SubPartImpl implements IPart {
	
	private static final Logger LOG = Logger.getLogger(SubPartImpl.class);

	private final NextPartHandling nextPartHandling;
	
	private final EBatchType batchType;
	
	private List<IPart> subParts;
	
	public SubPartImpl(List<IPart> subParts, EBatchType batchType, NextPartHandling nextPartHandling) {
		this.subParts = subParts;
		this.batchType = batchType;
		this.nextPartHandling = nextPartHandling;
	}

	public SubPartImpl(IPart subPart, EBatchType batchType, NextPartHandling nextPartHandling) {
		this(CollectionUtils.newOneElementArrayList(subPart), batchType, nextPartHandling);
	}
	
	/**
	 * Don't make this public. (tp)
	 */
	List<IPart> getSubParts() {
		return subParts;
	}

	@Override
	public boolean process(DalCallResult result, IPreparedStringExecutor ex) {
		boolean succeeded = true;
		for (IPart p: subParts) {
			succeeded = false;
			succeeded = p.process(result, ex);
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
		for (IPart p: subParts) {
			result.append(p).append("\n");
		}
		result.append("] // end of part ");
		return result.toString();
	}

}
