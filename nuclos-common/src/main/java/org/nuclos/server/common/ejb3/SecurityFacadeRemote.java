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

import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.security.auth.login.LoginException;

import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.security.Permission;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.common.MasterDataPermissions;
import org.nuclos.server.common.ModulePermissions;

// @Remote
public interface SecurityFacadeRemote {
	
	String USERNAME = "USERNAME";
	String IS_SUPER_USER = "IS_SUPER_USER";
	String ALLOWED_ACTIONS = "ALLOWED_ACTIONS";
	String MODULE_PERMISSIONS = "MODULE_PERMISSIONS";
	String MASTERDATA_PERMISSIONS = "MASTERDATA_PERMISSIONS";

	/**
	 * logs the current user in.
	 * @return session id for the current user
	 */
	@RolesAllowed("Login")
	Integer login();

	/**
	 * logs the current user out.
	 * @param iSessionId session id for the current user
	 */
	@RolesAllowed("Login")
	void logout(Integer iSessionId) throws LoginException;

	/**
	 * get the date of password expiration (or null if password never expires)
	 */
	@RolesAllowed("Login")
	Date getPasswordExpiration();

	/**
	 * @return information about the current version of the application installed on the server.
	 */
	@RolesAllowed("Login")
	ApplicationProperties.Version getCurrentApplicationVersionOnServer();

	/**
	 * @return information about the current version of the application installed on the server.
	 */
	@RolesAllowed("Login")
	String getUserName();

	/**
	 * Get all actions that are allowed for the current user.
	 * @return set that contains the Actions objects (no duplicates).
	 */
	@RolesAllowed("Login")
	Set<String> getAllowedActions();

	/**
	 * @return the module permissions for the current user.
	 */
	@RolesAllowed("Login")
	ModulePermissions getModulePermissions();

	/**
	 * @return the masterdata permissions for the current user.
	 */
	@RolesAllowed("Login")
	MasterDataPermissions getMasterDataPermissions();

	/**
	 * get a String representation of the users session context
	 */
	@RolesAllowed("Login")
	String getSessionContextAsString();

	/**
	 * @return the readable subforms
	 */
	Map<Integer, Permission> getSubFormPermission(
		String sEntityName);

	/**
	 * determine the permission of an attribute regarding the state of a module for the current user
	 * @param sAttributeName
	 * @param iModuleId
	 * @param iStatusNumeral
	 * @return Permission
	 */
	Permission getAttributePermission(String sEntity, String sAttributeName,
		Integer iState);


	@RolesAllowed("Login")
	Map<String, Permission> getAttributePermissionsByEntity(String entity, Integer stateId);

	@RolesAllowed("Login")
	void invalidateCache();

	@RolesAllowed("Login")
	Boolean isSuperUser();

	@RolesAllowed("Login")
	Map<String, Object> getInitialSecurityData();

	@RolesAllowed("Login")
	Integer getUserId(final String sUserName) throws CommonFatalException;

	Boolean isLdapAuthenticationActive();

	Boolean isLdapSynchronizationActive();
}
