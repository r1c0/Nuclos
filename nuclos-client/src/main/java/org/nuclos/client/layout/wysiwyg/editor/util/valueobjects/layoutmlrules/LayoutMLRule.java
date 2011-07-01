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
import org.nuclos.client.layout.wysiwyg.LayoutMLLoader;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSubFormColumn;

/**
 * Valueclass for storing {@link LayoutMLConstants#ELEMENT_RULE}<br>
 * It contains:
 * <ul>
 * <li> the Name of the Rule</li>
 * <li> {@link LayoutMLRuleEventType} </li>
 * <li> {@link LayoutMLRuleCondition} <b>Not used - Parser does not understand this</b></li>
 * <li> {@link LayoutMLRuleActions} </li>
 * </ul>
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
public class LayoutMLRule implements Serializable, Cloneable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private LayoutMLRuleEventType layoutMLRuleEventType = new LayoutMLRuleEventType();
	private LayoutMLRuleCondition layoutMLRuleCondition = new LayoutMLRuleCondition();
	private LayoutMLRuleActions layoutMLRuleActions = new LayoutMLRuleActions();
	private String ruleName = "";
	
	private String componentName = null;
	private String componentEntity = null;
	
	private boolean isSubformEntity = false;
	//NUCLEUSINT-341
	private boolean isListOfValuesORCombobox = false;
	
	/**
	 * Constructor, directly setting the Name of the {@link LayoutMLRule#setRuleName(String)}
	 * @param ruleName
	 */
	public LayoutMLRule(String ruleName) {
		this.ruleName = ruleName;
	}

	/**
	 * Empty Constructor
	 */
	public LayoutMLRule() {}

	
	/**
	 * @return the {@link LayoutMLRuleEventType#LOOKUP} or {@link LayoutMLRuleEventType#VALUE_CHANGED}
	 */
	public LayoutMLRuleEventType getLayoutMLRuleEventType(){
		return this.layoutMLRuleEventType;
	}
	
	/**
	 * @return the condition <b>Not used - Parser does not understand this</b>
	 */
	public LayoutMLRuleCondition getLayoutMLRuleCondition(){
		return this.layoutMLRuleCondition;
	}
	
	/**
	 * @return the {@link LayoutMLRuleActions} defined for this {@link LayoutMLRule}
	 */
	public LayoutMLRuleActions getLayoutMLRuleActions(){
		return this.layoutMLRuleActions;
	}
	
	/**
	 * @param ruleName sets the Name of the Rule 
	 * @see LayoutMLConstants#ATTRIBUTE_NAME
	 */
	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}
	
	/**
	 * @return the Name of the Rule
	 */
	public String getRuleName() {
		return ruleName;
	}
	
	public String getComponentName() {
		return this.componentName;
	}
	
	public String getComponentEntity() {
		return this.componentEntity;
	}
	
	public void setComponentNameAndEntity(WYSIWYGComponent ruleSourceComponent) {
		if (ruleSourceComponent instanceof WYSIWYGSubFormColumn) {
			this.componentEntity = ((WYSIWYGSubFormColumn)ruleSourceComponent).getSubForm().getEntityName();
			this.componentName = ((WYSIWYGSubFormColumn)ruleSourceComponent).getName();
			setSubformEntity();
		} else if (ruleSourceComponent instanceof WYSIWYGCollectableComponent) {
			this.componentName = ((WYSIWYGCollectableComponent)ruleSourceComponent).getName();
		}
		
	}
	
	/**
	 * Small HelperMethod for detecting a SubformRule quickly
	 * @return true if Subform
	 */
	public boolean isSubformEntity() {
		return isSubformEntity;
	}
	
	/**
	 * This Method is called when parsing the LayoutML and when creating a new Rule
	 * @see LayoutMLLoader
	 */
	public void setSubformEntity() {
		this.isSubformEntity = true;
	}
	
	/**
	 * NUCLEUSINT-341
	 * Small HelperMethod for detecting a ListOfValueRule quickly
	 * @return true if ListOfValue
	 */
	public boolean isListOfValues() {
		return isListOfValuesORCombobox;
	}

	/**
	 * NUCLEUSINT-341
	 * This Method is called when parsing the LayoutML and when creating a new Rule
	 * @see LayoutMLLoader
	 */
	public void setListOfValues() {
		this.isListOfValuesORCombobox = true;
	}

	/**
	 * Overwritten Method to create a new Instance of this {@link LayoutMLRule} Object
	 * 
	 * @return new Instance of this Object
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		LayoutMLRule layoutMLRule = new LayoutMLRule();
		
		layoutMLRule.layoutMLRuleEventType = (LayoutMLRuleEventType) layoutMLRuleEventType.clone();
		layoutMLRule.layoutMLRuleCondition = (LayoutMLRuleCondition) layoutMLRuleCondition.clone();
		layoutMLRule.layoutMLRuleActions = (LayoutMLRuleActions) layoutMLRuleActions.clone();
		layoutMLRule.ruleName = new String (ruleName);
		layoutMLRule.isListOfValuesORCombobox = new Boolean(isListOfValuesORCombobox);
		layoutMLRule.isSubformEntity = new Boolean(isSubformEntity);
		
		return layoutMLRule;
	}
	

	/**
	 * Overwritten equals for compairing {@link LayoutMLRule} Objects<br>
	 * Calls:
	 * <ul>
	 * <li> {@link LayoutMLRuleAction#equals(Object)}</li>
	 * <li> {@link LayoutMLRuleCondition#equals(Object)}</li>
	 * <li> {@link LayoutMLRuleEventType#equals(Object)}</li>
	 * </ul>
	 * 
	 * @param obj the Object to compare to this Object
	 * @return true if the content is equal, false if not
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LayoutMLRule))
			return false;
		
		LayoutMLRule rule = (LayoutMLRule)obj;
		
		if (!rule.layoutMLRuleEventType.equals(this.layoutMLRuleEventType))
			return false;
		if (!rule.layoutMLRuleCondition.equals(this.layoutMLRuleCondition))
			return false;
		if (!rule.layoutMLRuleActions.equals(this.layoutMLRuleActions))
			return false;
		if (!rule.ruleName.equals(this.ruleName))
			return false;
		if (!rule.isSubformEntity == this.isSubformEntity)
			return false;
		if (!rule.isListOfValuesORCombobox == this.isListOfValuesORCombobox)
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
		
		sb.append("Rule= " + this.ruleName + "\n");
		sb.append("subformentity= " + this.isSubformEntity + "\n");
		sb.append("Listofvalues or Combobox= " + this.isSubformEntity + "\n");
		sb.append(this.layoutMLRuleEventType.toString() + "\n");
		sb.append(this.layoutMLRuleActions.toString() + "\n");
		
		return sb.toString();
	}
	
}
