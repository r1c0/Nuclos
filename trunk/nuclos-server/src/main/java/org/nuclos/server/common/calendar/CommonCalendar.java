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
package org.nuclos.server.common.calendar;

import org.nuclos.server.common.calendar.holiday.*;

/**
 * CommonCalendar.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars Rueckemann</a>
 * @version 01.00.00
 */
public class CommonCalendar {

	private static CommonCalendar instance;

	/**
	 * First year in range to compute holidays.
	 */
	private int firstYear;

	/**
	 * Last year in range to compute holidays.
	 */
	private int lastYear;
	/**
	 * ordinal of Jan 1 of first year to compute holidays.
	 */
	private int firstOrd;
	/**
	 * ordinal of Dec 31 of last year to compute holidays.
	 */
	private int lastOrd;

	/**
	 * corresponding bit is true means day is a holiday.
	 */
	private java.util.BitSet holidayBits;

	/**
	 * get first year in range to compute the holidays.
	 * @return year yyyy.
	 */
	public int getFirstYear() {
		return firstYear;
	}

	/**
	 * Get last year in range to compute holidays.
	 * @return year YYYY.
	 */
	public int getLastYear() {
		return lastYear;
	}

	public synchronized static CommonCalendar getInstance() {
		if (instance == null) {
			instance = new CommonCalendar();
		}
		return instance;
	}

	/**
	 * creates a NovabitCalender with the maximal supported range with a range from year 1 to 999999.
	 */
	private CommonCalendar() {
		//@todo find a solution that this works with the parameter entries from server and client side
		initialize(1970, 2099);
		//initialize(Integer.parseInt(ServerParameterProvider.getInstance().getParameter("CommonCalendar MinYear")), Integer.parseInt(ServerParameterProvider.getInstance().getParameter("CommonCalendar MaxYear")));
	}

	private CommonCalendar(CommonDate firstDay, CommonDate lastDay) {
		initialize(firstDay.getOrdinal(), lastDay.getOrdinal());
	}

	private CommonCalendar(int firstYear, int lastYear) {
		initialize(firstYear, lastYear);
	}

	private void initialize(int firstYear, int lastYear) {
		// avoid problem of non-existent year 0,  only support AD
		if (firstYear < 1) {
			throw new IllegalArgumentException("firstYear="
					+ firstYear
					+ " must be > 0.");
		}
		if (lastYear > BigDate.MAX_YEAR) {
			throw new IllegalArgumentException("lastYear="
					+ lastYear
					+ " must be <= "
					+ BigDate.MAX_YEAR
					+ ".");
		}
		if (lastYear < firstYear) {
			throw new IllegalArgumentException("firstYear="
					+ firstYear
					+ " must be <= lastYear="
					+ lastYear
					+ ".");
		}
		this.firstYear = firstYear;
		this.lastYear = lastYear;

		firstOrd = BigDate.toOrdinal(firstYear, 1, 1);
		lastOrd = BigDate.toOrdinal(lastYear, 12, 31);
		// starts off all zeros, nothing is declared a holiday yet
		holidayBits = new java.util.BitSet(lastOrd - firstOrd + 1);

		addGermanHolidays();
		addSundaysAsHolidays();
	}

	private void addGermanHolidays() {
		for (int year = firstYear; year <= lastYear; year++) {
			// Neujahr
			addHoliday(new NewYearsDay().when(year, false, Holiday.PROCLAIMED));
			// Aschermittwoch
			//addHoliday(new AshWednesday().when(year, false, Holiday.PROCLAIMED));
			// Karfreitag
			addHoliday(new GoodFriday().when(year, false, Holiday.PROCLAIMED));
			// Ostersonntag
			addHoliday(new EasterSunday().when(year, false, Holiday.PROCLAIMED));
			// Ostermontag
			addHoliday(new EasterMonday().when(year, false, Holiday.PROCLAIMED));
			// 1. May
			addHoliday(new FirstMay().when(year, false, Holiday.PROCLAIMED));
			// Christi Himmelfahrt
			addHoliday(new AscensionDay().when(year, false, Holiday.PROCLAIMED));
			//Pfingstmontag
			addHoliday(new PentecostDay().when(year, false, Holiday.PROCLAIMED));
			// Tag d. deutschen Einheit
			addHoliday(new GermanUnionDay().when(year, false, Holiday.PROCLAIMED));
			// Fronleichnam
			addHoliday(new CorpusChristiDay().when(year, false, Holiday.PROCLAIMED));
			//Heiligabend
			//addHoliday(new ChristmasEve().when(year, false, Holiday.PROCLAIMED));
			// 1 Weihnachten
			addHoliday(new Christmas().when(year, false, Holiday.PROCLAIMED));
			// 2. Weihnachten
			addHoliday(new BoxingDay().when(year, false, Holiday.PROCLAIMED));
		}
	}

	/**
	 * add another holiday to the holiday table.
	 *
	 * @param ordinal day to add as a holiday measured in days since Jan 1, 1970.
	 *                Note you must add it once for each year in the range.
	 *                We do not presume it falls the same day each year.
	 */
	public synchronized void addHoliday(int ordinal) {
		if (ordinal < firstOrd || ordinal > lastOrd) {
			// just ignore out of range or NULL_ORDINAL.
			// It is possible for shifted dates to wander a tad out of range.
			return;
		}
		holidayBits.set(ordinal - firstOrd);
	}

	/**
	 * add all Sundays in the year range as holidays
	 */
	private void addSundaysAsHolidays() {
		int ordFirstSunday = BigDate.ordinalOfnthXXXDay(1 /* first */,
				0 /* Sunday */,
				firstYear,
				1 /* January */);
		for (int i = ordFirstSunday; i <= lastOrd; i += 7) {
			addHoliday(i);
		} // end for
	}

	/**
	 * Is the given day a holiday?
	 * What constitutes a holiday is determined by the setHoliday calls
	 *
	 * @param ordinal days since Jan 1, 1970
	 * @return true if that day is a holiday.
	 */
	public boolean isHoliday(int ordinal) {
		if (ordinal < firstOrd || ordinal > lastOrd) {
			throw new IllegalArgumentException("out of range ordinal date: " + ordinal);
		}
		return holidayBits.get(ordinal - firstOrd);
	}

	/**
	 * Is the given day a holiday?
	 * What constitutes a holiday is determined by the setHoliday calls
	 * @param bigDate date to test
	 * @return true if that day is a holiday
	 */
	public boolean isHoliday(CommonDate bigDate) {
		return isHoliday(bigDate.getOrdinal());
	}

	/* business days (Werktage) are monday to saturday without holidays */
	public int getBusinessDayCount(CommonDate firstDay, CommonDate lastDay) throws IllegalArgumentException {
		if (firstDay.getOrdinal() > lastDay.getOrdinal()) {
			throw new IllegalArgumentException("lastDay has to be greater than or equal to firstDay");
		}
		int startOrd = firstDay.getOrdinal();
		int endOrd = lastDay.getOrdinal();
		int count = 0;
		for (int i = startOrd; i <= endOrd; i++) {
			if (!isHoliday(i)) {
				count++;
			}
		}
		return count;
	}
}
