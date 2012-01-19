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
package org.nuclos.client.ui.table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.nuclos.client.ui.Icons;

/**
 * @todo enter class description.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version	01.00.00
 */
public class RowIndicatorTable extends JTable {

	public class RowIndicatorCellRenderer extends DefaultTableCellRenderer {

		private ImageIcon icon = (ImageIcon) Icons.getInstance().getIconRowSelection16();
		private JPanel panel = new JPanel();
		private JLabel bgLabel = new JLabel();

		public RowIndicatorCellRenderer() {
			bgLabel.setBackground(Color.LIGHT_GRAY);
			panel.setLayout(new BorderLayout());
			panel.add(bgLabel, BorderLayout.CENTER);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			if (isSelected) {
				bgLabel.setIcon(icon);
			}
			else {
				bgLabel.setIcon(null);
			}
			return panel;
		}
	}

	public class RowIndicatorTableModel extends DefaultTableModel {

		private Object[] rows;

		public void setRows(Object[] oArray) {
			rows = oArray;
			fireTableDataChanged();
		}

		@Override
		public Object getValueAt(int row, int column) {
			return (column < rows.length ? rows[column] : null);
		}

		@Override
		public int getRowCount() {
			return (rows != null ? rows.length : 0);
		}

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

		@Override
		public String getColumnName(int column) {
			return "";
		}
	}

	public static final int COLUMN_SIZE = 20;

	private RowIndicatorTableModel tableModel;
	private JTable externalTable;
	private TableModel externalModel;
	private final TableModelListener internalTableModelListener = new TableModelListener() {
		@Override
		public void tableChanged(TableModelEvent e) {
			synchronizeModel();
			setInternalSelection();
		}
	};

	protected RowIndicatorTable() {
		init();
	}

	protected RowIndicatorTable(TableModel dm, TableColumnModel cm) {
		super(dm, cm);
		init();
	}

	protected RowIndicatorTable(int numRows, int numColumns) {
		super(numRows, numColumns);
		init();
	}

	protected RowIndicatorTable(Object[][] rowData, Object[] columnNames) {
		super(rowData, columnNames);
		init();
	}

	protected RowIndicatorTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
		super(dm, cm, sm);
		init();
	}

	protected RowIndicatorTable(Vector<?> rowData, Vector<?> columnNames) {
		super(rowData, columnNames);
		init();
	}

	protected RowIndicatorTable(TableModel dm) {
		super(dm);
		init();
	}

	public RowIndicatorTable(TableModel externalModel, JTable externalTable) {
		super();
		init();
		setAutoCreateColumnsFromModel(true);
		setModel(externalModel);
		setTable(externalTable);
	}

	public void init() {
		setGridColor(Color.GRAY);
		setShowGrid(true);
	}

	@Override
	public void setModel(TableModel dataModel) {
		externalModel = dataModel;
		tableModel = new RowIndicatorTableModel();
		super.setModel(tableModel);

		if (externalModel != null) {
			externalModel.removeTableModelListener(internalTableModelListener);
			externalModel.addTableModelListener(internalTableModelListener);
			synchronizeModel();
			if (externalTable != null) {
				setInternalSelection();
			}
		}
	}

	public void setTable(JTable table) {
		externalTable = table;
		if (externalTable != null) {
			getSelectionModel().setSelectionMode(externalTable.getSelectionModel().getSelectionMode());
			externalTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
               if (!e.getValueIsAdjusting()) {
                  setInternalSelection();
               }
				}
			});

			getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
               if (!e.getValueIsAdjusting()) {
                  setExternalSelection();
               }
				}
			});
		}
	}

	private void setExternalSelection() {
		int iSelectedRow = getSelectionModel().getMinSelectionIndex();
      if (getSelectionModel().isSelectionEmpty())
      	externalTable.clearSelection();
      else
      	externalTable.setRowSelectionInterval(iSelectedRow, iSelectedRow);
	}

	private void setInternalSelection() {
		int iSelectedRow = externalTable.getSelectionModel().getMinSelectionIndex();
      if (externalTable.getSelectionModel().isSelectionEmpty())
      	clearSelection();
      else
      	setRowSelectionInterval(iSelectedRow, iSelectedRow);
	}

	private void synchronizeModel() {
		int iRowCount = externalModel.getRowCount();
		Object[] oArray = new Object[iRowCount];
		for (int i = 0; i < iRowCount; oArray[i++] = new Object()) {
			;
		}
		tableModel.setRows(oArray);

		RowIndicatorCellRenderer renderer = new RowIndicatorCellRenderer();
		renderer.setBackground(Color.LIGHT_GRAY);
		TableColumn column = getColumnModel().getColumn(0);
		column.setPreferredWidth(COLUMN_SIZE);
		column.setMaxWidth(COLUMN_SIZE);
		column.setCellRenderer(renderer);
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return new Dimension(COLUMN_SIZE, Integer.MAX_VALUE);
	}
}
