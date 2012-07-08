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
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.nuclos.client.common.prefs.PreferencesMigration;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.main.GenericAction;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.ExternalFrame;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameSplitPane;
import org.nuclos.client.main.mainframe.MainFrameSpringComponent;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.main.mainframe.MainFrameTabbedPane;
import org.nuclos.client.ui.CommonJFrame;
import org.nuclos.client.ui.MainFrameTabAdapter;
import org.nuclos.client.ui.ResultListener;
import org.nuclos.client.ui.ResultListenerX;
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
import org.nuclos.common.collection.Predicate;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.common.ejb3.PreferencesFacadeRemote;

public class RestoreUtils {

	private static final Logger LOG = Logger.getLogger(RestoreUtils.class);

	private static final String PREFS_NODE_LAST_SETTINGS = "lastSettings";
	private static final String NORMAL_BOUNDS = "normalBounds";
	private static final String EXTENDED_STATE = "extendedState";

	private static final String THREAD_NAME = "RestoreUtils.workspaceRestore.";
	private static final List<Thread> threadList = new ArrayList<Thread>();
	
	private static RestoreUtils INSTANCE;
	
	//

	private static List<GenericAction> cachedActions;
	
	// Spring injection
	
	private PreferencesFacadeRemote preferencesFacadeRemote;
	
	private PreferencesMigration preferencesMigration;
	
	private MainFrameSpringComponent mainFrameSpringComponent;
	
	// end of Spring injection

	RestoreUtils() {
		INSTANCE = this;
	}
	
	public final void setPreferencesFacadeRemote(PreferencesFacadeRemote preferencesFacadeRemote) {
		this.preferencesFacadeRemote = preferencesFacadeRemote;
	}
	
	public final void setMainFrameSpringComponent(MainFrameSpringComponent mainFrameSpringComponent) {
		this.mainFrameSpringComponent = mainFrameSpringComponent;
	}
	
	public final void setPreferencesMigration(PreferencesMigration preferencesMigration) {
		this.preferencesMigration = preferencesMigration;
	}
	
	public MainFrame getMainFrame() {
		return mainFrameSpringComponent.getMainFrame();
	}
	
	public WorkspaceVO getWorkspace() {
		final WorkspaceVO result = mainFrameSpringComponent.getMainFrame().getWorkspace();
		if (result == null) {
			throw new NullPointerException();
		}
		return result;
	}

