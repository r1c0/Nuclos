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

public class WeekModel implements TimeModel<Date> {
	
	public WeekModel() {}

	@Override
	public List<Interval<Date>> getTimeIntervals(Date start, Date end) {
		List<Interval<Date>> intervals = new ArrayList<Interval<Date>>();
		
		Calendar calStart = new GregorianCalendar();
		calStart.setTime(DateUtils.getPureDate(start));
		Calendar calEnd = new GregorianCalendar();
		calEnd.setTime(DateUtils.getPureDate(end));
		
		int yStart = calStart.get(Calendar.YEAR);
		int yEnd   = calEnd.get(Calendar.YEAR);
		int wStart = calStart.get(Calendar.WEEK_OF_YEAR);
		int wEnd   = calEnd.get(Calendar.WEEK_OF_YEAR);
		
		if (wStart > 10 && calStart.get(Calendar.MONTH) == 0) {
			// week is in previous year, set year -1
			yStart--;
		}
		
		if (wEnd < 10 && calEnd.get(Calendar.MONTH) == 11) {
			// week is in next year
			yEnd++;
		}
		
		for (int y = yStart; y <= yEnd; y++) {
			int w = (y == yStart) ? wStart : 1;
			GregorianCalendar gc = new GregorianCalendar(y, 11, 31);
			int w2 = (y == yEnd) ? wEnd : gc.getActualMaximum(Calendar.WEEK_OF_YEAR);
			while (w <= w2) {
				calStart.set(Calendar.YEAR, y);
				calStart.set(Calendar.WEEK_OF_YEAR, w);
				calStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				calEnd.set(Calendar.YEAR, y);
				calEnd.set(Calendar.WEEK_OF_YEAR, w);
				calEnd.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
				if (y == calStart.get(Calendar.YEAR)) {
					checkAddDate(intervals, new Interval<Date>(calStart.getTime(), calEnd.getTime()));
				}
				w++;
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
		calIntervalStart.setTime(newStart);
		Calendar calIntervalEnd = new GregorianCalendar();
		calIntervalEnd.setTime(newStart);
		
		int duration = DateUtils.daysBetween(givenInterval.getStart(), givenInterval.getEnd());
		
		Calendar calStart = new GregorianCalendar();
		calStart.setTime(newStart);
		
		calIntervalEnd.add(Calendar.DAY_OF_YEAR, duration);
		calIntervalEnd.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		
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
