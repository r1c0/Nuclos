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

import javax.annotation.security.RolesAllowed;

import org.nuclos.api.Settings;
import org.nuclos.api.service.UserSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional(noRollbackFor= {Exception.class})
@RolesAllowed("Login")
public class ApiUserSettingsService implements UserSettingsService {
	
	private PreferencesFacadeRemote prefsFacade;
	
	@Autowired
	void setPreferencesFacadeRemote(PreferencesFacadeRemote prefsFacade) {
		this.prefsFacade = prefsFacade;
	}

	@Override
	public Settings getUserSettings(String key) {
		return prefsFacade.getApiUserSettings(key);
	}

	@Override
	public void setUserSettings(String key, Settings userSettings) {
		prefsFacade.setApiUserSettings(key, userSettings);
	}	
	
}
