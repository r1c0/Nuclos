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

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map.Entry;

import org.nuclos.client.layout.wysiwyg.editor.ui.panels.layoutmlrule.LayoutMLRuleActionPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.layoutmlrule.LayoutMLRuleValidationLayer;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRuleAction;

/**
 * Small {@link ItemListener} for saving the selected {@link LayoutMLRuleAction}
 * @author hartmut.beckschulze
 *
 */
public class ActionAwareItemListener implements ItemListener {

	private LayoutMLRuleAction layoutMLRuleAction = null;
	private LayoutMLRuleActionPanel layoutMLRuleActionPanel = null;
	
	/**
	 * 
	 * @param layoutMLRuleAction
	 * @param layoutMLRuleActionPanel
	 */
	public ActionAwareItemListener(LayoutMLRuleAction layoutMLRuleAction, LayoutMLRuleActionPanel layoutMLRuleActionPanel) {
		this.layoutMLRuleAction = layoutMLRuleAction;
		this.layoutMLRuleActionPanel = layoutMLRuleActionPanel;
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			Component actionPanel = ((Component) e.getSource()).getParent();
			if (actionPanel instanceof LayoutMLRuleActionPanel) {
				String itemValue = e.getItem().toString();
				for (Entry<String, String> entry : LayoutMLRuleValidationLayer.actionType.entrySet()) {
					if (entry.getValue().equals(itemValue)) {
						layoutMLRuleAction.setRuleAction(entry.getKey());
					}
				}
				layoutMLRuleActionPanel.changeActionDetailPanels(itemValue);
			}
		}
	}
	
	
}
