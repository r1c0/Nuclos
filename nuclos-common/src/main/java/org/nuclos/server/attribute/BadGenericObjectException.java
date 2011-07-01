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
package org.nuclos.server.attribute;

import org.nuclos.common2.exception.CommonValidationException;
import java.util.Collection;

/**
 * Fatal exception indicating bad attribute values (in the database).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class BadGenericObjectException extends CommonValidationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Integer iGenericObjectId;
	private Collection<BadAttributeValueException> collex;
	private final int iTotalAttributeCount;

	/**
	 * @param iGenericObjectId
	 * @param collex All elements must have <code>iGenericObjectId</code> as their <code>iGenericObjectId</code>.
	 */
	public BadGenericObjectException(Integer iGenericObjectId, Collection<BadAttributeValueException> collex, int iTotalAttributeCount) {
		this.iGenericObjectId = iGenericObjectId;
		this.collex = collex;
		this.iTotalAttributeCount = iTotalAttributeCount;
	}

	/**
	 * @return Collection<BadAttributeValueException>
	 */
	public Collection<BadAttributeValueException> getBadAttributeValueExceptions() {
		return this.collex;
	}

	@Override
	public String getMessage() {
		final StringBuffer sb = new StringBuffer("The following errors were occurred with the examination of the attribute values:\n");
			//"Folgende Fehler sind bei der \u00dcberpr\u00fcfung der Attributwerte ermittelt worden:\n");

		for (BadAttributeValueException ex : collex) {
			sb.append(ex.getMessage()).append("\n");
		}
		return sb.toString();
	}

	public String getExtendedMessage() {
		final StringBuffer sb = new StringBuffer("The following errors were occurred with the examination of the attribute values:\n");
			//"Folgende Fehler sind bei der \u00dcberpr\u00fcfung der Attributwerte ermittelt worden:\n");

		sb.append("intid_t_ud_genericobject: ");
		sb.append(iGenericObjectId);
		sb.append("\n");

		for (BadAttributeValueException ex : collex) {
			sb.append(ex.getExtendedMessage()).append("\n");
		}
		return sb.toString();
	}

	/**
	 * @return number of attributes (valid and bad ones)
	 */
	public int getTotalAttributeCount() {
		return this.iTotalAttributeCount;
	}

}	// class BadAttributeValueException
