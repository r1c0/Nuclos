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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.Border;

import org.nuclos.client.layout.wysiwyg.CollectableWYSIWYGLayoutEditor.WYSIWYGLayoutEditorChangeDescriptor;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.ERROR_MESSAGES;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.JSPLITPANE;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PROPERTY_LABELS;
import org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.PropertiesSorter;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.TableLayoutPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRules;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.layoutml.LayoutMLConstants;

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
public class WYSIWYGSplitPane extends JPanel implements WYSIWYGComponent{
	
	public static final String PROPERTY_NAME = PROPERTY_LABELS.NAME;
	public static final String PROPERTY_PREFFEREDSIZE = PROPERTY_LABELS.PREFFEREDSIZE;
	public static final String PROPERTY_ORIENTATION = PROPERTY_LABELS.ORIENTATION;
	public static final String PROPERTY_EXPANDABLE = PROPERTY_LABELS.EXPANDABLE;
	public static final String PROPERTY_DIVIDERSIZE = PROPERTY_LABELS.DIVIDERSIZE;
	public static final String PROPERTY_RESIZEWEIGHT = PROPERTY_LABELS.RESIZEWEIGHT;
	public static final String PROPERTY_CONTINUOUSLAYOUT = PROPERTY_LABELS.CONTINUOUSLAYOUT;
	
	public static final String[][] PROPERTIES_TO_LAYOUTML_ATTRIBUTES = new String[][] {
		{PROPERTY_NAME, ATTRIBUTE_NAME}, 
		{PROPERTY_ORIENTATION, ATTRIBUTE_ORIENTATION},
		{PROPERTY_EXPANDABLE, ATTRIBUTE_EXPANDABLE},
		{PROPERTY_DIVIDERSIZE, ATTRIBUTE_DIVIDERSIZE},
		{PROPERTY_RESIZEWEIGHT, ATTRIBUTE_RESIZEWEIGHT},
		{PROPERTY_CONTINUOUSLAYOUT, ATTRIBUTE_CONTINUOUSLAYOUT}
		};
	
	public static final String[][] PROTERTY_VALUES_STATIC = new String[][] {
		{PROPERTY_ORIENTATION, ATTRIBUTEVALUE_HORIZONTAL, ATTRIBUTEVALUE_VERTICAL}
		};
	
	private static final String[] PROPERTY_NAMES = new String[] {
		PROPERTY_NAME, 
		PROPERTY_PREFFEREDSIZE, 
		PROPERTY_ORIENTATION,
		PROPERTY_EXPANDABLE,
		PROPERTY_DIVIDERSIZE,
		PROPERTY_RESIZEWEIGHT,
		PROPERTY_CONTINUOUSLAYOUT,
		PROPERTY_BORDER
	};
	
	private static final PropertyClass[] PROPERTY_CLASSES = new PropertyClass[] {
		new PropertyClass(PROPERTY_NAME, String.class),
		new PropertyClass(PROPERTY_PREFFEREDSIZE, Dimension.class),
		new PropertyClass(PROPERTY_ORIENTATION, String.class),
		new PropertyClass(PROPERTY_EXPANDABLE, Boolean.class),
		new PropertyClass(PROPERTY_DIVIDERSIZE, int.class),
		new PropertyClass(PROPERTY_RESIZEWEIGHT, double.class),
		new PropertyClass(PROPERTY_CONTINUOUSLAYOUT, boolean.class),
		new PropertyClass(PROPERTY_BORDER, Border.class)
	};
	
	private static final PropertySetMethod[] PROPERTY_SETMETHODS = new PropertySetMethod[]{
		new PropertySetMethod(PROPERTY_NAME,"setName"),
		new PropertySetMethod(PROPERTY_PREFFEREDSIZE,"setPreferredSize"),
		new PropertySetMethod(PROPERTY_ORIENTATION,"setOrientation"),
		new PropertySetMethod(PROPERTY_EXPANDABLE,"setExpandable"),
		new PropertySetMethod(PROPERTY_DIVIDERSIZE,"setDividerSize"),
		new PropertySetMethod(PROPERTY_RESIZEWEIGHT,"setResizeWeight"),
		new PropertySetMethod(PROPERTY_CONTINUOUSLAYOUT,"setContinuousLayout"),
		new PropertySetMethod(PROPERTY_BORDER, "setBorder")
	};
	
	private static PropertyFilter[] PROPERTY_FILTERS = new PropertyFilter[] {
		new PropertyFilter(PROPERTY_NAME, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_PREFFEREDSIZE, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_ORIENTATION, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_EXPANDABLE, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_DIVIDERSIZE, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_RESIZEWEIGHT, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_CONTINUOUSLAYOUT, STANDARD_MODE | EXPERT_MODE),
		new PropertyFilter(PROPERTY_BORDER, STANDARD_MODE | EXPERT_MODE)
	};
	
	private ComponentProperties properties;
	
	private JSplitPane jSplit;
	
	private WYSIWYGLayoutEditorPanel firstEditor = null;
	private WYSIWYGLayoutEditorPanel secondEditor = null;
	
	private WYSIWYGLayoutEditorChangeDescriptor wysiwygLayoutEditorChangeDescriptor = null;
	
