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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.nuclos.common2.StringUtils;
import org.nuclos.common2.layoutml.LayoutMLConstants;
import org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.LAYOUTML_RULE_EDITOR;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSubForm;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSubFormColumn;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueValuelistProvider;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRule;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRuleAction;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRuleEventType;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRules;
import org.nuclos.common.NuclosBusinessException;

/**
 * Class for (de)activating Actions
 * 
 * Some Events and Actions are not added to components by layoutml Parser
 *
 * @author hartmut.beckschulze
 */
public class LayoutMLRuleValidationLayer{

	public static HashMap<String, String> eventType = new HashMap<String, String>();
	public static HashMap<String, String> actionType = new HashMap<String, String>();


	{
		/** key - label */
		eventType.put(LayoutMLRuleEventType.LOOKUP, LAYOUTML_RULE_EDITOR.NAME_FOR_EVENT_LOOKUP);
		eventType.put(LayoutMLRuleEventType.VALUE_CHANGED, LAYOUTML_RULE_EDITOR.NAME_FOR_EVENT_VALUE_CHANGED);

		actionType.put(LayoutMLRuleAction.TRANSFER_LOOKEDUP_VALUE, LAYOUTML_RULE_EDITOR.NAME_FOR_ACTION_TRANSFER_LOOKEDUP_VALUE);
		actionType.put(LayoutMLRuleAction.CLEAR, LAYOUTML_RULE_EDITOR.NAME_FOR_ACTION_CLEAR);
		actionType.put(LayoutMLRuleAction.ENABLE, LAYOUTML_RULE_EDITOR.NAME_FOR_ACTION_ENABLE);
		actionType.put(LayoutMLRuleAction.REFRESH_VALUELIST, LAYOUTML_RULE_EDITOR.NAME_FOR_ACTION_REFRESH_VALUELIST);
	}
	
	/**
	 * This method checks which controlType the component is of and returns the fitting Actions
	 * for the Event.
	 * @param eventType
	 * @param controlType
	 * @return {@link String}[] 
	 */
	public static String[] getActionsForEventType(String eventType, String controlType) {
		if (LayoutMLConstants.ELEMENT_SUBFORMCOLUMN.equals(controlType))
			return getActionsForSubformColumns(eventType);
		else
			return getActionsForCollectableComponents(eventType, controlType);
	}
	
	/**
	 * This Method returns the possible EventTypes for Components depending on their controlType
	 * @param metaInformation 
	 * @param layoutMLRule 
	 * @param controlType
	 * @return {@link String}[] 
	 */
	public static String[] getEventTypesForComponents(WYSIWYGMetaInformation metaInformation, LayoutMLRule layoutMLRule, String controlType) {
		if (LayoutMLConstants.ELEMENT_SUBFORMCOLUMN.equals(controlType)) {
			boolean lovORcombobox = layoutMLRule.isListOfValues();
			if (lovORcombobox)
				return getEventTypesForSubformColumns(true);
			else
				return getEventTypesForSubformColumns(false);
		} 
		
		return getEventTypesForCollectableComponents(metaInformation, layoutMLRule, controlType);
	}
	
	/**
	 * @param lovORcombobox 
	 * @return possible {@link LayoutMLRuleEventType} for {@link WYSIWYGSubFormColumn}
	 */
	private static String[] getEventTypesForSubformColumns(boolean lovORcombobox) {
		if (lovORcombobox)
			return new String[]{LayoutMLRuleEventType.LOOKUP, LayoutMLRuleEventType.VALUE_CHANGED};

		return new String[]{LayoutMLRuleEventType.VALUE_CHANGED};
	}
	
	/**
	 * @param layoutMLRule 
	 * @param metaInformation 
	 * @return possible {@link LayoutMLRuleEventType} for {@link WYSIWYGCollectableComponent}
	 */
	private static String[] getEventTypesForCollectableComponents(WYSIWYGMetaInformation meta, LayoutMLRule layoutMLRule, String controlType) {
		if (LayoutMLConstants.ATTRIBUTEVALUE_LISTOFVALUES.equals(controlType)) {
			return new String[]{LayoutMLRuleEventType.LOOKUP, LayoutMLRuleEventType.VALUE_CHANGED};
		} else if (LayoutMLConstants.CONTROLTYPE_COMBOBOX.equals(controlType)) {
			String linkedEntity = meta.getLinkedEntityForAttribute(layoutMLRule.getComponentEntity(), layoutMLRule.getComponentName());
			if (linkedEntity != null) {
				return new String[]{LayoutMLRuleEventType.LOOKUP, LayoutMLRuleEventType.VALUE_CHANGED};
			}
		}
		return new String[]{LayoutMLRuleEventType.VALUE_CHANGED};
	}
	
