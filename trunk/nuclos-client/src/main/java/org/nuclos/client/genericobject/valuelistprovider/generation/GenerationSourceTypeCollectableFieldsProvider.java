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

import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.LocalizedCollectableValueField;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * Value list provider for the object generation source type field (used for copying attributes/subform).
 */
public class GenerationSourceTypeCollectableFieldsProvider implements CollectableFieldsProvider {

	private Integer parameterEntityId;

	/**
	 * Supported parameters:
	 * <ul>
	 *   <li>"parameterEntity" = parameter entity id</li>
	 * </ul>
	 * @param name parameter name
	 * @param value parameter value
	 */
	@Override
	public void setParameter(String name, Object value) {
		if (name.equals("parameterEntity")) {
			parameterEntityId = (Integer) value;
		}
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		if (parameterEntityId != null) {
			return Collections.<CollectableField>singletonList(new LocalizedCollectableValueField("parameter", "parameter"));
		} else {
			return Collections.<CollectableField>emptyList();
		}
	}

}	// class GenerationAttributeCollectableFieldsProvider
