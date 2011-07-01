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

import org.apache.log4j.Logger;

import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.client.genericobject.GenericObjectMetaDataCache;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.masterdata.MetaDataCache;
import org.nuclos.server.attribute.valueobject.AttributeCVO;

/**
 * Value list provider to get all fields or attributes for an entity.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:martin.weber@novabit.de">Martin Weber</a>
 * @version 00.01.000
 */
public class FieldOrAttributeCollectableFieldsProvider implements CollectableFieldsProvider {

	private static final Logger log = Logger.getLogger(FieldOrAttributeCollectableFieldsProvider.class);
	
	private String sEntity = null;
	
	@Override
	public void setParameter(String sName, Object oValue) {
		if("entity".equals(sName) && oValue != null) {
			this.sEntity = MetaDataCache.getInstance().getMetaDataById((Integer)oValue).getEntityName();
		}
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		log.debug("getCollectableFields");
		
		final List<CollectableField> result = new ArrayList<CollectableField>();
		
		if (StringUtils.isNullOrEmpty(sEntity)) {
			return result;
		}
		else if (Modules.getInstance().isModuleEntity(sEntity)) {
			Integer iModuleId = Modules.getInstance().getModuleIdByEntityName(sEntity);
			final Collection<AttributeCVO> collattrcvo = GenericObjectMetaDataCache.getInstance().getAttributeCVOsByModuleId(iModuleId, false);

			for (AttributeCVO attributeCVO : collattrcvo) {
				result.add(new CollectableValueField(attributeCVO.getName()));
			}
		}
		else {
			for (String sFieldName : MetaDataCache.getInstance().getMetaData(sEntity).getFieldNames()) {
				result.add(new CollectableValueField(sFieldName));
			}
		}

		Collections.sort(result);

		return result;
	}
}
