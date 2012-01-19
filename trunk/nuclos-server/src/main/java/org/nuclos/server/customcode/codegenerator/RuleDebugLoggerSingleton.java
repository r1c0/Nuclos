package org.nuclos.server.customcode.codegenerator;

import org.apache.log4j.Logger;

/**
 * Singleton used by classes enhanced by {@link ClassDebugAdapter} to log debug output.
 * <p>
 * In Nuclos this is used for (server) Rules that have the debug flag enabled.
 * </p>
 * @author Thomas Pasch
 * @since Nuclos 3.0.11
 */
public final class RuleDebugLoggerSingleton {
	
	private static final RuleDebugLoggerSingleton INSTANCE = new RuleDebugLoggerSingleton();
	
	private Logger LOG = Logger.getLogger(RuleDebugLoggerSingleton.class);
	
	private RuleDebugLoggerSingleton() {
	}
	
	public static RuleDebugLoggerSingleton getInstance() {
		return INSTANCE;
	}
	
	public void log(String prefix, String var, Object o) {
		if (!LOG.isInfoEnabled()) return;
		final StringBuilder sb = new StringBuilder();
		try {
			sb.append(prefix).append(var).append(" => ").append(o);
			LOG.info(sb.toString());
		}
		catch (Exception e) {
			LOG.debug("Failed to report rule step in debug mode", e);
		}
	}
	
}
