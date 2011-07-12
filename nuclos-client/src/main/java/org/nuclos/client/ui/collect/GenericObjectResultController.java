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
package org.nuclos.client.ui.collect;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.swing.JTable;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import org.nuclos.client.data.pivot.PivotInfo;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.client.genericobject.GenericObjectClientUtils;
import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.genericobject.GenericObjectMetaDataCache;
import org.nuclos.common.CollectableEntityFieldWithEntity;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableSorting;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.exception.PreferencesException;

/**
 * A specialization of ResultController for use with an {@link GenericObjectCollectController}.
 * <p>
 * At present the feature to include rows from a subform in the base entity result list 
 * is only available for GenericObjects. The support for finding the support fields is 
 * implemented in {@link #getFieldsAvailableForResult}. 
 * </p>
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 */
public class GenericObjectResultController<Clct extends CollectableGenericObjectWithDependants> extends NuclosResultController<Clct> {
	
	/**
	 * subform name -> pivot information.
	 * <p>
	 * If a subform is included in this list, {@link #getFieldsAvailableForResult} will provide a
	 * pivot representation instead of a subform field representation.
	 * </p> 
	 */
	private final Map<String,PivotInfo> pivots;
	
	public GenericObjectResultController(CollectableEntity clcte) {
		super(clcte);
		pivots = new HashMap<String, PivotInfo>();
	}
	
	public void putPivotInfo(PivotInfo info) {
		pivots.put(info.getSubform(), info);
	}
	
	public PivotInfo getPivotInfo(String subformName) {
		return pivots.get(subformName);
	}
	
	public void clearPivotInfo() {
		pivots.clear();
	}

	private final class GetCollectableEntityFieldForResult implements Transformer<String, CollectableEntityField> {
		private final CollectableEntity clcte;

		public GetCollectableEntityFieldForResult(CollectableEntity clcte) {
			this.clcte = clcte;
		}

		@Override
		public CollectableEntityField transform(String sFieldName) {
			return getCollectableEntityFieldForResult(clcte, sFieldName);
		}
	}

	/**
	 * This methods adds (pivot, subform, and parent key) field inclusion to result display.
	 * 
	 * @param clcte
	 * @return the fields of the given entity, plus the fields of all subentities for that entity.
	 * 
	 * @deprecated Remove this.
	 */
	@Override
	public SortedSet<CollectableEntityField> getFieldsAvailableForResult(CollectableEntity clcte, Comparator<CollectableEntityField> comp) {
		final SortedSet<CollectableEntityField> result = super.getFieldsAvailableForResult(clcte, comp);
		final GenericObjectCollectController controller = getGenericObjectCollectController();

		// add parent entity's fields, if any:
		final CollectableEntity clcteParent = controller.getParentEntity();
		if (clcteParent != null)
			result.addAll(CollectionUtils.transform(clcteParent.getFieldNames(), new GetCollectableEntityFieldForResult(clcteParent)));

		// add subentities' fields, if any:
		final Set<String> stSubEntityNames = GenericObjectMetaDataCache.getInstance().getSubFormEntityNamesByModuleId(controller.getModuleId());
		final Set<String> stSubEntityLabels = new HashSet<String>();
		for (String sSubEntityName : stSubEntityNames) {
			if (pivots.containsKey(sSubEntityName)) {
				getFieldsAvaibleInPivotSubform(result, stSubEntityLabels, sSubEntityName);
			}
			else {
				getFieldsAvaibleInSubform(result, stSubEntityLabels, sSubEntityName);
			}
		}
		return result;
	}
	
	private void getFieldsAvaibleInSubform(SortedSet<CollectableEntityField> result, Set<String> stSubEntityLabels, String sSubEntityName) {
		final CollectableEntity clcteSub = DefaultCollectableEntityProvider.getInstance().getCollectableEntity(sSubEntityName);
		// WORKAROUND for general search: We don't want duplicate entities (assetcomment, ordercomment etc.), so we
		// ignore entities with duplicate labels:
		// TODO: eliminate this workaround 
		final String sSubEntityLabel = clcteSub.getLabel();
		if (!stSubEntityLabels.contains(sSubEntityLabel)) {
			stSubEntityLabels.add(sSubEntityLabel);
			result.addAll(CollectionUtils.transform(clcteSub.getFieldNames(), new GetCollectableEntityFieldForResult(clcteSub)));
		}
	}

