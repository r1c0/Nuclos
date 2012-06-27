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
package org.nuclos.client.masterdata.user;

import org.nuclos.common.security.UserFacadeRemote;
import org.nuclos.common.security.UserVO;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;

public class UserDelegate {

	private static UserDelegate INSTANCE;
	
	// Spring injectin
	
	private UserFacadeRemote userFacadeRemote;
	
	// end of Spring injection

	UserDelegate() {
		INSTANCE = this;
	}

	public static UserDelegate getInstance() {
		return INSTANCE;
	}
	
	public final void setUserFacadeRemote(UserFacadeRemote userFacadeRemote) {
		this.userFacadeRemote = userFacadeRemote;
	}

	public UserVO create(UserVO vo, DependantMasterDataMap mpDependants) throws CommonBusinessException {
		return userFacadeRemote.create(vo, mpDependants);
	}

	public UserVO modify(UserVO vo, DependantMasterDataMap mpDependants) throws CommonBusinessException {
		return userFacadeRemote.modify(vo, mpDependants);
	}

	public void remove(UserVO vo) throws CommonBusinessException {
		userFacadeRemote.remove(vo);
	}

}
