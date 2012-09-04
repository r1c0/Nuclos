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
package org.nuclos.client.genericobject.valuelistprovider.generation;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.genericobject.GenericObjectLayoutCache;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.masterdata.MasterDataLayoutHelper;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.exception.CommonBusinessException;
/**
 * Value list provider to get subentities of a module.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 00.01.000
 */
public class GenerationSubEntityCollectableFieldsProvider implements CollectableFieldsProvider {
	private static final Logger log = Logger.getLogger(GenerationSubEntityCollectableFieldsProvider.class);

	private Integer moduleId;
	private Integer parameterEntityId;
	private String sourceType;

	@Override
	public void setParameter(String sName, Object oValue) {
		log.debug("setParameter - sName = " + sName + " - oValue = " + oValue);
		if (sName.equals("module")) {
			this.moduleId = (Integer) oValue;
		} else if (sName.equals("parameterEntity")) {
			this.parameterEntityId = (Integer) oValue;
		} else if (sName.equals("sourceType")) {
			this.sourceType = (String) oValue;
		} else {
			// ignore
		}
	}

	protected Integer getEntityId() {
		return "parameter".equals(sourceType) ? parameterEntityId : moduleId;
	}
	
	
	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		log.debug("getCollectableFields");

		Integer entityId = getEntityId();
		if (entityId == null) {
			return Collections.<CollectableField>emptyList();
		} else {
			List<CollectableField> result = MasterDataDelegate.getInstance().getSubEntities(entityId);
			String sEntityName = MetaDataClientProvider.getInstance().getEntity(IdUtils.toLongId(entityId)).getEntity();
			if(Modules.getInstance().isModuleEntity(sEntityName)) {
				for (EntityAndFieldName entityAndFieldName : GenericObjectLayoutCache.getInstance().getSubFormEntities(entityId)) {
					EntityMetaDataVO metaDataVO = MetaDataClientProvider.getInstance().getEntity(entityAndFieldName.getEntityName());
					CollectableValueIdField cIdField = new CollectableValueIdField(new Integer(metaDataVO.getId().intValue()), metaDataVO.getEntity());		
					if (!result.contains(cIdField)) {
						result.add(cIdField);
					}
				}
			} else {
				for (String entityFieldName : MasterDataLayoutHelper.newLayoutRoot(sEntityName, false, ClientParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY)).getMapOfSubForms().keySet()) {
					EntityMetaDataVO metaDataVO = MetaDataClientProvider.getInstance().getEntity(entityFieldName);
					CollectableValueIdField cIdField = new CollectableValueIdField(new Integer(metaDataVO.getId().intValue()), metaDataVO.getEntity());		
					if (!result.contains(cIdField)) {
						result.add(cIdField);
					}
				}				
			}
			Collections.sort(result);
			return result;
		}
	}

}	// class ModuleSubEntityCollectableFieldsProvider
