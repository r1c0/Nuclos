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

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.nuclos.common2.exception.CommonFatalException;

/**
 * Utility methods for <code>Date</code>s.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@novabit.de">Maik Stueker</a>
 * @version 01.00.00
 */
public class SeriesUtils {
	
	/**
	 * represents MONDAY. id is taken from GregorianCalendar.MONDAY
	 */
	public static SeriesListItem WEEKDAY_ITEM_MONDAY = new SeriesListItem("Montag", GregorianCalendar.MONDAY);
	
	/**
	 * represents TUESDAY. id is taken from GregorianCalendar.TUESDAY
	 */
	public static SeriesListItem WEEKDAY_ITEM_TUESDAY = new SeriesListItem("Dienstag", GregorianCalendar.TUESDAY);
	
	/**
	 * represents WEDNESDAY. id is taken from GregorianCalendar.WEDNESDAY
	 */
	public static SeriesListItem WEEKDAY_ITEM_WEDNESDAY = new SeriesListItem("Mittwoch", GregorianCalendar.WEDNESDAY);
	
	/**
	 * represents THURSDAY. id is taken from GregorianCalendar.THURSDAY
	 */
	public static SeriesListItem WEEKDAY_ITEM_THURSDAY = new SeriesListItem("Donnerstag", GregorianCalendar.THURSDAY);
	
	/**
	 * represents FRIDAY. id is taken from GregorianCalendar.FRIDAY
	 */
	public static SeriesListItem WEEKDAY_ITEM_FRIDAY = new SeriesListItem("Freitag", GregorianCalendar.FRIDAY);
	
	/**
	 * represents SATURDAY. id is taken from GregorianCalendar.SATURDAY
	 */
	public static SeriesListItem WEEKDAY_ITEM_SATURDAY = new SeriesListItem("Samstag", GregorianCalendar.SATURDAY);
	
	/**
	 * represents SUNDAY. id is taken from GregorianCalendar.SUNDAY
	 */
	public static SeriesListItem WEEKDAY_ITEM_SUNDAY = new SeriesListItem("Sonntag", GregorianCalendar.SUNDAY);
	
	/**
	 * represents 1.
	 */
	public static SeriesListItem NUMBER_ITEM_FIRST = new SeriesListItem("ersten", 1);
	
	/**
	 * represents 2.
	 */
	public static SeriesListItem NUMBER_ITEM_SECOND = new SeriesListItem("zweiten", 2);
	
	/**
	 * represents 3.
	 */
	public static SeriesListItem NUMBER_ITEM_THIRD = new SeriesListItem("dritten", 3);
	
	/**
	 * represents 4.
	 */
	public static SeriesListItem NUMBER_ITEM_FOURTH = new SeriesListItem("vierten", 4);
	
	/**
	 * represents JANUARY. id is taken from GregorianCalendar.JANUARY
	 */
	public static SeriesListItem MONTH_ITEM_JANUARY = new SeriesListItem("Januar", GregorianCalendar.JANUARY);
	
	/**
	 * represents FEBRUARY. id is taken from GregorianCalendar.FEBRUARY
	 */
	public static SeriesListItem MONTH_ITEM_FEBRUARY = new SeriesListItem("Februar", GregorianCalendar.FEBRUARY);
	
	/**
	 * represents MARCH. id is taken from GregorianCalendar.MARCH
	 */
	public static SeriesListItem MONTH_ITEM_MARCH = new SeriesListItem("M\u00e4rz", GregorianCalendar.MARCH);
	
	/**
	 * represents APRIL. id is taken from GregorianCalendar.APRIL
	 */
	public static SeriesListItem MONTH_ITEM_APRIL = new SeriesListItem("April", GregorianCalendar.APRIL);
	
	/**
	 * represents MAY. id is taken from GregorianCalendar.MAY
	 */
	public static SeriesListItem MONTH_ITEM_MAY = new SeriesListItem("Mai", GregorianCalendar.MAY);
	
	/**
	 * represents JUNE. id is taken from GregorianCalendar.JUNE
	 */
	public static SeriesListItem MONTH_ITEM_JUNE = new SeriesListItem("Juni", GregorianCalendar.JUNE);
	
	/**
	 * represents JULY. id is taken from GregorianCalendar.JULY
	 */
	public static SeriesListItem MONTH_ITEM_JULY = new SeriesListItem("Juli", GregorianCalendar.JULY);
	
	/**
	 * represents AUGUST. id is taken from GregorianCalendar.AUGUST
	 */
	public static SeriesListItem MONTH_ITEM_AUGUST = new SeriesListItem("August", GregorianCalendar.AUGUST);
	
