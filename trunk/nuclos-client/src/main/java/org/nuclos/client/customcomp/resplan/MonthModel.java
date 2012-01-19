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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.nuclos.client.ui.resplan.Interval;
import org.nuclos.client.ui.resplan.TimeModel;
import org.nuclos.common2.DateUtils;

public class MonthModel implements TimeModel<Date> {
	
	public MonthModel() {}

	@Override
	public List<Interval<Date>> getTimeIntervals(Date start, Date end) {
		List<Interval<Date>> intervals = new ArrayList<Interval<Date>>();
		
		Calendar calStart = new GregorianCalendar();
		calStart.setTime(DateUtils.getPureDate(start));
		Calendar calEnd = new GregorianCalendar();
		calEnd.setTime(DateUtils.getPureDate(end));
		
		int yStart = calStart.get(Calendar.YEAR);
		int yEnd   = calEnd.get(Calendar.YEAR);
		int mStart = calStart.get(Calendar.MONTH);
		int mEnd   = calEnd.get(Calendar.MONTH);
		
		for (int y = yStart; y <= yEnd; y++) {
			int m = (y == yStart) ? mStart : 0;
			int m2 = (y == yEnd) ? mEnd : 11;
			while (m <= m2) {
				calStart.set(Calendar.DAY_OF_MONTH, 1);
				calStart.set(Calendar.YEAR, y);
				calStart.set(Calendar.MONTH, m);
				calEnd.set(Calendar.DAY_OF_MONTH, 1);
				calEnd.set(Calendar.YEAR, y);
				calEnd.set(Calendar.MONTH, m);
				calEnd.set(Calendar.DAY_OF_MONTH, calEnd.getActualMaximum(Calendar.DAY_OF_MONTH));
				calEnd.add(Calendar.DAY_OF_MONTH, 1);
				checkAddDate(intervals, new Interval<Date>(calStart.getTime(), calEnd.getTime()));
				m++;
			}
			
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
		Calendar calIntervalStart = new GregorianCalendar();
		calIntervalStart.setTime(givenInterval.getStart());
		Calendar calIntervalEnd = new GregorianCalendar();
		calIntervalEnd.setTime(givenInterval.getEnd());
		
		Calendar calStart = new GregorianCalendar();
		calStart.setTime(newStart);
		
		calIntervalStart.set(Calendar.DAY_OF_MONTH, 1);
		calIntervalStart.set(Calendar.YEAR, calStart.get(Calendar.YEAR));
		calIntervalStart.set(Calendar.MONTH, calStart.get(Calendar.MONTH));
		calIntervalEnd.set(Calendar.DAY_OF_MONTH, 1);
		calIntervalEnd.set(Calendar.YEAR, calStart.get(Calendar.YEAR));
		calIntervalEnd.set(Calendar.MONTH, calStart.get(Calendar.MONTH));
		calIntervalEnd.set(Calendar.DAY_OF_MONTH, calIntervalEnd.getActualMaximum(Calendar.DAY_OF_MONTH));
		calIntervalEnd.add(Calendar.DAY_OF_MONTH, 1);
		
		return new Interval<Date>(calIntervalStart.getTime(), calIntervalEnd.getTime());
	}
	
	private static void checkAddDate(List<Interval<Date>> list, Interval<Date> interval) {
		int size = list.size();
		if (size > 0 && !interval.isAfter(list.get(size - 1))) {
			throw new IllegalArgumentException("Invalid time order " + list.get(size - 1) + " < " + interval);
		}
		list.add(interval);
	}
}
