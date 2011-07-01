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
package org.nuclos.client.ui.collect;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.table.SortableTableModel;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;

public class ToolTipsTableHeader extends JTableHeader {

	private CollectableEntityFieldBasedTableModel entityTableModel;
	private final Icon ascendingSortIcon;
	private final Icon descendingSortIcon;

	public ToolTipsTableHeader(CollectableEntityFieldBasedTableModel aTableModel, TableColumnModel cm) {
		super(cm);
		this.entityTableModel = aTableModel;
		this.ascendingSortIcon = UIManager.getIcon("Table.ascendingSortIcon");
		this.descendingSortIcon = UIManager.getIcon("Table.descendingSortIcon");
	}

	/**
	 * Used for fixed column selection.
	 * @param externalModel
	 */
	public void setExternalModel(CollectableEntityFieldBasedTableModel externalModel) {
		this.entityTableModel = externalModel;
	}

	@Override
	public String getToolTipText(MouseEvent ev) {
		String result = "";
		final int iIndex = getColumnModel().getColumnIndexAtX(ev.getPoint().x);
		if (iIndex >= 0 && entityTableModel != null) {
			final int iModelColumn = adjustColumnIndex(table.convertColumnIndexToModel(iIndex));
			//final int iModelColumn = getColumnModel().getColumn(iIndex).getModelIndex();
			if(iModelColumn > -1) {
				final CollectableEntityField clctefTarget = entityTableModel.getCollectableEntityField(iModelColumn);
				result = clctefTarget.getDescription();
			}
		}

		return result;
	}

	@Override
	public TableCellRenderer getDefaultRenderer() {
		final TableCellRenderer tcrDefault = ToolTipsTableHeader.super.getDefaultRenderer(); 
		// TODO Synth: Zeile wei\u00df beim ersten mal nicht, das sie sortiert ist
		TableCellRenderer tcr = new TableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				Component comp = tcrDefault.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				
				if(comp instanceof JLabel) {
					List<? extends SortKey> sortKeys = null;
					if (entityTableModel instanceof SortableTableModel) {
						sortKeys = ((SortableTableModel) entityTableModel).getSortKeys();
					}
					
					int sortKeyIndex = -1;
					SortOrder sortOrder = null;
					if (sortKeys != null) {
						final int modelColumn = adjustColumnIndex(table.convertColumnIndexToModel(column));
						sortKeyIndex = CollectionUtils.indexOfFirst(sortKeys, new Predicate<SortKey>() {
							@Override public boolean evaluate(SortKey t) { return t.getColumn() == modelColumn; }
						});
						if (sortKeyIndex != -1) {
							sortOrder = sortKeys.get(sortKeyIndex).getSortOrder();
						}
						
						Icon sortIcon = null;
						if (sortOrder != null) {
							switch (sortOrder) {
							case ASCENDING:
								sortIcon = ascendingSortIcon;
								break;
							case DESCENDING:
								sortIcon = descendingSortIcon;
								break;
							}
							if (sortKeyIndex > 0 && sortIcon != null) {
								sortIcon = new Icons.ResizedIcon(sortIcon, 0.75d);
							}
						}
						((JLabel) comp).setIcon(sortIcon);
					}
				}
				
				return comp;
			}
			
		};
		return tcr;
	}
	
	/**
	 * For synchronizing with the fixed header table etc.  
	 */
	protected int adjustColumnIndex(int iColumn) {
		return iColumn;
	}
}
