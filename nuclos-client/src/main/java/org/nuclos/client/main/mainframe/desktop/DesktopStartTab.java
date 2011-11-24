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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragGestureEvent;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.TransferHandler;

import org.apache.commons.httpclient.util.LangUtils;
import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.nuclos.client.NuclosIcons;
import org.nuclos.client.common.WorkspaceUtils;
import org.nuclos.client.main.GenericAction;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.LinkLabel;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameUtils;
import org.nuclos.client.resource.NuclosResourceCache;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.WrapLayout;
import org.nuclos.common.WorkspaceDescription;
import org.nuclos.common.WorkspaceDescription.Desktop;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;

public abstract class DesktopStartTab {

	private static final Logger LOG = Logger.getLogger(DesktopStartTab.class);
	
	private Desktop desktopPrefs;

	private JScrollPane scrollPane;
	private final JPanel jpnMain;
	private JPanel jpnContent;
	
	private boolean isSetup = false;
	
	private ImageIcon backgroundImage;

	private final List<DesktopItem> desktopItems = new ArrayList<DesktopItem>();
	
	private final Action actAddMenubutton = new AbstractAction(
			CommonLocaleDelegate.getMessage("DesktopStartTab.1", "Menu Button hinzufügen"), 
			Icons.getInstance().getIconPlus16()) {
		@Override
		public void actionPerformed(ActionEvent e) {
			final WorkspaceDescription.MenuButton mbPrefs = new WorkspaceDescription.MenuButton();
			_getDesktop();
			cmdAddButton(mbPrefs);
		}
	};
	
	private final Action actEditDesktop = new AbstractAction(
			CommonLocaleDelegate.getMessage("DesktopStartTab.2", "Desktop Eigenschaften"), 
			Icons.getInstance().getIconEdit16()) {
		@Override
		public void actionPerformed(ActionEvent e) {
			DesktopStartTabEditor editor = new DesktopStartTabEditor(desktopPrefs);
			if (editor.isSaved()) {
				desktopPrefs.setLayout(editor.getLayout());
				desktopPrefs.setHorizontalGap(editor.getHorizontalGap());
				desktopPrefs.setVerticalGap(editor.getVerticalGap());
				desktopPrefs.setMenuItemTextSize(editor.getMenuItemTextSize());
				desktopPrefs.setMenuItemTextHorizontalAlignment(editor.getMenuItemTextHorizontalAlignment());
				desktopPrefs.setMenuItemTextHorizontalPadding(editor.getMenuItemTextHorizontalPadding());
				desktopPrefs.setMenuItemTextColor(editor.getColorMenuItemText());
				desktopPrefs.setMenuItemTextHoverColor(editor.getColorMenuItemTextHover());
				desktopPrefs.setResourceMenuBackground(editor.getResourceMenuBackground());
				desktopPrefs.setResourceMenuBackgroundHover(editor.getResourceMenuBackgroundHover());
				desktopPrefs.setResourceBackground(editor.getResourceBackground());
				setupDesktop(Main.getMainController().getGenericActions());
				jpnMain.revalidate();
				jpnMain.repaint();
			}
		}
	};
	
	private final Action actHideDesktop = new AbstractAction(
			CommonLocaleDelegate.getMessage("DesktopStartTab.3", "Desktop ausblenden"), 
			Icons.getInstance().getIconClearSearch16()) {
		@Override
		public void actionPerformed(ActionEvent e) {
			hide();
		}
	};
	
	private final Action actHideToolBar = new AbstractAction(
			CommonLocaleDelegate.getMessage("DesktopStartTab.4", "Symbolleiste ausblenden")) {
		@Override
		public void actionPerformed(ActionEvent e) {
			showToolBar(!MainFrameUtils.isActionSelected(this));
			_getDesktop().setHideToolBar(MainFrameUtils.isActionSelected(this));
		}
	};
	
	private final Action actRestoreDesktop = new AbstractAction(
			CommonLocaleDelegate.getMessage("DesktopStartTab.5", "Auf Vorlage zurücksetzen"), 
			Icons.getInstance().getIconUndo16()) {
		@Override
		public void actionPerformed(ActionEvent e) {
			restoreDesktop();
		}
	};
	
	private final JPopupMenu contextMenu;

	public DesktopStartTab() {
		this.jpnMain = new JPanel(new BorderLayout(0, 0)){
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (backgroundImage != null) {
					final Rectangle bounds = getBounds();
					final int icoHeight = backgroundImage.getIconHeight();
					final int icoWidth = backgroundImage.getIconWidth();
					for (int i = 0; i < bounds.width / icoWidth + 1; i++) {
						for (int j = 0; j < bounds.height / icoHeight + 1; j++) {
							g.drawImage(backgroundImage.getImage(), i * icoWidth, j * icoHeight, null);
						}
					}
				}
			}
		};
		
