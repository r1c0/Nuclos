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
package org.nuclos.client.masterdata.valuelistprovider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.masterdata.MetaDataCache;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaFieldVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;

public class SubFormFieldsCollectableFieldsProvider implements CollectableFieldsProvider {

	private String entity;

	@Override
	public void setParameter(String name, Object value) {
		if (name.equals("entityId") && value != null) {
			this.entity = MetaDataCache.getInstance().getMetaDataById((Integer)value).getEntityName();
		}
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		final List<CollectableField> result = new ArrayList<CollectableField>();

		Collection<MasterDataMetaVO> collSubEntities = null;

		if (Modules.getInstance().isModuleEntity(entity)) {
			collSubEntities = MasterDataDelegate.getInstance().getMetaDataByModuleId(Modules.getInstance().getModuleIdByEntityName(entity));
		}
		else {
			collSubEntities = new ArrayList<MasterDataMetaVO>();
			final Set<String> subEntities = MasterDataDelegate.getInstance().getSubFormEntityNamesByMasterDataEntity(entity);
			for (String subEntity : subEntities) {
				collSubEntities.add(MasterDataDelegate.getInstance().getMetaData(subEntity));
			}
		}

		for (MasterDataMetaVO mdmcvoSubEntity : collSubEntities) {
			if(!mdmcvoSubEntity.isDynamic() && mdmcvoSubEntity.isEditable()) {
				List<MasterDataMetaFieldVO> lstFieldVO = mdmcvoSubEntity.getFields();
				for(MasterDataMetaFieldVO fieldVO : lstFieldVO) {
					//result.add(new CollectableValueIdField(fieldVO.getId(), mdmcvoSubEntity.getLabel() + "." + fieldVO.getLabel()));
					result.add(new CollectableValueIdField(fieldVO.getId(), CommonLocaleDelegate.getLabelFromMetaDataVO(mdmcvoSubEntity) + "." + CommonLocaleDelegate.getResource(fieldVO.getResourceSIdForLabel(), fieldVO.getLabel())));
				}
			}
		}

		Collections.sort(result);
		return result;
	}
}
