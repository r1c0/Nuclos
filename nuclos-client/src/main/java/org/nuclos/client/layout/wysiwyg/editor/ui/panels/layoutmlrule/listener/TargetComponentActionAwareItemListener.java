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

import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRuleAction;

/**
 * Small {@link ItemListener} for saving the targetcomponent to the {@link LayoutMLRuleAction}
 * @author hartmut.beckschulze
 *
 */
public class TargetComponentActionAwareItemListener implements ItemListener {

	private LayoutMLRuleAction layoutMLRuleAction = null;
	
	/**
	 * 
	 * @param layoutMLRuleAction
	 */
	public TargetComponentActionAwareItemListener(LayoutMLRuleAction layoutMLRuleAction) {
		this.layoutMLRuleAction = layoutMLRuleAction;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			layoutMLRuleAction.setTargetComponent(e.getItem().toString());
		}
	}
}
