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
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.xml.sax.Attributes;

import org.nuclos.client.NuclosIcons;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PROPERTY_VALUE_COLOR;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;

/**
 * This Class handles the {@link PropertyValueColor}.<br>
 * It uses the {@link JColorChooser} to pick colors.<br>
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class PropertyValueColor implements PropertyValue<Color> {

	private static final long serialVersionUID = 71637695193846059L;
	
	private Color color;
	
	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getTableCellEditor(org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent, java.lang.String, org.nuclos.client.layout.wysiwyg.component.properties.PropertiesDialog)
	 */
	@Override
	public TableCellEditor getTableCellEditor(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		return new PropertyEditorColor();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getTableCellRenderer(org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent, java.lang.String, org.nuclos.client.layout.wysiwyg.component.properties.PropertiesDialog)
	 */
	@Override
	public TableCellRenderer getTableCellRenderer(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		return new PropertyEditorColor();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getValue()
	 */
	@Override
	public Color getValue() {
		return color;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#getValue(java.lang.Class, org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent)
	 */
	@Override
	public Object getValue(Class<?> cls, WYSIWYGComponent c) {
		if (cls != null && cls.equals(Color.class)) {
			if (color == null) {
				return PropertyUtils.getDefaultBackground(c); 
			}
			return color;
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
	public void setValue(Color value) {
		this.color = value;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue#setValue(java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void setValue(String attributeName, Attributes attributes) {
		Integer red = Integer.parseInt(attributes.getValue(ATTRIBUTE_RED));
		Integer green = Integer.parseInt(attributes.getValue(ATTRIBUTE_GREEN));
		Integer blue = Integer.parseInt(attributes.getValue(ATTRIBUTE_BLUE));

		this.color = new Color(red, green, blue);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof PropertyValueColor) {
			PropertyValueColor value = (PropertyValueColor)obj;
			
			if (this.color != null) {
				return this.color.equals(value.getValue());
			}
			else if (value.getValue() != null) {
				return value.getValue().equals(this.color);
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
		PropertyValueColor clone = new PropertyValueColor();
		clone.setValue(color);
		return clone;
	}
	
	/**
	 * This Class is for the display of color values.<br>
	 * The Editing is done in the {@link ColorDialog} underneath.<br>
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	public class PropertyEditorColor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

		private static final long serialVersionUID = -5492846644090432597L;
		
		private JPanel panel = new JPanel();
		
		/*
		 * (non-Javadoc)
		 * @see javax.swing.CellEditor#getCellEditorValue()
		 */
		@Override
		public Object getCellEditorValue() {
			return PropertyValueColor.this;
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
		 * @param editable should the value be editable?
		 * @return
		 */
		private Component getComponent(boolean editable) {
			panel.setOpaque(true);
			panel.setBackground(Color.WHITE);
			
			panel.setLayout(new TableLayout(new double[][]{
					{
						InterfaceGuidelines.CELL_MARGIN_LEFT,
						TableLayout.FILL,
						InterfaceGuidelines.MARGIN_BETWEEN,
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
			label.setOpaque(true);
			if (getValue() != null) {
				label.setText("");
				label.setBackground(getValue());
			}
			else {
				label.setText(PROPERTY_VALUE_COLOR.NO_COLOR_DEFINED);
				label.setBackground(Color.WHITE);
			}
			
			JButton button = new JButton("...");
			button.setPreferredSize(new Dimension(30,17));
			if (editable) {
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						ColorDialog dialog = new ColorDialog(color);	
						PropertyValueColor.this.color = dialog.getColor();
						label.setBackground(PropertyValueColor.this.color==null?Color.white:PropertyValueColor.this.color);
						
						if (getValue() != null) {
							label.setText("");
							label.setBackground(getValue());
						}
						else {
							label.setText(PROPERTY_VALUE_COLOR.NO_COLOR_DEFINED);
							label.setBackground(Color.WHITE);
						}
					}
				});
			}
			
			panel.add(label, new TableLayoutConstraints(1,1));
			panel.add(button, new TableLayoutConstraints(3,1));
			return panel;
		}
	}
	
	/**
	 * Small Class providing a {@link JDialog} for choosing a {@link Color}
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	class ColorDialog extends JDialog implements SaveAndCancelButtonPanelControllable{

		private static final long serialVersionUID = 2806498099173674503L;
		
		private Color color;
		private JColorChooser colorChooser;
		private JButton remove;
		
		private ColorDialog(Color color) {
			this.setIconImage(NuclosIcons.getInstance().getScaledDialogIcon(48).getImage());
			
			this.setLayout(new TableLayout(new double[][]{
					{
						InterfaceGuidelines.MARGIN_LEFT,
						TableLayout.FILL,
						TableLayout.PREFERRED,
						InterfaceGuidelines.DISTANCE_TO_OTHER_OBJECTS,
						TableLayout.PREFERRED,
						InterfaceGuidelines.DISTANCE_TO_OTHER_OBJECTS,
						TableLayout.PREFERRED,
						TableLayout.FILL,
						InterfaceGuidelines.MARGIN_RIGHT
					},
					{
						InterfaceGuidelines.MARGIN_TOP,
						TableLayout.FILL,
						InterfaceGuidelines.DISTANCE_TO_OTHER_OBJECTS,
						TableLayout.PREFERRED,
						InterfaceGuidelines.MARGIN_BOTTOM
					}
			}));
			
			this.color = color;
			this.colorChooser = new JColorChooser(this.color==null?Color.WHITE:this.color);
			
			this.add(colorChooser, new TableLayoutConstraints(1,1,7,1));
			
			this.remove = new JButton(PROPERTY_VALUE_COLOR.LABEL_REMOVE_BACKGROUND);
			this.remove.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ColorDialog.this.color = null;
					ColorDialog.this.setVisible(false);
				}
			});
			
			List<AbstractButton> additionalButtons = new ArrayList<AbstractButton>();
			additionalButtons.add(remove);
			
			SaveAndCancelButtonPanel saveandcancel = new SaveAndCancelButtonPanel(this.getBackground(), this, additionalButtons);
			this.add(saveandcancel, new TableLayoutConstraints(2,3,6,3));
			
			setTitle(PROPERTY_VALUE_COLOR.COLOR_PICKER_TITLE);
			
			int width = 400;
			int height = 300;
			Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
			int x = (screenSize.width - width) / 2;
			int y = (screenSize.height - height) / 2;
			this.setBounds(x, y, width, height);
			this.setModal(true);
			this.setVisible(true);
		}
		
		/**
		 * @return the {@link Color} set
		 */
		public Color getColor() {
			return this.color;
		}

		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable#performCancelAction()
		 */
		@Override
		public void performCancelAction() {
			ColorDialog.this.dispose();
		}

		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable#performSaveAction()
		 */
		@Override
		public void performSaveAction() {
			ColorDialog.this.color = colorChooser.getColor();
			ColorDialog.this.setVisible(false);
		}
	}
}
