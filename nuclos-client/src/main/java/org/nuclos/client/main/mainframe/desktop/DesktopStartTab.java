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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.TransferHandler;

import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.nuclos.api.ui.DesktopItemFactory;
import org.nuclos.client.common.WorkspaceUtils;
import org.nuclos.client.main.GenericAction;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameSpringComponent;
import org.nuclos.client.main.mainframe.MainFrameUtils;
import org.nuclos.client.main.mainframe.StartTabPanel;
import org.nuclos.client.nuclet.NucletComponentRepository;
import org.nuclos.client.resource.NuclosResourceCache;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.synthetica.NuclosThemeSettings;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.WrapLayout;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.WorkspaceDescription;
import org.nuclos.common.WorkspaceDescription.Desktop;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;

public abstract class DesktopStartTab {

	private static final Logger LOG = Logger.getLogger(DesktopStartTab.class);
	
	private Desktop desktopPrefs;

	private JScrollPane scrollPane;

	private JPanel jpnMain;
	
	private JPopupMenu contextMenu;

	private JPanel jpnContent;
	
	private boolean isSetup = false;

	private DesktopBackgroundPainter desktopBackgroundPainter;
	private final List<DesktopItem> desktopItems = new ArrayList<DesktopItem>();
	
	// former Spring injection
	
	private NucletComponentRepository nucletComponentRepository;
	
	private SpringLocaleDelegate localeDelegate;
	
	private ResourceCache resourceCache;
	
	private MainFrame mainFrame;
	
	private WorkspaceUtils workspaceUtils;
	
	// end of former Spring injection
	
	private Action actAddMenubutton;
	
	private Action actEditDesktop;
	
	private Action actHideDesktop;
	
	private Action actHideToolBar;
	
	private Action actHideTabBar;
	
	private Action actRestoreDesktop;
	
	private Action actRemoveSplitPaneFixations;
	
	private List<org.nuclos.api.ui.DesktopItemFactory> apiDesktopItemFactories;
	
	public DesktopStartTab() {
		setSpringLocaleDelegate(SpringApplicationContextHolder.getBean(SpringLocaleDelegate.class));
		setResourceCache(SpringApplicationContextHolder.getBean(ResourceCache.class));
		setMainFrame(SpringApplicationContextHolder.getBean(MainFrameSpringComponent.class).getMainFrame());
		setWorkspaceUtils(SpringApplicationContextHolder.getBean(WorkspaceUtils.class));
		setNucletComponentRepository(SpringApplicationContextHolder.getBean(NucletComponentRepository.class));
		
		init();
	}
	
