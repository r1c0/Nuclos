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
package org.nuclos.client.entityobject;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.server.masterdata.ejb3.EntityFacadeRemote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// @Component
public class EntityFacadeDelegate implements EntityFacadeRemote {

	private static final Logger LOG = Logger.getLogger(EntityFacadeDelegate.class);

	private static EntityFacadeDelegate INSTANCE;
	
	//

	private EntityFacadeRemote facade;

	EntityFacadeDelegate() {
		INSTANCE = this;
	}

	public static EntityFacadeDelegate getInstance() {
		return INSTANCE;
	}
	
	@Autowired
	void setEntityFacadeRemote(EntityFacadeRemote entityFacadeRemote) {
		this.facade = entityFacadeRemote;
	}

	public List<CollectableField> getCollectableFieldsByName(
		String sEntityName,
		String sFieldName,
		boolean bCheckValidity) {
		return facade.getCollectableFieldsByName(sEntityName, sFieldName, bCheckValidity);
	}

	public Map<EntityAndFieldName, String> getSubFormEntityAndParentSubFormEntityNames(
		String sEntity, Integer ilaoyutId) throws RemoteException {
		return facade.getSubFormEntityAndParentSubFormEntityNames(sEntity,
			ilaoyutId);
	}

	@Override
	public List<CollectableValueIdField> getQuickSearchResult(String entity, String field, String search,
			Integer vlpId, Map<String, Object> vlpParameter, Integer iMaxRowCount) {
		return facade.getQuickSearchResult(entity, field, search, vlpId, vlpParameter, iMaxRowCount);
	}

	@Override
	public List<CollectableValueIdField> getQuickSearchResult(String entity, EntityFieldMetaDataVO efMeta, String search,
			Integer vlpId, Map<String, Object> vlpParameter, Integer iMaxRowCount) {
		return facade.getQuickSearchResult(entity, efMeta, search, vlpId, vlpParameter, iMaxRowCount);
	}
	
	@Override
	public String getBaseEntity(String dynamicentityname) {
		return facade.getBaseEntity(dynamicentityname);
	}
	
}
