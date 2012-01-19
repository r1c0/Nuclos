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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import org.nuclos.common2.exception.CommonFatalException;

/**
 * Class containing general static helper functions.<br>
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 01.00.00
 * @todo move SQL related methods to SQLUtils.
 * @todo move Date related methods to DateUtils.
 * @see LangUtils
 * @see StringUtils
 * @see DateUtils
 */
public class Helper {

	private static final Logger log = Logger.getLogger(Helper.class);

	public Helper() {
	}

	/**
	 * @return the local JNDI context.
	 * @throws CommonFatalException if the local JNDI context cannot be initialized.
	 * @see InitialContext
	 * @todo this should probably be moved to ServiceLocator
	 */
	public static Context getLocalContext() {
		try {
			return new InitialContext();
		}
		catch (NamingException ex) {
			throw new CommonFatalException("Local JNDI context could not be initialized.", ex);
		}
	}

	/**
	 * subtracts two Double
	 * @param dMinuend minuend
	 * @param dSubtrahend subtrahend
	 * @return Double result
	 */
	public static Double subtract(Double dMinuend, Double dSubtrahend) {
		return new Double((dMinuend == null ? 0 : dMinuend.doubleValue()) - (dSubtrahend == null ? 0 : dSubtrahend.doubleValue()));
	}

	/**
	 * subtracts two Integer
	 * @param iMinuend minuend
	 * @param iSubtrahend subtrahend
	 * @return Integer result
	 */
	public static Integer subtract(Integer iMinuend, Integer iSubtrahend) {
		return new Integer((iMinuend == null ? 0 : iMinuend.intValue()) - (iSubtrahend == null ? 0 : iSubtrahend.intValue()));
	}

	/**
	 * returns the real day difference between two dates
	 * @param dateUntil until (later) date
	 * @param dateFrom from (earlier) date
	 * @param bAddDay is same day a difference of one?
	 * @return day difference
	 * @todo move to DateUtils
	 */
	public static int diffReal(Date dateUntil, Date dateFrom, boolean bAddDay) {
		int result = 0;
		if (dateFrom.after(dateUntil)) {
			result = -1;
		}
		else if (dateFrom.equals(dateUntil)) {
			result = (bAddDay ? 1 : 0);
		}
		else {
			final Calendar calFrom = new GregorianCalendar();
			calFrom.setTime(dateFrom);
			final Calendar calUntil = new GregorianCalendar();
			calUntil.setTime(dateUntil);
			result = calUntil.get(GregorianCalendar.DAY_OF_YEAR) - calFrom.get(GregorianCalendar.DAY_OF_YEAR) + (bAddDay ? 1 : 0);

			int iYearFrom = calFrom.get(GregorianCalendar.YEAR);
			final int iYearUntil = calUntil.get(GregorianCalendar.YEAR);

			while (iYearFrom < iYearUntil) {
				calFrom.clear();
				calFrom.set(iYearFrom, 11, 31);
				log.debug(calFrom.getTime());
				result += calFrom.get(GregorianCalendar.DAY_OF_YEAR);
				iYearFrom++;
			}
		}
		return result;
	}

