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

import java.util.List;

import javax.swing.RowSorter.SortKey;

import org.nuclos.client.layout.admin.LayoutCollectController;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.collect.collectable.CollectableSorting;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * A specialization of ResultController for use with an {@link LayoutCollectController}.
 * 
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 */
public class LayoutResultController<Clct extends CollectableMasterDataWithDependants> extends NuclosResultController<Clct> {
	
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
