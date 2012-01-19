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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.nuclos.client.ui.DefaultSelectObjectsPanel;
import org.nuclos.client.ui.SelectObjectsController;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.component.CollectableComboBox;
import org.nuclos.client.ui.collect.component.CollectableComponentTableCellEditor;
import org.nuclos.client.ui.collect.model.CollectableTableModel;
import org.nuclos.client.ui.model.ChoiceList;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableComparator;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * Controller for inserting/removing multiple rows in a subform.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

class SubFormMultiEditController<Clct extends Collectable> extends SelectObjectsController<CollectableField> {
	private final Logger log = Logger.getLogger(this.getClass());

	private final SubForm subform;
	private final AbstractDetailsSubFormController<Clct> controller;

	SubFormMultiEditController(SubForm subform, AbstractDetailsSubFormController<Clct> ctl) {
		super(subform, new DefaultSelectObjectsPanel<CollectableField>());
		this.subform = subform;
		this.controller = ctl;
	}

	public void run() {
		final CollectableTableModel<Clct> model = (CollectableTableModel<Clct>) this.subform.getJTable().getModel();

		/** @todo remove those optimistic assumptions below */
		final String columnName = this.subform.getUniqueMasterColumnName();
		if (columnName == null) {
			throw new CommonFatalException(CommonLocaleDelegate.getMessage("SubFormMultiEditController.1", "Im Unterformular {0} wurde keine eindeutige Spalte (unique master column) definiert.", subform.getEntityName()));
		}

		final int colIndex = model.findColumnByFieldName(columnName);
		if (colIndex < 0) {
			throw new CommonFatalException(CommonLocaleDelegate.getMessage("SubFormMultiEditController.2", "Im Unterformular {0} ist keine eindeutige Spalte (unique master column) namens {1} vorhanden.", subform.getEntityName(), columnName));
		}

		final int viewColumn = this.subform.getJTable().convertColumnIndexToView(colIndex);
		final CollectableComponentTableCellEditor celleditor = (CollectableComponentTableCellEditor)
				this.subform.getJTable().getCellEditor(0, viewColumn);

		final CollectableComboBox comboBox = (CollectableComboBox) celleditor.getCollectableComponent();

		final Comparator<CollectableField> comp = CollectableComparator.getFieldComparator(comboBox.getEntityField());
		final SortedSet<CollectableField> oldAvailableObjects = getNonNullValues(comboBox, comp);
		final List<CollectableField> oldSelectedObjects = new ArrayList<CollectableField>();
		final Collection<CollectableField> fixed = new ArrayList<CollectableField>();

		// iterate through the table and compute selected fields:
		for (int row = 0; row < model.getRowCount(); ++row) {
			final CollectableField value = model.getValueAt(row, colIndex);

			// int oldAvailableIdx = oldAvailableObjects.indexOf(value);
			if(oldAvailableObjects.contains(value)) {
				oldSelectedObjects.add(value);
				oldAvailableObjects.remove(value);
				log.debug("Value " + value + " removed from available objects.");
			}
			else {
				log.debug("Value " + value + " not found in available objects.");
			}

			if (!controller.isRowRemovable(row)) {
				fixed.add(value);
			}
		}

		// perform the dialog:
		ChoiceList<CollectableField> ro = new ChoiceList<CollectableField>();
		ro.set(oldAvailableObjects, oldSelectedObjects, comp);
		ro.setFixed(fixed);
		setModel(ro);
		final boolean bOK = run(
				CommonLocaleDelegate.getMessage("SubFormMultiEditController.3", "Mehrere Datens\u00e4tze in Unterformular einf\u00fcgen/l\u00f6schen"));

		if (bOK) {
			final List<?> lstNewSelectedObjects = this.getSelectedObjects();

			// 1. iterate through the table model and remove all rows that are not selected:
			for (int iRow = model.getRowCount() - 1; iRow >= 0; --iRow) {
				final Object oValue = model.getValueAt(iRow, colIndex);
				if (!lstNewSelectedObjects.contains(oValue)) {
					model.remove(iRow);
				}
			}

			// 2. iterate through the selected objects and add a row for each that is not contained in the table model already:
			for (Object oSelected : lstNewSelectedObjects) {
				if (!isContainedInTableModel(oSelected, model, colIndex)) {
					// add row
					final Clct clctNew = controller.newCollectable();
					clctNew.setField(columnName, (CollectableField) oSelected);
					model.add(clctNew);
				}
			}
		}
	}

	private static SortedSet<CollectableField> getNonNullValues(CollectableComboBox clctcmbbx, Comparator<CollectableField> comp) {
		final List<CollectableField> result = clctcmbbx.getValueList();

		// remove the first entry, if it is null:
		if (!result.isEmpty()) {
			if (result.get(0).isNull()) {
				result.remove(0);
			}
		}
		final SortedSet<CollectableField> realResult = new TreeSet<CollectableField>(comp);
		realResult.addAll(result);
		return realResult;
	}

	private static <Clct extends Collectable> boolean isContainedInTableModel(Object oSelected, CollectableTableModel<Clct> tblmdl, int iColumn) {
		boolean result = false;
		for (int iRow = 0; iRow < tblmdl.getRowCount(); ++iRow) {
			final Object oValue = tblmdl.getValueAt(iRow, iColumn);
			if (oValue.equals(oSelected)) {
				result = true;
				break;
			}
		}
		return result;
	}

}	// class SubFormMultiEditController
