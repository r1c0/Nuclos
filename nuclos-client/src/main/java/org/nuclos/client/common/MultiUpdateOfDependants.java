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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.table.TableCellRenderer;

import org.nuclos.client.entityobject.CollectableEOEntityClientProvider;
import org.nuclos.client.entityobject.CollectableEntityObject;
import org.nuclos.client.masterdata.CollectableWithDependants;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.table.TableCellRendererProvider;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collection.BinaryPredicate;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.SymmetricBinaryPredicate;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.entityobject.CollectableEOEntity;
import org.nuclos.common.entityobject.CollectableEOEntityProvider;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;

/**
 * Provides support for multi-update of dependant masterdata. <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class MultiUpdateOfDependants implements TableCellRendererProvider {

	private final Map<String, SubFormCollectableMap> subFormCollectableMaps;

	/**
	 * the color to be used as background for multi editable subforms that don't
	 * share a common value.
	 */
	public final static Color COLOR_NO_COMMON_VALUES = Utils.translateColorFromParameter(ParameterProvider.KEY_HISTORICAL_STATE_CHANGED_COLOR);// new
																																				// Color(246,229,255);

	/**
	 * initiates the multi-editing of dependants. Calculates the objects that
	 * are common in the dependants of all the given collectables and puts these
	 * in the given subform controllers, for each subentity.
	 * 
	 * @param collsubformctl
	 * @param collclctwd
	 */
	public MultiUpdateOfDependants(Collection<? extends DetailsSubFormController<CollectableEntityObject>> collsubformctl, Collection<? extends CollectableWithDependants> collclctwd) {
		subFormCollectableMaps = prepareSubFormsForMultiEdit(collsubformctl, collclctwd);
	}

	/**
	 * @param collsubformctl
	 *            the subform controllers containing the data entered by the
	 *            user during multi-edit.
	 * @param clctwd
	 *            the collectable to update
	 * @return the dependant collectables needed for update of the given
	 *         collectable
	 * @todo we should return a DependantMasterDataMap here
	 */
	public DependantCollectableMasterDataMap getDependantCollectableMapForUpdate(Collection<? extends DetailsSubFormController<CollectableEntityObject>> collsubformctl, CollectableWithDependants clctwd) {
		final DependantMasterDataMap mpDependants = new DependantMasterDataMap();

		// iterate all enabled subforms:
		for (DetailsSubFormController<CollectableEntityObject> subformctl : collsubformctl) {
			if (subformctl.getSubForm().isEnabled()) {

				final String sSubEntityName = subformctl.getSubForm().getEntityName();
				final String sForeignKeyFieldName = subformctl.getForeignKeyFieldName();
				final Object oParentId = clctwd.getId();

				String parentSubform = subformctl.getSubForm().getParentSubForm();

				// special handling for child subforms
				if (!StringUtils.isNullOrEmpty(parentSubform)) {
					// to be done
					continue;
				}

				Collection<EntityObjectVO> collresult = new Vector<EntityObjectVO>();

				SubFormCollectableMap map = subFormCollectableMaps.get(sSubEntityName);
				for (Map<Long, CollectableEntityObject> mapForCurrentPrototype : map.values()) {
					CollectableEntityObject dependantData = mapForCurrentPrototype.get(new Long((Integer) oParentId));
					if (dependantData != null) {
						collresult.add(dependantData.getEntityObjectVO());
					}
				}

				// 7. Correct the parent ids of those objects:
				setParentIds(collresult, sForeignKeyFieldName, oParentId);

				// and put them to the result:
				mpDependants.addAllData(sSubEntityName, collresult);
			}
		}

		// LOCC.updateCollectable expects a DependantCollectableMap:
		return new DependantCollectableMasterDataMap(mpDependants);
	}

	/**
	 * prepares the subforms for multi edit. Creates the id mapping for multi
	 * edit.
	 * 
	 * @param collsubformctl
	 * @param collclctwd
	 * @return the id mapping for multi edit, which is needed when updating is
	 *         started.
	 */
	private Map<String, SubFormCollectableMap> prepareSubFormsForMultiEdit(Collection<? extends DetailsSubFormController<CollectableEntityObject>> collsubformctl, Collection<? extends CollectableWithDependants> collclctwd) {
		final Map<String, SubFormCollectableMap> result = new HashMap<String, SubFormCollectableMap>();
		for (DetailsSubFormController<CollectableEntityObject> subformctl : collsubformctl) {
			subformctl.clear();
			subformctl.setMultiUpdateOfDependants(this);
			final String sSubEntityName = subformctl.getSubForm().getEntityName();
			final String sParentFieldName = subformctl.getForeignKeyFieldName();

			String parentSubform = subformctl.getSubForm().getParentSubForm();

			final Collection<CollectableEntityObject> collclctCommon;
			// load data of subforms of the first hierarchy
			if (StringUtils.isNullOrEmpty(parentSubform)) {
				SubFormCollectableMap subFormCollectableMap = getCommonSubCollectables(collclctwd, sSubEntityName, sParentFieldName, subformctl);
				result.put(sSubEntityName, subFormCollectableMap);
				collclctCommon = subFormCollectableMap.keySet();
			}
			// load data of child subforms
			else {
				// to be done
				continue;
			}

			if (areDependantsEmpty(collclctwd, sSubEntityName, sParentFieldName) || !collclctCommon.isEmpty()) {
				/**
				 * @todo try to use MasterDataSubFormController.fillSubForm
				 *       instead
				 */
				subformctl.updateTableModel(new ArrayList<CollectableEntityObject>(collclctCommon));
				subformctl.getSubForm().getJTable().setBackground(null);
			}
			else {
				subformctl.getSubForm().getJTable().setBackground(COLOR_NO_COMMON_VALUES);
			}
		}
		return result;
	}

	/**
	 * @param collclct
	 * @param sSubEntityName
	 * @return dependant data records of the given sub entity that are identical
	 *         for all given Collectables
	 */
	private static SubFormCollectableMap getCommonSubCollectables(Collection<? extends CollectableWithDependants> collclct, final String sSubEntityName, final String sParentFieldName,
			DetailsSubFormController<CollectableEntityObject> subformctl) {

		// compare all fields except the parent field:
		Collection<CollectableEntityObject> dependants = new ArrayList<CollectableEntityObject>();

		// reload subform data from the database instead of using the tablemodel
		// data like it was made before
		for (CollectableWithDependants clctWithDependants : collclct) {
			final Collection<EntityObjectVO> collmdcvo = (clctWithDependants.getId() == null) ? new ArrayList<EntityObjectVO>() : MasterDataDelegate.getInstance().getDependantMasterData(sSubEntityName, sParentFieldName,
					clctWithDependants.getId(), subformctl.getSubForm().getMapParams());
			CollectableEOEntityProvider provider = CollectableEOEntityClientProvider.getInstance();
			CollectableEOEntity eo = (CollectableEOEntity) provider.getCollectableEntity(sSubEntityName);
			List<CollectableEntityObject> list = CollectionUtils.transform(collmdcvo, new CollectableEntityObject.MakeCollectable(eo));
			dependants.addAll(list);
		}

		Collection<String> combination = new ArrayList<String>();

		// determine field combination
		EntityMetaDataVO entity = MetaDataClientProvider.getInstance().getEntity(sSubEntityName);
		Map<String, EntityFieldMetaDataVO> entityfields = MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(sSubEntityName); 
		
		if (!StringUtils.isNullOrEmpty(entity.getFieldsForEquality())) {
			for (String field : entity.getFieldsForEquality().split(",")) {
				if (entityfields.containsKey(field)) {
					combination.add(field);
				}
			}
		}
		
		if (combination.size() == 0) {
			for (EntityFieldMetaDataVO field : entityfields.values()) {
				if (field.isUnique()) {
					combination.add(field.getField());
				}
			}
		}
		
		if (combination.size() == 0) {
			combination.addAll(entityfields.keySet());
		}
		
		CollectionUtils.retainAll(combination, new Predicate<String>() {
			@Override
			public boolean evaluate(String t) {
				if (NuclosEOField.getByField(t) != null) {
					return false;
				}
				else if (sParentFieldName.equals(t)) {
					return false;
				}
				return true;
			}
		});
		
		List<Set<CollectableEntityObject>> equivalenceClasses = getEquivalenceClasses(dependants, new AreFieldsEqual(combination));
		return new SubFormCollectableMap(equivalenceClasses, sSubEntityName, sParentFieldName, collclct, subformctl, combination);
	}

	// builds the equivalence classes of the given set
	private static <E1, E extends E1> List<Set<E>> getEquivalenceClasses(Collection<? extends E> collection, BinaryPredicate<E1, E1> predicateEquals) {
		final List<Set<E>> result = new LinkedList<Set<E>>();

		for (E e : collection) {
			boolean equivalenceClassFound = false;
			for (Set<E> set : result) {
				if (CollectionUtils.contains(set, e, predicateEquals)) {
					set.add(e);
					equivalenceClassFound = true;
					break;
				}
			}
			if (!equivalenceClassFound) {
				Set<E> newEquivalenceClass = new HashSet<E>();
				newEquivalenceClass.add(e);
				result.add(newEquivalenceClass);
			}
		}
		return result;
	}

	private static boolean areDependantsEmpty(Collection<? extends CollectableWithDependants> collclct, String sSubEntityName, String sParentFieldName) {

		List<EntityObjectVO> dependants = new ArrayList<EntityObjectVO>();

		for (CollectableWithDependants clctWithDependants : collclct) {
			if (clctWithDependants.getId() != null)
				dependants.addAll(MasterDataDelegate.getInstance().getDependantMasterData(sSubEntityName, sParentFieldName, clctWithDependants.getId()));
		}
		return dependants.isEmpty();

	}

	private static void setParentIds(Collection<EntityObjectVO> collmdvo, String sForeignKeyFieldName, Object iParentId) {
		for (EntityObjectVO mdvo : collmdvo) {
			mdvo.getFields().put(sForeignKeyFieldName + "Id", iParentId);

			if (iParentId instanceof Integer) {
				Long id = new Long((Integer) iParentId);
				mdvo.getFieldIds().put(sForeignKeyFieldName, id);
			}
			else {
				mdvo.getFieldIds().put(sForeignKeyFieldName, (Long) iParentId);
			}
		}
	}

	private static class AreFieldsEqual implements SymmetricBinaryPredicate<Collectable> {
		private Collection<String> collFieldNames;

		AreFieldsEqual(Collection<String> collFieldNames) {
			this.collFieldNames = collFieldNames;
		}

		@Override
		public boolean evaluate(Collectable clct1, Collectable clct2) {
			for (String sFieldName : collFieldNames) {
				if (!clct1.getField(sFieldName).equals(clct2.getField(sFieldName))) {
					return false;
				}
			}
			return true;
		}

	} // inner class AreFieldsEqual

	@Override
	public TableCellRenderer getTableCellRenderer(CollectableEntityField clctefTarget) {
		String entity = clctefTarget.getEntityName();
		SubFormCollectableMap map = subFormCollectableMaps.get(entity);
		return new SubFormTableCellRenderer(map);
	}

	public void transfer(SubForm sf) {
		int[] selectedRows = sf.getJTable().getSelectedRows();
		SubFormCollectableMap map = subFormCollectableMaps.get(sf.getEntityName());
		for (int row : selectedRows) {
			if (!map.allEntitiesHaveDataInRow(row)) {
				CollectableEntityObject prototype = (CollectableEntityObject) map.getPrototype(row);
				map.transferDataToAllEntities(prototype);
			}
		}
		sf.getJTable().repaint();
		sf.fireStateChanged();
	}

	public boolean isTransferPossible(SubForm sf) {
		if (sf.isEnabled()) {
			SubFormCollectableMap map = subFormCollectableMaps.get(sf.getEntityName());
			int[] selectedRows = sf.getJTable().getSelectedRows();
			for (int row : selectedRows) {
				if (!map.allEntitiesHaveDataInRow(row)) {
					return true;
				}
			}
		}
		return false;
	}

	public void close() {
		for (SubFormCollectableMap map : subFormCollectableMaps.values()) {
			map.close();
		}
	}
} // class MultiUpdateOfDependants
