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
package org.nuclos.server.genericobject.valueobject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.nuclos.common2.DateTime;
import org.nuclos.common2.DateUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.RelativeDate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.NuclosImage;
import org.nuclos.common.NuclosPassword;

/**
 * Defines formatting and parsing of (leased object) attributes.
 * @todo check formats!
 * @todo all formats must be locale independent!
 * @todo add comments about thread safety!
 * @thread-safe
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public abstract class CanonicalAttributeFormat {

	private static Map<Class<?>, CanonicalAttributeFormat> mpFormats;

	/**
	 * formats the given value.
	 * @param oValue
	 * @return the object as String
	 */
	public abstract String format(Object oValue);

	/**
	 * parses the given canonical value.
	 * @param sCanonicalValue
	 * @return the parsed object.
	 */
	public abstract Object parse(String sCanonicalValue) throws CommonValidationException;

	/**
	 * Factory method to create an appropriate instance of <code>CollectableFieldFormat</code>
	 * for the given data type.
	 * @param cls
	 * @return <code>CollectableFieldFormat</code> for the given data type
	 */
	public static CanonicalAttributeFormat getInstance(Class<?> cls) {
		CanonicalAttributeFormat result = getFormat(cls);

		// We also allow classes derived from java.util.Date, especially java.sql.Date:
		if (result == null && Date.class.isAssignableFrom(cls)) {
			result = getFormat(Date.class);
		}
		if (result == null) {
			throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("attributecvo.exception.1", cls.getName()));//"Unbekannter Datentyp: " + cls.getName());
		}
		return result;
	}

	private static synchronized Map<Class<?>, CanonicalAttributeFormat> getMapOfFormats() {
		if (mpFormats == null) {
			mpFormats = new HashMap<Class<?>, CanonicalAttributeFormat>(5);
			mpFormats.put(String.class, new StringFormat());
			mpFormats.put(Date.class, new DateFormat());
			mpFormats.put(Integer.class, new IntegerFormat());
			mpFormats.put(Double.class, new DoubleFormat());
			mpFormats.put(Boolean.class, new BooleanFormat());
			mpFormats.put(NuclosImage.class, new ImageFormat());
			mpFormats.put(DateTime.class, new DateTimeFormat());
			mpFormats.put(NuclosPassword.class, new PasswordFormat());
			/** @todo insert BigDecimal - "scale" problem must be solved. */
//			mpFormats.put(BigDecimal.class, new DecimalFormat());
		}
		return mpFormats;
	}

	private static CanonicalAttributeFormat getFormat(Class<?> cls) {
		return getMapOfFormats().get(cls);
	}
	
	/**
	 * @thread-safe
	 */
	private static class ImageFormat extends CanonicalAttributeFormat {
		@Override
		public String format(Object oValue) {
			if(oValue == null)
				return null;
			return oValue.toString();
		}

		@Override
		public Object parse(String sText) {
			return sText;
		}
	}	// class ImageFormat	
	
	/**
	 * @thread-safe
	 */
	private static class PasswordFormat extends CanonicalAttributeFormat {
		@Override
		public String format(Object oValue) {
			if(oValue == null)
				return null;
			return oValue.toString();
		}

		@Override
		public Object parse(String sText) {
			return sText;
		}
	}	// class PasswordFormat	

	/**
	 * @thread-safe
	 */
	private static class StringFormat extends CanonicalAttributeFormat {
		@Override
		public String format(Object oValue) {
			return (String) oValue;
		}

		@Override
		public Object parse(String sText) {
			return sText;
		}
	}	// class StringFormat

	/**
	 * @thread-safe
	 */
	private static class DateFormat extends CanonicalAttributeFormat {

		private static java.text.DateFormat getDateFormat() {
			// Note that a new DateFormat must be created each time as SimpleDateFormat is not thread safe!
			final SimpleDateFormat result = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
			result.setLenient(false);
			return result;
		}

		@Override
		public String format(Object oValue) {
			if (oValue == null) {
				return null;
			}
			//for default value TODAY
			if (oValue.toString().equals(RelativeDate.today().toString())) {
				return getDateFormat().format(DateUtils.today());
			}
			if (!(oValue instanceof java.util.Date)) {
				throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("attribute.format.invalid.date.1", oValue, oValue.getClass()));
					//"Kein g\u00fcltiges Datum: " + oValue + " (" + oValue.getClass() + ").");
			}
			return getDateFormat().format(oValue);
		}

		@Override
		public Object parse(String sText) throws CommonValidationException {
			if(StringUtils.nullIfEmpty(sText) == null) {
				return null;
			}
			try {
				if (sText.equals(RelativeDate.today().toString())) {
					return RelativeDate.today();
				}
				else {
					return getDateFormat().parse(sText);
				}
			}
			catch (ParseException ex) {
				throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("attribute.format.invalid.date.2", sText), ex);
				//"Ung\u00fcltiges Datum: " + sText, ex);
			}
		}
	}	// class DateFormat
	
	/**
	 * @thread-safe
	 */
	private static class DateTimeFormat extends CanonicalAttributeFormat {

		private static java.text.DateFormat getDateFormat() {
			// Note that a new DateFormat must be created each time as SimpleDateFormat is not thread safe!
			final SimpleDateFormat result = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
			result.setLenient(false);
			return result;
		}

		@Override
		public String format(Object oValue) {
			if (oValue == null) {
				return null;
			}
			if (!(oValue instanceof DateTime)) {
				throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("attribute.format.invalid.date.1", oValue, oValue.getClass()));
					//"Kein g\u00fcltiges Datum: " + oValue + " (" + oValue.getClass() + ").");
			}
			return getDateFormat().format(((org.nuclos.common2.DateTime)oValue).getDate());
		}

		@Override
		public Object parse(String sText) throws CommonValidationException {
			if (sText == null) {
				return null;
			}
			try {
				return new DateTime(getDateFormat().parse(sText));
			}
			catch (ParseException ex) {
				throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("attribute.format.invalid.date.2", sText), ex);
				//"Ung\u00fcltiges Datum: " + sText, ex);
			}
		}
	}	// class DateFormat

	/**
	 * @thread-safe
	 */
	private static class IntegerFormat extends CanonicalAttributeFormat {

		@Override
		public String format(Object oValue) {
			return LangUtils.toString(oValue);
		}

		@Override
		public Object parse(String sText) throws CommonValidationException {
			if (StringUtils.isNullOrEmpty(sText)) {
				return null;
			}
			try {
				return new Integer(sText);
			}
			catch (NumberFormatException ex) {
				throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("attribute.format.invalid.integer", sText), ex); 
					//"Ung\u00fcltige Ganze Zahl: " + sText, ex);
			}
		}
	}	// class IntegerFormat

	/**
	 * @thread-safe
	 */
	private static class DoubleFormat extends CanonicalAttributeFormat {

		@Override
		public String format(Object oValue) {
			return LangUtils.toString(oValue);
		}

		@Override
		public Object parse(String sText) throws CommonValidationException {
			if (StringUtils.isNullOrEmpty(sText)) {
				return null;
			}
			try {
				return Double.valueOf(sText);
			}
			catch (NumberFormatException ex) {
				throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("attribute.format.invalid.double", sText), ex); 
					//"Ung\u00fcltige Dezimalzahl: " + sText, ex);
			}
		}
	}	// class DoubleFormat

