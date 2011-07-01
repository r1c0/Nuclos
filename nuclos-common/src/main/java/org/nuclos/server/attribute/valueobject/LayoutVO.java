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
package org.nuclos.server.attribute.valueobject;

import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.valueobject.NuclosValueObject;

/**
 * Value object representing a leased object layout. Immutable.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 00.01.000
 */
public class LayoutVO extends NuclosValueObject {

	private final String sName;
	private final String sDescription;
	private final String sLayoutML;

	/**
	 * constructor to be called by client only
	 * @param sName layout name of underlying database record
	 * @param sDescription layout description of underlying database record
	 * @param sLayoutML layout ml of underlying database record
	 */
	public LayoutVO(String sName, String sDescription, String sLayoutML) {
		super();
		this.sName = sName;
		this.sDescription = sDescription;
		this.sLayoutML = sLayoutML;
	}

	@Override
	public NuclosValueObject clone() {
		return super.clone();
	}

	public String getName() {
		return this.sName;
	}

	public String getDescription() {
		return this.sDescription;
	}

	public String getLayoutML() {
		return this.sLayoutML;
	}

	/**
	 * validity checker
	 */
	@Override
	public void validate() throws CommonValidationException {
		if (StringUtils.isNullOrEmpty(this.getName())) {
			throw new CommonValidationException("attribute.error.validation.layout.value");
		}
		if (StringUtils.isNullOrEmpty(this.getDescription())) {
			throw new CommonValidationException("attribute.error.validation.layout.description");
		}
		if (StringUtils.isNullOrEmpty(this.getLayoutML())) {
			throw new CommonValidationException("attribute.error.validation.layout.layoutml");
		}
	}

}	// class LayoutVO
