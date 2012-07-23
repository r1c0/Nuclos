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
package org.nuclos.common.attribute;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.nuclos.common.AttributeProvider;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.attribute.BadAttributeValueException;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.genericobject.valueobject.CanonicalAttributeFormat;



/**
 * Value object representing a dynamic leased object attribute.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 01.00.000
 */
public class DynamicAttributeVO implements Serializable, Cloneable {

	/**
	 * @deprecated As table t_go_attribue does not exist any more, this is always unset (null). 
	 */
	private int iId;
	
	private boolean bRemoved = false;
	private int iAttributeId;
	private int iValueId;
	private Object oValue;

	/**
	 * constructor to be called by server only
	 * @param iId primary key of underlying database record
	 * @param iAttributeId attribute id of underlying database record
	 * @param iValueId value id of underlying database record
	 * @param sCanonicalValue value of underlying database record
	 */
	public DynamicAttributeVO(Integer iId, Integer iAttributeId, Integer iValueId, String sCanonicalValue) throws CommonValidationException {
		this(iId, iAttributeId, iValueId, sCanonicalValue, 
				SpringApplicationContextHolder.getBean(AttributeProvider.class));
	}

	/**
	 * constructor to be called by server only
	 * @param iId primary key of underlying database record
	 * @param iAttributeId attribute id of underlying database record
	 * @param iValueId value id of underlying database record
	 * @param sCanonicalValue value of underlying database record
	 * @param attrprovider provides the attribute for the given attribute id
	 */
	public DynamicAttributeVO(Integer iId, Integer iAttributeId, Integer iValueId, String sCanonicalValue,
			AttributeProvider attrprovider) throws CommonValidationException {
		this.iId = LangUtils.zeroIfNullStrict(iId);
		this.iAttributeId = LangUtils.zeroIfNullStrict(iAttributeId);
		this.iValueId = LangUtils.zeroIfNullStrict(iValueId);
		setCanonicalValue(sCanonicalValue, attrprovider);
	}

	/**
	 * constructor to be called by client only
	 * @param iAttributeId attribute id of underlying database record
	 * @param iValueId value id of underlying database record
	 * @param oValue value of underlying database record
	 */
	public DynamicAttributeVO(Integer iAttributeId, Integer iValueId, Object oValue) {
		super();
		this.iAttributeId = LangUtils.zeroIfNullStrict(iAttributeId);
		this.iValueId = LangUtils.zeroIfNullStrict(iValueId);
		this.oValue = oValue;
		/** @todo check oValue? */
	}

