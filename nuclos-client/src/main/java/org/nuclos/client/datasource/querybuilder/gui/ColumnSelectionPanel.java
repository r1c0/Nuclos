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
package org.nuclos.client.datasource.querybuilder.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.client.datasource.querybuilder.controller.QueryBuilderController;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.database.query.definition.Table;

/**
 * @todo enter class description.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class ColumnSelectionPanel extends JPanel {

	private class ColumnMouseAdapter extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON3) {
				doPopup(e.getX(), e.getY());
			}
		}
	}

	private class ClearColumnAction extends AbstractAction {

		ClearColumnAction() {
			super(SpringLocaleDelegate.getInstance().getMessage("ColumnSelectionPanel.2","Spalte zur\u00fccksetzen"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			clearColumn();
		}
	}

	private class ClearAllColumnsAction extends AbstractAction {

		ClearAllColumnsAction() {
			super(SpringLocaleDelegate.getInstance().getMessage("ColumnSelectionPanel.1","Alle Spalten zur\u00fccksetzen"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			clearAllColumns();
		}
	}

	private final ColumnSelectionTable table;
	private final ColumnSelectionTableModel model = new ColumnSelectionTableModel();
	private final QueryBuilderController controller;
	private final JPopupMenu popupMenu = new JPopupMenu();
	private int iSelectedColumn = -1;

	/**
	 * @param controller
	 */
	public ColumnSelectionPanel(QueryBuilderController controller) {
		super();
		table = new ColumnSelectionTable(controller);
		setLayout(new BorderLayout());
		this.controller = controller;
		init();
	}

	private void init() {
		table.setModel(model);
		model.removeAllColumns();
		model.fireTableDataChanged();

		final JScrollPane scrlpn = new JScrollPane();
		scrlpn.setRowHeaderView(new LabelPanel(table));
		scrlpn.setViewportView(table);
		add(scrlpn, BorderLayout.CENTER);

		controller.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("Tables")) {
					updateTables(CollectionUtils.typecheck((List<?>) evt.getNewValue(), Table.class));
				}
			}
		});

		final ColumnMouseAdapter mouseAdapter = new ColumnMouseAdapter();
		table.addMouseListener(mouseAdapter);
		table.getTableHeader().addMouseListener(mouseAdapter);

		popupMenu.add(new ClearColumnAction());
		popupMenu.add(new ClearAllColumnsAction());
	}

	/**
	 *
	 * @param tables
	 */
	public void updateTables(List<Table> tables) {
		final List<Table> sortedTables = CollectionUtils.sorted(tables, new Comparator<Table>() {
			@Override
			public int compare(Table t1, Table t2) {
				return t1.getName().compareTo(t2.getName());
			}
		});
		table.updateTables(sortedTables);
	}

	public ColumnSelectionTable getTable() {
		return table;
	}

	public ColumnSelectionTableModel getModel() {
		return model;
	}

	private void doPopup(int iX, int iY) {
		iSelectedColumn = table.getColumnModel().getColumn(table.getColumnModel().getColumnIndexAtX(iX)).getModelIndex();
		popupMenu.show(this, iX, iY);
	}

	private void clearColumn() {
		if (iSelectedColumn >= 0) {
			model.removeColumn(iSelectedColumn);
		}
	}

	private void clearAllColumns() {
		model.removeAllColumns();
	}

	public void cancelEditing() {
		if (table.isEditing()) {
			table.getCellEditor().cancelCellEditing();
		}
	}

	public boolean stopEditing() {
		boolean result = true;

		if (table.isEditing()) {
			result = table.getCellEditor().stopCellEditing();
		}
		return result;
	}

}	// class ColumnSelectionPanel
