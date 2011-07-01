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

import java.util.Date;

import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.valueobject.NuclosValueObject;

/**
 * Value object representing a possible value for a dynamic attribute.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 00.01.000
 */
public class AttributeValueVO extends NuclosValueObject {

	private String sValue;
	private String sMnemonic;
	private String sDescription;
	private Date dateValidFrom;
	private Date dateValidUntil;

	/**
	 * constructor to be called by server only
	 * @param evo contains the common fields.
	 * @param sValue value of underlying database record
	 * @param sMnemonic mnemonic of underlying database record
	 * @param sDescription description of underlying database record
	 * @param dateValidFrom valid from date of underlying database record
	 * @param dateValidUntil valid until date of underlying database record
	 */
	public AttributeValueVO(NuclosValueObject evo, String sValue, String sMnemonic, String sDescription,
			Date dateValidFrom, Date dateValidUntil) {
		super(evo);
		this.sValue = sValue;
		this.sMnemonic = sMnemonic;
		this.sDescription = sDescription;
		this.dateValidFrom = dateValidFrom;
		this.dateValidUntil = dateValidUntil;
	}

	/**
	 * constructor to be called by client only
	 * @param sValue value of underlying database record
	 */
	public AttributeValueVO(String sValue, String sMnemonic, String sDescription, Date dateValidFrom,
			Date dateValidUntil) {
		super();
		this.sValue = sValue;
		this.sMnemonic = sMnemonic;
		this.sDescription = sDescription;
		this.dateValidFrom = dateValidFrom;
		this.dateValidUntil = dateValidUntil;
	}

	/**
	 * get value of underlying database record
	 * @return value of underlying database record
	 */
	public String getValue() {
		return sValue;
	}

	/**
	 * set value of underlying database record
	 * @param sValue value of underlying database record
	 */
	public void setValue(String sValue) {
		this.sValue = sValue;
	}

	/**
	 * get mnemonic of underlying database record
	 * @return mnemonic of underlying database record
	 */
	public String getMnemonic() {
		return sMnemonic;
	}

	/**
	 * set mnemonic of underlying database record
	 * @param sMnemonic value of underlying database record
	 */
	public void setMnemonic(String sMnemonic) {
		this.sMnemonic = sMnemonic;
	}

	/**
	 * get description of underlying database record
	 * @return description of underlying database record
	 */
	public String getDescription() {
		return sDescription;
	}

	/**
	 * set description of underlying database record
	 * @param sDescription description of underlying database record
	 */
	public void setDescription(String sDescription) {
		this.sDescription = sDescription;
	}

	/**
	 * get valid from date of underlying database record
	 * @return valid from date of underlying database record
	 */
	public Date getValidFrom() {
		return dateValidFrom;
	}

	/**
	 * set valid from date of underlying database record
	 * @param dateValidFrom valid from date of underlying database record
	 */
	public void setValidFrom(Date dateValidFrom) {
		this.dateValidFrom = dateValidFrom;
	}

	/**
	 * get valid until date of underlying database record
	 * @return valid until date of underlying database record
	 */
	public Date getValidUntil() {
		return dateValidUntil;
	}

	/**
	 * set valid until date of underlying database record
	 * @param dateValidUntil valid until date of underlying database record
	 */
	public void setValidUntil(Date dateValidUntil) {
		this.dateValidUntil = dateValidUntil;
	}

	/**
	 * validity checker
	 */
	@Override
	public void validate() throws CommonValidationException {
		if ((this.getValue() == null) || (this.getValue()).equals("")) {
			throw new CommonValidationException("attribute.error.validation.attributevalue.value");
		}
	}

}	// class AttributeValueVO
