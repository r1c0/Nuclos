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

import java.awt.MenuItem;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuItem;

import org.nuclos.client.layout.wysiwyg.WYSIWYGEditorModes;
import org.nuclos.client.layout.wysiwyg.WYSIWYGLayoutControllingPanel;
import org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PROPERTY_LABELS;
import org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertiesPanel;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueString;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.TableLayoutUtil;
import org.nuclos.client.layout.wysiwyg.editor.util.UndoRedoFunction;
import org.nuclos.client.layout.wysiwyg.editor.util.popupmenu.ComponentPopUp;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRules;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.layoutml.LayoutMLConstants;

/**
 * This interface builds the Base of all {@link WYSIWYGComponent}.<br>
 * Every Component <b>must</b> be a {@link WYSIWYGComponent}.
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public interface WYSIWYGComponent extends LayoutMLConstants, WYSIWYGEditorModes {
	
	/** common properties for all {@link WYSIWYGComponent} */
	public static final String CONSTRAINT_COL1 = PROPERTY_LABELS.CONSTRAINT_COL1;
	public static final String CONSTRAINT_COL2 = PROPERTY_LABELS.CONSTRAINT_COL2;
	public static final String CONSTRAINT_ROW1 = PROPERTY_LABELS.CONSTRAINT_ROW1;
	public static final String CONSTRAINT_ROW2 =PROPERTY_LABELS.CONSTRAINT_ROW2;
	public static final String CONSTRAINT_HALIGN =PROPERTY_LABELS.CONSTRAINT_HALIGN;
	public static final String CONSTRAINT_VALIGN = PROPERTY_LABELS.CONSTRAINT_VALIGN;
	public static final String PROPERTY_VALUELISTPROVIDER = PROPERTY_LABELS.VALUELISTPROVIDER;
	public static final String PROPERTY_BACKGROUNDCOLOR = PROPERTY_LABELS.BACKGROUNDCOLOR;
	public static final String PROPERTY_BORDER = PROPERTY_LABELS.BORDER;
	public static final String PROPERTY_FONT = PROPERTY_LABELS.FONT;
	public static final String PROPERTY_DESCRIPTION = PROPERTY_LABELS.DESCRIPTION;
	public static final String PROPERTY_PREFFEREDSIZE = PROPERTY_LABELS.PREFFEREDSIZE;
	public static final String PROPERTY_ENABLED = PROPERTY_LABELS.ENABLED;
	public static final String PROPERTY_VISIBLE = PROPERTY_LABELS.VISIBLE;
	public static final String PROPERTY_OPAQUE = PROPERTY_LABELS.OPAQUE;
	public static final String PROPERTY_ROWS = PROPERTY_LABELS.ROWS;
	public static final String PROPERTY_COLUMNS =PROPERTY_LABELS.COLUMNS;
	public static final String PROPERTY_TRANSLATIONS = PROPERTY_LABELS.TRANSLATIONS;
	public static final String PROPERTY_NEXTFOCUSCOMPONENT = PROPERTY_LABELS.NEXTFOCUSCOMPONENT;
	
	public static Class<Integer> CONSTRAINT_CLASS = Integer.class;
	public static String[] CONSTRAINT_NAMES = new String[] {CONSTRAINT_COL1, CONSTRAINT_COL2, CONSTRAINT_ROW1, CONSTRAINT_ROW2, CONSTRAINT_HALIGN, CONSTRAINT_VALIGN};

	/**
	 * Every {@link WYSIWYGComponent} has Properties.<br>
	 * @see ComponentProperties
	 * 
	 * @return the {@link ComponentProperties} for this {@link WYSIWYGComponent}
	 */
	public ComponentProperties getProperties();
	
	/**
	 * Add a single Property to this Component
	 * @param property the Property to add (e.g. PROPERTY_DESCRIPTION)
	 * @param value the {@link PropertyValue} to add
	 * @param valueClass the Class for this Value (e.g. String.class)
	 * @throws CommonBusinessException if something went wrong
	 */
	public void setProperty(String property, PropertyValue<?> value, Class<?> valueClass) throws CommonBusinessException;
	
	/**
	 * Setting a complete {@link ComponentProperties} set of Values
	 * @param properties the {@link ComponentProperties} to set
	 */
	public void setProperties(ComponentProperties properties);
	
	/**
	 * Get the Names valid for this {@link WYSIWYGComponent}
	 * @return a String[] with all the PropertyNames for this {@link WYSIWYGComponent}
	 */
	public String[] getPropertyNames();
	
	/**
	 * This Method returns a {@link PropertySetMethod}[] with all the setMethods for the related Property.<br>
	 * e.g. PROPERTY_FONT has the {@link PropertySetMethod} "mySetFontMethod"
	 * @return
	 */
	public PropertySetMethod[] getPropertySetMethods();
	
	/**
	 * This Method returns the Mapping of Property to Classes.<br>
	 * @see PropertyClass
	 * @return the Mapping of Properties to Java Classes (e.g. "Font" -> Font.class)
	 */
	public PropertyClass[] getPropertyClasses();
	
	/**
	 * This Method is needed for accessing the {@link WYSIWYGLayoutEditorPanel}.<br>
	 * Over this component everything can be accessed, like {@link TableLayoutUtil}, {@link UndoRedoFunction} etc.
	 * <br>
	 * If it returns <b>null</b> the Main {@link WYSIWYGLayoutEditorPanel} is reached, its the one embedded in the {@link WYSIWYGLayoutControllingPanel} 
	 *
	 * 
	 * @return the {@link WYSIWYGLayoutEditorPanel} which has all the Things needed for modifying, <b>null</b> if Top Level Panel
	 */
	public WYSIWYGLayoutEditorPanel getParentEditor();
	
	/**
	 * This Method returns the {@link LayoutMLConstants}ATTRIBUTE_VALUE connection to PROPERTYNAME.<br>
	 * This makes storing Properties as Attributes lots easier.
	 * 
	 * @return String[][] with the link of the Attribute to Propertyname
	 */
	public String[][] getPropertyAttributeLink();
	
	/**
	 * This Method can be used to create special {@link MenuItem} for a {@link WYSIWYGComponent}.<br>
	 * If a {@link MenuItem} has "-" as Name, a Seperator is created.<br>
	 * Its called from {@link ComponentPopUp}.<br>
	 * 
	 * @param xClick the x Positon (used for SubformColumns)
	 * @return a {@link List} of {@link MenuItem} with additional Contextmenu Items for the Component
	 */
	public List<JMenuItem> getAdditionalContextMenuItems(int xClick);
	
	/**
	 * Returns the Information needed to get the fitting Values from {@link WYSIWYGMetaInformation}
	 * 
	 * e.g. new String[][]{{PROPERTY_ENTITY, WYSIWYGMetaInformation.META_ENTITY_NAMES}};
	 * @return String[][] with the Values
	 */
	public String[][] getPropertyValuesFromMetaInformation();
	
	/**
	 * This Method returns a String[][] with the Values for a {@link PropertyValueString}.<br>
	 * Structure is:
	 * <ul>
	 * <li> String[PROPERTY_NAME][PROPERTY_VALUES] e.g. {{"Alignment"},{"Top","Left","Right","Buttom",}};
	 * </ul>
	 * @return a String[][] with the combobox Values
	 */
	public String[][] getPropertyValuesStatic();
	
	/**
	 * This Method returns {@link PropertyFilter}.<br>
	 * This makes "on the fly" Filtering of Propertys possible.<br>
	 * It is used in the {@link PropertiesPanel}<br>
	 * e.g. new PropertyFilter("Font", EXPERT_MODE);
	 * 
	 * @see PropertyFilter#PropertyFilter(String, int)
	 * @return {@link PropertyFilter}[] with the Filters
	 */
	public PropertyFilter[] getPropertyFilters();
	
	/**
	 * This Method is called everytime a Property was changed in the {@link PropertiesPanel}.<br>
	 * It is to be used for validating {@link PropertyValue}s. 
	 * @param values the Values coming directly from the {@link PropertiesPanel}
	 * @throws NuclosBusinessException is thrown if Validation of the Values failed.<br>
	 * The Message of the Exception is shown in a Dialog. Saving is not possible until valid Values are entered.
	 */
	public void validateProperties(Map<String, PropertyValue<Object>> values) throws NuclosBusinessException;
	
	/** 
	 * This method returns the LayoutMLRules object if the component is the possible source of a rule
	 * else it returns null.
	 * @return LayoutMLRules if enabled, otherwise <b>null</b>
	 */
	public LayoutMLRules getLayoutMLRulesIfCapable();
	
	
	/**
	 * Small Valueclass for storing the {@link PropertySetMethod}s.<br>
	 * 
	 * @see WYSIWYGComponent#getPropertySetMethods()
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	public class PropertySetMethod{
		String name;
		String method;
		
		/**
		 * @param name the Name of the Property (e.g. "Font")
		 * @param method the Method to be used to set this Property (e.g. "setMyFont")
		 */
		public PropertySetMethod(String name, String method){
			this.name = name;
			this.method = method;
		}
		
		/**
		 * @return the Name of the {@link PropertyValue}
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return the Method that will be used to set the Property
		 */
		public String getMethod() {
			return method;
		}
		
	}
	
	/**
	 * This Class is used for mapping the Property to a class.<br>
	 * e.g. PROPERTY_FONT - Font.class
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	public class PropertyClass{
		String name;
		Class<?> propertyClass;
		
		/**
		 * @param name the Name of the Property
		 * @param propertyClass the Class of the Property
		 */
		public PropertyClass(String name, Class<?> propertyClass){
			this.name = name;
			this.propertyClass = propertyClass;
		}

		/**
		 * @return the Name for this Property (e.g. "Font")
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return the Class mapped to this Property (e.g. Font.class)
		 */
		public Class<?> getPropertyClass() {
			return propertyClass;
		}
	}
	
	/**
	 * Small Helperclass for storing the Filterinformation.<br>
	 * Makes filtering of Properties possible.
	 * 
	 * @see WYSIWYGEditorModes
	 * @see WYSIWYGComponent#getPropertyFilters()
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	public class PropertyFilter {
		String name;
		int mode;
		
		/**
		 * Default Constructor
		 * @see WYSIWYGEditorModes
		 * @param name the Name of the Property (e.g. "Font")
		 * @param mode the Mode (e.g. EXPERT_MODE)
		 */
		public PropertyFilter(String name, int mode) {
			this.name = name;
			this.mode = mode;
		}
		
		/**
		 * @return return the Name of the {@link PropertyValue} for this Filter (e.g. "Font")
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * @see WYSIWYGEditorModes
		 * @return the Mode for this {@link PropertyFilter} 
		 */
		public int getMode() {
			return mode;
		}
		
		/**
		 * @see WYSIWYGEditorModes
		 * @param mode the Mode (e.g. EXPERT_MODE)
		 */
		public void setMode(int mode){
			this.mode = mode;
		}
	}
}
