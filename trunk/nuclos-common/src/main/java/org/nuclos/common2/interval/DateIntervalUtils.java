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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.PredicateUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.DateUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.functional.BinaryOperation;
import org.nuclos.common2.functional.FunctionalUtils;
import org.nuclos.common2.interval.DateInterval.IsEmpty;

/**
 * <code>DateInterval</code> utility methods.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public class DateIntervalUtils {

	/**
	 * a date interval representing "always".
	 */
	private static final DateInterval ALWAYS = newDateInterval(null, null);

	/**
	 * a date interval representing "never".
	 */
	private static final EmptyDateInterval NEVER = new EmptyDateInterval();

	private DateIntervalUtils() {
	}

	/**
	 * @param dateDayFromIncluding
	 * @param dateDayUntilIncluding
	 * @return Is the interval represented by the given days empty? That means: Is "day from" after "day until"?
	 */
	public static boolean isEmptyDateInterval(Date dateDayFromIncluding, Date dateDayUntilIncluding) {
		return dateDayFromIncluding != null && dateDayUntilIncluding != null &&
				DateUtils.getPureDate(dateDayFromIncluding).after(DateUtils.getPureDate(dateDayUntilIncluding));
	}

	/**
	 * a new date interval from <code>dateDayFromIncluding</code> (including) until <code>dateDayUntilIncluding</code> (including).
	 * Note that that date interval will be empty if "day from" is after "day until".
	 * @param dateDayFromIncluding <code>null</code> means the Big Bang.
	 * @param dateDayUntilIncluding <code>null</code> means the end of time.
	 * @return a possibly empty date interval
	 * @postcondition result.isEmpty() || LangUtils.equals(this.getDayFrom(), DateUtils.getPureDate(dateDayFromIncluding))
	 * @postcondition result.isEmpty() || LangUtils.equals(this.getDayUntil(), DateUtils.getPureDate(dateDayUntilIncluding))
	 */
	public static DateInterval newDateInterval(Date dateDayFromIncluding, Date dateDayUntilIncluding) {
		return isEmptyDateInterval(dateDayFromIncluding, dateDayUntilIncluding) ?
				NEVER :
				new NonEmptyDateInterval(dateDayFromIncluding, dateDayUntilIncluding);
	}

