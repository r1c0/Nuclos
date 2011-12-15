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
package org.nuclos.server.dbtransfer.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dbtransfer.NucletContentUID;
import org.nuclos.common.dbtransfer.TransferOption;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.dbtransfer.NucletContentMap;
import org.nuclos.server.dbtransfer.TransferFacadeLocal;
import org.nuclos.server.dbtransfer.TransferUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class EntityFieldNucletContent extends DefaultNucletContent implements INucletInterface {
	
	public static final String NUCLET_INTERFACE_FOREIGN_ENTITY_UID = "EntityNucletContent.nucletInterfaceForeignEntityUID";
	public static final String NUCLET_INTERFACE_FOREIGN_ENTITY_UID_IS_ADJUSTED = "adjusted";

	public EntityFieldNucletContent(List<INucletContent> contentTypes) {
		super(NuclosEntity.ENTITYFIELD, NuclosEntity.ENTITY, contentTypes);
	}

	@Override
	public List<EntityObjectVO> getNcObjects(Set<Long> nucletIds, TransferOption.Map transferOptions) {
		List<EntityObjectVO> result = super.getNcObjects(nucletIds, transferOptions);
		for (EntityObjectVO ncObject : result) {
			storeLocaleResources(ncObject, "localeresourcel", "localeresourced");
		}
		return result;
	}

	@Override
	public void insertOrUpdateNcObject(DalCallResult result, EntityObjectVO ncObject, boolean isNuclon) {
		restoreLocaleResources(ncObject);
		super.insertOrUpdateNcObject(result, ncObject, isNuclon);
	}
	
	@Override
	public boolean validate(EntityObjectVO ncObject, ValidationType type, NucletContentMap importContentMap, NucletContentUID.Map uidMap, Set<Long> existingNucletIds, ValidityLogEntry log, org.nuclos.common.dbtransfer.TransferOption.Map transferOptions) {
		boolean result =  super.validate(ncObject, type, importContentMap, uidMap, existingNucletIds, log, transferOptions);
		if (result == false) {
			return result;
		}
		
		switch (type) {
		case INSERT:
			// check if references are possible
			String foreignentity = org.nuclos.common2.LangUtils.defaultIfNull(ncObject.getField("foreignentity", String.class), ncObject.getField("unreferencedforeignentity", String.class));
			if (foreignentity != null && !NuclosEntity.isNuclosEntity(foreignentity) && TransferUtils.getEntityObjectVO(importContentMap.getValues(NuclosEntity.ENTITY), "entity", foreignentity) == null) {
				
				// check if nuclet interfaces entity uid is present and foreignentity exists
				String nucletInterfaceUID = ncObject.getField(NUCLET_INTERFACE_FOREIGN_ENTITY_UID, String.class);
				boolean dummy = true;
				if (nucletInterfaceUID != null) {
					info("Nuclet interface entity UID " + nucletInterfaceUID + " found. Searching for entity...");
					if (TransferUtils.getNcObjectIdFromNucletContentUID(NuclosEntity.ENTITY, nucletInterfaceUID) != null){
						info("Found!");
						dummy = false;
					} else {
						info("NOT found!");
					}
				}
				
				if (dummy) {
					EntityObjectVO entityVO = TransferUtils.getEntityObjectVO(importContentMap.getValues(NuclosEntity.ENTITY), ncObject.getFieldId("entity"));
					log.newWarningLine("Entity " + entityVO.getField("entity", String.class) + " references to unknown entity " + foreignentity + ". Redirect to dummy entity!");
					ncObject.getFields().put("foreignentity", NuclosEntity.DUMMY.getEntityName());
					ncObject.getFields().put("foreignentityfield", "name");
				}
			}
			break;
		case UPDATE:
			foreignentity = org.nuclos.common2.LangUtils.defaultIfNull(ncObject.getField("foreignentity", String.class), ncObject.getField("unreferencedforeignentity", String.class));
			if (foreignentity != null && !NuclosEntity.isNuclosEntity(foreignentity) && TransferUtils.getEntityObjectVO(importContentMap.getValues(NuclosEntity.ENTITY), "entity", foreignentity) == null) {
				
				// check if nuclet interfaces entity uid is present
				String nucletInterfaceUID = ncObject.getField(NUCLET_INTERFACE_FOREIGN_ENTITY_UID, String.class);
				if (nucletInterfaceUID != null) {
					
					// adjust nuclet interface entity uid
					NucletContentUID uid = uidMap.getUID(ncObject);
					Long existingFieldId = TransferUtils.getNcObjectIdFromNucletContentUID(NuclosEntity.ENTITYFIELD, uid.uid);
					EntityObjectVO existingFieldVO = NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.ENTITYFIELD).getByPrimaryKey(existingFieldId);
					String existingForeignEntity = existingFieldVO.getField("foreignentity", String.class);
					
					ncObject.getFields().put("foreignentity", existingForeignEntity);
					ncObject.getFields().put("foreignentityfield", existingFieldVO.getField("foreignentityfield", String.class));
					ncObject.getFields().put(NUCLET_INTERFACE_FOREIGN_ENTITY_UID, NUCLET_INTERFACE_FOREIGN_ENTITY_UID_IS_ADJUSTED);
				}
			}
			break;
		}
		
		return result;
	}

	@Override
	public void addNucletInterfaces(Map<NuclosEntity, List<EntityObjectVO>> result,	List<EntityObjectVO> uidObjects) {
		for (EntityObjectVO ncEntityField : result.get(getEntity())) {
			final String foreignEntityName = ncEntityField.getField("foreignentity");
			if (foreignEntityName != null) {
				// is referencing field... check for interface
				if (NuclosEntity.isNuclosEntity(foreignEntityName)) {
					continue;
				}
				boolean found = false;
				for (EntityObjectVO ncEntity : result.get(NuclosEntity.ENTITY)) {
					if (LangUtils.equals(foreignEntityName, ncEntity.getField("entity"))) {
						found = true;
					}
				}
				if (!found) {
					ncEntityField.getFields().put(NUCLET_INTERFACE_FOREIGN_ENTITY_UID, 
							getOrCreateEntityUID(foreignEntityName));
				}
			}
		}		
	}
	
	private String getOrCreateEntityUID(String entity) {
		Long entityId = MetaDataServerProvider.getInstance().getEntity(entity).getId();
		EntityObjectVO uidObject = TransferUtils.getUID(NuclosEntity.ENTITY, entityId);
		if (uidObject == null) {
			uidObject = TransferUtils.createUIDRecordForNcObject(
					NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.ENTITY).getByPrimaryKey(entityId));
		}
		if (uidObject != null) {
			String uid = uidObject.getField("uid");
			info("Storing UID " + uid + " for nuclet interface entity " + entity);
			
			return uid;
		} else {
			throw new NuclosFatalException("UID for entity " + entity + " not created!");
		}
	}
	
	/**
	 * 
	 * @param importContentMap
	 * @return
	 */
	public static List<EntityObjectVO> getNucletInterfaces(NucletContentMap importContentMap) {
		return CollectionUtils.select(importContentMap.getValues(NuclosEntity.ENTITYFIELD),
				new Predicate<EntityObjectVO>() {
					@Override
					public boolean evaluate(EntityObjectVO t) {
						return t.getField(NUCLET_INTERFACE_FOREIGN_ENTITY_UID) != null;
					}
				});
	}
}
