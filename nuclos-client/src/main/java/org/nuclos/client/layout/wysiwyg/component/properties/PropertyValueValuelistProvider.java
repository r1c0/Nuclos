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

import org.nuclos.common.NuclosEOField;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.layoutml.LayoutMLConstants;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.VALUELIST_PROVIDER_EDITOR;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.valuelistprovidereditor.ValueListProviderEditor;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGValuelistProvider;

/**
 * This class is for Editing {@link WYSIWYGValuelistProvider}.<br>
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class PropertyValueValuelistProvider implements PropertyValue<WYSIWYGValuelistProvider>, LayoutMLConstants {

	private WYSIWYGValuelistProvider wysiwygStaticValuelistProvider = null;
	//NUCLEUSINT-811 need to know what component the vp is for
	private WYSIWYGComponent c = null;

	public PropertyValueValuelistProvider() {
		this.wysiwygStaticValuelistProvider = new WYSIWYGValuelistProvider();
	}

	@Override
	public TableCellEditor getTableCellEditor(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		//NUCLEUSINT-811
		this.c = c;
		return new PropertyEditorValuelistProvider();
	}

	@Override
	public TableCellRenderer getTableCellRenderer(WYSIWYGComponent c, String property, PropertiesPanel dialog) {
		//NUCLEUSINT-811
		this.c = c;
		return new PropertyEditorValuelistProvider();
	}

	@Override
	public WYSIWYGValuelistProvider getValue() {
		return wysiwygStaticValuelistProvider;
	}

	@Override
	public Object getValue(Class<?> cls, WYSIWYGComponent c) {
		return null;
	}

	@Override
	public void setValue(WYSIWYGValuelistProvider value) {
		this.wysiwygStaticValuelistProvider = value;
	}

	@Override
	public void setValue(String attributeName, Attributes attributes) {
		String attributeType = attributes.getValue(ATTRIBUTE_TYPE);
		this.wysiwygStaticValuelistProvider = new WYSIWYGValuelistProvider(attributeType);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		if (wysiwygStaticValuelistProvider == null)
			return null;
		PropertyValueValuelistProvider clonedValuelistProviderProperty = new PropertyValueValuelistProvider();
		clonedValuelistProviderProperty.setValue((WYSIWYGValuelistProvider) wysiwygStaticValuelistProvider.clone());
		return clonedValuelistProviderProperty;
	}
	class PropertyEditorValuelistProvider extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

		private JLabel valuelistprovider = null;

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			return getComponent(isEditable());
		}
		
		private boolean isEditable() {
			boolean editable = true;
			PropertyValue<?> pv = c.getProperties().getProperty(org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableComponent.PROPERTY_NAME);
			if (pv != null && NuclosEOField.PROCESS.getName().equals(pv.getValue())) {
				editable = false;
			}
			
			return editable;
		}

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

			valuelistprovider = new JLabel();
			if (wysiwygStaticValuelistProvider != null && !StringUtils.isNullOrEmpty(wysiwygStaticValuelistProvider.getType()))
				valuelistprovider.setText(wysiwygStaticValuelistProvider.getType());
			else
				valuelistprovider.setText(VALUELIST_PROVIDER_EDITOR.NO_VALUELISTPROVIDER_DEFINED);

			TableLayoutConstraints constraint = new TableLayoutConstraints(1, 1, 1, 1, TableLayout.FULL, TableLayout.CENTER);
			panel.add(valuelistprovider, constraint);
			
			if (editable) {
				JButton launchEditor = new JButton("...");
				launchEditor.setPreferredSize(new Dimension(30, InterfaceGuidelines.CELL_BUTTON_MAXHEIGHT));
				launchEditor.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						//NUCLEUSINT-811
						launchEditor(c);
	 				}
				});
				constraint = new TableLayoutConstraints(2, 1);
				panel.add(launchEditor, constraint);
			} else {
				valuelistprovider.setForeground(Color.GRAY);
			}
			return panel;
		}
		//NUCLEUSINT-811
		private final void launchEditor(WYSIWYGComponent component){
			if (getValue() == null) {
				setValue(new WYSIWYGValuelistProvider());
			}
			//NUCLEUSINT-811
			WYSIWYGValuelistProvider returnStaticValuelistProvider = ValueListProviderEditor.showEditor(component, wysiwygStaticValuelistProvider);
			wysiwygStaticValuelistProvider = returnStaticValuelistProvider;
			
			if (wysiwygStaticValuelistProvider != null && !StringUtils.isNullOrEmpty(wysiwygStaticValuelistProvider.getType()))
				valuelistprovider.setText(wysiwygStaticValuelistProvider.getType());
			else
				valuelistprovider.setText(VALUELIST_PROVIDER_EDITOR.NO_VALUELISTPROVIDER_DEFINED);
		}

		@Override
		public Object getCellEditorValue() {
			return PropertyValueValuelistProvider.this;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			return getComponent(isEditable());
		}

	}
}
