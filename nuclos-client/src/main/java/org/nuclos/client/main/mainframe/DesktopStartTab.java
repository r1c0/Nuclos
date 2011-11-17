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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.nuclos.client.NuclosIcons;
import org.nuclos.client.main.mainframe.StartTabPanel.LinkLabel;
import org.nuclos.client.resource.NuclosResourceCache;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.synthetica.NuclosSyntheticaConstants;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.WrapLayout;
import org.nuclos.client.ui.resource.ResourceIconChooser;
import org.nuclos.common2.CommonLocaleDelegate;

public class DesktopStartTab {

	private static final Logger log = Logger.getLogger(DesktopStartTab.class);

	public static final Color HOVER_COLOR = new Color(255, 255, 255, 50);

	private final JScrollPane scrollPane;
	private final JPanel jpnMain;
	private final JPanel jpnContent;
	private final JPanel jpnToolbar;

	private final List<DesktopItem> desktopItems = new ArrayList<DesktopItem>();

	private final LinkLabel llSettings = new LinkLabel(
			new AbstractAction(
					"",
					MainFrame.resizeAndCacheIcon(
							NuclosResourceCache
									.getNuclosResourceIcon("org.nuclos.client.resource.icon.glyphish.19-gear.png"),
							16)) {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdShowSettings();
				}
			}) {
		@Override
		protected List<JMenuItem> getContextMenuItems() {
			return null;
		}
	};

	public DesktopStartTab() {
		this.jpnMain = new JPanel(new BorderLayout(5, 5));
		this.jpnToolbar = new JPanel(new WrapLayout(WrapLayout.RIGHT));
		if (MainFrame.isStarttabEditable()) {
			this.jpnMain.add(jpnToolbar, BorderLayout.NORTH);
		}

		this.jpnContent = new JPanel(new WrapLayout(WrapLayout.LEFT, 40, 40));
		this.scrollPane = new JScrollPane(jpnContent,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.scrollPane.setBorder(BorderFactory.createEmptyBorder());
		this.scrollPane.getVerticalScrollBar().setUnitIncrement(15);
		this.jpnMain.add(this.scrollPane, BorderLayout.CENTER);

		this.initToolBar();
	}

	private void initToolBar() {
		this.jpnToolbar.add(llSettings);
	}

	private void cmdShowSettings() {
		final JPopupMenu popup = new JPopupMenu();

		final JMenuItem miAddButton = new JMenuItem(new AbstractAction(
				CommonLocaleDelegate.getMessage("DesktopStartTab.1",
						"Menu Button hinzufügen"), Icons.getInstance()
						.getIconPlus16()) {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmdAddButton();
			}
		});
		popup.add(miAddButton);

		popup.show(llSettings, 5, 5);
	}

	private void cmdAddButton() {

		MenuButton mb = new MenuButton();
		desktopItems.add(mb);

		this.jpnContent.add(mb.getJComponent());
		this.jpnContent.revalidate();
	}

	public JComponent getJComponent() {
		return this.jpnMain;
	}

	private class MenuButton implements DesktopItem {

		private boolean hover = false;
		
		private static final int velocity = 30;

		private final JLabel jlbButton;

		private String sResourceIcon;

		private JWindow popupWindow;
		private JComponent popupContent;

		public MenuButton() {
			jlbButton = new JLabel(MainFrame.resizeAndCacheIcon(NuclosIcons
					.getInstance().getBigTransparentApplicationIcon512(), 128)) {
				@Override
				public void paint(Graphics g) {
					super.paint(g);
					if (hover) {
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
			};

			jlbButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent mev) {
					if (SwingUtilities.isLeftMouseButton(mev)) {

					} else if (SwingUtilities.isRightMouseButton(mev)) {
						showContextMenu(mev);
					}
				}

				@Override
				public void mouseEntered(MouseEvent mev) {
					hover = true;
					jlbButton.repaint();
					showMenu(mev);
				}

				@Override
				public void mouseExited(MouseEvent mev) {
					if (popupWindow != null) {
						if (!isMouseOverMenu())
							hideMenu();
					}						
				}
			});
		}

		private void showMenu(MouseEvent mev) {
			hideMenu();
			System.out.println("show menu");
			
			JPanel jpnMenu = new JPanel();
			jpnMenu.setLayout(new BoxLayout(jpnMenu, BoxLayout.Y_AXIS));
			jpnMenu.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			jpnMenu.setOpaque(false);
			
			jpnMenu.add(getTestLabel("acquisition"));
			jpnMenu.add(getTestLabel("proposal"));
			jpnMenu.add(getTestLabel("contract"));
			jpnMenu.add(getTestLabel("reservation"));
			jpnMenu.add(getTestLabel("reporting"));

			jpnMenu.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent mev) {
					System.out.println("mouse over menu");
				}

				@Override
				public void mouseExited(MouseEvent mev) {
					hideMenu();
				}
			});
			
			final JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			split.setOpaque(false);
			JPanel emptypanel = new JPanel();
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
			popupWindow.getContentPane().add(popupContent, BorderLayout.CENTER);
			split.invalidate();
			
			split.setVisible(true);
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
		
		private boolean isMouseOverMenu() {
			if (popupWindow == null)
				return false;
			
			Point mousePoint = MouseInfo.getPointerInfo().getLocation();
			final Rectangle screenBounds = popupWindow.getBounds();

			System.out.print(mousePoint + " --> " + screenBounds);
			if (screenBounds.contains(mousePoint)) {
				System.out.println(" mouse is over");
				return true;
			} else {
				System.out.println(" mouse is NOT over");
				return false; 
			}
		}

		private void hideMenu() {
			if (popupWindow != null) {
				System.out.println("hide menu");
				popupWindow.setVisible(false);
				popupWindow.removeAll();
				popupWindow.dispose();
				popupWindow = null;
				popupContent = null;
				hover = false;
				jlbButton.repaint();
			}
		}
		
		private DefaultMenuLabel getTestLabel(String s) {
			return new DefaultMenuLabel(s) {
				@Override
				boolean isMouseOverMenu() {
					return MenuButton.this.isMouseOverMenu();
				}
				@Override
				void hideMenu() {
					MenuButton.this.hideMenu();
				}
			};
		}

		private void showContextMenu(MouseEvent mev) {
			final JPopupMenu popup = new JPopupMenu();

			final JMenuItem miSelectIcon = new JMenuItem(new AbstractAction(
					CommonLocaleDelegate.getMessage("DesktopStartTab.2",
							"Ressource Icon auswählen")) {
				@Override
				public void actionPerformed(ActionEvent e) {
					ResourceIconChooser iconChooser = new ResourceIconChooser(
							true);
					iconChooser.showDialog(sResourceIcon);
					if (iconChooser.isSaved()) {
						setIcon(sResourceIcon = iconChooser
								.getSelectedResourceIconName());
					}
				}
			});
			popup.add(miSelectIcon);

			popup.show(jlbButton, mev.getX(), mev.getY());
		}

		public void setIcon(String sResourceIcon) {
			this.sResourceIcon = sResourceIcon;
			jlbButton.setIcon(ResourceCache.getIconResource(sResourceIcon));
		}

		public JComponent getJComponent() {
			return jlbButton;
		}
	}
	
	private abstract class DefaultMenuLabel extends JLabel{
		private boolean hover = false;

		public DefaultMenuLabel(String text) {
			super(text);
			setForeground(NuclosSyntheticaConstants.BACKGROUND_DARKER);
			setOpaque(false);
			setFont(new Font("Dialog", 0, 20));
			
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent mev) {
					hover = true;
					setForeground(Color.BLACK);
				}

				@Override
				public void mouseExited(MouseEvent mev) {
					hover = false;
					setForeground(NuclosSyntheticaConstants.BACKGROUND_DARKER);
					if (!isMouseOverMenu())
						hideMenu();
				}
			});
		}
		
		abstract boolean isMouseOverMenu();
		
		abstract void hideMenu();

		@Override
		public void paint(Graphics g) {
//			if (hover) {
//				Graphics2D g2 = (Graphics2D) g;
//				Object renderingHint = g2
//						.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
//				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//						RenderingHints.VALUE_ANTIALIAS_ON);
//	
//				Rectangle bounds = getBounds();
//				g2.setColor(NuclosSyntheticaConstants.BACKGROUND_DARK);
//				g2.drawRoundRect(0, 1, bounds.width, bounds.height-2,
//						4, 4);
//	
//				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//						renderingHint);
//			}
//			
			Graphics2D g2 = (Graphics2D) g;
			Object renderingHint = g2
					.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			Rectangle bounds = getBounds();
			g2.setColor(NuclosSyntheticaConstants.DEFAULT_BACKGROUND);
			g2.fillRoundRect(1, 2, bounds.width-2, bounds.height-4,
					4, 4);

			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					renderingHint);
			
			super.paint(g);
		}
	}

	public interface DesktopItem {
		public JComponent getJComponent();
	}

	private static Frame getFrame(Component c) {
		Component w = c;

		while (!(w instanceof Frame) && (w != null)) {
			w = w.getParent();
		}
		return (Frame) w;
	}
}
