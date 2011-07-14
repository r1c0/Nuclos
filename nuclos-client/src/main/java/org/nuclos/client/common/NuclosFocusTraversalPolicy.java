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
package org.nuclos.client.common;

import java.awt.Component;
import java.awt.Container;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.LayoutFocusTraversalPolicy;

import org.nuclos.client.ui.JInfoTabbedPane;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.component.LabeledCollectableComponent;
import org.nuclos.client.ui.labeled.LabeledComponent;
import org.nuclos.common2.layoutml.LayoutMLConstants;

public class NuclosFocusTraversalPolicy extends	LayoutFocusTraversalPolicy {
	
	JComponent compRoot;
	
	Map<String, JComponent> mpComponentsBackwards;

	/**
	 *  
	 */
	private static final long serialVersionUID = -5062809400980161409L;

	public NuclosFocusTraversalPolicy(JComponent compRoot) {
		this.compRoot = compRoot;
		mpComponentsBackwards = new HashMap<String, JComponent>();
	}
	
	@Override
	public Component getComponentAfter(Container aContainer, Component aComponent) {
		if(aComponent instanceof JComponent) {
			JComponent jComponent = (JComponent)aComponent;
			Object obj = jComponent.getClientProperty(LayoutMLConstants.ATTRIBUTE_NEXTFOCUSCOMPONENT);
			if(obj != null && obj instanceof String) {
				if(aContainer instanceof JPanel) {
					JComponent jFound = UIUtils.findJComponentStartsWithName((JPanel)aContainer, (String)obj);
					mpComponentsBackwards.put((String)obj, (JComponent)aComponent);
					if(jFound instanceof LabeledComponent){
						return ((LabeledComponent)jFound).getControlComponent();
					}
					return jFound;
				}
			}
		}
		Component comp = super.getComponentAfter(aContainer, aComponent);	
		if(comp instanceof JInfoTabbedPane) {
			JInfoTabbedPane pane = (JInfoTabbedPane)comp;
			comp = getNextFocusAbleComponent(pane);
			if(comp instanceof SubForm) {
				SubForm subform = (SubForm)comp;
				subform.fireFocusGained();
			}
		}
		else if(comp instanceof JButton) { // may be subform
			JButton bt = (JButton)comp;
			if(bt.getParent() instanceof JToolBar) {
				comp = bt.getParent().getParent().getParent().getParent();
				if(comp instanceof SubForm) {
					SubForm subform = (SubForm)comp;
					subform.fireFocusGained();
				}
			}
		}
		return comp;
	}

	@Override
	public Component getComponentBefore(Container aContainer, Component aComponent) {
		if(aComponent instanceof JComponent) {
			if(mpComponentsBackwards.containsValue((JComponent)aComponent)) {
				for(String sNext : mpComponentsBackwards.keySet()) {
					if(mpComponentsBackwards.get(sNext).equals(aComponent)) {
						JComponent jFound = UIUtils.findJComponentStartsWithName((JPanel)aContainer, sNext);
						if(jFound instanceof LabeledComponent){
							return ((LabeledComponent)jFound).getControlComponent();
						}
						return jFound;
					}
				}
			}
		}
		return super.getComponentBefore(aContainer, aComponent);
	}

	@Override
	public Component getFirstComponent(Container aContainer) {				
		return super.getFirstComponent(aContainer);
	}

	@Override
	public Component getLastComponent(Container aContainer) {
		return super.getLastComponent(aContainer);
	}

	@Override
	public Component getDefaultComponent(Container aContainer) {
		return super.getFirstComponent(aContainer);
	}

	@Override
	public void setImplicitDownCycleTraversal(boolean implicitDownCycleTraversal) {
		super.setImplicitDownCycleTraversal(implicitDownCycleTraversal);
	}

	@Override
	public boolean getImplicitDownCycleTraversal() {
		return super.getImplicitDownCycleTraversal();
	}

	@Override
	protected boolean accept(Component aComponent) {
		return super.accept(aComponent);
	}
	
	private Component getNextFocusAbleComponent(JInfoTabbedPane pane) {
		Component next = pane;
		for(Component comp : pane.getComponents()) {
			next = cycle(comp);
			if(next instanceof LabeledComponent) {
				LabeledComponent lc = (LabeledComponent)next;
				return lc.getControlComponent();
			}
			else if(next instanceof SubForm)
			{
				if(next.isShowing())
					break;
			}
				
		}
		return next;
	}
	
	private Component cycle(Component container) {
		Component comp = container;
		if(comp instanceof LabeledComponent) {
			return comp;
		}
		if(comp instanceof SubForm) {
			if(comp.isShowing())
				return comp;
		}		
		else if(comp instanceof Container) {
			for(Component c : ((Container) comp).getComponents()) {
				comp = cycle(c);
				if(comp instanceof LabeledComponent) {
					LabeledComponent lc = (LabeledComponent)comp;
					if(lc.getControlComponent().isShowing())
						return comp;
				}
				if(comp instanceof SubForm) {
					return comp;
				}
					
			}			
		}
		
		return comp;
	}

}
