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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.border.Border;

import org.nuclos.client.layout.wysiwyg.WYSIWYGEditorModes;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.ERROR_MESSAGES;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PROPERTY_LABELS;
import org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.DnDUtil;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.PropertiesSorter;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.TableLayoutPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRules;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;

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
public class WYSIWYGStaticComboBox extends JComboBox implements WYSIWYGComponent, WYSIWYGEditorModes {
	
	public static final String PROPERTY_NAME = PROPERTY_LABELS.NAME;
	public static final String PROPERTY_ENABLED = PROPERTY_LABELS.ENABLED;
	public static final String PROPERTY_EDITABLE = PROPERTY_LABELS.EDITABLE;
	public static String PROPERTY_BORDER = PROPERTY_LABELS.BORDER;

	public static final String[][] PROPERTIES_TO_LAYOUTML_ATTRIBUTES = new String[][]{
		{PROPERTY_NAME, ATTRIBUTE_NAME}, 
		{PROPERTY_ENABLED, ATTRIBUTE_ENABLED}, 
		{PROPERTY_EDITABLE, ATTRIBUTE_EDITABLE}
		};

	private static String[] PROPERTY_NAMES = new String[]{
		PROPERTY_NAME,
		PROPERTY_ENABLED,
		PROPERTY_EDITABLE,
		PROPERTY_FONT,
		PROPERTY_BORDER,
		PROPERTY_DESCRIPTION,
		PROPERTY_PREFFEREDSIZE};

	private static PropertyClass[] PROPERTY_CLASSES = new PropertyClass[]{
		new PropertyClass(PROPERTY_BORDER, Border.class),
		new PropertyClass(PROPERTY_NAME, String.class),
		new PropertyClass(PROPERTY_ENABLED, boolean.class),
		new PropertyClass(PROPERTY_EDITABLE, boolean.class),
		new PropertyClass(PROPERTY_FONT, Font.class),
		new PropertyClass(PROPERTY_DESCRIPTION, String.class), 
		new PropertyClass(PROPERTY_PREFFEREDSIZE, Dimension.class)};

	private static PropertySetMethod[] PROPERTY_SETMETHODS = new PropertySetMethod[]{
		new PropertySetMethod(PROPERTY_NAME, "setName"), 
		new PropertySetMethod(PROPERTY_NAME, "addNameAsItem"), 
		new PropertySetMethod(PROPERTY_ENABLED, "setEnabled"), 
		new PropertySetMethod(PROPERTY_EDITABLE, "setEditable"), 
		new PropertySetMethod(PROPERTY_BORDER, "setBorder"), 
		new PropertySetMethod(PROPERTY_FONT, "setFont"),
		new PropertySetMethod(PROPERTY_DESCRIPTION, "setToolTipText"),
		new PropertySetMethod(PROPERTY_PREFFEREDSIZE, "setPreferredSize")
	};
	
	private static PropertyFilter[] PROPERTY_FILTERS = new PropertyFilter[] {
		new PropertyFilter(PROPERTY_BORDER, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_NAME, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_ENABLED, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_EDITABLE, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_FONT, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_DESCRIPTION, STANDARD_MODE | EXPERT_MODE), 
		new PropertyFilter(PROPERTY_PREFFEREDSIZE, STANDARD_MODE | EXPERT_MODE)
	};
	
	/**
	 * <!ELEMENT combobox
	 * ((%layoutconstraints;)?,(%borders;),(%sizes;),font?,description?)>
	 * <!ATTLIST combobox name CDATA #IMPLIED enabled (%boolean;) #IMPLIED
	 * editable (%boolean;) #REQUIRED >
	 */

	private LayoutMLRules componentsRules = new LayoutMLRules();
	
	private ComponentProperties properties;
	
	public WYSIWYGStaticComboBox() {
	    DnDUtil.addDragGestureListener(this);
	}
		
	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getAdditionalContextMenuItems(int)
	 */
	@Override
	public List<JMenuItem> getAdditionalContextMenuItems(int click) {
		return null;
	}

	@Override
	public synchronized void addMouseListener(MouseListener l) {
		super.addMouseListener(l);
		
		Component[] comps = getComponents();
		
		for (int i = 0; i < comps.length; i++){
			comps[i].addMouseListener(l);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getLayoutMLRulesIfCapable()
	 */
	@Override
	public LayoutMLRules getLayoutMLRulesIfCapable() {
		return this.componentsRules;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getParentEditor()
	 */
	@Override
	public WYSIWYGLayoutEditorPanel getParentEditor() {
		if (super.getParent() instanceof TableLayoutPanel) {
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
	
	public void addNameAsItem(String name){
		this.removeAllItems();
		this.addItem(name);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#setProperty(java.lang.String, org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue, java.lang.Class)
	 */
	@Override
	public void setProperty(String property, PropertyValue<?> value, Class<?> valueClass) throws CommonBusinessException {
		properties.setProperty(property, value, valueClass);
	}

	/**
	 * This Method draws a small red box on the Component to indicate existing {@link LayoutMLRules}
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (this.componentsRules.getSize() > 0) {
			Graphics2D g2d = (Graphics2D) g;

			g2d.setColor(Color.RED);
			g2d.fillRect(this.getWidth() - 10, 0, 10, 10);
		}
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
}
