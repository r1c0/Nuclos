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
package org.nuclos.server.resource.ejb3;

import java.util.Set;

import javax.annotation.security.RolesAllowed;

import org.nuclos.common.collection.Pair;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.resource.valueobject.ResourceVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;

// @Remote
public interface ResourceFacadeRemote {

	@RolesAllowed("Login")
	ResourceVO getResourceByName(String sResourceName);

	@RolesAllowed("Login")
	ResourceVO getResourceById(Integer iResourceId);

	@RolesAllowed("Login")
	Pair<ResourceVO, byte[]> getResource(String sResourceName);

	@RolesAllowed("Login")
	Pair<ResourceVO, byte[]> getResource(Integer resourceId);

	MasterDataVO create(String sEntityName, MasterDataVO mdvo,
		DependantMasterDataMap mpDependants) throws CommonCreateException,
		CommonPermissionException, NuclosBusinessRuleException;

	Object modify(String sEntityName, MasterDataVO mdvo,
		DependantMasterDataMap mpDependants) throws CommonCreateException,
		CommonFinderException, CommonRemoveException,
		CommonStaleVersionException, CommonValidationException,
		CommonPermissionException, NuclosBusinessRuleException;

	void remove(String sEntityName, MasterDataVO mdvo)
		throws CommonFinderException, CommonRemoveException,
		CommonStaleVersionException, CommonPermissionException,
		CommonCreateException, NuclosBusinessRuleException;

	/**
	 * get the file content of a resource file
	 * @param iResourceId
	 * @param sFileName
	 * @return resource file content
	 * @throws CommonFinderException
	 */
	@RolesAllowed("Login")
	byte[] loadResource(Integer iResourceId, String sFileName)
		throws CommonFinderException;

	Set<String> getResourceNames();

}
