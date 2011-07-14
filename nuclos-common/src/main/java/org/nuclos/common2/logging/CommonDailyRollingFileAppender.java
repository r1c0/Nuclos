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
package org.nuclos.common2.logging;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Modified version of the DailyRollingFileAppender.
 * By specifying the MaxBackupDays option it is possible to set a number of days as a maximum
 * life time for the backup files. After this period has expired, older files are deleted.
 *
 * Example for inclusion in log4j.xml:
 *<code>
 * &lt;appender name="ROLLINGFILE" class="org.nuclos.common2.logging.CommonDailyRollingFileAppender"&gt;
 *	 &lt;param name="File" value="${jboss.server.home.dir}/log/client.log"/&gt;
 *	 &lt;param name="Append" value="true"/&gt;
 *	 &lt;param name="Threshold" value="DEBUG"/&gt;
 *	 &lt;param name="MaxBackupDays" value="1"/&gt;
 *	 &lt;!-- Rollover each hour --&gt;
 *	 &lt;param name="DatePattern" value="'.'yyyy-MM-dd-HH"/&gt;
 *	 &lt;!-- Rollover each day --&gt;
 *	 &lt;!-- param name="DatePattern" value="'.'yyyy-MM-dd"/--&gt;
 *	 &lt;layout class="org.apache.log4j.PatternLayout"&gt;
 *		 &lt;param name="ConversionPattern" value="%d{ISO8601} %-5p [%c] %m%n"/&gt;
 *	 &lt;/layout&gt;
 *&lt;/appender>
 *</code>
 *
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version	01.00.00
 */
public class CommonDailyRollingFileAppender extends FileAppender {
	// The code assumes that the following constants are in an increasing
	// sequence.
	static final int TOP_OF_TROUBLE=-1;
	static final int TOP_OF_MINUTE = 0;
	static final int TOP_OF_HOUR   = 1;
	static final int HALF_DAY      = 2;
	static final int TOP_OF_DAY    = 3;
	static final int TOP_OF_WEEK   = 4;
	static final int TOP_OF_MONTH  = 5;


	/**
	 The date pattern. By default, the pattern is set to
	 "'.'yyyy-MM-dd" meaning daily rollover.
	 */
	private String datePattern = "'.'yyyy-MM-dd";

	/**
	 The log file will be renamed to the value of the
	 scheduledFilename variable when the next interval is entered. For
	 example, if the rollover period is one hour, the log file will be
	 renamed to the value of "scheduledFilename" at the beginning of
	 the next hour.

	 The precise time when a rollover occurs depends on logging
	 activity.
	 */
	private String nakedFileName;

	private int iMaxBackupDays;

	public String getMaxBackupDays() {
		return new StringBuffer("").append(iMaxBackupDays).toString();
	}

	public void setMaxBackupDays(String sMaxBackupDays) {
		this.iMaxBackupDays = Integer.parseInt(sMaxBackupDays);
	}

	/**
	 The next time we estimate a rollover should occur. */
	private long nextCheck = System.currentTimeMillis () - 1;

	Date now = new Date();

	SimpleDateFormat sdf;

	RollingCalendar rc = new RollingCalendar();

	int checkPeriod = TOP_OF_TROUBLE;

	// The gmtTimeZone is used only in computeCheckPeriod() method.
	static final TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");


	/**
	 The default constructor does nothing. */
	public CommonDailyRollingFileAppender() {
	}

	/**
	 * Instantiate a <code>DailyRollingFileAppender</code> and open the file designated by <code>filename</code>.
	 * The opened filename will become the ouput destination for this appender.
	 * @param layout
	 * @param filename
	 * @param datePattern
	 * @throws IOException when the specified file cannot be created or written to
	 */
	public CommonDailyRollingFileAppender (Layout layout, String filename, String datePattern) throws IOException {
		super(layout, filename + new SimpleDateFormat(datePattern).format(new Date()), true);
		nakedFileName = filename;
		this.datePattern = datePattern;
	}

	/**
	 The <b>DatePattern</b> takes a string in the same format as
	 expected by {@link SimpleDateFormat}. This options determines the
	 rollover schedule.
	 */
	public void setDatePattern(String pattern) {
		datePattern = pattern;
	}

