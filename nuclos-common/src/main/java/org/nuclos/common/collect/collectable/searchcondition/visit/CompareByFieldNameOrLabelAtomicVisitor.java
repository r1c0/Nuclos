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
package org.nuclos.common.collect.collectable.searchcondition.visit;

import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdListCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableJoinCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSelfSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.PlainSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.ReferencingCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.TrueCondition;
import org.nuclos.common2.LangUtils;

public class CompareByFieldNameOrLabelAtomicVisitor implements Visitor<Integer, RuntimeException>,
		CompositeVisitor<Integer, RuntimeException> {

	private final CollectableSearchCondition cond2;
	private final boolean bSortByLabels;

	/**
	 * @param cond2 the second condition - must have the same type as the visited condition.
	 */
	public CompareByFieldNameOrLabelAtomicVisitor(CollectableSearchCondition cond2, boolean bSortByLabels) {
		this.cond2 = cond2;
		this.bSortByLabels = bSortByLabels;
	}

	@Override
	public Integer visitTrueCondition(TrueCondition truecond) throws RuntimeException {
		return 0;
	}

	@Override
	public Integer visitAtomicCondition(AtomicCollectableSearchCondition atomiccond1) throws RuntimeException {
		final AtomicCollectableSearchCondition atomiccond2 = (AtomicCollectableSearchCondition) cond2;
		return LangUtils.compare(getFieldNameOrLabel(atomiccond1, bSortByLabels),
				getFieldNameOrLabel(atomiccond2, bSortByLabels));
	}

	@Override
	public Integer visitCompositeCondition(CompositeCollectableSearchCondition compositecond1) throws RuntimeException {
		// all composites are created equal ;)
		return 0;
	}

	@Override
	public Integer visitIdCondition(CollectableIdCondition idcond1) throws RuntimeException {
		final CollectableIdCondition idcond2 = (CollectableIdCondition) cond2;
		return LangUtils.compare(idcond1.getId(), idcond2.getId());
	}

	@Override
	public Integer visitSubCondition(CollectableSubCondition subcond1) throws RuntimeException {
		final CollectableSubCondition subcond2 = (CollectableSubCondition) cond2;
		/** @todo use label if bSortByLabels == true? */
		return subcond1.getSubEntityName().compareTo(subcond2.getSubEntityName());
	}

	@Override
	public Integer visitJoinCondition(CollectableJoinCondition subcond1) throws RuntimeException {
		final CollectableJoinCondition subcond2 = (CollectableJoinCondition) cond2;
		/** @todo use label if bSortByLabels == true? */
		return subcond1.getSubEntityName().compareTo(subcond2.getSubEntityName());
	}

	@Override
	public Integer visitReferencingCondition(ReferencingCollectableSearchCondition refcond1) throws RuntimeException {
		// no order specified for referencing conditions.
		return 0;
	}

	@Override
	public Integer visitPlainSubCondition(PlainSubCondition subcond1) throws RuntimeException {
		//final PlainSubCondition subcond2 = (PlainSubCondition) cond2;
		// no order specified for referencing conditions.
		//return subcond1.getConditionName().compareTo(subcond2.getConditionName());
		return 0;
	}

	@Override
	public Integer visitSelfSubCondition(CollectableSelfSubCondition subcond) {
		return 0;
	}

	/**
	 * @param cond
	 * @param bSortByLabels
	 * @return the field name or label of the given condition
	 */
	private static String getFieldNameOrLabel(AtomicCollectableSearchCondition cond, boolean bSortByLabels) {
		return bSortByLabels ? cond.getFieldLabel() : cond.getFieldName();
	}

	@Override
	public Integer visitIdListCondition(CollectableIdListCondition collectableIdListCondition) throws RuntimeException {
		return 0;
	}

} // inner class CompareByFieldNameOrLabelAtomicVisitor

