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
package org.nuclos.client.layout.wysiwyg.component.properties;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.xml.sax.Attributes;

import org.nuclos.common2.StringUtils;
import org.nuclos.common2.layoutml.LayoutMLConstants;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PROPERTY_INITIAL_SORTING_ORDER;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSubForm;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGInitialSortingOrder;

/**
 * This Class is for editing the {@link WYSIWYGInitialSortingOrder}.<br>
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
@SuppressWarnings("serial")
public class PropertyValueInitialSortingOrder implements PropertyValue<WYSIWYGInitialSortingOrder>, LayoutMLConstants {

	private WYSIWYGInitialSortingOrder initialSortingOrder = null;

	/**
	 * Constructor
	 */
	public PropertyValueInitialSortingOrder() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getTableCellEditor(org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent, java.lang.String, org.nuclos.client.layout.wysiwyg.component.properties.PropertiesDialog)
	 */
	@Override
	public TableCellEditor getTableCellEditor(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		return new PropertyEditorInitialSortingOrder(c);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getTableCellRenderer(org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent, java.lang.String, org.nuclos.client.layout.wysiwyg.component.properties.PropertiesDialog)
	 */
	@Override
	public TableCellRenderer getTableCellRenderer(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		return new PropertyEditorInitialSortingOrder(c);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getValue()
	 */
	@Override
	public WYSIWYGInitialSortingOrder getValue() {
		return initialSortingOrder;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getValue(java.lang.Class, org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent)
	 */
	@Override
	public Object getValue(Class<?> cls, WYSIWYGComponent c) {
		if (WYSIWYGInitialSortingOrder.class.equals(cls)) {
			return initialSortingOrder;
		}
		else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(WYSIWYGInitialSortingOrder value) {
		this.initialSortingOrder = value;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#setValue(java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void setValue(String attributeName, Attributes attributes) {
		String name = attributes.getValue(ATTRIBUTE_NAME);
		String sortingorder = attributes.getValue(ATTRIBUTE_SORTINGORDER);
		
		this.initialSortingOrder = new WYSIWYGInitialSortingOrder(name, sortingorder);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		PropertyValueInitialSortingOrder clone = new PropertyValueInitialSortingOrder();
		if (initialSortingOrder != null) {
			clone.setValue(new WYSIWYGInitialSortingOrder(initialSortingOrder.getName(), initialSortingOrder.getSortingOrder()));
		}
		return clone;
	}
	
	/**
	 * This Class creates a Editor for editing the {@link WYSIWYGInitialSortingOrder}.<br>
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	class PropertyEditorInitialSortingOrder extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

		private JComboBox textField = new JComboBox();
		private JComboBox comboBox = new JComboBox();
		private WYSIWYGSubForm subform;
		
		/**
		 * Constructor
		 * @param c the {@link WYSIWYGSubForm}
		 */
		public PropertyEditorInitialSortingOrder(WYSIWYGComponent c) {
			if (c instanceof WYSIWYGSubForm)
				subform = (WYSIWYGSubForm)c;
		}
		
		/*
		 * (non-Javadoc)
		 * @see javax.swing.CellEditor#getCellEditorValue()
		 */
		@Override
		public Object getCellEditorValue() {
			return PropertyValueInitialSortingOrder.this;
		}
		
		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
		 */
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			return getComponent(true);
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			return getComponent(false);
		}

		/**
		 * @param editable should it be possible to edit the Value?
		 * @return
		 */
		private Component getComponent(boolean editable) {
			JPanel panel = new JPanel();
			panel.setMinimumSize(new Dimension(100,25));
			
			textField.removeAllItems();
			comboBox.removeAllItems();
			
			panel.setOpaque(true);
			panel.setBackground(Color.WHITE);
			
			textField = new JComboBox();
			
			if (!StringUtils.isNullOrEmpty(subform.getEntityName())){
				List<String> subformColums = subform.getParentEditor().getMetaInformation().getSubFormColumns(subform.getEntityName());
				
				for (String string : subformColums) {
					textField.addItem(string);
				}
			}
			
			if (PropertyValueInitialSortingOrder.this.initialSortingOrder != null) {
				textField.setSelectedItem(PropertyValueInitialSortingOrder.this.initialSortingOrder.getName());
			}

			comboBox = new JComboBox();
			comboBox.addItem(PROPERTY_INITIAL_SORTING_ORDER.ASCENDING);
			comboBox.addItem(PROPERTY_INITIAL_SORTING_ORDER.DESCENDING);
			
			double[][] layout = {
					{
						InterfaceGuidelines.CELL_MARGIN_LEFT, 
						TableLayout.FILL, 
						InterfaceGuidelines.MARGIN_BETWEEN, 
						TableLayout.PREFERRED,
						InterfaceGuidelines.CELL_MARGIN_RIGHT
					}, 
					{
						2, 
						TableLayout.PREFERRED
					}
				};
			
			panel.setLayout(new TableLayout(layout));
			panel.add(textField, "1,1");
			panel.add(comboBox, "3,1");
			return panel;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.AbstractCellEditor#stopCellEditing()
		 */
		@Override
		public boolean stopCellEditing() {
			if (textField.getSelectedItem() != null) {
				String selectedItem = (String) textField.getSelectedItem();
				if (StringUtils.isNullOrEmpty(selectedItem)) {
					PropertyValueInitialSortingOrder.this.initialSortingOrder = null;
				} else {
					PropertyValueInitialSortingOrder.this.initialSortingOrder = new WYSIWYGInitialSortingOrder(selectedItem, (String) comboBox.getSelectedItem());
				}
			}
			return super.stopCellEditing();
		}
	}
}
