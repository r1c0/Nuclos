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
 * This Class represents a single {@link LayoutMLRuleAction}<br>
 * It can be: 
 * <ul>
 * <li> {@link LayoutMLConstants#ELEMENT_TRANSFERLOOKEDUPVALUE}</li>
 * <li> {@link LayoutMLConstants#ELEMENT_CLEAR}</li>
 * <li> {@link LayoutMLConstants#ELEMENT_ENABLE} <b>NOT USED - Parser does not create a Action</b></li>
 * <li> {@link LayoutMLConstants#ELEMENT_REFRESHVALUELIST}</li>
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
public class LayoutMLRuleAction implements Serializable {

	public static final String TRANSFER_LOOKEDUP_VALUE = LayoutMLConstants.ELEMENT_TRANSFERLOOKEDUPVALUE;
	public static final String CLEAR = LayoutMLConstants.ELEMENT_CLEAR;
	public static final String ENABLE = LayoutMLConstants.ELEMENT_ENABLE;
	public static final String REFRESH_VALUELIST = LayoutMLConstants.ELEMENT_REFRESHVALUELIST;

	private String sourceField;
	private String targetComponent;
	private String entity;
	private boolean invertable = false;
	private String parameterForSourceComponent;

	private String ruleAction;

	/**
	 * <ul>
	 * <li> {@link LayoutMLConstants#ELEMENT_TRANSFERLOOKEDUPVALUE}</li>
	 * <li> {@link LayoutMLConstants#ELEMENT_CLEAR}</li>
	 * <li> {@link LayoutMLConstants#ELEMENT_ENABLE} <b>NOT USED - Parser does not create a Action</b></li>
	 * <li> {@link LayoutMLConstants#ELEMENT_REFRESHVALUELIST}</li>
	 * </ul>
	 * @param ruleAction to set 
	 */
	public void setRuleAction(String ruleAction) {
		this.ruleAction = ruleAction;
	}

	/**
	 * @param sourceField to set {@link LayoutMLConstants#ATTRIBUTE_SOURCEFIELD}
	 */
	public void setSourceField(String sourceField) {
		this.sourceField = sourceField;
	}
	
	/**
	 * @param entity the Entity to set (if it is a Subform Action)
	 */
	public void setEntity(String entity) {
		this.entity = entity;
	}

	/**
	 * @param parameterForSourceComponent to set {@link LayoutMLConstants#ATTRIBUTE_PARAMETER_FOR_SOURCECOMPONENT}
	 */
	public void setParameterForSourceComponent(String parameterForSourceComponent) {
		this.parameterForSourceComponent = parameterForSourceComponent;
	}

	/**
	 * @param targetComponent to set {@link LayoutMLConstants#ATTRIBUTE_TARGETCOMPONENT}
	 */
	public void setTargetComponent(String targetComponent) {
		this.targetComponent = targetComponent;
	}

	/**
	 * @param invertable to set {@link LayoutMLConstants#ATTRIBUTE_INVERTABLE}
	 */
	public void setInvertable(boolean invertable) {
		this.invertable = invertable;
	}

	/**
	 * <ul>
	 * <li> {@link LayoutMLConstants#ELEMENT_TRANSFERLOOKEDUPVALUE}</li>
	 * <li> {@link LayoutMLConstants#ELEMENT_CLEAR}</li>
	 * <li> {@link LayoutMLConstants#ELEMENT_ENABLE} <b>NOT USED - Parser does not create a Action</b></li>
	 * <li> {@link LayoutMLConstants#ELEMENT_REFRESHVALUELIST}</li>
	 * </ul>
	 * @return the Action for this Rule
	 */
	public String getRuleAction() {
		return ruleAction;
	}

	/**
	 * return the SourceField for {@link LayoutMLConstants#ATTRIBUTE_SOURCEFIELD}
	 */
	public String getSourceField() {
		return sourceField;
	}

	/**
	 * @return the TargetComponent for {@link LayoutMLConstants#ATTRIBUTE_TARGETCOMPONENT}
	 */
	public String getTargetComponent() {
		return targetComponent;
	}

	/**
	 * @return the Entity for {@link LayoutMLConstants#ATTRIBUTE_ENTITY}
	 */
	public String getEntity() {
		return entity;
	}

	/**
	 * @return boolean value for {@link LayoutMLConstants#ATTRIBUTE_INVERTABLE}
	 */
	public boolean isInvertable() {
		return invertable;
	}

	/**
	 * @return the Parameter for the {@link LayoutMLConstants#ATTRIBUTE_SOURCECOMPONENT}
	 */
	public String getParameterForSourceComponent() {
		return parameterForSourceComponent;
	}

	/** 
	 * Overwritten clone Method, creating a new Instance of this Object
	 * 
	 * @return new Instance of this Object
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		LayoutMLRuleAction layoutMLRuleAction = new LayoutMLRuleAction();
		if (sourceField != null)
			layoutMLRuleAction.sourceField = new String(sourceField);
		if (targetComponent != null)
			layoutMLRuleAction.targetComponent = new String(targetComponent);
		if (entity != null)
			layoutMLRuleAction.entity = new String(entity);
		layoutMLRuleAction.invertable = invertable;
		if (parameterForSourceComponent != null)
			layoutMLRuleAction.parameterForSourceComponent = new String(parameterForSourceComponent);
		if (ruleAction != null)
			layoutMLRuleAction.ruleAction = new String(ruleAction);

		return layoutMLRuleAction;
	}

	/**
	 * Overwritten equals for compairing {@link LayoutMLRuleAction} Objects
	 * 
	 * @param obj the Object to compare to this Object
	 * @return true if the content is equal, false if not
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LayoutMLRuleAction))
			return false;

		LayoutMLRuleAction action = (LayoutMLRuleAction) obj;
		
		if (action.ruleAction != null) {
			if (action.getSourceField() == null){
				if (sourceField != null)
					return false;
			} else {
				if (!action.sourceField.equals(sourceField))
				return false;
			}
		} else {
			if (sourceField != null)
				return false;
		}
		if (action.ruleAction != null) {
			if (!action.ruleAction.equals(ruleAction))
				return false;
		} else {
			if (ruleAction != null)
				return false;
		}
		if (action.targetComponent != null) {
			if (!action.targetComponent.equals(targetComponent))
				return false;
		} else {
			if (targetComponent != null)
				return false;
		}
		if (action.entity != null) {
			if (!action.entity.equals(entity))
				return false;
		} else {
			if (entity != null)
				return false;
		}
		if (!action.invertable == invertable)
			return false;
		if (action.parameterForSourceComponent != null) {
			if (!action.parameterForSourceComponent.equals(parameterForSourceComponent))
				return false;
		} else {
			if (parameterForSourceComponent != null)
				return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("RuleAction= " +this.ruleAction + "\n");
		sb.append("Entity= " +this.entity + "\n");
		sb.append("SourceField= " +this.sourceField + "\n");
		sb.append("TargetComponent= " +this.targetComponent + "\n");
		sb.append("Parameter for SourceComponent= " +this.parameterForSourceComponent + "\n");

		return sb.toString();
	}
	
	
}
