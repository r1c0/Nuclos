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

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.annotation.PostConstruct;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;

import org.nuclos.client.ui.Icons;
import org.nuclos.common.WorkspaceDescription.Split;
import org.nuclos.common2.SpringLocaleDelegate;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class MainFrameSplitPane extends JSplitPane {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final int defaultDividerSize;
	private final double defaultResizeWeight;
	
	private int fixedState;

	public MainFrameSplitPane(int newOrientation, boolean newContinuousLayout, Component newLeftComponent, Component newRightComponent) {
		super(newOrientation, newContinuousLayout, newLeftComponent, newRightComponent);
		defaultDividerSize = getDividerSize();
		defaultResizeWeight = getResizeWeight();
		fixedState = Split.FIXED_STATE_NONE;
	}
	
	@SuppressWarnings("serial")
	@PostConstruct
	void init() {
		if (MainFrame.isSplittingEnabled()) {
			Action actFixLeft = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setFixedState(Split.FIXED_STATE_LEFT);
				}

				@Override
				public Object getValue(String key) {
					if (Action.NAME.equals(key)) {
						if (MainFrameSplitPane.this.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
							return SpringLocaleDelegate.getInstance().getMessage("MainFrameSplitPane.fixLeft", "Fixiere linken Bereich");
						} else {
							return SpringLocaleDelegate.getInstance().getMessage("MainFrameSplitPane.fixUpper", "Fixiere oberen Bereich");
						}
					} else if (Action.SMALL_ICON.equals(key)) {
						if (MainFrameSplitPane.this.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
							return Icons.getInstance().getIconGoFirst16();
						} else {
							return Icons.getInstance().getIconGoTop16();
						}
					}
					return super.getValue(key);
				}
				
			};
			Action actFixRight = new AbstractAction() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					setFixedState(Split.FIXED_STATE_RIGHT);
				}
				
				@Override
				public Object getValue(String key) {
					if (Action.NAME.equals(key)) {
						if (MainFrameSplitPane.this.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
							return SpringLocaleDelegate.getInstance().getMessage("MainFrameSplitPane.fixRight", "Fixiere rechten Bereich");
						} else {
							return SpringLocaleDelegate.getInstance().getMessage("MainFrameSplitPane.fixLower", "Fixiere unteren Bereich");
						}
					} else if (Action.SMALL_ICON.equals(key)) {
						if (MainFrameSplitPane.this.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
							return Icons.getInstance().getIconGoLast16();
						} else {
							return Icons.getInstance().getIconGoBottom16();
						}
					}
					return super.getValue(key);
				}
				
			};
			
			JPopupMenu popup = new JPopupMenu();
			popup.add(actFixLeft);
			popup.add(actFixRight);
			setComponentPopupMenu(popup);
		}
	}
	
	public void setFixedState(int fixedState) {
		this.fixedState = fixedState;
		switch (fixedState) {
		case Split.FIXED_STATE_NONE:
			setDividerSize(defaultDividerSize);
			setResizeWeight(defaultResizeWeight);
			break;
		case Split.FIXED_STATE_LEFT:
			setDividerSize(0);
			setResizeWeight(0d);
			break;
		case Split.FIXED_STATE_RIGHT:
			setDividerSize(0);
			setResizeWeight(1d);
			break;
		}
	}
	
	public int getFixedState() {
		return this.fixedState;
	}

	public int getDefaultDividerSize() {
		return defaultDividerSize;
	}

}
