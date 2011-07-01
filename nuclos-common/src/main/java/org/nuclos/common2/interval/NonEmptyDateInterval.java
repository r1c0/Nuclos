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
package org.nuclos.common2.interval;

import java.text.DateFormat;
import java.util.Date;

import org.nuclos.common2.DateUtils;
import org.nuclos.common2.LangUtils;

/**
 * An interval between two (pure) dates (aka "days" ;)).
 * This class is immutable.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 * @todo Consider using the Null Object pattern for day from/until also!
 */
class NonEmptyDateInterval implements DateInterval {

	/**
	 * <code>null</code> means -infinity.
	 */
	private final Date dateDayFrom;

	/**
	 * <code>null</code> means +infinity.
	 */
	private final Date dateDayUntil;

	/**
	 * creates a new date interval from <code>dateDayFrom</code> until <code>dateDayUntil</code>.
	 * Both given days are included in the interval.
	 * @param dateDayFrom <code>null</code> means -infinity (the Big Bang).
	 * @param dateDayUntil <code>null</code> means +infinity (the end of time).
	 * @precondition dateDayFrom == null || dateDayUntil == null || !DateUtils.getPureDate(dateDayFrom).after(DateUtils.getPureDate(dateDayUntil))
	 * @postcondition LangUtils.equals(this.getDayFrom(), DateUtils.getPureDate(dateDayFrom))
	 * @postcondition LangUtils.equals(this.getDayUntil(), DateUtils.getPureDate(dateDayUntil))
	 */
	NonEmptyDateInterval(Date dateDayFrom, Date dateDayUntil) {
		if (!(dateDayFrom == null || dateDayUntil == null || !DateUtils.getPureDate(dateDayFrom).after(DateUtils.getPureDate(dateDayUntil))))
		{
			throw new IllegalArgumentException("\"date from\" must be before \"date until\".");
		}
		this.dateDayFrom = DateUtils.copyDate(DateUtils.getPureDate(dateDayFrom));
		this.dateDayUntil = DateUtils.copyDate(DateUtils.getPureDate(dateDayUntil));

		assert LangUtils.equals(this.getDayFrom(), DateUtils.getPureDate(dateDayFrom));
		assert LangUtils.equals(this.getDayUntil(), DateUtils.getPureDate(dateDayUntil));
	}

	/**
	 * @return false
	 */
	@Override
	public boolean isEmpty() {
		return false;
	}

	/**
	 * @return the first day of this interval (including that day)
	 * @postcondition result == null || DateUtils.isPure(result)
	 */
	@Override
	public Date getDayFrom() {
		final Date result = DateUtils.copyDate(this.dateDayFrom);
		assert result == null || DateUtils.isPure(result);
		return result;
	}

	/**
	 * @return the last day of this interval (including that day)
	 * @postcondition result == null || DateUtils.isPure(result)
	 */
	@Override
	public Date getDayUntil() {
		final Date result = DateUtils.copyDate(this.dateDayUntil);
		assert result == null || DateUtils.isPure(result);
		return result;
	}

	/**
	 * @param date
	 * @return Does this date interval contain the given date?
	 * @precondition date != null
	 */
	@Override
	public boolean contains(Date date) {
		return (this.getDayFrom() == null || !this.getDayFrom().after(date)) &&
				(this.getDayUntil() == null || !this.getDayUntil().before(date));
	}

	/**
	 * @param that
	 * @return Does this date interval contain that date interval?
	 * @precondition that != null
	 */
	@Override
	public boolean contains(DateInterval that) {
		return that.isEmpty() ||
				(DateUtils.compareDateFrom(this.getDayFrom(), that.getDayFrom()) <= 0 &&
						DateUtils.compareDateUntil(this.getDayUntil(), that.getDayUntil()) >= 0);
	}

//	/** @todo write a test */
//	public boolean overlapsWith(DateInterval that) {
//		return this.overlapsWith1(that);
//	}
//
//	private boolean overlapsWith1(DateInterval that) {
//		return containsOverlapping(Arrays.asList(this, that));
//	}
//
//	private boolean overlapsWith2(DateInterval that) {
//		return intersection(this, that).isValid();
//	}

	/**
	 * Two date intervals are considered equal iff they represent the same time period.
	 * @param o
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof NonEmptyDateInterval)) {
			return false;
		}
		final NonEmptyDateInterval that = (NonEmptyDateInterval) o;
		return LangUtils.equals(this.dateDayFrom, that.dateDayFrom) && LangUtils.equals(this.dateDayUntil, that.dateDayUntil);
	}

	@Override
	public int hashCode() {
		return LangUtils.hashCode(this.dateDayFrom) ^ LangUtils.hashCode(this.dateDayUntil);
	}

	@Override
	public String toString() {
		return DateInterval.class.getName() + "[" + getDateString(this.dateDayFrom, "-infinity") + ", " + getDateString(this.dateDayUntil, "infinity") + "]";
	}

	private static String getDateString(Date date, String sDefault) {
		return (date == null) ? sDefault : DateFormat.getDateInstance().format(date);
	}

}	// class NonEmptyDateInterval
