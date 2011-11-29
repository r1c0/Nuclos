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
package org.nuclos.client.main.mainframe.desktop;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import org.apache.log4j.Logger;
import org.nuclos.client.NuclosIcons;
import org.nuclos.client.main.GenericAction;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MenuActionChooser;
import org.nuclos.client.resource.NuclosResourceCache;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.resource.ResourceDelegate;
import org.nuclos.client.synthetica.NuclosSyntheticaConstants;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.resource.ResourceIconChooser;
import org.nuclos.common.WorkspaceDescription;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LangUtils;

abstract class MenuButton extends DesktopItem implements DragGestureListener {
	
//	private static final Logger LOG = Logger.getLogger(MenuButton.class);
	
	public static final Color HOVER_COLOR = new Color(255, 255, 255, 50);
	
	public static final int ICON_MIN_SIZE = 48;

	private boolean hover = false;
	
	private int flashLight = 0;
	
	private static final int velocity = 30;
	
	private final WorkspaceDescription.MenuButton prefs;

	private final JLabel jlbButton;
	private Action action;
	
	private JWindow popupWindow;
	private JComponent popupContent;
	
	private ImageIcon resourceIcon;
	private ImageIcon resourceIconHover;
	
	private final int itemFontSize;
	private final int itemTextHorizontalAlignment;
	private final int itemTextHorizontalPadding;
	private final Color itemTextColor;
	private final Color itemTextColorHover;
	private final String itemResourceBackground;
	private final String itemResourceBackgroundHover;
	
	private final List<DefaultMenuItem> menuItems = new ArrayList<DefaultMenuItem>();
	
