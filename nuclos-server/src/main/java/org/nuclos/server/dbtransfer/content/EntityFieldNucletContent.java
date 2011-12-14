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
import org.nuclos.common.dbtransfer.TransferOption;
import org.nuclos.common2.LangUtils;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.dbtransfer.NucletContentMap;
import org.nuclos.server.dbtransfer.TransferUtils;

public class EntityFieldNucletContent extends DefaultNucletContent implements INucletInterface {
	
	public static final String NUCLET_INTERFACE_FOREIGN_ENTITY_UID = "EntityNucletContent.nucletInterfaceForeignEntityUID";

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
	public void addNucletInterfaces(Map<NuclosEntity, List<EntityObjectVO>> result,	List<EntityObjectVO> uidObjects) {
		for (EntityObjectVO ncEntityField : result.get(getEntity())) {
			final String sForeignEntityName = ncEntityField.getField("foreignentity");
			if (sForeignEntityName != null) {
				// is referencing field... check for interface
				
				boolean found = false;
				for (EntityObjectVO ncEntity : result.get(NuclosEntity.ENTITY)) {
					if (LangUtils.equals(sForeignEntityName, ncEntity.getField("entity"))) {
						found = true;
					}
				}
				if (!found) {
					Long entityId = MetaDataServerProvider.getInstance().getEntity(sForeignEntityName).getId();
					EntityObjectVO uidObject = TransferUtils.getUID(NuclosEntity.ENTITY, entityId);
					if (uidObject == null) {
						uidObject = TransferUtils.createUIDRecordForNcObject(NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.ENTITY).getByPrimaryKey(entityId));
					}
					if (uidObject != null) {
						String uid = uidObject.getField("uid");
						info("Storing UID " + uid + " for nuclet interface entity " + sForeignEntityName);
						ncEntityField.getFields().put(NUCLET_INTERFACE_FOREIGN_ENTITY_UID, uid);
					} else {
						throw new NuclosFatalException("UID for entity " + sForeignEntityName + " not created!");
					}
				}
			}
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