	private synchronized boolean restoreTab(WorkspaceDescription.Tab wdTab, MainFrameTab tab, boolean onDemand) {
		try {
			final ClassLoader cl = Thread.currentThread().getContextClassLoader();
			final ITabRestoreController restoreController = (ITabRestoreController) cl.loadClass(
					wdTab.getRestoreController()).getConstructor().newInstance();
			if (onDemand) {
				tab.setTabRestoreController(restoreController);
				tab.setTabRestorePreferencesXML(wdTab.getPreferencesXML());
			}
			else {
				restoreController.restoreFromPreferences(wdTab.getPreferencesXML(), tab);
			}
		}
		catch (RuntimeException e) {
			LOG.warn("TabRestoreController failed, disposing " + tab + ": " + e, e);
			tab.dispose();
		}
		catch (Exception e) {
			LOG.warn("TabRestoreController could not be created or restored", e);
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
	private synchronized ContentRestorer createContentRestorer(WorkspaceDescription.MutableContent content, JFrame frame) {
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
	private synchronized void restoreFrame(final WorkspaceDescription wd, final WorkspaceDescription.Frame wdFrame) {
		WorkspaceFrame wsFrame;
		if (wdFrame.isMainFrame()) {
			wsFrame = Main.getInstance().getMainFrame();
		} else {
			wsFrame = MainFrame.createWorkspaceFrame();
		}

		final CommonJFrame frame = wsFrame.getFrame();

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = ge.getScreenDevices();

		final Rectangle recNormalBounds;
		final int iExtendedState;
		if (wdFrame.isMainFrame() && wd.isUseLastFrameSettings()) {
			recNormalBounds = PreferencesUtils.getRectangle(Main.getInstance().getMainController().getMainFramePreferences().node(PREFS_NODE_LAST_SETTINGS), NORMAL_BOUNDS, wdFrame.getNormalBounds().width, wdFrame.getNormalBounds().height);
			iExtendedState = Main.getInstance().getMainController().getMainFramePreferences().node(PREFS_NODE_LAST_SETTINGS).getInt(EXTENDED_STATE, wdFrame.getExtendedState());
		} else {
			recNormalBounds = wdFrame.getNormalBounds();
			iExtendedState = wdFrame.getExtendedState();
		}

		boolean restoreAtStoredPosition = false;
		LOG.info("stored frame bounds: " + recNormalBounds);
		for (GraphicsDevice gd : devices) {
			if (restoreAtStoredPosition) break;

			GraphicsConfiguration[] gc = gd.getConfigurations();
			for (int i=0; i < gc.length && !restoreAtStoredPosition; i++) {
				Rectangle deviceBounds = gc[i].getBounds();
				LOG.debug("device bounds: " + deviceBounds);
				if (deviceBounds.contains(recNormalBounds)) {
					restoreAtStoredPosition = true;
				}
			}
		}
		
		frame.setNormalBounds(recNormalBounds);
		frame.setBounds(recNormalBounds);
		frame.setExtendedState(iExtendedState);

		if (!restoreAtStoredPosition) {
			recNormalBounds.x = 40;
			recNormalBounds.y = 40;
			SwingUtilities.invokeLater(new Runnable() { // restore bounds later. This is needed for fixed splitpanes.
				@Override
				public void run() {
					frame.setNormalBounds(recNormalBounds);
					frame.setBounds(recNormalBounds);
					frame.setExtendedState(iExtendedState);
				}
			});
		} 

		if (wdFrame.isMainFrame()) {
			Main.getInstance().getMainFrame().repositionSwitchingWorkspace();
			JPanel contentpane = (JPanel) Main.getInstance().getMainFrame().getContentPane();
			contentpane.revalidate();
			contentpane.paintImmediately(0,0,contentpane.getSize().width,contentpane.getSize().height);
		}

		ContentRestorer cr = createContentRestorer(wdFrame.getContent(), frame);
		wsFrame.setFrameContent(cr.getEmptyContent());

		cr.restoreContent();

		MainFrame.updateTabbedPaneActions(frame);

		if (wdFrame.isMainFrame()) {
			Main.getInstance().getMainFrame().showSwitchingWorkspace(false);
		} else {
			frame.setVisible(true);
		}
	}

	/**
	 *
	 * @param lastWorkspaceIdFromPreferences
	 * @param lastWorkspaceFromPreferences
	 * @param lastAlwaysOpenWorkspaceIdFromPreferences
	 * @param lastAlwaysOpenWorkspaceFromPreferences
	 * @throws CommonBusinessException
	 */
	public void restoreWorkspaceThreaded(Long lastWorkspaceIdFromPreferences, String lastWorkspaceFromPreferences,
			Long lastAlwaysOpenWorkspaceIdFromPreferences, String lastAlwaysOpenWorkspaceFromPreferences) throws CommonBusinessException {

		WorkspaceVO wovoToRestore = null;
		final MainFrame mainFrame = mainFrameSpringComponent.getMainFrame();

		List<WorkspaceVO> alwaysOpenWorkspaces = CollectionUtils.select(
				mainFrame.getWorkspaceHeaders(), new Predicate<WorkspaceVO>() {
			@Override
			public boolean evaluate(WorkspaceVO t) {
				return t.getWoDesc().isAlwaysOpenAtLogin();
			}
		});

		if (alwaysOpenWorkspaces.size() > 0) {
			if (alwaysOpenWorkspaces.size() == 1) {
				wovoToRestore = alwaysOpenWorkspaces.get(0);
			} else {
				if (lastAlwaysOpenWorkspaceIdFromPreferences != null && lastAlwaysOpenWorkspaceIdFromPreferences.longValue() != 0l &&
						lastAlwaysOpenWorkspaceFromPreferences != null) {
					for (WorkspaceVO wovo : alwaysOpenWorkspaces) {
						if (lastAlwaysOpenWorkspaceIdFromPreferences.equals(wovo.getId())) {
							wovoToRestore = wovo;
							break; // priority of id is higher
						}
						// if id not found try to search for name
						if (lastAlwaysOpenWorkspaceFromPreferences.equals(wovo.getName())) {
							wovoToRestore = wovo;
						}
					}
				} else {
					wovoToRestore = alwaysOpenWorkspaces.get(0); // select first always open workspace
				}
			}
		} else {
			for (WorkspaceVO wovo : mainFrame.getWorkspaceHeaders()) {
				if (lastWorkspaceIdFromPreferences.equals(wovo.getId())) {
					wovoToRestore = wovo;
					break; // priority of id is higher
				}
				// if id not found try to search for name
				if (lastWorkspaceFromPreferences.equals(wovo.getName())) {
					wovoToRestore = wovo;
				}
			}
		}


		if (wovoToRestore == null && !mainFrame.getWorkspaceHeaders().isEmpty()) {
			wovoToRestore = mainFrame.getWorkspaceHeaders().get(0);
		}

		if (wovoToRestore == null) {
			wovoToRestore = createDefaultWorkspace(createDefaultWorkspace());
			mainFrame.refreshWorkspacesHeaders();
		}
		restoreWorkspaceThreaded(wovoToRestore);
	}

	/**
	 *
	 * @param name
	 * @param restoreToDefault
	 * @throws CommonBusinessException
	 */
	private synchronized void restoreWorkspaceThreaded(WorkspaceVO wovo) throws CommonBusinessException {
		checkRestoreRunning();
		
		final MainFrame mainFrame = mainFrameSpringComponent.getMainFrame();
		mainFrame.setWorkspaceManagementEnabled(false);
		cachedActions = Main.getInstance().getMainController().getGenericActions();

		//load from db. wovo contains header only
		WorkspaceDescription wd;
		if (wovo.getWoDesc().getFrames().isEmpty()) {
			try {
				wd = preferencesFacadeRemote.getWorkspace(wovo.getId()).getWoDesc();
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
		if (wovo.getWoDesc() != wd) {
			wovo.getWoDesc().removeAllEntityPreferences();
			wovo.getWoDesc().addAllEntityPreferences(wd.getEntityPreferences());
			wovo.getWoDesc().removeAllTasklistPreferences();
			wovo.getWoDesc().addAllTasklistPreferences(wd.getTasklistPreferences());
			wovo.getWoDesc().removeAllParameters();
			wovo.getWoDesc().setAllParameters(wd.getParameters());
		}

		mainFrame.setWorkspace(wovo);
		if (wd.isAlwaysOpenAtLogin()) {
			MainFrame.setLastAlwaysOpenWorkspace(wovo.getName());
			MainFrame.setLastAlwaysOpenWorkspaceId(wovo.getId());
		}
		preferencesMigration.migrateEntityAndSubFormColumnPreferences();

		threadList.clear();

		MainFrame.resetExternalFrameNumber();
		for (WorkspaceDescription.Frame wdFrame : CollectionUtils.sorted(wd.getFrames(), new Comparator<WorkspaceDescription.Frame>() {
			@Override
			public int compare(Frame o1, Frame o2) {
				return LangUtils.compare(o1.getNumber(), o2.getNumber());
			}
		})) {
			restoreFrame(wd, wdFrame);
		}

		// refresh menus for window list
		Main.getInstance().getMainController().refreshMenus();

		final Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Main.getInstance().getMainFrame().hideProgress();
					mainFrame.setWorkspaceManagementEnabled(true);
					MainFrame.setActiveTabNavigation(MainFrame.getHomePane());
					threadList.clear();
				}
				catch (Exception e) {
					LOG.error("restoreWorkspaceThreaded failed: " + e, e);
				}
			}
		}, THREAD_NAME + "restoreWorkspaceThreaded");
		t.setDaemon(true);
		threadList.add(t);

		Main.getInstance().getMainFrame().showProgress(threadList.size());
		if (threadList.size() > 0) {
			threadList.get(0).start();
		}
	}

	/**
	 *
	 * @return
	 * @throws CommonBusinessException
	 */
	public WorkspaceDescription createDefaultWorkspace() throws CommonBusinessException {
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
	public WorkspaceVO createDefaultWorkspace(WorkspaceDescription wdOrigin) throws CommonBusinessException {
		WorkspaceVO wovo = new WorkspaceVO(WorkspaceDescriptionDefaultsFactory.createOldMdiStyle());
		wovo.importHeader(wdOrigin);
		wovo = preferencesFacadeRemote.storeWorkspace(wovo);
		return wovo;
	}

	/**
	 *
	 * @param wdTabbed
	 * @param tab
	 * @return
	 */
	private synchronized boolean storeTab(WorkspaceDescription.Tabbed wdTabbed, MainFrameTab tab) {
		if (tab.hasTabStoreController() || tab.hasTabRestoreController()) {
			final WorkspaceDescription.Tab wdTab = new WorkspaceDescription.Tab();
			wdTab.setLabel(tab.getTitle());
			wdTab.setIconResolver(tab.getTabIconResolver());
			wdTab.setIcon(tab.getTabIconName());
			wdTab.setNeverClose(tab.isNeverClose());
			wdTab.setFromAssigned(tab.isFromAssigned());
			wdTab.setPreferencesXML(tab.getTabRestorePreferencesXML());
			wdTab.setRestoreController(tab.getTabRestoreControllerClassName());
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
	private synchronized void storeTabbedPane(MutableContent content, MainFrameTabbedPane tabbedPane) {
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
		wdTabbed.setDesktop(tabbedPane.getDesktop());
		wdTabbed.setDesktopActive(tabbedPane.isDesktopActive());
		wdTabbed.setHideStartTab(!tabbedPane.isStartTabVisible());
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
	private synchronized void storeSplitPane(MutableContent content, MainFrameSplitPane splitPane) {
		final WorkspaceDescription.Split wdSplit = new WorkspaceDescription.Split();
		content.setContent(wdSplit);
		wdSplit.setHorizontal(splitPane.getOrientation()==JSplitPane.HORIZONTAL_SPLIT);
		wdSplit.setPosition(splitPane.getDividerLocation());
		wdSplit.setFixedState(splitPane.getFixedState());
		storeContent(wdSplit.getContentA(), splitPane.getLeftComponent());
		storeContent(wdSplit.getContentB(), splitPane.getRightComponent());
	}

	/**
	 *
	 * @param content
	 * @param comp
	 */
	private synchronized void storeContent(MutableContent content, Component comp) {
		if (comp instanceof MainFrameSplitPane) {
			storeSplitPane(content, (MainFrameSplitPane) comp);
		} else if (comp instanceof MainFrameTabbedPane.ComponentPanel) {
			storeTabbedPane(content, ((MainFrameTabbedPane.ComponentPanel) comp).getMainFrameTabbedPane());
		} else {
			throw new IllegalArgumentException(comp.toString());
		}
	}

	/**
	 *
	 * @param wd
	 * @param frame
	 */
	private synchronized void storeFrame(WorkspaceDescription wd, CommonJFrame frame) {
		if (frame instanceof WorkspaceFrame) {

			final WorkspaceDescription.Frame wdFrame = new WorkspaceDescription.Frame();
			wdFrame.setExtendedState(frame.getExtendedState());
			wdFrame.setNormalBounds(frame.getNormalBounds());
			wdFrame.setMainFrame(Main.getInstance().getMainFrame()==frame);
			wdFrame.setNumber(((WorkspaceFrame) frame).getNumber());
			storeContent(wdFrame.getContent(), ((WorkspaceFrame) frame).getFrameContent());

			wd.addFrame(wdFrame);

			if (Main.getInstance().getMainFrame()==frame) {
				Main.getInstance().getMainController().getMainFramePreferences().node(PREFS_NODE_LAST_SETTINGS).putInt(EXTENDED_STATE, frame.getExtendedState());
				PreferencesUtils.putRectangle(Main.getInstance().getMainController().getMainFramePreferences().node(PREFS_NODE_LAST_SETTINGS), NORMAL_BOUNDS, frame.getNormalBounds());
			}
		}
	}

	/**
	 *
	 * @param name
	 * @throws CommonBusinessException
	 */
	public synchronized WorkspaceVO storeWorkspace(WorkspaceVO wovo) throws CommonBusinessException {
		if (wovo == null) {
			return null;
		}

		if (isRestoreRunning()) {
			// do not store workspace if restore is running...
			return null;
		}

		WorkspaceDescription wd = wovo.getWoDesc();
		wd.getFrames().clear();

		Main.getInstance().getMainFrame().restoreAllTabbedPaneContainingArea();
		for (CommonJFrame frame : MainFrame.getOrderedFrames()) {
			storeFrame(wd, frame);
		}

		return preferencesFacadeRemote.storeWorkspace(wovo);
	}

	/**
	 *
	 * @param name
	 */
	public synchronized void clearAndRestoreWorkspace(final WorkspaceVO wovo, final ResultListenerX<Boolean, CommonBusinessException> rl) {
		clearWorkspace(new ResultListener<Boolean>() {
			@Override
			public void done(Boolean result) {
				if (Boolean.TRUE.equals(result)) {
					try {
						restoreWorkspaceThreaded(wovo);
						rl.done(true, null);
					} catch (CommonBusinessException x) {
						rl.done(null, x);
					}
				} else {
					rl.done(false, null);
				}
			}
		});
	}

	/**
	 *
	 * @param name
	 */
	public synchronized void clearAndRestoreToDefaultWorkspace(final ResultListenerX<WorkspaceVO, CommonBusinessException> rl) {
		try {
			clearAndRestoreToDefaultWorkspace(createDefaultWorkspace(), rl);
		} catch (CommonBusinessException e) {
			rl.done(null, e);
		}
	}

	/**
	 *
	 * @param wd
	 */
	public synchronized void clearAndRestoreToDefaultWorkspace(WorkspaceDescription wd, final ResultListenerX<WorkspaceVO, CommonBusinessException> rl) {
		final WorkspaceVO wovo;
		try {
			wovo = createDefaultWorkspace(wd);
		} catch (CommonBusinessException e) {
			rl.done(null, e);
			return;
		}
		
		clearWorkspace(new ResultListener<Boolean>() {
			@Override
			public void done(Boolean result) {
				if (Boolean.TRUE.equals(result)) {
					try {
						restoreWorkspaceThreaded(wovo);
					} catch (CommonBusinessException e) {
						rl.done(null, e);
						return;
					}

					if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_TASKLIST))
						Main.getInstance().getMainController().getTaskController().getPersonalTaskController().cmdShowPersonalTasks();
					if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_TIMELIMIT_LIST))
						Main.getInstance().getMainController().getTaskController().getTimelimitTaskController().cmdShowTimelimitTasks();

					Main.getInstance().getMainController().getExplorerController().cmdShowPersonalSearchFilters();

					//arrangeTabsOnDefaultWorkspace(removeTabsFromWorkspace());

					for (MainFrameTabbedPane tabbedPane : MainFrame.getAllTabbedPanes()) {
						if (tabbedPane.getTabCount() > 1) {
							tabbedPane.setSelectedIndex(1);
						}
					}
					
					rl.done(wovo, null);
				} else {
					preferencesFacadeRemote.removeWorkspace(wovo.getId());
					rl.done(null, null);
				}
			}
		});
	}

	/**
	 *
	 * @return
	 */
	public synchronized void closeTabs(final boolean notifyOnly, final ResultListener<Boolean> rl) {
		final List<MainFrameTab> allTabs = MainFrame.getAllTabs();
		if (allTabs.isEmpty()) {
			rl.done(true);
			return;
		}
		
		final Iterator<MainFrameTab> iter = allTabs.iterator();
		
		class Helper {
			public void next() {
				if (iter.hasNext()) {
					final MainFrameTab tab = iter.next();
					tab.notifyClosing(new ResultListener<Boolean>() {
						@Override
						public void done(Boolean result) {
							if (Boolean.TRUE.equals(result)) {
								if (!notifyOnly) {
									tab.notifyClosed();
								}
								new Helper().next();
							} else {
								rl.done(false);
							}
						}
					});
				} else {
					rl.done(true);
				}		
			}
		}
		
		new Helper().next();
	}

	/**
	 *
	 * @return
	 */
	public synchronized void clearWorkspace(final ResultListener<Boolean> rl) {
		checkRestoreRunning();

		closeTabs(false, new ResultListener<Boolean>() {
			@Override
			public void done(Boolean result) {
				if (Boolean.FALSE.equals(result)) {
					rl.done(false);
				} else {
					for (JFrame frame : new ArrayList<JFrame>(MainFrame.getAllFrames())) {
						if (frame instanceof ExternalFrame) {
							frame.dispose();
						} else if (frame instanceof MainFrame) {
							Main.getInstance().getMainFrame().showSwitchingWorkspace(true);
							((MainFrame) frame).clearFrame();
						}
					}

					for (MainFrameTabbedPane tabbedPane : new ArrayList<MainFrameTabbedPane>(MainFrame.getAllTabbedPanes())) {
						Main.getInstance().getMainFrame().removeTabbedPane(tabbedPane, true, false);
					}
					
					rl.done(true);
				}
			}
		});
	}

	private class TabbedRestorer implements ContentRestorer {

		WorkspaceDescription.Tabbed wdTabbed;

		MainFrameTabbedPane result;

		public TabbedRestorer(WorkspaceDescription.Tabbed wdTabbed, JFrame frame) {
			this.wdTabbed = wdTabbed;
			result = Main.getInstance().getMainFrame().createTabbedPane(frame);
		}

		@Override
		public Component getEmptyContent() {
			return result.getComponentPanel();
		}

		@Override
		public void restoreContent() {
			result.startInitiating();
			
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
			result.setDesktop(wdTabbed.getDesktop(), cachedActions);
			result.setDesktopActive(wdTabbed.isDesktopActive());
			result.setStartTabVisible(!wdTabbed.isHideStartTab());

			final int selected = wdTabbed.getSelected();

			for (int i = 0; i < wdTabbed.getTabs().size(); i++) {
				final WorkspaceDescription.Tab wdTab = wdTabbed.getTabs().get(i);
				final MainFrameTab tab = new MainFrameTab(wdTab.getLabel());
				tab.setTabIcon(wdTab.getIconResolver(), wdTab.getIcon());
				tab.setNeverClose(wdTab.isNeverClose());
				tab.setFromAssigned(wdTab.isFromAssigned());
				result.addTab(tab, false);

				if (selected == i) {
					toSelect = tab;
					tab.setNotifyRestore(true);
					
					final Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								threadList.remove(0);
	
								UIUtils.runCommand(tab, new Runnable() {
									@Override
									public void run() {
										UIUtils.invokeOnDispatchThread(new Runnable() {
											@Override
											public void run() {
												try {
													if (restoreTab(wdTab, tab, false)) {
														tab.postAdd();
													} else {
														tab.setTabIconFromSystem("getIconTabNotRestored");
														tab.setTitle(wdTab.getLabel());
														// TODO TABS Show nice message in content "Tab konnte nicht wiederhergestellt werden. Möglicherweise existiert der Datensatz oder die Funktion nicht länger, oder Ihnen wurde die Berechtigung entzogen."
													}
												}
												catch (Exception e) {
													LOG.error("restoreContent failed: " + e, e);
												}
											}
										});
									}
								});
	
								Main.getInstance().getMainFrame().continueProgress();
	
								if (threadList.size() > 0) {
									threadList.get(0).start();
								}
							}
							catch (Exception e) {
								LOG.error("restoreContent failed: " + e, e);
							}
						}
					}, THREAD_NAME + "restoreContent");
					t.setDaemon(true);
					tab.addMainFrameTabListener(new MainFrameTabAdapter() {
						@Override
						public void tabClosing(MainFrameTab tab, ResultListener<Boolean> rl) {
							if (!t.isAlive()) {
								threadList.remove(t);
								Main.getInstance().getMainFrame().continueProgress();
							}
							tab.removeMainFrameTabListener(this);
							rl.done(true);
						}
					});
	
//					if (selected == i) {
//						toSelect = tab;
						threadList.add(0, t);
//					} else {
//						threadList.add(t);
//					}

				} else {
					// restores on demand...
					restoreTab(wdTab, tab, true);
					Main.getInstance().getMainFrame().continueProgress();
				}
				
			}
			
			final MainFrameTab selectLater = toSelect;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						result.finishInitiating();
						if (selectLater != null && wdTabbed.getSelected() >= 0) {
							result.setSelectedComponent(selectLater);
						}
					} catch(IllegalArgumentException e) {
						// may be not all tabs are restoredaubspl
						LOG.info("restoreContent: " + e);
					}
				}
			});
		}
	}

	private class SplitRestorer implements ContentRestorer {

		WorkspaceDescription.Split wdSplit;
		ContentRestorer crA;
		ContentRestorer crB;

		MainFrameSplitPane result;

		public SplitRestorer(WorkspaceDescription.Split wdSplit, JFrame frame) {
			this.wdSplit = wdSplit;
			crA = createContentRestorer(wdSplit.getContentA(), frame);
			crB = createContentRestorer(wdSplit.getContentB(), frame);
			result = new MainFrameSplitPane(wdSplit.isHorizontal()? JSplitPane.HORIZONTAL_SPLIT : JSplitPane.VERTICAL_SPLIT,
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
			result.setFixedState(wdSplit.getFixedState());
			
			crA.restoreContent();
			crB.restoreContent();
		}

	}

	private static interface ContentRestorer {
		public Component getEmptyContent();
		public void restoreContent();
	}

	private synchronized void checkRestoreRunning() {
		if (isRestoreRunning())
			throw new IllegalArgumentException("Workspace Restore is running");
	}

	public synchronized boolean isRestoreRunning() {
		return !threadList.isEmpty();
	}

}
