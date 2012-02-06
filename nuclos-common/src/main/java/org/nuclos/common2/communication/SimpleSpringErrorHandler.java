package org.nuclos.common2.communication;

import org.apache.log4j.Logger;
import org.springframework.util.ErrorHandler;

public class SimpleSpringErrorHandler implements ErrorHandler {
	
	private static final Logger LOG = Logger.getLogger(SimpleSpringErrorHandler.class);
	
	//
	
	private String prefix = "";
	
	SimpleSpringErrorHandler() {
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	public String getPrefix() {
		return prefix;
	}

	@Override
	public void handleError(Throwable t) {
		LOG.warn(prefix + ": " + t, t);
	}

}