//	/**
//	 * a new date interval from <code>dateDayFrom</code> (including) until <code>dateDayUntil</code> (excluding).
//	 * @param dateDayFrom <code>null</code> means the Big Bang.
//	 * @param dateDayUntil <code>null</code> means the end of time.
//	 * @postcondition LangUtils.equals(this.getDayFrom(), DateUtils.getPureDate(dateDayFrom))
//	 * @postcondition LangUtils.equals(this.getDayUntil(), DateUtils.getPureDate(dateDayUntil))
//	 */
//	public static DateInterval newDateIntervalExcludingUntil(Date dateDayFrom, Date dateDayUntil) {
//		/** @todo check if this is correct */
//		return new DateInterval(dateDayFrom, (dateDayUntil == null) ? null : new Date(dateDayUntil-DateUtils.MILLISECONDS_PER_DAY);
//	}

	/**
	 * @return a date interval representing "always".
	 * @postcondition result.getDayFrom() == null
	 * @postcondition result.getDayUntil() == null
	 */
	public static DateInterval always() {
		return ALWAYS;
	}

	/**
	 * @return a date interval representing "never".
	 * @postcondition result.isEmpty()
	 */
	public static DateInterval never() {
		return NEVER;
	}

	/**
	 * compares the "from" days of the given intervals. Empty intervals are considered lower than non-empty ones.
	 * @param dateinterval1
	 * @param dateinterval2
	 * @return -1/0/+1 (as usual)
	 * @precondition dateinterval1 != null
	 * @precondition dateinterval2 != null
	 */
	public static int compareDateFrom(DateInterval dateinterval1, DateInterval dateinterval2) {
		return (dateinterval1.isEmpty() || dateinterval2.isEmpty()) ?
				compareEmptiness(dateinterval1, dateinterval2) :
				DateUtils.compareDateFrom(dateinterval1.getDayFrom(), dateinterval2.getDayFrom());
	}

	/**
	 * compares the "until" days of the given intervals. Empty intervals are considered lower than non-empty ones.
	 * @param dateinterval1
	 * @param dateinterval2
	 * @return -1/0/+1 (as usual)
	 * @precondition dateinterval1 != null
	 * @precondition dateinterval2 != null
	 */
	public static int compareDateUntil(DateInterval dateinterval1, DateInterval dateinterval2) {
		return (dateinterval1.isEmpty() || dateinterval2.isEmpty()) ?
				compareEmptiness(dateinterval1, dateinterval2) :
				DateUtils.compareDateUntil(dateinterval1.getDayUntil(), dateinterval2.getDayUntil());
	}

	/**
	 * Empty intervals are considered lower than non-empty ones.
	 * @param dateinterval1
	 * @param dateinterval2
	 * @return -1/0/+1 (as usual)
	 * @precondition dateinterval1 != null
	 * @precondition dateinterval2 != null
	 */
	private static int compareEmptiness(DateInterval dateinterval1, DateInterval dateinterval2) {
		return -LangUtils.comparebooleans(dateinterval1.isEmpty(), dateinterval2.isEmpty());
	}

	/**
	 * @param dateinterval1
	 * @param dateinterval2
	 * @return a new date interval containing the intersection of the given intervals.
	 * @precondition dateinterval1 != null
	 * @precondition dateinterval2 != null
	 * @postcondition result != null
	 * @postcondition (dateinterval1.isEmpty() || dateinterval2.isEmpty()) --> result.isEmpty()
	 */
	public static DateInterval intersection(DateInterval dateinterval1, DateInterval dateinterval2) {
		final DateInterval result = (dateinterval1.isEmpty() || dateinterval2.isEmpty()) ? never() :
				newDateInterval(
						maxFrom(dateinterval1.getDayFrom(), dateinterval2.getDayFrom()),
						minUntil(dateinterval1.getDayUntil(), dateinterval2.getDayUntil()));

		assert LangUtils.implies((dateinterval1.isEmpty() || dateinterval2.isEmpty()), result.isEmpty());
		return result;
	}

	/**
	 * @param adateinterval
	 * @return a new date interval containing the intersection of the given intervals.
	 * @precondition adateinterval != null
	 * @postcondition result != null
	 */
	public static DateInterval intersection(DateInterval... adateinterval) {
		return FunctionalUtils.foldl1(new BinaryOperation<DateInterval, RuntimeException>() {
			@Override
			public DateInterval execute(DateInterval dateinterval1, DateInterval dateinterval2) {
				return intersection(dateinterval1, dateinterval2);
			}
		}, Arrays.asList(adateinterval));
	}

	/**
	 * @param dateFrom1 <code>null</code> means -infinity.
	 * @param dateFrom2 <code>null</code> means -infinity.
	 * @return the maximum of the given dates, where <code>null</code> means -infinity.
	 */
	private static Date maxFrom(Date dateFrom1, Date dateFrom2) {
		return DateUtils.compareDateFrom(dateFrom1, dateFrom2) > 0 ? dateFrom1 : dateFrom2;
	}

	/**
	 * @param dateUntil1 <code>null</code> means +infinity.
	 * @param dateUntil2 <code>null</code> means +infinity.
	 * @return the minimum of the given dates, where <code>null</code> means +infinity.
	 */
	private static Date minUntil(Date dateUntil1, Date dateUntil2) {
		return DateUtils.compareDateUntil(dateUntil1, dateUntil2) < 0 ? dateUntil1 : dateUntil2;
	}

	/**
	 * subtracts dateinterval2 from dateinterval1, resulting in a list of 0, 1 or 2 date intervals.
	 * @param dateinterval1
	 * @param dateinterval2
	 * @precondition dateinterval1 != null
	 * @precondition dateinterval2 != null
	 * @postcondition result != null
	 * @postcondition result.size() <= 2
	 */
	public static List<DateInterval> subtract(DateInterval dateinterval1, DateInterval dateinterval2) {
		final List<DateInterval> result = new ArrayList<DateInterval>();

		if (dateinterval1.isEmpty() || dateinterval2.contains(dateinterval1)) {
			// the result is empty
		}
		else {
			final DateInterval dateintervalIntersection = intersection(dateinterval1, dateinterval2);
			if (dateintervalIntersection.isEmpty()) {
				// intersection is empty:
				result.add(dateinterval1);
			}
			else {
				if (compareDateFrom(dateinterval1, dateinterval2) < 0) {
					assert dateinterval2.getDayFrom() != null;
					final Date dateFromNextDay = DateUtils.addDays(dateinterval2.getDayFrom(), -1);
					result.add(newDateInterval(dateinterval1.getDayFrom(), dateFromNextDay));
					result.addAll(subtract(newDateInterval(dateinterval2.getDayFrom(), dateinterval1.getDayUntil()), dateinterval2));
				}
				else {
					// consider remainder:
					assert dateinterval2.getDayUntil() != null;

					final Date dateUntilNextDay = DateUtils.addDays(dateinterval2.getDayUntil(), 1);
					final DateInterval dateintervalRemainder = newDateInterval(dateUntilNextDay, dateinterval1.getDayUntil());
					if (!dateintervalRemainder.isEmpty()) {
						result.add(dateintervalRemainder);
					}
				}
			}
		}
		assert result != null;
		assert result.size() <= 2;
		return result;
	}

	/**
	 * subtracts a list of date intervals (2nd argument) from a given date interval (1st argument).
	 * @param dateinterval1
	 * @param lstdateinterval2
	 * @return the list of remaining date intervals
	 * @precondition dateinterval1 != null
	 * @precondition lstdateinterval2 != null
	 * @postcondition result != null
	 */
	public static List<DateInterval> subtract(DateInterval dateinterval1, List<DateInterval> lstdateinterval2) {
		return dateinterval1.isEmpty() ?
				Collections.<DateInterval>emptyList() :
				subtract(Collections.singletonList(dateinterval1), lstdateinterval2);
	}

	/**
	 * subtracts a list of date intervals (2nd argument) from each member of a list of date intervals (1st argument).
	 * @param lstdateinterval1
	 * @param lstdateinterval2
	 * @return the list of remaining date intervals
	 * @precondition lstdateinterval1 != null
	 * @precondition lstdateinterval2 != null
	 * @postcondition result != null
	 */
	private static List<DateInterval> subtract(List<DateInterval> lstdateinterval1, List<DateInterval> lstdateinterval2) {
		return lstdateinterval2.isEmpty() ?
				lstdateinterval1 :
				subtract(subtract(lstdateinterval1, lstdateinterval2.get(0)), lstdateinterval2.subList(1, lstdateinterval2.size()));
	}

	/**
	 * subtracts a date interval (2nd argument) from each member of a list of date intervals (1st argument).
	 * @param lstdateinterval1
	 * @param dateinterval2
	 * @return the list of remaining non-empty date intervals
	 * @precondition lstdateinterval1 != null
	 * @precondition dateinterval2 != null
	 * @postcondition result != null
	 */
	private static List<DateInterval> subtract(List<DateInterval> lstdateinterval1, final DateInterval dateinterval2) {
		return CollectionUtils.select(CollectionUtils.concatAll(CollectionUtils.transform(lstdateinterval1, new Subtract(dateinterval2))), PredicateUtils.not(new IsEmpty()));
	}

	/**
	 * @param colldateinterval
	 * @return Does the given collection contain overlapping date intervals?
	 * @precondition colldateinterval != null
	 * @postcondition (colldateinterval.size() <= 1) --> !result
	 */
	public static boolean containsOverlapping(Collection<DateInterval> colldateinterval) {
		return containsOverlappingSortedByDateFrom(CollectionUtils.sorted(colldateinterval, new DateFromComparator()));
	}

	/**
	 * @param lstdateintervalSorted must be sorted by "from" day.
	 * @return Does the given collection contain overlapping time spans?
	 * @precondition lstdateintervalSorted != null
	 */
	private static boolean containsOverlappingSortedByDateFrom(List<DateInterval> lstdateintervalSorted) {
		final boolean result;
		if (lstdateintervalSorted.size() <= 1) {
			result = false;
		}
		else {
			result = isOverlappingSortedByDateFrom(lstdateintervalSorted.get(0), lstdateintervalSorted.get(1)) ||
					containsOverlappingSortedByDateFrom(lstdateintervalSorted.subList(1, lstdateintervalSorted.size()));
		}
		return result;
	}

	/**
	 * @param dateinterval1 must begin before <code>dateinterval2</code>
	 * @param dateinterval2
	 * @return Do the given date intervals overlap?
	 * @precondition dateinterval1 != null
	 * @precondition dateinterval2 != null
	 * @precondition compareDateFrom(dateinterval1, dateinterval2) <= 0
	 */
	private static boolean isOverlappingSortedByDateFrom(DateInterval dateinterval1, DateInterval dateinterval2) {
		if (!(compareDateFrom(dateinterval1, dateinterval2) <= 0)) {
			throw new IllegalArgumentException("The given intervals are not sorted by their \"from\" dates.");
		}
		return !dateinterval1.isEmpty() && !dateinterval2.isEmpty() && isOverlapping(dateinterval1.getDayUntil(), dateinterval2.getDayFrom());
	}

	/**
	 * @param dateDayUntil <code>null</code> means +infinity.
	 * @param dateDayFrom <code>null</code> means -infinity.
	 */
	private static boolean isOverlapping(Date dateDayUntil, Date dateDayFrom) {
		return (dateDayUntil == null || dateDayFrom == null || !dateDayUntil.before(dateDayFrom));
	}

	/**
	 * Transformer: Subtract
	 */
	private static class Subtract implements Transformer<DateInterval, List<DateInterval>> {
		private final DateInterval dateinterval2;

		Subtract(DateInterval dateinterval2) {
			this.dateinterval2 = dateinterval2;
		}

		@Override
		public List<DateInterval> transform(DateInterval dateinterval) {
			return subtract(dateinterval, dateinterval2);
		}
	}

	/**
	 * Comparator: DateFromComparator
	 */
	public static class DateFromComparator implements Comparator<DateInterval> {
		@Override
		public int compare(DateInterval dateinterval1, DateInterval dateinterval2) {
			return DateIntervalUtils.compareDateFrom(dateinterval1, dateinterval2);
		}
	}

}	// class DateIntervalUtils
