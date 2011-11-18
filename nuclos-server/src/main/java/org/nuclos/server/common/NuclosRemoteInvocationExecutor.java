//Copyright (C) 2010  Novabit Informationssysteme GmbH
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
package org.nuclos.server.common;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.nuclos.api.context.InputContext;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationExecutor;

public class NuclosRemoteInvocationExecutor implements RemoteInvocationExecutor {

	private static final Logger LOG = Logger.getLogger(NuclosRemoteInvocationExecutor.class);

	@SuppressWarnings("unchecked")
	@Override
	public Object invoke(RemoteInvocation invoke, Object param) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		try {
			NuclosUserDetailsContextHolder.setTimeZone((TimeZone) invoke.getAttribute("user.timezone"));
			NuclosRemoteContextHolder.setRemotly(true);

			if (invoke.getAttribute("org.nuclos.api.context.InputContextSupported") != null) {
				Object o = invoke.getAttribute("org.nuclos.api.context.InputContextSupported");
				if (o instanceof Boolean) {
					InputContext.setSupported(((Boolean) o).booleanValue());
				}
			}
			if (invoke.getAttribute("org.nuclos.api.context.InputContext") != null) {
				HashMap<String, Serializable> context = (HashMap<String, Serializable>) invoke.getAttribute("org.nuclos.api.context.InputContext");
				if (LOG.isDebugEnabled()) {
					LOG.debug("Receiving call with dynamic context:");
					for (Map.Entry<String, Serializable> entry : context.entrySet()) {
						LOG.debug(entry.getKey() + ": " + String.valueOf(entry.getValue()));
					}
				}
				InputContext.set(context);
			}
			return invoke.invoke(param);
		}
		finally {
			NuclosUserDetailsContextHolder.clear();
			NuclosRemoteContextHolder.clear();
			InputContext.clear();
		}
	}

}
