//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.server.dal.processor;

import java.lang.reflect.Method;

import org.nuclos.common.dal.vo.IDalVO;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * Type parameter T is the java type
 */
public final class ColumnToBeanVOMapping<T> extends AbstractColumnToVOMapping<T> {

	private final String fieldName;
	private final Method setMethod;
	private final Method getMethod;

	/**
	 * Konstruktor für statische VO Werte (Aufruf von Methoden zum setzen und lesen von Werten).
	 */
	public ColumnToBeanVOMapping(String column, String fieldName, Method setMethod, Method getMethod, Class<T> dataType,
			boolean isReadonly) {
		super(column, dataType, isReadonly, false);
		if (fieldName == null) throw new NullPointerException();
		if (getMethod == null) throw new NullPointerException();
		if (setMethod == null && !isReadonly) throw new NullPointerException();
		this.fieldName = fieldName;
		this.setMethod = setMethod;
		this.getMethod = getMethod;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("col=").append(getColumn());
		result.append(", getter=").append(getMethod);
		if (getDataType() != null)
			result.append(", type=").append(getDataType().getName());
		result.append("]");
		return result.toString();
	}

	@Override
	public String getField() {
		return fieldName;
	}

	@Override
	public Object convertFromDalFieldToDbValue(IDalVO dal) {
		try {
			return convertToDbValue(getDataType(), getMethod.invoke(dal));
		} catch (Exception e) {
			throw new CommonFatalException(e);
		}
	}

	@Override
	public void convertFromDbValueToDalField(IDalVO result, T o) {
		try {
			setMethod.invoke(result, convertFromDbValue(o, getColumn(), getDataType(), result.getId()));
		} catch (Exception e) {
			throw new CommonFatalException(e);
		}
	}

}
