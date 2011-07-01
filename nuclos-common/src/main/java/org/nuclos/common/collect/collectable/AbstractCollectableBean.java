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
import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;

import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * Abstract (basic) implementation of a <code>Collectable</code> wrapping a JavaBean.
 * The methods that return meta information are delegated to a <code>CollectableEntity</code> object.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public abstract class AbstractCollectableBean<T> extends AbstractCollectable {

	private static final Logger log = Logger.getLogger(AbstractCollectableBean.class);

	private final T tBean;

	/**
	 * @param tBean
	 * @precondition tBean != null
	 */
	protected AbstractCollectableBean(T tBean) {
		if (tBean == null) {
			throw new NullArgumentException("tBean");
		}
		this.tBean = tBean;
	}

	/**
	 * @return the <code>CollectableEntity</code> for this <code>Collectable</code>.
	 */
	protected abstract CollectableEntity getCollectableEntity();

	/**
	 * @return the wrapped JavaBean.
	 * @postcondition result != null
	 */
	protected T getBean() {
		return this.tBean;
	}

	@Override
	public CollectableField getField(String sFieldName) {
		return new BeanPropertyCollectableField(this.getBean(), sFieldName, this.getCollectableEntity());
	}

	@Override
	public void setField(String sFieldName, CollectableField clctfValue) {
		this.setCollectableFieldUsingBeanProperty(this.getBean(), sFieldName, clctfValue);

		assert this.getField(sFieldName).equals(clctfValue) : "value of field " + sFieldName + " is " + this.getField(sFieldName) + " - expected: " + clctfValue;
	}

	/**
	 * Helper method for setField() implementations. Ignores failure of setting the value.
	 * @param oBean
	 * @param sFieldName
	 * @param clctfValue
	 */
	protected void setCollectableFieldUsingBeanProperty(Object oBean, String sFieldName, CollectableField clctfValue) {
		this.setCollectableFieldUsingBeanProperty(oBean, sFieldName, clctfValue, true);
	}

	/**
	 * Helper method for setField() implementations.
	 * @param oBean
	 * @param sFieldName
	 * @param clctfValue
	 * @param bIgnoreFailureOfSetValue Setting the value may not be supported by some id fields in some VOs. If true,
	 * 		we try to do so, but ignore NoSuchMethodException.
	 */
	protected void setCollectableFieldUsingBeanProperty(Object oBean, String sFieldName, CollectableField clctfValue,
			boolean bIgnoreFailureOfSetValue) {
		try {
			setProperty(oBean, sFieldName, clctfValue.getValue());
		}
		catch (NoSuchMethodException ex) {
			if (bIgnoreFailureOfSetValue) {
				log.warn("CollectableField \"" + sFieldName + "\" could not be set.", ex);
				/** @todo A cleaner alternative would be to introduce the concept of "read only value" id fields. */
			}
			else {
				throw new CommonFatalException(ex);
			}
		}

		if (this.getCollectableEntity().getEntityField(sFieldName).isIdField()) {
			try {
				setProperty(oBean, sFieldName + "Id", clctfValue.getValueId());
			}
			catch (NoSuchMethodException ex) {
				throw new CommonFatalException(ex);
			}
		}
	}

	/**
	 * Utility method to set a bean property.
	 * @param sPropertyName
	 * @param oValue
	 * @todo Replace with CollectableElisaValueObject.setPropertyValue implementation and move to BeanUtils.
	 */
	protected static void setProperty(Object oBean, String sPropertyName, Object oValue) throws NoSuchMethodException {
		try {
			PropertyUtils.setSimpleProperty(oBean, sPropertyName, oValue);
			/** @todo BeanUtils.setProperty(tBean, sPropertyName, oValue); */
		}
		catch (IllegalAccessException ex) {
			throw new CommonFatalException(getErrorMessage(sPropertyName, oBean, true), ex);
		}
		catch (InvocationTargetException ex) {
			throw new CommonFatalException(getErrorMessage(sPropertyName, oBean, true), ex.getCause());
		}
		catch (IllegalArgumentException ex) {
			throw new CommonFatalException(getErrorMessage(sPropertyName, oBean, true), ex);
		}
	}

	/**
	 * @param sPropertyName
	 * @param oBean
	 * @param bSet true: "set" - false: "get"
	 * @return error message
	 */
	protected static String getErrorMessage(String sPropertyName, Object oBean, boolean bSet) {
		if(bSet)
			return "Failed to set property \"" + sPropertyName + "\" on bean \"" + oBean + "\".";
		else
			return "Failed to get property \"" + sPropertyName + "\" on bean \"" + oBean + "\".";
	}

	/**
	 * Transformer: extracts the wrapped bean from a given Collectable.
	 */
	public static class ExtractBean<T> implements Transformer<AbstractCollectableBean<T>, T> {
		@Override
		public T transform(AbstractCollectableBean<T> clctbean) {
			return clctbean.getBean();
		}
	}

}	// class AbstractCollectableBean
