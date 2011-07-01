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

import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;

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

@Remote
public interface ResourceFacadeRemote {

	/**
	 * @param sResourceName
	 */
	@RolesAllowed("Login")
	public abstract ResourceVO getResourceByName(String sResourceName);

	/**
	 * @param sResourceId
	 */
	@RolesAllowed("Login")
	public abstract ResourceVO getResourceById(Integer iResourceId);

	/**
	 * @param sResourceName
	 */
	@RolesAllowed("Login")
	public abstract Pair<ResourceVO, byte[]> getResource(String sResourceName);

	@RolesAllowed("Login")
	public abstract Pair<ResourceVO, byte[]> getResource(Integer resourceId);

	public abstract MasterDataVO create(String sEntityName, MasterDataVO mdvo,
		DependantMasterDataMap mpDependants) throws CommonCreateException,
		CommonPermissionException, NuclosBusinessRuleException;

	public abstract Object modify(String sEntityName, MasterDataVO mdvo,
		DependantMasterDataMap mpDependants) throws CommonCreateException,
		CommonFinderException, CommonRemoveException,
		CommonStaleVersionException, CommonValidationException,
		CommonPermissionException, NuclosBusinessRuleException;

	public abstract void remove(String sEntityName, MasterDataVO mdvo)
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
	public abstract byte[] loadResource(Integer iResourceId, String sFileName)
		throws CommonFinderException;

}
