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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.apache.log4j.Logger;
import org.nuclos.api.ui.LayoutComponent;
import org.nuclos.client.layout.wysiwyg.WYSIWYGEditorModes;
import org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COMPONENT_PROCESSOR;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PROPERTYUTILS;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.VALIDATION_MESSAGES;
import org.nuclos.client.layout.wysiwyg.component.TranslationMap;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGChart;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableCheckBox;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableComboBox;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableDateChooser;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableLabel;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableListOfValues;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableOptionGroup;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectablePasswordfield;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableTextArea;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableTextfield;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent.PropertyClass;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGLayoutComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGScrollPane;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSplitPane;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGStaticButton;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGStaticComboBox;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGStaticLabel;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGStaticSeparator;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGStaticTextarea;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGStaticTextfield;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGStaticTitledSeparator;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSubForm;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGTabbedPane;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGUniversalComponent;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.TableLayoutUtil;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGInitialSortingOrder;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGOption;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGOptions;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGValuelistProvider;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGProperty;
import org.nuclos.client.ui.collect.component.CollectableOptionGroup;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.NuclosScript;
import org.nuclos.common.NuclosTranslationMap;
import org.nuclos.common.NuclosValueListProvider;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.layoutml.LayoutMLConstants;

