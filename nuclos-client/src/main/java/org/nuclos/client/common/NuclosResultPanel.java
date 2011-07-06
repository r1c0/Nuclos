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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicScrollPaneUI.VSBChangeListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.genericobject.ChangeListenerForResultTableVerticalScrollBar;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.report.reportrunner.BackgroundProcessInfo;
import org.nuclos.client.report.reportrunner.BackgroundProcessStatusController;
import org.nuclos.client.report.reportrunner.BackgroundProcessStatusDialog;
import org.nuclos.client.report.reportrunner.BackgroundProcessTableEntry;
import org.nuclos.client.transfer.XmlExportDelegate;
import org.nuclos.client.transfer.XmlImportDelegate;
import org.nuclos.client.ui.CommonBackgroundProcessClientWorkerAdapter;
import org.nuclos.client.ui.CommonMultiThreader;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.UIUtils.CommandHandler;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.CollectableTableHelper;
import org.nuclos.client.ui.collect.CollectableTableModel;
import org.nuclos.client.ui.collect.ResultPanel;
import org.nuclos.client.ui.collect.SelectFixedColumnsController;
import org.nuclos.client.ui.collect.SortableCollectableTableModel;
import org.nuclos.client.ui.collect.ToolTipsTableHeader;
import org.nuclos.client.ui.collect.component.model.ChoiceEntityFieldList;
import org.nuclos.client.ui.table.CommonJTable;
import org.nuclos.client.ui.table.SortableTableModel;
import org.nuclos.client.ui.table.TableUtils;
import org.nuclos.common.CollectableEntityFieldWithEntityForExternal;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.security.Permission;
import org.nuclos.common2.ClientPreferences;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.DateUtils;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.PreferencesException;
import org.nuclos.server.genericobject.ProxyList;


