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

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.nuclos.client.masterdata.CollectableMasterDataProxyListAdapter;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIsNullCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;

public class MasterDataSearchStrategy extends CollectSearchStrategy<CollectableMasterDataWithDependants> {

	private static final Logger LOG = Logger.getLogger(MasterDataSearchStrategy.class);

	protected final MasterDataDelegate mddelegate = MasterDataDelegate.getInstance();

	private ProxyList<CollectableMasterDataWithDependants> proxylstclct;

	public MasterDataSearchStrategy() {
	}

	protected final MasterDataCollectController getMasterCollectDataController() {
		return (MasterDataCollectController) getCollectController();
	}

	@Override
	public SearchWorker<CollectableMasterDataWithDependants> getSearchWorker() {
		return new MasterDataObservableSearchWorker(getMasterCollectDataController());
	}

	@Override
	public SearchWorker<CollectableMasterDataWithDependants> getSearchWorker(List<Observer> lstObservers) {
		MasterDataObservableSearchWorker observableSearchWorker = new MasterDataObservableSearchWorker(getMasterCollectDataController());
		for (Observer observer : lstObservers) {
			observableSearchWorker.addObserver(observer);
		}
		return observableSearchWorker;
	}

	@Override
	public CollectableMasterDataProxyListAdapter getSearchResult() throws CollectableFieldFormatException {
		final MasterDataCollectController mdc = getMasterCollectDataController();
		final CollectableSearchExpression clctexpr = new CollectableSearchExpression(getCollectableSearchCondition(),
				mdc.getResultController().getCollectableSortingSequence());
		clctexpr.setValueListProviderDatasource(getValueListProviderDatasource());
		clctexpr.setValueListProviderDatasourceParameter(getValueListProviderDatasourceParameter());
		final ProxyList<MasterDataWithDependantsVO> mdproxylst = mddelegate.getMasterDataProxyList(mdc.getEntityName(),
				clctexpr);
		return new CollectableMasterDataProxyListAdapter(mdproxylst, mdc.getCollectableEntity());
	}

	@Override
	public void search() throws CommonBusinessException {
		final MasterDataCollectController mdc = getMasterCollectDataController();

		mdc.makeConsistent(true);
		mdc.removePreviousChangeListenersForResultTableVerticalScrollBar();
		final ProxyList<CollectableMasterDataWithDependants> lstclctResult = getSearchResult();
		setCollectableProxyList(lstclctResult);
		mdc.fillResultPanel(lstclctResult, lstclctResult.size(), false);
		mdc.setupChangeListenerForResultTableVerticalScrollBar();
	}

	// todo: can all (or at least most) of the methods concerning the search
	// proxy list be factored out somehow, as they are doubled in
	// GenericObjectCollectController?

	@Override
	public void setCollectableProxyList(ProxyList<CollectableMasterDataWithDependants> proxylstclct) {
		this.proxylstclct = proxylstclct;
	}

	@Override
	public ProxyList<CollectableMasterDataWithDependants> getCollectableProxyList() {
		return this.proxylstclct;
	}

	@Override
	public CollectableSearchCondition getCollectableSearchCondition() throws CollectableFieldFormatException {
		final CollectableSearchCondition result;
		final CollectableSearchCondition cond = (getCollectableIdListCondition() != null) ? addMainFilter(getCollectableIdListCondition()) : super.getCollectableSearchCondition();
		if (getMasterCollectDataController().isFilteringDesired()) {
			if (cond == null) {
				// If no other search conditions specified, the validity
				// condition is the only search condition
				result = this.getValiditySubCondition();
			} else {
				// We have to add the validity condition by AND to the other
				// condition(s)
				final CollectableSearchCondition condValidity = this.getValiditySubCondition();
				if (condValidity != null) {
					final Collection<CollectableSearchCondition> collOperands = Arrays
							.asList(new CollectableSearchCondition[] { cond, condValidity });
					result = new CompositeCollectableSearchCondition(LogicalOperator.AND, collOperands);
				} else {
					result = cond;
				}
			}
		} else {
			result = cond;
		}
		return result;
	}

	/**
	 * @return the subcondition that checks for "activeness"/validity.
	 */
	private CollectableSearchCondition getValiditySubCondition() {
		final MasterDataCollectController cc = getMasterCollectDataController();

		final CollectableSearchCondition result;
		// Create search condition to check if today is between validFrom and validUntil when they are not NULL:
		if (cc.hasValidityDate()) {
			final CollectableSearchCondition condValidFromGreaterToday = new CollectableComparison(cc
					.getCollectableEntity().getEntityField(MasterDataCollectController.FIELDNAME_VALIDFROM),
					ComparisonOperator.LESS_OR_EQUAL, new CollectableValueField(new Date()));
			final CollectableSearchCondition condValidFromIsNull = new CollectableIsNullCondition(cc
					.getCollectableEntity().getEntityField(MasterDataCollectController.FIELDNAME_VALIDFROM));
			final CollectableSearchCondition condValidFrom = new CompositeCollectableSearchCondition(
					LogicalOperator.OR, Arrays.asList(new CollectableSearchCondition[] { condValidFromIsNull,
							condValidFromGreaterToday }));

			final CollectableSearchCondition condValidUntilLessThan = new CollectableComparison(cc
					.getCollectableEntity().getEntityField(MasterDataCollectController.FIELDNAME_VALIDUNTIL),
					ComparisonOperator.GREATER_OR_EQUAL, new CollectableValueField(new Date()));
			final CollectableSearchCondition condValidUntilIsNull = new CollectableIsNullCondition(cc
					.getCollectableEntity().getEntityField(MasterDataCollectController.FIELDNAME_VALIDUNTIL));
			final CollectableSearchCondition condValidUntil = new CompositeCollectableSearchCondition(
					LogicalOperator.OR, Arrays.asList(new CollectableSearchCondition[] { condValidUntilIsNull,
							condValidUntilLessThan }));

			result = new CompositeCollectableSearchCondition(LogicalOperator.AND,
					Arrays.asList(new CollectableSearchCondition[] { condValidFrom, condValidUntil }));
		} else {
			result = null;
		}

		// Create search condition to check if active sign is true
		/*
		      if (hasActiveSign()) {
		         condActive = new CollectableComparison(getCollectableEntity().getEntityField(FIELDNAME_ACTIVE),
		               ComparisonOperator.EQUAL, new CollectableValueField(new Boolean(true)));

		         result = condActive;
		      }


		      // If both of the abovemust be checked, create a combined search condition
		      if (condValid != null && condActive != null) {
		         final Collection collOperands = Arrays.asList(new CollectableSearchCondition[]{condActive, condValid});
		         result = new CompositeCollectableSearchCondition(LogicalOperator.AND, collOperands);
		      }
		*/
		return result;
	}
}
