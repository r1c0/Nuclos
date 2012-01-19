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

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.server.dblayer.EBatchType;
import org.nuclos.server.dblayer.IBatch;
import org.nuclos.server.dblayer.IPreparedStringExecutor;
import org.nuclos.server.dblayer.IUnit;
import org.nuclos.server.dblayer.impl.util.PreparedString;

/**
 * @author Thomas Pasch
 * @since Nuclos 3.2.0
 */
public class BatchImpl implements IBatch {
	
	private static final Logger LOG = Logger.getLogger(BatchImpl.class);
	
	public static BatchImpl simpleBatch(List<PreparedString> sequence) {
		if (sequence == null || sequence.isEmpty()) {
			return null;
		}
		return new BatchImpl(CollectionUtils.<IUnit>newOneElementArrayList(new SqlSequentialUnit(sequence)));
	}
	
	public static BatchImpl simpleBatch(PreparedString sequence) {
		if (sequence == null) {
			return null;
		}
		return new BatchImpl(CollectionUtils.<IUnit>newOneElementArrayList(new SqlSequentialUnit(sequence)));
	}
	
	public static IBatch concat(IBatch... bs) {
		if (bs == null) {
			return null;
		}
		final int len = bs.length;
		IBatch result = null;
		int i = 0;
		while (i < len) {
			if (bs[i] != null) {
				result = bs[i];
				++i;
				break;
			}
			++i;
		}
		if (result != null) {
			while (i < len) {
				final IBatch batch = bs[i];
				if (batch != null) {
					result.append(batch);
				}
				++i;
			}
		}
		return result;
	}

	//
	
	private final List<IUnit> units;

	public BatchImpl(List<IUnit> units) {
		if (units == null) throw new NullPointerException();
		this.units = units;
	}
	
	public BatchImpl(IUnit unit) {
		this(CollectionUtils.newOneElementArrayList(unit));
	}
	
	@Override
	public Iterator<IUnit> iterator() {
		return units.iterator();
	}

	@Override
	public List<IUnit> getUnits() {
		return units;
	}

	@Override
	public void append(IBatch batch) {
		if (batch != null) {
			final List<IUnit> units = batch.getUnits();
			if (units != null) {
				this.units.addAll(units);
			}
		}
	}

	@Override
	public void append(IUnit unit) {
		if (unit != null) {
			units.add(unit);
		}
	}

	@Override
	public DalCallResult process(IPreparedStringExecutor ex, EBatchType type) {
		final EBatchType unitType = (type == EBatchType.FAIL_EARLY) ? EBatchType.FAIL_EARLY : EBatchType.FAIL_NEVER;
		final boolean debug = LOG.isDebugEnabled();
		if (debug) LOG.debug("begin process of " + this);
		final DalCallResult result = new DalCallResult();
		for (IUnit unit: units) {
			if (!type.equals(EBatchType.FAIL_NEVER_IGNORE_EXCEPTION)) {
				result.add(unit.process(ex, unitType));
			}
			else {
				if (debug) LOG.debug("Potential ignoring execption on unit " + unit);
			}
		}
		if (debug) LOG.debug("end batch process with result: " + result);
		if (type.equals(EBatchType.FAIL_LATE)) {
			result.throwFirstException();
		}
		return result;
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append(":[\n");
		for (IUnit unit: units) {
			unit.toString(result, "\t");
			result.append("\n");
		}
		result.append("] // end of batch");
		return result.toString();
	}

}
