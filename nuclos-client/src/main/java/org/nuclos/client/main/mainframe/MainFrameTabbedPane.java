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

import static org.nuclos.client.main.mainframe.MainFrameUtils.setActionSelected;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.nuclos.client.StartIcons;
import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.common.NuclosCollectController;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.common.NuclosDropTargetListener;
import org.nuclos.client.main.GenericAction;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame.SplitRange;
import org.nuclos.client.main.mainframe.desktop.DesktopBackgroundPainter;
import org.nuclos.client.main.mainframe.desktop.DesktopListener;
import org.nuclos.client.synthetica.NuclosThemeSettings;
import org.nuclos.client.ui.ColoredLabel;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.PopupButton;
import org.nuclos.client.ui.ResultListener;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.WorkspaceDescription.Desktop;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;

public class MainFrameTabbedPane implements Closeable {

	private static final Logger LOG = Logger.getLogger(MainFrameTabbedPane.class);

	public final static int TAB_WIDTH_MAX = 200;
	public final static int TAB_WIDTH_MIN = 100;
	public final static int DEFAULT_TAB_COMPONENT_HEIGHT = 18;
	
	private ComponentPanel content;
	
	MFTabbedPane mfTabbed;
	boolean startTabVisible;
	
	private JPanel startTabMain;
	private DesktopBackgroundPainter desktopBackgroundPainter = DesktopBackgroundPainter.DEFAULT;
	private JToolBar startTabToolBar;
	private boolean showDesktopOnly = false;
	private StartTabPanel startTab;

	private ImageIcon defaultFirstTabIcon = MainFrame.resizeAndCacheTabIcon(Icons.getInstance().getIconStartTab());
	private ImageIcon maximizedFirstTabIcon = MainFrame.resizeAndCacheTabIcon(Icons.getInstance().getIconTabbedPaneMaximized());
	private ImageIcon maximizedFirstTabHomeIcon = MainFrame.resizeAndCacheTabIcon(Icons.getInstance().getIconTabbedPaneMaximized_Home());
	private ImageIcon maximizedFirstTabHomeTreeIcon = MainFrame.resizeAndCacheTabIcon(Icons.getInstance().getIconTabbedPaneMaximized_HomeTree());
	
	// former Spring injection
	
	private MainFrameSpringComponent mainFrameSpringComponent;
	
	// end of former Spring injection
	
	private JLabel lbClose = new JLabel(Icons.getInstance().getIconTabbedPaneClose());
	private JLabel lbMax = new JLabel(Icons.getInstance().getIconTabbedPaneMax());
	boolean maximizedTabs = false;

	public static final long DOUBLE_CLICK_SPEED = 400l;

	boolean ignoreAdjustTabs = false;

	private Action actionHome;
	private Action actionHomeTree;
	private Action[] actionSelectHistorySize = new Action[MainFrame.HISTORY_SIZES.length];
	private Icon homeIcon = Icons.getInstance().getIconHome16();
	private Icon homeTreeIcon = Icons.getInstance().getIconTree16();

	static Timer scheduleAdjustTabsTimer = new Timer(MainFrameTabbedPane.class.getName() + " AdjustTabsTimer");
	TimerTask adjustTabsTimerTask;
	
	public static boolean RESIZE_AND_ADJUST_IMMEDIATE = false;
	
	int tabClosing = -1;
	
	public void close() {
		Container parent = content.getParent();
		if (parent != null) {
			parent.remove(content);
		}
		content.close();
		content = null;
		mfTabbed.removeAll();
		mfTabbed = null;
		startTabMain.removeAll();
		startTabMain = null;
		startTab.removeAll();
		startTab = null;
		desktopBackgroundPainter = null;
		startTabToolBar = null;
	}
	
	private static class FirstTabLabel extends JLabel {
		
		private FirstTabLabel(ImageIcon icon) {
			super(icon);
		}
		
		@Override
		public Dimension getSize() {
			final Dimension result = super.getSize();
			result.height = MainFrameTabbedPane.DEFAULT_TAB_COMPONENT_HEIGHT;
			return result;
		}
		
		@Override
		public int getHeight() {
			return MainFrameTabbedPane.DEFAULT_TAB_COMPONENT_HEIGHT;
		}
	}
	
	private final JLabel lbFirstTabComponent = new FirstTabLabel(defaultFirstTabIcon);

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

	public static class DragParameter {
		public final MainFrameTabbedPane originTabbedPane;

		MainFrameTabbedPane mouseOverTabbedPane;
		Image tabImage = null;
		Rectangle tabBounds;
		Point currentMouseLocation = null;

		public int	draggedTabIndex = 0;
		int	xOffset = 0;
		int	y = 0;
		int	mouseOverIndex = -1;
		int	canceledAdjustTabsTasks = 0;

		SplitRange splitRange = SplitRange.NONE;
		SplitRange splitRangeBefore = SplitRange.NONE;

		public DragParameter(MainFrameTabbedPane originTabbedPane) {
			super();
			this.originTabbedPane = originTabbedPane;
		}
	}

	public static class DragWindow extends Window {

		public DragWindow(Frame owner, Image image, Dimension imageSize) {
			super(owner);
			imageSize.height += 1;
			setSize(imageSize);
//			setOpaque(false);
			JLabel lbImage = new JLabel(new ImageIcon(image));
			lbImage.setOpaque(false);
			add(lbImage);
			UIUtils.setWindowOpacity(DragWindow.this, 0.7f);
		}

	}

