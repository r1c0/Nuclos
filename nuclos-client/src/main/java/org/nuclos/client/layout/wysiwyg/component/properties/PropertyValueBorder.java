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
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.BORDER_EDITOR;
import org.nuclos.client.layout.wysiwyg.component.TitledBorderWithTranslations;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.bordereditor.BorderEditor;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.xml.sax.Attributes;

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
@SuppressWarnings("serial")
public class PropertyValueBorder implements PropertyValue<Border> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Border border;
	private boolean clearBorder = false;

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getTableCellEditor(org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent, java.lang.String, org.nuclos.client.layout.wysiwyg.component.properties.PropertiesDialog)
	 */
	@Override
	public TableCellEditor getTableCellEditor(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		return new PropertyEditorBorder(c);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getTableCellRenderer(org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent, java.lang.String, org.nuclos.client.layout.wysiwyg.component.properties.PropertiesDialog)
	 */
	@Override
	public TableCellRenderer getTableCellRenderer(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		return new PropertyEditorBorder(c);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getValue()
	 */
	@Override
	public Border getValue() {
		return border;
	}
	

	public boolean isClearBorder() {
		return clearBorder;
	}
	
	public void setClearBorder(boolean value) {
		this.clearBorder = value;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getValue(java.lang.Class, org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent)
	 */
	@Override
	public Object getValue(Class<?> cls, WYSIWYGComponent c) {
		if (cls != null && cls.equals(Border.class)) {
			if (c instanceof JComponent) {
				final Border borderOld = PropertyUtils.getDefaultBorder(c);
				if (borderOld == null) {
					return border;
				}
				else if (!clearBorder) {
					return BorderFactory.createCompoundBorder(border, borderOld);
				}
			}
			return border;
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
	public void setValue(Border value) {
		this.border = value;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#setValue(java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void setValue(String attributeName, Attributes attributes) {
		Border border = null;
		if (attributeName.equals(ELEMENT_BEVELBORDER)) {
			String type = attributes.getValue(ATTRIBUTE_TYPE);
			border = BorderFactory.createBevelBorder(ATTRIBUTEVALUE_RAISED.equals(type) ? BevelBorder.RAISED : BevelBorder.LOWERED);
		} else if (attributeName.equals(ELEMENT_EMPTYBORDER)) {
			Integer top = Integer.parseInt(attributes.getValue(ATTRIBUTE_TOP));
			Integer left = Integer.parseInt(attributes.getValue(ATTRIBUTE_LEFT));
			Integer bottom = Integer.parseInt(attributes.getValue(ATTRIBUTE_BOTTOM));
			Integer right = Integer.parseInt(attributes.getValue(ATTRIBUTE_RIGHT));
			
			border = BorderFactory.createEmptyBorder(top, left, bottom, right);
		} else if (attributeName.equals(ELEMENT_ETCHEDBORDER)) {
			String type = attributes.getValue(ATTRIBUTE_TYPE);
			border = BorderFactory.createEtchedBorder(ATTRIBUTEVALUE_RAISED.equals(type) ? EtchedBorder.RAISED : EtchedBorder.LOWERED);
		} else if (attributeName.equals(ELEMENT_LINEBORDER)) {
			Integer red = Integer.parseInt(attributes.getValue(ATTRIBUTE_RED));
			Integer green = Integer.parseInt(attributes.getValue(ATTRIBUTE_GREEN));
			Integer blue = Integer.parseInt(attributes.getValue(ATTRIBUTE_BLUE));
			Integer thickness = Integer.parseInt(attributes.getValue(ATTRIBUTE_THICKNESS));
			
			border = BorderFactory.createLineBorder(new Color(red, green, blue), thickness);
		} else if (attributeName.equals(ELEMENT_TITLEDBORDER)) {
			String title = attributes.getValue(ATTRIBUTE_TITLE);
			border = new TitledBorderWithTranslations(title);
		} else if (attributeName.equals(ELEMENT_CLEARBORDER)) {
			this.clearBorder = true;
		}
		
		if (border != null) {
			if (this.border == null) {
				this.border = border;
			}
			else {
				this.border = BorderFactory.createCompoundBorder(border, this.border);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof PropertyValueBorder) {
			PropertyValueBorder value = (PropertyValueBorder)obj;
			
			if (this.border != null) {
				if (this.border.equals(value.getValue()) && this.clearBorder == value.clearBorder) {
					return true;
				}
				else { 
					return false;
				}
			}
			else if (value.getValue() != null) {
				if (value.getValue().equals(this.border) && this.clearBorder == value.clearBorder) {
					return true;
				}
				else { 
					return false;
				}
			}
			else {
				return this.clearBorder == value.clearBorder;
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
		PropertyValueBorder clone = new PropertyValueBorder();
		clone.setValue(border);
		clone.setClearBorder(clearBorder);
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
	public class PropertyEditorBorder extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JPanel panel = new JPanel();
		private WYSIWYGComponent c;
		
		/**
		 * Constructor
		 * @param c the {@link WYSIWYGComponent} to set the {@link Border} to 
		 */
		public PropertyEditorBorder(WYSIWYGComponent c) {
			this.c = c;
		}
		
		/*
		 * (non-Javadoc)
		 * @see javax.swing.CellEditor#getCellEditorValue()
		 */
		@Override
		public Object getCellEditorValue() {
			return PropertyValueBorder.this;
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
		 * @param editable should it be possible to edit the value?
		 * @return
		 */
		private Component getComponent(boolean editable) {
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
			
			JButton button = new JButton("...");
			button.setPreferredSize(new Dimension(30,InterfaceGuidelines.CELL_BUTTON_MAXHEIGHT));
			if (editable) {
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						BorderEditor editor = new BorderEditor(c, PropertyValueBorder.this.border, PropertyValueBorder.this.clearBorder);
						PropertyValueBorder.this.border = editor.getBorder();
						PropertyValueBorder.this.clearBorder = editor.getClearBorder();
						setLabel(label,	PropertyValueBorder.this.border);
					}
				});
			}
			
			panel.add(label, new TableLayoutConstraints(1,1));
			panel.add(button, new TableLayoutConstraints(2,1));
			return panel;
		}
		
		/**
		 * This Method sets the {@link JLabel} the Numbers of {@link Border}s defined for this {@link WYSIWYGComponent}
		 * @param label
		 * @param border the defined {@link Border}
		 */
		private void setLabel(JLabel label, Border border) {
			int i = 0;
			if (border != null) {
				while (border instanceof CompoundBorder) {
					i++;
					border = ((CompoundBorder)border).getInsideBorder();
				}
				i++;
				label.setText(WYSIWYGStringsAndLabels.partedString(BORDER_EDITOR.BORDERS_DEFINED, String.valueOf(i)));
			}
			else {
				label.setText(BORDER_EDITOR.NO_BORDERS_DEFINED);
			}
		}
	}
}
