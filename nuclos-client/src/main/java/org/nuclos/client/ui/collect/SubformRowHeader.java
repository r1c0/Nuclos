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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Closeable;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;
import org.nuclos.client.ui.Icons;

/**
 * Header for the rows of an SubForm.SubFormTable.
 * Displays an RowIndicator column in front o each row
 * @author <a href="mailto:rainer.schneider@novabit.de">rainer.schneider</a>
 */
public class SubformRowHeader implements Closeable {
	
	private static final Logger LOG = Logger.getLogger(SubformRowHeader.class);

	public static final int COLUMN_SIZE = 20;
	
	// 

	/** table which display the header columns */
	private final JTable headerTable;

	private JScrollPane scrlpnOriginalTable;

	/** table to which the header is added */
	private SubForm.SubFormTable externalTable;

	/** listener to synchronize the header table with the external table */
	private final TableModelListener externalTableModelListener;
	
	private boolean closed = false;
	
	/**
	 * initializes the header table and the listeners
	 */
	public SubformRowHeader() {
		super();
		headerTable = createHeaderTable();
		initializeHeaderTable();

		externalTableModelListener = new TableModelListener() {
			@Override
            public void tableChanged(TableModelEvent event) {
				// update model on events wich change  the structure of the table
				if(event.getType() != TableModelEvent.UPDATE
						|| event.getFirstRow() != event.getLastRow())
					synchronizeModel();
			}
		};
	}

	/**
	 * initializes the header table and the listeners, sets the used external table and its Scrollpane
	 * @see #setExternalTable(SubForm.SubFormTable, JScrollPane)
	 * @param tbl
	 * @param scrlpnOriginalTable
	 */
	public SubformRowHeader(SubForm.SubFormTable tbl, JScrollPane scrlpnOriginalTable) {
		this();
		setExternalTable(tbl, scrlpnOriginalTable);
	}
	
	@Override
	public void close() {
		// Close is needed for avoiding memory leaks
		// If you want to change something here, please consult me (tp). 
		if (!closed) {
			LOG.info("close(): " + this);
			// headerTable = null;
			scrlpnOriginalTable = null;
			externalTable = null;
			// externalTableModelListener = null;
			closed = true;
		}
	}

	/**
	 * creates the Header table, may be overwritten by subclasses to change the display of the header columns.
	 */
	protected JTable createHeaderTable() {
		return new SubForm.SubFormTable();
	}

	/**
	 * create the table model used by the header table
	 */
	protected RowIndicatorTableModel createHeaderTableModel() {
		return new RowIndicatorTableModel();
	}

	/**
	 * initialize the look of the header table
	 */
	private void initializeHeaderTable() {
		//headerTable.setGridColor(Color.GRAY);
		headerTable.setShowGrid(true);
		headerTable.getTableHeader().setReorderingAllowed(false);
		
		headerTable.setPreferredScrollableViewportSize(new Dimension(COLUMN_SIZE, Integer.MAX_VALUE));
		headerTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		headerTable.setModel(createHeaderTableModel());
	}
	

	/**
	 * getter for the header table
	 */
	public JTable getHeaderTable() {
		return headerTable;
	}

	/**
	 * getter for the external table
	 */
	public SubForm.SubFormTable getExternalTable() {
		return externalTable;
	}
	
	protected SubForm.SubFormTableModel getExternalModel() {
		return externalTable.getSubFormModel();
	}

	/**
	 * getter for scrollpane of the external table
	 */
	protected JScrollPane getScrlpnOriginalTable() {
		return scrlpnOriginalTable;
	}

	/**
	 * Add the header table to the scrollpane of the external table and
	 * add listeners to synchronize the both tables
	 * @param aTableToAddHeader
	 * @param aScrlpOriginalTable
	 */
	protected void setExternalTable(SubForm.SubFormTable aTableToAddHeader, JScrollPane aScrlpOriginalTable) {
		if(externalTable != null) {
			externalTable.removePropertyChangeListener(modelPropertyListener);
			externalTable.removePropertyChangeListener(rowHeightPropertyListener);
		}
		
		// diplay header table in the external Scrollpane
		externalTable = aTableToAddHeader;
		scrlpnOriginalTable = aScrlpOriginalTable;
		scrlpnOriginalTable.setRowHeaderView(this.headerTable);

		// synchronize the appearance of the tables
		headerTable.setSelectionModel(aTableToAddHeader.getSelectionModel());
		headerTable.setBackground(externalTable.getBackground());
		scrlpnOriginalTable.getViewport().setBackground(externalTable.getBackground());

		if(externalTable != null) {
			externalTable.addPropertyChangeListener("model", modelPropertyListener);
			externalTable.addPropertyChangeListener("rowHeight", rowHeightPropertyListener);
		}
		setExternalModel(externalTable.getModel());
	}

