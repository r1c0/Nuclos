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

import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.SubForm.SubFormTableModel;
import org.nuclos.client.ui.collect.model.ResultFields;
import org.nuclos.client.ui.table.CommonJTable;
import org.nuclos.client.ui.table.SortableTableModel;
import org.nuclos.client.ui.table.TableCellEditorProvider;
import org.nuclos.client.ui.table.TableCellRendererProvider;
import org.nuclos.client.ui.table.TableHeaderMouseListenerForSorting;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.PreferencesException;

/**
 *
 * @author <a href="mailto:rainer.schneider@novabit.de">rainer.schneider</a>
 *
 */
public class FixedColumnRowHeader extends SubformRowHeader {

	public static final String PREFS_NODE_FIXEDFIELDS = "fixedFields";
	public static final String PREFS_NODE_FIXEDFIELDS_WIDTHS = "fixedFieldWidths";

	private List<String> lstFixedColumnCollNames;
	private Map<Object, Integer> mpFixedColumnCollWidths;
	private List<Integer> lstHeaderColumnWidthsFromPref;
	private ToolTipsTableHeader tableHeader;
	private TableHeaderMouseListenerForSorting sortingListener;
	private TableColumnModelListener headerColumnModelListener;

	public FixedColumnRowHeader() {
		super();
		this.lstFixedColumnCollNames = new ArrayList<String>();
		this.mpFixedColumnCollWidths = new HashMap<Object, Integer>();
		this.initColumnModelListener();
		getHeaderTable().getColumnModel().addColumnModelListener(headerColumnModelListener);
	}

	public FixedColumnRowHeader(SubForm.SubFormTable tableToAddHeader, JScrollPane scrlpnOriginalTable) {
		super(tableToAddHeader, scrlpnOriginalTable);
		this.lstFixedColumnCollNames = new ArrayList<String>();
		this.mpFixedColumnCollWidths = new HashMap<Object, Integer>();
		this.initColumnModelListener();
		getHeaderTable().getColumnModel().addColumnModelListener(headerColumnModelListener);
	}

	private void initColumnModelListener() {

		this.headerColumnModelListener = new TableColumnModelListener() {
			@Override
            public void columnAdded(TableColumnModelEvent e) {
			}

			@Override
            public void columnRemoved(TableColumnModelEvent e) {
			}

			@Override
            public void columnMoved(TableColumnModelEvent e) {
				lstFixedColumnCollNames = getColumnNames();
				mpFixedColumnCollWidths = getVisibleColumnWidth();
			}

			@Override
            public void columnMarginChanged(ChangeEvent e) {
				invalidateHeaderTable();
				mpFixedColumnCollWidths = getVisibleColumnWidth();
			}

			@Override
            public void columnSelectionChanged(ListSelectionEvent e) {
			}

		};
	}

	@Override
	protected JTable createHeaderTable() {

		HeaderTable newHeaderTable = new HeaderTable();
		this.tableHeader = new FixedRowToolTipsTableHeader(null, newHeaderTable.getColumnModel());
		newHeaderTable.setTableHeader(this.tableHeader);
		return newHeaderTable;
	}

	@Override
	protected RowIndicatorTableModel createHeaderTableModel() {
		return new FixedRowIndicatorTableModel();
	}

	@Override
	protected void setRowHeight(int rowHeight) {
		super.setRowHeight(rowHeight);
		invalidateHeaderTable();
	}

	@Override
	protected void setRowHeightInRow(int row, int iRowHeight) {
		super.setRowHeightInRow(row, iRowHeight);
	}

