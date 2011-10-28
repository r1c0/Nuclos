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
package org.nuclos.server.genericobject.ejb3;

import java.util.Set;

import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;

// @Local
public interface GenericObjectGroupFacadeLocal {

	/**
	 * @param iGenericObjectId
	 * @return the ids of the object group, the genericobject is assigned to
	 */
	Set<Integer> getObjectGroupId(Integer iGenericObjectId);

	/**
	 * @param iGenericObjectId
	 * @return the group name
	 */
	String getObjectGroupName(Integer iGenericObjectId);

	/**
	 * @param iGroupId
	 * @return the ids of the genericobjects, which are assigned to the given objectgroup
	 */
	Set<Integer> getGenericObjectIdsForGroup(Integer iModuleId,
		Integer iGroupId);

	/**
	 * removes the generic object with the given id from the group with the given id.
	 *
	 * @return
	 * @param iGenericObjectId generic object id to be removed. Must be a main module object.
	 * @param iGroupId id of group to remove generic object from
	 * @throws CommonCreateException
	 * @throws CommonPermissionException
	 * @throws CommonStaleVersionException
	 * @throws CommonRemoveException
	 * @throws CommonFinderException
	 * @throws NuclosBusinessRuleException
	 * @throws CommonCreateException
	 * @throws CommonPermissionException
	 * @throws CommonStaleVersionException
	 * @throws CommonRemoveException
	 * @throws CommonFinderException
	 * @throws NuclosBusinessRuleException
	 */
	void removeFromGroup(int iGenericObjectId, int iGroupId)
		throws NuclosBusinessRuleException, CommonFinderException,
		CommonRemoveException, CommonStaleVersionException,
		CommonPermissionException, CommonCreateException;

	/**
	 * adds the generic object with the given id to the group with the given id.
	 *
	 * @return
	 * @param iGenericObjectId generic object id to be grouped.  Must be a main module object.
	 * @param iGroupId id of group to add generic object to
	 * @throws CommonPermissionException
	 * @throws CommonCreateException
	 * @throws NuclosBusinessRuleException
	 * @throws CommonPermissionException
	 * @throws CommonCreateException
	 * @throws NuclosBusinessRuleException
	 */
	void addToGroup(int iGenericObjectId, int iGroupId)
		throws NuclosBusinessRuleException, CommonCreateException,
		CommonPermissionException;

}
