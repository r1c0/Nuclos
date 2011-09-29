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
package org.nuclos.server.dblayer.query;

import java.io.Serializable;
import java.util.Collection;

import org.nuclos.server.dblayer.impl.util.PreparedStringBuilder;

/**
 * TODO: DbExpression should *not* extend DbSelection.
 */
public class DbExpression<T> extends DbSelection<T> implements Serializable {

	private PreparedStringBuilder sqlString;

	/**
	 * @deprecated This should be package-private.
	 */
	public DbExpression(DbQueryBuilder builder, Class<? extends T> javaType, PreparedStringBuilder sqlString) {
		super(builder, javaType);
		this.sqlString = sqlString.freeze();
	}

	DbExpression(DbQueryBuilder builder, Class<? extends T> javaType, String tableAlias, PreparedStringBuilder sqlString) {
		super(builder, javaType, tableAlias);
		this.sqlString = sqlString.freeze();
	}

	@Override
	public String getSqlColumnExpr() {
		if (getAlias() == null)
			return getSqlString().toString();
		final StringBuilder result = new StringBuilder(getSqlString().toString());
		result.append(" ").append(getAlias());
		return result.toString();
	}

	/**
	 * Changes the underlying java type, but does <b>not</b> perform any conversions.
	 */
	public <X> DbExpression<X> as(Class<X> javaType) {
		return new DbExpression<X>(getBuilder(), javaType, sqlString);
	}

	public final DbCondition isNull() {
		return getBuilder().isNull(this);
	}

	public final DbCondition isNotNull() {
		return getBuilder().isNotNull(this);
	}

	public final DbCondition in(Collection<T> values) {
		return getBuilder().in(this, values);
	}

	public final DbCondition in(DbQuery<T> subquery) {
		return getBuilder().in(this, subquery);
	}

	final PreparedStringBuilder getSqlString() {
		return sqlString;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("alias=").append(getAlias());
		result.append(", type=").append(getJavaType());
		final PreparedStringBuilder psb = getSqlString();
		if (psb != null) {
			result.append(", sql=").append(psb);
			result.append(", frozen=").append(psb.isFrozen());
		}
		result.append("]");
		return result.toString();
	}

}
