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
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;
import org.nuclos.client.ui.DateChooser;
import org.nuclos.client.ui.JInfoTabbedPane;
import org.nuclos.client.ui.OptionGroup;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.SubForm.SubFormTable;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.model.CollectableTableModel;
import org.nuclos.client.ui.labeled.LabeledComponent;
import org.nuclos.client.ui.labeled.LabeledTextComponent;
import org.nuclos.client.ui.layoutml.LayoutRoot;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.layoutml.LayoutMLConstants;

public class NuclosFocusTraversalPolicy extends	LayoutFocusTraversalPolicy {
	
	private static final Logger LOG = Logger.getLogger(NuclosFocusTraversalPolicy.class);
	
	private final JComponent compRoot;
	private final Map<String, SubForm> mpSubforms;
	private final Collection<CollectableComponent> collectableComponents;
	
	private final Map<String, JComponent> mpComponentsBackwards;
	
	private static final long serialVersionUID = -5062809400980161409L;

	public NuclosFocusTraversalPolicy(LayoutRoot layoutRoot) {
		this.compRoot = layoutRoot.getRootComponent();
		this.mpSubforms = layoutRoot.getMapOfSubForms();
		this.collectableComponents = layoutRoot.getCollectableComponents();
		mpComponentsBackwards = new HashMap<String, JComponent>();
	}
	
	private JComponent getComponentBefore(Component aComponent) {
		String nextComp = null;
		
		for (CollectableComponent colComp : collectableComponents) {
			JComponent jComp = colComp.getControlComponent();
			if (jComp instanceof DateChooser)
				jComp = ((DateChooser)colComp.getControlComponent()).getJTextField();
			if (aComponent.equals(jComp)) {
					nextComp = colComp.getFieldName();
					break;
			}
		}	
		
		if (nextComp != null) {
			for (CollectableComponent colComp : collectableComponents) {
				String curNextComp = (String)colComp.getControlComponent().getClientProperty(LayoutMLConstants.ATTRIBUTE_NEXTFOCUSCOMPONENT);
				if (curNextComp != null && curNextComp.equals(nextComp))
					return colComp.getControlComponent();
			}		
		}
		
		return null;
	}
	
