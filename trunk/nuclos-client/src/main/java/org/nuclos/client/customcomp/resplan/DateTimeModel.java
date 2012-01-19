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

package org.nuclos.client.customcomp.resplan;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.nuclos.client.ui.resplan.Interval;
import org.nuclos.client.ui.resplan.Intervals;
import org.nuclos.client.ui.resplan.TimeModel;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.time.LocalTime;
import org.nuclos.common2.DateUtils;

public class DateTimeModel implements TimeModel<Date> {

	private final List<Pair<LocalTime, LocalTime>> timeIntervals;
	
	public DateTimeModel() {
		this.timeIntervals = null;
	}
	
	public DateTimeModel(List<Pair<LocalTime, LocalTime>> timeIntervals) {
		this.timeIntervals = timeIntervals;
	}

	@Override
	public List<Interval<Date>> getTimeIntervals(Date start, Date end) {
		List<Interval<Date>> intervals = new ArrayList<Interval<Date>>();
		Date date = DateUtils.getPureDate(start);
		Date realEnd = DateUtils.getPureDate(DateUtils.addDays(end, 1));
		while (date.compareTo(realEnd) < 0) {
			Date nextDate = DateUtils.getPureDate(DateUtils.addDays(date, 1));
			if (timeIntervals != null) {
				for (Pair<LocalTime, LocalTime> p : timeIntervals) {
					Date date1 = new Date(date.getTime() + p.x.toSecondOfDay() * 1000);
					if (p.x.compareTo(p.y) > 0) {
						date = DateUtils.addDays(date, 1);
					}
					Date date2 = new Date(date.getTime() + p.y.toSecondOfDay() * 1000);
					checkAddDate(intervals, new Interval<Date>(date1, date2, true));
				}
			} else {
				checkAddDate(intervals, new Interval<Date>(date, nextDate));
			}
			date = nextDate;
		}
		return intervals;
	}
	
	@Override
	public long getDuration(Date startInclusive, Date endExclusive) {
		return endExclusive.getTime() - startInclusive.getTime();
	}
	
	public long getDuration(Interval<Date> interval) {
		return getDuration(interval.getStart(), interval.getEnd());
	}
	
	@Override
	public Interval<Date> shiftInterval(Interval<Date> givenInterval, Date newStart) {
		Date givenStart = givenInterval.getStart(), givenEnd = givenInterval.getEnd();
		
		List<? extends Interval<Date>> sigIntervals = getSignificantIntervals(givenStart, givenEnd);
		long sigDuration = 0, offsetStart = 0, offsetEnd = 0;
		if (sigIntervals.size() > 0) {
			for (Interval<Date> i : sigIntervals) {
				sigDuration += getDuration(i.getStart(), i.getEnd());
			}
			offsetStart = getDuration(sigIntervals.get(0).getStart(), givenStart);
			offsetEnd = getDuration(givenEnd, sigIntervals.get(sigIntervals.size()-1).getEnd());
			sigDuration = sigDuration - offsetStart - offsetEnd;
		}

		Date approximateNewEnd = new Date(newStart.getTime() + getDuration(givenStart, givenEnd) + 24 * 60 * 60 * 1000);
		List<Interval<Date>> approximatedIntervals = getTimeIntervals(newStart, approximateNewEnd);
		if (approximatedIntervals.isEmpty())
			return givenInterval;

		Date realNewStart = null, realNewEnd = null;
		for (Interval<Date> newInterval : approximatedIntervals) {
			// getTimeIntervals may return some periods out of the time
			if (newInterval.getStart().compareTo(newStart) < 0)
				continue;
			if (realNewStart == null) {
				long ofs = offsetStart % getDuration(newInterval.getStart(), newInterval.getEnd());
				realNewStart = new Date(newInterval.getStart().getTime() + ofs);
				sigDuration -= getDuration(realNewStart, newInterval.getEnd());
			} else {
				sigDuration -= getDuration(newInterval.getStart(), newInterval.getEnd());
			}
			realNewEnd = newInterval.getEnd();
			
			if (sigDuration <= 0) {
				realNewEnd = new Date(realNewEnd.getTime() + sigDuration);
				break;
			}
		}
		if (realNewStart != null && realNewEnd != null) {
			return new Interval<Date>(realNewStart, realNewEnd); 
		} else {
			return approximatedIntervals.get(0);
		}
	}
	
	private List<? extends Interval<Date>> getSignificantIntervals(Date start, Date end) {
		List<Interval<Date>> intervals = this.getTimeIntervals(start, end);
		// getTimeIntervals may have returned some insignificant intervals (e.g. in order
		// to ensure that whole days are returned), findIntervalSubList removes these.
		return Intervals.findIntervalSubList(intervals, start, end);
	}
	
	private static void checkAddDate(List<Interval<Date>> list, Interval<Date> interval) {
		int size = list.size();
		if (size > 0 && !interval.isAfter(list.get(size - 1))) {
			throw new IllegalArgumentException("Invalid time order " + list.get(size - 1) + " < " + interval);
		}
		list.add(interval);
	}
}
