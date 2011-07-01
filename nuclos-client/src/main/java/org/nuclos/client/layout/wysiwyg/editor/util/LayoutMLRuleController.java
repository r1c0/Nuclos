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
package org.nuclos.client.layout.wysiwyg.editor.util;

import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSubForm;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRule;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRules;

/**
 * This class is used for attaching {@link LayoutMLRules} to the corresponding {@link WYSIWYGComponent}.
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class LayoutMLRuleController {

	/**
	 * This Method attaches the {@link LayoutMLRules} to the transfered {@link WYSIWYGComponent}
	 * @param c
	 * @param layoutMLRules
	 */
	public static void attachRulesToComponent(WYSIWYGComponent c, LayoutMLRules layoutMLRules) {
		LayoutMLRules componentRules = c.getLayoutMLRulesIfCapable();
		
		c.getParentEditor().getUndoRedoFunction().loggingChangeComponentsLayoutMLRules(c, componentRules, c.getParentEditor().getTableLayoutUtil());
		
		componentRules.clearRulesForComponent();

		for (LayoutMLRule rule : layoutMLRules.getRules()) {
			componentRules.addRule(rule);
		}
		
		componentRules = c.getLayoutMLRulesIfCapable();
		if (c instanceof WYSIWYGSubForm){
			((WYSIWYGSubForm)c).attachSubformColumnRulesToSubformColumns();
		}
		c.getParentEditor().getUndoRedoFunction().loggingChangeComponentsLayoutMLRules(c, componentRules, c.getParentEditor().getTableLayoutUtil());
	}
	
	/**
	 * Removes the {@link LayoutMLRules} from a {@link WYSIWYGComponent}
	 * @param c
	 */
	public static void clearLayoutMLRulesForComponent(WYSIWYGComponent c) {
		LayoutMLRules componentRules = c.getLayoutMLRulesIfCapable();
		c.getParentEditor().getUndoRedoFunction().loggingChangeComponentsLayoutMLRules(c, componentRules, c.getParentEditor().getTableLayoutUtil());
		componentRules.clearRulesForComponent();
		componentRules = c.getLayoutMLRulesIfCapable();
		c.getParentEditor().getUndoRedoFunction().loggingChangeComponentsLayoutMLRules(c, componentRules, c.getParentEditor().getTableLayoutUtil());

	}
	
}
