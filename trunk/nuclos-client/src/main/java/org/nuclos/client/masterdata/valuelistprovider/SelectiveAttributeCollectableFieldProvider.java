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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.genericobject.GenericObjectMetaDataCache;
import org.nuclos.common.ParameterProvider;
import org.nuclos.server.attribute.valueobject.AttributeCVO;

/**
 * @todo enter class description.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */
public class SelectiveAttributeCollectableFieldProvider implements CollectableFieldsProvider {
	private Integer iModuleId;
	private Set<String> stExcludedAttributes = new HashSet<String>();

	@Override
	public void setParameter(String sName, Object oValue) {
		if (sName.equals("moduleId")) {
			this.iModuleId = (Integer) oValue;
		}
		else if(sName.equals("exclude")) {
			Integer iExcludeParameter = (Integer) oValue;

			ParameterProvider paramProvider = ClientParameterProvider.getInstance();
			String sForbiddenAttributes = "";

			// TODO: Use GenericObjectImportUtils.getForbiddenAttributeNames()!
			if(iExcludeParameter != null) {
				switch(iExcludeParameter) {
				case 0:		// Create new object
					sForbiddenAttributes = paramProvider.getValue(ParameterProvider.KEY_GENERICOBJECT_IMPORT_EXCLUDED_ATTRIBUTES_FOR_CREATE);
					break;
				case 1:		// Update existing object
					sForbiddenAttributes = paramProvider.getValue(ParameterProvider.KEY_GENERICOBJECT_IMPORT_EXCLUDED_ATTRIBUTES_FOR_UPDATE);
					break;
				default:
					break;
				}
			}
			
			stExcludedAttributes.clear();
			if(sForbiddenAttributes != null) {
				stExcludedAttributes.addAll(Arrays.asList(sForbiddenAttributes.split(",")));
			}
		}
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		final List<CollectableField> result = new ArrayList<CollectableField>();

		Collection<AttributeCVO> collAttributeCVO = GenericObjectMetaDataCache.getInstance().getAttributeCVOsByModuleId(iModuleId, Boolean.FALSE);
		for(AttributeCVO attrcvo : collAttributeCVO) {
			if(!attrcvo.isCalculated() && !stExcludedAttributes.contains(attrcvo.getName())) {
				result.add(new CollectableValueIdField(attrcvo.getId(), attrcvo.getName()));
			}
		}

		Collections.sort(result);
		return result;
	}
}
