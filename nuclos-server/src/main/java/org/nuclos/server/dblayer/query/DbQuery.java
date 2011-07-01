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

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.server.dblayer.DbTuple;

public class DbQuery<T> {

	private final DbQueryBuilder builder;
	private final Class<T> resultType;
	private Set<DbFrom> roots = new LinkedHashSet<DbFrom>();
	private Map<String, DbFrom> aliases = new TreeMap<String, DbFrom>(String.CASE_INSENSITIVE_ORDER);
	private boolean distinct = false;
	private int maxResults = -1;
	private DbSelection<T> selection;
	private DbCondition condition;
	private List<DbExpression<?>> groupList;
	private DbCondition groupRestriction;
	private List<DbOrder> orderList;
	// private Map<String, Object> parameters;
	
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
	
	@SuppressWarnings("unchecked")
	public DbQuery<T> select(DbSelection<? extends T> selection) {
		this.selection = (DbExpression<T>) selection;
		return this;
	}
	
	public DbQuery<T> multiselect(DbSelection<?> selection1, DbSelection<?>...selections) {
		return multiselect(CollectionUtils.asList(selection1, selections));
	}
	
	public DbQuery<T> multiselect(List<? extends DbSelection<?>> selections) {
		if (getResultType() == Object[].class || getResultType() == DbTuple.class) {
			this.selection = new DbCompoundSelection<T>(builder, getResultType(), selections);
		} else {
			throw new IllegalArgumentException("Multi selection requires tuple/array result type");
		}
		return this;
	}
	
	public DbSelection<T> getSelection() {
		return selection;
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
		this.condition = condition;
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

//	public void setParameter(String name, Object value) {
//		this.parameters.put(name, value);
//	}
//	
//	public Map<String, Object> getParameters() {
//		return parameters;
//	}
	
	//
	//
	//
	
	void registerAlias(DbFrom from, String alias) {
		if (aliases.containsKey(alias))
			throw new IllegalArgumentException("Alias " + alias + " already used");
		aliases.put(alias, from);
	}
}
