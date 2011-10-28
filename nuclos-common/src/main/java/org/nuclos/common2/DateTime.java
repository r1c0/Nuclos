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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.nuclos.common2.exception.CommonFatalException;

/**
 * Date object for representing a date with time in our framework.
 * Use this class in strdatatype of T_MD_MASTERDATA_FIELD and T_MD_ATTRIBUTE to deactivate the 'time cut'.
 * In Masterdata tables the fieldtype has to be timestamp (not date!). Otherwise flexible reporting still 
 * cut the time in date.
 * 
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@novabit.de">Maik Stueker</a>
 * @version 00.01.000
 */
public class DateTime implements Serializable{
	
	public static final String DATE_FORMAT_STRING = "dd.MM.yyyy HH:mm";
	public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);
	
	private Date value;
	
	/**
	 * Including date would be parsed to <code>DATE_FORMAT_STRING</code>
	 * @param timeInMillis
	 */
	public DateTime(long timeInMillis) {
		this(new Date(timeInMillis));
	}

	/**
	 * Including date would be parsed to <code>DATE_FORMAT_STRING</code>
	 * @param date (not null!)
	 */
	public DateTime(Date date) {
		if (date != null){
			try {
				this.value = DATE_TIME_FORMAT.parse(DATE_TIME_FORMAT.format(date)); 
			} catch (ParseException e) {
				throw new CommonFatalException(e);
			}
		} else {
			throw new CommonFatalException("NULL is not allowed");
		}
	}

	/**
	 * Including date would be parsed to <code>DATE_FORMAT_STRING</code>
	 */
	public DateTime() {
		this(new Date());
	}
	
	public Date getDate() {
		return this.value;
	}

	@Override
	public String toString(){
		return DATE_TIME_FORMAT.format(this.value);
	}
	
	public boolean before(Date when){
		return this.value.before(when);
	}
	
	public boolean after(Date when){
		return this.value.after(when);
	}
	
	public boolean before(DateTime when){
		return this.value.before(when.getDate());
	}
	
	public boolean after(DateTime when){
		return this.value.after(when.getDate());
	}
	
	public long getTime(){
		return this.value.getTime();
	}
	
	public boolean equals(DateTime that){
		return this.value.equals(that.getDate());
	}
	
	public boolean equals(Date that){
		return this.value.equals(that);
	}

	@Override
	public boolean equals(Object that) {
		if (that instanceof DateTime) {
			return equals(((DateTime)that));
		} else {
			return this.value.equals(that);
		}
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}
	
	
}
