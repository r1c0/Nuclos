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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.event.EventListenerList;

import org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.ERROR_MESSAGES;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PROPERTY_LABELS;
import org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.PropertiesSorter;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.TableLayoutPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGValuelistProvider;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGProperty;
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
public abstract class WYSIWYGCollectableComponent extends JPanel implements WYSIWYGComponent, MouseListener {

	public static final String PROPERTY_NAME = PROPERTY_LABELS.NAME;
	public static final String PROPERTY_SHOWONLY = PROPERTY_LABELS.SHOWONLY;
	public static final String PROPERTY_ENABLED = PROPERTY_LABELS.ENABLED;
	public static final String PROPERTY_VISIBLE = PROPERTY_LABELS.VISIBLE;
	public static final String PROPERTY_OPAQUE = PROPERTY_LABELS.OPAQUE;
	public static final String PROPERTY_INSERTABLE = PROPERTY_LABELS.INSERTABLE;
	public static final String PROPERTY_MNEMONIC = PROPERTY_LABELS.MNEMONIC;
	public static final String PROPERTY_CONTROLTYPE = PROPERTY_LABELS.CONTROLTYPE;
	public static final String PROPERTY_CONTROLTYPECLASS = PROPERTY_LABELS.CONTROLTYPECLASS;
	public static final String PROPERTY_LABEL = PROPERTY_LABELS.LABEL;
	public static final String PROPERTY_FILL_CONTROL_HORIZONTALLY = PROPERTY_LABELS.FILL_HORIZONTALLY;
	public static final String PROPERTY_COLLECTABLECOMPONENTPROPERTY = PROPERTY_LABELS.COLLECTABLECOMPONENTPROPERTY;
	public static final String PROPERTY_OPTIONS = "options";
	public static final String PROPERTY_NEXTFOCUSCOMPONENT = PROPERTY_LABELS.NEXTFOCUSCOMPONENT;
	
	protected ComponentProperties properties;
	protected LayoutMLRules componentsRules = new LayoutMLRules();
	
	protected List<String> propertyNames = new ArrayList<String>();
	protected Map<String, String> propertiesToAttributes = new HashMap<String, String>();
	protected Map<String, PropertyClass> propertyClasses = new HashMap<String, PropertyClass>();
	protected Map<String, PropertySetMethod> propertySetMethods = new HashMap<String, PropertySetMethod>();
	protected Map<String, PropertyFilter> propertyFilters = new HashMap<String, PropertyFilter>();
	
