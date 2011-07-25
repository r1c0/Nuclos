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
import java.util.Observer;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.genericobject.CollectableGenericObjectProxyListAdapter;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.client.genericobject.GenericObjectClientUtils;
import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.common.AttributeProvider;
import org.nuclos.common.CollectableEntityFieldWithEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.genericobject.GenericObjectUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.genericobject.searchcondition.CollectableGenericObjectSearchExpression;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;

public class GenericObjectSearchStrategy extends CollectSearchStrategy<CollectableGenericObjectWithDependants> {

	// static

	private static final Logger LOG = Logger.getLogger(GenericObjectSearchStrategy.class);

	// instance

	private final GenericObjectDelegate lodelegate = GenericObjectDelegate.getInstance();

	private ProxyList<CollectableGenericObjectWithDependants> proxylstclct;

	// contructor

	public GenericObjectSearchStrategy() {
	}

	protected final GenericObjectCollectController getGenericObjectController() {
		return (GenericObjectCollectController) getCollectController();
	}

	/**
	 * @return List<Collectable>
	 * @throws CollectableFieldFormatException
	 * @postcondition result != null
	 * @own-thread
	 * 
	 * TODO: Make this private again.
	 */
	@Override
	public ProxyList<CollectableGenericObjectWithDependants> getSearchResult() throws CollectableFieldFormatException {
		final CollectableGenericObjectSearchExpression clctexprInternal = getInternalSearchExpression();
		clctexprInternal.setValueListProviderDatasource(getValueListProviderDatasource());
		clctexprInternal.setValueListProviderDatasourceParameter(getValueListProviderDatasourceParameter());
		LOG.debug("Interne Suchbedingung: " + clctexprInternal.getSearchCondition());

		// OPTIMIZATION: only selected and/or required attributes are loaded here:
		final ProxyList<GenericObjectWithDependantsVO> proxylstlovwdvo = lodelegate.getGenericObjectsWithDependants(
				getGenericObjectController().getModuleId(), clctexprInternal, getSelectedAndRequiredAttributeIds(),
				getGenericObjectController().getSelectedSubEntityNames(), isParentFieldSelected(),
				getIncludeSubModulesForSearch());

		return new CollectableGenericObjectProxyListAdapter(proxylstlovwdvo);
	}

	@Override
	public SearchWorker<CollectableGenericObjectWithDependants> getSearchWorker() {
		return new GenericObjectObservableSearchWorker(getGenericObjectController());
	}

	@Override
	public SearchWorker<CollectableGenericObjectWithDependants> getSearchWorker(List<Observer> lstObservers) {
		GenericObjectObservableSearchWorker observableSearchWorker = new GenericObjectObservableSearchWorker(getGenericObjectController());
		for (Observer observer : lstObservers)
			observableSearchWorker.addObserver(observer);
		return observableSearchWorker;
	}

	@Override
	public void search() throws CommonBusinessException {
		this.search(false);
	}

	@Override
	public void search(boolean bRefreshOnly) throws CommonBusinessException {
		LOG.debug("START search");
		final GenericObjectCollectController cc = getGenericObjectController();

		cc.makeConsistent(true);
		cc.removePreviousChangeListenersForResultTableVerticalScrollBar();
		final ProxyList<CollectableGenericObjectWithDependants> lstclctResult = getSearchResult();
		setCollectableProxyList(lstclctResult);
		cc.fillResultPanel(lstclctResult, lstclctResult.size(), false);
		cc.setupChangeListenerForResultTableVerticalScrollBar();
		LOG.debug("FINISHED search");
	}

	/**
	 * @return the internal version of the collectable search condition, that is used for performing the actual search.
	 *         Includes the selected global search filter's internal search condition (if any).
	 * @throws CollectableFieldFormatException
	 */
	protected final CollectableSearchCondition getInternalSearchCondition() throws CollectableFieldFormatException {
		final CompositeCollectableSearchCondition compositecond = new CompositeCollectableSearchCondition(
				LogicalOperator.AND);

		final CollectableSearchCondition clctcond = GenericObjectClientUtils.getInternalSearchCondition(
				getGenericObjectController().getModuleId(), getCollectableSearchCondition());
		if (clctcond != null)
			compositecond.addOperand(clctcond);

		return SearchConditionUtils.simplified(compositecond);
	}

	/**
	 * @return search expression containing the internal version of the collectable search condition,
	 *         that is used for performing the actual search, and the sorting sequence.
	 * @throws CollectableFieldFormatException
	 */
	@Override
	public CollectableGenericObjectSearchExpression getInternalSearchExpression()
			throws CollectableFieldFormatException {
		final GenericObjectCollectController cc = getGenericObjectController();
		CollectableGenericObjectSearchExpression clctGOSearchExpression = new CollectableGenericObjectSearchExpression(
				getInternalSearchCondition(), cc.getResultController().getCollectableSortingSequence(),
				cc.getSearchDeleted());
		return clctGOSearchExpression;
	}

	private Set<Integer> getSelectedAndRequiredAttributeIds() {
		final GenericObjectCollectController cc = getGenericObjectController();
		final Set<? extends CollectableEntityField> stSelectedAttributes = CollectionUtils.selectIntoSet(
				cc.getSelectedFields(), new CollectableEntityFieldWithEntity.HasEntity(cc.getCollectableEntity()));
		final Set<String> stFieldNamesSelected = CollectionUtils.transformIntoSet(stSelectedAttributes,
				new CollectableEntityField.GetName());
		final Set<String> stFieldNamesRequired = getRequiredFieldNamesForResult();
		final Set<String> stFieldNamesSelectedOrRequired = CollectionUtils.union(stFieldNamesSelected,
				stFieldNamesRequired);
		return CollectionUtils.transformIntoSet(stFieldNamesSelectedOrRequired,
				new AttributeProvider.GetAttributeIdByName(cc.getEntity(), AttributeCache.getInstance()));
	}

	@Override
	public boolean getIncludeSubModulesForSearch() {
		// Don't include submodules unless this controller is for a submodule.
		// Don't include submodules for general search:
		final Integer iModuleId = getGenericObjectController().getModuleId();
		return (iModuleId != null) && Modules.getInstance().isSubModule(iModuleId);
	}

	private boolean isParentFieldSelected() {
		final GenericObjectCollectController cc = getGenericObjectController();
		return GenericObjectUtils.containsParentField(cc.getSelectedFields(), cc.getParentEntityName());
	}

	/**
	 * TODO: Make this protected again.
	 * 
	 * @deprecated Move to ObservableSearchWorker???
	 */
	@Override
	public void setCollectableProxyList(ProxyList<CollectableGenericObjectWithDependants> proxylstclct) {
		this.proxylstclct = proxylstclct;
	}

	// @todo move to ResultController or ResultPanel?
	@Override
	public ProxyList<CollectableGenericObjectWithDependants> getCollectableProxyList() {
		return proxylstclct;
	}

}