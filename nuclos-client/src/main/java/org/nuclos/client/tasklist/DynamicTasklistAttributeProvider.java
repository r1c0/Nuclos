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
package org.nuclos.client.tasklist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.nuclos.client.datasource.DatasourceDelegate;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.exception.CommonBusinessException;

public class DynamicTasklistAttributeProvider implements CollectableFieldsProvider {

	public static String PARAMETER_DYNAMICTASKLIST = "dynamictasklist";
	
	private Integer dynamicTasklistId;
	
	@Override
	public void setParameter(String sName, Object oValue) {
		if (PARAMETER_DYNAMICTASKLIST.equals(sName) && !(oValue instanceof String)) {
			dynamicTasklistId = IdUtils.unsafeToId(oValue);
		}
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		if (dynamicTasklistId != null) {
			List<CollectableField> result = new ArrayList<CollectableField>();
			for (String s : DatasourceDelegate.getInstance().getDynamicTasklistAttributes(dynamicTasklistId)) {
				result.add(new CollectableValueField(s));
			}
			return result;
		}
		else {
			return Collections.emptyList();
		}
	}

}
