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

import java.util.Collection;
import java.util.Map;

import javax.annotation.security.RolesAllowed;

import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.WorkspaceDescription;
import org.nuclos.common.WorkspaceVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.common.valueobject.PreferencesVO;

// @Remote
public interface PreferencesFacadeRemote {

	/**
	 * @return the preferences for the user currently logged
	 */
	@RolesAllowed("Login")
	PreferencesVO getUserPreferences()
		throws CommonFinderException;
	
	/**
	 * @return the preferences for the user defined as template user, exception if not defined in t_ad_parameter
	 */
	@RolesAllowed("Login")
	PreferencesVO getTemplateUserPreferences() throws NuclosBusinessException, CommonFinderException;

	/**
	 * stores the preferences for the user currently logged
	 * @param prefsvo the preferences for the user currently logged
	 */
	@RolesAllowed("Login")
	void modifyUserPreferences(PreferencesVO prefsvo)
		throws CommonFinderException;

	/**
	 * sets the preferences for the user with the given name (administrative use only).
	 * @param sUserName
	 * @param prefsvo the preferences for the user currently logged. If <code>null</code>, the preferences for the given user will be reset.
	 * @throws CommonFinderException
	 */
	@RolesAllowed("UseManagementConsole")
	void setPreferencesForUser(String sUserName,
		PreferencesVO prefsvo) throws CommonFinderException;

	/**
	 * sets the preferences for the user with the given name (administrative use only).
	 * @param sUserName
	 * @param prefsvo the preferences for the user currently logged. If <code>null</code>, the preferences for the given user will be reset.
	 * @throws CommonFinderException
	 */
	@RolesAllowed("UseManagementConsole")
	PreferencesVO getPreferencesForUser(String sUserName)
		throws CommonFinderException;

	/**
	 * Merges the given preferences into the preferences for the user with the given name (administrative use only).
	 * @throws CommonFinderException
	 */
	@RolesAllowed("UseManagementConsole")
	void mergePreferencesForUser(String name, Map<String, Map<String, String>> preferencesToMerge)
		throws CommonFinderException;
	
	/**
	 * 
	 * @return
	 */
	public Collection<WorkspaceVO> getWorkspaceHeaderOnly();
	
	/**
	 * 
	 * @param id
	 * @return
	 * @throws CommonBusinessException 
	 */
	public WorkspaceVO getWorkspace(Long id) throws CommonBusinessException;
		
	/**
	 * 
	 * @param wovo
	 * @return 
	 * @throws CommonBusinessException 
	 */
	public WorkspaceVO storeWorkspace(WorkspaceVO wovo) throws CommonBusinessException;
	
	/**
	 * 
	 * @param wovo
	 * @throws CommonBusinessException 
	 */
	public void storeWorkspaceHeaderOnly(WorkspaceVO wovo) throws CommonBusinessException;
	
	/**
	 * 
	 * @param id
	 */
	public void removeWorkspace(Long id);
	
	/**
	 * 
	 * @param wovo 
	 * 			private or customized workspace
	 * @param roleIds
	 * @throws CommonBusinessException 
	 * return private or customized workspace
	 */
	public WorkspaceVO assignWorkspace(WorkspaceVO wovo, Collection<Long> roleIds) throws CommonBusinessException;
	
	/**
	 * 
	 * @return 
	 */
	public Collection<EntityObjectVO> getAssignableRoles();
	
	/**
	 * 
	 * @param assignedWorkspaceId
	 * @return
	 */
	public Collection<Long> getAssignedRoleIds(Long assignedWorkspaceId);

	/**
	 * 
	 * @param customizedWovo
	 * @param isPublishStructureChanged 
	 * @param isPublishStructureUpdate 
	 * @param isPublishStarttabConfiguration
	 * @param isPublishToolbarConfiguration 
	 * @param isPublishTableColumnConfiguration 
	 * @throws CommonBusinessException
	 */
	public void publishWorkspaceChanges(WorkspaceVO customizedWovo, boolean isPublishStructureChanged, boolean isPublishStructureUpdate, boolean isPublishStarttabConfiguration, boolean isPublishTableColumnConfiguration, boolean isPublishToolbarConfiguration) throws CommonBusinessException;

	/**
	 * 
	 * @param customizedWovo
	 * @return
	 * @throws CommonBusinessException
	 */
	public WorkspaceVO restoreWorkspace(WorkspaceVO customizedWovo) throws CommonBusinessException;

	/**
	 * 
	 * @param id1
	 * @param id2
	 * @return
	 * @throws CommonBusinessException
	 */
	public boolean isWorkspaceStructureChanged(Long id1, Long id2) throws CommonBusinessException;

}
