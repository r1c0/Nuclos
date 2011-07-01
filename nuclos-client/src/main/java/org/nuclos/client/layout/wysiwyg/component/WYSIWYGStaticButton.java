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
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.border.Border;

import org.nuclos.client.layout.wysiwyg.WYSIWYGEditorModes;
import org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.ERROR_MESSAGES;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PROPERTY_LABELS;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.STATIC_BUTTON;
import org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.PropertiesSorter;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.TableLayoutPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGProperty;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRules;
import org.nuclos.client.ui.collect.CollectActionAdapter;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common2.StringUtils;
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
@SuppressWarnings("serial")
public class WYSIWYGStaticButton extends JButton implements WYSIWYGComponent, WYSIWYGEditorModes {

	public static final String PROPERTY_NAME = PROPERTY_LABELS.NAME;
	public static final String PROPERTY_ACTIONCOMMAND = PROPERTY_LABELS.ACTIONCOMMAND;
	//NUCLEUSINT-1159
	public static final String PROPERTY_PROPERTIES = PROPERTY_LABELS.COLLECTABLECOMPONENTPROPERTY;
	public static final String PROPERTY_LABEL= PROPERTY_LABELS.LABEL;
	public static final String PROPERTY_TOOLTIP = PROPERTY_LABELS.TOOLTIP;
	public static String PROPERTY_ENABLED = PROPERTY_LABELS.ENABLED;
	//NUCLOSINT-743
	public static String PROPERTY_RULE = PROPERTY_LABELS.RULE;

	public static final String[][] PROPERTIES_TO_LAYOUTML_ATTRIBUTES = new String[][]{
		{PROPERTY_NAME, ATTRIBUTE_NAME}, 
		{PROPERTY_ACTIONCOMMAND, ATTRIBUTE_ACTIONCOMMAND}, 
		{PROPERTY_LABEL, ATTRIBUTE_LABEL},
		{PROPERTY_TOOLTIP, ATTRIBUTE_TOOLTIP},
		{PROPERTY_ENABLED, ATTRIBUTE_ENABLED},
		};

	private static String[] PROPERTY_NAMES = new String[]{
		PROPERTY_NAME,
		PROPERTY_ACTIONCOMMAND,
		PROPERTY_LABEL,
		PROPERTY_TOOLTIP, 
		PROPERTY_ENABLED,
		PROPERTY_BORDER,
		PROPERTY_DESCRIPTION,
		PROPERTY_FONT,
		PROPERTY_PREFFEREDSIZE,
		PROPERTY_TRANSLATIONS,
		//NUCLEUSINT-1159
		PROPERTY_PROPERTIES	,
		//NUCLOSINT-743
		PROPERTY_RULE
	};

	private static PropertyClass[] PROPERTY_CLASSES = new PropertyClass[]{
		new PropertyClass(PROPERTY_BORDER, Border.class),
		new PropertyClass(PROPERTY_NAME, String.class),
		new PropertyClass(PROPERTY_ENABLED, boolean.class),
		new PropertyClass(PROPERTY_TOOLTIP, String.class),
		new PropertyClass(PROPERTY_LABEL, String.class), 
		new PropertyClass(PROPERTY_ACTIONCOMMAND, String.class), 
		new PropertyClass(PROPERTY_DESCRIPTION, String.class), 
		new PropertyClass(PROPERTY_FONT, Font.class),
		new PropertyClass(PROPERTY_PREFFEREDSIZE, Dimension.class),
		new PropertyClass(PROPERTY_TRANSLATIONS, TranslationMap.class),
		//NUCLEUSINT-1159
		new PropertyClass(PROPERTY_PROPERTIES, WYSIYWYGProperty.class),
		//NUCLOSINT-743
		new PropertyClass(PROPERTY_RULE, String.class)
		};

	private static PropertySetMethod[] PROPERTY_SETMETHODS = new PropertySetMethod[]{
		new PropertySetMethod(PROPERTY_NAME, "setName"), 
		new PropertySetMethod(PROPERTY_LABEL, "setText"), 
		new PropertySetMethod(PROPERTY_ENABLED, "setEnabled"), 
		new PropertySetMethod(PROPERTY_TOOLTIP, "setToolTipText"), 
		new PropertySetMethod(PROPERTY_DESCRIPTION, "setToolTipText"), 
		new PropertySetMethod(PROPERTY_BORDER, "setBorder"), 
		new PropertySetMethod(PROPERTY_FONT, "setFont"),
		new PropertySetMethod(PROPERTY_PREFFEREDSIZE, "setPreferredSize")
	};
	
