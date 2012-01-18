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

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.IllegalComponentStateException;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
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
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
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
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.nuclos.client.NuclosIcons;
import org.nuclos.client.common.NuclosCollectController;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.common.NuclosDropTargetListener;
import org.nuclos.client.common.NuclosDropTargetVisitor;
import org.nuclos.client.main.GenericAction;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame.SplitRange;
import org.nuclos.client.main.mainframe.desktop.DesktopListener;
import org.nuclos.client.synthetica.NuclosThemeSettings;
import org.nuclos.client.ui.BlackLabel;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.PopupButton;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.WorkspaceDescription.Desktop;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;

public class MainFrameTabbedPane extends JTabbedPane implements NuclosDropTargetVisitor {

	private static final Logger LOG = Logger.getLogger(MainFrameTabbedPane.class);

	private static MainFrameTabbedPane.DragParameter dp = null;
	private static MainFrameTabbedPane.DragWindow dw = null;

	public final static int TAB_WIDTH_MAX = 200;
	public final static int TAB_WIDTH_MIN = 100;
	public final static int DEFAULT_TAB_COMPONENT_HEIGHT = 18;

	private static final int insetLeft = 2;
	private static final int insetRight = 2;
	private static final int insetBottom = 2;

	private final StartTabPanel startTab = new StartTabPanel(MainFrameTabbedPane.this);

	private final ImageIcon defaultFirstTabIcon = MainFrame.resizeAndCacheTabIcon(NuclosIcons.getInstance().getFrameIcon());
	private final ImageIcon maximizedFirstTabIcon = MainFrame.resizeAndCacheTabIcon(Icons.getInstance().getIconTabbedPaneMaximized());
	private final ImageIcon maximizedFirstTabHomeIcon = MainFrame.resizeAndCacheTabIcon(Icons.getInstance().getIconTabbedPaneMaximized_Home());
	private final ImageIcon maximizedFirstTabHomeTreeIcon = MainFrame.resizeAndCacheTabIcon(Icons.getInstance().getIconTabbedPaneMaximized_HomeTree());

	private final JLabel lbFirstTabComponent = new JLabel(defaultFirstTabIcon);
	private final JLabel lbClose = new JLabel(Icons.getInstance().getIconTabbedPaneClose());
	private final JLabel lbMax = new JLabel(Icons.getInstance().getIconTabbedPaneMax());
	private boolean maximizedTabs = false;

	private static final long doubleClickSpeed = 400l;
	private long lastClickOnTab = 0l;
	private Point lastClickOnTabPosition;

	private boolean ignoreAdjustTabs = false;
	private boolean isMouseOverTabHiddenHint = false;

	private final Action actionHome;
	private final Action actionHomeTree;
	private final Action[] actionSelectHistorySize = new Action[MainFrame.HISTORY_SIZES.length];
	private final Icon homeIcon = Icons.getInstance().getIconHome16();
	private final Icon homeTreeIcon = Icons.getInstance().getIconTree16();

	private final Timer scheduleAdjustTabsTimer = new Timer(MainFrameTabbedPane.class.getName() + " AdjustTabsTimer");
	private TimerTask adjustTabsTimerTask;

	private final Timer resizeTimer = new Timer(MainFrameTabbedPane.class.getName() + " ResizeTimer");
	private TimerTask resizeTimerTask;
	
	public static boolean RESIZE_AND_ADJUST_IMMEDIATE = false;

	@Override
	protected void finalize() throws Throwable {
		scheduleAdjustTabsTimer.cancel();
		resizeTimer.cancel();
		super.finalize();
	}

	/**
	 *
	 */
	public class DragParameter {
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

	/**
	 *
	 */
	public class DragWindow extends Window {

		public DragWindow(Frame owner, Image image, Dimension imageSize) {
			super(owner);
			imageSize.height += 1;
			setSize(imageSize);
			setOpaque(false);
			JLabel lbImage = new JLabel(new ImageIcon(image));
			lbImage.setOpaque(false);
			add(lbImage);
			UIUtils.setWindowOpacity(DragWindow.this, 0.7f);
		}

	}

