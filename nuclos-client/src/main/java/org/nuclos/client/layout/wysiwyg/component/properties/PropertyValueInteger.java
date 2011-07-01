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

import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Component;
import java.awt.Font;
import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.xml.sax.Attributes;

import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;

/**
 * This Class is for Editing Integer Values.<br>
 * 
 * e.g. {@link TableLayoutConstraints}
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
@SuppressWarnings("serial")
public class PropertyValueInteger implements PropertyValue<Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer value;

	Map<Integer, String> values;
	
	/**
	 * Constructor
	 */
	public PropertyValueInteger() { }
	
	/**
	 * Constructor
	 * @param value single value to restore (creates {@link JTextField} for editing)
	 */
	public PropertyValueInteger(Integer value) {
		this.value = value;
	}
	
	/**
	 * Constructor
	 * @param values List of Values to show (creates {@link JComboBox} for editing
	 */
	public PropertyValueInteger(Map<Integer, String> values) {
		this.values = values;
	}
	
	/**
	 * 
	 * @param value
	 * @param values
	 */
	public PropertyValueInteger(Integer value, Map<Integer, String> values) {
		this.value = value;
		this.values = values;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getTableCellEditor(org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent, java.lang.String, org.nuclos.client.layout.wysiwyg.component.properties.PropertiesDialog)
	 */
	@Override
	public TableCellEditor getTableCellEditor(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		return new PropertyEditorInteger(values);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getTableCellRenderer(org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent, java.lang.String, org.nuclos.client.layout.wysiwyg.component.properties.PropertiesDialog)
	 */
	@Override
	public TableCellRenderer getTableCellRenderer(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		return new PropertyEditorInteger(values);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getValue()
	 */
	@Override
	public Integer getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getValue(java.lang.Class, org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent)
	 */
	@Override
	public Object getValue(Class<?> cls, WYSIWYGComponent c) {
		if (cls != null && cls.equals(int.class)) {
			return (value==null)?0:value;
		}
		else if (cls !=null && cls.equals(Font.class)) {
			Font def = PropertyUtils.getDefaultFont(c);
			if (def != null) {
				float fNewFontSize = def.getSize();
				if (value != null)
					fNewFontSize = fNewFontSize + value;
				final Font fontNew = def.deriveFont(fNewFontSize);
				return fontNew;
			}
			return null;
		}
		else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#setValue(java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void setValue(String attributeName, Attributes attributes) {
		if (attributes.getValue(attributeName) != null) {
			this.value = Integer.parseInt(attributes.getValue(attributeName));
		} else {
			this.value = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(Integer value) {
		this.value = value;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof PropertyValueInteger) {
			PropertyValueInteger value = (PropertyValueInteger)obj;
			
			if (this.value != null) {
				return this.value.equals(value.getValue());
			}
			else if (value.getValue() != null) {
				return value.getValue().equals(this.value);
			}
			else {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		return new PropertyValueInteger(value);
	}
	
	/**
	 * This Class is for editing Integer Values.<br>
	 * It displays a {@link JTextField} for editing if there is only one value, if there is a List of Values, it displays a {@link JComboBox}.<br>
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	class PropertyEditorInteger extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Map<Integer, String> list;
		private JComboBox comboBox;
		private JTextField textField;
		
		private CollectableFieldFormat format = CollectableFieldFormat.getInstance(Integer.class);
		
		/**
		 * @param list
		 */
		public PropertyEditorInteger(Map<Integer, String> list) {
			this.list = list;
		}
		
		/*
		 * (non-Javadoc)
		 * @see javax.swing.CellEditor#getCellEditorValue()
		 */
		@Override
		public Object getCellEditorValue() {
			
			return PropertyValueInteger.this;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			return getComponent(false);
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
		 * @see javax.swing.AbstractCellEditor#stopCellEditing()
		 */
		@Override
		public boolean stopCellEditing() {
			if (comboBox != null) {
				for (Map.Entry<Integer, String> e : list.entrySet()) {
					if (e.getValue() != null && e.getValue().equals(comboBox.getSelectedItem())) {
						PropertyValueInteger.this.value = e.getKey();
					}
				}
			}
			else if (textField != null) {
				try {
					PropertyValueInteger.this.value = (Integer)format.parse(null, textField.getText());
				}
				catch (CollectableFieldFormatException ex) {
					JOptionPane.showMessageDialog(textField, ex.getMessage());
					return false;
				}
			}
			return super.stopCellEditing();
		}

		/**
		 * @param editable should it be possible to edit the value?
		 * @return
		 */
		private Component getComponent(boolean editable) {
			if (list != null) {
				comboBox = new JComboBox();
				comboBox.setBorder(null);
				for (Map.Entry<Integer, String> item : list.entrySet()) {
					comboBox.addItem(item.getValue());
				}
				comboBox.setSelectedItem(list.get(PropertyValueInteger.this.value));
				return comboBox;
			}
			else {
				if (editable) {
					textField = new JTextField();
					textField.setBorder(null);
					if (PropertyValueInteger.this.value != null) {
						textField.setText(format.format(null, PropertyValueInteger.this.value));
					}
					return textField;
				}
				else {
					if (PropertyValueInteger.this.value != null) {
						return PropertiesPanel.getCellComponent(format.format(null, PropertyValueInteger.this.value));
					}
					else {
						return new JLabel();
					}
				}
				
			}
		}
	}
}
