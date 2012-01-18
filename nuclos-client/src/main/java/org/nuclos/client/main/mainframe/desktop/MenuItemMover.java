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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import org.apache.log4j.Logger;
import org.nuclos.client.main.Main;
import org.nuclos.client.synthetica.NuclosThemeSettings;
import org.nuclos.client.ui.Icons;
import org.nuclos.common2.CommonLocaleDelegate;

public class MenuItemMover extends JPanel {

	private static final Logger LOG = Logger.getLogger(MenuItemMover.class);
	
	private final JList list;
	
	final List<DefaultMenuItem> menuItems;
	
	private boolean saved;
	
	public MenuItemMover(final List<DefaultMenuItem> _menuItems) {
		setLayout(new BorderLayout());
		this.menuItems = new ArrayList<DefaultMenuItem>(_menuItems);
		
		list = new JList(new AbstractListModel() {
            public int getSize() { 
            	return menuItems.size(); 
            }
            public Object getElementAt(int i) { 
            	return menuItems.get(i); 
            }
        }) {

			@Override
			public int getScrollableUnitIncrement(Rectangle visibleRect,
                                                  int orientation,
                                                  int direction) {
                int row;
                if (orientation == SwingConstants.VERTICAL &&
                      direction < 0 && (row = getFirstVisibleIndex()) != -1) {
                    Rectangle r = getCellBounds(row, row);
                    if ((r.y == visibleRect.y) && (row != 0))  {
                        Point loc = r.getLocation();
                        loc.y--;
                        int prevIndex = locationToIndex(loc);
                        Rectangle prevR = getCellBounds(prevIndex, prevIndex);

                        if (prevR == null || prevR.y >= r.y) {
                            return 0;
                        }
                        return prevR.height;
                    }
                }
                return super.getScrollableUnitIncrement(
                                visibleRect, orientation, direction);
            }
        };
        
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setCellRenderer(new ListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				DefaultMenuItem defMenuItem = (DefaultMenuItem) value;
				JPanel btnPanel = new JPanel(new BorderLayout());
				btnPanel.setOpaque(false);
				
				JLabel jlbMenuItem = new JLabel((String) defMenuItem.getAction().getValue(Action.NAME), Icons.getInstance().getTableMoveIndicator(), SwingConstants.LEFT);
				jlbMenuItem.setOpaque(false);
				btnPanel.add(jlbMenuItem, BorderLayout.CENTER);	
				
				//if (index > 0) btn.setToolTipText(ResourceIconChooser.this.getResourceIconName(index));
				
				if (isSelected) {
					jlbMenuItem.setOpaque(true);
					jlbMenuItem.setBackground(NuclosThemeSettings.BACKGROUND_COLOR4);
					btnPanel.setBorder(BorderFactory.createLineBorder(NuclosThemeSettings.BACKGROUND_ROOTPANE, 1));
				} else {
					jlbMenuItem.setOpaque(false);
					btnPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
				}
				
				return btnPanel;
			}
		});
        list.setVisibleRowCount(-1);
        DragSource dragSource = DragSource.getDefaultDragSource();
		dragSource.createDefaultDragGestureRecognizer(list, DnDConstants.ACTION_COPY_OR_MOVE, new DefaultMenuItemDragGestureListener());
        list.setTransferHandler(new DefaultMenuItemTransferHandler());
        list.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(250, 80));
        listScroller.setAlignmentX(LEFT_ALIGNMENT);
        
        add(listScroller, BorderLayout.CENTER);
	}
	
	public List<DefaultMenuItem> getDefaultMenuItems() {
		return menuItems;
	}
	
	public void showDialog(DefaultMenuItem selected) {
		final CommonLocaleDelegate cld = CommonLocaleDelegate.getInstance();
		final JDialog dialog = new JDialog(Main.getInstance().getMainFrame(), 
				cld.getMessage("MenuItemMover.3","Reihenfolge ändern"), true);
		
		JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		contentPanel.add(this, BorderLayout.CENTER);
		
		JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
		JButton btSave = new JButton(cld.getMessage("MenuItemMover.1","Speichern"));
		JButton btCancel = new JButton(cld.getMessage("MenuItemMover.2","Abbrechen"));
		actionsPanel.add(btSave);
		actionsPanel.add(btCancel);
		contentPanel.add(actionsPanel, BorderLayout.SOUTH);
		
		setSelected(selected);
		
		dialog.setContentPane(contentPanel);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.getRootPane().setDefaultButton(btSave);
		Rectangle mfBounds = Main.getInstance().getMainFrame().getBounds();
		dialog.setBounds(mfBounds.x+(mfBounds.width/2)-180, mfBounds.y+(mfBounds.height/2)-200, 360, 400);
		dialog.setResizable(false);
		
		btSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saved = true;
				dialog.dispose();
			}
		});
		
		btCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
		
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
					saved = true;
					dialog.dispose();
				}
			}
		});
		
		dialog.setVisible(true);
	}
	
	public DefaultMenuItem getSelected() {
		return menuItems.get(getSelectedIndex());
	}
	
	public int getSelectedIndex() {
		return list.getSelectedIndex();
	}
	
	public void setSelected(DefaultMenuItem selected) {
		int selectedIndex = -1;
		
		if (selected == null) {
			selectedIndex = 0;
		} else {
			for (int i = 0; i < menuItems.size(); i++) {
				if (menuItems.get(i) != null && menuItems.get(i).equals(selected)) {
					selectedIndex = i;
				}
			}
		}
		
		if (selectedIndex >= 0) {
			final int select = selectedIndex;
    			list.setSelectedIndex(selectedIndex);
    			SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							list.scrollRectToVisible(list.getCellBounds(select, select));
						}
						catch (Exception e) {
							LOG.error("setSelected failed: " + e, e);
						}																									
					}
				});
		}
	}
	
	public boolean isSaved() {
		return saved;
	}
	
	class DefaultMenuItemDragGestureListener implements DragGestureListener {
		@Override
		public void dragGestureRecognized(DragGestureEvent dge) {
			Transferable transferable = new DefaultMenuItemTransferable(getSelectedIndex());
		    dge.startDrag(null, transferable, null);
		}		
	}
	
	class DefaultMenuItemTransferHandler extends TransferHandler {
		@Override
		public boolean importData(TransferHandler.TransferSupport support) {
			if (!canImport(support)) {
				return false;
			}
			
			final int iSourceIndex;
			try {
				iSourceIndex = (Integer) support.getTransferable().getTransferData(DEFAULT_MENU_ITEM_FLAVOR);
			} catch (Exception e) {
				return false;
			}
			
			JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
			int iTargetIndex = list.locationToIndex(dl.getDropPoint());
			
			LOG.info(iSourceIndex + " --> " + iTargetIndex);
			
			if (iSourceIndex != iTargetIndex) {
				DefaultMenuItem dmiSource = menuItems.get(iSourceIndex);
				menuItems.remove(iSourceIndex);
				menuItems.add(iTargetIndex, dmiSource);
				
				list.repaint();
			}
			return true;
		}
		
		@Override
		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
			for (DataFlavor dataFlavor : transferFlavors) {
				if (DEFAULT_MENU_ITEM_FLAVOR.equals(dataFlavor)) {
					return true;
				}
			}
			return false;
		}
	}

	public class DefaultMenuItemTransferable implements Transferable {

		private final Integer menuItemIndex;

		public DefaultMenuItemTransferable(Integer menuItemIndex) {
			super();
			this.menuItemIndex = menuItemIndex;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return flavors;
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor fl) {
			if (DEFAULT_MENU_ITEM_FLAVOR.equals(fl))
				return true;
			return false;
		}

		@Override
		public Object getTransferData(DataFlavor fl) throws UnsupportedFlavorException, IOException {
			if (DEFAULT_MENU_ITEM_FLAVOR.equals(fl)) {
				return menuItemIndex;
			}
			return null;
		}
	}
		
	public static final DataFlavor DEFAULT_MENU_ITEM_FLAVOR = new DataFlavor(Integer.class, "DefaultMenuItem.Index");
	private static final DataFlavor[] flavors = new DataFlavor[] {DEFAULT_MENU_ITEM_FLAVOR};
}
