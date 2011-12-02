package org.nuclos.server.dblayer.impl;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.server.dblayer.EBatchType;
import org.nuclos.server.dblayer.IPart;
import org.nuclos.server.dblayer.IPart.NextPartHandling;
import org.nuclos.server.dblayer.IPreparedStringExecutor;
import org.nuclos.server.dblayer.IUnit;
import org.nuclos.server.dblayer.impl.util.PreparedString;

public class SqlConditionalUnit implements IUnit {

	private static final Logger LOG = Logger.getLogger(SqlSequentialUnit.class);

	private final List<IPart> parts;

	public SqlConditionalUnit(List<IPart> parts) {
		if (parts == null)
			throw new NullPointerException();
		this.parts = parts;
	}

	public SqlConditionalUnit(IPart part) {
		this(Collections.singletonList(part));
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
			
			succeeded = false;
			final EBatchType partBatchType = p.getBatchType();
			List<PreparedString> statements = p.getStatements();
			for (PreparedString ps : statements) {
				try {
					final int changes = ex.executePreparedStatement(ps);
					succeeded = true;
					result.addToNumberOfDbChanges(changes);
				} catch (SQLException e) {
					succeeded = false;
					if (!partBatchType.equals(EBatchType.FAIL_NEVER_IGNORE_EXCEPTION)) {
						result.addBusinessException(null, Collections.singletonList(ps.toString()), e);
					} else {
						if (debug)
							LOG.debug("Ignored exception: " + e + " while executing " + ps);
					}
					switch (partBatchType) {
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
		result.append("] // end of unit");
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		toString(result, "");
		return result.toString();
	}

}
