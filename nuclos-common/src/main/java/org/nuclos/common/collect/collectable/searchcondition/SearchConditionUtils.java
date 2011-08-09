//Copyright (C) 2010  Novabit Informationssysteme GmbH
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
package org.nuclos.common.collect.collectable.searchcondition;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.prefs.Preferences;

import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collect.collectable.searchcondition.visit.AtomicVisitor;
import org.nuclos.common.collect.collectable.searchcondition.visit.CompareByFieldNameOrLabelAtomicVisitor;
import org.nuclos.common.collect.collectable.searchcondition.visit.CompositeVisitor;
import org.nuclos.common.collect.collectable.searchcondition.visit.ContainsVisitor;
import org.nuclos.common.collect.collectable.searchcondition.visit.GetAtomicFieldsMapVisitor;
import org.nuclos.common.collect.collectable.searchcondition.visit.PutSearchConditionToPrefsVisitor;
import org.nuclos.common.collect.collectable.searchcondition.visit.SimplifiedVisitor;
import org.nuclos.common.collect.collectable.searchcondition.visit.Visitor;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.PredicateUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.exception.PreferencesException;

/**
 * Helper class for <code>CollectableSearchCondition</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class SearchConditionUtils {

	private static final String ALWAYS_FALSE_CONDITION = "AlwaysFalseCondition";

	protected SearchConditionUtils() {
	}

	/**
	 * @param cond May be <code>null</code>.
	 * @postcondition result != null
	 * @postcondition (cond != null) --> result == cond
	 * @postcondition (cond == null) --> result == TrueCondition.TRUE
	 */
	public static CollectableSearchCondition trueIfNull(CollectableSearchCondition cond) {
		return (cond == null) ? TrueCondition.TRUE : cond;
	}

	/**
	 * @param acond
	 * @return a new conjunction containing the given operands.
	 * @postcondition result.getLogicalOperator() == LogicalOperator.AND
	 */
	public static CompositeCollectableSearchCondition and(CollectableSearchCondition... acond) {
		final CompositeCollectableSearchCondition result = new CompositeCollectableSearchCondition(LogicalOperator.AND);
		for (CollectableSearchCondition cond : acond) {
			result.addOperand(cond);
		}
		assert result.getLogicalOperator() == LogicalOperator.AND;
		return result;
	}

	/**
	 * @param acond
	 * @return a new disjunction containing the given operands.
	 * @postcondition result.getLogicalOperator() == LogicalOperator.OR
	 */
	public static CompositeCollectableSearchCondition or(CollectableSearchCondition... acond) {
		final CompositeCollectableSearchCondition result = new CompositeCollectableSearchCondition(LogicalOperator.OR);
		for (CollectableSearchCondition cond : acond) {
			result.addOperand(cond);
		}
		assert result.getLogicalOperator() == LogicalOperator.OR;
		return result;
	}

	/**
	 * @param cond
	 * @return a new negation containing the given operand.
	 * @postcondition result.getLogicalOperator() == LogicalOperator.NOT
	 */
	public static CompositeCollectableSearchCondition not(CollectableSearchCondition cond) {
		final CompositeCollectableSearchCondition result = new CompositeCollectableSearchCondition(LogicalOperator.NOT);
		result.addOperand(cond);
		assert result.getLogicalOperator() == LogicalOperator.NOT;
		return result;
	}

	/**
	 * @param cond
	 * @return true if this condition is ALWAYS_FALSE_CONDITION -- constructed as not(TrueCondition).
	 */
	public static boolean isAlwaysFalseCondition(CollectableSearchCondition cond){
		return cond.getConditionName() != null && cond.getConditionName().equals(ALWAYS_FALSE_CONDITION);
	}
	
	/**
	 * @param collIds Collection<Object> collection of ids
	 * @return a <code>CollectableSearchCondition</code> like "(id=id1) OR (id=id2) OR ..." that finds all objects
	 * with the given ids.
	 * @precondition !CollectionUtils.isNullOrEmpty(collIds)
	 */
	public static CollectableSearchCondition getCollectableSearchConditionForIds(Collection<?> collIds) {
		if (CollectionUtils.isNullOrEmpty(collIds)) {
			CompositeCollectableSearchCondition alwaysFalseCondition = SearchConditionUtils.not(TrueCondition.TRUE);
			alwaysFalseCondition.setConditionName(ALWAYS_FALSE_CONDITION);
			return alwaysFalseCondition;
			//throw new IllegalArgumentException("collIds");
		}

		final CompositeCollectableSearchCondition cond = new CompositeCollectableSearchCondition(LogicalOperator.OR);

		for (Object oId : collIds) {
			cond.addOperand(new CollectableIdCondition(oId));
		}
		return simplified(cond);
	}

	/**
	 * @param cond
	 * @return a condition that is syntactically simpler (if possible) but semantically equivalent to the given condition.
	 */
	public static CollectableSearchCondition simplified(CollectableSearchCondition cond) {
		return (cond == null) ? null : cond.accept(new SimplifiedVisitor());
		// @todo
		// return trueIfNull(cond).accept(new SimplifiedVisitor());
	}

	/**
	 * @param cond May be <code>null</code>.
	 * @return a semantically equivalent search condition in which the nodes in each level are sorted by this order:
	 * atomic nodes come first, ordered by their labels, composite nodes come last (unordered).
	 * @see #sorted(CollectableSearchCondition, boolean)
	 * @postcondition result == null <--> cond == null
	 */
	public static CollectableSearchCondition sortedByLabels(CollectableSearchCondition cond) {
		return sorted(cond, true);
	}

	/**
	 * @param cond May be <code>null</code>.
	 * @param bSortByLabels Sort by field labels? Otherwise sort by field names.
	 * @return a semantically equivalent search condition in which the nodes in each level are sorted by this order:
	 * atomic nodes come first, ordered by their field names/labels, composite nodes come last (unordered).
	 * @postcondition result == null <--> cond == null
	 */
	public static CollectableSearchCondition sorted(CollectableSearchCondition cond, boolean bSortByLabels) {
		return (cond == null) ? cond : cond.accept(new SortedVisitor(bSortByLabels));
	}

	/**
	 * @param cond
	 * @param predicate
	 * @param bTraverseSubConditions true: traverse subconditions/referencing conditions - false: stop in subconditions/referencing conditions
	 * @return Does the given search condition contain a node that matches the given predicate?
	 * @precondition cond != null
	 */
	public static boolean contains(CollectableSearchCondition cond, Predicate<CollectableSearchCondition> predicate,
			boolean bTraverseSubConditions) {
		return cond.accept(new ContainsVisitor(predicate, bTraverseSubConditions));
	}

	/**
	 * @param clctefPeer
	 * @param atomiccond
	 * @precondition atomiccond != null
	 * @return the equivalent search condition for the given peer
	 */
	public static AtomicCollectableSearchCondition getConditionForPeer(CollectableEntityField clctefPeer, AtomicCollectableSearchCondition cond) {
		return cond.accept(new GetAtomicConditionForPeerVisitor(clctefPeer));
	}

	/**
	 * @param atomiccond
	 * @return the negation of the the given condition.
	 * @todo generalize and make public
	 */
	public static AtomicCollectableSearchCondition negationOf(AtomicCollectableSearchCondition atomiccond) {
		return atomiccond.accept(new NegationOfAtomicSearchConditionVisitor());
	}

	/**
	 * @param prefs
	 * @return the search condition stored in prefs, if any.
	 */
	public static CollectableSearchCondition getSearchCondition(Preferences prefs, String sEntityName) throws PreferencesException {
		return PutSearchConditionToPrefsVisitor.getSearchCondition(prefs, sEntityName, DefaultCollectableEntityProvider.getInstance());
	}
	
	/**
	 * @param collOperands Collection<CollectableSearchCondition>
	 * @return Is it true that there is not more than one atomic condition for a fieldname?
	 */
	public static boolean areAtomicConditionsUnique(Collection<CollectableSearchCondition> collOperands) {
		final Collection<AtomicCollectableSearchCondition> collAtomicOperands = CollectionUtils.selectInstancesOf(collOperands, AtomicCollectableSearchCondition.class);
		return CollectionUtils.forall(collAtomicOperands, PredicateUtils.transformedInputPredicate(new GetFieldName(), PredicateUtils.<String>isUnique()));
	}

	/**
	 * gets a map of all <code>CollectableField</code>s in <code>CollectableComparison</code>s contained in <code>cond</code>.
	 * @param cond
	 * @return Map<String sFieldName, CollectableField clctfValue>
	 * @postcondition result != null
	 */
	public static Map<String, CollectableField> getAtomicFieldsMap(CollectableSearchCondition cond) {
		return SearchConditionUtils.trueIfNull(cond).accept(new GetAtomicFieldsMapVisitor());
	}

	/**
	 * Predicate: Does a given <code>CollectableSearchCondition</code> have the given type?
	 */
	public static class HasType implements Predicate<CollectableSearchCondition> {
		private final int iType;

		public HasType(int iType) {
			this.iType = iType;
		}

		@Override
		public boolean evaluate(CollectableSearchCondition cond) {
			return cond.getType() == this.iType;
		}
	}	// inner class HasType

	/**
	 * Transformer: Gets the field name of a given <code>AtomicCollectableSearchCondition</code>.
	 */
	public static class GetFieldName implements Transformer<AtomicCollectableSearchCondition, String> {
		@Override
		public String transform(AtomicCollectableSearchCondition o) {
			return o.getFieldName();
		}
	}	// inner class GetFieldName

	/**
	 * inner class NegationOfAtomicSearchConditionVisitor
	 */
	private static class NegationOfAtomicSearchConditionVisitor implements AtomicVisitor<AtomicCollectableSearchCondition, RuntimeException> {

		@Override
		public AtomicCollectableSearchCondition visitComparison(CollectableComparison comparison) {
			CollectableComparison res = new CollectableComparison(comparison.getEntityField(), getComplementalOperator(comparison), comparison.getComparand());
			res.setConditionName(comparison.getConditionName());
			return res;
		}

		@Override
		public AtomicCollectableSearchCondition visitComparisonWithParameter(CollectableComparisonWithParameter comparisonwp) {
			CollectableComparisonWithParameter res = new CollectableComparisonWithParameter(comparisonwp.getEntityField(), getComplementalOperator(comparisonwp), comparisonwp.getParameter());
			res.setConditionName(comparisonwp.getConditionName());
			return res;
		}

		@Override
		public AtomicCollectableSearchCondition visitComparisonWithOtherField(CollectableComparisonWithOtherField comparisonwf) {
			CollectableComparisonWithOtherField res = new CollectableComparisonWithOtherField(comparisonwf.getEntityField(), getComplementalOperator(comparisonwf), comparisonwf.getOtherField());
			res.setConditionName(comparisonwf.getConditionName());
			return res;
		}

		@Override
		public AtomicCollectableSearchCondition visitIsNullCondition(CollectableIsNullCondition isnullcond) {
			CollectableIsNullCondition res = new CollectableIsNullCondition(isnullcond.getEntityField(), getComplementalOperator(isnullcond));
			res.setConditionName(isnullcond.getConditionName());
			return res;
		}

		@Override
		public AtomicCollectableSearchCondition visitLikeCondition(CollectableLikeCondition likecond) {
			CollectableLikeCondition res = new CollectableLikeCondition(likecond.getEntityField(), getComplementalOperator(likecond), likecond.getLikeComparand());
			res.setConditionName(likecond.getConditionName());
			return res;
		}

		private static ComparisonOperator getComplementalOperator(AtomicCollectableSearchCondition atomiccond) {
			return ComparisonOperator.complement(atomiccond.getComparisonOperator());
		}

	}	// NegationOfAtomicSearchConditionVisitor

	/**
	 * inner class SortedVisitor
	 */
	private static class SortedVisitor implements Visitor<CollectableSearchCondition, RuntimeException>, CompositeVisitor<CollectableSearchCondition, RuntimeException> {

		private final boolean bSortByLabels;

		SortedVisitor(boolean bSortByLabels) {
			this.bSortByLabels = bSortByLabels;
		}

		@Override
		public CollectableSearchCondition visitTrueCondition(TrueCondition truecond) throws RuntimeException {
			// there is nothing to sort. We can return the condition itself as it is immutable.
			return truecond;
		}

		@Override
		public CollectableSearchCondition visitAtomicCondition(AtomicCollectableSearchCondition atomiccond) {
			// there is nothing to sort. We can return the condition itself as it is immutable.
			return atomiccond;
		}

		@Override
		public CollectableSearchCondition visitCompositeCondition(CompositeCollectableSearchCondition compositecond) {
			CompositeCollectableSearchCondition res = new CompositeCollectableSearchCondition(compositecond.getLogicalOperator(),
					CollectionUtils.sorted(
							CollectionUtils.transform(compositecond.getOperands(), new Sorted(bSortByLabels)),
							new CompareByFieldNameOrLabel(bSortByLabels)));
			res.setConditionName(compositecond.getConditionName());
			return res;
		}

		@Override
		public CollectableSearchCondition visitIdCondition(CollectableIdCondition idcond) {
			// there is nothing to sort. We can return the condition itself as it is immutable.
			return idcond;
		}

		@Override
		public CollectableSearchCondition visitSubCondition(CollectableSubCondition subcond) {
			CollectableSubCondition res = new CollectableSubCondition(subcond.getSubEntityName(), subcond.getForeignKeyFieldName(),
					sorted(subcond.getSubCondition(), bSortByLabels));
			res.setConditionName(subcond.getConditionName());
			return res;
		}

		@Override
		public CollectableSearchCondition visitPivotJoinCondition(PivotJoinCondition joincond) {
			// do noting
			return joincond;
		}

		@Override
		public CollectableSearchCondition visitSelfSubCondition(CollectableSelfSubCondition subcond) {
			CollectableSelfSubCondition res = new CollectableSelfSubCondition(subcond.getForeignKeyFieldName(),
					sorted(subcond.getSubCondition(), bSortByLabels), subcond.getSubEntityName());
			res.setConditionName(subcond.getConditionName());
			return res;
		}

		@Override
		public CollectableSearchCondition visitPlainSubCondition(PlainSubCondition subcond) {
			//return new PlainSubCondition(subcond.getPlainSQL(), subcond.getConditionName());
			return subcond;
		}

		@Override
		public CollectableSearchCondition visitReferencingCondition(ReferencingCollectableSearchCondition refcond) {
			ReferencingCollectableSearchCondition res = new ReferencingCollectableSearchCondition(refcond.getReferencingField(),
					sorted(refcond.getSubCondition(), bSortByLabels));
			res.setConditionName(refcond.getConditionName());
			return res;
		}

		@Override
        public CollectableSearchCondition visitIdListCondition(CollectableIdListCondition collectableIdListCondition) throws RuntimeException {
	        return collectableIdListCondition;
        }

	}	 // inner class SortedVisitor

	/**
	 * Comparator: CompareByFieldNameOrLabel
	 */
	private static class CompareByFieldNameOrLabel implements Comparator<CollectableSearchCondition> {

		private final boolean bSortByLabels;

		CompareByFieldNameOrLabel(boolean bSortByLabels) {
			this.bSortByLabels = bSortByLabels;
		}

		@Override
		public int compare(CollectableSearchCondition cond1, final CollectableSearchCondition cond2) {
			final int result;
			final int iTypeDiff = cond1.getType() - cond2.getType();

			if (iTypeDiff != 0) {
				result = iTypeDiff;
			}
			else {
				assert cond1.getType() == cond2.getType();

				result = cond1.accept(new CompareByFieldNameOrLabelAtomicVisitor(cond2, bSortByLabels));
			}
			return result;
		}

	}	// inner class CompareByFieldNameOrLabel

	/**
	 * Transformer: Sorted
	 */
	private static class Sorted implements Transformer<CollectableSearchCondition, CollectableSearchCondition> {

		private final boolean bSortByLabels;

		Sorted(boolean bSortByLabels) {
			this.bSortByLabels = bSortByLabels;
		}

		@Override
		public CollectableSearchCondition transform(CollectableSearchCondition cond) {
			return sorted(cond, bSortByLabels);
		}

	} // inner class Sorted

	/**
	 * Visitor returning an equivalent atomic condition for a given entity field.
	 */
	private static class GetAtomicConditionForPeerVisitor implements AtomicVisitor<AtomicCollectableSearchCondition, RuntimeException> {

		private final CollectableEntityField clctefPeer;

		GetAtomicConditionForPeerVisitor(CollectableEntityField clctefPeer) {
			this.clctefPeer = clctefPeer;
		}

		@Override
		public AtomicCollectableSearchCondition visitComparison(CollectableComparison comparison) {
			return new CollectableComparison(clctefPeer, comparison.getComparisonOperator(), comparison.getComparand());
		}

		@Override
		public AtomicCollectableSearchCondition visitComparisonWithParameter(CollectableComparisonWithParameter comparisonwp) {
			return new CollectableComparisonWithParameter(clctefPeer, comparisonwp.getComparisonOperator(), comparisonwp.getParameter());
		}
		
		@Override
		public AtomicCollectableSearchCondition visitComparisonWithOtherField(CollectableComparisonWithOtherField comparisonwf) {
			return new CollectableComparisonWithOtherField(clctefPeer, comparisonwf.getComparisonOperator(), comparisonwf.getOtherField());
		}

		@Override
		public AtomicCollectableSearchCondition visitIsNullCondition(CollectableIsNullCondition isnullcond) {
			return new CollectableIsNullCondition(clctefPeer, isnullcond.getComparisonOperator());
		}

		@Override
		public AtomicCollectableSearchCondition visitLikeCondition(CollectableLikeCondition likecond) {
			return new CollectableLikeCondition(clctefPeer, likecond.getComparisonOperator(), likecond.getLikeComparand());
		}
	}	// inner class GetAtomicConditionForPeerVisitor
	
}	// class SearchConditionUtils
