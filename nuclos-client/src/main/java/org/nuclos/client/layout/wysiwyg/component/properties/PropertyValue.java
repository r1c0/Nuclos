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

import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.xml.sax.Attributes;

import org.nuclos.common2.layoutml.LayoutMLConstants;
import org.nuclos.client.layout.wysiwyg.LayoutMLLoader;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;

/**
 * This is the Interface for integrating a new Property in the System.
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public interface PropertyValue<T> extends Cloneable, Serializable, LayoutMLConstants {
	
	/**
	 * This Method sets a Value.<br>
	 * @param value the Value to be set
	 */
	void setValue(T value);
	
	/**
	 * @return get the Value 
	 */
	T getValue();
	
	/**
	 * This method sets {@link PropertyValue}s.<br>
	 * 
	 * @see LayoutMLLoader#startElement(Attributes)
	 * 
	 * @param attributeName
	 * @param attributes
	 */
	void setValue(String attributeName, Attributes attributes);	
	
	/**
	 * Returns the Value in a specific Class.<br>
	 * 
	 * @param cls the Class that should the return Value be in (e.g. Dimension.class)
	 * @param c the {@link WYSIWYGComponent} some Properties are read from (e.g.  {@link Border}
	 * @return the Value, may be null
	 */
	Object getValue(Class<?> cls, WYSIWYGComponent c);
	
	/**
	 * This Method returns a modified {@link TableCellEditor} for display in the {@link PropertiesPanel}.<br>
	 * It is for <b>editing</b> the Values and may be complex (like {@link JPanel} with a {@link JTextField}, a {@link JLabel} and a {@link JButton})
	 * @param c the {@link WYSIWYGComponent} some Properties are directly attached to
	 * @param property the Name of the Property
	 * @param dialog The dialog for getting values from other Properties
	 * @return the {@link TableCellEditor}
	 */
	TableCellEditor getTableCellEditor(WYSIWYGComponent c, String property, PropertiesPanel dialog);
	
	/**
	 * This Method returns a modified {@link TableCellRenderer} for display in the {@link PropertiesPanel}.<br>
	 * Its only for display use, if the cell is edited it switches to {@link #getTableCellEditor(WYSIWYGComponent, String, PropertiesPanel)}
	 * @param c the {@link WYSIWYGComponent} some Properties are directly attached to
	 * @param property the Name of the Property
	 * @param dialog The dialog for getting values from other Properties
	 * @return the {@link TableCellRenderer}
	 */
	TableCellRenderer getTableCellRenderer(WYSIWYGComponent c, String property, PropertiesPanel dialog);
	
	/**
	 * This method is used to created a cloned instance of this {@link PropertyValue}.
	 * @return a new Instance of this {@link PropertyValue}
	 * @throws CloneNotSupportedException
	 */
	Object clone() throws CloneNotSupportedException;
}
