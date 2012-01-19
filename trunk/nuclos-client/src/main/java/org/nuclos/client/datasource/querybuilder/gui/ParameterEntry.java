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
package org.nuclos.client.datasource.querybuilder.gui;

/**
 * Parameter entry.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris@novabit.de">Boris</a>
 * @version 01.00.00
 */
public class ParameterEntry {

	public static final int BOOLEAN_TYPE = 1;
	public static final int INTEGER_TYPE = 2;
	public static final int NUMBER_TYPE = 4;
	public static final int STRING_TYPE = 8;
	public static final int DATE_TYPE = 16;

	private String sName;
	private int iDataType;

	/**
	 * @param iDataType
	 * @param sName
	 */
	public ParameterEntry(int iDataType, String sName) {
		this.iDataType = iDataType;
		this.sName = sName;
	}

	/**
	 *
	 */
	public ParameterEntry() {
	}

	/**
	 * @return data type
	 */
	public int getDataType() {
		return iDataType;
	}

	/**
	 * @param iDataType
	 */
	public void setDataType(int iDataType) {
		this.iDataType = iDataType;
	}

	/**
	 * @return name
	 */
	public String getName() {
		return sName;
	}

	/**
	 * @param sName
	 */
	public void setName(String sName) {
		this.sName = sName;
	}

}	// class ParameterEntry
