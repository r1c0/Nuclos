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
import java.util.Collections;
import java.util.List;

import org.nuclos.client.common.LocaleDelegate;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.LocalizedCollectableValueField;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.exception.CommonBusinessException;

public class LocaleCollectableFieldsProvider implements CollectableFieldsProvider {

	private Boolean includeNull = false;
	
	@Override
	public void setParameter(String sName, Object oValue) {
		if ("includeNull".equalsIgnoreCase(sName)) {
			includeNull = "true".equals(oValue);
		}
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		List<CollectableField> result = new ArrayList<CollectableField>();

		for (LocaleInfo li : LocaleDelegate.getInstance().getAllLocales(includeNull)) {
			result.add(new LocalizedCollectableValueField(li.name, li.title));
		}

		Collections.sort(result);
		return result;
	}
}	// class LocaleCollectableFieldsProvider
