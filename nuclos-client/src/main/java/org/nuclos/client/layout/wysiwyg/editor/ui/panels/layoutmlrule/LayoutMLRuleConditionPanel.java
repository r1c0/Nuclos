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
package org.nuclos.client.layout.wysiwyg.editor.ui.panels.layoutmlrule;

import java.awt.Color;

import javax.swing.JPanel;

import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRuleCondition;

/**
 * Small Class, prepared for editing Conditions.<br>
 * But the Parser does not create conditions, therefore this is discontinued...
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
@SuppressWarnings("serial")
public class LayoutMLRuleConditionPanel extends JPanel {
	
		/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		private LayoutMLRuleCondition layoutMLRuleCondition = null;
		
		/**
		 * Default Constructor
		 * @param backgroundColor passing the Background Color because the Color changes alternating
		 * @param layoutMLRuleCondition the {@link LayoutMLRuleCondition} to be edited
		 */
		public LayoutMLRuleConditionPanel(Color backgroundColor, LayoutMLRuleCondition layoutMLRuleCondition) {
			this.layoutMLRuleCondition = layoutMLRuleCondition;
		}

		/**
		 * @return the Condition attached to this {@link LayoutMLRuleConditionPanel}
		 */
		public LayoutMLRuleCondition getLayoutMLRuleCondition() {
			return layoutMLRuleCondition;
		}
	

	
}
