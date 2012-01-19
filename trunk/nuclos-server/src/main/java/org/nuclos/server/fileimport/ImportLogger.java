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
package org.nuclos.server.fileimport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.nuclos.common.HashResourceBundle;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.StringUtils;

/**
 * Utility class for logging in file imports.
 *
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class ImportLogger {

	private final Logger logger;

	private final HashResourceBundle bundle;

	private int errorcount = 0;

	public int getErrorcount() {
		return errorcount;
	}

	public ImportLogger(String filename, HashResourceBundle bundle) {
		logger = Logger.getLogger(filename);
		logger.setLevel(Level.INFO);

		logger.removeAllAppenders();
		Layout layout = new PatternLayout("%d{ISO8601} %m%n");
		FileAppender appender;
		try {
			appender = new FileAppender(layout, filename);
			logger.addAppender(appender);
		}
		catch(IOException e) {
			throw new NuclosFatalException(e);
		}

		this.bundle = bundle;
	}

	public String info(String message) {
		String result = localize(message);
		logger.info(result);
		return result;
	}

	public void error(String message) {
		errorcount++;
		logger.error(localize(message));
	}

	public void error(String message, Throwable ex) {
		errorcount++;
		logger.error(localize(message, ex));
	}

	public void error(int line, String message) {
		errorcount++;
		logger.error(localize(StringUtils.getParameterizedExceptionMessage("import.logging.line", line, localize(message))));
	}

	public void error(int line, String message, Throwable ex) {
		errorcount++;
		logger.error(localize(StringUtils.getParameterizedExceptionMessage("import.logging.line", line, localize(message, ex))));
	}

	protected String getStringResource(String rid) {
		return bundle.getString(rid);
	}

	protected String localize(String message, Throwable ex) {
		return localize(message) + "; " + localizeException(ex);
	}

	private String localizeException(Throwable ex) {
		StringBuilder result = new StringBuilder();
		if (ex.getLocalizedMessage() != null) {
			result.append(localize(ex.getLocalizedMessage()));
		}
		else if (ex.getMessage() != null) {
			result.append(localize(ex.getMessage()));
		}
		else {
			result.append(ex.toString());
		}
		return result.toString();
	}

	public String localize(String message) {
		String resText = null;
		try {
			resText = bundle.getString(StringUtils.getFirstSubString(message, CollectionUtils.asSet("{"," ")));
		}
		catch (RuntimeException e) {
			// text seems to be no resourceId
		}

		if (resText != null) {
			Pattern REF_PATTERN = Pattern.compile("\\{([^\\}]*?)\\}");
			List<String> paramList = new ArrayList<String>();
			StringBuffer rb = new StringBuffer();

			Matcher m = REF_PATTERN.matcher(message);
			while(m.find()) {
				String param = m.group(1);
				try {
					if (param.length() > 0 && bundle.containsKey(param.trim())) {
						param = bundle.getString(param.trim());
					}
				}
				catch (RuntimeException e) {
					// param seems to be no resourceId
				}
				paramList.add(param);
			}

			m = REF_PATTERN.matcher(resText);
			while(m.find()) {
				if (paramList.size() > Integer.valueOf(m.group(1))) {
					m.appendReplacement(rb, paramList.get(Integer.valueOf(m.group(1))));
				}
			}

			m.appendTail(rb);

			return rb.toString();
		}
		else {
			return message;
		}
	}
}
