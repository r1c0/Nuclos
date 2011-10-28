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

import java.awt.Component;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.explorer.ExplorerController;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.main.mainframe.MainFrameTabbedPane;
import org.nuclos.client.main.mainframe.workspace.ITabStoreController;
import org.nuclos.client.main.mainframe.workspace.TabRestoreController;
import org.nuclos.client.searchfilter.EntitySearchFilter;
import org.nuclos.client.searchfilter.SearchFilterCache;
import org.nuclos.client.timelimit.TimelimitTaskController;
import org.nuclos.client.timelimit.TimelimitTaskDelegate;
import org.nuclos.client.ui.Controller;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.MainFrameTabAdapter;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.CollectableTableHelper;
import org.nuclos.client.ui.table.TableUtils;
import org.nuclos.common.Actions;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.ClientPreferences;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.PreferencesException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Controller for the whole task panel. The control of the personal tasks is
 * delegated to <code>PersonalTaskController</code>. <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author <a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class TaskController extends Controller {

	private final static Logger LOG = Logger.getLogger(TaskController.class);

	private static final String PREFS_NODE_TASKPANEL = "taskPanel";
	@Deprecated
	private static final String PREFS_NODE_FILTERS = "filters";
	@Deprecated
	private static final String PREFS_NODE_TASKPANEL_TABS = "taskPanelTabs";
	@Deprecated
	private static final String PREFS_NODE_REFRESH_INTERVAL = "refreshInterval";

	private final Preferences prefs = ClientPreferences.getUserPreferences().node(PREFS_NODE_TASKPANEL);
	private final TaskDelegate taskdelegate = new TaskDelegate();
	private final TimelimitTaskDelegate timelimittaskdelegate = new TimelimitTaskDelegate();
	private ExplorerController ctlExplorer;
	private final PersonalTaskController ctlPersonalTasks;
	private final TimelimitTaskController ctlTimelimitTasks;
	private final GenericObjectTaskController ctlGenericObjectTasks;

	private final Map<GenericObjectTaskView, MainFrameTab> taskTabs = new HashMap<GenericObjectTaskView, MainFrameTab>();

	/**
	 *
	 * @param parent
	 * @param parentForGenericObjectController
	 * @param pnlTasks
	 * @param sCurrentUser
	 */
	public TaskController(Component parent, String sCurrentUser) {
		super(parent);

		ctlPersonalTasks = new PersonalTaskController(parent, prefs, taskdelegate, sCurrentUser);
		ctlTimelimitTasks = new TimelimitTaskController(parent, prefs, timelimittaskdelegate);
		ctlGenericObjectTasks = new GenericObjectTaskController(parent);

	}

	public void setExplorerController(ExplorerController ctlExplorer) {
		this.ctlExplorer = ctlExplorer;
		ctlPersonalTasks.setExplorerController(ctlExplorer);
		ctlTimelimitTasks.setExplorerController(ctlExplorer);
	}

	public void run() {
		// initialize the lists:
		refreshAllTaskViews();
	}

	public void close() throws PreferencesException {
		ctlPersonalTasks.storeOrderBySelectedColumnToPreferences();
		ctlPersonalTasks.storeShowAllTasksIsPressedToPreferences();
		ctlPersonalTasks.storeShowDelegatedTasksIsSelectedToPreferences();
	}

	public static class RestorePreferences implements Serializable {
		private static final long serialVersionUID = 6637996725938917463L;

		public static final int GENERIC = -1;
		public static final int PERSONAL = 1;
		public static final int TIMELIMIT = 2;

		/**
		 * use GENERIC, PERSONAL or TIMELIMIT
		 */
		Integer type;
		Integer refreshInterval;
		/**
		 * only for type GENERIC
		 */
		Integer searchFilterId;
	}

	private static String toXML(RestorePreferences rp) {
		XStream xstream = new XStream(new DomDriver());
		return xstream.toXML(rp);
	}

	private static RestorePreferences fromXML(String xml) {
		XStream xstream = new XStream(new DomDriver());
		return (RestorePreferences) xstream.fromXML(xml);
	}

	/**
	 *
	 *
	 */
	public static class TaskTabStoreController implements ITabStoreController {

		int type;
		TaskView view;

		public TaskTabStoreController(int type, TaskView view) {
			this.view = view;
			this.type = type;
		}

		@Override
		public Class<?> getTabRestoreControllerClass() {
			return TaskTabRestoreController.class;
		}

		@Override
		public String getPreferencesXML() {
			RestorePreferences rp = new RestorePreferences();
			rp.type = type;
			rp.refreshInterval = view.getRefreshInterval();
			if (type == rp.GENERIC) {
				rp.searchFilterId = ((GenericObjectTaskView)view).getFilter().getId();
			}

			return toXML(rp);
		}
	}

	/**
	 *
	 *
	 */
	public static class TaskTabRestoreController extends TabRestoreController {

		@Override
		public void restoreFromPreferences(String preferencesXML, final MainFrameTab tab) throws Exception {
			RestorePreferences rp = fromXML(preferencesXML);

			switch (rp.type) {
				case RestorePreferences.PERSONAL:
					if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_TASKLIST)) {
						Main.getMainController().getTaskController().getPersonalTaskController().showPersonalTasks(tab);
					} else {
						throw new IllegalArgumentException("Personal Tasks not granted any more");
					}
					break;
				case RestorePreferences.TIMELIMIT:
					if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_TIMELIMIT_LIST)) {
						Main.getMainController().getTaskController().getTimelimitTaskController().showTimelimitTasks(tab);
					} else {
						throw new IllegalArgumentException("Timelimit Tasks not granted any more");
					}
					break;
				case RestorePreferences.GENERIC:
					EntitySearchFilter filter = SearchFilterCache.getInstance().getEntitySearchFilterById(rp.searchFilterId);
					Main.getMainController().getTaskController().addOrReplaceGenericObjectTaskViewFor(filter, tab);
					break;
				default:
					throw new IllegalArgumentException("Task type: "+rp.type);
			}
		}

	}

	private Preferences getPreferences(GenericObjectTaskView goTaskView) {
		return org.nuclos.common2.ClientPreferences.getUserPreferences().node("taskPanel").node(goTaskView.getFilter().getId().toString());
	}

	/**
	 * stores the order of the columns in the table
	 */
	private void storeColumnOrderAndWidthsInPrefs(GenericObjectTaskView goTaskView) throws PreferencesException {
		PreferencesUtils.putStringList(getPreferences(goTaskView), CollectController.PREFS_NODE_SELECTEDFIELDS,
			CollectableTableHelper.getFieldNamesFromColumns(goTaskView.getJTable()));
		PreferencesUtils.putIntegerList(getPreferences(goTaskView), CollectController.PREFS_NODE_SELECTEDFIELDWIDTHS,
			CollectableTableHelper.getColumnWidths(goTaskView.getJTable()));
	}

	private void storeSortingColumnToPrefs(GenericObjectTaskView goTaskView) {
		// Not clear: The table was resorted with a model-to-view conversion, but why?
		// tblModel.getSortedColumn() returns already a model-based index, so converting it gives a nonsensical result.
		// Call sort with sortImmediately=true to see the problem.
		// SortableCollectableTableModel<Collectable> tblModel = ((SortableCollectableTableModel<Collectable>)goTaskView.getTableModel());
		// tblModel.setSortingOrder(goTaskView.getJTable().convertColumnIndexToModel(tblModel.getSortedColumn()),tblModel.isSortedAscending(),false);
		goTaskView.storeOrderBySelectedColumnToPreferences();
	}

	/**
	 * ONLY FOR MIGRATION
	 * @return
	 * @throws PreferencesException
	 */
	@Deprecated
	private List<String> getFiltersFromPreferences() throws PreferencesException {
		return PreferencesUtils.getStringList(prefs, PREFS_NODE_FILTERS);
	}

	/**
	 * ONLY FOR MIGRATION
	 * @return
	 * @throws PreferencesException
	 * @throws BackingStoreException
	 */
	@Deprecated
	private Map<String,Integer> getRefreshIntervalsFromPreferences() throws PreferencesException, BackingStoreException {
		Map<String,Integer> refreshIntervals = new HashMap<String,Integer>();

		String[] tabNames = null;
		try {
			tabNames = prefs.node(PREFS_NODE_TASKPANEL_TABS).childrenNames();
		}
		catch(BackingStoreException e) {
			/*dann halt nicht -> return empty map*/
			LOG.info("getRefreshIntervalsFromPreferences failed: " + e);
			return refreshIntervals;
		}
		for(String tabName : tabNames) {
			Integer[] refreshInterval = PreferencesUtils.getIntegerArray(prefs.node(PREFS_NODE_TASKPANEL_TABS).node(tabName), PREFS_NODE_REFRESH_INTERVAL);
			if(refreshInterval != null && refreshInterval.length == 1){
				refreshIntervals.put(tabName, refreshInterval[0]);
			}
		}
		// remove nodes and never use it again...
		prefs.node(PREFS_NODE_TASKPANEL_TABS).removeNode();
		return refreshIntervals;
	}

	/**
	 * ONLY FOR MIGRATION
	 * restores the generic object views from the preferences
	 * @throws BackingStoreException
	 */
	@Deprecated
	public void restoreGenericObjectTaskViewsFromPreferences()
	throws PreferencesException, BackingStoreException {
		final List<String> lstFilterNames = getFiltersFromPreferences();
		// remove nodes and never use it again...
		prefs.node(PREFS_NODE_FILTERS).removeNode();
		Map<String, Integer> mapRefreshIntervals = getRefreshIntervalsFromPreferences();
		int i = 0;

		//search for task tabbed...
		MainFrameTabbedPane taskTabbed = null;
		for (MainFrameTabbedPane tabbedPane : MainFrame.getAllTabbedPanes()) {
			if (!tabbedPane.isHome() && !tabbedPane.isHomeTree()) {
				taskTabbed = tabbedPane;
			}
		}
		if (taskTabbed == null) Main.getMainFrame().getHomePane();

		for (String sFilterName : lstFilterNames) {
			try {
				Integer refreshInterval = mapRefreshIntervals.get(sFilterName);
				refreshInterval = (refreshInterval != null && refreshInterval >= 0)? refreshInterval : 0;
				if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_TASKLIST) &&
					sFilterName.startsWith(CommonLocaleDelegate.getMessage("tabMyTasks", "Meine Aufgaben"))) {
					MainFrameTab tab = new MainFrameTab();
					taskTabbed.addTab(tab, false);
					ctlPersonalTasks.showPersonalTasks(tab);
					ctlPersonalTasks.setRefreshIntervalForSingleViewRefreshable(refreshInterval);
					MainFrame.setSelectedTab(tab);
				}
				else if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_TIMELIMIT_LIST) &&
					sFilterName.startsWith(CommonLocaleDelegate.getMessage("tabTimelimits", "Fristen"))) {
					MainFrameTab tab = new MainFrameTab();
					taskTabbed.addTab(tab, false);
					ctlTimelimitTasks.showTimelimitTasks(tab);
					ctlTimelimitTasks.setRefreshIntervalForSingleViewRefreshable(refreshInterval);
				}
				else {
					try {
						Integer iSearchFilterId = new Integer(sFilterName);
						final EntitySearchFilter filter = SearchFilterCache.getInstance().getEntitySearchFilterById(iSearchFilterId);

						if (filter != null) {
							MainFrameTab tab = new MainFrameTab();
							taskTabbed.addTab(tab, false);
							try {
								addOrReplaceGenericObjectTaskViewFor(filter, tab);
								ctlGenericObjectTasks.setRefreshIntervalForMultiViewRefreshable(getTaskViewFor(filter), refreshInterval);
							} catch(CommonBusinessException e) {
								LOG.warn("restoreGenericObjectTaskViewsFromPreferences failed: " + e);
							}
						}
					}
					catch (NumberFormatException e) {
						LOG.info("restoreGenericObjectTaskViewsFromPreferences failed: " + e);
						continue;
					}
				}
			}
			catch (NoSuchElementException ex) {
				LOG.warn("A search filter named \"" + sFilterName
					+ "\" does not exist.");
				// otherwise ignored
			}
			i++;
		}
	}

	public void addForcedFilters() {
		for(EntitySearchFilter entitySearchFilter : SearchFilterCache.getInstance().getForcedFilters()){
			if(entitySearchFilter.isValid()){
				addOrReplaceGenericObjectTaskViewFor(entitySearchFilter, false);
			}
		}
	}

	/**
	 * adds a tab for the given treenode, if it is not contained already.
	 * Otherwise, replaces the view containing the treenode, if it is not
	 * identical to the given treenode. In both cases, selects the view
	 * afterwards.
	 *
	 * @param filter
	 */
	private GenericObjectTaskView addOrReplaceGenericObjectTaskViewFor(EntitySearchFilter filter, boolean select) {
		if (!filter.isValid()) {
			return null;
		}

		MainFrameTab tab = new MainFrameTab();
		try {
			if (addOrReplaceGenericObjectTaskViewFor(filter, tab)) {
				MainFrame.addTab(tab);
				if (select) {
					MainFrame.setSelectedTab(tab);
				}
			}
			return getTaskViewFor(filter);
		}
		catch(CommonBusinessException e) {
			LOG.error("addOrReplaceGenericObjectTaskViewFor failed: " + e, e);
			return null;
		}
	}

	/**
	 *
	 * @param filter
	 * @param tab
	 * @return true if added, false if replaced
	 * @throws CommonBusinessException
	 */
	private boolean addOrReplaceGenericObjectTaskViewFor(EntitySearchFilter filter, MainFrameTab tab) throws CommonBusinessException {
		if (!filter.isValid()) {
			throw new CommonBusinessException("Filter " + filter.getName() + " is not valid");
		}

		GenericObjectTaskView view = getTaskViewFor(filter);
		if(view == null) {
			// This filter is not yet in the task list; create a new tab for it

			final GenericObjectTaskView newView = ctlGenericObjectTasks.newGenericObjectTaskView(filter);
			final String sLabel = StringUtils.isNullOrEmpty(filter.getLabelResourceId()) ? filter.getName() : CommonLocaleDelegate.getTextFallback(filter.getLabelResourceId(), filter.getName());
			tab.addMainFrameTabListener(new MainFrameTabAdapter() {
				@Override
				public boolean tabClosing(MainFrameTab tab) {
					removeGenericObjectTaskView(newView);
					return true;
				}
				@Override
				public void tabClosed(MainFrameTab tab) {
					tab.removeMainFrameTabListener(this);
				}
			});

			tab.setTabIcon(Icons.getInstance().getIconFilter16());
			tab.setTitle(sLabel);
			tab.setLayeredComponent(newView);
			tab.setTabStoreController(new TaskTabStoreController(RestorePreferences.GENERIC, newView));

			taskTabs.put(newView, tab);

			setupColumnModelListener(newView);
			return true;
		} else {
			if(filter != view.getFilter()) {
				view.setFilter(filter);
				ctlGenericObjectTasks.refresh(view);
			}
			return false;
		}
	}

	private void setupColumnModelListener(final GenericObjectTaskView goTaskView) {
		goTaskView.getJTable().getColumnModel().addColumnModelListener(new TableColumnModelListener() {

			@Override
			public void columnMoved(TableColumnModelEvent ev) {

				if (ev.getFromIndex() != ev.getToIndex()) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							try {
								TaskController.this.storeColumnOrderAndWidthsInPrefs(goTaskView);
								TaskController.this.storeSortingColumnToPrefs(goTaskView);
							}
							catch (PreferencesException ex) {
								final String sMessage = CommonLocaleDelegate.getMessage("tasklist.error.column.order", "Die Spaltenreihenfolge konnte nicht gespeichert werden.");
								Errors.getInstance().showExceptionDialog(goTaskView.getJTable(), sMessage, ex);
							}
						}
					});
				}
			}

			@Override
			public void columnAdded(TableColumnModelEvent ev) {}
			@Override
			public void columnRemoved(TableColumnModelEvent ev) {}
			@Override
			public void columnMarginChanged(ChangeEvent ev) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							TaskController.this.storeColumnOrderAndWidthsInPrefs(goTaskView);
						}
						catch (PreferencesException ex) {
							final String sMessage = CommonLocaleDelegate.getMessage("tasklist.error.column.order", "Die Spaltenreihenfolge konnte nicht gespeichert werden.");
							Errors.getInstance().showExceptionDialog(goTaskView.getJTable(), sMessage, ex);
						}
					}
				});
			}
			@Override
			public void columnSelectionChanged(ListSelectionEvent ev) {}
		});
	}

	/**
	 * closes the tab with the given index
	 *
	 * @param view
	 */
	private void closeGenericObjectTaskView(GenericObjectTaskView view)	throws PreferencesException, CommonBusinessException {

		if (view.getFilter().isForced()) {
			throw new NuclosBusinessException(CommonLocaleDelegate.getMessage("tasklist.error.searchfilter.fixed", "Der Suchfilter ist fixiert und darf nicht entfernt werden."));
		}

		MainFrame.closeTab(getTabFor(view));
		ctlGenericObjectTasks.removeGenericObjectTaskView(view);
	}

	/**
	 * searches for a GenericObjectTaskView containing the given
	 * filter.
	 *
	 * @param filter
	 * @return the view containing the <code>filter</code> or
	 *         <code>null</code>, if none was found.
	 * @todo maybe this should be refactored to int indexOfTabTaskViewFor(String
	 *       sFilterName), but then the filter name must be unique across all
	 *       modules.
	 */
	private GenericObjectTaskView getTaskViewFor(EntitySearchFilter filter) {

		GenericObjectTaskView result = null;
		for (GenericObjectTaskView view : taskTabs.keySet()) {
			final EntitySearchFilter filter1 = view.getFilter();
			if (filter1.equals(filter)) {
				result = view;
				break;
			}
		}
		return result;
	}

	protected MainFrameTab getTabFor(GenericObjectTaskView view) {
		MainFrameTab result = taskTabs.get(view);
		if (result == null) {
			throw new NuclosFatalException("No tab for GenericObjectTaskView found");
		}
		return result;
	}

	/**
	 * refreshes all task views
	 */
	public void refreshAllTaskViews() {
		try {
			ctlPersonalTasks.refreshPersonalTaskView();
			ctlTimelimitTasks.refreshTimelimitTaskView();
			for (GenericObjectTaskView gotaskview : taskTabs.keySet()) {
				ctlGenericObjectTasks.refresh(gotaskview);
			}
		}
		catch (NuclosFatalException ex) {
			Errors.getInstance().showExceptionDialog(getParent(), CommonLocaleDelegate.getMessage("tasklist.error.load", "Die Aufgaben-Liste kann nicht geladen werden."), ex);
		}
	}

	/**
	 * shows the filter with the given name in the task panel
	 *
	 * @param iModuleId
	 * @param sFilterName
	 */
	public void cmdShowFilterInTaskPanel(final EntitySearchFilter searchFilter) {

		UIUtils.runCommand(getParent(), new Runnable() {
			@Override
			public void run() {
				TaskController.this.addOrReplaceGenericObjectTaskViewFor(searchFilter, true);
			}
		});
	}

	/**
	 * hides the filter with the given name in the task panel
	 *
	 * @param sFilterName
	 */
	public void hideFilterInTaskPanel(EntitySearchFilter searchFilter) throws CommonBusinessException {
		final GenericObjectTaskView view = getTaskViewFor(searchFilter);
		if (view != null) {
			try {
				closeGenericObjectTaskView(view);
			}
			catch (PreferencesException ex) {
				throw new NuclosFatalException(CommonLocaleDelegate.getMessage("tasklist.error.delete.searchfilter", "Der Suchfilter {0} konnte nicht aus der Aufgabenleiste entfernt werden.", searchFilter.getName()), ex);
			}
		}
	}

	public ExplorerController getExplorerController() {
		assert ctlExplorer != null;
		return ctlExplorer;
	}

	/**
	 * sets all column widths to user preferences; set optimal width if no preferences yet saved
	 * @param tableColumnWidthsFromPreferences
	 */
	public static final void setColumnWidths(List<Integer> tableColumnWidthsFromPreferences, JTable tbl) {
		boolean useCustomColumnWidths = tableColumnWidthsFromPreferences != null && !tableColumnWidthsFromPreferences.isEmpty() && tableColumnWidthsFromPreferences.size() == tbl.getColumnCount();
		if(useCustomColumnWidths) {
			assert(tableColumnWidthsFromPreferences.size() == tbl.getColumnCount());
			Enumeration<TableColumn> columns = tbl.getColumnModel().getColumns();
			for(int colIdx = 0; columns.hasMoreElements(); colIdx++) {
				TableColumn column = columns.nextElement();
				int prefdColWidth = tableColumnWidthsFromPreferences.get(colIdx);
				column.setPreferredWidth(prefdColWidth);
				column.setWidth(prefdColWidth);
			}
		}
		else {
			// If there are no stored field widths or the number of stored field widths differs from the column count
			// (that is, the number of columns has changed since the last invocation of the client), set optimal column widths:
			TableUtils.setOptimalColumnWidths(tbl);
		}
		tbl.revalidate();
	}

	public PersonalTaskController getPersonalTaskController() {
		return this.ctlPersonalTasks;
	}

	public TimelimitTaskController getTimelimitTaskController() {
		return this.ctlTimelimitTasks;
	}

	private void removeGenericObjectTaskView(GenericObjectTaskView view) {
		taskTabs.remove(view);
	}

	/**
	 *
	 * @param tab
	 */
	public boolean isTaskTab(MainFrameTab tab) {
		if (taskTabs.values().contains(tab))
			return true;

		if (ctlPersonalTasks.getTab() == tab)
			return true;

		if (ctlTimelimitTasks.getTab() == tab)
			return true;

		if (tab.getContent() instanceof TaskView)
			return true;

		return false;
	}

} // class TaskController
