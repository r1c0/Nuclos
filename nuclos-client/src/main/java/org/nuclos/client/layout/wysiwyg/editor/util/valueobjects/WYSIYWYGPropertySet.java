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
package org.nuclos.client.layout.wysiwyg.editor.util.valueobjects;

import java.io.Serializable;

/**
 * This ValueClass keeps a single Property.<br>
 * Multiple Properties are kept in {@link WYSIYWYGProperty}.
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
@SuppressWarnings("serial")
public class WYSIYWYGPropertySet implements Cloneable, Serializable{

	private String propertyName = "";
	private String propertyValue = "";
	
	/**
	 * 
	 * @param propertyName the Name of the Property
	 * @param propertyValue the Value of the Property
	 */
	public WYSIYWYGPropertySet(String propertyName, String propertyValue){
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
	}

	/**
	 * Empty Constructor
	 */
	public WYSIYWYGPropertySet() {}

	/**
	 * @return the Propertyname
	 */
	public String getPropertyName() {
		return propertyName;
	}
	
	/**
	 * @return the Propertyvalue
	 */
	public String getPropertyValue() {
		return propertyValue;
	}

	/**
	 * @param propertyName to set
	 */
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	/**
	 * @param propertyValue to set
	 */
	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}
	
	/**
	 * Overwritten clone Method for creating a new Instance of this Property
	 * called by {@link WYSIYWYGProperty#clone()}
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		String cloneName = new String(propertyName);
		String cloneValue = new String(propertyValue);
		WYSIYWYGPropertySet clonedProperty = new WYSIYWYGPropertySet(cloneName, cloneValue);
		
		return clonedProperty ;
	}
}
