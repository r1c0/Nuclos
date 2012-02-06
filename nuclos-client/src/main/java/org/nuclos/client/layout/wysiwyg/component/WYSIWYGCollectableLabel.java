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
import java.awt.Component;
import java.awt.Font;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.InvalidDnDOperationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JMenuItem;

import org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue;
import org.nuclos.client.layout.wysiwyg.datatransfer.TransferableComponent;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRules;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.labeled.LabeledComponent;
import org.nuclos.client.ui.labeled.LabeledTextField;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common2.StringUtils;

/**
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:maik.stueker@novabit.de">maik.stueker</a>
 * @version 01.00.00
 */
public class WYSIWYGCollectableLabel extends WYSIWYGCollectableComponent {

	//NUCLEUSINT-275 Uses LabeledComponnent instead of normal JLabel (insets from GridbagLayout caused the WYSIAWYG :) )
	private LabeledComponent component = new LabeledTextField();
		
	private WYSIWYGMetaInformation meta;
	
	public WYSIWYGCollectableLabel(WYSIWYGMetaInformation meta) {
		this.meta = meta;
		propertyNames.add(PROPERTY_FONT);
		
		//the label everything needed here, hide the control
		component.getControlComponent().setVisible(false);
		
		propertySetMethods.put(PROPERTY_NAME, new PropertySetMethod(PROPERTY_NAME, "setName"));
		//NUCLEUSINT-303
		propertySetMethods.put(PROPERTY_LABEL, new PropertySetMethod(PROPERTY_LABEL, "setName"));
		propertySetMethods.remove(PROPERTY_FILL_CONTROL_HORIZONTALLY);
		
		propertyFilters.put(PROPERTY_SHOWONLY, new PropertyFilter(PROPERTY_SHOWONLY, DISABLED));
		propertyFilters.put(PROPERTY_VALUELISTPROVIDER, new PropertyFilter(PROPERTY_VALUELISTPROVIDER, DISABLED));
		propertyFilters.put(PROPERTY_CONTROLTYPECLASS, new PropertyFilter(PROPERTY_CONTROLTYPECLASS, DISABLED));
		//NUCLEUSINT-303
		propertyFilters.put(PROPERTY_LABEL, new PropertyFilter(PROPERTY_LABEL, EXPERT_MODE));
		propertyFilters.put(PROPERTY_ROWS, new PropertyFilter(PROPERTY_ROWS, DISABLED));
		propertyFilters.put(PROPERTY_COLUMNS, new PropertyFilter(PROPERTY_COLUMNS, DISABLED));
		propertyFilters.put(PROPERTY_INSERTABLE, new PropertyFilter(PROPERTY_INSERTABLE, DISABLED));
		propertyFilters.put(PROPERTY_FILL_CONTROL_HORIZONTALLY, new PropertyFilter(PROPERTY_FILL_CONTROL_HORIZONTALLY, DISABLED));
		
		//NUCLEUSINT-447
		this.setLayout(new BorderLayout());
		this.add(component, BorderLayout.CENTER);
		this.addMouseListener();	
		this.addDragGestureListener();    
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableComponent#getAdditionalContextMenuItems(int)
	 */
	@Override
	public List<JMenuItem> getAdditionalContextMenuItems(int xClick) {
		List<JMenuItem> list = new ArrayList<JMenuItem>();
		return list;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableComponent#render()
	 */
	@Override
	protected void render() {
		//NUCLEUSINT-303
		setName("");
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setFont(java.awt.Font)
	 * NUCLEUSINT-275 setting the values on the label
	 */
	@Override
	public void setFont(Font font) {
		//FIX NUCLEUSINT-192
		if (this.component != null)
			this.component.getJLabel().setFont(font);
	}
	
	/**
	 * Method for setting the Displayname for the Label.
	 * If a custom Label is set it overrides the Name set for this Entity
	 * NUCLEUSINT-303
	 * NUCLEUSINT-275 setting the values on the label
	 */
	@Override
	public void setName(String name) {
		if (component != null) {
			String entityName = null;
			String label = null;
			if (properties != null) {
				entityName = (String) properties.getProperty(PROPERTY_NAME).getValue();
				label = (String) properties.getProperty(PROPERTY_LABEL).getValue();
			}
			
			if (!StringUtils.isNullOrEmpty(entityName))
				component.setLabelText(meta.getLabelForCollectableComponent(entityName));

			if (!StringUtils.isNullOrEmpty(label)) {
				component.setLabelText(label);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableComponent#validateProperties(java.util.Map)
	 */
	@Override
	public void validateProperties(Map<String, PropertyValue<Object>> values) throws NuclosBusinessException {
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableComponent#getLayoutMLRulesIfCapable()
	 */
	@Override
	public LayoutMLRules getLayoutMLRulesIfCapable() {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setToolTipText(java.lang.String)
	 */
	@Override
	public void setToolTipText(String toolTipText) {
		if (component != null) {
			component.setToolTipText(toolTipText);
		}
	}

}
