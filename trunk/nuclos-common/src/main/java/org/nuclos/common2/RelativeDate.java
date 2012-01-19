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
package org.nuclos.common2;

import java.io.Serializable;

import org.nuclos.common2.exception.CommonFatalException;

/**
 * A relative date (e.g. "today"). Useful for relative comparisons, esp. in search conditions.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class RelativeDate extends java.util.Date implements Serializable {

	private static final long serialVersionUID = -8790729340275863967L;

	/**
	 * the one and only instance of "today".
	 */
	private static final RelativeDate TODAY = new RelativeDate();

	protected RelativeDate() {
		// Note that the current date is not necessary here - but we need to give the object a date:
		super(DateUtils.today().getTime());
	}

	/**
	 * @return the one and only instance of "today". It's allowed to "==" against this object.
	 */
	public static RelativeDate today() {
		return TODAY;
	}

	/**
	 * @return the current date (with time set to 0:00:00)
	 */
	@Override
	public long getTime() {
		return DateUtils.today().getTime();
	}

	@Override
	public void setTime(long time) {
		throwNotSupported();
	}

	@Override
	@Deprecated
	public void setYear(int year) {
		throwNotSupported();
	}

	@Override
	@Deprecated
	public void setMonth(int month) {
		throwNotSupported();
	}

	@Override
	@Deprecated
	public void setDate(int date) {
		throwNotSupported();
	}

	@Override
	@Deprecated
	public void setHours(int hours) {
		throwNotSupported();
	}

	@Override
	@Deprecated
	public void setMinutes(int minutes) {
		throwNotSupported();
	}

	@Override
	@Deprecated
	public void setSeconds(int seconds) {
		throwNotSupported();
	}

	/**
	 * @return "TODAY"
	 */
	@Override
	public String toString() {
		return "TODAY";
	}

	/**
	 * @return the one and only instance of "today". The object is not cloned.
	 * This is okay, as the object is immutable.
	 */
	@Override
	public Object clone() {
		return this;
	}

	/**
	 * ensures that the singleton object is used on deserialization.
	 * @return the one and only instance of "today".
	 */
	private Object readResolve() {
		return TODAY;
	}

	private static void throwNotSupported() {
		throw new CommonFatalException("This object is immutable.");
	}
}	// class RelativeDate
