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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.border.Border;

import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue;
import org.nuclos.client.ui.labeled.LabeledComponent;
import org.nuclos.client.ui.labeled.LabeledPasswordField;
import org.nuclos.common.NuclosBusinessException;

/**
 * 
 * 
 * NUCLEUSINT-1142
 * @author hartmut.beckschulze
 * @version 01.00.00
 */
public class WYSIWYGCollectablePasswordfield extends WYSIWYGCollectableComponent {

	private LabeledPasswordField component = new LabeledPasswordField();

	public WYSIWYGCollectablePasswordfield() {
		propertyNames.add(PROPERTY_FONT);

		propertySetMethods.put(PROPERTY_NAME, new PropertySetMethod(PROPERTY_NAME, "setName"));
		propertySetMethods.put(PROPERTY_COLUMNS, new PropertySetMethod(PROPERTY_COLUMNS, "setColumns"));
		propertySetMethods.put(PROPERTY_FILL_CONTROL_HORIZONTALLY, new PropertySetMethod(PROPERTY_FILL_CONTROL_HORIZONTALLY, "setFillControlHorizontally"));

		propertyFilters.put(PROPERTY_SHOWONLY, new PropertyFilter(PROPERTY_SHOWONLY, DISABLED));
		propertyFilters.put(PROPERTY_VALUELISTPROVIDER, new PropertyFilter(PROPERTY_VALUELISTPROVIDER, DISABLED));
		propertyFilters.put(PROPERTY_CONTROLTYPECLASS, new PropertyFilter(PROPERTY_CONTROLTYPECLASS, DISABLED));
		propertyFilters.put(PROPERTY_LABEL, new PropertyFilter(PROPERTY_LABEL, DISABLED));
		propertyFilters.put(PROPERTY_ROWS, new PropertyFilter(PROPERTY_ROWS, DISABLED));
		propertyFilters.put(PROPERTY_INSERTABLE, new PropertyFilter(PROPERTY_INSERTABLE, DISABLED));
		propertyFilters.put(PROPERTY_FILL_CONTROL_HORIZONTALLY, new PropertyFilter(PROPERTY_FILL_CONTROL_HORIZONTALLY, DISABLED));

		this.setLayout(new BorderLayout());
		this.add(component, BorderLayout.CENTER);
		this.addMouseListener();
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

	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.Component#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		if (component != null) {
			component.getJTextComponent().setText(name);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setBackground(java.awt.Color)
	 */
	@Override
	public void setBackground(Color bg) {
		if (component != null) {
			component.setBackground(bg);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setBorder(javax.swing.border.Border)
	 */
	@Override
	public void setBorder(Border border) {
		if (component != null) {
			component.setBorder(border);
		}
	}

	public void setColumns(int columns) {
		if (component != null) {
			component.setColumns(columns);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		if (component != null) {
			component.setEnabled(enabled);
		}
	}

	public void setFillControlHorizontally(boolean fill) {
		if (component != null) {
			component.setFillControlHorizontally(fill);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setFont(java.awt.Font)
	 */
	@Override
	public void setFont(Font font) {
		if (component != null) {
			// NUCLEUSINT-276 NUCLEUSINT-192
			((LabeledComponent) component).getJLabel().setFont(font);
			((LabeledComponent) component).getControlComponent().setFont(font);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setMinimumSize(java.awt.Dimension)
	 */
	@Override
	public void setMinimumSize(Dimension minimumSize) {
		if (component != null) {
			component.setMinimumSize(minimumSize);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setOpaque(boolean)
	 */
	@Override
	public void setOpaque(boolean isOpaque) {
		if (component != null) {
			component.setOpaque(isOpaque);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setPreferredSize(java.awt.Dimension)
	 */
	@Override
	public void setPreferredSize(Dimension preferredSize) {
		if (component != null) {
			component.setPreferredSize(preferredSize);
		}
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

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableComponent#validateProperties(java.util.Map)
	 */
	@Override
	public void validateProperties(Map<String, PropertyValue<Object>> values) throws NuclosBusinessException {
	}

}
