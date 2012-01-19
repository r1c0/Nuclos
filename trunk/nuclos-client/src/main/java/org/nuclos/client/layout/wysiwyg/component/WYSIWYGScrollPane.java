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

import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import org.nuclos.client.layout.wysiwyg.CollectableWYSIWYGLayoutEditor.WYSIWYGLayoutEditorChangeDescriptor;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.ERROR_MESSAGES;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.JSCROLLPANE;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PROPERTY_LABELS;
import org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
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
 * @author <a href="mailto:maik.stueker@novabit.de">maik.stueker</a>
 * @version 01.00.00
 */
public class WYSIWYGScrollPane extends JScrollPane implements WYSIWYGComponent {

	public static String PROPERTY_NAME = PROPERTY_LABELS.NAME;
	public static String PROPERTY_PREFFEREDSIZE = PROPERTY_LABELS.PREFFEREDSIZE;
	public static String PROPERTY_HORIZONTALSCROLLBAR = PROPERTY_LABELS.HORIZONTALSCROLLBAR;
	public static String PROPERTY_VERTICALSCROLLBAR = PROPERTY_LABELS.VERTICALSCROLLBAR;
	
	private final Map<String, Integer> mpHorizontalScrollBarPolicies = new HashMap<String, Integer>(3);
	{
		this.mpHorizontalScrollBarPolicies.put(JSCROLLPANE.LABEL_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		this.mpHorizontalScrollBarPolicies.put(JSCROLLPANE.LABEL_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.mpHorizontalScrollBarPolicies.put(JSCROLLPANE.LABEL_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}
	
	private final Map<String, Integer> mpVerticalScrollBarPolicies = new HashMap<String, Integer>(3);
	{
		this.mpVerticalScrollBarPolicies.put(JSCROLLPANE.LABEL_ALWAYS, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.mpVerticalScrollBarPolicies.put(JSCROLLPANE.LABEL_NEVER, JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		this.mpVerticalScrollBarPolicies.put(JSCROLLPANE.LABEL_AS_NEEDED, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	}

	public static final String[][] PROPERTIES_TO_LAYOUTML_ATTRIBUTES = new String[][]{
		{PROPERTY_NAME, ATTRIBUTE_NAME},
		{PROPERTY_HORIZONTALSCROLLBAR, ATTRIBUTE_HORIZONTALSCROLLBARPOLICY},
		{PROPERTY_VERTICALSCROLLBAR, ATTRIBUTE_VERTICALSCROLLBARPOLICY}
	};

	private static String[] PROPERTY_NAMES = new String[]{
		PROPERTY_NAME, 
		PROPERTY_HORIZONTALSCROLLBAR,
		PROPERTY_VERTICALSCROLLBAR,
		PROPERTY_PREFFEREDSIZE,
		PROPERTY_BORDER
	};

	private static PropertyClass[] PROPERTY_CLASSES = new PropertyClass[]{
		new PropertyClass(PROPERTY_NAME, String.class), 
		new PropertyClass(PROPERTY_HORIZONTALSCROLLBAR, String.class),
		new PropertyClass(PROPERTY_VERTICALSCROLLBAR, String.class),
		new PropertyClass(PROPERTY_PREFFEREDSIZE, Dimension.class),
		new PropertyClass(PROPERTY_BORDER, Border.class)
	};

	private static PropertySetMethod[] PROPERTY_SETMETHODS = new PropertySetMethod[]{
		new PropertySetMethod(PROPERTY_NAME, "setName"), 
		new PropertySetMethod(PROPERTY_PREFFEREDSIZE, "setPreferredSize"),
		new PropertySetMethod(PROPERTY_BORDER, "setBorder"),
		new PropertySetMethod(PROPERTY_HORIZONTALSCROLLBAR, "setVerticalScrollbar"),
		new PropertySetMethod(PROPERTY_VERTICALSCROLLBAR, "setHorizontalScrollbar")
	};
	
	private static PropertyFilter[] PROPERTY_FILTERS = new PropertyFilter[] {
		new PropertyFilter(PROPERTY_NAME, STANDARD_MODE | EXPERT_MODE), 
		new PropertyFilter(PROPERTY_HORIZONTALSCROLLBAR, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_VERTICALSCROLLBAR, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_PREFFEREDSIZE, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_BORDER, STANDARD_MODE | EXPERT_MODE)
	};
	
	public static final String[][] PROPERTY_VALUES_STATIC = new String[][] {
		{PROPERTY_HORIZONTALSCROLLBAR, ATTRIBUTEVALUE_ALWAYS, aTTRIBUTEVALUE_NEVER, ATTRIBUTEVALUE_ASNEEDED},
		{PROPERTY_VERTICALSCROLLBAR, ATTRIBUTEVALUE_ALWAYS, aTTRIBUTEVALUE_NEVER, ATTRIBUTEVALUE_ASNEEDED}
	};

	private ComponentProperties properties;

	private WYSIWYGLayoutEditorPanel wysiwygLayoutEditorPanel = null;
	
	private WYSIWYGLayoutEditorChangeDescriptor wysiwygLayoutEditorChangeDescriptor = null;

	@Override
	public void setViewportView(Component view) {
		super.setViewportView(view);
		this.wysiwygLayoutEditorPanel = (WYSIWYGLayoutEditorPanel) view;
		this.wysiwygLayoutEditorPanel.getTableLayoutPanel().setEditorChangeDescriptor(this.wysiwygLayoutEditorChangeDescriptor);
	}

	public void setWYSIWYGLayoutEditorChangeDescriptor(WYSIWYGLayoutEditorChangeDescriptor wysiwygLayoutEditorChangeDescriptor) {
		this.wysiwygLayoutEditorChangeDescriptor = wysiwygLayoutEditorChangeDescriptor;
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
	public void setProperty(String property, PropertyValue<?> value, Class<?> valueClass) throws CommonBusinessException {
		properties.setProperty(property, value, valueClass);
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
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyClasses()
	 */
	@Override
	public PropertyClass[] getPropertyClasses() {
		return PROPERTY_CLASSES;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getParentEditor()
	 */
	@Override
	public WYSIWYGLayoutEditorPanel getParentEditor() {
		if (getTabbedPane() != null) {
			return getTabbedPane().getParentEditor();
		} else {
			if (super.getParent() instanceof TableLayoutPanel) {
				return (WYSIWYGLayoutEditorPanel) super.getParent().getParent();
			}
		}

		throw new CommonFatalException(ERROR_MESSAGES.PARENT_NO_WYSIWYG);
	}

	/**
	 * 
	 * @return If Editor is placed in a WYSIWYGTabbedPane the WYSIWYGTabbedPane
	 *         would be returned. Otherwise return null.
	 */
	public WYSIWYGTabbedPane getTabbedPane() {
		if (super.getParent() instanceof WYSIWYGTabbedPane) {
			return (WYSIWYGTabbedPane) super.getParent();
		}
		return null;
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
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getAdditionalContextMenuItems(int)
	 */
	@Override
	public List<JMenuItem> getAdditionalContextMenuItems(int xClick) {
		return null;
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
		return PROPERTY_VALUES_STATIC;
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
	
	public void setVerticalScrollbar(String value) {
		if (value != null) {
			if (ATTRIBUTEVALUE_ASNEEDED.equals(value))
				this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			else if (ATTRIBUTEVALUE_ALWAYS.equals(value))
				this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			else if (aTTRIBUTEVALUE_NEVER.equals(value))
				this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		} else {
			this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		}
	}

	public void setHorizontalScrollbar(String value) {
		if (value != null) {
			if (ATTRIBUTEVALUE_ASNEEDED.equals(value))
				this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			else if (ATTRIBUTEVALUE_ALWAYS.equals(value))
				this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			else if (aTTRIBUTEVALUE_NEVER.equals(value))
				this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		} else {
			this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		}
	}	
}
