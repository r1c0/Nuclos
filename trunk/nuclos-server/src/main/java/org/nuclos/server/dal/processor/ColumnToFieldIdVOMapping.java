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

import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.IDalVO;
import org.nuclos.common.dal.vo.IDalWithFieldsVO;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.dal.DalUtils;

/**
 * Map a database column to a reference entity field representation.
 * <p>
 * This mapping is only used for primary key refs to foreign table now.
 * In all other ref cases it is replaced by 
 * {@link org.nuclos.server.dal.processor.ColumnToRefFieldVOMapping<T>}.
 * Perhaps we should consider this as deprecated ?!?
 * </p>
 * @see org.nuclos.common.dal.vo.IDalWithFieldsVO#getFieldIds()
 *
 * @param <T> Java type for the data in this column of the database.
 */
public final class ColumnToFieldIdVOMapping<T> extends AbstractColumnToVOMapping<T> 
	implements IColumnToVOMapping<T>
{

	private final EntityFieldMetaDataVO field;

	/**
	 * Konstruktor für dynamische VO Werte (Die Werte werden in einer "FieldIds"-Liste gespeichert)
	 * @param column
	 * @param setMethod
	 * @param getMethod
	 * @param dataType
	 * @param isReadonly
	 * @throws ClassNotFoundException 
	 */
	public ColumnToFieldIdVOMapping(String tableAlias, EntityFieldMetaDataVO field) throws ClassNotFoundException {
		// This is the reference from the (base) entity to the foreign table.
		super(tableAlias, DalUtils.getDbIdFieldName(field.getDbColumn()), Long.class.getName(), field.isReadonly(), field.isDynamic());
		this.field = field;
	}

	// @Override
	public EntityFieldMetaDataVO getMeta() {
		return field;
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
		if (!(o instanceof ColumnToFieldIdVOMapping)) return false;
		final ColumnToFieldIdVOMapping<T> other = (ColumnToFieldIdVOMapping<T>) o;
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
		return field.getField();
	}

	@Override
	public Object convertFromDalFieldToDbValue(IDalVO dal) {
		final IDalWithFieldsVO<?> realDal = (IDalWithFieldsVO<?>) dal;
		try {
			return convertToDbValue(getDataType(), realDal.getFieldIds().get(field.getField()));
		} catch (Exception e) {
			throw new CommonFatalException(e);
		}
	}

	@Override
	public void convertFromDbValueToDalField(IDalVO result, T o) {
		final IDalWithFieldsVO<?> realDal = (IDalWithFieldsVO<?>) result;
		try {
			realDal.getFieldIds().put(field.getField(),
					(Long) convertFromDbValue(o, getColumn(), getDataType(), result.getId()));
		} catch (Exception e) {
			throw new CommonFatalException(e);
		}
	}

}
