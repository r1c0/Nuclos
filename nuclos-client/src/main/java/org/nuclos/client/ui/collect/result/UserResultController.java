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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.user.UserCollectController;
import org.nuclos.client.ui.collect.component.model.ChoiceEntityFieldList;
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
	
	/**
	 * reads the previously selected fields from the user preferences, ignoring unknown fields that might occur when
	 * the database schema has changed from one software release to another. This method tries to avoid throwing exceptions.
	 * @param clcte
	 * @return List<CollectableEntityField> the previously selected fields from the user preferences.
	 * @see #writeSelectedFieldsToPreferences(List)
	 * TODO: make private?
	 */
	protected List<? extends CollectableEntityField> readSelectedFieldsFromPreferences() {
		final List<CollectableEntityField> result = new ArrayList<CollectableEntityField>();
		for (CollectableEntityField cef : super.readSelectedFieldsFromPreferences()) {
			if (!UserCollectController.FIELD_PREFERENCES.equals(cef.getName()) && !UserCollectController.FIELD_PASSWORD.equals(cef.getName())) {
				result.add(cef);
			}
		}
		return result;
	}

	protected void setSelectColumns(final ChoiceEntityFieldList fields, 
			final SortedSet<CollectableEntityField> lstAvailableObjects, final List<CollectableEntityField> lstSelectedObjects, 
			final Set<CollectableEntityField> stFixedObjects, final boolean restoreWidthsFromPreferences, final Map<String, Integer> mpWidths, final boolean restoreOrder) {
		//just to be sure.
		SortedSet<CollectableEntityField> lst2AvailableObjects = new TreeSet<CollectableEntityField>(new CollectableEntityField.LabelComparator());
		for (CollectableEntityField cef : lstAvailableObjects) {
			if (!UserCollectController.FIELD_PREFERENCES.equals(cef.getName()) && !UserCollectController.FIELD_PASSWORD.equals(cef.getName())) {
				lst2AvailableObjects.add(cef);
			}
		}
		final List<CollectableEntityField> lst2SelectedObjects = new ArrayList<CollectableEntityField>();
		for (CollectableEntityField cef : lstSelectedObjects) {
			if (!UserCollectController.FIELD_PREFERENCES.equals(cef.getName()) && !UserCollectController.FIELD_PASSWORD.equals(cef.getName())) {
				lst2SelectedObjects.add(cef);
			}
		}
		final Set<CollectableEntityField> st2FixedObjects = new HashSet<CollectableEntityField>();
		for (CollectableEntityField cef : stFixedObjects) {
			if (!UserCollectController.FIELD_PREFERENCES.equals(cef.getName()) && !UserCollectController.FIELD_PASSWORD.equals(cef.getName())) {
				stFixedObjects.add(cef);
			}
		}
		super.setSelectColumns(fields, lst2AvailableObjects, lst2SelectedObjects, st2FixedObjects, restoreWidthsFromPreferences, mpWidths, restoreOrder);
	}
}