//	private static class DecimalFormat extends CanonicalAttributeFormat {
//		public String format(Object oValue) {
//			return LangUtils.toString(oValue);
//		}
//
//		public Object parse(String sText) throws CommonValidationException {
//			if (StringUtils.isNullOrEmpty(sText)) {
//				return null;
//			}
//			try {
//				return new BigDecimal(sText);
//			}
//			catch (NumberFormatException ex) {
//				throw new CommonValidationException("Ung\u00fcltige Dezimalzahl: " + sText, ex);
//			}
//		}
//	}  // class DecimalFormat

//	private static class DecimalFormat extends CanonicalAttributeFormat {
//		public String format(Object oValue, Integer iScale, Integer iPrecision) {
//			if (iScale == null) {
//				throw new IllegalArgumentException("iScale");
//			}
//			if (iPrecision == null) {
//				throw new IllegalArgumentException("iPrecision");
//			}
//			if(oValue == null) {
//				return null;
//			}
//			BigDecimal bdValue = (BigDecimal) oValue;
//
//			final String sPattern = getPattern(iScale.intValue(), iPrecision.intValue());
//			java.text.DecimalFormat df = new java.text.DecimalFormat(sPattern, new DecimalFormatSymbols(Locale.US));
//			df.format()
//			bd.toString()
//			return df.format()
//			return LangUtils.toString(oValue);
////			if(oValue == null) {
////				return null;
////			}
////			return nf.format(oValue);
//		}
//
//		private String getPattern(int iScale, int iPrecision) {
//			if(iScale <= 0) {
//				throw new IllegalArgumentException("iScale");
//			}
//			if(iPrecision < 0) {
//				throw new IllegalArgumentException("iPrecision");
//			}
//			if(iScale < iPrecision) {
//				throw new IllegalArgumentException("iScale < iPrecision");
//			}
//			final int iIntegerDigitCount = iScale-iPrecision;
//			assert iIntegerDigitCount >= 0;
//			final String sIntegerDigits = StringUtils.repeat("#", iIntegerDigitCount);
//			final StringBuffer sbResult = new StringBuffer(sIntegerDigits);
//			if(iPrecision > 0) {
//				sbResult.append(".");
//				final String sDecimalDigits = StringUtils.repeat("0", iPrecision);
//				sbResult.append(sDecimalDigits);
//			}
//			// append negative pattern:
//			sbResult.append(";-#");
//			return sbResult.toString();
//		}
//
//		public Object parse(String sText) throws CommonValidationException {
//			if (StringUtils.isNullOrEmpty(sText)) {
//				return null;
//			}
//			try {
//				return new BigDecimal(sText);
//			}
//			catch (NumberFormatException ex) {
//				throw new CommonValidationException("Ung\u00fcltige Dezimalzahl: " + sText, ex);
//			}
//		}
//	}  // class DecimalFormat

	/**
	 * @thread-safe
	 */
	private static class BooleanFormat extends CanonicalAttributeFormat {
		/** @todo change this! */
		@Override
		public String format(Object oValue) {
			final Boolean bValue = (Boolean) oValue;
			if (bValue == null) {
				return null;
			}
			else if (bValue.equals(Boolean.TRUE)) {
				return "ja";
			}
			else {
				return "nein";
			}
		}

		@Override
		public Object parse(String sText) throws CommonValidationException {
			if (sText == null) {
				return null;
			}
			else if (sText.equals("ja")) {
				return Boolean.TRUE;
			}
			else if (sText.equals("nein")) {
				return Boolean.FALSE;
			}
			else {
				throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("attribute.format.invalid.boolean", sText, "ja", "nein")); 
					//"Ung\u00fcltiger Wahrheitswert: " + sText + " (\"ja\" oder \"nein\" erwartet).");
			}
		}
	}	// class BooleanFormat

}	// class CanonicalAttributeFormat