	public MenuButton(final WorkspaceDescription.MenuButton prefs, List<GenericAction> actions, 
			int itemFontSize, int itemTextHorizontalAlignment, int itemTextHorizontalPadding, 
			Color itemTextColor, Color itemTextColorHover, String itemResourceBackground, String itemResourceBackgroundHover) {
		if (prefs == null) {
			throw new IllegalArgumentException("prefs could not be null");
		}
		this.prefs = prefs;
		if (actions == null) {
			actions = Main.getMainController().getGenericActions();
		}
		this.itemFontSize = itemFontSize;
		this.itemTextHorizontalAlignment = itemTextHorizontalAlignment;
		this.itemTextHorizontalPadding = itemTextHorizontalPadding;
		this.itemTextColor = itemTextColor;
		this.itemTextColorHover = itemTextColorHover;
		this.itemResourceBackground = itemResourceBackground;
		this.itemResourceBackgroundHover = itemResourceBackgroundHover;
		
		if (prefs.getMenuAction() != null) {	
			for (GenericAction genAction : actions) {
				if (LangUtils.equals(genAction.x, prefs.getMenuAction())) {
					action = genAction.y.y;
				}
			}
		}
		
		jlbButton = new JLabel() {
			@Override
			public void paint(Graphics g) {
				if (resourceIcon != null) {
					if (resourceIcon.getIconWidth() < ICON_MIN_SIZE && resourceIcon.getIconHeight() < ICON_MIN_SIZE) {
						final int x = Math.max(0, (ICON_MIN_SIZE - resourceIcon.getIconWidth()) / 2);
						final int y = Math.max(0, (ICON_MIN_SIZE - resourceIcon.getIconHeight()) / 2);
						g.drawImage(resourceIcon.getImage(),x,y,this);
					} else {
						g.drawImage(resourceIcon.getImage(),0,0,this);
					}
				} else {
					super.paint(g);
				}
				
				if (hover) {
					if (MenuButton.this.resourceIconHover != null) {
						g.drawImage(MenuButton.this.resourceIconHover.getImage(),0,0,this);
					} else {
						Graphics2D g2 = (Graphics2D) g;
						Object renderingHint = g2
								.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
						g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
								RenderingHints.VALUE_ANTIALIAS_ON);
	
						Rectangle bounds = getBounds();
						g2.setColor(HOVER_COLOR);
						g2.fillRoundRect(0, 0, bounds.width-1, bounds.height-1,
								getWidth() / 16, getHeight() / 16);
	
						g2.setColor(NuclosSyntheticaConstants.BACKGROUND_DARK);
						g2.drawRoundRect(0, 0, bounds.width-1, bounds.height-1,
								getWidth() / 16, getHeight() / 16);
						
						g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
								renderingHint);
						}
				}
				
				if (flashLight > 1) {
					Graphics2D g2 = (Graphics2D) g;
					Object renderingHint = g2
							.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);

					Rectangle bounds = getBounds();
					final Color flashColor = new Color(
							NuclosSyntheticaConstants.BACKGROUND_SPOT.getRed(), 
							NuclosSyntheticaConstants.BACKGROUND_SPOT.getGreen(), 
							NuclosSyntheticaConstants.BACKGROUND_SPOT.getBlue(), 
							flashLight);
					g2.setColor(flashColor);
					g2.fillRoundRect(0, 0, bounds.width-1, bounds.height-1,
							getWidth() / 16, getHeight() / 16);

					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							renderingHint);
				}
			}
		};
		
		for (WorkspaceDescription.MenuItem miPrefs : this.prefs.getMenuItems()) {
			Action menuItemAction = null;
			for (GenericAction genAction : actions) {
				if (LangUtils.equals(genAction.x, miPrefs.getMenuAction())) {
					menuItemAction = genAction.y.y;
				}
			}
			if (menuItemAction != null || MainFrame.isStarttabEditable()) {
				addMenuItem(getDefaultMenuItem(miPrefs, menuItemAction));
			}
		}

		jlbButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mev) {
				if (SwingUtilities.isLeftMouseButton(mev)) {
					if (action != null && SwingUtilities.isLeftMouseButton(mev)) {
						action.actionPerformed(new ActionEvent(jlbButton, 8279, (String) action.getValue(Action.ACTION_COMMAND_KEY)));
					}
				} else if (SwingUtilities.isRightMouseButton(mev)) {
					showContextMenu(mev);
				}
			}

			@Override
			public void mouseEntered(MouseEvent mev) {
				hover = true;
				jlbButton.repaint();
				showMenu();
			}

			@Override
			public void mouseExited(MouseEvent mev) {
				if (popupWindow != null) {
					if (!isMouseOver())
						hideMenu();
				}						
			}
		});
		
		setResourceIcon(prefs.getResourceIcon(), prefs.getNuclosResource());
		setResourceIconHover(prefs.getResourceIconHover(), prefs.getNuclosResourceHover());
		
		DragSource dragSource = DragSource.getDefaultDragSource();
		dragSource.createDefaultDragGestureRecognizer(jlbButton, DnDConstants.ACTION_COPY_OR_MOVE, MenuButton.this);
	}
	
	abstract void remove();
	
	abstract void revalidateParent();

	private void showMenu() {
		if (popupWindow != null) {
			// is showing
			return;
		}
		
//		LOG.info("show menu");
		
		JPanel jpnMenu = new JPanel();
		jpnMenu.setLayout(new BoxLayout(jpnMenu, BoxLayout.Y_AXIS));
		jpnMenu.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		jpnMenu.setOpaque(false);
		
		jpnMenu.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent mev) {
//				LOG.info("mouse over menu");
			}

			@Override
			public void mouseExited(MouseEvent mev) {
				hideMenu(mev);
			}
		});
		
		final JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		split.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent mev) {
				hideMenu(mev);
			}
		});
		
		split.setOpaque(false);
		JPanel emptypanel = new JPanel();
		emptypanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent mev) {
				hideMenu(mev);
			}
		});
		emptypanel.setOpaque(false);
		split.setTopComponent(jpnMenu);
		split.setBottomComponent(emptypanel);

		split.setDividerLocation(0);
		split.setDividerSize(0);
		popupContent = split;
		
		split.setVisible(false);
		Point p = jlbButton.getLocationOnScreen();
		
		popupWindow = new JWindow();
		try {
			com.sun.awt.AWTUtilities.setWindowOpaque(popupWindow, false);
		} catch (Exception ex) {
			// no support for linux.
		}
		popupWindow.setLocation(p.x, p.y + jlbButton.getHeight());
		popupWindow.setFocusable(false);
		
		popupWindow.getContentPane().add(popupContent, BorderLayout.CENTER);
		split.invalidate();
		
		split.setVisible(true);
		
		for (final DefaultMenuItem mi : menuItems) {
			JPanel itemFullSizePanel = new JPanel(new TableLayout(new double[]{TableLayout.FILL}, new double[]{TableLayout.PREFERRED})) {
				@Override
				public Dimension getMaximumSize() {
					Dimension size = new Dimension(super.getMaximumSize().width, mi.getSize().height);
					return size;
				}
			};
			itemFullSizePanel.setOpaque(false);
			itemFullSizePanel.add(mi, "0,0");
			jpnMenu.add(itemFullSizePanel);
		}
		
		popupWindow.setVisible(true);
		popupWindow.pack();
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					double velocity_step = 0.25;
					for(double d = 0.0; d < 1.0; d += velocity_step) {
						split.setDividerLocation(d);
						velocity_step = Math.max(velocity_step / 1.265, 0.01);
						try {
	                        Thread.sleep(velocity);
                        }
                        catch(InterruptedException e1) {
                        	// stop loop
                        	split.setDividerLocation(1.0);
                        	break;
                        }
					}			
				}
				catch (Exception e) {}
			}
		};
		Thread t = new Thread(r);
		t.start();
	}
	
	private boolean isMouseOver() {
		final boolean mouseOverButton;
		final boolean mouseOverMenu;
		final Point mousePoint = MouseInfo.getPointerInfo().getLocation();
		
		final Rectangle buttonScreenBounds = jlbButton.getBounds();
		try {
			buttonScreenBounds.setLocation(jlbButton.getLocationOnScreen());
		} catch (Exception e) {
			return false;
		}
		mouseOverButton = buttonScreenBounds.contains(mousePoint);
//		LOG.info(mousePoint + " --> " + buttonScreenBounds);
		
		if (popupContent != null) {
			final Rectangle menuScreenBounds = popupContent.getBounds();
			try {
				menuScreenBounds.setLocation(popupContent.getLocationOnScreen());
			} catch (Exception e) {
				return false;
			}
			mouseOverMenu = menuScreenBounds.contains(mousePoint);
//			LOG.info(mousePoint + " --> " + menuScreenBounds);
		} else {
			mouseOverMenu = false;
		}

		if (mouseOverButton || mouseOverMenu) {
//			LOG.info("mouse is over");
			return true;
		} else {
//			LOG.info("mouse is NOT over");
			return false; 
		}
	}
	
	public void hideMenu() {
		hideMenu(null);
	}

	private void hideMenu(MouseEvent mev) {
		if (popupWindow != null) {
			if (mev == null || (mev != null && !isMouseOver())) {
//				LOG.info("hide menu");
				popupWindow.setVisible(false);
				popupWindow.removeAll();
				popupWindow.dispose();
				popupWindow = null;
				popupContent = null;
				hover = false;
				jlbButton.repaint();
			}
		}
	}

	private void showContextMenu(MouseEvent mev) {
		if (!MainFrame.isStarttabEditable()) {
			return;
		}
		
		hideMenu();
		
		final JPopupMenu popup = new JPopupMenu();

		popup.add(new JLabel("<html><b>"+CommonLocaleDelegate.getMessage("MenuButton.5", "Eigenschaften")+"</b></html>"));
		
		JMenu myIcons = new JMenu(CommonLocaleDelegate.getMessage("MenuButton.8", "Meine Icons"));
		myIcons.setEnabled(ResourceDelegate.getInstance().containsIconResources());
		final JMenuItem miSelectResourceIcon = new JMenuItem(new AbstractAction(
				CommonLocaleDelegate.getMessage("MenuButton.2",
						"Standard Icon auswählen"), Icons.getInstance().getIconEmpty16()) {
			@Override
			public void actionPerformed(ActionEvent e) {
				ResourceIconChooser iconChooser = new ResourceIconChooser(true);
				iconChooser.showDialog(prefs.getResourceIcon());
				if (iconChooser.isSaved()) {
					setResourceIcon(iconChooser.getSelectedResourceIconName(), null);
				}
			}
		});
		myIcons.add(miSelectResourceIcon);
		final JMenuItem miSelectResourceIconHover = new JMenuItem(new AbstractAction(
				CommonLocaleDelegate.getMessage("MenuButton.7",
						"Mouseover Icon auswählen"), Icons.getInstance().getIconEmpty16()) {
			@Override
			public void actionPerformed(ActionEvent e) {
				ResourceIconChooser iconChooser = new ResourceIconChooser(true);
				iconChooser.showDialog(prefs.getResourceIconHover());
				if (iconChooser.isSaved()) {
					setResourceIconHover(iconChooser.getSelectedResourceIconName(), null);
				}
			}
		});
		myIcons.add(miSelectResourceIconHover);
		popup.add(myIcons);
		
		JMenu nuclosIcons = new JMenu(CommonLocaleDelegate.getMessage("MenuButton.9", "Nuclos Icons"));
		final JMenuItem miSelectNuclosIcon = new JMenuItem(new AbstractAction(
				CommonLocaleDelegate.getMessage("MenuButton.2",
						"Standard Icon auswählen"), Icons.getInstance().getIconEmpty16()) {
			@Override
			public void actionPerformed(ActionEvent e) {
				ResourceIconChooser iconChooser = new ResourceIconChooser(false);
				iconChooser.showDialog(prefs.getNuclosResource());
				if (iconChooser.isSaved()) {
					setResourceIcon(null, iconChooser.getSelectedResourceIconName());
				}
			}
		});
		nuclosIcons.add(miSelectNuclosIcon);
		popup.add(nuclosIcons);
		
		/*final JMenuItem miSelectNuclosResourceIcon = new JMenuItem(new AbstractAction(
				CommonLocaleDelegate.getMessage("DesktopStartTab.2",
						"Ressource Icon auswählen")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				ResourceIconChooser iconChooser = new ResourceIconChooser(false);
				iconChooser.showDialog(prefs.getNuclosResource());
				if (iconChooser.isSaved()) {
					setResourceIcon(null, iconChooser.getSelectedResourceIconName());
				}
			}
		});
		popup.add(miSelectNuclosResourceIcon);*/
		
		final JMenuItem miSelectAction = new JMenuItem(new AbstractAction(
		CommonLocaleDelegate.getMessage("MenuButton.3",
				"Aktion", Icons.getInstance().getIconEmpty16())) {
			@Override
			public void actionPerformed(ActionEvent e) {
				MenuActionChooser actionChooser = new MenuActionChooser();
				actionChooser.showDialog(prefs.getMenuAction());
				if (actionChooser.isSaved()) {
					GenericAction selected = actionChooser.getSelectedAction();
					if (selected == null) {
						prefs.setMenuAction(null);
						setAction(null);
					} else {
						prefs.setMenuAction(selected.x);
						setAction(selected.y.y);
					}
				}
			}
		});
		popup.add(miSelectAction);
		final JMenuItem miAddMenuItem = new JMenuItem(new AbstractAction(
				CommonLocaleDelegate.getMessage("MenuButton.4",
						"Menu Eintrag hinzufügen"), Icons.getInstance().getIconPlus16()) {
					@Override
					public void actionPerformed(ActionEvent e) {
						MenuActionChooser actionChooser = new MenuActionChooser();
						actionChooser.showDialog(null);
						if (actionChooser.isSaved()) {
							cmdAddMenuItem(actionChooser.getSelectedAction());
						}
					}
				});
		popup.add(miAddMenuItem);
		
		popup.addSeparator();
		final JMenuItem miRemove = new JMenuItem(new AbstractAction(
				CommonLocaleDelegate.getMessage("MenuButton.6",
						"Menu Button entfernen"), Icons.getInstance().getIconMinus16()) {
					@Override
					public void actionPerformed(ActionEvent e) {
						remove();
					}
				});
		popup.add(miRemove);

		popup.show(jlbButton, mev.getX(), mev.getY());
	}
	
	public void setResourceIcon(String sResourceIcon, String sNuclosResourceIcon) {
		prefs.setResourceIcon(null);
		prefs.setNuclosResource(null);
		
		if (sResourceIcon != null && sNuclosResourceIcon == null) {
			try {
				resourceIcon = ResourceCache.getIconResource(sResourceIcon);
				applyIconSize();
				prefs.setResourceIcon(sResourceIcon);
				return;
			} catch (Exception ex) {
				// resource not found any more
			}
		}
		
		if (sNuclosResourceIcon != null && sResourceIcon == null) {
			try {
				resourceIcon = NuclosResourceCache.getNuclosResourceIcon(sNuclosResourceIcon);
				applyIconSize();
				prefs.setNuclosResource(sNuclosResourceIcon);
				return;
			} catch (Exception ex) {
				// resource not found any more
			}
		}
		
		resourceIcon = Icons.getInstance().getIconTargetBorder64();
		applyIconSize();
	}
	
	private void applyIconSize() {
		final Dimension size;
		if (resourceIcon != null) {
			if (resourceIcon.getIconWidth() < ICON_MIN_SIZE && resourceIcon.getIconHeight() < ICON_MIN_SIZE) {
				size = new Dimension(ICON_MIN_SIZE, ICON_MIN_SIZE);
			} else {
				size = new Dimension(resourceIcon.getIconWidth(), resourceIcon.getIconHeight());
			}
		} else {
			size = new Dimension(128, 128);
		}
		jlbButton.setSize(size);
		jlbButton.setMinimumSize(size);
		jlbButton.setMaximumSize(size);
		jlbButton.setPreferredSize(size);
		revalidateParent();
	}
	
	public void setResourceIconHover(String sResourceIconHover, String sNuclosResourceIconHover) {
		prefs.setResourceIconHover(null);
		prefs.setNuclosResourceHover(null);
		
		if (sResourceIconHover != null && sNuclosResourceIconHover == null) {
			try {
				resourceIconHover = ResourceCache.getIconResource(sResourceIconHover);
				prefs.setResourceIconHover(sResourceIconHover);
				return;
			} catch (Exception ex) {
				// resource not found any more
			}
		}
		
		if (sNuclosResourceIconHover != null && sResourceIconHover == null) {
			try {
				resourceIconHover = NuclosResourceCache.getNuclosResourceIcon(sNuclosResourceIconHover);
				prefs.setNuclosResourceHover(sNuclosResourceIconHover);
				return;
			} catch (Exception ex) {
				// resource not found any more
			}
		}
		
		resourceIconHover = null;
	}
	
	public void setAction(Action action) {
		this.action = action;
	}
	
	public void cmdAddMenuItem(GenericAction genAction) {
		WorkspaceDescription.MenuItem miPrefs = new WorkspaceDescription.MenuItem();
		Action menuItemAction = null;
		if (genAction != null && genAction.x != null) {
			miPrefs.setMenuAction(genAction.x);
			menuItemAction = genAction.y.y;
		}
		prefs.addMenuItem(miPrefs);
		
		addMenuItem(getDefaultMenuItem(miPrefs, menuItemAction));
		showMenu();
	}
	
	public DefaultMenuItem getDefaultMenuItem(WorkspaceDescription.MenuItem miPrefs, Action action) {
		final DefaultMenuItem mi = new DefaultMenuItem(miPrefs, action, 
				itemFontSize, itemTextHorizontalAlignment, itemTextHorizontalPadding, 
				itemTextColor, itemTextColorHover, itemResourceBackground, itemResourceBackgroundHover) {
			@Override
			boolean isMouseOverMenu() {
				return MenuButton.this.isMouseOver();
			}
			@Override
			void hideMenu() {
				MenuButton.this.hideMenu();
			}
			@Override
			void remove() {
				prefs.removeMenuItem(getPreferences());
				menuItems.remove(this);
				hideMenu();
			}
			@Override
			public void dragGestureRecognized(DragGestureEvent dge) {
				if (MainFrame.isStarttabEditable()) {
					Transferable transferable = new MenuItemTransferable(getPreferences());
					this.setHover(false);
					dge.startDrag(null, transferable, null);
				}
			}
			@Override
			void move() {
				MenuItemMover itemMover = new MenuItemMover(menuItems);
				itemMover.showDialog(this);
				if (itemMover.isSaved()) {
					hideMenu();
					
					List<DefaultMenuItem> newOrderedItems = itemMover.getDefaultMenuItems();
					List<WorkspaceDescription.MenuItem> newPrefMenuItems = new ArrayList<WorkspaceDescription.MenuItem>();
					List<WorkspaceDescription.MenuItem> oldPrefMenuItems = prefs.getMenuItems();
					for (DefaultMenuItem dmi : newOrderedItems) {
						int iOldIndex = menuItems.indexOf(dmi);
						newPrefMenuItems.add(oldPrefMenuItems.get(iOldIndex));
					}
					menuItems.clear();
					menuItems.addAll(newOrderedItems);
					prefs.removeAllMenuItems();
					prefs.addAllMenuItems(newPrefMenuItems);
					
					showMenu();
				}
			}
		};
		
		// No useful drag and drop in JWindow, only in JFrame ;-(
		// May be in later java releases...
//		mi.setTransferHandler(new TransferHandler() {
//			@Override
//			public boolean importData(JComponent comp, Transferable t) {
//				if (MainFrame.isStarttabEditable()) {
//				try {
//					WorkspaceDescription.MenuItem otherPrefs = (WorkspaceDescription.MenuItem) t.getTransferData(MenuItemTransferable.MENU_ITEM_FLAVOR);
//					if (otherPrefs != null &&
//							!LangUtils.equals(otherPrefs, mi.getPreferences())) {
//						hideMenu();
//						int iTargetIndex = prefs.getMenuItems().indexOf(mi.getPreferences());
//						int iSourceIndex = prefs.getMenuItems().indexOf(otherPrefs);
//						DefaultMenuItem dmiSource = menuItems.get(iSourceIndex);
//						WorkspaceDescription.DesktopItem wdmiSource = prefs.getMenuItems().get(iSourceIndex);
//						prefs.removeMenuItem(wdmiSource);
//						prefs.addMenuItem(iTargetIndex, wdmiSource);
//						menuItems.remove(dmiSource);
//						menuItems.add(iTargetIndex, dmiSource);
//						showMenu();
//						return true;
//					}
//				} catch (Exception e) {
//					LOG.error(e);
//				} 
//				}
//				return super.importData(comp, t);
//			}
//			@Override
//			public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
//				if (MainFrame.isStarttabEditable()) {
//				for (DataFlavor dataFlavor : transferFlavors) {
//					if (MenuItemTransferable.MENU_ITEM_FLAVOR.equals(dataFlavor)) {
//						mi.flashLight();
//						return true;
//					}
//				}
//				}
//				return false;
//			}
//		});

		return mi;
	}
	
	public void addMenuItem(DefaultMenuItem mi) {
		menuItems.add(mi);
	}

	public JComponent getJComponent() {
		return jlbButton;
	}

	public WorkspaceDescription.MenuButton getPreferences() {
		return prefs;
	}
	
	public void setTransferHandler(TransferHandler th) {
		jlbButton.setTransferHandler(th);
	}
	
	public void setHover(boolean hover) {
		boolean repaint = false;
		if (this.hover != hover) {
			repaint = true;
			this.hover = hover;
		}
		if (repaint) {
			jlbButton.repaint();
		}
	}
	
	public void flashLight() {
		if (flashLight > 0)
			return;
		
		flashLight = 200;
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					double step = 40;
					for(int i = flashLight; i > 0; i -= step) {
						step = Math.max(step / 2, 10);
						flashLight = i;
						jlbButton.repaint();
						try {
	                        Thread.sleep(velocity);
                        }
                        catch(InterruptedException e1) {
                        	// stop loop
                        	flashLight = 0;
                        	jlbButton.repaint();
                        	break;
                        }
					}
					flashLight = 0;
					jlbButton.repaint();
				}
				catch (Exception e) {
					flashLight = 0;
					jlbButton.repaint();
				}
			}
		};
		Thread t = new Thread(r);
		t.start();
	}
	
}
