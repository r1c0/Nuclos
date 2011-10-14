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

import java.util.List;

import org.nuclos.client.entityobject.EntityFacadeDelegate;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * generic valuelistprovider for all entities (genericobject and masterdata).
 *
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:thomas.schiffmann@novabit.de">thomas.schiffmann</a>
 * @version 01.00.00
 */
public class GenericCollectableFieldsProvider implements CollectableFieldsProvider {

	private String entity;
	private String fieldexpression;
	private boolean valid = false;

	/* (non-Javadoc)
	 * @see org.nuclos.common.collect.collectable.CollectableFieldsProvider#setParameter(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setParameter(String name, Object value) {
		if ("entity".equals(name)) {
			entity = value.toString();
		}
		else if ("field".equals(name)) {
			fieldexpression = value.toString();
		}
		else if ("valid".equals(name)) {
			valid = Boolean.parseBoolean(value.toString());
		}
	}

	/* (non-Javadoc)
	 * @see org.nuclos.common.collect.collectable.CollectableFieldsProvider#getCollectableFields()
	 */
	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		final List<CollectableField> result;

		if (Modules.getInstance().isModuleEntity(entity)) {
			result = EntityFacadeDelegate.getInstance().getCollectableFieldsByName(entity, fieldexpression, false);
		}
		else {
			CollectableFieldsProvider masterdataCollectableFieldsProvider = new MasterDataCollectableFieldsProvider(entity);
			masterdataCollectableFieldsProvider.setParameter("fieldName", fieldexpression);
			masterdataCollectableFieldsProvider.setParameter("_searchmode", !valid);
			result = masterdataCollectableFieldsProvider.getCollectableFields();
		}

		return result;
	}
}
