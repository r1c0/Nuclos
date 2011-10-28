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
package org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules;

import java.io.Serializable;

import org.nuclos.common2.layoutml.LayoutMLConstants;

/**
 * This Class represents a {@link LayoutMLConstants#ELEMENT_CONDITION}<br>
 * <b>Not supported from the Parser - inactive</b>
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class LayoutMLRuleCondition implements Serializable {

	private String conditionString = "";

	/**
	 * @return the Condition
	 */
	public String getConditionString() {
		return this.conditionString;
	}

	/**
	 * @param condition the Condition to be set
	 */
	public void setConditionString(String condition) {
		this.conditionString = condition;
	}

	/**
	 * Overwritten clone Method to create a new Instance of this Object
	 * @return new Instance of this Object
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		LayoutMLRuleCondition layoutMLRuleCondition = new LayoutMLRuleCondition();
		if (conditionString != null)
			layoutMLRuleCondition.conditionString = new String(conditionString);

		return layoutMLRuleCondition;
	}

	/**
	 * Overwritten equals Method to check if the provided {@link LayoutMLRuleCondition} is the same as this Object.
	 * 
	 * @param obj the Object to compare to this Object
	 * @return true if the content is equal, false if not
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LayoutMLRuleCondition))
			return false;

		if (!this.conditionString.equals(conditionString))
			return false;

		return true;

	}
}
