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

import java.awt.Dimension;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.log4j.Logger;

import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.client.genericobject.valuelistprovider.ProcessCollectableFieldsProvider;
import org.nuclos.client.layout.wysiwyg.WYSIWYGEditorModes;
import org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableListOfValues;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent.PropertySetMethod;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGValuelistProvider;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEOField;

public class ComponentProperties implements Serializable {

	private static final long serialVersionUID = 8669223366017896044L;
	
	@SuppressWarnings("unchecked")
	private Map<String, PropertyValue> properties;
	private Map<String, String> labels;
	private WYSIWYGComponent c;
	private WYSIWYGMetaInformation metaInf;
	
	private static final Logger log = Logger.getLogger(ComponentProperties.class);

	/**
	 * Constructor
	 * @param c the {@link WYSIWYGComponent} linked to this {@link ComponentProperties}
	 * @param metaInf the {@link WYSIWYGMetaInformation}
	 */
	@SuppressWarnings("unchecked")
	public ComponentProperties(WYSIWYGComponent c, WYSIWYGMetaInformation metaInf) {
		this.properties = new ListOrderedMap();
		this.labels = new HashMap();
		this.c = c;
		this.metaInf = metaInf;
	}

	/**
	 * Constructor
	 * @param properties {@link Map} with the {@link PropertyValue}
	 * @param c the {@link WYSIWYGComponent} for this {@link PropertyValue} {@link Map}
	 */
	@SuppressWarnings("unchecked")
	public ComponentProperties(Map<String, PropertyValue> properties, WYSIWYGComponent c) {
		this.properties = properties;
		this.c = c;
	}
	
	/**
	 * Constructor
	 * @param properties the {@link PropertyValue} {@link Map}
	 * @param c the {@link WYSIWYGComponent}
	 * @param labels the {@link Map} with the Labels
	 * @param metaInf the {@link WYSIWYGMetaInformation}
	 */
	@SuppressWarnings("unchecked")
	public ComponentProperties(Map<String, PropertyValue> properties, WYSIWYGComponent c, Map<String, String> labels, WYSIWYGMetaInformation metaInf) {
		this(properties, c);
		this.labels = labels;
		this.metaInf = metaInf;
	}

	/**
	 * @return a new Instance of this {@link ComponentProperties}
	 */
	private ComponentProperties getClonedInstance() {
		return new ComponentProperties(getClonedProperties(), this.c, this.labels, this.metaInf);
	}

	/**
	 * @return a {@link Map} with the Name of the Property and the {@link PropertyValue} Object
	 */
	@SuppressWarnings("unchecked")
	public Map<String, PropertyValue> getProperties() {
		return properties;
	}
	
	/**
	 * Returns a filtered Set of {@link PropertyValue} depending on the {@link WYSIWYGEditorModes} currently in.
	 * @return a {@link Map} with all the {@link PropertyValue}s allowed for the current {@link WYSIWYGEditorModes}
	 */
	@SuppressWarnings("unchecked")
	public Map<String, PropertyValue> getFilteredProperties() {
		Map<String, PropertyValue> result = getClonedProperties();
		
		try {
			WYSIWYGLayoutEditorPanel editor = (c instanceof WYSIWYGLayoutEditorPanel)?(WYSIWYGLayoutEditorPanel)c:c.getParentEditor();
			int mode = editor.getController().getMode();
			for (Map.Entry<String, PropertyValue> entry : properties.entrySet()) {
				if ((PropertyUtils.getPropertyMode(c, entry.getKey()) & mode) != mode) {
					result.remove(entry.getKey());
				}
			}
		} catch (NuclosBusinessException e) {
			log.error(e);
		}
		return result;
	}

	/**
	 * @return a new {@link Map} with the PropertyNames and the {@link PropertyValue} Objects
	 */
	@SuppressWarnings("unchecked")
	public Map<String, PropertyValue> getClonedProperties() {
		Map<String, PropertyValue> cloneProperties = new ListOrderedMap();

		for (Iterator<String> it = properties.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			try {
				cloneProperties.put(key, (PropertyValue)properties.get(key).clone());
			} catch (CloneNotSupportedException e) {
				log.error(e);
			}
		}

		return cloneProperties;
	}

	/**
	 * @param property the Property to get the Label from
	 * @return the Label for the Property, if not found, the property is returned
	 */
	public String getPropertyLabel(String property) {
		String label = labels.get(property);

		if (label == null) {
			return property;
		} else {
			return label;
		}
	}

	/**
	 * @param property the Property to set the Label for
	 * @param label the Label to set for the Property
	 */
	public void setPropertyLabel(String property, String label) {
		labels.put(property, label);
	}

