package org.nuclos.client.ui.collect;

import java.util.List;

import javax.swing.RowSorter.SortKey;

import org.nuclos.client.layout.admin.LayoutCollectController;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.collect.collectable.CollectableSorting;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;

public class LayoutResultController<Clct extends CollectableMasterDataWithDependants> extends ResultController<Clct> {
	
	public LayoutResultController() {
	}
	
	/**
	 * @deprecated Remove this.
	 */
	@Override
	public List<CollectableSorting> getCollectableSortingSequence() {
		final LayoutCollectController controller = getLayoutCollectController();
		final SortableCollectableTableModel<CollectableMasterDataWithDependants> rtm = controller.getResultTableModel(); 
		List<? extends SortKey> sortKeys = rtm.getSortKeys();
		boolean valid = true;
		for (SortKey sortKey : sortKeys) {
			final String fieldName = rtm.getCollectableEntityField(sortKey.getColumn()).getName();
			if(fieldName.equals("layoutML")){
				Errors.getInstance().showExceptionDialog(controller.getFrame(), CommonLocaleDelegate.getMessage("LayoutCollectController.7","Eine Sortierung nach der Spalte \"LayoutML\" ist nicht durchf\u00fchrbar."), new CommonBusinessException(""));
				rtm.setSortKeys(controller.getLastSortKeys(), false);
				valid = false;
				break;
			}
		}
		if (valid) {
			controller.setLastSortKeys(sortKeys);
		}
		return super.getCollectableSortingSequence();
	}
	
	/**
	 * @deprecated Remove this.
	 */
	@Override
	protected boolean isFieldToBeDisplayedInTable(String sFieldName) {
		return !sFieldName.equals("layoutML");
	}
	
	private LayoutCollectController getLayoutCollectController() {
		return (LayoutCollectController) getCollectController();
	}

}
