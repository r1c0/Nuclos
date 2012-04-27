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
package org.nuclos.client.common;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.nuclos.client.entityobject.CollectableEntityObject;
import org.nuclos.client.masterdata.CollectableWithDependants;
import org.nuclos.client.ui.model.AbstractListTableModel;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;

@SuppressWarnings("serial")
public class SubFormCollectableMap extends HashMap<CollectableEntityObject, Map<Long, CollectableEntityObject>> implements TableModelListener {

	private String sSubEntityName;
	private String sParentFieldName;
	private SubFormController subformctl;
	private Collection<? extends CollectableWithDependants> collclct;
	private AbstractListTableModel<Collectable> tableModel;
	private Collection<String> combination;
	
	@SuppressWarnings("unchecked")
	public SubFormCollectableMap(List<Set<CollectableEntityObject>> equivalenceClasses, String sSubEntityName, String sParentFieldName, Collection<? extends CollectableWithDependants> collclct, SubFormController subformctl, Collection<String> combination) {
		super();
		this.sSubEntityName = sSubEntityName;
		this.sParentFieldName = sParentFieldName;
		this.collclct = collclct;
		this.subformctl = subformctl;
		this.combination = combination;
		
		tableModel = (AbstractListTableModel<Collectable>) subformctl.getJTable().getModel();
		tableModel.addTableModelListener(this);

		// compute the prototype for each equivalenceClass (= set of data with
		// the same key)
		for (Set<CollectableEntityObject> equivalenceClass : equivalenceClasses) {
			CollectableEntityObject prototype = computePrototype(equivalenceClass);
			Map<Long, CollectableEntityObject> idToCollectableMap = new HashMap<Long, CollectableEntityObject>();
			for (CollectableEntityObject collectable : equivalenceClass) {
				idToCollectableMap.put((Long) collectable.getField(sParentFieldName).getValueId(), collectable);
			}
			put(prototype, idToCollectableMap);
		}
	}

	// compute the prototype for each equivalenceClass (= set of data with the
	// same key)
	private CollectableEntityObject computePrototype(Set<CollectableEntityObject> equivalenceClass) {
		Collection<String> nonUniqueFields = getNonUniqueFieldsWithoutParent(sSubEntityName, sParentFieldName);

		CollectableEntityObject firstEntry = equivalenceClass.iterator().next();
		CollectableEntityObject representative = new CollectableEntityObject(firstEntry.getCollectableEntity(), firstEntry.getEntityObjectVO().copy());

		for (CollectableEntityObject ceo : equivalenceClass) {
			for (String field : nonUniqueFields) {
				CollectableField representativeField = representative.getField(field);
				CollectableField currentField = ceo.getField(field);
				if (!representativeField.equals(currentField)) {
					representative.setField(field, new CollectableValueField(null));
				}
			}
		}
		return representative;
	}
	
	private Collection<String> getNonUniqueFieldsWithoutParent(String sSubEntityName, String sParentFieldName) {
		Collection<String> result = new Vector<String>();
		for (String fieldName : MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(sSubEntityName).keySet()) {
			if (NuclosEOField.getByField(fieldName) != null || sParentFieldName.equals(fieldName) || combination.contains(fieldName)) {
				continue;
			}
			result.add(fieldName);
		}
		return result;
	}

	// transfer the data in the given field from the prototype to its
	// equivalence class
	public void transferField(CollectableEntityObject prototype, String fieldName) {
		Collection<CollectableEntityObject> equivalenceClass = get(prototype).values();

		for (CollectableEntityObject object : equivalenceClass) {
			object.setField(fieldName, prototype.getField(fieldName));
		}
	}