	/** Returns the value of the <b>DatePattern</b> option. */
	public String getDatePattern() {
		return datePattern;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void activateOptions() {
		//super.activateOptions();

		if(datePattern != null && fileName != null) {
			now.setTime(System.currentTimeMillis());
			sdf = new SimpleDateFormat(datePattern);
			int type = computeCheckPeriod();
			printPeriodicity(type);
			rc.setType(type);
			if(nakedFileName == null) {
				nakedFileName = new File(fileName).getAbsolutePath();	// Ensure correct format of path string (separators etc.)

				try {
					this.setFile(nakedFileName + sdf.format(new Date()), true, this.bufferedIO, this.bufferSize);
				}
				catch(IOException ex) {
					errorHandler.error("setFile(" + fileName + ", false) call failed.");
				}

				deleteOutdatedFiles();
			}
		}
		else {
			LogLog.error("Either File or DatePattern options are not set for appender [" + name + "].");
		}
	}

	void printPeriodicity(int type) {
		switch(type) {
			case TOP_OF_MINUTE:
				LogLog.debug("Appender [" + name + "] to be rolled every minute.");
				break;
			case TOP_OF_HOUR:
				LogLog.debug("Appender [" + name + "] to be rolled on top of every hour.");
				break;
			case HALF_DAY:
				LogLog.debug("Appender [" + name + "] to be rolled at midday and midnight.");
				break;
			case TOP_OF_DAY:
				LogLog.debug("Appender [" + name + "] to be rolled at midnight.");
				break;
			case TOP_OF_WEEK:
				LogLog.debug("Appender [" + name + "] to be rolled at start of week.");
				break;
			case TOP_OF_MONTH:
				LogLog.debug("Appender [" + name + "] to be rolled at start of every month.");
				break;
			default:
				LogLog.warn("Unknown periodicity for appender [" + name + "].");
		}
	}


	// This method computes the roll over period by looping over the
	// periods, starting with the shortest, and stopping when the r0 is
	// different from from r1, where r0 is the epoch formatted according
	// the datePattern (supplied by the user) and r1 is the
	// epoch+nextMillis(i) formatted according to datePattern. All date
	// formatting is done in GMT and not local format because the test
	// logic is based on comparisons relative to 1970-01-01 00:00:00
	// GMT (the epoch).

	int computeCheckPeriod() {
		RollingCalendar rollingCalendar = new RollingCalendar(gmtTimeZone, Locale.ENGLISH);

		// set date to 1970-01-01 00:00:00 GMT
		Date epoch = new Date(0);
		if(datePattern != null) {
			for(int i = TOP_OF_MINUTE; i <= TOP_OF_MONTH; i++) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
				simpleDateFormat.setTimeZone(gmtTimeZone); // do all date formatting in GMT
				String r0 = simpleDateFormat.format(epoch);
				rollingCalendar.setType(i);
				Date next = new Date(rollingCalendar.getNextCheckMillis(epoch));
				String r1 =  simpleDateFormat.format(next);

				if(r0 != null && r1 != null && !r0.equals(r1)) {
					return i;
				}
			}
		}
		return TOP_OF_TROUBLE; // Deliberately head for trouble...
	}

	/**
	 Rollover the current file to a new file.
	 */
	void rollOver() {

		/* Compute filename, but only if datePattern is specified */
		if (datePattern == null) {
			errorHandler.error("Missing DatePattern option in rollOver().");
			return;
		}

		String datedFilename = nakedFileName + sdf.format(now);
		// It is too early to roll over because we are still within the
		// bounds of the current interval. Rollover will occur once the
		// next interval is reached.
		if (fileName.equals(datedFilename)) {
			return;
		}

		deleteOutdatedFiles();

		// close current file, and switch to datedFilename
		this.closeFile();

		try {
			// This will also close the file. This is OK since multiple close operations are safe.
			this.setFile(datedFilename, false, this.bufferedIO, this.bufferSize);
		}
		catch(IOException e) {
			errorHandler.error("setFile(" + fileName + ", false) call failed.");
		}
		fileName = datedFilename;
	}

