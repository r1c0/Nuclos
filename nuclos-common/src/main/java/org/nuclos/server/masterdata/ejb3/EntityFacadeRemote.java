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
package org.nuclos.server.masterdata.ejb3;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;

import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common2.EntityAndFieldName;

// @Remote
public interface EntityFacadeRemote {

	/**
	 * @param sEntity
	 * @param ilaoyutId
	 * @return
	 * @throws RemoteException
	 */
	@RolesAllowed("Login")
	Map<EntityAndFieldName, String> getSubFormEntityAndParentSubFormEntityNames(String sEntity,Integer ilaoyutId) throws RemoteException;

	/**
	 * @param sEntityName
	 * @param sFieldName masterdata field name
	 * @param bCheckValidity Test for active sign and validFrom/validUntil
	 * @return list of collectable fields
	 * @todo this method should be used in CollectableFieldsProviders
	 */
	@RolesAllowed("Login")
	List<CollectableField> getCollectableFieldsByName(
		String sEntityName,
		String sFieldName,
		boolean bCheckValidity);

	/**
	 *
	 * @param entity
	 * @param field
	 * @param search
	 * @param vlpId
	 * @param vlpParameter
	 * @param iMaxRowCount
	 * @return
	 */
	@RolesAllowed("Login")
	List<CollectableValueIdField> getQuickSearchResult(String entity, String field, String search, Integer vlpId, 
			Map<String, Object> vlpParameter, String vlpValueFieldName, Integer iMaxRowCount);

}
