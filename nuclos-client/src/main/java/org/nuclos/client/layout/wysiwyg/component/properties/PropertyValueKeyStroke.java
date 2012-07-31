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
import javax.swing.KeyStroke;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGUniversalComponent;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.editor.KeyStrokeEditor;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.editor.PropertyEditor;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGProperty;
import org.nuclos.common2.LangUtils;
import org.xml.sax.Attributes;

/**
 * This Class is for editing the {@link WYSIYWYGProperty} used for {@link WYSIWYGUniversalComponent}.
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:stefan.geiling@novabit.de">stefan.geiling</a>
 * @version 01.00.00
 */
public class PropertyValueKeyStroke extends PropertyValueString {

	private KeyStroke value = null;

	/**
	 * Constructor
	 */
	public PropertyValueKeyStroke() {
	}
	/**
	 * Constructor
	 */
	private PropertyValueKeyStroke(KeyStroke value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getTableCellEditor(org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent, java.lang.String, org.nuclos.client.layout.wysiwyg.component.properties.PropertiesDialog)
	 */
	@Override
	public TableCellEditor getTableCellEditor(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		return new PropertyEditorKeyStrokeProperty(dialog);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getTableCellRenderer(org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent, java.lang.String, org.nuclos.client.layout.wysiwyg.component.properties.PropertiesDialog)
	 */
	@Override
	public TableCellRenderer getTableCellRenderer(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		return new PropertyEditorKeyStrokeProperty(dialog);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getValue()
	 */
	@Override
	public String getValue() {
		return value == null ? "" : value.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getValue(java.lang.Class, org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent)
	 */
	@Override
	public Object getValue(Class<?> cls, WYSIWYGComponent c) {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(String value) {
		this.value = KeyStroke.getKeyStroke(value);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#setValue(java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void setValue(String attributeName, Attributes attributes) {
		this.value = KeyStroke.getKeyStroke(attributes.getValue(attributeName));
	}
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PropertyValueKeyStroke))
			return false;
		return LangUtils.equals(this.value, ((PropertyValueKeyStroke)obj).value); 
	}
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		return new PropertyValueKeyStroke(value);
	}
	
	/**
	 * This class launches the {@link PropertyEditor} to edit the {@link WYSIYWYGProperty}
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:stefan.geiling@novabit.de">stefan.geiling</a>
	 * @version 01.00.00
	 */
	class PropertyEditorKeyStrokeProperty extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

		private final PropertiesPanel dialog;
		public PropertyEditorKeyStrokeProperty(PropertiesPanel dialog) {
			this.dialog = dialog;
		}
		
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
			
			launchEditor.setEnabled(true);
		
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
		private void setLabel(JLabel label, String property) {
			if (property != null) {
				label.setText(property);
			} else {
				label.setText("");
			}
		}
		
		/**
		 * This Method launches the {@link PropertyEditor}
		 * @param label
		 */
		public final void launchEditor(JLabel label){
			setValue(KeyStrokeEditor.showEditor(getValue()));
			setLabel(label, getValue());
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.CellEditor#getCellEditorValue()
		 */
		@Override
		public Object getCellEditorValue() {
			return PropertyValueKeyStroke.this;
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
