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
package org.nuclos.server.genericobject.ejb3;

/**
 * Value object representing a leased object attribute to generate.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 01.00.000
 */
public class GeneratorGenericObjectAttributeVO {

	private Integer iAttributeId = null;
	private Integer iValueId = null;
	private String sValue = null;

	/**
	 * constructor to be called by server only
	 * @param iAttributeId attribute to generate
	 * @param iValueId value id for attribute
	 * @param sValue value for attribute
	 */
	GeneratorGenericObjectAttributeVO(Integer iAttributeId, Integer iValueId, String sValue) {
		this.iAttributeId = iAttributeId;
		this.iValueId = iValueId;
		this.sValue = sValue;
	}

	/**
	 * get attribute id
	 * @return attribute id
	 */
	public Integer getAttributeId() {
		return iAttributeId;
	}

	/**
	 * get value id
	 * @return value id
	 */
	public Integer getValueId() {
		return iValueId;
	}

	/**
	 * get value
	 * @return value
	 */
	public String getValue() {
		return sValue;
	}
}
