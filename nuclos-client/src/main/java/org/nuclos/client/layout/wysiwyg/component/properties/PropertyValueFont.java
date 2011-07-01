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
import java.awt.Font;

import javax.swing.AbstractCellEditor;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.xml.sax.Attributes;

import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;

/**
 * This Class is for Editing the Font Size.<br>
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
@SuppressWarnings("serial")
public class PropertyValueFont implements PropertyValue<Integer> {

	private final int FONT_MINIMUM_SIZE = InterfaceGuidelines.FONT_MINIMUM_SIZE;
	private final int FONT_MAXIMUM_SIZE = InterfaceGuidelines.FONT_MAXIMUM_SIZE;

	private Integer value;

	/**
	 * Constructor
	 */
	public PropertyValueFont() {
	}

	/**
	 * Constructor
	 * @param value the Value to restore
	 */
	public PropertyValueFont(Integer value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getTableCellEditor(org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent, java.lang.String, org.nuclos.client.layout.wysiwyg.component.properties.PropertiesDialog)
	 */
	@Override
	public TableCellEditor getTableCellEditor(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		return new PropertyEditorFont(value);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getTableCellRenderer(org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent, java.lang.String, org.nuclos.client.layout.wysiwyg.component.properties.PropertiesDialog)
	 */
	@Override
	public TableCellRenderer getTableCellRenderer(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		return new PropertyEditorFont(value);
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
			return (value == null) ? 0 : value;
		} else if (cls != null && cls.equals(Font.class)) {
			Font def = PropertyUtils.getDefaultFont(c);
			if (def != null) {
				float fNewFontSize = def.getSize();
				if (value != null)
					fNewFontSize = fNewFontSize + value;
				final Font fontNew = def.deriveFont(fNewFontSize);
				return fontNew;
			}
			return null;
		} else {
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
		if (obj != null && obj instanceof PropertyValueFont) {
			PropertyValueFont value = (PropertyValueFont) obj;

			if (this.value != null) {
				return this.value.equals(value.getValue());
			} else if (value.getValue() != null) {
				return value.getValue().equals(this.value);
			} else {
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
		return new PropertyValueFont(value);
	}

	/**
	 * This class provides a Editor for changing the Fontsize.<br>
	 * The size is changed with a {@link JSlider}.
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	class PropertyEditorFont extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

		private JTextField inputBox = null;
		private JSlider slider = null;
		private Integer fontSize = null;
		
		private int value = 0;

		private CollectableFieldFormat format = CollectableFieldFormat.getInstance(Integer.class);

		/**
		 * Constructor
		 * @param fontSize the size to restore
		 */
		public PropertyEditorFont(Integer fontSize) {
			this.fontSize = fontSize;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.CellEditor#getCellEditorValue()
		 */
		@Override
		public Object getCellEditorValue() {
			return PropertyValueFont.this;
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
			PropertyValueFont.this.value = slider.getValue();
			return super.stopCellEditing();
		}

		/**
		 * @param editable should it be possible to edit the Value?
		 * @return
		 */
		private Component getComponent(boolean editable) {
			JPanel panel = new JPanel();
			panel.setBackground(Color.WHITE);
			panel.setLayout(new TableLayout(new double[][]{{InterfaceGuidelines.CELL_MARGIN_LEFT, TableLayout.FILL, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED, InterfaceGuidelines.CELL_MARGIN_RIGHT}, {InterfaceGuidelines.CELL_MARGIN_TOP, TableLayout.PREFERRED, InterfaceGuidelines.CELL_MARGIN_BOTTOM}}));

			slider = new JSlider();
			slider.setBackground(Color.WHITE);
			slider.setMinimum(FONT_MINIMUM_SIZE);
			slider.setMaximum(FONT_MAXIMUM_SIZE);
			if (fontSize != null)
				slider.setValue(fontSize);
			else
				slider.setValue(0);
			slider.setBorder(null);
			slider.setPaintTicks(true);
			slider.setPreferredSize(new Dimension(150, InterfaceGuidelines.CELL_BUTTON_MAXHEIGHT));
			slider.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					changeValue(slider.getValue() + "");
					inputBox.setText(slider.getValue() + "");
				}

			});

			inputBox = new JTextField();
			inputBox.setEnabled(false);
			inputBox.setColumns(3);
			if (fontSize != null)
				inputBox.setText(fontSize + "");
			else
				inputBox.setText("0");

			panel.add(slider, "1,1");

			panel.add(inputBox, "3,1");

			return panel;
		}

		/**
		 * This Method handles the Change of the Value and does validation
		 * @param incomingvalue to set
		 */
		private final void changeValue(String incomingvalue) {
			try {
				Integer size = (Integer) format.parse(null, incomingvalue);
				if (size > FONT_MINIMUM_SIZE && size < FONT_MAXIMUM_SIZE) {
					value = size;
				}
				inputBox.setText(value + "");
			} catch (CollectableFieldFormatException e1) {}
		}
	}
}
