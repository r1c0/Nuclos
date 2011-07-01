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
package org.nuclos.common2;

import java.math.BigDecimal;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class for validation of (masterdata and/or attribute) values against regular expression or min/max values.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */
public final class ValueValidationHelper {
	private ValueValidationHelper() {
	}

	public static boolean validateInputFormat(Object oValue, String sPattern) {
		boolean result = true;

		if (oValue != null && sPattern != null) {
			if (oValue instanceof String) {
				// check against regex
				Pattern p = Pattern.compile(sPattern);
				Matcher m = p.matcher((String) oValue);
				result = m.matches();
			}
		}

		return result;
	}

	public static boolean validateBoundaries(Object oValue, String sPattern) {
		boolean result = true;

		if (oValue != null && sPattern != null && !(oValue instanceof String)) {
			String[] saMinMax = sPattern.split(" ");
			boolean hasMax = (saMinMax.length == 2);
			if (oValue instanceof Integer) {
				// min/max check
				if (!"".equals(saMinMax[0])) {
					if (((Integer) oValue).intValue() < Integer.parseInt(saMinMax[0])) {
						result = false;
					}
				}
				if (hasMax && !"".equals(saMinMax[1])) {
					if (((Integer) oValue).intValue() > Integer.parseInt(saMinMax[1])) {
						result = false;
					}
				}
			}
			else if (oValue instanceof Double) {
				// min/max check
				if (!"".equals(saMinMax[0])) {
					if (((Double) oValue).doubleValue() < Double.parseDouble(saMinMax[0])) {
						result = false;
					}
				}
				if (hasMax && !"".equals(saMinMax[1])) {
					if (((Double) oValue).doubleValue() > Double.parseDouble(saMinMax[1])) {
						result = false;
					}
				}
			}
			else if (oValue instanceof Date) {
				// min/max check
				if (!"".equals(saMinMax[0])) {
					if (((Date) oValue).getTime() < Long.parseLong(saMinMax[0])) {
						result = false;
					}
				}
				if (hasMax && !"".equals(saMinMax[1])) {
					if (((Date) oValue).getTime() > Long.parseLong(saMinMax[1])) {
						result = false;
					}
				}
			}
			else if (oValue instanceof BigDecimal) {
				// min/max check
				if (!"".equals(saMinMax[0])) {
					if (((BigDecimal) oValue).compareTo(new BigDecimal(saMinMax[0])) < 0) {
						result = false;
					}
				}
				if (hasMax && !"".equals(saMinMax[1])) {
					if (((BigDecimal) oValue).compareTo(new BigDecimal(saMinMax[1])) > 0) {
						result = false;
					}
				}
			}
		}
		// Boolean needs not be checked, Object cannot, also the diverse DocumentFile derivatives...

		return result;
	}
}
