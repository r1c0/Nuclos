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
import org.nuclos.server.dblayer.query.DbCompoundColumnExpression;
import org.nuclos.server.dblayer.query.DbExpression;
import org.nuclos.server.dblayer.query.DbFrom;

/**
 * Map a database column as a 'stringified' reference. 
 * <p>
 * In the general case, 'stringified' references are compound. Hence it is not 
 * possible to write to the database with this mapping class.
 * This is part of the effort to deprecate all views in Nuclos.
 * </p>
 * @param <T> Java type for the data in this column of the database.
 */
public final class ColumnToRefFieldVOMapping<T extends Object> extends AbstractColumnToVOMapping<T>
	implements IColumnWithMdToVOMapping<T>
{

	private final EntityFieldMetaDataVO field;

	/**
	 * Konstruktor für dynamische VO Werte (Die Werte werden in einer "Fields"-Liste gespeichert).
	 * @throws ClassNotFoundException 
	 */
	public ColumnToRefFieldVOMapping(String tableAlias, EntityFieldMetaDataVO field) throws ClassNotFoundException {
		super(tableAlias, field.getDbColumn(), field.getDataType(), field.isReadonly(), field.isDynamic());
		this.field = field;
	}

	@Override
	public DbExpression<T> getDbColumn(DbFrom from) {
		final DbExpression<T> result;
		if (field.getCalcFunction() == null) {
			try {
				result = new DbCompoundColumnExpression<T>(from, field);
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException(e.toString());
			}
		}
		else {
			throw new IllegalStateException();
		}
		return result;
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
		if (!(o instanceof ColumnToRefFieldVOMapping)) return false;
		final ColumnToRefFieldVOMapping<T> other = (ColumnToRefFieldVOMapping<T>) o;
		return field.equals(other.field);
	}
	
	@Override
	public int hashCode() {
		int result = getColumn().hashCode();
		result += 3 * field.hashCode();
		return result;
	}

	@Override
	public EntityFieldMetaDataVO getMeta() {
		return field;
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
		throw new CommonFatalException("Can't write to ref field " + field + " value " + dal);
	}

	@Override
	public void convertFromDbValueToDalField(IDalVO result, T o) {
		final IDalWithFieldsVO<Object> realDal = (IDalWithFieldsVO<Object>) result;
		try {
			realDal.getFields().put(field.getField(),
					convertFromDbValue(o, getColumn(), getDataType(), result.getId()));
		} catch (Exception e) {
			throw new CommonFatalException(e);
		}
	}

}
