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
import org.nuclos.common.dblayer.JoinType;
import org.nuclos.common2.exception.CommonFatalException;
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
public final class ColumnToBeanVORefMapping<T extends Object> extends AbstractColumnToVOMapping<T> {

	private final String refColumn;
	private final String table;
	private final JoinType type;
	private final String alias;
	private final Method setMethod;

	public ColumnToBeanVORefMapping(String refColumn, String table, String tableAlias, JoinType type, String column, String alias, Method setMethod, Class<T> dataType) {
		super(tableAlias, column, dataType, true, false);
		this.refColumn = refColumn;
		this.table = table;
		this.type = type;
		this.alias = alias;
		this.setMethod = setMethod;
	}

	public String getRefColumn() {
		return refColumn;
	}

	public String getTable() {
		return table;
	}

	public JoinType getType() {
		return type;
	}

	public String getAlias() {
		return alias;
	}

	@Override
	public DbExpression<T> getDbColumn(DbFrom from) {
		DbExpression<T> result = from.column(getTableAlias(), getColumn(), getDataType());
		result.alias(getAlias());
		return result;
	}

	@Override
	public Object convertFromDalFieldToDbValue(IDalVO dal) {
		throw new CommonFatalException("Can't write to ref field " + table + "." + getColumn());
	}

	@Override
	public void convertFromDbValueToDalField(IDalVO result, T o) {
		try {
			setMethod.invoke(result, convertFromDbValue(o, getColumn(), getDataType(), result.getId()));
		} catch (Exception e) {
			throw new CommonFatalException(e);
		}
	}

	@Override
	public String getField() {
		return getColumn();
	}
}
