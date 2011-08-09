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
import org.nuclos.common.collect.collectable.searchcondition.PivotJoinCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ReferencingCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.TrueCondition;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;

/**
 * inner class ContainsVisitor
 */
public class ContainsVisitor implements Visitor<Boolean, RuntimeException> {

	/**
	 * Predicate: Contains
	 */
	private static class Contains implements Predicate<CollectableSearchCondition> {

		private final Predicate<CollectableSearchCondition> predicate;
		private final boolean bTraverseSubConditions;

		Contains(Predicate<CollectableSearchCondition> predicate, boolean bTraverseSubConditions) {
			this.predicate = predicate;
			this.bTraverseSubConditions = bTraverseSubConditions;
		}

		@Override
		public boolean evaluate(CollectableSearchCondition cond) {
			return SearchConditionUtils.contains(cond, predicate, bTraverseSubConditions);
		}

	} // inner class Contains

	private final Predicate<CollectableSearchCondition> predicate;
	private final boolean bTraverseSubConditions;

	public ContainsVisitor(Predicate<CollectableSearchCondition> predicate, boolean bTraverseSubConditions) {
		this.predicate = predicate;
		this.bTraverseSubConditions = bTraverseSubConditions;
	}

	@Override
	public Boolean visitTrueCondition(TrueCondition truecond) {
		return predicate.evaluate(truecond);
	}

	@Override
	public Boolean visitAtomicCondition(AtomicCollectableSearchCondition atomiccond) {
		return predicate.evaluate(atomiccond);
	}

	@Override
	public Boolean visitCompositeCondition(CompositeCollectableSearchCondition compositecond) {
		return predicate.evaluate(compositecond)
				|| CollectionUtils.exists(compositecond.getOperands(), new Contains(predicate, bTraverseSubConditions));
	}

	@Override
	public Boolean visitIdCondition(CollectableIdCondition idcond) {
		return predicate.evaluate(idcond);
	}

	@Override
	public Boolean visitSubCondition(CollectableSubCondition subcond) {
		boolean result = predicate.evaluate(subcond);
		if (!result && bTraverseSubConditions) {
			final CollectableSearchCondition condSub = subcond.getSubCondition();
			result = (condSub != null) && SearchConditionUtils.contains(condSub, predicate, bTraverseSubConditions);
		}
		return result;
	}

	@Override
	public Boolean visitPivotJoinCondition(PivotJoinCondition joincond) {
		boolean result = predicate.evaluate(joincond);
		return result;
	}

	@Override
	public Boolean visitReferencingCondition(ReferencingCollectableSearchCondition refcond) {
		boolean result = predicate.evaluate(refcond);
		if (!result && bTraverseSubConditions) {
			final CollectableSearchCondition condSub = refcond.getSubCondition();
			result = (condSub != null) && SearchConditionUtils.contains(condSub, predicate, bTraverseSubConditions);
		}
		return result;
	}

	@Override
	public Boolean visitIdListCondition(CollectableIdListCondition collectableIdListCondition) throws RuntimeException {
		return predicate.evaluate(collectableIdListCondition);
	}

} // class ContainsVisitor
