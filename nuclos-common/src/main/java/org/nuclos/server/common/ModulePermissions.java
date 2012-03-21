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
package org.nuclos.server.common;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.nuclos.common.ModuleProvider;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.collection.Pair;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.genericobject.ejb3.GenericObjectGroupFacadeRemote;

/**
 * Contains the module permissions for a user, for all modules.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public class ModulePermissions implements Serializable {

	private final Map<Pair<String, Integer>, ModulePermission> mpByEntityName;
	private final Map<Pair<Integer, Integer>, ModulePermission> mpByModuleId;
	private final Map<String, Boolean> mpNewAllowedByEntityName;
	private final Map<Integer, Boolean> mpNewAllowedByModuleId;
	private final Map<String, Set<Integer>> mpNewAllowedProcessesByEntityName;
	private final Map<Integer, Set<Integer>> mpNewAllowedProcessesByModuleId;

	public Map<String, Boolean> getNewAllowedByEntityName() {
    	return mpNewAllowedByEntityName;
    }

	public Map<Integer, Boolean> getNewAllowedByModuleId() {
    	return mpNewAllowedByModuleId;
    }

	/**
	 * Create the module (aka GenericObject) permission object.
	 * This are the permissions for an (individual, pre-selected) user.
	 * 
	 * @param mpByEntityName 
	 * 			Map of Pair((String) entity name, (Integer) object group) -> ModulePermission.
	 * 			Null of PairX is not allowed.
	 * 			Null for PairY is for 'no object group'.
	 * @param mpByModuleId
	 * 			Map of Pair((Integer) moduleId, (Integer) object group) -> ModulePermission.
	 * 			Null of PairX is not allowed.
	 * 			Null for PairY is for 'no object group'.
	 * @param mpNewAllowedByEntityName
	 * 			Map of ((String) entity name) -> Boolean
	 * @param mpNewAllowedByModuleId
	 * 			Map of ((Integer) moduleId) -> Boolean
	 * @param mpNewAllowedProcessesByEntityName
	 * 			Map of ((String) entity name) -> Set of ((Integer) processIds/actions).
	 * 			The result set includes 'null' if new is allowed for the default (no action) case.
	 * @param mpNewAllowedProcessesByModuleId
	 * 			Map of ((Integer) moduleId) -> Set of ((Integer) processIds/actions).
	 * 			The result set includes 'null' if new is allowed for the default (no action) case.
	 */
	public ModulePermissions(
			Map<Pair<String, Integer>, ModulePermission> mpByEntityName,
			Map<Pair<Integer, Integer>, ModulePermission> mpByModuleId,
			Map<String, Boolean> mpNewAllowedByEntityName,
			Map<Integer, Boolean> mpNewAllowedByModuleId,
			Map<String, Set<Integer>> mpNewAllowedProcessesByEntityName,
			Map<Integer, Set<Integer>> mpNewAllowedProcessesByModuleId) {
		this.mpByEntityName = mpByEntityName;
		this.mpByModuleId = mpByModuleId;
		this.mpNewAllowedByEntityName = mpNewAllowedByEntityName;
		this.mpNewAllowedByModuleId = mpNewAllowedByModuleId;
		this.mpNewAllowedProcessesByEntityName = mpNewAllowedProcessesByEntityName;
		this.mpNewAllowedProcessesByModuleId = mpNewAllowedProcessesByModuleId;
	}

	public Map<Pair<String, Integer>, ModulePermission> getPermissionsByEntityName() {
		return mpByEntityName;
	}

	public Map<Pair<Integer, Integer>, ModulePermission> getPermissionsByModuleId() {
		return mpByModuleId;
	}

	public Map<String, Set<Integer>> getNewAllowedProcessesByEntityName() {
    	return mpNewAllowedProcessesByEntityName;
    }

	public Map<Integer, Set<Integer>> getNewAllowedProcessesByModuleId() {
    	return mpNewAllowedProcessesByModuleId;
    }

	/**
	 * returns the maximum right for an entity and the objectgroup
	 * @param sEntityName
	 * @param iGenericObjectId
	 */
	public ModulePermission getMaxPermissionForObjectGroup(String sEntityName, Integer iObjectGroupId) {
		ModulePermission maxpermission = null;

		for (Pair<String, Integer> keyPair : mpByEntityName.keySet()) {
			boolean permissionFound = false;
			if (iObjectGroupId == null && keyPair.getX().equals(sEntityName) && (keyPair.getY() == null || keyPair.getY().intValue() == 0)) {
				permissionFound = true;
			}
			else if (iObjectGroupId != null && (keyPair.getX().equals(sEntityName) && ((keyPair.getY() == null || keyPair.getY().intValue() == 0) || (keyPair.getY().intValue() == iObjectGroupId.intValue())))){
				permissionFound = true;
			}

			if (permissionFound == true && (maxpermission == null || mpByEntityName.get(keyPair).compareTo(maxpermission) > 0)) {
				permissionFound = false;
				maxpermission = mpByEntityName.get(keyPair);

				if (maxpermission == ModulePermission.DELETE_PHYSICALLY) {
					break;
				}
			}
		}
		return maxpermission;
	}
	
	/**
	 * @return the maximum right for an entity and the genericobject
	 * 		and ModulePermission.NO for no permissions.
	 */
	public ModulePermission getMaxPermissionForGO(String sEntityName, Integer iGenericObjectId) {
		final ModulePermission result = getMaxPermissionForGenericObject(sEntityName, iGenericObjectId);
		return result == null ? ModulePermission.NO : result;
	}

	/**
	 * @return the maximum right for an entity and the genericobject
	 * 		and null for no permissions.
	 */
	public ModulePermission getMaxPermissionForGenericObject(String sEntityName, Integer iGenericObjectId) {
		ModulePermission maxpermission = null;
		Set<Integer> stObjectGroupId = null;

		if (iGenericObjectId != null) {
			stObjectGroupId = getObjectGroupId(iGenericObjectId);
		}

		for (Pair<String, Integer> keyPair : mpByEntityName.keySet()) {
			// NUCLOS-150: for each iteration, permissionFound must be reseted 
			// to avoid privilege escalation.
			boolean permissionFound = false;
			// get maximum right for entity independant of a genericobject (new data record, general read operation)
			if (iGenericObjectId == null) {
				assert stObjectGroupId == null;
				if (keyPair.getX().equals(sEntityName)) {
					permissionFound = true;
				}
			}
			// get maximum right for entity dependand on a genericobject that is not assigned to an objectgroup
			else if (iGenericObjectId != null && (stObjectGroupId == null || stObjectGroupId.isEmpty())) {
				if (keyPair.getX().equals(sEntityName) && (keyPair.getY() == null || keyPair.getY().intValue() == 0)) {
					permissionFound = true;
				}
			}
			// get maximum right for entity dependant on genericobject that is assigned to one or more objectgroups
			else {
				for (Integer iOgId : stObjectGroupId) {
					if (keyPair.getX().equals(sEntityName) && (keyPair.getY() == null || keyPair.getY().intValue() == 0 || keyPair.getY().intValue() == iOgId.intValue())) {
						permissionFound = true;
						break;
					}
				}
			}

			if (permissionFound == true && (maxpermission == null || mpByEntityName.get(keyPair).compareTo(maxpermission) > 0)) {
				permissionFound = false;
				maxpermission = mpByEntityName.get(keyPair);

				if (maxpermission == ModulePermission.DELETE_PHYSICALLY) {
					break;
				}
			}
		}
		return maxpermission;
	}

	public ModulePermission get(Integer iModuleId, Integer iGenericObject) {
		ModuleProvider modules = SpringApplicationContextHolder.getBean(ModuleProvider.class);
		return this.getMaxPermissionForGenericObject(modules.getEntityNameByModuleId(iModuleId), iGenericObject);
	}

	Set<Entry<Pair<Integer, Integer>, ModulePermission>> getEntries() {
		return this.mpByModuleId.entrySet();
	}

	private Set<Integer> getObjectGroupId(Integer iGenericObjectId) {
		try {
			return ServiceLocator.getInstance().getFacade(GenericObjectGroupFacadeRemote.class).getObjectGroupId(iGenericObjectId);
		}
		catch (RuntimeException e) {
			throw new CommonFatalException(e);
		}
	}

}	// class ModulePermissions
