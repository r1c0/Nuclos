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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;

import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.resource.NuclosResourceCache;
import org.nuclos.client.synthetica.NuclosSyntheticaConstants;
import org.nuclos.client.ui.Icons;

public class ResourceIconChooser extends JPanel {
	
	private final JList list;
	private final List<String> iconNames = new ArrayList<String>();
	
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
	
	public ResourceIconChooser() {
		this(0);
	}
	
	public ResourceIconChooser(final int iconMaxSize) {
		setLayout(new BorderLayout());
		
		List<ImageIcon> icons = new ArrayList<ImageIcon>();
		iconNames.add(null);
		icons.add(Icons.getInstance().getIconEmpty16());
		for (String iconName : NuclosResourceCache.getNuclosResourceIcons()) {
			iconNames.add(iconName);
			icons.add(NuclosResourceCache.getNuclosResourceIcon(iconName));
		}
		
		list = new JList(icons.toArray()) {

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
				
				JButton btn = new JButton(iconMaxSize==0?
						(ImageIcon) value:
						MainFrame.resizeAndCacheIcon((ImageIcon) value, iconMaxSize));
				btn.setBorderPainted(false);
				btn.setContentAreaFilled(true);
				btn.addActionListener(iconActionListener);
				
				if (index > 0) btn.setToolTipText(ResourceIconChooser.this.getResourceIconName(index));
				
				if (isSelected && index > 0) {
					btn.setOpaque(true);
					btn.setBackground(NuclosSyntheticaConstants.BACKGROUND_SPOT);
					btnPanel.setBorder(BorderFactory.createLineBorder(NuclosSyntheticaConstants.BACKGROUND_DARKER, 1));
				} else {
					btn.setOpaque(false);
					btnPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
				}
				btnPanel.add(btn, BorderLayout.CENTER);
				
				return btnPanel;
			}
		});
        list.setVisibleRowCount(-1);
        
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(250, 80));
        listScroller.setAlignmentX(LEFT_ALIGNMENT);
        
        add(listScroller, BorderLayout.CENTER);
	}	
	
	public void setSelected(String name) {
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
						list.scrollRectToVisible(list.getCellBounds(select, select));
					}
				});
		}
	}
	
	public String getSelectedResourceIconName() {
		return getResourceIconName(getSelectedIndex());
	}
	
	public int getSelectedIndex() {
		return list.getSelectedIndex();
	}
	
	public String getResourceIconName(int index) {
		return iconNames.get(index);
	}
	
	public void addListSelectionListener(ListSelectionListener lsl) {
		list.addListSelectionListener(lsl);
	}
	
	public void removeListSelectionListener(ListSelectionListener lsl) {
		list.removeListSelectionListener(lsl);
	}
	
	public static void main(String[] args) {
		JFrame frm = new JFrame(ResourceIconChooser.class.getName());
		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frm.setBounds(100, 100, 300, 600);
		
		ResourceIconChooser ric = new ResourceIconChooser();
		ric.setSelected("org.nuclos.common.resource.icon.glyphish.88-beer-mug.png");
		frm.getContentPane().add(ric, BorderLayout.CENTER);
		
		frm.setVisible(true);
	}
	
}
