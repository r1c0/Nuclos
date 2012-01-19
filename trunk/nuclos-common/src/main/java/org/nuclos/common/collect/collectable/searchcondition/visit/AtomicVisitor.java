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

import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparisonWithOtherField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparisonWithParameter;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIsNullCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableLikeCondition;

/**
 * Visitor for <code>AtomicCollectableSearchCondition</code>s.
 * For a description of the Visitor pattern, see the "GoF" Patterns book.
 */
public interface AtomicVisitor<O, Ex extends Exception> {

	O visitComparison(CollectableComparison comparison) throws Ex;

	O visitComparisonWithOtherField(CollectableComparisonWithOtherField comparisonwf) throws Ex;

	O visitComparisonWithParameter(CollectableComparisonWithParameter comparisonwp) throws Ex;

	O visitLikeCondition(CollectableLikeCondition likecond) throws Ex;

	O visitIsNullCondition(CollectableIsNullCondition isnullcond) throws Ex;

} // interface AtomicVisitor
