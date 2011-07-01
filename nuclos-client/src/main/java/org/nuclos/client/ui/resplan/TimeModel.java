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

import java.util.List;

/**
 * This class represents the abstract time model used by the resource planing component.
 */
public interface TimeModel<T extends Comparable<? super T>> {

	/**
	 * Retrieves all time intervals between start and end.
	 */
	public abstract List<Interval<T>> getTimeIntervals(T start, T end);
	
	/**
	 * Calculates an "abstract" duration for the given interval. 
	 * Durations calculated by this method must satisfy the following laws:
	 * The duration may be negative, but the algorithm must satisfy the following rules:
	 * - getDuration(t, t) == 0 for any valid value t.
	 * - getDuration(t0, t) + getDuration(t, t1) == getDuration(t0, t1) for any valid
	 *   values t, t0, t1 with t0 <= t <= t1.
	 */
	// TODO_RESPLAN:
	public abstract long getDuration(T startInclusive, T endExclusive);
	
	public abstract Interval<T> shiftInterval(Interval<T> interval, T newStart);
}
