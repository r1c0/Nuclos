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
package org.nuclos.client.main.mainframe;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.IllegalComponentStateException;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.nuclos.client.NuclosIcons;
import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.common.ComponentNameSetter;
import org.nuclos.client.common.KeyBindingProvider;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.Utils;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.image.ImageScaler;
import org.nuclos.client.livesearch.LiveSearchController;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.MainController;
import org.nuclos.client.main.MenuGenerator;
import org.nuclos.client.main.NuclosMessagePanel;
import org.nuclos.client.main.NuclosNotificationDialog;
import org.nuclos.client.main.mainframe.workspace.WorkspaceChooserController;
import org.nuclos.client.main.mainframe.workspace.WorkspaceFrame;
import org.nuclos.client.report.reportrunner.BackgroundProcessStatusController;
import org.nuclos.client.resource.NuclosResourceCache;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.synthetica.NuclosThemeSettings;
import org.nuclos.client.ui.Bubble;
import org.nuclos.client.ui.CommonJFrame;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.ValidationLayerFactory;
import org.nuclos.client.ui.gc.IReferenceHolder;
import org.nuclos.client.ui.gc.ListenerUtil;
import org.nuclos.client.ui.util.TableLayoutBuilder;
import org.nuclos.common.Actions;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.WorkspaceDescription;
import org.nuclos.common.WorkspaceVO;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.ComparatorUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.collection.multimap.MultiListHashMap;
import org.nuclos.common.collection.multimap.MultiListMap;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.PreferencesException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The "main frame" of the application. Contains as little control logic as possible.
 * Control is delegated to separate controllers.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
@org.springframework.stereotype.Component
public class MainFrame extends CommonJFrame implements WorkspaceFrame, ComponentNameSetter {

	private static final Logger LOG = Logger.getLogger(MainFrame.class);

	private static final String PREFS_NODE_BOOKMARK = "bookmark";
	private static final String PREFS_NODE_HISTORY = "history";
	private static final String PREFS_NODE_HISTORY_SIZE_INDEX = "historysizeindex";
	private static final String PREFS_NODE_SPLITTING_DEACTIVATED = "splittingdeactivated";
	private static final String PREFS_NODE_DEFAULT_WORKSPACE = "defaultworkspace";
	private static final String PREFS_NODE_LAST_WORKSPACE = "lastworkspace";
	private static final String PREFS_NODE_LAST_WORKSPACE_ID = "lastworkspaceid";
	private static final String PREFS_NODE_LAST_ALWAYS_OPEN_WORKSPACE = "lastalwaysopenworkspace";
	private static final String PREFS_NODE_LAST_ALWAYS_OPEN_WORKSPACE_ID = "lastalwaysopenworkspaceid";
	private static final String PREFS_NODE_WORKSPACE_ORDER = "workspaceorder";
	private static final String PREFS_NODE_WORKSPACE_ORDER_IDS = "workspaceorderids";

	public final static boolean SPLIT_CONTINUOS_LAYOUT = false;
	public final static boolean SPLIT_ONE_TOUCH_EXPANDABLE = true;

	public final static int TAB_CONTENT_ICON_MAX = 16;
	public final static int LINK_ICON_MAX = 16;

	public final static int[] HISTORY_SIZES = new int[] {
		50, 100, 200
	};

	private final static int PROGRESSBAR_LAYER = JLayeredPane.MODAL_LAYER;
	private final static int PROGRESSBAR_WIDTH = 200;
	private final static JProgressBar progressBar = new JProgressBar();
	private final static JPanel componentMacPanel = new JPanel();
	private static final JPanel pnlDesktop = new JPanel();

	private static MainFrameTabbedPane homeTabbedPane;
	private static MainFrameTabbedPane homeTreeTabbedPane;
	private static MainFrameTabbedPane activeTabNavigation;

	private static int nextExternalFrameNumber = 1;
	private static final List<CommonJFrame> frameZOrder = new ArrayList<CommonJFrame>();
	private static final MultiListMap<JFrame, MainFrameTabbedPane> frameContent = new MultiListHashMap<JFrame, MainFrameTabbedPane>();
	private static final Map<MainFrameTabbedPane, MaximizedTabbedPaneParameter> maximizedTabbedPanes = new HashMap<MainFrameTabbedPane, MainFrame.MaximizedTabbedPaneParameter>();

	private static final MultiListMap<MainFrameTabbedPane, String> predefinedEntityOpenLocation = new MultiListHashMap<MainFrameTabbedPane, String>();

	private static final Map<ImageIcon, Map<Integer, ImageIcon>> resizedIcons = new HashMap<ImageIcon, Map<Integer, ImageIcon>>();

	/**
	 * String: entity
	 * EntityBookmark
	 */
	private static final MultiListMap<String, EntityBookmark> history = new MultiListHashMap<String, EntityBookmark>();
	private static final MultiListMap<String, EntityBookmark> bookmark = new MultiListHashMap<String, EntityBookmark>();
	private static int selectedHistorySize = 0;

	private static boolean splittingEnabled = true;
	private static boolean splittingDeactivated = false;

	private static String defaultWorkspace;
	private static String lastWorkspace;
	private static Long lastWorkspaceId;
	private static String lastAlwaysOpenWorkspace;
	private static Long lastAlwaysOpenWorkspaceId;
	
	private static JWindow winSwitchingWorkspace;

	//
	
