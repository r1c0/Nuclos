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

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.border.Border;

import org.apache.log4j.Logger;
import org.nuclos.client.layout.wysiwyg.WYSIWYGEditorModes;
import org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.ERROR_MESSAGES;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PROPERTY_LABELS;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.STATIC_BUTTON;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent.PropertyClass;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent.PropertyFilter;
import org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueKeyStroke;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.DnDUtil;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.PropertiesSorter;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.TableLayoutPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGProperty;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRules;
import org.nuclos.client.resource.ResourceCache;
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
public class WYSIWYGStaticButton extends JButton implements WYSIWYGComponent, WYSIWYGEditorModes {

	private final static Logger LOG = Logger.getLogger(WYSIWYGStaticButton.class);

	public static final String PROPERTY_NAME = PROPERTY_LABELS.NAME;
	public static final String PROPERTY_ACTIONCOMMAND = PROPERTY_LABELS.ACTIONCOMMAND;
	public static final String PROPERTY_ACTIONCOMMAND_PROPERTIES = PROPERTY_LABELS.ACTIONCOMMAND_PROPERTIES;
	public static final String PROPERTY_DISABLE_DURING_EDIT = PROPERTY_LABELS.DISABLE_DURING_EDIT;
	//NUCLEUSINT-1159
	public static final String PROPERTY_PROPERTIES = PROPERTY_LABELS.COLLECTABLECOMPONENTPROPERTY;
	public static final String PROPERTY_LABEL= PROPERTY_LABELS.LABEL;
	public static final String PROPERTY_TOOLTIP = PROPERTY_LABELS.TOOLTIP;
	public static final String PROPERTY_ENABLED = PROPERTY_LABELS.ENABLED;
	public static final String PROPERTY_ICON = PROPERTY_LABELS.ICON;
	public static final String PROPERTY_ACTIONKEYSTROKE = PROPERTY_LABELS.ACTIONKEYSTROKE;
	public static final String PROPERTY_NEXTFOCUSCOMPONENT = PROPERTY_LABELS.NEXTFOCUSCOMPONENT;
	public static final String PROPERTY_NEXTFOCUSONACTION = PROPERTY_LABELS.NEXTFOCUSONACTION;
	
	public static final String[][] PROPERTIES_TO_LAYOUTML_ATTRIBUTES = new String[][]{
		{PROPERTY_NAME, ATTRIBUTE_NAME},
		{PROPERTY_ACTIONCOMMAND, ATTRIBUTE_ACTIONCOMMAND},
		{PROPERTY_ACTIONKEYSTROKE, ATTRIBUTE_ACTIONKEYSTROKE},
		{PROPERTY_DISABLE_DURING_EDIT, ATTRIBUTE_DISABLE_DURING_EDIT},
		{PROPERTY_LABEL, ATTRIBUTE_LABEL},
		{PROPERTY_TOOLTIP, ATTRIBUTE_TOOLTIP},
		{PROPERTY_ENABLED, ATTRIBUTE_ENABLED},
		{PROPERTY_ICON, ATTRIBUTE_ICON},
	    {PROPERTY_NEXTFOCUSCOMPONENT, ATTRIBUTE_NEXTFOCUSCOMPONENT},
	    {PROPERTY_NEXTFOCUSONACTION, ATTRIBUTE_NEXTFOCUSONACTION}
	};

	private static String[] PROPERTY_NAMES = new String[]{
		PROPERTY_NAME,
		PROPERTY_ACTIONCOMMAND,
		PROPERTY_ACTIONKEYSTROKE,
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
		PROPERTY_ACTIONCOMMAND_PROPERTIES,
		PROPERTY_DISABLE_DURING_EDIT,
		PROPERTY_ICON,
		PROPERTY_NEXTFOCUSCOMPONENT,
		PROPERTY_NEXTFOCUSONACTION
	};

	private static PropertyClass[] PROPERTY_CLASSES = new PropertyClass[]{
		new PropertyClass(PROPERTY_BORDER, Border.class),
		new PropertyClass(PROPERTY_NAME, String.class),
		new PropertyClass(PROPERTY_ENABLED, boolean.class),
		new PropertyClass(PROPERTY_TOOLTIP, String.class),
		new PropertyClass(PROPERTY_LABEL, String.class),
		new PropertyClass(PROPERTY_ACTIONCOMMAND, String.class),
		new PropertyClass(PROPERTY_ACTIONKEYSTROKE, PropertyValueKeyStroke.class),
		new PropertyClass(PROPERTY_DESCRIPTION, String.class),
		new PropertyClass(PROPERTY_FONT, Font.class),
		new PropertyClass(PROPERTY_PREFFEREDSIZE, Dimension.class),
		new PropertyClass(PROPERTY_TRANSLATIONS, TranslationMap.class),
		//NUCLEUSINT-1159
		new PropertyClass(PROPERTY_PROPERTIES, WYSIYWYGProperty.class),
		new PropertyClass(PROPERTY_ACTIONCOMMAND_PROPERTIES, String.class),
		new PropertyClass(PROPERTY_DISABLE_DURING_EDIT, boolean.class),
		new PropertyClass(PROPERTY_ICON, String.class),
		new PropertyClass(PROPERTY_NEXTFOCUSCOMPONENT, String.class),
		new PropertyClass(PROPERTY_NEXTFOCUSONACTION, boolean.class)
	};

