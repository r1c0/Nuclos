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

import java.util.Map;

import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdListCondition;
import org.nuclos.common.collect.collectable.searchcondition.PivotJoinCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.RefJoinCondition;
import org.nuclos.common.collect.collectable.searchcondition.ReferencingCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.TrueCondition;
import org.nuclos.common.collection.CollectionUtils;

/**
 * inner class GetAtomicFieldsMapVisitor
 */
public class GetAtomicFieldsMapVisitor implements Visitor<Map<String, CollectableField>, RuntimeException> {

	private final Map<String, CollectableField> mpFields = CollectionUtils.newHashMap();

	@Override
	public Map<String, CollectableField> visitTrueCondition(TrueCondition truecond) {
		// do nothing
		return mpFields;
	}

	@Override
	public Map<String, CollectableField> visitAtomicCondition(AtomicCollectableSearchCondition atomiccond) {
		if (atomiccond instanceof CollectableComparison) {
			final CollectableComparison comparison = (CollectableComparison) atomiccond;
			mpFields.put(comparison.getFieldName(), comparison.getComparand());
		}
		return mpFields;
	}

	@Override
	public Map<String, CollectableField> visitCompositeCondition(CompositeCollectableSearchCondition compositecond) {
		for (CollectableSearchCondition condChild : compositecond.getOperands()) {
			condChild.accept(this);
		}
		return mpFields;
	}

	@Override
	public Map<String, CollectableField> visitIdCondition(CollectableIdCondition idcond) {
		// do nothing
		return mpFields;
	}

	@Override
	public Map<String, CollectableField> visitSubCondition(CollectableSubCondition subcond) {
		// do nothing
		return mpFields;
	}

	@Override
	public Map<String, CollectableField> visitPivotJoinCondition(PivotJoinCondition joincond) {
		// do nothing
		return mpFields;
	}

	@Override
	public Map<String, CollectableField> visitRefJoinCondition(RefJoinCondition joincond) {
		// do nothing
		return mpFields;
	}

	@Override
	public Map<String, CollectableField> visitReferencingCondition(ReferencingCollectableSearchCondition refcond) {
		// do nothing
		return mpFields;
	}

	@Override
	public Map<String, CollectableField> visitIdListCondition(CollectableIdListCondition collectableIdListCondition)
			throws RuntimeException {
		// do nothing
		return mpFields;
	}

} // class GetAtomicFieldsMapVisitor