	/**
	 * @return a clone from this
	 */
	@Override
	public final DynamicAttributeVO clone() {
		try {
			return (DynamicAttributeVO) super.clone();
			// Note that oValue should be immutable.
		}
		catch (CloneNotSupportedException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @todo make this a regular ctor again
	 * @param iAttributeId
	 * @param iValueId
	 * @param sCanonicalValue
	 * @param attrprovider provides the attribute for the given attribute id
	 * @return
	 * @throws CommonValidationException
	 */
	public static DynamicAttributeVO createGenericObjectAttributeVOCanonical(Integer iAttributeId, Integer iValueId,
			String sCanonicalValue, AttributeProvider attrprovider) throws CommonValidationException {
		final AttributeCVO attrcvo = attrprovider.getAttribute(iAttributeId);
		try {
			return new DynamicAttributeVO(iAttributeId, iValueId, getCanonicalFormat(attrcvo).parse(sCanonicalValue));
		}
		catch (CommonValidationException ex) {
			throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("dynamicattrvo.invalid.field.value.1", getDisplayableFieldName(attrcvo), ex.getMessage()), ex);
				//"Fehlerhafter Wert im Feld " + getDisplayableFieldName(attrcvo) + ": " + ex.getMessage(), ex);
		}
	}

	/**
	 * @return attribute id of underlying database record
	 */
	public Integer getAttributeId() {
		return LangUtils.nullIfZero(iAttributeId);
	}

	/**
	 * @return value id of underlying database record
	 */
	public Integer getValueId() {
		return LangUtils.nullIfZero(iValueId);
	}

	/**
	 * @param iValueId value id of underlying database record
	 */
	public void setValueId(Integer iValueId) {
		this.iValueId = LangUtils.zeroIfNullStrict(iValueId);
	}

	public static CanonicalAttributeFormat getCanonicalFormat(Class<?> cls) {
		return CanonicalAttributeFormat.getInstance(cls);
	}

	/**
	 * @param attrcvo
	 * @return the canonical attribute format for the given attribute.
	 * @precondition attrcvo != null
	 */
	public static CanonicalAttributeFormat getCanonicalFormat(AttributeCVO attrcvo) {
		return getCanonicalFormat(attrcvo.getJavaClass());
	}

	/**
	 * @param attrprovider provides the attribute for <code>this</code>' attribute id
	 * @return the canonical representation of <code>getValue()</code>.
	 */
	public String getCanonicalValue(AttributeProvider attrprovider) {
		return this.getCanonicalValue(attrprovider.getAttribute(getAttributeId()));
	}

	private String getCanonicalValue(AttributeCVO attrcvo) {
		/** @todo check if attrcvo.getId() == this.getAttributeId() */
		return getCanonicalFormat(attrcvo.getJavaClass()).format(oValue);
	}

	/**
	 * sets the value using the given canonical representation.
	 * @param sCanonicalValue
	 * @param attrprovider provides the attribute for <code>this</code>' attribute id
	 * @throws CommonValidationException
	 */
	public void setCanonicalValue(String sCanonicalValue, AttributeProvider attrprovider) throws CommonValidationException {
		final AttributeCVO attrcvo = attrprovider.getAttribute(getAttributeId());
		if (getAttributeId() == null) {
			throw new IllegalStateException("attributeId");
		}
		if (attrcvo == null || !getAttributeId().equals(attrcvo.getId())) {
			throw new IllegalArgumentException("attrcvo");
		}

		this.oValue = getCanonicalFormat(attrcvo.getJavaClass()).parse(sCanonicalValue);
	}

	/**
	 * We don't demand the attributecvo here, but we should maybe...
	 * <p>
	 * Attention: This method normally does not do what you expect as it needs very special
	 * formatted values (e.g. 'ja' and 'nein' String for Boolean, special formatted String for 
	 * Date etc.). I recommend to use {@link #setParsedValue}. (tp)
	 * </p>
	 * @todo adjust comment
	 * @param oValue
	 */
	public void setValue(Object oValue) {
		this.oValue = oValue;
	}
	
	/**
	 * This is a more save alternative to {@link #setValue(Object)} as it honours the strange 
	 * conversion requirements of {@link DynamicAttributeVO}.
	 * 
	 * @see #setValue(Object)
	 * @param o - value to set
	 * @param prov - attribute provider to use
	 * @throws CommonValidationException
	 */
	public void setParsedValue(Object o, AttributeProvider prov) throws CommonValidationException {
		final AttributeCVO attrcvo = prov.getAttribute(getAttributeId());
		if (getAttributeId() == null) {
			throw new IllegalStateException("attributeId");
		}
		if (attrcvo == null || !getAttributeId().equals(attrcvo.getId())) {
			throw new IllegalArgumentException("attrcvo");
		}
		final CanonicalAttributeFormat format = getCanonicalFormat(attrcvo.getJavaClass());
		String formatted = null;
		try {
			formatted = format.format(o);
		}
		catch (ClassCastException e) {
			// We must catch here as there are defects in s = format.parse(format.format(s'))
			// I consider this as design flaw. (tp)
			// 
			// Example: StringFormat defines:
			// public String format(Object oValue) {
			// 	return (String) oValue;
			// }
			// But is seems to get Integer as input ...

			if (o == null) {
				formatted = null;
			}
			else {
				formatted = String.valueOf(o);
			}
		}
		this.oValue = format.parse(formatted);
	}

	public Object getValue() {
		return this.oValue;
	}

	/**
	 * is underlying database record to be removed from database?
	 * @return boolean value
	 */
	public boolean isRemoved() {
		return this.bRemoved;
	}

	/**
	 * Mark underlying database record as to be removed from database.
	 * Set content to empty to prevent saving of removed attributes.
	 */
	public void remove() {
		iValueId = 0;
		oValue = null;
		this.bRemoved = true;
	}

	/**
	 * reverse operation of remove.
	 */
	public void unremove() {
		this.bRemoved = false;
	}

	/**
	 * get primary key (intid) of underlying database record
	 * @return primary key of underlying database record
	 * 
	 * @deprecated As table t_go_attribue does not exist any more, this is always unset (null). 
	 */
	public Integer getId() {
		return LangUtils.nullIfZero(iId);
	}

	/**
	 * validates <code>this</code>.
	 * @param iGenericObjectId
	 * @param attrcvo
	 * @throws BadAttributeValueException if <code>this</code> cannot be validated.
	 * @precondition attrcvo != null
	 * @deprecated Validation is performed by org.nuclos.server.validation.ValidationSupport.
	 */
	public void validate(Integer iGenericObjectId, AttributeCVO attrcvo) throws BadAttributeValueException {
		// does nothing
	}

	/**
	 * @param attrcvo
	 * @return the label + the name of attrcvo, for display in an error message.
	 * @precondition attrcvo != null
	 */
	private static String getDisplayableFieldName(AttributeCVO attrcvo) {
		//"\"" + attrcvo.getLabel() + "\" (Attributname: \"" + attrcvo.getName() + "\")";
		return StringUtils.getParameterizedExceptionMessage("dynamicattrvo.invalid.field.value.7", attrcvo.getLabel(), attrcvo.getName());
		//SpringLocaleDelegate.getMessage("DynamicAttributeVO.1","\"{0}\" (Attributname: \"{1}\")", CommonLocaleDelegate.getLabelFromAttributeCVO(attrcvo), attrcvo.getName());
	}

	/**
	 * validates <code>this</code>.
	 * @param attrprovider
	 * @throws CommonValidationException if <code>this</code> cannot be validated.
	 * @deprecated Validation is performed by org.nuclos.server.validation.ValidationSupport.
	 */
	public void validate(Integer iGenericObjectId, AttributeProvider attrprovider) throws CommonValidationException {
		this.validate(iGenericObjectId, attrprovider.getAttribute(getAttributeId()));
	}

	/**
	 * "implementation" of Serializable.
	 * As of JDK 1.4, deserialized Strings use more memory than necessary.
	 * Thus, we copy the String here to minimize memory usage.
	 * In JDK 1.5, this probably won't be needed anymore.
	 * @todo check if this is still necessary!
	 * @param ois
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		if (oValue != null && oValue instanceof String) {
			// Note that the String is copied deliberately here:
			oValue = new String((String) oValue);
		}
	}
	
	@Override
	public String toString() {
		final StringBuffer result = new StringBuffer();
		result.append("DaVO[aId=");
		result.append(iAttributeId);
		if (oValue != null) {
			result.append(",value=").append(oValue);
		}
		if (iValueId != 0) {
			result.append(",vId=").append(iValueId);
		}
		if (bRemoved) {
			result.append(",removed=").append(bRemoved);
		}
		result.append("]");
		return result.toString();
	}

}	// class DynamicAttributeVO
