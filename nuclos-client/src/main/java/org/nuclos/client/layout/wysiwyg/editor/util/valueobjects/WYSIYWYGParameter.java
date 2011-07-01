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

import org.nuclos.common2.layoutml.LayoutMLConstants;

/**
 * This class represents the LayoutML Tag {@link LayoutMLConstants#ELEMENT_PARAMETER}<br>
 * It contains:
 * <ul>
 * <li>{@link #getParameterName()}</li>
 * <li>{@link #getParameterValue()}</li>
 * </ul>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
@SuppressWarnings("serial")
public class WYSIYWYGParameter implements Cloneable, Serializable{

	private String parameterName = "";
	private String parameterValue = "";
	private Integer maximumNumberOfUsage = null;
	
	/**
	 * @param parameterName The name of this parameter
	 * @param parameterValue The value of this parameter
	 */
	public WYSIYWYGParameter(String parameterName, String parameterValue){
		this.parameterName = parameterName;
		this.parameterValue = parameterValue;
	}
	
	/**
	 * 
	 * @param parameterName
	 * @param parameterValue
	 * @param maximumNumberOfUsage
	 */
	public WYSIYWYGParameter(String parameterName, String parameterValue, Integer maximumNumberOfUsage){
		this.parameterName = parameterName;
		this.parameterValue = parameterValue;
		this.maximumNumberOfUsage = maximumNumberOfUsage;
	}

	/**
	 * Empty Constructor
	 */
	public WYSIYWYGParameter() {}

	/**
	 * @return the Name of the Parameter
	 */
	public String getParameterName() {
		return parameterName;
	}
	
	/**
	 * @return the Value of the Parameter
	 */
	public String getParameterValue() {
		return parameterValue;
	}

	/**
	 * @param parameterName for setting the Name
	 */
	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	/**
	 * @param parameterValue for setting the Value
	 */
	public void setParameterValue(String parameterValue) {
		this.parameterValue = parameterValue;
	}
	
	/**
	 * How many times can this Parameter be used?
	 * @param value null if infinite
	 */
	public void setMaximumNumberOfUsage(Integer value) {
		this.maximumNumberOfUsage = value;
	}
	
	/**
	 * Returns the maximum number of times this parameter can be used
	 * @return null if infinite, else the amount
	 */
	public Integer getMaximumNumberOfUsage(){
		return maximumNumberOfUsage;
	}

	/**
	 * Overwritten clone Method, creating a new Instance of this Object
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		String cloneName = new String(parameterName);
		String cloneValue = new String(parameterValue);
		Integer cloneUsage = null;
		if (maximumNumberOfUsage != null)
			cloneUsage = new Integer(maximumNumberOfUsage);
		WYSIYWYGParameter clonedParameter = new WYSIYWYGParameter(cloneName, cloneValue, cloneUsage);
		
		return clonedParameter;
	}
}
