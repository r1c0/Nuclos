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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.nuclos.client.main.GenericAction;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MenuActionChooser;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.synthetica.NuclosThemeSettings;
import org.nuclos.client.ui.Icons;
import org.nuclos.common.WorkspaceDescription;
import org.nuclos.common2.SpringLocaleDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

@Configurable
abstract class DefaultMenuItem extends JLabel implements DragGestureListener {
	
	private static final Logger LOG = Logger.getLogger(DefaultMenuItem.class);
	
	private boolean hover = false;
	
	private int flashLight = 0;
	
	private static final int velocity = 30;
	
	private final WorkspaceDescription.MenuItem prefs;
	private Action action;
	
	private final int itemFontSize;
	private final int itemTextHorizontalAlignment;
	private final int itemTextHorizontalPadding;
	private final Color itemTextColor;
	private final Color itemTextColorHover;
	private final ImageIcon itemBackground;
	private final ImageIcon itemBackgroundHover;
	
	// Spring injection
	
	private MainFrame mainFrame;
	
	// end of Spring injection
	
	public DefaultMenuItem(WorkspaceDescription.MenuItem prefs, Action action, 
			int itemFontSize, int itemTextHorizontalAlignment, int itemTextHorizontalPadding, 
			Color itemTextColor, Color itemTextColorHover, String itemResourceBackground, String itemResourceBackgroundHover) {
		this.prefs = prefs;
		setOpaque(false);
		setAction(action);
		this.itemFontSize = itemFontSize;
		this.itemTextHorizontalAlignment = itemTextHorizontalAlignment;
		this.itemTextHorizontalPadding = itemTextHorizontalPadding;
		this.itemTextColor = itemTextColor;
		this.itemTextColorHover = itemTextColorHover;
		
		ImageIcon resIconBackground = null;
		try {
			if (itemResourceBackground != null) {
				resIconBackground = ResourceCache.getInstance().getIconResource(itemResourceBackground);
				Dimension size = new Dimension(resIconBackground.getIconWidth(),resIconBackground.getIconHeight());
				setSize(size);
				setMinimumSize(size);
				setMaximumSize(size);
				setPreferredSize(size);
			}
		} catch (Exception ex) {
			LOG.error(ex);
		}
		itemBackground = resIconBackground;
		
		ImageIcon resIconBackgroundHover = null;
		try {
			if (itemResourceBackgroundHover != null) {
				resIconBackgroundHover = ResourceCache.getInstance().getIconResource(itemResourceBackgroundHover);
			}
		} catch (Exception ex) {
			LOG.error(ex);
		}
		itemBackgroundHover = resIconBackgroundHover;
		
		setFont(new Font(Font.DIALOG, Font.PLAIN, this.itemFontSize==0?20:this.itemFontSize));
		setHorizontalAlignment(this.itemTextHorizontalAlignment);
		if (this.itemTextHorizontalPadding > 0) {
			switch (this.itemTextHorizontalAlignment) {
			case SwingConstants.LEFT:
				setBorder(BorderFactory.createEmptyBorder(0, itemTextHorizontalPadding, 0, 0));
				break;
			case SwingConstants.RIGHT:
				setBorder(BorderFactory.createEmptyBorder(0, 0, 0, itemTextHorizontalPadding));
				break;
			}
		}
		if (itemTextColor != null) {
			setForeground(itemTextColor);
		} else {
			setForeground(NuclosThemeSettings.BACKGROUND_ROOTPANE);
		}
		
		initListener();
		DragSource dragSource = DragSource.getDefaultDragSource();
		dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
	}
	
	@Autowired
	final void setMainFrame(@Value("#{mainFrameSpringComponent.mainFrame}") MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}
	
	@Override
	public void setForeground(Color fg) {
		super.setForeground(fg);
		// always repaint
		repaint();
	}
	
	private void initListener() {
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mev) {
				if (SwingUtilities.isLeftMouseButton(mev)) {
					if (action != null && SwingUtilities.isLeftMouseButton(mev)) {
						hideMenu();
						hover = false;
						if (itemTextColor != null) {
							setForeground(itemTextColor);
						} else {
							setForeground(NuclosThemeSettings.BACKGROUND_ROOTPANE);
						}
						action.actionPerformed(new ActionEvent(this, 8279, (String) action.getValue(Action.ACTION_COMMAND_KEY)));
					}
				} else if (SwingUtilities.isRightMouseButton(mev)) {
					showContextMenu(mev);
				}
			}
			
			@Override
			public void mouseEntered(MouseEvent mev) {
				hover = true;
				if (itemTextColorHover != null) {
					setForeground(itemTextColorHover);
				} else {
					setForeground(Color.BLACK);
				}
			}

