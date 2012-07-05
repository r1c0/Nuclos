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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.layout.wysiwyg.WYSIWYGEditorModes;
import org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PROPERTY_LABELS;
import org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyUtils;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueBoolean;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueInteger;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueString;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueValuelistProvider;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGValuelistProvider;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGProperty;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRules;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.DefaultComponentTypes;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.collect.collectable.CollectableComponentTypes;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
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
 */
public class WYSIWYGSubFormColumn extends JLabel implements WYSIWYGComponent, Serializable, WYSIWYGEditorModes {
	
	private static final Logger LOG = Logger.getLogger(WYSIWYGSubFormColumn.class);
	
	public static final String PROPERTY_NAME = PROPERTY_LABELS.NAME;
	public static final String PROPERTY_LABEL = PROPERTY_LABELS.LABEL;
	public static final String PROPERTY_VISIBLE = PROPERTY_LABELS.VISIBLE;
	public static final String PROPERTY_ENABLED = PROPERTY_LABELS.ENABLED;
	public static final String PROPERTY_INSERTABLE = PROPERTY_LABELS.INSERTABLE;
	public static final String PROPERTY_CONTROLTYPE = PROPERTY_LABELS.CONTROLTYPE;
	public static final String PROPERTY_CONTROLTYPECLASS = PROPERTY_LABELS.CONTROLTYPECLASS;
	public static final String PROPERTY_DEFAULTVALUES = PROPERTY_LABELS.DEFAULTVALUES;
	public static final String PROPERTY_COLUMNWIDTH = PROPERTY_LABELS.COLUMNWIDTH;
	public static final String PROPERTY_NEXTFOCUSCOMPONENT = PROPERTY_LABELS.NEXTFOCUSCOMPONENT;
	public static final String PROPERTY_COLLECTABLECOMPONENTPROPERTY = PROPERTY_LABELS.COLLECTABLECOMPONENTPROPERTY;
	
	private final Map<Integer, String> controlTypes = new HashMap<Integer, String>();
	{
		this.controlTypes.put(CollectableComponentTypes.TYPE_TEXTFIELD, ATTRIBUTEVALUE_TEXTFIELD);
		this.controlTypes.put(CollectableComponentTypes.TYPE_IDTEXTFIELD, ATTRIBUTEVALUE_IDTEXTFIELD);
		this.controlTypes.put(CollectableComponentTypes.TYPE_TEXTAREA, ATTRIBUTEVALUE_TEXTAREA);
		this.controlTypes.put(CollectableComponentTypes.TYPE_COMBOBOX, ATTRIBUTEVALUE_COMBOBOX);
		this.controlTypes.put(CollectableComponentTypes.TYPE_CHECKBOX, ATTRIBUTEVALUE_CHECKBOX);
		this.controlTypes.put(CollectableComponentTypes.TYPE_DATECHOOSER, ATTRIBUTEVALUE_DATECHOOSER);
		this.controlTypes.put(CollectableComponentTypes.TYPE_HYPERLINK, ATTRIBUTEVALUE_HYPERLINK);
		this.controlTypes.put(CollectableComponentTypes.TYPE_EMAIL, ATTRIBUTEVALUE_EMAIL);
		this.controlTypes.put(CollectableComponentTypes.TYPE_OPTIONGROUP, ATTRIBUTEVALUE_OPTIONGROUP);
		this.controlTypes.put(CollectableComponentTypes.TYPE_LISTOFVALUES, ATTRIBUTEVALUE_LISTOFVALUES);
		this.controlTypes.put(CollectableComponentTypes.TYPE_FILECHOOSER, ATTRIBUTEVALUE_FILECHOOSER);
	}
	
