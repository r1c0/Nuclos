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
import java.awt.LayoutManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.LAYOUTML_RULE_EDITOR;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSubFormColumn;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.AddRemoveRowsFromPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.AddRemoveRowsFromPanel.AddRemoveButtonControllable;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRule;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRuleAction;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRules;
import org.nuclos.common2.layoutml.LayoutMLConstants;

/**
 * Class returning a visual set for editing a rule.<br>
 * Contains:
 * <ul>
 * <li> one {@link LayoutMLRuleEventPanel}</li>
 * <li> one or more {@link LayoutMLRuleActionPanel}</li>
 * </ul>
 * 
 * @author hartmut.beckschulze
 */
public class LayoutMLRuleSingleRulePanel extends JPanel implements AddRemoveButtonControllable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** the layoutdefinition of the editor */
	double[][] defaultLayout = {{InterfaceGuidelines.MARGIN_LEFT, 50, TableLayout.FILL, InterfaceGuidelines.MARGIN_BETWEEN, 50, TableLayout.FILL, InterfaceGuidelines.MARGIN_RIGHT}, {InterfaceGuidelines.MARGIN_TOP, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BOTTOM}};
	private int ruleCounter = 0;
	
	private LayoutMLRuleEditorDialog editor = null;
	
		private JPanel actions = null;
		private JPanel eventPanel = null;
		@SuppressWarnings("unused")
		private JPanel conditionPanel = null;
		private LayoutMLRule layoutMLRule = null;
		public AddRemoveRowsFromPanel addRemoveRowsFromPanel;
		
		private List<LayoutMLRuleActionPanel> actionPanelsForSingleRulePanel = new ArrayList<LayoutMLRuleActionPanel>(1);

		private Color backgroundColor = this.getBackground();
		
		/** this is needed for getting values from the metainformation */
		private String controlTypeOfRuleSourceComponent = null;
			
		/**
		 * Constructor
		 * @param controlType used for getting Values from the {@link WYSIWYGMetaInformation}
		 */
		public LayoutMLRuleSingleRulePanel(LayoutMLRuleEditorDialog editor, String controlType) {
			this(editor, new LayoutMLRule(), controlType);
		}

		/**
		 * Constructor
		 * @param layoutMLRule the {@link LayoutMLRule} to be attached to this {@link SingleRulePanel}
		 * @param controlType used for getting Values from the {@link WYSIWYGMetaInformation}
		 */
		public LayoutMLRuleSingleRulePanel(LayoutMLRuleEditorDialog editor, LayoutMLRule layoutMLRule, String controlType) {
			this.editor = editor;
			this.controlTypeOfRuleSourceComponent = controlType;
			this.layoutMLRule = layoutMLRule;
			this.layoutMLRule.setComponentNameAndEntity(editor.getRuleSourceComponent());
			
			/**
			 * the actions performed for a event (may be more than one)
			 */
			actions = new JPanel();
			actions.setBorder(BorderFactory.createTitledBorder(LAYOUTML_RULE_EDITOR.LABEL_TITLE_ACTION_PANEL));
			double[][] actionslayout = {{TableLayout.FILL}, {TableLayout.FILL}};
			actions.setLayout(new TableLayout(actionslayout));
			actions.setBackground(backgroundColor);
			
			if (LayoutMLConstants.ATTRIBUTEVALUE_LISTOFVALUES.equals(controlType)) {
				this.layoutMLRule.setListOfValues();
			} else if (LayoutMLConstants.CONTROLTYPE_COMBOBOX.equals(controlType)) {
				WYSIWYGMetaInformation meta = editor.getWYSIWYGLayoutEditorPanel().getMetaInformation();
				String linkedEntity = meta.getLinkedEntityForAttribute(getLayoutMLRule().getComponentEntity(), getLayoutMLRule().getComponentName());
				if (linkedEntity != null) {
					this.layoutMLRule.setListOfValues();
				}
			} else if (LayoutMLConstants.ELEMENT_SUBFORMCOLUMN.equals(controlType)) {
				WYSIWYGSubFormColumn column = (WYSIWYGSubFormColumn) editor.getRuleSourceComponent();
				if (column.isLOVorCombobox())
					this.layoutMLRule.setListOfValues();
			}
			
			this.setLayout(new TableLayout(defaultLayout));
			editor.getRulePanels().add(this);
			ruleCounter++;

			if (ruleCounter % 2 == 0) {
				backgroundColor = Color.LIGHT_GRAY;
			}

			if (layoutMLRule.getRuleName().length() > 0) {
				changeTitleForPanel(layoutMLRule.getRuleName());
			} else {
				changeTitleForPanel("Regel " + ruleCounter);
			}

			this.setBackground(backgroundColor);
			expandLayout(this.getLayout(), 2, true);

			TableLayoutConstraints constraint = null;

			/**
			 * event panel (just one time)
			 */
			eventPanel = new LayoutMLRuleEventPanel(backgroundColor, layoutMLRule.getLayoutMLRuleEventType(), editor, this);

			constraint = new TableLayoutConstraints();
			constraint.col1 = 1;
			constraint.col2 = 5;
			constraint.row1 = 1;
			constraint.row2 = 1;
			constraint.hAlign = TableLayout.FULL;
			constraint.vAlign = TableLayout.FULL;
			this.add(eventPanel, constraint);

			for (LayoutMLRuleAction singleAction : layoutMLRule.getLayoutMLRuleActions().getSingleActions()) {
				restoreAction(singleAction);
			}
	 
			constraint = new TableLayoutConstraints();
			constraint.col1 = 1;
			constraint.col2 = 5;
			constraint.row1 = 3;
			constraint.row2 = 3;
			constraint.hAlign = TableLayout.FULL;
			constraint.vAlign = TableLayout.FULL;
			this.add(actions, constraint);

			/**
			 * the conditions panel (conditions have not been seen yet)
			 */
			conditionPanel = new LayoutMLRuleConditionPanel(backgroundColor, layoutMLRule.getLayoutMLRuleCondition());

			this.addRemoveRowsFromPanel = new AddRemoveRowsFromPanel(backgroundColor, this, AddRemoveRowsFromPanel.HORIZONTAL);
			constraint = new TableLayoutConstraints();
			constraint.col1 = 5;
			constraint.col2 = 5;
			constraint.row1 = 5;
			constraint.row2 = 5;
			constraint.hAlign = TableLayout.RIGHT;
			constraint.vAlign = TableLayout.CENTER;
			this.add(addRemoveRowsFromPanel, constraint);

			/** double click to change the name of the rule */
			this.addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						if (e.getPoint().y < 20) {
							String formerTitle = ((TitledBorder) LayoutMLRuleSingleRulePanel.this.getBorder()).getTitle();
							String strRulename = JOptionPane.showInputDialog(LAYOUTML_RULE_EDITOR.INPUT_DIALOG_TITLE_CHANGE_RULENAME, formerTitle);
							if (strRulename != null) {
								if (strRulename.length() > 0) {
									changeTitleForPanel(strRulename);
								}
							}
						}
					}
				}

				@Override
				public void mouseEntered(MouseEvent e) {
				}

				@Override
				public void mouseExited(MouseEvent e) {
				}

				@Override
				public void mousePressed(MouseEvent e) {
				}

				@Override
				public void mouseReleased(MouseEvent e) {
				}
			});
		}
		

		private void expandLayout(LayoutManager layout, boolean hasMarginBottom) {
			expandLayout(layout, 1, hasMarginBottom);
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
		
		/**
	 	 * NUCLEUSINT-341
		 * @return the {@link WYSIWYGComponent} which contains the {@link LayoutMLRules}
		 */
		public WYSIWYGComponent getRuleSourceComponent(){
			return editor.getRuleSourceComponent();
		}
		
		/**
		 * @param title this sets the Name of the {@link LayoutMLRule}
		 */
		private final void changeTitleForPanel(String title) {
			this.setBorder(BorderFactory.createTitledBorder(title));
			layoutMLRule.setRuleName(title);
		}

		/**
		 * @return the {@link LayoutMLRule} attached to this {@link SingleRulePanel}
		 */
		public LayoutMLRule getLayoutMLRule() {
			return this.layoutMLRule;
		}

		/**
		 * This Method restores a already set {@link LayoutMLRuleAction}
		 * @param layoutMLRuleAction
		 */
		public void restoreAction(LayoutMLRuleAction layoutMLRuleAction) {
			addAction(layoutMLRuleAction);
		}

		/**
		 * Add a new {@link LayoutMLRuleAction} 
		 */
		public void addAnotherAction() {
			LayoutMLRuleAction action = new LayoutMLRuleAction();
			if (this.layoutMLRule.isSubformEntity())
				action.setEntity(this.layoutMLRule.getLayoutMLRuleEventType().getEntity());
			layoutMLRule.getLayoutMLRuleActions().addAction(action);
			addAction(action);
		}

		/**
		 * This Method adds a Action to this {@link SingleRulePanel}
		 * @param layoutMLRuleAction the {@link LayoutMLRuleAction} to be added
		 */
		public void addAction(LayoutMLRuleAction layoutMLRuleAction) {
			expandLayout(actions.getLayout(), false);

			Component[] actionPanels = actions.getComponents();
			for (int i = 0; i < actionPanels.length; i++) {
				((LayoutMLRuleActionPanel) actionPanels[i]).getAddRemoveRowsFromPanel().disableAddButton();
			}

			int lastRow = ((TableLayout) actions.getLayout()).getNumRow();
			LayoutMLRuleActionPanel newActionPanel = new LayoutMLRuleActionPanel(backgroundColor, layoutMLRuleAction, editor.getWYSIWYGLayoutEditorPanel(), this);
			actionPanelsForSingleRulePanel.add(newActionPanel);

			if (!layoutMLRule.getLayoutMLRuleActions().doesContainAction(layoutMLRuleAction)) {
				layoutMLRule.getLayoutMLRuleActions().addAction((newActionPanel).getLayoutMLRuleAction());
			}

			AddRemoveRowsFromPanel addRemoveRowsFromPanel = new AddRemoveRowsFromPanel(backgroundColor, newActionPanel, AddRemoveRowsFromPanel.HORIZONTAL);
			TableLayoutConstraints constraint = new TableLayoutConstraints();
			constraint.col1 = 1;
			constraint.col2 = 3;
			constraint.row1 = 5;
			constraint.row2 = 5;
			constraint.hAlign = TableLayout.RIGHT;
			constraint.vAlign = TableLayout.CENTER;
			newActionPanel.add(addRemoveRowsFromPanel, constraint);

			actions.add(newActionPanel, "0," + (lastRow - 1));

			/**
			 * button control, delete enabled for every action, excerpt there is
			 * jus one action left
			 */
			Component[] panels = this.actions.getComponents();
			for (int i = 0; i < panels.length; i++) {
				((LayoutMLRuleActionPanel) panels[i]).getAddRemoveRowsFromPanel().enableDeleteButton();
			}

			editor.getRuleContainer().updateUI();
		}

		/**
		 * @param layoutMLRuleActionPanel the {@link LayoutMLRuleActionPanel} to be removed from this {@link SingleRulePanel}
		 */
		public synchronized void removeActionFromPanel(LayoutMLRuleActionPanel layoutMLRuleActionPanel) {
				TableLayout tableLayout = (TableLayout) this.actions.getLayout();
				TableLayoutConstraints constraint = tableLayout.getConstraints(layoutMLRuleActionPanel);
				int row1 = constraint.row1;
				this.layoutMLRule.getLayoutMLRuleActions().removeActionFromActions(layoutMLRuleActionPanel.getLayoutMLRuleAction());
				actionPanelsForSingleRulePanel.remove(layoutMLRuleActionPanel);
				this.actions.remove(layoutMLRuleActionPanel);
				tableLayout.deleteRow(row1 - 1);
				tableLayout.deleteRow(row1 - 1);

				/**
				 * button control, delete disabled if just one action left, add
				 * enabled only for the last action.
				 */
				if (actions.getComponentCount() > 1) {
					Component[] panels = this.actions.getComponents();
					boolean addactivated = false;
					for (int i = 0; i < panels.length; i++) {
						if (((LayoutMLRuleActionPanel) panels[i]).getAddRemoveRowsFromPanel().isAddButtonEnabled())
							addactivated = true;
					}

					if (!addactivated)
						((LayoutMLRuleActionPanel) this.actions.getComponent(actions.getComponentCount() - 1)).getAddRemoveRowsFromPanel().enableAddButton();

				} else if (this.actions.getComponentCount() == 1) {
					((LayoutMLRuleActionPanel) this.actions.getComponent(0)).getAddRemoveRowsFromPanel().enableAddButton();
					((LayoutMLRuleActionPanel) this.actions.getComponent(0)).getAddRemoveRowsFromPanel().disableDeleteButton();
				}

				this.actions.updateUI();

		}
		
		/**
		 * This Method adds a new Rule
		 */
		public void addAnotherRule() {
			expandLayout(editor.getRuleContainer().getLayout(), false);
			int lastRow = ((TableLayout) editor.getRuleContainer().getLayout()).getNumRow();
			final LayoutMLRuleSingleRulePanel newRulePanel = new LayoutMLRuleSingleRulePanel(editor,this.controlTypeOfRuleSourceComponent);
			editor.getRuleContainer().add(newRulePanel, "0," + (lastRow - 1));

			for (LayoutMLRuleSingleRulePanel singleRule : editor.getRulePanels()) {
				singleRule.addRemoveRowsFromPanel.disableAddButton();
				singleRule.addRemoveRowsFromPanel.enableDeleteButton();
			}

			newRulePanel.addRemoveRowsFromPanel.enableAddButton();
			editor.getRuleContainer().revalidate();
			SwingUtilities.invokeLater(new Runnable(){
				@Override
				public void run() {
					newRulePanel.scrollRectToVisible(newRulePanel.getBounds());
				}
			});
		}

		/**
		 * @param singleRulePanel to be removed (Contains one {@link LayoutMLRule})
		 */
		public void removeRuleFromPanel(LayoutMLRuleSingleRulePanel singleRulePanel) {
			if (editor.getRuleContainer().getComponentCount() > 1) {
				TableLayout tableLayout = (TableLayout) editor.getRuleContainer().getLayout();
				TableLayoutConstraints constraint = tableLayout.getConstraints(singleRulePanel);
				int row1 = constraint.row1;
				if (row1 == 0)
					row1 = 1;
				editor.getRuleContainer().remove(singleRulePanel);
				tableLayout.deleteRow(row1 - 1);
				tableLayout.deleteRow(row1 - 1);
				
			} 
			
			if (editor.getRuleContainer().getComponentCount() == 1) {
				((LayoutMLRuleSingleRulePanel) editor.getRuleContainer().getComponent(0)).addRemoveRowsFromPanel.disableDeleteButton();
				((LayoutMLRuleSingleRulePanel) editor.getRuleContainer().getComponent(0)).addRemoveRowsFromPanel.enableAddButton();
			}
			editor.getRuleContainer().updateUI();
		}

		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.AddRemoveRowsFromPanel.AddRemoveButtonControllable#performAddAction()
		 */
		@Override
		public void performAddAction() {
			addAnotherRule();
		}

		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.AddRemoveRowsFromPanel.AddRemoveButtonControllable#performRemoveAction()
		 */
		@Override
		public void performRemoveAction() {
			removeRuleFromPanel(this);
		}

		/**
		 * @return a List with all the {@link LayoutMLRuleActionPanel} contained in this {@link SingleRulePanel}
		 */
		public List<LayoutMLRuleActionPanel> getActionPanelsForSingleRulePanel() {
			return actionPanelsForSingleRulePanel;
		}

		/**
		 * @return the ControlType set for this {@link SingleRulePanel} needed to get Information from {@link WYSIWYGMetaInformation}
		 */
		public String getControlTypeOfRuleSourceComponent() {
			return controlTypeOfRuleSourceComponent;
		}
}
