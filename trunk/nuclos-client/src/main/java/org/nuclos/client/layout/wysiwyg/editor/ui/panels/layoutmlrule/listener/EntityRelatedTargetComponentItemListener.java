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
package org.nuclos.client.layout.wysiwyg.editor.ui.panels.layoutmlrule.listener;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;

import org.nuclos.common2.StringUtils;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.layoutmlrule.LayoutMLRuleValidationLayer;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRuleAction;

/**
 * {@link ItemListener} for controlling the dependency between Entity and TargetComponent
 * @author hartmut.beckschulze
 *
 */
public class EntityRelatedTargetComponentItemListener implements ItemListener{

	private JComboBox targetComponent = null;
	private LayoutMLRuleAction layoutMLRuleAction = null;
	private WYSIWYGLayoutEditorPanel editorPanel = null;
	private JComboBox parameterForSourceComponent = null;
	
	/**
	 * 
	 * @param layoutMLRuleAction
	 * @param editorPanel
	 * @param targetComponent
	 * @param parameterForSourceComponent 
	 */
	public EntityRelatedTargetComponentItemListener(LayoutMLRuleAction layoutMLRuleAction, WYSIWYGLayoutEditorPanel editorPanel, JComboBox targetComponent, JComboBox parameterForSourceComponent) {
		this.targetComponent = targetComponent;
		this.layoutMLRuleAction = layoutMLRuleAction;
		this.editorPanel = editorPanel;
		this.parameterForSourceComponent = parameterForSourceComponent;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
				String selectedItem = e.getItem().toString();
								
				layoutMLRuleAction.setEntity(selectedItem);
				/**
				 * refreshing the targetcomponent depending on the selected entity
				 */
				targetComponent.removeAllItems();
				parameterForSourceComponent.removeAllItems();
				
				String[] collectableComponentsWithVP = null;
				if (!StringUtils.isNullOrEmpty(layoutMLRuleAction.getEntity())){
					collectableComponentsWithVP = LayoutMLRuleValidationLayer.getSubformColumnsWithValueListProvider(editorPanel, layoutMLRuleAction.getEntity());
				} else {
					collectableComponentsWithVP = LayoutMLRuleValidationLayer.getCollectableComponentsWithValueListProvider(editorPanel);
				}
				if (collectableComponentsWithVP != null) {
					for (String collComp : collectableComponentsWithVP) {
						targetComponent.addItem(collComp);
					}
					targetComponent.addItem("");
					targetComponent.setEnabled(true);
					parameterForSourceComponent.setEnabled(true);
				} else {
					// there are no components with vp, disabling the combobox, no entries could be made
					targetComponent.setEnabled(false);
					parameterForSourceComponent.setEnabled(false);
				}
		}
	}

}
