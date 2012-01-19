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
import java.util.Vector;

import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;

/**
 * This Class contains {@link LayoutMLRule} Objects.<br>
 * It does belong to one {@link WYSIWYGComponent}
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class LayoutMLRules implements Serializable {

	/** this is where the rules are stored */
	private Vector<LayoutMLRule> singleRules = new Vector<LayoutMLRule>();

	/**
	 * @param rule the {@link LayoutMLRule} to add
	 */
	public void addRule(LayoutMLRule rule) {
		singleRules.add(rule);
	}
	
	/**
	 * @return the number of {@link LayoutMLRule} contained in this "RuleSet"
	 */
	public int getSize(){
		return singleRules.size();
	}
	
	/**
	 * @return all {@link LayoutMLRule} for the {@link WYSIWYGComponent}
	 */
	public Vector<LayoutMLRule> getRules(){
		return this.singleRules;
	}

	/**
	 * Clears all {@link LayoutMLRule} for this {@link WYSIWYGComponent}
	 */
	public void clearRulesForComponent() {
		singleRules.clear();
	}

	/**
	 * Overwritten clone Method to create a new Instance of this Object<br>
	 * Calls {@link LayoutMLRule#clone()}
	 * @return new Instance of this Object
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		LayoutMLRules layoutMLRules = new LayoutMLRules();
	
		for (LayoutMLRule rule : singleRules) {
			layoutMLRules.addRule((LayoutMLRule)rule.clone());
		}
	
		return layoutMLRules;
	}

	/**
	 * Overwritten equals Method to compare the incoming {@link LayoutMLRules} object with this Objects
	 * 
	 * @param obj the Object to compare to this Object
	 * @return true if the content is equal, false if not
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LayoutMLRules))
			return false;
		
		LayoutMLRules rules = (LayoutMLRules) obj;
		
		if (!rules.getRules().equals(singleRules))
			return false;
		
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		for (LayoutMLRule singleRule: this.singleRules){
			sb.append(singleRule.toString());
			sb.append("\n");
		}
		
		return sb.toString();
	}
	
	
}