			@Override
			public void mouseExited(MouseEvent mev) {
				hover = false;
				if (itemTextColor != null) {
					setForeground(itemTextColor);
				} else {
					setForeground(NuclosThemeSettings.BACKGROUND_ROOTPANE);
				}
				if (!isMouseOverMenu())
					hideMenu();
			}
		});
	}
	
	abstract boolean isMouseOverMenu();
	
	abstract void hideMenu();
	
	abstract void remove();
	
	abstract void move();
	
	private void showContextMenu(MouseEvent mev) {	
		if (!mainFrame.isStarttabEditable()) {
			return;
		}
		
		final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
		final JPopupMenu popup = new JPopupMenu();
		
		popup.add(new JLabel("<html><b>"+ localeDelegate.getMessage("DefaultMenuItem.4", "Eigenschaften")+"</b></html>"));
		final JMenuItem miPosition = new JMenuItem(new AbstractAction(
				localeDelegate.getMessage("DefaultMenuItem.5",
						"Position"), Icons.getInstance().getIconEmpty16()) {
					@Override
					public void actionPerformed(ActionEvent e) {
						move();
					}
				});
		popup.add(miPosition);
				
		final JMenuItem miSelectAction = new JMenuItem(new AbstractAction(
		localeDelegate.getMessage("DefaultMenuItem.1",
				"Aktion"), Icons.getInstance().getIconEmpty16()) {
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
		
		popup.addSeparator();
		final JMenuItem miRemove = new JMenuItem(new AbstractAction(
				localeDelegate.getMessage("DefaultMenuItem.3",
						"Menu Eintrag entfernen"), Icons.getInstance().getIconMinus16()) {
					@Override
					public void actionPerformed(ActionEvent e) {
						remove();
					}
				});
		popup.add(miRemove);

		popup.show(this, mev.getX(), mev.getY());
	}
	
	public Action getAction() {
		return action;
	}
	
	public void setAction(Action action) {
		this.action = action;
		this.setText(action==null?
				SpringLocaleDelegate.getInstance().getMessage(
						"DefaultMenuItem.2", "Aktion nicht gefunden"):(String) action.getValue(Action.NAME));
	}
	
	public void setHover(boolean hover) {
		boolean repaint = false;
		if (this.hover != hover) {
			repaint = true;
			this.hover = hover;
			if (itemTextColorHover != null) {
				setForeground(itemTextColorHover);
			} else {
				setForeground(Color.BLACK);
			}
		}
		if (repaint) {
			repaint();
		}
	}

	@Override
	public void paint(Graphics g) {
//		if (hover) {
//			Graphics2D g2 = (Graphics2D) g;
//			Object renderingHint = g2
//					.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
//			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//					RenderingHints.VALUE_ANTIALIAS_ON);
//
//			Rectangle bounds = getBounds();
//			g2.setColor(NuclosSyntheticaConstants.BACKGROUND_DARK);
//			g2.drawRoundRect(0, 1, bounds.width, bounds.height-2,
//					4, 4);
//
//			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//					renderingHint);
//		}
//		
		
		if (itemBackgroundHover != null && hover) {
			g.drawImage(itemBackgroundHover.getImage(),0,0,this);
		} else if (itemBackground != null) {
			g.drawImage(itemBackground.getImage(),0,0,this);
		} else {
//			Graphics2D g2 = (Graphics2D) g;
//			Object renderingHint = g2
//					.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
//			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//					RenderingHints.VALUE_ANTIALIAS_ON);
//	
//			Rectangle bounds = getBounds();
//			g2.setColor(NuclosThemeSettings.BACKGROUND_PANEL);
//			
//			int x = 1;
//			int width = bounds.width-2;
//			
//			if (this.itemTextHorizontalPadding > 0) {
//				switch (this.itemTextHorizontalAlignment) {
//				case SwingConstants.LEFT:
//					x += itemTextHorizontalPadding;
//					width -= itemTextHorizontalPadding;
//					break;
//				case SwingConstants.RIGHT:
//					width -= itemTextHorizontalPadding;
//					break;
//				}
//			}
//			
//			g2.fillRoundRect(x, 2, width, bounds.height-4,
//					4, 4);
//	
//			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//					renderingHint);
		}
		
		super.paint(g);
		
		if (flashLight > 1) {
			Graphics2D g2 = (Graphics2D) g;
			Object renderingHint = g2
					.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			Rectangle bounds = getBounds();
			final Color flashColor = new Color(
					NuclosThemeSettings.BACKGROUND_COLOR4.getRed(), 
					NuclosThemeSettings.BACKGROUND_COLOR4.getGreen(), 
					NuclosThemeSettings.BACKGROUND_COLOR4.getBlue(), 
					flashLight);
			g2.setColor(flashColor);
			g2.fillRoundRect(0, 0, bounds.width-1, bounds.height-1,
					getWidth() / 16, getHeight() / 16);

			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					renderingHint);
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
						repaint();
						try {
	                        Thread.sleep(velocity);
                        }
                        catch(InterruptedException e1) {
                        	// stop loop
                        	flashLight = 0;
                        	repaint();
                        	break;
                        }
					}
					flashLight = 0;
					repaint();
				}
				catch (Exception e) {
					flashLight = 0;
					repaint();
				}
			}
		};
		Thread t = new Thread(r, "DefaultMenuItem.flashLight");
		t.start();
	}
	
	public WorkspaceDescription.MenuItem getPreferences() {
		return prefs;
	}
}
