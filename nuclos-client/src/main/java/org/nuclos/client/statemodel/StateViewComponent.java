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
package org.nuclos.client.statemodel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.nuclos.client.ui.Icons;

public class StateViewComponent extends JPanel {

	
	private final List<StateWrapper> items = new LinkedList<StateWrapper>();
	private StateWrapper selectedItem = null;
	
	private static class StateWrapperLabel extends JButton {
		private final StateWrapper state;
		private final StateViewComponent component;
		private final Icon icon = Icons.getInstance().getStateViewState();
		public StateWrapperLabel(StateWrapper state, StateViewComponent component) {
			this.state = state;
			this.component = component;
			
			if (state.getIcon() == null || state.getIcon().getContent() == null)
				setIcon(icon);
			else
				setIcon(new ImageIcon(state.getIcon().getContent()));
			
			setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
			setMargin(new Insets(-2, -3, -3, -3)); //sets the margin so buttons touch
			setFocusPainted(false);
			setBorderPainted(false);//turns off the borders
			setContentAreaFilled(true);
			setFocusable(false);
			
			setName(state.getCombinedStatusText());
			if (component.isSelected(state)) {
				setSelected(true);
			}
		}
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			Graphics2D g2d = (Graphics2D)g;
			if (state.getIcon() == null || state.getIcon().getContent() == null) {
				String numeral = state.getNumeral().toString();
				g2d.drawString(numeral, (icon.getIconWidth() / 2)
						- (SwingUtilities.computeStringWidth(g2d.getFontMetrics(), numeral) / 2) + 1,
						(icon.getIconHeight() / 2) + (int)(g2d.getFontMetrics().getHeight() / 3.5) + 1);
			}
			
			if (component.isSelected(state)) {
				g2d.drawString("selected", (icon.getIconWidth() / 2)
						- (SwingUtilities.computeStringWidth(g2d.getFontMetrics(), "selected") / 2) + 1,
						(icon.getIconHeight() / 2) + (int)(g2d.getFontMetrics().getHeight() / 3.5) + 1);
			}
		}
	}
	private static class StateTransitionLabel extends JLabel {
		private final Icon icon = Icons.getInstance().getStateViewTransition();
		public StateTransitionLabel() {
			setIcon(icon);
		}
	}
	private boolean bStandardTransitionsView = false;
	private boolean bEnableStandardTransitionsView = false;
    
	private boolean bHasStandardTransitions = false;
	
	private JPanel container;
	
	public StateViewComponent(boolean bEnableStandardTransitionsView) {
		this.bEnableStandardTransitionsView = bEnableStandardTransitionsView;
		
		init();
	}
	
	public boolean hasStandardTransitions() {
		return bHasStandardTransitions;
	}
	
	public void setStandardTransitionsView(boolean bStandardTransitionsView) {
		if (bEnableStandardTransitionsView) {
			this.bStandardTransitionsView = bStandardTransitionsView;
		}
	}
	
	private void init() {
		container = new JPanel() {
			@Override
			public Dimension getPreferredSize() {
				if (super.getPreferredSize() == null)
				{
					return new Dimension(0, 18);
				}
				return new Dimension(super.getPreferredSize().width, 18);
			}
			@Override
			public boolean isOpaque() {
				return true;
			}	
		};
		setLayout(new FlowLayout());
		add(container);
		container.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		container.setOpaque(false);
		setBorder(new EmptyBorder(new Insets(-4, 0, 0, 0)));
	}
	@Override
	public boolean isOpaque() {
		return false;
	}
	
	@Override
	public Dimension getPreferredSize() {
		if (super.getPreferredSize() == null)
		{
			return new Dimension(0, 20);
		}
		return new Dimension(container.getPreferredSize().width+2, 20);
	}
	
	private void initStates() {
		container.removeAll();
		for (Iterator<StateWrapper> iterator = items.iterator(); iterator.hasNext();) {
			StateWrapper item = iterator.next();
			
			container.add(new StateWrapperLabel(item, this));
			if (iterator.hasNext()) {
				container.add(new StateTransitionLabel());
			}
		}
		container.setBorder(new EmptyBorder(new Insets(-1, 2, 0, 0)));
	}
	
	private boolean isSelected(StateWrapper wrapper) {
		return getSelectedItem() != null && wrapper.getNumeral().equals(((StateWrapper)getSelectedItem()).getNumeral());
	}
	
	public boolean isStandardTransitionsView() {
		return bStandardTransitionsView;
	}
	
	public boolean isStandardTransitionsViewEnabled() {
		return bEnableStandardTransitionsView;
	}
	
	private void enableStateLabels() {
		boolean afterSelected = false;
		for (Iterator<StateWrapper> iterator = items.iterator(); iterator.hasNext();) {
			StateWrapper item = iterator.next();
			StateWrapperLabel label = findComponent(item);
			
			label.setEnabled(afterSelected);
			if (isSelected(item))
			{
				afterSelected = true;
			}
		}
	}
	
	private StateWrapperLabel findComponent(StateWrapper state) {
		Component[] components = container.getComponents();
		for (int i = 0; i < components.length; i++) {
			Component comp = components[i];
			if (comp instanceof StateWrapperLabel) {
				StateWrapperLabel label = (StateWrapperLabel)comp;
				if (label.getName().equals(state.getCombinedStatusText())) {
					return label;
				}
			}
		}
		return null;
	}
	
	public void addItems(List<StateWrapper> items) {
		this.items.addAll(items);
		initStates();
	}
	
	public void setSelectedItem(StateWrapper item) {
		this.selectedItem = item;
		enableStateLabels();
	}
	
	public void removeAllItems() {
		this.items.clear();
		initStates();
	};
	
	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(b);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
	}

    public int getItemCount() {
        return items.size();
    }
    public Object getSelectedItem() {
        return selectedItem;
    }
    
    public void addActionListener(ActionListener al, StateWrapper item) {
		StateWrapperLabel label = findComponent(item);
		label.addActionListener(al);
	}
    
    public void addSubsequentStatesActionListener(StateWrapper item, Map<StateWrapper, Action> mpSubsequentStatesAction) {
		StateWrapperLabel label = findComponent(item);
		
		final JPopupMenu popup = new JPopupMenu();
		for (Iterator iterator = mpSubsequentStatesAction.keySet().iterator(); iterator.hasNext();) {
			StateWrapper state = (StateWrapper) iterator.next();
			JMenuItem menuItem = new JMenuItem(mpSubsequentStatesAction.get(state));
			menuItem.setLabel(state.getCombinedStatusText());
			popup.add(menuItem);
		}
		label.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	if (popup.getComponentCount() > 0)
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        });
	}
    
    public List<StateWrapper> getStatesBefore(StateWrapper state) {
    	List<StateWrapper> result = new LinkedList<StateWrapper>();
    	boolean afterSelected = false;
		for (Iterator<StateWrapper> iterator = items.iterator(); iterator.hasNext();) {
			StateWrapper item = iterator.next();
			StateWrapperLabel label = findComponent(item);
			
			if (afterSelected) {
				result.add(item);
			}
			if (item.getNumeral().equals(state.getNumeral())) {
				break;
			}
			label.setEnabled(afterSelected);
			if (isSelected(item))
			{
				afterSelected = true;
			}
		}
		return result;
	}
    public void removeActionListeners() {
		for (Iterator<StateWrapper> iterator = items.iterator(); iterator.hasNext();) {
			StateWrapper item = iterator.next();
			StateWrapperLabel label = findComponent(item);
			for (ActionListener listener : label.getActionListeners())
				label.removeActionListener(listener);
		}
	}

}
