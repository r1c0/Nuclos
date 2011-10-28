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

import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.attribute.valueobject.AttributeCVO;

// @Remote
public interface AttributeFacadeRemote {

	/**
	 * @return a collection containing all dynamic attributes
	 */
	@RolesAllowed("Login")
	Collection<AttributeCVO> getAttributes(Integer iGroupId);

	/**
	 * @param iAttributeId id of attribute
	 * @return the attribute value object for the attribute with the given id
	 * @throws CommonPermissionException
	 * @precondition iAttributeId != null
	 */
	AttributeCVO get(Integer iAttributeId)
		throws CommonFinderException, CommonPermissionException;

	/**
	 * creates a new attribute in the database. External values will not be added, they must be added in the referenced master data table directly.
	 * @param attrcvo contains the attribute data
	 * @return same attribute as value object
	 * @throws CommonPermissionException
	 * @postcondition result != null
	@RolesAllowed("Login")
	AttributeCVO create(AttributeCVO attrcvo,
		DependantMasterDataMap mpDependants) throws CommonCreateException,
		CommonFinderException, CommonRemoveException,
		CommonStaleVersionException, CommonValidationException,
		CommonPermissionException;
	 */

	/**
	 * updates an existing attribute in the database.
	 * External values will not be modified, they must be modified in the referenced master data table directly.
	 *
	 * @param attrcvo contains the attribute data
	 * @return new attribute id
	 * @throws CommonPermissionException
	 * @throws CreateException
	@RolesAllowed("Login")
	Integer modify(AttributeCVO attrcvo,
		DependantMasterDataMap mpDependants) throws CommonCreateException,
		CommonFinderException, CommonRemoveException,
		CommonStaleVersionException, CommonValidationException,
		NuclosSystemAttributeNotModifiableException, CommonPermissionException;
	 */

	/**
	 * deletes the given attribute from the database.
	 * External values will not be deleted, they must be deleted in the referenced master data table directly.
	 * @param attrcvo the attribute to be deleted
	 * @throws CommonPermissionException
	@RolesAllowed("Login")
	void remove(AttributeCVO attrcvo)
		throws CommonFinderException, CommonRemoveException,
		CommonStaleVersionException,
		NuclosSystemAttributeNotModifiableException, CommonPermissionException;
	 */

	/**
	 * invalidates the attribute cache (console function)
	 */
	@RolesAllowed("Login")
	void invalidateCache();

	/**
	 * @return the available calculation functions for calculated attributes
	 */
	@RolesAllowed("Login")
	Collection<String> getCalculationFunctions();

	/**
	 *
	 * @param sAttributeName
	 * @return the layouts that contained this attribute
	 */
	@RolesAllowed("Login")
	Set<String> getAttributeLayouts(String sAttributeName);
	
	/**
	 *
	 * @param sAttributeName
	 * @return the layouts that contained this attribute
	 */
	@RolesAllowed("Login")
	Set<String> getAttributeForModule(String iModuleId);

}
