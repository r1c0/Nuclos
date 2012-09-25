package org.nuclos.client.main.mainframe;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.IllegalComponentStateException;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.nuclos.client.common.NuclosDropTargetVisitor;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame.SplitRange;
import org.nuclos.client.main.mainframe.MainFrameTabbedPane.DragParameter;
import org.nuclos.client.main.mainframe.MainFrameTabbedPane.DragWindow;
import org.nuclos.client.main.mainframe.MainFrameTabbedPane.EmptyPanel;
import org.nuclos.client.synthetica.NuclosThemeSettings;
import org.nuclos.client.ui.Icons;
import org.nuclos.common2.SpringLocaleDelegate;

final class MFTabbedPane extends JTabbedPane implements NuclosDropTargetVisitor {
	
	private static final Logger LOG = Logger.getLogger(MFTabbedPane.class);
	
	private static MainFrameTabbedPane.DragParameter dp = null;
	private static MainFrameTabbedPane.DragWindow dw = null;
	
	private static final int insetLeft = 2;
	private static final int insetRight = 2;
	private static final int insetBottom = 2;
	
	private final static Timer resizeTimer = new Timer(MainFrameTabbedPane.class.getName() + " ResizeTimer");
	private TimerTask resizeTimerTask;
	
	private boolean isMouseOverTabHiddenHint = false;
	
	private long lastClickOnTab = 0l;
	private Point lastClickOnTabPosition;
		
	private final MainFrameTabbedPane mainFrameTabbedPane;
	
