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

import org.nuclos.common.dal.vo.IDalVO;
import org.nuclos.common.dal.vo.IDalWithFieldsVO;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * Type parameter T is the java type
 */
public final class ColumnToFieldIdVOMapping<T> extends AbstractColumnToVOMapping<T> 
{

	private final String fieldId;

	/**
	 * Konstruktor für dynamische VO Werte (Die Werte werden in einer "FieldIds"-Liste gespeichert)
	 * @param column
	 * @param setMethod
	 * @param getMethod
	 * @param dataType
	 * @param isReadonly
	 */
	public ColumnToFieldIdVOMapping(String column, String fieldId, Class<T> dataType,
			boolean isReadonly, boolean isCaseSensitive) {
		super(column, dataType, isReadonly, isCaseSensitive);
		if (fieldId == null) throw new NullPointerException();
		this.fieldId = fieldId;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("col=").append(getColumn());
		result.append(", fieldId=").append(fieldId);
		if (getDataType() != null)
			result.append(", type=").append(getDataType().getName());
		result.append("]");
		return result.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ColumnToFieldIdVOMapping)) return false;
		final ColumnToFieldIdVOMapping<T> other = (ColumnToFieldIdVOMapping<T>) o;
		return getColumn().equals(other.getColumn()) && fieldId.equals(other.fieldId);
	}
	
	@Override
	public int hashCode() {
		int result = getColumn().hashCode();
		result += 3 * fieldId.hashCode();
		return result;
	}

	/**
	 * @deprecated This is impossible in the general case, thus avoid it.
	 */
	@Override
	public String getField() {
		return fieldId;
	}

	@Override
	public Object convertFromDalFieldToDbValue(IDalVO dal) {
		final IDalWithFieldsVO<?> realDal = (IDalWithFieldsVO<?>) dal;
		try {
			return convertToDbValue(getDataType(), realDal.getFieldIds().get(fieldId));
		} catch (Exception e) {
			throw new CommonFatalException(e);
		}
	}

	@Override
	public void convertFromDbValueToDalField(IDalVO result, T o) {
		final IDalWithFieldsVO<?> realDal = (IDalWithFieldsVO<?>) result;
		try {
			realDal.getFieldIds().put(fieldId,
					(Long) convertFromDbValue(o, getColumn(), getDataType(), result.getId()));
		} catch (Exception e) {
			throw new CommonFatalException(e);
		}
	}

}