/**
 * This Class contains some helper Methods used in context with the {@link ComponentProperties}.<br>
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class PropertyUtils implements LayoutMLConstants {
		
	private static final Logger log = Logger.getLogger(PropertyUtils.class);

	/**
	 * This Method returns a new empty {@link ComponentProperties} Object for the given {@link WYSIWYGComponent}
	 * @param c the {@link WYSIWYGComponent} 
	 * @param metaInf
	 * @return a new and empty {@link ComponentProperties} for the {@link WYSIWYGComponent}
	 */
	public static ComponentProperties getEmptyProperties (WYSIWYGComponent c, WYSIWYGMetaInformation metaInf){
		ComponentProperties properties = new ComponentProperties(c, metaInf);
		String[] propertyNames = c.getPropertyNames();
		
		for (int i = 0; i < propertyNames.length; i++){
			try {
				properties.setProperty(propertyNames[i], getPropertyValue(c, propertyNames[i]), null);
			} catch (CommonBusinessException e) {
			} catch (NullPointerException ex) {}
		}
		
		setDefaultValuesFor(c, properties);
		
		return properties;
	}
	
	/**
	 * This Method selects the Component and the fitting Default Generator for this Component.
	 * It also sets default Values for all Components.
	 * NUCLEUSINT-274
	 * @param c
	 * @param properties
	 */
	private static void setDefaultValuesFor(WYSIWYGComponent c, ComponentProperties properties) {

		try {
			/** which component is it and which method is to be used to create default properties */
			if (c instanceof WYSIWYGCollectableCheckBox) {
				setDefaultValuesForWYSIWYGCollectableCheckBox(properties);
			} else if (c instanceof WYSIWYGCollectableComboBox) {
				setDefaultValuesForWYSIWYGCollectableComboBox(properties);
			} else if (c instanceof WYSIWYGCollectableTextfield) {
				setDefaultValuesForWYSIWYGStaticTextfield(properties);
			} else if (c instanceof WYSIWYGCollectableDateChooser) {
				setDefaultValuesForWYSIWYGCollectableDateChooser(properties);
			} else if (c instanceof WYSIWYGCollectableLabel ) {
				setDefaultValuesForWYSIWYGCollectableLabel(properties);
			} else if (c instanceof WYSIWYGCollectableListOfValues) {
				setDefaultValuesForWYSIWYGCollectableListOfValues(properties);
			} else if (c instanceof WYSIWYGCollectableOptionGroup) {
				setDefaultValuesForWYSIWYGCollectableOptionGroup(properties, (WYSIWYGCollectableOptionGroup)c);
			} else if (c instanceof WYSIWYGCollectableTextArea) {
				setDefaultValuesForWYSIWYGCollectableTextArea(properties);
			} else if (c instanceof WYSIWYGCollectableTextfield) {
				setDefaultValuesForWYSIWYGCollectableTextfield(properties);
			} else if (c instanceof WYSIWYGCollectablePasswordfield) {
				//NUCLEUSINT-1142
				setDefaultValuesForWYSIWYGCollectablePasswordfield(properties);
			} else if (c instanceof WYSIWYGScrollPane) {
				setDefaultValuesForWYSIWYGScrollPane(properties);
			} else if (c instanceof WYSIWYGSplitPane) {
				setDefaultValuesForWYSIWYGSplitPane(properties);
			} else if (c instanceof WYSIWYGStaticButton) {
				setDefaultValuesForWYSIWYGStaticButton(properties);
			} else if (c instanceof WYSIWYGStaticComboBox) {
				setDefaultValuesForWYSIWYGStaticComboBox(properties);
			} else if (c instanceof WYSIWYGStaticLabel) {
				setDefaultValuesForWYSIWYGStaticLabel(properties);
			} else if (c instanceof WYSIWYGStaticSeparator) {
				setDefaultValuesForWYSIWYGStaticSeparator(properties);
			} else if (c instanceof WYSIWYGStaticTextarea) {
				setDefaultValuesForWYSIWYGStaticTextarea(properties);
			} else if (c instanceof WYSIWYGStaticTextfield) {
				setDefaultValuesForWYSIWYGStaticTextfield(properties);
			} else if (c instanceof WYSIWYGStaticTitledSeparator) {
				setDefaultValuesForWYSIWYGStaticTitledSeparator(properties);
			} else if (c instanceof WYSIWYGSubForm) {
				setDefaultValuesForWYSIWYGSubForm(properties);
			} else if (c instanceof WYSIWYGChart) {
				setDefaultValuesForWYSIWYGChart(properties);
			} else if (c instanceof WYSIWYGTabbedPane) {
				setDefaultValuesForWYSIWYGTabbedPane(properties);
			} else if (c instanceof WYSIWYGUniversalComponent) {
				setDefaultValuesForWYSIWYGUniversalComponent(properties);
			} 

			if (!(c instanceof WYSIWYGLayoutComponent)) {
				properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_OPAQUE, new PropertyValueBoolean(true), boolean.class);
			}
			properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_ENABLED, new PropertyValueBoolean(true), boolean.class);
			properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_VISIBLE, new PropertyValueBoolean(true), boolean.class);
			
		} catch (CommonBusinessException e) {
			log.error(e);
		} catch (NullPointerException ex) {
			log.error(ex);
		}
	}
	
	/**
	 * 
	 * @param properties
	 * @throws CommonBusinessException
	 */
	private static void setDefaultValuesForWYSIWYGCollectableCheckBox(ComponentProperties properties) throws CommonBusinessException{
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_PREFFEREDSIZE, new PropertyValueDimension(DEFAULTVALUE_CHECKBOX_PREFERREDSIZE), DEFAULTVALUE_CHECKBOX_PREFERREDSIZE.getClass());
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_SHOWONLY, new PropertyValueString(ATTRIBUTEVALUE_CONTROL), String.class);
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_FILL_CONTROL_HORIZONTALLY, new PropertyValueBoolean(true), boolean.class);		
	}
	
	/**
	 * 
	 * @param properties
	 * @throws CommonBusinessException
	 */
	private static void setDefaultValuesForWYSIWYGCollectableTextfield(ComponentProperties properties) throws CommonBusinessException{
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_PREFFEREDSIZE, new PropertyValueDimension(DEFAULTVALUE_TEXTFIELD_PREFERREDSIZE), DEFAULTVALUE_TEXTFIELD_PREFERREDSIZE.getClass());
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_SHOWONLY, new PropertyValueString(ATTRIBUTEVALUE_CONTROL), String.class);
		properties.setProperty(WYSIWYGCollectableTextfield.PROPERTY_COLUMNS, new PropertyValueInteger(DEFAULTVALUE_TEXTFIELD_COLUMNS), int.class);
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_FILL_CONTROL_HORIZONTALLY, new PropertyValueBoolean(true), boolean.class);		
	}
	
	/**
	 * NUCLEUSINT-1142
	 * @param properties
	 * @throws CommonBusinessException
	 */
	private static void setDefaultValuesForWYSIWYGCollectablePasswordfield(ComponentProperties properties) throws CommonBusinessException{
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_PREFFEREDSIZE, new PropertyValueDimension(DEFAULTVALUE_TEXTFIELD_PREFERREDSIZE), DEFAULTVALUE_TEXTFIELD_PREFERREDSIZE.getClass());
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_SHOWONLY, new PropertyValueString(ATTRIBUTEVALUE_CONTROL), String.class);
		properties.setProperty(WYSIWYGCollectableTextfield.PROPERTY_COLUMNS, new PropertyValueInteger(DEFAULTVALUE_TEXTFIELD_COLUMNS), int.class);
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_FILL_CONTROL_HORIZONTALLY, new PropertyValueBoolean(true), boolean.class);		
	}

	/**
	 * 
	 * @param properties
	 * @throws CommonBusinessException
	 */
	private static void setDefaultValuesForWYSIWYGCollectableComboBox(ComponentProperties properties) throws CommonBusinessException{
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_STRICTSIZE, new PropertyValueDimension(DEFAULTVALUE_COMBOBOX_STRICTSIZE), DEFAULTVALUE_COMBOBOX_STRICTSIZE.getClass());
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_SHOWONLY, new PropertyValueString(ATTRIBUTEVALUE_CONTROL), String.class);
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_FILL_CONTROL_HORIZONTALLY, new PropertyValueBoolean(true), boolean.class);
		//NUCLEUSINT-492
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_INSERTABLE, new PropertyValueBoolean(false), boolean.class);
		
	}

	/**
	 * 
	 * @param properties
	 * @throws CommonBusinessException
	 */
	private static void setDefaultValuesForWYSIWYGCollectableDateChooser(ComponentProperties properties) throws CommonBusinessException{
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_PREFFEREDSIZE, new PropertyValueDimension(DEFAULTVALUE_DATECHOOSER_PREFERREDSIZE), DEFAULTVALUE_DATECHOOSER_PREFERREDSIZE.getClass());
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_SHOWONLY, new PropertyValueString(ATTRIBUTEVALUE_CONTROL), String.class);
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_FILL_CONTROL_HORIZONTALLY, new PropertyValueBoolean(true), boolean.class);
	}

	/**
	 * 
	 * @param properties
	 * @param og
	 * @throws CommonBusinessException
	 */
	private static void setDefaultValuesForWYSIWYGCollectableOptionGroup(ComponentProperties properties, WYSIWYGCollectableOptionGroup og) throws CommonBusinessException{
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_PREFFEREDSIZE, new PropertyValueDimension(DEFAULTVALUE_OPTIONGROUP_PREFERREDSIZE), DEFAULTVALUE_OPTIONGROUP_PREFERREDSIZE.getClass());
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_SHOWONLY, new PropertyValueString(ATTRIBUTEVALUE_CONTROL), String.class);
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_FILL_CONTROL_HORIZONTALLY, new PropertyValueBoolean(true), boolean.class);
		//NUCLEUSINT-385
		/** the optiongroup is a extension of the universal component, setting by default the options controltype. editing is disabled for this component */
		properties.setProperty(WYSIWYGUniversalComponent.PROPERTY_CONTROLTYPE, new PropertyValueString(CONTROLTYPE_OPTIONGROUP), String.class);
		
		WYSIWYGOptions options = new WYSIWYGOptions("Options", "Option 1", null);
		options.addOptionToOptionsGroup(new WYSIWYGOption("Option 1", "Option 1", "Option 1", null));
		options.addOptionToOptionsGroup(new WYSIWYGOption("Option 2", "Option 2", "Option 2", null));
		
		PropertyOptions value = (PropertyOptions)PropertyUtils.getPropertyValue(og, WYSIWYGCollectableOptionGroup.PROPERTY_OPTIONS);
		value.setValue(options);
		
		properties.setProperty(WYSIWYGCollectableOptionGroup.PROPERTY_OPTIONS, value, PropertyUtils.getValueClass(og, WYSIWYGCollectableOptionGroup.PROPERTY_OPTIONS));
	}

	/**
	 * 
	 * @param properties
	 * @throws CommonBusinessException
	 */
	private static void setDefaultValuesForWYSIWYGCollectableListOfValues(ComponentProperties properties) throws CommonBusinessException{
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_PREFFEREDSIZE, new PropertyValueDimension(DEFAULTVALUE_LISTOFVALUES_PREFERREDSIZE), DEFAULTVALUE_LISTOFVALUES_PREFERREDSIZE.getClass());
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_SHOWONLY, new PropertyValueString(ATTRIBUTEVALUE_CONTROL), String.class);
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_FILL_CONTROL_HORIZONTALLY, new PropertyValueBoolean(true), boolean.class);
	}

	/**
	 * 
	 * @param properties
	 * @throws CommonBusinessException
	 */
	private static void setDefaultValuesForWYSIWYGCollectableTextArea(ComponentProperties properties) throws CommonBusinessException{
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_PREFFEREDSIZE, new PropertyValueDimension(DEFAULTVALUE_TEXTAREA_PREFERREDSIZE), DEFAULTVALUE_TEXTAREA_PREFERREDSIZE.getClass());
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_SHOWONLY, new PropertyValueString(ATTRIBUTEVALUE_CONTROL), String.class);
		properties.setProperty(WYSIWYGCollectableTextArea.PROPERTY_COLUMNS, new PropertyValueInteger(DEFAULTVALUE_TEXTAREA_COLUMNS), int.class);
		properties.setProperty(WYSIWYGCollectableTextArea.PROPERTY_ROWS, new PropertyValueInteger(DEFAULTVALUE_TEXTAREA_ROWS), int.class);
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_FILL_CONTROL_HORIZONTALLY, new PropertyValueBoolean(true), boolean.class);		
	}

	/**
	 * 
	 * @param properties
	 * @throws CommonBusinessException
	 */
	private static void setDefaultValuesForWYSIWYGCollectableLabel(ComponentProperties properties) throws CommonBusinessException{
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_PREFFEREDSIZE, new PropertyValueDimension(DEFAULTVALUE_LABEL_PREFERREDSIZE), DEFAULTVALUE_LABEL_PREFERREDSIZE.getClass());
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_SHOWONLY, new PropertyValueString(ATTRIBUTEVALUE_LABEL), String.class);
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_FILL_CONTROL_HORIZONTALLY, new PropertyValueBoolean(true), boolean.class);
		
	}

	/**
	 * 
	 * @param properties
	 * @throws CommonBusinessException
	 */
	private static void setDefaultValuesForWYSIWYGUniversalComponent(ComponentProperties properties) throws CommonBusinessException{
		properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_PREFFEREDSIZE, new PropertyValueDimension(DEFAULTVALUE_TEXTFIELD_PREFERREDSIZE), DEFAULTVALUE_TEXTFIELD_PREFERREDSIZE.getClass());
		//NUCLEUSINT-288
		//NUCLEUSINT-460 set showonly to control as value to avoid problems
		properties.setProperty(WYSIWYGUniversalComponent.PROPERTY_SHOWONLY, new PropertyValueString(WYSIWYGUniversalComponent.ATTRIBUTEVALUE_CONTROL), String.class);
		
	}

	/**
	 * 
	 * @param properties
	 * @throws CommonBusinessException
	 */
	private static void setDefaultValuesForWYSIWYGStaticLabel(ComponentProperties properties) throws CommonBusinessException{
		properties.setProperty(WYSIWYGStaticLabel.PROPERTY_PREFFEREDSIZE, new PropertyValueDimension(DEFAULTVALUE_LABEL_PREFERREDSIZE), DEFAULTVALUE_LABEL_PREFERREDSIZE.getClass());
	}

	/**
	 * 
	 * @param properties
	 * @throws CommonBusinessException
	 */
	private static void setDefaultValuesForWYSIWYGStaticTextfield(ComponentProperties properties) throws CommonBusinessException{
		properties.setProperty(WYSIWYGStaticTextfield.PROPERTY_PREFFEREDSIZE, new PropertyValueDimension(DEFAULTVALUE_TEXTFIELD_PREFERREDSIZE), DEFAULTVALUE_TEXTFIELD_PREFERREDSIZE.getClass());
		properties.setProperty(WYSIWYGStaticTextfield.PROPERTY_COLUMNS, new PropertyValueInteger(DEFAULTVALUE_TEXTFIELD_COLUMNS), int.class);		
	}

	/**
	 * 
	 * @param properties
	 * @throws CommonBusinessException
	 */
	private static void setDefaultValuesForWYSIWYGStaticTextarea(ComponentProperties properties) throws CommonBusinessException{
		properties.setProperty(WYSIWYGStaticTextarea.PROPERTY_PREFFEREDSIZE, new PropertyValueDimension(DEFAULTVALUE_TEXTAREA_PREFERREDSIZE), DEFAULTVALUE_TEXTAREA_PREFERREDSIZE.getClass());
		properties.setProperty(WYSIWYGStaticTextarea.PROPERTY_COLUMNS, new PropertyValueInteger(DEFAULTVALUE_TEXTAREA_COLUMNS), int.class);
		properties.setProperty(WYSIWYGStaticTextarea.PROPERTY_ROWS, new PropertyValueInteger(DEFAULTVALUE_TEXTAREA_ROWS), int.class);
	}

	/**
	 * 
	 * @param properties
	 * @throws CommonBusinessException
	 */
	private static void setDefaultValuesForWYSIWYGStaticComboBox(ComponentProperties properties) throws CommonBusinessException{
		properties.setProperty(WYSIWYGStaticComboBox.PROPERTY_EDITABLE, new PropertyValueBoolean(true), boolean.class);
		properties.setProperty(WYSIWYGStaticComboBox.PROPERTY_PREFFEREDSIZE, new PropertyValueDimension(DEFAULTVALUE_COMBOBOX_PREFERREDSIZE), DEFAULTVALUE_COMBOBOX_PREFERREDSIZE.getClass());
	}

	/**
	 * 
	 * @param properties
	 * @throws CommonBusinessException
	 */
	private static void setDefaultValuesForWYSIWYGStaticButton(ComponentProperties properties) throws CommonBusinessException{
		properties.setProperty(WYSIWYGStaticButton.PROPERTY_PREFFEREDSIZE, new PropertyValueDimension(DEFAULTVALUE_BUTTON_PREFERREDSIZE), DEFAULTVALUE_BUTTON_PREFERREDSIZE.getClass());
		//FIX NUCLEUSINT-255
		properties.setProperty(WYSIWYGStaticButton.PROPERTY_ACTIONCOMMAND, new PropertyValueString(COMPONENT_PROCESSOR.LABEL_DEFAULTACTIONCOMMAND_BUTTON), String.class);
		
	}

	/**
	 * 
	 * @param properties
	 * @throws CommonBusinessException
	 */
	private static void setDefaultValuesForWYSIWYGStaticSeparator(ComponentProperties properties) throws CommonBusinessException{
		properties.setProperty(WYSIWYGStaticSeparator.PROPERTY_ORIENTATION, new PropertyValueString(DEFAULTVALUE_SEPARATOR_ORIENTATION), String.class);
		properties.setProperty(WYSIWYGStaticSeparator.PROPERTY_PREFFEREDSIZE, new PropertyValueDimension(new Dimension(InterfaceGuidelines.SEPARATOR_MIN_WIDTH, InterfaceGuidelines.SEPARATOR_MIN_LENGTH)), Dimension.class);
	}

	/**
	 * 
	 * @param properties
	 * @throws CommonBusinessException
	 */
	private static void setDefaultValuesForWYSIWYGStaticTitledSeparator(ComponentProperties properties) throws CommonBusinessException{
	}

	/**
	 * 
	 * @param properties
	 * @throws CommonBusinessException
	 */
	private static void setDefaultValuesForWYSIWYGSubForm(ComponentProperties properties) throws CommonBusinessException{
		properties.setProperty(WYSIWYGSubForm.PROPERTY_PREFFEREDSIZE, new PropertyValueDimension(DEFAULTVALUE_SUBFORM_PREFERREDSIZE), DEFAULTVALUE_SUBFORM_PREFERREDSIZE.getClass());
		properties.setProperty(WYSIWYGSubForm.PROPERTY_INITIALSORTINGORDER, new PropertyValueInitialSortingOrder(), null);
	}

	/**
	 * 
	 * @param properties
	 * @throws CommonBusinessException
	 */
	private static void setDefaultValuesForWYSIWYGChart(ComponentProperties properties) throws CommonBusinessException{
		properties.setProperty(WYSIWYGChart.PROPERTY_PREFFEREDSIZE, new PropertyValueDimension(DEFAULTVALUE_CHART_PREFERREDSIZE), DEFAULTVALUE_CHART_PREFERREDSIZE.getClass());
	}

	/**
	 * 
	 * @param properties
	 * @throws CommonBusinessException
	 */
	private static void setDefaultValuesForWYSIWYGTabbedPane(ComponentProperties properties) throws CommonBusinessException{
		properties.setProperty(WYSIWYGTabbedPane.PROPERTY_PREFFEREDSIZE, new PropertyValueDimension(DEFAULTVALUE_TABBEDPANE_PREFERREDSIZE), DEFAULTVALUE_TABBEDPANE_PREFERREDSIZE.getClass());
	}

	/**
	 * 
	 * @param properties
	 * @throws CommonBusinessException
	 */
	private static void setDefaultValuesForWYSIWYGScrollPane(ComponentProperties properties) throws CommonBusinessException{
		properties.setProperty(WYSIWYGScrollPane.PROPERTY_PREFFEREDSIZE, new PropertyValueDimension(DEFAULTVALUE_SCROLLPANE_PREFERREDSIZE), DEFAULTVALUE_SCROLLPANE_PREFERREDSIZE.getClass());
		// NUCLEUSINT-253
		properties.setProperty(WYSIWYGScrollPane.PROPERTY_HORIZONTALSCROLLBAR, new PropertyValueString(ATTRIBUTEVALUE_ASNEEDED), String.class);
		properties.setProperty(WYSIWYGScrollPane.PROPERTY_VERTICALSCROLLBAR, new PropertyValueString(ATTRIBUTEVALUE_ASNEEDED), String.class);
	}

	/**
	 * 
	 * @param properties
	 * @throws CommonBusinessException
	 */
	private static void setDefaultValuesForWYSIWYGSplitPane(ComponentProperties properties) throws CommonBusinessException{
		properties.setProperty(WYSIWYGSplitPane.PROPERTY_PREFFEREDSIZE, new PropertyValueDimension(DEFAULTVALUE_SPLITPANE_PREFERREDSIZE), DEFAULTVALUE_SPLITPANE_MINIMUMSIZE.getClass());
		properties.setProperty(WYSIWYGSplitPane.PROPERTY_ORIENTATION, new PropertyValueString(ATTRIBUTEVALUE_HORIZONTAL), ATTRIBUTEVALUE_HORIZONTAL.getClass());
		properties.setProperty(WYSIWYGSplitPane.PROPERTY_DIVIDERSIZE, new PropertyValueInteger(DEFAULTVALUE_SPLITPANE_DIVIDERSIZE), int.class);
	}
	
	/**
	 * This Method gets the Class corresponding to the given PropertyName
	 * @param c the {@link WYSIWYGComponent}
	 * @param propertyName the PropertyName that Class should be returned
	 * @return the Class, if not found <b>null</b> is returned
	 */
	public static Class<?> getValueClass(WYSIWYGComponent c, String propertyName){
		for (int i = 0; i < c.getPropertyClasses().length; i++){
			PropertyClass pc = c.getPropertyClasses()[i];
			if (pc.getName().equals(propertyName)){
				return pc.getPropertyClass();
			}
		}
		return null;
	}
		
	/**
	 * This Method creates the {@link WYSIWYGComponent} specific {@link MenuItem} for opening the {@link PropertiesPanel}
	 * @param menuLabel the Label to show
	 * @param c the {@link WYSIWYGComponent} which Properties should be changed
	 * @param tableLayoutUtil the {@link TableLayoutUtil} for this {@link WYSIWYGComponent}
	 * @return the {@link MenuItem} which opens the {@link PropertiesPanel}
	 */
	public static JMenuItem getStandartContextMenuEntryForProperties(String menuLabel, final WYSIWYGComponent c, final TableLayoutUtil tableLayoutUtil){
		
		JMenuItem miSubFormProperties = new JMenuItem(menuLabel);
		miSubFormProperties.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				PropertiesPanel.showPropertiesForComponent(c, tableLayoutUtil);
			}
			
		});
		return miSubFormProperties;
	}
	
	/**
	 * This Method returns the fitting {@link PropertyValue} class for this Property
	 * @param c the {@link WYSIWYGComponent}
	 * @param property the Propertyname
	 * @return the fitting {@link PropertyValue} class for the Property, 
	 * @throws NuclosFatalException if there is no {@link PropertyValue} for this Property
	 */
	public static PropertyValue<?> getPropertyValue(WYSIWYGComponent c, String property) throws NuclosFatalException{
		Class<?> propertyClass = getValueClass(c, property);
		if (propertyClass == null) {
			return null;
		}
		if (propertyClass.equals(String.class)) {
			return new PropertyValueString();
		} else if (propertyClass.equals(Dimension.class)) {
			return new PropertyValueDimension();
		} else if (propertyClass.equals(Border.class)) {
			return new PropertyValueBorder();
		} else if (propertyClass.equals(Boolean.class) || propertyClass.equals(boolean.class)) {
			return new PropertyValueBoolean();
		} else if (propertyClass.equals(Color.class)) {
			return new PropertyValueColor();
		} else if (propertyClass.equals(Integer.class) ||propertyClass.equals(int.class)) {
			return new PropertyValueInteger();
		} else if (propertyClass.equals(double.class)) {
			return new PropertyValueDouble();
		} else if (propertyClass.equals(WYSIWYGValuelistProvider.class) || propertyClass.equals(NuclosValueListProvider.class)) {
			return new PropertyValueValuelistProvider(c instanceof LayoutComponent);
		} else if (propertyClass.equals(Font.class)) {
			return new PropertyValueFont();
		}  else if (propertyClass.equals(WYSIWYGOptions.class)) {
			return new PropertyOptions();
		}  else if (propertyClass.equals(WYSIWYGInitialSortingOrder.class)) {
			return new PropertyValueInitialSortingOrder();
		} else if (propertyClass.equals(WYSIYWYGProperty.class)) {
			return new PropertyCollectableComponentProperty();
		} else if (propertyClass.equals(PropertyValueFont.class)) {
			return new PropertyValueFont();
		} else if (propertyClass.equals(TranslationMap.class) || propertyClass.equals(NuclosTranslationMap.class)) {
			return new PropertyValueTranslations();
		} else if (propertyClass.equals(NuclosScript.class)) {
			return new PropertyValueScript();
		} else if (propertyClass.equals(PropertyChartProperty.class)) {
			return new PropertyChartProperty(c);
		} else if (propertyClass.equals(PropertyValueKeyStroke.class)) {
			return new PropertyValueKeyStroke();
		}
		throw new NuclosFatalException(WYSIWYGStringsAndLabels.partedString(PROPERTYUTILS.ERRORMESSAGE_NO_PROPERTYVALUE_FOR_PROPERTY, property,c.getClass().toString()));
	}

	/**
	 * Returns the Mode the Property can be accessed in
	 * @param c the {@link WYSIWYGComponent}
	 * @param property the Propertyname
	 * @return the Mode ( {@link WYSIWYGEditorModes} or <b>0</b> if there is no Mode
	 */
	public static int getPropertyMode(WYSIWYGComponent c, String property) {
		for (int i = 0; i < c.getPropertyFilters().length; i++){
			WYSIWYGComponent.PropertyFilter pf = c.getPropertyFilters()[i];
			if (pf.getName().equals(property)){
				return pf.getMode();
			}
		}
		return 0;
	}
	
	
	/**
	 * Method for checking the PreferredSize
	 * @param value the PreferredSize as {@link Dimension}
	 * @param width 
	 * @param height
	 * @throws NuclosBusinessException
	 */
	public static void validatePreferredSize(Dimension value, int width, int height) throws NuclosBusinessException{
		if (value != null) {
			if (value.getWidth() < width) {
				throw new NuclosBusinessException(WYSIWYGStringsAndLabels.partedString(VALIDATION_MESSAGES.PREFERREDSIZE_WIDTH, String.valueOf(width)));
			}
			if (value.getHeight() < height) {
				throw new NuclosBusinessException(WYSIWYGStringsAndLabels.partedString(VALIDATION_MESSAGES.PREFERREDSIZE_HEIGHT, String.valueOf(height)));
			}
		}
	}
	
	/**
	 * This Method gets the default Color read from the UIManager.<br>
	 * @param c the {@link WYSIWYGComponent} to get the default Color for
	 * @return the Color for this specific {@link Component}, <b>null</b> if there was found nothing
	 */
	public static Color getDefaultBackground(WYSIWYGComponent c) {
		if (c instanceof WYSIWYGCollectableComponent) {
			return UIManager.getColor("Panel.background");
		} else if (c instanceof WYSIWYGCollectableLabel) {
			return UIManager.getColor("Label.background");
		} else if (c instanceof WYSIWYGCollectableTextfield) {
			return UIManager.getColor("TextField.background");
		} else if (c instanceof WYSIWYGCollectableTextArea) {
			return UIManager.getColor("TextArea.background");
		} else if (c instanceof WYSIWYGCollectableComboBox) {
			return UIManager.getColor("ComboBox.background");
		} else if (c instanceof WYSIWYGCollectableCheckBox) {
			return UIManager.getColor("CheckBox.background");
		} else if (c instanceof WYSIWYGCollectableDateChooser) {
			return UIManager.getColor("TextField.background");
		} else if (c instanceof WYSIWYGCollectableListOfValues) {
			return UIManager.getColor("TextField.background");
		} else if (c instanceof CollectableOptionGroup) {
			return UIManager.getColor("Panel.background");
		} else if (c instanceof WYSIWYGLayoutEditorPanel) {
			return UIManager.getColor("Panel.background");
		} else if (c instanceof WYSIWYGSubForm) {
			return UIManager.getColor("Table.background");
		} else {
			return null;
		}
	}
	
	/**
	 * This Method gets the default Border read from the UIManager.<br>
	 * @param c the {@link WYSIWYGComponent} to get the default Border for
	 * @return the Border for this specific {@link Component}, <b>null</b> if there was found nothing
	 */
	public static Border getDefaultBorder(WYSIWYGComponent c) {
		if (c instanceof WYSIWYGCollectableComponent) {
			return UIManager.getBorder("Panel.border");
		} else if (c instanceof WYSIWYGCollectableLabel || c instanceof WYSIWYGStaticLabel) {
			return UIManager.getBorder("Label.border");
		} else if (c instanceof WYSIWYGCollectableTextfield || c instanceof WYSIWYGStaticTextfield) {
			return UIManager.getBorder("TextField.border");
		} else if (c instanceof WYSIWYGCollectableTextArea || c instanceof WYSIWYGStaticTextarea) {
			return UIManager.getBorder("TextArea.border");
		} else if (c instanceof WYSIWYGCollectableComboBox || c instanceof WYSIWYGStaticComboBox) {
			return UIManager.getBorder("ComboBox.border");
		} else if (c instanceof WYSIWYGCollectableCheckBox) {
			return UIManager.getBorder("CheckBox.border");
		} else if (c instanceof WYSIWYGCollectableDateChooser) {
			return UIManager.getBorder("TextField.border");
		} else if (c instanceof WYSIWYGCollectableListOfValues) {
			return null;
		} else if (c instanceof CollectableOptionGroup) {
			return UIManager.getBorder("Panel.border");
		} else if (c instanceof WYSIWYGLayoutEditorPanel) {
			return UIManager.getBorder("Panel.border");
		} else if (c instanceof WYSIWYGSubForm) {
			return UIManager.getBorder("Table.border");
		} else if (c instanceof WYSIWYGTabbedPane) {
			return UIManager.getBorder("TabbedPane.border");
		} else if (c instanceof WYSIWYGSplitPane) {
			return UIManager.getBorder("SplitPane.border");
		} else if (c instanceof WYSIWYGScrollPane) {
			return UIManager.getBorder("ScrollPane.border");
		} else if (c instanceof WYSIWYGStaticButton) {
			return UIManager.getBorder("Button.border");
		} else {
			return null;
		}
	}
	
	/**
	 * This Method gets the default Font read from the UIManager.<br>
	 * @param c the {@link WYSIWYGComponent} to get the default Font for
	 * @return the Font for this specific {@link Component}, <b>null</b> if there was found nothing
	 */
	public static Font getDefaultFont(WYSIWYGComponent c) {
		if (c instanceof WYSIWYGCollectableLabel || c instanceof WYSIWYGStaticLabel || c instanceof WYSIWYGStaticTitledSeparator) {
			return UIManager.getFont("Label.font");
		} else if (c instanceof WYSIWYGCollectableTextfield || c instanceof WYSIWYGStaticTextfield) {
			return UIManager.getFont("TextField.font");
		} else if (c instanceof WYSIWYGCollectableTextArea || c instanceof WYSIWYGStaticTextarea) {
			return UIManager.getFont("TextArea.font");
		} else if (c instanceof WYSIWYGCollectableComboBox || c instanceof WYSIWYGStaticComboBox) {
			return UIManager.getFont("ComboBox.font");
		} else if (c instanceof WYSIWYGCollectableCheckBox) {
			return UIManager.getFont("CheckBox.font");
		} else if (c instanceof WYSIWYGCollectableDateChooser) {
			return UIManager.getFont("TextField.font");
		} else if (c instanceof WYSIWYGCollectableListOfValues) {
			return UIManager.getFont("TextField.font");
		} else if (c instanceof CollectableOptionGroup) {
			return UIManager.getFont("Panel.font");
		} else if (c instanceof WYSIWYGLayoutEditorPanel) {
			return UIManager.getFont("Panel.font");
		} else if (c instanceof WYSIWYGSubForm) {
			return UIManager.getFont("Table.font");
		} else if (c instanceof WYSIWYGTabbedPane) {
			return UIManager.getFont("TabbedPane.font");
		} else if (c instanceof WYSIWYGSplitPane) {
			return UIManager.getFont("SplitPane.font");
		} else if (c instanceof WYSIWYGScrollPane) {
			return UIManager.getFont("ScollPane.font");
		} else if (c instanceof WYSIWYGStaticButton) {
			return UIManager.getFont("Button.font");
		} else if (c instanceof WYSIWYGLayoutComponent) {
			return ((WYSIWYGLayoutComponent)c).getDefaultFont();
		} else{
			return null;
		}
	}
}
