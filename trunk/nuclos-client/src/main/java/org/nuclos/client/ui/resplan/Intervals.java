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

import java.util.Collections;
import java.util.List;

public class Intervals {
	
	public static <T extends Comparable<? super T>> int findStartIndex(List<? extends Interval<T>> intervals, T value) {
		int index = 0;
		while (index < intervals.size() && value.compareTo(intervals.get(index).getEnd()) >= 0) {
			index++;
		}
		return index;
	}

	public static <T extends Comparable<? super T>> int findEndIndex(List<? extends Interval<T>> intervals, T value) {
		return findEndIndex(intervals, value, 0);
	}
	
	public static <T extends Comparable<? super T>> int findEndIndex(List<? extends Interval<T>> intervals, T value, int startIndex) {
		int index = startIndex;
		while (index < intervals.size() && value.compareTo(intervals.get(index).getStart()) > 0) {
			index++;
		}
		return index;
	}
	
	public static <T extends Comparable<? super T>> List<? extends Interval<T>> findIntervalSubList(List<? extends Interval<T>> intervals, T start, T end) {
		int startIndex = findStartIndex(intervals, start);
		if (startIndex >= 0) {
			int endIndex = findEndIndex(intervals, end, startIndex);
			if (endIndex > startIndex) {
				return intervals.subList(startIndex, endIndex);
			}
		}
		return Collections.emptyList();
	}
}
