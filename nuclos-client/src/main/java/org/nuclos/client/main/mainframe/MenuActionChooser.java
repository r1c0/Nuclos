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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.nuclos.client.main.ActionWithMenuPath;
import org.nuclos.client.main.GenericAction;
import org.nuclos.client.main.Main;
import org.nuclos.client.synthetica.NuclosSyntheticaConstants;
import org.nuclos.client.ui.Icons;
import org.nuclos.common.WorkspaceDescription;
import org.nuclos.common2.CommonLocaleDelegate;

public class MenuActionChooser extends JPanel{
	
	private static final Logger LOG = Logger.getLogger(MenuActionChooser.class);
	
	private final JList list;
	private final JScrollPane listScroller;
	private final List<GenericAction> actions = new ArrayList<GenericAction>();
	
	private boolean saved;
	
	private final ActionListener menuActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			 if (e.getActionCommand().compareTo("Copy")==0) {
				 int selectedIndex = list.getSelectedIndex();
				 if (selectedIndex > 0 && getAction(selectedIndex) != null) {
					 Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(getAction(selectedIndex).x.toString()), null);
				 }
			 }
		}
	};
	
	public MenuActionChooser() {
		setLayout(new BorderLayout());
		
		actions.add(new GenericAction(null, new ActionWithMenuPath(null, new AbstractAction("", Icons.getInstance().getIconEmpty16()) {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		})));
		actions.addAll(Main.getMainController().getGenericActions());

		list = new JList(actions.toArray()) {

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
        
        list.registerKeyboardAction(menuActionListener,"Copy", KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_FOCUSED);
        list.registerKeyboardAction(menuActionListener,"Copy", KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), JComponent.WHEN_FOCUSED);
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setCellRenderer(new ListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				GenericAction genAction = (GenericAction) value;
				JPanel btnPanel = new JPanel(new BorderLayout());
				btnPanel.setOpaque(false);
				
				JLabel jlbMenuPath = new JLabel(getMenuPath(genAction.y.x));
				jlbMenuPath.setOpaque(false);
				jlbMenuPath.setForeground(NuclosSyntheticaConstants.BACKGROUND_DARKER);
				btnPanel.add(jlbMenuPath, BorderLayout.WEST);
				
				String name = (String) genAction.y.y.getValue(Action.NAME);
				Icon ico = (Icon) genAction.y.y.getValue(Action.SMALL_ICON);
				JButton btn = new JButton(name, ico);
				btn.setHorizontalAlignment(SwingConstants.LEFT);
				btn.setBorderPainted(false);
				btn.setContentAreaFilled(true);
				btn.addActionListener(menuActionListener);
				
				//if (index > 0) btn.setToolTipText(ResourceIconChooser.this.getResourceIconName(index));
				
				if (isSelected && index > 0) {
					btn.setOpaque(true);
					jlbMenuPath.setOpaque(true);
					btn.setBackground(NuclosSyntheticaConstants.BACKGROUND_SPOT);
					jlbMenuPath.setBackground(NuclosSyntheticaConstants.BACKGROUND_SPOT);
					btnPanel.setBorder(BorderFactory.createLineBorder(NuclosSyntheticaConstants.BACKGROUND_DARKER, 1));
				} else {
					btn.setOpaque(false);
					jlbMenuPath.setOpaque(false);
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
        
        add(listScroller, BorderLayout.CENTER);
	}	
	
	private String getMenuPath(String[] menuPath) {
		if (menuPath == null)
			return "";
		
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < menuPath.length; i++) {
			result.append(menuPath[i]);
			if (i < menuPath.length-1) {
				result.append('/');
			}
		}
		return result.toString();
	}
	
	public void setSelected(WorkspaceDescription.Action wdAction) {
		int selectedIndex = -1;
		
		if (wdAction == null) {
			selectedIndex = 0;
		} else {
			for (int i = 0; i < actions.size(); i++) {
				if (actions.get(i).x != null && actions.get(i).x.equals(wdAction)) {
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
	
	public GenericAction getSelectedAction() {
		return getAction(getSelectedIndex());
	}
	
	public int getSelectedIndex() {
		return list.getSelectedIndex();
	}
	
	public GenericAction getAction(int index) {
		if (index == 0)
			return null;
		else
			return actions.get(index);
	}
	
	public void addListSelectionListener(ListSelectionListener lsl) {
		list.addListSelectionListener(lsl);
	}
	
	public void removeListSelectionListener(ListSelectionListener lsl) {
		list.removeListSelectionListener(lsl);
	}
	
	public void showDialog(WorkspaceDescription.Action selectedAction) {
		final JDialog dialog = new JDialog(Main.getMainFrame(), CommonLocaleDelegate.getMessage("MenuActionChooser.3","Aktion auswählen"), true);
		
		JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		contentPanel.add(this, BorderLayout.CENTER);
		
		JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
		JButton btSave = new JButton(CommonLocaleDelegate.getMessage("MenuActionChooser.1","Speichern"));
		JButton btCancel = new JButton(CommonLocaleDelegate.getMessage("MenuActionChooser.2","Abbrechen"));
		actionsPanel.add(btSave);
		actionsPanel.add(btCancel);
		contentPanel.add(actionsPanel, BorderLayout.SOUTH);
		
		setSelected(selectedAction);
		
		dialog.setContentPane(contentPanel);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.getRootPane().setDefaultButton(btSave);
		Rectangle mfBounds = Main.getMainFrame().getBounds();
		dialog.setBounds(mfBounds.x+(mfBounds.width/2)-360, mfBounds.y+(mfBounds.height/2)-200, 720, 400);
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
	
	public boolean isSaved() {
		return saved;
	}
}
