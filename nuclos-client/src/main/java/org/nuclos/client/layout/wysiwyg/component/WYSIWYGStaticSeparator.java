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
import java.util.List;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.nuclos.client.layout.wysiwyg.WYSIWYGEditorModes;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.ERROR_MESSAGES;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PROPERTY_LABELS;
import org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.DnDUtil;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
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
public class WYSIWYGStaticSeparator extends JSeparator implements WYSIWYGComponent, WYSIWYGEditorModes {

	private ComponentProperties properties;
	
	public static String PROPERTY_ORIENTATION = PROPERTY_LABELS.ORIENTATION;
	
	public static final String[][] PROPERTIES_TO_LAYOUTML_ATTRIBUTES = new String[][]{
		{PROPERTY_ORIENTATION,ATTRIBUTE_ORIENTATION}
	};
	
	private static String[] PROPERTY_NAMES = new String[]{
		PROPERTY_ORIENTATION, 
		PROPERTY_PREFFEREDSIZE
	};

	private static PropertyClass[] PROPERTY_CLASSES = new PropertyClass[]{
		new PropertyClass(PROPERTY_ORIENTATION, String.class),
		new PropertyClass(PROPERTY_PREFFEREDSIZE, Dimension.class)
	};
	
	public static final String[][] PROPERTY_VALUES_STATIC = new String[][] {
		{PROPERTY_ORIENTATION, ATTRIBUTEVALUE_HORIZONTAL, ATTRIBUTEVALUE_VERTICAL}
	};
	
	private static PropertySetMethod[] PROPERTY_SETMETHODS = new PropertySetMethod[]{
		new PropertySetMethod(PROPERTY_ORIENTATION, "setOrientation"), 
		new PropertySetMethod(PROPERTY_PREFFEREDSIZE, "setPreferredSize")
	};
	
	private static PropertyFilter[] PROPERTY_FILTERS = new PropertyFilter[] {
		new PropertyFilter(PROPERTY_ORIENTATION, ENABLED), 
		new PropertyFilter(PROPERTY_PREFFEREDSIZE, ENABLED)
	};
	
	public WYSIWYGStaticSeparator() {
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
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#validateProperties(java.util.Map)
	 */
	@Override
	public void validateProperties(Map<String, PropertyValue<Object>> values) throws NuclosBusinessException {
		String orientation = (String)values.get(PROPERTY_ORIENTATION).getValue();
		
		Dimension preferredsize = (Dimension)values.get(PROPERTY_PREFFEREDSIZE).getValue();
		
		if (ATTRIBUTEVALUE_VERTICAL.equals(orientation)) {
			if (preferredsize != null) {
				if (preferredsize.height < InterfaceGuidelines.SEPARATOR_MIN_LENGTH) {
					preferredsize.height = InterfaceGuidelines.SEPARATOR_MIN_LENGTH;
				}
				if (preferredsize.width < InterfaceGuidelines.SEPARATOR_MIN_WIDTH) {
					preferredsize.width = InterfaceGuidelines.SEPARATOR_MIN_WIDTH;
				}
			}
			else {
				preferredsize = new Dimension(InterfaceGuidelines.SEPARATOR_MIN_WIDTH, InterfaceGuidelines.SEPARATOR_MIN_LENGTH);				
			}
		}
		else {
			if (preferredsize != null) {
				if (preferredsize.width < InterfaceGuidelines.SEPARATOR_MIN_LENGTH) {
					preferredsize.width = InterfaceGuidelines.SEPARATOR_MIN_LENGTH;
				}
				if (preferredsize.height < InterfaceGuidelines.SEPARATOR_MIN_WIDTH) {
					preferredsize.height = InterfaceGuidelines.SEPARATOR_MIN_WIDTH;
				}
			}
			else {
				preferredsize = new Dimension(InterfaceGuidelines.SEPARATOR_MIN_LENGTH, InterfaceGuidelines.SEPARATOR_MIN_WIDTH);				
			}
		}
		
		values.get(PROPERTY_PREFFEREDSIZE).setValue(preferredsize);
		
	};
	
	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyFilters()
	 */
	@Override
	public PropertyFilter[] getPropertyFilters() {
		return PROPERTY_FILTERS;
	}

	public void setOrientation(String orientation) {
		if (orientation != null) {
			setOrientation(orientation.equals(ATTRIBUTEVALUE_VERTICAL)?SwingConstants.VERTICAL:SwingConstants.HORIZONTAL);
		}
	}
}
