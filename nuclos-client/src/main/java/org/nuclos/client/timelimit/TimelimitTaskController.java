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
package org.nuclos.client.timelimit;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;
import org.nuclos.client.common.KeyBinding;
import org.nuclos.client.common.KeyBindingProvider;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.explorer.ExplorerController;
import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.genericobject.ReportController;
import org.nuclos.client.genericobject.datatransfer.GenericObjectIdModuleProcess;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.task.RefreshableTaskController;
import org.nuclos.client.task.ScheduledRefreshable;
import org.nuclos.client.task.TaskController;
import org.nuclos.client.ui.CommonAbstractAction;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.MainFrameTabAdapter;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectableTableHelper;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.CollectableComponentFactory;
import org.nuclos.client.ui.popupmenu.DefaultJPopupMenuListener;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.PreferencesException;
import org.nuclos.server.common.valueobject.TimelimitTaskVO;

/**
 * Controller for <code>TimelimitTaskView</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Uwe.Allner@novabit.de">Uwe.Allner</a>
 * @version 01.00.00
 * @todo refactor (copied from PersonalTaskController)
 */

public class TimelimitTaskController extends RefreshableTaskController {
	
	private static final Logger LOG = Logger.getLogger(TimelimitTaskController.class);

	private final static String PREFS_NODE_TIMELIMITTASKS = "timelimitTasks";
	private static final String PREFS_NODE_SELECTEDFIELDS = "selectedFields";
	private static final String PREFS_NODE_SELECTEDFIELDWIDTHS = "selectedFieldWidths";

	/**
	 * popup menu for list of timelimit tasks
	 */
	private final TimelimitTasksPopupMenu popupTimelimit;

	private final TimelimitTaskView timelimittaskview;

	private final TimelimitTaskDelegate tltaskDelegate;

	private ExplorerController ctlExplorer;

	private final Preferences prefs;
	
	private MainFrameTab tab;

	private final Action actPerformTask = new CommonAbstractAction("Zugeordnete Objekte anzeigen", Icons.getInstance().getIconModule(),
		CommonLocaleDelegate.getMessage("TimelimitTaskController.16","Zugeordnete Objekte anzeigen")) {

		@Override
		public void actionPerformed(ActionEvent ev) {
			TimelimitTaskController.this.cmdPerformTimelimitTask(timelimittaskview);
		}
	};

	private final Action actRemoveTask = new CommonAbstractAction("L\u00f6schen...", Icons.getInstance().getIconDelete16(),
		CommonLocaleDelegate.getMessage("TimelimitTaskController.2","Ausgew\u00e4hlte Frist l\u00f6schen")) {

		@Override
		public void actionPerformed(ActionEvent ev) {
			TimelimitTaskController.this.cmdRemoveTimelimitTask(timelimittaskview, true);
		}
	};

	private final Action actFinishTask = new CommonAbstractAction("Erledigt", Icons.getInstance().getIconProperties16(),
		CommonLocaleDelegate.getMessage("TimelimitTaskController.1","Ausgew\u00e4hlte Frist als erledigt/unerledigt markieren")) {

		@Override
		public void actionPerformed(ActionEvent ev) {
			TimelimitTaskController.this.cmdFinishOrUnfinishTimelimitTask(timelimittaskview, true);
		}
	};

	final Action actPrint = new CommonAbstractAction(CommonLocaleDelegate.getMessage("TimelimitTaskController.11","Fristenliste drucken"), 
		Icons.getInstance().getIconPrintReport16(), null) {

		@Override
		public void actionPerformed(ActionEvent ev) {
			TimelimitTaskController.this.cmdPrintTimelimitTaskView();
		}
	};

