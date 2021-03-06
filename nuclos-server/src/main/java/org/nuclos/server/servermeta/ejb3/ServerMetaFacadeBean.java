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
package org.nuclos.server.servermeta.ejb3;

import java.util.TimeZone;

import org.nuclos.common.ParameterProvider;
import org.nuclos.server.common.ServerParameterProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Special "no-login"-service class. Keep small and secure ;)
 */
public class ServerMetaFacadeBean implements ServerMetaFacadeRemote {
	
	private ServerParameterProvider serverParameterProvider;
	
	public ServerMetaFacadeBean() {
	}
	
	@Autowired
	void setServerParameterProvider(ServerParameterProvider serverParameterProvider) {
		this.serverParameterProvider = serverParameterProvider;
	}
	
	public String getServerProperty(String key) {
		if(key.startsWith("application.settings.client."))
			return serverParameterProvider.getValue(key);
		return "<no access>";
	}
	
	public TimeZone getServerDefaultTimeZone() {
		return TimeZone.getDefault();
	}
	
	public String getDefaultNuclosTheme() {
		return serverParameterProvider.getValue(ParameterProvider.KEY_DEFAULT_NUCLOS_THEME);
	}

}