	/**
	 * represents SEPTEMBER. id is taken from GregorianCalendar.SEPTEMBER
	 */
	public static SeriesListItem MONTH_ITEM_SEPTEMBER = new SeriesListItem("September", GregorianCalendar.SEPTEMBER);
	
	/**
	 * represents OCTOBER. id is taken from GregorianCalendar.OCTOBER
	 */
	public static SeriesListItem MONTH_ITEM_OCTOBER = new SeriesListItem("Oktober", GregorianCalendar.OCTOBER);
	
	/**
	 * represents NOVEMBER. id is taken from GregorianCalendar.NOVEMBER
	 */
	public static SeriesListItem MONTH_ITEM_NOVEMBER = new SeriesListItem("November", GregorianCalendar.NOVEMBER);
	
	/**
	 * represents DECEMBER. id is taken from GregorianCalendar.DECEMBER
	 */
	public static SeriesListItem MONTH_ITEM_DECEMBER = new SeriesListItem("Dezember", GregorianCalendar.DECEMBER);
	
	/**
	 * 
	 * @param dateFrom
	 * @param dateUntil
	 * @return
	 */
	public static List<DateTime> getPossibleDates(String series, DateTime dateFrom, DateTime dateUntil){
		List<DateTime> result = new ArrayList<DateTime>();
		
		if (dateUntil.before(dateFrom)){
			return result;
		}
		
		final GregorianCalendar calendar = new GregorianCalendar();
		
		DateTime dateCalculated = getSeriesNext(series, dateFrom);
		while (dateCalculated.before(dateUntil)){
			result.add(dateCalculated);
			
			calendar.setTime(dateCalculated.getDate());
			calendar.add(GregorianCalendar.MINUTE, 1);
			dateCalculated = getSeriesNext(series, new DateTime(calendar.getTime()));
		}
		
		return result;
	}

