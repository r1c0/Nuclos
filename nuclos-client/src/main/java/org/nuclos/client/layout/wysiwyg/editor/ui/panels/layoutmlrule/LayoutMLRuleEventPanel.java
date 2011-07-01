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
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import org.nuclos.common2.StringUtils;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.LAYOUTML_RULE_EDITOR;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.layoutmlrule.listener.EventAwareItemListener;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRuleEventType;

/**
 * This class wraps a {@link LayoutMLRuleEventType} in a JPanel to make it visual editable.<br>
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
public class LayoutMLRuleEventPanel extends JPanel {
	private LayoutMLRuleEventType layoutMLRuleEventType = null;
	private JComboBox eventSelector = null;
		
	private LayoutMLRuleEditorDialog ruleEditorDialog;
	
	/** the parent component */
	private LayoutMLRuleSingleRulePanel singleRulePanel = null;
	
	private static final Logger log = Logger.getLogger(LayoutMLRuleEventPanel.class);
	
	/** the Layout for this Panel */
	private double[][] defaultLayout = new double[][] {
		{ InterfaceGuidelines.MARGIN_LEFT, 200, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.FILL, InterfaceGuidelines.MARGIN_RIGHT},
		{ InterfaceGuidelines.MARGIN_TOP, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BOTTOM}
	};

	/**
	 * The default Constructor
	 * 
	 * @param backgroundColor due the alternating Color it is handed over
	 * @param layoutMLRuleEventType the {@link LayoutMLRuleEventType} to edit
	 * @param eventType  
	 * @param ruleSourceComponent the {@link WYSIWYGComponent} the Rule is linked to
	 * @param ruleEditorDialog the {@link LayoutMLRuleEditorDialog}
	 * @param singleRulePanel the parent {@link SingleRulePanel}
	 */
	public LayoutMLRuleEventPanel(Color backgroundColor, LayoutMLRuleEventType layoutMLRuleEventType, LayoutMLRuleEditorDialog ruleEditorDialog, LayoutMLRuleSingleRulePanel singleRulePanel){
		this.layoutMLRuleEventType = layoutMLRuleEventType;
		this.singleRulePanel = singleRulePanel;
		
		this.ruleEditorDialog = ruleEditorDialog;
		this.setBackground(backgroundColor);
		this.setLayout(new TableLayout(defaultLayout));
		this.setBorder(BorderFactory.createTitledBorder(LAYOUTML_RULE_EDITOR.LABEL_TITLE_EVENT_PANEL));
		String sourceComponent = singleRulePanel.getLayoutMLRule().getLayoutMLRuleEventType().getSourceComponent();
		if (StringUtils.isNullOrEmpty(sourceComponent)) {
			if (singleRulePanel.getLayoutMLRule().isSubformEntity()) {
				this.layoutMLRuleEventType.setSourceComponent(singleRulePanel.getLayoutMLRule().getComponentName());
				this.layoutMLRuleEventType.setEntity(singleRulePanel.getLayoutMLRule().getComponentEntity());
			} else
				this.layoutMLRuleEventType.setSourceComponent(singleRulePanel.getLayoutMLRule().getComponentName());
		} 
		/**                                                                                                                 
		 * the event combobox and the event panel
		 */

		initEventSelector();
		restoreEventIfThereIsOne();
		eventSelector.addItemListener(new EventAwareItemListener(this.layoutMLRuleEventType, this));
		
		JLabel lblEventSelector = new JLabel(LAYOUTML_RULE_EDITOR.LABEL_EVENT_TRIGGERING_RULE);

		TableLayoutConstraints constraint = new TableLayoutConstraints();
		constraint.col1 = 1;
		constraint.col2 = 1;
		constraint.row1 = 1;
		constraint.row2 = 1;
		constraint.hAlign = TableLayout.FULL;
		constraint.vAlign = TableLayout.CENTER;
		this.add(lblEventSelector, constraint);

		constraint = new TableLayoutConstraints();
		constraint.col1 = 3;
		constraint.col2 = 3;
		constraint.row1 = 1;
		constraint.row2 = 1;
		constraint.hAlign = TableLayout.FULL;
		constraint.vAlign = TableLayout.CENTER;
		this.add(eventSelector, constraint);
	}
	
	private void initEventSelector() {
		eventSelector = new JComboBox();
		String[] validEvents = LayoutMLRuleValidationLayer.getEventTypesForComponents(
			ruleEditorDialog.getWYSIWYGLayoutEditorPanel().getMetaInformation(),
			singleRulePanel.getLayoutMLRule(), ruleEditorDialog.getControlTypeForSourceComponent());
		for (String key : validEvents) {
			eventSelector.addItem(LayoutMLRuleValidationLayer.eventType.get(key));
		}
		eventSelector.addItem("");
	}
	
	public void refreshActionsOnEventChange() {
		List<LayoutMLRuleActionPanel> actionPanels = this.singleRulePanel.getActionPanelsForSingleRulePanel();
		for (LayoutMLRuleActionPanel singleActionPanel : actionPanels) {
			singleActionPanel.refreshActionSelectorForEvent();
		}
	}
	
	private void restoreEventIfThereIsOne() {
		if (layoutMLRuleEventType.getEventType() != null) {
			// there is something to restore
			String existingEventType = layoutMLRuleEventType.getEventType();
			eventSelector.setSelectedItem(LayoutMLRuleValidationLayer.eventType.get(existingEventType));
			log.debug("Restoring event " + existingEventType);
		} else {
			eventSelector.setSelectedItem("");
		}
	}
	
	/**
	 * 
	 * @return the {@link LayoutMLRuleSingleRulePanel}
	 */
	public LayoutMLRuleSingleRulePanel getSingleRulePanel() {
		return this.singleRulePanel;
	}
	
	/**
	 * @return the {@link LayoutMLRuleEventType} Object linked to this {@link LayoutMLRuleEventPanel}
	 */
	public LayoutMLRuleEventType getLayoutMLRuleEventType() {
		return layoutMLRuleEventType;
	}

	/**
	 * 
	 */
	public void addAnotherAction() {
		singleRulePanel.addAnotherAction();
	}
	
	/**
	 * 
	 */
	public synchronized void removeAllActions() {
		List<LayoutMLRuleActionPanel> actionPanels = new ArrayList<LayoutMLRuleActionPanel>(singleRulePanel.getActionPanelsForSingleRulePanel());
		for(LayoutMLRuleActionPanel actionPanel : actionPanels) {
			singleRulePanel.removeActionFromPanel(actionPanel);
		}
		singleRulePanel.revalidate();
	}
}
