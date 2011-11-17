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
package org.nuclos.client;

import java.io.Serializable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nuclos.client.main.Main;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

public class NuclosHttpInvokerProxyFactoryBean extends HttpInvokerProxyFactoryBean {

	private static final Logger LOG = Logger.getLogger(NuclosHttpInvokerProxyFactoryBean.class);

	@Override
	protected RemoteInvocationResult executeRequest(RemoteInvocation invocation) throws Exception {
		invocation.addAttribute("user.timezone", Main.getInitialTimeZone());
		invocation.addAttribute("org.nuclos.api.context.InputContext", NuclosHttpInvokerAttributeContext.get());
		if (LOG.isDebugEnabled() && NuclosHttpInvokerAttributeContext.get().size() > 0) {
			LOG.debug("Sending call with dynamic context:");
			for (Map.Entry<String, Serializable> entry : NuclosHttpInvokerAttributeContext.get().entrySet()) {
				LOG.debug(entry.getKey() + ": " + String.valueOf(entry.getValue()));
			}
		}
		return super.executeRequest(invocation);
	}


}