	{
		propertyNames.add(PROPERTY_NAME);
		propertyNames.add(PROPERTY_PREFFEREDSIZE);
		propertyNames.add(PROPERTY_ENABLED);
		propertyNames.add(PROPERTY_VISIBLE);
		propertyNames.add(PROPERTY_OPAQUE);
		propertyNames.add(PROPERTY_INSERTABLE);
		propertyNames.add(PROPERTY_COLUMNS);
		propertyNames.add(PROPERTY_MNEMONIC);
		propertyNames.add(PROPERTY_BACKGROUNDCOLOR);
		propertyNames.add(PROPERTY_BORDER);
		propertyNames.add(PROPERTY_COLLECTABLECOMPONENTPROPERTY);
		propertyNames.add(PROPERTY_SHOWONLY);
		propertyNames.add(PROPERTY_CONTROLTYPECLASS);
		propertyNames.add(PROPERTY_LABEL);
		propertyNames.add(PROPERTY_ROWS);
		propertyNames.add(PROPERTY_FILL_CONTROL_HORIZONTALLY);		
		propertyNames.add(PROPERTY_VALUELISTPROVIDER);
		propertyNames.add(PROPERTY_DESCRIPTION);
		propertyNames.add(PROPERTY_TRANSLATIONS);
		propertyNames.add(PROPERTY_NEXTFOCUSCOMPONENT);
		
		propertiesToAttributes.put(PROPERTY_NAME, ATTRIBUTE_NAME);
		propertiesToAttributes.put(PROPERTY_ENABLED, ATTRIBUTE_ENABLED);
		propertiesToAttributes.put(PROPERTY_VISIBLE, ATTRIBUTE_VISIBLE);
		propertiesToAttributes.put(PROPERTY_OPAQUE, ATTRIBUTE_OPAQUE);
		propertiesToAttributes.put(PROPERTY_INSERTABLE, ATTRIBUTE_INSERTABLE);
		propertiesToAttributes.put(PROPERTY_COLUMNS, ATTRIBUTE_COLUMNS);
		propertiesToAttributes.put(PROPERTY_MNEMONIC, ATTRIBUTE_MNEMONIC);
		propertiesToAttributes.put(PROPERTY_CONTROLTYPECLASS, ATTRIBUTE_CONTROLTYPECLASS);
		propertiesToAttributes.put(PROPERTY_LABEL, ATTRIBUTE_LABEL);
		propertiesToAttributes.put(PROPERTY_ROWS, ATTRIBUTE_ROWS);
		propertiesToAttributes.put(PROPERTY_FILL_CONTROL_HORIZONTALLY, ATTRIBUTE_FILLCONTROLHORIZONTALLY);
		propertiesToAttributes.put(PROPERTY_NEXTFOCUSCOMPONENT, ATTRIBUTE_NEXTFOCUSCOMPONENT);
		
		propertyClasses.put(PROPERTY_NAME, new PropertyClass(PROPERTY_NAME, String.class));
		propertyClasses.put(PROPERTY_SHOWONLY, new PropertyClass(PROPERTY_SHOWONLY, String.class));
		propertyClasses.put(PROPERTY_PREFFEREDSIZE, new PropertyClass(PROPERTY_PREFFEREDSIZE, Dimension.class));
		propertyClasses.put(PROPERTY_ENABLED, new PropertyClass(PROPERTY_ENABLED, boolean.class));
		propertyClasses.put(PROPERTY_VISIBLE, new PropertyClass(PROPERTY_VISIBLE, boolean.class));
		propertyClasses.put(PROPERTY_OPAQUE, new PropertyClass(PROPERTY_OPAQUE, boolean.class));
		propertyClasses.put(PROPERTY_INSERTABLE, new PropertyClass(PROPERTY_INSERTABLE, boolean.class));
		propertyClasses.put(PROPERTY_BACKGROUNDCOLOR, new PropertyClass(PROPERTY_BACKGROUNDCOLOR, Color.class));
		propertyClasses.put(PROPERTY_BORDER, new PropertyClass(PROPERTY_BORDER, Border.class));
		propertyClasses.put(PROPERTY_FONT, new PropertyClass(PROPERTY_FONT, Font.class));
		propertyClasses.put(PROPERTY_COLUMNS, new PropertyClass(PROPERTY_COLUMNS, int.class));
		propertyClasses.put(PROPERTY_MNEMONIC, new PropertyClass(PROPERTY_MNEMONIC, String.class));
		propertyClasses.put(PROPERTY_COLLECTABLECOMPONENTPROPERTY, new PropertyClass(PROPERTY_COLLECTABLECOMPONENTPROPERTY, WYSIYWYGProperty.class));
		propertyClasses.put(PROPERTY_CONTROLTYPECLASS, new PropertyClass(PROPERTY_CONTROLTYPECLASS, String.class));
		propertyClasses.put(PROPERTY_NEXTFOCUSCOMPONENT, new PropertyClass(PROPERTY_NEXTFOCUSCOMPONENT, String.class));
		//NUCLEUSINT-269
		propertyClasses.put(PROPERTY_CONTROLTYPE, new PropertyClass(PROPERTY_CONTROLTYPE, String.class));
		propertyClasses.put(PROPERTY_LABEL, new PropertyClass(PROPERTY_LABEL, String.class));
		propertyClasses.put(PROPERTY_ROWS, new PropertyClass(PROPERTY_ROWS, int.class));
		propertyClasses.put(PROPERTY_FILL_CONTROL_HORIZONTALLY, new PropertyClass(PROPERTY_FILL_CONTROL_HORIZONTALLY, boolean.class));
		propertyClasses.put(PROPERTY_VALUELISTPROVIDER, new PropertyClass(PROPERTY_VALUELISTPROVIDER, WYSIWYGValuelistProvider.class));
		propertyClasses.put(PROPERTY_DESCRIPTION, new PropertyClass(PROPERTY_DESCRIPTION, String.class));
		propertyClasses.put(PROPERTY_TRANSLATIONS, new PropertyClass(PROPERTY_TRANSLATIONS, TranslationMap.class));
		
		propertySetMethods.put(PROPERTY_PREFFEREDSIZE, new PropertySetMethod(PROPERTY_PREFFEREDSIZE, "setPreferredSize"));
		propertySetMethods.put(PROPERTY_ENABLED, new PropertySetMethod(PROPERTY_ENABLED, "setEnabled"));
		propertySetMethods.put(PROPERTY_OPAQUE, new PropertySetMethod(PROPERTY_OPAQUE, "setOpaque"));
		propertySetMethods.put(PROPERTY_BACKGROUNDCOLOR, new PropertySetMethod(PROPERTY_BACKGROUNDCOLOR, "setBackground"));
		propertySetMethods.put(PROPERTY_BORDER, new PropertySetMethod(PROPERTY_BORDER, "setBorder"));
		propertySetMethods.put(PROPERTY_FONT, new PropertySetMethod(PROPERTY_FONT, "setFont"));
		propertySetMethods.put(PROPERTY_DESCRIPTION, new PropertySetMethod(PROPERTY_DESCRIPTION, "setToolTipText"));
		
		propertyFilters.put(PROPERTY_NAME, new PropertyFilter(PROPERTY_NAME, STANDARD_AND_EXPERT_MODE));
		propertyFilters.put(PROPERTY_SHOWONLY, new PropertyFilter(PROPERTY_SHOWONLY, STANDARD_AND_EXPERT_MODE));
		propertyFilters.put(PROPERTY_PREFFEREDSIZE, new PropertyFilter(PROPERTY_PREFFEREDSIZE, STANDARD_AND_EXPERT_MODE));
		propertyFilters.put(PROPERTY_ENABLED, new PropertyFilter(PROPERTY_ENABLED, STANDARD_AND_EXPERT_MODE));
		propertyFilters.put(PROPERTY_VISIBLE, new PropertyFilter(PROPERTY_VISIBLE, STANDARD_AND_EXPERT_MODE));
		propertyFilters.put(PROPERTY_OPAQUE, new PropertyFilter(PROPERTY_OPAQUE, STANDARD_AND_EXPERT_MODE));
		propertyFilters.put(PROPERTY_INSERTABLE, new PropertyFilter(PROPERTY_INSERTABLE, STANDARD_AND_EXPERT_MODE));
		propertyFilters.put(PROPERTY_BACKGROUNDCOLOR, new PropertyFilter(PROPERTY_BACKGROUNDCOLOR, STANDARD_AND_EXPERT_MODE));
		propertyFilters.put(PROPERTY_BORDER, new PropertyFilter(PROPERTY_BORDER, STANDARD_AND_EXPERT_MODE));
		propertyFilters.put(PROPERTY_FONT, new PropertyFilter(PROPERTY_FONT, STANDARD_AND_EXPERT_MODE));
		propertyFilters.put(PROPERTY_COLUMNS, new PropertyFilter(PROPERTY_COLUMNS, STANDARD_AND_EXPERT_MODE));
		propertyFilters.put(PROPERTY_MNEMONIC, new PropertyFilter(PROPERTY_MNEMONIC, STANDARD_AND_EXPERT_MODE));
		propertyFilters.put(PROPERTY_COLLECTABLECOMPONENTPROPERTY, new PropertyFilter(PROPERTY_COLLECTABLECOMPONENTPROPERTY, EXPERT_MODE));
		propertyFilters.put(PROPERTY_CONTROLTYPECLASS, new PropertyFilter(PROPERTY_CONTROLTYPECLASS, EXPERT_MODE));
		propertyFilters.put(PROPERTY_LABEL, new PropertyFilter(PROPERTY_LABEL, STANDARD_AND_EXPERT_MODE));
		propertyFilters.put(PROPERTY_ROWS, new PropertyFilter(PROPERTY_ROWS, STANDARD_AND_EXPERT_MODE));
		propertyFilters.put(PROPERTY_FILL_CONTROL_HORIZONTALLY, new PropertyFilter(PROPERTY_FILL_CONTROL_HORIZONTALLY, STANDARD_AND_EXPERT_MODE));
		propertyFilters.put(PROPERTY_VALUELISTPROVIDER, new PropertyFilter(PROPERTY_VALUELISTPROVIDER, EXPERT_MODE));
		propertyFilters.put(PROPERTY_DESCRIPTION, new PropertyFilter(PROPERTY_DESCRIPTION, STANDARD_AND_EXPERT_MODE));
		propertyFilters.put(PROPERTY_NEXTFOCUSCOMPONENT, new PropertyFilter(PROPERTY_NEXTFOCUSCOMPONENT, STANDARD_AND_EXPERT_MODE));
	}
	
