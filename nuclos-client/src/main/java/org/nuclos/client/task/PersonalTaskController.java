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
package org.nuclos.client.task;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;
import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.common.KeyBinding;
import org.nuclos.client.common.KeyBindingProvider;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.NuclosCollectController;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.common.NuclosDropTargetListener;
import org.nuclos.client.common.NuclosDropTargetVisitor;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.explorer.ExplorerController;
import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.genericobject.ReportController;
import org.nuclos.client.genericobject.datatransfer.GenericObjectIdModuleProcess;
import org.nuclos.client.genericobject.datatransfer.TransferableGenericObjects;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.datatransfer.MasterDataIdAndEntity;
import org.nuclos.client.ui.CommonAbstractAction;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.MainFrameTabAdapter;
import org.nuclos.client.ui.ResultListener;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.CollectController.CollectableEventListener;
import org.nuclos.client.ui.collect.CollectController.MessageType;
import org.nuclos.client.ui.collect.CollectableTableHelper;
import org.nuclos.client.ui.collect.ToolTipsTableHeader;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.CollectableComponentFactory;
import org.nuclos.client.ui.popupmenu.DefaultJPopupMenuListener;
import org.nuclos.client.ui.table.TableUtils;
import org.nuclos.common.Actions;
import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.DateUtils;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.PreferencesException;
import org.nuclos.server.common.valueobject.TaskObjectVO;
import org.nuclos.server.common.valueobject.TaskVO;

