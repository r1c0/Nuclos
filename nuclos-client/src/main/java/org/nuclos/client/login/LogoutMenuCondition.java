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
package org.nuclos.client.login;

import org.nuclos.client.main.DynamicClassCondition;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.servermeta.ejb3.ServerMetaFacadeRemote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.w3c.dom.Element;

/**
 * Menu evaluation condition: "logout" will not be shown on systems which support
 * no auto login anyway.
 */
@Configurable
public class LogoutMenuCondition implements DynamicClassCondition {
	
	// Spring injection
	
	private ServerMetaFacadeRemote serverMetaFacadeRemote;
	
	// end of Spring injection
	
	public LogoutMenuCondition() {
	}
	
	@Autowired
	final void setServerMetaFacadeRemote(ServerMetaFacadeRemote serverMetaFacadeRemote) {
		this.serverMetaFacadeRemote = serverMetaFacadeRemote;
	}
	
	@Override
	public boolean eval(Element conditionElement) {
		return Boolean.valueOf(
			StringUtils.defaultIfNull(
				StringUtils.nullIfEmpty(
					serverMetaFacadeRemote.getServerProperty("application.settings.client.autologin.allowed")),
			"false"));
	}
}