	/**
	 * Delete all files in the log directory with the given file name pattern and older than the maximum number of days to keep.
	 */
	private void deleteOutdatedFiles() {
		try {
			// Check if there is a file that has outlived its maximum life time; if so, delete it
			GregorianCalendar calOut = new GregorianCalendar();
			calOut.setTime(now);
			calOut.add(Calendar.DAY_OF_MONTH, -iMaxBackupDays);
			final Date dateOut = calOut.getTime();

			if (fileName != null) {
				// Find all files in the log directory which are older than dateOut, and delete them
				String filePath = new File(new File(fileName).getAbsolutePath()).getParent();
				File[] aFiles = new File(filePath).listFiles(new FileFilter() {
					@Override
					public boolean accept(File file) {
						boolean result = false;
						if(file.getPath().startsWith(nakedFileName) && file.lastModified() < dateOut.getTime()) {
							result = true;
						}
						return result;
					}
				});

				for(int i=0; i<aFiles.length;i++) {
					aFiles[i].delete();
				}
			}
		}
		catch (Exception ex) {
			System.err.println("Could not delete outdated log files:" + ex.getMessage());
		}
	}

	/**
	 * This method differentiates DailyRollingFileAppender from its
	 * super class.
	 *
	 * <p>Before actually logging, this method will check whether it is
	 * time to do a rollover. If it is, it will schedule the next
	 * rollover time and then rollover.
	 * */
	@Override
	protected void subAppend(LoggingEvent event) {
		long n = System.currentTimeMillis();
		if (n >= nextCheck) {
			now.setTime(n);
			nextCheck = rc.getNextCheckMillis(now);

			rollOver();
		}
		super.subAppend(event);
	}
}

/**
 *  RollingCalendar is a helper class to DailyRollingFileAppender.
 *  Given a periodicity type and the current time, it computes the
 *  start of the next interval.
 * */
class RollingCalendar extends GregorianCalendar {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int type = CommonDailyRollingFileAppender.TOP_OF_TROUBLE;

	RollingCalendar() {
		super();
	}

	RollingCalendar(TimeZone tz, Locale locale) {
		super(tz, locale);
	}

	void setType(int type) {
		this.type = type;
	}

	public long getNextCheckMillis(Date now) {
		return getNextCheckDate(now).getTime();
	}

	public Date getNextCheckDate(Date now) {
		this.setTime(now);

		switch(type) {
			case CommonDailyRollingFileAppender.TOP_OF_MINUTE:
				this.set(Calendar.SECOND, 0);
				this.set(Calendar.MILLISECOND, 0);
				this.add(Calendar.MINUTE, 1);
				break;
			case CommonDailyRollingFileAppender.TOP_OF_HOUR:
				this.set(Calendar.MINUTE, 0);
				this.set(Calendar.SECOND, 0);
				this.set(Calendar.MILLISECOND, 0);
				this.add(Calendar.HOUR_OF_DAY, 1);
				break;
			case CommonDailyRollingFileAppender.HALF_DAY:
				this.set(Calendar.MINUTE, 0);
				this.set(Calendar.SECOND, 0);
				this.set(Calendar.MILLISECOND, 0);
				int hour = get(Calendar.HOUR_OF_DAY);
				if(hour < 12) {
					this.set(Calendar.HOUR_OF_DAY, 12);
				}
				else {
					this.set(Calendar.HOUR_OF_DAY, 0);
					this.add(Calendar.DAY_OF_MONTH, 1);
				}
				break;
			case CommonDailyRollingFileAppender.TOP_OF_DAY:
				this.set(Calendar.HOUR_OF_DAY, 0);
				this.set(Calendar.MINUTE, 0);
				this.set(Calendar.SECOND, 0);
				this.set(Calendar.MILLISECOND, 0);
				this.add(Calendar.DATE, 1);
				break;
			case CommonDailyRollingFileAppender.TOP_OF_WEEK:
				this.set(Calendar.DAY_OF_WEEK, getFirstDayOfWeek());
				this.set(Calendar.HOUR_OF_DAY, 0);
				this.set(Calendar.SECOND, 0);
				this.set(Calendar.MILLISECOND, 0);
				this.add(Calendar.WEEK_OF_YEAR, 1);
				break;
			case CommonDailyRollingFileAppender.TOP_OF_MONTH:
				this.set(Calendar.DATE, 1);
				this.set(Calendar.HOUR_OF_DAY, 0);
				this.set(Calendar.SECOND, 0);
				this.set(Calendar.MILLISECOND, 0);
				this.add(Calendar.MONTH, 1);
				break;
			default:
				throw new IllegalStateException("Unknown periodicity type.");
		}
		return getTime();
	}
}