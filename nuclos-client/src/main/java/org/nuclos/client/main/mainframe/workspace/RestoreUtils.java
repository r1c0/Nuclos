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

package org.nuclos.client.main.mainframe.workspace;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import org.apache.log4j.Logger;
import org.nuclos.client.common.prefs.PreferencesMigration;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.ExternalFrame;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.main.mainframe.MainFrameTabbedPane;
import org.nuclos.client.ui.CommonJFrame;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.MainFrameTabAdapter;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.Actions;
import org.nuclos.common.WorkspaceDescription;
import org.nuclos.common.WorkspaceDescription.Frame;
import org.nuclos.common.WorkspaceDescription.MutableContent;
import org.nuclos.common.WorkspaceDescription.Split;
import org.nuclos.common.WorkspaceDescription.Tabbed;
import org.nuclos.common.WorkspaceDescriptionDefaultsFactory;
import org.nuclos.common.WorkspaceVO;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.common.ejb3.PreferencesFacadeRemote;

public class RestoreUtils {

	private static final Logger LOG = Logger.getLogger(RestoreUtils.class);

	private static final String THREAD_NAME = "Workspace restoring...";
	private static final List<Thread> threadList = new ArrayList<Thread>();

	/**
	 *
	 * @param wdTab
	 * @param tab
	 * @return
	 */
	private synchronized static boolean restoreTab(WorkspaceDescription.Tab wdTab, MainFrameTab tab) {
		try {
			TabRestoreController restoreController = (TabRestoreController) Class.forName(wdTab.getRestoreController()).getConstructor().newInstance();
			restoreController.restoreFromPreferences(wdTab.getPreferencesXML(), tab);
		} catch(Exception e) {
			LOG.warn("TabRestoreController could not be created or restore failed:", e);
			return false;
		}
		return true;
	}

	/**
	 *
	 * @param content
	 * @param frame
	 * @return
	 */
	private synchronized static ContentRestorer createContentRestorer(WorkspaceDescription.MutableContent content, JFrame frame) {
		if (content.getContent() instanceof WorkspaceDescription.Split) {
			return new SplitRestorer((Split) content.getContent(), frame);
		} else if (content.getContent() instanceof WorkspaceDescription.Tabbed) {
			return new TabbedRestorer((Tabbed) content.getContent(), frame);
		} else {
			throw new IllegalArgumentException(content.getClass().getName());
		}
	}

	/**
	 *
	 * @param wdFrame
	 */
	private synchronized static void restoreFrame(WorkspaceDescription.Frame wdFrame) {
		WorkspaceFrame wsFrame;
		if (wdFrame.isMainFrame()) {
			wsFrame = Main.getMainFrame();
		} else {
			wsFrame = MainFrame.createWorkspaceFrame();
		}

		CommonJFrame frame = wsFrame.getFrame();

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = ge.getScreenDevices();

		boolean restoreAtStoredPosition = false;
		LOG.info("stored frame bounds: " + wdFrame.getNormalBounds());
		for (GraphicsDevice gd : devices) {
			if (restoreAtStoredPosition) break;

			GraphicsConfiguration[] gc = gd.getConfigurations();
			for (int i=0; i < gc.length && !restoreAtStoredPosition; i++) {
				Rectangle deviceBounds = gc[i].getBounds();
				LOG.info("device bounds: " + deviceBounds);
				if (!deviceBounds.contains(wdFrame.getNormalBounds())) {
					restoreAtStoredPosition = true;
				}
			}
		}

		if (!restoreAtStoredPosition) {
			wdFrame.getNormalBounds().x = 40;
			wdFrame.getNormalBounds().y = 40;
		}
		frame.setNormalBounds(wdFrame.getNormalBounds());
		frame.setBounds(wdFrame.getNormalBounds());
		frame.setExtendedState(wdFrame.getExtendedState());

		ContentRestorer cr = createContentRestorer(wdFrame.getContent(), frame);
		wsFrame.setFrameContent(cr.getEmptyContent());
		cr.restoreContent();

		MainFrame.updateTabbedPaneActions(frame);

		frame.setVisible(true);
	}

