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

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.server.dblayer.EBatchType;
import org.nuclos.server.dblayer.IPart;
import org.nuclos.server.dblayer.impl.util.PreparedString;

/**
 * @author Thomas Pasch
 * @since Nuclos 3.2.0
 */
public class PartImpl implements IPart {
	
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
	public List<PreparedString> getStatements() {
		return statements;
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
		result.append("]");
		return result.toString();
	}

}
