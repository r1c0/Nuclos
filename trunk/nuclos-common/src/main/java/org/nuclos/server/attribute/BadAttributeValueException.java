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
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Fatal exception indicating a bad attribute value (in the database).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class BadAttributeValueException extends CommonValidationException {

	public final Integer iGenericObjectAttributeId;
	public final Integer iGenericObjectId;
	public final String sValue;
	public final Integer iAttributeId;
	public final String sAttributeName;
	public final Class<?> cls;
	public final String sCause;
	private final AttributeCVO attrcvo;

	/**
	 *
	 * @param iGenericObjectAttributeId
	 * @param iGenericObjectId
	 * @param sValue the String representation of the value in the database.
	 * @param iAttributeId
	 * @param attrcvo
	 * @param sCauseMessage
	 * @precondition attrcvo != null
	 */
	public BadAttributeValueException(Integer iGenericObjectAttributeId, Integer iGenericObjectId, String sValue,
			Integer iAttributeId, AttributeCVO attrcvo, String sCauseMessage) {
		this.iGenericObjectAttributeId = iGenericObjectAttributeId;
		this.iGenericObjectId = iGenericObjectId;
		this.sValue = sValue;
		this.iAttributeId = iAttributeId;
		this.sAttributeName = attrcvo.getName();
		this.cls = attrcvo.getClass();
		if (sCauseMessage == null) {
			sCause = "Undefined cause";
		}
		else {
			sCause = sCauseMessage;
		}
		this.attrcvo = attrcvo;
	}

	@Override
	public String getMessage() {
		final StringBuilder sb = new StringBuilder();
		sb.append(sAttributeName).append(" (Value: ").append(sValue).append(") : ").append(sCause);
		if (cls == String.class) {
			if (attrcvo.getInputFormat() != null) {
				sb.append(", expected input format: ").append(attrcvo.getInputFormat());
			}
		}
		else {
			final Object oMinValue = attrcvo.getMinValue();
			if (oMinValue != null) {
				sb.append(", minimum value: ").append(getStandardValueFormat(oMinValue));
			}
			final Object oMaxValue = attrcvo.getMaxValue();
			if (oMaxValue != null) {
				sb.append(", maximum value: ").append(getStandardValueFormat(oMaxValue));
			}
		}

		return sb.toString();
	}

	private String getStandardValueFormat(Object o) {
		if (o instanceof Date) {
			return new SimpleDateFormat("dd.MM.yyyy").format(o);
		}
		return o.toString();
	}

	public String getExtendedMessage() {
		return "The value of the attribute \"" + sAttributeName + "\" is invalid (" + sCause + ").\n" +
				 "Value: " + sValue + 
				 ", expected data type: " + cls + 
				 ",\nintid_t_ud_genericobject:" + iGenericObjectId + ", iGenericObjectAttributeId: " + iGenericObjectAttributeId;
//				"Der Wert des Attributs \"" + sAttributeName + "\" ist fehlerhaft (" + sCause + ").\n" +
//				"Wert: " + sValue +
//				", erwarteter Datentyp: " + cls +
//				",\nintid_t_ud_genericobject: " + iGenericObjectId + ", iGenericObjectAttributeId: " + iGenericObjectAttributeId;
	}

}	// class BadAttributeValueException