	public static final String[][] PROPERTIES_TO_LAYOUTML_ATTRIBUTES = new String[][] {
		{PROPERTY_NAME, ATTRIBUTE_NAME},
	    {PROPERTY_LABEL, ATTRIBUTE_LABEL},
	    {PROPERTY_ENABLED, ATTRIBUTE_ENABLED},
	    {PROPERTY_VISIBLE, ATTRIBUTE_VISIBLE},
	    {PROPERTY_INSERTABLE, ATTRIBUTE_INSERTABLE},
	    {PROPERTY_ROWS, ATTRIBUTE_ROWS},
	    {PROPERTY_COLUMNS, ATTRIBUTE_COLUMNS},
	    {PROPERTY_CONTROLTYPE, ATTRIBUTE_CONTROLTYPE},
	    {PROPERTY_CONTROLTYPECLASS, ATTRIBUTE_CONTROLTYPECLASS},
	    {PROPERTY_COLUMNWIDTH, ATTRIBUTE_COLUMNWIDTH},
	    {PROPERTY_NEXTFOCUSCOMPONENT, ATTRIBUTE_NEXTFOCUSCOMPONENT}
	};
	
	private static final String[] PROPERTY_NAMES = new String[] {
		PROPERTY_NAME,
		PROPERTY_LABEL,
		PROPERTY_ENABLED,
		PROPERTY_VISIBLE,
		PROPERTY_INSERTABLE,
		PROPERTY_CONTROLTYPE,
		PROPERTY_CONTROLTYPECLASS,
		PROPERTY_COLLECTABLECOMPONENTPROPERTY,
		PROPERTY_ROWS,
		PROPERTY_COLUMNS,
		PROPERTY_COLUMNWIDTH,
		PROPERTY_DEFAULTVALUES,
		PROPERTY_VALUELISTPROVIDER,
		PROPERTY_TRANSLATIONS,
		PROPERTY_NEXTFOCUSCOMPONENT
	};
	
	private static final PropertyClass[] PROPERTY_CLASSES = new PropertyClass[] {
		new PropertyClass(PROPERTY_NAME, String.class),
		new PropertyClass(PROPERTY_LABEL, String.class),
		new PropertyClass(PROPERTY_ENABLED, boolean.class),
		new PropertyClass(PROPERTY_VISIBLE, boolean.class),
		new PropertyClass(PROPERTY_INSERTABLE, boolean.class),
		new PropertyClass(PROPERTY_CONTROLTYPE, String.class),
		new PropertyClass(PROPERTY_CONTROLTYPECLASS, String.class),
		new PropertyClass(PROPERTY_ROWS, int.class),
		new PropertyClass(PROPERTY_COLUMNS, int.class),
		new PropertyClass(PROPERTY_DEFAULTVALUES, boolean.class),
		new PropertyClass(PROPERTY_VALUELISTPROVIDER, WYSIWYGValuelistProvider.class),
		new PropertyClass(PROPERTY_TRANSLATIONS, TranslationMap.class),
		new PropertyClass(PROPERTY_COLUMNWIDTH, int.class),
		new PropertyClass(PROPERTY_NEXTFOCUSCOMPONENT, String.class),
		new PropertyClass(PROPERTY_COLLECTABLECOMPONENTPROPERTY, WYSIYWYGProperty.class)
	};
	
	private static PropertyFilter[] PROPERTY_FILTERS = new PropertyFilter[] {
		new PropertyFilter(PROPERTY_NAME, 0),
		new PropertyFilter(PROPERTY_LABEL, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_ENABLED, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_VISIBLE, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_INSERTABLE, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_CONTROLTYPE, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_CONTROLTYPECLASS, EXPERT_MODE),
		new PropertyFilter(PROPERTY_VALUELISTPROVIDER, EXPERT_MODE),
		new PropertyFilter(PROPERTY_ROWS, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_COLUMNS, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_DEFAULTVALUES, 0),
		new PropertyFilter(PROPERTY_TRANSLATIONS, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_COLUMNWIDTH, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_NEXTFOCUSCOMPONENT, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_COLLECTABLECOMPONENTPROPERTY, EXPERT_MODE)
	};
	
	protected LayoutMLRules componentsRules = new LayoutMLRules();
	
