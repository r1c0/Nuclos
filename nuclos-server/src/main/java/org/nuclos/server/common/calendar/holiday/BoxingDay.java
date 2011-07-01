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

/**
 * Holiday calculator for 1. christmas.
 */
public class BoxingDay extends Holiday {

	static int boxingDayShift(int ordinal, boolean shift) {
		// This is complicated because preceeding Christmas day may be shifted too.
		if (shift) {
			switch (BigDate.dayOfWeek(ordinal)) {
				case 0:
					/* sunday */
					/* shift to Monday */
					return ordinal + 1;
				case 1:
					/* monday */
					/* shift to Tuesday */
					return ordinal + 1;
				case 2:
					/* tuesday */
				case 3:
					/* wednesday */
				case 4:
					/* thursday */
				case 5:
					/* friday */
				default :
					return ordinal;
				case 6:
					/* saturday */
					/* shift to Monday */
					return ordinal + 2;
			} // end switch
		} // end if
		else {
			return ordinal;
		}
	} // end boxingDayShift

	@Override
	public String getAuthority() {
		return "";
	}

	@Override
	public int getFirstYear(int base) {
		return 1;
	}

	@Override
	public String getName() {
		return "Boxing Day";
	}

	@Override
	public String getRule() {
		return "Day after Christmas.";
	}

	@Override
	public int when(int year, boolean shift, int base) {
		if (!isYearValid(year, base)) {
			return BigDate.NULL_ORDINAL;
		}
		return boxingDayShift(BigDate.toOrdinal(year, 12, 26), shift);
	} // end when.
}
