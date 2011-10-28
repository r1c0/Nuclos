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
package org.nuclos.server.ldap.ejb3;

import java.util.Collection;

import javax.annotation.security.RolesAllowed;

import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVOWrapper;

// @Remote
public interface LDAPDataFacadeRemote {

	/**
	 * validate and create a new ldap server configuration.
	 *
	 * @param vo ldap server configuration as valueobject
	 * @param mpDependants subform data (attribute mapping)
	 * @return created valueobject
	 * @throws CommonBusinessException
	 */
	MasterDataVO create(MasterDataVO vo, DependantMasterDataMap mpDependants) throws CommonBusinessException;

	/**
	 * validate and modify an existing ldap server configuration.
	 *
	 * @param vo ldap server configuration as valueobject
	 * @param mpDependants subform data (attribute mapping)
	 * @return modified valueobject
	 * @throws CommonBusinessException
	 */
	MasterDataVO modify(MasterDataVO vo, DependantMasterDataMap mpDependants) throws CommonBusinessException;

	/**
	 * Find all LDAP users.
	 * @param filterExpr ldap filter expression, i.e. (sAMAccountName={0})
	 * @param filterArgs filter parameters, i.e. username
	 * @return a collection containing the search result for the given search expression.
	 * TODO restrict permissions
	 */
	@RolesAllowed("Login")
	Collection<MasterDataWithDependantsVOWrapper> getUsers(String filterExpr, Object[] filterArgs) throws CommonBusinessException;

	boolean tryAuthentication(String ldapserver, String username, String password) throws CommonPermissionException, CommonBusinessException;
}