	final void init() {
		final SpringLocaleDelegate localeDelegate = getSpringLocaleDelegate();
		actAddMenubutton = new AbstractAction(
				localeDelegate.getMessage("DesktopStartTab.1", "Menu Button hinzufügen"), 
				Icons.getInstance().getIconPlus16()) {
			@Override
			public void actionPerformed(ActionEvent e) {
				final WorkspaceDescription.MenuButton mbPrefs = new WorkspaceDescription.MenuButton();
				_getDesktop();
				cmdAddButton(mbPrefs);
			}
		};
		
		actEditDesktop = new AbstractAction(
				localeDelegate.getMessage("DesktopStartTab.2", "Desktop Eigenschaften"), 
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
					desktopPrefs.setRootpaneBackgroundColor(editor.isRootpaneBackgroundColor());
					desktopPrefs.setStaticMenu(editor.isStaticMenu());
					setupDesktop(Main.getInstance().getMainController().getGenericActions());
					jpnMain.revalidate();
					jpnMain.repaint();
				}
			}
		};
		
		actHideDesktop = new AbstractAction(
				localeDelegate.getMessage("DesktopStartTab.3", "Desktop ausblenden"), 
				Icons.getInstance().getIconEmpty16()) {
			@Override
			public void actionPerformed(ActionEvent e) {
				hide();
			}
		};
		
		actHideToolBar = new AbstractAction(
				localeDelegate.getMessage("DesktopStartTab.4", "Symbolleiste ausblenden",
				Icons.getInstance().getIconEmpty16())) {
			@Override
			public void actionPerformed(ActionEvent e) {
				showToolBar(!MainFrameUtils.isActionSelected(this));
				_getDesktop().setHideToolBar(MainFrameUtils.isActionSelected(this));
			}
		};
		
		actHideTabBar = new AbstractAction(
				localeDelegate.getMessage("DesktopStartTab.6", "Tableiste ausblenden",
				Icons.getInstance().getIconEmpty16())) {
			@Override
			public void actionPerformed(ActionEvent e) {
				showTabBar(!MainFrameUtils.isActionSelected(this));
				actHideDesktop.setEnabled(!MainFrameUtils.isActionSelected(this));
				actHideToolBar.setEnabled(!MainFrameUtils.isActionSelected(this));
				_getDesktop().setHideTabBar(MainFrameUtils.isActionSelected(this));
			}
		};
		
		actRestoreDesktop = new AbstractAction(
				localeDelegate.getMessage("DesktopStartTab.5", "Auf Vorlage zurücksetzen"), 
				Icons.getInstance().getIconUndo16()) {
			@Override
			public void actionPerformed(ActionEvent e) {
				restoreDesktop();
			}
		};
		
		actRemoveSplitPaneFixations = new AbstractAction(
				localeDelegate.getMessage("DesktopStartTab.7", "Bereichsfixierungen aufheben")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				StartTabPanel.removeSplitPaneFixations(jpnMain);
			}
		};
		
		apiDesktopItemFactories = getNucletComponentRepository().getDesktopItemFactories();
				
		this.jpnMain = new JPanel(new BorderLayout(0, 0)){
			@Override
			public void paint(Graphics g) {
				if (desktopBackgroundPainter != null) {
					desktopBackgroundPainter.paint((Graphics2D) g, getWidth(), getHeight());
				}
				super.paint(g);
			}	
		};
		this.jpnMain.setOpaque(false);
		
		contextMenu = new JPopupMenu() {

			@Override
			public void show(Component invoker, int x, int y) {
				if (getMainFrame().isStarttabEditable()) {
					super.show(invoker, x, y);
				}
			}
			
		};
		contextMenu.add(new JMenuItem(actAddMenubutton));
		if (!apiDesktopItemFactories.isEmpty()) {
			JMenu jm = new JMenu(localeDelegate.getResource("DesktopStartTab.8", "Nuclet Komponenten"));
			contextMenu.add(jm);
			for (final org.nuclos.api.ui.DesktopItemFactory dif : CollectionUtils.sorted(apiDesktopItemFactories, new Comparator<org.nuclos.api.ui.DesktopItemFactory>() {
				@Override
				public int compare(DesktopItemFactory o1, DesktopItemFactory o2) {
					return StringUtils.compareIgnoreCase(o1.getLabel(), o2.getLabel());
				}
			})) {
				jm.add(new AbstractAction(
						dif.getLabel()) {
					@Override
					public void actionPerformed(ActionEvent e) {
						final WorkspaceDescription.ApiDesktopItem diPrefs = new WorkspaceDescription.ApiDesktopItem();
						diPrefs.setId(dif.getId());
						_getDesktop();
						cmdAddApiDesktopItem(diPrefs);
					}
				});
			}
		}
		contextMenu.add(new JMenuItem(actEditDesktop));
		contextMenu.addSeparator();
		JCheckBoxMenuItem chckHideToolBar = new JCheckBoxMenuItem(actHideToolBar);
