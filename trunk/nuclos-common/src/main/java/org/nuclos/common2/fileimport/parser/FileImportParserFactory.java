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
package org.nuclos.common2.fileimport.parser;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import org.nuclos.common2.StringUtils;
import org.nuclos.common2.fileimport.CommonParseException;

/**
 * Factory for <code>FileImportParser</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:buchmann@novabit.de">buchmann</a>
 * @version	01.00.00
 * @todo refactor
 * @todo cache parser instances
 * @todo complete tests and implementations
 * @todo write javadoc
 */

public class FileImportParserFactory {

	private static final Logger log = Logger.getLogger(FileImportParserFactory.class);

	private static FileImportParserFactory singleton;

	private FileImportParserFactory() {
	}

	public synchronized static FileImportParserFactory getInstance() {
		if (singleton == null) {
			singleton = new FileImportParserFactory();
		}
		return singleton;
	}

	/**
	 * @param cls
	 * @param sValue
	 * @param sFormat
	 * @return
	 * @throws CommonParseException
	 */
	public Object parse(Class<?> cls, String sValue, String sFormat) throws CommonParseException {
		return this.getParser(cls).parse(sValue, sFormat);
	}

	private FileImportParser getParser(Class<?> cls) throws CommonParseException {
		final FileImportParser result;

		if (cls.equals(Integer.class)) {
			result = new IntegerFileImportParser();
		}
		else if (cls.equals(Date.class)) {
			result = new DateFileImportParser();
		}
		else if (cls.equals(Double.class)) {
			result = new DoubleFileImportParser();
		}
		else if (cls.equals(String.class)) {
			result = new StringFileImportParser();
		}
		else if (cls.equals(BigDecimal.class)) {
			result = new BigDecimalFileImportParser();
		}
		else if (cls.equals(Boolean.class)) {
			//NUCLEUSINT-479
			result = new BooleanFileImportParser();
		}
		else {
			throw new CommonParseException(StringUtils.getParameterizedExceptionMessage("file.import.unknown.parser", cls.getName()));//"Es gibt keinen Parser f\u00fcr die Klasse " + cls.getName() + ".");
		}
		return result;
	}

	private static class StringFileImportParser implements FileImportParser {

		@Override
		public Object parse(String sValue, String sFormat) throws CommonParseException {
			return StringUtils.looksEmpty(sFormat) ? sValue : replace(sValue, sFormat);
		}

		private static String replace(String sValue, String sFormat) throws CommonParseException {
			final String sMetaPattern = "^(.*[^\\\\])#(.*)$";
			final Pattern pattern = Pattern.compile(sMetaPattern);
			final Matcher matcher = pattern.matcher(sFormat);
			if (!matcher.matches()) {
				throw new CommonParseException(StringUtils.getParameterizedExceptionMessage("file.import.invalid.format", StringUtils.emptyIfNull(sFormat), sMetaPattern));
					//"Das Format \"" + StringUtils.emptyIfNull(sFormat) + "\" ist nicht korrekt. Erwartet wird eine Ersetzungszeichenkette, die dem regul\u00e4ren Ausdruck \"" + sMetaPattern + "\" entspricht.");
			}
			final String sRegExp = matcher.group(1);
			final String sReplacement = matcher.group(2);
			return replace(sRegExp, sReplacement, sValue);
		}

		private static String replace(String sRegExp, String sReplacement, String sValue) throws CommonParseException {
			final Pattern pattern = Pattern.compile(sRegExp);
			final Matcher matcher = pattern.matcher(sValue);
			if (!matcher.matches()) {
				throw new CommonParseException(StringUtils.getParameterizedExceptionMessage("file.import.incompatible.value", StringUtils.emptyIfNull(sValue), sRegExp));
					//"Der Wert \"" + StringUtils.emptyIfNull(sValue) + "\" ist nicht zum Pattern \"" + sRegExp + "\" kompatibel.");
			}
			final int iGroupCount = matcher.groupCount();
			String result = sReplacement;
			for (int iGroup = 0; iGroup <= iGroupCount; iGroup++) {
				final String sMatchedGroup = matcher.group(iGroup);
				if (sMatchedGroup == null) {
					log.warn("Group " + iGroup + " failed to match part of the input \"" + sValue + "\" with regexp \"" + sRegExp + "\".");
				}
				else {
					final String sParameter = "\\$" + iGroup + "\\$";
					result = result.replaceAll(sParameter, sMatchedGroup);
				}
			}
			return result;
		}
	}