	public MFTabbedPane(final MainFrameTabbedPane mainFrameTabbedPane) {
		this.mainFrameTabbedPane = mainFrameTabbedPane;
		setMinimumSize(new Dimension(MainFrameTabbedPane.TAB_WIDTH_MIN+4+41, 50)); // 4=inset // 41=firstTab
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
						mainFrameTabbedPane.adjustTabs();
						revalidate();
					}
				};
				try {
					if (MainFrameTabbedPane.RESIZE_AND_ADJUST_IMMEDIATE) {
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
				if (mainFrameTabbedPane.countHiddenTabs() <= 0) {
					isMouseOverTabHiddenHint = false;
				} else {
					Rectangle thhBounds = mainFrameTabbedPane.getTabHiddenHintBounds();
					Polygon poly = new Polygon();
					poly.addPoint(thhBounds.x, thhBounds.y);
					poly.addPoint(thhBounds.x + thhBounds.width, thhBounds.y);
					poly.addPoint(thhBounds.x + thhBounds.width, thhBounds.y + thhBounds.height);
					isMouseOverTabHiddenHint = poly.contains(e.getPoint());
				}

				// Gets the tab index based on the mouse position
				final int tabNumber = MFTabbedPane.this.getUI().tabForCoordinate(MFTabbedPane.this, e.getX(), e.getY());

				mainFrameTabbedPane.tabClosing = -1;
				for (int i = (mainFrameTabbedPane.startTabVisible?1:0); i < getTabCount(); i++) {
					final Component tabComponent = getTabComponentAt(i);
					if (tabComponent instanceof MainFrameTab.TabTitle) {
						final MainFrameTab.TabTitle tabTitle = (MainFrameTab.TabTitle) tabComponent;
						if (i == tabNumber) {
							
							final Point pos = new Point(e.getX(), e.getY());
							if (tabTitle.isMouseOverClose(pos)) {
								mainFrameTabbedPane.tabClosing = tabNumber;
							}
							tabTitle.setMouseOverPosition(pos);
						} else {
							tabTitle.setMouseOverPosition(null);
						}
					}
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {

				if(dp == null) {
					DragParameter dpInit = new DragParameter(mainFrameTabbedPane);

					// Gets the tab index based on the mouse position
					int tabNumber = MFTabbedPane.this.getUI().tabForCoordinate(MFTabbedPane.this, e.getX(), e.getY());

					if(tabNumber >= (mainFrameTabbedPane.startTabVisible?1:0)) {
						if (mainFrameTabbedPane.adjustTabsTimerTask != null) {
							mainFrameTabbedPane.adjustTabsTimerTask.cancel();
						}
						dpInit.canceledAdjustTabsTasks = mainFrameTabbedPane.scheduleAdjustTabsTimer.purge();
						dpInit.currentMouseLocation = new Point(e.getX(), e.getY());
						dpInit.draggedTabIndex = tabNumber;
						dpInit.mouseOverIndex = tabNumber;
						dpInit.mouseOverTabbedPane = mainFrameTabbedPane;
						dpInit.tabBounds = MFTabbedPane.this.getUI().getTabBounds(MFTabbedPane.this, tabNumber);
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
							dpInit.tabBounds.y + dpInit.tabBounds.height+1, MFTabbedPane.this);

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
							Point p = tabbedPane.mfTabbed.getLocationOnScreen();
							Rectangle r = new Rectangle(p);
							Rectangle tpBounds = tabbedPane.mfTabbed.getBounds();
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
							mainFrameTabbedPane.repaintTabOptimized(dp);
						} else {
							if (dp.mouseOverTabbedPane != newMouseOverTabbedPane) {
								// repaint leaving tab pane
								mainFrameTabbedPane.repaintTabOptimized(dp);
							}
						}
					}
					dp.mouseOverTabbedPane = newMouseOverTabbedPane;
					if (dp.mouseOverTabbedPane == null)
						dp.mouseOverIndex = -1;

					if (dp.mouseOverTabbedPane != null) {

						try {
							Point mouseOnScreen = e.getLocationOnScreen();
							Point tabOnScreen = dp.mouseOverTabbedPane.mfTabbed.getLocationOnScreen();

							Point relativeToMouseOverTab = new Point();
							if (dp.mouseOverTabbedPane == dp.originTabbedPane) {
								relativeToMouseOverTab.x = e.getX();
								relativeToMouseOverTab.y = e.getY();
							}
							else {
								relativeToMouseOverTab.x = mouseOnScreen.x - tabOnScreen.x;
								relativeToMouseOverTab.y = mouseOnScreen.y - tabOnScreen.y;
							}

							int tabNumber = dp.mouseOverTabbedPane.mfTabbed.getUI().tabForCoordinate(dp.mouseOverTabbedPane.mfTabbed, relativeToMouseOverTab.x, 10);

							if(tabNumber >= 0) {
								if (dp.mouseOverTabbedPane.startTabVisible) {
									dp.mouseOverIndex = tabNumber==0?1:tabNumber;
								} else {
									dp.mouseOverIndex = tabNumber;
								}
							} else {
								dp.mouseOverIndex = -1;
							}

							LOG.debug("MouseOverIndex: " + dp.mouseOverIndex);

							dp.currentMouseLocation = relativeToMouseOverTab;

							if (!MainFrame.isSplittingDeactivated() && MainFrame.isSplittingEnabled()) {
								// save old splitrange for optimized repaint
								dp.splitRangeBefore = dp.splitRange;
								// check if in splitting range
								if (dp.currentMouseLocation.y > 50) {
									Rectangle tpBounds = dp.mouseOverTabbedPane.mfTabbed.getBounds();
									Dimension minSize = dp.mouseOverTabbedPane.mfTabbed.getMinimumSize();
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

						mainFrameTabbedPane.repaintTabOptimized(dp);
						if (dw != null) {
							dw.setVisible(false);
						}

					} else { // mouse over tab pane is null...

						if (!isMousePositionNearOrInTabbedPane) {
							// draw image and follow mouse
						    if (dw == null) {
						    LOG.debug("Creating Drag Window");
						    	dw = new DragWindow(MainFrame.getFrame(mainFrameTabbedPane), dp.tabImage, dp.tabBounds.getSize());
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
				if (mainFrameTabbedPane.countHiddenTabs() > 0 && isMouseOverTabHiddenHint) {
					mainFrameTabbedPane.showHiddenTabsPopup();
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
				int tabNumber = MFTabbedPane.this.getUI().tabForCoordinate(MFTabbedPane.this, e.getX(), e.getY());
				
				boolean consumed = false;
				if (tabNumber > -1) {
					final Component tabComponent = getTabComponentAt(tabNumber);
					if (tabComponent instanceof MainFrameTab.TabTitle) {
						final MainFrameTab.TabTitle tabTitle = (MainFrameTab.TabTitle) tabComponent;
						final Point pos = new Point(e.getX(), e.getY());
						final boolean left = SwingUtilities.isLeftMouseButton(e);
						
						consumed = tabTitle.mouseClicked(pos, left);
					}

					if (!consumed && SwingUtilities.isLeftMouseButton(e)
						&& lastClickOnTab + mainFrameTabbedPane.DOUBLE_CLICK_SPEED > System.currentTimeMillis()
						&& lastClickOnTabPosition != null) {

						final Rectangle doubleClickArea = new Rectangle(lastClickOnTabPosition.x-10, lastClickOnTabPosition.y-10, 20, 20);
						if (doubleClickArea.contains(e.getPoint())) {
							LOG.debug("DoubleClick on Tab. Maximized=" + mainFrameTabbedPane.maximizedTabs);
							if (mainFrameTabbedPane.maximizedTabs) {
								Main.getInstance().getMainFrame().restoreTabbedPaneContainingArea(mainFrameTabbedPane);
							} else {
								Main.getInstance().getMainFrame().maximizeTabbedPane(mainFrameTabbedPane);
							}
						}
					}

					lastClickOnTab = System.currentTimeMillis();
					lastClickOnTabPosition = e.getPoint();
				}
				
				if (!consumed && SwingUtilities.isRightMouseButton(e)) {
					if (mainFrameTabbedPane.getMainFrameSpringComponent().getMainFrame().isStarttabEditable()) {
						final JMenuItem miHideStartTab = new JMenuItem(
								new AbstractAction(
										SpringLocaleDelegate.getInstance().getMessage("MainFrameTabbedPane.4", "Starttab ausblenden")) {
							@Override
							public void actionPerformed(ActionEvent e) {
								mainFrameTabbedPane.setStartTabVisible(false);
							}
						});
						final JMenuItem miShowStartTab = new JMenuItem(
								new AbstractAction(
										SpringLocaleDelegate.getInstance().getMessage("MainFrameTabbedPane.5", "Starttab einblenden")) {
							@Override
							public void actionPerformed(ActionEvent e) {
								mainFrameTabbedPane.setStartTabVisible(true);
							}
						});
						final JPopupMenu componentPopup = new JPopupMenu() {
							@Override
							public void setVisible(boolean b) {
								if (b) {
									miHideStartTab.setVisible(mainFrameTabbedPane.startTabVisible);
									miShowStartTab.setVisible(!mainFrameTabbedPane.startTabVisible);
								}
								super.setVisible(b);
							}
							
						};
						componentPopup.add(miHideStartTab);
						componentPopup.add(miShowStartTab);
						componentPopup.show(MFTabbedPane.this, e.getX(), e.getY());
					}
				}
			}


			@Override
			public void mouseReleased(MouseEvent e) {
				try {
					if (dp != null && dp.mouseOverTabbedPane != null) {
						if (dp.originTabbedPane == dp.mouseOverTabbedPane && dp.originTabbedPane.mfTabbed.getTabCount() == 1) {
							// nothing to do
						} else {
							if (dp.splitRange == SplitRange.NONE) {
								if (dp.originTabbedPane == dp.mouseOverTabbedPane && dp.canceledAdjustTabsTasks == 0) {
									mainFrameTabbedPane.ignoreAdjustTabs = true; // same TabbedPane -> do not adjust tab widths
								}
								Component comp = dp.originTabbedPane.mfTabbed.getComponentAt(dp.draggedTabIndex);
								String title = dp.originTabbedPane.mfTabbed.getTitleAt(dp.draggedTabIndex);
								dp.originTabbedPane.mfTabbed.removeTabAt(dp.draggedTabIndex);

								try {
									if (dp.mouseOverIndex == -1) {
										dp.mouseOverTabbedPane.mfTabbed.addTab(title, comp);
										dp.mouseOverTabbedPane.mfTabbed.setSelectedIndex(dp.mouseOverTabbedPane.mfTabbed.getTabCount() -1);
									} else {
										dp.mouseOverTabbedPane.mfTabbed.insertTab(title, null, comp, null, dp.mouseOverIndex);
										dp.mouseOverTabbedPane.mfTabbed.setSelectedComponent(comp);
									}
								} catch (ArrayIndexOutOfBoundsException ex) {}

								if (dp.originTabbedPane == dp.mouseOverTabbedPane && dp.canceledAdjustTabsTasks == 0) {
									mainFrameTabbedPane.ignoreAdjustTabs = false;
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

	@Override
	public void visitDragEnter(DropTargetDragEvent dtde) {}

	@Override
	public void visitDragExit(DropTargetEvent dte) {}

	@Override
	public void visitDragOver(DropTargetDragEvent dtde) {
		MFTabbedPane.this.setSelectedIndex(0);
		dtde.rejectDrag();
	}

	@Override
	public void visitDrop(DropTargetDropEvent dtde) {}

	@Override
	public void visitDropActionChanged(DropTargetDragEvent dtde) {}
	
	/**
	 *
	 */
	@Override
	public boolean isEnabledAt(int index) {
		return isMouseOverTabHiddenHint ? false : super.isEnabledAt(index);
	}
	
	/**
	 *
	 * @param nuclosTab
	 * @param addToFront
	 */
	public void addTab(MainFrameTab nuclosTab, boolean addToFront) {
		if (addToFront)
			insertTab(nuclosTab.getName(), nuclosTab.getTabIcon(), nuclosTab, nuclosTab.getToolTipText(), mainFrameTabbedPane.startTabVisible?1:0);
		else
			addTab(nuclosTab.getName(), nuclosTab.getTabIcon(), nuclosTab, nuclosTab.getToolTipText());
	}
	
	/**
	 *
	 * @param nuclosTab
	 * @param index
	 */
	public void addTab(MainFrameTab nuclosTab, int index) {
		if (index <= 0) {
			if (mainFrameTabbedPane.startTabVisible)
				index = 1;
			else
				index = 0; 
		}

		if (index >= getTabCount())
			addTab(nuclosTab, false);
		else
			insertTab(nuclosTab.getName(), nuclosTab.getTabIcon(), nuclosTab, nuclosTab.getToolTipText(), index);
	}
	
	/**
	 *
	 */
	@Override
	public void insertTab(String title, Icon icon, Component component,	String tip, int index) {
		super.insertTab(title, icon, component, tip, index);
		if (!(component instanceof EmptyPanel)){
			final int removedEmptyPanel = removeEmptyPanel();
			if (removedEmptyPanel >=0 && removedEmptyPanel <= index) {
				index--;
			}
		}
		if (component instanceof MainFrameTab) {
			final MainFrameTab tab = (MainFrameTab) component;
			try {
				setTabComponentAt(index, tab.getTabTitle());
				setToolTipTextAt(index, tab.getTabTitle().getToolTipText());
				mainFrameTabbedPane.adjustTabs(tab);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						tab.postAdd();
						tab.notifyAdded();
					}
				});

			} catch (ArrayIndexOutOfBoundsException ex) {
				LOG.error(ex.getMessage(), ex);
			}
		}
	}
	
	/**
	 * 
	 */
	private int removeEmptyPanel() {
		for (int i = 0; i < super.getTabCount(); i++) {
			if (super.getComponentAt(i) instanceof EmptyPanel) {
				super.remove(i);
				return i;
			}
		}
		return -1;
	}
	
	/**
	 *
	 */
	@Override
	public void removeTabAt(int index) {
		removeTabAt(index, true);
	}
	
	public void removeTabAt(int index, boolean withAdjusting) {
		super.removeTabAt(index);
		if (super.getTabCount() == 0) {
			final EmptyPanel ep = new EmptyPanel();
			super.insertTab(null, null, new EmptyPanel(), null, 0);
			super.setTabComponentAt(0, ep.getTabTitle());
		}
		if (withAdjusting) {
			mainFrameTabbedPane.adjustTabs();
		}
	}
	
	/**
	 *
	 */
	@Override
	public void setSelectedIndex(int index) {
		setSelectedIndex(index, false);
	}
	
	/**
	 *
	 */
	public void setSelectedIndex(int index, boolean forceNotify) {
		if (index != mainFrameTabbedPane.tabClosing) {
			int oldSelectedIndex = this.getSelectedIndex();
			super.setSelectedIndex(index);
			MainFrame.setActiveTabNavigation(mainFrameTabbedPane);

			Component component = getComponentAt(index);
			if (component instanceof MainFrameTab) {
				if(forceNotify || oldSelectedIndex != index)
					((MainFrameTab) component).notifySelected();
			}
		} else {
			LOG.debug(String.format("Set selected index to %s refused. Tab is closing.", index));
		}
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2 = (Graphics2D) g;

		Rectangle tpBounds = getBounds();

		// draw notice for hidden tabs
		if (mainFrameTabbedPane.countHiddenTabs() > 0) {
			Rectangle ftBounds = mainFrameTabbedPane.getLastTabBounds();
			ImageIcon icoHint = isMouseOverTabHiddenHint ? Icons.getInstance().getIconTabHiddenHint_hover() : Icons.getInstance().getIconTabHiddenHint();
			g2.drawImage(icoHint.getImage(),
				ftBounds.x+ftBounds.width-icoHint.getIconWidth(),
				ftBounds.y, null);
		}

		// Are we dragging?
		if(dp != null) {

			// Shadow moving tab
			if (mainFrameTabbedPane.mfTabbed == dp.originTabbedPane.mfTabbed) {
				g2.setColor(new Color(80, 80, 80, 180));
				g2.fillRect(dp.tabBounds.x,
							dp.tabBounds.y,
							dp.tabBounds.width,
							dp.tabBounds.height);
			}


			// Draw moving tab or split indicator only if this is mouseover tab pane
			if (mainFrameTabbedPane != dp.mouseOverTabbedPane) {
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
					int w = getWidth() - insetLeft - insetRight;
					int h = (getHeight() - y) / 2;

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
					int y = (getHeight() - dp.tabBounds.height - dp.tabBounds.y) / 2 + dp.tabBounds.height + dp.tabBounds.y;
					int w = getWidth() - insetLeft - insetRight;
					int h = (getHeight() - dp.tabBounds.height - dp.tabBounds.y) / 2 - insetBottom;

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
					int w = getWidth() / 2 - insetLeft;
					int h = getHeight() - dp.tabBounds.height - dp.tabBounds.y - insetBottom;

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
					int x = getWidth() / 2;
					int y = dp.tabBounds.height + dp.tabBounds.y;
					int w = getWidth() / 2 - insetRight;
					int h = getHeight() - dp.tabBounds.height - dp.tabBounds.y - insetBottom;

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
						indiIndex = dp.mouseOverTabbedPane.mfTabbed.getTabCount() -1;
					}

					Rectangle moBounds = null;
					try {
						moBounds = dp.mouseOverTabbedPane.mfTabbed.getUI().getTabBounds(dp.mouseOverTabbedPane.mfTabbed, indiIndex);
					} catch (ArrayIndexOutOfBoundsException ex) {
						// happens if tabbedpane is empty (only start tab)
						moBounds = dp.mouseOverTabbedPane.mfTabbed.getUI().getTabBounds(dp.mouseOverTabbedPane.mfTabbed, --indiIndex);
						xIndi += moBounds.width;
					}

					if (dp.mouseOverIndex == -1) {
						xIndi += moBounds.width;
					}

					xIndi += moBounds.x;
					yIndi += moBounds.height;

					if (dp.mouseOverTabbedPane == dp.originTabbedPane && dp.mouseOverIndex > dp.draggedTabIndex)
						xIndi += dp.tabBounds.width;

				} catch (ArrayIndexOutOfBoundsException ex) {}
				
				if (!dp.mouseOverTabbedPane.startTabVisible && dp.mouseOverTabbedPane.getAllTabs().isEmpty()) {
					// only EmptyPanel is visible
					xIndi = 0;
				}
				
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

	public MainFrameTabbedPane getMainFrameTabbedPane() {
		return mainFrameTabbedPane;
	}		
	
}