	private static final PropertySetMethod[] PROPERTY_SETMETHODS = new PropertySetMethod[]{
		new PropertySetMethod(PROPERTY_LABEL, "setLabel")
	};
	
	public static final String[][] PROPERTY_VALUES_FROM_META = new String[][] {
		{PROPERTY_CONTROLTYPE, WYSIWYGMetaInformation.META_CONTROLTYPE},
		{PROPERTY_NEXTFOCUSCOMPONENT, WYSIWYGMetaInformation.META_FIELD_NAMES}
	};

	private ComponentProperties properties;
	
	private WYSIWYGSubForm subform;
	
	private CollectableEntityField field;
	
	public WYSIWYGSubFormColumn(WYSIWYGSubForm subform, CollectableEntityField field) {
		this.subform = subform;
		this.field = field;
	}
	
	public void setLabel(String label) {
		this.subform.setSubFormFromProperties();
	}
	
	public WYSIWYGSubForm getSubForm(){
		return subform;
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
		} else if (p.equals(PROPERTY_ENABLED)) {
			return new PropertyValueBoolean(null);
		} else if (p.equals(PROPERTY_VISIBLE)) {
			return new PropertyValueBoolean(null);
		} else if (p.equals(PROPERTY_INSERTABLE)) {
			return new PropertyValueBoolean(null);
		} else if (p.equals(PROPERTY_CONTROLTYPECLASS)) {
			return new PropertyValueString();
		} else if (p.equals(PROPERTY_CONTROLTYPE)) {
			return new PropertyValueString(controlTypes.get(getDefaultCollectableComponentType()));
		} else if (p.equals(PROPERTY_COLUMNS)) {
			return new PropertyValueInteger(field.getMaxLength());
		} else if (p.equals(PROPERTY_DEFAULTVALUES)) {
			return new PropertyValueBoolean(true);
		}  else if (p.equals(PROPERTY_VALUELISTPROVIDER)) {
			return new PropertyValueValuelistProvider(false);
		}
		return PropertyUtils.getPropertyValue(this, p);
	}
	
	private int getDefaultCollectableComponentType() {
		try {
			EntityFieldMetaDataVO efMeta = MetaDataClientProvider.getInstance().getEntityField(subform.getEntityName(), field.getName());
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
		return getSubForm().getParentEditor();
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
		return this.componentsRules;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#validateProperties(java.util.Map)
	 */
	@Override
	public void validateProperties(Map<String, PropertyValue<Object>> values) throws NuclosBusinessException {
		if (values.containsKey(PROPERTY_CONTROLTYPE) && values.containsKey(PROPERTY_CONTROLTYPECLASS)) {
			String controltype = (String)values.get(PROPERTY_CONTROLTYPE).getValue();
			String controltypeclass = (String)values.get(PROPERTY_CONTROLTYPECLASS).getValue();
			
			if (!StringUtils.isNullOrEmpty(controltype) && !StringUtils.isNullOrEmpty(controltypeclass)) {
				throw new NuclosBusinessException(WYSIWYGStringsAndLabels.VALIDATION_MESSAGES.CONTROLTYPEANDCONTROLYPECLASS);
			}
		}
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
	
	public boolean isLOVorCombobox() {
		boolean isOne = false;
		PropertyValueString controlType = (PropertyValueString) this.getProperties().getProperty(PROPERTY_CONTROLTYPE);
		
		if (controlType != null) {
			if (LayoutMLConstants.ATTRIBUTEVALUE_COMBOBOX.equals(controlType.getValue())) 
				isOne = true;
			else if (LayoutMLConstants.ATTRIBUTEVALUE_LISTOFVALUES.equals(controlType.getValue()))
				isOne = true;
			
		}
		
		return isOne;
	}
	
	private int relativeOrder = 0;
	
	public int getRelativeOrder() {
		return relativeOrder;
	}
	
	public void setRelativeOrder(int relativeOrder) {
		this.relativeOrder = relativeOrder;
	}
}
