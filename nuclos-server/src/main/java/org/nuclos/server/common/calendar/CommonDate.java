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

import java.util.Date;
import java.util.TimeZone;

/**
 * CommonDate wrapper Class for BigDate which adds some needed functions.
 * CommonDate doesn't use time information in given java.util.Date
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars Rueckemann</a>
 * @version 01.00.00
 */
public class CommonDate extends BigDate {

	public static final int SUNDAY = 1;
	public static final int MONDAY = 2;
	public static final int TUESDAY = 3;
	public static final int WEDNESDAY = 4;
	public static final int THURSDAY = 5;
	public static final int FRIDAY = 6;
	public static final int SATURDAY = 7;

	// @todo: check if this is the best way to handle the timezone
	private static final TimeZone TIMEZONE = TimeZone.getDefault();
	private static final CommonCalendar calendar = CommonCalendar.getInstance();

	/**
	 *  constructor create a CommonDate for today
	 */
	public CommonDate() {
		this(new java.util.Date());
	}

	/**
	 *  create a CommonDate form a valid java.util.Date
	 * @param date
	 */
	public CommonDate(Date date) {
		super(date, TIMEZONE);
	}

	/**
	 * create ja CommonDate with specified values for year, month and day
	 * @param yyyy
	 * @param mm
	 * @param dd
	 */
	public CommonDate(int yyyy, int mm, int dd) {
		super(yyyy, mm, dd);
	}

	public CommonDate(int ordinal) {
		super(ordinal);
	}

	/**
	 * get the timezone for this CommonDate
	 * @return
	 */
	public static TimeZone getTimeZone() {
		return TIMEZONE;
	}

	/**
	 * add one business day to this CommonDate
	 * "Werktag" -> Montag bis Samstag ohne Feiertage
	 */
	private void addBusinessDay() {
		this.addDays(1);
		if (calendar.isHoliday(this.getOrdinal())) {
			addBusinessDay();
		}
	}

	/**
	 * add business days to this CommonDate
	 * "Werktag" -> Montag bis Samstag ohne Feiertage
	 * @param days
	 */
	public void addBusinessDays(int days) {
		for (int i = 0; i < days; i++) {
			addBusinessDay();
		}
	}

	/**
	 * add one working day to this CommonDate
	 * "Arbeitstag" -> Montag bis Freitag ohne Feiertage
	 */
	private void addWorkingDay() {
		this.addDays(1);
		if (calendar.isHoliday(this.getOrdinal()) || this.getCalendarDayOfWeek() == 7) {
			addWorkingDay();
		}
	}

	/**
	 * add working days to this CommonDate
	 * "Arbeitstag" -> Montag bis Freitag ohne Feiertage
	 * @param days
	 */
	public void addWorkingDays(int days) {
		for (int i = 0; i < days; i++) {
			addWorkingDay();
		}
	}

	/**
	 * substract count days form this CommonDate
	 * @param days
	 */
	public void substractDays(int days) {
		if (days == 0) {
			return;
		}
		this.ordinal -= days;
		toGregorian();
	}

	/**
	 * substract one business day from this CommonDate
	 */
	private void substractBusinessDay() {
		this.substractDays(1);
		if (calendar.isHoliday(this.getOrdinal())) {
			this.substractBusinessDay();
		}
	}

	/**
	 * substract count days form this CommonDate
	 * @param days
	 */
	public void substractBusinessDays(int days) {
		for (int i = 0; i < days; i++) {
			substractBusinessDay();
		}
	}

	/**
	 * create a java.util.Date from this CommonDate
	 * @return
	 */
	public Date toDate() {
		return this.getLocalDate();
	}

	public void substractMonth(int month) {
		while (month > 0) {
			this.substractDays(CommonDate.daysInMonth(this.getMM(), this.getYYYY()));
			month--;
		}
	}

	public void addMonth(int month) {
		while (month > 0) {
			if (this.getMM() == 12) {
				this.addDays(CommonDate.daysInMonth(1, this.getYYYY() + 1));
			}
			else {
				this.addDays(CommonDate.daysInMonth(this.getMM() + 1, this.getYYYY()));
			}
			month--;
		}
	}
}
