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
package org.nuclos.client.remote.http;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nuclos.client.main.Main;
import org.nuclos.client.remote.NuclosHttpInvokerAttributeContext;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

/**
 * An extension to Spring HttpInvokerProxyFactoryBean used for remote calls from 
 * nuclos client to server.
 */
public class NuclosHttpInvokerProxyFactoryBean extends HttpInvokerProxyFactoryBean {

	private static final Logger LOG = Logger.getLogger(NuclosHttpInvokerProxyFactoryBean.class);
	
	/**
	 * Whether to turn INFO logging of profiling data on.
	 */
	private static final boolean PROFILE = true;
	
	/**
	 * Minimum delay for calls in order to appear in LOG. 
	 */
	private static final long PROFILE_MIN_MS = 300L;
	
	//
	
	private NuclosHttpInvokerAttributeContext ctx;
	
	public NuclosHttpInvokerProxyFactoryBean() {
	}
	
	// @Autowired
	public void setNuclosHttpInvokerAttributeContext(NuclosHttpInvokerAttributeContext ctx) {
		this.ctx = ctx;
	}

	/**
	 * This overridden executeRequest could be used for performance tracking of remote calls.
	 * It should commented out in production environments.
	 * 
	 * @author Thomas Pasch
	 * @since Nuclos 3.8
	 */
	/*
	@Override
	protected RemoteInvocationResult executeRequest(RemoteInvocation invocation) throws Exception {
		invocation.addAttribute("user.timezone", Main.getInitialTimeZone());
		invocation.addAttribute("org.nuclos.api.context.InputContextSupported", ctx.isSupported());
		final HashMap<String, Serializable> map = ctx.get();
		invocation.addAttribute("org.nuclos.api.context.InputContext", map);
		if (LOG.isDebugEnabled() && map.size() > 0) {
			LOG.debug("Sending call with dynamic context:");
			for (Map.Entry<String, Serializable> entry : map.entrySet()) {
				LOG.debug(entry.getKey() + ": " + String.valueOf(entry.getValue()));
			}
		}
		invocation.addAttribute("org.nuclos.api.context.MessageReceiverContext", ctx.getMessageReceiver());
		
		final long before;
		if (PROFILE) {
			before = System.currentTimeMillis();
		}
		else {
			before = 0L;
		}
		
		final RemoteInvocationResult result = super.executeRequest(invocation);
		
		if (PROFILE) {
			final long call = System.currentTimeMillis() - before;
			if (call >= PROFILE_MIN_MS) {
				LOG.info("remote invocation of " + invocation + " on " + getServiceInterface() + " took " + call + " ms");
			}
		}
		
		return result;
	}
	 */

}