	public TimelimitTaskController(Component parent, Preferences prefsParent, TimelimitTaskDelegate tltaskDelegate) {
		super(parent);

		this.timelimittaskview = new TimelimitTaskView();
		this.tltaskDelegate = tltaskDelegate;
		this.popupTimelimit = new TimelimitTasksPopupMenu(timelimittaskview);
		this.prefs = prefsParent.node(PREFS_NODE_TIMELIMITTASKS);

		this.timelimittaskview.setTimelimitTaskTableModel(newTimelimitTaskTableModel());
		final JTable tblTimelimit = timelimittaskview.getTable();

		/** todo calculate row height */
		tblTimelimit.setRowHeight(20);

		setupRenderers(tblTimelimit);
		setupColumnModelListener(tblTimelimit);
		setupActions(tblTimelimit);
		setupDataTransfer(tblTimelimit);
		
		KeyBinding keybinding = KeyBindingProvider.REFRESH;
		this.timelimittaskview.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keybinding.getKeystroke(), keybinding.getKey());
		this.timelimittaskview.getActionMap().put(keybinding.getKey(), new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				TimelimitTaskController.this.cmdRefreshTimelimitTaskView();
			}
		});
	}

	private void setupRenderers(JTable table) {
		// setup a table cell renderer for each column:
		for (Enumeration<TableColumn> enumeration = table.getColumnModel().getColumns(); enumeration.hasMoreElements();) {
			final TableColumn column = enumeration.nextElement();
			final int iModelIndex = column.getModelIndex();
			final CollectableEntityField clctef = this.timelimittaskview.getTimelimitTaskTableModel().getCollectableEntityField(iModelIndex);
			final CollectableComponent clctcomp = CollectableComponentFactory.getInstance().newCollectableComponent(clctef, null, false);
			column.setCellRenderer(clctcomp.getTableCellRenderer());
		}
	}

	private void setupColumnModelListener(final JTable tbl) {
		tbl.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
			@Override
			public void columnAdded(TableColumnModelEvent ev) {
				// do nothing
			}

			@Override
			public void columnRemoved(TableColumnModelEvent ev) {
				// do nothing
			}

			@Override
			public void columnMoved(TableColumnModelEvent ev) {
				if (ev.getFromIndex() != ev.getToIndex()) {
					try {
						TimelimitTaskController.this.storeColumnOrder(tbl);
					}
					catch (PreferencesException ex) {
						final String sMessage = CommonLocaleDelegate.getMessage("TimelimitTaskController.7","Die Spaltenreihenfolge konnte nicht gespeichert werden.");
						Errors.getInstance().showExceptionDialog(tbl, sMessage, ex);
					}
				}
			}

			@Override
			public void columnMarginChanged(ChangeEvent ev) {
				try {
					storeColumnWidthsInPrefs(tbl);
				}
				catch(PreferencesException ex) {
					final String sMessage = CommonLocaleDelegate.getMessage("PersonalTaskController.28","Die Spaltenbreite konnte nicht gespeichert werden.");
					Errors.getInstance().showExceptionDialog(tbl, sMessage, ex);
				}
			}

			@Override
			public void columnSelectionChanged(ListSelectionEvent ev) {
				// do nothing
			}
		});
	}

	/**
	 * stores the order of the columns in the table
	 */
	private void storeColumnOrder(JTable tbl) throws PreferencesException {
		this.storeSelectedFieldNamesInPrefs(CollectableTableHelper.getFieldNamesFromColumns(tbl));
	}

	/**
	 * stores the selected columns (fields) in user preferences
	 */
	private void storeSelectedFieldNamesInPrefs(List<String> lstSelectedFieldNames) throws PreferencesException {
		PreferencesUtils.putStringList(this.prefs, PREFS_NODE_SELECTEDFIELDS, lstSelectedFieldNames);
	}
	
	private void storeColumnWidthsInPrefs(JTable tbl) throws PreferencesException {
		PreferencesUtils.putIntegerList(this.prefs, PREFS_NODE_SELECTEDFIELDWIDTHS, CollectableTableHelper.getColumnWidths(tbl));
	}

	private void setupActions(final JTable table) {
		super.addRefreshIntervalActionsToSingleScheduledRefreshable();
		
		// assign popup menu:
		table.addMouseListener(new PopupMenuListener(this.popupTimelimit, table));

		// add mouse listeners for double click in table:
		table.addMouseListener(new DoubleClickListener(timelimittaskview));

		final Action actRefresh = new CommonAbstractAction(Icons.getInstance().getIconRefresh16(),
			CommonLocaleDelegate.getMessage("TimelimitTaskController.10","Fristenliste aktualisieren")) {

			@Override
			public void actionPerformed(ActionEvent ev) {
				TimelimitTaskController.this.cmdRefreshTimelimitTaskView();
			}
		};

		actPerformTask.setEnabled(false);
		actRemoveTask.setEnabled(false);
		actFinishTask.setEnabled(false);

		timelimittaskview.getRefreshButton().setAction(actRefresh);
		timelimittaskview.getPerformMenuItem().setAction(actPerformTask);
		timelimittaskview.getRemoveMenuItem().setAction(actRemoveTask);
		timelimittaskview.getFinishButton().setAction(actFinishTask);
		timelimittaskview.getPrintMenuItem().setAction(actPrint);

		// use only action listener for "show finished" button:
		timelimittaskview.btnShowAllTasks.addActionListener(actRefresh);

		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent ev) {
				if (!ev.getValueIsAdjusting()) {
					final ListSelectionModel lsm = (ListSelectionModel) ev.getSource();
					final boolean bSelectionEmpty = lsm.isSelectionEmpty();
					final Boolean bFinished;
					final Boolean bHasGenericObject;

					if (bSelectionEmpty) {
						bFinished = null;
						bHasGenericObject = null;
					}
					else {
						final int iSelectedRow = TimelimitTaskController.this.timelimittaskview.getTable().getSelectedRow();
						CollectableTimelimitTask taskclct = TimelimitTaskController.this.timelimittaskview.getTimelimitTaskTableModel().getCollectable(iSelectedRow);
						final TimelimitTaskVO taskvo = taskclct.getTimelimitTaskVO();
						bFinished = Boolean.valueOf(taskvo.getCompleted() != null);
						bHasGenericObject = Boolean.valueOf(taskvo.getGenericObjectId() != null);
					}

					final boolean bTaskSelected = !bSelectionEmpty;
					final boolean bFinishedTaskSelected = bTaskSelected && bFinished.booleanValue();

					actRemoveTask.setEnabled(bTaskSelected);
					actPerformTask.setEnabled(bTaskSelected && bHasGenericObject.booleanValue());
					actFinishTask.setEnabled(bTaskSelected);
					timelimittaskview.getFinishButton().setSelected(bFinishedTaskSelected);
				}
			}
		});
	}

	private void setupDataTransfer(JTable table) {
		table.setTransferHandler(new TransferHandler(this.getParent()));
	}

	private TimelimitTaskTableModel newTimelimitTaskTableModel() {
		final TimelimitTaskTableModel result = new TimelimitTaskTableModel();

		// add columns:
		final List<String> lstFieldNames = new LinkedList<String>(CollectableTimelimitTask.clcte.getFieldNames());

		// sort using the order stored in the preferences:
		List<String> lstFieldNameOrderTemp;
		try {
			lstFieldNameOrderTemp = PreferencesUtils.getStringList(this.prefs, PREFS_NODE_SELECTEDFIELDS);
		}
		catch (PreferencesException ex) {
			LOG.error("Failed to retrieve list of selected fields from the preferences. They will be empty.");
			lstFieldNameOrderTemp = new ArrayList<String>();
		}
		final List<String> lstFieldNameOrder = lstFieldNameOrderTemp;

		Collections.sort(lstFieldNames, new Comparator<String>() {
			private int getOrder(Object o) {
				int result = lstFieldNameOrder.indexOf(o);
				if (result == -1) {
					// new fields are shown at the end:
					result = lstFieldNameOrder.size();
				}
				return result;
			}

			@Override
			public int compare(String o1, String o2) {
				int iDiff = getOrder(o1) - getOrder(o2);
				return (iDiff == 0) ? 0 : (iDiff / Math.abs(iDiff));
			}
		});

		int iColumn = 0;
		final List<String> lstNamesOfFieldsToDisplay = CollectableTimelimitTask.clcte.getNamesOfFieldsToDisplay();
		for (Iterator<String> iter = lstFieldNames.iterator(); iter.hasNext();) {
			final String sFieldName = iter.next();
			final CollectableEntityField clctef = CollectableTimelimitTask.clcte.getEntityField(sFieldName);
			if (lstNamesOfFieldsToDisplay.contains(sFieldName)) {
				result.addColumn(iColumn++, clctef);
			}
		}

		if (result.getColumnCount() > 0) {
			result.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.ASCENDING)), false);
		}

		return result;
	}
	
	private void removeTimelimitTaskTab() {
		this.tab = null;
	}
	
	public void cmdShowTimelimitTasks() {
		if (tab == null) {
			UIUtils.runCommand(this.getParent(), new Runnable() {
				@Override
	            public void run() {
					try {
						showTimelimitTasks(new MainFrameTab());
						MainFrame.addTab(tab);
					}
					catch (Exception e) {
						LOG.error("cmdShowTimelimitTasks failed: " + e, e);
					}
				}
			});
		} 
		
		MainFrame.setSelectedTab(tab);
	}
	
	public void showTimelimitTasks(MainFrameTab tab) {
		if (this.tab != null) {
			throw new IllegalArgumentException("Timelimit Tasks already shown");
		}
		tab.addMainFrameTabListener(new MainFrameTabAdapter(){
			@Override
			public boolean tabClosing(MainFrameTab tab) {
				removeTimelimitTaskTab();
				return true;
			}
			@Override
			public void tabClosed(MainFrameTab tab) {
				tab.removeMainFrameTabListener(this);
			}
		});
		
		tab.setTitle(CommonLocaleDelegate.getMessage("tabTimelimits", "Fristen"));
		tab.setTabIcon(Icons.getInstance().getIconTabTimtlimit());
		tab.setLayeredComponent(TimelimitTaskController.this.timelimittaskview);
		tab.setTabStoreController(new TaskController.TaskTabStoreController(TaskController.RestorePreferences.TIMELIMIT, TimelimitTaskController.this.timelimittaskview));
		TimelimitTaskController.this.tab = tab;
	}

	/**
	 * finishes or "unfinishes" the selected task
	 * @param taskview
	 * @param bRefresh Refresh the lists afterwards?
	 */
	private void cmdFinishOrUnfinishTimelimitTask(final TimelimitTaskView taskview, final boolean bRefresh) {
		List<TimelimitTaskVO> lstSelectedTimelimitTasks = getSelectedTimelimitTasks(taskview);
		if(!lstSelectedTimelimitTasks.isEmpty() && lstSelectedTimelimitTasks !=null){
			final boolean bFinished = areTasksCompleted(lstSelectedTimelimitTasks);
			if (bFinished) {
				cmdUnfinishTimelimitTask(taskview, bRefresh);
			}
			else {
				cmdFinishTimelimitTask(taskview, bRefresh);
			}
		}
	}

	/**
	 * "unfinishes" the selected tasks
	 * @param taskview
	 * @param bRefresh Refresh the lists afterwards?
	 */
	private void cmdUnfinishTimelimitTask(final TimelimitTaskView taskview, final boolean bRefresh) {
		final List<TimelimitTaskVO> lstSelectedTimelimitTasks = getSelectedTimelimitTasks(taskview);
		if (!lstSelectedTimelimitTasks.isEmpty() && lstSelectedTimelimitTasks != null) {
			
			final String sMessage = CommonLocaleDelegate.getMessage("TimelimitTaskController.12","Soll(en) die ausgew\u00e4hlte(n) Frist(e) als unerledigt markiert werden?");
			final int iBtn = JOptionPane.showConfirmDialog(this.getParent(), sMessage, CommonLocaleDelegate.getMessage("TimelimitTaskController.9","Friste als unerledigt markieren"),
					JOptionPane.YES_NO_OPTION);
			if (iBtn == JOptionPane.YES_OPTION) {
				UIUtils.runCommand(this.getParent(), new Runnable() {
					@Override
					public void run() {
						try {
							for(TimelimitTaskVO taskvo : lstSelectedTimelimitTasks)
								tltaskDelegate.unfinish(taskvo);
							if (bRefresh) {
								refreshTimelimitTaskView();
							}
						}
						catch (/* CommonBusiness */ Exception ex) {
							Errors.getInstance().showExceptionDialog(getParent(), ex);
						}
					}
				});
			}
			else {
				// undo selection of the button:
				timelimittaskview.getFinishButton().setSelected(false);
			}
		}
	}

	/**
	 * finishes the selected tasks
	 * @param taskview
	 * @param bRefresh Refresh the lists afterwards?
	 */
	private void cmdFinishTimelimitTask(final TimelimitTaskView taskview, final boolean bRefresh) {
		final List<TimelimitTaskVO> lstSelectedTimelimitTasks = getSelectedTimelimitTasks(taskview);
		if (!lstSelectedTimelimitTasks.isEmpty() && lstSelectedTimelimitTasks != null) {
			UIUtils.runCommand(this.getParent(), new Runnable() {
				@Override
				public void run() {
					try {
						for(TimelimitTaskVO taskvo : lstSelectedTimelimitTasks)
							taskvo = tltaskDelegate.finish(taskvo);
						if (bRefresh) {
							refreshTimelimitTaskView();
						}
					}
					catch (/* CommonBusiness */Exception ex) {
						Errors.getInstance().showExceptionDialog(getParent(), ex);
					}
				}
			});
		}
	}

	/**
	 * removes the currently selected tasks
	 * @param taskview
	 * @param bRefresh Refresh the lists afterwards?
	 */
	private void cmdRemoveTimelimitTask(TimelimitTaskView taskview, final boolean bRefresh) {
		final List<TimelimitTaskVO> lstSelectedTimelimitTasks = getSelectedTimelimitTasks(taskview);
		
		if (!lstSelectedTimelimitTasks.isEmpty() && lstSelectedTimelimitTasks != null) {
			final String sMessage;
			if(lstSelectedTimelimitTasks.size() == 1)
				sMessage = CommonLocaleDelegate.getMessage("TimelimitTaskController.14","Wollen Sie den ausgew\u00e4hlten Eintrag wirklich l\u00f6schen?") + "\n";
			else
				sMessage = CommonLocaleDelegate.getMessage("TimelimitTaskController.15","Wollen Sie die ausgew\u00e4hlten Eintr\u00e4ge wirklich l\u00f6schen?") + "\n";
			
			final int btn = JOptionPane.showConfirmDialog(this.getParent(), sMessage, CommonLocaleDelegate.getMessage("TimelimitTaskController.8","Eintr\u00e4ge l\u00f6schen"),
					JOptionPane.YES_NO_OPTION);

			if (btn == JOptionPane.YES_OPTION) {
				UIUtils.runCommand(this.getParent(), new Runnable() {
					@Override
					public void run() {
						try {
							for(TimelimitTaskVO taskvo : lstSelectedTimelimitTasks)
								TimelimitTaskController.this.tltaskDelegate.remove(taskvo);
						}
						catch (CommonBusinessException ex) {
							Errors.getInstance().showExceptionDialog(getParent(), ex);
						}
						if (bRefresh) {
							refreshTimelimitTaskView();
						}
					}
				});
			}
		}
	}

	private void cmdRefreshTimelimitTaskView() {
		UIUtils.runCommand(this.getParent(), new Runnable() {
			@Override
			public void run() {
				refreshTimelimitTaskView();
			}
		});
	}

	private void cmdPrintTimelimitTaskView() {
		UIUtils.runCommand(this.getParent(), new Runnable() {
			@Override
			public void run() {
				printTimelimitTaskView();
			}
		});
	}

	public void refreshTimelimitTaskView() {
		boolean bUnfinishedOnly = !this.timelimittaskview.btnShowAllTasks.getModel().isSelected();
		final Collection<TimelimitTaskVO> colltaskvo = this.tltaskDelegate.getTimelimitTasks(bUnfinishedOnly);
		final List<CollectableTimelimitTask> lstclct = CollectionUtils.transform(colltaskvo, new CollectableTimelimitTask.MakeCollectable());

		final TimelimitTaskTableModel tblmdl = this.timelimittaskview.getTimelimitTaskTableModel();
		tblmdl.setCollectables(lstclct);

		//TableUtils.setOptimalColumnWidths(this.timelimittaskview.getTable());
		TaskController.setColumnWidths(readColumnWidthsFromPreferences(), this.timelimittaskview.getTable());

		// todo: sorting order? / UA
//		final int iColumnScheduled = tblmdl.findColumnByFieldName(CollectableTimelimitTask.FIELDNAME_SCHEDULED);
//		tblmdl.setSortingOrder(iColumnScheduled, true, true);
	}
	
	private List<Integer> readColumnWidthsFromPreferences() {
		List<Integer> lstColumnWidths = null;
		try {
			lstColumnWidths = PreferencesUtils.getIntegerList(this.prefs, PREFS_NODE_SELECTEDFIELDWIDTHS);
		}
		catch (PreferencesException ex) {
			LOG.error("Die Spaltenbreite konnte nicht aus den Preferences geladen werden.", ex);
			return lstColumnWidths;
		}

		return lstColumnWidths;
	}


	void printTimelimitTaskView() {
		try {
			new ReportController(this.getParent()).export(this.timelimittaskview.getTable(), null );
		}
		catch (CommonBusinessException ex) {
			Errors.getInstance().showExceptionDialog(this.getParent(), ex);
		}
	}

	private void cmdPerformTimelimitTask(TimelimitTaskView taskview) {
		final List<TimelimitTaskVO> collSelectedTimilimitTasks = getSelectedTimelimitTasks(taskview);
		
		if(!collSelectedTimilimitTasks.isEmpty() && collSelectedTimilimitTasks != null){
			UIUtils.runCommand(this.getParent(), new CommonRunnable() {
				@Override
				public void run() throws CommonBusinessException {
					final Collection<Integer> collGenericObjectIds = new ArrayList<Integer>();
					
					Integer iCommonModuleId = new Integer(0);
					for (TimelimitTaskVO tovo : collSelectedTimilimitTasks) {
						collGenericObjectIds.add(tovo.getGenericObjectId().intValue());
						if (iCommonModuleId != null && !iCommonModuleId.equals(tovo.getModuleId())) {
							if (iCommonModuleId.equals(0)) {
								iCommonModuleId = tovo.getModuleId();
							}
							else {
								iCommonModuleId = null;
							}
						}
					}
					
					if (collGenericObjectIds.size() == 1) {
						final GenericObjectCollectController ctlGenericObject = NuclosCollectControllerFactory.getInstance().
								newGenericObjectCollectController(MainFrame.getPredefinedEntityOpenLocation(MetaDataClientProvider.getInstance().getEntity(new Long(iCommonModuleId)).getEntity()), iCommonModuleId, null);
						ctlGenericObject.runViewSingleCollectableWithId(collGenericObjectIds.iterator().next());						
					}
					else if (iCommonModuleId != null && iCommonModuleId != 0) {
						final GenericObjectCollectController ctlGenericObject = NuclosCollectControllerFactory.getInstance().
								newGenericObjectCollectController(MainFrame.getPredefinedEntityOpenLocation(MetaDataClientProvider.getInstance().getEntity(new Long(iCommonModuleId)).getEntity()), iCommonModuleId, null);
						ctlGenericObject.runViewResults(getSearchConditionForRelatedObjects(collGenericObjectIds));
					}
				}
			});
		}
		else {
			final String sMessage = CommonLocaleDelegate.getMessage("TimelimitTaskController.6","Dieser Frist ist kein Objekt zugeordnet.");
			JOptionPane.showMessageDialog(taskview, sMessage, CommonLocaleDelegate.getMessage("TimelimitTaskController.18","Zugeordnetes Objekt anzeigen"), JOptionPane.ERROR_MESSAGE);
		}
	}

	private List<TimelimitTaskVO> getSelectedTimelimitTasks(final TimelimitTaskView taskview) {
		final List<Integer> lstSelectedRowNumbers = CollectionUtils.asList(taskview.getTable().getSelectedRows());
		final List<TimelimitTaskVO> result = CollectionUtils.transform(lstSelectedRowNumbers, new Transformer<Integer, TimelimitTaskVO>() {
			@Override
			public TimelimitTaskVO transform(Integer iRowNo) {
				return taskview.getTimelimitTaskTableModel().getTimelimitTask(iRowNo);
			}
		});
		assert result != null;
		return result;
	}
	
	private boolean areTasksCompleted(List<TimelimitTaskVO> lsttaskvo) {
		for(TimelimitTaskVO taskvo : lsttaskvo)
			if(taskvo.getCompleted() == null)
				return false;
		
		return true;
	}
	
	private boolean existsRelatedObjectsFor(List<TimelimitTaskVO> lsttaskvo) {
		for(TimelimitTaskVO taskvo : lsttaskvo)
			if(taskvo.getGenericObjectId() == null)
				return false;
		
		return true;
	}
	
	/**
	 * Build a search condition if there are more than one related objects
	 * @param collGenericObjectIds
	 * @return OR condition over all related object ids
	 */
	private CollectableSearchCondition getSearchConditionForRelatedObjects(Collection<Integer> collGenericObjectIds) {
		CompositeCollectableSearchCondition result = new CompositeCollectableSearchCondition(LogicalOperator.OR);

		for (Integer iId : collGenericObjectIds) {
			result.addOperand(new CollectableIdCondition(iId));
		}

		return result;
	}
	
	public void setExplorerController(ExplorerController ctlExplorer) {
		this.ctlExplorer = ctlExplorer;
	}

	public ExplorerController getExplorerController() {
		assert this.ctlExplorer != null;
		return this.ctlExplorer;
	}

	/**
	 * inner class: Popup menu for Timelimit tasks
	 */
	private class TimelimitTasksPopupMenu extends JPopupMenu {
		/**
		 * menu item: perform task (jump to leased object)
		 * todo must be disabled when there is no leased object
		 */
		private final JMenuItem miPerform = new JMenuItem();
		/**
		 * menu item: mark the task as finished (so it disappears from the task lists)
		 */
		private final JCheckBoxMenuItem miFinish = new JCheckBoxMenuItem();
		/**
		 * menu item: delete task from the database
		 */
		private final JMenuItem miRemove = new JMenuItem();

		public TimelimitTasksPopupMenu(final TimelimitTaskView taskview) {
			this.add(miPerform);
			this.add(miRemove);
			this.add(miFinish);

			miPerform.setAction(actPerformTask);
			miFinish.setAction(actFinishTask);
			miRemove.setAction(actRemoveTask);
		}
	}	// inner class TimelimitTasksPopupMenu

	/**
	 * inner class PopupMenuListener
	 * todo factor out (this is a reusable class)
	 * todo we have single selection model in the task list
	 */
	private class PopupMenuListener extends DefaultJPopupMenuListener {
		private final TimelimitTasksPopupMenu popupTimelimitTasks;
		private final JTable tblTasks;

		public PopupMenuListener(TimelimitTasksPopupMenu popupTasks, JTable tblTasks) {
			super(popupTasks);

			this.popupTimelimitTasks = popupTasks;
			this.tblTasks = tblTasks;
		}

		@Override
		protected void showPopupMenu(MouseEvent ev) {
			// first select/deselect the row:
			int iRow = tblTasks.rowAtPoint(ev.getPoint());

			// Nur, wenn nicht selektiert, selektieren:
			if (!tblTasks.isRowSelected(iRow)) {
				if ((ev.getModifiers() & MouseEvent.CTRL_MASK) != 0) {
					// Control gedr\u00fcckt:
					// Zeile zur Selektion hinzuf\u00fcgen:
					tblTasks.addRowSelectionInterval(iRow, iRow);
				}
				else {
					// Sonst nur diese Zeile selektieren:
					tblTasks.setRowSelectionInterval(iRow, iRow);
				}
			}	// if

			// enable/disable menu items
			final int iSelectedRow = this.tblTasks.getSelectedRow();
			final TimelimitTaskTableModel model = timelimittaskview.getTimelimitTaskTableModel();
			model.getTimelimitTask(iSelectedRow);

			// todo: there should be a better place for that...
			final boolean bPerformEnabled = existsRelatedObjectsFor(getSelectedTimelimitTasks(timelimittaskview));
			actPerformTask.setEnabled(bPerformEnabled);
			//this.popupTimelimitTasks.miPerform.setEnabled(bPerformEnabled);

			final boolean bFinished = areTasksCompleted(getSelectedTimelimitTasks(timelimittaskview));
			this.popupTimelimitTasks.miFinish.setState(bFinished);

			super.showPopupMenu(ev);
		}
	}	// inner class PopupMenuListener

	/**
	 * inner class DoubleClickListener
	 */
	private class DoubleClickListener extends MouseAdapter {
		private final TimelimitTaskView taskview;

		public DoubleClickListener(TimelimitTaskView taskview) {
			this.taskview = taskview;
		}

		@Override
		public void mouseClicked(MouseEvent ev) {
			if (ev.getClickCount() == 2) {
				TimelimitTaskController.this.cmdPerformTimelimitTask(this.taskview);
			}
		}
	}	// inner class DoubleClickListener

	/**
	 * inner class TransferHandler. Handles drag&drop, copy&paste for the Timelimit task list.
	 */
	private class TransferHandler extends javax.swing.TransferHandler {

		private final Component parent;

		public TransferHandler(Component parent) {
			this.parent = parent;
		}

		@Override
		public int getSourceActions(JComponent comp) {
			return NONE;
		}

		@Override
		public boolean canImport(JComponent comp, DataFlavor[] aflavors) {
			// Unfortunately, this method is not called for each row, so we only can say yes or no
			// for the whole table here. We must say yes to enable drop at all.

			class IsGenericObjectFlavor implements Predicate<DataFlavor> {
				@Override
				public boolean evaluate(DataFlavor flavor) {
					return (flavor instanceof GenericObjectIdModuleProcess.DataFlavor);
				}
			}
			return CollectionUtils.exists(Arrays.asList(aflavors), new IsGenericObjectFlavor());
		}

		@Override
		public boolean importData(JComponent comp, Transferable transferable) {
			boolean result = false;

			if (comp instanceof JTable) {
				final JTable tbl = (JTable) comp;

				try {
					final int iSelectedRow = tbl.getSelectedRow();
					if (iSelectedRow != -1) {
						final TimelimitTaskTableModel tblmdl = TimelimitTaskController.this.timelimittaskview.getTimelimitTaskTableModel();
						final CollectableTimelimitTask clctTarget = tblmdl.getCollectable(iSelectedRow);
						final TimelimitTaskVO taskvoTarget = clctTarget.getTimelimitTaskVO();

						final GenericObjectIdModuleProcess goimp = (GenericObjectIdModuleProcess) transferable.getTransferData(new GenericObjectIdModuleProcess.DataFlavor());

						if (taskvoTarget.getCompleted() != null) {
							final String sMessage = CommonLocaleDelegate.getMessage("TimelimitTaskController.5","Dieser Eintrag ist bereits abgeschlossen. Er kann nicht mehr ver\u00e4ndert werden.");
							throw new NuclosBusinessException(sMessage);
						}
						boolean bDoIt = (taskvoTarget.getGenericObjectId() == null);
						if (!bDoIt) {
							final String sQuestion = CommonLocaleDelegate.getMessage("TimelimitTaskController.13","Soll das Objekt \"{0}\" zugeordnet werden?", goimp.getGenericObjectIdentifier()) + "\n" + CommonLocaleDelegate.getMessage("TimelimitTaskController.3","Die bestehende Zuordnung zu \"{0}\" wird dadurch aufgehoben.", taskvoTarget.getIdentifier());
							final int iBtn = JOptionPane.showConfirmDialog(this.parent, sQuestion, CommonLocaleDelegate.getMessage("TimelimitTaskController.17","Zugeordnetes Objekt \u00e4ndern"),
									JOptionPane.OK_CANCEL_OPTION);
							bDoIt = (iBtn == JOptionPane.OK_OPTION);
						}

						if (bDoIt) {
							taskvoTarget.setGenericObjectId(new Integer(goimp.getGenericObjectId()));
							final TimelimitTaskVO taskvoUpdated = tltaskDelegate.update(taskvoTarget);
							tblmdl.setCollectable(iSelectedRow, new CollectableTimelimitTask(taskvoUpdated));
							result = true;
						}
					}
				}
				catch (UnsupportedFlavorException ex) {
					JOptionPane.showMessageDialog(parent, CommonLocaleDelegate.getMessage("TimelimitTaskController.4","Dieser Datentransfer wird von dem ausgew\u00e4hlten Objekt nicht unterst\u00fctzt."));
				}
				catch (IOException ex) {
					throw new NuclosFatalException(ex);
				}
				catch (CommonBusinessException ex) {
					Errors.getInstance().showExceptionDialog(parent, ex);
				}
			}

			return result;
		}
	}

	@Override
	public ScheduledRefreshable getSingleScheduledRefreshableView() {
		return this.timelimittaskview;
	}

	@Override
	public void refreshScheduled(ScheduledRefreshable isRefreshable) {
		refreshTimelimitTaskView();
	}
	
	/**
	 * 
	 * @return
	 */
	public MainFrameTab getTab() {
		return tab;
	}

}	// class TimelimitTaskController
