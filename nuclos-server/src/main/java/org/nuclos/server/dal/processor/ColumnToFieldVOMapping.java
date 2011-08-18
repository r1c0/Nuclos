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
public final class ColumnToFieldVOMapping<T extends Object> extends AbstractColumnToVOMapping<T> 
{

	private final String field;

	/**
	 * Konstruktor für dynamische VO Werte (Die Werte werden in einer "Fields"-Liste gespeichert).
	 */
	public ColumnToFieldVOMapping(String tableAlias, String column, String field, Class<T> dataType,
			boolean isReadonly, boolean isCaseSensitive) {
		super(tableAlias, column, dataType, isReadonly, isCaseSensitive);
		if (field == null) throw new NullPointerException();
		this.field = field;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("col=").append(getColumn());
		result.append(", tableAlias=").append(getTableAlias());
		result.append(", field=").append(field);
		if (getDataType() != null)
			result.append(", type=").append(getDataType().getName());
		result.append("]");
		return result.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ColumnToFieldVOMapping)) return false;
		final ColumnToFieldVOMapping<T> other = (ColumnToFieldVOMapping<T>) o;
		return getColumn().equals(other.getColumn()) && field.equals(other.field);
	}
	
	@Override
	public int hashCode() {
		int result = getColumn().hashCode();
		result += 3 * field.hashCode();
		return result;
	}

	/**
	 * @deprecated This is impossible in the general case, thus avoid it.
	 */
	@Override
	public String getField() {
		return field;
	}

	@Override
	public Object convertFromDalFieldToDbValue(IDalVO dal) {
		final IDalWithFieldsVO<?> realDal = (IDalWithFieldsVO<?>) dal;
		try {
			return convertToDbValue(getDataType(), realDal.getFields().get(field));
		} catch (Exception e) {
			throw new CommonFatalException(e);
		}
	}

	@Override
	public void convertFromDbValueToDalField(IDalVO result, T o) {
		final IDalWithFieldsVO<Object> realDal = (IDalWithFieldsVO<Object>) result;
		try {
			realDal.getFields().put(field,
					convertFromDbValue(o, getColumn(), getDataType(), result.getId()));
		} catch (Exception e) {
			throw new CommonFatalException(e);
		}
	}

}
