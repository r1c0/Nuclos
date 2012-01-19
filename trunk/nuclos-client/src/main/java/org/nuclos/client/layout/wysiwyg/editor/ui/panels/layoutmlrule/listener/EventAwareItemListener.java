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
import java.util.Map.Entry;

import org.nuclos.client.layout.wysiwyg.editor.ui.panels.layoutmlrule.LayoutMLRuleEventPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.layoutmlrule.LayoutMLRuleValidationLayer;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRuleEventType;

/**
 * {@link ItemListener} controlling the events for {@link LayoutMLRuleEventType}
 * @author hartmut.beckschulze
 *
 */
public class EventAwareItemListener implements ItemListener{

	private LayoutMLRuleEventType layoutMLRuleEventType = null;
	private LayoutMLRuleEventPanel eventPanel = null;
	
	/**
	 * 
	 * @param layoutMLRuleEventType
	 * @param eventPanel
	 */
	public EventAwareItemListener(LayoutMLRuleEventType layoutMLRuleEventType, LayoutMLRuleEventPanel eventPanel) {
		this.layoutMLRuleEventType = layoutMLRuleEventType;
		this.eventPanel = eventPanel;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			eventPanel.removeAllActions();
			if ("".equals(e.getItem().toString())){
				layoutMLRuleEventType.setEventType(null);
			} else {
				for (Entry<String, String> entry : LayoutMLRuleValidationLayer.eventType.entrySet()) {
					if (entry.getValue().equals(e.getItem())) {
						layoutMLRuleEventType.setEventType(entry.getKey());
					}
				}
			}
			if(eventPanel.getSingleRulePanel().getActionPanelsForSingleRulePanel().size() == 0) {
				// this is a new rule and its just created, so we need a action panel at first
				eventPanel.addAnotherAction();
			} else {
				// its a existing rule, so there must be a refresh
				eventPanel.refreshActionsOnEventChange();
			}
		}
	}
}
