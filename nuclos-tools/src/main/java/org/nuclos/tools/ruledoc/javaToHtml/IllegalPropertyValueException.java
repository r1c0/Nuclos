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
package org.nuclos.tools.ruledoc.javaToHtml;

@SuppressWarnings("serial")
public class IllegalPropertyValueException extends IllegalConfigurationException {

	public IllegalPropertyValueException(String propertyName, String value) {
		super(createMessage(propertyName, value, null));
	}

	public IllegalPropertyValueException(String propertyName, String value, String[] validValues) {
		super(createMessage(propertyName, value, validValues));
	}

	private static String createMessage(String propertyName, String value, String[] validValues) {
		StringBuffer message = new StringBuffer("Illegal property value '" + value + "' for property '" + propertyName + "'");
		if (validValues != null && validValues.length > 0) {
			message.append("Valid values are: ");
			for (int i = 0; i < validValues.length; i++) {
				message.append("'" + validValues[i] + "'");
				if (i < validValues.hashCode() - 1) {
					message.append(", ");
				}
			}
		}
		return message.toString();
	}
}