	public MainFrameTabbedPane() {
		content = new ComponentPanel(this);
		startTab = new StartTabPanel(MainFrameTabbedPane.this);
		mfTabbed = new MFTabbedPane(MainFrameTabbedPane.this);
		
		content.setContent(mfTabbed);
		
		actionHome = createHomeAction();
		actionHomeTree = createHomeTreeAction();
		
		setMainFrameSpringComponent(SpringApplicationContextHolder.getBean(MainFrameSpringComponent.class));
		init();
	}
	
	final void init() {
		setupStartTab();
		setCloseEnabled(false);
		setMaximizeEnabled(false);
	}
	
	final void setMainFrameSpringComponent(MainFrameSpringComponent mainFrameSpringComponent) {
		this.mainFrameSpringComponent = mainFrameSpringComponent;
	}
	
	final MainFrameSpringComponent getMainFrameSpringComponent() {
		return mainFrameSpringComponent;
	}
	
	public ComponentPanel getComponentPanel() {
		return content;
	}
	
	private void setShowDesktopOnly(boolean showDesktopOnly) {
		this.showDesktopOnly = showDesktopOnly;
		if (showDesktopOnly) {
			startTabMain.remove(startTab);
			content.setContent(startTab);
			startTab.setDesktopBackgroundPainter(desktopBackgroundPainter);
		} else {
			content.setContent(mfTabbed);
			startTabMain.add(startTab, BorderLayout.CENTER);
			startTab.setDesktopBackgroundPainter(DesktopBackgroundPainter.TRANSPARENT);
		}
	}

	/**
	 *
	 * @return
	 */
	List<MainFrameTab> getAllTabs() {
		List<MainFrameTab> result = new ArrayList<MainFrameTab>();
		result.addAll(getHiddenTabs());
		for (int i = 0; i < mfTabbed.getTabCount(); i++) {
			if (mfTabbed.getComponentAt(i) instanceof MainFrameTab) {
				result.add((MainFrameTab) mfTabbed.getComponentAt(i));
			}
		}
		return result;
	}

	/**
	 *
	 * @return
	 */
	public List<MainFrameTab> getHiddenTabs() {
		return startTab.getHiddenTabs();
	}

	/**
	 *
	 * @param enable
	 */
	void setCloseEnabled(boolean enable) {
		lbClose.setEnabled(enable);
	}

	/**
	 *
	 * @param enable
	 */
	void setMaximizeEnabled(boolean enable) {
		lbMax.setEnabled(enable);
	}

	/**
	 *
	 * @param maximized
	 */
	void setMaximized(boolean maximized) {
		maximizedTabs = maximized;
		if (isMaximized()) {
			lbMax.setIcon(Icons.getInstance().getIconTabbedPaneSplit());
		} else {
			lbMax.setIcon(Icons.getInstance().getIconTabbedPaneMax());
		}
		updateFirstTabIcon();
	}

	/**
	 *
	 */
	protected void updateFirstTabIcon() {
		if (isMaximized()) {
			if (isHome())
				lbFirstTabComponent.setIcon(maximizedFirstTabHomeIcon);
			else if (isHomeTree())
				lbFirstTabComponent.setIcon(maximizedFirstTabHomeTreeIcon);
			else
				lbFirstTabComponent.setIcon(maximizedFirstTabIcon);
		} else {
			if (isHome())
				lbFirstTabComponent.setIcon(homeIcon);
			else if (isHomeTree())
				lbFirstTabComponent.setIcon(homeTreeIcon);
			else
				lbFirstTabComponent.setIcon(defaultFirstTabIcon);
		}
	}

	/**
	 *
	 * @return
	 */
	protected boolean isMaximized() {
		return maximizedTabs;
	}

	/**
	 *
	 */
	public boolean isEnabledAt(int index) {
		return mfTabbed.isEnabledAt(index);
	}

	/**
	 *
	 */
	void refreshHistory() {
		startTab.refreshHistory();
	}

	/**
	 *
	 */
	void refreshBookmark() {
		startTab.refreshBookmark();
	}

	/**
	 *
	 */
	void setupStartmenu() {
		startTab.setupStartmenu();
	}

	/**
	 *
	 * @param entity
	 * @param marker
	 */
	void setStartmenuEntryMarker(String entity, LinkMarker marker) {
		startTab.setStartmenuEntryMarker(entity, marker);
	}

	/**
	 *
	 * @param eb
	 */
	void newNuclosTab(final EntityBookmark eb) {
		final String entity = eb.getEntity();
		UIUtils.runCommandLater(content, new Runnable() {
			@Override
			public void run() {
				try {
					NuclosCollectController<?> ncc = NuclosCollectControllerFactory.getInstance().newCollectController(entity, null, ClientParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
					if(ncc != null) {
						ncc.runViewSingleCollectableWithId(eb.getId());
					}
				}
				catch (CommonBusinessException ex) {
					final String sErrorMsg = SpringLocaleDelegate.getInstance().getMessage(
							"MainController.21","Die Stammdaten k\u00f6nnen nicht bearbeitet werden.");
					Errors.getInstance().showExceptionDialog(content, sErrorMsg, ex);
				}
			}
		});
	}

	/**
	 *
	 */
	void showHiddenTabsPopup() {
		if (countHiddenTabs() > 0) {
			JPopupMenu pm = new JPopupMenu();
			for (final MainFrameTab nuclosTab : startTab.getHiddenTabs()) {
				JMenuItem mi = new JMenuItem(nuclosTab.getTitle(), nuclosTab.getTabIcon());
				mi.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						restoreHiddenTab(nuclosTab);
					}
				});
				pm.add(mi);
			}

