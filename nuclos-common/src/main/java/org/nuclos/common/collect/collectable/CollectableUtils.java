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
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosDateTime;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosImage;
import org.nuclos.common.NuclosPassword;
import org.nuclos.common.collect.exception.CollectableFieldValidationException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.DateUtils;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.RelativeDate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.PreferencesException;



/**
 * Helper class for <code>CollectableField</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class CollectableUtils {

	private static final Logger log = Logger.getLogger(CollectableUtils.class);

	private static final String PREFS_KEY_FIELDTYPE = "fieldType";
	private static final String PREFS_KEY_VALUE = "value";
	private static final String PREFS_KEY_VALUEID = "valueId";

	private CollectableUtils() {
	}

	public static void validate(CollectableField clctf, CollectableEntityField clctef) throws CollectableFieldValidationException {
		validateNull(clctf, clctef);
		validateFieldType(clctf, clctef);
		validateValueClass(clctf, clctef);
		validateFieldDimensions(clctf, clctef);
	}

	/**
	 * helper method which checks the size of the field content vs the size allowed on database
	 * @param clctf
	 * @param clctef
	 */
	public static void validateFieldDimensions(CollectableField clctf, CollectableEntityField clctef) throws CollectableFieldValidationException {
		final Object oValue = clctf.getValue();
		if (oValue != null && clctef != null && clctef.getMaxLength() != null) {
			int maxlength = clctef.getMaxLength();

			if (oValue instanceof Integer) {
				if (((Integer) oValue).toString().length() > maxlength)
					throw new CollectableFieldValidationException(SpringLocaleDelegate.getInstance().getMessage(
							"CollectableUtils.6","Der Wert \"{0}\" ist zu gross f\u00fcr das Feld \"{1}\"", oValue, clctef.getLabel()));
			} else if (oValue instanceof String) {
				if (((String) oValue).length() > maxlength)
					throw new CollectableFieldValidationException(SpringLocaleDelegate.getInstance().getMessage(
							"CollectableUtils.7","Der Wert \"{0}\" ist zu gross f\u00fcr das Feld \"{1}\"", oValue, clctef.getLabel()));
			} else if (oValue instanceof Double){

				BigDecimal bd = BigDecimal.valueOf((Double) oValue);
				int digitsBeforeSep = bd.precision() - bd.scale();
				if (digitsBeforeSep > maxlength - clctef.getPrecision())
					throw new CollectableFieldValidationException(SpringLocaleDelegate.getInstance().getMessage(
							"CollectableUtils.8","Der Wert \"{0}\" ist zu gross f\u00fcr das Feld \"{1}\"", oValue, clctef.getLabel()));
			}
		}
	}

	/**
	 * validates the field type.
	 * @param clctf
	 * @param clctef
	 * @throws CollectableFieldValidationException if the field type of field differs from that of the entity field.
	 */
	public static void validateFieldType(CollectableField clctf, CollectableEntityField clctef)
			throws CollectableFieldValidationException {
		if (clctf.getFieldType() != clctef.getFieldType() && !NuclosEOField.isEOFieldWithForceValueSearch(clctef.getName())) {
			String msg;
			if(clctef.getFieldType() == CollectableEntityField.TYPE_VALUEFIELD)
				msg = SpringLocaleDelegate.getInstance().getMessage(
						"CollectableUtils.2","Das Feld \"{0}\" darf keine Id enthalten.", clctef.getLabel());
			else
				msg = SpringLocaleDelegate.getInstance().getMessage(
						"CollectableUtils.4","Das Feld \"{0}\" muss eine Id enthalten.", clctef.getLabel());
			throw new CollectableFieldValidationException(msg);
		}
	}

	/**
	 * validates null / nullabiliby.
	 * @param clctf
	 * @param clctef
	 * @throws CollectableFieldValidationException if <code>clctf</code> is null, but <code>clctef</code> is not nullable.
	 */
	public static void validateNull(CollectableField clctf, CollectableEntityField clctef)
			throws CollectableFieldValidationException {
		// check null/nullable:
		if (clctf.isNull() && !clctef.isNullable()) {
			String msg = SpringLocaleDelegate.getInstance().getMessage(
					"CollectableUtils.3","Das Feld \"{0}\" darf nicht leer sein.", clctef.getLabel());
			throw new CollectableFieldValidationException(msg);
		}
	}

	/**
	 * validates the value's class.
	 *
	 * @param clctf
	 * @param clctef
	 * @throws CollectableFieldValidationException
	 */
	public static void validateValueClass(CollectableField clctf, CollectableEntityField clctef) throws CollectableFieldValidationException {
		// check value/class:
		final Object oValue = clctf.getValue();
		if (oValue != null) {
			final Class<?> clsValue = oValue.getClass();
			final Class<?> clsEntity = clctef.getJavaClass();
			if(InternalTimestamp.class.equals(clsEntity) && Date.class.isAssignableFrom(clsValue)) {
				// it is okay!
				return;
			}
			if(clctef.isIdField() && clctf.isIdField()) {
				// it is okay!
				return;
			}
			//NUCLEUSINT-1142
			if (!clsEntity.isAssignableFrom(clsValue) && !NuclosPassword.class.equals(clsEntity)) {
				String msg = SpringLocaleDelegate.getInstance().getMessage(
						"CollectableUtils.5","Der Wert \"{0}\" hat nicht den f\u00fcr das Feld \"{1}\" vorgeschriebenen Datentyp {2}, sondern den Datentyp {3}.", oValue, clctef.getLabel(), clsEntity.getName(), clsValue.getName());
				throw new CollectableFieldValidationException(msg);
			}
		}

	}

	public static void putCollectableField(Preferences prefs, String sNodeName, CollectableField clctf) throws PreferencesException {
		final Preferences prefsChild = prefs.node(sNodeName);
		final int iFieldType = clctf.getFieldType();
		prefsChild.putInt(PREFS_KEY_FIELDTYPE, iFieldType);
		PreferencesUtils.putSerializable(prefs, PREFS_KEY_VALUE, clctf.getValue());
		if (iFieldType == CollectableField.TYPE_VALUEIDFIELD) {
			PreferencesUtils.putSerializable(prefs, PREFS_KEY_VALUEID, clctf.getValueId());
		}
	}

	/**
	 * @param prefs
	 * @param sNodeName
	 * @return the <code>CollectableField</code> stored in <code>prefs.node(sNodeName)</code>, if any.
	 * @throws PreferencesException
	 */
	public static CollectableField getCollectableField(Preferences prefs, String sNodeName) throws PreferencesException {
		final CollectableField result;
		if (!PreferencesUtils.nodeExists(prefs, sNodeName)) {
			result = null;
		}
		else {
			final Preferences prefsChild = prefs.node(sNodeName);
			final int iFieldType = prefsChild.getInt(PREFS_KEY_FIELDTYPE, CollectableField.TYPE_UNDEFINED);
			switch (iFieldType) {
				case CollectableField.TYPE_UNDEFINED: {
					result = null;
					break;
				}
				case CollectableField.TYPE_VALUEFIELD: {
					final Object oValue = PreferencesUtils.getSerializable(prefs, PREFS_KEY_VALUE);
					result = new CollectableValueField(oValue);
					break;
				}
				case CollectableField.TYPE_VALUEIDFIELD: {
					final Object oValue = PreferencesUtils.getSerializable(prefs, PREFS_KEY_VALUE);
					final Object oValueId = PreferencesUtils.getSerializable(prefs, PREFS_KEY_VALUEID);
					result = new CollectableValueIdField(oValueId, oValue);
					break;
				}
				default:
					throw new CommonFatalException(SpringLocaleDelegate.getInstance().getMessage(
							"CollectableUtils.11","Unbekannter Feldtyp: {0}", iFieldType));
			}
		}
		return result;
	}

	/**
	 * @param clcte
	 * @return List<CollectableEntityField>. Contains all of the fields contained in <code>clcte</code>.
	 */
	public static List<CollectableEntityField> getCollectableEntityFields(CollectableEntity clcte) {
		return getCollectableEntityFieldsFromFieldNames(clcte, clcte.getFieldNames());
	}

	/**
	 * @param clcte
	 * @param collFieldNames Collection<String>
	 * @return List<CollectableEntityField> the <code>CollectableEntityField</code>s from the given <code>CollectableEntity</code> and the given collection of field names.
	 */
	public static List<CollectableEntityField> getCollectableEntityFieldsFromFieldNames(CollectableEntity clcte, Collection<String> collFieldNames) {
		return CollectionUtils.transform(collFieldNames, new CollectableEntity.GetEntityField(clcte));
	}

	/**
	 * @param collclctef Collection<CollectableEntityField>
	 * @return List<String> the field names from the <code>CollectableEntityField</code>s.
	 *
	 * @deprecated Not really usefull. Use transform directly.
	 */
	public static List<String> getFieldNamesFromCollectableEntityFields(Collection<? extends CollectableEntityField> collclctef) {
		return CollectionUtils.transform(collclctef, new CollectableEntityField.GetName());
	}

	/**
	 * creates a copy of the given CollectableField.
	 * The result's field type (value/valueId) is the same as the given entity field's type.
	 * <ul>
	 *   <li>If the field types of the target and the source match, a direct copy is returned.</li>
	 *   <li>If the source is a value id field, the target is a value field, only the value component is copied.</li>
	 *   <li>If the source is a value field, the target is a value id field:  If {@value allowSettingTargetIdToNull} is true,
	 *     only the value component is copied and the id component of the target is set to null.
	 *     If {@value allowSettingTargetIdToNull} is false, this method returns null.
	 * </ul>
	 * @param clctfSource the source collectable
	 * @param clctefTarget the field type of the result.
	 * @param allowSettingTargetIdToNull See above. Be careful when setting this to true.
	 */
	public static CollectableField copyCollectableField(CollectableField clctfSource, CollectableEntityField clctefTarget,
			boolean allowSettingTargetIdToNull) {
		final Object oValue = clctfSource.getValue();
		final CollectableField result;
		if (clctefTarget.isIdField()) {
			if (clctfSource.isIdField()) {
				// As CollectableFields are immutable, we may just return the source object here:
				result = clctfSource;
			}
			else {
				if (allowSettingTargetIdToNull) {
					result = new CollectableValueIdField(null, oValue);
				} else {
					result = null;
				}
			}
		}
		else {
			result = new CollectableValueField(oValue);
		}
		return result;
	}

	/**
	 * @param iFieldType
	 * @return a null field for the given field type
	 * @postcondition result != null
	 * @postcondition result.isNull()
	 * @postcondition result.getFieldType() == iFieldType
	 */
	public static CollectableField getNullField(int iFieldType) {
		final CollectableField result;
		switch (iFieldType) {
			case CollectableEntityField.TYPE_VALUEFIELD:
				result = CollectableValueField.NULL;
				break;
			case CollectableEntityField.TYPE_VALUEIDFIELD:
				result = CollectableValueIdField.NULL;
				break;
			default:
				throw new IllegalStateException(SpringLocaleDelegate.getInstance().getMessage(
						"CollectableUtils.9","Invalid fieldtype: {0}", iFieldType));
		}
		assert result != null;
		assert result.isNull();
		assert result.getFieldType() == iFieldType;

		return result;
	}

	/**
	 * @param clctef
	 * @return a null field for the given entity field
	 * @postcondition result != null
	 * @postcondition result.isNull()
	 * @postcondition result.getFieldType() == clctef.getFieldType()
	 */
	public static CollectableField getNullField(CollectableEntityField clctef) {
		final CollectableField result = getNullField(clctef.getFieldType());
		assert result != null;
		assert result.isNull();
		assert result.getFieldType() == clctef.getFieldType();
		return result;
	}

	/**
	 * @param clctef
	 * @param oValue
	 * @return a new CollectableField containing the given value.
	 * @postcondition result != null
	 * @postcondition result.isIdField() <--> clctef.isIdField()
	 * @postcondition result.getValue() == oValue
	 * @postcondition clctef.isIdField() --> result.getValueId() == null
	 */
	public static CollectableField newCollectableFieldForValue(CollectableEntityField clctef, Object oValue) {
		final CollectableField result = clctef.isIdField() ? (CollectableField) new CollectableValueIdField(null, oValue) : new CollectableValueField(oValue);

		assert result != null;
		assert result.isIdField() == clctef.isIdField();
		assert result.getValue() == oValue;
		assert !clctef.isIdField() || result.getValueId() == null;

		return result;
	}

	public static CollectableField newCollectableValueIdField(CollectableEntityField clctef, Integer valueId, Object value) {
		if (!clctef.isIdField()) {
			throw new CommonFatalException("Kein ValueId Feld");
		}

		final CollectableField result = new CollectableValueIdField(valueId, value);

		assert result != null;
		assert result.isIdField() == clctef.isIdField();
		assert result.getValueId() == valueId;

		return result;
	}

	public static CollectableField newCollectableValueIdFieldForValueId(CollectableEntityField clctef, Integer valueId) {
		return newCollectableValueIdField(clctef, valueId, null);
	}


	/**
	 * sets all fields in the given <code>Collectable</code> to their respective default values (according to the given <code>CollectableEntity</code>).
	 * @param clct the <code>Collectable</code> to be changed.
	 * @param clcte the <code>CollectableEntity</code> containing the required information about the fields in <code>clct</code>.
	 * @precondition clct != null
	 * @precondition clct.isComplete()
	 * @precondition clcte != null
	 */
	public static void setDefaultValues(Collectable clct, CollectableEntity clcte) {
		for (String sFieldName : clcte.getFieldNames()) {
			if (clcte.getEntityField(sFieldName).getDefault().getValue() != null &&
				clcte.getEntityField(sFieldName).getDefault().getValue().toString().equals(RelativeDate.today().toString())) {
				clct.setField(sFieldName, new CollectableValueField(DateUtils.today()));
			}
			else if (clcte.getEntityField(sFieldName).getJavaClass() == Boolean.class &&
				clcte.getEntityField(sFieldName).getDefault().getValue() == null) {
				//null default values for boolean fields
			}
			else {
				clct.setField(sFieldName, clcte.getEntityField(sFieldName).getDefault());
			}
		}
	}

	/**
	 * @param collclct
	 * @param sFieldName
	 * @return the common value (if any) of the field with the given name, in all Collectables in the given collection.
	 *         <code>null</code> if there is no common value.
	 * @precondition collclct != null
	 * @precondition sFieldName != null
	 */
	public static CollectableField getCommonValue(Collection<? extends Collectable> collclct, String sFieldName) {
		CollectableField result = null;
		for (Collectable clct : collclct) {
			final CollectableField clctf = clct.getField(sFieldName);
			if (result == null) {
				result = clctf;
			}
			else {
				if (!result.equals(clctf)) {
					result = null;
					break;
				}
			}
		}
		return result;
	}

	public static int getCollectableComponentTypeForClass(Class<?> clazz) {
		if (clazz.equals(Boolean.class)) {
			return CollectableComponentTypes.TYPE_CHECKBOX;
		} else if (clazz.equals(Date.class) || clazz.equals(InternalTimestamp.class) || clazz.equals(NuclosDateTime.class)) {
			return CollectableComponentTypes.TYPE_DATECHOOSER;
		} else if (org.nuclos.common2.File.class.isAssignableFrom(clazz)) {
			return CollectableComponentTypes.TYPE_FILECHOOSER;
		} else if(clazz.equals(NuclosImage.class)) {
			return CollectableComponentTypes.TYPE_IMAGE;
		} else if(clazz.equals(NuclosPassword.class)) {
			//NUCLEUSINT-1142
			return CollectableComponentTypes.TYPE_PASSWORDFIELD;
		} else {
			return CollectableComponentTypes.TYPE_TEXTFIELD;
		}
	}

	/**
	 * @param lstclctfValues
	 * @param clctfSource
	 * @return
	 * @throws NoSuchElementException
	 * @postcondition result != null
	 */
	public static CollectableField findCollectableFieldByValue(List<CollectableField> lstclctfValues, CollectableField clctfSource) {
		CollectableField result = null;
		for (CollectableField clctfValue : lstclctfValues) {
			if (clctfSource.getValue().equals(clctfValue.getValue())) {
				result = clctfValue;
				break;
			}
		}
		if (result == null) {
			throw new NoSuchElementException(clctfSource.toString());
		}
		assert result != null;
		return result;
	}

	public static String formatFieldExpression(String expr, final Collectable clct) {
		if (expr == null || expr.isEmpty()) {
			try {
				CollectableField field = clct.getField("name");
				if (field != null) {
					return field.toString();
				}
			} catch (Exception ex) {
			}
			return clct.getIdentifierLabel();
		}
		final Transformer<String, String> paramTransformer = new Transformer<String, String>() {
			@Override public String transform(String param) {
				String value = null;
				try {
					CollectableField field = clct.getField(param);
					if (field != null) {
						value = field.toString();
					}
				} catch (Exception ex) {
				}
				if (value == null) {
					value = "${" + param + "}";
				}
				return value;
			}
		};
		if (!expr.contains("${")) {
			return paramTransformer.transform(expr);
		} else {
			return StringUtils.replaceParameters(expr,paramTransformer);
		}
	}

	/**
	 * Comparator for sorting collectable fields along a given string list
	 */
	public static class GivenFieldOrderComparator implements Comparator<CollectableEntityField> {
		private final List<String> lstFieldNames;

		public GivenFieldOrderComparator(List<String> lstFieldNames) {
			this.lstFieldNames = lstFieldNames;
		}

		@Override
        public int compare(CollectableEntityField clctef1, CollectableEntityField clctef2) {
			final Integer pos1 = (lstFieldNames.indexOf(clctef1.getName()) == -1) ? Integer.MAX_VALUE : lstFieldNames.indexOf(clctef1.getName());
			final Integer pos2 = (lstFieldNames.indexOf(clctef2.getName()) == -1) ? Integer.MAX_VALUE : lstFieldNames.indexOf(clctef2.getName());
			return pos1.compareTo(pos2);
		}
	}

}	// class CollectableUtils