/**
 * Controller for <code>PersonalTaskView</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class PersonalTaskController extends RefreshableTaskController implements CollectableEventListener, NuclosDropTargetVisitor {

	private static final Logger LOG = Logger.getLogger(PersonalTaskController.class);

	public static final int DEFAULT_MIN_WIDTH_FOR_DEFAULT_OPEN_IN_NEW_TAB = 450;
	public static final int DEFAULT_MIN_HEIGHT_FOR_DEFAULT_OPEN_IN_NEW_TAB = 450;

	private static final String PREFS_NODE_PERSONALTASKS = "personalTasks";
	private static final String PREFS_NODE_PERSONALTASKS_BUTTON_ALL_PRESSED = "personalTasksButtonAllIsPressed";
	private static final String PREFS_NODE_PERSONALTASKS_SHOW_TASKS_ITEM_SELECTED = "personalTasksShowTasksItemSelected";
	private static final String PREFS_NODE_PERSONALTASKS_REFRESH_INTERVAL_SELECTED = "personalTasksRefreshSelected";
	
	private static final String PREFS_NODE_SELECTEDFIELDS = "selectedFields";
	private static final String PREFS_NODE_SELECTEDFIELDWIDTHS = "selectedFieldWidths";

	private final PersonalTasksPopupMenu popupPersonal;

	private final PersonalTaskView personaltaskview;

	private boolean bOpenInNewTabIsDefault = false;

	private MainFrameTab tab;

	private final TaskDelegate taskDelegate;

	private final String sCurrentUser;

	private ExplorerController ctlExplorer;

	private final Preferences prefs;

	private final Action actEditTaskInNewTab = new CommonAbstractAction(getSpringLocaleDelegate().getMessage(
			"PersonalTaskController.29","In neuem Tab Bearbeiten..."),
		Icons.getInstance().getIconEdit16(), getSpringLocaleDelegate().getMessage(
				"PersonalTaskController.8","Definition der ausgew\u00e4hlten Aufgabe bearbeiten / Aufgabe delegieren")) {

		@Override
		public void actionPerformed(ActionEvent ev) {
			PersonalTaskController.this.cmdEditPersonalTaskDefinition(personaltaskview, true);
		}
	};

	private final Action actEditTask = new CommonAbstractAction(getSpringLocaleDelegate().getMessage(
			"PersonalTaskController.7","Bearbeiten..."), Icons.getInstance().getIconEdit16(), 
			getSpringLocaleDelegate().getMessage("PersonalTaskController.8","Ausgew\u00e4hlte Aufgabe(n) bearbeiten")) {

		@Override
		public void actionPerformed(ActionEvent ev) {
			PersonalTaskController.this.cmdEditPersonalTaskDefinition(personaltaskview, false);
		}
	};

	private final Action actPerformTask = new CommonAbstractAction(getSpringLocaleDelegate().getMessage(
			"PersonalTaskController.25","\u00D6ffne zugeordnete(s) Objekt(e)"), Icons.getInstance().getIconModule(),
			getSpringLocaleDelegate().getMessage("PersonalTaskController.27","Zugeordnetes Objekt anzeigen")) {

		@Override
		public void actionPerformed(ActionEvent ev) {
			PersonalTaskController.this.cmdPerformPersonalTask(personaltaskview);
		}
	};

	private final Action actCopyCell = new CommonAbstractAction(getSpringLocaleDelegate().getMessage(
			"ResultPanel.13","Kopiere markierte Zellen"), Icons.getInstance().getIconCopy16(),
			getSpringLocaleDelegate().getMessage("ResultPanel.13","Kopiere markierte Zellen")) {

		@Override
		public void actionPerformed(ActionEvent ev) {
			JTable table = personaltaskview.getTable();
			UIUtils.copyCells(table);
		}
	};

	private final Action actCopyRows = new CommonAbstractAction(getSpringLocaleDelegate().getMessage(
			"ResultPanel.14","Kopiere markierte Zeilen"), Icons.getInstance().getIconCopy16(),
			getSpringLocaleDelegate().getMessage("ResultPanel.14","Kopiere markierte Zeilen")) {

		@Override
		public void actionPerformed(ActionEvent ev) {
			JTable table = personaltaskview.getTable();
			UIUtils.copyRows(table);
		}
	};



	private final Action actRemoveTask = new CommonAbstractAction(getSpringLocaleDelegate().getMessage(
			"PersonalTaskController.21","L\u00f6schen..."), Icons.getInstance().getIconRealDelete16(), 
			getSpringLocaleDelegate().getMessage("PersonalTaskController.6","Ausgew\u00e4hlte Aufgabe l\u00f6schen")) {

		@Override
		public void actionPerformed(ActionEvent ev) {
			PersonalTaskController.this.cmdRemovePersonalTask(personaltaskview);
		}
	};

	private final Action actCompleteTask = new CommonAbstractAction(getSpringLocaleDelegate().getMessage(
			"PersonalTaskController.16","Erledigt"), Icons.getInstance().getIconProperties16(), 
			getSpringLocaleDelegate().getMessage("PersonalTaskController.5","Ausgew\u00e4hlte Aufgabe(n) als erledigt/unerledigt markieren")) {

		@Override
		public void actionPerformed(ActionEvent ev) {
			PersonalTaskController.this.cmdCompleteOrUncompleteTask(personaltaskview);
		}
	};

	PersonalTaskController(Preferences prefsParent, TaskDelegate taskdelegate, String sCurrentUser) {
		super();
		this.personaltaskview = new PersonalTaskView();
		this.personaltaskview.init();
		this.taskDelegate = taskdelegate;
		this.sCurrentUser = sCurrentUser;
		this.popupPersonal = new PersonalTasksPopupMenu(personaltaskview);
		this.prefs = prefsParent.node(PREFS_NODE_PERSONALTASKS);
		this.personaltaskview.setPersonalTaskTableModel(newPersonalTaskTableModel());
		
		final JTable tblPersonal = personaltaskview.getTable();
		tblPersonal.setTableHeader(new ToolTipsTableHeader(this.personaltaskview.getPersonalTaskTableModel(), tblPersonal.getColumnModel()));
		TableUtils.addMouseListenerForSortingToTableHeader(tblPersonal, this.personaltaskview.getPersonalTaskTableModel(), new CommonRunnable() {
			@Override
            public void run() {
				personaltaskview.getPersonalTaskTableModel().sort();
				storeOrderBySelectedColumnToPreferences();
			}
		});
		
		tblPersonal.setRowHeight(20);
		setupRenderers(tblPersonal);
		setupColumnModelListener(tblPersonal);
		setupActions(tblPersonal);
		setupDataTransfer(tblPersonal);
		setupTableModelSorting();

		setRowColorRendererInPersonalTaskTable();
		tblPersonal.getModel().addTableModelListener( new TableModelListener() {
 			@Override
			public void tableChanged(TableModelEvent e) {
 				setRowColorRendererInPersonalTaskTable();
			}
        });

		this.personaltaskview.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				Dimension personalTaskViewSize = PersonalTaskController.this.personaltaskview.getBounds().getSize();
				PersonalTaskController.this.bOpenInNewTabIsDefault =
					personalTaskViewSize.height < DEFAULT_MIN_HEIGHT_FOR_DEFAULT_OPEN_IN_NEW_TAB ||
					personalTaskViewSize.width < DEFAULT_MIN_WIDTH_FOR_DEFAULT_OPEN_IN_NEW_TAB;

				UIUtils.setFontStyleBold(PersonalTaskController.this.popupPersonal.miEditDefinition, !PersonalTaskController.this.bOpenInNewTabIsDefault);
				UIUtils.setFontStyleBold(PersonalTaskController.this.popupPersonal.miEditDefinitionInNewTab, PersonalTaskController.this.bOpenInNewTabIsDefault);
			}
		});

		KeyBinding keybinding = KeyBindingProvider.REFRESH;
		this.personaltaskview.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keybinding.getKeystroke(), keybinding.getKey());
		this.personaltaskview.getActionMap().put(keybinding.getKey(), new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				PersonalTaskController.this.cmdRefreshPersonalTaskView();
			}
		});
	}

	class RowColorRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object oValue, boolean bSelected, boolean bHasFocus, int iRow, int iColumn) {
			final PersonalTaskTableModel tblmdl = personaltaskview.getPersonalTaskTableModel();
			Component comp = super.getTableCellRendererComponent(table,  oValue, bSelected, bHasFocus, iRow, iColumn);
			Date dScheduled = DateUtils.getPureDate((Date)tblmdl.getValueAt(iRow, tblmdl.findColumnByFieldName(CollectableTask.FIELDNAME_SCHEDULED)).getValue());
			Date dFinished = DateUtils.getPureDate((Date)tblmdl.getValueAt(iRow, tblmdl.findColumnByFieldName(CollectableTask.FIELDNAME_COMPLETED)).getValue());

			if((dFinished == null) && (dScheduled != null) && ((DateUtils.today()).after(dScheduled))) {
				comp.setForeground(Color.red);
			} else {
				comp.setForeground(bSelected ? Color.white : null);
			}

			if(iColumn == tblmdl.findColumnByFieldName(CollectableTask.FIELDNAME_VISIBILITY)){
				String vNum  = CollectableTask.DEFAULT_VISIBILITY.toString();
				if(oValue != null && oValue.toString() != null && !oValue.toString().trim().equals("")){
					vNum = oValue.toString();
				}
				this.setText(getSpringLocaleDelegate().getTextFallback(
						"nuclos.entityfield.todolist.visibility."+vNum, "<[" + oValue + "]>"));
			}
			return comp;
		}
	}

	private void setRowColorRendererInPersonalTaskTable() {
		//SwingUtilities.invokeLater( new Runnable() {
			//@Override
			//public void run() {
				for (int columnIndex=0; columnIndex < personaltaskview.getTable().getColumnCount();columnIndex++) {
					TableColumn column = personaltaskview.getTable().getColumnModel().getColumn(columnIndex);
					column.setCellRenderer(new RowColorRenderer());
				}
			//}
		//});
	}

	private void setupRenderers(JTable table) {
		for (Enumeration<TableColumn> enumeration = table.getColumnModel().getColumns(); enumeration.hasMoreElements();) {
			final TableColumn column = enumeration.nextElement();
			final int iModelIndex = column.getModelIndex();
			final CollectableEntityField clctef = this.personaltaskview.getPersonalTaskTableModel().getCollectableEntityField(iModelIndex);
			final CollectableComponent clctcomp = CollectableComponentFactory.getInstance().newCollectableComponent(clctef, null, false);
			column.setCellRenderer(clctcomp.getTableCellRenderer(false));
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
						PersonalTaskController.this.storeColumnOrder(tbl);
					}
					catch (PreferencesException ex) {
						final String sMessage = getSpringLocaleDelegate().getMessage(
								"PersonalTaskController.15","Spaltenreihenfolge konnte nicht gespeichert werden.");
						Errors.getInstance().showExceptionDialog(tbl, sMessage, ex);
					}
				}
			}

			@Override
			public void columnMarginChanged(ChangeEvent ev) {
				try {
					PersonalTaskController.this.storeColumnWidthsInPrefs(tbl);
				}
				catch (PreferencesException ex) {
					final String sMessage = getSpringLocaleDelegate().getMessage(
							"PersonalTaskController.28","Spaltenbreiten konnte nicht gespeichert werden.");
					Errors.getInstance().showExceptionDialog(tbl, sMessage, ex);
				}
			}

			@Override
			public void columnSelectionChanged(ListSelectionEvent ev) {
				// do nothing
			}
		});
	}

	private void storeColumnOrder(JTable tbl) throws PreferencesException {
		this.storeSelectedFieldNamesInPrefs(CollectableTableHelper.getFieldNamesFromColumns(tbl));
	}

	private void storeSelectedFieldNamesInPrefs(List<String> lstSelectedFieldNames) throws PreferencesException {
		PreferencesUtils.putStringList(this.prefs, PREFS_NODE_SELECTEDFIELDS, lstSelectedFieldNames);
	}

	private void storeColumnWidthsInPrefs(JTable tbl) throws PreferencesException {
		PreferencesUtils.putIntegerList(this.prefs, PREFS_NODE_SELECTEDFIELDWIDTHS, CollectableTableHelper.getColumnWidths(tbl));
	}

	private void setupActions(final JTable table) {
		super.addRefreshIntervalActionsToSingleScheduledRefreshable();

		table.addMouseListener(new PopupMenuListener(this.popupPersonal, table));
		table.addMouseListener(new DoubleClickListener(personaltaskview));

		final Action actRefresh = new CommonAbstractAction(Icons.getInstance().getIconRefresh16(),
				getSpringLocaleDelegate().getMessage("PersonalTaskController.3","Liste aktualisieren")) {

			@Override
			public void actionPerformed(ActionEvent ev) {
				PersonalTaskController.this.cmdRefreshPersonalTaskView();
			}
		};

		final Action actNew = new CommonAbstractAction(Icons.getInstance().getIconNew16(), getSpringLocaleDelegate().getMessage(
				"PersonalTaskController.22","Neue Aufgabe erstellen")) {

			@Override
			public void actionPerformed(ActionEvent ev) {
				PersonalTaskController.this.cmdNewPersonalTask(true, bOpenInNewTabIsDefault);
			}
		};

		final Action actPrint = new CommonAbstractAction(getSpringLocaleDelegate().getMessage(
				"PersonalTaskController.4","Liste drucken"),
			Icons.getInstance().getIconPrintReport16(), null) {

			@Override
			public void actionPerformed(ActionEvent ev) {
				PersonalTaskController.this.cmdPrintPersonalTaskView();
			}
		};

		actEditTask.setEnabled(false);
		actCompleteTask.setEnabled(false);
		actPerformTask.setEnabled(false);
		actRemoveTask.setEnabled(false);
		actPrint.setEnabled(false);

		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_PRINT_TASKLIST)) {
			actPrint.setEnabled(true);
		}

		personaltaskview.getRefreshButton().setAction(actRefresh);
		personaltaskview.getNewButton().setAction(actNew);
		personaltaskview.getEditMenuItem().setAction(actEditTask);
		personaltaskview.getCompleteButton().setAction(actCompleteTask);
		personaltaskview.getPerformMenuItem().setAction(actPerformTask);

		personaltaskview.getRemoveMenuItem().setAction(actRemoveTask);
		personaltaskview.getPrintMenuItem().setAction(actPrint);

		// use only action listener for "show finished" button:

		personaltaskview.ckbShowCompletedTasks.setSelected(readIsButtonAllTasksSelected());
		personaltaskview.ckbShowCompletedTasks.addActionListener(actRefresh);

		setRefreshIntervalForSingleViewRefreshable(readIntervalTasksFromPreferences());

		personaltaskview.bgShowTasks.setSelected(personaltaskview.rbShowTask[readItemShowTasks()].getModel(), true);
		for (int i = 0; i < personaltaskview.rbShowTask.length; i++) {
			personaltaskview.rbShowTask[i].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdRefreshPersonalTaskView();
				}
			});
		}
		personaltaskview.bgPriority.setSelected(personaltaskview.rbPrio[0].getModel(), true);
		for (int i = 0; i < personaltaskview.rbPrio.length; i++) {
			personaltaskview.rbPrio[i].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdRefreshPersonalTaskView();
				}
			});
		}

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
					} else {
						final int iSelectedRow = PersonalTaskController.this.personaltaskview.getTable().getSelectedRow();
						CollectableTask taskclct = PersonalTaskController.this.personaltaskview.getPersonalTaskTableModel().getCollectable(iSelectedRow);
						final TaskVO taskvo = taskclct.getTaskVO();
						bFinished = Boolean.valueOf(taskvo.getCompleted() != null);
						bHasGenericObject = taskvo.hasRelatedObjects();
					}

					final boolean bTaskSelected = !bSelectionEmpty;
					final boolean bCompletedTaskSelected = bTaskSelected && bFinished.booleanValue();

					actEditTask.setEnabled(bTaskSelected && (lsm.getMinSelectionIndex() == lsm.getMaxSelectionIndex()));
					actCompleteTask.setEnabled(bTaskSelected);
					actPerformTask.setEnabled(bTaskSelected && bHasGenericObject.booleanValue());
					actRemoveTask.setEnabled(bTaskSelected);
					personaltaskview.getCompleteButton().setSelected(bCompletedTaskSelected);
				}
			}
		});
	}

	private void setupDataTransfer(JTable table) {
		DropTarget dt = new DropTarget(table, new NuclosDropTargetListener(this));
		dt.setActive(true);
	}

	private void setupTableModelSorting() {
		final PersonalTaskTableModel tblmdl = this.personaltaskview.getPersonalTaskTableModel();
		if (tblmdl.getColumnCount() > 0) {
			List<SortKey> sortKeys = readColumnOrderFromPreferences();
			if (sortKeys.isEmpty()) {
				sortKeys = Collections.singletonList(new SortKey(tblmdl.findColumnByFieldName(CollectableTask.FIELDNAME_SCHEDULED), SortOrder.ASCENDING));
			}
			try {
				tblmdl.setSortKeys(sortKeys, false);
			} catch (Exception e) {
			}
		}
	}

	private PersonalTaskTableModel newPersonalTaskTableModel() {
		final PersonalTaskTableModel result = new PersonalTaskTableModel();
		final List<String> lstFieldNames = new LinkedList<String>(CollectableTask.clcte.getFieldNames());
		List<String> lstFieldNameOrderTemp;
		try {
			lstFieldNameOrderTemp = PreferencesUtils.getStringList(this.prefs, PREFS_NODE_SELECTEDFIELDS);
		} catch (PreferencesException ex) {
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
		final List<String> lstNamesOfFieldsToDisplay = CollectableTask.clcte.getNamesOfFieldsToDisplay();
		if(lstNamesOfFieldsToDisplay.size() == lstFieldNames.size()){
			for (Iterator<String> iter = lstFieldNames.iterator(); iter.hasNext();) {
				final String sFieldName = iter.next();
				final CollectableEntityField clctef = CollectableTask.clcte.getEntityField(sFieldName);
				if (lstNamesOfFieldsToDisplay.contains(sFieldName)) {
					result.addColumn(iColumn++, clctef);
				}
			}
		} else {
			for (String fieldName : lstNamesOfFieldsToDisplay) {
				final CollectableEntityField clctef = CollectableTask.clcte.getEntityField(fieldName);
				result.addColumn(iColumn++, clctef);
			}
		}

		return result;
	}

	private List<SortKey> readColumnOrderFromPreferences() {
		try {
			return PreferencesUtils.readSortKeysFromPrefs(getPreferences());
		} catch (PreferencesException ex) {
			LOG.error("The column order could not be loaded from preferences.", ex);
			return Collections.emptyList();
		}
	}

	private List<Integer> readColumnWidthsFromPreferences() {
		List<Integer> lstColumnWidths = null;
		try {
			lstColumnWidths = PreferencesUtils.getIntegerList(getPreferences(), PREFS_NODE_SELECTEDFIELDWIDTHS);
		} catch (PreferencesException ex) {
			LOG.error("Die Spaltenbreite konnte nicht aus den Preferences geladen werden.", ex);
			return lstColumnWidths;
		}

		return lstColumnWidths;
	}

	private int getShowTasksSelectedIndex() {
		for (int i = 0; i < personaltaskview.rbShowTask.length; i++) {
			if (personaltaskview.bgShowTasks.isSelected(personaltaskview.rbShowTask[i].getModel())) {
				return i;
			}
		}
		return -1;
	}

	private boolean isButtonAllTasksSelected(){
		return personaltaskview.ckbShowCompletedTasks.getModel().isSelected();
	}

	private int getSelectedPriority() {
		for (int i = 0; i < personaltaskview.rbPrio.length; i++) {
			if (personaltaskview.bgPriority.isSelected(personaltaskview.rbPrio[i].getModel())) {
				return i;
			}
		}
		return -1;
	}

	private Preferences getPreferences() {
		return org.nuclos.common2.ClientPreferences.getUserPreferences().node("taskPanel").node("personalTasks");
	}

	private void removePersonalTaskTab() {
		this.tab = null;
	}

	public void cmdShowPersonalTasks() {
		if (tab == null) {
			UIUtils.runCommandForTabbedPane(this.getTabbedPane(), new Runnable() {
				@Override
	            public void run() {
					showPersonalTasks(new MainFrameTab());
					MainFrame.addTab(tab);
					MainFrame.setSelectedTab(tab);
				}
			});
		}

		MainFrame.setSelectedTab(tab);
	}

	public void showPersonalTasks(MainFrameTab tab) {
		if (this.tab != null) {
			throw new IllegalArgumentException("Personal Tasks already shown");
		}
		tab.addMainFrameTabListener(new MainFrameTabAdapter() {
			@Override
			public void tabClosing(MainFrameTab tab, ResultListener<Boolean> rl) {
				removePersonalTaskTab();
				rl.done(true);
			}
			@Override
			public void tabClosed(MainFrameTab tab) {
				tab.removeMainFrameTabListener(this);
			}
		});

		tab.setTitle(getSpringLocaleDelegate().getMessage("tabMyTasks", "Meine Aufgaben"));
		tab.setTabIconFromSystem("getIconTabTask");
		tab.setLayeredComponent(PersonalTaskController.this.personaltaskview);
		tab.setTabStoreController(new TaskController.TaskTabStoreController(TaskController.RestorePreferences.PERSONAL, PersonalTaskController.this.personaltaskview));
		PersonalTaskController.this.tab = tab;
	}

	public void storeShowAllTasksIsPressedToPreferences(){
		final boolean showAllTasksButtonIsSelected = isButtonAllTasksSelected();
		final Integer[] showAllTasksButtonIsSelectedArray= { new Integer(showAllTasksButtonIsSelected ? 1 : 0)};
		try {
			PreferencesUtils.putIntegerArray(getPreferences(), PREFS_NODE_PERSONALTASKS_BUTTON_ALL_PRESSED, showAllTasksButtonIsSelectedArray);
		} catch (PreferencesException e1) {
			Errors.getInstance().showExceptionDialog(this.getTabbedPane().getComponentPanel(), getSpringLocaleDelegate().getMessage(
					"PersonalTaskController.17","Fehler beim Abspeichern der Einstellungen"), e1);
		}
	}

	public void storeShowDelegatedTasksIsSelectedToPreferences(){
		final Integer[] showTasks= { new Integer(getShowTasksSelectedIndex())};
		try {
			PreferencesUtils.putIntegerArray(getPreferences(), PREFS_NODE_PERSONALTASKS_SHOW_TASKS_ITEM_SELECTED, showTasks);
		} catch (PreferencesException e1) {
			Errors.getInstance().showExceptionDialog(this.getTabbedPane().getComponentPanel(), getSpringLocaleDelegate().getMessage(
					"PersonalTaskController.18","Fehler beim Abspeichern der Einstellungen"), e1);
		}
	}

	private boolean readIsButtonAllTasksSelected(){
		List<Integer> isButtonAllTasksSelectedList;
		try {
			isButtonAllTasksSelectedList = PreferencesUtils.getIntegerList(getPreferences(), PREFS_NODE_PERSONALTASKS_BUTTON_ALL_PRESSED);
		} catch (PreferencesException ex) {
			LOG.error(getSpringLocaleDelegate().getMessage(
					"PersonalTaskController.9","Der Filterzustand konnte nicht aus den Eisntellungen gelesen werden"), ex);
			return false;
		}
		if (isButtonAllTasksSelectedList.isEmpty()) return false ;
		return isButtonAllTasksSelectedList.get(0) == 1 ? true : false;
	}

	private int readItemShowTasks(){
		List<Integer> isButtonDelegateTasksSelectedList;
		try {
			isButtonDelegateTasksSelectedList = PreferencesUtils.getIntegerList(getPreferences(), PREFS_NODE_PERSONALTASKS_SHOW_TASKS_ITEM_SELECTED);
		} catch (PreferencesException ex) {
			LOG.error(getSpringLocaleDelegate().getMessage(
					"PersonalTaskController.10","Der Filterzustand konnte nicht aus den Einstellungen gelesen werden"), ex);
			return 0;
		}
		if (isButtonDelegateTasksSelectedList.isEmpty()) return 0 ;
		return isButtonDelegateTasksSelectedList.get(0);
	}

	public void storeIntervalTasksToPreferences(){
		final Integer[] refreshInterval = { new Integer((getSingleScheduledRefreshableView().getRefreshInterval()))};
		try {
			PreferencesUtils.putIntegerArray(getPreferences(), PREFS_NODE_PERSONALTASKS_REFRESH_INTERVAL_SELECTED, refreshInterval);
		} catch (PreferencesException e1) {
			Errors.getInstance().showExceptionDialog(this.getTabbedPane().getComponentPanel(), getSpringLocaleDelegate().getMessage(
					"PersonalTaskController.18","Fehler beim Abspeichern der Einstellungen"), e1);
		}
	}

	private int readIntervalTasksFromPreferences() {
		List<Integer> refreshIntervalTasksSelectedList;
		try {
			refreshIntervalTasksSelectedList = PreferencesUtils.getIntegerList(getPreferences(), PREFS_NODE_PERSONALTASKS_REFRESH_INTERVAL_SELECTED);
		} catch (PreferencesException ex) {
			LOG.error(getSpringLocaleDelegate().getMessage(
					"PersonalTaskController.10","Der Filterzustand konnte nicht aus den Einstellungen gelesen werden"), ex);
			return 0;
		}
		if (refreshIntervalTasksSelectedList.isEmpty()) return 0 ;
		return refreshIntervalTasksSelectedList.get(0);
	}

	public void storeOrderBySelectedColumnToPreferences(){
		try {
			PreferencesUtils.writeSortKeysToPrefs(getPreferences(), this.personaltaskview.getPersonalTaskTableModel().getSortKeys());
		} catch (PreferencesException e1) {
			Errors.getInstance().showExceptionDialog(this.getTabbedPane().getComponentPanel(), getSpringLocaleDelegate().getMessage(
					"PersonalTaskController.19","Fehler beim Abspeichern der Einstellungen"), e1);
		}
	}

	private void cmdCompleteOrUncompleteTask(final PersonalTaskView taskview) {
		final List<TaskVO> lsttaskvo = getSelectedPersonalTasks(taskview);
		if(!lsttaskvo.isEmpty() && lsttaskvo != null){
			final boolean bCompleted = areTasksCompleted(lsttaskvo);
			if (!bCompleted) {
				cmdCompleteTask(taskview);
			} else {
				cmdUncompleteTask(taskview);
			}
		}
	}

	private void cmdCompleteTask(final PersonalTaskView taskview) {
		final List<TaskVO> lsttaskvo = getSelectedPersonalTasks(taskview);
		if(!lsttaskvo.isEmpty() && lsttaskvo != null){
			UIUtils.runCommandForTabbedPane(this.getTabbedPane(), new Runnable() {
				@Override
				public void run() {
					try {
						for(TaskVO taskvo : lsttaskvo)
							taskvo = taskDelegate.complete(taskvo);
						refreshPersonalTaskView();
					} catch (CommonBusinessException ex) {
						Errors.getInstance().showExceptionDialog(getTabbedPane().getComponentPanel(), ex);
					}
				}
			});
		}
	}

	private void cmdUncompleteTask(final PersonalTaskView taskview) {
		final List<TaskVO> lsttaskvo = getSelectedPersonalTasks(taskview);
		if(!lsttaskvo.isEmpty() && lsttaskvo != null){
			final String sMessage = getSpringLocaleDelegate().getMessage(
					"PersonalTaskController.23","Soll(en) die ausgew\u00e4hlte(n) Aufgabe(n) als unerledigt markiert werden?");
			final int iBtn = JOptionPane.showConfirmDialog(this.getTabbedPane().getComponentPanel(), sMessage, 
					getSpringLocaleDelegate().getMessage("PersonalTaskController.1","Aufgaben als unerledigt markieren"), 
					JOptionPane.YES_NO_OPTION);
			if (iBtn == JOptionPane.YES_OPTION) {
				UIUtils.runCommandForTabbedPane(this.getTabbedPane(), new Runnable() {
					@Override
					public void run() {
						try {
							for(TaskVO taskvo : lsttaskvo)
								taskDelegate.uncomplete(taskvo);
							refreshPersonalTaskView();
						} catch (CommonBusinessException ex) {
							Errors.getInstance().showExceptionDialog(getTabbedPane().getComponentPanel(), ex);
						}
					}
				});
			} else {
				personaltaskview.getCompleteButton().setSelected(false);
			}
		}
	}

	private void cmdRemovePersonalTask(PersonalTaskView taskview) {
		final List<TaskVO> lsttaskvo = getSelectedPersonalTasks(taskview);
		final String sMessage = getSpringLocaleDelegate().getMessage(
				"PersonalTaskController.24","Soll(en) die ausgew\u00e4hlte(n) Aufgabe(n) wirklich gel\u00f6scht werden?");
		final int btn = JOptionPane.showConfirmDialog(this.getTabbedPane().getComponentPanel(), sMessage, getSpringLocaleDelegate().getMessage(
				"PersonalTaskController.2","Aufgaben l\u00f6schen"), JOptionPane.YES_NO_OPTION);
		if (btn == JOptionPane.YES_OPTION) {
			UIUtils.runCommandForTabbedPane(this.getTabbedPane(), new Runnable() {
				@Override
				public void run() {
					try {
						for(TaskVO taskvo : lsttaskvo)
							taskDelegate.remove(taskvo);
						refreshPersonalTaskView();
					} catch (CommonBusinessException ex) {
						Errors.getInstance().showExceptionDialog(getTabbedPane().getComponentPanel(), ex);
					}
				}
			});
		}
	}

	private void cmdRefreshPersonalTaskView() {
		UIUtils.runCommandForTabbedPane(this.getTabbedPane(), new Runnable() {
			@Override
			public void run() {
				refreshPersonalTaskView();
			}
		});
	}

	private void cmdPrintPersonalTaskView() {
		UIUtils.runCommandForTabbedPane(this.getTabbedPane(), new Runnable() {
			@Override
			public void run() {
				printPersonalTaskView();
			}
		});
	}

	void refreshPersonalTaskView() {
		final boolean bUncompletedOnly = !this.isButtonAllTasksSelected();
		final int iSelectedIndex = this.getShowTasksSelectedIndex();
		final int iSelectedPriority = this.getSelectedPriority();
		final Collection<TaskVO> colltaskvo;
		try {
			if (iSelectedIndex == 0) {
				colltaskvo = this.taskDelegate.getOwnTasks(this.sCurrentUser, bUncompletedOnly, iSelectedPriority);
			} else if (iSelectedIndex == 1) {
				colltaskvo = this.taskDelegate.getDelegatedTasks(this.sCurrentUser, bUncompletedOnly, iSelectedPriority);
			} else  {
				colltaskvo = this.taskDelegate.getAllTasks(this.sCurrentUser, bUncompletedOnly, iSelectedPriority);
			}

			final List<CollectableTask> lstclct = CollectionUtils.transform(colltaskvo, new CollectableTask.MakeCollectable());
			final PersonalTaskTableModel tblmdl = this.personaltaskview.getPersonalTaskTableModel();
			tblmdl.setCollectables(lstclct);
			tblmdl.sort();

			//	NUCLOS-642
			setupRenderers(personaltaskview.getTable());
			List<Integer> columnWidthsFromPreferences = readColumnWidthsFromPreferences();
			if(columnWidthsFromPreferences.size() == tblmdl.getColumnCount()){
				TaskController.setColumnWidths(columnWidthsFromPreferences, this.personaltaskview.getTable());
			} else {
				TableUtils.setOptimalColumnWidths(this.personaltaskview.getTable());
			}
		} catch (NuclosBusinessException ex) {
			Errors.getInstance().showExceptionDialog(this.getTabbedPane().getComponentPanel(), getSpringLocaleDelegate().getMessage(
					"PersonalTaskController.20","Fehler beim Aktualisieren der Aufgabenliste"), ex);
		} catch (Exception e) {
			LOG.error("unhandled exception: " + e.toString(), e);
		}
	}

	void printPersonalTaskView() {
		try {
			new ReportController(this.getTabbedPane().getComponentPanel()).export(this.personaltaskview.getTable(), null);
		} catch (CommonBusinessException ex) {
			Errors.getInstance().showExceptionDialog(this.getTabbedPane().getComponentPanel(), ex);
		} catch (Exception e) {
			LOG.error("unhandled exception: " + e.toString(), e);
		}
	}

	public void cmdNewPersonalTask(final boolean bRefresh, final boolean bInNewTab) {
		UIUtils.runCommandForTabbedPane(this.getTabbedPane(), new Runnable() {
			@Override
			public void run() {
				try {
					MainFrameTab tabIfAny = null;
					if (tab != null && !bInNewTab) {
						tabIfAny = new MainFrameTab();
					}
					PersonalTaskCollectController newCollectController =
						(PersonalTaskCollectController)NuclosCollectControllerFactory.getInstance().newMasterDataCollectController(NuclosEntity.TODOLIST.getEntityName(), tabIfAny, null);
					
					if (tabIfAny != null) {
						Main.getInstance().getMainController().initMainFrameTab(newCollectController, tabIfAny);
						tab.add(tabIfAny);
					}
					
					newCollectController.runNew();

				} catch (CommonBusinessException ex) {
					Errors.getInstance().showExceptionDialog(getTabbedPane().getComponentPanel(), ex);
				}
			}
		});
	}

	public void cmdEditPersonalTaskDefinition(PersonalTaskView taskview, final boolean bInNewTab) {
		final int iSelectedRow = taskview.getTable().getSelectedRow();
		if (iSelectedRow >= 0) {
			final TaskVO taskvo = taskview.getPersonalTaskTableModel().getTask(iSelectedRow);
			UIUtils.runCommandForTabbedPane(this.getTabbedPane(), new Runnable() {
				@Override
				public void run() {
					try {
						
						if (tab != null && !bInNewTab) {
							final MainFrameTab tabIfAny = new MainFrameTab();
							final NuclosCollectController<?> ctrl = NuclosCollectControllerFactory.getInstance().newCollectController(NuclosEntity.TODOLIST.getEntityName(), tabIfAny, ClientParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
							
							Main.getInstance().getMainController().initMainFrameTab(ctrl, tabIfAny);
							tab.add(tabIfAny);
							
							ctrl.runViewSingleCollectableWithId(taskvo.getId());
						} else {
							Main.getInstance().getMainController().showDetails(NuclosEntity.TODOLIST.getEntityName(), taskvo.getId());
						}
					}
					catch(CommonBusinessException e) {
						Errors.getInstance().showExceptionDialog(getTabbedPane().getComponentPanel(), e);
					}
				}
			});
		}
	}

	private void cmdPerformPersonalTask(final PersonalTaskView taskview) {
		final Collection<TaskObjectVO> collGenericObjects = getRelatedObjects(taskview);
		if(collGenericObjects != null && !collGenericObjects.isEmpty()) {
			UIUtils.runCommandForTabbedPane(this.getTabbedPane(), new CommonRunnable() {
				@Override
				public void run() throws CommonBusinessException {
					final MetaDataProvider mdProv = MetaDataClientProvider.getInstance();
					
					// (String) Entityname -> (List<Long>) ids
					final Map<String, List<Long>> eObjectIds = new HashMap<String, List<Long>>();
					final Collection<Long> collGenericObjectIds = new ArrayList<Long>(collGenericObjects.size());
					Integer iCommonModuleId = new Integer(0);
					for (TaskObjectVO tovo : collGenericObjects) {
						String entityName = tovo.getEntityName();
						if(entityName == null){
							// dead code
							assert false;
							collGenericObjectIds.add(tovo.getObjectId());
							int moduleId = GenericObjectDelegate.getInstance().getModuleContainingGenericObject(tovo.getObjectId().intValue());
							if (iCommonModuleId != null && !iCommonModuleId.equals(moduleId)) {
								if (iCommonModuleId.equals(0)) {
									iCommonModuleId = moduleId;
								} else {
									iCommonModuleId = null;
								}
							}
						} else {
							if(!eObjectIds.containsKey(entityName)){
								eObjectIds.put(entityName, new ArrayList<Long>());
							}
							List<Long> ids = eObjectIds.get(entityName);
							ids.add(tovo.getObjectId());
							eObjectIds.put(entityName, ids);
						}
					}
					/*
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
					 */

					for(String entity : eObjectIds.keySet()){
						final EntityMetaDataVO mdEntity = mdProv.getEntity(entity);
						final CollectController<?> newCollectController;
						if (mdEntity.isStateModel().booleanValue()) {
							newCollectController = NuclosCollectControllerFactory.getInstance().newGenericObjectCollectController(IdUtils.unsafeToId(mdEntity.getId()), null, ClientParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
						}
						else {
							newCollectController = NuclosCollectControllerFactory.getInstance().newMasterDataCollectController(entity, null, ClientParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
						}
						final List<Long> oIds = eObjectIds.get(entity);
						switch(oIds.size()){
							case 0:
								break;
							case 1:
								newCollectController.runViewSingleCollectableWithId(oIds.get(0));
								break;
							default:
								if (!newCollectController.isSearchPanelAvailable()){
									newCollectController.runViewAll();
								} else {
									newCollectController.runViewResults(oIds);
								}
						}
					}
				}

			});
		} else {
			final String sMessage = getSpringLocaleDelegate().getMessage(
					"PersonalTaskController.13","Dieser Aufgabe ist kein Objekt zugeordnet");
			JOptionPane.showMessageDialog(taskview, sMessage, getSpringLocaleDelegate().getMessage(
					"PersonalTaskController.26","\u00D6ffne zugeordnete(s) Objekt(e)"), JOptionPane.ERROR_MESSAGE);
		}
	}

	private Collection<TaskObjectVO> getRelatedObjects(PersonalTaskView taskview){
		Collection<TaskObjectVO> collGenericObjects = new ArrayList<TaskObjectVO>();
		List<TaskVO> lsttaskvo = getSelectedPersonalTasks(taskview);
		for(TaskVO taskvo :lsttaskvo) {
			if (!taskvo.getRelatedObjects().isEmpty())
				collGenericObjects.addAll(taskvo.getRelatedObjects());
		}
		return collGenericObjects;
	}

	private List<TaskVO> getSelectedPersonalTasks(final PersonalTaskView taskview) {
		final List<Integer> lstSelectedRowNumbers = CollectionUtils.asList(taskview.getTable().getSelectedRows());
		final List<TaskVO> result = CollectionUtils.transform(lstSelectedRowNumbers, new Transformer<Integer, TaskVO>() {
			@Override
			public TaskVO transform(Integer iRowNo) {
				return taskview.getPersonalTaskTableModel().getTask(iRowNo);
			}
		});
		assert result != null;
		return result;
	}

	private boolean areTasksCompleted(List<TaskVO> lsttaskvo) {
		for(TaskVO taskvo : lsttaskvo)
			if(taskvo.getCompleted() == null)
				return false;
		return true;
	}

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

	private class PersonalTasksPopupMenu extends JPopupMenu {

		private final JMenuItem miPerform = new JMenuItem();
		private final JCheckBoxMenuItem miComplete = new JCheckBoxMenuItem();
		protected final JMenuItem miEditDefinition = new JMenuItem();
		protected final JMenuItem miEditDefinitionInNewTab = new JMenuItem();
		protected final JMenuItem miCopyCell = new JMenuItem();
		protected final JMenuItem miCopyRows = new JMenuItem();
		private final JMenuItem miRemove = new JMenuItem();

		public PersonalTasksPopupMenu(final PersonalTaskView taskview) {
			this.add(miEditDefinition);
			this.add(miEditDefinitionInNewTab);
			this.add(miComplete);
			this.add(miPerform);
			this.add(miRemove);
			this.add(miCopyCell);
			this.add(miCopyRows);

			miComplete.setAction(actCompleteTask);
			miPerform.setAction(actPerformTask);
			miEditDefinition.setAction(actEditTask);
			UIUtils.setFontStyleBold(miEditDefinition, true);
			miEditDefinitionInNewTab.setAction(actEditTaskInNewTab);
			miCopyCell.setAction(actCopyCell);
			miCopyRows.setAction(actCopyRows);
			miRemove.setAction(actRemoveTask);
		}
	}

	private class PopupMenuListener extends DefaultJPopupMenuListener {
		private final PersonalTasksPopupMenu popupPersonalTasks;
		private final JTable tblTasks;

		public PopupMenuListener(PersonalTasksPopupMenu popupTasks, JTable tblTasks) {
			super(popupTasks);
			this.popupPersonalTasks = popupTasks;
			this.tblTasks = tblTasks;
		}

		@Override
		protected void showPopupMenu(MouseEvent ev) {
			int iRow = tblTasks.rowAtPoint(ev.getPoint());
			if (!tblTasks.isRowSelected(iRow)) {
				if ((ev.getModifiers() & MouseEvent.CTRL_MASK) != 0) {
					tblTasks.addRowSelectionInterval(iRow, iRow);
				} else {
					tblTasks.setRowSelectionInterval(iRow, iRow);
				}
			}

			final int iSelectedRowCount = this.tblTasks.getSelectedRowCount();
			this.popupPersonalTasks.miEditDefinition.setEnabled(iSelectedRowCount == 1 ? true : false);
			this.popupPersonalTasks.miEditDefinitionInNewTab.setEnabled(iSelectedRowCount == 1 ? true : false);

			final boolean bPerformEnabled = (PersonalTaskController.this.getRelatedObjects(personaltaskview) != null && !PersonalTaskController.this.getRelatedObjects(personaltaskview).isEmpty()) ? true : false;
			this.popupPersonalTasks.miPerform.setEnabled(bPerformEnabled);

			final boolean bFinished = PersonalTaskController.this.areTasksCompleted(getSelectedPersonalTasks(personaltaskview));
			this.popupPersonalTasks.miComplete.setState(bFinished);

			super.showPopupMenu(ev);
		}
	}

	private class DoubleClickListener extends MouseAdapter {
		private final PersonalTaskView taskview;

		public DoubleClickListener(PersonalTaskView taskview) {
			this.taskview = taskview;
		}

		@Override
		public void mouseClicked(MouseEvent ev) {
			if (ev.getClickCount() == 2) {
				PersonalTaskController.this.cmdEditPersonalTaskDefinition(this.taskview, PersonalTaskController.this.bOpenInNewTabIsDefault);
			}
		}
	}

	@Override
	public ScheduledRefreshable getSingleScheduledRefreshableView(){
		return this.personaltaskview;
	}

	@Override
	public void refreshScheduled(ScheduledRefreshable isRefreshable) {
		this.refreshPersonalTaskView();
	}

	@Override
	public void handleCollectableEvent(Collectable collectable,	MessageType messageType) {
		switch (messageType) {
			case EDIT_DONE :
			case NEW_DONE :
				this.refreshPersonalTaskView();
				break;
			default :
		}
	}

	/**
	 *
	 * @return
	 */
	public MainFrameTab getTab() {
		return tab;
	}

	@Override
	public void visitDragEnter(DropTargetDragEvent dtde) { }

	@Override
	public void visitDragExit(DropTargetEvent dte) { }

	@Override
	public void visitDragOver(DropTargetDragEvent dtde) {
		if (dtde.isDataFlavorSupported(TransferableGenericObjects.dataFlavor)
				|| dtde.isDataFlavorSupported(MasterDataIdAndEntity.dataFlavor)) {
			dtde.acceptDrag(dtde.getDropAction());
		}
		else {
			dtde.rejectDrag();
		}
	}

	@Override
	public void visitDrop(DropTargetDropEvent dtde) {
		final JTable tbl = personaltaskview.getTable();
		final PersonalTaskTableModel tblmdl = personaltaskview.getPersonalTaskTableModel();

		try {
			final int iSelectedRow = tbl.rowAtPoint(dtde.getLocation());
			if (iSelectedRow != -1) {
				final CollectableTask clctTarget = tblmdl.getCollectable(iSelectedRow);
				final TaskVO taskvoTarget = clctTarget.getTaskVO();

				if (taskvoTarget.getCompleted() != null) {
					final String sMessage = getSpringLocaleDelegate().getMessage(
							"PersonalTaskController.12","Erledigte Aufgaben k\u00F6nnen nicht mehr ver\u00e4ndert werden");
					throw new NuclosBusinessException(sMessage);
				}

				Collection<Long> collReleatedIds = CollectionUtils.transform(taskvoTarget.getRelatedObjects(), new Transformer<TaskObjectVO, Long>() {
					@Override
					public Long transform(TaskObjectVO in) {
						return in.getObjectId();
					}
				});

				if (dtde.isDataFlavorSupported(TransferableGenericObjects.dataFlavor)) {
					Object transferData = dtde.getTransferable().getTransferData(TransferableGenericObjects.dataFlavor);
					final Collection<GenericObjectIdModuleProcess> collgoimp = (Collection<GenericObjectIdModuleProcess>) transferData;
					for (GenericObjectIdModuleProcess goimp : collgoimp) {
						final Integer iGenericObjectId = new Integer(goimp.getGenericObjectId());
						if (!collReleatedIds.contains(iGenericObjectId)) {
							taskvoTarget.addRelatedObject(iGenericObjectId.longValue(), Modules.getInstance().getEntityNameByModuleId(goimp.getModuleId()));
						}
					}
				}
				else if (dtde.isDataFlavorSupported(MasterDataIdAndEntity.dataFlavor)) {
					Object transferData = dtde.getTransferable().getTransferData(MasterDataIdAndEntity.dataFlavor);
				  	final Collection<MasterDataIdAndEntity> collimp = (Collection<MasterDataIdAndEntity>)transferData;
					for (MasterDataIdAndEntity mdimp : collimp) {
						final Integer iMdId = (Integer)(mdimp.getId());
						if(!collReleatedIds.contains(iMdId)) {
							taskvoTarget.addRelatedObject(iMdId.longValue(), mdimp.getEntity());
						}
					}
				}
				else {
					throw new UnsupportedFlavorException(dtde.getCurrentDataFlavors()[0]);
				}

				final TaskVO taskvoUpdated = taskDelegate.update(taskvoTarget, null);
				tblmdl.setCollectable(iSelectedRow, new CollectableTask(taskvoUpdated));
			}
		}
		catch (UnsupportedFlavorException ex) {
			JOptionPane.showMessageDialog(getTab(), getSpringLocaleDelegate().getMessage(
					"PersonalTaskController.14","Dieser Datentransfer wird von dem ausgew\u00e4hlten Objekt nicht unterst\u00fctzt"));
		}
		catch (IOException ex) {
			throw new NuclosFatalException(ex);
		}
		catch (CommonBusinessException ex) {
			Errors.getInstance().showExceptionDialog(getTab(), ex);
		}
	}

	@Override
	public void visitDropActionChanged(DropTargetDragEvent dtde) {	}
}
