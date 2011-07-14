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

public class DbExpression<T> extends DbSelection<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	PreparedStringBuilder sqlString;
	
	public DbExpression(DbQueryBuilder builder, Class<? extends T> javaType, PreparedStringBuilder sqlString) {
		super(builder, javaType);
		this.sqlString = sqlString.freeze();
	}
	
	@Override
	public DbExpression<T> alias(String alias) {
		return (DbExpression<T>) super.alias(alias);
	}
	
	/**
	 * Changes the underlying java type, but does <b>not</b> perform any conversions.
	 */
	public <X> DbExpression<X> as(Class<X> javaType) {
		return new DbExpression<X>(builder, javaType, sqlString);
	}
	
	public DbCondition isNull() {
		return builder.isNull(this);
	}
	
	public DbCondition isNotNull() {
		return builder.isNotNull(this);
	}
	
	public DbCondition in(Collection<T> values) {
		return builder.in(this, values);
	}
	
	public DbCondition in(DbQuery<T> subquery) {
		return builder.in(this, subquery);
	}
}