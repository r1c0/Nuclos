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
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.security.auth.login.LoginException;

import org.nuclos.common.security.Permission;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.common.MasterDataPermissions;
import org.nuclos.server.common.ModulePermissions;

@Local
public interface SecurityFacadeLocal {

	/**
	 * @return information about the current version of the application installed on the server.
	 */
	@RolesAllowed("Login")
	public abstract String getUserName();

	/**
	 * Get all actions that are allowed for the current user.
	 * @return set that contains the Actions objects (no duplicates).
	 */
	@RolesAllowed("Login")
	public abstract Set<String> getAllowedActions();

	/**
	 * determine the permission of an attribute regarding the state of a module for the current user
	 * @param sAttributeName
	 * @param iModuleId
	 * @param iStatusNumeral
	 * @return Permission
	 */
	public abstract Permission getAttributePermission(String sEntity, String sAttributeName,
		Integer iState);

	@RolesAllowed("Login")
	public Map<String, Permission> getAttributePermissionsByEntity(String entity, Integer stateId);

	@RolesAllowed("Login")
	public abstract ModulePermissions getModulePermissions();

	@RolesAllowed("Login")
	public abstract MasterDataPermissions getMasterDataPermissions();

	/**
	 * logs the current user in.
	 * @return session id for the current user
	 */
	public abstract Integer login();

	/**
	 * logs the current user out.
	 * @param iSessionId session id for the current user
	 */
	public abstract void logout(Integer iSessionId) throws LoginException;
	
	@RolesAllowed("Login")
	public Integer getUserId(final String sUserName) throws CommonFatalException;
	
}
