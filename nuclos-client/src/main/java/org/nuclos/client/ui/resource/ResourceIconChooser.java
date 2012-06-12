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
package org.nuclos.client.ui.resource;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXBusyLabel;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.resource.NuclosResourceCache;
import org.nuclos.client.resource.NuclosResourceCategory;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.resource.ResourceDelegate;
import org.nuclos.client.synthetica.NuclosThemeSettings;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.SpringLocaleDelegate;

public class ResourceIconChooser extends JPanel {
	
	private static final Logger LOG = Logger.getLogger(ResourceIconChooser.class);
	
	private final NuclosResourceCategory cat;
	
	private final JList list;
	private final JScrollPane listScroller;
	private final List<String> iconNames = new ArrayList<String>();
	
	private boolean loading;
	private String iconToSelect;
	
	private boolean saved;
	
	final ActionListener iconActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			 if (e.getActionCommand().compareTo("Copy")==0) {
				 int selectedIndex = list.getSelectedIndex();
				 if (selectedIndex > 0) {
					 Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(getResourceIconName(selectedIndex)), null);
				 }
			 }
		}
	};
	
	/**
	 * 
	 * @param cat (<code>null</code> for custom resource icons)
	 */
	public ResourceIconChooser(NuclosResourceCategory cat) {
		this(0, cat);
	}
	
	/**
	 * 
	 * @param iconMaxSize
	 * @param cat (<code>null</code> for custom resource icons)
	 */
	public ResourceIconChooser(final int iconMaxSize, NuclosResourceCategory cat) {
		this.cat = cat;
		setLayout(new BorderLayout());
		
		list = new JList() {

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
        
        list.registerKeyboardAction(iconActionListener,"Copy", KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_FOCUSED);
        list.registerKeyboardAction(iconActionListener,"Copy", KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), JComponent.WHEN_FOCUSED);
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        list.setCellRenderer(new ListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				JPanel btnPanel = new JPanel(new BorderLayout());
				btnPanel.setOpaque(false);
				
				Icon ico = (ImageIcon) value;
				if (iconMaxSize==0) {
					if (((ImageIcon) value).getIconWidth() > 256 ||
						((ImageIcon) value).getIconHeight() > 256) {
						ico = MainFrame.resizeAndCacheIcon(ico, 256);
					}
				} else {
					ico = MainFrame.resizeAndCacheIcon(ico, iconMaxSize);
				}
				
				JButton btn = new JButton(ico);
				btn.setBorderPainted(false);
				btn.setContentAreaFilled(true);
				btn.addActionListener(iconActionListener);
				
				if (index > 0) btn.setToolTipText(ResourceIconChooser.this.getResourceIconName(index));
				
				if (isSelected && index > 0) {
					btn.setOpaque(true);
					btn.setBackground(NuclosThemeSettings.BACKGROUND_COLOR4);
					btnPanel.setBorder(BorderFactory.createLineBorder(NuclosThemeSettings.BACKGROUND_ROOTPANE, 1));
				} else {
					btn.setOpaque(false);
					btnPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
				}
				btnPanel.add(btn, BorderLayout.CENTER);
				
				return btnPanel;
			}
		});
        list.setVisibleRowCount(-1);
        
        listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(250, 80));
        listScroller.setAlignmentX(LEFT_ALIGNMENT);
        
        JXBusyLabel busyLabel = new JXBusyLabel();
        busyLabel.setBusy(true);
        
        JPanel busyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        busyPanel.setBackground(Color.WHITE);
        busyPanel.add(busyLabel);
        
        add(new JScrollPane(busyPanel), BorderLayout.CENTER);
        
        loading = true;
        iconToSelect = null;
        
        LoadWorker worker = new LoadWorker();
        worker.execute();
	}	
	
	public void removeBorder() {
		listScroller.setBorder(BorderFactory.createEmptyBorder());
	}
	
	private class LoadWorker extends SwingWorker<List<ImageIcon>, Object> {

		@Override
		protected List<ImageIcon> doInBackground() throws Exception {
			List<ImageIcon> icons = new ArrayList<ImageIcon>();
			iconNames.add(null);
			icons.add(Icons.getInstance().getIconEmpty16());
			if (cat == null) {
				for (String sResource : CollectionUtils.sorted(ResourceDelegate.getInstance().getResourceNames())) {
					try {
						ImageIcon iconResource = ResourceCache.getInstance().getIconResource(sResource);
						iconNames.add(sResource);
						icons.add(iconResource);
					} catch (Exception ex) {
						// ignore. not an image icon.
					}
				}
			} else {
				for (String iconName : NuclosResourceCache.getNuclosResourceIcons(cat)) {
					iconNames.add(iconName);
					icons.add(NuclosResourceCache.getNuclosResourceIcon(iconName));
				}
			}
			return icons;
		}

		@Override
		protected void done() {
			try {
				final Object[] result = get().toArray();
				list.setModel(new AbstractListModel() {
	                public int getSize() { return result.length; }
	                public Object getElementAt(int i) { return result[i]; }
	            });
				removeAll();
				add(listScroller, BorderLayout.CENTER);
				
				list.revalidate();
				listScroller.revalidate();
				revalidate();
				repaint();
				
				loading = false;
				
				if (iconToSelect != null) {
					setSelected(iconToSelect);
					iconToSelect = null;
				}
			} catch (Exception e) {
				Errors.getInstance().showExceptionDialog(list, e);
			}
		}
		
	}
	
	public void setSelected(String name) {
		if (loading) {
			iconToSelect = name;
		} else {
			int selectedIndex = -1;
			
			if (name == null) {
				selectedIndex = 0;
			} else {
				for (int i = 0; i < iconNames.size(); i++) {
					if (iconNames.get(i) != null && iconNames.get(i).equals(name)) {
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
	}
	
	public String getSelectedResourceIconName() {
		return getResourceIconName(getSelectedIndex());
	}
	
	public int getSelectedIndex() {
		return list.getSelectedIndex();
	}
	
	public String getResourceIconName(int index) {
		if (index == -1)
			return null;
		return iconNames.get(index);
	}
	
	public void addListSelectionListener(ListSelectionListener lsl) {
		list.addListSelectionListener(lsl);
	}
	
	public void removeListSelectionListener(ListSelectionListener lsl) {
		list.removeListSelectionListener(lsl);
	}
	
	public void showDialog(String sSelectedResource) {
		final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
		
		JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		contentPanel.add(this, BorderLayout.CENTER);
		
		JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
		JButton btSave = new JButton(localeDelegate.getMessage("ResourceIconChooser.1","Speichern"));
		JButton btCancel = new JButton(localeDelegate.getMessage("ResourceIconChooser.2","Abbrechen"));
		actionsPanel.add(btSave);
		actionsPanel.add(btCancel);
		contentPanel.add(actionsPanel, BorderLayout.SOUTH);
		
		setSelected(sSelectedResource);
		
		final JDialog dialog = new JDialog(Main.getInstance().getMainFrame(), localeDelegate.getMessage(
				"ResourceIconChooser.3","Ressource Icon auswählen"), true);
		dialog.setContentPane(contentPanel);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.getRootPane().setDefaultButton(btSave);
		Rectangle mfBounds = Main.getInstance().getMainFrame().getBounds();
		dialog.setBounds(mfBounds.x+(mfBounds.width/2)-480, mfBounds.y+(mfBounds.height/2)-300, 960, 600);
		dialog.setResizable(true);
		
		btSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (loading) {
					//  ignore, no save
				} else {
					saved = true;
				}
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
	
	public boolean isSaved() {
		return saved;
	}
	
	public static class Button extends JButton {
		
		private String sResource;
		
		private String sLabel;
		
		private NuclosResourceCategory cat;
		
		private Collection<ItemListener> itemListener = new ArrayList<ItemListener>();
		
		public Button() {
			this(null, null, null);
		}
		
		/**
		 * 
		 * @param sLabel
		 * @param sResource (selected icon)
		 * @param cat (<code>null</code> for custom resource icons)
		 */
		public Button(String sLabel, String sResource, NuclosResourceCategory cat) {
			super();
			this.sLabel = sLabel;
			this.sResource = sResource;
			this.cat = cat;
			updateButtonText();
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ResourceIconChooser iconChooser = new ResourceIconChooser(getCategory());
					iconChooser.showDialog(getResource());
					if (iconChooser.isSaved()) {
						setResource(iconChooser.getSelectedResourceIconName());
						fireItemStateChanged();
					}
				}
			});
		}
		
		public NuclosResourceCategory getCategory() {
			return cat;
		}
		
		public void setCategory(NuclosResourceCategory cat) {
			this.cat = cat;
		}
		
		public void setLabel(String sLabel) {
			this.sLabel = sLabel;
		}
		
		public String getResource() {
			return sResource;
		}
		
		public void setResource(String sResource) {
			this.sResource = sResource;
			updateButtonText();
		}
		
		private void updateButtonText() {
			setText(sLabel + (sResource==null?"":(": " + sResource)));
		}
		
		public void addItemListener(ItemListener il) {
			itemListener.add(il);
		}
		
		public void removeItemListener(ItemListener il) {
			itemListener.remove(il);
		}
		
		private void fireItemStateChanged() {
			ItemEvent ie = new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, this, ItemEvent.ITEM_STATE_CHANGED);
			for (ItemListener il : itemListener) {
				il.itemStateChanged(ie);
			}
		}
		
	}
	
	public static void main(String[] args) {
		JFrame frm = new JFrame(ResourceIconChooser.class.getName());
		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frm.setBounds(100, 100, 300, 600);
		
		ResourceIconChooser ric = new ResourceIconChooser(NuclosResourceCategory.ENTITY_ICON);
		ric.setSelected("org.nuclos.common.resource.icon.glyphish.88-beer-mug.png");
		frm.getContentPane().add(ric, BorderLayout.CENTER);
		
		frm.setVisible(true);
	}
	
}
