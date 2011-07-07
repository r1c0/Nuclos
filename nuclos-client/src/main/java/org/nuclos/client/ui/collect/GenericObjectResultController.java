package org.nuclos.client.ui.collect;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

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
import org.nuclos.common2.exception.PreferencesException;

public class GenericObjectResultController<Clct extends CollectableGenericObjectWithDependants> extends ResultController<Clct> {
	
	public GenericObjectResultController() {
		super();
	}

	/**
	 * @param clcte
	 * @return the fields of the given entity, plus the fields of all subentities for that entity.
	 * 
	 * @deprecated Remove this.
	 */
	@Override
	public List<CollectableEntityField> getFieldsAvailableForResult(CollectableEntity clcte) {
		final List<CollectableEntityField> result = super.getFieldsAvailableForResult(clcte);
		final GenericObjectCollectController controller = getGenericObjectCollectController();

		// add parent entity's fields, if any:
		final CollectableEntity clcteParent = controller.getParentEntity();
		if (clcteParent != null)
			result.addAll(CollectionUtils.transform(clcteParent.getFieldNames(), controller.new GetCollectableEntityFieldForResult(clcteParent)));

		// add subentities' fields, if any:
		final Set<String> stSubEntityNames = GenericObjectMetaDataCache.getInstance().getSubFormEntityNamesByModuleId(controller.getModuleId());
		final Set<String> stSubEntityLabels = new HashSet<String>();
		for (String sSubEntityName : stSubEntityNames) {
			final CollectableEntity clcteSub = DefaultCollectableEntityProvider.getInstance().getCollectableEntity(sSubEntityName);
			// WORKAROUND for general search: We don't want duplicate entities (assetcomment, ordercomment etc.), so we
			// ignore entities with duplicate labels:
			/** @todo eliminate this workaround */
			final String sSubEntityLabel = clcteSub.getLabel();
			if (!stSubEntityLabels.contains(sSubEntityLabel)) {
				stSubEntityLabels.add(sSubEntityLabel);
				result.addAll(CollectionUtils.transform(clcteSub.getFieldNames(), controller.new GetCollectableEntityFieldForResult(clcteSub)));
			}
		}
		return result;
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
	public CollectableEntityField getCollectableEntityFieldForResult(org.nuclos.common.collect.collectable.CollectableEntity sClcte, String sFieldName) {
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
	 * @todo eliminate this workaround
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
	
	private GenericObjectCollectController getGenericObjectCollectController() {
		return (GenericObjectCollectController) getCollectController();
	}

}
