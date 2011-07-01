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
package org.nuclos.server.attribute.ejb3;

import java.util.Collection;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.CreateException;
import javax.ejb.Remote;

import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.attribute.NuclosSystemAttributeNotModifiableException;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;

@Remote
public interface AttributeFacadeRemote {

	/**
	 * @return a collection containing all dynamic attributes
	 */
	@RolesAllowed("Login")
	public abstract Collection<AttributeCVO> getAttributes(Integer iGroupId);

	/**
	 * @param iAttributeId id of attribute
	 * @return the attribute value object for the attribute with the given id
	 * @throws CommonPermissionException
	 * @precondition iAttributeId != null
	 */
	public abstract AttributeCVO get(Integer iAttributeId)
		throws CommonFinderException, CommonPermissionException;

	/**
	 * creates a new attribute in the database. External values will not be added, they must be added in the referenced master data table directly.
	 * @param attrcvo contains the attribute data
	 * @return same attribute as value object
	 * @throws CommonPermissionException
	 * @postcondition result != null
	 */
	@RolesAllowed("Login")
	public abstract AttributeCVO create(AttributeCVO attrcvo,
		DependantMasterDataMap mpDependants) throws CommonCreateException,
		CommonFinderException, CommonRemoveException,
		CommonStaleVersionException, CommonValidationException,
		CommonPermissionException;

	/**
	 * updates an existing attribute in the database.
	 * External values will not be modified, they must be modified in the referenced master data table directly.
	 *
	 * @param attrcvo contains the attribute data
	 * @return new attribute id
	 * @throws CommonPermissionException
	 * @throws CreateException
	 */
	@RolesAllowed("Login")
	public abstract Integer modify(AttributeCVO attrcvo,
		DependantMasterDataMap mpDependants) throws CommonCreateException,
		CommonFinderException, CommonRemoveException,
		CommonStaleVersionException, CommonValidationException,
		NuclosSystemAttributeNotModifiableException, CommonPermissionException,
		CreateException;

	/**
	 * deletes the given attribute from the database.
	 * External values will not be deleted, they must be deleted in the referenced master data table directly.
	 * @param attrcvo the attribute to be deleted
	 * @throws CommonPermissionException
	 */
	@RolesAllowed("Login")
	public abstract void remove(AttributeCVO attrcvo)
		throws CommonFinderException, CommonRemoveException,
		CommonStaleVersionException,
		NuclosSystemAttributeNotModifiableException, CommonPermissionException;

	/**
	 * invalidates the attribute cache (console function)
	 */
	@RolesAllowed("Login")
	public abstract void invalidateCache();

	/**
	 * @return the available calculation functions for calculated attributes
	 */
	@RolesAllowed("Login")
	public abstract Collection<String> getCalculationFunctions();

	/**
	 *
	 * @param sAttributeName
	 * @return the layouts that contained this attribute
	 */
	@RolesAllowed("Login")
	public abstract Set<String> getAttributeLayouts(String sAttributeName);
	
	/**
	 *
	 * @param sAttributeName
	 * @return the layouts that contained this attribute
	 */
	@RolesAllowed("Login")
	public abstract Set<String> getAttributeForModule(String iModuleId);

}