	/**
	 * returns the commercial day difference between two dates
	 * @param dateUntil until (later) date
	 * @param dateFrom from (earlier) date
	 * @param bAddDay is same day a difference of one?
	 * @return day difference
	 */
	public static int diffCommercial(Date dateUntil, Date dateFrom, boolean bAddDay) {
		int result = 0;
		if (dateFrom.after(dateUntil)) {
			result = -1;
		}
		else if (dateFrom.equals(dateUntil)) {
			result = (bAddDay ? 1 : 0);
		}
		else {
			final Calendar calFrom = new GregorianCalendar();
			calFrom.setTime(dateFrom);
			final Calendar calUntil = new GregorianCalendar();
			calUntil.setTime(dateUntil);
			final Calendar calTemp = new GregorianCalendar();
			while (!calFrom.after(calUntil)) {
				calTemp.setTime(calFrom.getTime());
				calTemp.set(GregorianCalendar.DAY_OF_MONTH, calFrom.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
				calTemp.setTimeInMillis(Math.min(calTemp.getTimeInMillis(), calUntil.getTimeInMillis()));
				if (calFrom.get(GregorianCalendar.DAY_OF_MONTH) == calFrom.getActualMinimum(GregorianCalendar.DAY_OF_MONTH) &&
						calTemp.get(GregorianCalendar.DAY_OF_MONTH) == calTemp.getActualMaximum(GregorianCalendar.DAY_OF_MONTH)) {
					result += 30;
				}
				else {
					result += calTemp.get(GregorianCalendar.DAY_OF_MONTH) - calFrom.get(GregorianCalendar.DAY_OF_MONTH);
				}
				calFrom.add(GregorianCalendar.MONTH, 1);
				calFrom.set(GregorianCalendar.DAY_OF_MONTH, calFrom.getActualMinimum(GregorianCalendar.DAY_OF_MONTH));
			}
			result += (bAddDay ? 1 : 0);
		}
		return result;
	}

	/**
	 * minimize date
	 * @param date date to minimize
	 * @return minimized date (dd.mm.yyyy 00:00:00.000)
	 * @todo This seems to be the same as DateUtils.getPureDate()
	 */
	public static Date minimizeDate(Date date) {
		final Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.set(GregorianCalendar.HOUR_OF_DAY, 0);
		cal.set(GregorianCalendar.MINUTE, 0);
		cal.set(GregorianCalendar.SECOND, 0);
		cal.set(GregorianCalendar.MILLISECOND, 0);
		return cal.getTime();
	}

	/**
	 * maximize date
	 * @param date date to maximize
	 * @return maximized date (dd.mm.yyyy 23:59:59.999)
	 */
	public static Date maximizeDate(Date date) {
		final Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.set(GregorianCalendar.HOUR_OF_DAY, cal.getActualMaximum(GregorianCalendar.HOUR_OF_DAY));
		cal.set(GregorianCalendar.MINUTE, cal.getActualMaximum(GregorianCalendar.MINUTE));
		cal.set(GregorianCalendar.SECOND, cal.getActualMaximum(GregorianCalendar.SECOND));
		cal.set(GregorianCalendar.MILLISECOND, cal.getActualMaximum(GregorianCalendar.MILLISECOND));
		return cal.getTime();
	}

	/**
	 * get first date of this month
	 * @param date date to modify as requested
	 * @return modified date
	 * @todo rename to getFirstOfSameMonth, write a test and adjust specification (date doesn't seem to be modified)
	 */
	public static Date getFirstOfThisMonth(Date date) {
		final Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.set(GregorianCalendar.DAY_OF_MONTH, cal.getActualMinimum(GregorianCalendar.DAY_OF_MONTH));
		cal.set(GregorianCalendar.HOUR_OF_DAY, cal.getActualMinimum(GregorianCalendar.HOUR_OF_DAY));
		cal.set(GregorianCalendar.MINUTE, cal.getActualMinimum(GregorianCalendar.MINUTE));
		cal.set(GregorianCalendar.SECOND, cal.getActualMinimum(GregorianCalendar.SECOND));
		cal.set(GregorianCalendar.MILLISECOND, cal.getActualMinimum(GregorianCalendar.MILLISECOND));
		return cal.getTime();
	}

	/**
	 * get last date of this month
	 * @param date date to modify as requested
	 * @return modified date
	 * @todo rename to getLastOfSameMonth, write a test and adjust specification (date doesn't seem to be modified)
	 */
	public static Date getLastOfThisMonth(Date date) {
		final Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.set(GregorianCalendar.DAY_OF_MONTH, cal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
		cal.set(GregorianCalendar.HOUR_OF_DAY, cal.getActualMinimum(GregorianCalendar.HOUR_OF_DAY));
		cal.set(GregorianCalendar.MINUTE, cal.getActualMinimum(GregorianCalendar.MINUTE));
		cal.set(GregorianCalendar.SECOND, cal.getActualMinimum(GregorianCalendar.SECOND));
		cal.set(GregorianCalendar.MILLISECOND, cal.getActualMinimum(GregorianCalendar.MILLISECOND));
		return cal.getTime();
	}

	/**
	 * get first date of next month
	 * @param date date to modify as requested
	 * @return modified date
	 * @todo write a test and adjust specification (date doesn't seem to be modified)
	 */
	public static Date getFirstOfNextMonth(Date date) {
		final GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(GregorianCalendar.MONTH, 1);
		cal.set(GregorianCalendar.DAY_OF_MONTH, cal.getActualMinimum(GregorianCalendar.DAY_OF_MONTH));
		cal.set(GregorianCalendar.HOUR_OF_DAY, cal.getActualMinimum(GregorianCalendar.HOUR_OF_DAY));
		cal.set(GregorianCalendar.MINUTE, cal.getActualMinimum(GregorianCalendar.MINUTE));
		cal.set(GregorianCalendar.SECOND, cal.getActualMinimum(GregorianCalendar.SECOND));
		cal.set(GregorianCalendar.MILLISECOND, cal.getActualMinimum(GregorianCalendar.MILLISECOND));
		return cal.getTime();
	}

	/**
	 * get last date of previous month
	 * @param date date to modify as requested
	 * @return modified date
	 * @todo write a test and adjust specification (date doesn't seem to be modified)
	 */
	public static Date getLastOfPreviousMonth(Date date) {
		final Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(GregorianCalendar.MONTH, -1);
		cal.set(GregorianCalendar.DAY_OF_MONTH, cal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
		cal.set(GregorianCalendar.HOUR_OF_DAY, cal.getActualMinimum(GregorianCalendar.HOUR_OF_DAY));
		cal.set(GregorianCalendar.MINUTE, cal.getActualMinimum(GregorianCalendar.MINUTE));
		cal.set(GregorianCalendar.SECOND, cal.getActualMinimum(GregorianCalendar.SECOND));
		cal.set(GregorianCalendar.MILLISECOND, cal.getActualMinimum(GregorianCalendar.MILLISECOND));
		return cal.getTime();
	}

}	// class Helper
