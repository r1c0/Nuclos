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
/**
 * 
 */
package org.nuclos.server.processmonitor.valueobject;

import java.io.Serializable;
import java.util.GregorianCalendar;


/**
 * @author Maik Stueker
 *
 * value object of a Process State Runtime Format
 * 
 * minutes = 1
 * hours = 2
 * days = 3
 * weeks = 4
 * months = 5
 * 
 */
public class ProcessStateRuntimeFormatVO implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * GregorianCalendar.MINUTE
	 */
	public final static int MINUTE = GregorianCalendar.MINUTE;
	/**
	 * GregorianCalendar.HOUR
	 */
	public final static int HOUR = GregorianCalendar.HOUR;
	/**
	 * GregorianCalendar.DAY_OF_MONTH
	 */
	public final static int DAY = GregorianCalendar.DAY_OF_MONTH;
	/**
	 * GregorianCalendar.WEEK_OF_MONTH
	 */
	public final static int WEEK = GregorianCalendar.WEEK_OF_MONTH;
	/**
	 * GregorianCalendar.MONTH
	 */
	public final static int MONTH = GregorianCalendar.MONTH;
		
	private String label;
	private Integer value;;

	public ProcessStateRuntimeFormatVO(String label, Integer value) {
		this.label = label;
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	@Override
	public String toString(){
		return this.label;
	}

}
