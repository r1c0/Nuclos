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
package org.nuclos.client.layout.wysiwyg.component;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.border.Border;

import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.ERROR_MESSAGES;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PROPERTY_LABELS;
import org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.PropertiesSorter;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.TableLayoutPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRules;
import org.nuclos.client.ui.labeled.LabeledTextArea;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.layoutml.LayoutMLConstants;

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
 * 
 * NUCLEUSINT-317
 * now the WYSIWYGStaticTextarea extends LabeledTextArea for scrollbar
 * 
 */
@SuppressWarnings("serial")
public class WYSIWYGStaticTextarea extends LabeledTextArea implements WYSIWYGComponent, LayoutMLConstants  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String PROPERTY_NAME = PROPERTY_LABELS.NAME;
	public static final String PROPERTY_EDITABLE = PROPERTY_LABELS.EDITABLE;
	
	public static final String[][] PROPERTIES_TO_LAYOUTML_ATTRIBUTES = new String[][] {
		{PROPERTY_ENABLED, ATTRIBUTE_ENABLED},
		{PROPERTY_NAME, ATTRIBUTE_NAME},
		{PROPERTY_EDITABLE, ATTRIBUTE_EDITABLE},
		{PROPERTY_COLUMNS, ATTRIBUTE_COLUMNS},
		{PROPERTY_ROWS, ATTRIBUTE_ROWS}
	};

	private static String[] PROPERTY_NAMES = new String[] {
		PROPERTY_NAME,
		PROPERTY_ENABLED,
		PROPERTY_EDITABLE,
		PROPERTY_COLUMNS, 
		PROPERTY_ROWS,
		PROPERTY_FONT,
		PROPERTY_BORDER,
		PROPERTY_DESCRIPTION,
		PROPERTY_PREFFEREDSIZE
	};
	
	private static PropertyClass[] PROPERTY_CLASSES = new PropertyClass[] {
		new PropertyClass(PROPERTY_NAME, String.class),
		new PropertyClass(PROPERTY_ENABLED, boolean.class),
		new PropertyClass(PROPERTY_EDITABLE, boolean.class),
		new PropertyClass(PROPERTY_COLUMNS, int.class),
		new PropertyClass(PROPERTY_FONT, Font.class),
		new PropertyClass(PROPERTY_ROWS, int.class),
		new PropertyClass(PROPERTY_BORDER, Border.class),
		new PropertyClass(PROPERTY_DESCRIPTION, String.class),
		new PropertyClass(PROPERTY_PREFFEREDSIZE, Dimension.class)
	};
	
	private static PropertySetMethod[] PROPERTY_SETMETHODS = new PropertySetMethod[]{
		new PropertySetMethod(PROPERTY_NAME, "setName"), 
		new PropertySetMethod(PROPERTY_NAME,"setText"),
		new PropertySetMethod(PROPERTY_ENABLED, "setEnabled"), 
		new PropertySetMethod(PROPERTY_EDITABLE, "setEditable"), 
		new PropertySetMethod(PROPERTY_COLUMNS, "setColumns"),
		new PropertySetMethod(PROPERTY_ROWS, "setRows"),
		new PropertySetMethod(PROPERTY_FONT, "setFont"),
		new PropertySetMethod(PROPERTY_BORDER, "setBorder"), 
		new PropertySetMethod(PROPERTY_DESCRIPTION, "setToolTipText"),
		new PropertySetMethod(PROPERTY_PREFFEREDSIZE, "setPreferredSize")
	};
	
	private static PropertyFilter[] PROPERTY_FILTERS = new PropertyFilter[] {
		new PropertyFilter(PROPERTY_NAME, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_ENABLED, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_EDITABLE, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_COLUMNS, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_FONT, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_ROWS, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_BORDER, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_DESCRIPTION, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_PREFFEREDSIZE, STANDARD_MODE | EXPERT_MODE)
	};
	
	/**
	 * <!ELEMENT textarea
	 * ((%layoutconstraints;)?,(%borders;),(%sizes;),font?,description?)>
	 * <!ATTLIST textarea name CDATA #IMPLIED enabled (%boolean;) #IMPLIED
	 * editable (%boolean;) #IMPLIED rows CDATA #IMPLIED columns CDATA #IMPLIED
	 * >
	 */
	
	private ComponentProperties properties;
	
	public WYSIWYGStaticTextarea(){
		super.getJLabel().setVisible(false);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getAdditionalContextMenuItems(int)
	 */
	@Override
	public List<JMenuItem> getAdditionalContextMenuItems(int click) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getLayoutMLRulesIfCapable()
	 */
	@Override
	public LayoutMLRules getLayoutMLRulesIfCapable() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getParentEditor()
	 */
	@Override
	public WYSIWYGLayoutEditorPanel getParentEditor() {
		if (super.getParent() instanceof TableLayoutPanel){
			return (WYSIWYGLayoutEditorPanel) super.getParent().getParent();
		}
		
		throw new CommonFatalException(ERROR_MESSAGES.PARENT_NO_WYSIWYG);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getProperties()
	 */
	@Override
	public ComponentProperties getProperties() {
		return properties;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyAttributeLink()
	 */
	@Override
	public String[][] getPropertyAttributeLink() {
		return PROPERTIES_TO_LAYOUTML_ATTRIBUTES;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyClasses()
	 */
	@Override
	public PropertyClass[] getPropertyClasses() {
		return PROPERTY_CLASSES;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyNames()
	 */
	@Override
	public String[] getPropertyNames() {
		return PropertiesSorter.sortPropertyNames(PROPERTY_NAMES);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertySetMethods()
	 */
	@Override
	public PropertySetMethod[] getPropertySetMethods() {
		return PROPERTY_SETMETHODS;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyValuesFromMetaInformation()
	 */
	@Override
	public String[][] getPropertyValuesFromMetaInformation() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyValuesStatic()
	 */
	@Override
	public String[][] getPropertyValuesStatic() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#setProperties(org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties)
	 */
	@Override
	public void setProperties(ComponentProperties properties) {
		this.properties = properties;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#setProperty(java.lang.String, org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue, java.lang.Class)
	 */
	@Override
	public void setProperty(String property, PropertyValue value, Class<?> valueClass) throws CommonBusinessException {
		properties.setProperty(property, value, valueClass);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#validateProperties(java.util.Map)
	 */
	@Override
	public void validateProperties(Map<String, PropertyValue<Object>> values) throws NuclosBusinessException {};
	
	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyFilters()
	 */
	@Override
	public PropertyFilter[] getPropertyFilters() {
		return PROPERTY_FILTERS;
	}
	
	@Override
	public void setToolTipText(String description) {
		super.setToolTipText(description);
	}

	public void setText(String text) {
		super.getJTextArea().setText(text);
	}

	@Override
	public synchronized void addMouseListener(MouseListener l) {
		super.getJTextArea().addMouseListener(l);
	}
	
	
}