	private void getFieldsAvaibleInPivotSubform(SortedSet<CollectableEntityField> result, Set<String> stSubEntityLabels, String sSubEntityName) {
		// remove the subform entries
		// TODO: make sth sensible here!
	}
	
	/**
	 * reads the selected fields and their entities from the user preferences.
	 * @param clcte
	 * @return the list of previously selected fields
	 * @deprecated Remove this.
	 */
	@Override
	protected List<CollectableEntityFieldWithEntity> readSelectedFieldsFromPreferences(CollectableEntity clcte) {
		return GenericObjectClientUtils.readCollectableEntityFieldsFromPreferences(
				getGenericObjectCollectController().getPreferences(), clcte, 
				CollectController.PREFS_NODE_SELECTEDFIELDS, CollectController.PREFS_NODE_SELECTEDFIELDENTITIES);
	}

	/**
	 * writes the selected fields and their entities to the user preferences.
	 * @param lstclctefweSelected
	 * @throws PreferencesException
	 * @deprecated Remove this.
	 */
	@Override
	public void writeSelectedFieldsToPreferences(List<? extends CollectableEntityField> lstclctefweSelected) throws PreferencesException {
		GenericObjectClientUtils.writeCollectableEntityFieldsToPreferences(
				getGenericObjectCollectController().getPreferences(), 
				CollectionUtils.typecheck(lstclctefweSelected, CollectableEntityFieldWithEntity.class), 
				CollectController.PREFS_NODE_SELECTEDFIELDS, CollectController.PREFS_NODE_SELECTEDFIELDENTITIES);
		super.writeSelectedFieldsToPreferences(lstclctefweSelected);
	}

	/**
	 * We need to return a <code>CollectableEntityFieldWithEntity</code> here so we can filter by entity.
	 * @param clcte
	 * @param sFieldName
	 * @return a <code>CollectableEntityField</code> of the given entity with the given field name, to be used in the Result metadata.
	 * 
	 * @deprecated Remove this.
	 */
	@Override
	public CollectableEntityField getCollectableEntityFieldForResult(CollectableEntity sClcte, String sFieldName) {
		final GenericObjectCollectController controller = getGenericObjectCollectController();
		final CollectableEntity ce = controller.getCollectableEntity();
		CollectableEntity clcte = sClcte;
		CollectableEntityFieldWithEntity.QualifiedEntityFieldName qFieldName = new CollectableEntityFieldWithEntity.QualifiedEntityFieldName(sFieldName);
		if(qFieldName.isQualifiedEntityFieldName()){
			String clcteName = qFieldName.getEntityName();
			if(clcteName != null && !clcteName.equals(ce.getName()))
				clcte = DefaultCollectableEntityProvider.getInstance().getCollectableEntity(clcteName);
		}
		return GenericObjectClientUtils.getCollectableEntityFieldForResult(clcte, qFieldName.getFieldName(), ce);
	}
	
	/**
	 * sets all column widths to user preferences; set optimal width if no preferences yet saved
	 * Unfinalized in the CollectController
	 * @param tbl
	 * @deprecated Remove this.
	 */
	@Override
	public void setColumnWidths(final JTable tbl) {
		final GenericObjectCollectController controller = getGenericObjectCollectController();
		if(controller.getSearchResultTemplateController() == null || controller.getSearchResultTemplateController().isSelectedDefaultSearchResultTemplate()) {
			super.setColumnWidths(tbl);
		}
	}


	/**
	 * TODO: eliminate this workaround
	 * @deprecated Remove this.
	 */
	public List<CollectableSorting> getCollectableSortingSequence() {
		final GenericObjectCollectController controller = getGenericObjectCollectController();
		final List<CollectableSorting> result = new LinkedList<CollectableSorting>();
		SortableCollectableTableModel<CollectableGenericObjectWithDependants> tm = controller.getResultTableModel();
		for (SortKey sortKey :  tm.getSortKeys()) {
			final CollectableEntityFieldWithEntity clctefwe = (CollectableEntityFieldWithEntity) tm.getCollectableEntityField(sortKey.getColumn());
			if (clctefwe.getCollectableEntityName().equals(controller.getCollectableEntity().getName())) {
				final String fieldName = clctefwe.getName();
				result.add(new CollectableSorting(fieldName, sortKey.getSortOrder() == SortOrder.ASCENDING));
			}
		}
		return result;
	}
	
	public GenericObjectCollectController getGenericObjectCollectController() {
		return (GenericObjectCollectController) getCollectController();
	}

}