	@Override
	protected void setExternalTable(SubForm.SubFormTable tableToAddHeader, JScrollPane scrlpOriginalTable) {
		super.setExternalTable(tableToAddHeader, scrlpOriginalTable);
		
		tableToAddHeader.addPropertyChangeListener("rowSorter", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ("rowSorter".equals(evt.getPropertyName())) {
					rowSorterChanged();
				}
			}
		});
		rowSorterChanged();
		
		((HeaderTable) getHeaderTable()).setTableCellEditorProvider(tableToAddHeader.getTableCellEditorProvider());
		((HeaderTable) getHeaderTable()).setTableCellRendereProvider(tableToAddHeader.getTableCellRendererProvider());
		((HeaderTable) getHeaderTable()).setExternalTable(tableToAddHeader);
		getHeaderTable().setBackground(getHeaderTable().getBackground());
		getScrlpnOriginalTable().setRowHeaderView(getHeaderTable());
		getScrlpnOriginalTable().getRowHeader().setBackground(getHeaderTable().getBackground());
		getScrlpnOriginalTable().getRowHeader().setPreferredSize(getHeaderTable().getPreferredSize());
		getScrlpnOriginalTable().setCorner(JScrollPane.UPPER_LEFT_CORNER, getHeaderTable().getTableHeader());

		invalidateHeaderTable();
	}
	
	private void rowSorterChanged() {
		RowSorter<? extends TableModel> rowSorter = getExternalTable().getRowSorter();
		if (rowSorter != null) 
			rowSorter = new FixedRowIndicicatorRowSorter<FixedRowIndicatorTableModel>((FixedRowIndicatorTableModel) getHeaderTable().getModel(), rowSorter);
		getHeaderTable().setRowSorter(rowSorter);
		
	}

	/**
	 * command: select columns
	 * Lets the user select the columns to show in the result list.
	 */
	@SuppressWarnings("unchecked")
	private void cmdSelectColumns() {

		final SelectFixedColumnsController ctl = new SelectFixedColumnsController(this.getHeaderTable());
		final List<CollectableEntityField> lstAvailable = getAllAvailableFields();
		final List<CollectableEntityField> lstFixed = getDisplayedHeaderTableFields();
		final List<CollectableEntityField> lstSelected = new ArrayList<CollectableEntityField>(lstFixed);
		final ResultFields ro = new ResultFields();
		ro.set(lstAvailable, lstSelected,
				(Comparator<CollectableEntityField>) getCollectableEntityFieldComparator());

		lstSelected.addAll(getDisplayedExternalTableFields());

		for (CollectableEntityField curField : lstSelected) {
			lstAvailable.remove(curField);
		}

		// remember the widths of the currently visible columns
		final Map<Object,Integer> mpWidths = getVisibleColumnWidth();

		final boolean bOK = ctl.run(ro, new HashSet<CollectableEntityField>(lstFixed), CommonLocaleDelegate.getMessage("SelectColumnsController.1","Anzuzeigende Spalten ausw\u00e4hlen"));

		if (bOK) {
			UIUtils.runCommand(this.getHeaderTable(), new CommonRunnable() {
				@Override
                public void run() /* throws CommonBusinessException */ {
					final int iSelRow = getExternalTable().getSelectedRow();

					final List<CollectableEntityField> lstSelectedNew = (List<CollectableEntityField>) ctl.getSelectedObjects();
					final Set<?> setFixedNew = ctl.getFixedObjects();
					final List<CollectableEntityField> lstFixedNew = new ArrayList<CollectableEntityField>(setFixedNew.size());

					for (CollectableEntityField curField : lstSelectedNew) {
						if (setFixedNew.contains(curField)) {
							lstFixedNew.add(curField);
						}
					}

					lstFixedColumnCollNames = CollectableUtils.getFieldNamesFromCollectableEntityFields(lstFixedNew);
					synchronizeColumnsInHeaderTable(lstFixedColumnCollNames);
					lstSelectedNew.removeAll(lstFixedNew);
					synchronizeColumnsInExternalTable(lstSelectedNew);

					// reselect the previously selected row (which gets lost be refreshing the model)
					if (iSelRow != -1) {
						getExternalTable().setRowSelectionInterval(iSelRow, iSelRow);
					}

					restoreColumnWidthsInExternalTable(mpWidths);
					restoreColumnWidthsInHeaderTable(mpWidths);
					mpFixedColumnCollWidths = getVisibleColumnWidth();
					invalidateHeaderTable();
				}
			});
		}
	}

	/**
	 * Get all possible column field from the table model
	 * @return List of CollectableEntityField
	 */
	private List<CollectableEntityField> getAllAvailableFields() {
		ArrayList<CollectableEntityField> resultList = new ArrayList<CollectableEntityField>();
		CollectableEntityFieldBasedTableModel subformtblmdl = getExternalModel();

		for (int iColumnNr = 0; iColumnNr < subformtblmdl.getColumnCount(); iColumnNr++) {
			resultList.add(subformtblmdl.getCollectableEntityField(iColumnNr));
		}

		return resultList;
	}

	/**
	 * get the CollectableEntityField of the displayed columns of the external table
	 * @return List of CollectableEntityField
	 */
	private List<CollectableEntityField> getDisplayedExternalTableFields() {
		TableColumnModel externalColumnModel = getExternalTable().getColumnModel();
		List<CollectableEntityField> resultList = new ArrayList<CollectableEntityField>();
		for (Enumeration<TableColumn> columnEnum = externalColumnModel.getColumns(); columnEnum.hasMoreElements();) {
			TableColumn varColumn = columnEnum.nextElement();
			final int iModelColumn = varColumn.getModelIndex();
			final CollectableEntityField clctefTarget = getExternalModel().getCollectableEntityField(iModelColumn);
			resultList.add(clctefTarget);
		}
		return resultList;
	}

	/**
	 * get the CollectableEntityField of the displayed columns of the header table
	 * @return List of CollectableEntityField
	 */
	private List<CollectableEntityField> getDisplayedHeaderTableFields() {
		TableColumnModel externalColumnModel = getHeaderTable().getColumnModel();
		List<CollectableEntityField> resultList = new ArrayList<CollectableEntityField>();
		for (Enumeration<TableColumn> columnEnum = externalColumnModel.getColumns(); columnEnum.hasMoreElements();) {
			TableColumn varColumn = columnEnum.nextElement();
			if (varColumn.getModelIndex() != FixedRowIndicatorTableModel.ROWMARKERCOLUMN_INDEX) {
				final int iModelColumn = varColumn.getModelIndex() - FixedRowIndicatorTableModel.ROWMARKERCOLUMN_COUNT;
				final CollectableEntityField clctefTarget = getExternalModel().getCollectableEntityField(iModelColumn);
				resultList.add(clctefTarget);
			}
		}
		return resultList;
	}

	private Map<Object, Integer> getVisibleColumnWidth() {

		final Map<Object, Integer> mpWidths = new HashMap<Object, Integer>(20);

		// remember the widths of the currently visible columns
		TableColumnModel externalColumnModel = getExternalTable().getColumnModel();
		for (Enumeration<TableColumn> columnEnum = externalColumnModel.getColumns(); columnEnum.hasMoreElements();) {
			TableColumn varColumn = columnEnum.nextElement();
			mpWidths.put(varColumn.getIdentifier(), new Integer(varColumn.getWidth()));
		}

		TableColumnModel headerColumnModel = getHeaderTable().getColumnModel();
		for (Enumeration<TableColumn> columnEnum = headerColumnModel.getColumns(); columnEnum.hasMoreElements();) {
			TableColumn varColumn = columnEnum.nextElement();
			if (StringUtils.nullIfEmpty((String) varColumn.getIdentifier()) != null) {
				mpWidths.put(varColumn.getIdentifier(), new Integer(varColumn.getWidth()));
			}
		}

		return mpWidths;
	}

	private void restoreColumnWidthsInExternalTable(final Map<Object,Integer> mpWidths) {
		// restore the widths of the still present columns
		TableColumnModel externalColumnModel = getExternalTable().getColumnModel();
		for (Enumeration<TableColumn> columnEnum = externalColumnModel.getColumns(); columnEnum.hasMoreElements();) {
			TableColumn varColumn = columnEnum.nextElement();
			Integer width = mpWidths.get(varColumn.getIdentifier());
			if (width != null) {
				varColumn.setPreferredWidth(width.intValue());
				varColumn.setWidth(width.intValue());
			}
		}
	}

	private void restoreColumnWidthsInHeaderTable(final Map<Object,Integer> mpWidths) {
		TableColumnModel headerColumnModel = getHeaderTable().getColumnModel();
		for (Enumeration<TableColumn> columnEnum = headerColumnModel.getColumns(); columnEnum.hasMoreElements();) {
			TableColumn varColumn = columnEnum.nextElement();
			if (StringUtils.nullIfEmpty((String) varColumn.getIdentifier()) != null) {
				Integer width = mpWidths.get(varColumn.getIdentifier());
				if (width != null) {
					varColumn.setPreferredWidth(width.intValue());
					varColumn.setWidth(width.intValue());
				}
			}
		}
	}

	private void restoreColumnWidthsFromPrefsInHeaderTable(final List<Integer> lstWidthsFromPref) {
		TableColumnModel headerColumnModel = getHeaderTable().getColumnModel();
		Iterator<Integer> widthIter = lstWidthsFromPref.iterator();
		for (Enumeration<TableColumn> columnEnum = headerColumnModel.getColumns(); columnEnum.hasMoreElements();) {
			TableColumn varColumn = columnEnum.nextElement();
			if (StringUtils.nullIfEmpty((String) varColumn.getIdentifier()) != null) {
				if (widthIter.hasNext()) {
					Integer width = widthIter.next();
					varColumn.setPreferredWidth(width.intValue());
					varColumn.setWidth(width.intValue());
				}
			}
		}
	}

	/**
	 * @return the <code>Comparator</code> used for <code>CollectableEntityField</code>s (columns in the Result).
	 * The default is to compare the column labels.
	 * @postcondition result != null
	 */
	private Comparator<? extends CollectableEntityField> getCollectableEntityFieldComparator() {
		return new CollectableEntityField.LabelComparator();
	}

	/**
	 * adds a mouse listener to the table header to trigger a table sort when a column heading is clicked in the JTable.
	 * @param tbl
	 * @param tblmodel
	 * @param runnableSort Runnable to execute for sorting the table. If <code>null</code>, <code>tblmodel.sort()</code> is performed.
	 */
	private void addMouseListenerForSortingToTableHeader(JTable tbl, SortableTableModel tblmodel, CommonRunnable runnableSort) {
		// clicking header does not mean column selection, but sorting:

		if (this.sortingListener == null) {
			tbl.setColumnSelectionAllowed(false);
			this.sortingListener = new TableHeaderMouseListenerForSorting(tbl, tblmodel, runnableSort) {
				@Override
				protected int convertColumnIndexToModel(int viewColumnIndex) {
					return super.convertColumnIndexToModel(viewColumnIndex) - FixedRowIndicatorTableModel.ROWMARKERCOLUMN_COUNT;
				}

				// misuse sort column to start select columns
				@Override
				protected void sortColumn(int iColumn, Component windowComponent) throws CommonBusinessException {
					if (iColumn == -1) {
						cmdSelectColumns();
					}
					else {
						super.sortColumn(iColumn, windowComponent);
						getExternalTable().repaint();
					}
				}
			};
			tbl.getTableHeader().addMouseListener(sortingListener);
		}
		else {
			this.sortingListener.setTableModel(tblmodel);
		}
	}

	@Override
	protected void synchronizeModel() {

		getHeaderTable().getColumnModel().removeColumnModelListener(headerColumnModelListener);

		List<CollectableEntityField> selectedFields = getDisplayedExternalTableFields();
		// remember the widths of the currently visible columns
		final Map<Object,Integer> mpWidths = getVisibleColumnWidth();
		getScrlpnOriginalTable().getRowHeader().setBackground(getHeaderTable().getBackground());

		super.synchronizeModel();

		((HeaderTable) getHeaderTable()).setTableCellEditorProvider(getExternalTable().getTableCellEditorProvider());
		((HeaderTable) getHeaderTable()).setTableCellRendereProvider(getExternalTable().getTableCellRendererProvider());
		this.tableHeader.setExternalModel(getExternalModel());

		if (getExternalTable().getModel() instanceof SortableTableModel) {
			addMouseListenerForSortingToTableHeader(getHeaderTable(), (SortableTableModel) getExternalTable().getModel(), null);
		}

		synchronizeColumnsInHeaderTable(lstFixedColumnCollNames);
		restoreColumnWidthsInHeaderTable(this.mpFixedColumnCollWidths);
		// remove row header columns from external columns
		for (Iterator<CollectableEntityField> fieldIter = selectedFields.iterator(); fieldIter.hasNext();) {
			CollectableEntityField curField = fieldIter.next();
			if (lstFixedColumnCollNames.contains(curField.getName())) {
				fieldIter.remove();
			}
		}
		synchronizeColumnsInExternalTable(selectedFields);

		// set table header renderer
		if (this.getHeaderTable().getColumnModel().getColumnCount() > 0) {
			TableColumn column = this.getHeaderTable().getColumnModel().getColumn(0);
			column.setHeaderRenderer(new ColumnSelectionTableCellRenderer(getHeaderTable()));
		}

		invalidateHeaderTable();
		getHeaderTable().getColumnModel().addColumnModelListener(headerColumnModelListener);
		restoreColumnWidthsInExternalTable(mpWidths);
		if (lstHeaderColumnWidthsFromPref != null) {
			restoreColumnWidthsFromPrefsInHeaderTable(lstHeaderColumnWidthsFromPref);
			lstHeaderColumnWidthsFromPref = null;
		}
		else {
			restoreColumnWidthsInHeaderTable(mpWidths);
		}
	}

	/**
	 * invalidate and repaint Header table, for example after the size of a column has changed
	 */
	public void invalidateHeaderTable() {

		getScrlpnOriginalTable().getRowHeader().setPreferredSize(getHeaderTable().getPreferredSize());
		getHeaderTable().setRowHeight(getExternalTable().getRowHeight());
		getHeaderTable().revalidate();
		getHeaderTable().invalidate();
		getHeaderTable().repaint();

		getScrlpnOriginalTable().revalidate();
		getScrlpnOriginalTable().invalidate();
		getScrlpnOriginalTable().repaint();

	}

	/**
	 * removed/add the columns from the external table, according to the given list
	 * @param lstFixedNew	List of CollectableEntityField
	 *
	 */
	private void synchronizeColumnsInExternalTable(List<CollectableEntityField> lstFixedNew) {

		TableColumnModel externalColumnModel = getExternalTable().getColumnModel();
		Set<TableColumn> columnsToRemove = new HashSet<TableColumn>();
		// remove all columns
		for (Enumeration<TableColumn> columnEnum = externalColumnModel.getColumns(); columnEnum.hasMoreElements();) {

			TableColumn varColumn = columnEnum.nextElement();
			columnsToRemove.add(varColumn);
		}
		for (Iterator<TableColumn> colIter = columnsToRemove.iterator(); colIter.hasNext();) {

			externalColumnModel.removeColumn(colIter.next());
		}

		// add inserted columns
		for (Iterator<CollectableEntityField> fieldIter = lstFixedNew.iterator(); fieldIter.hasNext();) {

			try {
				boolean doInsert = true;
				CollectableEntityField curField = fieldIter.next();
	
				for (Enumeration<TableColumn> columnEnum = externalColumnModel.getColumns(); doInsert && columnEnum.hasMoreElements();) {
	
					TableColumn varColumn = columnEnum.nextElement();
					if (curField.getLabel().equals(varColumn.getIdentifier())) {
						doInsert = false;
					}
				}
	
				if (doInsert) {
					int index = ((SubFormTableModel) getExternalTable().getModel()).findColumnByFieldName(curField.getName());
					TableColumn newColumn = new TableColumn(index);
					newColumn.setIdentifier(curField.getName());
					String sLabel = ((SubFormTableModel) getExternalTable().getModel()).getColumnName(index);
					newColumn.setHeaderValue(sLabel);
					externalColumnModel.addColumn(newColumn);
				}
			}
			catch(Exception e) {
				// only add Column on non exception
				// column may be removed
			}
		}
	}

	/**
	 * removed/add the columns from the fixed table, according to the given list
	 * @param lstFixedNew List of String
	 *
	 */
	private void synchronizeColumnsInHeaderTable(List<String> lstFixedNew) {

		TableColumnModel fixedColumnModel = getHeaderTable().getColumnModel();
		Set<TableColumn> columnsToRemove = new HashSet<TableColumn>();
		// remove all columns
		for (Enumeration<TableColumn> columnEnum = fixedColumnModel.getColumns(); columnEnum.hasMoreElements();) {

			TableColumn varColumn = columnEnum.nextElement();
			if (StringUtils.nullIfEmpty((String) varColumn.getIdentifier()) != null) {
				columnsToRemove.add(varColumn);
			}
		}

		for (Iterator<TableColumn> colIter = columnsToRemove.iterator(); colIter.hasNext();) {

			fixedColumnModel.removeColumn(colIter.next());
		}

		// add inserted columns
		for (Iterator<String> fieldIter = lstFixedNew.iterator(); fieldIter.hasNext();) {
			try {
				String curField = fieldIter.next();
				int index = ((SubFormTableModel) getExternalTable().getModel()).findColumnByFieldName(curField);
				TableColumn newColumn = new TableColumn(index + FixedRowIndicatorTableModel.ROWMARKERCOLUMN_COUNT);
				newColumn.setIdentifier(curField);
				String sLabel = ((SubFormTableModel) getExternalTable().getModel()).getColumnName(index);
				newColumn.setHeaderValue(sLabel);
				newColumn.setHeaderRenderer(new ColumnSelectionTableCellRenderer(getHeaderTable()));
				fixedColumnModel.addColumn(newColumn);
			}
			catch(Exception e) {
				// only add Column on non exception
				// column may be removed
			}
		}
	}