	/**
	 * @param eventType {@link LayoutMLRuleEventType} 
	 * @return {@link LayoutMLRuleAction} allowed for this {@link LayoutMLRuleEventType}
	 */
	private static String[] getActionsForSubformColumns(String eventType) {
		if(LayoutMLRuleEventType.LOOKUP.equals(eventType))
			return new String[]{LayoutMLRuleAction.TRANSFER_LOOKEDUP_VALUE};
		if(LayoutMLRuleEventType.VALUE_CHANGED.equals(eventType))
			return new String[]{LayoutMLRuleAction.CLEAR, LayoutMLRuleAction.REFRESH_VALUELIST};
		return null;
	}
	
	/**
	 * @param controlType the ControlType for the {@link WYSIWYGCollectableComponent}
	 * @param eventType {@link LayoutMLRuleEventType} 
	 * @return {@link LayoutMLRuleAction} allowed for this {@link LayoutMLRuleEventType}
	 */
	private static String[] getActionsForCollectableComponents(String eventType, String controlType) {
		if(LayoutMLRuleEventType.LOOKUP.equals(eventType))
			return new String[]{LayoutMLRuleAction.TRANSFER_LOOKEDUP_VALUE};
		if(LayoutMLRuleEventType.VALUE_CHANGED.equals(eventType))
			return new String[]{LayoutMLRuleAction.CLEAR, LayoutMLRuleAction.REFRESH_VALUELIST};
		return null;		
	}
	
