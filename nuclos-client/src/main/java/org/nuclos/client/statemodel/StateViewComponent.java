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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LangUtils;


public class StateViewComponent extends JPanel {	
	private final List<StateWrapper> items = new LinkedList<StateWrapper>();
	private StateWrapper selectedItem = null;
	
	private static class StateWrapperLabel extends JButton {
		private final StateWrapper state;
		private final StateViewComponent component;
		public StateWrapperLabel(StateWrapper state, StateViewComponent component) {
			this.state = state;
			this.component = component;
			
			if (component.isSelected(state)) {
				setSelected(true);
			}
			
			setMargin(new Insets(-2, -3, -3, -3)); //sets the margin so buttons touch
			setFocusPainted(false);
			setBorderPainted(false);//turns off the borders
			setContentAreaFilled(true);
			setFocusable(false);
			setRolloverEnabled(true);
			
			setName(state.getCombinedStatusText());
			setToolTipText(state.getDescription());
		}
		
		@Override
		public Dimension getSize() {
			return getPreferredSize();
		}
		
		@Override
		public Dimension getPreferredSize() {
			int height = 41;
			if (component.getItemCount() == 0) {
				height = 0;
			}
			if (getParent() == null)
				return new Dimension(0, height);
			return new Dimension(200, height);
		}
		
		@Override
		public void paint(Graphics g) {
			//super.paint(g);

			Graphics2D g2d = (Graphics2D)g;
			
			Icon firstIcon;
			Icon secondIcon;
			Icon thirdIcon;
			
			if (isEnabled()) {
				firstIcon = Icons.getInstance().getStateViewStateNormal("first");
				secondIcon = Icons.getInstance().getStateViewStateNormal("second");
				thirdIcon = Icons.getInstance().getStateViewStateNormal("third");
			} else {
				firstIcon = Icons.getInstance().getStateViewStateDisabled("first");
				secondIcon = Icons.getInstance().getStateViewStateDisabled("second");
				thirdIcon = Icons.getInstance().getStateViewStateDisabled("third");
			}
			
			if (isSelected() || getModel().isRollover()) {
				firstIcon = Icons.getInstance().getStateViewStateSelected("first");
				secondIcon = Icons.getInstance().getStateViewStateSelected("second");
				thirdIcon = Icons.getInstance().getStateViewStateSelected("third");
			}
			
			String statusText = state.getName();
			
			Icon statusIcon;
			if (state.getIcon() == null || state.getIcon().getContent() == null)
				statusIcon = Icons.getInstance().getStateViewDefaultStateIcon();
			else
				statusIcon = new ImageIcon(state.getIcon().getContent());
			
			int width = super.getSize().width - firstIcon.getIconWidth() - thirdIcon.getIconWidth();

			g.drawImage(((ImageIcon)firstIcon).getImage(), 0,0, null);
			g.drawImage(((ImageIcon)secondIcon).getImage(), firstIcon.getIconWidth(),0, width, secondIcon.getIconHeight(), null);
			g.drawImage(((ImageIcon)thirdIcon).getImage(), firstIcon.getIconWidth() + width, 0, null);
									
			g2d.drawImage(((ImageIcon)statusIcon).getImage(), statusIcon.getIconWidth(),
					(statusIcon.getIconHeight() / 2) + (int)(g2d.getFontMetrics().getHeight() / 3.5) - 1, null);

			g2d.drawString(statusText, (statusIcon.getIconWidth() * 2) + getIconTextGap(),
					(firstIcon.getIconHeight() / 2) + (int)(g2d.getFontMetrics().getHeight() / 3.5) + 1);
		}
	}
	
	private JPanel container;
	
	public StateViewComponent() {
		init();
	}
	
	private void init() {
		container = new JPanel() {
			
			@Override
			public Dimension getPreferredSize() {
				int height = 41;
				if (getItemCount() == 0) {
					height = 0;
				}
				Dimension prefSize = super.getPreferredSize();
				if (prefSize == null)
				{
					return new Dimension(0, height);
				}
				return new Dimension(prefSize.width, height);
			}
			@Override
			public boolean isOpaque() {
				return true;
			}	
			
			@Override
			public void paint(Graphics g) {
				super.paint(g);
			}
		};
		setLayout(new BorderLayout());
		add(container, BorderLayout.CENTER);
		container.setOpaque(false);
		setBorder(new EmptyBorder(new Insets(0, 5, 0, 5)));
	}
	@Override
	public boolean isOpaque() {
		return false;
	}
	
	@Override
	public Dimension getPreferredSize() {
		int height = 44;
		if (getItemCount() == 0) {
			height = 0;
		}
		Dimension prefSize= super.getPreferredSize();
		if (prefSize == null)
		{
			return new Dimension(0, height);
		}
		return new Dimension(prefSize.width, height);
	}
	
	private void initStates() {
		container.removeAll();
		container.setLayout(new GridLayout(1, items.size()));
		for (Iterator<StateWrapper> iterator = items.iterator(); iterator.hasNext();) {
			StateWrapper item = iterator.next();
			container.add(new StateWrapperLabel(item, this));
		}
		
		container.setBorder(new EmptyBorder(new Insets(-1, 2, 0, 0)));
		revalidate();
		repaint();
	}
	
	private boolean isSelected(StateWrapper wrapper) {
		return getSelectedItem() != null && wrapper.getNumeral().equals(((StateWrapper)getSelectedItem()).getNumeral());
	}
	
	private void enableStateLabels() {
		boolean afterSelected = false;
		for (Iterator<StateWrapper> iterator = items.iterator(); iterator.hasNext();) {
			StateWrapper item = iterator.next();
			StateWrapperLabel label = findComponent(item);
			
			label.setEnabled((item.isReachable()) && !item.isFromAutomatic());
			if (isSelected(item))
			{
				afterSelected = true;
			}
		}
	}
	
	private void disableStateLabels() {
		for (Iterator<StateWrapper> iterator = items.iterator(); iterator.hasNext();) {
			StateWrapper item = iterator.next();
			StateWrapperLabel label = findComponent(item);
			label.setEnabled(false);
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
		if (!b)
			disableStateLabels();
		else
			enableStateLabels();
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

		if (mpSubsequentStatesAction.size() > 0) {
			JLabel lbl = new JLabel(CommonLocaleDelegate.getInstance().getResource(
					"StateViewComponent.01", "M\u00f6gliche Statuswechsel:"));
			lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
			popup.add(lbl);
		}
		List<StateWrapper> lstSubsequentStates = new ArrayList<StateWrapper>();
		for (Iterator<StateWrapper> iterator = mpSubsequentStatesAction.keySet().iterator(); iterator.hasNext();) {
			StateWrapper state = iterator.next();
			lstSubsequentStates.add(state);
		}
		Collections.sort(lstSubsequentStates, new Comparator<StateWrapper>() {
			@Override
			public int compare(StateWrapper o1, StateWrapper o2) {
				return LangUtils.compare(o1.getName(), o2.getName());
			}
		});
		for (Iterator<StateWrapper> iterator = lstSubsequentStates.iterator(); iterator.hasNext();) {
			StateWrapper state = iterator.next();
			JMenuItem menuItem = new JMenuItem(mpSubsequentStatesAction.get(state));
			menuItem.setLabel(state.getName());
			popup.add(menuItem);
		}
		label.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	if (popup.getComponentCount() > 0 && SwingUtilities.isRightMouseButton(e))
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
