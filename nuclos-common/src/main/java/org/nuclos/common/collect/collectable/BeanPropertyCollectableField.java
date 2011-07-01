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

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import org.nuclos.common.collect.exception.CollectableFieldValidationException;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * A <code>CollectableField</code> initialized with the property of a JavaBean.
 * Note that a getter named <code>isXxx</code> (rather than <code>getXxx</code>) is allowed for methods returning <code>boolean</code>,
 * but not for those returning <code>java.lang.Boolean</code>.
 * This class is immutable.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public class BeanPropertyCollectableField extends AbstractCollectableField {
	private final Object oValue;
	private final Object oValueId;
	private final int iFieldType;

	/**
	 * @param oBean the wrapped JavaBean
	 * @param sFieldName the field name. The value component is taken from the property with the name sFieldName.
	 * For value id fields, the id component is taken from the property with the name sFieldName, appended by "Id".
	 * @param iFieldType the field type, as in getFieldType()
	 */
	public BeanPropertyCollectableField(Object oBean, String sFieldName, int iFieldType) {
		this.iFieldType = iFieldType;
		this.oValue = this.getProperty(oBean, sFieldName);
		this.oValueId = this.isIdField() ? this.getProperty(oBean, sFieldName + "Id") : null;
	}

	/**
	 * @param oWrapped the wrapped JavaBean
	 * @param sFieldName the field name, which is the same as the property name
	 * @param clcte the entity of the <code>Collectable</code>, used to query the field's type
	 */
	public BeanPropertyCollectableField(Object oWrapped, String sFieldName, CollectableEntity clcte) {
		this(oWrapped, sFieldName, clcte.getEntityField(sFieldName).getFieldType());
	}

	@Override
	public int getFieldType() {
		return this.iFieldType;
	}

	private Object getProperty(Object oBean, String sPropertyName) {
		try {
			return PropertyUtils.getSimpleProperty(oBean, sPropertyName);
			/** @todo return BeanUtils.getSimpleProperty(oBean, sPropertyName); */
		}
		catch (IllegalAccessException ex) {
			throw new CommonFatalException(ex);
		}
		catch (InvocationTargetException ex) {
			throw new CommonFatalException(ex);
		}
		catch (NoSuchMethodException ex) {
			throw new CommonFatalException(ex);
		}
	}

	@Override
	public Object getValue() {
		return this.oValue;
	}

	@Override
	public Object getValueId() {
		if (!this.isIdField()) {
			throw new UnsupportedOperationException("getValueId");
		}
		return this.oValueId;
	}

	@Override
	public void validate(CollectableEntityField clctef) throws CollectableFieldValidationException {
		CollectableUtils.validate(this, clctef);
	}

	@Override
	public String toDescription() {
		final ToStringBuilder b = new ToStringBuilder(this).append(iFieldType).append(oValueId).append(oValue);
		return b.toString();
	}

}	// class BeanPropertyCollectableField