			Rectangle thhBounds = getTabHiddenHintBounds();
			pm.show(content, thhBounds.x, thhBounds.y + thhBounds.height);
		}
	}

	/**
	 *
	 * @return
	 */
	private Rectangle getFirstTabBounds() {
		return mfTabbed.getUI().getTabBounds(mfTabbed, 0);
	}
	
	/**
	 *
	 * @return
	 */
	Rectangle getLastTabBounds() {
		return mfTabbed.getUI().getTabBounds(mfTabbed, mfTabbed.getTabCount()-1);
	}
	
	/**
	 * 
	 * @param visible
	 */
	public void setStartTabVisible(boolean visible) {
		if (startTabVisible != visible) {
			startTabVisible = visible;
			if (visible) {
				mfTabbed.insertTab("", defaultFirstTabIcon, startTabMain, null, 0);
				mfTabbed.setTabComponentAt(0, lbFirstTabComponent);
				adjustTabs();
			} else {
				mfTabbed.remove(0);
			}
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isStartTabVisible() {
		return startTabVisible;
	}

	void refreshSelectedHistorySize() {
		if (0 <= MainFrame.getSelectedHistorySize() && MainFrame.getSelectedHistorySize() < actionSelectHistorySize.length) {
			setActionSelected(actionSelectHistorySize[MainFrame.getSelectedHistorySize()], true);
		}
	}

	/**
	 *
	 * @return
	 */
	protected Action createHomeAction() {
		AbstractAction result = new AbstractAction(null, homeIcon) {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmdHome();
			}
		};
		result.putValue(Action.SELECTED_KEY, isHome());
		result.putValue(Action.SHORT_DESCRIPTION, SpringLocaleDelegate.getInstance().getMessage(
				"MainFrameTabbedPane.1","Neue Tabs hier oeffnen"));
		return result;
	}

	/**
	 *
	 */
	private void cmdHome() {
		MainFrame.setHomeTabbedPane(MainFrameTabbedPane.this);
		for (MainFrameTabbedPane tabbedPane : MainFrame.getAllTabbedPanes()) {
			tabbedPane.updateHomes();
		}
	}

	/**
	 *
	 * @return
	 */
	protected Action createHomeTreeAction() {
		AbstractAction result = new AbstractAction(null, homeTreeIcon) {

			@Override
			public void actionPerformed(ActionEvent e) {
				cmdHomeTree();
			}
		};
		result.putValue(Action.SELECTED_KEY, isHomeTree());
		result.putValue(Action.SHORT_DESCRIPTION, SpringLocaleDelegate.getInstance().getMessage(
				"MainFrameTabbedPane.2","Neue Explorer Tabs hier oeffnen"));
		return result;
	}

	/**
	 *
	 */
	private void cmdHomeTree() {
		MainFrame.setHomeTreeTabbedPane(MainFrameTabbedPane.this);
		for (MainFrameTabbedPane tabbedPane : MainFrame.getAllTabbedPanes()) {
			tabbedPane.updateHomes();
		}
	}

	/**
	 *
	 * @return
	 */
	public boolean isHome() {
		return MainFrame.getHomePane() == MainFrameTabbedPane.this;
	}

	/**
	 *
	 */
	public void setHome() {
		cmdHome();
	}

	/**
	 *
	 * @return
	 */
	public boolean isHomeTree() {
		return MainFrame.getHomeTreePane() == MainFrameTabbedPane.this;
	}

	/**
	 *
	 */
	public void setHomeTree() {
		cmdHomeTree();
	}

	/**
	 *
	 * @return
	 */
	public boolean isShowAdministration() {
		return startTab.isShowAdministration();
	}

	/**
	 *
	 * @param showAdministration
	 */
	public void setShowAdministration(boolean showAdministration) {
		startTab.setShowAdministration(showAdministration);
	}

	/**
	 *
	 * @return
	 */
	public boolean isShowConfiguration() {
		return startTab.isShowConfiguration();
	}

	/**
	 *
	 * @param showConfiguration
	 */
	public void setShowConfiguration(boolean showConfiguration) {
		startTab.setShowConfiguration(showConfiguration);
	}

	/**
	 *
	 * @return
	 */
	public boolean isShowEntity() {
		return startTab.isShowEntity();
	}

	/**
	 *
	 * @param showEntity
	 */
	public void setShowEntity(boolean showEntity) {
		startTab.setShowEntity(showEntity);
	}

	/**
	 *
	 * @return
	 */
	public boolean isNeverHideStartmenu() {
		return startTab.isNeverHideStartmenu();
	}

	/**
	 *
	 * @param neverHideStartmenu
	 */
	public void setNeverHideStartmenu(boolean neverHideStartmenu) {
		startTab.setNeverHideStartmenu(neverHideStartmenu);
	}

	/**
	 *
	 * @return
	 */
	public boolean isNeverHideHistory() {
		return startTab.isNeverHideHistory();
	}

	/**
	 *
	 * @param neverHideHistory
	 */
	public void setNeverHideHistory(boolean neverHideHistory) {
		startTab.setNeverHideHistory(neverHideHistory);
	}

	/**
	 *
	 * @return
	 */
	public boolean isNeverHideBookmark() {
		return startTab.isNeverHideBookmark();
	}

	/**
	 *
	 * @param neverHideBookmark
	 */
	public void setNeverHideBookmark(boolean neverHideBookmark) {
		startTab.setNeverHideBookmark(neverHideBookmark);
	}

	/**
	 *
	 * @return
	 */
	public boolean isAlwaysHideStartmenu() {
		return startTab.isAlwaysHideStartmenu();
	}

	/**
	 *
	 * @param alwaysHideStartmenu
	 */
	public void setAlwaysHideStartmenu(boolean alwaysHideStartmenu) {
		startTab.setAlwaysHideStartmenu(alwaysHideStartmenu);
	}

	/**
	 *
	 * @return
	 */
	public boolean isAlwaysHideHistory() {
		return startTab.isAlwaysHideHistory();
	}

	/**
	 *
	 * @param alwaysHideHistory
	 */
	public void setAlwaysHideHistory(boolean alwaysHideHistory) {
		startTab.setAlwaysHideHistory(alwaysHideHistory);
	}

	/**
	 *
	 * @return
	 */
	public boolean isAlwaysHideBookmark() {
		return startTab.isAlwaysHideBookmark();
	}

	/**
	 *
	 * @param alwaysHideBookmark
	 */
	public void setAlwaysHideBookmark(boolean alwaysHideBookmark) {
		startTab.setAlwaysHideBookmark(alwaysHideBookmark);
	}

	/**
	 *
	 * @return
	 */
	public Set<String> getReducedStartmenus() {
		return startTab.getReducedStartmenus();
	}

	/**
	 *
	 * @param reducedStartmenus
	 */
	public void setReducedStartmenus(Set<String> reducedStartmenus) {
		startTab.setReducedStartmenus(reducedStartmenus);
	}

	/**
	 *
	 * @return
	 */
	public Set<String> getReducedHistoryEntities() {
		return startTab.getReducedHistoryEntities();
	}

	/**
	 *
	 * @param reducedHistoryEntities
	 */
	public void setReducedHistoryEntities(Set<String> reducedHistoryEntities) {
		startTab.setReducedHistoryEntities(reducedHistoryEntities);
		refreshHistory();
	}

	/**
	 *
	 * @return
	 */
	public Set<String> getReducedBookmarkEntities() {
		return startTab.getReducedBookmarkEntities();
	}

	/**
	 *
	 * @param reducedBookmarkEntities
	 */
	public void setReducedBookmarkEntities(Set<String> reducedBookmarkEntities) {
		startTab.setReducedBookmarkEntities(reducedBookmarkEntities);
		refreshBookmark();
	}

	/**
	 *
	 */
	public void updateHomes() {
		actionHome.putValue(Action.SELECTED_KEY, isHome());
		actionHomeTree.putValue(Action.SELECTED_KEY, isHomeTree());
		updateFirstTabIcon();
	}

	protected void setupDragDrop() {
		DropTarget drop = new DropTarget(this.lbFirstTabComponent, new NuclosDropTargetListener(mfTabbed));
		drop.setActive(true);
	}
	
	private void setupStartTab() {
		final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
		startTabMain = new JPanel(new BorderLayout()) {
			@Override
			public void paint(Graphics g) {
				if (!showDesktopOnly && desktopBackgroundPainter != null) {
					desktopBackgroundPainter.paint((Graphics2D) g, getWidth(), getHeight());
				}
				super.paint(g);
			}	
		};
		startTabMain.setOpaque(false);
		startTabToolBar = UIUtils.createNonFloatableToolBar();
		startTabToolBar.setOpaque(false);

		startTabMain.add(startTabToolBar, BorderLayout.NORTH);
		startTabMain.add(startTab, BorderLayout.CENTER);

		mfTabbed.addTab("", defaultFirstTabIcon, startTabMain);
		startTabVisible = true;

		mfTabbed.setTabComponentAt(0, lbFirstTabComponent);

		setupDragDrop();

		JPanel jpnTabbedPaneControl = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 3));
		/*final JLabel lbMin = new JLabel(Icons.getInstance().getIconTabbedPaneMin());
		lbMin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				lbMin.setIcon(Icons.getInstance().getIconTabbedPaneMin_hover());
			}
			@Override
			public void mouseExited(MouseEvent e) {
				lbMin.setIcon(Icons.getInstance().getIconTabbedPaneMin());
			}
		});*/
		lbMax.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if (lbMax.isEnabled()) {
					if (maximizedTabs)
						lbMax.setIcon(Icons.getInstance().getIconTabbedPaneSplit_hover());
					else
						lbMax.setIcon(Icons.getInstance().getIconTabbedPaneMax_hover());
				}
			}
			@Override
			public void mouseExited(MouseEvent e) {
				if (maximizedTabs)
					lbMax.setIcon(Icons.getInstance().getIconTabbedPaneSplit());
				else
					lbMax.setIcon(Icons.getInstance().getIconTabbedPaneMax());
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if (maximizedTabs)
					Main.getInstance().getMainFrame().restoreTabbedPaneContainingArea(MainFrameTabbedPane.this);
				else
					Main.getInstance().getMainFrame().maximizeTabbedPane(MainFrameTabbedPane.this);
			}
		});
		lbClose.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if (lbClose.isEnabled()) {
					lbClose.setIcon(Icons.getInstance().getIconTabbedPaneClose_hover());
				}
			}
			@Override
			public void mouseExited(MouseEvent e) {
				lbClose.setIcon(Icons.getInstance().getIconTabbedPaneClose());
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				Main.getInstance().getMainFrame().removeTabbedPane(MainFrameTabbedPane.this);
			}

		});
		//jpnTabbedPaneControl.add(lbMin);
		jpnTabbedPaneControl.add(lbMax);
		if (MainFrame.isSplittingEnabled()) {
			jpnTabbedPaneControl.add(lbClose);
		}

		final ColoredLabel bl = new ColoredLabel(jpnTabbedPaneControl, 
				localeDelegate.getMessage("MainFrameTabbedPane.3","Tableiste"));
		bl.setGradientPaint(false);
		startTabToolBar.add(bl);

		/**
		 * HOME's
		 */
		final JToggleButton btnHome = new JToggleButton(actionHome);
		btnHome.setFocusable(false);
		final MainFrame mainFrame = getMainFrameSpringComponent().getMainFrame();
		if (mainFrame.isStarttabEditable()) {
			startTabToolBar.add(btnHome);
		}
		final JToggleButton btnHomeTree = new JToggleButton(actionHomeTree);
		btnHomeTree.setFocusable(false);
		if (mainFrame.isStarttabEditable()) {
			startTabToolBar.add(btnHomeTree);
		}

		/**
		 * EXTRAS
		 */
		final PopupButton extraButton = new PopupButton(localeDelegate.getMessage(
				"PopupButton.Extras","Extras"));

		if (mainFrame.isStarttabEditable()) {
			extraButton.add(startTab.createHeadline(localeDelegate.getMessage(
					"StartTabPanel.11","Startmenu"), null));
			extraButton.add(new ShowHideJCheckBoxMenuItem(startTab.getShowStartmenuAction()));
			extraButton.add(new ShowHideJCheckBoxMenuItem(startTab.getAlwaysHideStartmenuAction()));
			JCheckBoxMenuItem cbmiAdministration = new JCheckBoxMenuItem(startTab.getShowAdministration());
			JCheckBoxMenuItem cbmiConfiguration = new JCheckBoxMenuItem(startTab.getShowConfiguration());
			JCheckBoxMenuItem cbmiEntity = new JCheckBoxMenuItem(startTab.getShowEntity());
			cbmiAdministration.setSelected(startTab.isShowAdministration());
			cbmiConfiguration.setSelected(startTab.isShowConfiguration());
			cbmiEntity.setSelected(startTab.isShowEntity());
			extraButton.add(cbmiAdministration);
			extraButton.add(cbmiConfiguration);
			extraButton.add(cbmiEntity);	
			extraButton.addSeparator();
		}

		extraButton.add(startTab.createHeadline(localeDelegate.getMessage("StartTabPanel.12","Zuletzt angesehen"), null));
		
		if (mainFrame.isStarttabEditable()) {
			extraButton.add(new ShowHideJCheckBoxMenuItem(startTab.getShowHistoryAction()));
			extraButton.add(new ShowHideJCheckBoxMenuItem(startTab.getAlwaysHideHistoryAction()));
		}	
		
		ButtonGroup bgHistorySize = new ButtonGroup();

		for (int i = 0; i < MainFrame.HISTORY_SIZES.length; i++) {
			actionSelectHistorySize[i] = startTab.createSelectHistorySize(i);
			JRadioButtonMenuItem radmiHistorySize = new JRadioButtonMenuItem(actionSelectHistorySize[i]);
			bgHistorySize.add(radmiHistorySize);
			extraButton.add(radmiHistorySize);
			if (i == MainFrame.getSelectedHistorySize()) {
				radmiHistorySize.setSelected(true);
			}
		}
		extraButton.add(new JMenuItem(startTab.getClearHistoryAction()));
		
		extraButton.addSeparator();
		extraButton.add(startTab.createHeadline(localeDelegate.getMessage("StartTabPanel.13","Lesezeichen"), null));
		
		if (mainFrame.isStarttabEditable()) {
			extraButton.add(new ShowHideJCheckBoxMenuItem(startTab.getShowBookmarkAction()));
			extraButton.add(new ShowHideJCheckBoxMenuItem(startTab.getAlwaysHideBookmarkAction()));
		}
		
		extraButton.add(new JMenuItem(startTab.getClearBookmarkAction()));
		if (mainFrame.isStarttabEditable()) {
			extraButton.addSeparator();
			extraButton.add(new JMenuItem(startTab.getActivateDesktopAction()));
		}
		
		if (MainFrame.isSplittingEnabled()) {
			extraButton.addSeparator();
			extraButton.add(new JMenuItem(startTab.getRemoveSplitPaneFixationsAction()));
		}

		startTabToolBar.add(extraButton);
		
		startTab.addDesktopListener(new DesktopListener() {
			@Override
			public void desktopShowing() {
				extraButton.setVisible(false);
			}
			@Override
			public void desktopHiding() {
				extraButton.setVisible(true);
				setDesktopBackgroundPainter(DesktopBackgroundPainter.DEFAULT);
			}
			@Override
			public void toolbarChange(boolean show) {
				bl.setVisible(show);
				btnHome.setVisible(show);
				btnHomeTree.setVisible(show);
			}
			@Override
			public void tabbarChange(boolean show) {
				setShowDesktopOnly(!show);
			}
			@Override
			public void backgroundChange(DesktopBackgroundPainter painter) {
				setDesktopBackgroundPainter(painter);
			}
		});
	}
	
	private void setDesktopBackgroundPainter(DesktopBackgroundPainter painter) {
		if (DesktopBackgroundPainter.DEFAULT.equals(painter)) {
			startTabToolBar.putClientProperty("Synthetica.opaque", Boolean.TRUE);
		} else {
			startTabToolBar.putClientProperty("Synthetica.opaque", Boolean.FALSE);
		}
		
		startTab.setDesktopBackgroundPainter(showDesktopOnly ?
				painter:
				DesktopBackgroundPainter.TRANSPARENT);
		this.desktopBackgroundPainter = painter;
		startTabMain.repaint();
	}

	/**
	 *
	 * @param nuclosTab
	 * @param addToFront
	 */
	public void addTab(MainFrameTab nuclosTab, boolean addToFront) {
		mfTabbed.addTab(nuclosTab, addToFront);
	}

	/**
	 *
	 * @param nuclosTab
	 * @param index
	 */
	public void addTab(MainFrameTab nuclosTab, int index) {
		mfTabbed.addTab(nuclosTab, index);
	}

	/**
	 *
	 * @param nuclosTab
	 * @return index of tab (could be <0 if tab is hidden)
	 */
	public int getTabIndex(MainFrameTab nuclosTab) {
		final int index = mfTabbed.indexOfComponent(nuclosTab);

		return index;
	}

	/**
	 * close tabs without adjusting width
	 * @return not closable tabs
	 */
	public void closeAllTabs(final ResultListener<List<MainFrameTab>> rl) {
		closeAllTabs(null, rl);
	}

	/**
	 * close tabs without adjusting width
	 * @return not closable tabs
	 */
	public void closeAllTabs(final MainFrameTab ignoreTab, final ResultListener<List<MainFrameTab>> rl) {
		final List<MainFrameTab> notClosableTabs = new ArrayList<MainFrameTab>();

		List<MainFrameTab> tabsToRemove = new ArrayList<MainFrameTab>();
		for (int i = 1; i < mfTabbed.getTabCount(); i++) {
			if (mfTabbed.getComponentAt(i) instanceof MainFrameTab) {
				MainFrameTab tab = (MainFrameTab) mfTabbed.getComponentAt(i);
				if (tab == ignoreTab) {
					continue;
				}
				if (tab.isClosable()) {
					tabsToRemove.add(tab);
				} else {
					notClosableTabs.add(tab);
				}
			}
		}
		
		class Counter {
			int targetCount = 0;
			int answers = 0;
			boolean isComplete() {
				return targetCount == answers;
			}
			void answered() {
				answers++;
				if (isComplete()) {
					rl.done(notClosableTabs);
				}
			}
		}
		final Counter counter = new Counter();
		
		counter.targetCount = startTab.getHiddenTabs().size() + tabsToRemove.size();
		if (counter.targetCount == 0) {
			rl.done(notClosableTabs);
			return;
		}
		
		for (final MainFrameTab tab : startTab.getHiddenTabs()) {
			if (tab == ignoreTab) {
				counter.answered();
				continue;
			}
			if (tab.isClosable()) {
					tab.notifyClosing(new ResultListener<Boolean>() {
						@Override
						public void done(Boolean result) {
							if (Boolean.TRUE.equals(result)) {
								startTab.removeHiddenTab(tab);
								tab.notifyClosed();
							} else {
								notClosableTabs.add(tab);
							}
							counter.answered();
						}
					});
			} else {
				notClosableTabs.add(tab);
				counter.answered();
			}
		}
		for (final MainFrameTab tab : tabsToRemove) {
			tab.notifyClosing(new ResultListener<Boolean>() {
				@Override
				public void done(Boolean result) {
					if (Boolean.TRUE.equals(result)) {
						mfTabbed.remove(tab);
						tab.notifyClosed();	
					} else {
						notClosableTabs.add(tab);
					}
					counter.answered();
				}
			});
		}
	}

	/**
	 * only remove... NO close
	 * @param tab
	 */
	public void removeTab(MainFrameTab tab) {
		final int index = mfTabbed.indexOfComponent(tab);

		if (index < 0) {
			startTab.removeHiddenTab(tab);
		} else {
			mfTabbed.removeTabAt(index, false);
		}

		scheduleAdjustTabs(1250);
	}

	/**
	 *
	 * @param tab
	 * @param mousePosition
	 */
	public void closeTab(final MainFrameTab tab, final Point mousePosition, final ResultListener<Boolean> rl) {
		if (!tab.isClosable()) {
			return;
		}
		
		tab.notifyClosing(new ResultListener<Boolean>() {
			@Override
			public void done(Boolean result) {
				if (Boolean.TRUE.equals(result)) {
					final int index = mfTabbed.indexOfComponent(tab);
					final int selected = mfTabbed.getSelectedIndex();
					
					Point mp = mousePosition; 
					if (mp == null) {
						// try to get MousePosition from current tab
						final Component tabComponent = mfTabbed.getTabComponentAt(index);
						if (tabComponent != null) {
							mp = tabComponent.getMousePosition();
						}
					}

					if (index < 0) {
						startTab.removeHiddenTab(tab);
					} else {
						mfTabbed.removeTabAt(index, false);
					}
					tab.notifyClosed();
					adjustTabs(false);

					if (index == selected) {
						tabClosing = -1;
						if (mfTabbed.getTabCount() > 0) {
							if (mfTabbed.getTabCount() == index) {
								mfTabbed.setSelectedIndex(index-1, true);
							} else {
								mfTabbed.setSelectedIndex(index, true);
							}
						}
					}
					
					if (mfTabbed.getTabCount() > index) {
						// setMouseOver on tab at same index
						 if (mp != null) {
							final Component tabComponent = mfTabbed.getTabComponentAt(index);
							if (tabComponent instanceof MainFrameTab.TabTitle) {
								final MainFrameTab.TabTitle tabTitle = (MainFrameTab.TabTitle) tabComponent;
								tabTitle.setMouseOverPosition(mp);
							}
						}
					}
					scheduleAdjustTabs(1250);
					
					rl.done(true);
				} else {
					rl.done(false);
				}
			}
		});
	}

	/**
	 *
	 * @param delay
	 */
	private void scheduleAdjustTabs(final int delay) {
		if (adjustTabsTimerTask != null) {
			adjustTabsTimerTask.cancel();
		}
		scheduleAdjustTabsTimer.purge();
		adjustTabsTimerTask = new TimerTask() {
			@Override
			public void run() {
				if (ignoreAdjustTabs) {
					// currently ignoring adjustment... extra delay
					scheduleAdjustTabs(delay);
				} else {
					adjustTabs();
				}
			}
		};
		if (RESIZE_AND_ADJUST_IMMEDIATE) {
			adjustTabsTimerTask.run();
		} else {
			scheduleAdjustTabsTimer.schedule(adjustTabsTimerTask, delay);
		}
	}
	
	/**
	 * 
	 */
	public void startInitiating() {
		startTab.startInitiating();
		ignoreAdjustTabs = true;
	}
	
	/**
	 * 
	 */
	public void finishInitiating() {
		startTab.finishInitiating();
		ignoreAdjustTabs = false;
		adjustTabs();
	}

	/**
	 *
	 */
	public void adjustTabs() {
		adjustTabs(null, true);
	}

	/**
	 *
	 * @param tabForceVisibility
	 */
	void adjustTabs(final MainFrameTab tabForceVisibility) {
		adjustTabs(tabForceVisibility, true);
	}

	/**
	 *
	 * @param recalculateWidth
	 */
	void adjustTabs(final boolean recalculateWidth) {
		adjustTabs(null, recalculateWidth);
	}

	/**
	 *
	 * @param tabForceVisibility
	 * @param recalculateWidth
	 */
	void adjustTabs(final MainFrameTab tabForceVisibility, final boolean recalculateWidth) {
		if ((mfTabbed.getTabCount() <= (startTabVisible?1:0) && countHiddenTabs() == 0) || ignoreAdjustTabs)
			return;

		ignoreAdjustTabs = true;
		final int tabbedPaneWidth = mfTabbed.getBounds().width - 4; // -4=insets

		// first tab width
		int startTabWidth;
		try {
			startTabWidth = startTabVisible?(mfTabbed.getUI().getTabBounds(mfTabbed, 0).width):0;
		} catch (java.lang.ArrayIndexOutOfBoundsException ex) {
			startTabWidth = 0;
		}

		int hideTabs = mfTabbed.getTabCount() - (startTabVisible?1:0) - ((tabbedPaneWidth - startTabWidth) / TAB_WIDTH_MIN);
		if (hideTabs == mfTabbed.getTabCount() - (startTabVisible?1:0)) hideTabs--; // do not hide all content tabs... last man standing ;-)

		// hideTabs * -1 = restoreTabs...
		int restoreTabs = hideTabs * -1;
		if (countHiddenTabs() < restoreTabs) restoreTabs = countHiddenTabs();

		final int contentTabWidth = (tabbedPaneWidth - startTabWidth) / ((mfTabbed.getTabCount() - (startTabVisible?1:0)) - (hideTabs>0 ? hideTabs : 0) + (restoreTabs > 0 ? restoreTabs : 0));
		LOG.debug("HideTabs="+hideTabs + " content tab width="+contentTabWidth);

		if (restoreTabs > 0) {
			for (MainFrameTab nuclosTab : startTab.removeHiddenTabs(restoreTabs)) {
				addTab(nuclosTab, true);
			}
		}
		if (recalculateWidth) {
			for (int i = (startTabVisible?1:0); i < mfTabbed.getTabCount(); i++) {
				Component c = mfTabbed.getTabComponentAt(i);
				if (c instanceof MainFrameTab.TabTitle) {
					MainFrameTab.TabTitle tt = (MainFrameTab.TabTitle) c;
					tt.setWidth(TAB_WIDTH_MAX < contentTabWidth ? TAB_WIDTH_MAX : (TAB_WIDTH_MIN > contentTabWidth  ? TAB_WIDTH_MIN : contentTabWidth));
				}
			}
		}
		if (hideTabs > 0) {
			int hideUntil = mfTabbed.getTabCount() - (startTabVisible?1:0) < hideTabs ? mfTabbed.getTabCount() - (startTabVisible?1:0) : hideTabs;
			int indexToHide = (startTabVisible?1:0);
			for (int i = 0; i < hideUntil; i++) {
				if (mfTabbed.getComponentAt(indexToHide) instanceof MainFrameTab) {
					MainFrameTab tabToHide = (MainFrameTab) mfTabbed.getComponentAt(indexToHide);
					if (tabToHide == tabForceVisibility) {
						hideUntil++;
						indexToHide++;
					} else {
						startTab.addHiddenTab(tabToHide);
						mfTabbed.removeTabAt(indexToHide);
					}
				}
			}
		}

		ignoreAdjustTabs = false;
	}

	/**
	 *
	 * @param nuclosTab
	 * @return true if tab hidden, false if nothing to do
	 */
	public boolean restoreHiddenTab(MainFrameTab nuclosTab) {
		if (startTab.getHiddenTabs().contains(nuclosTab)) {
			addTab(nuclosTab, false);
			startTab.removeHiddenTab(nuclosTab);
			mfTabbed.setSelectedIndex(mfTabbed.getTabCount()-1);
			return true;
		}
		return false;
	}

	/**
	 *
	 * @return
	 */
	Rectangle getTabHiddenHintBounds() {
		Rectangle ftBounds = getLastTabBounds();
		ImageIcon icoHint = Icons.getInstance().getIconTabHiddenHint();

		return new Rectangle(
			ftBounds.x+ftBounds.width-icoHint.getIconWidth(),
			ftBounds.y,
			icoHint.getIconWidth(),
			icoHint.getIconHeight());
	}

	/**
	 *
	 * @param dp
	 */
	void repaintTabOptimized(DragParameter dp) {
		if (dp != null && dp.mouseOverTabbedPane != null) {
			Rectangle tpBounds = dp.mouseOverTabbedPane.mfTabbed.getBounds();
			int height = dp.mouseOverTabbedPane.mfTabbed.getHeight();
			LOG.trace("SplitRange: " + dp.splitRangeBefore + " --> " + dp.splitRange);

			if (dp.splitRange == SplitRange.NONE && dp.splitRangeBefore == SplitRange.NONE)
				height = 35;

			LOG.trace("Repaint TabBar: " + tpBounds.width + " x " + height);
			dp.mouseOverTabbedPane.mfTabbed.repaint(0, 0, tpBounds.width, height);
		}
	}

	/**
	 *
	 * @return
	 */
	public int countHiddenTabs() {
		return startTab.countHiddenTabs();
	}

	private static class ShowHideJCheckBoxMenuItem extends JCheckBoxMenuItem {

		public ShowHideJCheckBoxMenuItem(Action a) {
			super(a);
			setForeground(NuclosThemeSettings.ICON_BLUE);
		}

	}

	public Desktop getDesktop() {
		return startTab.getDesktop();
	}
	
	public void setDesktop(Desktop desktop, List<GenericAction> actions) {
		startTab.setDesktop(desktop, actions);
	}
	
	public boolean isDesktopActive() {
		return startTab.isDesktopActive();
	}
	
	public void setDesktopActive(boolean desktopActive) {
		startTab.setDesktopActive(desktopActive);
	}
	
	public static class EmptyPanel extends JPanel {
		
		private final ImageIcon bgImg = new ImageIcon(StartIcons.getInstance().getBigTransparentApplicationIcon512().getImage());
		
		private JLabel lbTitle;
		
		public EmptyPanel() {
			super(new BorderLayout(0, 0));
			add(UIUtils.createNonFloatableToolBar(), BorderLayout.NORTH);
			setupTabTitle();
		}
		
		private void setupTabTitle() {
			lbTitle = new JLabel(Icons.getInstance().getIconEmpty16());
			lbTitle.setBounds(0, 0, 16+MainFrameTab.TabTitle.TAB_WIDTH_CONSTRUCT, DEFAULT_TAB_COMPONENT_HEIGHT);
		}
		
		public JLabel getTabTitle() {
			return lbTitle;
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			Graphics2D g2 = (Graphics2D) g;

			RenderingHints oldRH = g2.getRenderingHints();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

			final int wIco = bgImg.getIconWidth();
			final int hIco = bgImg.getIconHeight();

			BufferedImage bi = new BufferedImage(wIco, hIco, BufferedImage.TYPE_INT_ARGB);
			Graphics gbi = bi.getGraphics();
			gbi.drawImage(bgImg.getImage(), 0, 0, null);
			gbi.dispose();

			// 10% opaque
			float[] scales = { 1f, 1f, 1f, 0.1f };
			float[] offsets = new float[4];
			RescaleOp rop = new RescaleOp(scales, offsets, null);
		   	g2.drawImage(bi, rop, getWidth()-wIco/2, getHeight()-(hIco/2));

			g2.setRenderingHints(oldRH);
		}
	}
	
	public static class ComponentPanel extends JPanel {
		
		private final MainFrameTabbedPane tabbedPane;
		
		public ComponentPanel(MainFrameTabbedPane tabbedPane) {
			super(new BorderLayout(0, 0));
			this.tabbedPane = tabbedPane;
			setOpaque(false);
		}
		
		void setContent(Component comp) {
			super.removeAll();
			super.add(comp, BorderLayout.CENTER);
			revalidate();
			repaint();
		}

		public MainFrameTabbedPane getMainFrameTabbedPane() {
			return tabbedPane;
		}
		
		public void add(MainFrameTab tab) {
			tabbedPane.add(tab);
		}
		
		@Override
		public Component add(Component comp) {
			throw new IllegalArgumentException();
		}

		@Override
		public Component add(String name, Component comp) {
			throw new IllegalArgumentException();
		}

		@Override
		public Component add(Component comp, int index) {
			throw new IllegalArgumentException();
		}

		@Override
		public void add(Component comp, Object constraints) {
			throw new IllegalArgumentException();
		}

		@Override
		public void add(Component comp, Object constraints, int index) {
			throw new IllegalArgumentException();
		}

		@Override
		public void remove(int index) {
			throw new IllegalArgumentException();
		}

		@Override
		public void remove(Component comp) {
			throw new IllegalArgumentException();
		}

		@Override
		public void removeAll() {
			throw new IllegalArgumentException();
		}
		
		public void close() {
			super.removeAll();
		}
		
	}

	public Component getComponentAt(int tabIndexSource) {
		return mfTabbed.getComponentAt(tabIndexSource);
	}

	public void addTab(String title, ImageIcon tabIcon, Component c, String tip) {
		mfTabbed.addTab(title, tabIcon, c, tip);
	}

	public void setSelectedIndex(int i) {
		mfTabbed.setSelectedIndex(i);
	}

	public void setSelectedComponent(Component c) {
		mfTabbed.setSelectedComponent(c);
	}

	public Component getSelectedComponent() {
		return mfTabbed.getSelectedComponent();
	}

	public int indexOfComponent(Component c) {
		return mfTabbed.indexOfComponent(c);
	}

	public int getTabCount() {
		return mfTabbed.getTabCount();
	}

	public int getSelectedIndex() {
		return mfTabbed.getSelectedIndex();
	}

	public void setTabComponentAt(int index, Component component) {
		mfTabbed.setTabComponentAt(index, component);
	}

	public void setToolTipTextAt(int index, String toolTipText) {
		mfTabbed.setToolTipTextAt(index, toolTipText);
	}

	public void remove(int index) {
		mfTabbed.remove(index);
	}

	public void add(MainFrameTab tab) {
		mfTabbed.add(tab);
	}

	public void revalidate() {
		mfTabbed.invalidate();
		mfTabbed.revalidate();
		mfTabbed.repaint();
	}
	
}