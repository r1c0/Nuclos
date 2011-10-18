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
package org.nuclos.common.collect.collectable;



import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.NuclosImage;
import org.nuclos.common.NuclosPassword;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.DateTime;
import org.nuclos.common2.ExtendedRelativeDate;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.RelativeDate;
import org.nuclos.common2.StringUtils;

/**
 * Defines formatting and parsing of <code>CollectableField</code>s. This may be used to get the
 * value of a <code>CollectableField</code> into or out of a <code>CollectableTextComponent</code>.
 * This class is deliberately dependent on the default Locale.
 * @todo But, it shouldn't be! This class is to be used on a server as well as on a client.
 * @todo handle empty strings consistently!
 * Thus the Locale should be given as a parameter.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public abstract class CollectableFieldFormat {

	private static final Logger log = Logger.getLogger(CollectableFieldFormat.class);

	private static Map<Class<?>, CollectableFieldFormat> mpFormats;
	private static CollectableFieldFormat clctfformatDefault;

	/**
	 * formats the given value according to the given output format.
	 * @param sOutputFormat
	 * @param oValue
	 * @return a String representation of the given value.
	 */
	public abstract String format(String sOutputFormat, Object oValue);

	/**
	 * parses the given text according to the given input format.
	 * @param sOutputFormat
	 * @param sInputFormat
	 * @param sText
	 * @return the parsed object
	 * @throws CollectableFieldFormatException
	 */
	public abstract Object parse(String sInputFormat, String sText) throws CollectableFieldFormatException;

	/**
	 * @param cls
	 * @return an appropriate instance of <code>CollectableFieldFormat</code> for the given data type.
	 */
	public static CollectableFieldFormat getInstance(Class<?> cls) {
		CollectableFieldFormat result = getFormat(cls);

		if (result instanceof CollectableTimestampFormat) {
			// set property here, because initialization is static and parameters can be changed at runtime
			ParameterProvider provider = SpringApplicationContextHolder.getBean(ParameterProvider.class);
			String value = provider.getValue(ParameterProvider.KEY_SHOW_INTERNAL_TIMESTAMP_WITH_TIME);
			((CollectableTimestampFormat) result).setFormatWithTime(value != null && value.toUpperCase().equals("TRUE"));
		}
		// We also allow classes derived from java.util.Date, especially java.sql.Date:
		if (result == null && java.util.Date.class.isAssignableFrom(cls)) {
			result = getFormat(Date.class);
		}
		if (result == null) {
			if (log.isDebugEnabled()) {
				log.debug("Default-Format erzeugt f\u00fcr Klasse " + cls.getName());
			}
			return getDefaultFormat();
		}
		return result;
	}

	private static synchronized CollectableFieldFormat getDefaultFormat() {
		if (clctfformatDefault == null) {
			clctfformatDefault = new DefaultFormat();
		}
		return clctfformatDefault;
	}

	private static synchronized Map<Class<?>, CollectableFieldFormat> getMapOfFormats() {
		if (mpFormats == null) {
			mpFormats = new HashMap<Class<?>, CollectableFieldFormat>(9);
			mpFormats.put(String.class, new CollectableStringFormat());
			mpFormats.put(Date.class, new CollectableDateFormat());
			mpFormats.put(DateTime.class, new CollectableDateTimeFormat());
			mpFormats.put(Integer.class, new CollectableIntegerFormat());
			mpFormats.put(Double.class, new CollectableDoubleFormat());
			mpFormats.put(Boolean.class, new CollectableBooleanFormat());
			mpFormats.put(BigDecimal.class, new CollectableBigDecimalFormat());
			mpFormats.put(NuclosImage.class, new CollectableNuclosImageFormat());
			mpFormats.put(NuclosPassword.class, new CollectablePasswordFormat());
			mpFormats.put(InternalTimestamp.class, new CollectableTimestampFormat());
		}
		return mpFormats;
	}

	private static CollectableFieldFormat getFormat(Class<?> cls) {
		return getMapOfFormats().get(cls);
	}

	private static class CollectableStringFormat extends CollectableFieldFormat {
		@Override
		public String format(String sOutputFormat, Object oValue) {
			return (String) oValue;
		}

		@Override
		public Object parse(String sInputFormat, String sText) {
			return sText;
		}
	}	// class CollectableStringFormat

	private static class CollectableNuclosImageFormat extends CollectableFieldFormat {
		@Override
		public String format(String sOutputFormat, Object oValue) {
			final NuclosImage image = (NuclosImage) oValue;
			if (image == null || image.getContent() == null || image.getContent().length == 0) {
				return "";
			}
			final String base64 = Base64.encodeBase64String(image.getContent());
			return "[$" + CollectableFieldFormat.class.getName() + "," + oValue.getClass().getName() + "," + image.getContent().length+ "," + base64 + "$]";
		}

		@Override
		public Object parse(String sInputFormat, String sText) {
			return new NuclosImage("", Base64.decodeBase64(sText), null, true);
		}
	}	// class CollectableStringFormat
	
	private static class CollectablePasswordFormat extends CollectableFieldFormat {
		@Override
		public String format(String outputFormat, Object value) {
			return value == null ? "" : ((NuclosPassword) value).getValue();
		}

		@Override
		public Object parse(String inputFormat, String text) {
			return text == null ? null : new NuclosPassword(text);
		}
	}

	private static class CollectableDateTimeFormat extends CollectableFieldFormat {

		private static java.text.DateFormat getDateFormat() {
			// Note that a new DateFormat must be created each time as SimpleDateFormat is not thread safe!
			final SimpleDateFormat result = new SimpleDateFormat(DateTime.DATE_FORMAT_STRING, Locale.GERMANY);
			result.setLenient(false);
			return result;
		}

		@Override
		public String format(String sOutputFormat, Object oValue) {
			if (oValue == null) {
				return null;
			}
			if (!(oValue instanceof org.nuclos.common2.DateTime)) {
				throw new NuclosFatalException("Kein g\u00fcltiges Datum: " + oValue + " (" + oValue.getClass() + ").");
			}
			return getDateFormat().format(((org.nuclos.common2.DateTime)oValue).getDate());
		}

		@Override
		public org.nuclos.common2.DateTime parse(String sInputFormat, String sText) throws CollectableFieldFormatException {
			if (StringUtils.looksEmpty(sText)) {
				return null;
			}
			try {
				return new DateTime(getDateFormat().parse(sText));
			}
			catch (ParseException ex) {
				throw new CollectableFieldFormatException("Ung\u00fcltiges Datum: " + sText, ex);
			}
		}
	}	// class CollectableDateTimeFormat

	private static class CollectableDateFormat extends CollectableFieldFormat {

		@Override
		public String format(String sOutputFormat, Object oValue) {
			if (oValue == null) {
				return null;
			}
			else if (oValue.toString().equalsIgnoreCase(RelativeDate.today().toString())) {
				return CommonLocaleDelegate.getMessage("datechooser.today.label", "Heute");
			}
			else if (oValue instanceof ExtendedRelativeDate) {
				return ((ExtendedRelativeDate)oValue).getString(CommonLocaleDelegate.getMessage("datechooser.today.label", "Heute"));
			}
			else if(sOutputFormat != null) {
				SimpleDateFormat sdf = new SimpleDateFormat(sOutputFormat);
				return sdf.format(oValue);
			}
			else {
				return CommonLocaleDelegate.getDateFormat().format(oValue);
			}
		}

		@Override
		public java.util.Date parse(String sInputFormat, String sText) throws CollectableFieldFormatException {
			java.util.Date result = null;

			if (StringUtils.looksEmpty(sText)) {
				return null;
			}

			sText = sText.toUpperCase();

			String labelToday = CommonLocaleDelegate.getMessage("datechooser.today.label", "Heute");
			if (sText.equalsIgnoreCase(RelativeDate.today().toString()) || sText.equalsIgnoreCase(labelToday)) {
				result = RelativeDate.today();
			}
			else if (sText.startsWith(labelToday.toUpperCase())) {
				result = ExtendedRelativeDate.today();

				// remove 'LABEL_TODAY' (HEUTE)
				String sDateWithoutToday = sText.substring(labelToday.length()).trim();

				if (StringUtils.looksEmpty(sDateWithoutToday)) {
					return result;
				}

				// get operand
				if (sDateWithoutToday.startsWith(ExtendedRelativeDate.NEGATIVE_OPERAND)) {
					((ExtendedRelativeDate)result).setOperand(ExtendedRelativeDate.NEGATIVE_OPERAND);
				}
				else if (sDateWithoutToday.startsWith(ExtendedRelativeDate.POSITIVE_OPERAND)){
					((ExtendedRelativeDate)result).setOperand(ExtendedRelativeDate.POSITIVE_OPERAND);
				}
				else {
					throw new CollectableFieldFormatException(CommonLocaleDelegate.getMessage("CollectableFieldFormat.3","Invalid date: {0}", sText));
				}

				sDateWithoutToday = sDateWithoutToday.substring(1).trim();

				if (StringUtils.looksEmpty(sDateWithoutToday)) {
					return result;
				}

				// get unit
				String sUnit = sDateWithoutToday.substring(sDateWithoutToday.length()-1);

				if (Character.isLetter(sUnit.charAt(0))) {
					if (sUnit.equalsIgnoreCase(ExtendedRelativeDate.UNIT_DAY_DE) ||
							sUnit.equalsIgnoreCase(ExtendedRelativeDate.UNIT_DAY_EN)) {
						((ExtendedRelativeDate)result).setUnit(sUnit.toUpperCase());
					}
					else if (sUnit.equalsIgnoreCase(ExtendedRelativeDate.UNIT_MONTH)) {
						((ExtendedRelativeDate)result).setUnit(ExtendedRelativeDate.UNIT_MONTH);
					}
					else {
						throw new CollectableFieldFormatException(CommonLocaleDelegate.getMessage("CollectableFieldFormat.4","Invalid date: {0}", sText));
					}

					sDateWithoutToday = sDateWithoutToday.substring(0, sDateWithoutToday.length()-1).trim();
				}

				if (StringUtils.looksEmpty(sDateWithoutToday)) {
					return result;
				}

				// get quantity
				try {
					Integer iQuantity = Integer.parseInt(sDateWithoutToday);
					((ExtendedRelativeDate)result).setQuantity(iQuantity);
				}
				catch (NumberFormatException e) {
					throw new CollectableFieldFormatException(CommonLocaleDelegate.getMessage("CollectableFieldFormat.5","Invalid date: {0}", sText), e);
				}
			}
			else {
				try {
					ParseException pe = null;
					if (sInputFormat != null) {
						try {
							result = tryParse(sInputFormat, sText);
						}
						catch (ParseException ex) {
							pe = ex;
						}
					}
					if (result == null) {
						try {
							result = tryParse(((SimpleDateFormat)CommonLocaleDelegate.getDateFormat()).toPattern(), sText);
						}
						catch (ParseException ex) {
							throw (pe != null) ? pe : ex;
						}
					}
				}
				catch (ParseException ex) {
						throw new CollectableFieldFormatException(CommonLocaleDelegate.getMessage("CollectableFieldFormat.6","Invalid date: {0}", sText), ex);
				}
			}
			return result;
		}

		/**
		 * try different parsing alternatives
		 * @param format the outputformat
		 * @param input the text to parse
		 * @return
		 * @throws ParseException
		 */
		private Date tryParse(String format, String input) throws ParseException {
			ParseException pe = null;
			try {
				if (format.indexOf("yyyy") > 0) {
					return tryParseWith2DigitYear(format, input);
				}
			}
			catch (ParseException ex) {
				pe = ex;
			}
			try {
				return new SimpleDateFormat(format).parse(input);
			}
			catch (ParseException ex) {
				pe = ex;
			}
			try {
				return tryParseWithoutSymbols(format, input);
			}
			catch (ParseException ex) {
				pe = ex;
			}
			throw pe;
		}

		private Date tryParseWith2DigitYear(String format, String input) throws ParseException {
			String alternate = format.replaceAll("yyyy", "yy");
			return new SimpleDateFormat(alternate).parse(input);
		}

		private Date tryParseWithoutSymbols(String format, String input) throws ParseException {
			String alternate = format.replaceAll("[^a-zA-Z]", "");
			try {
				if (format.indexOf("yyyy") > 0) {
					return tryParseWith2DigitYear(alternate, input);
				}
			}
			catch (ParseException ex) { }
			return new SimpleDateFormat(alternate).parse(input);
		}
	}	// class CollectableDateFormat

	/**
	 * Format and parse instances of {@link InternalTimestamp}.
	 * Default fall-back behaviour (parsing) is inherited by {@link CollectableDateFormat}.
	 *
	 * @author thomas.schiffmann
	 */
	private static class CollectableTimestampFormat extends CollectableDateFormat {

		private boolean formatWithTime;

		public boolean isFormatWithTime() {
			return formatWithTime;
		}

		public void setFormatWithTime(boolean formatWithTime) {
			this.formatWithTime = formatWithTime;
		}

		@Override
		public String format(String sOutputFormat, Object oValue) {
			if (oValue == null) {
				return null;
			}
			if (!(oValue instanceof InternalTimestamp) && !(oValue instanceof Date)) {
				throw new NuclosFatalException("Kein g\u00fcltiger Zeitstempel: " + oValue + " (" + oValue.getClass() + ").");
			}
			if (isFormatWithTime()) {
				return CommonLocaleDelegate.getDateTimeFormat().format(oValue);
			}
			else {
				return super.format(sOutputFormat, oValue);
			}
		}

		@Override
		public InternalTimestamp parse(String sInputFormat, String sText) throws CollectableFieldFormatException {
			if (StringUtils.looksEmpty(sText)) {
				return null;
			}
			try {
				return new InternalTimestamp(CommonLocaleDelegate.getDateTimeFormat().parse(sText).getTime());
			}
			catch (ParseException ex) {
				return new InternalTimestamp(super.parse(sInputFormat, sText).getTime());
			}
		}
	}	// class CollectableTimestampFormat

	private static class CollectableIntegerFormat extends CollectableFieldFormat {

		@Override
		public String format(String sOutputFormat, Object oValue) {
			if (oValue == null) {
				return null;
			}
			NumberFormat nf;
			if (sOutputFormat == null) {
				nf = CommonLocaleDelegate.getIntegerFormat();
			}
			else {
				nf = new DecimalFormat(sOutputFormat);
			}
			return nf.format(oValue);
		}

		@Override
		public Integer parse(String sInputFormat, String sText) throws CollectableFieldFormatException {
			if (StringUtils.looksEmpty(sText)) {
				return null;
			}
			try {
				// try input format first
				ParseException pe = null;
				Number result = null;
				if (sInputFormat != null) {
					try {
						result = new DecimalFormat(sInputFormat).parse(sText);
					}
					catch (ParseException ex) {
						pe = ex;
					}
				}
				if (result == null) {
					try {
						result = NumberFormat.getNumberInstance().parse(sText);
					}
					catch (ParseException ex) {
						throw (pe != null) ? pe : ex;
					}
				}

				if (new BigInteger(result.toString()).compareTo(new BigInteger(new Integer(Integer.MAX_VALUE).toString())) > 0) {
					throw new CollectableFieldFormatException(CommonLocaleDelegate.getMessage("CollectableFieldFormat.13","Number too big: {0}", sText));
				}
				return result.intValue();
			}
			catch (ParseException ex) {
				throw new CollectableFieldFormatException(CommonLocaleDelegate.getMessage("CollectableFieldFormat.11","Invalid integer number: {0}", sText), ex);
			} catch (NumberFormatException ex) {
				throw new CollectableFieldFormatException(CommonLocaleDelegate.getMessage("CollectableFieldFormat.12","Invalid integer number: {0}", sText), ex);
			}

		}
	}	// class CollectableIntegerFormat

	private static class CollectableDoubleFormat extends CollectableFieldFormat {

		@Override
		public String format(String sOutputFormat, Object oValue) {
			if (oValue == null) {
				return null;
			}
			final NumberFormat nf;
			if (sOutputFormat == null) {
				nf = NumberFormat.getNumberInstance();
				nf.setMaximumFractionDigits(100);
				nf.setGroupingUsed(false);
			}
			else {
				nf = new DecimalFormat(sOutputFormat);
			}
			return nf.format(oValue);
		}

		@Override
		public Double parse(String sInputFormat, String sText) throws CollectableFieldFormatException {
			if (StringUtils.looksEmpty(sText)) {
				return null;
			}

			try {
				// try input format first
				ParseException pe = null;
				if (sInputFormat != null) {
					try {
						return new DecimalFormat(sInputFormat).parse(sText).doubleValue();
					}
					catch (ParseException ex) {
						pe = ex;
					}
				}
				try {
					return NumberFormat.getNumberInstance().parse(sText).doubleValue();
				}
				catch (ParseException ex) {
					throw (pe != null) ? pe : ex;
				}
			}
			catch (ParseException ex) {
				throw new CollectableFieldFormatException(CommonLocaleDelegate.getMessage("CollectableFieldFormat.8","Invalid decimal number: {0}", sText), ex);
			}
		}
	}	// class CollectableDoubleFormat

	private static class CollectableBooleanFormat extends CollectableFieldFormat {

		private final String labelYes;
		private final String labelNo;

		public CollectableBooleanFormat() {
			this.labelYes = CommonLocaleDelegate.getTextFallback("CollectableBooleanFormat.yes", "yes") ;
			this.labelNo = CommonLocaleDelegate.getTextFallback("CollectableBooleanFormat.no", "no") ;
		}

		@Override
		public String format(String sOutputFormat, Object oValue) {
			if (oValue == null) {
				return null;
			}
			else if (oValue.equals(Boolean.TRUE)) {
				return labelYes;
			}
			else {
				return labelNo;
			}
		}

		@Override
		public Boolean parse(String sInputFormat, String sText) throws CollectableFieldFormatException {
			if (sText == null) {
				return null;
			}
			else if (sText.equalsIgnoreCase(labelYes) || sText.equalsIgnoreCase("Ja") || sText.equals("true")) {
				return Boolean.TRUE;
			}
			else if (sText.equalsIgnoreCase(labelNo) || sText.equalsIgnoreCase("Nein") || sText.equals("false")) {
				return Boolean.FALSE;
			}
			else {
				throw new CollectableFieldFormatException(
					CommonLocaleDelegate.getMessage("CollectableFieldFormat.2","Invalid boolean: {0} (expected \"{1}\" or \"{2}\").", sText, labelYes, labelNo));
			}
		}
	}	// class CollectableBooleanFormat

	private static class CollectableBigDecimalFormat extends CollectableFieldFormat {
		@Override
		public String format(String sOutputFormat, Object oValue) {
			if (oValue == null) {
				return null;
			}
			final NumberFormat numberformat;
			if (sOutputFormat == null) {
				numberformat = NumberFormat.getNumberInstance();
				numberformat.setGroupingUsed(false);
			}
			else {
				numberformat = new DecimalFormat(sOutputFormat);
			}
			return numberformat.format(oValue);
		}

		@Override
		public BigDecimal parse(String sInputFormat, String sText) throws CollectableFieldFormatException {
			if (StringUtils.looksEmpty(sText)) {
				return null;
			}

			try {
				final DecimalFormat numberformat;
				if (sInputFormat == null) {
					// getNumberInstance returns DecimalFormat
					numberformat = (DecimalFormat) NumberFormat.getNumberInstance();
				}
				else {
					numberformat = new DecimalFormat(sInputFormat);
				}
				numberformat.setParseBigDecimal(true);
				return (BigDecimal)numberformat.parse(sText);
			}
			catch (ParseException ex) {
				throw new CollectableFieldFormatException(CommonLocaleDelegate.getMessage("CollectableFieldFormat.10","Invalid decimal number: {0}", sText), ex);
			}
		}
	}	// class CollectableBigDecimalFormat

	private static class DefaultFormat extends CollectableFieldFormat {
		@Override
		public String format(String sOutputFormat, Object oValue) {
			return StringUtils.emptyIfNull(LangUtils.toString(oValue));
		}

		@Override
		public Object parse(String sInputFormat, String sText) throws CollectableFieldFormatException {
			throw new UnsupportedOperationException(CommonLocaleDelegate.getMessage("CollectableFieldFormat.1","DefaultFormat.parse not implemented."));
		}
	}
}	// class CollectableFieldFormat
