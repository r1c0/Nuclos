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
import java.awt.Component;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ToolTipManager;
import javax.swing.table.TableCellRenderer;

import org.nuclos.client.entityobject.CollectableEntityObject;
import org.nuclos.client.ui.collect.model.CollectableTableModel;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;

public class SubFormTableCellRenderer implements TableCellRenderer {

	private SubFormCollectableMap map;

	public SubFormTableCellRenderer(SubFormCollectableMap map) {
		this.map = map;
	}

	@SuppressWarnings("unchecked")
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		CollectableTableModel<Collectable> tableModel = (CollectableTableModel<Collectable>) map.getSubformController().getJTable().getModel();
		CollectableEntityField cef = tableModel.getCollectableEntityField(column);

		JComponent c = (JComponent) map.getSubformController().getTableCellRenderer(cef).getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		String field = getFieldNameForColumn(column);

		Collectable prototype = tableModel.getRow(row);

		Collection<CollectableEntityObject> equivalenceClass = map.get(prototype).values();

		if (MetaDataClientProvider.getInstance().getEntityField(map.getSubformController().getEntityAndForeignKeyFieldName().getEntityName(), field).isUnique()) {
			// unique field: just check if all parent collectables have this row
			if (equivalenceClass.size() != map.getParentCollectables().size()) {
				c.setBackground(Color.LIGHT_GRAY);
			}
			else {
				c.setBackground(Color.WHITE);
			}
			c.setToolTipText(buildToolTipText(equivalenceClass, null, column));
		}
		else {
			// non-unique field: compute how often each value occurs
			Map<CollectableField, Integer> multiplicities = computeMultiplicitiesOfValues(equivalenceClass, field);
			if (multiplicities.size() <= 1) {
				c.setBackground(Color.WHITE);
			}
			else {
				c.setBackground(MultiUpdateOfDependants.COLOR_NO_COMMON_VALUES);
			}
			c.setToolTipText(buildToolTipText(equivalenceClass, multiplicities, column));
		}
		c.setForeground(Color.BLACK);
		ToolTipManager.sharedInstance().setDismissDelay(10000);
		ToolTipManager.sharedInstance().registerComponent(table);
		return c;
	}

	// Compute how often each value in the given field occurs
	private Map<CollectableField, Integer> computeMultiplicitiesOfValues(Collection<CollectableEntityObject> equivalenceClass, String field) {

		Map<CollectableField, Integer> multiplicities = new HashMap<CollectableField, Integer>();
		for (CollectableEntityObject ceo : equivalenceClass) {
			CollectableField currentField = ceo.getField(field);
			Integer count = multiplicities.get(currentField);
			if (count == null) {
				multiplicities.put(currentField, 1);
			}
			else {
				multiplicities.put(currentField, count + 1);
			}
		}
		return multiplicities;
	}

	private String buildToolTipText(Collection<CollectableEntityObject> equivalenceClass, Map<CollectableField, Integer> multiplicities, int column) {
		String toolTipText = "<html>";

		Integer size = map.getParentCollectables().size();
		if (equivalenceClass.size() != size) {
			toolTipText += "Die Zeile ist in " + equivalenceClass.size() + " von " + size + " Entitäten vorhanden.<br>";
		}
		else {
			toolTipText += "Die Zeile ist in allen " + size + " Entitäten vorhanden.<br>";
		}

		if (multiplicities != null) {
			if (multiplicities.size() == 0) {
				toolTipText += "Keine dieser Entitäten einhält einen Wert im Feld \"" + (String) map.getSubformController().getJTable().getColumnModel().getColumn(column).getHeaderValue() + "\".";
			}
			else if (multiplicities.size() == 1) {
				toolTipText += "Alle diese Entitäten einhalten den Wert \"" + multiplicities.entrySet().iterator().next().getKey() + "\" im Feld \""
						+ (String) map.getSubformController().getJTable().getColumnModel().getColumn(column).getHeaderValue() + "\".";
			}
			else {
				toolTipText += "Die darin enthaltenen Werte für \"" + (String) map.getSubformController().getJTable().getColumnModel().getColumn(column).getHeaderValue() + "\" sind:<br>";

				List<Entry<CollectableField, Integer>> entries = new LinkedList<Entry<CollectableField, Integer>>(multiplicities.entrySet());
				Collections.sort(entries, new Comparator<Entry<CollectableField, Integer>>() {

					@Override
					public int compare(Entry<CollectableField, Integer> o1, Entry<CollectableField, Integer> o2) {
						// Sort values which occur often to the start of the
						// list
						return -o1.getValue().compareTo(o2.getValue());
					}
				});

				for (Map.Entry<CollectableField, Integer> entry : entries) {
					toolTipText += entry.getKey() + " (" + entry.getValue() + "x)<br>";
				}
			}
		}
		toolTipText += "</html>";

		return toolTipText;
	}

	private String getFieldNameForColumn(int column) {
		return map.getSubformController().getTableColumns().get(column).getName();
	}

}
