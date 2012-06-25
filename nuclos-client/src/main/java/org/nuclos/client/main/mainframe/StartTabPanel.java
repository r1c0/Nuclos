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

import static org.nuclos.client.main.mainframe.MainFrameUtils.isActionSelected;
import static org.nuclos.client.main.mainframe.MainFrameUtils.setActionSelected;
import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.common.EntityUtils;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.NuclosCollectController;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.common.NuclosDropTarget;
import org.nuclos.client.common.NuclosDropTargetVisitor;
import org.nuclos.client.common.OneDropNuclosDropTargetListener;
import org.nuclos.client.main.GenericAction;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.MainController;
import org.nuclos.client.main.mainframe.desktop.DesktopBackgroundPainter;
import org.nuclos.client.main.mainframe.desktop.DesktopListener;
import org.nuclos.client.main.mainframe.desktop.DesktopStartTab;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.MainFrameTabAdapter;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.WrapLayout;
import org.nuclos.client.ui.util.TableLayoutBuilder;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.WorkspaceDescription;
import org.nuclos.common.WorkspaceDescription.Desktop;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.collection.multimap.MultiListHashMap;
import org.nuclos.common.collection.multimap.MultiListMap;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

@Configurable
public class StartTabPanel extends JPanel implements NuclosDropTargetVisitor {

	private static final Logger log = Logger.getLogger(StartTabPanel.class);

	public final static int CENTER_COLUMNS_PREFFERED_WIDTH = 200;

	private final MainFrameTabbedPane tabbedPane;
	
	
	private boolean desktopActive;
	
	private final List<DesktopListener> desktopListener = new ArrayList<DesktopListener>();

	/**
	 * NORTH
	 */
	private final JPanel jpnHiddenTabs = new JPanel(new WrapLayout(WrapLayout.LEFT, 3, 3));

	/**
	 * CENTER
	 */
	private final JPanel jpnCenter = new JPanel(new TableLayout(new double[]{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL}, new double[]{TableLayout.FILL}));
	private final JPanel jpnStartmenu = new JPanel();
	private final JPanel jpnHistory = new JPanel();
	private final JPanel jpnBookmark = new JPanel();
	private final TableLayoutBuilder tlbStartmenu = (new TableLayoutBuilder(jpnStartmenu)).columns(TableLayout.FILL);
	private final TableLayoutBuilder tlbHistory = (new TableLayoutBuilder(jpnHistory)).columns(TableLayout.FILL);
	private final TableLayoutBuilder tlbBookmark = (new TableLayoutBuilder(jpnBookmark)).columns(TableLayout.FILL);
	private boolean isStartmenuShown = true;
	private boolean isHistoryShown = true;
	private boolean isBookmarkShown = true;
	private final DesktopStartTab desktop = new DesktopStartTab() {
		@Override
		public void hide() {
			setDesktopActive(false);
			fireDesktopToolBarChange(true);
		}
		@Override
		public void showToolBar(boolean show) {
			if (isDesktopActive()) {
				fireDesktopToolBarChange(show);
			}
		}
		@Override
		public void showTabBar(boolean show) {
			if (isDesktopActive()) {
				fireDesktopTabBarChange(show);
			}
		}
		@Override
		public void desktopBackgroundChanged(DesktopBackgroundPainter painter) {
			if (isDesktopActive()) {
				fireDesktopBackgroundChange(painter);
			}
		}
	};

	/**
	 * CONTENTS
	 */
	private final List<MainFrameTab> hiddenTabs = new ArrayList<MainFrameTab>();
	private final Map<MainFrameTab, LinkLabel> hiddenTabLinks = new HashMap<MainFrameTab, LinkLabel>();
	private final Map<String, LinkLabel> startmenuEntries = new HashMap<String, LinkLabel>();

	/**
	 * FUNCTIONS
	 */
	private Action actionNeverHideStartmenu;
	private Action actionNeverHideHistory;
	private Action actionNeverHideBookmark;
	private Action actionAlwaysHideStartmenu;
	private Action actionAlwaysHideHistory;
	private Action actionAlwaysHideBookmark;
	private Action actionShowEntity;
	private Action actionShowAdministration;
	private Action actionShowConfiguration;
	private Action actionClearHistory;
	private Action actionClearBookmark;
	private Action actionActivateDesktop;
	private Action actionRemoveSplitPaneFixations;

	private boolean showEntityStartmenuEntries = true;
	private boolean showAdministrationStartmenuEntries = false;
	private boolean showConfigurationStartmenuEntries = false;

	private Set<String> reducedStartmenus = new HashSet<String>();
	private final Map<String, ExpandOrReduceAction> startmenuExpandOrReduceActions = new HashMap<String, StartTabPanel.ExpandOrReduceAction>();

	private Set<String> reducedHistoryEntities = new HashSet<String>();
	private final Map<String, ExpandOrReduceAction> historyExpandOrReduceActions = new HashMap<String, StartTabPanel.ExpandOrReduceAction>();

	private Set<String> reducedBookmarkEntities = new HashSet<String>();
	private final Map<String, ExpandOrReduceAction> bookmarkExpandOrReduceActions = new HashMap<String, StartTabPanel.ExpandOrReduceAction>();
	
	private boolean initiating;
	
	private SpringLocaleDelegate localeDelegate;
	
	private MainFrame mainFrame;

	/**
	 *
	 * @param tabbedPane
	 */
	public StartTabPanel(MainFrameTabbedPane tabbedPane) {
		super(new BorderLayout(10, 10));
		this.tabbedPane = tabbedPane;
	}
	
	@PostConstruct
	void init() {
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setOpaque(false);

		jpnHiddenTabs.setOpaque(false);
		jpnCenter.setOpaque(false);
		jpnStartmenu.setOpaque(false);
		jpnHistory.setOpaque(false);
		jpnBookmark.setOpaque(false);

		setupActions();

		jpnHiddenTabs.setBorder(BorderFactory.createTitledBorder(localeDelegate.getMessage(
				"StartTabPanel.1","Ausgeblendete Tabs")));

		jpnCenter.add(createTitledScrollPane(localeDelegate.getMessage(
				"StartTabPanel.11","Startmenu"), jpnStartmenu), "0,0");
		jpnCenter.add(createTitledScrollPane(localeDelegate.getMessage(
				"StartTabPanel.12","Zuletzt angesehen"), jpnHistory), "1,0");
		jpnCenter.add(createTitledScrollPane(localeDelegate.getMessage(
				"StartTabPanel.13","Lesezeichen"), jpnBookmark), "2,0");

		add(jpnHiddenTabs, BorderLayout.NORTH);
		add(jpnCenter, BorderLayout.CENTER);

		setupStartmenu();
		refreshHistory();
		refreshBookmark();

		adjustCenter();
		addComponentListener(createResizeListener());

		updateControlOverview();
	}
	