	/**
	 * @param property the Property to get the {@link PropertyValue} for
	 * @return the {@link PropertyValue} found for the Property
	 */
	@SuppressWarnings("unchecked")
	public PropertyValue getProperty(String property) {
		return properties.get(property);
	}

	/**
	 * This Method sets the Property to the {@link WYSIWYGComponent}.<br>
	 * It uses Reflection to get the Methods.<br>
	 * 
	 * @param property the Property to set
	 * @param value the {@link PropertyValue} with the value
	 * @param valueClass the Class the Value is in
	 * @throws CommonBusinessException
	 */
	@SuppressWarnings("unchecked")
	public void setProperty(String property, PropertyValue value, Class<?> valueClass) throws CommonBusinessException {
		try {
			if (c instanceof WYSIWYGLayoutEditorPanel)
				((WYSIWYGLayoutEditorPanel) c).getUndoRedoFunction().loggingChangeComponentsProperties(c, getClonedInstance(), ((WYSIWYGLayoutEditorPanel) c).getTableLayoutUtil());
			else
				c.getParentEditor().getUndoRedoFunction().loggingChangeComponentsProperties(c, getClonedInstance(), c.getParentEditor().getTableLayoutUtil());
		} catch (CommonFatalException ex) {
			/**
			 * nothing to do, happens when layout is loaded... no serious reason
			 * to panic
			 */
		} catch (NullPointerException ex) {
			/**
			 * nothing to do, happens when layout is loaded... no serious reason
			 * to panic
			 */
		}
		properties.put(property, value);
		try {
			if (c instanceof WYSIWYGLayoutEditorPanel)
				((WYSIWYGLayoutEditorPanel) c).getUndoRedoFunction().loggingChangeComponentsProperties(c, getClonedInstance(), ((WYSIWYGLayoutEditorPanel) c).getTableLayoutUtil());
			else
				c.getParentEditor().getUndoRedoFunction().loggingChangeComponentsProperties(c, getClonedInstance(), c.getParentEditor().getTableLayoutUtil());
		} catch (CommonFatalException ex) {
			/**
			 * nothing to do, happens when layout is loaded... no serious reason
			 * to panic
			 */
		} catch (NullPointerException ex) {
			/**
			 * nothing to do, happens when layout is loaded... no serious reason
			 * to panic
			 */
		}
		
		addStaticDependantProperties(property, value);
		
		if (valueClass != null) {

			Class<? extends WYSIWYGComponent> componentClass = c.getClass();
			Collection<String> methodNames = getMethodForProperty(property);

			for (Iterator<String> it = methodNames.iterator(); it.hasNext();) {
				String methodName = it.next();
				try {
					Method method = componentClass.getMethod(methodName, valueClass);
					method.invoke(c, value.getValue(valueClass, c));
				} catch (NullPointerException e){
					log.error(e);
					Errors.getInstance().showExceptionDialog(null, e);
				} catch (SecurityException e) {
					log.error(e);
					Errors.getInstance().showExceptionDialog(null, e);
				} catch (NoSuchMethodException e) {
					log.error(e);
					Errors.getInstance().showExceptionDialog(null, e);
				} catch (IllegalArgumentException e) {
					log.error(e);
					Errors.getInstance().showExceptionDialog(null, e);
				} catch (IllegalAccessException e) {
					log.error(e);
					Errors.getInstance().showExceptionDialog(null, e);
				} catch (InvocationTargetException e) {
					log.error(e);
					Errors.getInstance().showExceptionDialog(null, e.getTargetException());
					if (e.getTargetException() instanceof CommonBusinessException) {
						throw (CommonBusinessException) e.getTargetException();
					} else {
						throw new CommonFatalException(e);
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @param newProperties
	 * @throws CommonBusinessException
	 */
	@SuppressWarnings("unchecked")
	public void setProperties(Map<String, PropertyValue> newProperties) throws CommonBusinessException {
		try {
			if (c instanceof WYSIWYGLayoutEditorPanel)
				((WYSIWYGLayoutEditorPanel) c).getUndoRedoFunction().loggingChangeComponentsProperties(c, getClonedInstance(), ((WYSIWYGLayoutEditorPanel) c).getTableLayoutUtil());
			else
				c.getParentEditor().getUndoRedoFunction().loggingChangeComponentsProperties(c, getClonedInstance(), c.getParentEditor().getTableLayoutUtil());
		} 
		catch (CommonFatalException ex) { } 
		catch (NullPointerException ex) { }
		
		for (Map.Entry<String, PropertyValue> e : newProperties.entrySet()) {
			properties.put(e.getKey(), e.getValue());
			
			if (PropertyUtils.getValueClass(c, e.getKey()) != null) {
				Class<?> valueClass = PropertyUtils.getValueClass(c, e.getKey());
				Class<? extends WYSIWYGComponent> componentClass = c.getClass();
				Collection<String> methodNames = getMethodForProperty(e.getKey());

				for (Iterator<String> it = methodNames.iterator(); it.hasNext();) {
					String methodName = it.next();
					try {
						Method method = componentClass.getMethod(methodName, valueClass);
						method.invoke(c, e.getValue().getValue(valueClass, c));
						try{
						if (method.equals(componentClass.getMethod("setPreferredSize", valueClass))){
							c.getParentEditor().getTableLayoutUtil().revalidateLayoutCellForComponent(c, ((Dimension)e.getValue().getValue(valueClass, c)));
						} 
						} catch (Exception exc) {}
					} catch (SecurityException ex) {
						log.error(e);
						Errors.getInstance().showExceptionDialog(null, ex);
					} catch (NoSuchMethodException ex) {
						log.error(e);
						Errors.getInstance().showExceptionDialog(null, ex);
					} catch (IllegalArgumentException ex) {
						log.error(e);
						Errors.getInstance().showExceptionDialog(null, ex);
					} catch (IllegalAccessException ex) {
						log.error(e);
						Errors.getInstance().showExceptionDialog(null, ex);
					} catch (InvocationTargetException ex) {
						log.error(e);
						Errors.getInstance().showExceptionDialog(null, ex);
						if (ex.getTargetException() instanceof CommonBusinessException) {
							throw (CommonBusinessException) ex.getTargetException();
						} else {
							throw new CommonFatalException(ex);
						}
					}
				}
			}
		}
		
		for (Map.Entry<String, PropertyValue> e : newProperties.entrySet()) {
			addStaticDependantProperties(e.getKey(), e.getValue());
		}
		
		try {
			if (c instanceof WYSIWYGLayoutEditorPanel)
				((WYSIWYGLayoutEditorPanel) c).getUndoRedoFunction().loggingChangeComponentsProperties(c, getClonedInstance(), ((WYSIWYGLayoutEditorPanel) c).getTableLayoutUtil());
			else
				c.getParentEditor().getUndoRedoFunction().loggingChangeComponentsProperties(c, getClonedInstance(), c.getParentEditor().getTableLayoutUtil());
		} 
		catch (CommonFatalException ex) { } 
		catch (NullPointerException ex) { }
	}

	/**
	 * @param property the Property the setMethod should be recieved
	 * @return a Collection with the Methodnames defined in {@link WYSIWYGComponent#getPropertySetMethods()}
	 */
	private Collection<String> getMethodForProperty(String property) {
		Collection<String> lstMethods = new ArrayList<String>();

		PropertySetMethod[] methods = c.getPropertySetMethods();

		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equals(property)) {
				lstMethods.add(methods[i].getMethod());
			}
		}

		return lstMethods;
	}
	
	/**
	 * 
	 * @param property
	 * @param value
	 * @throws CommonBusinessException 
	 */
	private void addStaticDependantProperties(String property, @SuppressWarnings("rawtypes") PropertyValue value) throws CommonBusinessException {
		
		/**
		 * CollectableField:NuclosProcess --> ValueListProvider:ProcessCollectableFieldsProvider
		 */
		if (WYSIWYGCollectableComponent.PROPERTY_NAME.equals(property) &&
			value instanceof PropertyValueString) {
			PropertyValueString pvs = (PropertyValueString) value;
			if (NuclosEOField.PROCESS.getName().equals(pvs.getValue())) {
				PropertyValueValuelistProvider pvVlp = new PropertyValueValuelistProvider();
				WYSIWYGValuelistProvider wysiwygVlp = new WYSIWYGValuelistProvider();
				wysiwygVlp.setType(ProcessCollectableFieldsProvider.NAME);
				pvVlp.setValue(wysiwygVlp);
				setProperty(WYSIWYGCollectableComponent.PROPERTY_VALUELISTPROVIDER, pvVlp, WYSIWYGCollectableListOfValues.class);
			}
		}
	}

	/**
	 * @return the {@link WYSIWYGComponent} this {@link ComponentProperties} is for
	 */
	public WYSIWYGComponent getComponent() {
		return c;
	}

	/**
	 * @return the {@link WYSIWYGMetaInformation}
	 */
	public WYSIWYGMetaInformation getMetaInformation() {
		return metaInf;
	}

	@Override
	public String toString() {
		StringBuffer fubber = new StringBuffer();

		if (c != null)
			fubber.append("\nWYSIWYGComponent: " + c.toString() + "\n");
		else
			fubber.append("nWYSIWYGComponent: null " + "\n");
		if (properties != null)
			fubber.append("Properties: " + properties.toString() + "\n");
		else
			fubber.append("Properties: null " + "\n");
		if (metaInf != null)
			fubber.append("MetaINfo: " + metaInf.toString() + "\n");
		else
			fubber.append("MetaINfo: null " + "\n");

		return fubber.toString();
	}

}
