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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.nuclos.common.collect.collectable.searchcondition.TrueCondition;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.server.dblayer.DbTuple;

public class DbQuery<T extends Object> {
	
	private static final Logger LOG = Logger.getLogger(DbQuery.class);

	private final DbQueryBuilder builder;
	private final Class<T> resultType;
	private Set<DbFrom> roots = new LinkedHashSet<DbFrom>();
	private Map<String, DbFrom> tableAliases = new TreeMap<String, DbFrom>(String.CASE_INSENSITIVE_ORDER);
	private boolean distinct = false;
	private int maxResults = -1;
	private List<DbSelection<T>> selections;
	private DbCondition condition;
	private List<DbExpression<?>> groupList;
	private DbCondition groupRestriction;
	private List<DbOrder> orderList;
	
	DbQuery(DbQueryBuilder builder, Class<T> resultType) {
		this.builder = builder;
		this.resultType = resultType;
	}
	
	public DbQueryBuilder getBuilder() {
		return builder;
	}
	
	public Class<T> getResultType() {
		return resultType;
	}	
	public DbFrom from(String table) {
		DbFrom from = new DbFrom(this, table);
		roots.add(from);
		return from;
	}
	
	public Set<DbFrom> getRoots() {
		return roots;
	}
	
	public DbQuery<T> select(DbSelection<T> selection) {
		// this.selections = Collections.singletonList(selection);	
		this.selections = new ArrayList<DbSelection<T>>();
		this.selections.add(selection);
		checkSelections();
		return this;
	}
	
	public DbQuery<T> selectLiberate(DbSelection<?> selection) {
		// this.selections = Collections.singletonList(selection);
		this.selections = new ArrayList<DbSelection<T>>();
		this.selections.add((DbSelection<T>) selection);
		checkSelections();
		return this;
	}
	
	public DbQuery<T> multiselect(DbSelection<?> selection1, DbSelection<?>...selections) {
		return multiselect(CollectionUtils.asList(selection1, selections));
	}
	
	public DbQuery<T> multiselect(List<? extends DbSelection<?>> selections) {
		final Class<T> clazz = getResultType();
		if (clazz == Object[].class || clazz == DbTuple.class) {
			this.selections = (List<DbSelection<T>>) selections;
		} else {
			throw new IllegalArgumentException("Multi selection requires tuple/array result type, not " + clazz);
		}
		checkSelections();
		return this;
	}
	
	private final void checkSelections() {
		final int size = selections.size();
		if (size == 0) throw new IllegalArgumentException("No item in SELECT clause");
		if (size > 1) {
			final Class<T> clazz = getResultType();
			if (clazz != Object[].class && clazz != DbTuple.class) {
				throw new IllegalArgumentException("Multi selection requires tuple/array result type, not " + clazz);
			}			
		}
		/*
		final SortedSet<String> names = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		for (Iterator<? extends DbSelection<?>> it = selections.iterator(); it.hasNext();) {
			final DbSelection<?> s = it.next();
			final String n = s.getSqlColumnExpr();
			if (!names.add(n)) {
				LOG.info("The name/alias " + n + " appears more than once in the SELECT clause " + selections);
				it.remove();
				// throw new IllegalArgumentException("The name/alias " + n + " appears more than once in the SELECT clause " + selections);
			}
		}
		*/
	}
	
	public List<DbSelection<T>> getSelections() {
		return selections;
	}
	
	public DbQuery<T> distinct(boolean distinct) {
		this.distinct = distinct;
		return this;
	}
	
	public boolean isDistinct() {
		return distinct;
	}
	
	public DbQuery<T> maxResults(int maxResults) {
		this.maxResults = maxResults;
		return this;
	}

	public int getMaxResults() {
		return maxResults;
	}	
	
	public DbQuery<T> where(DbCondition condition) {
		if (this.condition != null && !TrueCondition.TRUE.equals(this.condition)) {
			throw new IllegalStateException("where condition already set, use addToWhere or replaceWhere if this is intended");
		}
		this.condition = condition;
		return this;
	}
	
	public DbQuery<T> replaceWhere(DbCondition condition) {
		this.condition = condition;
		return this;
	}
	
	public DbQuery<T> addToWhereAsAnd(DbCondition condition) {
		if (this.condition == null) {
			this.condition = condition;
		}
		else {
			this.condition = builder.and(this.condition, condition);
		}
		return this;
	}	
	
	public DbQuery<T> addToWhereAsOr(DbCondition condition) {
		this.condition = builder.or(this.condition, condition);
		return this;
	}	
	
	public DbCondition getRestriction() {
		return condition;
	}

	public DbQuery<T> groupBy(DbExpression<?>...grouping) {
		this.groupList = Arrays.asList(grouping);
		return this;
	}
	
	public DbQuery<T> groupBy(List<DbExpression<?>> lst) {
		this.groupList = lst;
		return this;
	}
	
	public List<DbExpression<?>> getGroupList() {
		return groupList != null ? groupList : Collections.<DbExpression<?>>emptyList();
	}
	
	public DbQuery<T> having(DbCondition condition) {
		this.groupRestriction = condition;
		return this;
	}
	
	public DbCondition getGroupRestriction() {
		return groupRestriction;
	}

	public void orderBy(DbOrder...order) {
		orderBy(Arrays.asList(order)); 
	}
	
	public void orderBy(List<DbOrder> order) {
		this.orderList = new ArrayList<DbOrder>(order);
	}
	
	public List<DbOrder> getOrderList() {
		return (orderList != null) ? orderList : Collections.<DbOrder>emptyList();
	}

	public <U> DbQuery<U> subquery(Class<U> type) {
		return builder.createQuery(type);
	}

	void registerAlias(DbFrom from, String tableAlias) {
		if (tableAliases.containsKey(tableAlias))
			throw new IllegalArgumentException("Alias " + tableAlias + " already used: " + tableAliases.keySet());
		tableAliases.put(tableAlias, from);
	}

	@Override
	public String toString() {
		return "DbQuery [resultType=" + resultType + ", roots=" + roots + ", tableAliases=" + tableAliases + ", distinct=" + distinct + ", maxResults=" + maxResults + ", selections=" + selections + ", condition=" + condition
				+ ", groupList=" + groupList + ", groupRestriction=" + groupRestriction + ", orderList=" + orderList + "]";
	}
}
