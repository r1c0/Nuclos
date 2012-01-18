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

import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaFieldVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;

/**
 * Collectable fields provider for masterdata fields belonging to a certain (leased object) module. 
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */
public class AllCollectableEntityFieldsCollectableFieldsProvider implements CollectableFieldsProvider  {
	private Integer iModuleId = null;
	@Override
	public void setParameter(String sName, Object oValue) {
		if (sName.equals("moduleId")) {
			this.iModuleId = (Integer) oValue;
		}
		else if(sName.equals("_searchmode")) {
		}
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		final List<CollectableField> result = new ArrayList<CollectableField>();

		// add subentities' fields, if any:
		final Collection<MasterDataMetaVO> collSubEntities = MasterDataDelegate.getInstance().getMetaDataByModuleId(iModuleId);
		for (MasterDataMetaVO mdmcvoSubEntity : collSubEntities) {
			if(!mdmcvoSubEntity.isDynamic() && mdmcvoSubEntity.isEditable()) {
				List<MasterDataMetaFieldVO> lstFieldVO = mdmcvoSubEntity.getFields();
				for(MasterDataMetaFieldVO fieldVO : lstFieldVO) {
					//result.add(new CollectableValueIdField(fieldVO.getId(), mdmcvoSubEntity.getLabel() + "." + fieldVO.getLabel()));
					result.add(new CollectableValueIdField(fieldVO.getId(), 
							CommonLocaleDelegate.getInstance().getLabelFromMetaDataVO(mdmcvoSubEntity) + "." 
							+ CommonLocaleDelegate.getInstance().getResource(fieldVO.getResourceSIdForLabel(), fieldVO.getLabel())));
				}
			}
		}

		Collections.sort(result);
		return result;
	}
}