	// add a new prototype to the mapping. The mapping will contain a copy of
	// the prototype for
	// each parent collectable
	public void addPrototype(CollectableEntityObject prototype) {
		Map<Long, CollectableEntityObject> idToCollectableMap = new HashMap<Long, CollectableEntityObject>();
		for (CollectableWithDependants clctWithDependants : collclct) {
			CollectableEntityObject object = new CollectableEntityObject(prototype.getCollectableEntity(), prototype.getEntityObjectVO().copy());
			idToCollectableMap.put(new Long((Integer) clctWithDependants.getId()), object);
		}
		put(prototype, idToCollectableMap);
	}

	// mark all data associated with the prototype as removed
	public void removePrototype(CollectableEntityObject prototype) {
		Collection<CollectableEntityObject> equivalenceClass = get(prototype).values();
		for (CollectableEntityObject object : equivalenceClass) {
			object.markRemoved();
		}

	}

	// transfer the data of the prototyp to all parent collectables. If a parent
	// already has the
	// data, it will be changed. If it does not, the data will be created.
	public void transferDataToAllEntities(CollectableEntityObject representative) {
		Map<Long, CollectableEntityObject> map = get(representative);
		for (CollectableWithDependants clctWithDependants : collclct) {
			Long parentId = new Long((Integer) clctWithDependants.getId());
			CollectableEntityObject object = map.get(parentId);
			if (object == null) {
				map.put(parentId, copyCollectableEO(representative));
			}
			else {
				copyAllFields(representative, object);
			}
		}
	}

	private void copyAllFields(CollectableEntityObject source, CollectableEntityObject target) {
		for (String fieldName : MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(sSubEntityName).keySet()) {
			if (NuclosEOField.getByField(fieldName) != null || sParentFieldName.equals(fieldName)) {
				continue;
			}
			target.setField(fieldName, source.getField(fieldName));
		}
	}

	private CollectableEntityObject copyCollectableEO(CollectableEntityObject original) {
		return new CollectableEntityObject(original.getCollectableEntity(), original.getEntityObjectVO().copy());
	}

	public Collectable getPrototype(int row) {
		return tableModel.getRow(row);
	}

	/**
	 * Checks whether all parent collectables have data in the given row
	 * @param row
	 * @return
	 */
	public boolean allEntitiesHaveDataInRow(int row) {
		Collectable collectable = tableModel.getRow(row);
		Collection<CollectableEntityObject> equivalenceClass = get(collectable).values();
		return equivalenceClass.size() == collclct.size();
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		int row = e.getFirstRow();
		int column = e.getColumn();

		if (e.getType() == TableModelEvent.UPDATE && column != -1) {
			// change existing cell
			String field = subformctl.getTableColumns().get(column).getName();
			CollectableEntityObject collectable = (CollectableEntityObject) tableModel.getRow(row);
			transferField(collectable, field);
		}
		else if (e.getType() == TableModelEvent.INSERT && column == -1) {
			// insert new row
			CollectableEntityObject representative = (CollectableEntityObject) tableModel.getRow(row);
			addPrototype(representative);
		}
		else if (e.getType() == TableModelEvent.DELETE && e.getColumn() == -1) {
			// delete a row
			// The table model has already been changed. So we cannot find the
			// deleted Collectable in it. Hence, we
			// search through the table. The deleted Collectables are the ones
			// which are still in the map but not in the
			// table anymore.
			Set<CollectableEntityObject> representatives = new HashSet<CollectableEntityObject>(keySet());
			for (int currentRow = 0; currentRow < subformctl.getJTable().getRowCount(); currentRow++) {
				CollectableEntityObject representative = (CollectableEntityObject) tableModel.getRow(currentRow);
				representatives.remove(representative);
			}
			for (CollectableEntityObject representative : representatives) {
				removePrototype(representative);
			}
		}
	}

	public SubFormController getSubformController() {
		return subformctl;
	}

	public Collection<CollectableWithDependants> getParentCollectables() {
		return Collections.unmodifiableCollection(collclct);
	}
	
	public void close() {
		tableModel.removeTableModelListener(this);
	}
}
