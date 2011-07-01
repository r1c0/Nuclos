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

import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.LocalizedCollectableValueField;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.KeyEnum;
import org.nuclos.common2.Localizable;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * {@link CollectableFieldsProvider} which is filled with the values of a given Enum class.
 */
public class EnumCollectableFieldsProvider implements CollectableFieldsProvider {

	private String showEnum;
	
	@Override
	public void setParameter(String sName, Object oValue) {
		if (sName.equals("showEnum")) {
			showEnum = (String) oValue;
		}
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		List<CollectableField> result = new ArrayList<CollectableField>();

		try {
			// asSubclass does not work with wild cards types (i.e. Enum<?>)...
			Class<? extends Enum> clazz = Class.forName(this.showEnum).asSubclass(Enum.class);
			for (Enum e : clazz.getEnumConstants()) {
				Object value = (e instanceof KeyEnum) ? ((KeyEnum) e).getValue() : e.name();
				String text = (e instanceof Localizable) ? CommonLocaleDelegate.getText((Localizable) e) : e.toString();
				CollectableField cf = new LocalizedCollectableValueField(value, text);
				result.add(cf);
			}
		} catch (Exception e) {
			throw new CommonBusinessException("Invalid parameter: ", e);
		}

		Collections.sort(result);
		return result;
	}

}	// class RoleActionsCollectableFieldsProvider
