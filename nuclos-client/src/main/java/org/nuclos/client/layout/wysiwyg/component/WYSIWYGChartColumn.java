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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.layout.wysiwyg.WYSIWYGEditorModes;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PROPERTY_LABELS;
import org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyUtils;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueBoolean;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueString;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueValuelistProvider;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGValuelistProvider;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRules;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.DefaultComponentTypes;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.collect.collectable.CollectableComponentTypes;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * 
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:stefan.geiling@novabit.de">stefan.geiling</a>
 * @version 01.00.00
 */
public class WYSIWYGChartColumn extends JLabel implements WYSIWYGComponent, Serializable, WYSIWYGEditorModes {
	
	private static final Logger LOG = Logger.getLogger(WYSIWYGChartColumn.class);
	
	public static final String PROPERTY_NAME = PROPERTY_LABELS.NAME;
	public static final String PROPERTY_LABEL = PROPERTY_LABELS.LABEL;
	public static final String PROPERTY_DEFAULTVALUES = PROPERTY_LABELS.DEFAULTVALUES;
	
	public static final String[][] PROPERTIES_TO_LAYOUTML_ATTRIBUTES = new String[][] {
		{PROPERTY_NAME, ATTRIBUTE_NAME},
	    {PROPERTY_LABEL, ATTRIBUTE_LABEL}
	};
	
	private static final String[] PROPERTY_NAMES = new String[] {
		PROPERTY_NAME,
		PROPERTY_LABEL,
		PROPERTY_DEFAULTVALUES,
		PROPERTY_VALUELISTPROVIDER,
		PROPERTY_TRANSLATIONS
	};
	
	private static final PropertyClass[] PROPERTY_CLASSES = new PropertyClass[] {
		new PropertyClass(PROPERTY_NAME, String.class),
		new PropertyClass(PROPERTY_LABEL, String.class),
		new PropertyClass(PROPERTY_DEFAULTVALUES, boolean.class),
		new PropertyClass(PROPERTY_VALUELISTPROVIDER, WYSIWYGValuelistProvider.class),
		new PropertyClass(PROPERTY_TRANSLATIONS, TranslationMap.class)
	};
	
	private static PropertyFilter[] PROPERTY_FILTERS = new PropertyFilter[] {
		new PropertyFilter(PROPERTY_NAME, 0),
		new PropertyFilter(PROPERTY_LABEL, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_ENABLED, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_VALUELISTPROVIDER, EXPERT_MODE),
		new PropertyFilter(PROPERTY_DEFAULTVALUES, 0),
		new PropertyFilter(PROPERTY_TRANSLATIONS, STANDARD_MODE | EXPERT_MODE)
	};
	
	private static final PropertySetMethod[] PROPERTY_SETMETHODS = new PropertySetMethod[]{
		new PropertySetMethod(PROPERTY_LABEL, "setLabel")
	};
	
	public static final String[][] PROPERTY_VALUES_FROM_META = new String[][] {};

	private ComponentProperties properties;
	
	private WYSIWYGChart chart;
	
	private CollectableEntityField field;
	
	public WYSIWYGChartColumn(WYSIWYGChart chart, CollectableEntityField field) {
		this.chart = chart;
		this.field = field;
	}
	
	public void setLabel(String label) {
		this.chart.setChartFromProperties();
	}
	
	public WYSIWYGChart getChart(){
		return chart;
	}
	
	public void setPropertiesFromEntityField() {
		for (String s : getPropertyNames()) {
			try {
				getProperties().setProperty(s, getDefaultPropertyValue(s), PropertyUtils.getValueClass(this, s));
			} catch (CommonBusinessException e) {
				Errors.getInstance().showExceptionDialog(this.getParentEditor(), e);
			}
		}
	}
	
	public PropertyValue<?> getDefaultPropertyValue(String p) {
		if (p.equals(PROPERTY_LABEL)) {
			return new PropertyValueString(field.getLabel());
		} else if (p.equals(PROPERTY_DEFAULTVALUES)) {
			return new PropertyValueBoolean(true);
		}  else if (p.equals(PROPERTY_VALUELISTPROVIDER)) {
			return new PropertyValueValuelistProvider(false);
		}
		return PropertyUtils.getPropertyValue(this, p);
	}
	
	private int getDefaultCollectableComponentType() {
		try {
			EntityFieldMetaDataVO efMeta = MetaDataClientProvider.getInstance().getEntityField(chart.getEntityName(), field.getName());
			if (StringUtils.equalsIgnoreCase(efMeta.getDefaultComponentType(), DefaultComponentTypes.HYPERLINK)) {
				return CollectableComponentTypes.TYPE_HYPERLINK;
			}
			if (StringUtils.equalsIgnoreCase(efMeta.getDefaultComponentType(), DefaultComponentTypes.EMAIL)) {
				return CollectableComponentTypes.TYPE_EMAIL;
			}
		} catch (Exception ex) {
			LOG.error(ex.getMessage(), ex);
		}
		return field.getDefaultCollectableComponentType();
	}
	
	public CollectableEntityField getEntityField() {
		return field;
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
		return PROPERTY_NAMES;
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
	public WYSIWYGLayoutEditorPanel getParentEditor(){
		return getChart().getParentEditor();
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
		return PROPERTY_VALUES_FROM_META;
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
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getLayoutMLRulesIfCapable()
	 */
	@Override
	public LayoutMLRules getLayoutMLRulesIfCapable() {
		return new LayoutMLRules();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#validateProperties(java.util.Map)
	 */
	@Override
	public void validateProperties(Map<String, PropertyValue<Object>> values) throws NuclosBusinessException {
	};

	public CollectableEntityField getCollectableEntityField() {
		return field;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyFilters()
	 */
	@Override
	public PropertyFilter[] getPropertyFilters() {
		return PROPERTY_FILTERS;
	}

	@Override
	public String getName() {
		if (properties != null) {
			return (String)properties.getProperty(PROPERTY_NAME).getValue();
		}
		else {
			return "";
		}
	}
}
