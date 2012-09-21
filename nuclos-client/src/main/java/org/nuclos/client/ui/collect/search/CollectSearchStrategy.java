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
package org.nuclos.client.ui.collect.search;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.strategy.AlwaysLoadCompleteCollectablesStrategy;
import org.nuclos.client.ui.collect.strategy.CompleteCollectablesStrategy;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdListCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.genericobject.searchcondition.CollectableGenericObjectSearchExpression;
import org.nuclos.server.report.valueobject.DatasourceVO;

public class CollectSearchStrategy<Clct extends Collectable> implements ISearchStrategy<Clct> {

	private static final Logger LOG = Logger.getLogger(CollectSearchStrategy.class);

	/**
	 * Valuelist provider datasource for additional search condition
	 */
	private DatasourceVO valueListProviderDatasource;
	private Map<String, Object> valueListProviderDatasourceParameter;

	private CollectController<Clct> cc;

	private CompleteCollectablesStrategy<Clct> completecollectablesstrategy;

	private CollectableIdListCondition idListCondition;

	public CollectSearchStrategy() {
	}

	public final void setCollectController(CollectController<Clct> cc) {
		this.cc = cc;
		this.completecollectablesstrategy = new AlwaysLoadCompleteCollectablesStrategy<Clct>(cc);
	}

	protected final CollectController<Clct> getCollectController() {
		return cc;
	}

	/**
	 * performs a single-threaded search, according to the current search condition, if any.
	 * TODO replace with search(boolean bRefreshOnly)
	 * @see #getSearchWorker()
	 *
	 * @deprecated Use multithreaded search for new applications.
	 */
	@Deprecated
	public void search() throws CommonBusinessException {
		// leave implementation for derived class
		throw new UnsupportedOperationException("search");
	}

	@Override
	@Deprecated
	public void search(boolean bRefreshOnly) throws CommonBusinessException {
		this.search();
	}

	@Override
	public SearchWorker<Clct> getSearchWorker() {
		// leave implementation for derived class
		return null;
	}

	@Override
	public SearchWorker<Clct> getSearchWorker(List<Observer> lstObservers) {
		// leave implementation for derived class
		return null;
	}

	/**
	 * @return Valuelist provider datasource for additional search condition
	 */
	@Override
	public final DatasourceVO getValueListProviderDatasource() {
		return valueListProviderDatasource;
	}

	@Override
	public final Map<String, Object> getValueListProviderDatasourceParameter() {
		return valueListProviderDatasourceParameter;
	}

	/**
	 * TODO: Can we get rid of this?
	 */
	@Override
	public final void setValueListProviderDatasource(DatasourceVO valueListProviderDatasource) {
		this.valueListProviderDatasource = valueListProviderDatasource;
	}

	/**
	 * TODO: Can we get rid of this?
	 */
	@Override
	public final void setValueListProviderDatasourceParameter(Map<String, Object> valueListProviderDatasourceParameter) {
		this.valueListProviderDatasourceParameter = valueListProviderDatasourceParameter;
	}

	/**
	 * @return the search condition to be used for a search. This method returns <code>getCollectableSearchConditionFromSearchPanel()</code>.
	 *         This condition can be refined by subclasses, by ANDing, ORing or whatever. May be <code>null</code>.
	 * @postcondition result == null || result.isSyntacticallyCorrect()
	 */
	@Override
	public CollectableSearchCondition getCollectableSearchCondition() throws CollectableFieldFormatException {
		return addMainFilter(cc.getCollectableSearchConditionFromSearchPanel(false));
	}

	/**
	 * @return Set<String> the names of the required fields for the search result. It is merged with the names of the fields (columns)
	 * that the user has selected to be displayed, so these don't need to be given here.
	 */
	protected final Set<String> getRequiredFieldNamesForResult() {
		return this.getCompleteCollectablesStrategy().getRequiredFieldNamesForResult();
	}

	/**
	 * @param clct
	 * @return Is the given Collectable complete? That is: Does it contain all data necessary for display in the Details panel?
	 */
	@Override
	public final boolean isCollectableComplete(Clct clct) {
		return this.getCompleteCollectablesStrategy().isComplete(clct);
	}

	/**
	 * @return the strategy used by this CollectController to complete <code>Collectable</code>s when necessary.
	 */
	@Override
	public final CompleteCollectablesStrategy<Clct> getCompleteCollectablesStrategy() {
		return this.completecollectablesstrategy;
	}

	/**
	 * @param strategy
	 */
	@Override
	public final void setCompleteCollectablesStrategy(CompleteCollectablesStrategy<Clct> strategy) {
		this.completecollectablesstrategy = strategy;
	}

	/**
	 * @return Are <code>Collectable</code>s in the ResultTable always complete in this <code>CollectController</code>?
	 *
	 * @deprecated Use #getCompleteCollectablesStrategy().getCollectablesInResultAreAlwaysComplete() directly.
	 */
	@Override
	public final boolean getCollectablesInResultAreAlwaysComplete() {
		return getCompleteCollectablesStrategy().getCollectablesInResultAreAlwaysComplete();
	}

	/*
	 * The following methods have been defined only for a subset of strategies.
	 */

	@Override
	public ProxyList<Clct> getCollectableProxyList() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCollectableProxyList(ProxyList<Clct> list) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ProxyList<Clct> getSearchResult() throws CollectableFieldFormatException {
		throw new UnsupportedOperationException();
	}

	@Override
	public CollectableGenericObjectSearchExpression getInternalSearchExpression()
			throws CollectableFieldFormatException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getIncludeSubModulesForSearch() {
		throw new UnsupportedOperationException();
	}

	public CollectableIdListCondition getCollectableIdListCondition() {
		return idListCondition;
	}

	@Override
	public void setCollectableIdListCondition(CollectableIdListCondition condition) {
		this.idListCondition = condition;
	}
	
	public CollectableSearchCondition addMainFilter(CollectableSearchCondition cond) {
		if (cond == null) {
			if (cc.getMainFilter() != null) {
				return cc.getMainFilter().getSearchCondition();
			} else {
				return null;
			}
		} else {
			if (cc.getMainFilter() == null || cc.getMainFilter().getSearchCondition() == null) {
				return cond;
			} else {
				return SearchConditionUtils.and(cond, cc.getMainFilter().getSearchCondition());
			}
		}
	}
	
	@Override
	public Collection<EntityFieldMetaDataVO> getFields() {
		return null;
	}
}