	/**
	 * 
	 * @param lastWorkspaceIdFromPreferences
	 * @param lastWorkspaceFromPreferences
	 * @throws CommonBusinessException 
	 */
	public static void restoreWorkspaceThreaded(Long lastWorkspaceIdFromPreferences, String lastWorkspaceFromPreferences) throws CommonBusinessException {
		WorkspaceVO wovoToRestore = null;
		for (WorkspaceVO wovo : MainFrame.getWorkspaceHeaders()) {
			if (lastWorkspaceIdFromPreferences.equals(wovo.getId())) {
				wovoToRestore = wovo;
				break; // priority of id is higher
			}
			// if id not found try to search for name
			if (lastWorkspaceFromPreferences.equals(wovo.getName())) {
				wovoToRestore = wovo; 
			}
		}
		
		if (wovoToRestore == null && !MainFrame.getWorkspaceHeaders().isEmpty()) {
			wovoToRestore = MainFrame.getWorkspaceHeaders().get(0);
		}
		
		if (wovoToRestore == null) {
			wovoToRestore = createDefaultWorkspace(createDefaultWorkspace());
			MainFrame.refreshWorkspacesHeaders();
		}
		restoreWorkspaceThreaded(wovoToRestore);
	}

	/**
	 *
	 * @param name
	 * @param restoreToDefault
	 * @throws CommonBusinessException 
	 */
	private synchronized static void restoreWorkspaceThreaded(WorkspaceVO wovo) throws CommonBusinessException {
		checkRestoreRunning();

		MainFrame.setWorkspaceManagementEnabled(false);

		//load from db. wovo contains header only
		WorkspaceDescription wd;
		if (wovo.getWoDesc().getFrames().isEmpty()) {
			try {
				wd = getPrefsFacade().getWorkspace(wovo.getId()).getWoDesc();
			} catch (Exception e) {
				try {
					wovo = createDefaultWorkspace(wovo.getWoDesc());
					wd = wovo.getWoDesc();
				} catch (Exception e1) {
					wd = WorkspaceDescriptionDefaultsFactory.createOldMdiStyle();
				}
			}
			if (wd.getFrames().isEmpty()) {
				wd = WorkspaceDescriptionDefaultsFactory.createOldMdiStyle();
			}
		} else {
			wd = wovo.getWoDesc();
		}
		
		try {
			wd.getHomeTabbed();
			wd.getHomeTreeTabbed();
		} catch (Exception e) {
			wd = WorkspaceDescriptionDefaultsFactory.createOldMdiStyle();
		}

		// set workspace preferences
		wovo.getWoDesc().removeAllEntityPreferences();
		wovo.getWoDesc().addAllEntityPreferences(wd.getEntityPreferences());
		
		MainFrame.setWorkspace(wovo);
		PreferencesMigration.migrateEntityAndSubFormColumnPreferences();
		
		threadList.clear();

		MainFrame.resetExternalFrameNumber();
		for (WorkspaceDescription.Frame wdFrame : CollectionUtils.sorted(wd.getFrames(), new Comparator<WorkspaceDescription.Frame>() {
			@Override
			public int compare(Frame o1, Frame o2) {
				return LangUtils.compare(o1.getNumber(), o2.getNumber());
			}
		})) {
			restoreFrame(wdFrame);
		}

		// refresh menus for window list
		Main.getMainController().refreshMenus();

		final Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				MainFrame.hideProgress();
				MainFrame.setWorkspaceManagementEnabled(true);
				MainFrame.setActiveTabNavigation(MainFrame.getHomePane());
				threadList.clear();
			}
		}, THREAD_NAME);
		t.setDaemon(true);
		threadList.add(t);

		MainFrame.showProgress(threadList.size());
		if (threadList.size() > 0) {
			threadList.get(0).start();
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws CommonBusinessException 
	 */
	public static WorkspaceDescription createDefaultWorkspace() throws CommonBusinessException {
		WorkspaceDescription wd = new WorkspaceDescription();
		wd.setName(MainFrame.getDefaultWorkspace());
		wd.setHideName(true);
		wd.setNuclosResource("org.nuclos.client.resource.icon.glyphish.174-imac.png");
		return wd;
	}
	
	/**
	 * 
	 * @param name
	 * @param hideName
	 * @param nuclosResource (nullable)
	 * @return
	 * @throws CommonBusinessException 
	 */
	public static WorkspaceVO createDefaultWorkspace(WorkspaceDescription wdOrigin) throws CommonBusinessException {
		WorkspaceVO wovo = new WorkspaceVO(WorkspaceDescriptionDefaultsFactory.createOldMdiStyle());
		wovo.importHeader(wdOrigin);
		wovo = getPrefsFacade().storeWorkspace(wovo);
		return wovo;
	}

	/**
	 *
	 * @param wdTabbed
	 * @param tab
	 * @return
	 */
	private synchronized static boolean storeTab(WorkspaceDescription.Tabbed wdTabbed, MainFrameTab tab) {
		if (tab.hasTabStoreController()) {
			final WorkspaceDescription.Tab wdTab = new WorkspaceDescription.Tab();
			wdTab.setLabel(tab.getTitle());
			wdTab.setNeverClose(tab.isNeverClose());
			wdTab.setFromAssigned(tab.isFromAssigned());
			wdTab.setPreferencesXML(tab.getTabStoreController().getPreferencesXML());
			wdTab.setRestoreController(tab.getTabStoreController().getTabRestoreControllerClass().getName());
			wdTabbed.addTab(wdTab);
			return true;
		} else {
			return false;
		}
	}

	/**
	 *
	 * @param content
	 * @param tabbedPane
	 */
	private synchronized static void storeTabbedPane(MutableContent content, MainFrameTabbedPane tabbedPane) {
		final WorkspaceDescription.Tabbed wdTabbed = new WorkspaceDescription.Tabbed();
		content.setContent(wdTabbed);

		wdTabbed.setHome(tabbedPane.isHome());
		wdTabbed.setHomeTree(tabbedPane.isHomeTree());
		wdTabbed.setNeverHideStartmenu(tabbedPane.isNeverHideStartmenu());
		wdTabbed.setNeverHideHistory(tabbedPane.isNeverHideHistory());
		wdTabbed.setNeverHideBookmark(tabbedPane.isNeverHideBookmark());
		wdTabbed.setAlwaysHideStartmenu(tabbedPane.isAlwaysHideStartmenu());
		wdTabbed.setAlwaysHideHistory(tabbedPane.isAlwaysHideHistory());
		wdTabbed.setAlwaysHideBookmark(tabbedPane.isAlwaysHideBookmark());
		wdTabbed.setShowAdministration(tabbedPane.isShowAdministration());
		wdTabbed.setShowConfiguration(tabbedPane.isShowConfiguration());
		wdTabbed.setShowEntity(tabbedPane.isShowEntity());
		wdTabbed.addAllPredefinedEntityOpenLocations(MainFrame.getPredefinedEntityOpenLocations(tabbedPane));
		wdTabbed.addAllReducedStartmenus(tabbedPane.getReducedStartmenus());
		wdTabbed.addAllReducedHistoryEntities(tabbedPane.getReducedHistoryEntities());
		wdTabbed.addAllReducedBookmarkEntities(tabbedPane.getReducedBookmarkEntities());
		wdTabbed.setSelected(-1);
		int tabOrder = 0;
		for (MainFrameTab tab : tabbedPane.getHiddenTabs()) {
			if (storeTab(wdTabbed, tab)) {
				tabOrder++;
			}
		}

		final int selected = tabbedPane.getSelectedIndex();
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			if (tabbedPane.getComponentAt(i) instanceof MainFrameTab) {
				if (storeTab(wdTabbed, (MainFrameTab) tabbedPane.getComponentAt(i))) {
					if (selected == i) {
						wdTabbed.setSelected(tabOrder);
					}
					tabOrder++;
				}
			}
		}
	}

	/**
	 *
	 * @param content
	 * @param splitPane
	 */
	private synchronized static void storeSplitPane(MutableContent content, JSplitPane splitPane) {
		final WorkspaceDescription.Split wdSplit = new WorkspaceDescription.Split();
		content.setContent(wdSplit);

		wdSplit.setHorizontal(splitPane.getOrientation()==JSplitPane.HORIZONTAL_SPLIT);
		wdSplit.setPosition(splitPane.getDividerLocation());
		storeContent(wdSplit.getContentA(), splitPane.getLeftComponent());
		storeContent(wdSplit.getContentB(), splitPane.getRightComponent());
	}

	/**
	 *
	 * @param content
	 * @param comp
	 */
	private synchronized static void storeContent(MutableContent content, Component comp) {
		if (comp instanceof JSplitPane) {
			storeSplitPane(content, (JSplitPane) comp);
		} else if (comp instanceof MainFrameTabbedPane) {
			storeTabbedPane(content, (MainFrameTabbedPane) comp);
		} else {
			throw new IllegalArgumentException(comp.toString());
		}
	}

	/**
	 *
	 * @param wd
	 * @param frame
	 */
	private synchronized static void storeFrame(WorkspaceDescription wd, CommonJFrame frame) {
		if (frame instanceof WorkspaceFrame) {

			final WorkspaceDescription.Frame wdFrame = new WorkspaceDescription.Frame();
			wdFrame.setExtendedState(frame.getExtendedState());
			wdFrame.setNormalBounds(frame.getNormalBounds());
			wdFrame.setMainFrame(Main.getMainFrame()==frame);
			wdFrame.setNumber(((WorkspaceFrame) frame).getNumber());
			storeContent(wdFrame.getContent(), ((WorkspaceFrame) frame).getFrameContent());

			wd.addFrame(wdFrame);
		}
	}

	/**
	 *
	 * @param name
	 * @throws CommonBusinessException 
	 */
	public synchronized static WorkspaceVO storeWorkspace(WorkspaceVO wovo) throws CommonBusinessException {
		if (wovo == null) {
			return null;
		}
		
		if (isRestoreRunning()) {
			// do not store workspace if restore is running...
			return null;
		}

		WorkspaceDescription wd = wovo.getWoDesc();
		wd.getFrames().clear();

		MainFrame.restoreAllTabbedPaneContainingArea();
		for (CommonJFrame frame : MainFrame.getOrderedFrames()) {
			storeFrame(wd, frame);
		}

		return getPrefsFacade().storeWorkspace(wovo);
	}

	/**
	 *
	 * @param name
	 * @throws CommonBusinessException 
	 */
	public synchronized static boolean clearAndRestoreWorkspace(WorkspaceVO wovo) throws CommonBusinessException {
		if (clearWorkspace()) {
			restoreWorkspaceThreaded(wovo);
			return true;
		}
		return false;
	}
	
	/**
	 *
	 * @param name
	 * @throws CommonBusinessException 
	 */
	public synchronized static WorkspaceVO clearAndRestoreToDefaultWorkspace() throws CommonBusinessException {
		return clearAndRestoreToDefaultWorkspace(createDefaultWorkspace());
	}

	/**
	 *
	 * @param wd
	 * @throws CommonBusinessException 
	 */
	public synchronized static WorkspaceVO clearAndRestoreToDefaultWorkspace(WorkspaceDescription wd) throws CommonBusinessException {
		WorkspaceVO wovo = createDefaultWorkspace(wd);
		if (clearWorkspace()) {
			restoreWorkspaceThreaded(wovo);

			if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_TASKLIST))
				Main.getMainController().getTaskController().getPersonalTaskController().cmdShowPersonalTasks();
			if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_TIMELIMIT_LIST))
				Main.getMainController().getTaskController().getTimelimitTaskController().cmdShowTimelimitTasks();

			Main.getMainController().getExplorerController().cmdShowPersonalSearchFilters();

			//arrangeTabsOnDefaultWorkspace(removeTabsFromWorkspace());

			for (MainFrameTabbedPane tabbedPane : MainFrame.getAllTabbedPanes()) {
				if (tabbedPane.getTabCount() > 1) {
					tabbedPane.setSelectedIndex(1);
				}
			}
			return wovo;
		} else {
			getPrefsFacade().removeWorkspace(wovo.getId());
		}
		return null;
	}
	
	/**
	 * 
	 * @return
	 */
	public synchronized static boolean closeTabs(boolean notifyOnly) {
		List<MainFrameTab> allTabs = MainFrame.getAllTabs();

		for (MainFrameTab tab : allTabs) {
			try {
				if (tab.notifyClosing()) {
					if (!notifyOnly)
						tab.notifyClosed();
				} else {
					return false;
				}
				
			} catch(CommonBusinessException e) {
				Errors.getInstance().showExceptionDialog(Main.getMainFrame(), e);
				return false;
			}
		}
		
		return true;
	}

	/**
	 *
	 * @return
	 */
	public synchronized static boolean clearWorkspace() {
		checkRestoreRunning();

		if (!closeTabs(false)) {
			return false;
		}

		for (JFrame frame : new ArrayList<JFrame>(MainFrame.getAllFrames())) {
			if (frame instanceof ExternalFrame) {
				frame.dispose();
			} else if (frame instanceof MainFrame) {
				frame.setVisible(false);
				// setVisible to false, cause of problems during repaint in synthetica:
				/* Exception in thread "AWT-EventQueue-0" java.lang.ArrayIndexOutOfBoundsException: 1
						at javax.swing.plaf.basic.BasicTabbedPaneUI.getTabBounds(BasicTabbedPaneUI.java:1517)
						at javax.swing.plaf.basic.BasicTabbedPaneUI.getTabBounds(BasicTabbedPaneUI.java:1452)
						at javax.swing.JTabbedPane.getBoundsAt(JTabbedPane.java:1268)
						at de.javasoft.plaf.synthetica.painter.TabbedPanePainter.getTabPosition(TabbedPanePainter.java:666)
						at de.javasoft.plaf.synthetica.painter.TabbedPanePainter.drawContentBorderLine4SelectedTab(TabbedPanePainter.java:763)
						at de.javasoft.plaf.synthetica.painter.TabbedPanePainter.paintTabbedPaneContentBorder(TabbedPanePainter.java:191)
				 */
			}
		}

		for (MainFrameTabbedPane tabbedPane : new ArrayList<MainFrameTabbedPane>(MainFrame.getAllTabbedPanes())) {
			MainFrame.removeTabbedPane(tabbedPane, true, false);
		}

		return true;
	}

	/**
	 *
	 * @return
	 */
	/*private synchronized static List<MainFrameTab> removeTabsFromWorkspace() {
		List<MainFrameTab> allTabs = MainFrame.getAllTabs();

		for (MainFrameTab tab : allTabs) {
			MainFrame.getTabbedPane(tab).removeTab(tab);
		}

		return allTabs;
	}*/

	/**
	 *
	 * @param allTabs
	 */
	/*private synchronized static void arrangeTabsOnDefaultWorkspace (List<MainFrameTab> allTabs) {
		MainFrameTabbedPane taskPane = null;
		for (MainFrameTabbedPane tabbedPane : MainFrame.getAllTabbedPanes()) {
			if (!tabbedPane.isHome() && !tabbedPane.isHomeTree()) {
				taskPane = tabbedPane;
			}
		}

		for (MainFrameTab tab : allTabs) {
			if (Main.getMainController().getExplorerController().isExplorerTab(tab)) {
				MainFrame.addTabToTreeHome(tab);
			} else if (Main.getMainController().getTaskController().isTaskTab(tab)) {
				if (taskPane != null) {
					taskPane.addTab(tab, false);
				} else {
					MainFrame.addTab(tab);
				}
			} else {
				MainFrame.addTab(tab);
			}
			tab.postAdd();
		}
	}*/

	private static class TabbedRestorer implements ContentRestorer {

		WorkspaceDescription.Tabbed wdTabbed;

		MainFrameTabbedPane result;

		public TabbedRestorer(WorkspaceDescription.Tabbed wdTabbed, JFrame frame) {
			this.wdTabbed = wdTabbed;
			result = Main.getMainFrame().createTabbedPane(frame);
		}

		@Override
		public Component getEmptyContent() {
			return result;
		}

		@Override
		public void restoreContent() {
			MainFrameTab toSelect = null;

			if (wdTabbed.isHome()) result.setHome();
			if (wdTabbed.isHomeTree()) result.setHomeTree();
			result.setNeverHideStartmenu(wdTabbed.isNeverHideStartmenu());
			result.setNeverHideHistory(wdTabbed.isNeverHideHistory());
			result.setNeverHideBookmark(wdTabbed.isNeverHideBookmark());
			result.setAlwaysHideStartmenu(wdTabbed.isAlwaysHideStartmenu());
			result.setAlwaysHideHistory(wdTabbed.isAlwaysHideHistory());
			result.setAlwaysHideBookmark(wdTabbed.isAlwaysHideBookmark());
			result.setShowAdministration(wdTabbed.isShowAdministration());
			result.setShowConfiguration(wdTabbed.isShowConfiguration());
			result.setShowEntity(wdTabbed.isShowEntity());
			for (String entity : wdTabbed.getPredefinedEntityOpenLocations()) {
				MainFrame.setPredefinedEntityOpenLocation(entity, result);
			}
			if (wdTabbed.getReducedStartmenus() != null) {
				result.setReducedStartmenus(wdTabbed.getReducedStartmenus());
			}
			if (wdTabbed.getReducedHistoryEntities() != null) {
				result.setReducedHistoryEntities(wdTabbed.getReducedHistoryEntities());
			}
			if (wdTabbed.getReducedBookmarkEntities() != null) {
				result.setReducedBookmarkEntities(wdTabbed.getReducedBookmarkEntities());
			}

			final int selected = wdTabbed.getSelected();

			for (int i = 0; i < wdTabbed.getTabs().size(); i++) {
				final WorkspaceDescription.Tab wdTab = wdTabbed.getTabs().get(i);
				final MainFrameTab tab = new MainFrameTab(CommonLocaleDelegate.getMessage("WorkspaceRestore.restoring","Wiederherstellen")+"...");
				tab.setNeverClose(wdTab.isNeverClose());
				tab.setFromAssigned(wdTab.isFromAssigned());
				result.addTab(tab, false);

				final Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						threadList.remove(0);

						UIUtils.runCommand(tab, new Runnable() {
							@Override
							public void run() {
								UIUtils.invokeOnDispatchThread(new Runnable() {
									@Override
									public void run() {
										if (restoreTab(wdTab, tab)) {
											tab.postAdd();
										} else {
											tab.setTabIcon(Icons.getInstance().getIconTabNotRestored());
											tab.setTitle(wdTab.getLabel());
											// TODO TABS Show nice message in content "Tab konnte nicht wiederhergestellt werden. Möglicherweise existiert der Datensatz oder die Funktion nicht länger, oder Ihnen wurde die Berechtigung entzogen."
										}
									}
								});
							}
						});

						MainFrame.continueProgress();

						if (threadList.size() > 0) {
							threadList.get(0).start();
						}
					}
				}, THREAD_NAME);
				t.setDaemon(true);
				tab.addMainFrameTabListener(new MainFrameTabAdapter() {
					@Override
					public boolean tabClosing(MainFrameTab tab) {
						if (!t.isAlive()) {
							threadList.remove(t);
							MainFrame.continueProgress();
						}
						tab.removeMainFrameTabListener(this);
						return true;
					}
				});

				if (selected == i) {
					toSelect = tab;
					threadList.add(0, t);
				} else {
					threadList.add(t);
				}
			}

			result.adjustTabs();
			if (toSelect != null && wdTabbed.getSelected() >= 0) {
				try {
					result.setSelectedComponent(toSelect);
				} catch(IllegalArgumentException e) {
					// may be not all tabs are restored
					LOG.info("restoreContent: " + e);
				}
			}
		}
	}

	private static class SplitRestorer implements ContentRestorer {

		WorkspaceDescription.Split wdSplit;
		ContentRestorer crA;
		ContentRestorer crB;

		JSplitPane result;

		public SplitRestorer(WorkspaceDescription.Split wdSplit, JFrame frame) {
			this.wdSplit = wdSplit;
			crA = createContentRestorer(wdSplit.getContentA(), frame);
			crB = createContentRestorer(wdSplit.getContentB(), frame);
			result = new JSplitPane(wdSplit.isHorizontal()? JSplitPane.HORIZONTAL_SPLIT : JSplitPane.VERTICAL_SPLIT,
				MainFrame.SPLIT_CONTINUOS_LAYOUT,
				crA.getEmptyContent(), crB.getEmptyContent());
		}

		@Override
		public Component getEmptyContent() {
			return result;
		}

		@Override
		public void restoreContent() {
			result.setOneTouchExpandable(MainFrame.SPLIT_ONE_TOUCH_EXPANDABLE);
			result.setDividerLocation(wdSplit.getPosition());

			crA.restoreContent();
			crB.restoreContent();
		}

	}

	private static interface ContentRestorer {
		public Component getEmptyContent();
		public void restoreContent();
	}

	private static PreferencesFacadeRemote getPrefsFacade() {
		return ServiceLocator.getInstance().getFacade(PreferencesFacadeRemote.class);
	}

	private synchronized static void checkRestoreRunning() {
		if (isRestoreRunning())
			throw new IllegalArgumentException("Workspace Restore is running");
	}

	public synchronized static boolean isRestoreRunning() {
		return !threadList.isEmpty();
	}

}