	private PropertyChangeListener modelPropertyListener = new PropertyChangeListener() {
    	@Override
        public void propertyChange(PropertyChangeEvent ev) {
    		setExternalModel(externalTable.getModel());
    	}
    };

    private PropertyChangeListener rowHeightPropertyListener = new PropertyChangeListener() {
    	@Override
    	public void propertyChange(PropertyChangeEvent ev) {
    		SubformRowHeader.this.setRowHeight(externalTable.getRowHeight());
    	}
    };
	
	private void setExternalModel(TableModel externalModel) {
		if (externalModel != null) {
			externalModel.removeTableModelListener(externalTableModelListener);
			externalModel.addTableModelListener(externalTableModelListener);
			synchronizeModel();
		}
	}

	/**
	 * Set the header table model depending to the external model
	 * if JTable#setAutoCreateColumnsFromModel() is set to true,
	 * initialize column model
	 */
	protected void synchronizeModel() {
		((RowIndicatorTableModel) headerTable.getModel()).setExternalDataModel(externalTable.getModel());

		TableColumn column = headerTable.getColumnModel().getColumn(0);
		column.setPreferredWidth(COLUMN_SIZE);
		column.setMinWidth(COLUMN_SIZE);
		column.setMaxWidth(COLUMN_SIZE);
		column.setCellRenderer(RENDERER);

		headerTable.setBackground(externalTable.getBackground());
		scrlpnOriginalTable.getViewport().setBackground(externalTable.getBackground());
	}

	/**
	 * set the row height of the headerTable depending on the rowHeight of the external table
	 * @param rowHeight
	 */
	protected void setRowHeight(int rowHeight) {
		this.headerTable.setRowHeight(externalTable.getRowHeight());
	}

	/**
	 * set the row height in one row of the headerTable
	 * @param row
	 * @param iRowHeight
	 */
	protected void setRowHeightInRow(int row, int iRowHeight) {
		if (iRowHeight != this.headerTable.getRowHeight(row)) {
			if (getHeaderTable().getRowCount() > row) {
				this.headerTable.setRowHeight(row, iRowHeight);
			}
		}
	}

	public void endEditing() {
		if (this.headerTable.getCellEditor() != null) {
			this.headerTable.getCellEditor().stopCellEditing();
		}
	}

	/**
	 *
	 * @author <a href="mailto:rainer.schneider@novabit.de">rainer.schneider</a>
	 *
	 */
	public static class RowIndicatorTableModel extends AbstractTableModel {

		private int rowCount;

		public void setExternalDataModel(TableModel externalModel) {
			int newRowCount = externalModel.getRowCount();
			if(newRowCount != rowCount) {
				rowCount = newRowCount;
				fireTableDataChanged();
			}
		}

		@Override
        public Object getValueAt(int row, int column) {
			return null;
		}

		@Override
        public int getRowCount() {
			return rowCount;
		}

		@Override
        public int getColumnCount() {
			return 1;
		}

		@Override
		public String getColumnName(int column) {
			return "";
		}
	}

	/**
	 * Renderer for the header Table, to display the row inicator
	 * @author <a href="mailto:rainer.schneider@novabit.de">rainer.schneider</a>
	 */
	private static final RowIndicatorCellRenderer RENDERER = new RowIndicatorCellRenderer(); 
	private static final class RowIndicatorCellRenderer extends DefaultTableCellRenderer {

		private ImageIcon icon = (ImageIcon) Icons.getInstance().getIconRowSelection16();
		private JPanel panel = new JPanel();
		private JLabel bgLabel = new JLabel();

		public RowIndicatorCellRenderer() {
			bgLabel.setBackground(Color.LIGHT_GRAY);
			panel.setLayout(new BorderLayout());
			panel.add(bgLabel, BorderLayout.CENTER);
			setBackground(Color.LIGHT_GRAY);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (isSelected)
				bgLabel.setIcon(icon);
			else
				bgLabel.setIcon(null);
			return panel;
		}
	}
}
