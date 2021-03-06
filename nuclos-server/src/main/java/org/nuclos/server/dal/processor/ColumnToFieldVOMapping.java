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
import org.nuclos.server.dblayer.query.DbExpression;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQueryBuilder;

/**
 * Map a database column to a simple value reference entity field representation.
 * 
 * @see org.nuclos.common.dal.vo.IDalWithFieldsVO#getFields()
 *
 * @param <T> Java type for the data in this column of the database.
 */
public final class ColumnToFieldVOMapping<T extends Object> extends AbstractColumnToVOMapping<T>
	implements IColumnWithMdToVOMapping<T>
{

	private final EntityFieldMetaDataVO field;

	/**
	 * Konstruktor für dynamische VO Werte (Die Werte werden in einer "Fields"-Liste gespeichert).
	 * @throws ClassNotFoundException 
	 */
	public ColumnToFieldVOMapping(String tableAlias, EntityFieldMetaDataVO field) throws ClassNotFoundException {
		super(tableAlias, field.getDbColumn(), field.getDataType(), field.isReadonly(), field.isDynamic());
		this.field = field;
	}

	@Override
	public DbExpression<T> getDbColumn(DbFrom from) {
		final DbExpression<T> result;
		if (field.getCalcFunction() == null) {
			result = super.getDbColumn(from);
		}
		else {
			final String tableAlias = from.getAlias();
			final DbQueryBuilder builder = from.getQuery().getBuilder();
			result = (DbExpression<T>) builder.plainExpression(
					DalUtils.getDbType(field.getDataType()), 
					builder.getDBAccess().getSchemaName() + "." 
					+ field.getCalcFunction() + "(" + tableAlias + ".INTID) " + field.getDbColumn());
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
		if (!(o instanceof ColumnToFieldVOMapping)) return false;
		final ColumnToFieldVOMapping<T> other = (ColumnToFieldVOMapping<T>) o;
		return field.equals(other.field);
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
	public EntityFieldMetaDataVO getMeta() {
		return field;
	}

	@Override
	public Object convertFromDalFieldToDbValue(IDalVO dal) {
		final IDalWithFieldsVO<?> realDal = (IDalWithFieldsVO<?>) dal;
		try {
			return convertToDbValue(getDataType(), realDal.getFields().get(field.getField()));
		} catch (Exception e) {
			throw new CommonFatalException(e);
		}
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

	@Override
	public boolean constructJoinForStringifiedRefs() {
		return true;
	}

}
