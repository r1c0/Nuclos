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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.nuclos.client.layout.wysiwyg.WYSIWYGLayoutControllingPanel;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.LAYOUTML_RULE_EDITOR;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableCheckBox;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableComboBox;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableListOfValues;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableOptionGroup;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableTextArea;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableTextfield;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSubFormColumn;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.LayoutMLRuleController;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRule;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRules;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.layoutml.LayoutMLConstants;

/**
 * This is the Main Editor for {@link LayoutMLRules}.<br>
 * It contains:
 * <ul>
 *	<li> one or more {@link SingleRulePanel}s
 * 		<ul>
 * 			<li> with one {@link LayoutMLRuleEventPanel}</li>
 * 			<li> with one or more {@link LayoutMLRuleActionPanel}</li>
 * 		</ul>
 * 	</li>
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
public class LayoutMLRuleEditorDialog extends JPanel implements SaveAndCancelButtonPanelControllable{

	int width = 600;
	int height = 500;

	WYSIWYGComponent ruleSourceComponent = null;

	/** the scrollpane for scrolling the rules */
	private JScrollPane scrollpane = null;

	private JPanel ruleContainer = null;

	/** every rule is stored here */
	private Vector<LayoutMLRuleSingleRulePanel> rulePanels = new Vector<LayoutMLRuleSingleRulePanel>();

	private WYSIWYGLayoutEditorPanel editorPanel;
	
	public static int EXIT_CANCEL = -1;
	public static  int EXIT_SAVE = 0;
	
	private int exitStatus = EXIT_SAVE;
	
	private String controlType = null;
	
	private final MainFrameTab tab;
 
	public LayoutMLRuleEditorDialog(WYSIWYGComponent ruleSourceComponent, WYSIWYGLayoutEditorPanel editorPanel) {
		this.ruleSourceComponent = ruleSourceComponent;
		this.editorPanel = editorPanel;
		
		tab = new MainFrameTab();
		
		ImageIcon controllerIcon = getController(editorPanel).getLayoutCollectController().getFrame().getTabIcon();
    	if(controllerIcon != null){
    		tab.setTabIcon(controllerIcon);
    	}
	   	
		new LayoutMLRuleValidationLayer();
		
		/** 
		 * Find out what kind of Component is the Source of the Rule
		 */
		controlType = getControlTypeForComponent(ruleSourceComponent);
		
		/**
		 * Getting the Component Name to display
		 */
		String componentName = getComponentNameForDisplay(ruleSourceComponent);
		JLabel lblRuleSourceComponent = new JLabel(LAYOUTML_RULE_EDITOR.LABEL_RULE_SOURCECOMPONENT);

		JTextField txtRuleSourceComponent = new JTextField(componentName);
		txtRuleSourceComponent.setPreferredSize(new Dimension(300, 20));
		txtRuleSourceComponent.setEnabled(false);

		ruleContainer = new JPanel();

		double[][] containerLayout = {{TableLayout.FILL}, {TableLayout.PREFERRED}};
		ruleContainer.setLayout(new TableLayout(containerLayout));
		scrollpane = new JScrollPane(ruleContainer);
		scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollpane.getVerticalScrollBar().setUnitIncrement(20);

		
		/**
		 * Setting up the "Core Editor Window"
		 */
		double[][] mainWindowLayout = {{InterfaceGuidelines.MARGIN_LEFT, TableLayout.FILL, InterfaceGuidelines.MARGIN_RIGHT}, {TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED}};
		setLayout(new TableLayout(mainWindowLayout));

		JPanel jpnSourceComponent = new JPanel();
		double[][] titleLayout = {{TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED}, {InterfaceGuidelines.MARGIN_TOP, TableLayout.FILL, InterfaceGuidelines.MARGIN_BOTTOM}};
		jpnSourceComponent.setLayout(new TableLayout(titleLayout));

		jpnSourceComponent.add(lblRuleSourceComponent, "0,1");
		jpnSourceComponent.add(txtRuleSourceComponent, "2,1");

		add(jpnSourceComponent, "1,0");

		add(scrollpane, "1,1");

		/**
		 * Restoring Rules if there are any, otherwise start with empty
		 */
		restoreRules(controlType);
		
		/**
		 * Creating the default Buttons for Adding/ Saving and Removing all Rules
		 */
		JButton clearRulesForComponent = new JButton(LAYOUTML_RULE_EDITOR.BUTTON_REMOVE_ALL_RULES_FOR_COMPONENT);
		clearRulesForComponent.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clearAllRulesForThisComponent();
			}
		});

		ArrayList<AbstractButton> additionalButtons = new ArrayList<AbstractButton>(1);
		additionalButtons.add(clearRulesForComponent);
		add(new SaveAndCancelButtonPanel(this.getBackground(), this, additionalButtons), new TableLayoutConstraints(0,2,2,2));
				
		tab.setLayeredComponent(this);
		
		/**
		try {
			//NUCLEUSINT-354
			result.setLocationRelativeTo(this.editorPanel.getController().getLayoutCollectController().getFrame());
		} catch (NuclosBusinessException e) {}
		
		//NUCLEUSINT-354
		result.setBounds(result.getBounds().x - (width/2), result.getBounds().y- (height/2), width, height);
		
		result.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		*/
		
		getController(editorPanel).getLayoutCollectController().getFrame().add(tab);
		tab.setTitle(LAYOUTML_RULE_EDITOR.TITLE_LAYOUTML_RULE_EDITOR);
	}
	
	protected String getControlTypeForSourceComponent() {
		return this.controlType;
	}
	
	/**
	 * 
	 * @return the {@link JPanel} with all the {@link LayoutMLRuleSingleRulePanel} contained
	 */
	protected JPanel getRuleContainer() {
		return this.ruleContainer;
	}
	
	/**
	 * 
	 * @return the {@link WYSIWYGLayoutEditorPanel} used for finding Components of particular types
	 */
	protected WYSIWYGLayoutEditorPanel getWYSIWYGLayoutEditorPanel() {
		return this.editorPanel;
	}
	
	/**
	 * @return The {@link JScrollPane} for adjusting the Viewport
	 */
	protected JScrollPane getScrollPane() {
		return this.scrollpane;
	}
	
	/**
	 * This method does the setup of the {@link LayoutMLRuleEditorDialog}.
	 * It restores already existing rules or creates a empty panel if there are none
	 * @param controlType
	 */
	private void restoreRules(String controlType) {
		if (ruleSourceComponent.getLayoutMLRulesIfCapable().getSize() > 0) {
			/** there are rules, starting reconstruction... */
			expandLayout(ruleContainer.getLayout(), ruleSourceComponent.getLayoutMLRulesIfCapable().getSize(), false);
			int row = 0;
			for (LayoutMLRule singleRule : ruleSourceComponent.getLayoutMLRulesIfCapable().getRules()) {
				LayoutMLRuleSingleRulePanel restoredRulePanel;
				try {
					restoredRulePanel = new LayoutMLRuleSingleRulePanel(this, (LayoutMLRule)singleRule.clone(), controlType);
					ruleContainer.add(restoredRulePanel, "0," + row);
				} catch (CloneNotSupportedException e1) { }
				row = row + 2;
			}
		} else {
			/** no rules defined for component, just starting with a new panel */
			LayoutMLRuleSingleRulePanel initialPanel = new LayoutMLRuleSingleRulePanel(this, controlType);
			initialPanel.addRemoveRowsFromPanel.disableDeleteButton();
			ruleContainer.add(initialPanel, "0,0");
		}
	}
	
	/**
	 * This Method gets the Controltype for the {@link WYSIWYGComponent}
	 * 
	 * Its needed for Selecting the Fitting Combinations of Rules...
	 * @param ruleSourceComponent
	 * @return The String representation for the Component, e.g. {@link LayoutMLConstants.ATTRIBUTEVALUE_TEXTFIELD} or {@link LayoutMLConstants.ELEMENT_SUBFORMCOLUMN} 
	 */
	private String getControlTypeForComponent(WYSIWYGComponent ruleSourceComponent) {
		String controlType = null;
		if (ruleSourceComponent instanceof WYSIWYGCollectableTextfield)
			controlType = LayoutMLConstants.ATTRIBUTEVALUE_TEXTFIELD;
		else if (ruleSourceComponent instanceof WYSIWYGCollectableTextArea)
			controlType = LayoutMLConstants.ATTRIBUTEVALUE_TEXTAREA;
		else if (ruleSourceComponent instanceof WYSIWYGCollectableComboBox)
			controlType = LayoutMLConstants.ATTRIBUTEVALUE_COMBOBOX;
		else if (ruleSourceComponent instanceof WYSIWYGCollectableOptionGroup)
			controlType = LayoutMLConstants.ATTRIBUTEVALUE_OPTIONGROUP;
		else if (ruleSourceComponent instanceof WYSIWYGCollectableCheckBox)
			controlType = LayoutMLConstants.ATTRIBUTEVALUE_CHECKBOX;
		else if (ruleSourceComponent instanceof WYSIWYGSubFormColumn)
			controlType = LayoutMLConstants.ELEMENT_SUBFORMCOLUMN;
		//NUCLEUSINT-341
		else if (ruleSourceComponent instanceof WYSIWYGCollectableListOfValues)
			controlType = LayoutMLConstants.ATTRIBUTEVALUE_LISTOFVALUES;
			
		return controlType;
	}
	
	/**
	 * This Method returns the Name of the Component, if its a {@link WYSIWYGSubFormColumn} its Entityname.SubformColumn
	 * @param ruleSourceComponent
	 * @return
	 */
	private String getComponentNameForDisplay(WYSIWYGComponent ruleSourceComponent) {
		String componentName = null;
		
		if (!(ruleSourceComponent instanceof WYSIWYGSubFormColumn)){
			componentName = ((Component) ruleSourceComponent).getName();
		} else {
			String subformEntity = ((WYSIWYGSubFormColumn)ruleSourceComponent).getSubForm().getEntityName();
			String subformColumn = ((WYSIWYGSubFormColumn)ruleSourceComponent).getName();
			
			componentName = subformEntity + "." + subformColumn;			
		}
		return componentName;
	}
	
	/**
	 * 
	 * @return the {@link Vector} containing all {@link LayoutMLRuleSingleRulePanel}
	 */
	protected Vector<LayoutMLRuleSingleRulePanel> getRulePanels() {
		return this.rulePanels;
	}
	
	/**
	 * NUCLEUSINT-341
	 * @return the {@link WYSIWYGComponent} the Rule(s) are attached to
	 */
	public WYSIWYGComponent getRuleSourceComponent() {
		return ruleSourceComponent;
	}

	private void expandLayout(LayoutManager layout, int rowsToAdd, boolean hasMarginBottom) {
		TableLayout tableLayout = (TableLayout) layout;
		double[] rows = tableLayout.getRow();
		double[] newRows = new double[rows.length + (rowsToAdd * 2)];

		for (int i = 0; i < rows.length; i++) {
			newRows[i] = rows[i];
		}

		int startingValue = rows.length;
		if (hasMarginBottom)
			startingValue--;

		for (int i = startingValue; i < newRows.length - 1; i = i + 2) {
			newRows[i] = InterfaceGuidelines.MARGIN_BETWEEN;
			newRows[i + 1] = TableLayout.PREFERRED;
		}

		if (hasMarginBottom)
			newRows[newRows.length - 1] = rows[rows.length - 1];

		tableLayout.setRow(newRows);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable#performCancelAction()
	 */
	@Override
	public void performCancelAction() {
		/** no action to perform, everything is irrelevant */
		tab.dispose();
		this.exitStatus = EXIT_CANCEL;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable#performSaveAction()
	 */
	@Override
	public void performSaveAction() {
		Component[] rules = ruleContainer.getComponents();
		LayoutMLRules rulesToAdd = new LayoutMLRules();
		for (int i = 0; i < rules.length; i++) {
			LayoutMLRule singleRule = ((LayoutMLRuleSingleRulePanel) rules[i]).getLayoutMLRule();
			rulesToAdd.addRule(singleRule);
		}
		
		try {
			LayoutMLRuleValidationLayer.validateLayoutMLRules(rulesToAdd);
			LayoutMLRuleController.attachRulesToComponent(ruleSourceComponent, rulesToAdd);
			tab.dispose();
		}
		catch(NuclosBusinessException e) {
			Errors.getInstance().showExceptionDialog(tab, new NuclosBusinessException(e.getMessage()));
		}
	}

	/**
	 * Removing all Rules for the {@link #ruleSourceComponent}
	 */
	protected void clearAllRulesForThisComponent() {
		int value = JOptionPane.showConfirmDialog(tab, LAYOUTML_RULE_EDITOR.MESSAGE_DIALOG_SURE_TO_DELETE_ALL_RULES, LAYOUTML_RULE_EDITOR.TITLE_DIALOG_SURE_TO_DELETE_ALL_RULES, JOptionPane.YES_NO_OPTION);

		if (value == JOptionPane.YES_OPTION) {
			LayoutMLRuleController.clearLayoutMLRulesForComponent(ruleSourceComponent);
			tab.dispose();
		}
	}

	/**
	 * @return the Status the Editor was closed with is either {@link LayoutMLRuleEditorDialog#EXIT_SAVE} (default) or {@link LayoutMLRuleEditorDialog#EXIT_CANCEL}
	 */
	public int getExitStatus() {
		return this.exitStatus;
	}
	
	private static WYSIWYGLayoutControllingPanel getController(WYSIWYGLayoutEditorPanel editorPanel) {
		try {
			return editorPanel.getController();
		}
		catch(NuclosBusinessException e) {
			throw new NuclosFatalException(e);
		}
	}

}