	@Autowired
	void setMainFrame(@Value("#{mainFrameSpringComponent.mainFrame}") MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}
	
	@Autowired
	void setSpringLocaleDelegate(SpringLocaleDelegate cld) {
		this.localeDelegate = cld;
	}

	/**
	 *
	 * @param title
	 * @param jpnContent
	 * @return
	 */
	private JPanel createTitledScrollPane(String title, JPanel jpnContent) {
		final JPanel result = new JPanel(new BorderLayout());
		result.setOpaque(false);
		result.setBorder(BorderFactory.createTitledBorder(title));

		final JScrollPane scrollPane = new JScrollPane(jpnContent, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getVerticalScrollBar().setUnitIncrement(15);

		result.add(scrollPane, BorderLayout.CENTER);
		return result;
	}

	/**
	 *
	 */
	private void setupActions() {
		actionNeverHideStartmenu =  new AbstractAction(localeDelegate.getMessage("StartTabPanel.2","Immer anzeigen")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (transferSelected(e, actionNeverHideStartmenu))
					actionAlwaysHideStartmenu.putValue(Action.SELECTED_KEY, false);
				adjustCenter();
			}
		};
		actionNeverHideHistory = new AbstractAction(localeDelegate.getMessage("StartTabPanel.2","Immer anzeigen")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (transferSelected(e, actionNeverHideHistory))
					actionAlwaysHideHistory.putValue(Action.SELECTED_KEY, false);
				adjustCenter();
			}
		};
		actionNeverHideBookmark = new AbstractAction(localeDelegate.getMessage("StartTabPanel.2","Immer anzeigen")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (transferSelected(e, actionNeverHideBookmark))
					actionAlwaysHideBookmark.putValue(Action.SELECTED_KEY, false);
				adjustCenter();
			}
		};
		actionAlwaysHideStartmenu =  new AbstractAction(localeDelegate.getMessage("StartTabPanel.16","Niemals anzeigen")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (transferSelected(e, actionAlwaysHideStartmenu))
					actionNeverHideStartmenu.putValue(Action.SELECTED_KEY, false);
				adjustCenter();
			}
		};
		actionAlwaysHideHistory = new AbstractAction(localeDelegate.getMessage("StartTabPanel.16","Niemals anzeigen")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (transferSelected(e, actionAlwaysHideHistory))
					actionNeverHideHistory.putValue(Action.SELECTED_KEY, false);
				adjustCenter();
			}
		};
		actionAlwaysHideBookmark = new AbstractAction(localeDelegate.getMessage("StartTabPanel.16","Niemals anzeigen")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (transferSelected(e, actionAlwaysHideBookmark))
					actionNeverHideBookmark.putValue(Action.SELECTED_KEY, false);
				adjustCenter();
			}
		};
		actionShowAdministration = new AbstractAction(localeDelegate.getMessage("StartTabPanel.3","Administration")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean selected = transferSelected(e, actionShowAdministration);
				setShowAdministration(selected);
			}
		};
		actionShowEntity = new AbstractAction(localeDelegate.getMessage("StartTabPanel.14","Entitäten")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean selected = transferSelected(e, actionShowEntity);
				setShowEntity(selected);
			}
		};
		actionShowConfiguration = new AbstractAction(localeDelegate.getMessage("StartTabPanel.15","Konfiguration")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean selected = transferSelected(e, actionShowConfiguration);
				setShowConfiguration(selected);
			}
		};
		actionClearHistory = new AbstractAction(localeDelegate.getMessage("StartTabPanel.4","Alle Eintraege entfernen"), 
				Icons.getInstance().getIconRealDelete16()) {

			@Override
			public void actionPerformed(ActionEvent e) {
				MainFrame.clearHistory();
			}
		};
		actionClearBookmark = new AbstractAction(localeDelegate.getMessage("StartTabPanel.5","Alle Lesezeichen entfernen"), 
				Icons.getInstance().getIconRealDelete16()) {

			@Override
			public void actionPerformed(ActionEvent e) {
				MainFrame.clearBookmark();
			}
		};
		actionActivateDesktop = new AbstractAction(localeDelegate.getMessage("StartTabPanel.17","Desktop anzeigen")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				setDesktopActive(true);
			}
		};
		actionRemoveSplitPaneFixations = new AbstractAction(
				localeDelegate.getMessage("DesktopStartTab.7", "Bereichsfixierungen aufheben")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				StartTabPanel.removeSplitPaneFixations(StartTabPanel.this);
			}
		};
	}

	/**
	 *
	 * @param ev
	 * @param ac
	 * @return
	 */
	private boolean transferSelected(ActionEvent ev, Action ac) {
		boolean selected = false;
		if (ev.getSource() instanceof AbstractButton) {
			selected = ((AbstractButton) ev.getSource()).isSelected();
		} else if (ev.getSource() instanceof LinkLabel) {
			selected = ((LinkLabel) ev.getSource()).isSelected();
		}
		ac.putValue(Action.SELECTED_KEY, selected);
		return selected;
	}

	/**
	 *
	 * @return
	 */
	public Action getShowStartmenuAction() {
		return actionNeverHideStartmenu;
	}

	/**
	 *
	 * @return
	 */
	public Action getShowHistoryAction() {
		return actionNeverHideHistory;
	}

	/**
	 *
	 * @return
	 */
	public Action getShowBookmarkAction() {
		return actionNeverHideBookmark;
	}

	/**
	 *
	 * @return
	 */
	public Action getAlwaysHideStartmenuAction() {
		return actionAlwaysHideStartmenu;
	}

	/**
	 *
	 * @return
	 */
	public Action getAlwaysHideHistoryAction() {
		return actionAlwaysHideHistory;
	}

	/**
	 *
	 * @return
	 */
	public Action getAlwaysHideBookmarkAction() {
		return actionAlwaysHideBookmark;
	}

	/**
	 *
	 * @return
	 */
	public Action getShowAdministration() {
		return actionShowAdministration;
	}

	/**
	 *
	 * @return
	 */
	public Action getShowEntity() {
		return actionShowEntity;
	}

	/**
	 *
	 * @return
	 */
	public Action getShowConfiguration() {
		return actionShowConfiguration;
	}

	/**
	 *
	 * @return
	 */
	public Action getClearHistoryAction() {
		return actionClearHistory;
	}

	/**
	 *
	 * @return
	 */
	public Action getClearBookmarkAction() {
		return actionClearBookmark;
	}
	
	/**
	 * 
	 * @return
	 */
	public Action getActivateDesktopAction() {
		return actionActivateDesktop;
	}
	
	/**
	 * 
	 * @return
	 */
	public Action getRemoveSplitPaneFixationsAction() {
		return actionRemoveSplitPaneFixations;
	}
	
	/**
	 * 
	 */
	public static void removeSplitPaneFixations(Component comp) {
		for (MainFrameSplitPane splitpane : getParentSplitPanes(comp)) {
			splitpane.setFixedState(WorkspaceDescription.Split.FIXED_STATE_NONE);
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public static List<MainFrameSplitPane> getParentSplitPanes(Component comp) {
		List<MainFrameSplitPane> result = new ArrayList<MainFrameSplitPane>();
		addParentSplitPanes(result, comp);
		return result;
	}
	
	/**
	 * 
	 * @param list
	 * @param parent
	 */
	public static void addParentSplitPanes(List<MainFrameSplitPane> list, Component parent) {
		if (parent == null) {
			return;
		}
		
		if (parent instanceof MainFrameSplitPane) {
			list.add((MainFrameSplitPane) parent);
		}
		addParentSplitPanes(list, parent.getParent());
	}

	/**
	 *
	 * @param index
	 * @return
	 */
	public Action createSelectHistorySize(final int index) {
		Action result = new AbstractAction(localeDelegate.getMessage("StartTabPanel.6","Merke {0} Eintraege",MainFrame.HISTORY_SIZES[index])) {

			@Override
			public void actionPerformed(ActionEvent e) {
				JRadioButtonMenuItem radmi = (JRadioButtonMenuItem) e.getSource();
				if (radmi.isSelected()) {
					MainFrame.setSelectedHistorySize(index);
				}
			}
		};
		return result;
	}

	/**
	 *
	 */
	private void adjustCenter() {
		int columnPrefferedCount = getWidth() / CENTER_COLUMNS_PREFFERED_WIDTH;
		if (columnPrefferedCount == 0) columnPrefferedCount = 1;

		int columnShowingCount = (isStartmenuShown?1:0) + (isHistoryShown?1:0) + (isBookmarkShown?1:0);

		if (!isActionSelected(getAlwaysHideStartmenuAction()) && isActionSelected(getShowStartmenuAction()) && !isStartmenuShown) {
			setShowStartmenu(true);
			columnShowingCount++;
		}
		if (!isActionSelected(getAlwaysHideHistoryAction()) && isActionSelected(getShowHistoryAction()) && !isHistoryShown) {
			setShowHistory(true);
			columnShowingCount++;
		}
		if (!isActionSelected(getAlwaysHideBookmarkAction()) && isActionSelected(getShowBookmarkAction()) && !isBookmarkShown) {
			setShowBookmark(true);
			columnShowingCount++;
		}

		/**
		 * hide columns always
		 */
		if (isActionSelected(getAlwaysHideBookmarkAction()) && isBookmarkShown) {
			setShowBookmark(false);
			columnShowingCount--;
		}
		if (isActionSelected(getAlwaysHideHistoryAction()) && isHistoryShown) {
			setShowHistory(false);
			columnShowingCount--;
		}
		if (isActionSelected(getAlwaysHideStartmenuAction()) && isStartmenuShown) {
			setShowStartmenu(false);
			columnShowingCount--;
		}

		/**
		 * show more columns
		 */
		if (!isActionSelected(getAlwaysHideStartmenuAction()) && columnPrefferedCount > columnShowingCount && !isStartmenuShown) {
			setShowStartmenu(true);
			columnShowingCount++;
		}
		if (!isActionSelected(getAlwaysHideHistoryAction()) && columnPrefferedCount > columnShowingCount && !isHistoryShown) {
			setShowHistory(true);
			columnShowingCount++;
		}
		if (!isActionSelected(getAlwaysHideBookmarkAction()) && columnPrefferedCount > columnShowingCount && !isBookmarkShown) {
			setShowBookmark(true);
			columnShowingCount++;
		}

		/**
		 * hide coulmns if avaiable
		 */
		if (!isActionSelected(getAlwaysHideBookmarkAction()) && columnPrefferedCount < columnShowingCount &&
			isBookmarkShown && !isActionSelected(getShowBookmarkAction())) {
			setShowBookmark(false);
			columnShowingCount--;
		}
		if (!isActionSelected(getAlwaysHideHistoryAction()) && columnPrefferedCount < columnShowingCount &&
			isHistoryShown && !isActionSelected(getShowHistoryAction())) {
			setShowHistory(false);
			columnShowingCount--;
		}
		if (!isActionSelected(getAlwaysHideStartmenuAction()) && columnPrefferedCount < columnShowingCount &&
			isStartmenuShown && !isActionSelected(getShowStartmenuAction())) {
			setShowStartmenu(false);
			columnShowingCount--;
		}

	}

	/**
	 *
	 * @param show
	 */
	private void setShowStartmenu(boolean show) {
		((TableLayout)jpnCenter.getLayout()).setColumn(0, show? TableLayout.FILL : 0d);
		jpnCenter.revalidate();
		isStartmenuShown = show;
	}

	/**
	 *
	 * @param show
	 */
	private void setShowHistory(boolean show) {
		((TableLayout)jpnCenter.getLayout()).setColumn(1, show? TableLayout.FILL : 0d);
		jpnCenter.revalidate();
		isHistoryShown = show;
	}

	/**
	 *
	 * @param show
	 */
	private void setShowBookmark(boolean show) {
		((TableLayout)jpnCenter.getLayout()).setColumn(2, show? TableLayout.FILL : 0d);
		jpnCenter.revalidate();
		isBookmarkShown = show;
	}

	/**
	 *
	 * @return
	 */
	protected ComponentListener createResizeListener() {
		return new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				adjustCenter();
			}
		};
	}

	/**
	 *
	 * @param menu
	 * @return
	 */
	private String getStartmenuLabel(String[] menu) {
		return StringUtils.join("/", menu);
	}

	/**
	 *
	 */
	void refreshHistory() {
		jpnHistory.removeAll();
		for (final Entry<String, String> entity : sortEntities(MainFrame.getHistory().keySet()).entrySet()) {
			final ExpandOrReduceAction headlineAction = new ExpandOrReduceAction(reducedHistoryEntities, entity.getKey(), 
					MainFrame.resizeAndCacheLinkIcon(mainFrame.getEntityIcon(entity.getValue())));
			historyExpandOrReduceActions.put(entity.getKey(), headlineAction);

			final Action openAll = new AbstractAction(localeDelegate.getText("ExplorerController.22"), 
					MainFrame.resizeAndCacheLinkIcon(Icons.getInstance().getIconShowList())) {
				@Override
				public void actionPerformed(ActionEvent e) {
					List<EntityBookmark> history = MainFrame.getHistory().getValues(entity.getValue());
					viewList(entity.getValue(), CollectionUtils.transform(history, new Transformer<EntityBookmark, Object>() {
						@Override
						public Object transform(EntityBookmark i) {
							return i.getId();
						}
					}));
				}
			};

			LinkLabel ll = new LinkLabel(headlineAction, true) {
				@Override
				protected List<JMenuItem> getContextMenuItems() {
					JMenuItem miOpenAll = new JMenuItem(openAll);
					return Collections.singletonList(miOpenAll);
				}
			};

			tlbHistory.newRow().add(ll);

			for (EntityBookmark eb : sortEntityBookmark(MainFrame.getHistory().getValues(entity.getValue()))) {
				LinkLabel historyEntry = createHistoryEntry(eb);
				tlbHistory.newRow().add(historyEntry);
				headlineAction.addLinkLabel(historyEntry);
				setupDragDrop(historyEntry, eb);
			}
		}
		jpnHistory.revalidate();
		jpnHistory.repaint();
	}

	protected void setupDragDrop(final LinkLabel ll, final EntityBookmark eb) {
		OneDropNuclosDropTargetListener listener = new OneDropNuclosDropTargetListener(this, ClientParameterProvider.getInstance().getIntValue(ParameterProvider.KEY_DRAG_CURSOR_HOLDING_TIME, 600));
		NuclosDropTarget drop = new NuclosDropTarget(ll, listener, eb);
		drop.setActive(true);
	}

	/**
	 *
	 */
	void refreshBookmark() {
		jpnBookmark.removeAll();
		for (final Entry<String, String> entity : sortEntities(MainFrame.getBookmark().keySet()).entrySet()) {
			final ExpandOrReduceAction headlineAction = new ExpandOrReduceAction(reducedBookmarkEntities, entity.getKey(), 
					MainFrame.resizeAndCacheLinkIcon(mainFrame.getEntityIcon(entity.getValue())));
			bookmarkExpandOrReduceActions.put(entity.getKey(), headlineAction);

			final Action openAll = new AbstractAction(localeDelegate.getText("ExplorerController.22"), 
					MainFrame.resizeAndCacheLinkIcon(Icons.getInstance().getIconShowList())) {
				@Override
				public void actionPerformed(ActionEvent e) {
					List<EntityBookmark> bookmarks = MainFrame.getBookmark().getValues(entity.getValue());
					viewList(entity.getValue(), CollectionUtils.transform(bookmarks, new Transformer<EntityBookmark, Object>() {
						@Override
						public Object transform(EntityBookmark i) {
							return i.getId();
						}
					}));
				}
			};

			LinkLabel ll = new LinkLabel(headlineAction, true) {
				@Override
				protected List<JMenuItem> getContextMenuItems() {
					JMenuItem miOpenAll = new JMenuItem(openAll);
					return Collections.singletonList(miOpenAll);
				}
			};

			//tlbBookmark.newRow().add(createHeadline(entity.getKey(), MainFrame.resizeAndCacheLinkIcon(MainFrame.getEntityIcon(entity.getValue()))));
			tlbBookmark.newRow().add(ll);

			for (EntityBookmark eb : sortEntityBookmark(MainFrame.getBookmark().getValues(entity.getValue()))) {
				LinkLabel bookmarkEntry = createBookmarkEntry(eb);
				headlineAction.addLinkLabel(bookmarkEntry);
				tlbBookmark.newRow().add(bookmarkEntry);
				setupDragDrop(bookmarkEntry, eb);
			}
		}
		jpnBookmark.revalidate();
		jpnBookmark.repaint();
	}

	/**
	 *
	 * @param entities
	 * @return
	 */
	private SortedMap<String, String> sortEntities(Set<String> entities) {
		SortedMap<String, String> result = new TreeMap<String, String>();
		for (String entity : entities) {
			try {
				result.put(localeDelegate.getLabelFromMetaDataVO(MetaDataClientProvider.getInstance().getEntity(entity)), entity);
			} catch (Exception e) {
				// ignore: Entity removed
			}
		}
		return result;
	}

	/**
	 *
	 * @param ebs
	 * @return
	 */
	private List<EntityBookmark> sortEntityBookmark(List<EntityBookmark> ebs) {
		List<EntityBookmark> result = CollectionUtils.sorted(ebs, new Comparator<EntityBookmark>() {
			@Override
			public int compare(EntityBookmark o1, EntityBookmark o2) {
				return LangUtils.compare(o1.getLabel(), o2.getLabel());
			}});
		return result;
	}

	/**
	 *
	 */
	void setupStartmenu() {
		if (initiating) {
			return;
		}
		
		UIUtils.runCommand(StartTabPanel.this, new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				final MainController mc = Main.getInstance().getMainController();
				String mainMenuAdministration = mc.getMainMenuAdministration();
				String mainMenuConfiguration = mc.getMainMenuConfiguration();

				List<Pair<String[], Action>> startmenuActionsAll = new ArrayList<Pair<String[], Action>>();
				List<Pair<String[], Action>> startmenuActionsGeneric = new ArrayList<Pair<String[], Action>>();
				List<Pair<String[], Action>> startmenuActionsStatic = new ArrayList<Pair<String[], Action>>();

				startmenuActionsStatic.addAll(mc.getAdministrationMenuActions());
				startmenuActionsStatic.addAll(mc.getConfigurationMenuActions());

				startmenuActionsGeneric.addAll(mc.getEntityMenuActions());
				startmenuActionsGeneric.addAll(mc.getCustomComponentMenuActions());
				startmenuActionsGeneric.addAll(mc.getNucletComponentMenuActions());

				startmenuActionsAll.addAll(startmenuActionsStatic);
				startmenuActionsAll.addAll(startmenuActionsGeneric);


				startmenuEntries.clear();
				jpnStartmenu.removeAll();

				List<String[]> listAllMenus = new ArrayList<String[]>();

				for (Pair<String[], Action> startmenuAction : startmenuActionsAll) {
					listAllMenus.add(startmenuAction.x);
				}

				List<String[]> listMenusSorted = CollectionUtils.sorted(listAllMenus, new Comparator<String[]>(){
					@Override
					public int compare(String[] o1, String[] o2) {
						int depth = o1.length > o2.length ? o2.length : o1.length;

						for (int i = 0; i < depth; i++) {
							int result = LangUtils.compare(o1[i], o2[i]);
							if (result != 0) {
								return result;
							}
						}

						return o1.length - o2.length;
					}
				});

				List<String> listMenusSortedDistinctAll = new ArrayList<String>();
				List<String> listMenusSortedDistinctAdministration = new ArrayList<String>();
				List<String> listMenusSortedDistinctConfiguration = new ArrayList<String>();

				for (String[] menu : listMenusSorted) {
					String menuString = getStartmenuLabel(menu);
					if (!listMenusSortedDistinctAll.contains(menuString) &&
						!listMenusSortedDistinctAdministration.contains(menuString) &&
						!listMenusSortedDistinctConfiguration.contains(menuString)) {

						if (menu.length > 0) {
							if (mainMenuAdministration.equalsIgnoreCase(menu[0])) {
								if (showAdministrationStartmenuEntries) {
									listMenusSortedDistinctAdministration.add(menuString);
									log.trace(menuString);
								}
							} else if (mainMenuConfiguration.equalsIgnoreCase(menu[0])) {
								if (showConfigurationStartmenuEntries) {
									listMenusSortedDistinctConfiguration.add(menuString);
									log.trace(menuString);
								}
							} else {
								if (showEntityStartmenuEntries) {
									listMenusSortedDistinctAll.add(menuString);
									log.trace(menuString);
								}
							}
						}
					}
				}

				listMenusSortedDistinctAll.addAll(0, listMenusSortedDistinctConfiguration);
				listMenusSortedDistinctAll.addAll(0, listMenusSortedDistinctAdministration);

				MultiListMap<String, Action> mapStartmenuActionsAll = new MultiListHashMap<String, Action>();
				MultiListMap<String, Action> mapStartmenuActionsStatic = new MultiListHashMap<String, Action>();
				MultiListMap<String, Action> mapStartmenuActionsGeneric = new MultiListHashMap<String, Action>();

				for (Pair<String[], Action> startmenuAction : startmenuActionsStatic) {
					String menuString = getStartmenuLabel(startmenuAction.x);
					mapStartmenuActionsAll.addValue(menuString, startmenuAction.y);
					mapStartmenuActionsStatic.addValue(menuString, startmenuAction.y);
				}
				for (Pair<String[], Action> startmenuAction : startmenuActionsGeneric) {
					String menuString = getStartmenuLabel(startmenuAction.x);
					mapStartmenuActionsAll.addValue(menuString, startmenuAction.y);
					mapStartmenuActionsGeneric.addValue(menuString, startmenuAction.y);
				}

				for (String menu : listMenusSortedDistinctAll) {
					if (mapStartmenuActionsAll.getValues(menu).isEmpty())
						continue;

					ExpandOrReduceAction actionExpOrRed = new ExpandOrReduceAction(reducedStartmenus, menu);
					startmenuExpandOrReduceActions.put(menu, actionExpOrRed);
					// do not add disabled and reduced headlines (asigned workspace without rights to customize)
					if (actionExpOrRed.isEnabled() || isActionSelected(actionExpOrRed))
						tlbStartmenu.newRow().add(createHeadline(actionExpOrRed));

					// add static items unsorted
					for (Action action : mapStartmenuActionsStatic.getValues(menu)) {
						LinkLabel startmenuEntry = createStarmenuEntry(action);
						tlbStartmenu.newRow().add(startmenuEntry);
						actionExpOrRed.addLinkLabel(startmenuEntry);
					}

					// add generic items sorted
					List<Action> listActionsSorted = CollectionUtils.sorted(mapStartmenuActionsGeneric.getValues(menu), new Comparator<Action>(){
						@Override
						public int compare(Action o1, Action o2) {
							return LangUtils.compare(o1.getValue(Action.NAME), o2.getValue(Action.NAME));
						}
					});
					for (Action action : listActionsSorted) {
						LinkLabel startmenuEntry = createStarmenuEntry(action);
						tlbStartmenu.newRow().add(startmenuEntry);
						actionExpOrRed.addLinkLabel(startmenuEntry);
					}
				}

				jpnStartmenu.revalidate();
				jpnStartmenu.repaint();
			}
		});

	}

	/**
	 *
	 * @param title
	 * @param ico
	 * @return
	 */
	protected JComponent createHeadline(String title, Icon ico) {
		JLabel result = new JLabel("<html><b>"+title+"</b></html>", ico, JLabel.LEFT);
		result.setBorder(BorderFactory.createEmptyBorder(5, 0, 2, 0));
		return result;
	}

	private LinkLabel createHeadline(Action action) {
		return new LinkLabel(action, true){

			@Override
			protected List<JMenuItem> getContextMenuItems() {
				return null;
			}};
	}

	/**
	 *
	 * @return
	 */
	boolean isNeverHideStartmenu() {
		return isActionSelected(actionNeverHideStartmenu);
	}

	/**
	 *
	 * @param neverHideStartmenu
	 */
	void setNeverHideStartmenu(boolean neverHideStartmenu) {
		setActionSelected(actionNeverHideStartmenu, neverHideStartmenu);
		adjustCenter();
	}

	/**
	 *
	 * @return
	 */
	boolean isNeverHideHistory() {
		return isActionSelected(actionNeverHideHistory);
	}

	/**
	 *
	 * @param neverHideHistory
	 */
	void setNeverHideHistory(boolean neverHideHistory) {
		setActionSelected(actionNeverHideHistory, neverHideHistory);
		adjustCenter();
	}

	/**
	 *
	 * @return
	 */
	boolean isNeverHideBookmark() {
		return isActionSelected(actionNeverHideBookmark);
	}

	/**
	 *
	 * @param neverHideBookmark
	 */
	void setNeverHideBookmark(boolean neverHideBookmark) {
		setActionSelected(actionNeverHideBookmark, neverHideBookmark);
		adjustCenter();
	}

	/**
	 *
	 * @return
	 */
	boolean isAlwaysHideStartmenu() {
		return isActionSelected(actionAlwaysHideStartmenu);
	}

	/**
	 *
	 * @param alwaysHideStartmenu
	 */
	void setAlwaysHideStartmenu(boolean alwaysHideStartmenu) {
		setActionSelected(actionAlwaysHideStartmenu, alwaysHideStartmenu);
		adjustCenter();
	}

	/**
	 *
	 * @return
	 */
	boolean isAlwaysHideHistory() {
		return isActionSelected(actionAlwaysHideHistory);
	}

	/**
	 *
	 * @param alwaysHideHistory
	 */
	void setAlwaysHideHistory(boolean alwaysHideHistory) {
		setActionSelected(actionAlwaysHideHistory, alwaysHideHistory);
		adjustCenter();
	}

	/**
	 *
	 * @return
	 */
	boolean isAlwaysHideBookmark() {
		return isActionSelected(actionAlwaysHideBookmark);
	}

	/**
	 *
	 * @param alwaysHideBookmark
	 */
	void setAlwaysHideBookmark(boolean alwaysHideBookmark) {
		setActionSelected(actionAlwaysHideBookmark, alwaysHideBookmark);
		adjustCenter();
	}

	/**
	 *
	 * @return
	 */
	boolean isShowAdministration() {
		return showAdministrationStartmenuEntries;
	}

	/**
	 *
	 * @return
	 */
	boolean isShowEntity() {
		return showEntityStartmenuEntries;
	}

	/**
	 *
	 * @return
	 */
	boolean isShowConfiguration() {
		return showConfigurationStartmenuEntries;
	}

	/**
	 *
	 * @param showAdministration
	 */
	void setShowAdministration(boolean showAdministration) {
		this.showAdministrationStartmenuEntries = showAdministration;
		setActionSelected(actionShowAdministration, showAdministration);
		setupStartmenu();
	}

	/**
	 *
	 * @param showEntity
	 */
	void setShowEntity(boolean showEntity) {
		this.showEntityStartmenuEntries = showEntity;
		setActionSelected(actionShowEntity, showEntity);
		setupStartmenu();
	}

	/**
	 *
	 * @param showConfiguration
	 */
	void setShowConfiguration(boolean showConfiguration) {
		this.showConfigurationStartmenuEntries = showConfiguration;
		setActionSelected(actionShowConfiguration, showConfiguration);
		setupStartmenu();
	}

	/**
	 *
	 * @param entity
	 * @param marker
	 */
	void setStartmenuEntryMarker(String entity, LinkMarker marker) {
		if (startmenuEntries.containsKey(entity)) {
			startmenuEntries.get(entity).setMarker(marker);
		}
	}

	/**
	 *
	 * @param eb
	 * @return
	 */
	private LinkLabel createBookmarkEntry(final EntityBookmark eb) {
		Action act = new AbstractAction(eb.getLabel()) {

			@Override
			public void actionPerformed(ActionEvent e) {
				tabbedPane.newNuclosTab(eb);
			}
		};

		act.putValue(Action.SELECTED_KEY, true);

		LinkLabel result = new LinkLabel(act) {
			@Override
			protected List<JMenuItem> getContextMenuItems() {
				List<JMenuItem> result = new ArrayList<JMenuItem>();
				result.add(new JMenuItem(new AbstractAction(localeDelegate.getMessage("StartTabPanel.7","Lesezeichen entfernen"), 
						Icons.getInstance().getIconDelete16()){

					@Override
					public void actionPerformed(ActionEvent e) {
						MainFrame.getBookmark().removeValue(eb.getEntity(), eb);
						MainFrame.refreshBookmark();
					}
				}));
				return result;
			}

		};

		return result;
	}

	/**
	 *
	 * @param eb
	 * @return
	 */
	private LinkLabel createHistoryEntry(final EntityBookmark eb) {
		Action act = new AbstractAction(eb.getLabel()) {

			@Override
			public void actionPerformed(ActionEvent e) {
				tabbedPane.newNuclosTab(eb);
			}
		};

		act.putValue(Action.SELECTED_KEY, true);

		LinkLabel result = new LinkLabel(act) {

			@Override
			protected List<JMenuItem> getContextMenuItems() {
				List<JMenuItem> result = new ArrayList<JMenuItem>();
				result.add(new JMenuItem(new AbstractAction(localeDelegate.getMessage("StartTabPanel.8","Lesezeichen setzen"), 
						Icons.getInstance().getIconBookmark16()){

					@Override
					public void actionPerformed(ActionEvent e) {
						MainFrame.addBookmark(eb.copy(), true);
					}
				}));
				result.add(new JMenuItem(new AbstractAction(localeDelegate.getMessage("StartTabPanel.9","Eintrag entfernen"), 
						Icons.getInstance().getIconDelete16()){

					@Override
					public void actionPerformed(ActionEvent e) {
						MainFrame.getHistory().removeValue(eb.getEntity(), eb);
						MainFrame.refreshHistory();
					}
				}));
				return result;
			}

		};

		return result;
	}

	/**
	 *
	 * @param action
	 * @return
	 */
	private LinkLabel createStarmenuEntry(Action action) {
		final String command = (String) action.getValue(Action.ACTION_COMMAND_KEY);
		String label = (String) action.getValue(Action.NAME);
		if (label.endsWith("...")) label = label.substring(0, label.length()-3);
		action.putValue(Action.NAME, label);
		action.putValue(Action.SELECTED_KEY, true);

		LinkLabel result = new LinkLabel(action) {

			@Override
			protected List<JMenuItem> getContextMenuItems() {
				
				List<JMenuItem>result = new ArrayList<JMenuItem>();

				if (MainFrame.isStarttabEditable()) {
					JCheckBoxMenuItem cbmiOpenHere = new JCheckBoxMenuItem(new AbstractAction(localeDelegate.getMessage(
							"StartTabPanel.10","Immer hier oeffnen")) {

						@Override
						public void actionPerformed(ActionEvent e) {
							JCheckBoxMenuItem cbmiThis = (JCheckBoxMenuItem) e.getSource();
							boolean selected = cbmiThis.isSelected();
							setMarker(selected ? LinkMarker.ORANGE_HALF : LinkMarker.NONE);
							if (selected) {
								MainFrame.setPredefinedEntityOpenLocation(command, tabbedPane);
							} else {
								MainFrame.removePredefinedEntityOpenLocation(command, true);
							}
						}
					});
					cbmiOpenHere.setSelected(getMarker() == LinkMarker.ORANGE_HALF);
					result.add(cbmiOpenHere);
				}
				return result;
			}
		};

		if (MainFrame.getPredefinedEntityOpenLocations(tabbedPane).contains(command)) {
			result.setMarker(LinkMarker.ORANGE_HALF);
		} else if (MainFrame.getAllPredefinedEntityOpenLocations().contains(command)) {
			result.setMarker(LinkMarker.GRAY);
		}

		startmenuEntries.put(command, result);
		return result;
	}

	void addHiddenTab(final MainFrameTab tab) {
		hiddenTabs.add(tab);
		final LinkLabel restoreLink = new LinkLabel(new AbstractAction(tab.getTitle(), tab.getTabIcon()) {

			@Override
			public void actionPerformed(ActionEvent e) {
				tabbedPane.restoreHiddenTab(tab);
			}
		}) {
			@Override
			protected List<JMenuItem> getContextMenuItems() {
				return null;
			}

		};
		tab.addMainFrameTabListener(new MainFrameTabAdapter() {
			@Override
			public void tabTitleChanged(MainFrameTab tab) {
				restoreLink.setText(tab.getTitle());
				restoreLink.setIcon(MainFrame.resizeAndCacheLinkIcon(tab.getTabIcon()));
			}
			@Override
			public void tabRestoredFromHidden(MainFrameTab tab) {
				tab.removeMainFrameTabListener(this);
			}
		});
		hiddenTabLinks.put(tab, restoreLink);
		jpnHiddenTabs.add(restoreLink);

		updateControlOverview();
		tab.notifyHidden();
	}

	/**
	 *
	 * @param nuclosTab
	 * @param updateCO
	 */
	private void removeHiddenTab(MainFrameTab tab, boolean updateCO) {
		hiddenTabs.remove(tab);
		final LinkLabel hidden = hiddenTabLinks.get(tab);
		if (hidden != null) {
			jpnHiddenTabs.remove(hidden);
		}
		hiddenTabLinks.remove(tab);

		if (updateCO) {
			updateControlOverview();
		}
		tab.notifyRestoredFromHidden();
	}

	/**
	 *
	 * @param nuclosTab
	 */
	void removeHiddenTab(MainFrameTab nuclosTab) {
		removeHiddenTab(nuclosTab, true);
	}

	/**
	 *
	 * @param size
	 * @return list of CommonJInternalFrame (reversed order from end ... list_size=5 param_size=3 --> 5, 4, 3)
	 */
	public List<MainFrameTab> removeHiddenTabs(int size) {
		final List<MainFrameTab> result = new ArrayList<MainFrameTab>();
		final int removeUntil = hiddenTabs.size()>size ? hiddenTabs.size()-size : 0;
		for (int i = hiddenTabs.size()-1; i >= removeUntil; i--) {

			MainFrameTab hiddenTab = hiddenTabs.get(i);
			result.add(hiddenTab);

			removeHiddenTab(hiddenTab, false);
		}

		updateControlOverview();
		return result;
	}

	/**
	 *
	 */
	private void updateControlOverview() {
		jpnHiddenTabs.setVisible(hiddenTabs.size()>0);
	}

	/**
	 *
	 * @return
	 */
	int countHiddenTabs() {
		return hiddenTabs.size();
	}

	/**
	 *
	 * @return
	 */
	List<MainFrameTab> getHiddenTabs() {
		return new ArrayList<MainFrameTab>(hiddenTabs);
	}

	private void viewList(String entity, List<Object> ids) {
		try {
			NuclosCollectController<?> controller = NuclosCollectControllerFactory.getInstance().newCollectController(entity, null);
			controller.runViewResults(ids);
		}
		catch (CommonBusinessException ex) {
			Errors.getInstance().showExceptionDialog(this, "StartTabPanel.error.open.list", ex);
		}
	}

	@Override
	public void visitDragEnter(DropTargetDragEvent dtde) {}

	@Override
	public void visitDragExit(DropTargetEvent dte) {}

	@Override
	public void visitDragOver(DropTargetDragEvent dtde) {
		if(dtde.getSource() instanceof NuclosDropTarget) {
			NuclosDropTarget target = (NuclosDropTarget) dtde.getSource();
			EntityBookmark eb = (EntityBookmark) target.getUserObject();
			String entity = eb.getEntity();
			if(EntityUtils.hasEntityDocumentType(entity) || EntityUtils.hasAnySubformDocumentType(entity) || EntityUtils.hasEntityGeneralDocumentSubform(entity))
				tabbedPane.newNuclosTab(eb);
			else
				dtde.rejectDrag();
		}

	}

	@Override
	public void visitDrop(DropTargetDropEvent dtde) {}

	@Override
	public void visitDropActionChanged(DropTargetDragEvent dtde) {}

	/**
	 *
	 * @return
	 */
	public Set<String> getReducedStartmenus() {
		return reducedStartmenus;
	}

	public Set<String> getReducedHistoryEntities() {
		return reducedHistoryEntities;
	}

	public Set<String> getReducedBookmarkEntities() {
		return reducedBookmarkEntities;
	}

	/**
	 *
	 * @param reducedStartmenus
	 */
	public void setReducedStartmenus(Set<String> reducedStartmenus) {
		if (reducedStartmenus != null) {
			for (String reducedStartmenu : reducedStartmenus) {
				if (startmenuExpandOrReduceActions.containsKey(reducedStartmenu)) {
					ExpandOrReduceAction act = startmenuExpandOrReduceActions.get(reducedStartmenu);
					act.setSelected(false);
					this.reducedStartmenus.add(reducedStartmenu);
				}
			}
		}
	}

	public void setReducedHistoryEntities(Set<String> reducedHistoryEntities) {
		if (reducedHistoryEntities != null) {
			for (String reducedHistoryEntity : reducedHistoryEntities) {
				if (historyExpandOrReduceActions.containsKey(reducedHistoryEntity)) {
					ExpandOrReduceAction act = historyExpandOrReduceActions.get(reducedHistoryEntity);
					act.setSelected(false);
					this.reducedHistoryEntities.add(reducedHistoryEntity);
				}
			}
		}
	}

	public void setReducedBookmarkEntities(Set<String> reducedBookmarkEntities) {
		if (reducedBookmarkEntities != null) {
			for (String reducedBookmarkEntity : reducedBookmarkEntities) {
				if (bookmarkExpandOrReduceActions.containsKey(reducedBookmarkEntity)) {
					ExpandOrReduceAction act = bookmarkExpandOrReduceActions.get(reducedBookmarkEntity);
					act.setSelected(false);
					this.reducedBookmarkEntities.add(reducedBookmarkEntity);
				}
			}
		}
	}

	public boolean isStartmenuShown() {
		return isStartmenuShown;
	}

	public boolean isHistoryShown() {
		return isHistoryShown;
	}

	public boolean isBookmarkShown() {
		return isBookmarkShown;
	}

	private class ExpandOrReduceAction extends AbstractAction {

		private final Set<String> reducedItems;
		private Collection<LinkLabel> items = new ArrayList<LinkLabel>();

		public ExpandOrReduceAction(Set<String> reducedItems, String menu) {
			this(reducedItems, menu, null);
		}

		public ExpandOrReduceAction(Set<String> reducedItems, String menu, Icon icon) {
			super(menu, icon);
			this.reducedItems = reducedItems;
			setSelected(!reducedItems.contains(menu));
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			if (isEnabled()) {
				setSelected(transferSelected(ev, this));
			}
		}

		public void setSelected(boolean selected) {
			setActionSelected(this, selected);
			for (LinkLabel ll : items) {
				ll.setVisible(selected);
			}
			if (selected) {
				reducedItems.remove(getValue(Action.NAME));
			} else {
				reducedItems.add((String) getValue(Action.NAME));
			}
		}

		public void addLinkLabel(LinkLabel ll) {
			items.add(ll);
			ll.setVisible(isActionSelected(this));
		}

		@Override
		public boolean isEnabled() {
			return MainFrame.isStarttabEditable();
		}
	}
	
	Desktop getDesktop() {
		return desktop.getDesktopPreferences();
	}
	
	void setDesktop(Desktop desktopPreferences, List<GenericAction> actions) {
		this.desktop.setDesktopPreferences(desktopPreferences, actions);
	}
	
	boolean isDesktopActive() {
		return desktopActive;
	}
	
	void setDesktopActive(boolean desktopActive) {
		boolean bRepaint = false;
		if (this.desktopActive != desktopActive) {
			if (desktopActive) {
				remove(jpnCenter);
				add(desktop.getJComponent(), BorderLayout.CENTER);
				setBorderDesktop(desktop.getDesktopPreferences().isHideToolBar());
				fireDesktopShowing();
				if (desktop.getDesktopPreferences().isHideToolBar()) {
					fireDesktopToolBarChange(false);
				}
				if (desktop.getDesktopPreferences().isHideTabBar()) {
					fireDesktopTabBarChange(false);
				}
				fireDesktopBackgroundChange(desktop.getDesktopBackgroundPainter());
			} else {
				remove(desktop.getJComponent());
				add(jpnCenter, BorderLayout.CENTER);
				setBorderDefault();
				fireDesktopHiding();
			}
			bRepaint = true;
		}
		this.desktopActive = desktopActive;
		if (bRepaint)
			repaint();
	}
	
	private void setBorderDefault() {
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	}
	
	private void setBorderDesktop(boolean hideTabBar) {
		setBorder(BorderFactory.createEmptyBorder(hideTabBar? 0 : 10, 0, 0, 0));
	}
	
	/**
	 * 
	 */
	public void startInitiating() {
		initiating = true;
	}
	
	/**
	 * 
	 */
	public void finishInitiating() {
		initiating = false;
		if (desktopActive) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					setupStartmenu();
				}
			});
		} else {
			setupStartmenu();
		}
	}
	
	public void setDesktopBackgroundPainter(DesktopBackgroundPainter desktopBackgroundPainter) {
		desktop.setDesktopBackgroundPainter(desktopBackgroundPainter);
	}
	
	public void addDesktopListener(DesktopListener dl) {
		this.desktopListener.add(dl);
	}
	
	public boolean removeDesktopListener(DesktopListener dl) {
		return this.desktopListener.remove(dl);
	}
	
	private void fireDesktopShowing() {
		for (DesktopListener dl : this.desktopListener) {
			dl.desktopShowing();
		}
	}
		
	private void fireDesktopHiding() {
		for (DesktopListener dl : this.desktopListener) {
			dl.desktopHiding();
		}
	}
	
	private void fireDesktopToolBarChange(boolean show) {
		for (DesktopListener dl : this.desktopListener) {
			dl.toolbarChange(show);
		}
	}
	
	private void fireDesktopTabBarChange(boolean show) {
		for (DesktopListener dl : this.desktopListener) {
			dl.tabbarChange(show);
		}
		setBorderDesktop(!show);
	}
	
	private void fireDesktopBackgroundChange(DesktopBackgroundPainter painter) {
		for (DesktopListener dl : this.desktopListener) {
			dl.backgroundChange(painter);
		}
	}
}
