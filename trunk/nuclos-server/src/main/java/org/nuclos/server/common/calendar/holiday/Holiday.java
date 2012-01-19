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
 * Information about a single holiday.
 * base class for various holiday calculators.
 */

public abstract class Holiday {

	/**
	 * true if debugging. May turn on extra logging information.
	 */
	public static final boolean DEBUGGING = false;

	/**
	 * base calculations on date holiday was first officially proclaimed.
	 */
	public static final int PROCLAIMED = 0;

	/**
	 * base calculations on date holiday was first celebrated.
	 */
	public static final int CELEBRATED = 1;

	/**
	 * base calculations on the actual date the holiday is observed.
	 */
	public static final boolean ACTUAL = false;

	/**
	 * base calculations on the nearest weekday to the holiday.
	 */
	public static final boolean SHIFTED = true;

	/**
	 * authority who provided the information about the holiday.
	 *
	 * @return name of person, email address, website etc. that describes the
	 *         rules about the holiday.  ""  if no one in particular.
	 */
	abstract public String getAuthority();

	/**
	 * Get year holiday first proclaimed or first celebrated.
	 *
	 * @param base PROCLAIMED=based on date holiday was officially proclaimed.
	 * CELEBRATED=based on date holiday was first celebrated.
	 *
	 * @return year first proclaimed, or first celebrated.
	 *
	 */
	abstract public int getFirstYear(int base);

	/**
	 * Get name of holiday e.g. "Christmas"
	 *
	 * @return English language name of the holiday.
	 *
	 */
	abstract public String getName();

	/**
	 * Get rule in English for how the holiday is calculated. e.g.
	 * "Always on Dec 25." or "Third Monday in March."
	 * may contain embedded \n characters.
	 *
	 *
	 * @return rule for how holiday is computed.
	 *
	 */
	abstract public String getRule();

	/**
	 * Is year valid for this holiday?
	 *
	 * @param year	 The year you want to test.
	 * @param base PROCLAIMED=based on date holiday was officially proclaimed .
	 * CELEBRATED=based on date holiday was first celebrated.
	 * @return true if the holiday was celebrated/proclained by that year.
	 */
	final protected boolean isYearValid(int year, int base) {
		return (year != 0) && (year >= getFirstYear(base));
	} // isYearValid

	/**
	 * convert Saturdays to preceeding Friday, Sundays to following Monday.
	 *
	 * @param ordinal days since Jan 1, 1970.
	 * @param shift ACTUAL = false if you want the actual date of the holiday.
	 *        SHIFTED = true if you want the date taken off work, usually
	 *        the nearest weekday.
	 * @return adjusted ordinal
	 */
	final protected int shiftSatToFriSunToMon(int ordinal, boolean shift) {
		if (shift) {
			switch (BigDate.dayOfWeek(ordinal)) {
				case 0: /* sunday */
					/* shift to Monday */
					return ordinal + 1;

				case 1: /* monday */
				case 2: /* tuesday */
				case 3: /* wednesday */
				case 4: /* thursday */
				case 5: /* friday */
				default:
					/* leave as is */
					return ordinal;

				case 6: /* saturday */
					/* shift to Friday */
					return ordinal - 1;

			} // end switch
		} // end if
		else {
			return ordinal;
		}

	} // end shiftSatToFriSunToMon

	/**
	 * When was this holiday in a given year?, based on PROCLAIMED date.
	 *
	 * @param year	 must be 1583 or later.
	 * @return ordinal days since Jan 1, 1970.
	 */
	final public int when(int year) {
		return when(year, false, PROCLAIMED);
	}

	/**
	 * When was this holiday in a given year?, based on PROCLAIMED date.
	 * @param year must be 1583 or later.
	 * @param shift ACTUAL = false if you want the actual date of the holiday.
	 *        SHIFTED = true if you want the date taken off work, usually
	 *        the nearest weekday.
	 * @return ordinal days since Jan 1, 1970.
	 *
	 */
	final public int when(int year, boolean shift) {
		return when(year, shift, PROCLAIMED);

	}

	/**
	 * When was this holiday in a given year?
	 *
	 * @param year	 (-ve means BC, +ve means AD, 0 not permitted.)
	 * @param shift ACTUAL = false if you want the actual date of the holiday.
	 *        SHIFTED = true if you want the date taken off work, usually
	 *        the nearest weekday.
	 * @param base	 PROCLAIMED=based on date holiday was officially proclaimed
	 *               CELEBRATED=based on date holiday was first celebrated
	 * @return ordinal days since Jan 1, 1970.
	 *         return NULL_ORDINAL if the holiday was not celebrated in that year.
	 */
	abstract public int when(int year, boolean shift, int base);
}
