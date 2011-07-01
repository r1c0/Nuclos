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
import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.nuclos.client.layout.wysiwyg.component.TranslationMap;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGStaticButton;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGStaticLabel;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGStaticTitledSeparator;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSubFormColumn;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.TranslationPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.xml.sax.Attributes;

public class PropertyValueTranslations implements PropertyValue<TranslationMap> {

	private TranslationMap value;

	public PropertyValueTranslations() {
		this(new TranslationMap());
	}
	
	public PropertyValueTranslations(TranslationMap map) {
		this.value = map;
	}
	
	@Override
	public TableCellEditor getTableCellEditor(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		return new PropertyEditorTranslations(c);
	}

	@Override
	public TableCellRenderer getTableCellRenderer(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		return new PropertyEditorTranslations(c);
	}

	@Override
	public TranslationMap getValue() {
		return value;
	}

	@Override
	public Object getValue(Class<?> cls, WYSIWYGComponent c) {
		if (cls == TranslationMap.class) {
			return value;
		}
		return null;
	}

	@Override
	public void setValue(TranslationMap value) {
		this.value = value;
	}
	
	@Override
	public void setValue(String attributeName, Attributes attributes) {
		if (ELEMENT_TRANSLATION.equals(attributeName)) {
			String attributeLang = attributes.getValue(ATTRIBUTE_LANG);
			String attributeText = attributes.getValue(ATTRIBUTE_TEXT);
			value.put(attributeLang, attributeText);
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PropertyValueTranslations) {
			return value.equals(((PropertyValueTranslations) obj).value);
		}
		return false; 
	}

	@Override
	public Object clone() {
		return new PropertyValueTranslations((TranslationMap) value.clone());
	}

	public class PropertyEditorTranslations extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

		private JPanel panel = new JPanel();
		private WYSIWYGComponent c;
		
		public PropertyEditorTranslations(WYSIWYGComponent c) {
			this.c = c;
		}
		
		@Override
		public Object getCellEditorValue() {
			return PropertyValueTranslations.this;
		}
		
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			return getComponent(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			return getComponent(false);
		}
		
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
			configureLabelText(label);
			
			JButton button = new JButton("\u2026");
			button.setPreferredSize(new Dimension(30,InterfaceGuidelines.CELL_BUTTON_MAXHEIGHT));
			if (editable) {
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Map<String, String> res = TranslationPanel.showDialog(c.getParentEditor(), value, getDefaultText());
						if (res != null) {
							value.merge(res);
							configureLabelText(label);
						}
					}
				});
			}
			
			panel.add(label, new TableLayoutConstraints(1,1));
			panel.add(button, new TableLayoutConstraints(2,1));
			return panel;
		}
		
		protected String getDefaultText() {
			PropertyValue<?> p = null;;
			if (c instanceof WYSIWYGStaticButton) {
				 p = c.getProperties().getProperties().get(WYSIWYGStaticButton.PROPERTY_LABEL);
			} else if (c instanceof WYSIWYGStaticLabel) {
				 p = c.getProperties().getProperties().get(WYSIWYGStaticLabel.PROPERTY_TEXT);
			} else if (c instanceof WYSIWYGCollectableComponent) {
				 p = c.getProperties().getProperties().get(WYSIWYGCollectableComponent.PROPERTY_LABEL);
			} else if (c instanceof WYSIWYGStaticTitledSeparator) {
				 p = c.getProperties().getProperties().get(WYSIWYGStaticTitledSeparator.PROPERTY_TITLE);
			} else if (c instanceof WYSIWYGSubFormColumn) {
				 p = c.getProperties().getProperties().get(WYSIWYGSubFormColumn.PROPERTY_LABEL);
			}
			String text = null;
			if (p != null) {
				text = (String) p.getValue(String.class, c);
			}
			return (text != null) ? text : "(Default)";
		}
		
		protected void configureLabelText(JLabel label) {
			int count = PropertyValueTranslations.this.value.size();
			label.setText(count + " hinterlegt");
		}
	}
}