	private static PropertyFilter[] PROPERTY_FILTERS = new PropertyFilter[] {
		new PropertyFilter(PROPERTY_BORDER, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_NAME, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_ENABLED, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_TOOLTIP, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_LABEL, STANDARD_MODE | EXPERT_MODE), 
		new PropertyFilter(PROPERTY_ACTIONCOMMAND, STANDARD_MODE | EXPERT_MODE), 
		new PropertyFilter(PROPERTY_DESCRIPTION, STANDARD_MODE | EXPERT_MODE), 
		new PropertyFilter(PROPERTY_FONT, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_PREFFEREDSIZE, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_TRANSLATIONS, STANDARD_MODE),
		//NUCLEUSINT-1159
		new PropertyFilter(PROPERTY_PROPERTIES, EXPERT_MODE),
		////NUCLOSINT-743
		new PropertyFilter(PROPERTY_RULE, EXPERT_MODE)
	};
	
	//NUCLEUSINT-1159
    public static final String[][] PROPERTY_VALUES_STATIC = new String[][] {
      {PROPERTY_ACTIONCOMMAND, STATIC_BUTTON.DUMMY_BUTTON_ACTION_LABEL, STATIC_BUTTON.STATE_CHANGE_ACTION_LABEL, STATIC_BUTTON.EXECUTE_RULE_ACTION_LABEL}
    };
    
    ////NUCLOSINT-743 get the Business Rules for the entity
	public static final String[][] PROPERTY_VALUES_FROM_METAINFORMATION = new String[][] {
		{PROPERTY_RULE, WYSIWYGMetaInformation.META_RULES}
	};
	
	/**
	 * <!ELEMENT button
	 * ((%layoutconstraints;)?,(%borders;),(%sizes;),font?,description?)>
	 * <!ATTLIST button name CDATA #IMPLIED actioncommand CDATA #REQUIRED label
	 * CDATA #IMPLIED tooltip CDATA #IMPLIED enabled (%boolean;) #IMPLIED >
	 */

	private ComponentProperties properties;
	
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
	 * NUCLOSINT-743
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyValuesFromMetaInformation()
	 */
	@Override
	public String[][] getPropertyValuesFromMetaInformation() {
		return PROPERTY_VALUES_FROM_METAINFORMATION;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyValuesStatic()
	 */
	@Override
	public String[][] getPropertyValuesStatic() {
		//NUCLEUSINT-1159
		return PROPERTY_VALUES_STATIC;
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
	@SuppressWarnings("unchecked")
	public void setProperty(String property, PropertyValue value, Class<?> valueClass) throws CommonBusinessException {
		//NUCLEUSINT-1159
		//NUCLOSINT-743
		if (PROPERTY_LABELS.ACTIONCOMMAND.equals(property)) {
			if (STATIC_BUTTON.DUMMY_BUTTON_ACTION.equals(value.getValue()))
				value.setValue(STATIC_BUTTON.DUMMY_BUTTON_ACTION_LABEL);
			else if (STATIC_BUTTON.STATE_CHANGE_ACTION.equals(value.getValue()))
				value.setValue(STATIC_BUTTON.STATE_CHANGE_ACTION_LABEL);
			else if (STATIC_BUTTON.EXECUTE_RULE_ACTION.equals(value.getValue()))
				value.setValue(STATIC_BUTTON.EXECUTE_RULE_ACTION_LABEL);
		}
		
		properties.setProperty(property, value, valueClass);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#validateProperties(java.util.Map)
	 */
	@Override
	public void validateProperties(Map<String, PropertyValue<Object>> values) throws NuclosBusinessException {
		
		//FIX NUCLEUSINT-255
		PropertyValue<?> actionCommandPropertyValue = values.get(PROPERTY_ACTIONCOMMAND);
		if (actionCommandPropertyValue != null) {
			String actionCommand = (String) actionCommandPropertyValue.getValue();
			if (StringUtils.isNullOrEmpty(actionCommand)) {
				throw new NuclosBusinessException(STATIC_BUTTON.EXCEPTION_AC_ISNULL);
			}
			//NUCLEUSINT-1159
			//NUCLOSINT-743
			if (STATIC_BUTTON.DUMMY_BUTTON_ACTION_LABEL.equals(actionCommand))
				actionCommand = STATIC_BUTTON.DUMMY_BUTTON_ACTION;
			else if (STATIC_BUTTON.STATE_CHANGE_ACTION_LABEL.equals(actionCommand))
				actionCommand = STATIC_BUTTON.STATE_CHANGE_ACTION;
			else if (STATIC_BUTTON.EXECUTE_RULE_ACTION_LABEL.equals(actionCommand))
				actionCommand = STATIC_BUTTON.EXECUTE_RULE_ACTION;
			
			try {
				Class<?> actionClass = Class.forName(actionCommand);

				boolean implementsCollectActionAdapter = false;
				for (Class<?> interfaceClass : actionClass.getInterfaces()) {
					if (interfaceClass.equals(CollectActionAdapter.class))
						implementsCollectActionAdapter = true;
				}
				
				if (!implementsCollectActionAdapter) {
					throw new NuclosBusinessException(STATIC_BUTTON.EXCEPTION_AC_WRONG_TYPE);
				}
			} catch (ClassNotFoundException e) {
				throw new NuclosBusinessException(STATIC_BUTTON.EXCEPTION_AC_NOTFOUND);
			}
		}
	};
	
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
