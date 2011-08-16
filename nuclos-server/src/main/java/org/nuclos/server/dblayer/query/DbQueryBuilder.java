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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.nuclos.common2.InternalTimestamp;
import org.nuclos.server.dal.processor.ProcessorFactorySingleton;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.expression.DbNull;
import org.nuclos.server.dblayer.impl.SQLUtils2;
import org.nuclos.server.dblayer.impl.util.PreparedStringBuilder;
import org.nuclos.server.dblayer.impl.util.PreparedStringBuilder.Parameter;


public abstract class DbQueryBuilder implements Serializable {

	public static final String DATE_PATTERN_GERMAN = "dd.mm.yyyy";
	
	protected DbQueryBuilder() {
	}
	
	//
	// Query
	//
	
	public <T> DbQuery<T> createQuery(Class<T> javaType) {
		return new DbQuery<T>(this, javaType);
	}
	
	public DbQuery<DbTuple> createTupleQuery() {
		return createQuery(DbTuple.class); 
	}

	/**
	 * Creates a simple {@code select column, ... from table} query.
	 * Returns the result as tuple.
	 */
	public DbQuery<DbTuple> createSimpleQuery(String tableName, String columnName1, Class<?> columnType1, Object...varargs) {
		DbQuery<DbTuple> query = createTupleQuery();
		DbFrom from = query.from(tableName).alias(ProcessorFactorySingleton.BASE_ALIAS);
		List<DbExpression<?>> columns = new ArrayList<DbExpression<?>>();
		columns.add(from.column(columnName1, columnType1).alias(columnName1));
		for (int i = 0; i < varargs.length; i += 2) {
			String columnName = (String) varargs[i];
			Class<?> columnType = (Class<?>) varargs[i+1];
			columns.add(from.column(columnName, columnType).alias(columnName));
		}
		query.multiselect(columns);
		return query;
	}
	
	//
	// Expressions
	//

	@SuppressWarnings("unchecked")
	public <T> DbExpression<T> literal(T value) {
		Class<? extends T> javaType = (Class<? extends T>) value.getClass();
		return buildExpressionSql(javaType, literalImpl(value));
	}
	
	protected PreparedStringBuilder literalImpl(Object value) {
		Parameter param = new PreparedStringBuilder.Parameter().bind(value);
		return new PreparedStringBuilder().append(param);
	}
	
	public <T> DbExpression<T> nullLiteral(Class<T> javaType) {
		Parameter param = new PreparedStringBuilder.Parameter().bind(DbNull.forType(javaType));
		return buildExpressionSql(javaType, param);
	}
	
	public DbExpression<String> upper(DbExpression<String> x) {
		return buildExpressionSql(String.class, "UPPER(", x, ")");
	}
	
	public abstract DbExpression<java.util.Date> currentDate();

	public DbExpression<String> convertDateToString(DbExpression<java.util.Date> x, String pattern) {
		return buildExpressionSql(String.class, "TO_CHAR(", x, ", ", SQLUtils2.escape(pattern), ")");
	}
	
	public DbExpression<java.util.Date> convertInternalTimestampToDate(DbExpression<InternalTimestamp> x) {
		return buildExpressionSql(Date.class, "CAST(", x, " AS DATE)");
	}
	
	@Deprecated
	public <T> DbExpression<T> plainExpression(Class<T> javaType, String sql) {
		return buildExpressionSql(javaType, sql);
	}

	//
	// Count
	//
	
	public DbExpression<Long> count(DbExpression<?> x) {
		return buildExpressionSql(Long.class, "COUNT (", x, ")");
	}
	
	public DbExpression<Long> countDistinct(DbExpression<?> x) {
		return buildExpressionSql(Long.class, "COUNT (DISTINCT ", x, ")");
	}
	
	public DbExpression<Long> countRows() {
		return buildExpressionSql(Long.class, "COUNT (*)");
	}
	
	//
	// Aggregates
	//

	public <T> DbExpression<T> min(DbExpression<T> x) {
		return buildExpressionSql(x.getJavaType(), "MIN (", x, ")");
	}
	
	public <T> DbExpression<T> max(DbExpression<T> x) {
		return buildExpressionSql(x.getJavaType(), "MAX (", x, ")");
	}

	public <T> DbExpression<T> avg(DbExpression<T> x) {
		return buildExpressionSql(x.getJavaType(), "AVG (", x, ")");
	}

	public <T> DbExpression<T> sum(DbExpression<T> x) {
		return buildExpressionSql(x.getJavaType(), "SUM (", x, ")");
	}

	//
	// Conditions
	//

	public DbCondition equal(DbExpression<?> x, DbExpression<?> y) {
		return buildConditionSql(x, " = ", y);
	}
	
	public DbCondition equal(DbExpression<?> x, Object y) {
		if (y == null) {
			// Note that "x = NULL" (which *is always false*) is the intended behavior here!!
			return buildConditionSql(x, " = NULL");
		} else {
			return buildConditionSql(x, " = ", literalImpl(y));
		}
	}
	
	public DbCondition notEqual(DbExpression<?> x, DbExpression<?> y) {
		return buildConditionSql(x, " <> ", y);
	}

	public DbCondition lessThan(DbExpression<?> x, DbExpression<?> y) {
		return buildConditionSql(x, " < ", y);
	}
	