	private EventListenerList listenerList = new EventListenerList();
	
	public static final String[][] PROPERTY_VALUES_FROM_METAINFORMATION = new String[][] {
		{PROPERTY_NAME, WYSIWYGMetaInformation.META_FIELD_NAMES},
		{PROPERTY_SHOWONLY, WYSIWYGMetaInformation.META_SHOWONLY},
		{PROPERTY_NEXTFOCUSCOMPONENT, WYSIWYGMetaInformation.META_FIELD_NAMES} 
	};
	
	/**
	 * This Method is called to recreate the Component for the View.<br>
	 * Its needed for refreshing the Subform and some other complex Components.<br>
	 */
	protected abstract void render();
	
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
		return this.componentsRules;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getParentEditor()
	 */
	@Override
    public WYSIWYGLayoutEditorPanel getParentEditor() {
		if (super.getParent() instanceof TableLayoutPanel) {
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
		String[][] result = new String[propertiesToAttributes.size()][2];
		int i = 0;
		for (Map.Entry<String, String> e : propertiesToAttributes.entrySet()) {
			result[i][0] = e.getKey();
			result[i][1] = e.getValue();
			i++;
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyClasses()
	 */
	@Override
    public PropertyClass[] getPropertyClasses() {
		return propertyClasses.values().toArray(new PropertyClass[propertyClasses.size()]);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyNames()
	 */
	@Override
    public String[] getPropertyNames() {
		//NUCLEUSINT-366
		return PropertiesSorter.sortPropertyNames(propertyNames.toArray(new String[propertyNames.size()]));
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertySetMethods()
	 */
	@Override
    public PropertySetMethod[] getPropertySetMethods() {
		return propertySetMethods.values().toArray(new PropertySetMethod[propertySetMethods.size()]);
	}

	/*
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
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyFilters()
	 */
	@Override
    public PropertyFilter[] getPropertyFilters() {
		return propertyFilters.values().toArray(new PropertyFilter[propertyFilters.size()]);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#setProperties(org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties)
	 */
	@Override
    public void setProperties(ComponentProperties properties) {
		this.properties = properties;
		this.render();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#setProperty(java.lang.String, org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue, java.lang.Class)
	 */
	@Override
	public void setProperty(String property, PropertyValue<?> value, Class<?> valueClass) throws CommonBusinessException {
		this.properties.setProperty(property, value, valueClass);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#validateProperties(java.util.Map)
	 */
	@Override
    public void validateProperties(Map<String, PropertyValue<Object>> values) throws NuclosBusinessException {
	}
	
	@Override
	public synchronized void addMouseListener(MouseListener l) {
		super.addMouseListener(l);
		listenerList.add(MouseListener.class, l);
	}

	@Override
	public synchronized void removeMouseListener(MouseListener l) {
		super.removeMouseListener(l);
		listenerList.remove(MouseListener.class, l);
	}
	
	protected void addMouseListener() {
		if (getComponents() != null && getComponents().length > 0) {
			addMouseListenerDeep((JComponent)getComponents()[0]);
		}
	}
	
	protected void removeMouseListener() {
		if (getComponents() != null && getComponents().length > 0) {
			removeMouseListenerDeep((JComponent)getComponents()[0]);
		}
	}

	@Override
    public void mouseClicked(MouseEvent e) {
		for (MouseListener l : listenerList.getListeners(MouseListener.class)) {
			l.mouseClicked(e);
		}
	}
	
	@Override
    public void mouseEntered(MouseEvent e) {
		for (MouseListener l : listenerList.getListeners(MouseListener.class)) {
			l.mouseEntered(e);
		}
	}
	
	@Override
    public void mouseExited(MouseEvent e) {
		for (MouseListener l : listenerList.getListeners(MouseListener.class)) {
			l.mouseExited(e);
		}
	}
	
	@Override
    public void mousePressed(MouseEvent e) {
		for (MouseListener l : listenerList.getListeners(MouseListener.class)) {
			l.mousePressed(e);
		}
	}
	
	@Override
    public void mouseReleased(MouseEvent e) {
		for (MouseListener l : listenerList.getListeners(MouseListener.class)) {
			l.mouseReleased(e);
		}
	}
	
	private void addMouseListenerDeep(Container component) {
		MouseListener[] oldListeners = component.getMouseListeners();
		for (MouseListener l : oldListeners) {
			component.removeMouseListener(l);
		}
		component.addMouseListener(this);
		for (Component c : component.getComponents()) {
			c.addMouseListener(this);
			if (c instanceof Container) {
				addMouseListenerDeep((Container)c);
			}
		}
	}
	
	private void removeMouseListenerDeep(Container component) {
		component.removeMouseListener(this);
		for (Component c : component.getComponents()) {
			c.removeMouseListener(this);
			if (c instanceof Container) {
				removeMouseListenerDeep((Container)c);
			}
		}
	}
	
	/**
	 * This Method draws a small red box on the {@link WYSIWYGComponent} to indicate existing {@link LayoutMLRules}
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		if (this.componentsRules.getSize() > 0) {
			Graphics2D g2d = (Graphics2D) g;

			g2d.setColor(Color.RED);
			g2d.fillRect(this.getWidth() - 10, 0, 10, 10);
		}
	}

	@Override
	public String getName() {
		if (properties != null) {
			return (String)properties.getProperty(PROPERTY_NAME).getValue();
		}
		return super.getName();
	}
	
	
}
