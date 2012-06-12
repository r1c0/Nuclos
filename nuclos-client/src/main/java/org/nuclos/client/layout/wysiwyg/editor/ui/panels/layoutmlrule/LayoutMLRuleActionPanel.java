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

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.LAYOUTML_RULE_EDITOR;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.AddRemoveRowsFromPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.AddRemoveRowsFromPanel.AddRemoveButtonControllable;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.layoutmlrule.listener.ActionAwareItemListener;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.layoutmlrule.listener.EntityRelatedTargetComponentItemListener;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.layoutmlrule.listener.ParameterForSourceComponentAwareItemListener;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.layoutmlrule.listener.SourceFieldActionAwareItemListener;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.layoutmlrule.listener.TargetComponentActionAwareItemListener;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.layoutmlrule.listener.TargetComponentAwareItemListener;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRuleAction;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRuleEventType;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.layoutml.LayoutMLConstants;

/**
 * This Class wraps a {@link LayoutMLRuleAction}.<br>
 * It contains JPanels for all {@link LayoutMLRuleAction} possible:
 * <ul>
 * <li> {@link #createActionTransferLookupValuePanel(Color)}
 * <li> {@link #createActionRefreshValuelistPanel(Color)}
 * <li> {@link #createActionClearPanel(Color)}
 * <li> {@link #createActionRefreshValuelistPanel(Color)}
 * </ul>
 * Changing the {@link LayoutMLRuleEventType} does filter the shown Actions.<br>
 * Filtering is done by {@link LayoutMLRuleValidationLayer}.<br>
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class LayoutMLRuleActionPanel extends JPanel implements AddRemoveButtonControllable {

	private static final Logger log = Logger.getLogger(LayoutMLRuleActionPanel.class);
	
	/** the action to attach to this panel */
	private LayoutMLRuleAction layoutMLRuleAction = null;
	/** the combobox for selecting the actions */
	private JComboBox actionSelector = null;

	private JCheckBox chkInvertable = null;
	/** the actionpanels */
	private JPanel actionTransferLookupValuePanel = null;
	private JPanel actionClearPanel = null;
	private JPanel actionRefreshValuelistPanel = null;

	public static int ADDREMOVEPOSITION_COLUMN = 7;

	public static int ACTIONPANELPOSITION = 5;

	private double[][] actionPanelLayout = {
		{InterfaceGuidelines.MARGIN_LEFT, 200, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.FILL, InterfaceGuidelines.MARGIN_RIGHT},
		{InterfaceGuidelines.MARGIN_TOP, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BOTTOM}	
	};

	private double[][] detailsPanelLayout = {
		{200, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.FILL},
		{TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED}
	};

	private HashMap<String, JPanel> actionPanels = new HashMap<String, JPanel>(4);

	private JPanel actualActionDetailsPanel = null;

	private WYSIWYGLayoutEditorPanel editorPanel;

	private LayoutMLRuleSingleRulePanel singleRulePanel = null;

	private HashMap<JComboBox, JComboBox> entityTargetComponent = new HashMap<JComboBox, JComboBox>(2);

	/**
	 * Constructor
	 * @param backgroundColor
	 * @param layoutMLRuleAction the {@link LayoutMLRuleAction} to attach
	 * @param actionType 
	 * @param editorPanel the {@link WYSIWYGLayoutEditorPanel}
	 * @param singleRulePanel the parent container {@link SingleRulePanel}
	 */
	public LayoutMLRuleActionPanel(Color backgroundColor, LayoutMLRuleAction layoutMLRuleAction, WYSIWYGLayoutEditorPanel editorPanel, LayoutMLRuleSingleRulePanel singleRulePanel) {
		this.layoutMLRuleAction = layoutMLRuleAction;
		this.editorPanel = editorPanel;
		this.singleRulePanel = singleRulePanel;
		this.setBackground(backgroundColor);
		this.setLayout(new TableLayout(actionPanelLayout));

		actionSelector = new JComboBox();
		actionSelector.setName("actionSelector");
		initActionSelector();
		restoreValue(actionSelector, LayoutMLRuleValidationLayer.actionType.get(layoutMLRuleAction.getRuleAction()));
		actionSelector.addItemListener(new ActionAwareItemListener(layoutMLRuleAction, this));
		
		chkInvertable = new JCheckBox();
		chkInvertable.setBackground(backgroundColor);
		initChkInvertableForAction();

		JLabel lblActionSelector = new JLabel(LAYOUTML_RULE_EDITOR.LABEL_ACTION_SELECTOR);

		TableLayoutConstraints constraint = new TableLayoutConstraints();
		constraint.col1 = 1;
		constraint.col2 = 1;
		constraint.row1 = 1;
		constraint.row2 = 1;
		constraint.hAlign = TableLayout.FULL;
		constraint.vAlign = TableLayout.CENTER;
		this.add(lblActionSelector, constraint);

		constraint = new TableLayoutConstraints();
		constraint.col1 = 3;
		constraint.col2 = 3;
		constraint.row1 = 1;
		constraint.row2 = 1;
		constraint.hAlign = TableLayout.FULL;
		constraint.vAlign = TableLayout.CENTER;
		this.add(actionSelector, constraint);

		try {
			actionTransferLookupValuePanel = createActionTransferLookupValuePanel(backgroundColor);
			actionClearPanel = createActionClearPanel(backgroundColor);
			actionRefreshValuelistPanel = createActionRefreshValuelistPanel(backgroundColor);
		} catch (NuclosBusinessException e) {
			Errors.getInstance().showExceptionDialog(this, e);
		}
		
		/**
		 * registering the action panels created before
		 */
		actionPanels.put(LayoutMLRuleAction.TRANSFER_LOOKEDUP_VALUE, actionTransferLookupValuePanel);
		actionPanels.put(LayoutMLRuleAction.REFRESH_VALUELIST, actionRefreshValuelistPanel);
		actionPanels.put(LayoutMLRuleAction.CLEAR, actionClearPanel);

		if (actionSelector.getSelectedItem() != null)
			changeActionDetailPanels(actionSelector.getSelectedItem().toString());
	}
	
	/**
	 * This Method creates a Panel for editing {@link LayoutMLConstants#ELEMENT_TRANSFERLOOKEDUPVALUE}
	 * @param backgroundColor
	 * @return the Panel with everything needed to create a valid Action
	 * @throws NuclosBusinessException 
	 */
	private JPanel createActionTransferLookupValuePanel(Color backgroundColor) throws NuclosBusinessException {
		JPanel detailsPanel = new JPanel();
		detailsPanel.setLayout(new TableLayout(this.detailsPanelLayout));
		detailsPanel.setBackground(backgroundColor);
		TableLayoutConstraints constraint = null;
		
		/**
		 * sourcefield
		 */
		JComboBox sourceField = new JComboBox();
		sourceField.setName("createActionTransferLookupValuePanel sourceField");
		constraint = new TableLayoutConstraints();
		constraint.col1 = 2;
		constraint.col2 = 2;
		constraint.row1 = 0;
		constraint.row2 = 0;
		constraint.hAlign = TableLayout.FULL;
		constraint.vAlign = TableLayout.CENTER;
		detailsPanel.add(sourceField, constraint);
		/** label */
		JLabel lblSourceField = new JLabel(LAYOUTML_RULE_EDITOR.LABEL_SOURCE_COMPONENT);
		constraint = new TableLayoutConstraints();
		constraint.col1 = 0;
		constraint.col2 = 0;
		constraint.row1 = 0;
		constraint.row2 = 0;
		constraint.hAlign = TableLayout.FULL;
		constraint.vAlign = TableLayout.CENTER;
		detailsPanel.add(lblSourceField, constraint);
		
		/**
		 * target component
		 */
		JComboBox targetComponent = new JComboBox();
		targetComponent.setName("createActionTransferLookupValuePanel targetComponent");
		constraint = new TableLayoutConstraints();
		constraint.col1 = 2;
		constraint.col2 = 2;
		constraint.row1 = 2;
		constraint.row2 = 2;
		constraint.hAlign = TableLayout.FULL;
		constraint.vAlign = TableLayout.CENTER;
		detailsPanel.add(targetComponent, constraint);
		/** label */
		JLabel lblTargetComponent = new JLabel(LAYOUTML_RULE_EDITOR.LABEL_TARGET_COMPONENT);
		constraint = new TableLayoutConstraints();
		constraint.col1 = 0;
		constraint.col2 = 0;
		constraint.row1 = 2;
		constraint.row2 = 2;
		constraint.hAlign = TableLayout.FULL;
		constraint.vAlign = TableLayout.CENTER;
		detailsPanel.add(lblTargetComponent, constraint);
		
		/**
		 * filling the components with fitting values, adding listeners and restoring
		 */
		initTargetComponentForTransferLookupValue(targetComponent);
		restoreValue(targetComponent, layoutMLRuleAction.getTargetComponent());
		targetComponent.addItemListener(new TargetComponentActionAwareItemListener(layoutMLRuleAction));
		
		initSourceFieldForTransferLookupValue(sourceField);
		restoreValue(sourceField, layoutMLRuleAction.getSourceField());	
		sourceField.addItemListener(new SourceFieldActionAwareItemListener(layoutMLRuleAction));
		
		return detailsPanel;
	}
	



	/**
	 * This Method creates a Panel for editing {@link LayoutMLConstants#ELEMENT_CLEAR}
	 * @param backgroundColor
	 * @return the Panel with everything needed to create a valid Action
	 */
	private JPanel createActionClearPanel(Color backgroundColor) throws NuclosBusinessException {
		JPanel detailsPanel = new JPanel();
		detailsPanel.setLayout(new TableLayout(this.detailsPanelLayout));
		detailsPanel.setBackground(backgroundColor);
		TableLayoutConstraints constraint = null;

	//NUCLEUSINT-411 not supported by parser, disabled until implemented in parser
//		JLabel lblEntity = new JLabel(LAYOUTML_RULE_EDITOR.LABEL_ENTITY);
//		constraint = new TableLayoutConstraints();
//		constraint.col1 = 0;
//		constraint.col2 = 0;
//		constraint.row1 = 0;
//		constraint.row2 = 0;
//		constraint.hAlign = TableLayout.FULL;
//		constraint.vAlign = TableLayout.CENTER;
//		detailsPanel.add(lblEntity, constraint);
//
//		final JComboBox entity = new JComboBox();
//		fillEntity(entity);
//		layoutMLRuleAction.setEntity(setSelectedValueForComboBox(entity, layoutMLRuleAction.getEntity()));
//		entity.addItemListener(new ItemListener(){
//			public void itemStateChanged(ItemEvent e) {
//				LayoutMLRuleActionPanel.this.entityChanged(entity);
//			}
//		});
//		constraint = new TableLayoutConstraints();
//		constraint.col1 = 2;
//		constraint.col2 = 2;
//		constraint.row1 = 0;
//		constraint.row2 = 0;
//		constraint.hAlign = TableLayout.FULL;
//		constraint.vAlign = TableLayout.CENTER;
//		detailsPanel.add(entity, constraint);
		
		/**
		 * target component
		 */
		JComboBox targetComponent = new JComboBox();
		targetComponent.setName("createActionClearPanel targetComponent");
		constraint = new TableLayoutConstraints();
		constraint.col1 = 2;
		constraint.col2 = 2;
		constraint.row1 = 2;
		constraint.row2 = 2;
		constraint.hAlign = TableLayout.FULL;
		constraint.vAlign = TableLayout.CENTER;
		detailsPanel.add(targetComponent, constraint);
		/** label */
		JLabel lblTargetComponent = new JLabel(LAYOUTML_RULE_EDITOR.LABEL_TARGET_COMPONENT);
		constraint = new TableLayoutConstraints();
		constraint.col1 = 0;
		constraint.col2 = 0;
		constraint.row1 = 2;
		constraint.row2 = 2;
		constraint.hAlign = TableLayout.FULL;
		constraint.vAlign = TableLayout.CENTER;
		detailsPanel.add(lblTargetComponent, constraint);
		
		/**
		 * filling the components with fitting values, adding listeners and restoring
		 */
		initTargetComponentForClearAction(targetComponent);
		restoreValue(targetComponent, layoutMLRuleAction.getTargetComponent());
		targetComponent.addItemListener(new TargetComponentActionAwareItemListener(layoutMLRuleAction));

		//NUCLEUSINT-411  not supported by parser, disabled until implemented in parser
//		entityTargetComponent.put(entity, targetComponent);
		return detailsPanel;
	}

	/**
	 * @param backgroundColor
	 * @return the {@link JPanel} for RefreshValueList {@link LayoutMLRuleAction}
	 */
	private JPanel createActionRefreshValuelistPanel(Color backgroundColor) throws NuclosBusinessException{
		JPanel detailsPanel = new JPanel();
		TableLayout layout = new TableLayout(this.detailsPanelLayout);
		layout.insertRow(layout.getNumRow() , InterfaceGuidelines.MARGIN_BETWEEN);
		layout.insertRow(layout.getNumRow() , TableLayout.PREFERRED);
		detailsPanel.setLayout(layout);
		detailsPanel.setBackground(backgroundColor);
		TableLayoutConstraints constraint = null; 

		/**
		 * entity
		 */
		JComboBox entity = new JComboBox();
		entity.setName("createActionRefreshValuelistPanel entity");
		constraint = new TableLayoutConstraints();
		constraint.col1 = 2;
		constraint.col2 = 2;
		constraint.row1 = 0;
		constraint.row2 = 0;
		constraint.hAlign = TableLayout.FULL;
		constraint.vAlign = TableLayout.CENTER;
		detailsPanel.add(entity, constraint);
		/** the label */
		JLabel lblEntity = new JLabel(LAYOUTML_RULE_EDITOR.LABEL_ENTITY);
		constraint = new TableLayoutConstraints();
		constraint.col1 = 0;
		constraint.col2 = 0;
		constraint.row1 = 0;
		constraint.row2 = 0;
		constraint.hAlign = TableLayout.FULL;
		constraint.vAlign = TableLayout.CENTER;
		detailsPanel.add(lblEntity, constraint);
		
		/**
		 * target component
		 */
		JComboBox targetComponent = new JComboBox();
		targetComponent.setName("createActionRefreshValuelistPanel targetComponent");
		constraint = new TableLayoutConstraints();
		constraint.col1 = 2;
		constraint.col2 = 2;
		constraint.row1 = 2;
		constraint.row2 = 2;
		constraint.hAlign = TableLayout.FULL;
		constraint.vAlign = TableLayout.CENTER;
		detailsPanel.add(targetComponent, constraint);
		/** the label */
		JLabel lblTargetComponent = new JLabel(LAYOUTML_RULE_EDITOR.LABEL_TARGET_COMPONENT);
		constraint = new TableLayoutConstraints();
		constraint.col1 = 0;
		constraint.col2 = 0;
		constraint.row1 = 2;
		constraint.row2 = 2;
		constraint.hAlign = TableLayout.FULL;
		constraint.vAlign = TableLayout.CENTER;
		detailsPanel.add(lblTargetComponent, constraint);
		
		/**
		 * parameter for source component
		 */
		JComboBox parameterForSourceComponent = new JComboBox();
		parameterForSourceComponent.setName("createActionRefreshValuelistPanel parameterForSourceComponent");
		constraint = new TableLayoutConstraints();
		constraint.col1 = 2;
		constraint.col2 = 2;
		constraint.row1 = 4;
		constraint.row2 = 4;
		constraint.hAlign = TableLayout.FULL;
		constraint.vAlign = TableLayout.CENTER;
		detailsPanel.add(parameterForSourceComponent, constraint);
		/** the label */
		JLabel lblParameterForSourceComponent = new JLabel(LAYOUTML_RULE_EDITOR.LABEL_PARAMETER_FOR_SOURCECOMPONENT);
		constraint = new TableLayoutConstraints();
		constraint.col1 = 0;
		constraint.col2 = 0;
		constraint.row1 = 4;
		constraint.row2 = 4;
		constraint.hAlign = TableLayout.FULL;
		constraint.vAlign = TableLayout.CENTER;
		detailsPanel.add(lblParameterForSourceComponent, constraint);

		/**
		 * filling the components with fitting values, adding listeners and restoring
		 */

		initEntityForRefreshValueListProvider(entity);
		boolean defaultValue = restoreValue(entity, layoutMLRuleAction.getEntity());
		entity.addItemListener(new EntityRelatedTargetComponentItemListener(layoutMLRuleAction, editorPanel, targetComponent, parameterForSourceComponent));
		
		
		initTargetComponentForRefreshValueListProvider(targetComponent, layoutMLRuleAction.getEntity());
		restoreValue(targetComponent, layoutMLRuleAction.getTargetComponent());
		TargetComponentAwareItemListener targetComponentAwaraListener = new TargetComponentAwareItemListener(parameterForSourceComponent, entity, layoutMLRuleAction, editorPanel);
		targetComponent.addItemListener(targetComponentAwaraListener);

		// A little bit hackish... but, since the complete logic is encapsulated in the TargetComponentAwareItemListener,
		// this is the only way to do it (without duplicating code or a complete refactoring).
		targetComponentAwaraListener.initParameterForSourceComponent(layoutMLRuleAction.getTargetComponent());
		restoreValue(parameterForSourceComponent, layoutMLRuleAction.getParameterForSourceComponent());
		parameterForSourceComponent.addItemListener(new ParameterForSourceComponentAwareItemListener(layoutMLRuleAction));
		
		/**
		 * 
		 */
		entityTargetComponent.put(entity, targetComponent);
		return detailsPanel;
	}
	
	/**
	 * This Method does the Setup for the Actionselector.
	 * Uses {@link LayoutMLRuleValidationLayer} for getting the Actions valid for the {@link LayoutMLRuleEventType}
	 */
	private void initActionSelector() {
		String controlType = singleRulePanel.getControlTypeOfRuleSourceComponent();
		String eventType = singleRulePanel.getLayoutMLRule().getLayoutMLRuleEventType().getEventType();
		String[] validActions = LayoutMLRuleValidationLayer.getActionsForEventType(eventType, controlType);
		if (validActions != null) {
			for (String validAction : validActions) {
				actionSelector.addItem(LayoutMLRuleValidationLayer.actionType.get(validAction));
			}
			actionSelector.addItem("");
		}
	}
	
	/**
	 * Adds a {@link ChangeListener} to the invertable {@link JCheckbox}
	 */
	private void initChkInvertableForAction() {
		chkInvertable.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				setInvertable();
			}
		});

		if (isRuleInitialized()) {
			chkInvertable.setSelected(layoutMLRuleAction.isInvertable());
		}
	}
	
	/**
	 * Inits the Sourcefield for TransferLookup
	 * @param sourceField
	 */
	private void initSourceFieldForTransferLookupValue(JComboBox sourceField) {
		List<String> availableFields = null;
		
		if (singleRulePanel.getLayoutMLRule().isSubformEntity()) {
			// its a subform
			if (singleRulePanel.getLayoutMLRule().isListOfValues()) {
				singleRulePanel.getRuleSourceComponent();
				
				String linkedEntity = editorPanel.getMetaInformation().getLinkedEntityForSubformColumn(singleRulePanel.getLayoutMLRule().getComponentEntity(), singleRulePanel.getLayoutMLRule().getComponentName());		
				availableFields = editorPanel.getMetaInformation().getSubFormColumns(linkedEntity);
			}
		} else {
			// is linked attribute
			if (singleRulePanel.getLayoutMLRule().isListOfValues()) {
				// get linked entity
				String linkedEntity = editorPanel.getMetaInformation().getLinkedEntityForAttribute(singleRulePanel.getLayoutMLRule().getComponentEntity(), singleRulePanel.getLayoutMLRule().getComponentName());
				availableFields = editorPanel.getMetaInformation().getDependingAttributes(linkedEntity);
			}
		}
		if (availableFields != null) {
			for (String field : availableFields) {
				sourceField.addItem(field);
			}
		}
		sourceField.addItem("");
	}
	
	/**
	 * Inits the targetComponent for TransferLookupValue
	 * @param targetComponent
	 */
	private void initTargetComponentForTransferLookupValue(JComboBox targetComponent) {
		String[] values = null;
		if(singleRulePanel.getLayoutMLRule().isSubformEntity()) {
			// its a subform
			values = LayoutMLRuleValidationLayer.getAllSubformColumns(singleRulePanel.getRuleSourceComponent());
		} else {
			// its no subform
			values = LayoutMLRuleValidationLayer.getAllCollectableComponents(editorPanel);
		}

		if(values != null) {
			for(String components : values) {
				targetComponent.addItem(components);
			}
			targetComponent.addItem("");
		} 
	}
	
	/**
	 * Inits the entity combobox for RefreshValueListProvider
	 * @param entity
	 */
	private void initEntityForRefreshValueListProvider(JComboBox entity) {
		String[] subformEntitys = LayoutMLRuleValidationLayer.getAllSubform(this.editorPanel);
		if (subformEntitys != null) {
			for(String subform : subformEntitys) {
				entity.addItem(subform);
			}
			// adding "" for deselecting entity, only makes sense if there are entities... 
			entity.addItem("");

		} else {
			// no subforms avaible, no need for the entity combobox
			entity.setEnabled(false);
		}
	}
	
	/**
	 * Inits the targetComponent Combobox for RefreshValueListProvider
	 * @param targetComponent
	 */
	private void initTargetComponentForRefreshValueListProvider(JComboBox targetComponent, String entity) {
		String[] collectableComponentsWithVP = null;
		if (entity != null && !entity.isEmpty())
			collectableComponentsWithVP = LayoutMLRuleValidationLayer.getSubformColumnsWithValueListProvider(editorPanel, entity);
		else
			collectableComponentsWithVP = LayoutMLRuleValidationLayer.getCollectableComponentsWithValueListProvider(editorPanel);
		
		if(collectableComponentsWithVP != null) {
			for(String collComp : collectableComponentsWithVP) {
				targetComponent.addItem(collComp);
			}
			targetComponent.setEnabled(true);
		}
		else {
			// there are no components with vp, disabling the combobox, no entries could be made
			targetComponent.setEnabled(false);
		}
		
		targetComponent.addItem("");
	}

	/**
	 * Inits the targetComponent for Clear
	 * @param targetComponent
	 */
	private void initTargetComponentForClearAction(JComboBox targetComponent) {
		String[] components;
		if(singleRulePanel.getLayoutMLRule().isSubformEntity()) {
			// its a subform
			components = LayoutMLRuleValidationLayer.getAllSubformColumns(singleRulePanel.getRuleSourceComponent());
		} else {
			// its no subform
			components = LayoutMLRuleValidationLayer.getAllCollectableComponents(editorPanel);
		}
		if (components != null) {
			for (String component : components) {
				targetComponent.addItem(component);
			}
		}
		targetComponent.addItem("");
	}

	/**
	 * Sets the Attribute {@link LayoutMLConstants#ATTRIBUTE_INVERTABLE}
	 */
	private void setInvertable() {
		LayoutMLRuleActionPanel.this.layoutMLRuleAction.setInvertable(chkInvertable.isSelected());
	}

	/**
	 * 
	 * @param target
	 * @param value
	 * @return true if default value was selected, if restored false is returned
	 */
	private boolean restoreValue(JComboBox target, String value) {
		if (!StringUtils.isNullOrEmpty(value)) {
			// something to restore
			if (((DefaultComboBoxModel)target.getModel()).getIndexOf(value) != -1) {
				target.setSelectedItem(value);
				log.debug("Restored " + target.getName() + " " + value);
			} else {
				target.setSelectedItem("");
				log.debug("Defaulting " + target.getName() + " " + value);
			}
			return false;
		} else {
			// nothing to restore, defaulting to ""
			target.setSelectedItem("");
			log.debug("Defaulting " + target.getName());
			return true;
		}
	}
	
	/**
	 * @return true if there is minimum one {@link LayoutMLRuleAction} contained
	 */
	private boolean isRuleInitialized() {
		if (this.layoutMLRuleAction.getRuleAction() != null)
			return (this.layoutMLRuleAction.getRuleAction().length() > 0);
		
		return false;
	}
	
	/**
	 * This Method does filter the {@link LayoutMLRuleAction} Types by:
	 * <ul>
	 * <li> Component Type (CollectableComponent <b>or</b> Subform)</li>
	 * <li> The Event selected in the {@link LayoutMLRuleEventType}</li>
	 * </ul>
	 */
	public void refreshActionSelectorForEvent() {
		String event = this.singleRulePanel.getLayoutMLRule().getLayoutMLRuleEventType().getEventType();
		
		if (event != null) {
			// there is a event
			this.changeActionDetailPanels(event);
		}
	}
	
	/**
	 * This Method changes the Panels for each Action invoked by {@link #actionSelector}
	 * 
	 * @see #createActionClearPanel(Color)
	 * @see #createActionEnablePanel(Color)
	 * @see #createActionRefreshValuelistPanel(Color)
	 * @see #createActionTransferLookupValuePanel(Color)
	 * 
	 * @param itemValue
	 */
	public void changeActionDetailPanels(String itemValue) {
		if ("".equals(itemValue)) {
			if (this.actualActionDetailsPanel != null) {
				this.remove(actualActionDetailsPanel);
				actualActionDetailsPanel = null;
				this.revalidate();
			}
		} else {
			for (Entry<String, String> entry : LayoutMLRuleValidationLayer.actionType.entrySet()) {
				if (entry.getValue().equals(itemValue)) {
					JPanel newPanel = actionPanels.get(entry.getKey());
					if (this.actualActionDetailsPanel != null) {
						this.remove(actualActionDetailsPanel);
					}
					actualActionDetailsPanel = newPanel;
					TableLayoutConstraints constraint = new TableLayoutConstraints();
					constraint.col1 = 1;
					constraint.col2 = 3;
					constraint.row1 = 3;
					constraint.row2 = 3;
					constraint.hAlign = TableLayout.FULL;
					constraint.vAlign = TableLayout.CENTER;
					this.add(actualActionDetailsPanel, constraint);
					this.updateUI();
				}
			}
		}
	}


	/**
	 * @return the {@link LayoutMLRuleAction} wrapped by this {@link LayoutMLRuleActionPanel}
	 */
	public LayoutMLRuleAction getLayoutMLRuleAction() {
		return layoutMLRuleAction;
	}

	/**
	 * @return the {@link AddRemoveRowsFromPanel} for enabling/ disabling the Buttons to add/ remove
	 */
	public AddRemoveRowsFromPanel getAddRemoveRowsFromPanel() {
		Component[] comps = this.getComponents();

		AddRemoveRowsFromPanel temp = null;
		for (int i = 0; i < comps.length; i++) {
			if (comps[i] instanceof AddRemoveRowsFromPanel)
				temp = (AddRemoveRowsFromPanel) comps[i];
		}

		return temp;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.AddRemoveRowsFromPanel.AddRemoveButtonControllable#performAddAction()
	 */
	@Override
	public void performAddAction() {
		singleRulePanel.addAnotherAction();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.AddRemoveRowsFromPanel.AddRemoveButtonControllable#performRemoveAction()
	 */
	@Override
	public void performRemoveAction() {
		singleRulePanel.removeActionFromPanel(this);
	}

}
