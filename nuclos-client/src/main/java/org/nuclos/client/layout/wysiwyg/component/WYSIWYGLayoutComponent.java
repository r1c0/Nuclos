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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;

import org.nuclos.api.Preferences;
import org.nuclos.api.Property;
import org.nuclos.api.ui.Alignment;
import org.nuclos.api.ui.AlignmentH;
import org.nuclos.api.ui.AlignmentV;
import org.nuclos.api.ui.DefaultAlignment;
import org.nuclos.api.ui.LayoutComponent;
import org.nuclos.api.ui.LayoutComponentFactory;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.ERROR_MESSAGES;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PROPERTY_LABELS;
import org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.DnDUtil;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.TableLayoutPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRules;
import org.nuclos.client.ui.LayoutComponentHolder;
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
 * @author <a href="mailto:maik.stueker@nuclos.de">Maik Stueker</a>
 * @version 01.00.00
 */
public class WYSIWYGLayoutComponent extends LayoutComponentHolder implements WYSIWYGComponent, LayoutComponent, DefaultAlignment {
	
	public static final Alignment DEFAULT_ALIGNMENT = new Alignment(AlignmentH.FULL, AlignmentV.CENTER);

	public static final String PROPERTY_NAME = PROPERTY_LABELS.NAME;

	public static final String[][] PROPERTIES_TO_LAYOUTML_ATTRIBUTES = new String[][]{
		{PROPERTY_NAME, ATTRIBUTE_NAME}
	};
	
	/**
	 * <!ELEMENT layoutcomponent
	 * ((%layoutconstraints;)?,property*,(%borders;),(%sizes;),font?,description?)>
	 * <!ATTLIST layoutcomponent class CDATA #IMPLIED enabled (%boolean;) #IMPLIED
	 * editable (%boolean;) #IMPLIED >
	 */

	private ComponentProperties properties;
	
	private final LayoutComponentFactory lcf;
	
	private final LayoutComponent lc;
	
	private final JComponent dc;
	
	public WYSIWYGLayoutComponent(LayoutComponentFactory lcf) {
		super(lcf.newInstance(), true);
		
		this.lc = super.getLayoutComponent();
		this.dc = super.getHoldingComponent();
		this.lcf = lcf;
		
		DnDUtil.addDragGestureListener(this);
	}
	
	public Font getDefaultFont() {
		return dc.getFont();
	}
	
	@Override
	public void setComponentPopupMenu(JPopupMenu popup) {
		super.setComponentPopupMenu(popup);
		setComponentPopupMenu(popup, dc);
	}
	
	private void setComponentPopupMenu(JPopupMenu popup, Component c) {
		if (c instanceof JComponent) {
			((JComponent) c).setComponentPopupMenu(popup);
		}
		if (c instanceof Container) {
			for (Component child : ((Container)c).getComponents()) {
				setComponentPopupMenu(popup, child);
			}
		}
	}

	@Override
	public synchronized void addMouseListener(MouseListener l) {
		super.addMouseListener(l);
		addMouseListener(l, dc);
	}
	
	private void addMouseListener(MouseListener l, Component c) {
		if (c instanceof JComponent) {
			((JComponent) c).addMouseListener(l);
		}
		if (c instanceof Container) {
			for (Component child : ((Container)c).getComponents()) {
				addMouseListener(l, child);
			}
		}
	}

	public Property[] getAdditionalProperties() {
		return this.lc.getComponentProperties();
	}

	public String getLayoutComponentFactoryClass() {
		return this.lcf.getClass().getName();
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
		if (this.getParent() instanceof TableLayoutPanel) {
			return (WYSIWYGLayoutEditorPanel) this.getParent().getParent();
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
		List<PropertyClass> result = new ArrayList<PropertyClass>();
		result.add(new PropertyClass(PROPERTY_NAME, String.class));
		result.add(new PropertyClass(PROPERTY_BORDER, Border.class));
		result.add(new PropertyClass(PROPERTY_PREFFEREDSIZE, Dimension.class));
		
		if (lc.getComponentProperties() != null) {
			for (Property pt : lc.getComponentProperties()) {
				result.add(new PropertyClass(pt.name, pt.type));
			}
		}
		
		return result.toArray(new PropertyClass[]{});
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyNames()
	 */
	@Override
	public String[] getPropertyNames() {
		List<String> result = new ArrayList<String>();
		result.add(PROPERTY_NAME);
		result.add(PROPERTY_BORDER);
		result.add(PROPERTY_PREFFEREDSIZE);
		
		if (lc.getComponentProperties() != null) {
			for (Property pt : lc.getComponentProperties()) {
				result.add(pt.name);
			}
		}
				
		return result.toArray(new String[]{});
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertySetMethods()
	 */
	@Override
	public PropertySetMethod[] getPropertySetMethods() {
		List<PropertySetMethod> result = new ArrayList<WYSIWYGComponent.PropertySetMethod>();
		result.add(new PropertySetMethod(PROPERTY_NAME, "setName"));
		result.add(new PropertySetMethod(PROPERTY_BORDER, "setBorder"));
		result.add(new PropertySetMethod(PROPERTY_PREFFEREDSIZE, "setPreferredSize"));
		
		return result.toArray(new PropertySetMethod[]{});
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
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#setProperties(org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties)
	 */
	@Override
	public void setProperties(ComponentProperties properties) {
		this.properties = properties;
		String[] labels = lc.getComponentPropertyLabels();
		if (labels != null && lc.getComponentProperties() != null) {
			for (int i = 0; i < lc.getComponentProperties().length; i++) {
				if (labels.length > i) {
					Property pt = lc.getComponentProperties()[i];
					String label = labels[i];
					this.properties.setPropertyLabel(pt.name, label);
				}
			}
		}
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
	public void validateProperties(Map<String, PropertyValue<Object>> values) throws NuclosBusinessException {};
	
	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyFilters()
	 */
	@Override
	public PropertyFilter[] getPropertyFilters() {
		List<PropertyFilter> result = new ArrayList<WYSIWYGComponent.PropertyFilter>();
		result.add(new PropertyFilter(PROPERTY_NAME, ENABLED));
		result.add(new PropertyFilter(PROPERTY_BORDER, ENABLED));
		result.add(new PropertyFilter(PROPERTY_PREFFEREDSIZE, ENABLED));
		
		if (lc.getComponentProperties() != null) {
			for (Property pt : lc.getComponentProperties()) {
				result.add(new PropertyFilter(pt.name, ENABLED));
			}
		}
		
		return result.toArray(new PropertyFilter[]{});
	}

	@Override
	public void setPreferredSize(Dimension preferredSize) {
		if (lc != null)
			lc.setPreferredSize(preferredSize);
	}
	
	@Override
	public void setBorder(Border border) {
		super.setBorder(border);
	}

	@Override
	public void setProperty(String name, Object value) {
		lc.setProperty(name, value);
	}
	
	@Override
	public Property[] getComponentProperties() {
		return lc.getComponentProperties();
	}

	@Override
	public String[] getComponentPropertyLabels() {
		return lc.getComponentPropertyLabels();
	}

	@Override
	public Alignment getDefaultAlignment() {
		Alignment def = lcf.getDefaulAlignment();
		if (def == null) {
			return DEFAULT_ALIGNMENT;
		} else {
			return def;
		}
	}	
	
	@Override
	public JComponent getComponent() {
		return null; // not here
	}

	@Override
	public JComponent getDesignComponent() {
		return null; // not here
	}

	@Override
	public void setPreferences(Preferences prefs) {
	}

}
