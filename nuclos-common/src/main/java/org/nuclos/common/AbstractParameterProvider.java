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
package org.nuclos.common;

import org.apache.log4j.Logger;

/**
 * Abstract implementation for <code>ParameterProvider</code>s.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public abstract class AbstractParameterProvider implements ParameterProvider {
	private final Logger log = Logger.getLogger(this.getClass());

	@Override
	public int getIntValue(String sParameterName, int iDefaultValue) {
		int result;
		try {
			result = Integer.parseInt(this.getValue(sParameterName));
		}
		catch (Exception ex) {
			log.warn("Parameter \"" + sParameterName + "\" cannot be retrieved from the parameter table.");
			result = iDefaultValue;
		}
		return result;
	}

	@Override
	public boolean isEnabled(String sParameterName) {
		String sValue = getValue(sParameterName);
		if (sValue != null) {
			sValue = sValue.trim();
			if ("1".equals(sValue) ||
					"true".equalsIgnoreCase(sValue) ||
					"enable".equals(sValue) ||
					"enabled".equals(sValue)) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public String[] getList(String sParameterName) {
		String sValue = getValue(sParameterName);
		if (sValue != null) {
			String[] result = sValue.split(";");
			for (int i = 0; i < result.length; i++) {
				result[i] = result[i].trim();
			}
			return result;
		}
		return new String[0];
	}

}	// class AbstractParameterProvider
