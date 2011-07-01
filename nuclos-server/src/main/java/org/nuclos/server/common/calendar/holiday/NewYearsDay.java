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
package org.nuclos.server.common.calendar.holiday;

import org.nuclos.server.common.calendar.BigDate;

// com.mindprod.holidays.NewYearsDay

/**
 * Holiday calculator for new years day.
 */

public class NewYearsDay extends Holiday {

	@Override
	public String getAuthority() {
		return "";
	}

	@Override
	public int getFirstYear(int base) {
		return -153;
	}

	@Override
	public String getName() {
		return "New Years Day";
	}

	@Override
	public String getRule() {
		return "Always on January 1";
	}

	@Override
	public int when(int year, boolean shift, int base) {
		if (!isYearValid(year, base)) {
			return BigDate.NULL_ORDINAL;
		}
		return shiftSatToFriSunToMon(BigDate.toOrdinal(year, 1, 1), shift);

	} // end when.
}
