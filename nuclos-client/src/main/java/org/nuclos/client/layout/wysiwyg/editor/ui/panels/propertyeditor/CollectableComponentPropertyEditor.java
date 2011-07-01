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
package org.nuclos.client.layout.wysiwyg.editor.ui.panels.propertyeditor;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.nuclos.common2.layoutml.LayoutMLConstants;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_COMPONENT_PROPERTY_EDITOR;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.AddRemoveRowsFromPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.AddRemoveRowsFromPanel.AddRemoveButtonControllable;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGProperty;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGPropertySet;

/**
 * This Class opens a small Editor for adding {@link LayoutMLConstants#ELEMENT_PROPERTY} to a {@link WYSIWYGComponent}.<br>
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
public class CollectableComponentPropertyEditor extends JDialog implements SaveAndCancelButtonPanelControllable {

	private int height = 300;
	private int width = 350;

	private JPanel propertyContainer = null;

	private WYSIYWYGProperty wysiwygProperty;

	private WYSIYWYGProperty backupWYSIWYGProperty;

	public static WYSIYWYGProperty returnWYSIWYGProperty;

	/**
	 * Constructor
	 * @param wysiwygProperty the {@link WYSIYWYGProperty} to be edited by this Editor
	 */
	private CollectableComponentPropertyEditor(WYSIYWYGProperty wysiwygProperty) {
		//NUCLEUSINT-283
		this.setTitle(COLLECTABLE_COMPONENT_PROPERTY_EDITOR.TITLE_PROPERTY_EDITOR);
		//TODO align relative to parent Component
		this.wysiwygProperty = wysiwygProperty;
		double[][] layout = {{InterfaceGuidelines.MARGIN_LEFT, TableLayout.FILL,TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_RIGHT}, {InterfaceGuidelines.MARGIN_TOP, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.FILL, InterfaceGuidelines.MARGIN_BETWEEN , TableLayout.PREFERRED}};
		this.setLayout(new TableLayout(layout));

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		/**
		 * the property panel
		 */
		JButton addProperty = new JButton(COLLECTABLE_COMPONENT_PROPERTY_EDITOR.BUTTON_ADD_PROPERTY);
		addProperty.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addPropertyPanelIntoPanel(null);
			}
		});

		this.add(addProperty, "2,1");

		/**
		 * the parameters
		 */
		propertyContainer = new JPanel();
		propertyContainer.setLayout(new TableLayout(new double[][]{{TableLayout.FILL}, {}}));

		JScrollPane scrollbar = new JScrollPane(propertyContainer);
		scrollbar.getVerticalScrollBar().setVisible(true);
		scrollbar.getVerticalScrollBar().setUnitIncrement(20);
		TableLayoutConstraints constraint = new TableLayoutConstraints(1, 3, 2, 3);
		this.add(scrollbar, constraint);

		/**
		 * save and cancel
		 */
		JButton deleteValueList = new JButton(COLLECTABLE_COMPONENT_PROPERTY_EDITOR.BUTTON_DELETE_PROPERTY);
		deleteValueList.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				clearPropertiesForComponent();
			}

		});

		constraint = new TableLayoutConstraints(0, 5, 3, 5);
		ArrayList<AbstractButton> buttons = new ArrayList<AbstractButton>(1);
		buttons.add(deleteValueList);
		this.add(new SaveAndCancelButtonPanel(propertyContainer.getBackground(), this, buttons), constraint);

		try {
			backupWYSIWYGProperty = (WYSIYWYGProperty) wysiwygProperty.clone();
		} catch (CloneNotSupportedException e1) {
			/** nothing to do, does support clone() */
		}

		for (WYSIYWYGPropertySet propertySet : this.wysiwygProperty.getAllPropertyEntries()) {
			addPropertyPanelIntoPanel(propertySet);
		}

		Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screenSize.width - width) / 2;
		int y = (screenSize.height - height) / 2;
		this.setBounds(x, y, width, height);
		this.setModal(true);
		this.setVisible(true);
	}

	/**
	 * This Method shows the Editor, works like {@link JOptionPane#showInputDialog(Object)}
	 * @param wysiwygProperty the {@link WYSIYWYGProperty} to be edited
	 * @return the edited {@link WYSIYWYGProperty}
	 */
	public static WYSIYWYGProperty showEditor(WYSIYWYGProperty wysiwygProperty) {
		new CollectableComponentPropertyEditor(wysiwygProperty);

		return returnWYSIWYGProperty;
	}

	/**
	 * Removing all {@link WYSIYWYGProperty} for this {@link WYSIWYGComponent}
	 */
	private final void clearPropertiesForComponent() {
		wysiwygProperty = null;
		backupWYSIWYGProperty = null;

		returnWYSIWYGProperty = null;
		this.dispose();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable#performCancelAction()
	 */
	@Override
	public void performCancelAction() {
		wysiwygProperty = null;
		wysiwygProperty = backupWYSIWYGProperty;
		returnWYSIWYGProperty = backupWYSIWYGProperty;
		this.dispose();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable#performSaveAction()
	 */
	@Override
	public void performSaveAction() {
		returnWYSIWYGProperty = wysiwygProperty;
			this.dispose();
	}

	/**
	 * Remove a {@link PropertyPanel} from this Editor
	 * @param propertyPanel to be removed
	 */
	public void removePropertyFromPanel(PropertyPanel propertyPanel) {
		this.wysiwygProperty.removeWYSIYWYGPropertySet(propertyPanel.getWYSIWYGPropertySet());
		TableLayout tablelayout = (TableLayout) propertyContainer.getLayout();
		TableLayoutConstraints constraint = tablelayout.getConstraints(propertyPanel);
		int row = constraint.row1;
		if (row - 1 < 0)
			row = 0;
		else
			row = row - 1;

		propertyContainer.remove(propertyPanel);
		removeOneRow(row);
		propertyContainer.updateUI();
	}

	/**
	 * Add a Property to this Editor
	 * @param wysiwygPropertySet the {@link WYSIYWYGPropertySet} to be added
	 */
	public void addPropertyPanelIntoPanel(WYSIYWYGPropertySet wysiwygPropertySet) {
		WYSIYWYGPropertySet newPropertySet = wysiwygPropertySet;
		if (newPropertySet == null) {
			newPropertySet = new WYSIYWYGPropertySet();
			wysiwygProperty.addWYSIYWYGPropertySet(newPropertySet);
		}
		PropertyPanel newPanel = new PropertyPanel(newPropertySet);

		expandLayout();
		propertyContainer.add(newPanel, "0,0");
		propertyContainer.updateUI();
	}

	/**
	 * Add a new Row to the Layout to put a {@link PropertyPanel} in it
	 */
	private void expandLayout() {
		TableLayout tablelayout = (TableLayout) propertyContainer.getLayout();
		tablelayout.insertRow(0, InterfaceGuidelines.MARGIN_BETWEEN);
		tablelayout.insertRow(0, TableLayout.PREFERRED);
	}

	/**
	 * Remove a Row from the Layout
	 * @param row the Row to delete
	 */
	private void removeOneRow(int row) {
		TableLayout tablelayout = (TableLayout) propertyContainer.getLayout();
		tablelayout.deleteRow(row);
		tablelayout.deleteRow(row);
	}

	/**
	 * This Class wraps a {@link WYSIYWYGPropertySet}.<br>
	 * Its used by the {@link CollectableComponentPropertyEditor} to store:
	 * <ul>
	 * <li> the Name of the Property</li>
	 * <li> the Value of the Property</li>
	 * </ul>
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	private class PropertyPanel extends JPanel implements AddRemoveButtonControllable {

		WYSIYWYGPropertySet wysiwygPropertySet = null;
		private JTextField txtName = null;
		private JTextField txtValue = null;

		/**
		 * Constructor
		 * @param wysiwygPropertySet to attach to this {@link PropertyPanel}
		 */
		public PropertyPanel(WYSIYWYGPropertySet wysiwygPropertySet) {
			this.wysiwygPropertySet = wysiwygPropertySet;
			double[][] layout = {
					{InterfaceGuidelines.MARGIN_LEFT, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED}, 
					{InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED}};
			this.setLayout(new TableLayout(layout));
			JLabel lblName = new JLabel(COLLECTABLE_COMPONENT_PROPERTY_EDITOR.LABEL_PROPERTY_NAME);
			JLabel lblValue = new JLabel(COLLECTABLE_COMPONENT_PROPERTY_EDITOR.LABEL_PROPERTY_VALUE);

			txtName = new JTextField(15);
			txtName.setText(wysiwygPropertySet.getPropertyName());
			txtName.addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {}

				@Override
				public void focusLost(FocusEvent e) {
					changeValueForName();
				}
			});

			txtValue = new JTextField(15);
			txtValue.setText(wysiwygPropertySet.getPropertyValue());
			txtValue.addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {}

				@Override
				public void focusLost(FocusEvent e) {
					changeValueForValue();
				}
			});

			this.add(lblName, "1,1");
			TableLayoutConstraints constraint = new TableLayoutConstraints(3, 1, 3, 1, TableLayout.FULL, TableLayout.CENTER);
			this.add(txtName, constraint);
			this.add(lblValue, "1,3");
			constraint = new TableLayoutConstraints(3, 3, 3, 3, TableLayout.FULL, TableLayout.CENTER);
			this.add(txtValue, constraint);
			constraint = new TableLayoutConstraints(5, 1, 5, 3, TableLayout.FULL, TableLayout.FULL);
			this.add(new AddRemoveRowsFromPanel(this.getBackground(), this), constraint);
		}

		/**
		 * Method for storing the Value of this Property read from the Textfield
		 * 
		 */
		protected void changeValueForValue() {
			this.wysiwygPropertySet.setPropertyValue(this.txtValue.getText());
		}

		/**
		 * Method for storing the Name of this Property read from the Textfield 
		 */
		protected void changeValueForName() {
			this.wysiwygPropertySet.setPropertyName(this.txtName.getText());
		}

		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.AddRemoveRowsFromPanel.AddRemoveButtonControllable#performAddAction()
		 */
		@Override
		public void performAddAction() {
			CollectableComponentPropertyEditor.this.addPropertyPanelIntoPanel(null);
		}

		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.AddRemoveRowsFromPanel.AddRemoveButtonControllable#performRemoveAction()
		 */
		@Override
		public void performRemoveAction() {
			CollectableComponentPropertyEditor.this.removePropertyFromPanel(this);
		}

		/**
		 * @return the {@link WYSIYWYGPropertySet} attached to this {@link PropertyPanel}
		 */
		public WYSIYWYGPropertySet getWYSIWYGPropertySet() {
			return this.wysiwygPropertySet;
		}
	}
}
