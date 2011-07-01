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

package org.nuclos.client.ui.resplan;

import java.io.Serializable;

public final class Interval<T extends Comparable<? super T>> implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final int START = 0;
	public static final int END = 1;

	private final T start;
	private final T end;

	public Interval(T start, T end) {
		this(start, end, false);
	}

	public Interval(T start, T end, boolean lenient) {
		if (start.compareTo(end) <= 0) {
			this.start = start;
			this.end = end;
		} else if (lenient) {
			this.end = start;
			this.start = end;
		} else {
			throw new IllegalArgumentException("interval with start > end");
		}
	}

	public T getStart() {
		return start;
	}

	public T getEnd() {
		return end;
	}

	public T get(int direction) {
		switch (direction) {
		case START:
			return start;
		case END:
			return end;
		default:
			throw new IllegalArgumentException();
		}
	}

	public boolean isEmpty() {
		return start.compareTo(end) == 0;
	}

	public boolean contains(T value) {
		return start.compareTo(value) <= 0 && end.compareTo(value) > 0;
	}

	public boolean contains(T value, boolean includeEnd) {
		return start.compareTo(value) <= 0 && (includeEnd ? end.compareTo(value) >= 0 : end.compareTo(value) > 0);
	}
	
	public boolean contains(Interval<T> interval) {
		return start.compareTo(interval.start) <= 0 && end.compareTo(interval.end) >= 0;
	}
	
	public boolean intersects(Interval<T> interval) {
		return !(isBefore(interval) || isAfter(interval));
	}
	
	public boolean isBefore(Interval<T> other) {
		return this.getEnd().compareTo(other.getStart()) <= 0;
	}
	
	public boolean isAfter(Interval<T> other) {
		return this.getStart().compareTo(other.getEnd()) >= 0;
	}
	
	@Override
	public int hashCode() {
		return start.hashCode() ^ end.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof Interval) {
			Interval<?> other = (Interval<?>) obj;
			return start.equals(other.start) && end.equals(other.end);
		}
		return false;
	}

	@Override
	public String toString() {
		return start.toString() + '-' + end.toString();
	}
}