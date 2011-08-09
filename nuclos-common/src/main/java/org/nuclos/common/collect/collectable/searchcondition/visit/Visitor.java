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
import org.nuclos.common.collect.collectable.searchcondition.CollectableSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ReferencingCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.TrueCondition;

/**
 * Visitor for <code>CollectableSearchCondition</code>s.
 * For a description of the Visitor pattern, see the "GoF" Patterns book.
 */
public interface Visitor<O, Ex extends Exception> {

	O visitTrueCondition(TrueCondition truecond) throws Ex;

	O visitAtomicCondition(AtomicCollectableSearchCondition atomiccond) throws Ex;

	O visitCompositeCondition(CompositeCollectableSearchCondition compositecond) throws Ex;

	O visitIdCondition(CollectableIdCondition idcond) throws Ex;

	O visitIdListCondition(CollectableIdListCondition collectableIdListCondition) throws Ex;

	O visitSubCondition(CollectableSubCondition subcond) throws Ex;

	O visitJoinCondition(CollectableJoinCondition joincond) throws Ex;

	O visitReferencingCondition(ReferencingCollectableSearchCondition refcond) throws Ex;

} // interface Visitor
