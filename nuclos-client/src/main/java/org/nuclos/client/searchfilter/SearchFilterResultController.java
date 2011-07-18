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
package org.nuclos.client.searchfilter;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.SearchFilterCollectController;
import org.nuclos.client.ui.collect.result.ResultController;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;

/**
 * A specialization of ResultController for use with an {@link SearchFilterCollectController}.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class SearchFilterResultController<Clct extends CollectableMasterDataWithDependants> extends ResultController<Clct> {

	public SearchFilterResultController(String entityName) {
		super(entityName);
	}

	@Override
	public SortedSet<CollectableEntityField> getFieldsAvailableForResult(CollectableEntity clcte, Comparator<CollectableEntityField> comp) {
		assert getEntity().equals(clcte);
		final SortedSet<CollectableEntityField> result = new TreeSet<CollectableEntityField>(comp);
		for (CollectableEntityField cef : super.getFieldsAvailableForResult(clcte, comp)) {
			if (!SearchFilterCollectController.FIELD_SEARCHFILTER.equals(cef.getName())
					&& !SearchFilterCollectController.FIELD_LABELRES.equals(cef.getName())
					&& !SearchFilterCollectController.FIELD_DESCRIPTIONRES.equals(cef.getName())) {
				result.add(cef);
			}
		}
		return result;
	}
}
