//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.common;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;

import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;

/**
 * Common interface implemented by EntityObject -Delegates, -Facades, and -Providers. 
 * 
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 */
public interface EntityObjectCommon {

	/**
	 * gets the ids of all leased objects that match a given search expression (ordered, when necessary)
	 * @param iModuleId id of module to search for leased objects in
	 * @param cond condition that the leased objects to be found must satisfy
	 * @return List<Integer> list of leased object ids
	 */
	@RolesAllowed("Login")
	List<Long> getEntityObjectIds(Long id, CollectableSearchExpression cse);
	
	/**
	 * gets all generic objects along with its dependents, that match a given search condition
	 * @param iModuleId id of module to search for generic objects in
	 * @param clctexpr value object containing search expression
	 * @param stRequiredAttributeIds may be <code>null</code>, which means all attributes are required
	 * @param stRequiredSubEntityNames
	 * @param bIncludeParentObjects
	 * @param bIncludeSubModules Include submodules in search?
	 * @return list of generic object value objects
	 * @precondition stRequiredSubEntityNames != null
	 * @todo rename to getGenericObjectProxyList?
	 */
	@RolesAllowed("Login")
	ProxyList<EntityObjectVO> getEntityObjectProxyList(Long id, CollectableSearchExpression clctexpr, 
			Collection<EntityFieldMetaDataVO> fields);
	
	/**
	 * gets more leased objects that match a given search condition
	 * @param iModuleId					id of module to search for leased objects in
	 * @param clctexpr value object containing search condition
	 * @param lstSortOrderValues field values for building correct where clause
	 * @param iRecordCount					 number of records to get for search condition
	 * @param stRequiredAttributeIds may be <code>null</code>, which means all attributes are required
	 * @param stRequiredSubEntityNames
	 * @param bIncludeParentObjects

	 * @return list of leased object value objects
	 * @precondition stRequiredSubEntityNames != null
	 */
	@RolesAllowed("Login")
	Collection<EntityObjectVO> getEntityObjectsMore(Long id, List<Long> lstIds, Collection<EntityFieldMetaDataVO> fields);


	/**
	 * gets the dependent master data records for the given entity, using the given foreign key field and the given id as foreign key.
	 * @param sEntityName name of the entity to get all dependent master data records for
	 * @param sForeignKeyField name of the field relating to the foreign entity
	 * @param oRelatedId id by which sEntityName and sParentEntity are related
	 * @return
	 * @precondition oRelatedId != null
	 * @todo restrict permissions by entity name
	 */
	@RolesAllowed("Login")
	Collection<EntityObjectVO> getDependentEntityObjects(
		String sEntityName, String sForeignKeyField, Long oRelatedId);
	
	@RolesAllowed("Login")
	Collection<EntityObjectVO> getDependentPivotEntityObjects(
		EntityFieldMetaDataVO pivot, String sForeignKeyField, Long oRelatedId);	

}
