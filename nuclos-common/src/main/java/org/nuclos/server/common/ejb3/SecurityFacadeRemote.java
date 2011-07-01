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
import javax.ejb.Remote;
import javax.security.auth.login.LoginException;

import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.security.Permission;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.common.MasterDataPermissions;
import org.nuclos.server.common.ModulePermissions;

@Remote
public interface SecurityFacadeRemote {
	public static final String IS_SUPER_USER = "IS_SUPER_USER";
	public static final String ALLOWED_ACTIONS = "ALLOWED_ACTIONS";
	public static final String MODULE_PERMISSIONS = "MODULE_PERMISSIONS";
	public static final String MASTERDATA_PERMISSIONS = "MASTERDATA_PERMISSIONS";
	
	/**
	 * logs the current user in.
	 * @return session id for the current user
	 */
	@RolesAllowed("Login")
	public abstract Integer login();

	/**
	 * logs the current user out.
	 * @param iSessionId session id for the current user
	 */
	@RolesAllowed("Login")
	public abstract void logout(Integer iSessionId) throws LoginException;

	/**
	 * @return information about the current version of the application installed on the server.
	 */
	@RolesAllowed("Login")
	public abstract ApplicationProperties.Version getCurrentApplicationVersionOnServer();

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
	 * @return the module permissions for the current user.
	 */
	@RolesAllowed("Login")
	public abstract ModulePermissions getModulePermissions();

	/**
	 * @return the masterdata permissions for the current user.
	 */
	@RolesAllowed("Login")
	public abstract MasterDataPermissions getMasterDataPermissions();

	/**
	 * get a String representation of the users session context
	 */
	@RolesAllowed("Login")
	public abstract String getSessionContextAsString();

	/**
	 * change the password of the logged in user after validating the old password
	 * @param sOldPassword
	 * @param sNewPassword
	 * @throws NuclosBusinessException
	 */
	@RolesAllowed("Login")
	public abstract void changePassword(String sOldPassword, String sNewPassword)
		throws NuclosBusinessException;

	/**
	 * change the password of the given user without validating the old password used in the management console.
	 * The encrypted password is build with the username and the given password
	 * @param sOldPassword
	 * @param sNewPassword
	 * @return
	 * @throws NuclosBusinessException
	 */
	@RolesAllowed("UseManagementConsole")
	public abstract void changeUserPassword(String sUserName, String sNewPassword)
		throws NuclosBusinessException;

	/**
	 * @return the readable subforms
	 */
	public abstract java.util.Map<Integer, org.nuclos.common.security.Permission> getSubFormPermission(
		String sEntityName);

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
	public abstract void invalidateCache();
	
	@RolesAllowed("Login")
	public Boolean isSuperUser();
	
	@RolesAllowed("Login")
	public Map<String, Object> getInitialSecurityData();
	
	@RolesAllowed("Login")
	public Integer getUserId(final String sUserName) throws CommonFatalException;

}
