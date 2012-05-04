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

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation.StringResourceIdPair;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PROPERTY_LABELS;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.ui.ResourceIdMapper;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.StringUtils;
import org.xml.sax.Attributes;

/**
 * This class is for Editing String Values.<br>
 * e.g. Name of a {@link WYSIWYGComponent}
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class PropertyValueString implements PropertyValue<String> {

	private String value;

	private List<StringResourceIdPair> values;

	/**
	 * Constructor
	 */
	public PropertyValueString() { }
	
	/**
	 * Constructor
	 * @param value the value to restore (creates a {@link JTextField} for editing)
	 */
	public PropertyValueString(String value) {
		this.value = value;
	}
	
	@Override
	public TableCellEditor getTableCellEditor(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		if (c.getPropertyValuesFromMetaInformation() != null){
			for (String[] valueFromMeta : c.getPropertyValuesFromMetaInformation()){
				if (valueFromMeta[0].equals(property)){
					values = c.getProperties().getMetaInformation().getListOfMetaValues(c, valueFromMeta, dialog);
				}
			}
		}
		
		if (c.getPropertyValuesStatic() != null){
			for (String[] valueFromStatic : c.getPropertyValuesStatic()){
				if (valueFromStatic[0].equals(property)){
					values = new ArrayList<StringResourceIdPair>();
					
					for (int i = 1; i < valueFromStatic.length; i++){
						values.add(new StringResourceIdPair(valueFromStatic[i], null));
					}
				}
			}
		}
		//NUCLEUSINT-1159
		if (PROPERTY_LABELS.ACTIONCOMMAND.equals(property))
			return new PropertyEditorString(values,true);
		
		return new PropertyEditorString(values,false);	
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getTableCellRenderer(org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent, java.lang.String, org.nuclos.client.layout.wysiwyg.component.properties.PropertiesDialog)
	 */
	@Override
	public TableCellRenderer getTableCellRenderer(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		if (c.getPropertyValuesFromMetaInformation() != null){
			for (String[] valueFromMeta : c.getPropertyValuesFromMetaInformation()){
				if (valueFromMeta[0].equals(property)){
					values = c.getProperties().getMetaInformation().getListOfMetaValues(c, valueFromMeta, dialog);
				}
			}
		}
		
		if (c.getPropertyValuesStatic() != null){
			for (String[] valueFromStatic : c.getPropertyValuesStatic()){
				if (valueFromStatic[0].equals(property)){
					values = new ArrayList<StringResourceIdPair>();
					
					for (int i = 1; i < valueFromStatic.length; i++){
						values.add(new StringResourceIdPair(valueFromStatic[i], null));
					}
				}
			}
		}
		//NUCLEUSINT-1159
		if (PROPERTY_LABELS.ACTIONCOMMAND.equals(property))
			return new PropertyEditorString(values,true);
		
		return new PropertyEditorString(values,false);	
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getValue()
	 */
	@Override
	public String getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getValue(java.lang.Class, org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent)
	 */
	@Override
	public Object getValue(Class<?> cls, WYSIWYGComponent c) {
		if (cls != null && cls.equals(String.class)) {
			return value;
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
		this.value = attributes.getValue(attributeName);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(String value) {
		this.value = value;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof PropertyValueString) {
			PropertyValueString value = (PropertyValueString)obj;
			
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
		return new PropertyValueString(value);
	}
	
	/**
	 * This Class is for editing Strings.<br>
	 * If there are {@link WYSIWYGComponent#getPropertyValuesStatic()} it shows a {@link JComboBox} with the provided Values.<br>
	 * Otherwise it shows a {@link JTextField} for editing.<br>
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	class PropertyEditorString extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

		private final List<String> list;
		private final ResourceIdMapper<String> resourceIdMapper;
		private JComboBox comboBox;
		private JTextField textField;
		//NUCLEUSINT-1159
		private final boolean insertable;
		
		/**
		 * The Constructor
		 * @param pairList
		 */
		public PropertyEditorString(List<StringResourceIdPair> pairList, boolean insertable) {
			//NUCLEUSINT-1159
			this.insertable = insertable;
			
			if (pairList != null) {
				Map<String, String> map = CollectionUtils.transformPairsIntoMap(pairList);
				this.resourceIdMapper = new ResourceIdMapper<String>(map);
				this.list = new ArrayList<String>(map.keySet());
				// Sort collections by translation
				Collections.sort(this.list, resourceIdMapper);
			} else {
				this.list = null;
				this.resourceIdMapper = null;
			}
		}
		
		/**
		 * 
		 */
		@Override
		public Object getCellEditorValue() {
			if (comboBox != null) {
				PropertyValueString.this.value = StringUtils.isNullOrEmpty((String)comboBox.getSelectedItem())?null:(String)comboBox.getSelectedItem();
			}
			else if (textField != null) {
				if (StringUtils.isNullOrEmpty(textField.getText())) {
					PropertyValueString.this.value = null;
				}
				else {
					PropertyValueString.this.value = textField.getText();
				}
			}
			return PropertyValueString.this;
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
		
		/**
		 * 
		 * @param editable should it be possible to edit the values?
		 * @return 
		 */
		private Component getComponent(boolean editable) {
			if (list != null) {
				comboBox = new JComboBox();
				//NUCLEUSINT-1159
				if (insertable)
					comboBox.setEditable(true);
				comboBox.setBorder(null);
				
				for (String item : list) {
					comboBox.addItem(item);
				}
				comboBox.setSelectedItem(PropertyValueString.this.value);
				
				comboBox.setRenderer(new DefaultListRenderer(resourceIdMapper));
				AutoCompleteDecorator.decorate(comboBox, resourceIdMapper);
				
				return comboBox;
			}
			else {
				if (editable) {
					textField = new JTextField(PropertyValueString.this.value);
					textField.setBorder(null);
					return textField;
				}
				else {
					return PropertiesPanel.getCellComponent(PropertyValueString.this.value);
				}
			}
		}
	}
}
