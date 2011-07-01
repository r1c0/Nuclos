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
/**
 * 
 */
package org.nuclos.client.layout.wysiwyg.component.properties;

import java.awt.Color;
import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.xml.sax.Attributes;

import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;

/**
 * 
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author <a href="mailto:thomas.schiffmann@novabit.de">thomas.schiffmann</a>
 * @version 01.00.00
 */
@SuppressWarnings("serial")
public class PropertyValueBoolean implements PropertyValue<Boolean> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean selected = true;
	
	/**
	 * Constructor
	 */
	public PropertyValueBoolean() { }
	
	/**
	 * Constructor
	 * @param value the value to be restored
	 */
	public PropertyValueBoolean(Boolean value) {
		this.selected = (value==null)?false:value;
	}
	
	/**
	 * @return boolean if value is selected
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * @param selected sets the selected State
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getValue()
	 */
	@Override
	public Boolean getValue() {
		return selected;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(Boolean value) {
		if (value != null) {
			this.selected = value;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getTableCellEditor(org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent, java.lang.String, org.nuclos.client.layout.wysiwyg.component.properties.PropertiesDialog)
	 */
	@Override
	public TableCellEditor getTableCellEditor(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		return new PropertyEditorBoolean();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getTableCellRenderer(org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent, java.lang.String, org.nuclos.client.layout.wysiwyg.component.properties.PropertiesDialog)
	 */
	@Override
	public TableCellRenderer getTableCellRenderer(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		return new PropertyEditorBoolean();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getValue(java.lang.Class, org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent)
	 */
	@Override
	public Object getValue(Class<?> cls, WYSIWYGComponent c) {
		if (cls != null && cls.equals(boolean.class)) {
			return selected;
		} else if (cls != null && cls.equals(Boolean.class)) {
			return selected;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#setValue(java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void setValue(String attributeName, Attributes attributes) {
		String attribute = attributes.getValue(attributeName);
		if (attribute != null) {
			if (attribute.equalsIgnoreCase(ATTRIBUTEVALUE_YES)) {
				this.selected = true;
			}
			else if (attribute.equalsIgnoreCase(ATTRIBUTEVALUE_NO)) {
				this.selected = false;
			}

		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PropertyValueBoolean) {
			PropertyValueBoolean value = (PropertyValueBoolean)obj;
			
			if (selected == value.isSelected()) {
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
		PropertyValueBoolean clone = new PropertyValueBoolean();

		clone.setSelected(selected);

		return clone;		
	}
	
	/**
	 * 
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	public class PropertyEditorBoolean extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JCheckBox checkbox;
		
		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
		 */
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			checkbox = new JCheckBox();
			checkbox.setSelected(PropertyValueBoolean.this.isSelected());
			
			checkbox.setBackground(Color.WHITE);
			
			return checkbox;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			PropertyValueBoolean val = (PropertyValueBoolean)value;
			
			checkbox = new JCheckBox();
			checkbox.setSelected(val.isSelected());

			checkbox.setBackground(Color.WHITE);
			
			return checkbox;
		}
		
		/*
		 * (non-Javadoc)
		 * @see javax.swing.CellEditor#getCellEditorValue()
		 */
		@Override
		public Object getCellEditorValue() {
			return PropertyValueBoolean.this;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.AbstractCellEditor#stopCellEditing()
		 */
		@Override
		public boolean stopCellEditing() {
			PropertyValueBoolean.this.setSelected(checkbox.isSelected());
			return super.stopCellEditing();
		}
		
		
	}
}
