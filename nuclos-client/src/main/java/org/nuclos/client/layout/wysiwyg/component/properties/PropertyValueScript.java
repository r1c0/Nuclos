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

import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.scripting.ScriptEditor;
import org.nuclos.common.NuclosScript;
import org.xml.sax.Attributes;

/**
 * This class is for Editing NuclosScript Values.<br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author thomas.schiffmann
 * @see NuclosScript
 */
@SuppressWarnings("serial")
public class PropertyValueScript implements PropertyValue<NuclosScript> {

	private NuclosScript value;

	public PropertyValueScript() { }
	
	public PropertyValueScript(NuclosScript value) {
		this.value = value;
	}
	
	@Override
	public NuclosScript getValue() {
		return value;
	}

	@Override
	public void setValue(NuclosScript value) {
		this.value = value;
	}
	
	@Override
	public void setValue(String attributeName, Attributes attributes) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getValue(Class<?> cls, WYSIWYGComponent c) {
		if (cls != null && cls.equals(NuclosScript.class)) {
			return value;
		}
		return null;
	}

	@Override
	public TableCellEditor getTableCellEditor(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		return new PropertyEditorScript(c);
	}

	@Override
	public TableCellRenderer getTableCellRenderer(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		return new PropertyEditorScript(c);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof PropertyValueScript) {
			PropertyValueScript value = (PropertyValueScript)obj;
			
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

	@Override
	public Object clone() throws CloneNotSupportedException {
		return new PropertyValueScript(value);
	}
	
	public class PropertyEditorScript extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

		private JPanel panel = new JPanel();
		private WYSIWYGComponent c;
		
		public PropertyEditorScript(WYSIWYGComponent c) {
			this.c = c;
		}
		
		@Override
		public Object getCellEditorValue() {
			return PropertyValueScript.this;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			return getComponent(true);
		}

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
			if (editable) {
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						ScriptEditor editor = new ScriptEditor();
						if (getValue() != null) {
							editor.setScript(getValue());
						}
						editor.run();
						NuclosScript script = editor.getScript();
						if (org.nuclos.common2.StringUtils.isNullOrEmpty(script.getSource())) {
							script = null;
						}
						setValue(script);
						setLabel(label, script);
					}
				});
			}
			button.setPreferredSize(new Dimension(30,InterfaceGuidelines.CELL_BUTTON_MAXHEIGHT));
			
			panel.add(label, new TableLayoutConstraints(1,1));
			panel.add(button, new TableLayoutConstraints(2,1));
			return panel;
		}
		
		private void setLabel(JLabel label, NuclosScript script) {
			if (script != null) {
				Integer m = Math.min(30, script.getSource().length());
				label.setText(script.getSource().substring(0, m));
			}
			else {
				label.setText("Undefiniert.");
			}
		}
	}
}
