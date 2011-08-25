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
package org.nuclos.server.genericobject.searchcondition;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.nuclos.common.collect.collectable.CollectableSorting;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.server.report.valueobject.DatasourceVO;

/**
 * A search expression consists of a search condition and an optional sorting order.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph Radig</a>
 * @version 00.01.000
 */
public class CollectableSearchExpression implements Serializable {

	private static final long serialVersionUID = -5153960862338691472L;
	
	/**
	 * the search condition
	 */
	private CollectableSearchCondition clctcond;

	/**
	 * the sorting order.
	 */
	private List<CollectableSorting> lstSortingOrder;
	
	/** if true, Nuclos-internal data records should be included */ 
	private boolean includingSystemData = true;
	
	private DatasourceVO valueListProviderDatasource;
	private Map<String, Object> valueListProviderDatasourceParameter;

	/**
	 * creates a search expression with an empty search condition.
	 * @postcondition this.getSearchCondition() == null
	 */
	public CollectableSearchExpression() {
		this(null);
		assert this.getSearchCondition() == null;
	}

	/**
	 * creates a search expression with the given search condition.
	 * @param clctcond
	 * @postcondition this.getSearchCondition() == clctcond
	 */
	public CollectableSearchExpression(CollectableSearchCondition clctcond) {
		this(clctcond, Collections.<CollectableSorting>emptyList());
		assert this.getSearchCondition() == clctcond;
	}

	/**
	 * creates a search expression with the given search condition and sorting order.
	 * @param clctcond
	 * @param lstSortingOrder
	 * @postcondition this.getSearchCondition() == clctcond
	 */
	public CollectableSearchExpression(CollectableSearchCondition clctcond, List<CollectableSorting> lstSortingOrder) {
		this.clctcond = clctcond;
		this.lstSortingOrder = lstSortingOrder;
		assert this.getSearchCondition() == clctcond;
	}

	/**
	 * @return the search condition
	 */
	public CollectableSearchCondition getSearchCondition() {
		return this.clctcond;
	}
	
	public void setSearchCondition(CollectableSearchCondition searchCondition) {
		this.clctcond = searchCondition;
	}

	/**
	 * @return sorting order
	 */
	public List<CollectableSorting> getSortingOrder() {
		return this.lstSortingOrder;
	}
	
	public void setSortingOrder(List<CollectableSorting> sortingOrder) {
		this.lstSortingOrder = sortingOrder;
	}

	public void setIncludingSystemData(boolean includingSystemData) {
		this.includingSystemData = includingSystemData;
	}
	
	public boolean isIncludingSystemData() {
		return includingSystemData;
	}

	/**
     * @param valueListProviderDatasource the valueListProviderDatasource to set
     */
    public void setValueListProviderDatasource(
        DatasourceVO valueListProviderDatasource) {
	    this.valueListProviderDatasource = valueListProviderDatasource;
    }

	/**
     * @return the valueListProviderDatasource
     */
    public DatasourceVO getValueListProviderDatasource() {
	    return valueListProviderDatasource;
    }

	/**
     * @param valueListProviderDatasourceParameter the valueListProviderDatasourceParameter to set
     */
    public void setValueListProviderDatasourceParameter(
        Map<String, Object> valueListProviderDatasourceParameter) {
	    this.valueListProviderDatasourceParameter = valueListProviderDatasourceParameter;
    }

	/**
     * @return the valueListProviderDatasourceParameter
     */
    public Map<String, Object> getValueListProviderDatasourceParameter() {
	    return valueListProviderDatasourceParameter;
    }

    @Override
    public String toString() {
    	final StringBuilder result = new StringBuilder();
    	result.append(getClass().getName()).append("[");
    	result.append("search=").append(getSearchCondition());
    	result.append(",inclSystem=").append(isIncludingSystemData());
    	result.append(",sort=").append(getSortingOrder());
    	result.append(",ds=").append(getValueListProviderDatasource());
    	result.append("]");
    	return result.toString();
    }
	
}	// class CollectableSearchExpression