//	private Map getHeaderColumnWidths(JTable tbl) {
//		final Map result = new HashMap();
//		final Enumeration enumeration = tbl.getColumnModel().getColumns();
//		while (enumeration.hasMoreElements()) {
//			final TableColumn column = (TableColumn) enumeration.nextElement();
//			if( !column.getIdentifier().equals("")) {
//				CollectableEntityField clctbl = ((CollectableEntityFieldBasedTableModel)getExternalTable().getModel()).getEntityField(column.getModelIndex() - FixedRowIndicatorTableModel.ROWMARKERCOLUMN_COUNT);
//				result.put(clctbl.getName(), new Integer(column.getWidth()));
//			}
//		}
//		return result;
//	}

	private List<String> getColumnNames() {
		final List<String> result = new ArrayList<String>();
		final Enumeration<TableColumn> enumeration = getHeaderTable().getColumnModel().getColumns();
		while (enumeration.hasMoreElements()) {
			final TableColumn column = enumeration.nextElement();
			if (StringUtils.nullIfEmpty((String) column.getIdentifier()) != null) {
				CollectableEntityField clctbl = getExternalModel().getCollectableEntityField(column.getModelIndex() - FixedRowIndicatorTableModel.ROWMARKERCOLUMN_COUNT);
				result.add(clctbl.getName());
			}
		}
		return result;
	}

	public void writeFieldToPreferences(Preferences preferences) throws PreferencesException {

		final List<Integer> lstWidths = new ArrayList<Integer>();
		final Enumeration<TableColumn> enumeration = getHeaderTable().getColumnModel().getColumns();
		while (enumeration.hasMoreElements()) {
			final TableColumn column = enumeration.nextElement();
			if (StringUtils.nullIfEmpty((String) column.getIdentifier()) != null) {
				lstWidths.add(column.getWidth());
			}
		}
		PreferencesUtils.putIntegerList(preferences, PREFS_NODE_FIXEDFIELDS_WIDTHS, lstWidths);
		PreferencesUtils.putStringList(preferences, PREFS_NODE_FIXEDFIELDS, getColumnNames());
	}

	public void initializeFieldsFromPreferences(Preferences preferences) {

		try {
			this.lstFixedColumnCollNames = PreferencesUtils.getStringList(preferences, PREFS_NODE_FIXEDFIELDS);
			this.lstHeaderColumnWidthsFromPref = PreferencesUtils.getIntegerList(preferences, PREFS_NODE_FIXEDFIELDS_WIDTHS);
		}
		catch (PreferencesException ex) {
			this.lstFixedColumnCollNames = new ArrayList<String>();
			this.mpFixedColumnCollWidths = new HashMap<Object, Integer>();
		}
	}

	/**
	 * Header table model which provides editors and renderers for the CollectableEntityField
	 */
	public static class HeaderTable extends CommonJTable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private TableCellEditorProvider cellEditorProvider;
		private TableCellRendererProvider cellRendererProvider;

		private JTable externalTable;

		public HeaderTable() {
		}

		@Override
		public TableCellRenderer getCellRenderer(int iRow, int iColumn) {

			TableCellRenderer result = null;

			if (cellRendererProvider != null && getModel() instanceof FixedRowIndicatorTableModel) {
				final int iModelColumn = getColumnModel().getColumn(iColumn).getModelIndex();
				if (iModelColumn != FixedRowIndicatorTableModel.ROWMARKERCOLUMN_INDEX) {
					FixedRowIndicatorTableModel fixedModel = (FixedRowIndicatorTableModel) getModel();
					final CollectableEntityField clctefTarget = ((SubFormTableModel) fixedModel.getExternalModel()).getCollectableEntityField(iModelColumn - FixedRowIndicatorTableModel.ROWMARKERCOLUMN_COUNT);
					result = cellRendererProvider.getTableCellRenderer(clctefTarget);
				}
			}
			if (result == null) {
				result = super.getCellRenderer(iRow, iColumn);
			}
			return result;
		}

		public TableCellEditorProvider getTableCellEditorProvider() {
			return this.cellEditorProvider;
		}

		public void setTableCellEditorProvider(TableCellEditorProvider aCellEditorProvider) {
			this.cellEditorProvider = aCellEditorProvider;
		}

		public void setTableCellRendereProvider(TableCellRendererProvider aCellRendererProvider) {
			this.cellRendererProvider = aCellRendererProvider;
		}

		@Override
		public TableCellEditor getCellEditor(int iRow, int iColumn) {
			TableCellEditor result = null;
		
			if (cellEditorProvider != null && getModel() instanceof FixedRowIndicatorTableModel) {
				final int iModelColumn = getColumnModel().getColumn(iColumn).getModelIndex();
				if (iModelColumn != FixedRowIndicatorTableModel.ROWMARKERCOLUMN_INDEX) {
					FixedRowIndicatorTableModel headerModel = (FixedRowIndicatorTableModel) getModel();
					final CollectableEntityField clctefTarget = ((SubFormTableModel) headerModel.externalModel).getCollectableEntityField(iModelColumn - FixedRowIndicatorTableModel.ROWMARKERCOLUMN_COUNT);
					result = cellEditorProvider.getTableCellEditor(this, iRow, clctefTarget);
				}
			}
			if (result == null) {
				result = super.getCellEditor(iRow, iColumn);
			}
			return result;
		}

		public void setExternalTable(JTable aExternalTable) {
			this.externalTable = aExternalTable;
		}

		@Override
		public void setEditingRow(int aRow) {
			super.setEditingRow(aRow);
			if (this.externalTable != null) {
				this.externalTable.setEditingRow(aRow);
			}
		}

		/**
		 * set the row height in one row (used by the ElisaCollectableTextArea)
		 * @param iRow
		 * @param iRowHeight
		 */
		public void setRowHeightFromTextArea(int iRow, int iRowHeight) {
			if (iRowHeight != this.getRowHeight(iRow)) {
				this.setRowHeight(iRow, iRowHeight);
			}

			if (this.externalTable != null) {
				if (iRowHeight != this.externalTable.getRowHeight(iRow)) {
					this.externalTable.setRowHeight(iRow, iRowHeight);
				}
			}
		}

	}	// inner class Table

	/**
	 * table model which provides a row indicator column and all columns of the wrapped external model
	 * @author <a href="mailto:rainer.schneider@novabit.de">rainer.schneider</a>
	 */
	public static class FixedRowIndicatorTableModel extends SubformRowHeader.RowIndicatorTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private static final int ROWMARKERCOLUMN_INDEX = 0;
		public static final int ROWMARKERCOLUMN_COUNT = 1;
		private TableModel externalModel = new DefaultTableModel(0, 0);

		@Override
		public void setExternalDataModel(TableModel aExternalModel) {
			externalModel = (externalModel != null) ? aExternalModel : new DefaultTableModel(0, 0);
			fireTableStructureChanged();
		}

		@Override
		public Object getValueAt(int row, int column) {
			return (column == ROWMARKERCOLUMN_INDEX) ? null : externalModel.getValueAt(row, column - ROWMARKERCOLUMN_COUNT);
		}

		@Override
		public void setValueAt(Object aValue, int row, int column) {
			if (column != ROWMARKERCOLUMN_INDEX) {
				externalModel.setValueAt(aValue, row, column - ROWMARKERCOLUMN_COUNT);
			}
		}

		@Override
		public int getRowCount() {
			return externalModel.getRowCount();
		}

		@Override
		public int getColumnCount() {
			return externalModel.getColumnCount() + ROWMARKERCOLUMN_COUNT;
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return (column != ROWMARKERCOLUMN_INDEX) && externalModel.isCellEditable(row, column - ROWMARKERCOLUMN_COUNT);
		}

		@Override
		public String getColumnName(int column) {
			return (column == ROWMARKERCOLUMN_INDEX) ? "" : externalModel.getColumnName(column - ROWMARKERCOLUMN_COUNT);
		}

		TableModel getExternalModel() {
			return externalModel;
		}
	}

	/**
	 * displays the column select icon in the first column of the table
	 */
	public static class ColumnSelectionTableCellRenderer implements TableCellRenderer {
		private JTable rendererTable;

		public ColumnSelectionTableCellRenderer(JTable headerTable) {
			this.rendererTable = headerTable;
		}

		@Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			Component renderer = rendererTable.getTableHeader().getDefaultRenderer()
					.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			if (renderer instanceof JLabel) {
				if (column == 0) {
					((JLabel) renderer).setIcon(Icons.getInstance().getIconSelectVisibleColumns16());
					((JLabel) renderer).setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 0));
				}
			}
			
			return renderer;
		}

	}
	
	private static class FixedRowToolTipsTableHeader extends ToolTipsTableHeader {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public FixedRowToolTipsTableHeader(CollectableEntityFieldBasedTableModel aTableModel, TableColumnModel cm) {
			super(aTableModel, cm);
		}
		
		@Override
		public TableCellRenderer getDefaultRenderer() {
			TableCellRenderer tcr = new TableCellRenderer() {
				@Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
						boolean hasFocus, int row, int column) {
					
					Component comp = FixedRowToolTipsTableHeader.super.getDefaultRenderer().getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
					comp.setBackground(Color.lightGray);
					
					return comp;
				}
			};
			
			return tcr;
		};

		@Override
		protected int adjustColumnIndex(int iColumn) {
			return iColumn - FixedRowIndicatorTableModel.ROWMARKERCOLUMN_COUNT;
		}
	}
	
	/**
	 * A RowSorter which listens to a base row sorter and propagates all sorting changes.
	 * The sort keys are delegated directly to the base row sorter, i.e. 
	 */
	public static class FixedRowIndicicatorRowSorter<M extends FixedRowIndicatorTableModel> extends RowSorter<M> {
		
		private final RowSorterListener externalRowSorterListener;
		private final M tableModel;
		private final RowSorter<?> externalRowSorter;
		
		public FixedRowIndicicatorRowSorter(M tableModel, RowSorter<?> externalRowSorter) {
			this.externalRowSorterListener = new RowSorterListener() {
				@Override
				public void sorterChanged(RowSorterEvent e) {
					externalSorterChanged(e);
				}
			};
			this.tableModel = tableModel;
			this.externalRowSorter = externalRowSorter;
			this.externalRowSorter.addRowSorterListener(externalRowSorterListener);
		}
		
		@Override
		public M getModel() {
			return tableModel;
		}

		@Override
		public int convertRowIndexToModel(int index) {
			return externalRowSorter.convertRowIndexToModel(index);
		}

		@Override
		public int convertRowIndexToView(int index) {
			return externalRowSorter.convertRowIndexToView(index);
		}

		@Override
		public int getViewRowCount() {
			return externalRowSorter.getViewRowCount();
		}

		@Override
		public int getModelRowCount() {
			return externalRowSorter.getModelRowCount();
		}
		
		protected void externalSorterChanged(RowSorterEvent e) {
			switch (e.getType()) {
			case SORT_ORDER_CHANGED:
				fireSortOrderChanged();
				break;
			case SORTED:
				// TODO: provide access to the old indices
				fireRowSorterChanged(null);
				break;
			default:
				throw new IllegalArgumentException("Invalid row sorter event type " + e.getType());
			}
		}
		
		@Override
		public void setSortKeys(List<? extends javax.swing.RowSorter.SortKey> keys) {
			List<SortKey> externalKeys = new ArrayList<SortKey>();
			for (SortKey key : keys) {
				if (key.getColumn() > FixedRowIndicatorTableModel.ROWMARKERCOLUMN_COUNT)
					externalKeys.add(shiftSortKey(key, - FixedRowIndicatorTableModel.ROWMARKERCOLUMN_COUNT));
			}
			externalRowSorter.setSortKeys(externalKeys);
		}

		@Override
		public List<? extends javax.swing.RowSorter.SortKey> getSortKeys() {
			List<SortKey> keys = new ArrayList<SortKey>();
			List<? extends SortKey> externalKeys = externalRowSorter.getSortKeys();
			for (SortKey externalKey : externalKeys) {
				keys.add(shiftSortKey(externalKey, + FixedRowIndicatorTableModel.ROWMARKERCOLUMN_COUNT));
			}
			return keys;
		}

		@Override
		public void toggleSortOrder(int column) {
			if (column > FixedRowIndicatorTableModel.ROWMARKERCOLUMN_COUNT)
				externalRowSorter.toggleSortOrder(column - FixedRowIndicatorTableModel.ROWMARKERCOLUMN_COUNT);
		}

		@Override
		public void modelStructureChanged() {
		}

		@Override
		public void allRowsChanged() {
		}

		@Override
		public void rowsInserted(int firstRow, int endRow) {
		}

		@Override
		public void rowsDeleted(int firstRow, int endRow) {
		}

		@Override
		public void rowsUpdated(int firstRow, int endRow) {
		}

		@Override
		public void rowsUpdated(int firstRow, int endRow, int column) {
		}
		
		private static SortKey shiftSortKey(SortKey key, int delta) {
			return new SortKey(key.getColumn() + delta, key.getSortOrder());
		}
	}
	
}
