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
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.xml.sax.Attributes;

import org.nuclos.common2.layoutml.LayoutMLConstants;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_COMPONENT_PROPERTY_EDITOR;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGUniversalComponent;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.propertyeditor.CollectableComponentPropertyEditor;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGProperty;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGPropertySet;

/**
 * This Class is for editing the {@link WYSIYWYGProperty} used for {@link WYSIWYGUniversalComponent}.
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
public class PropertyCollectableComponentProperty implements PropertyValue<WYSIYWYGProperty>, LayoutMLConstants {

	private WYSIYWYGProperty wysiwygProperty = null;

	/**
	 * Constructor
	 */
	public PropertyCollectableComponentProperty() {
		this.wysiwygProperty = new WYSIYWYGProperty();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getTableCellEditor(org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent, java.lang.String, org.nuclos.client.layout.wysiwyg.component.properties.PropertiesDialog)
	 */
	@Override
	public TableCellEditor getTableCellEditor(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		return new PropertyEditorCollectableComponentProperty();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getTableCellRenderer(org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent, java.lang.String, org.nuclos.client.layout.wysiwyg.component.properties.PropertiesDialog)
	 */
	@Override
	public TableCellRenderer getTableCellRenderer(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		return new PropertyEditorCollectableComponentProperty();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getValue()
	 */
	@Override
	public WYSIYWYGProperty getValue() {
		return wysiwygProperty;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getValue(java.lang.Class, org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent)
	 */
	@Override
	public Object getValue(Class<?> cls, WYSIWYGComponent c) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(WYSIYWYGProperty value) {
		this.wysiwygProperty = value;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#setValue(java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void setValue(String attributeName, Attributes attributes) {
		if (ELEMENT_PROPERTY.equals(attributeName)){
			String propertyName = attributes.getValue(ATTRIBUTE_NAME);
			String propertyValue = attributes.getValue(ATTRIBUTE_VALUE);
			WYSIYWYGPropertySet newPropertySet = new WYSIYWYGPropertySet(propertyName, propertyValue);
			wysiwygProperty.addWYSIYWYGPropertySet(newPropertySet);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		PropertyCollectableComponentProperty clonedPropertyCollectableComponentProperty = new PropertyCollectableComponentProperty();
		if (wysiwygProperty != null) {
			clonedPropertyCollectableComponentProperty.setValue((WYSIYWYGProperty) wysiwygProperty.clone());
		}
		return clonedPropertyCollectableComponentProperty;
	}
	
	/**
	 * This class launches the {@link CollectableComponentPropertyEditor} to edit the {@link WYSIYWYGProperty}
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	class PropertyEditorCollectableComponentProperty extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
		 */
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			return getComponent(true);
		}

		/**
		 * @param editable should it be possible to edit the value?
		 * @return
		 */
		private Component getComponent(boolean editable) {
			JPanel panel = new JPanel();
			panel.setOpaque(true);
			panel.setBackground(Color.WHITE);
			panel.setLayout(new TableLayout(new double[][]{
					{
						InterfaceGuidelines.CELL_MARGIN_LEFT,
						TableLayout.FILL,
						TableLayout.PREFERRED,
						InterfaceGuidelines.MARGIN_RIGHT
					},
					{
						InterfaceGuidelines.CELL_MARGIN_TOP,
						TableLayout.PREFERRED,
						InterfaceGuidelines.CELL_MARGIN_BOTTOM
					}
			}));
			
			final JLabel label = new JLabel();
			setLabel(label,	getValue());

			JButton launchEditor = new JButton("...");
			launchEditor.setPreferredSize(new Dimension(30, InterfaceGuidelines.CELL_BUTTON_MAXHEIGHT));
			launchEditor.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					launchEditor(label);
 				}
			});
			TableLayoutConstraints constraint = new TableLayoutConstraints(2, 1);
			panel.add(launchEditor, constraint);
			panel.add(label, new TableLayoutConstraints(1,1));
			return panel;
		}
		
		/**
		 * Sets the Label for display in the {@link PropertiesPanel} to indicate a value is set
		 * @param label
		 * @param property
		 */
		private void setLabel(JLabel label, WYSIYWYGProperty property) {
			if (property != null && property.getSize() > 0) {
				label.setText(WYSIWYGStringsAndLabels.partedString(COLLECTABLE_COMPONENT_PROPERTY_EDITOR.PROPERTIES_DEFINED, String.valueOf(property.getSize())));
			}
			else {
				label.setText(COLLECTABLE_COMPONENT_PROPERTY_EDITOR.NO_PROPERTIES_DEFINED);
			}
		}
		
		/**
		 * This Method launches the {@link CollectableComponentPropertyEditor}
		 * @param label
		 */
		private final void launchEditor(JLabel label){
			if (getValue() == null) {
				setValue(new WYSIYWYGProperty());
			}
			WYSIYWYGProperty returnWYSIWYGProperty = CollectableComponentPropertyEditor.showEditor(wysiwygProperty);
			wysiwygProperty = returnWYSIWYGProperty;
			setLabel(label, returnWYSIWYGProperty);
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.CellEditor#getCellEditorValue()
		 */
		@Override
		public Object getCellEditorValue() {
			return PropertyCollectableComponentProperty.this;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			return getComponent(false);
		}

	}
}