	/**
	 *
	 */
	public MainFrameTabbedPane() {
		super();
		setMinimumSize(new Dimension(TAB_WIDTH_MIN+4+41, 50)); // 4=inset // 41=firstTab

		actionHome = createHomeAction();
		actionHomeTree = createHomeTreeAction();
		setupStartTab();
		setCloseEnabled(false);
		setMaximizeEnabled(false);
		setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				if (resizeTimerTask != null) {
					resizeTimerTask.cancel();
					resizeTimerTask = null;
					resizeTimer.purge();
				}

				resizeTimerTask = new TimerTask() {
					@Override
					public void run() {
						adjustTabs();
						revalidate();
					}
				};
				try {
					if (RESIZE_AND_ADJUST_IMMEDIATE) {
						resizeTimerTask.run();
					} else {
						resizeTimer.schedule(resizeTimerTask, 500);
					}
				} catch (IllegalStateException ex) {
					// ignore Timer already cancelled.
				}
			}
		});

		addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {
				if (countHiddenTabs() <= 0) {
					isMouseOverTabHiddenHint = false;
				} else {
					Rectangle thhBounds = getTabHiddenHintBounds();
					Polygon poly = new Polygon();
					poly.addPoint(thhBounds.x, thhBounds.y);
					poly.addPoint(thhBounds.x + thhBounds.width, thhBounds.y);
					poly.addPoint(thhBounds.x + thhBounds.width, thhBounds.y + thhBounds.height);
					isMouseOverTabHiddenHint = poly.contains(e.getPoint());
				}

				// Gets the tab index based on the mouse position
				final int tabNumber = getUI().tabForCoordinate(MainFrameTabbedPane.this, e.getX(), e.getY());

				for (int i = 1; i < getTabCount(); i++) {
					final Component tabComponent = getTabComponentAt(i);
					if (tabComponent instanceof MainFrameTab.TabTitle) {
						final MainFrameTab.TabTitle tabTitle = (MainFrameTab.TabTitle) tabComponent;
						if (i == tabNumber) {
							tabTitle.setMouseOverPosition(new Point(
								e.getX(),
								e.getY()));
						} else {
							tabTitle.setMouseOverPosition(null);
						}
					}
				}

				if(tabNumber >= 1) {
					Component tabComponent = getTabComponentAt(tabNumber);
					if (tabComponent instanceof MainFrameTab.TabTitle) {

					}
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {

				if(dp == null) {
					DragParameter dpInit = new DragParameter(MainFrameTabbedPane.this);

					// Gets the tab index based on the mouse position
					int tabNumber = getUI().tabForCoordinate(MainFrameTabbedPane.this, e.getX(), e.getY());

					if(tabNumber >= 1) {
						if (adjustTabsTimerTask != null) {
							adjustTabsTimerTask.cancel();
						}
						dpInit.canceledAdjustTabsTasks = scheduleAdjustTabsTimer.purge();
						dpInit.currentMouseLocation = new Point(e.getX(), e.getY());
						dpInit.draggedTabIndex = tabNumber;
						dpInit.mouseOverIndex = tabNumber;
						dpInit.mouseOverTabbedPane = MainFrameTabbedPane.this;
						dpInit.tabBounds = getUI().getTabBounds(MainFrameTabbedPane.this, tabNumber);
						dpInit.tabBounds.height += 1;
						dpInit.xOffset = dpInit.tabBounds.x - e.getX();
						dpInit.y = dpInit.tabBounds.y;

						// Paint the tabbed pane to a buffer
						Image totalImage = new BufferedImage(getWidth(), getHeight()+1, BufferedImage.TYPE_INT_ARGB);
						Graphics totalGraphics = totalImage.getGraphics();
						totalGraphics.setClip(dpInit.tabBounds);
						// Don't be double buffered when painting to a
						// static image.
						setDoubleBuffered(false);
						paint(totalGraphics);

						// Paint just the dragged tab to the buffer
						dpInit.tabImage = new BufferedImage(dpInit.tabBounds.width, dpInit.tabBounds.height+1, BufferedImage.TYPE_INT_ARGB);
						Graphics graphics = dpInit.tabImage.getGraphics();
						graphics.drawImage(totalImage, 0, 0, dpInit.tabBounds.width, dpInit.tabBounds.height+1,
							dpInit.tabBounds.x, dpInit.tabBounds.y, dpInit.tabBounds.x + dpInit.tabBounds.width,
							dpInit.tabBounds.y + dpInit.tabBounds.height+1, MainFrameTabbedPane.this);

						dp = dpInit;
						repaint();
					}
				}
				else {
					// is mouse position near a tabbedPane? This is needed for drawing the drag window later
					boolean isMousePositionNearOrInTabbedPane = false;

					// locate mouse over tab pane if any
					MainFrameTabbedPane newMouseOverTabbedPane = null;
					for (MainFrameTabbedPane tabbedPane : MainFrame.getOrderedTabbedPanes()) {
						try {
							Point p = tabbedPane.getLocationOnScreen();
							Rectangle r = new Rectangle(p);
							Rectangle tpBounds = tabbedPane.getBounds();
							r.width = tpBounds.width;
							r.height = tpBounds.height;

							if (r.contains(e.getLocationOnScreen())) {
								newMouseOverTabbedPane = tabbedPane;
								isMousePositionNearOrInTabbedPane = true;
								break;
							} else {
								// extend rectangle by 10 pixel in every direction
								r.x -= 10;
								r.y -= 10;
								r.width += 20;
								r.height += 20;
								if (r.contains(e.getLocationOnScreen())) {
									isMousePositionNearOrInTabbedPane = true;
								}
							}

						} catch (IllegalComponentStateException ex) {
							continue;
						}
					}
					if (dp.mouseOverTabbedPane != null) {
						if (newMouseOverTabbedPane == null) {
							// repaint leaving tab pane
							repaintTabOptimized(dp);
						} else {
							if (dp.mouseOverTabbedPane != newMouseOverTabbedPane) {
								// repaint leaving tab pane
								repaintTabOptimized(dp);
							}
						}
					}
					dp.mouseOverTabbedPane = newMouseOverTabbedPane;
					if (dp.mouseOverTabbedPane == null)
						dp.mouseOverIndex = -1;

					if (dp.mouseOverTabbedPane != null) {

						try {
							Point mouseOnScreen = e.getLocationOnScreen();
							Point tabOnScreen = dp.mouseOverTabbedPane.getLocationOnScreen();

							Point relativeToMouseOverTab = new Point();
							if (dp.mouseOverTabbedPane == dp.originTabbedPane) {
								relativeToMouseOverTab.x = e.getX();
								relativeToMouseOverTab.y = e.getY();
							}
							else {
								relativeToMouseOverTab.x = mouseOnScreen.x - tabOnScreen.x;
								relativeToMouseOverTab.y = mouseOnScreen.y - tabOnScreen.y;
							}

							int tabNumber = dp.mouseOverTabbedPane.getUI().tabForCoordinate(dp.mouseOverTabbedPane, relativeToMouseOverTab.x, 10);

							if(tabNumber >= 0) {
								dp.mouseOverIndex = tabNumber==0?1:tabNumber;
							} else {
								dp.mouseOverIndex = -1;
							}

							LOG.debug("MouseOverIndex: " + dp.mouseOverIndex);

							dp.currentMouseLocation = relativeToMouseOverTab;

							if (!MainFrame.isSplittingDeactivated()) {
								// save old splitrange for optimized repaint
								dp.splitRangeBefore = dp.splitRange;
								// check if in splitting range
								if (dp.currentMouseLocation.y > 50) {
									Rectangle tpBounds = dp.mouseOverTabbedPane.getBounds();
									Dimension minSize = dp.mouseOverTabbedPane.getMinimumSize();
									// check if size is big enough to split
									final boolean widthIsBigEnough = tpBounds.width > minSize.width*2;
									final boolean heightIsBigEnough = tpBounds.height > minSize.height*2;
									if (widthIsBigEnough && dp.currentMouseLocation.x >= 0 && dp.currentMouseLocation.x < tpBounds.width / 3) {
										// WEST
										dp.splitRange = SplitRange.WEST;
									} else if (heightIsBigEnough && dp.currentMouseLocation.x >= tpBounds.width / 3 && dp.currentMouseLocation.x < tpBounds.width / 3 * 2) {
										// NORTH OR SOUTH
										if (dp.currentMouseLocation.y < (tpBounds.height - dp.tabBounds.height - dp.tabBounds.y) / 2 + dp.tabBounds.height + dp.tabBounds.y)
											dp.splitRange = SplitRange.NORTH;
										else
											dp.splitRange = SplitRange.SOUTH;
									} else if (widthIsBigEnough) {
										// EAST
										dp.splitRange = SplitRange.EAST;
									} else {
										dp.splitRange = SplitRange.NONE;
									}

								} else {
									dp.splitRange = SplitRange.NONE;
								}
							}
						}
						catch(IllegalComponentStateException e1) {
							// do nothing
							LOG.info("mouseDragged: " + e1);
						}

						repaintTabOptimized(dp);
						if (dw != null) {
							dw.setVisible(false);
						}

					} else { // mouse over tab pane is null...

						if (!isMousePositionNearOrInTabbedPane) {
							// draw image and follow mouse
						    if (dw == null) {
						    LOG.debug("Creating Drag Window");
						    	dw = new DragWindow(MainFrame.getFrame(MainFrameTabbedPane.this), dp.tabImage, dp.tabBounds.getSize());
						    }

						    int x = e.getXOnScreen()+dp.xOffset;
						    int y = e.getYOnScreen()+10;
						    LOG.debug("Drag Window follows mouse to " + x + " x " + y);
						    dw.setLocation(x, y);

						    if (!dw.isVisible()) {
						    	dw.setVisible(true);
						    }
						}
					}
				}

				super.mouseDragged(e);
			}
		});

		addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if (countHiddenTabs() > 0 && isMouseOverTabHiddenHint) {
					showHiddenTabsPopup();
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				isMouseOverTabHiddenHint = false;
				for (int i = 1; i < getTabCount(); i++) {
					final Component tabComponent = getTabComponentAt(i);
					if (tabComponent instanceof MainFrameTab.TabTitle) {
						MainFrameTab.TabTitle tabTitle = (MainFrameTab.TabTitle) tabComponent;
						tabTitle.setMouseOverPosition(null);
					}
				}
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				// Gets the tab index based on the mouse position
				int tabNumber = getUI().tabForCoordinate(MainFrameTabbedPane.this, e.getX(), e.getY());

				if (tabNumber > -1) {

					boolean consumed = false;

					final Component tabComponent = getTabComponentAt(tabNumber);
					if (tabComponent instanceof MainFrameTab.TabTitle) {
						final MainFrameTab.TabTitle tabTitle = (MainFrameTab.TabTitle) tabComponent;
						consumed = tabTitle.mouseClicked(new Point(
							e.getX(),
							e.getY()), SwingUtilities.isLeftMouseButton(e));
					}


					if (!consumed && SwingUtilities.isLeftMouseButton(e)
						&& lastClickOnTab + doubleClickSpeed > System.currentTimeMillis()
						&& lastClickOnTabPosition != null) {

						final Rectangle doubleClickArea = new Rectangle(lastClickOnTabPosition.x-10, lastClickOnTabPosition.y-10, 20, 20);
						if (doubleClickArea.contains(e.getPoint())) {
							LOG.debug("DoubleClick on Tab. Maximized=" + maximizedTabs);
							if (maximizedTabs) {
								Main.getInstance().getMainFrame().restoreTabbedPaneContainingArea(MainFrameTabbedPane.this);
							} else {
								Main.getInstance().getMainFrame().maximizeTabbedPane(MainFrameTabbedPane.this);
							}
						}
					}

					lastClickOnTab = System.currentTimeMillis();
					lastClickOnTabPosition = e.getPoint();
				}
			}


			@Override
			public void mouseReleased(MouseEvent e) {
				try {
					if (dp != null && dp.mouseOverTabbedPane != null) {
						if (dp.originTabbedPane == dp.mouseOverTabbedPane && dp.originTabbedPane.getTabCount() == 1) {
							// nothing to do
						} else {
							if (dp.splitRange == SplitRange.NONE) {
								if (dp.originTabbedPane == dp.mouseOverTabbedPane && dp.canceledAdjustTabsTasks == 0) {
									ignoreAdjustTabs = true; // same TabbedPane -> do not adjust tab widths
								}
								Component comp = dp.originTabbedPane.getComponentAt(dp.draggedTabIndex);
								String title = dp.originTabbedPane.getTitleAt(dp.draggedTabIndex);
								dp.originTabbedPane.removeTabAt(dp.draggedTabIndex);

								try {
									if (dp.mouseOverIndex == -1) {
										dp.mouseOverTabbedPane.addTab(title, comp);
										dp.mouseOverTabbedPane.setSelectedIndex(dp.mouseOverTabbedPane.getTabCount() -1);
									} else {
										dp.mouseOverTabbedPane.insertTab(title, null, comp, null, dp.mouseOverIndex);
										dp.mouseOverTabbedPane.setSelectedComponent(comp);
									}
								} catch (ArrayIndexOutOfBoundsException ex) {}

								if (dp.originTabbedPane == dp.mouseOverTabbedPane && dp.canceledAdjustTabsTasks == 0) {
									ignoreAdjustTabs = false;
								}
							} else {
								// Split tab pane...
								Main.getInstance().getMainFrame().splitTabbedPane(
										dp.mouseOverTabbedPane, dp.splitRange, dp.originTabbedPane, dp.draggedTabIndex);
							}
						}
					} else if (dp != null && dw != null && dw.isVisible()) {

						MainFrame.createExternalFrame(dp, e.getLocationOnScreen());
					}

					if (dw != null) {
						dw.setVisible(false);
					}
				} finally {
					dp = null;
					dw = null;
				}
			}
		});
	}

	/**
	 *
	 * @return
	 */
	List<MainFrameTab> getAllTabs() {
		List<MainFrameTab> result = new ArrayList<MainFrameTab>();
		result.addAll(getHiddenTabs());
		for (int i = 0; i < getTabCount(); i++) {
			if (getComponentAt(i) instanceof MainFrameTab) {
				result.add((MainFrameTab) getComponentAt(i));
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
	@Override
	public boolean isEnabledAt(int index) {
		return isMouseOverTabHiddenHint ? false : super.isEnabledAt(index);
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
		final JTabbedPane targetTabbedPane = MainFrame.getPredefinedEntityOpenLocation(entity);
		UIUtils.runCommandLater(MainFrameTabbedPane.this, new Runnable() {
			@Override
			public void run() {
				try {
					NuclosCollectController<?> ncc = NuclosCollectControllerFactory.getInstance().newCollectController(targetTabbedPane, entity, null);
					if(ncc != null) {
						ncc.runViewSingleCollectableWithId(eb.getId());
					}
				}
				catch (CommonBusinessException ex) {
					final String sErrorMsg = CommonLocaleDelegate.getInstance().getMessage(
							"MainController.21","Die Stammdaten k\u00f6nnen nicht bearbeitet werden.");
					Errors.getInstance().showExceptionDialog(MainFrameTabbedPane.this, sErrorMsg, ex);
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
			pm.show(MainFrameTabbedPane.this, thhBounds.x, thhBounds.y + thhBounds.height);
		}
	}

	/**
	 *
	 * @return
	 */
	Rectangle getFirstTabBounds() {
		return getUI().getTabBounds(MainFrameTabbedPane.this, 0);
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
		result.putValue(Action.SHORT_DESCRIPTION, CommonLocaleDelegate.getInstance().getMessage(
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
		result.putValue(Action.SHORT_DESCRIPTION, CommonLocaleDelegate.getInstance().getMessage(
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
		DropTarget drop = new DropTarget(this.lbFirstTabComponent, new NuclosDropTargetListener(this));
		drop.setActive(true);
	}

	/**
	 *
	 */
	private void setupStartTab() {
		final CommonLocaleDelegate cld = CommonLocaleDelegate.getInstance();
		JPanel jpn = new JPanel(new BorderLayout());
		JToolBar toolBar = UIUtils.createNonFloatableToolBar();

		jpn.add(toolBar, BorderLayout.NORTH);
		jpn.add(startTab, BorderLayout.CENTER);

		addTab("", defaultFirstTabIcon, jpn);

		lbFirstTabComponent.setMaximumSize(new Dimension(lbFirstTabComponent.getMaximumSize().width, DEFAULT_TAB_COMPONENT_HEIGHT));
		lbFirstTabComponent.setMinimumSize(new Dimension(lbFirstTabComponent.getMinimumSize().width, DEFAULT_TAB_COMPONENT_HEIGHT));
		lbFirstTabComponent.setPreferredSize(new Dimension(lbFirstTabComponent.getPreferredSize().width, DEFAULT_TAB_COMPONENT_HEIGHT));

		setTabComponentAt(0, lbFirstTabComponent);

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

		final BlackLabel bl = new BlackLabel(jpnTabbedPaneControl, 
				cld.getMessage("MainFrameTabbedPane.3","Tableiste"));
		toolBar.add(bl);

		/**
		 * HOME's
		 */
		final JToggleButton btnHome = new JToggleButton(actionHome);
		btnHome.setFocusable(false);
		if (MainFrame.isStarttabEditable()) {
			toolBar.add(btnHome);
		}
		final JToggleButton btnHomeTree = new JToggleButton(actionHomeTree);
		btnHomeTree.setFocusable(false);
		if (MainFrame.isStarttabEditable()) {
			toolBar.add(btnHomeTree);
		}

		/**
		 * EXTRAS
		 */
		final PopupButton extraButton = new PopupButton(cld.getMessage(
				"PopupButton.Extras","Extras"));

		if (MainFrame.isStarttabEditable()) {
			extraButton.add(startTab.createHeadline(cld.getMessage(
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

		extraButton.add(startTab.createHeadline(cld.getMessage("StartTabPanel.12","Zuletzt angesehen"), null));
		
		if (MainFrame.isStarttabEditable()) {
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
		extraButton.add(startTab.createHeadline(cld.getMessage("StartTabPanel.13","Lesezeichen"), null));
		
		if (MainFrame.isStarttabEditable()) {
			extraButton.add(new ShowHideJCheckBoxMenuItem(startTab.getShowBookmarkAction()));
			extraButton.add(new ShowHideJCheckBoxMenuItem(startTab.getAlwaysHideBookmarkAction()));
		}
		
		extraButton.add(new JMenuItem(startTab.getClearBookmarkAction()));
		if (MainFrame.isStarttabEditable()) {
			extraButton.addSeparator();
			extraButton.add(new JMenuItem(startTab.getActivateDesktopAction()));
		}

		toolBar.add(extraButton);
		
		startTab.addDesktopListener(new DesktopListener() {
			@Override
			public void desktopShowing() {
				extraButton.setVisible(false);
			}
			@Override
			public void desktopHiding() {
				extraButton.setVisible(true);
			}
			@Override
			public void toolbarChange(boolean show) {
				bl.setVisible(show);
				btnHome.setVisible(show);
				btnHomeTree.setVisible(show);
			}
		});
	}

	/**
	 *
	 * @param nuclosTab
	 * @param addToFront
	 */
	public void addTab(MainFrameTab nuclosTab, boolean addToFront) {
		if (addToFront)
			insertTab(nuclosTab.getName(), nuclosTab.getTabIcon(), nuclosTab, nuclosTab.getToolTipText(), 1);
		else
			addTab(nuclosTab.getName(), nuclosTab.getTabIcon(), nuclosTab, nuclosTab.getToolTipText());
	}

	/**
	 *
	 * @param nuclosTab
	 * @param index
	 */
	public void addTab(MainFrameTab nuclosTab, int index) {
		if (index <= 0)
			index = 1;

		if (index >= getTabCount())
			addTab(nuclosTab, false);
		else
			insertTab(nuclosTab.getName(), nuclosTab.getTabIcon(), nuclosTab, nuclosTab.getToolTipText(), index);
	}

	/**
	 *
	 * @param nuclosTab
	 * @return index of tab (could by <0 if tab is hidden)
	 */
	public int getTabIndex(MainFrameTab nuclosTab) {
		final int index = indexOfComponent(nuclosTab);

		return index;
	}

	/**
	 *
	 */
	@Override
	public void insertTab(String title, Icon icon, Component component,	String tip, int index) {
		super.insertTab(title, icon, component, tip, index);
		if (component instanceof MainFrameTab) {
			MainFrameTab tab = (MainFrameTab) component;
			try {
				setTabComponentAt(index, tab.getTabTitle());
				setToolTipTextAt(index, tab.getTabTitle().getToolTipText());
				adjustTabs(tab);
				tab.postAdd();
				tab.notifyAdded();

			} catch (ArrayIndexOutOfBoundsException ex) {
				LOG.error(ex.getMessage(), ex);
			}
		}
	}



	/**
	 *
	 */
	@Override
	public void removeTabAt(int index) {
		super.removeTabAt(index);
		adjustTabs();
	}

	/**
	 *
	 */
	@Override
	public void setSelectedIndex(int index) {
		int oldSelectedIndex = this.getSelectedIndex();
		super.setSelectedIndex(index);
		MainFrame.setActiveTabNavigation(this);

		Component component = getComponentAt(index);
		if (component instanceof MainFrameTab) {
			if(oldSelectedIndex != index)
				((MainFrameTab) component).notifySelected();
		}
	}

	/**
	 * close tabs without adjusting width
	 * @return not closable tabs
	 */
	public List<MainFrameTab> closeAllTabs() {
		return closeAllTabs(null);
	}

	/**
	 * close tabs without adjusting width
	 * @return not closable tabs
	 */
	public List<MainFrameTab> closeAllTabs(MainFrameTab ignoreTab) {
		List<MainFrameTab> notClosableTabs = new ArrayList<MainFrameTab>();

		for (MainFrameTab tab : startTab.getHiddenTabs()) {
			if (tab == ignoreTab) {
				continue;
			}
			if (tab.isClosable()) {
				try {
					tab.notifyClosing();
					startTab.removeHiddenTab(tab);
					tab.notifyClosed();
				} catch(CommonBusinessException e) {
					LOG.debug("Treat tab " + tab + " as non-closable because of " + e);
					notClosableTabs.add(tab);
				}
			} else {
				notClosableTabs.add(tab);
			}
		}
		List<MainFrameTab> tabsToRemove = new ArrayList<MainFrameTab>();
		for (int i = 1; i < getTabCount(); i++) {
			if (getComponentAt(i) instanceof MainFrameTab) {
				MainFrameTab tab = (MainFrameTab) getComponentAt(i);
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
		for (MainFrameTab tab : tabsToRemove) {
			try {
				tab.notifyClosing();
				remove(tab);
				tab.notifyClosed();
			} catch(CommonBusinessException e) {
				LOG.debug("Treat tab " + tab + " as non-closable because of " + e);
				notClosableTabs.add(tab);
			}
		}
		return notClosableTabs;
	}

	/**
	 * only remove... NO close
	 * @param tab
	 */
	public void removeTab(MainFrameTab tab) {
		final int index = indexOfComponent(tab);

		if (index < 0) {
			startTab.removeHiddenTab(tab);
		} else {
			super.removeTabAt(index);
		}

		scheduleAdjustTabs(1250);
	}

	/**
	 *
	 * @param tab
	 * @param mousePosition
	 * @throws CommonBusinessException if tab is not closable
	 */
	public void closeTab(MainFrameTab tab, Point mousePosition) throws CommonBusinessException {
		if (!tab.isClosable() || !tab.notifyClosing()) {
			return;
		}

		final int index = indexOfComponent(tab);

		if (mousePosition == null) {
			// try to get MousePosition from current tab
			final Component tabComponent = getTabComponentAt(index);
			if (tabComponent != null) {
				mousePosition = tabComponent.getMousePosition();
			}
		}

		if (index < 0) {
			startTab.removeHiddenTab(tab);
		} else {
			super.removeTabAt(index);
		}
		tab.notifyClosed();
		adjustTabs(false);

		if (getTabCount() > index) {
			setSelectedIndex(index);
			// setMouseOver on tab at same index
			 if (mousePosition != null) {
				final Component tabComponent = getTabComponentAt(index);
				if (tabComponent instanceof MainFrameTab.TabTitle) {
					final MainFrameTab.TabTitle tabTitle = (MainFrameTab.TabTitle) tabComponent;
					tabTitle.setMouseOverPosition(mousePosition);
				}
			}
		}
		scheduleAdjustTabs(1250);
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
		if ((getTabCount() <= 1 && countHiddenTabs() == 0) || ignoreAdjustTabs)
			return;

		ignoreAdjustTabs = true;
		final int tabbedPaneWidth = getBounds().width - 4; // -4=insets

		// first tab width
		int firstTabWidth;
		try {
			firstTabWidth =getUI().getTabBounds(MainFrameTabbedPane.this, 0).width;
		} catch (java.lang.ArrayIndexOutOfBoundsException ex) {
			firstTabWidth = 0;
		}

		int hideTabs = getTabCount() - 1 - ((tabbedPaneWidth - firstTabWidth) / TAB_WIDTH_MIN);
		if (hideTabs == getTabCount()-1) hideTabs--; // do not hide all content tabs... last man standing ;-)

		// hideTabs * -1 = restoreTabs...
		int restoreTabs = hideTabs * -1;
		if (countHiddenTabs() < restoreTabs) restoreTabs = countHiddenTabs();

		final int contentTabWidth = (tabbedPaneWidth - firstTabWidth) / ((getTabCount()-1) - (hideTabs>0 ? hideTabs : 0) + (restoreTabs > 0 ? restoreTabs : 0));
		LOG.debug("HideTabs="+hideTabs + " content tab width="+contentTabWidth);

		if (restoreTabs > 0) {
			for (MainFrameTab nuclosTab : startTab.removeHiddenTabs(restoreTabs)) {
				addTab(nuclosTab, true);
			}
		}
		if (recalculateWidth) {
			for (int i = 1; i < getTabCount(); i++) {
				Component c = getTabComponentAt(i);
				if (c instanceof MainFrameTab.TabTitle) {
					MainFrameTab.TabTitle tt = (MainFrameTab.TabTitle) c;
					tt.setWidth(TAB_WIDTH_MAX < contentTabWidth ? TAB_WIDTH_MAX : (TAB_WIDTH_MIN > contentTabWidth  ? TAB_WIDTH_MIN : contentTabWidth));
				} else if (c != null) {
					throw new IllegalArgumentException("Unknown Tab Component " + c.getClass().getName() + " at " + i);
				}
			}
		}
		if (hideTabs > 0) {
			int hideUntil = getTabCount()-1 < hideTabs ? getTabCount()-1 : hideTabs;
			int indexToHide = 1;
			for (int i = 0; i < hideUntil; i++) {
				MainFrameTab tabToHide = (MainFrameTab) getComponentAt(indexToHide);
				if (tabToHide == tabForceVisibility) {
					hideUntil++;
					indexToHide++;
				} else {
					startTab.addHiddenTab(tabToHide);
					removeTabAt(indexToHide);
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
			setSelectedIndex(getTabCount()-1);
			return true;
		}
		return false;
	}

	/**
	 *
	 * @return
	 */
	private Rectangle getTabHiddenHintBounds() {
		Rectangle ftBounds = getFirstTabBounds();
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
			Rectangle tpBounds = dp.mouseOverTabbedPane.getBounds();
			int height = dp.mouseOverTabbedPane.getHeight();
			LOG.trace("SplitRange: " + dp.splitRangeBefore + " --> " + dp.splitRange);

			if (dp.splitRange == SplitRange.NONE && dp.splitRangeBefore == SplitRange.NONE)
				height = 35;

			LOG.trace("Repaint TabBar: " + tpBounds.width + " x " + height);
			dp.mouseOverTabbedPane.repaint(0, 0, tpBounds.width, height);
		}
	}

	/**
	 *
	 * @return
	 */
	public int countHiddenTabs() {
		return startTab.countHiddenTabs();
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2 = (Graphics2D) g;

		Rectangle tpBounds = getBounds();

		// draw notice for hidden tabs
		if (countHiddenTabs() > 0) {
			Rectangle ftBounds = getFirstTabBounds();
			ImageIcon icoHint = isMouseOverTabHiddenHint ? Icons.getInstance().getIconTabHiddenHint_hover() : Icons.getInstance().getIconTabHiddenHint();
			g2.drawImage(icoHint.getImage(),
				ftBounds.x+ftBounds.width-icoHint.getIconWidth(),
				ftBounds.y, null);
		}

		// Are we dragging?
		if(dp != null) {

			// Shadow moving tab
			if (MainFrameTabbedPane.this == dp.originTabbedPane) {
				g2.setColor(new Color(120, 120, 120, 120));
				g2.fillRect(dp.tabBounds.x,
							dp.tabBounds.y,
							dp.tabBounds.width,
							dp.tabBounds.height);
			}


			// Draw moving tab or split indicator only if this is mouseover tab pane
			if (MainFrameTabbedPane.this != dp.mouseOverTabbedPane) {
				LOG.trace("this is not the mouseOverTabbedPane " + tpBounds);
				return;
			}


			// Draw indicator for new splitpane
			switch (dp.splitRange) {
				case NONE:
					break;

				case NORTH: {
					int x = insetLeft;
					int y = dp.tabBounds.height + dp.tabBounds.y;
					int w = MainFrameTabbedPane.this.getWidth() - insetLeft - insetRight;
					int h = (MainFrameTabbedPane.this.getHeight() - y) / 2;

					BufferedImage buffImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
					Graphics2D gbi = buffImg.createGraphics();
					gbi.setColor(new Color(120, 120, 120, 120));

					gbi.fillRect(0, 0, w, h);
					gbi.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OUT));
					gbi.setPaint(new Color(0, 0, 0, 255));

					int h2 = w / 3 < h / 3 ? w / 3 : h / 3;
					int w2 = h2;
					int x2 = w / 2 - w2 / 2;
					int y2 = h / 2 - h2 / 4;
					gbi.fillRect(x2 ,y2, w2, h2);

					Polygon p = new Polygon();
					int x3 = w / 2;
					int y3 = h / 2 - h2;
					p.addPoint(x3, y3);
					x3 += w2;
					y3 += h2;
					p.addPoint(x3, y3);
					x3 -= w2 * 2;
					p.addPoint(x3, y3);
					gbi.fillPolygon(p);

					g2.drawImage(buffImg, null, x, y);
					// no more drawing
					return;
				}

				case SOUTH: {
					int x = insetLeft;
					int y = (MainFrameTabbedPane.this.getHeight() - dp.tabBounds.height - dp.tabBounds.y) / 2 + dp.tabBounds.height + dp.tabBounds.y;
					int w = MainFrameTabbedPane.this.getWidth() - insetLeft - insetRight;
					int h = (MainFrameTabbedPane.this.getHeight() - dp.tabBounds.height - dp.tabBounds.y) / 2 - insetBottom;

					BufferedImage buffImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
					Graphics2D gbi = buffImg.createGraphics();
					gbi.setColor(new Color(120, 120, 120, 120));

					gbi.fillRect(0, 0, w, h);
					gbi.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OUT));
					gbi.setPaint(new Color(0, 0, 0, 255));

					int h2 = w / 3 < h / 3 ? w / 3 : h / 3;
					int w2 = h2;
					int x2 = w / 2 - w2 / 2;
					int y2 = h / 2 + h2 / 4 - h2;
					gbi.fillRect(x2 ,y2, w2, h2);

					Polygon p = new Polygon();
					int x3 = w / 2;
					int y3 = h / 2 + h2;
					p.addPoint(x3, y3);
					x3 -= w2;
					y3 -= h2;
					p.addPoint(x3, y3);
					x3 += w2 * 2;
					p.addPoint(x3, y3);
					gbi.fillPolygon(p);

					g2.drawImage(buffImg, null, x, y);
					// no more drawing
					return;
				}

				case WEST: {
					int x = insetLeft;
					int y = dp.tabBounds.height + dp.tabBounds.y;
					int w = MainFrameTabbedPane.this.getWidth() / 2 - insetLeft;
					int h = MainFrameTabbedPane.this.getHeight() - dp.tabBounds.height - dp.tabBounds.y - insetBottom;

					BufferedImage buffImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
					Graphics2D gbi = buffImg.createGraphics();
					gbi.setColor(new Color(120, 120, 120, 120));

					gbi.fillRect(0, 0, w, h);
					gbi.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OUT));
					gbi.setPaint(new Color(0, 0, 0, 255));

					int h2 = w / 3 < h / 3 ? w / 3 : h / 3;
					int w2 = h2;
					int x2 = w / 2 - w2 / 4;
					int y2 = h / 2 - h2 / 2;
					gbi.fillRect(x2 ,y2, w2, h2);

					Polygon p = new Polygon();
					int x3 = w / 2 - w2;
					int y3 = h / 2;
					p.addPoint(x3, y3);
					x3 += w2;
					y3 += h2;
					p.addPoint(x3, y3);
					y3 -= h2 * 2;
					p.addPoint(x3, y3);
					gbi.fillPolygon(p);

					g2.drawImage(buffImg, null, x, y);
					// no more drawing
					return;
				}

				case EAST: {
					int x = MainFrameTabbedPane.this.getWidth() / 2;
					int y = dp.tabBounds.height + dp.tabBounds.y;
					int w = MainFrameTabbedPane.this.getWidth() / 2 - insetRight;
					int h = MainFrameTabbedPane.this.getHeight() - dp.tabBounds.height - dp.tabBounds.y - insetBottom;

					BufferedImage buffImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
					Graphics2D gbi = buffImg.createGraphics();
					gbi.setColor(new Color(120, 120, 120, 120));

					gbi.fillRect(0, 0, w, h);
					gbi.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OUT));
					gbi.setPaint(new Color(0, 0, 0, 255));

					int h2 = w / 3 < h / 3 ? w / 3 : h / 3;
					int w2 = h2;
					int x2 = w / 2 + w2 / 4 - w2;
					int y2 = h / 2 - h2 / 2;
					gbi.fillRect(x2 ,y2, w2, h2);

					Polygon p = new Polygon();
					int x3 = w / 2 + w2;
					int y3 = h / 2;
					p.addPoint(x3, y3);
					x3 -= w2;
					y3 -= h2;
					p.addPoint(x3, y3);
					y3 += h2 * 2;
					p.addPoint(x3, y3);
					gbi.fillPolygon(p);

					g2.drawImage(buffImg, null, x, y);
					// no more drawing
					return;
				}
			}


			// Draw the dragged tab
			int xTab = dp.currentMouseLocation.x + dp.xOffset;
			if (xTab < insetLeft) xTab = insetLeft;
			if (xTab + dp.tabBounds.width > tpBounds.width - insetRight) xTab = tpBounds.width - dp.tabBounds.width - insetRight;

			g2.drawImage(dp.tabImage, xTab, dp.y, this);


			// Draw insert indicator
			if (!(dp.mouseOverTabbedPane == dp.originTabbedPane && dp.draggedTabIndex == dp.mouseOverIndex)) {
				int xIndi = 0;
				int yIndi = 0;

				try {
					int indiIndex = dp.mouseOverIndex;
					if (dp.mouseOverIndex == -1) {
						indiIndex = dp.mouseOverTabbedPane.getTabCount() -1;
					}

					Rectangle moBounds = dp.mouseOverTabbedPane.getUI().getTabBounds(dp.mouseOverTabbedPane, indiIndex);

					if (dp.mouseOverIndex == -1) {
						xIndi += moBounds.width;
					}

					xIndi += moBounds.x;
					yIndi += moBounds.height;

					if (dp.mouseOverTabbedPane == dp.originTabbedPane && dp.mouseOverIndex > dp.draggedTabIndex)
						xIndi += dp.tabBounds.width;

				} catch (ArrayIndexOutOfBoundsException ex) {}
				xIndi = xIndi < 5 ? 5 : xIndi;

				RenderingHints oldRendHints = g2.getRenderingHints();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				g2.setPaint(new GradientPaint(new Point(0, yIndi+5), NuclosThemeSettings.BACKGROUND_ROOTPANE,
											  new Point(0, yIndi+10), new Color(Color.BLUE.getRed(), Color.BLUE.getGreen(),	Color.BLUE.getBlue(), 50)));
				Polygon p = new Polygon();
				p.addPoint(xIndi, 	yIndi);
				p.addPoint(xIndi+5, yIndi+12);
				p.addPoint(xIndi,   yIndi+8);
				p.addPoint(xIndi-5, yIndi+12);
				g2.fillPolygon(p);

				g2.setRenderingHints(oldRendHints);
			}
		}
	}

	@Override
	public void visitDragEnter(DropTargetDragEvent dtde) {}

	@Override
	public void visitDragExit(DropTargetEvent dte) {}

	@Override
	public void visitDragOver(DropTargetDragEvent dtde) {
		MainFrameTabbedPane.this.setSelectedIndex(0);
		dtde.rejectDrag();
	}

	@Override
	public void visitDrop(DropTargetDropEvent dtde) {}

	@Override
	public void visitDropActionChanged(DropTargetDragEvent dtde) {}

	private class ShowHideJCheckBoxMenuItem extends JCheckBoxMenuItem {

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
}