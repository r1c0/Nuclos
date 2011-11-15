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

import java.util.LinkedList;
import java.util.List;

import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdListCondition;
import org.nuclos.common.collect.collectable.searchcondition.PivotJoinCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSelfSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collect.collectable.searchcondition.PlainSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.RefJoinCondition;
import org.nuclos.common.collect.collectable.searchcondition.ReferencingCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.TrueCondition;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.PredicateUtils;

public class SimplifiedVisitor implements Visitor<CollectableSearchCondition, RuntimeException>,
		CompositeVisitor<CollectableSearchCondition, RuntimeException> {

	@Override
	public CollectableSearchCondition visitTrueCondition(TrueCondition truecond) {
		// there is nothing to simplify. We can return the condition itself as it is immutable.
		return truecond;
	}

	@Override
	public CollectableSearchCondition visitAtomicCondition(AtomicCollectableSearchCondition atomiccond) {
		// there is nothing to simplify. We can return the condition itself as it is immutable.
		return atomiccond;
	}

	@Override
	public CollectableSearchCondition visitCompositeCondition(CompositeCollectableSearchCondition compositecond) {
		// 1. simplify the operands and eliminate duplicates among them:
		final List<CollectableSearchCondition> lstSimplifiedOperands = CollectionUtils.select(
				getSimplifiedOperands(compositecond), PredicateUtils.<CollectableSearchCondition> isUnique());

		// 2. then look what is left:
		final CollectableSearchCondition result;
		if (lstSimplifiedOperands.isEmpty()) {
			// @todo replace with TrueCondition
			result = null;
		} else {
			final LogicalOperator logicalop = compositecond.getLogicalOperator();
			switch (logicalop) {
			case NOT:
				final CollectableSearchCondition condOperand = lstSimplifiedOperands.get(0);
				if (condOperand instanceof AtomicCollectableSearchCondition) {
					result = SearchConditionUtils.negationOf((AtomicCollectableSearchCondition) condOperand);
				} else {
					result = SearchConditionUtils.not(condOperand);
				}
				break;

			case AND:
			case OR:
				assert lstSimplifiedOperands.size() >= 1;

				if (lstSimplifiedOperands.size() == 1) {
					result = lstSimplifiedOperands.get(0);
				} else {
					result = new CompositeCollectableSearchCondition(logicalop, lstSimplifiedOperands);
				}
				break;

			default:
				throw new IllegalArgumentException("Unknown operand:" + logicalop);
			}
		}
		return result;
	}

	@Override
	public CollectableSearchCondition visitIdCondition(CollectableIdCondition idcond) {
		// there is nothing to simplify. We can return the condition itself as it is immutable.
		return idcond;
	}

	@Override
	public CollectableSearchCondition visitReferencingCondition(ReferencingCollectableSearchCondition refcond) {
		ReferencingCollectableSearchCondition res = new ReferencingCollectableSearchCondition(
				refcond.getReferencingField(), SearchConditionUtils.simplified(refcond.getSubCondition()));
		res.setConditionName(refcond.getConditionName());
		return res;
	}

	@Override
	public CollectableSearchCondition visitSubCondition(CollectableSubCondition subcond) {
		CollectableSubCondition res = new CollectableSubCondition(subcond.getSubEntityName(),
				subcond.getForeignKeyFieldName(), SearchConditionUtils.simplified(subcond.getSubCondition()));
		res.setConditionName(subcond.getConditionName());
		return res;
	}

	@Override
	public CollectableSearchCondition visitPivotJoinCondition(PivotJoinCondition joincond) {
		// do nothing
		return joincond;
	}

	@Override
	public CollectableSearchCondition visitRefJoinCondition(RefJoinCondition joincond) {
		// do nothing
		return joincond;
	}

	@Override
	public CollectableSearchCondition visitSelfSubCondition(CollectableSelfSubCondition subcond) {
		CollectableSelfSubCondition res = new CollectableSelfSubCondition(subcond.getForeignKeyFieldName(),
				SearchConditionUtils.simplified(subcond.getSubCondition()), subcond.getSubEntityName());
		res.setConditionName(subcond.getConditionName());
		return res;
	}

	@Override
	public CollectableSearchCondition visitPlainSubCondition(PlainSubCondition subcond) {
		return subcond;
	}

	private static List<CollectableSearchCondition> getSimplifiedOperands(
			CompositeCollectableSearchCondition compositecond) {
		final List<CollectableSearchCondition> result = new LinkedList<CollectableSearchCondition>();
		for (CollectableSearchCondition condChild : compositecond.getOperands()) {
			assert condChild != null;
			final CollectableSearchCondition condSimplifiedChild = SearchConditionUtils.simplified(condChild);
			if (condSimplifiedChild != null) {
				boolean bChildAdded = false;
				// if the child's logical operator is the same as this' operator and the operator is associative,
				// just add the child's operands to this' operands:
				if (compositecond.getLogicalOperator().isAssociative()
						&& condSimplifiedChild instanceof CompositeCollectableSearchCondition) {
					final CompositeCollectableSearchCondition compositecondSimplifiedChild = (CompositeCollectableSearchCondition) condSimplifiedChild;
					if (compositecondSimplifiedChild.getLogicalOperator() == compositecond.getLogicalOperator()) {
						result.addAll(compositecondSimplifiedChild.getOperands());
						bChildAdded = true;
					}
				}
				// otherwise, add the simplified child:
				if (!bChildAdded) {
					result.add(condSimplifiedChild);
				}
			}
		}
		return result;
	}

	@Override
	public CollectableSearchCondition visitIdListCondition(CollectableIdListCondition collectableIdListCondition)
			throws RuntimeException {
		return collectableIdListCondition;
	}

} // class SimplifiedVisitor