	/**
	 * 
	 * @param editorPanel
	 * @param entity
	 * @return
	 */
	public static String[] getSubformColumnsWithValueListProvider(WYSIWYGLayoutEditorPanel editorPanel, String entity) {
		List<WYSIWYGComponent> subforms = new ArrayList<WYSIWYGComponent>();
		//getting all Subforms
		editorPanel.getWYSIWYGComponents(WYSIWYGSubForm.class, editorPanel.getMainEditorPanel(), subforms);
		for (WYSIWYGComponent subform : subforms) {
			if (entity.equals(((WYSIWYGSubForm)subform).getEntityName())){
				return LayoutMLRuleValidationLayer.getValueListProviderComponents(editorPanel, (WYSIWYGSubForm)subform);
			}
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param editorPanel
	 * @return
	 */
	public static String[] getCollectableComponentsWithValueListProvider(WYSIWYGLayoutEditorPanel editorPanel) {
		return LayoutMLRuleValidationLayer.getValueListProviderComponents(editorPanel.getMainEditorPanel(), null);
	}
		
	/**
	 * 
	 * @param editorPanel the {@link WYSIWYGLayoutEditorPanel}
	 * @param subform optional the {@link WYSIWYGSubForm} for getting the {@link WYSIWYGSubFormColumn} with {@link PropertyValueValuelistProvider}
	 * @return a {@link String}[] with the names of the {@link WYSIWYGComponent}s found, null if there are none
	 */
	private static String[] getValueListProviderComponents(WYSIWYGLayoutEditorPanel editorPanel, WYSIWYGSubForm subform) {
		List<String> components = null;
		if (subform != null) {
			// just subformcolumns with vp
			components = subform.getColumnsWithValueListProvider();
		} else {
			// all collectable components with vp
			components = editorPanel.getCollectableComponentsWithValuelistProvider();
		}
		
		if (components != null)
			return components.toArray(new String[components.size()]);
		
		return null;
	}
	
	/**
	 * 
	 * @param editorPanel the {@link WYSIWYGLayoutEditorPanel} for getting the {@link WYSIWYGComponent}
	 * @return a {@link String}[] with the names of all {@link WYSIWYGCollectableComponent}, null if there are none
	 */
	public static String[] getAllCollectableComponents(WYSIWYGLayoutEditorPanel editorPanel) {
		List<String> collectableComponents = editorPanel.getCollectableComponents();
		
		if (collectableComponents != null)
			return collectableComponents.toArray(new String[collectableComponents.size()]);

		return null;		
	}
	
	/**
	 * @param subform the {@link WYSIWYGSubForm} to get the Names from
	 * @return a {@link String}[] with the Names of the Columns, may be null if there are none
	 */
	public static String[] getAllSubformColumns(WYSIWYGComponent component) {
		WYSIWYGSubForm subform = null;
		if (component instanceof WYSIWYGSubFormColumn) {
			subform = ((WYSIWYGSubFormColumn)component).getSubForm();
		}
		
		if (subform == null)
			return null;
		
		List<String> subformColumns = subform.getColumnNames();
		
		if (subformColumns != null)
			return subformColumns.toArray(new String[subformColumns.size()]);
		
		return null;
	}
	
	/**
	 * 
	 * @param editorPanel
	 * @return
	 */
	public static String[] getAllSubform(WYSIWYGLayoutEditorPanel editorPanel) {
		List<String> subformEntitys = editorPanel.getSubFormEntityNames();
		
		if (subformEntitys.size() > 0)
			return subformEntitys.toArray(new String[subformEntitys.size()]);
		
		return null;
	}
	
	public static void validateLayoutMLRules(LayoutMLRules layoutMLRules) throws NuclosBusinessException {
		StringBuffer errors = new StringBuffer();
		errors.append(WYSIWYGStringsAndLabels.ERROR_MESSAGES.ERROR_VALIDATING_LAYOUTMLRULES);
		boolean error = false;
		
		for(LayoutMLRule layoutMLRule : layoutMLRules.getRules()) {
			String ruleName = layoutMLRule.getRuleName() + "\n";
			if (layoutMLRule.isSubformEntity()){
				if (StringUtils.isNullOrEmpty(layoutMLRule.getComponentEntity())) {
					errors.append(ruleName);
					ruleName = "";
					errors.append(WYSIWYGStringsAndLabels.LAYOUTML_RULE_EDITOR.LABEL_ENTITY + " " + WYSIWYGStringsAndLabels.ERROR_MESSAGES.TEXT_MISSING + "\n");
					error = true;
				}
			}
			if (StringUtils.isNullOrEmpty(layoutMLRule.getComponentName())){
				errors.append(ruleName);
				ruleName = "";
				errors.append(WYSIWYGStringsAndLabels.LAYOUTML_RULE_EDITOR.LABEL_SOURCE_COMPONENT + " " + WYSIWYGStringsAndLabels.ERROR_MESSAGES.TEXT_MISSING + "\n");
				error = true;
			}
			if (StringUtils.isNullOrEmpty(layoutMLRule.getLayoutMLRuleEventType().getEventType())) {
				errors.append(ruleName);
				ruleName = "";
				errors.append(WYSIWYGStringsAndLabels.LAYOUTML_RULE_EDITOR.LABEL_EVENT_TRIGGERING_RULE + " " + WYSIWYGStringsAndLabels.ERROR_MESSAGES.TEXT_MISSING + "\n");
				error = true;
			}
			for (LayoutMLRuleAction action : layoutMLRule.getLayoutMLRuleActions().getSingleActions()) {
				if (StringUtils.isNullOrEmpty(action.getRuleAction())){
					errors.append(ruleName);
					ruleName = "";
					errors.append(WYSIWYGStringsAndLabels.LAYOUTML_RULE_EDITOR.LABEL_ACTION_SELECTOR + " " + WYSIWYGStringsAndLabels.ERROR_MESSAGES.TEXT_MISSING + "\n");
					error = true;
				} else {
				if (!layoutMLRule.isSubformEntity() && LayoutMLRuleAction.CLEAR.equals(action.getRuleAction())) {
					if (StringUtils.isNullOrEmpty(action.getTargetComponent())){
						errors.append(ruleName);
						ruleName = "";
						errors.append(WYSIWYGStringsAndLabels.LAYOUTML_RULE_EDITOR.LABEL_ACTION_SELECTOR + " " + actionType.get(action.getRuleAction()) + "\n");
						errors.append(WYSIWYGStringsAndLabels.LAYOUTML_RULE_EDITOR.LABEL_TARGET_COMPONENT + " " + WYSIWYGStringsAndLabels.ERROR_MESSAGES.TEXT_MISSING + "\n");
						error = true;
					}
				} else if (!layoutMLRule.isSubformEntity() && LayoutMLRuleAction.REFRESH_VALUELIST.equals(action.getRuleAction())) {
					if (StringUtils.isNullOrEmpty(action.getEntity())) {
						if (StringUtils.isNullOrEmpty(action.getTargetComponent())){
							errors.append(ruleName);
							ruleName = "";
							errors.append(WYSIWYGStringsAndLabels.LAYOUTML_RULE_EDITOR.LABEL_ACTION_SELECTOR + " " + actionType.get(action.getRuleAction()) + "\n");
							errors.append(WYSIWYGStringsAndLabels.LAYOUTML_RULE_EDITOR.LABEL_TARGET_COMPONENT + " " + WYSIWYGStringsAndLabels.ERROR_MESSAGES.TEXT_MISSING + "\n");
							error = true;
						}
					}
				} else if (layoutMLRule.isSubformEntity() && LayoutMLRuleAction.REFRESH_VALUELIST.equals(action.getRuleAction())) {
					if (StringUtils.isNullOrEmpty(action.getEntity())) {
						errors.append(ruleName);
						ruleName = "";
						errors.append(WYSIWYGStringsAndLabels.LAYOUTML_RULE_EDITOR.LABEL_ACTION_SELECTOR + " " + actionType.get(action.getRuleAction()) + "\n");
						errors.append(WYSIWYGStringsAndLabels.LAYOUTML_RULE_EDITOR.LABEL_ENTITY + " " + WYSIWYGStringsAndLabels.ERROR_MESSAGES.TEXT_MISSING + "\n");
						error = true;
					}
						if (StringUtils.isNullOrEmpty(action.getTargetComponent())){
							errors.append(ruleName);
							ruleName = "";
							errors.append(WYSIWYGStringsAndLabels.LAYOUTML_RULE_EDITOR.LABEL_ACTION_SELECTOR + " " + actionType.get(action.getRuleAction()) + "\n");
							errors.append(WYSIWYGStringsAndLabels.LAYOUTML_RULE_EDITOR.LABEL_TARGET_COMPONENT + " " + WYSIWYGStringsAndLabels.ERROR_MESSAGES.TEXT_MISSING + "\n");
							error = true;
						}
					} else if (LayoutMLRuleAction.TRANSFER_LOOKEDUP_VALUE.equals(action.getRuleAction())) {
						if (StringUtils.isNullOrEmpty(action.getTargetComponent())){
							errors.append(ruleName);
							ruleName = "";
							errors.append(WYSIWYGStringsAndLabels.LAYOUTML_RULE_EDITOR.LABEL_ACTION_SELECTOR + " " + actionType.get(action.getRuleAction()) + "\n");
							errors.append(WYSIWYGStringsAndLabels.LAYOUTML_RULE_EDITOR.LABEL_TARGET_COMPONENT + " " + WYSIWYGStringsAndLabels.ERROR_MESSAGES.TEXT_MISSING + "\n");
							error = true;
						}
						if (StringUtils.isNullOrEmpty(action.getSourceField())){
							errors.append(ruleName);
							ruleName = "";
							errors.append(WYSIWYGStringsAndLabels.LAYOUTML_RULE_EDITOR.LABEL_ACTION_SELECTOR + " " + actionType.get(action.getRuleAction()) + "\n");
							errors.append(WYSIWYGStringsAndLabels.LAYOUTML_RULE_EDITOR.LABEL_SOURCE_COMPONENT + " " + WYSIWYGStringsAndLabels.ERROR_MESSAGES.TEXT_MISSING + "\n");
							error = true;
						}
				} 
			}
				if (StringUtils.isNullOrEmpty(ruleName))
					errors.append("\n");
			}
			if (StringUtils.isNullOrEmpty(ruleName))
				errors.append("\n");
		}
		if (error)
			throw new NuclosBusinessException(errors.toString());
		
	}
	
}