		contextMenu = new JPopupMenu() {

			@Override
			public void show(Component invoker, int x, int y) {
				if (MainFrame.isStarttabEditable()) {
					super.show(invoker, x, y);
				}
			}
			
		};
		contextMenu.add(new JMenuItem(actAddMenubutton));
		contextMenu.add(new JMenuItem(actEditDesktop));
		contextMenu.addSeparator();
		contextMenu.add(new JCheckBoxMenuItem(actHideToolBar));
		contextMenu.add(new JMenuItem(actHideDesktop));
		contextMenu.addSeparator();
		contextMenu.add(new JMenuItem(actRestoreDesktop));
		this.jpnMain.setComponentPopupMenu(contextMenu);
	}
	
	public abstract void hide();
	
	public abstract void showToolBar(boolean show);
	
	public void restoreDesktop() {
		try {
			WorkspaceDescription.Desktop restoredPrefs = WorkspaceUtils.restoreDesktop(desktopPrefs);
			setDesktopPreferences(restoredPrefs, null);
		} catch (CommonBusinessException e) {
			Errors.getInstance().showExceptionDialog(getJComponent(), e);
		}
	}
	
	private void setupDesktop() {
		setupDesktop(null);
	}
	
	private void setupDesktop(List<GenericAction> actions) {
		if (desktopPrefs != null) {
			showToolBar(!desktopPrefs.isHideToolBar());
			MainFrameUtils.setActionSelected(actHideToolBar, desktopPrefs.isHideToolBar());
			try {
				backgroundImage = null;
				boolean resIcon = false;
				if (desktopPrefs.getResourceBackground() != null) {
					backgroundImage = ResourceCache.getIconResource(desktopPrefs.getResourceBackground());
					resIcon = true;
				}
				if (!resIcon && desktopPrefs.getNuclosResourceBackground() != null) {
					backgroundImage = NuclosResourceCache.getNuclosResourceIcon(desktopPrefs.getNuclosResourceBackground());
				}
			} catch (Exception ex) {
				Log.error(ex);
			}
			if (this.scrollPane != null) {
				this.jpnMain.remove(this.scrollPane);
				this.scrollPane.removeAll();
				this.jpnContent.removeAll();
				this.scrollPane.setComponentPopupMenu(null);
			}
			desktopItems.clear();
			
			switch (desktopPrefs.getLayout()) {
			case Desktop.LAYOUT_WRAP:
				 this.jpnContent = new JPanel(new WrapLayout(WrapLayout.LEFT, 
						 desktopPrefs.getHorizontalGap(), desktopPrefs.getVerticalGap()));
				this.scrollPane = new JScrollPane(jpnContent);
				this.scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
				this.scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				this.scrollPane.getVerticalScrollBar().setUnitIncrement(15);
				break;
			
			case Desktop.LAYOUT_ONE_ROW:
			default:
				this.jpnContent = new JPanel();
				final BoxLayout blContent = new BoxLayout(jpnContent, BoxLayout.X_AXIS);
				this.jpnContent.setLayout(blContent);
				this.jpnContent.add(Box.createHorizontalGlue());
				
				final JPanel jpnScroll = new JPanel();
				jpnScroll.setOpaque(false);
				jpnScroll.setBorder(BorderFactory.createEmptyBorder(desktopPrefs.getVerticalGap(), desktopPrefs.getHorizontalGap(), 0, 0));
				jpnScroll.add(jpnContent, BorderLayout.NORTH);
				this.scrollPane = new JScrollPane(jpnScroll);
				this.scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
				this.scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				this.scrollPane.getHorizontalScrollBar().setUnitIncrement(15);
				break;
			}
			
			this.scrollPane.setComponentPopupMenu(contextMenu);
			this.scrollPane.setBorder(BorderFactory.createEmptyBorder());
			this.scrollPane.setOpaque(false);
			this.scrollPane.getViewport().setOpaque(false);
			this.jpnContent.setOpaque(false);
			this.jpnMain.add(this.scrollPane, BorderLayout.CENTER);
			
			isSetup = true;
			
			for (WorkspaceDescription.DesktopItem diPrefs : desktopPrefs.getDesktopItems()) {
				if (diPrefs instanceof WorkspaceDescription.MenuButton) {
					WorkspaceDescription.MenuButton mbPrefs = (WorkspaceDescription.MenuButton) diPrefs;
					MenuButton mb = getMenuButton(mbPrefs, actions);
					addDesktopItem(mb);
				}
			}
		}
	}
	
	private void revalidateDesktopItems() {
		for (DesktopItem di : desktopItems) {
			for (Component c : di.getAdditionalComponents()) {
				jpnContent.remove(c);
			}
			jpnContent.remove(di.getJComponent());
		}
		for (DesktopItem di : desktopItems) {
			addDesktopItem(di);
		}
		jpnContent.revalidate();
	}
	
	
	private void addDesktopItem(DesktopItem di) {
		final Desktop desktop = _getDesktop();
		if (!isSetup) {
			setupDesktop();
		}

		if (!desktopItems.contains(di)) {
			desktopItems.add(di);
		}
		
		switch (desktop.getLayout()) {
		case Desktop.LAYOUT_ONE_ROW:
			this.jpnContent.add(di.getJComponent());
			this.jpnContent.add(di.addAdditionalComponent(Box.createRigidArea(new Dimension(desktop.getHorizontalGap(),0))));
			//this.jpnContent.add(di.addAdditionalComponent(Box.createHorizontalGlue()));
			break;
			
		case Desktop.LAYOUT_WRAP:
		default:
			this.jpnContent.add(di.getJComponent());
			break;
		}
		
		this.jpnContent.revalidate();
	}
	
	private void removeDesktopItem(DesktopItem di) {
		desktopItems.remove(di);
		
		for (Component c : di.getAdditionalComponents()) {
			this.jpnContent.remove(c);
		}
		this.jpnContent.remove(di.getJComponent());
		
		this.jpnContent.revalidate();
//		this.jpnContent.repaint();
	}

	private void cmdAddButton(final WorkspaceDescription.MenuButton mbPrefs) {
		MenuButton mb = getMenuButton(mbPrefs, null);
		addDesktopItem(mb);
		desktopPrefs.addDesktopItem(mbPrefs);
	}
	
	private MenuButton getMenuButton(WorkspaceDescription.MenuButton mbPrefs, List<GenericAction> actions) {
		final MenuButton mb = new MenuButton(mbPrefs, actions, 
				desktopPrefs.getMenuItemTextSize(),
				desktopPrefs.getMenuItemTextHorizontalAlignment(),
				desktopPrefs.getMenuItemTextHorizontalPadding(),
				desktopPrefs.getMenuItemTextColor()==null?null:desktopPrefs.getMenuItemTextColor().toColor(), 
				desktopPrefs.getMenuItemTextHoverColor()==null?null:desktopPrefs.getMenuItemTextHoverColor().toColor(),
				desktopPrefs.getResourceMenuBackground(), 
				desktopPrefs.getResourceMenuBackgroundHover()) {
			@Override
			void remove() {
				this.hideMenu();
				desktopPrefs.removeDesktopItem(getPreferences());
				removeDesktopItem(this);
			}
			@Override
			public void dragGestureRecognized(DragGestureEvent dge) {
				if (MainFrame.isStarttabEditable()) {
					Transferable transferable = new DesktopItemTransferable(getPreferences(), desktopPrefs);
					this.setHover(false);
					dge.startDrag(null, transferable, null);
				}
			}
			@Override
			void revalidateParent() {
				jpnMain.revalidate();
				jpnMain.repaint();
			}
		};
		
		mb.setTransferHandler(new TransferHandler() {
			@Override
			public boolean importData(JComponent comp, Transferable t) {
				if (MainFrame.isStarttabEditable()) {
					try {
						DesktopItemTransferable.TransferData transferData = (DesktopItemTransferable.TransferData) t.getTransferData(DesktopItemTransferable.DESKTOP_ITEM_FLAVOR);
						if (transferData != null &&
								!LangUtils.equals(transferData.desktopItem, mb.getPreferences())) {
							// only dnd in same desktop						
							if (LangUtils.equals(transferData.desktop, desktopPrefs)) {
								int iTargetIndex = desktopPrefs.getDesktopItems().indexOf(mb.getPreferences());
								int iSourceIndex = desktopPrefs.getDesktopItems().indexOf(transferData.desktopItem);
								if (iTargetIndex != -1 && iSourceIndex != -1) {
									DesktopItem diSource = desktopItems.get(iSourceIndex);
									WorkspaceDescription.DesktopItem wddiSource = desktopPrefs.getDesktopItems().get(iSourceIndex);
									desktopPrefs.removeDesktopItem(wddiSource);
									desktopPrefs.addDesktopItem(iTargetIndex, wddiSource);
									desktopItems.remove(diSource);
									desktopItems.add(iTargetIndex, diSource);
									revalidateDesktopItems();
									return true;
								} else {
									LOG.warn("iSourceIndex=" + iSourceIndex + ", iTargetIndex=" + iTargetIndex);
									return false;
								}
							}
						}
					} catch (Exception e) {
						Log.error(e);
					} 
				}
				
				return super.importData(comp, t);
			}
			@Override
			public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
				if (MainFrame.isStarttabEditable()) {
					for (DataFlavor dataFlavor : transferFlavors) {
						if (DesktopItemTransferable.DESKTOP_ITEM_FLAVOR.equals(dataFlavor)) {
							mb.flashLight();
							return true;
						}
					}
				}
				return false;
			}
		});

		return mb;
	}

	public JComponent getJComponent() {
		return this.jpnMain;
	}
	
	private Desktop _getDesktop() {
		if (desktopPrefs == null) {
			desktopPrefs = new Desktop();
			desktopPrefs.setLayout(Desktop.LAYOUT_ONE_ROW);
			desktopPrefs.setHorizontalGap(20);
			desktopPrefs.setVerticalGap(20);
			desktopPrefs.setMenuItemTextHorizontalAlignment(WorkspaceDescription.Desktop.HORIZONTAL_ALIGNMENT_LEFT);
			setupDesktop();
		}
		return desktopPrefs;
	}

	public Desktop getDesktopPreferences() {
		return _getDesktop();
	}
	
	public void setDesktopPreferences(Desktop desktop, List<GenericAction> actions) {
		this.desktopPrefs = desktop;
		setupDesktop(actions);
	}
}