	private final AbstractAction actDeactivateSplitting = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			setSplittingDeactivated(!isSplittingDeactivated());
		}
	};

	private JCheckBoxMenuItem miDeactivateSplitting;
	
	private JMenu menuWindow;

	private NuclosMessagePanel msgPanel;
	
	private LiveSearchController liveSearchController;
	private WorkspaceChooserController workspaceChooserController;
	
	private SpringLocaleDelegate localeDelegate;
	
	private ClientParameterProvider clientParameterProvider;
	
	private NuclosIcons nuclosIcons;
	
	private ResourceCache resourceCache;
	
	/**
	 * creates the main frame. Note that here we don't follow the general rule that the view shouldn't
	 * contain a reference to its controller. We hide that fact in this package though.
	 */
	MainFrame() {
	}	// ctor
	
	@Autowired
	void setResourceCache(ResourceCache resourceCache) {
		this.resourceCache = resourceCache;
	}
	
	@Autowired
	void setSpringLocaleDelegate(SpringLocaleDelegate cld) {
		this.localeDelegate = cld;
	}
	
	@Autowired
	void setClientParameterProvider(ClientParameterProvider clientParameterProvider) {
		this.clientParameterProvider = clientParameterProvider;
	}
	
	@Autowired
	void setNuclosIcons(NuclosIcons nuclosIcons) {
		this.nuclosIcons = nuclosIcons;
	}
	
	/*
	@Autowired
	void prepare(@Value("#{mainController.userName}") String sUserName, 
			@Value("#{mainController.nuclosServerName}") String sNucleusServerName) 
	{
		init(sUserName, sNucleusServerName);
	}
	 */
	
	public final void postConstruct() 
	{
		ValidationLayerFactory.setCurrentPainter(clientParameterProvider.getValue(
				ParameterProvider.KEY_CLIENT_VALIDATION_LAYER_PAINTER_NAME));
		msgPanel = new NuclosMessagePanel();
		
		// init(sUserName, sNucleusServerName);

		setName("mainframe");
		addWindowFocusListener(new ZOrderUpdater(MainFrame.this));
		Utils.setComponentNames(this);
		setIconImage(nuclosIcons.getFrameIcon().getImage());

		// liveSearchController = new LiveSearchController(this);
		// workspaceChooserController = new WorkspaceChooserController();
		setupLiveSearchKey(this);
	}
	
	@Autowired
	void setWorkspaceChooserController(WorkspaceChooserController wcc) {
		this.workspaceChooserController = wcc;
	}
	
	@Autowired
	void setLiveSearchController(LiveSearchController lsc) {
		this.liveSearchController = lsc;
	}

	private void setProgressBounds() {
		progressBar.setBounds((getBounds().width - PROGRESSBAR_WIDTH) / 2, 0, PROGRESSBAR_WIDTH, 10);
	}

	public void showProgress(final int maxValue) {
		UIUtils.invokeOnDispatchThread(new Runnable() {
			@Override
			public void run() {
				progressBar.setMinimum(0);
				progressBar.setMaximum(maxValue);
				progressBar.setValue(0);
				setProgressBounds();
				getLayeredPane().add(progressBar, PROGRESSBAR_LAYER);
			}
		});
	}

	public void hideProgress() {
		UIUtils.invokeOnDispatchThread(new Runnable() {
			@Override
			public void run() {
				getLayeredPane().remove(getLayeredPane().getIndexOf(progressBar));
				getLayeredPane().repaint();
				progressBar.setValue(0);
			}
		});
	}

	public void continueProgress() {
		UIUtils.invokeOnDispatchThread(new Runnable() {
			@Override
			public void run() {
				progressBar.setValue(progressBar.getValue()+1);
				setProgressBounds();
			}
		});
	}

	public NuclosMessagePanel getMessagePanel() {
		return msgPanel;
	}

	public void setTitle() {
		String template = "{0} {1}";
		String application = ApplicationProperties.getInstance().getCurrentVersion().getAppName();
		String version = ApplicationProperties.getInstance().getCurrentVersion().getVersionNumber();
		String instance = ClientParameterProvider.getInstance().getValue(ParameterProvider.KEY_NUCLOS_INSTANCE_NAME);
		if (!StringUtils.isNullOrEmpty(instance)) {
			template = "{0} {1} ({2})";
		}
		super.setTitle(MessageFormat.format(template, application, version, instance));
	}

	public final void init(String sUserName, String sNucleusServerName) {
		this.setTitle();
		setBackground(NuclosThemeSettings.BACKGROUND_ROOTPANE);
		JPanel contentpane = (JPanel) getContentPane();
		contentpane.setLayout(new BorderLayout());
		contentpane.add(pnlDesktop, BorderLayout.CENTER);
		contentpane.setBackground(NuclosThemeSettings.BACKGROUND_ROOTPANE);
		pnlDesktop.setOpaque(false);
		pnlDesktop.setLayout(new BorderLayout());
	}

	public JPanel newEmbeddedToolBar() {
		final JPanel result = new JPanel(new BorderLayout());

		// add a small border at the top otherwise it looks weird esp. in Classic Windows L&F:
		result.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

		result.setMaximumSize(result.getPreferredSize());

		return result;
	}

	Dimension getViewportSize() {
		return pnlDesktop.getSize();
	}

	/**
	 *
	 * @return
	 */
	public static MainFrameTabbedPane getHomePane() {
		return homeTabbedPane;
	}

	/**
	 *
	 * @return
	 */
	public static MainFrameTabbedPane getHomeTreePane() {
		return homeTreeTabbedPane;
	}

	/**
	 *
	 * @param tabbedPane
	 * @return
	 */
	private static boolean isTabbedPaneVisible(MainFrameTabbedPane tabbedPane) {
		if (!maximizedTabbedPanes.containsKey(tabbedPane)) {
			for (JFrame frame : frameContent.keySet()) {
				if (frameContent.getValues(frame).contains(tabbedPane)) {
					// check if other tabbedPane is maximized
					for (MainFrameTabbedPane other : frameContent.getValues(frame)) {
						if (other != tabbedPane) {
							if (maximizedTabbedPanes.containsKey(other)) {
								return false;
							}
						}
					}
				}
			}
		}

		try {
			tabbedPane.getComponentPanel().getLocationOnScreen();
			return true;
		} catch (IllegalComponentStateException e) {
			LOG.info("isTabbedPaneVisible: " + e);
			return false;
		}
	}

	private static MainFrameTabbedPane getMaximizedTabbedPaneIfAny(MainFrameTabbedPane possiblyNotVisibleTabbedPane) {
		for (JFrame frame : frameContent.keySet()) {
			if (frameContent.getValues(frame).contains(possiblyNotVisibleTabbedPane)) {
				for (MainFrameTabbedPane tabbedPane : frameContent.getValues(frame)) {
					if (maximizedTabbedPanes.containsKey(tabbedPane)) {
						return tabbedPane;
					}
				}
			}
		}

		// no maximized found? must be shown...
		return possiblyNotVisibleTabbedPane;
	}

	/**
	 *
	 * @return
	 */
	public static MainFrameTabbedPane getTreeOpenLocation() {
		return homeTreeTabbedPane;
	}

	/**
	 *
	 * @param tabbedPane
	 * @return
	 */
	public static List<String> getPredefinedEntityOpenLocations(MainFrameTabbedPane tabbedPane) {
		return predefinedEntityOpenLocation.getValues(tabbedPane);
	}

	/**
	 *
	 * @return
	 */
	public static Set<String> getAllPredefinedEntityOpenLocations() {
		return predefinedEntityOpenLocation.getAllValues();
	}

	/**
	 *
	 * @param entity
	 * @return
	 */
	public static boolean isPredefinedEntityOpenLocationSet(String entity) {
		for (MainFrameTabbedPane tabbedPane : predefinedEntityOpenLocation.keySet()) {
			if (predefinedEntityOpenLocation.getValues(tabbedPane).contains(entity)) {
				return true;
			}
		}
		return false;
	}

	/**
	 *
	 * @param entity
	 * @return
	 */
	public static MainFrameTabbedPane getPredefinedEntityOpenLocation(String entity) {
		MainFrameTabbedPane result = null;
		for (MainFrameTabbedPane tabbedPane : predefinedEntityOpenLocation.keySet()) {
			if (predefinedEntityOpenLocation.getValues(tabbedPane).contains(entity)) {
				result = tabbedPane;
			}
		}

		if (result == null) {
			result = homeTabbedPane;
		}

		if (!isTabbedPaneVisible(result)) {
			MainFrameTabbedPane maxTabbedPane = getMaximizedTabbedPaneIfAny(result);
			(new Bubble(maxTabbedPane.getComponentPanel(),
					SpringLocaleDelegate.getInstance().getMessage("MainFrame.3","Neuer Tab im ausgeblendeten Bereich."),
				5,
				Bubble.Position.NO_ARROW_CENTER)).setVisible(true);

		}

		return result;
	}

	public static void removePredefinedEntityOpenLocation(String entity, boolean setup) {
		for (MainFrameTabbedPane tabbedPane : new ArrayList<MainFrameTabbedPane>(predefinedEntityOpenLocation.keySet())) {
			predefinedEntityOpenLocation.removeValue(tabbedPane, entity);
			if (setup) {
				setupStartmenu();
			}
		}
	}

	public static void setPredefinedEntityOpenLocation(String entity, MainFrameTabbedPane tabbedPane) {
		removePredefinedEntityOpenLocation(entity, false);
		predefinedEntityOpenLocation.addValue(tabbedPane, entity);
		setupStartmenu();
	}

	@Override
	public void setComponentName(Field field) {
		final String sFieldName = field.getName();
		final String sQualifiedFieldName = (field.getDeclaringClass().getName() + "." + sFieldName);
		try {
			final Component comp = (Component) field.get(this);
			if (comp != null) {
				LOG.debug("Setting name for component " + sQualifiedFieldName);
				comp.setName(sFieldName);
			}
		}
		catch (IllegalAccessException ex) {
			LOG.warn("Cannot set name for component " + sQualifiedFieldName, ex);
		}
	}


	public Map<String, JComponent> getComponentMap() {
		HashMap<String, JComponent> res = new HashMap<String, JComponent>();
		res.put("windowMenu", menuWindow);
		if (isWorkspaceManagementAvaiable())
			res.put("workspaceChooser", workspaceChooserController.getChooserComponent());
		res.put("liveSearch", liveSearchController.getSearchComponent());
		return res;
	}
	
	private static class ToFrontAction extends AbstractAction {
		
		private final JFrame frame;
		
		private ToFrontAction(String title, JFrame frame) {
			super(title);
			this.frame = frame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			frame.setVisible(true);
		}
		
	}
	
	//init WindowListener to set correct state of notificationButton and menuItem
	private static class NotificationDialogListener extends WindowAdapter {
		
		private final JToggleButton btnNotify;
		
		private final JCheckBoxMenuItem miWindowNotificationDialog;
		
		private NotificationDialogListener(JToggleButton btnNotify, JCheckBoxMenuItem miWindowNotificationDialog) {
			this.btnNotify = btnNotify;
			this.miWindowNotificationDialog = miWindowNotificationDialog;
		}
		
		@Override
		public void windowClosed(WindowEvent ev) {
			btnNotify.setSelected(false);
			miWindowNotificationDialog.setSelected(false);
		}

		@Override
		public void windowOpened(WindowEvent ev) {
			btnNotify.setSelected(true);
			miWindowNotificationDialog.setSelected(true);
		}
	}
	
	//init WindowListener to set correct state of menuItem
	private static class WindowBackgroundTasksListener extends WindowAdapter {
		
		private final JCheckBoxMenuItem miWindowBackgroundTasks;
		
		private WindowBackgroundTasksListener(JCheckBoxMenuItem miWindowBackgroundTasks) {
			this.miWindowBackgroundTasks = miWindowBackgroundTasks;
		}

		@Override
		public void windowClosed(WindowEvent ev) {
			miWindowBackgroundTasks.setSelected(false);
		}

		@Override
		public void windowOpened(WindowEvent ev) {
			miWindowBackgroundTasks.setSelected(true);
		}

	}

	private static class MyCheckBoxMenuItem extends JCheckBoxMenuItem implements IReferenceHolder {
		
		private final List<Object> ref = new LinkedList<Object>();
		
		public MyCheckBoxMenuItem() {
		}
		
		@Override
		public void addRef(EventListener o) {
			ref.add(o);
		}
					
	}
	
	private void initWindowMenu(Map<String, Map<String, Action>> commandMap, NuclosNotificationDialog notificationDialog) {
		menuWindow = new JMenu();
		miDeactivateSplitting = new JCheckBoxMenuItem(actDeactivateSplitting);
		miDeactivateSplitting.setSelected(splittingDeactivated);
		miDeactivateSplitting.setEnabled(splittingEnabled);
		miDeactivateSplitting.setVisible(splittingEnabled);
		
		// Windows menu:
		menuWindow.removeAll();
		
		final MyCheckBoxMenuItem miWindowNotificationDialog = new MyCheckBoxMenuItem();
		final MyCheckBoxMenuItem miWindowBackgroundTasks = new MyCheckBoxMenuItem();
		JMenuItem miNextTab = new JMenuItem();
		JMenuItem miPreviousTab = new JMenuItem();
		JMenuItem miCloseAllTabs = new JMenuItem();
		JMenuItem miRestoreDefaultWorkspace = new JMenuItem();

		MenuGenerator.initMenuItem(menuWindow, localeDelegate.getMessage("miWindow", "^Window"), null, null);
		MenuGenerator.initMenuItem(miDeactivateSplitting, localeDelegate.getMessage("miWDeactivateSplitting","^Disable Window Splitting"), null, null);

		MenuGenerator.initMenuItem(miWindowBackgroundTasks, localeDelegate.getMessage("miBGTasks", "^Background Tasks"), null, null);
		MenuGenerator.initMenuItem(miWindowNotificationDialog, localeDelegate.getMessage("miMessages", "^Messages"), null, null);
		MenuGenerator.initMenuItem(miCloseAllTabs, localeDelegate.getMessage("miWCloseAll", "^Close All Tabs"), null, null);
		MenuGenerator.initMenuItem(miNextTab, localeDelegate.getMessage("miWNext", "^Next Tab"), null, KeyBindingProvider.NEXT_TAB.getKeystroke());
		MenuGenerator.initMenuItem(miPreviousTab, localeDelegate.getMessage("miWPrev", "^Previous Tab"), null, KeyBindingProvider.PREVIOUS_TAB.getKeystroke());
		MenuGenerator.initMenuItem(miRestoreDefaultWorkspace, localeDelegate.getMessage("miWRestoreDefaultWorkspace","Restore Default Workspace"), null, null);

		menuWindow.add(miWindowBackgroundTasks);
		menuWindow.add(miWindowNotificationDialog);
		miWindowNotificationDialog.setSelected(false);
		menuWindow.addSeparator();

		for (final JFrame frame : CollectionUtils.sorted(frameContent.keySet(), new Comparator<JFrame>() {
			@Override
			public int compare(JFrame o1, JFrame o2) {
				int number1 = (o1 instanceof WorkspaceFrame) ? ((WorkspaceFrame) o1).getNumber() : Integer.MAX_VALUE;
				int number2 = (o2 instanceof WorkspaceFrame) ? ((WorkspaceFrame) o2).getNumber() : Integer.MAX_VALUE;
				return LangUtils.compare(number1, number2);
			}
		})) {
			if (frame instanceof WorkspaceFrame) {
				String title = (frame instanceof ExternalFrame) ?
					localeDelegate.getMessage("ExternalFrame.Title","Erweiterungsfenster {0}",((WorkspaceFrame) frame).getNumber()) :
						localeDelegate.getMessage("MainFrame.Title","Hauptfenster");
				JMenuItem miFrameToFront = new JMenuItem(new ToFrontAction("Nuclos " + title, frame));
				menuWindow.add(miFrameToFront);
			}
		}
		menuWindow.addSeparator();

		menuWindow.add(miPreviousTab);
		menuWindow.add(miNextTab);
		menuWindow.add(miCloseAllTabs);
		menuWindow.addSeparator();

		menuWindow.add(miDeactivateSplitting);
		menuWindow.addSeparator();

		menuWindow.add(miRestoreDefaultWorkspace);

		miWindowBackgroundTasks.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				UIUtils.runCommand(MainFrame.this, new Runnable() {
					@Override
					public void run() {
						BackgroundProcessStatusController.getStatusDialog(MainFrame.this).setVisible(miWindowBackgroundTasks.isSelected());
					}
				});
			}
		});
		miNextTab.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdCycleThroughTabs(true);
			}
		});

		miPreviousTab.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdCycleThroughTabs(false);
			}
		});

		miCloseAllTabs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdCloseAllTabs();
			}
		});
		miWindowNotificationDialog.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				getMessagePanel().toggleButton();
			}
		});
		miRestoreDefaultWorkspace.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UIUtils.runCommand(MainFrame.this, new Runnable() {
					@Override
					public void run() {
						WorkspaceChooserController.restoreSelectedWorkspace();
					}
				});
			}
		});

		ListenerUtil.registerWindowListener(notificationDialog, miWindowNotificationDialog,
				new NotificationDialogListener(getMessagePanel().btnNotify, miWindowNotificationDialog));
		ListenerUtil.registerWindowListener(BackgroundProcessStatusController.getStatusDialog(this),
				miWindowBackgroundTasks, new WindowBackgroundTasksListener(miWindowBackgroundTasks));
	}

	/**
	 * tries closes all windows, as requested by the user. All frames get the chance to ask and save if necessary.
	 */
	private void cmdCloseAllTabs() {
		UIUtils.runCommand(this, new Runnable() {
			@Override
			public void run() {
				closeAllTabs();
			}
		});
	}

	/**
	 *
	 * @param next
	 */
	private void cmdCycleThroughTabs(boolean next) {
		MainFrameTabbedPane activeTabNavigation = getActiveTabNavigation();

		int currentIndex = activeTabNavigation.getSelectedIndex();
		int tabCount = activeTabNavigation.getTabCount();
		int toSelect;

		if (next) {
			toSelect = currentIndex + 1;
			if (toSelect == tabCount) toSelect = 0;
		} else {
			toSelect = currentIndex - 1;
			if (toSelect == -1) toSelect = tabCount - 1;
		}

		activeTabNavigation.setSelectedIndex(toSelect);
	}

	public void closeAllTabs() {
		for (MainFrameTabbedPane tabbedPane : frameContent.getAllValues()) {
			tabbedPane.closeAllTabs();
		}
	}

	/**
	 * @return
	 */
	public static List<MainFrameTab> getAllTabs() {
		List<MainFrameTab> result = new ArrayList<MainFrameTab>();
		for (MainFrameTabbedPane tp : frameContent.getAllValues()) {
			result.addAll(tp.getAllTabs());
		}

		return result;
	}

	/**
	 *
	 * @return
	 */
	public static Set<JFrame> getAllFrames() {
		return frameContent.keySet();
	}


	/**
	 * Parses all menuconfig.xml and does an appropriate menu setup.
	 *
	 * @param commandMap command-reference map, which contains master topics
	 * as the first key, and name to action mappings as the result. The menu
	 * parsers commandreference-elements will be split at the period, so that
	 * after all, a command reference "A.B" will result in the action contained
	 * in commandMap.get("A").get("B"),
	 */
	public void menuSetup(Map<String, Map<String, Action>> commandMap,
		Map<String, Map<String, JComponent>> componentMap,
		NuclosNotificationDialog notificationDialog) {
		initWindowMenu(commandMap, notificationDialog);
		final boolean macosx = Main.getInstance().isMacOSX();
		
		try {
			List<Component> exportNotJMenuComponents = macosx ? new ArrayList<Component>() : null;
			MenuGenerator menuGen = new MenuGenerator(commandMap, componentMap, exportNotJMenuComponents);
			menuGen.processMenuConfig(getClass().getClassLoader().getResourceAsStream("nuclos-menuconfig.xml"));
			if (getWorkspace() != null && !getWorkspace().getWoDesc().isHideMenuBar()) {
				Enumeration<URL> resources = getClass().getClassLoader().getResources("menuconfig.xml");
				while (resources.hasMoreElements()) {
					menuGen.processMenuConfig(resources.nextElement());
				}
			}
			JMenuBar mb = menuGen.getJMenuBar();
			setJMenuBar(mb);
			if (macosx && !exportNotJMenuComponents.isEmpty()) {
				componentMacPanel.removeAll();
				componentMacPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
				componentMacPanel.setOpaque(false);
				componentMacPanel.setLayout(new BoxLayout(componentMacPanel, BoxLayout.X_AXIS));

				componentMacPanel.add(Box.createHorizontalGlue());
				for (Component c : exportNotJMenuComponents) {
					componentMacPanel.add(c);
				}

				JPanel contentpane = (JPanel) getContentPane();
				contentpane.add(componentMacPanel, BorderLayout.NORTH);
			}
		}
		catch(Exception e) {
			throw new NuclosFatalException(e);
		}

		if (getWorkspace() != null && !getWorkspace().getWoDesc().isHideMenuBar()) {
			addStaticsToMenu();
			addEntitiesToMenu();
		}
		setupStartmenu();
	}


	private JMenu getParentItem(List<String> menuPath) {
		// Find or create the main item
		JMenuBar b = getJMenuBar();
		JMenu main = null;
		Pair<String, Character> nam = MenuGenerator.getMnemonic(menuPath.get(0));
		for(Component c : b.getComponents()) {
			if(c instanceof JMenu) {
				JMenu m = (JMenu) c;
				if(m.getText().equals(nam.x))
					main = m;
			}
		}
		if(main == null) {
			main = new JMenu(nam.x);
			if (nam.y != null) {
				main.setMnemonic(nam.y);
			}
			int index = MenuGenerator.findCustomInsertionIndex(b.getComponents());
			b.add(main, index);
		}
		main.setVisible(true);

		// find / create parent menu
		JMenu parent = main;

		for(String menuName : menuPath.subList(1, menuPath.size())) {
			nam = MenuGenerator.getMnemonic(menuName);
			JMenu men = null;
			for(int i = 0, n = parent.getItemCount(); i < n; i++) {
				JMenuItem me = parent.getItem(i);
				if(me instanceof JMenu) {
					JMenu m = (JMenu) me;
					if(m.getText().equals(nam.x))
						men = m;
				}
			}
			if(men == null) {
				men = new JMenu(nam.x);
				if (nam.y != null) {
					men.setMnemonic(nam.y);
				}
				int index = MenuGenerator.findCustomInsertionIndex(parent.getMenuComponents());
				parent.add(men, index);
			}
			parent = men;
			parent.setVisible(true);
		}
		return parent;
	}

	/**
	 *
	 * @param eb
	 * @param refresh
	 */
	static void addBookmark(EntityBookmark eb, boolean refresh) {
		if (!updateBookmark(eb)) {
			bookmark.addValue(eb.getEntity(), eb);
			if (refresh) {
				refreshBookmark();
			}
		}
	}

	/**
	 *
	 * @param entity
	 * @param id
	 * @param label
	 */
	public static void addBookmark(String entity, Integer id, String label) {
		addBookmark(new EntityBookmark(entity, id, label), true);
	}

	/**
	 *
	 * @param eb
	 * @return
	 */
	private static boolean updateBookmark(EntityBookmark eb) {
		final int index = bookmark.getValues(eb.getEntity()).indexOf(eb);
		if (index >= 0) {
			final EntityBookmark existing = bookmark.getValues(eb.getEntity()).get(index);
			if (!LangUtils.equals(eb.getLabel(), existing.getLabel())) {
				existing.setLabel(eb.getLabel());
				refreshBookmark();
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 *
	 * @param entity
	 * @param id
	 * @param label
	 */
	public static void addHistory(String entity, Integer id, String label) {
		final EntityBookmark eb = new EntityBookmark(entity, id, label);

		// update label if bookmarked
		updateBookmark(eb);

		final int index = history.getValues(entity).indexOf(eb);
		if (index >= 0) {
			final EntityBookmark existing = history.getValues(entity).get(index);
			if (!LangUtils.equals(eb.getLabel(), existing.getLabel())) {
				existing.setLabel(eb.getLabel());
				refreshHistory();
			}
		} else {
			cleanupHistory(1, false);
			history.addValue(entity, eb);
			refreshHistory();
		}
	}

	/**
	 *
	 * @param newFreeSpace
	 * @param refresh
	 */
	static void cleanupHistory(int newFreeSpace, boolean refresh) {
		Set<EntityBookmark> allValues = history.getAllValues();

		final int freeSpace = HISTORY_SIZES[selectedHistorySize] - allValues.size();
		final int cleanupSize = newFreeSpace - freeSpace;
		final int removeCount = cleanupSize > allValues.size() ? allValues.size() : cleanupSize;

		if (removeCount > 0) {
			final List<EntityBookmark> sortedEB = CollectionUtils.sorted(allValues, new Comparator<EntityBookmark>() {
				@Override
				public int compare(EntityBookmark o1, EntityBookmark o2) {
					return LangUtils.compare(o1.timestamp, o2.timestamp);
				}
			});
			for (int i = 0; i < removeCount; i++) {
				EntityBookmark ebToRemove = sortedEB.get(i);
				history.removeValue(ebToRemove.getEntity(), ebToRemove);
			}
			if (refresh) {
				refreshHistory();
			}
		}
	}

	/**
	 *
	 */
	static void  refreshSelectedHistorySize() {
		for (MainFrameTabbedPane tabbedPane : frameContent.getAllValues()) {
			tabbedPane.refreshSelectedHistorySize();
		}
	}

	/**
	 *
	 */
	static void refreshHistory() {
		for (MainFrameTabbedPane tabbedPane : frameContent.getAllValues()) {
			tabbedPane.refreshHistory();
		}
	}

	/**
	 *
	 */
	static void refreshBookmark() {
		for (MainFrameTabbedPane tabbedPane : frameContent.getAllValues()) {
			tabbedPane.refreshBookmark();
		}
	}

	/**
	 *
	 */
	private void addStaticsToMenu() {
		for (final Pair<String[], Action> p : Main.getInstance().getMainController().getAdministrationMenuActions()) {
			UIUtils.runCommand(MainFrame.this, new CommonRunnable() {
				@Override
				public void run() throws CommonBusinessException {
					addMenuItem(Arrays.asList(p.x), p.y);
				}
			});
		}
		for (final Pair<String[], Action> p : Main.getInstance().getMainController().getConfigurationMenuActions()) {
			UIUtils.runCommand(MainFrame.this, new CommonRunnable() {
				@Override
				public void run() throws CommonBusinessException {
					addMenuItem(Arrays.asList(p.x), p.y);
				}
			});
		}
	}

	private void addEntitiesToMenu() {
		List<Pair<String[], Action>> menuAdditions = new ArrayList<Pair<String[], Action>>();
		final MainController mc = Main.getInstance().getMainController();
		
		menuAdditions.addAll(mc.getEntityMenuActions());
		menuAdditions.addAll(mc.getCustomComponentMenuActions());
		menuAdditions.addAll(mc.getNucletComponentMenuActions());
		menuAdditions.addAll(mc.getTasklistMenuActions());

		final Collator collator = Collator.getInstance(Locale.getDefault());
		final Comparator<String[]> arrayCollator = ComparatorUtils.arrayComparator(collator);
		Collections.sort(menuAdditions, new Comparator<Pair<String[], Action>>() {
			@Override
			public int compare(Pair<String[],Action> p1, Pair<String[],Action> p2) {
				int cmp = arrayCollator.compare(p1.x, p2.x);
				if (cmp == 0)
					cmp = collator.compare(p1.y.getValue(Action.NAME), p2.y.getValue(Action.NAME));
				return cmp;
			}
		});

		for (final Pair<String[], Action> p : menuAdditions) {
			UIUtils.runCommand(MainFrame.this, new CommonRunnable() {
				@Override
				public void run() throws CommonBusinessException {
					addMenuItem(Arrays.asList(p.x), p.y);
				}
			});
		}
	}

	/**
	 *
	 * @param menuPath
	 * @param action
	 */
	void addMenuItem(List<String> menuPath, Action action) {
		if (menuPath == null || menuPath.isEmpty() || action == null)
			return;
		JMenu parent = getParentItem(menuPath);
		int index = MenuGenerator.findCustomInsertionIndex(parent.getMenuComponents());
		JMenuItem mi = new JMenuItem(action);
		parent.add(mi, index);
	}

	/**
	 *
	 *
	 */
	static class ZOrderUpdater implements WindowFocusListener {

		private CommonJFrame frame;

		public ZOrderUpdater(CommonJFrame frame) {
			super();
			this.frame = frame;
		}

		private void updateZOrder() {
			frameZOrder.remove(frame);
			frameZOrder.add(frame);

			LOG.debug("Frame Z-Order: ");
			for (JFrame frame : frameZOrder) {
				LOG.debug(frame);
			}
		}

		@Override
		public void windowGainedFocus(WindowEvent e) {
			 updateZOrder();
		}

		@Override
		public void windowLostFocus(WindowEvent e) {
		}

	}

	/**
	 *
	 * @param ico
	 * @param max
	 * @return
	 */
	public static ImageIcon resizeAndCacheIcon(Icon ico, int max) {
		if (ico instanceof ImageIcon) {
			ImageIcon imgico = (ImageIcon) ico;

			Map<Integer, ImageIcon> icoCache = resizedIcons.get(imgico);
			if (icoCache == null) {
				icoCache = new HashMap<Integer, ImageIcon>();
				resizedIcons.put(imgico, icoCache);
			}

			ImageIcon cached = icoCache.get(max);
			if (cached == null) {
				final int w = imgico.getIconWidth();
				final int h = imgico.getIconHeight();

		        final BufferedImage bi = new BufferedImage(w > h ? w : h, w > h ? w : h, BufferedImage.TYPE_INT_ARGB);
		        final Graphics2D g2 = bi.createGraphics();
		        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		        g2.drawImage(imgico.getImage(), w > h ? 0 : (h-w)/2, w > h ? (w-h)/2 : 0, null);
		        g2.dispose();
		        // final Image image = bi.getScaledInstance(max, -1, java.awt.Image.SCALE_SMOOTH);
		        final Image image = ImageScaler.scaleImage(bi, max, -1);
				cached =  new ImageIcon(image);
				icoCache.put(max, cached);
			}

			return cached;
		} else {
			return null;
		}
	}

	/**
	 *
	 * @param ico
	 * @return
	 */
	public static ImageIcon resizeAndCacheLinkIcon(Icon ico) {
		return resizeAndCacheIcon(ico, LINK_ICON_MAX);
	}

	/**
	 *
	 * @param ico
	 * @return
	 */
	public static ImageIcon resizeAndCacheTabIcon(ImageIcon ico) {
		return resizeAndCacheIcon(ico, TAB_CONTENT_ICON_MAX);
	}

	/**
	 *
	 *
	 */
	private static class MaximizedTabbedPaneParameter {
		final JSplitPane splitPaneRoot;
		final JSplitPane splitPaneOrigin;
		final boolean left;
		final MainFrameTabbedPane tabbedPane;
		final Map<JSplitPane, Integer> dividerLocations;

		public MaximizedTabbedPaneParameter(
				JSplitPane splitPaneRoot,
				JSplitPane splitPaneOrigin, boolean left,
				MainFrameTabbedPane tabbedPane,
				Map<JSplitPane, Integer> dividerLocations) {
			this.splitPaneRoot = splitPaneRoot;
			this.splitPaneOrigin = splitPaneOrigin;
			this.left = left;
			this.tabbedPane = tabbedPane;
			this.dividerLocations = dividerLocations;
		}
	}

	/**
	 *
	 * @param tabbedPane
	 */
	void restoreTabbedPaneContainingArea(MainFrameTabbedPane tabbedPane) {
		if (maximizedTabbedPanes.containsKey(tabbedPane)) {
			MaximizedTabbedPaneParameter mtpp = maximizedTabbedPanes.get(tabbedPane);
			final JFrame frame = getJFrame(tabbedPane);

			if (frame == this) {
				pnlDesktop.remove(tabbedPane.getComponentPanel());
				restoreJSplitPanes(mtpp);
				setFrameContent(mtpp.splitPaneRoot);

				pnlDesktop.validate();
				pnlDesktop.repaint();

			} else  if (frame instanceof ExternalFrame) {
				((ExternalFrame) frame).clearFrameContent();
				restoreJSplitPanes(mtpp);
				((ExternalFrame) frame).setFrameContent(mtpp.splitPaneRoot);
				frame.validate();
				frame.repaint();

			} else {
				throw new IllegalArgumentException("Unknown frame: " + frame);
			}

			tabbedPane.setMaximized(false);
			maximizedTabbedPanes.remove(tabbedPane);
		}
	}

	/**
	 * Restore all maximized tabbedpanes
	 */
	public void restoreAllTabbedPaneContainingArea() {
		for (MainFrameTabbedPane tabbedPane : frameContent.getAllValues()) {
			restoreTabbedPaneContainingArea(tabbedPane);
		}
	}

	/**
	 *
	 * @param mtpp
	 */
	private static void restoreJSplitPanes(MaximizedTabbedPaneParameter mtpp) {
		if (mtpp.left) {
			mtpp.splitPaneOrigin.setLeftComponent(mtpp.tabbedPane.getComponentPanel());
		} else {
			mtpp.splitPaneOrigin.setRightComponent(mtpp.tabbedPane.getComponentPanel());
		}
		for (JSplitPane splitPane : mtpp.dividerLocations.keySet()) {
			splitPane.setDividerLocation(mtpp.dividerLocations.get(splitPane));
		}
	}

	/**
	 *
	 * @param tabbedPane
	 */
	void maximizeTabbedPane(MainFrameTabbedPane tabbedPane) {

		MaximizedTabbedPaneParameter mtpp = getMaximizedTabbedPaneParameter(tabbedPane);
		if (mtpp != null) {
			JFrame frame = getJFrame(tabbedPane);
			tabbedPane.setMaximized(true);
			maximizedTabbedPanes.put(tabbedPane, mtpp);

			if (frame == this) {
				pnlDesktop.remove(mtpp.splitPaneRoot);
				pnlDesktop.add(tabbedPane.getComponentPanel(), BorderLayout.CENTER);
				pnlDesktop.validate();
				pnlDesktop.repaint();

			} else  if (frame instanceof ExternalFrame) {
				((ExternalFrame) frame).clearFrameContent();
				((ExternalFrame) frame).setFrameContent(tabbedPane.getComponentPanel());
				frame.validate();
				frame.repaint();

			} else {
				throw new IllegalArgumentException("Unknown frame: " + frame);
			}
		}
	}

	/**
	 *
	 * @param tabbedPane
	 * @return
	 */
	static boolean isTabbedPaneMaximized(MainFrameTabbedPane tabbedPane) {
		return maximizedTabbedPanes.containsKey(tabbedPane);
	}

	/**
	 *
	 * @param tabbedPane
	 * @return
	 */
	static boolean isTabbedPaneMaximizable(MainFrameTabbedPane tabbedPane) {
		int countTabbedFrames = countTabbedPanes(getFrame(tabbedPane));
		return countTabbedFrames > 1;
	}

	/**
	 *
	 * @param tabbedPane
	 * @return
	 */
	private static MaximizedTabbedPaneParameter getMaximizedTabbedPaneParameter(MainFrameTabbedPane tabbedPane) {
		if (tabbedPane.getComponentPanel().getParent() instanceof JSplitPane) {
			final JSplitPane splitPane = (JSplitPane) tabbedPane.getComponentPanel().getParent();
			final boolean left = splitPane.getLeftComponent() == tabbedPane.getComponentPanel();

			final Map<JSplitPane, Integer> dividerLocations = new HashMap<JSplitPane, Integer>();
			dividerLocations.put(splitPane, splitPane.getDividerLocation());

			JSplitPane splitPaneRoot = null;
			Container parent = tabbedPane.getComponentPanel().getParent();
			while (parent != null) {
				if (parent instanceof JSplitPane) {
					splitPaneRoot = (JSplitPane) parent;
					dividerLocations.put(splitPaneRoot, splitPaneRoot.getDividerLocation());
				}
				parent = parent.getParent();
			}
			if (splitPaneRoot == null) splitPaneRoot = splitPane;

			MaximizedTabbedPaneParameter result = new MaximizedTabbedPaneParameter(splitPaneRoot, splitPane, left, tabbedPane, dividerLocations);
			return result;
		} else {
			return null;
		}
	}

	/**
	 *
	 * @param tabbedPaneToSplit
	 * @param splitRange
	 * @param tabbedPaneSource
	 * @param tabIndexSource
	 */
	void splitTabbedPane(MainFrameTabbedPane tabbedPaneToSplit, SplitRange splitRange, MainFrameTabbedPane tabbedPaneSource, int tabIndexSource) {

		restoreTabbedPaneContainingArea(tabbedPaneToSplit);

		final MainFrameTab tab = (MainFrameTab) tabbedPaneSource.getComponentAt(tabIndexSource);
		// remove first to join necessary TabbedPanes
		tabbedPaneSource.remove(tabIndexSource);

		final Container parent = tabbedPaneToSplit.getComponentPanel().getParent();

		final JFrame frame = getJFrame(tabbedPaneToSplit);
		final MainFrameTabbedPane newTabbedPane = createTabbedPane(frame);

		newTabbedPane.addTab(tab.getTitle(), tab.getTabIcon(), tab, tab.getTitle());
		newTabbedPane.setSelectedIndex(1);

		int newOrientation = splitRange == SplitRange.NORTH ||
							 splitRange == SplitRange.SOUTH ?
								 JSplitPane.VERTICAL_SPLIT : JSplitPane.HORIZONTAL_SPLIT;

		boolean northOrWest = splitRange == SplitRange.NORTH || splitRange == SplitRange.WEST;

		final JSplitPane newSplitPane;
		final Dimension newSplitPaneSize = tabbedPaneToSplit.getComponentPanel().getSize();

		if (parent instanceof JSplitPane) {
			JSplitPane splitPaneParent = (JSplitPane) parent;
			int parentDiverLocation = splitPaneParent.getDividerLocation();
			boolean isLeftComponent = splitPaneParent.getLeftComponent() == tabbedPaneToSplit.getComponentPanel();

			splitPaneParent.remove(tabbedPaneToSplit.getComponentPanel());
			newSplitPane = new MainFrameSplitPane(newOrientation, SPLIT_CONTINUOS_LAYOUT, northOrWest ? newTabbedPane.getComponentPanel() : tabbedPaneToSplit.getComponentPanel(), northOrWest ? tabbedPaneToSplit.getComponentPanel() : newTabbedPane.getComponentPanel());

			if (isLeftComponent) {
				splitPaneParent.setLeftComponent(newSplitPane);
			} else {
				splitPaneParent.setRightComponent(newSplitPane);
			}
			splitPaneParent.setDividerLocation(parentDiverLocation);
			splitPaneParent.validate();
			splitPaneParent.repaint();

		} else if (frame == MainFrame.this) {
			// first SplitPane
			pnlDesktop.remove(tabbedPaneToSplit.getComponentPanel());
			newSplitPane = new MainFrameSplitPane(newOrientation, SPLIT_CONTINUOS_LAYOUT, northOrWest ? newTabbedPane.getComponentPanel() : tabbedPaneToSplit.getComponentPanel(), northOrWest ? tabbedPaneToSplit.getComponentPanel() : newTabbedPane.getComponentPanel());

			pnlDesktop.add(newSplitPane, BorderLayout.CENTER);
			pnlDesktop.validate();
			pnlDesktop.repaint();

		} else if (frame instanceof ExternalFrame) {
			((ExternalFrame) frame).clearFrameContent();
			newSplitPane = new MainFrameSplitPane(newOrientation, SPLIT_CONTINUOS_LAYOUT, northOrWest ? newTabbedPane.getComponentPanel() : tabbedPaneToSplit.getComponentPanel(), northOrWest ? tabbedPaneToSplit.getComponentPanel() : newTabbedPane.getComponentPanel());

			((ExternalFrame) frame).setFrameContent(newSplitPane);
			frame.validate();
			frame.repaint();

		} else {
			throw new IllegalArgumentException("Unknown parent: " + parent.getClass().getName());
		}

		newSplitPane.setOneTouchExpandable(SPLIT_ONE_TOUCH_EXPANDABLE);
		newSplitPane.setDividerLocation(newOrientation==JSplitPane.VERTICAL_SPLIT ? newSplitPaneSize.height/2 : newSplitPaneSize.width/2);

		updateTabbedPaneActions(frame);
	}

	/**
	 *
	 * @param tab
	 */
	public static void addTab(MainFrameTab tab) {
		if (homeTabbedPane != null)
			homeTabbedPane.add(tab);
	}

	/**
	 *
	 * @param tab
	 */
	public static void addTabToTreeHome(MainFrameTab tab) {
		homeTreeTabbedPane.add(tab);
	}

	/**
	 *
	 * @param tab
	 * @param entity
	 */
	static void addTab(MainFrameTab tab, String entity) {
		if (entity == null) {
			addTab(tab);
		}
		getPredefinedEntityOpenLocation(entity).add(tab);
	}

	/**
	 *
	 * @param tab
	 * @param mousePosition
	 * @throws CommonBusinessException if tab is not closable
	 */
	static void closeTab(MainFrameTab tab, Point mousePosition) throws CommonBusinessException {
		final MainFrameTabbedPane mtab = getTabbedPane(tab);
		if (mtab != null) {
			mtab.closeTab(tab, mousePosition);
		}
	}

	/**
	 *
	 * @param tab
	 * @throws CommonBusinessException
	 */
	public static void closeTab(MainFrameTab tab) throws CommonBusinessException {
		closeTab(tab, null);
	}

	/**
	 *
	 * @param tab
	 */
	public static void setSelectedTab(MainFrameTab tab) {
		MainFrameTabbedPane tabbedPane = getTabbedPane(tab);
		if (tabbedPane != null && !tabbedPane.restoreHiddenTab(tab)) {
			tabbedPane.setSelectedComponent(tab);
		}
	}

	/**
	 *
	 * @param tabbedPaneOnScreen
	 * @return
	 */
	public static MainFrameTab getSelectedTab(Point tabbedPaneOnScreen) {
		final MainFrameTabbedPane tabbedPane = getTabbedPane(tabbedPaneOnScreen);

		if (tabbedPane != null) {
			Component c = tabbedPane.getSelectedComponent();
			if (c instanceof MainFrameTab) {
				return (MainFrameTab) c;
			} else {
				return null;
			}
		} else {
			throw new NuclosFatalException("At this point "+tabbedPaneOnScreen.toString()+" exists no TabbedPane");
		}
	}

	/**
	 *
	 * @param locOnScreen
	 * @return
	 */
	static MainFrameTabbedPane getTabbedPane(Point locOnScreen) {
		for (MainFrameTabbedPane tabbedPane : getOrderedTabbedPanes()) {
			try {
				final Rectangle boundsOnScreen = new Rectangle(tabbedPane.getComponentPanel().getLocationOnScreen(), tabbedPane.getComponentPanel().getBounds().getSize());
				if (boundsOnScreen.contains(locOnScreen)) {
					return tabbedPane;
				}
			} catch (IllegalComponentStateException e) {
				// hidden tab
			}
		}

		return null;
	}

	/**
	 *
	 * @return
	 */
	public static Set<MainFrameTabbedPane> getAllTabbedPanes() {
		return frameContent.getAllValues();
	}

	/**
	 *
	 * @param tab
	 * @return
	 */
	public static MainFrameTabbedPane getTabbedPane(MainFrameTab tab) {
		for (MainFrameTabbedPane tabbedPane : frameContent.getAllValues()) {
			if (tabbedPane.indexOfComponent(tab) >= 0 || tabbedPane.getHiddenTabs().contains(tab)) {
				return tabbedPane;
			}
		}
		LOG.warn("Tab not found in MainFrame: " + tab);
		return null;
	}

	/**
	 *
	 * @param tabbedPane
	 */
	public void removeTabbedPane(MainFrameTabbedPane tabbedPane) {
		removeTabbedPane(tabbedPane, false);
	}

	/**
	 *
	 * @param tabbedPane
	 * @param forcedFromFrameClose
	 */
	public void removeTabbedPane(MainFrameTabbedPane tabbedPane, boolean forcedFromFrameClose) {
		removeTabbedPane(tabbedPane, forcedFromFrameClose, true);
	}

	/**
	 *
	 * @param tabbedPane
	 * @param forcedFromFrameClose
	 * @param addNotClosableToHome
	 */
	public void removeTabbedPane(MainFrameTabbedPane tabbedPane, boolean forcedFromFrameClose, boolean addNotClosableToHome) {
		final JFrame frame = getJFrame(tabbedPane);
		final int countTabbedsOnFrame = countTabbedPanes(frame);

		if (!forcedFromFrameClose && countTabbedsOnFrame == 1) {
			// do not remove last TabbedPane

		} else {

			if (!forcedFromFrameClose && tabbedPane.getTabCount() > 1) {
				if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(
					tabbedPane.getComponentPanel(),
					localeDelegate.getMessage("MainFrame.1","Tab Leiste mit allen enthaltenen Tabs entfernen.\nMoechten Sie fortfahren?"),
					localeDelegate.getMessage("MainFrame.2","Tab Leiste entfernen"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE))
					return;
			}

			restoreTabbedPaneContainingArea(tabbedPane);

			List<MainFrameTab> notClosableTabs = tabbedPane.closeAllTabs();


			if (forcedFromFrameClose) {
				frameContent.removeValue(frame, tabbedPane);
			} else {
				final JSplitPane splitPane = (JSplitPane) tabbedPane.getComponentPanel().getParent();
				Component otherComp = splitPane.getLeftComponent() == tabbedPane.getComponentPanel() ? splitPane.getRightComponent() : splitPane.getLeftComponent();
				splitPane.removeAll();
				frameContent.removeValue(frame, tabbedPane);

				if (splitPane.getParent() instanceof JSplitPane){
					JSplitPane splitPaneParent = (JSplitPane) splitPane.getParent();
					int dividerLocation = splitPaneParent.getDividerLocation();

					if (splitPaneParent.getLeftComponent() == splitPane) {
						splitPaneParent.remove(splitPane);
						splitPaneParent.setLeftComponent(otherComp);
					} else {
						splitPaneParent.remove(splitPane);
						splitPaneParent.setRightComponent(otherComp);
					}
					splitPaneParent.setDividerLocation(dividerLocation);
					splitPaneParent.validate();
					splitPaneParent.repaint();

				} else if (frame == this) {
					pnlDesktop.remove(splitPane);
					pnlDesktop.add(otherComp, BorderLayout.CENTER);
					pnlDesktop.validate();
					pnlDesktop.repaint();

				} else if (frame instanceof ExternalFrame) {
					((ExternalFrame) frame).clearFrameContent();
					((ExternalFrame) frame).setFrameContent(otherComp);
					frame.validate();
					frame.repaint();

				} else {
					throw new IllegalArgumentException("Unknown parent: " + splitPane.getParent().getClass().getName());
				}
			}

			if (homeTabbedPane == tabbedPane || homeTreeTabbedPane == tabbedPane) {
				if (homeTabbedPane == tabbedPane) {
					try {
						homeTabbedPane = frameContent.getAllValues().iterator().next();
					} catch (NoSuchElementException ex) {
						// clearing workspace...
						homeTabbedPane = null;
					}
				}
				if (homeTreeTabbedPane == tabbedPane) {
					try {
						homeTreeTabbedPane = frameContent.getAllValues().iterator().next();
					} catch (NoSuchElementException ex) {
						// clearing workspace...
						homeTreeTabbedPane = null;
					}

				}
				for (MainFrameTabbedPane cursor : frameContent.getAllValues()) {
					cursor.updateHomes();
				}
			}

			if (predefinedEntityOpenLocation.containsKey(tabbedPane)) {
				predefinedEntityOpenLocation.removeKey(tabbedPane);
				setupStartmenu();
			}

			if (addNotClosableToHome) {
				for (MainFrameTab notClosed : notClosableTabs) {
					addTab(notClosed);
				}
			}

			if (!forcedFromFrameClose) {
				updateTabbedPaneActions(frame);
			}
		}
	}

	/**
	 *
	 */
	static void setupStartmenu() {
		for (MainFrameTabbedPane tabbedPane : frameContent.getAllValues()) {
			tabbedPane.setupStartmenu();
		}
	}

	/**
	 *
	 * @param dp
	 * @param position
	 */
	static void createExternalFrame(MainFrameTabbedPane.DragParameter dp, Point position) {

		MainFrameTab tab = (MainFrameTab) dp.originTabbedPane.getComponentAt(dp.draggedTabIndex);
		// remove first to join necessary TabbedPanes
		dp.originTabbedPane.remove(dp.draggedTabIndex);

		MainFrameTabbedPane newTabbedPane = new MainFrameTabbedPane();

		newTabbedPane.addTab(tab.getTitle(), tab.getTabIcon(), tab, tab.getTitle());
		newTabbedPane.setSelectedIndex(1);

		ExternalFrame ef = new ExternalFrame(nextExternalFrameNumber);
		ef.setLocation(position);
		ef.setSize(newTabbedPane.getComponentPanel().getPreferredSize());
		ef.setFrameContent(newTabbedPane.getComponentPanel());
		ef.setVisible(true);
		ef.setFrameContent(newTabbedPane.getComponentPanel());
		newTabbedPane.adjustTabs();
		frameContent.addValue(ef, newTabbedPane);

		nextExternalFrameNumber++;

		Main.getInstance().getMainController().refreshMenus();
	}

	/**
	 *
	 * @return
	 */
	public static WorkspaceFrame createWorkspaceFrame() {
		ExternalFrame result = new ExternalFrame(nextExternalFrameNumber);

		nextExternalFrameNumber++;

		return result;
	}

	/**
	 *
	 *
	 */
	public enum SplitRange {
		NORTH, SOUTH, WEST, EAST, NONE;
	}

	/**
	 *
	 * @param tabbedPane
	 * @return
	 */
	private static JFrame getJFrame(MainFrameTabbedPane tabbedPane) {
		for (JFrame frame : frameContent.keySet()) {
			for (MainFrameTabbedPane tabs : frameContent.getValues(frame)) {
				if (tabbedPane == tabs) {
					return frame;
				}
			}
		}

		throw new IllegalArgumentException("TabbedPane is not listed");
	}

	/**
	 *
	 * @param frame
	 * @return
	 */
	private static int countTabbedPanes(JFrame frame) {
		if (!frameContent.containsKey(frame)) {
			throw new IllegalArgumentException("Frame is not listed");
		}

		return frameContent.getValues(frame).size();
	}

	/**
	 *
	 * @param frame
	 */
	public static void updateTabbedPaneActions(JFrame frame) {
		int countTabbedFrames = countTabbedPanes(frame);

		for (MainFrameTabbedPane tab : frameContent.getValues(frame)) {
			tab.setCloseEnabled(countTabbedFrames > 1);
			tab.setMaximizeEnabled(countTabbedFrames > 1);
		}
	}

	/**
	 *
	 * @return
	 */
	static List<MainFrameTabbedPane> getOrderedTabbedPanes() {
		List<MainFrameTabbedPane> result = new ArrayList<MainFrameTabbedPane>();

		for (int i = frameZOrder.size()-1; i >= 0; i--) {
			result.addAll(frameContent.getValues(frameZOrder.get(i)));
		}

		return result;
	}

	/**
	 *
	 * @return
	 */
	public static List<CommonJFrame> getOrderedFrames() {
		return new ArrayList<CommonJFrame>(frameZOrder);
	}

	/**
	 *
	 */
	@Override
	public Component getFrameContent() {
		return pnlDesktop.getComponent(0);
	}

	/**
	 *
	 */
	public void clearFrame() {
		pnlDesktop.removeAll();
	}

	/**
	 *
	 * @param frame
	 * @return
	 */
	public MainFrameTabbedPane createTabbedPane(JFrame frame) {
		final MainFrameTabbedPane newTabbedPane = new MainFrameTabbedPane();
		frameContent.addValue(frame, newTabbedPane);
		return newTabbedPane;
	}

	/**
	 *
	 */
	@Override
	public void setFrameContent(Component comp) {
		pnlDesktop.removeAll();
		pnlDesktop.add(comp, BorderLayout.CENTER);
	}

	@Override
	public CommonJFrame getFrame() {
		return this;
	}

	void setupLiveSearchKey(JFrame frame) {
		frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyBindingProvider.FOCUS_ON_LIVE_SEARCH.getKeystroke(), KeyBindingProvider.FOCUS_ON_LIVE_SEARCH.getKey());
		frame.getRootPane().getActionMap().put(KeyBindingProvider.FOCUS_ON_LIVE_SEARCH	.getKey(), new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				cmdFocusLiveSearch();
			}
		});
	}

	private void cmdFocusLiveSearch() {
		setVisible(true);
		requestFocusInWindow();
		liveSearchController.getSearchComponent().requestFocusInWindow();
	}

	/**
	 *
	 * @param mainFramePrefs
	 * @throws BackingStoreException
	 * @throws PreferencesException
	 */
	public static void writeMainFramePreferences(Preferences mainFramePrefs) throws BackingStoreException, PreferencesException {
		mainFramePrefs.putBoolean(PREFS_NODE_SPLITTING_DEACTIVATED, splittingDeactivated);
		mainFramePrefs.putInt(PREFS_NODE_HISTORY_SIZE_INDEX, selectedHistorySize);
		mainFramePrefs.put(PREFS_NODE_DEFAULT_WORKSPACE, defaultWorkspace);
		mainFramePrefs.put(PREFS_NODE_LAST_WORKSPACE, getWorkspace()==null?defaultWorkspace:getWorkspace().getName());
		mainFramePrefs.putLong(PREFS_NODE_LAST_WORKSPACE_ID, getWorkspace().getId());
		if (lastAlwaysOpenWorkspace != null)
			mainFramePrefs.put(PREFS_NODE_LAST_ALWAYS_OPEN_WORKSPACE, lastAlwaysOpenWorkspace);
		if (lastAlwaysOpenWorkspaceId != null)
			mainFramePrefs.putLong(PREFS_NODE_LAST_ALWAYS_OPEN_WORKSPACE_ID, lastAlwaysOpenWorkspaceId);
		PreferencesUtils.putStringList(mainFramePrefs, PREFS_NODE_WORKSPACE_ORDER,
				CollectionUtils.transform(WorkspaceChooserController.getWorkspaceHeaders(),
						new Transformer<WorkspaceVO, String>() {
							@Override
							public String transform(WorkspaceVO i) {
								return i.getWoDesc().getName();
							}
						}));
		PreferencesUtils.putLongList(mainFramePrefs, PREFS_NODE_WORKSPACE_ORDER_IDS,
				CollectionUtils.transform(WorkspaceChooserController.getWorkspaceHeaders(),
						new Transformer<WorkspaceVO, Long>() {
							@Override
							public Long transform(WorkspaceVO i) {
								return i.getId();
							}
						}));

		mainFramePrefs.node(PREFS_NODE_BOOKMARK).removeNode();
		Preferences prefsBookmark = mainFramePrefs.node(PREFS_NODE_BOOKMARK);
		for (String entity : bookmark.keySet()) {
			PreferencesUtils.putSerializableListXML(prefsBookmark, entity, bookmark.getValues(entity));
		}

		mainFramePrefs.node(PREFS_NODE_HISTORY).removeNode();
		Preferences prefsHistory = mainFramePrefs.node(PREFS_NODE_HISTORY);
		for (String entity : history.keySet()) {
			PreferencesUtils.putSerializableListXML(prefsHistory, entity, history.getValues(entity));
		}
	}

	/**
	 *
	 * @param mainFramePrefs
	 * @throws BackingStoreException
	 * @throws PreferencesException
	 */
	public void readMainFramePreferences(Preferences mainFramePrefs)
			throws BackingStoreException, PreferencesException {
		setSplittingDeactivated(mainFramePrefs.getBoolean(PREFS_NODE_SPLITTING_DEACTIVATED, false));
		selectedHistorySize = mainFramePrefs.getInt(PREFS_NODE_HISTORY_SIZE_INDEX, 0);
		defaultWorkspace = mainFramePrefs.get(PREFS_NODE_DEFAULT_WORKSPACE, SpringLocaleDelegate.getInstance().getMessage(
				"Workspace.Default","Standard"));
		lastWorkspace = mainFramePrefs.get(PREFS_NODE_LAST_WORKSPACE, SpringLocaleDelegate.getInstance().getMessage(
				"Workspace.Default","Standard"));
		lastWorkspaceId = mainFramePrefs.getLong(PREFS_NODE_LAST_WORKSPACE_ID, 0l);
		lastAlwaysOpenWorkspace = mainFramePrefs.get(PREFS_NODE_LAST_ALWAYS_OPEN_WORKSPACE, null);
		lastAlwaysOpenWorkspaceId = mainFramePrefs.getLong(PREFS_NODE_LAST_ALWAYS_OPEN_WORKSPACE_ID, 0l);
		WorkspaceChooserController.setupWorkspaces(
				PreferencesUtils.getLongList(mainFramePrefs, PREFS_NODE_WORKSPACE_ORDER_IDS),
				PreferencesUtils.getStringList(mainFramePrefs, PREFS_NODE_WORKSPACE_ORDER));

		Preferences prefsBookmark = mainFramePrefs.node(PREFS_NODE_BOOKMARK);
		for (String entity : prefsBookmark.childrenNames()) {
			bookmark.addAllValues(entity, (List<EntityBookmark>) PreferencesUtils.getSerializableListXML(prefsBookmark, entity, true));
		}

		Preferences prefsHistory = mainFramePrefs.node(PREFS_NODE_HISTORY);
		for (String entity : prefsHistory.childrenNames()) {
			history.addAllValues(entity, (List<EntityBookmark>) PreferencesUtils.getSerializableListXML(prefsHistory, entity, true));
		}

		refreshSelectedHistorySize();
		refreshBookmark();
		refreshHistory();
	}

	/**
	 *
	 */
	static void clearHistory() {
		history.clear();
		refreshHistory();
	}

	/**
	 *
	 */
	static void clearBookmark() {
		bookmark.clear();
		refreshBookmark();
	}

	/**
	 *
	 * @return
	 */
	static int getSelectedHistorySize() {
		return selectedHistorySize;
	}

	/*
	 *
	 */
	static void setSelectedHistorySize(int index) {
		selectedHistorySize = index;
		cleanupHistory(0, true);
		refreshSelectedHistorySize();
	}

	/**
	 *
	 * @return
	 */
	static MultiListMap<String, EntityBookmark> getHistory() {
		return history;
	}

	/**
	 *
	 * @return
	 */
	static MultiListMap<String, EntityBookmark> getBookmark() {
		return bookmark;
	}

	/**
	 *
	 * @param tabbedPane
	 * @return
	 */
	static JFrame getFrame(MainFrameTabbedPane tabbedPane) {
		for (JFrame frame : frameContent.keySet()) {
			if (frameContent.getValues(frame).contains(tabbedPane)) {
				return frame;
			}
		}
		throw new NuclosFatalException("Tabbedpane is not listed");
	}

	/**
	 *
	 * @param tabbedPane
	 */
	static void setHomeTabbedPane(MainFrameTabbedPane tabbedPane) {
		homeTabbedPane = tabbedPane;
	}

	/**
	 *
	 * @param tabbedPane
	 */
	static void setHomeTreeTabbedPane(MainFrameTabbedPane tabbedPane) {
		homeTreeTabbedPane = tabbedPane;
	}

	/**
	 *
	 * @param frame
	 * @return
	 */
	static List<MainFrameTabbedPane> getTabbedPanes(JFrame frame) {
		return frameContent.getValues(frame);
	}

	/**
	 *
	 * @param frame
	 */
	static void removeFrameFromContent(JFrame frame) {
		frameContent.removeKey(frame);
		frameZOrder.remove(frame);
	}

	/**
	 *
	 * @return
	 */
	public static boolean isSplittingDeactivated() {
		return splittingDeactivated;
	}

	/**
	 *
	 * @param splittingDeactivated
	 */
	public void setSplittingDeactivated(boolean splittingDeactivated) {
		MainFrame.splittingDeactivated = splittingDeactivated;
		miDeactivateSplitting.setSelected(splittingDeactivated);
	}

	/**
	 *
	 * @return splittingEnabled
	 */
	public static boolean isSplittingEnabled() {
		return MainFrame.splittingEnabled;
	}

	/**
	 *
	 * @param enabled
	 */
	public void setSplittingEnabled(boolean enabled) {
		MainFrame.splittingEnabled = enabled;
		miDeactivateSplitting.setEnabled(enabled);
		miDeactivateSplitting.setVisible(enabled);
	}

	/**
	 *
	 * @return
	 */
	public static boolean isStarttabEditable() {
		if (getWorkspace().getAssignedWorkspace() == null) {
			return true;
		} else {
			return SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CUSTOMIZE_STARTTAB);
		}
	}

	/**
	 *
	 */
	@Override
	public int getNumber() {
		return 0;
	}

	/**
	 *
	 * @return
	 */
	public static MainFrameTabbedPane getActiveTabNavigation() {
		return activeTabNavigation == null? homeTabbedPane : activeTabNavigation;
	}

	/**
	 *
	 * @param activeTabNavigation
	 */
	public static void setActiveTabNavigation(MainFrameTabbedPane activeTabNavigation) {
		MainFrame.activeTabNavigation = activeTabNavigation;
	}

	/**
	 *
	 * @return
	 */
	public static WorkspaceVO getWorkspace() {
		return WorkspaceChooserController.getSelectedWorkspace();
	}

	/**
	 *
	 * @return
	 */
	public static WorkspaceDescription getWorkspaceDescription() {
		return getWorkspace().getWoDesc();
	}

	/**
	 *
	 * @return
	 */
	public static List<WorkspaceVO> getWorkspaceHeaders() {
		return WorkspaceChooserController.getWorkspaceHeaders();
	}

	/**
	 *
	 */
	public static void refreshWorkspacesHeaders() {
		WorkspaceChooserController.setupWorkspaces();
	}

	/**
	 *
	 * @param name
	 */
	public static void setWorkspace(WorkspaceVO wovo) {
		WorkspaceChooserController.setSelectedWorkspace(wovo);
	}

	/**
	 *
	 */
	public static void resetExternalFrameNumber() {
		nextExternalFrameNumber = 1;
	}

	/**
	 *
	 * @return
	 */
	public static boolean isWorkspaceManagementAvaiable() {
		return true; //ApplicationProperties.getInstance().isFunctionBlockDev();
	}

	/**
	 *
	 * @return
	 */
	public static boolean isWorkspaceManagementEnabled() {
		return WorkspaceChooserController.isEnabled();
	}

	/**
	 *
	 * @param b
	 * @return
	 */
	public static void setWorkspaceManagementEnabled(boolean b) {
		WorkspaceChooserController.setEnabled(b);
	}

	/**
	 *
	 * @return
	 */
	public static String getDefaultWorkspace() {
		return defaultWorkspace;
	}

	/**
	 *
	 * @return
	 */
	public static String getLastWorkspaceFromPreferences() {
		return lastWorkspace;
	}

	/**
	 *
	 * @return
	 */
	public static Long getLastWorkspaceIdFromPreferences() {
		return lastWorkspaceId;
	}

	/**
	 *
	 * @return
	 */
	public static String getLastAlwaysOpenWorkspaceFromPreferences() {
		return lastAlwaysOpenWorkspace;
	}

	/**
	 *
	 * @param lastAlwaysOpenWorkspace
	 */
	public static void setLastAlwaysOpenWorkspace(String lastAlwaysOpenWorkspace) {
		MainFrame.lastAlwaysOpenWorkspace = lastAlwaysOpenWorkspace;
	}

	/**
	 *
	 * @param lastAlwaysOpenWorkspaceId
	 */
	public static void setLastAlwaysOpenWorkspaceId(Long lastAlwaysOpenWorkspaceId) {
		MainFrame.lastAlwaysOpenWorkspaceId = lastAlwaysOpenWorkspaceId;
	}

	/**
	 *
	 * @return
	 */
	public static Long getLastAlwaysOpenWorkspaceIdFromPreferences() {
		return lastAlwaysOpenWorkspaceId;
	}

	/**
	 *
	 * @param entity
	 * @return
	 */
	public ImageIcon getEntityIcon(String entity) {
		ImageIcon result = null;

		Integer resourceId = MetaDataClientProvider.getInstance().getEntity(entity).getResourceId();
		String nuclosResource = MetaDataClientProvider.getInstance().getEntity(entity).getNuclosResource();
		if (resourceId != null) {
			result = resourceCache.getIconResource(resourceId);
		} else if (nuclosResource != null) {
			result = NuclosResourceCache.getNuclosResourceIcon(nuclosResource);
		}

		if (result == null) {
			if (NuclosEntity.isNuclosEntity(entity)) {
				result = NuclosIcons.getInstance().getDefaultFrameIcon();
			} else {
				result = Icons.getInstance().getIconTabGeneric();
			}
		}

		return result;
	}
	
	private void newSwitchingWorkspaceSplash() {
		if (winSwitchingWorkspace != null) {
			return;
		}
		
		winSwitchingWorkspace = new JWindow(this);
		winSwitchingWorkspace.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		JLabel lab = new JLabel(localeDelegate.getMessage("MainFrame.4", "Arbeitsumgebung wird gewechselt") + "...");
		lab.setForeground(Color.WHITE);
		lab.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
		lab.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		
		JPanel jpnSwitchingWorkspace = new JPanel();
		jpnSwitchingWorkspace.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		jpnSwitchingWorkspace.setOpaque(true);
		jpnSwitchingWorkspace.setBackground(NuclosThemeSettings.BACKGROUND_COLOR3);
		
		TableLayoutBuilder tlbSwitchingWorkspace = new TableLayoutBuilder(jpnSwitchingWorkspace);
		tlbSwitchingWorkspace.columns(TableLayout.FILL)
		.newRow(TableLayout.FILL).add(new JLabel(Icons.getInstance().getIconSwitchWorkspace()), 1, TableLayout.CENTER, TableLayout.CENTER)
		.newRow().add(lab, 1, TableLayout.CENTER, TableLayout.CENTER);
		
		winSwitchingWorkspace.getContentPane().add(jpnSwitchingWorkspace, BorderLayout.CENTER);
		winSwitchingWorkspace.setSize(250, 250);
		winSwitchingWorkspace.setAlwaysOnTop(true);
		
		repositionSwitchingWorkspace();
		winSwitchingWorkspace.setVisible(true);
		
		jpnSwitchingWorkspace.paintImmediately(0, 0, jpnSwitchingWorkspace.getSize().width, jpnSwitchingWorkspace.getSize().height);
	}
	
	public void repositionSwitchingWorkspace() {
		if (winSwitchingWorkspace != null) {
			winSwitchingWorkspace.setLocationRelativeTo(this);
		}
	}
	
	public void showSwitchingWorkspace(boolean show) {
		if (show) {
			MainFrameTabbedPane.RESIZE_AND_ADJUST_IMMEDIATE = true;
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			componentMacPanel.setVisible(false);
			pnlDesktop.setVisible(false);
			newSwitchingWorkspaceSplash();
			JPanel contentpane = (JPanel) getContentPane();
			contentpane.revalidate();
			contentpane.paintImmediately(0,0,contentpane.getSize().width,contentpane.getSize().height);
			
		} else {			
			final JPanel contentpane = (JPanel) getContentPane();
			componentMacPanel.setVisible(true);
			pnlDesktop.setVisible(true);
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						LOG.error(e);
					}
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							contentpane.revalidate();
							contentpane.repaint();
							
							MainFrameTabbedPane.RESIZE_AND_ADJUST_IMMEDIATE = false;
							if (winSwitchingWorkspace != null) {
								winSwitchingWorkspace.dispose();
								winSwitchingWorkspace = null;
							}
						}
					});
				}
			}, "MainFrame.showSwitchingWorkspace.invokeLater");
			t.start();
		}
	}
	
	public static void setApplicationIconBadge(String text) {
		if (Main.getInstance().isMacOSX() && Main.isMacOSXSnowLeopardOrBetter()) {
			try {
				Class<?> macAppClass = Class.forName("com.apple.eawt.Application");
				Object macAppObject = macAppClass.getConstructor().newInstance();
				Method macAppSetAboutHandlerMethod = macAppClass.getDeclaredMethod("setDockIconBadge", new Class[] { String.class });
				macAppSetAboutHandlerMethod.invoke(macAppObject, new Object[] { text });
			} catch (Exception ex) {
				LOG.error(String.format("setApplicationIconBadge(%s): error=%s", text, ex.getMessage()), ex);
			}
		}
	}

}	// class MainFrame
