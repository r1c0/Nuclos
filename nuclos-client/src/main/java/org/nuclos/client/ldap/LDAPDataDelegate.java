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
package org.nuclos.client.ldap;

import java.rmi.RemoteException;
import java.util.Collection;

import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.ldap.ejb3.LDAPDataFacadeRemote;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVOWrapper;

/**
 * Business Delegate for <code>LDAPDataFacade</code>
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Rostislav.Maksymovskyi@novabit.de">Rostislav.Maksymovskyi</a>
 * @version 01.00.00
 */
public class LDAPDataDelegate {
	
	private static LDAPDataDelegate INSTANCE;
	
	// Spring injection

	private LDAPDataFacadeRemote ldapDataFacadeRemote;
	
	// end of Spring injection

	/**
	 * Use getInstance() to create an (the) instance of this class
	 */
	LDAPDataDelegate() throws RemoteException {
		INSTANCE = this;
	}
	
	public static LDAPDataDelegate getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}

	public final void setLDAPDataFacadeRemote(LDAPDataFacadeRemote ldapDataFacadeRemote) {
		this.ldapDataFacadeRemote = ldapDataFacadeRemote;
	}

	public MasterDataVO create(MasterDataVO vo, DependantMasterDataMap mpDependants) throws CommonBusinessException {
		return this.getLDAPDataFacade().create(vo, mpDependants);
	}

	public MasterDataVO modify(MasterDataVO vo, DependantMasterDataMap mpDependants) throws CommonBusinessException {
		return this.getLDAPDataFacade().modify(vo, mpDependants);
	}

	public Collection<MasterDataWithDependantsVOWrapper> getUsers(String filterExpr, Object[] filterArgs) throws CommonBusinessException {
		return this.getLDAPDataFacade().getUsers(filterExpr, filterArgs);
	}

	public boolean tryAuthentication(String ldapserver, String username, String password) throws CommonBusinessException {
		return this.getLDAPDataFacade().tryAuthentication(ldapserver, username, password);
	}

	public LDAPDataFacadeRemote getLDAPDataFacade() {
		return this.ldapDataFacadeRemote;
	}

}