	private static PropertySetMethod[] PROPERTY_SETMETHODS = new PropertySetMethod[]{
		new PropertySetMethod(PROPERTY_NAME, "setName"),
		new PropertySetMethod(PROPERTY_LABEL, "setText"),
		new PropertySetMethod(PROPERTY_ENABLED, "setEnabled"),
		new PropertySetMethod(PROPERTY_TOOLTIP, "setToolTipText"),
		new PropertySetMethod(PROPERTY_DESCRIPTION, "setToolTipText"),
		new PropertySetMethod(PROPERTY_BORDER, "setBorder"),
		new PropertySetMethod(PROPERTY_FONT, "setFont"),
		new PropertySetMethod(PROPERTY_PREFFEREDSIZE, "setPreferredSize"),
		new PropertySetMethod(PROPERTY_ICON, "setIcon")
	};

	private static PropertyFilter[] PROPERTY_FILTERS = new PropertyFilter[] {
		new PropertyFilter(PROPERTY_BORDER, ENABLED),
		new PropertyFilter(PROPERTY_NAME, ENABLED),
		new PropertyFilter(PROPERTY_ENABLED, ENABLED),
		new PropertyFilter(PROPERTY_TOOLTIP, ENABLED),
		new PropertyFilter(PROPERTY_LABEL, ENABLED),
		new PropertyFilter(PROPERTY_ACTIONCOMMAND, ENABLED),
		new PropertyFilter(PROPERTY_ACTIONKEYSTROKE, ENABLED),
		new PropertyFilter(PROPERTY_DESCRIPTION, ENABLED),
		new PropertyFilter(PROPERTY_FONT, ENABLED),
		new PropertyFilter(PROPERTY_PREFFEREDSIZE, ENABLED),
		new PropertyFilter(PROPERTY_TRANSLATIONS, ENABLED),
		//NUCLEUSINT-1159
		new PropertyFilter(PROPERTY_PROPERTIES, ENABLED),
		new PropertyFilter(PROPERTY_ACTIONCOMMAND_PROPERTIES, ENABLED),
		new PropertyFilter(PROPERTY_DISABLE_DURING_EDIT, ENABLED),
		new PropertyFilter(PROPERTY_ICON, ENABLED),
		new PropertyFilter(PROPERTY_NEXTFOCUSCOMPONENT, ENABLED),
		new PropertyFilter(PROPERTY_NEXTFOCUSONACTION, ENABLED)
	};

	//NUCLEUSINT-1159
    public static final String[][] PROPERTY_VALUES_STATIC = new String[][] {
      {PROPERTY_ACTIONCOMMAND, STATIC_BUTTON.DUMMY_BUTTON_ACTION_LABEL, STATIC_BUTTON.STATE_CHANGE_ACTION_LABEL, STATIC_BUTTON.EXECUTE_RULE_ACTION_LABEL, STATIC_BUTTON.GENERATOR_ACTION_LABEL}
    };

    ////NUCLOSINT-743 get the Business Rules for the entity
	public static final String[][] PROPERTY_VALUES_FROM_METAINFORMATION = new String[][] {
		{PROPERTY_ACTIONCOMMAND_PROPERTIES, WYSIWYGMetaInformation.META_ACTIONCOMMAND_PROPERTIES},
		{PROPERTY_ICON, WYSIWYGMetaInformation.META_ICONS},
		{PROPERTY_NEXTFOCUSCOMPONENT, WYSIWYGMetaInformation.META_FIELD_NAMES}
	};

	/**
	 * <!ELEMENT button
	 * ((%layoutconstraints;)?,(%borders;),(%sizes;),font?,description?)>
	 * <!ATTLIST button name CDATA #IMPLIED actioncommand CDATA #REQUIRED label
	 * CDATA #IMPLIED tooltip CDATA #IMPLIED enabled (%boolean;) #IMPLIED >
	 */

	private ComponentProperties properties;

	public WYSIWYGStaticButton() {
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
	public void setProperty(String property, PropertyValue<? extends Object> value, Class<?> valueClass) throws CommonBusinessException {
		final PropertyValue<String> pv = (PropertyValue<String>) value;
		//NUCLEUSINT-1159
		//NUCLOSINT-743
		if (PROPERTY_LABELS.ACTIONCOMMAND.equals(property)) {
			if (STATIC_BUTTON.DUMMY_BUTTON_ACTION.equals(value.getValue()))
				pv.setValue(STATIC_BUTTON.DUMMY_BUTTON_ACTION_LABEL);
			else if (STATIC_BUTTON.STATE_CHANGE_ACTION.equals(value.getValue()))
				pv.setValue(STATIC_BUTTON.STATE_CHANGE_ACTION_LABEL);
			else if (STATIC_BUTTON.EXECUTE_RULE_ACTION.equals(value.getValue()))
				pv.setValue(STATIC_BUTTON.EXECUTE_RULE_ACTION_LABEL);
			else if (STATIC_BUTTON.GENERATOR_ACTION.equals(value.getValue()))
				pv.setValue(STATIC_BUTTON.GENERATOR_ACTION_LABEL);
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
			else if (STATIC_BUTTON.GENERATOR_ACTION_LABEL.equals(actionCommand))
				actionCommand = STATIC_BUTTON.GENERATOR_ACTION;

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

	public void setIcon(String resource) {
		try {
			ImageIcon ico = ResourceCache.getInstance().getIconResource(resource);
			setIcon(ico);
		}
		catch (Exception ex) {
			LOG.warn("setIcon", ex);
		}
	}
}