//		chckHideToolBar.setIcon(Icons.getInstance().getIconEmpty16());
		contextMenu.add(chckHideToolBar);
		JCheckBoxMenuItem chckHideTabBar = new JCheckBoxMenuItem(actHideTabBar);
		contextMenu.add(chckHideTabBar);
		contextMenu.add(new JMenuItem(actHideDesktop));
		contextMenu.addSeparator();
		contextMenu.add(new JMenuItem(actRestoreDesktop));
		if (MainFrame.isSplittingEnabled()) {
			contextMenu.addSeparator();
			contextMenu.add(new JMenuItem(actRemoveSplitPaneFixations));
		}
		this.jpnMain.setComponentPopupMenu(contextMenu);
	}
	
	final void setNucletComponentRepository(NucletComponentRepository nucletComponentRepository) {
		this.nucletComponentRepository = nucletComponentRepository;
	}
	
	final NucletComponentRepository getNucletComponentRepository() {
		return nucletComponentRepository;
	}
	
	final void setResourceCache(ResourceCache resourceCache) {
		this.resourceCache = resourceCache;
	}
	
	final ResourceCache getResourceCache() {
		return resourceCache;
	}
	
	final void setSpringLocaleDelegate(SpringLocaleDelegate cld) {
		this.localeDelegate = cld;
	}
	
	final SpringLocaleDelegate getSpringLocaleDelegate() {
		return localeDelegate;
	}
	
	final void setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}
	
	final MainFrame getMainFrame() {
		return mainFrame;
	}
	
	final void setWorkspaceUtils(WorkspaceUtils workspaceUtils) {
		this.workspaceUtils = workspaceUtils;
	}
	
	final WorkspaceUtils getWorkspaceUtils() {
		return workspaceUtils;
	}
	
	public void setDesktopBackgroundPainter(DesktopBackgroundPainter desktopBackgroundPainter) {
		this.desktopBackgroundPainter = desktopBackgroundPainter;
		jpnMain.repaint();
	}
	
	public abstract void hide();
	
	public abstract void showToolBar(boolean show);
	
	public abstract void showTabBar(boolean show);
	
	public abstract void desktopBackgroundChanged(DesktopBackgroundPainter painter);
	
	public void restoreDesktop() {
		try {
			WorkspaceDescription.Desktop restoredPrefs = getWorkspaceUtils().restoreDesktop(desktopPrefs);
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
			showTabBar(!desktopPrefs.isHideTabBar());
			MainFrameUtils.setActionSelected(actHideTabBar, desktopPrefs.isHideTabBar());
			actHideDesktop.setEnabled(!desktopPrefs.isHideTabBar());
			actHideToolBar.setEnabled(!desktopPrefs.isHideTabBar());
			desktopBackgroundChanged(getDesktopBackgroundPainter());
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
				} else if (diPrefs instanceof WorkspaceDescription.ApiDesktopItem) {
					ApiDesktopItem adi = getApiDesktopItem(((WorkspaceDescription.ApiDesktopItem) diPrefs));
					if (adi != null) {
						addDesktopItem(adi);
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public DesktopBackgroundPainter getDesktopBackgroundPainter() {
		Color backgroundColor = desktopPrefs.isRootpaneBackgroundColor()?
				NuclosThemeSettings.BACKGROUND_ROOTPANE : null;
		ImageIcon backgroundImage = null;
		try {
			backgroundImage = null;
			boolean resIcon = false;
			if (desktopPrefs.getResourceBackground() != null) {
				backgroundImage = getResourceCache().getIconResource(desktopPrefs.getResourceBackground());
				resIcon = true;
			}
			if (!resIcon && desktopPrefs.getNuclosResourceBackground() != null) {
				backgroundImage = NuclosResourceCache.getNuclosResourceIcon(desktopPrefs.getNuclosResourceBackground());
			}

		} catch (Exception ex) {
			Log.error(ex);
		}
		return new DesktopBackgroundPainter(backgroundColor, backgroundImage);
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
	
	private void cmdAddApiDesktopItem(final WorkspaceDescription.ApiDesktopItem diPrefs) {
		for (org.nuclos.api.ui.DesktopItemFactory dif : apiDesktopItemFactories) {
			if (LangUtils.equals(diPrefs.getId(), dif.getId())) {
				ApiDesktopItem adi = getApiDesktopItem(diPrefs);
				if (adi != null) {
					addDesktopItem(adi);
					desktopPrefs.addDesktopItem(diPrefs);
				}
			}
		}
	}
	
	private ApiDesktopItem getApiDesktopItem(final WorkspaceDescription.ApiDesktopItem diPrefs) {
		for (org.nuclos.api.ui.DesktopItemFactory dif : apiDesktopItemFactories) {
			if (LangUtils.equals(diPrefs.getId(), dif.getId())) {
				final org.nuclos.api.ui.DesktopItem di = dif.newInstance();
						
				DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(di.getComponent(), DnDConstants.ACTION_MOVE, new DragGestureListener() {
					@Override
					public void dragGestureRecognized(DragGestureEvent dge) {
						if (mainFrame.isStarttabEditable()) {
							Transferable transferable = new DesktopItemTransferable(diPrefs, desktopPrefs);
							dge.startDrag(null, transferable, null);
						}
					}
				});
				
				di.getComponent().setTransferHandler(new DesktopItemTransferHandler(diPrefs, null));
				
				JPopupMenu popup = di.getComponent().getComponentPopupMenu();
				if (popup == null) {
					popup = new JPopupMenu();
					di.getComponent().setComponentPopupMenu(popup);
				}
				
				final ApiDesktopItem adi = new ApiDesktopItem(di);
				
				popup.add(new JMenuItem(new AbstractAction(
						localeDelegate.getMessage("DesktopStartTab.9",
								"Entfernen"), Icons.getInstance().getIconMinus16()) {
							@Override
							public void actionPerformed(ActionEvent e) {
								desktopPrefs.removeDesktopItem(diPrefs);
								removeDesktopItem(adi);
							}
						}));
				
				return adi;
			}
		}
		
		return null;
	}

	private void cmdAddButton(final WorkspaceDescription.MenuButton mbPrefs) {
		MenuButton mb = getMenuButton(mbPrefs, null);
		addDesktopItem(mb);
		desktopPrefs.addDesktopItem(mbPrefs);
	}
	
	private MenuButton getMenuButton(WorkspaceDescription.MenuButton mbPrefs, List<GenericAction> actions) {
		final Color defaultBackroundColor = desktopPrefs.isRootpaneBackgroundColor() ? NuclosThemeSettings.BACKGROUND_ROOTPANE : NuclosThemeSettings.BACKGROUND_PANEL;
		final Color itemTextColor = desktopPrefs.getMenuItemTextColor() == null ?
				(desktopPrefs.isRootpaneBackgroundColor() ? Color.WHITE : NuclosThemeSettings.BACKGROUND_ROOTPANE):
				desktopPrefs.getMenuItemTextColor().toColor();
		final Color itemTextHoverColor = desktopPrefs.getMenuItemTextHoverColor() == null ?
				(desktopPrefs.isRootpaneBackgroundColor() ? NuclosThemeSettings.ICON_BLUE_LIGHTER : Color.BLACK):
				desktopPrefs.getMenuItemTextHoverColor().toColor();
		
		final MenuButton mb = new MenuButton(mbPrefs, actions, 
				defaultBackroundColor,
				desktopPrefs.isStaticMenu(),
				desktopPrefs.getMenuItemTextSize(),
				desktopPrefs.getMenuItemTextHorizontalAlignment(),
				desktopPrefs.getMenuItemTextHorizontalPadding(),
				itemTextColor, 
				itemTextHoverColor,
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
				if (getMainFrame().isStarttabEditable()) {
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
		
		mb.setTransferHandler(new DesktopItemTransferHandler(mbPrefs, mb));

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
	
	private class DesktopItemTransferHandler extends TransferHandler {
		
		private static final long serialVersionUID = 7393959101880598963L;
		
		private final DesktopItem di;
		private final WorkspaceDescription.DesktopItem diPrefs;
		
		public DesktopItemTransferHandler(WorkspaceDescription.DesktopItem diPrefs, DesktopItem di) {
			super();
			this.di = di;
			this.diPrefs = diPrefs;
		}
		
		@Override
		public boolean importData(JComponent comp, Transferable t) {
			if (getMainFrame().isStarttabEditable()) {
				try {
					DesktopItemTransferable.TransferData transferData = (DesktopItemTransferable.TransferData) t.getTransferData(DesktopItemTransferable.DESKTOP_ITEM_FLAVOR);
					if (transferData != null &&
							!LangUtils.equals(transferData.desktopItem, diPrefs)) {
						// only dnd in same desktop						
						if (LangUtils.equals(transferData.desktop, desktopPrefs)) {
							int iTargetIndex = desktopPrefs.getDesktopItems().indexOf(diPrefs);
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
					LOG.error(e);
				} 
			}
			return super.importData(comp, t);
		}
		
		@Override
		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
			if (getMainFrame().isStarttabEditable()) {
				for (DataFlavor dataFlavor : transferFlavors) {
					if (DesktopItemTransferable.DESKTOP_ITEM_FLAVOR.equals(dataFlavor)) {
						if (di instanceof MenuButton) {
							((MenuButton)di).flashLight();
						}
						return true;
					}
				}
			}
			return false;
		}
	}
}
