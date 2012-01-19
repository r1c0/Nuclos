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

import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.xml.sax.Attributes;

import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.layoutml.LayoutMLConstants;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PROPERTY_VALUE_DIMENSION;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.common.NuclosFatalException;

/**
 * This class handles Dimension Values.<br>
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class PropertyValueDimension implements PropertyValue<Dimension>, LayoutMLConstants {

	private Dimension dimension;

	/**
	 * Constructor
	 */
	public PropertyValueDimension() {
	};

	/**
	 * Constructor
	 * @param dimension the {@link Dimension} to restore
	 */
	public PropertyValueDimension(Dimension dimension) {
		this.dimension = dimension;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getTableCellEditor(org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent, java.lang.String, org.nuclos.client.layout.wysiwyg.component.properties.PropertiesDialog)
	 */
	@Override
	public TableCellEditor getTableCellEditor(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		return new PropertyEditorDimension();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getTableCellRenderer(org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent, java.lang.String, org.nuclos.client.layout.wysiwyg.component.properties.PropertiesDialog)
	 */
	@Override
	public TableCellRenderer getTableCellRenderer(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		return new PropertyEditorDimension();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getValue()
	 */
	@Override
	public Dimension getValue() {
		return dimension;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getValue(java.lang.Class, org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent)
	 */
	@Override
	public Object getValue(Class<?> cls, WYSIWYGComponent c) {
		if (cls != null && cls.equals(Dimension.class)) {
			return dimension;
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(Dimension value) {
		this.dimension = value;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#setValue(java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void setValue(String attributeName, Attributes attributes) {
		String sHeight = attributes.getValue(ATTRIBUTE_HEIGHT);
		String sWidth = attributes.getValue(ATTRIBUTE_WIDTH);

		if (sHeight != null && sWidth != null) {
			try {
				this.dimension = new Dimension(Integer.parseInt(sWidth), Integer.parseInt(sHeight));
			} catch (NumberFormatException ex) {
				throw new NuclosFatalException(WYSIWYGStringsAndLabels.partedString(PROPERTY_VALUE_DIMENSION.ERRORMESSAGE_NUMBERFORMAT, ELEMENT_PREFERREDSIZE));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof PropertyValueDimension) {
			PropertyValueDimension value = (PropertyValueDimension) obj;

			if (this.dimension != null) {
				return this.dimension.equals(value.getValue());
			} else if (value.getValue() != null) {
				return value.getValue().equals(this.dimension);
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
		PropertyValueDimension clone = new PropertyValueDimension();
		if (dimension != null) {
			Dimension dimclone = new Dimension();
			dimclone.width = dimension.width;
			dimclone.height = dimension.height;
			clone.setValue(dimclone);
		}
		return clone;
	}

	/**
	 * This class provides a Editor for {@link Dimension}.<br>
	 * It contains a {@link JTextField} for the width and one for the height.<br>
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	class PropertyEditorDimension extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

		private JTextField textWidth = new JTextField(4);
		private JTextField textHeight = new JTextField(4);
		
		private CollectableFieldFormat format = CollectableFieldFormat.getInstance(Integer.class);

		/**
		 * This Method creates a {@link JPanel} with the Fields contained for editing.
		 * @return a {@link JPanel} with {@link JTextField} for width and height
		 */
		private JPanel getPanel() {
			JPanel panel = new JPanel();
			panel.setMinimumSize(new Dimension(100,25));
			
			panel.setOpaque(true);
			panel.setBackground(Color.WHITE);

			//textWidth.setText("0");
			textWidth.setBackground(Color.WHITE);
			//textHeight.setText("0");
			textHeight.setBackground(Color.WHITE);

			JLabel labelWidth = new JLabel(WYSIWYGStringsAndLabels.OPTIONS_EDITOR.LABEL_DIMENSION_WIDTH);
			labelWidth.setOpaque(true);
			labelWidth.setBackground(Color.WHITE);
			JLabel labelHeight = new JLabel(WYSIWYGStringsAndLabels.OPTIONS_EDITOR.LABEL_DIMENSION_HEIGHT);
			labelHeight.setOpaque(true);
			labelHeight.setBackground(Color.WHITE);
			double[][] layout = {{TableLayout.PREFERRED, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED, TableLayout.PREFERRED}, {2, TableLayout.PREFERRED}};
			panel.setLayout(new TableLayout(layout));
			panel.add(labelWidth, new TableLayoutConstraints(0, 1, 0, 1, TableLayout.CENTER, TableLayout.CENTER));
			panel.add(textWidth, new TableLayoutConstraints(1, 1, 1, 1, TableLayout.CENTER, TableLayout.CENTER));
			panel.add(labelHeight, new TableLayoutConstraints(3, 1, 3, 1, TableLayout.CENTER, TableLayout.CENTER));
			panel.add(textHeight, new TableLayoutConstraints(4, 1, 4, 1, TableLayout.CENTER, TableLayout.CENTER));

			if (dimension != null) {
				textWidth.setText(String.valueOf(dimension.width));
				textHeight.setText(String.valueOf(dimension.height));
			}
			return panel;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
		 */
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			return getPanel();
		}
		
		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			return getPanel();
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.CellEditor#getCellEditorValue()
		 */
		@Override
		public Object getCellEditorValue() {
			return PropertyValueDimension.this;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.AbstractCellEditor#stopCellEditing()
		 */
		@Override
		public boolean stopCellEditing() {
			try {
				if (dimension == null) {
					dimension = new Dimension();
				}
				if (StringUtils.isNullOrEmpty(textWidth.getText()) && StringUtils.isNullOrEmpty(textWidth.getText())) {
					dimension = null;
				}
				else {
					dimension.width = (Integer)format.parse(null, StringUtils.isNullOrEmpty(textWidth.getText())?"0":textWidth.getText());
					dimension.height = (Integer)format.parse(null, StringUtils.isNullOrEmpty(textHeight.getText())?"0":textHeight.getText());
				}
			} catch (CollectableFieldFormatException ex) {
				JOptionPane.showMessageDialog(textWidth, PROPERTY_VALUE_DIMENSION.ERRORMESSAGE_PARSING_FAILED);
				return false;
			}
			return super.stopCellEditing();
		}

	}
}
