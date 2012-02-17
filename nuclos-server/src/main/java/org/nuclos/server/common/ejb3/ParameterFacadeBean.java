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
package org.nuclos.server.common.ejb3;

import java.util.Map;

import org.nuclos.server.common.ServerParameterProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
* Facade bean for managing parameters.
* <br>
* <br>Created by Novabit Informationssysteme GmbH
* <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
*/
//@Stateless
//@Remote(ParameterFacadeRemote.class)
//@Transactional
public class ParameterFacadeBean extends NuclosFacadeBean implements ParameterFacadeRemote {
	
	private ServerParameterProvider serverParameterProvider;
	
	public ParameterFacadeBean() {	
	}
	
	@Autowired
	void setServerParameterProvider(ServerParameterProvider serverParameterProvider) {
		this.serverParameterProvider = serverParameterProvider;
	}

	/**
	 * get all parameter entries
	 * @return map of parameters with values
	 */
	public Map<String, String> getParameters() {
		return serverParameterProvider.getAllParameters();
	}
}