	private Component componentAfter;
	@Override
	public Component getComponentAfter(Container aContainer, Component aComponent) {
		// @see  	NUCLOS-1018 - prevent a stack overflow 
		Component c =  _getComponentAfter(aContainer, aComponent);
		componentAfter = null; // reset component after.
		
		return c;
	}
	public Component _getComponentAfter(Container aContainer, Component aComponent) {
		if(aComponent instanceof JComponent) {
			JComponent jComponent = (JComponent)aComponent;
			if (UIUtils.findFirstParentJComponent(jComponent, SubFormTable.class) != null)
				return aComponent; // we do not want to cycle inside a subform with this focustraversal policy.
			Object obj = jComponent.getClientProperty(LayoutMLConstants.ATTRIBUTE_NEXTFOCUSCOMPONENT);
			if (obj == null && jComponent.getParent() != null && jComponent.getParent() instanceof JComponent)
				if (jComponent.getParent() instanceof OptionGroup) {
					Enumeration elements = ((OptionGroup)jComponent.getParent()).getButtonGroup().getElements();
					while (elements.hasMoreElements()) {
						Object elem = elements.nextElement();
						if (!elements.hasMoreElements() && elem.equals(jComponent))
							obj = ((JComponent)jComponent.getParent()).getClientProperty(LayoutMLConstants.ATTRIBUTE_NEXTFOCUSCOMPONENT);
					}
				} else {
					obj = ((JComponent)jComponent.getParent()).getClientProperty(LayoutMLConstants.ATTRIBUTE_NEXTFOCUSCOMPONENT);
					if (obj == null && jComponent.getParent().getParent() != null && jComponent.getParent().getParent() instanceof JComponent) {// this is for DateChooser
						obj = ((JComponent)jComponent.getParent().getParent()).getClientProperty(LayoutMLConstants.ATTRIBUTE_NEXTFOCUSCOMPONENT);
					}
				}
			if(obj != null && obj instanceof String) {
				if(aContainer instanceof JPanel) {
					JComponent jFound = UIUtils.findJComponentStartsWithName((JPanel)aContainer, (String)obj);
					if (jFound == null && ((String)obj).indexOf(".") != -1) {
						getFocusableSubFormComponent(((String)obj));
					}
					mpComponentsBackwards.put((String)obj, (JComponent)aComponent);
					if(jFound instanceof LabeledTextComponent){
						return ((LabeledTextComponent)jFound).getJTextComponent();
					}
					if(jFound instanceof LabeledComponent){
						return ((LabeledComponent)jFound).getControlComponent();
					}
					if (jFound instanceof OptionGroup) {
						Enumeration elements = ((OptionGroup)jFound).getButtonGroup().getElements();
						if (elements.hasMoreElements())
							jFound = (JComponent)elements.nextElement();
					}
					return jFound;
				}
			}
		}
		Component comp = super.getComponentAfter(aContainer, aComponent);	
		if(comp == null)
			return null;
		if(comp.equals(aComponent))
			return comp;
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
				comp = getSubFormIfAny(bt);
				if(comp instanceof SubForm) {
					SubForm subform = (SubForm)comp;
					subform.fireFocusGained();
				}
			}
		}
		if(UIUtils.isEditable(comp))
			return comp;
		else {
			// @see  	NUCLOS-1018 - we need an exit criteria to prevent a stack overflow 
			if (LangUtils.equals(comp, componentAfter)) {
				componentAfter = null;
				return null;
			}
			else {
				if (componentAfter == null)
					componentAfter = comp;
				return _getComponentAfter(aContainer, comp);
			}			
		}			
	}
	
	private void getFocusableSubFormComponent(String value) {
		// component is not part of panel. must be a subform.
		int idxDot = value.indexOf(".");
		String subformName = value.substring(0, idxDot);
		SubForm subform = mpSubforms.get(subformName);
		if (subform != null) {
			JTabbedPane tabbedPane = (JTabbedPane)UIUtils.findFirstParentJComponent(subform.getSubformTable(), JTabbedPane.class);
			if (tabbedPane != null) {
				boolean bSelected = false;
				for (int i = 0; i < tabbedPane.getTabCount(); i++) {
					// find contained subform.
					//JTable subform_tmp = (JTable)UIUtils.findFirstJComponent((JComponent)tabbedPane.getComponentAt(i), JScrollPane.class); {
					for (JTable subform_tmp : UIUtils.findAllInstancesOf(tabbedPane.getComponentAt(i), SubFormTable.class)) {
						if (subform_tmp == subform.getSubformTable()) {
							tabbedPane.setSelectedIndex(i);
							bSelected = true;
							break;
						}
					}
					if (bSelected)
						break;
				}
			}
				
			String sColumnName = value.substring(idxDot + 1);
			TableModel mdl = subform.getSubformTable().getModel();
			if (mdl instanceof CollectableTableModel) {
				int idxCol = ((CollectableTableModel)mdl).findColumnByFieldName(sColumnName);
				if (idxCol != -1) {
					if (subform.getSubformTable().getRowCount() == 0) {
						AbstractButton button = subform.getToolbarButton(SubForm.ToolbarFunction.NEW.name());
						button.doClick();
					}
					if (subform.getSubformTable().getRowCount() != 0) {
						subform.getSubformTable().changeSelection(0, idxCol, false, false);
					}
					return;
				}
			}
		}
	}
	
	private Component getSubFormIfAny(JButton bt) {
		try {
			return bt.getParent().getParent().getParent().getParent();
		}
		catch (Exception e) {
			// no Subform return default
			LOG.warn("getSubFormIfAny failed: " + e);
		}
		return bt;
	}

	private Component componentBefore;
	@Override
	public Component getComponentBefore(Container aContainer, Component aComponent) {
		// @see  	NUCLOS-1018 - prevent a stack overflow 
		Component c =  _getComponentAfter(aContainer, aComponent);
		componentBefore = null; // reset component before.
		
		return c;
	}
	public Component _getComponentBefore(Container aContainer, Component aComponent) {
		boolean bOption = false;
		if (aComponent.getParent() instanceof OptionGroup) {
			Enumeration elements = ((OptionGroup)aComponent.getParent()).getButtonGroup().getElements();
			while (elements.hasMoreElements()) {
				Object elem = elements.nextElement();
				if (elem.equals(aComponent)) {// first option
					break;
				}
				bOption = true;
				break;
			}
		}
			
		if(!bOption && aComponent instanceof JComponent) {
			JComponent jFound = getComponentBefore(aComponent);
			if (jFound == null) {
				jFound = getComponentBefore(aComponent.getParent());
				if (jFound != null)
					return jFound;
			} else
				return jFound;
			if(mpComponentsBackwards.containsValue((JComponent)aComponent)) {
				for(String sNext : mpComponentsBackwards.keySet()) {
					if(mpComponentsBackwards.get(sNext).equals(aComponent)) {
						jFound = UIUtils.findJComponentStartsWithName((JPanel)aContainer, sNext);
						if(jFound instanceof LabeledComponent){
							return ((LabeledComponent)jFound).getControlComponent();
						}
						return jFound;
					}
				}
			}
		}
		Component comp = super.getComponentBefore(aContainer, aComponent);
		if(UIUtils.isEditable(comp))
			return comp;
		else{
			// @see  	NUCLOS-1018 - we need an exit criteria to prevent a stack overflow 
			if (LangUtils.equals(comp, componentBefore)) {
				componentBefore = null;
				return null;
			}
			else {
				if (componentBefore == null)
					componentBefore = comp;
				return _getComponentBefore(aContainer, comp);
			}			
		}	
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
