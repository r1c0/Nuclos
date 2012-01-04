//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.client.ui.collect.result;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.user.UserCollectController;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;

/**
 * A specialization of ResultController for use with an {@link UserCollectController}.
 * 
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 */
public class UserResultController<Clct extends CollectableMasterDataWithDependants> extends NuclosResultController<Clct> {
	
	public UserResultController(CollectableEntity clcte, ISearchResultStrategy<Clct> srs) {
		super(clcte, srs);
	}

	/**
	 * @deprecated You should really provide a CollectableEntity here.
	 */
	public UserResultController(String entityName, ISearchResultStrategy<Clct> srs) {
		super(entityName, srs);
	}
	
	/**
	 * @deprecated Remove this.
	 */
	@Override
	public SortedSet<CollectableEntityField> getFieldsAvailableForResult(Comparator<CollectableEntityField> comp) {
		final SortedSet<CollectableEntityField> result = new TreeSet<CollectableEntityField>(comp);
		for (CollectableEntityField cef : super.getFieldsAvailableForResult(comp)) {
			if (!UserCollectController.FIELD_PREFERENCES.equals(cef.getName()) && !UserCollectController.FIELD_PASSWORD.equals(cef.getName())) {
				result.add(cef);
			}
		}
		return result;
	}
}
