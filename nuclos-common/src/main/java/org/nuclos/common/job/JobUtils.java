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
package org.nuclos.common.job;

import java.text.DateFormatSymbols;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class JobUtils {

	private static Map<IntervalUnit, String> templates = new HashMap<IntervalUnit, String>();

	static {
		templates.put(IntervalUnit.MINUTE, "0 */{4} * * * ?");
		templates.put(IntervalUnit.HOUR, "0 {0} */{4} * * ?");
		templates.put(IntervalUnit.DAY, "0 {0} {1} */{4} * ?");
		templates.put(IntervalUnit.MONTH, "0 {0} {1} {2} */{4} ?");
	}

	/**
	 * Seconds 	YES 	0-59 	, - * /
	 * Minutes 	YES 	0-59 	, - * /
	 * Hours 	YES 	0-23 	, - * /
	 * Day of month 	YES 	1-31 	, - * ? / L W
	 * Month 	YES 	1-12 or JAN-DEC 	, - * /
	 * Day of week 	YES 	1-7 or SUN-SAT 	, - * ? / L #
	 * Year 	NO 	empty, 1970-2099 	, - * /
	 *
	 * @param unit
	 * @param interval
	 * @param startTime
	 * @return
	 */
	public static String getCronExpressionFromInterval(IntervalUnit unit, Integer interval, Calendar calendar) {
		return MessageFormat.format(templates.get(unit),
			calendar.get(Calendar.MINUTE),
			calendar.get(Calendar.HOUR_OF_DAY),
			calendar.get(Calendar.DAY_OF_MONTH),
			DateFormatSymbols.getInstance(Locale.ENGLISH).getShortMonths()[calendar.get(Calendar.MONTH)].toUpperCase(),
			interval);
	}
}