	public WYSIWYGSplitPane() {
		jSplit = new JSplitPane();
	}
	
	public void setEditors(WYSIWYGLayoutEditorPanel firstEditor, WYSIWYGLayoutEditorPanel secondEditor) {
		this.removeAll();
		
		this.firstEditor = firstEditor;
		this.secondEditor = secondEditor;
		
		this.firstEditor.getTableLayoutPanel().setEditorChangeDescriptor(this.wysiwygLayoutEditorChangeDescriptor);
		this.secondEditor.getTableLayoutPanel().setEditorChangeDescriptor(this.wysiwygLayoutEditorChangeDescriptor);

		this.setLayout(new BorderLayout());
		String orientation = null;
		if(properties != null){
			orientation = (String) properties.getProperty(PROPERTY_ORIENTATION).getValue();
		}
		
		this.add(this.firstEditor,   BorderLayout.NORTH);
		this.add(jSplit,        BorderLayout.CENTER);
		this.add(this.secondEditor,  BorderLayout.SOUTH);
		
		jSplit.setLeftComponent(this.firstEditor);
		jSplit.setRightComponent(this.secondEditor);
		
		//DnDUtil.addDragGestureListener(this, this);
	}
	
	public void setFirstEditor(WYSIWYGLayoutEditorPanel panel) {
		this.firstEditor = panel;
		if (this.firstEditor != null && this.secondEditor != null) {
			setEditors(firstEditor, secondEditor);
		}
	}
	
	public void setSecondEditor(WYSIWYGLayoutEditorPanel panel) {
		this.secondEditor = panel;
		if (this.firstEditor != null && this.secondEditor != null) {
			setEditors(firstEditor, secondEditor);
		}
	}
	
	public void setWYSIWYGLayoutEditorChangeDescriptor(WYSIWYGLayoutEditorChangeDescriptor wysiwygLayoutEditorChangeDescriptor){
		this.wysiwygLayoutEditorChangeDescriptor = wysiwygLayoutEditorChangeDescriptor;
		
		// set the change descriptor for the splitpanels
		if (firstEditor != null)
			firstEditor.getTableLayoutPanel().setEditorChangeDescriptor(this.wysiwygLayoutEditorChangeDescriptor);
		if (secondEditor != null)
			secondEditor.getTableLayoutPanel().setEditorChangeDescriptor(this.wysiwygLayoutEditorChangeDescriptor);
	}
		
	
	public void setOrientation(String orientation){
		if (jSplit != null){
			jSplit.setOrientation(
					LayoutMLConstants.ATTRIBUTEVALUE_HORIZONTAL.equals(orientation)? 
							JSplitPane.HORIZONTAL_SPLIT: 
							JSplitPane.VERTICAL_SPLIT);
		}
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
	public WYSIWYGLayoutEditorPanel getParentEditor(){
		if (super.getParent() instanceof TableLayoutPanel){
			return (WYSIWYGLayoutEditorPanel) super.getParent().getParent();
		}
		
		throw new CommonFatalException(ERROR_MESSAGES.PARENT_NO_WYSIWYG);
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
		return PROTERTY_VALUES_STATIC;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getLayoutMLRulesIfCapable()
	 */
	@Override
	public LayoutMLRules getLayoutMLRulesIfCapable() {
		return null;
	}
	
	public void setExpandable(Boolean value) {
		if (jSplit != null){
			jSplit.setOneTouchExpandable((value==null)?false:value);
		}
	}
	
	public void setDividerSize(int value) {
		if (jSplit != null) {
			jSplit.setDividerSize(value);
		}
	}
	
	public void setResizeWeight(double value) {
		if (jSplit != null) {
			jSplit.setResizeWeight(value);
		}
	}
	
	public void setContinuousLayout(boolean value) {
		if (jSplit != null) {
			jSplit.setContinuousLayout(value);
		}
	}
	
	@Override
	public void setBorder(Border border) {
		if (jSplit != null) {
			jSplit.setBorder(border);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#validateProperties(java.util.Map)
	 */
	@Override
	public void validateProperties(Map<String, PropertyValue<Object>> values) throws NuclosBusinessException {
		PropertyValue<Object> resizeweight = values.get(PROPERTY_RESIZEWEIGHT);
		if (resizeweight.getValue() != null && resizeweight.getValue() instanceof Double) {
			Double val = (Double)resizeweight.getValue();
			
			if (val < 0D || val > 1D) {
				throw new NuclosBusinessException(JSPLITPANE.ERRORMESSAGE_VALIDATION_RESIZEWEIGHT);
			}
		}
		
		PropertyValue<Object> dividersize = values.get(PROPERTY_DIVIDERSIZE);
		if (dividersize.getValue() != null && dividersize.getValue() instanceof Integer) {
			Integer val = (Integer)dividersize.getValue();
			
			if (val < InterfaceGuidelines.SPLITPANE_DIVIDERSIZE_MIN || val > InterfaceGuidelines.SPLITPANE_DIVIDERSIZE_MAX) {
				throw new NuclosBusinessException(JSPLITPANE.ERRORMESSAGE_VALIDATION_DIVIDERSIZE);
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
}
