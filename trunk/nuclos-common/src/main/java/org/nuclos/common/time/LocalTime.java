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

package org.nuclos.common.time;

import java.io.Serializable;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nuclos.common2.DateUtils;

/**
 * A time class for storing time values without a date/timezone.
 * Modeled after the JSR-310 and Jodatime.
 */
public class LocalTime implements Serializable, Comparable<LocalTime> {
	
	/**
	 * Parses a time.
	 */
	public static LocalTime parse(String str) {
		Matcher matcher = PATTERN.matcher(str);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("Invalid time string " + str);
		}
		int h = Integer.parseInt(matcher.group(1));
		int m = Integer.parseInt(matcher.group(2));
		int s = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0;
		return of(h, m, s);
	}
	
	public static LocalTime of(int h, int m) {
		return of(h, m, 0);
	}
	
	public static LocalTime of(int h, int m, int s) {
		check("hour", h, 0, 23);
		check("minute", m, 0, 59);
		check("second", s, 0, 59);
		return new LocalTime(h * 60 * 60 + m * 60 + s);
	}
	
	public static LocalTime ofSecondOfDay(long secondOfDay) {
		check("second-of-day", (int) secondOfDay, 0, 24*60*60-1);
		return new LocalTime((int) secondOfDay);
	}
	
	public Date toDate(Date date) {
		return new Date(DateUtils.getPureDate(date).getTime() + toSecondOfDay() * 1000);
	}
	
	/** the time in seconds */
	private final int time;
	
	public LocalTime(int time) {
		this.time = time;
	}

	private static void check(String name, int val, int min, int max) {
		if (val < min || val > max) {
			throw new IllegalArgumentException(name + "(" + val + ") must be between " + min + " and " + max);
		}
	}

	@Override
	public int hashCode() {
		return time;
	}
	
	public long toSecondOfDay() {
		return time;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LocalTime) {
			return time == ((LocalTime) obj).time;
		}
		return false;
	}
	
	@Override
	public int compareTo(LocalTime o) {
		return Integer.valueOf(time).compareTo(o.time);
	}

	@Override
	public String toString() {
		int seconds = time % 60;
		int minutes = time / 60;
		if (seconds == 0) {
			return String.format("%02d:%02d", minutes / 60, minutes % 60);
		} else {
			return String.format("%02d:%02d:%02d", minutes / 60, minutes % 60, seconds);
		}
	}
	
	private static final Pattern PATTERN = Pattern.compile("(\\d+):(\\d+)(?::(\\d+))?");
}