	private static class IntegerFileImportParser implements FileImportParser {
		@Override
		public Object parse(String sValue, String sFormat) throws CommonParseException {
			// first apply a (regexp) format, if any:
			final String sReplacedValue = StringUtils.looksEmpty(sFormat) ? sValue : StringFileImportParser.replace(sValue, sFormat);
			try {
				return StringUtils.looksEmpty(sReplacedValue) ? null : new Integer(sReplacedValue);
			}
			catch (NumberFormatException ex) {
				throw new CommonParseException(StringUtils.getParameterizedExceptionMessage("file.import.invalid.integer", StringUtils.emptyIfNull(sValue)));
				//"Fehler beim Parsen einer Ganzzahl: Wert: \"" + StringUtils.emptyIfNull(sValue) + "\".", ex);
			}
		}
	}

	private static class DateFileImportParser implements FileImportParser {
		@Override
		public Object parse(String sValue, String sFormat) throws CommonParseException {
			final Object result;

			final DateFormat df;
			if (sFormat == null || sFormat.equals("")) {
				df = new SimpleDateFormat("dd.MM.yyyy");
			}
			else {
				try {
					df = new SimpleDateFormat(sFormat);
				}
				catch (IllegalArgumentException ex) {
					throw new CommonParseException(StringUtils.getParameterizedExceptionMessage("file.import.invalid.dateformat", StringUtils.defaultIfNull(sFormat, "dd.MM.yyyy")));
					//"Fehler im Datumsformat \"" + StringUtils.defaultIfNull(sFormat, "dd.MM.yyyy") + "\".", ex);
				}
			}

			if (StringUtils.looksEmpty(sValue)) {
				result = null;
			}
			else {
				try {
					return df.parse(sValue);
				}
				catch (ParseException ex) {
					throw new CommonParseException(StringUtils.getParameterizedExceptionMessage("file.import.invalid.date", StringUtils.defaultIfNull(sFormat, "dd.MM.yyyy"), StringUtils.emptyIfNull(sValue)));
						//"Fehler beim Parsen eines Datums im Format \"" + StringUtils.defaultIfNull(sFormat, "dd.MM.yyyy") + "\": Wert: \"" + StringUtils.emptyIfNull(sValue) + "\".", ex);
				}
			}
			return result;
		}
	}

	private static class DoubleFileImportParser implements FileImportParser {
		@Override
		public Object parse(String sValue, String sFormat) throws CommonParseException {
			try {
				// ignore format
				/** @todo use format here */
				return StringUtils.looksEmpty(sValue) ? null : new Double(sValue.replace(',', '.'));
			}
			catch (NumberFormatException ex) {
				throw new CommonParseException(StringUtils.getParameterizedExceptionMessage("file.import.invalid.double", StringUtils.emptyIfNull(sValue)));
					//"Fehler beim Parsen einer Flie\u00dfkommazahl: Wert: \"" + StringUtils.emptyIfNull(sValue) + "\".", ex);
			}
		}
	}

	private static class BigDecimalFileImportParser implements FileImportParser {
		@Override
		public Object parse(String sValue, String sFormat) throws CommonParseException {
			try {
				// ignore format
				/** @todo use format here */
				return StringUtils.looksEmpty(sValue) ? null : new BigDecimal(sValue.replace(',', '.'));
			}
			catch (NumberFormatException ex) {
				throw new CommonParseException(StringUtils.getParameterizedExceptionMessage("file.import.invalid.bigdecimal", StringUtils.emptyIfNull(sValue)));
					//"Fehler beim Parsen einer Festkommazahl: Wert: \"" + StringUtils.emptyIfNull(sValue) + "\".", ex);
			}
		}
	}
	
	//NUCLEUSINT-479
	private static class BooleanFileImportParser implements FileImportParser {
		@Override
		public Object parse(String sValue, String sFormat) throws CommonParseException {
			// ignore format
			/** @todo use format here */
			String[] trueValues = { "ja", "yes", "true", "1", "wahr", "y", "j" };
			String[] falseValues = { "nein", "no", "false", "0", "falsch", "n", "n" };

			if (!StringUtils.looksEmpty(sValue)) {
				for (String string : trueValues) {
					if (string.equalsIgnoreCase(sValue))
						return new Boolean(true);
				}

				for (String string : falseValues) {
					if (string.equalsIgnoreCase(sValue))
						return new Boolean(false);
				}
			}
			return new Boolean(false);
		}
	}

}  // class FileImportParserFactory
