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

import java.io.Serializable;
import java.util.Date;

import org.nuclos.common.collection.Predicate;

/**
 * A possibly empty interval between two days.
 * Note that for an empty interval, there is no "day from" or "day until".
 * All classes implementing this interface are meant to be immutable.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 *
 * @see DateIntervalUtils#newDateInterval(Date, Date)
 * @see EmptyDateInterval
 * @see NonEmptyDateInterval
 */
public interface DateInterval extends Serializable {

	/**
	 * @return Is this an empty interval?
	 */
	boolean isEmpty();

	/**
	 * @return the first day of this interval (including that day). <code>null</code> means -infinity.
	 * @precondition !this.isEmpty()
	 * @postcondition result == null || DateUtils.isPure(result)
	 */
	Date getDayFrom();

	/**
	 * @return the last day of this interval (including that day). <code>null</code> means +infinity.
	 * @precondition !this.isEmpty()
	 * @postcondition result == null || DateUtils.isPure(result)
	 */
	Date getDayUntil();

	/**
	 * @param date
	 * @return Does this interval contain the given date?
	 * @precondition date != null
	 * @todo Does date have to be a pure date?
	 */
	boolean contains(Date date);

	/**
	 * @param that
	 * @return Does this interval contain that interval?
	 * @precondition that != null
	 * @todo consider moving this method to DateIntervalUtils
	 */
	boolean contains(DateInterval that);

	/**
	 * Two date intervals are considered equal iff they represent the same time period.
	 * @param o
	 */
	@Override
	boolean equals(Object o);

	@Override
	int hashCode();

	@Override
	String toString();

	/**
	 * Predicate: IsEmpty
	 */
	static class IsEmpty implements Predicate<DateInterval> {
		@Override
		public boolean evaluate(DateInterval dateinterval) {
			return dateinterval.isEmpty();
		}
	}

}	// interface DateInterval
