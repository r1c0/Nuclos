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
package org.nuclos.client.genericobject.valuelistprovider;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.genericobject.GenericObjectMetaDataCache;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.server.attribute.valueobject.AttributeCVO;

/**
 * Value list provider to get attributes by module.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">Christoph Radig</a>
 * @version 00.01.000
 */
public class AttributeCollectableFieldsProvider implements CollectableFieldsProvider {
	private static final Logger log = Logger.getLogger(AttributeCollectableFieldsProvider.class);

	private Integer iModuleId;

	/**
	 * valid parameters:
	 * <ul>
	 *   <li>"module" = module id</li>
	 *   <li>"moduleId" = module id</li>
	 *   <li>"moduleName" = module name</li>
	 * </ul>
	 * @param sName parameter name
	 * @param oValue parameter value
	 */
	@Override
	public void setParameter(String sName, Object oValue) {
		log.debug("setParameter - sName = " + sName + " - oValue = " + oValue);
		if (sName.equals("module") || sName.equals("moduleId")) {
			if (oValue == null || oValue instanceof Integer) {
				this.iModuleId = (Integer) oValue;
			}
			else {
				this.iModuleId = new Integer(oValue.toString());
			}
		}
		else if (sName.equals("moduleName")) {
			this.iModuleId = (oValue == null) ? null : Modules.getInstance().getModuleIdByEntityName(oValue.toString());
		}
		else {
			// ignore
		}
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		log.debug("getCollectableFields");
		final Collection<AttributeCVO> collattrcvo = (this.iModuleId == null) ? AttributeCache.getInstance().getAttributes() :
				GenericObjectMetaDataCache.getInstance().getAttributeCVOsByModuleId(this.iModuleId, false);

		final List<CollectableField> result = CollectionUtils.transform(collattrcvo, new Transformer<AttributeCVO, CollectableField>() {
			@Override
			public CollectableField transform(AttributeCVO attrcvo) {
				/** @todo replace getName with getLabel? */
				return new CollectableValueIdField(attrcvo.getId(), attrcvo.getName());
			}
		});

		Collections.sort(result);

		return result;
	}

}	// class AttributeCollectableFieldsProvider