	public DbCondition lessThanOrEqualTo(DbExpression<?> x, DbExpression<?> y) {
		return buildConditionSql(x, " <= ", y);
	}

	public DbCondition greaterThan(DbExpression<?> x, DbExpression<?> y) {
		return buildConditionSql(x, " > ", y);
	}

	public DbCondition greaterThanOrEqualTo(DbExpression<?> x, DbExpression<?> y) {
		return buildConditionSql(x, " >= ", y);
	}

	public DbCondition isNull(DbExpression<?> expression) {
		return buildConditionSql(expression, " IS NULL");
	}
	
	public DbCondition isNotNull(DbExpression<?> expression) {
		return buildConditionSql(expression, " IS NOT NULL");
	}
	
	public DbCondition like(DbExpression<String> x, String pattern) {
		return buildConditionSql(x, " LIKE '", SQLUtils2.escape(pattern), "'");
	}

	public DbCondition notLike(DbExpression<String> x, String pattern) {
		return buildConditionSql(x, " NOT LIKE '", SQLUtils2.escape(pattern), "'");
	}
	
	public DbCondition like(DbExpression<String> x, DbExpression<String> y) {
		return buildConditionSql(x, " LIKE ", y);
	}

	public DbCondition notLike(DbExpression<String> x, DbExpression<String> y) {
		return buildConditionSql(x, " NOT LIKE ", x);
	}
	
	public <T, C extends Collection<T>> DbCondition isMember(DbExpression<T> e, DbExpression<T> coll) {
		return buildConditionSql(e, " IN ", coll);
	}
	
	protected <T> DbCondition in(DbExpression<T> expression, Collection<T> values) {
		if (values.size() > getInLimit())
			throw new IllegalArgumentException("limit for in-clause is " + getInLimit() + ", please split list");
		PreparedStringBuilder ps = new PreparedStringBuilder();
		int index = 0;
		for (T value : values) {
			if (index++ > 0)
				ps.append(", ");
			ps.append(literalImpl(value));
		}
		if (index == 0)
			ps.append("NULL");
		return buildConditionSql(expression, " IN (", ps, ")"); 
	}
	
	public <T> DbCondition in(DbExpression<T> expression, String sql) {
		return buildConditionSql(expression, " IN (", sql, ")");
	}
	
	protected <T> DbCondition in(DbExpression<T> expression, DbQuery<T> subquery) {
		return buildConditionSql(expression, " IN (", buildPreparedString(subquery), ")");
	}
	
	public DbCondition not(DbCondition condition) {
		return buildConditionSql("NOT (", condition.getSqlString(), ")");
	}

	public DbCondition and(DbCondition...conditions) {
		if (conditions.length == 0)
			return alwaysTrue(); // This is the same behavior as JPA's Criteria API
		PreparedStringBuilder sqlString = PreparedStringBuilder.valueOf("(");
		for (int i = 0; i < conditions.length; i++) {
			if (i > 0) sqlString.append(" AND ");
			sqlString.append(conditions[i].getSqlString());
		}
		return new DbCondition(this, sqlString.append(")"));
	}

	public DbCondition or(DbCondition...conditions) {
		if (conditions.length == 0)
			return alwaysFalse(); // This is the same behavior as JPA's Criteria API
		PreparedStringBuilder sqlString = PreparedStringBuilder.valueOf("(");
		for (int i = 0; i < conditions.length; i++) {
			if (i > 0) sqlString.append(" OR ");
			sqlString.append(conditions[i].getSqlString());
		}
		return new DbCondition(this, sqlString.append(")"));
	}
	
	public DbCondition alwaysTrue() {
		return buildConditionSql("1=1");
	}

	public DbCondition alwaysFalse() {
		return buildConditionSql("1<>1");
	}
	
	public int getInLimit() {
		// this limit is smaller than Oracle's
		return 100;
	}
	
	@Deprecated
	public DbCondition plainCondition(String sql) {
		return new DbCondition(this, PreparedStringBuilder.valueOf(sql));
	}
	
	//
	// Order
	//
	
	public DbOrder asc(DbExpression<?> expression) {
		return new DbOrder(expression, true);
	}
	
	public DbOrder desc(DbExpression<?> expression) {
		return new DbOrder(expression, false);
	}
	
	//
	//
	//
	
	protected DbCondition buildConditionSql(Object...args) {
		return new DbCondition(this, buildSql(args));
	}

	protected <T> DbExpression<T> buildExpressionSql(Class<? extends T> javaType, Object...args) {
		return new DbExpression<T>(this, javaType, buildSql(args));
	}
	
	protected abstract PreparedStringBuilder buildPreparedString(DbQuery<?> query);
	
	protected PreparedStringBuilder getPreparedString(DbCondition condition) {
		return condition.getSqlString();
	}

	protected PreparedStringBuilder getPreparedString(DbExpression<?> expression) {
		return expression.getSqlString();
	}
	
	protected PreparedStringBuilder buildSql(Object...args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i] instanceof DbCondition) {
				args[i] = getPreparedString((DbCondition) args[i]);
			} else if (args[i] instanceof DbExpression<?>) {
				args[i] = getPreparedString((DbExpression<?>) args[i]);
			}
		}
		return PreparedStringBuilder.concat(args);
	}
}
