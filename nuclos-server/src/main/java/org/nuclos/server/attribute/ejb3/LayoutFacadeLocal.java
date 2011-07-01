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

import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;

import org.nuclos.common2.EntityAndFieldName;

@Local
public interface LayoutFacadeLocal {

	/**
	 * @param sEntity
	 * @return true, if detail layout is available for the given entity name, otherwise false
	 */
	@RolesAllowed("Login")
	public abstract boolean isMasterDataLayoutAvailable(String sEntity);

	/**
	 * @param sEntity
	 * @return the detail layout for the given entity name if any, otherwise null
	 */
	@RolesAllowed("Login")
	public abstract String getMasterDataLayout(String sEntity);

	/**
	 * @param sEntity
	 * @return the layout for the given entity name if any, otherwise null
	 */
	@RolesAllowed("Login")
	public abstract String getMasterDataLayout(String sEntity,
		boolean bSearchMode);

	/**
	 * returns the entity names of the subform entities along with their foreignkey field
	 * and the referenced parent entity name used in the given layout
	 * Note that this works only for genericobject entities
	 * @param iLayoutId
	 */
	@RolesAllowed("Login")
	public abstract Map<EntityAndFieldName, String> getSubFormEntityAndParentSubFormEntityNamesByLayoutId(
		Integer iLayoutId);

	/**
	 * returns the names of the subform entities along with their foreignkey field
	 * and the referenced parent entity name used in the given entity
	 * @param entityName
	 * @param id, id of MasterDataVO or GenericObjectVO
	 * @param forImportOrExport, true if it is used for import- or export-routines
	 */
	@RolesAllowed("Login")
	public abstract Map<EntityAndFieldName, String> getSubFormEntityAndParentSubFormEntityNames(
		String entityName, Integer id, boolean forImportOrExport);

	@RolesAllowed("Login")
	public Map<EntityAndFieldName, String> getSubFormEntityAndParentSubFormEntityNamesById(Integer iLayoutId);
}
