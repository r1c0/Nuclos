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

import java.util.List;
import java.util.Map;
import java.util.Observer;

import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.strategy.CompleteCollectablesStrategy;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.genericobject.searchcondition.CollectableGenericObjectSearchExpression;
import org.nuclos.server.report.valueobject.DatasourceVO;

public interface ISearchStrategy<Clct extends Collectable> {

	// initializing
	
	void setCollectController(CollectController<Clct> cc);
	
	// business
	
	/**
	 * performs a single-threaded search, according to the current search
	 * condition, if any. TODO replace with search(boolean bRefreshOnly)
	 * 
	 * @see #getSearchWorker()
	 * 
	 * @deprecated Use multithreaded search for new applications.
	 */
	void search() throws CommonBusinessException;

	/**
	 * @param bRefreshOnly
	 * @throws CommonBusinessException
	 * 
	 * @deprecated Use multithreaded search for new applications. Move to
	 *             SearchController. TODO: Make this protected again.
	 */
	@Deprecated
	void search(boolean bRefreshOnly) throws CommonBusinessException;

	/**
	 * performs a search, according to the current search condition, if any.
	 * This default implementation returns <code>null</code> indicating that
	 * single threaded search is used, which calls the deprecated search()
	 * method.
	 * 
	 * TODO add parameter bRefreshOnly
	 */
	SearchWorker<Clct> getSearchWorker();

	SearchWorker<Clct> getSearchWorker(List<Observer> lstObservers);

	//

	/**
	 * TODO: Can we get rid of this?
	 */
	void setValueListProviderDatasource(DatasourceVO valueListProviderDatasource);

	/**
	 * TODO: Can we get rid of this?
	 */
	void setValueListProviderDatasourceParameter(Map<String, Object> valueListProviderDatasourceParameter);

	DatasourceVO getValueListProviderDatasource();

	Map<String, Object> getValueListProviderDatasourceParameter();
	
	//
	
	/**
	 * @return the strategy used by this CollectController to complete <code>Collectable</code>s when necessary.
	 */
	CompleteCollectablesStrategy<Clct> getCompleteCollectablesStrategy();
	
	/**
	 * TODO: Can we get rid of this?
	 */
	void setCompleteCollectablesStrategy(CompleteCollectablesStrategy<Clct> strategy);

	//

	/**
	 * @param clct
	 * @return Is the given Collectable complete? That is: Does it contain all
	 *         data necessary for display in the Details panel?
	 */
	boolean isCollectableComplete(Clct clct);
	
	boolean getCollectablesInResultAreAlwaysComplete();

	//

	/**
	 * @return the search condition to be used for a search. This method returns
	 *         <code>getCollectableSearchConditionFromSearchPanel()</code>. This
	 *         condition can be refined by subclasses, by ANDing, ORing or
	 *         whatever. May be <code>null</code>.
	 * @postcondition result == null || result.isSyntacticallyCorrect()
	 */
	CollectableSearchCondition getCollectableSearchCondition()
			throws CollectableFieldFormatException;

	/**
	 * @return search expression containing the internal version of the collectable search condition,
	 *         that is used for performing the actual search, and the sorting sequence.
	 * @throws CollectableFieldFormatException
	 */
	CollectableGenericObjectSearchExpression getInternalSearchExpression() throws CollectableFieldFormatException;
	
	boolean getIncludeSubModulesForSearch();
	
	//

	ProxyList<Clct> getCollectableProxyList();

	/**
	 * TODO: Can we get rid of this?
	 */
	void setCollectableProxyList(ProxyList<Clct> list);

	ProxyList<Clct> getSearchResult() throws CollectableFieldFormatException;
}