	/**
	 * 
	 * @param series
	 * @param dateOrigin
	 * @return the next date calculated by series from origin. 
	 * 		   origin could be a calculated date (result >= origin)
	 */
	public static DateTime getSeriesNext(String series, DateTime dateOrigin) {
		if (series == null)
			return dateOrigin;
		
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(dateOrigin.getDate());
		
		String[] split = org.apache.commons.lang.StringUtils.split(series, '|');
		
		if (split.length > 0){
			String modus = split[0];
			
			if ("d".equals(modus)){
				int hour = Integer.parseInt(split[1]);
				int minute = Integer.parseInt(split[2]);
				
				calendar.set(GregorianCalendar.HOUR_OF_DAY, hour);
				calendar.set(GregorianCalendar.MINUTE, minute);
				
				int days = Integer.parseInt(split[3]);
				if (days == 0){
					// add one day if calculated date is before origin
					if (calendar.getTime().before(dateOrigin.getDate())) {
						calendar.add(GregorianCalendar.DAY_OF_MONTH, 1);
					}
					
					while (!isWorkingDay(calendar.get(GregorianCalendar.DAY_OF_WEEK))){
						calendar.add(GregorianCalendar.DAY_OF_MONTH, 1);	
					}
					
				} else {
					// add one day if calculated date is before origin
					if (calendar.getTime().before(dateOrigin.getDate())) {
						calendar.add(GregorianCalendar.DAY_OF_MONTH, 1);
					}
					
					if (days > 1){
						calendar.add(GregorianCalendar.DAY_OF_MONTH, days-1);
					}
				}
				
			} else
			if ("w".equals(modus)){	
				int hour = Integer.parseInt(split[1]);
				int minute = Integer.parseInt(split[2]);
				
				calendar.set(GregorianCalendar.HOUR_OF_DAY, hour);
				calendar.set(GregorianCalendar.MINUTE, minute);
				
				int weeks = Integer.parseInt(split[3]);
				
				List<Integer> possibleWeekdays = new ArrayList<Integer>();
				int firstSelectedWeekday = -1000; 
				int lastWeekday = -1000;
				
				// use getWeekdayItems() in order to get the right start (end) of the week
				for (SeriesListItem sli : getWeekdayItems()){
					boolean addWeekday = false;
					
					switch (sli.getId()){
					case GregorianCalendar.MONDAY:
						if(split[4].equals("0")?false:true)
							addWeekday = true;
						break;
					case GregorianCalendar.TUESDAY:
						if(split[5].equals("0")?false:true)
							addWeekday = true;
						break;
					case GregorianCalendar.WEDNESDAY:
						if(split[6].equals("0")?false:true)
							addWeekday = true;
						break;
					case GregorianCalendar.THURSDAY:
						if(split[7].equals("0")?false:true)
							addWeekday = true;
						break;
					case GregorianCalendar.FRIDAY:
						if(split[8].equals("0")?false:true)
							addWeekday = true;
						break;
					case GregorianCalendar.SATURDAY:
						if(split[9].equals("0")?false:true)
							addWeekday = true;
						break;
					case GregorianCalendar.SUNDAY:
						if(split[10].equals("0")?false:true)
							addWeekday = true;
						break;
					}
					
					if (addWeekday){
						possibleWeekdays.add(sli.getId());
						if (firstSelectedWeekday == -1000)
							firstSelectedWeekday = sli.getId();
					}
					
					lastWeekday = sli.getId();
				}
				
				// add one day if calculated date is before origin
				boolean weeksAdded = false;
				if (calendar.getTime().before(dateOrigin.getDate())) {
					if (lastWeekday == calendar.get(GregorianCalendar.DAY_OF_WEEK)){
						calendar.add(GregorianCalendar.WEEK_OF_YEAR, weeks-1);
						weeksAdded = true;
					} else {
						calendar.add(GregorianCalendar.DAY_OF_MONTH, 1);
					}
				}
				
				while (!possibleWeekdays.contains(new Integer(calendar.get(GregorianCalendar.DAY_OF_WEEK)))){
					if (!weeksAdded && lastWeekday == calendar.get(GregorianCalendar.DAY_OF_WEEK)){
						calendar.add(GregorianCalendar.WEEK_OF_YEAR, weeks-1);
					}
					calendar.add(GregorianCalendar.DAY_OF_MONTH, 1);
				}
				
			} else
			if ("m".equals(modus)){	
				int hour = Integer.parseInt(split[1]);
				int minute = Integer.parseInt(split[2]);
				
				calendar.set(GregorianCalendar.HOUR_OF_DAY, hour);
				calendar.set(GregorianCalendar.MINUTE, minute);
				
				if ("m1".equals(split[3])){
					int day = Integer.parseInt(split[4]);
					int months = Integer.parseInt(split[5]);
					
					calendar.set(GregorianCalendar.DAY_OF_MONTH, day);
					
					// add one month if calculated date is before origin
					if (calendar.getTime().before(dateOrigin.getDate())) {
						calendar.add(GregorianCalendar.MONTH, 1);
						calendar.set(GregorianCalendar.DAY_OF_MONTH, day);
					}
					
					if (months > 1) {
						calendar.add(GregorianCalendar.MONTH, months-1);
						calendar.set(GregorianCalendar.DAY_OF_MONTH, day);
					}
					
				} else {
					int number = Integer.parseInt(split[4]);
					int weekday = Integer.parseInt(split[5]);
					int months = Integer.parseInt(split[6]);
					
					calendar.set(GregorianCalendar.DAY_OF_WEEK, weekday);
					calendar.set(GregorianCalendar.DAY_OF_WEEK_IN_MONTH, number);
					
					// add one month if calculated date is before origin
					if (calendar.getTime().before(dateOrigin.getDate())) {
						calendar.add(GregorianCalendar.MONTH, 1);
						calendar.set(GregorianCalendar.DAY_OF_WEEK, weekday);
						calendar.set(GregorianCalendar.DAY_OF_WEEK_IN_MONTH, number);
					}
					
					if (months > 1) {
						calendar.add(GregorianCalendar.MONTH, months-1);
						calendar.set(GregorianCalendar.DAY_OF_WEEK, weekday);
						calendar.set(GregorianCalendar.DAY_OF_WEEK_IN_MONTH, number);
					}
				}
				
			} else
			if ("y".equals(modus)){	
				int hour = Integer.parseInt(split[1]);
				int minute = Integer.parseInt(split[2]);
				
				calendar.set(GregorianCalendar.HOUR_OF_DAY, hour);
				calendar.set(GregorianCalendar.MINUTE, minute);
				
				if ("y1".equals(split[3])){
					int day = Integer.parseInt(split[4]);
					int month = Integer.parseInt(split[5]);
					
					calendar.set(GregorianCalendar.MONTH, month);
					calendar.set(GregorianCalendar.DAY_OF_MONTH, day);
					
					// add one year if calculated date is before origin
					if (calendar.getTime().before(dateOrigin.getDate())) {
						calendar.add(GregorianCalendar.YEAR, 1);
						calendar.set(GregorianCalendar.MONTH, month);
						calendar.set(GregorianCalendar.DAY_OF_MONTH, day);
					}
					
				} else {
					int number = Integer.parseInt(split[4]);
					int weekday = Integer.parseInt(split[5]);
					int month = Integer.parseInt(split[6]);
					
					calendar.set(GregorianCalendar.MONTH, month);
					calendar.set(GregorianCalendar.DAY_OF_WEEK, weekday);
					calendar.set(GregorianCalendar.DAY_OF_WEEK_IN_MONTH, number);
					
					// add one year if calculated date is before origin
					if (calendar.getTime().before(dateOrigin.getDate())) {
						calendar.add(GregorianCalendar.YEAR, 1);
						calendar.set(GregorianCalendar.MONTH, month);
						calendar.set(GregorianCalendar.DAY_OF_WEEK, weekday);
						calendar.set(GregorianCalendar.DAY_OF_WEEK_IN_MONTH, number);
					}
				}
			}
		}
		
		return new DateTime(calendar.getTimeInMillis());
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public static boolean isWorkingDay(int id){
		for (SeriesListItem sli : getWorkingDays()){
			if (sli.getId() == id){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 
	 * @return 
	 */
	public static List<SeriesListItem> getWorkingDays(){
		List<SeriesListItem> result = new ArrayList<SeriesListItem>();
		result.add(WEEKDAY_ITEM_MONDAY);
		result.add(WEEKDAY_ITEM_TUESDAY);
		result.add(WEEKDAY_ITEM_WEDNESDAY);
		result.add(WEEKDAY_ITEM_THURSDAY);
		result.add(WEEKDAY_ITEM_FRIDAY);
		
		return result;
	}
	
	/**
	 * 
	 * @return all WeekdayItems
	 */
	public static List<SeriesListItem> getWeekdayItems(){
		List<SeriesListItem> result = new ArrayList<SeriesListItem>();
		result.add(WEEKDAY_ITEM_MONDAY);
		result.add(WEEKDAY_ITEM_TUESDAY);
		result.add(WEEKDAY_ITEM_WEDNESDAY);
		result.add(WEEKDAY_ITEM_THURSDAY);
		result.add(WEEKDAY_ITEM_FRIDAY);
		result.add(WEEKDAY_ITEM_SATURDAY);
		result.add(WEEKDAY_ITEM_SUNDAY);
		
		return result;
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public static SeriesListItem getWeekdayItemById(int id){
		for (SeriesListItem sli : getWeekdayItems()){
			if (sli.getId() == id){
				return sli;
			}
		}
		
		throw new CommonFatalException("WeekdayItem with id "+ id +" not found!");
	}
	
	/**
	 * 
	 * @return all NumberItems
	 */
	public static List<SeriesListItem> getNumberItems(){
		List<SeriesListItem> result = new ArrayList<SeriesListItem>();
		result.add(NUMBER_ITEM_FIRST);
		result.add(NUMBER_ITEM_SECOND);
		result.add(NUMBER_ITEM_THIRD);
		result.add(NUMBER_ITEM_FOURTH);
		
		return result;
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public static SeriesListItem getNumberItemById(int id){
		for (SeriesListItem sli : getNumberItems()){
			if (sli.getId() == id){
				return sli;
			}
		}
		
		throw new CommonFatalException("NumberItem with id "+ id +" not found!");
	}
	
	/**
	 * 
	 * @return all MonthItems
	 */
	public static List<SeriesListItem> getMonthItems(){
		List<SeriesListItem> result = new ArrayList<SeriesListItem>();
		result.add(MONTH_ITEM_JANUARY);
		result.add(MONTH_ITEM_FEBRUARY);
		result.add(MONTH_ITEM_MARCH);
		result.add(MONTH_ITEM_APRIL);
		result.add(MONTH_ITEM_MAY);
		result.add(MONTH_ITEM_JUNE);
		result.add(MONTH_ITEM_JULY);
		result.add(MONTH_ITEM_AUGUST);
		result.add(MONTH_ITEM_SEPTEMBER);
		result.add(MONTH_ITEM_OCTOBER);
		result.add(MONTH_ITEM_NOVEMBER);
		result.add(MONTH_ITEM_DECEMBER);
		
		return result;
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public static SeriesListItem getMonthItemById(int id){
		for (SeriesListItem sli : getMonthItems()){
			if (sli.getId() == id){
				return sli;
			}
		}
		
		throw new CommonFatalException("MonthItem with id "+ id +" not found!");
	}
	
	/**
	 * 
	 * @author maik.stueker
	 *
	 */
	public static class SeriesListItem{
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof SeriesListItem){
				return this.getId() == ((SeriesListItem)obj).getId();
			}
			return super.equals(obj);
		}

		private String label;
		private int id;
		
		public SeriesListItem(String label, int id){
			this.label = label;
			this.id = id;
		}

		public String getLabel() {
			return label;
		}

		public int getId() {
			return id;
		}
		
		@Override
		public String toString(){
			return label==null?"":label;
		}
	}
}
