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

import java.util.Date;

/**
 * An empty date interval.
 * This class is immutable.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
class EmptyDateInterval implements DateInterval {

	/**
	 * @return true
	 */
	@Override
	public boolean isEmpty() {
		return true;
	}

	/**
	 * @precondition !this.isEmpty()
	 */
	@Override
	public Date getDayFrom() {
		throw new IllegalStateException("empty");
	}

	/**
	 * @precondition !this.isEmpty()
	 */
	@Override
	public Date getDayUntil() {
		throw new IllegalStateException("empty");
	}

	/**
	 * @param date
	 * @return false
	 * @precondition date != null
	 */
	@Override
	public boolean contains(Date date) {
		return false;
	}

	/**
	 *
	 * @param that
	 * @return false
	 * @precondition that != null
	 * @todo Does an empty interval contain another empty interval? (probably not)
	 */
	@Override
	public boolean contains(DateInterval that) {
		return false;
	}

	/**
	 * @param o
	 * @return <code>true</code> iff o is an empty date interval
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		return (o instanceof EmptyDateInterval);
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public String toString() {
		return DateInterval.class.getName() + "[]";
	}

}	// class EmptyDateInterval