/** @todo refactor: This class contains a lot of Controller code, which should be in a (Nucleus)ResultController. */
public class NuclosResultPanel<Clct extends Collectable> extends ResultPanel<Clct> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTable tblFixedResult;
	private Set<CollectableEntityField> stFixedColumns;
	public static final String PREFS_NODE_FIXEDFIELDS = "fixedFields";
	public static final String PREFS_NODE_FIXEDFIELDS_WIDTHS = "fixedFieldWidths";

	private static final String PREFS_KEY_LASTXMLTRANSFERPATH = "lastXMLTransferPath";

	final ExecutorService cachedThreadPoolExecutor = Executors.newCachedThreadPool();

	public NuclosResultPanel() {
		super();
		this.stFixedColumns = new HashSet<CollectableEntityField>();
	}

	@Override
	protected JPanel newResultTablePanel() {
		super.newResultTablePanel();

		tblFixedResult = new CommonJTable();

		tblFixedResult.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblFixedResult.getTableHeader().setForeground(Color.darkGray);

		tblFixedResult.setSelectionModel(getResultTable().getSelectionModel());

		//override copy Action for both tables
		setupCopyAction();

		final JPanel result = new JPanel(new BorderLayout());
		result.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		result.add(getResultTableScrollPane(), BorderLayout.CENTER);

		tblFixedResult.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
			@Override
			public void columnAdded(TableColumnModelEvent e) {
			}

			@Override
			public void columnRemoved(TableColumnModelEvent e) {
			}

			@Override
			public void columnMoved(TableColumnModelEvent e) {
			}

			@Override
			public void columnMarginChanged(ChangeEvent e) {
				invalidateFixedTable();
			}

			@Override
			public void columnSelectionChanged(ListSelectionEvent e) {
			}

		});
		
		ToolTipsTableHeader tblHeader = new ToolTipsTableHeader(null, tblFixedResult.getColumnModel());

		tblHeader.setName("tblHeader");
		tblFixedResult.setTableHeader(tblHeader);

		return result;
	}

	/**
	 * @return the number of selected rows, 0 if no rows are selected
	 */
	public Set<Integer> getSelectedRowCount(ListSelectionModel selectionModel) {
		final int iMin = selectionModel.getMinSelectionIndex();
		final int iMax = selectionModel.getMaxSelectionIndex();

		final Set<Integer> result = new HashSet<Integer>();
		for (int i = iMin; i <= iMax; i++) {
			if (selectionModel.isSelectedIndex(i)) {
				result.add(i);
			}
		}
		return result;
	}

	/**
	 * @return the table containing the search results
	 */
	public JTable getFixedResultTable() {
		return this.tblFixedResult;
	}

	@Override
	protected void initializeFields(CollectableEntity clcte, CollectController<Clct> clctctl, Preferences preferences) {
		super.initializeFields(clcte, clctctl, preferences);

		List<String> lstSelectedFieldNames;
		try {
			lstSelectedFieldNames = PreferencesUtils.getStringList(preferences, PREFS_NODE_FIXEDFIELDS);
		}
		catch (PreferencesException ex) {
			lstSelectedFieldNames = new ArrayList<String>();
		}

		ChoiceEntityFieldList fields = clctctl.getFields();
		for (CollectableEntityField clctef : fields.getSelectedFields()) {
			if (lstSelectedFieldNames.contains(clctef.getName())) {
				this.stFixedColumns.add(clctef);
			}
		}
	}

	@Override
	protected void setModel(CollectableTableModel<Clct> tblmodel, final CollectableEntity clcte, final CollectController<Clct> clctctl) {
		super.setModel(tblmodel, clcte, clctctl);
		tblFixedResult.setModel(tblmodel);
		
		ToolTipsTableHeader tblHeader = new ToolTipsTableHeader(tblmodel, tblFixedResult.getColumnModel());

		tblHeader.setName("tblHeader");
		tblFixedResult.setTableHeader(tblHeader);
		
		TableUtils.addMouseListenerForSortingToTableHeader(tblFixedResult, (SortableTableModel) tblmodel, new CommonRunnable() {
	         @Override
	       	public void run() {
	             clctctl.cmdRefreshResult();
	          }
	       });

		removeColumnsFromFixedTable();
		removeColumnsFromResultTable();

		setFixedTable();
		TableUtils.setOptimalColumnWidths(tblFixedResult);
		
	}

	/**
	 * removes the columns from the result tabll, wich are  in the stFixedColumns
	 */
	private void removeColumnsFromResultTable() {
		TableColumnModel resultColumnModel = getResultTable().getColumnModel();
		Set<TableColumn> columnsToRemove = new HashSet<TableColumn>();
		for (Enumeration<TableColumn> columnEnum = resultColumnModel.getColumns(); columnEnum.hasMoreElements();) {

			final TableColumn varColumn = columnEnum.nextElement();
			boolean doRemove = false;
			for (CollectableEntityField clctefFixed : stFixedColumns) {
				if (clctefFixed.getLabel().equals(varColumn.getIdentifier())) {
					doRemove = true;
				}
			}
			if (doRemove) {
				columnsToRemove.add(varColumn);
			}
		}

		for (TableColumn columnToRemove : columnsToRemove) {
			resultColumnModel.removeColumn(columnToRemove);
		}
	}

	/**
	 * removes the columns from the fixed table, wich are not in the stFixedColumns
	 */
	private void removeColumnsFromFixedTable() {
		TableColumnModel fixedColumnModel = tblFixedResult.getColumnModel();
		Set<TableColumn> columnsToRemove = new HashSet<TableColumn>();
		for (Enumeration<TableColumn> columnEnum = fixedColumnModel.getColumns(); columnEnum.hasMoreElements();) {

			TableColumn varColumn = columnEnum.nextElement();
			boolean doRemove = true;
			for (CollectableEntityField clctefFixed : stFixedColumns) {
				if (clctefFixed.getLabel().equals(varColumn.getIdentifier())) {
					doRemove = false;
				}
			}
			if (doRemove) {
				columnsToRemove.add(varColumn);
			}
		}

		for (TableColumn columnToRemove : columnsToRemove) {
			fixedColumnModel.removeColumn(columnToRemove);
		}
	}

	private void invalidateFixedTable() {
		getResultTableScrollPane().getRowHeader().setPreferredSize(tblFixedResult.getPreferredSize());
		tblFixedResult.setRowHeight(getResultTable().getRowHeight());
		tblFixedResult.revalidate();
		tblFixedResult.invalidate();
		tblFixedResult.repaint();
		this.revalidate();
		this.invalidate();
		this.repaint();

		getResultTableScrollPane().revalidate();
		getResultTableScrollPane().invalidate();
		getResultTableScrollPane().repaint();

	}

	@Override
	protected void setupTableCellRenderers(JTable table) {
		super.setupTableCellRenderers(table);
		super.setupTableCellRenderers(tblFixedResult);
		invalidateFixedTable();
	}

	private void setFixedTable() {
		tblFixedResult.setRowHeight(getResultTable().getRowHeight());
		getResultTableScrollPane().setRowHeaderView(tblFixedResult);
		getResultTableScrollPane().getRowHeader().setBackground(tblFixedResult.getBackground());
		getResultTableScrollPane().getRowHeader().setPreferredSize(tblFixedResult.getPreferredSize());
		getResultTableScrollPane().setCorner(JScrollPane.UPPER_LEFT_CORNER, tblFixedResult.getTableHeader());

		final TableCellRenderer originalRenderer = tblFixedResult.getTableHeader().getDefaultRenderer();
		TableCellRenderer headerRenderer = new TableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				Component renderComp = originalRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				if (renderComp != null && renderComp.getBackground() != null) {
					renderComp.setBackground(Color.LIGHT_GRAY);
				}
				return renderComp;
			}

			;
		};
				
		tblFixedResult.getTableHeader().setDefaultRenderer(headerRenderer);

		invalidateFixedTable();
	}

	public void setupChangeListenerForResultTableVerticalScrollBar(ProxyList<?> lstgovo, MainFrameTab aFrame) {
		final JScrollBar scrlbarVertical = this.getResultTableScrollPane().getVerticalScrollBar();
		final DefaultBoundedRangeModel model = (DefaultBoundedRangeModel) scrlbarVertical.getModel();
		removePreviousChangeListenersForResultTableVerticalScrollBar(model);

		final JViewport vp = this.getResultTableScrollPane().getViewport();
		model.addChangeListener(new ChangeListenerForResultTableVerticalScrollBar(aFrame, this, model, vp, lstgovo) {
			@Override
			public synchronized void stateChanged(ChangeEvent ev) {
				super.stateChanged(ev);
				invalidateFixedTable();
			}
		});
	}

	public static void removePreviousChangeListenersForResultTableVerticalScrollBar(final DefaultBoundedRangeModel model) {
		for (ChangeListener cl : model.getChangeListeners()) {
			if (cl instanceof VSBChangeListener || cl.getClass().getName().endsWith("BasicScrollPaneUI$Handler")) {
				// remove the change listener that is responsible for synchronizing the scroll pane with changes to the
				// vertical scroll bar. We do the synchronization ourselves, but not before the user releases the knob.
				// In JDK 1.4, it used to be a VSBChangeListener. Since JDK 1.5, it's a private inner class called Handler.
				// @todo Note that this relies on a specific UI implementation.
				// A clean solution would probably be to get rid of the JScrollPane and do the scrolling with our own scrollbar.
				model.removeChangeListener(cl);
			}
			else if (cl instanceof ChangeListenerForResultTableVerticalScrollBar) {
				model.removeChangeListener(cl);
			}
		}
	}

	@Override
	protected Map<CollectableEntityField, Integer> getVisibleColumnWidth(List<? extends CollectableEntityField> lstclctefSelected) {

		final Map<CollectableEntityField, Integer> mpWidths = new HashMap<CollectableEntityField, Integer>(lstclctefSelected.size());
		for (CollectableEntityField clctef : lstclctefSelected) {
			TableColumn column = null;
			try {
				column = getResultTable().getColumn(clctef.getLabel());
			}
			catch (IllegalArgumentException ex) {
				// ignore
				column = tblFixedResult.getColumn(clctef.getLabel());
			}
			if (column != null) {
				mpWidths.put(clctef, column.getWidth());
			}
		}

		return mpWidths;
	}

	@Override
	public Map<String, Integer> getCurrentFieldWithsMap(){
		Map<String, Integer> result = super.getCurrentFieldWithsMap();
		Map<String, Integer> columnWidthsMapFixed = CollectableTableHelper.getColumnWidthsMap(tblFixedResult);
		result.putAll(columnWidthsMapFixed);
		return result;
	}

	@Override
	protected void restoreColumnWidths(List<? extends CollectableEntityField> lstclctefColumns, Map<CollectableEntityField, Integer> mpWidths) {
		// restore the widths of the still present columns:
		for (CollectableEntityField clctef : lstclctefColumns) {
			if (mpWidths.containsKey(clctef)) {
				TableColumn column = null;
				try {
					column = getResultTable().getColumn(clctef.getLabel());
				}
				catch (IllegalArgumentException ex) {
					// ignore
					column = tblFixedResult.getColumn(clctef.getLabel());
				}
				column.setPreferredWidth(mpWidths.get(clctef));
				column.setWidth(mpWidths.get(clctef));
			}
		}
	}

	/**
	 * initializes the <code>fields</code> field.
	 * @param clcte
	 * @param preferences
	 */
	@Override
	public void initializeFields(final ChoiceEntityFieldList fields, final CollectController<Clct> clctctl, final List<CollectableEntityField> lstSelectedNew, final List<CollectableEntityField> lstFixedNew, final Map<CollectableEntityField,Integer> lstColumnWiths) {
		final NuclosCollectController<Clct> elisaController = (NuclosCollectController<Clct>) clctctl;
		final List<CollectableEntityField> lstSelected = new ArrayList<CollectableEntityField>(fields.getSelectedFields());
		final JTable tblResult = getResultTable();
		UIUtils.runCommand(clctctl.getFrame(),
			new CommandHandler() {
				@Override
				public void commandStarted(Component parent) {
					setVisibleTable(false);
				}

				@Override
				public void commandFinished(Component parent) {
					//don't set setVisibleTable(true) here
					//see finishSearchObserver in this.refreshResult(... and this.run(...
				}
			},
			new CommonRunnable() {
			@Override
			@SuppressWarnings("unchecked")
			public void run() throws CommonBusinessException {
				final int iSelRow = tblResult.getSelectedRow();
				NuclosResultPanel.super.initializeFields(fields, clctctl, lstSelectedNew, lstFixedNew, lstColumnWiths);

				NuclosResultPanel.this.stFixedColumns = new HashSet<CollectableEntityField>(lstFixedNew);

				final List<CollectableEntityField> lstSelectedNewAfterMove = (List<CollectableEntityField>) fields.getSelectedFields();
				((SortableCollectableTableModel<Clct>) getResultTable().getModel()).setColumns(lstSelectedNewAfterMove);
				TableColumnModel variableColumnModel = getResultTable().getColumnModel();
				TableColumnModel fixedColumnModel = tblFixedResult.getColumnModel();

				adjustColumnModels(lstSelectedNewAfterMove, variableColumnModel, fixedColumnModel);

				final Collection<CollectableEntityField> collNewlySelected = CollectionUtils.subtract(lstSelectedNewAfterMove, lstSelected);
				if (!collNewlySelected.isEmpty() && !elisaController.getCollectablesInResultAreAlwaysComplete()) {
					// refresh the result:
					refreshResult(clctctl);
				} else {
					setVisibleTable(true);
				}

				// reselect the previously selected row (which gets lost be refreshing the model)
				if (iSelRow != -1) {
					tblResult.setRowSelectionInterval(iSelRow, iSelRow);
				}

				// restore the widths of the still present columns
				restoreColumnWidths(lstSelectedNew, lstColumnWiths);

				invalidateFixedTable();
			}

			private void refreshResult(final CollectController<Clct> clctctl) throws CommonBusinessException {
				//((ElisaCollectController) clctctl).refreshResult();
				Observer finishSearchObserver = new Observer() {
					@Override
					public void update(Observable beobachtbarer, Object arg) {
						setVisibleTable(true);
					}
				};
				List<Observer> lstObserver = new ArrayList<Observer>();
				lstObserver.add(finishSearchObserver);
				((NuclosCollectController<Clct>) clctctl).refreshResult(lstObserver);
			}
		});
	}

	/**
	 * set both(fixed fields / not fixed fields) visible/invisible.
	 * @param visibility
	 */
	@Override
	protected void setVisibleTable(boolean visibility){
		super.setVisibleTable(visibility);
		tblFixedResult.setVisible(visibility);
	}

	/**
	 * command: select columns
	 * Lets the user select the columns to show in the result list.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void cmdSelectColumns(final ChoiceEntityFieldList fields, final CollectController<Clct> clctctl) {
		final NuclosCollectController<Clct> nucleusctl = (NuclosCollectController<Clct>) clctctl;
		final SelectFixedColumnsController ctl = new SelectFixedColumnsController(clctctl.getFrame());
		final List<CollectableEntityField> lstAvailable = fields.getAvailableFields();
		final List<CollectableEntityField> lstSelected = fields.getSelectedFields();
		final ChoiceEntityFieldList ro = new ChoiceEntityFieldList(stFixedColumns);
		ro.set(lstAvailable, lstSelected, nucleusctl.getCollectableEntityFieldComparator());

		getVisibleColumnWidth(lstSelected);

		final boolean bOK = ctl.run(ro,  
				CommonLocaleDelegate.getMessage("SelectColumnsController.1","Anzuzeigende Spalten ausw\u00e4hlen"));

		if (bOK) {
			setSelectColumns(fields, clctctl, ctl.getAvailableObjects(), ctl.getSelectedObjects(), ctl.getFixedObjects());
		}
	}

	protected final void setSelectColumns(final ChoiceEntityFieldList fields, final CollectController<Clct> clctctl, final List<CollectableEntityField> lstAvailableObjects, final List<CollectableEntityField> lstSelectedObjects, final Set<CollectableEntityField> stFixedObjects) {
		final JTable tblResult = getResultTable();
		// remember the widths of the currently visible columns
		final Map<CollectableEntityField, Integer> mpWidths = getVisibleColumnWidth(fields.getSelectedFields());

		UIUtils.runCommand(clctctl.getFrame(),
				new CommandHandler() {
					@Override
					public void commandStarted(Component parent) {
						setVisibleTable(false);
					}

					@Override
					public void commandFinished(Component parent) {
						//don't set setVisibleTable(true) here
						//see finishSearchObserver in this.refreshResult(... and this.run(...
					}
				},
				new CommonRunnable() {
				@Override
				@SuppressWarnings("unchecked")
				public void run() throws CommonBusinessException {
					final int iSelRow = tblResult.getSelectedRow();
					final List<CollectableEntityField> lstSelectedOld = (List<CollectableEntityField>) fields.getSelectedFields();
					fields.set(lstAvailableObjects, lstSelectedObjects, clctctl.getCollectableEntityFieldComparator());

					final List<CollectableEntityField> lstSelectedNew = (List<CollectableEntityField>) fields.getSelectedFields();
					NuclosResultPanel.this.stFixedColumns = stFixedObjects;

					((SortableCollectableTableModel<Clct>) getResultTable().getModel()).setColumns(lstSelectedNew);
					TableColumnModel variableColumnModel = getResultTable().getColumnModel();
					TableColumnModel fixedColumnModel = tblFixedResult.getColumnModel();

					adjustColumnModels(lstSelectedNew, variableColumnModel, fixedColumnModel);

					final Collection<? extends CollectableEntityField> collNewlySelected = CollectionUtils.subtract(lstSelectedNew, lstSelectedOld);
					if (!collNewlySelected.isEmpty() && !((NuclosCollectController<Clct>)clctctl).getCollectablesInResultAreAlwaysComplete()) {
						// refresh the result:
						refreshResult(clctctl);
					} else {
						setVisibleTable(true);
					}

					// reselect the previously selected row (which gets lost be refreshing the model)
					if (iSelRow != -1) {
						tblResult.setRowSelectionInterval(iSelRow, iSelRow);
					}

					// restore the widths of the still present columns
					restoreColumnWidths(lstSelectedNew, mpWidths);

					// write preferences after column width was restored
					writeFieldWidthsToPreferences(clctctl.getPreferences());

					invalidateFixedTable();

					setupTableCellRenderers(tblResult);
				}

				private void refreshResult(final CollectController<Clct> clctctl) throws CommonBusinessException {
					Observer finishSearchObserver = new Observer() {
						@Override
						public void update(Observable beobachtbarer, Object arg) {
							setVisibleTable(true);
						}
					};
					List<Observer> lstObserver = new ArrayList<Observer>();
					lstObserver.add(finishSearchObserver);
					((NuclosCollectController<Clct>) clctctl).refreshResult(lstObserver);
				}

			});
	}

	private void adjustColumnModels(final List<? extends CollectableEntityField> lstclctefSelected,
			TableColumnModel columnmodelVariable, TableColumnModel columnmodelFixed) {

		for (CollectableEntityField clctefSelected : lstclctefSelected) {
			if (NuclosResultPanel.this.stFixedColumns.contains(clctefSelected)) {
				try {
					final int iSelectedIndex = columnmodelVariable.getColumnIndex(clctefSelected.getLabel());
					columnmodelVariable.removeColumn(columnmodelVariable.getColumn(iSelectedIndex));
				}
				catch (IllegalArgumentException ex) {
					// ignore
				}
			}
			else {
				try {
					final int iSelectedIndex = columnmodelFixed.getColumnIndex(clctefSelected.getLabel());
					columnmodelFixed.removeColumn(columnmodelFixed.getColumn(iSelectedIndex));
				}
				catch (IllegalArgumentException ex) {
					// ignore
				}
			}
		}
	}

	@Override
	protected void cmdAddColumn(ChoiceEntityFieldList fields, TableColumn columnBefore, String sFieldNameToAdd) throws CommonBusinessException {
		super.cmdAddColumn(fields, columnBefore, sFieldNameToAdd);
		final TableColumnModel columnmodelVariable = getResultTable().getColumnModel();
		final TableColumnModel columnmodelFixed = tblFixedResult.getColumnModel();
		adjustColumnModels(fields.getSelectedFields(), columnmodelVariable, columnmodelFixed);
	}

	@Override
	protected void cmdRemoveColumn(ChoiceEntityFieldList fields, CollectableEntityField clctef, CollectController<Clct> ctl) {
		super.cmdRemoveColumn(fields, clctef, ctl);
		stFixedColumns.remove(clctef);
//		final TableColumnModel columnmodelVariable = getResultTable().getColumnModel();
//		final TableColumnModel columnmodelFixed = tblFixedResult.getColumnModel();
//		adjustColumnModels(fields.getSelectedFields(), columnmodelVariable, columnmodelFixed);

		List<CollectableEntityField> lstAvailableFields = new ArrayList<CollectableEntityField>(fields.getAvailableFields());
		//Collections.copy(lstAvailableFields, (List<CollectableEntityField>)fields.getAvailableFields());
		List<CollectableEntityField> lstSelectedFields = new ArrayList<CollectableEntityField>(fields.getSelectedFields());
		//Collections.copy(lstSelectedFields, (List<CollectableEntityField>)fields.getSelectedFields());
		setSelectColumns(fields, ctl, lstAvailableFields, lstSelectedFields, stFixedColumns);
	}

	/**
	 * command: export
	 * export selected data
	 */
	@Override
	public void cmdExport(final CollectController<Clct> clctctl) {
		final JFileChooser filechooser = NuclosResultPanel.this.getFileChooser(
			CommonLocaleDelegate.getMessage("NuclosResultPanel.1", "Export"), CommonLocaleDelegate.getMessage("NuclosResultPanel.2", "Exportieren in selektierte Datei"), clctctl);
		final JCheckBox cbxDeepExport = new JCheckBox(CommonLocaleDelegate.getMessage("NuclosResultPanel.3", "Tiefer Export?"));
		
		filechooser.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if("ancestor".equals(evt.getPropertyName()))
					filechooser.getParent().add(cbxDeepExport, BorderLayout.SOUTH);
			}
		});
		
		if (filechooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
			final File file = filechooser.getSelectedFile();
			CommonBackgroundProcessClientWorkerAdapter<Clct> workerAdapter = new CommonBackgroundProcessClientWorkerAdapter<Clct>(clctctl) {

				final Logger log = Logger.getLogger(NuclosResultPanel.class);
				BackgroundProcessTableEntry entry;

				@Override
				public void init() throws CommonBusinessException {
					super.init();
				}

				@Override
				public void work() throws CommonBusinessException{
					if (file == null) {
						throw new NuclosFatalException(CommonLocaleDelegate.getMessage("NuclosResultPanel.4", "Bitte geben Sie einen Dateinamen f\u00fcr den Export ein!"));
					}

					getTransferPreferences().put(PREFS_KEY_LASTXMLTRANSFERPATH, file.getAbsolutePath());

					final Map<Integer, String> mpExportData = new HashMap<Integer, String>();

					for (Collectable clct : clctctl.getSelectedCollectables()) {
						mpExportData.put((Integer)clct.getId(), clctctl.getEntityName());
					}


					if (!mpExportData.isEmpty()) {
						String sExportAbsolutePath = file.getAbsolutePath();
						if (!sExportAbsolutePath.toLowerCase().endsWith(EXPORT_IMPORT_EXTENSION)) {
							sExportAbsolutePath = sExportAbsolutePath += EXPORT_IMPORT_EXTENSION;
						}

						String sFileName = file.getName();
						if (!sFileName.toLowerCase().endsWith(EXPORT_IMPORT_EXTENSION)) {
							sFileName += EXPORT_IMPORT_EXTENSION;
						}

						Boolean bDeepExport = cbxDeepExport.isSelected();

						try {
							org.nuclos.common2.File file = XmlExportDelegate.getInstance().xmlExport(mpExportData, bDeepExport, sFileName);
							IOUtils.writeToBinaryFile(new File(sExportAbsolutePath), file.getContents());
							file = null;

							postXMLExport(clctctl);
						}
						catch (CommonBusinessException e) {
							throw new CommonBusinessException(e);
						}
						catch (IOException e) {
							throw new NuclosFatalException(e);
						}
					}

				}

				@Override
				public void setBackgroundProcessTableEntry(BackgroundProcessTableEntry backgroundProcessTableEntry) {
					this.entry = backgroundProcessTableEntry;
				}

				private void setBackgroundProcessFinishedStatus(final BackgroundProcessTableEntry entry, final BackgroundProcessInfo.Status status, final String statusMessage) {
					entry.setStatus(status);
					entry.setMessage(statusMessage);
					log.debug("Set xml export status to "+status + " because "+statusMessage);
				}

				@Override
				public void paint() throws CommonBusinessException {
					super.paint();
					if(this.entry != null && this.entry.getStatus() == BackgroundProcessInfo.Status.CANCELLED){
						setBackgroundProcessFinishedStatus(entry, BackgroundProcessInfo.Status.ERROR, CommonLocaleDelegate.getMessage("NuclosResultPanel.5", "Der Export wurde abgebrochen. Es wurden keine Daten exportiert"));
					}
					else if(this.entry != null && this.entry.getStatus() == BackgroundProcessInfo.Status.ERROR){
						setBackgroundProcessFinishedStatus(entry, BackgroundProcessInfo.Status.ERROR, CommonLocaleDelegate.getMessage("NuclosResultPanel.6", "Der Export ist fehlgeschlagen."));
					}
					else {
						setBackgroundProcessFinishedStatus(entry, BackgroundProcessInfo.Status.DONE, CommonLocaleDelegate.getMessage("NuclosResultPanel.7", "Der Export wurde erfolgreich abgeschlossen."));
					}
				}

				@Override
				public void handleError(Exception ex) {
					entry.setStatus(BackgroundProcessInfo.Status.ERROR);
					Errors.getInstance().showExceptionDialog(clctctl.getFrame(), ex);
				}
			};

			final BackgroundProcessStatusDialog dlgStatus = BackgroundProcessStatusController.getStatusDialog(UIUtils.getFrameForComponent(clctctl.getFrame().getParent()));
			Future<?> future = CommonMultiThreader.getInstance().executeInterruptible(workerAdapter);
			BackgroundProcessTableEntry entry = new BackgroundProcessTableEntry(CommonLocaleDelegate.getMessage("NuclosResultPanel.18", "XML-Export"), BackgroundProcessInfo.Status.RUNNING, DateUtils.now(), future);
			workerAdapter.setBackgroundProcessTableEntry(entry);
			dlgStatus.getStatusPanel().getModel().addEntry(entry);
			dlgStatus.setVisible(true);
		}
	}

	/**
	 * this method is called after the data was exported successfully
	 */
	protected void postXMLExport(final CollectController<Clct> clctctl) {
		// do nothing
	}

	/**
	 * command: import
	 * import data
	 */
	@Override
	public void cmdImport(final CollectController<Clct> clctctl) {
		final JFileChooser filechooser = this.getFileChooser(CommonLocaleDelegate.getMessage("NuclosResultPanel.8", "Import"), CommonLocaleDelegate.getMessage("NuclosResultPanel.9", "Importieren der selektierten Datei"), clctctl);
		final int iBtn = filechooser.showOpenDialog(getParent());
		if (iBtn == JFileChooser.APPROVE_OPTION) {

			if (filechooser.getSelectedFile() == null) {
				throw new NuclosFatalException(CommonLocaleDelegate.getMessage("NuclosResultPanel.10", "Bitte geben Sie einen Dateinamen f\u00fcr den Import ein!"));
			}

			String sMessage = CommonLocaleDelegate.getMessage("NuclosResultPanel.11", "Sollen die Daten der ausgew\u00e4hlten Datei [{0}] wirklich importiert werden?", filechooser.getSelectedFile().getName());
			final int iBtnOption = JOptionPane.showConfirmDialog(this, sMessage, CommonLocaleDelegate.getMessage("NuclosResultPanel.12", "Entit\u00e4tsdaten-Import"),
					JOptionPane.OK_CANCEL_OPTION);
			if (iBtnOption != JOptionPane.OK_OPTION) {
				return;
			}

			CommonBackgroundProcessClientWorkerAdapter<Clct> workerAdapter = new CommonBackgroundProcessClientWorkerAdapter<Clct>(clctctl) {

				final Logger log = Logger.getLogger(NuclosResultPanel.class);
				BackgroundProcessTableEntry entry;

				@Override
				public void init() throws CommonBusinessException {
					super.init();
				}

				@Override
				public void work() throws CommonBusinessException{
					getTransferPreferences().put(PREFS_KEY_LASTXMLTRANSFERPATH, filechooser.getSelectedFile().getAbsolutePath());
					String sEntityName = clctctl.getEntityName();

					try {
						byte[] abContents = IOUtils.readFromBinaryFile(filechooser.getSelectedFile());
						org.nuclos.common2.File file = new org.nuclos.common2.File(filechooser.getSelectedFile().getName(), abContents);

						XmlImportDelegate.getInstance().xmlImport(sEntityName, file);
						file = null;
						abContents = null;

						postXMLImport(clctctl);
					}
					catch (IOException e) {
						throw new NuclosFatalException(e);
					}
				}

				@Override
				public void setBackgroundProcessTableEntry(BackgroundProcessTableEntry backgroundProcessTableEntry) {
					this.entry = backgroundProcessTableEntry;
				}

				private void setBackgroundProcessFinishedStatus(final BackgroundProcessTableEntry entry, final BackgroundProcessInfo.Status status, final String statusMessage) {
					// set the status in status dialog:
					entry.setStatus(status);
					entry.setMessage(statusMessage);
					log.debug("Set xml import status to "+status + " because "+statusMessage);
				}

				@Override
				public void paint() throws CommonBusinessException {
					super.paint();
					if(this.entry != null && this.entry.getStatus() == BackgroundProcessInfo.Status.CANCELLED){
						setBackgroundProcessFinishedStatus(entry, BackgroundProcessInfo.Status.ERROR, CommonLocaleDelegate.getMessage("NuclosResultPanel.13", "Der Import wurde abgebrochen. Es wurden keine Daten importiert"));
					}
					else if(this.entry != null && this.entry.getStatus() == BackgroundProcessInfo.Status.ERROR){
						setBackgroundProcessFinishedStatus(entry, BackgroundProcessInfo.Status.ERROR, CommonLocaleDelegate.getMessage("NuclosResultPanel.14", "Der Import ist fehlgeschlagen."));
					}
					else {
						setBackgroundProcessFinishedStatus(entry, BackgroundProcessInfo.Status.DONE, CommonLocaleDelegate.getMessage("NuclosResultPanel.15", "Der Import wurde erfolgreich abgeschlossen."));
					}
				}

				@Override
				public void handleError(Exception ex) {
					entry.setStatus(BackgroundProcessInfo.Status.ERROR);
					Errors.getInstance().showExceptionDialog(clctctl.getFrame(), ex);
				}
			};

			final BackgroundProcessStatusDialog dlgStatus = BackgroundProcessStatusController.getStatusDialog(UIUtils.getFrameForComponent(clctctl.getFrame().getParent()));
			Future<?> future = CommonMultiThreader.getInstance().executeInterruptible(workerAdapter);
			BackgroundProcessTableEntry entry = new BackgroundProcessTableEntry(CommonLocaleDelegate.getMessage("NuclosResultPanel.17", "XML-Import"), BackgroundProcessInfo.Status.RUNNING, DateUtils.now(), future);
			workerAdapter.setBackgroundProcessTableEntry(entry);
			dlgStatus.getStatusPanel().getModel().addEntry(entry);
			dlgStatus.setVisible(true);
		}
	}

	/**
	 * this method is called after the data was imported successfully
	 */
	protected void postXMLImport(final CollectController<Clct> clctctl) {
		clctctl.cmdRefreshResult();
	}

	/**
	 * @return FileChooser for import/export
	 */
	protected final JFileChooser getFileChooser(String sTitle, String sTootltip, CollectController<Clct> clctctl) {
		final JFileChooser result = new JFileChooser(getTransferPreferences().get(PREFS_KEY_LASTXMLTRANSFERPATH, null));
		result.setApproveButtonText(sTitle);
		result.setApproveButtonMnemonic(sTitle.toCharArray()[0]);
		result.setApproveButtonToolTipText(sTootltip);
		result.addChoosableFileFilter(filefilter);
		return result;
	}

	protected final FileFilter filefilter = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return file.isDirectory() || file.getName().toLowerCase().endsWith(EXPORT_IMPORT_EXTENSION);
		}

		@Override
		public String getDescription() {
			return CommonLocaleDelegate.getMessage("NuclosResultPanel.16", "Komprimierte Dateien (*.zip)");
		}
	};

	/**
	 * @return the user preferences node for transfer operations
	 */
	public Preferences getTransferPreferences() {
		return ClientPreferences.getUserPreferences().node("transfer");
	}

	public Future<?> executeInterruptible(Runnable runnable) {
		return cachedThreadPoolExecutor.submit(runnable);
	}

	@Override
	protected void writeFieldWidthsToPreferences(Preferences preferences) throws PreferencesException {
		super.writeFieldWidthsToPreferences(preferences);

		PreferencesUtils.putIntegerList(preferences, PREFS_NODE_FIXEDFIELDS_WIDTHS, CollectableTableHelper.getColumnWidths(tblFixedResult));
	}

	@Override
	protected void setColumnWidths(JTable tblResult, boolean bUseCustomColumnWidths, Preferences preferences) {
		super.setColumnWidths(tblResult, bUseCustomColumnWidths, preferences);

		final List<Integer> lstWidthsFromPreferences = getFixedTableColumnWidthsFromPreferences(preferences);
		setColumnWiths(lstWidthsFromPreferences);
	}

	private void setColumnWiths(final List<Integer> lstWidths) {
		boolean bUseCustomColumnWidths;
		bUseCustomColumnWidths = !lstWidths.isEmpty() && lstWidths.size() == tblFixedResult.getColumnCount();
		if (bUseCustomColumnWidths) {
			Logger.getLogger(this.getClass()).debug("Restoring column widths from user preferences");
			final Enumeration<TableColumn> enumeration = tblFixedResult.getColumnModel().getColumns();
			int iColumn = 0;
			while (enumeration.hasMoreElements()) {
				final TableColumn column = enumeration.nextElement();
				final int iPreferredCellWidth = lstWidths.get(iColumn++);
				column.setPreferredWidth(iPreferredCellWidth);
				column.setWidth(iPreferredCellWidth);
			}
		}
		else {
			// If there are no stored field widths or the number of stored field widths differs from the column count
			// (that is, the number of columns has changed since the last invocation of the client), set optimal column widths:
			Logger.getLogger(this.getClass()).debug("Setting optimal column widths");
			TableUtils.setOptimalColumnWidths(tblFixedResult);
			// use custom column widths as soon as a column width was changed after setting the optimal column width:
		}
	}

	/**
	 * @return the table columns widths. If there are stored user preferences, the sizes will be restored.
	 * Size and order of list entries is determined by number and order of visible columns
	 */
	private List<Integer> getFixedTableColumnWidthsFromPreferences(Preferences preferences) {
		List<Integer> result;
		try {
			result = PreferencesUtils.getIntegerList(preferences, PREFS_NODE_FIXEDFIELDS_WIDTHS);
		}
		catch (PreferencesException ex) {
			result = new ArrayList<Integer>();
		}

		return result;
	}

	public Set<CollectableEntityField> getFixedColumns() {
		return stFixedColumns;
	}

	@Override
	protected void addDoubleClickMouseListener(MouseListener mouselistener) {
		super.addDoubleClickMouseListener(mouselistener);
		tblFixedResult.addMouseListener(mouselistener);
	}

	@Override
	protected void removeDoubleClickMouseListener(MouseListener mouselistener) {
		super.removeDoubleClickMouseListener(mouselistener);
		tblFixedResult.removeMouseListener(mouselistener);
	}
	
	@Override
	protected void addPopupMenuListener() {
		super.addPopupMenuListener();

		// popup menu for rows:
		tblFixedResult.addMouseListener(new PopupMenuRowsListener(popupmenuRow, tblFixedResult));
	}

	/** called when a column was moved in the header*/
	@Override
	public void columnMovedInHeader(ChoiceEntityFieldList fields) {
		final List<CollectableEntityField> allColumns = CollectableTableHelper.getCollectableEntityFieldsFromColumns(this.tblFixedResult);
		allColumns.addAll(CollectableTableHelper.getCollectableEntityFieldsFromColumns(this.getResultTable()));
		fields.setSelectedFields(allColumns);
	}

	@Override
    protected void setupCopyAction() {
		//override copy Action for both tables
		final ActionMap am = new ActionMap();
		am.put("copy", new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent ev) {
				final StringBuffer sb = new StringBuffer();

				for (int iSelectedRow : tblFixedResult.getSelectedRows()) {
					sb.append(getColumnData(tblFixedResult, iSelectedRow));
					if (tblFixedResult.getColumnCount() > 0 && NuclosResultPanel.this.getResultTable().getColumnCount() > 0) {
						sb.append("\t");
					}
					sb.append(getColumnData(NuclosResultPanel.this.getResultTable(), iSelectedRow));
					sb.append("\n");
				}
				final StringSelection stsel = new StringSelection(sb.toString());
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stsel, stsel);
			}

			@SuppressWarnings("unchecked")
			private StringBuffer getColumnData(final JTable table, final int iSelectedRow) {
				final int iColumnCount = table.getColumnCount();
				final StringBuffer sb = new StringBuffer();
				
				Integer iStatusId = null;
				SortableCollectableTableModel<Clct> tablemodel = null;
				
				try {
					if (table.getModel() instanceof SortableCollectableTableModel<?>) {
						tablemodel = (SortableCollectableTableModel<Clct>)table.getModel();
						Clct clct = tablemodel.getCollectable(iSelectedRow);
						CollectableField clctfield = clct.getField(NuclosEOField.STATE.getMetaData().getField() );
						iStatusId = (clctfield != null) ? (Integer)clctfield.getValueId() : null;
					}
				}
				catch (CommonFatalException e) {
					// thrown, when no field "nuclosState" is given for the current collectable,
					// especially the collectable is a masterdata entity
					// in this cases just go on working
				}
				
				for (int iColumn = 0; iColumn < iColumnCount; iColumn++) {
					boolean readAllowed = true;
					if (iStatusId != null && tablemodel != null) {
						CollectableEntityField clctef = tablemodel.getCollectableEntityField(table.convertColumnIndexToModel(iColumn));

						Permission permission = null;
						
						if (clctef instanceof CollectableEntityFieldWithEntityForExternal) {
							// check subform permission
							if (((CollectableEntityFieldWithEntityForExternal)clctef).fieldBelongsToSubEntity()) {
								String sEntityName = ((CollectableEntityFieldWithEntityForExternal)clctef).getCollectableEntityName();
								permission = SecurityCache.getInstance().getSubFormPermission(sEntityName, iStatusId);
							}
							// check attribute permission
							else {
								permission = SecurityCache.getInstance().getAttributePermission(clctef.getCollectableEntity().getName(), clctef.getName(), iStatusId);
							}
							
							readAllowed = (permission == null) ? false : true;
						}
					}
					
					if (readAllowed) {
						sb.append(table.getValueAt(iSelectedRow, iColumn));
					}
					else {
						sb.append("\t");
					}
					
					if (iColumn < iColumnCount - 1) {
						sb.append("\t");
					}
				}
				return sb;
			}
		});
		am.setParent(tblFixedResult.getActionMap());
		tblFixedResult.setActionMap(am);
		this.getResultTable().setActionMap(am);
    }

}	// class NuclosResultPanel
