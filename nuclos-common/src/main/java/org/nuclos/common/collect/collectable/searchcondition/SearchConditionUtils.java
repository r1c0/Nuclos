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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableEntityProvider;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition.AtomicVisitor;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparisonWithParameter.ComparisonParameter;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition.CompositeVisitor;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition.Visitor;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.PredicateUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.StringUtils;
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

	private static final String PREFS_KEY_TYPE = "type";
	private static final String PREFS_KEY_FIELDNAME = "fieldName";
	private static final String PREFS_KEY_PARAMETER_COMPARAND = "parameterComparand";
	private static final String PREFS_KEY_FIELDNAME_COMPARAND = "fieldNameComparand";
	private static final String PREFS_KEY_COMPARISON_OPERATOR = "comparisonOperator";
	private static final String PREFS_KEY_LOGICAL_OPERATOR = "logicalOperator";
	private static final String PREFS_KEY_LIKE_COMPARAND = "likeComparand";
	private static final String PREFS_NODE_COMPARAND = "comparand";
	private static final String PREFS_NODE_COMPOSITESEARCHCONDITION = "compositeSearchCondition";
	private static final String PREFS_KEY_ENTITYNAME = "entity";
	private static final String PREFS_KEY_FOREIGNKEYFIELDNAME = "foreignKeyField";
	private static final String PREFS_NODE_SUBCONDITION = "subCondition";
	private static final String PREFS_KEY_ID = "id";
	//private static final String PREFS_KEY_PLAINSUBCONDITION_SQL = "plainSearchConditionSQL";
	private static final String PREFS_KEY_PLAINSUBCONDITION_NAME = "plainSearchConditionName";
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
	private static AtomicCollectableSearchCondition negationOf(AtomicCollectableSearchCondition atomiccond) {
		return atomiccond.accept(new NegationOfAtomicSearchConditionVisitor());
	}

	/**
	 * inner class PreferencesIO: specifies how to read/write a collectable search condition
	 * from/to the preferences.
	 */
	private static class PreferencesIO implements PreferencesUtils.PreferencesIO<CollectableSearchCondition> {
		private final String sEntityName;
		private final CollectableEntityProvider clcteprovider;

		public PreferencesIO(String sEntityName, CollectableEntityProvider clcteprovider) {
			this.sEntityName = sEntityName;
			this.clcteprovider = clcteprovider;
		}

		@Override
		public CollectableSearchCondition get(Preferences prefs) throws PreferencesException {
			return getSearchCondition(prefs, this.sEntityName, clcteprovider);
		}

		@Override
		public void put(Preferences prefs, CollectableSearchCondition cond) throws PreferencesException {
			putSearchCondition(prefs, cond);
		}

	}	// inner class PreferencesIO

	/**
	 * @param prefs
	 * @return the search condition stored in prefs, if any.
	 */
	public static CollectableSearchCondition getSearchCondition(Preferences prefs, String sEntityName) throws PreferencesException {
		return getSearchCondition(prefs, sEntityName, DefaultCollectableEntityProvider.getInstance());
	}
	
	@SuppressWarnings("deprecation")
	public static CollectableSearchCondition getSearchCondition(Preferences prefs, String sEntityName, CollectableEntityProvider clcteprovider)
			throws PreferencesException {
		final CollectableSearchCondition result;

		final int iType = prefs.getInt(PREFS_KEY_TYPE, CollectableSearchCondition.TYPE_UNDEFINED);
		/** @todo replace switch statement with Strategy */
		switch (iType) {
			case CollectableSearchCondition.TYPE_ATOMIC:
				result = getAtomicSearchCondition(prefs, clcteprovider, sEntityName);
				break;
			case CollectableSearchCondition.TYPE_COMPOSITE:
				result = getCompositeSearchCondition(prefs, clcteprovider, sEntityName);
				break;
			case CollectableSearchCondition.TYPE_SUB:
				if(isPlainSubCondition(prefs)){
					result = getPlainSubCondition(prefs);
				} else {
					result = getSubCondition(prefs, clcteprovider);
				}
				break;
			case CollectableSearchCondition.TYPE_REFERENCING:
				result = getReferencingCondition(prefs, clcteprovider, sEntityName);
				break;
			case CollectableSearchCondition.TYPE_ID:
				result = getIdCondition(prefs);
				break;
			default:
				// no searchcondition at all
				result = null;
		}
		return result;
	}

	private static boolean isPlainSubCondition(Preferences prefs) throws PreferencesException {
		return PreferencesUtils.getSerializable(prefs, PREFS_KEY_PLAINSUBCONDITION_NAME) != null;
	}

	/**
	 * @param prefs
	 * @param clcteprovider
	 * @param sEntityName
	 * @return
	 * @throws PreferencesException
	 * @todo This method is duplicated in CollectableTextComponentHelper and AtomicNodeController - try to merge
	 */
	@SuppressWarnings("deprecation")
	private static AtomicCollectableSearchCondition getAtomicSearchCondition(Preferences prefs,
			CollectableEntityProvider clcteprovider, String sEntityName) throws PreferencesException {
		final AtomicCollectableSearchCondition result;

		final String sFieldName = prefs.get(PREFS_KEY_FIELDNAME, null);
		final CollectableEntity clcte = clcteprovider.getCollectableEntity(sEntityName);
		final CollectableEntityField clctef = clcte.getEntityField(sFieldName);
		final int iComparisonOperator = prefs.getInt(PREFS_KEY_COMPARISON_OPERATOR, ComparisonOperator.INT_UNDEFINED);
		final ComparisonOperator compop = ComparisonOperator.getInstance(iComparisonOperator);

		switch (compop) {
			case IS_NULL:
			case IS_NOT_NULL:
				result = new CollectableIsNullCondition(clctef, compop);
				break;
			case LIKE:
			case NOT_LIKE:
				final String sLikeComparand = prefs.get(PREFS_KEY_LIKE_COMPARAND, null);
				result = new CollectableLikeCondition(clctef, compop, StringUtils.emptyIfNull(sLikeComparand));
				break;
			default:
				final CollectableField clctfComparand = CollectableUtils.getCollectableField(prefs, PREFS_NODE_COMPARAND);
				if (clctfComparand == null) {
					// comparison with other field or parameter
					String parameter = prefs.get(PREFS_KEY_PARAMETER_COMPARAND, null);
					if (parameter != null) {
						result = new CollectableComparisonWithParameter(clctef, compop, ComparisonParameter.parse(parameter));
					} else {
						final String sOtherFieldName = prefs.get(PREFS_KEY_FIELDNAME_COMPARAND, null);
						result = new CollectableComparisonWithOtherField(clctef, compop, clcte.getEntityField(sOtherFieldName));
					}
				}
				else if (clctfComparand.isNull() && (compop == ComparisonOperator.EQUAL)) {
					// This is for compatibility reasons: It used to be possible to specify a CollectableComparison with null comparand.
					result = new CollectableIsNullCondition(clctef);
				}
				else {
					result = new CollectableComparison(clctef, compop, clctfComparand);
				}
		}

		return result;
	}

	private static CompositeCollectableSearchCondition getCompositeSearchCondition(Preferences prefs, CollectableEntityProvider clcteprovider, String sEntityName) throws PreferencesException {
		final int iLogicalOperator = prefs.getInt(PREFS_KEY_LOGICAL_OPERATOR, CompositeCollectableSearchCondition.UNDEFINED);

		final List<CollectableSearchCondition> lstOperands = PreferencesUtils.getGenericList(prefs, PREFS_NODE_COMPOSITESEARCHCONDITION,
				new PreferencesIO(sEntityName, clcteprovider));

		return new CompositeCollectableSearchCondition(LogicalOperator.getInstance(iLogicalOperator), lstOperands);
	}

	private static CollectableSubCondition getSubCondition(Preferences prefs, CollectableEntityProvider clcteprovider) throws PreferencesException {
		final String sSubEntityName = prefs.get(PREFS_KEY_ENTITYNAME, null);
		final String sForeignKeyFieldName = prefs.get(PREFS_KEY_FOREIGNKEYFIELDNAME, null);
		final CollectableSearchCondition condSub = getSearchCondition(prefs.node(PREFS_NODE_SUBCONDITION), sSubEntityName, clcteprovider);
		return new CollectableSubCondition(sSubEntityName, sForeignKeyFieldName, condSub);
	}

	private static PlainSubCondition getPlainSubCondition(Preferences prefs) throws PreferencesException {
		final String sPlainName = (String)PreferencesUtils.getSerializable(prefs, PREFS_KEY_PLAINSUBCONDITION_NAME);
		//final String sPlainSQL = (String)PreferencesUtils.getSerializable(prefs, PREFS_KEY_PLAINSUBCONDITION_SQL);
		//return new PlainSubCondition(sPlainSQL, sPlainName);
		return new PlainSubCondition(null, sPlainName);
	}

	private static CollectableSearchCondition getReferencingCondition(Preferences prefs, CollectableEntityProvider clcteprovider, String sEntityName) throws PreferencesException {
		final String sFieldName = prefs.get(PREFS_KEY_FIELDNAME, null);
		final CollectableEntityField clctefReferencing = clcteprovider.getCollectableEntity(sEntityName).getEntityField(sFieldName);
		final CollectableSearchCondition condSub = getSearchCondition(prefs.node(PREFS_NODE_SUBCONDITION), clctefReferencing.getReferencedEntityName(), clcteprovider);
		return new ReferencingCollectableSearchCondition(clctefReferencing, condSub);
	}

	private static CollectableSearchCondition getIdCondition(Preferences prefs) throws PreferencesException {
		return new CollectableIdCondition(PreferencesUtils.getSerializable(prefs, PREFS_KEY_ID));
	}

	/**
	 * writes the given searchcondition to the given preferences
	 * @param prefs
	 * @param cond may be <code>null</code>
	 */
	public static void putSearchCondition(Preferences prefs, CollectableSearchCondition cond) throws PreferencesException {
		if (cond == null) {
			prefs.remove(PREFS_KEY_TYPE);
		}
		else {
			prefs.putInt(PREFS_KEY_TYPE, cond.getType());

			cond.accept(new PutSearchConditionToPrefsVisitor(prefs));
		}
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
	 * inner class PutSearchConditionToPrefsVisitor
	 */
	private static class PutSearchConditionToPrefsVisitor implements CollectableSearchCondition.Visitor<Void, PreferencesException>, CollectableSearchCondition.CompositeVisitor<Void, RuntimeException> {
		private final Preferences prefs;

		PutSearchConditionToPrefsVisitor(Preferences prefs) {
			this.prefs = prefs;
		}

		@Override
		public Void visitTrueCondition(TrueCondition truecond) {
			throw new IllegalArgumentException("truecond");
		}

		@Override
		@SuppressWarnings("deprecation")
		public Void visitAtomicCondition(AtomicCollectableSearchCondition atomiccond) throws PreferencesException {
			prefs.put(PREFS_KEY_FIELDNAME, atomiccond.getFieldName());
			final int iComparisonOperator = atomiccond.getComparisonOperator().getIntValue();
			prefs.putInt(PREFS_KEY_COMPARISON_OPERATOR, iComparisonOperator);

			atomiccond.accept(new PutSearchConditionToPrefsAtomicVisitor(prefs));
			return null;
		}

		@Override
		public Void visitCompositeCondition(CompositeCollectableSearchCondition compositecond) throws PreferencesException {
			prefs.putInt(PREFS_KEY_LOGICAL_OPERATOR, compositecond.getLogicalOperator().getIntValue());
			PreferencesUtils.putGenericList(prefs, PREFS_NODE_COMPOSITESEARCHCONDITION, compositecond.getOperands(), new PreferencesIO(null, DefaultCollectableEntityProvider.getInstance()));
			return null;
		}

		@Override
		public Void visitIdCondition(CollectableIdCondition idcond) throws PreferencesException {
			PreferencesUtils.putSerializable(prefs, PREFS_KEY_ID, idcond.getId());
			return null;
		}

		@Override
		public Void visitReferencingCondition(ReferencingCollectableSearchCondition refcond) throws PreferencesException {
			prefs.put(PREFS_KEY_FIELDNAME, refcond.getReferencingField().getName());
			putSearchCondition(prefs.node(PREFS_NODE_SUBCONDITION), refcond.getSubCondition());
			return null;
		}

		@Override
		public Void visitSubCondition(CollectableSubCondition subcond) throws PreferencesException {
			prefs.put(PREFS_KEY_ENTITYNAME, subcond.getSubEntityName());
			prefs.put(PREFS_KEY_FOREIGNKEYFIELDNAME, subcond.getForeignKeyFieldName());
			putSearchCondition(prefs.node(PREFS_NODE_SUBCONDITION), subcond.getSubCondition());
			return null;
		}

		@Override
		public Void visitSelfSubCondition(CollectableSelfSubCondition subcond) throws RuntimeException {
			return null;
		}

		@Override
		public Void visitPlainSubCondition(PlainSubCondition subcond) throws RuntimeException {
			try {
				PreferencesUtils.putSerializable(prefs, PREFS_KEY_PLAINSUBCONDITION_NAME, subcond.getConditionName());
				//PreferencesUtils.putSerializable(prefs, PREFS_KEY_PLAINSUBCONDITION_SQL, subcond.getPlainSQL());
			} catch (PreferencesException e) {
				throw new RuntimeException(e);
			}
			return null;
		}

		@Override
        public Void visitIdListCondition(CollectableIdListCondition collectableIdListCondition) throws PreferencesException {
			PreferencesUtils.putSerializable(prefs, PREFS_KEY_ID, collectableIdListCondition.getIds());
			return null;
        }
	}	// inner class PutSearchConditionToPrefsVisitor

	/**
	 * inner class PutSearchConditionToPrefsAtomicVisitor
	 */
	private static class PutSearchConditionToPrefsAtomicVisitor implements AtomicVisitor<Void, PreferencesException> {
		private final Preferences prefs;

		PutSearchConditionToPrefsAtomicVisitor(Preferences prefs) {
			this.prefs = prefs;
		}

		@Override
		public Void visitComparison(CollectableComparison comparison) throws PreferencesException {
			CollectableUtils.putCollectableField(prefs, PREFS_NODE_COMPARAND, comparison.getComparand());
			prefs.remove(PREFS_KEY_PARAMETER_COMPARAND);
			prefs.remove(PREFS_KEY_LIKE_COMPARAND);
			prefs.remove(PREFS_KEY_FIELDNAME_COMPARAND);
			return null;
		}

		@Override
		public Void visitComparisonWithParameter(CollectableComparisonWithParameter comparisonwp) {
			prefs.put(PREFS_KEY_PARAMETER_COMPARAND, comparisonwp.getParameter().getInternalName());
			prefs.remove(PREFS_NODE_COMPARAND);
			prefs.remove(PREFS_KEY_FIELDNAME_COMPARAND);
			prefs.remove(PREFS_KEY_LIKE_COMPARAND);
			return null;
		}
		
		@Override
		public Void visitComparisonWithOtherField(CollectableComparisonWithOtherField comparisonwf) {
			prefs.put(PREFS_KEY_FIELDNAME_COMPARAND, comparisonwf.getOtherField().getName());
			prefs.remove(PREFS_NODE_COMPARAND);
			prefs.remove(PREFS_KEY_PARAMETER_COMPARAND);
			prefs.remove(PREFS_KEY_LIKE_COMPARAND);
			return null;
		}

		@Override
		public Void visitLikeCondition(CollectableLikeCondition likecond) {
			prefs.put(PREFS_KEY_LIKE_COMPARAND, likecond.getLikeComparand());
			prefs.remove(PREFS_NODE_COMPARAND);
			prefs.remove(PREFS_KEY_PARAMETER_COMPARAND);
			prefs.remove(PREFS_KEY_FIELDNAME_COMPARAND);
			return null;
		}

		@Override
		public Void visitIsNullCondition(CollectableIsNullCondition isnullcond) {
			// nothing more needs to be written
			prefs.remove(PREFS_NODE_COMPARAND);
			prefs.remove(PREFS_KEY_PARAMETER_COMPARAND);
			prefs.remove(PREFS_KEY_FIELDNAME_COMPARAND);
			prefs.remove(PREFS_KEY_LIKE_COMPARAND);
			return null;
		}

	}	// inner class PutSearchConditionToPrefsAtomicVisitor

	/**
	 * inner class GetAtomicFieldsMapVisitor
	 */
	private static class GetAtomicFieldsMapVisitor implements CollectableSearchCondition.Visitor<Map<String, CollectableField>, RuntimeException> {

		final Map<String, CollectableField> mpFields = CollectionUtils.newHashMap();

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
		public Map<String, CollectableField> visitReferencingCondition(ReferencingCollectableSearchCondition refcond) {
			// do nothing
			return mpFields;
		}

		@Override
        public Map<String, CollectableField> visitIdListCondition(CollectableIdListCondition collectableIdListCondition) throws RuntimeException {
			// do nothing
	        return mpFields;
        }

	}	// inner class GetAtomicFieldsMapVisitor

	/**
	 * inner class SimplifiedVisitor
	 */
	private static class SimplifiedVisitor implements CollectableSearchCondition.Visitor<CollectableSearchCondition, RuntimeException>, CollectableSearchCondition.CompositeVisitor<CollectableSearchCondition, RuntimeException> {

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
					getSimplifiedOperands(compositecond),
					PredicateUtils.<CollectableSearchCondition>isUnique());

			// 2. then look what is left:
			final CollectableSearchCondition result;
			if (lstSimplifiedOperands.isEmpty()) {
				// @todo replace with TrueCondition
				result = null;
			}
			else {
				final LogicalOperator logicalop = compositecond.getLogicalOperator();
				switch (logicalop) {
					case NOT:
						final CollectableSearchCondition condOperand = lstSimplifiedOperands.get(0);
						if (condOperand instanceof AtomicCollectableSearchCondition) {
							result = negationOf((AtomicCollectableSearchCondition) condOperand);
						}
						else {
							result = SearchConditionUtils.not(condOperand);
						}
						break;

					case AND:
					case OR:
						assert lstSimplifiedOperands.size() >= 1;

						if (lstSimplifiedOperands.size() == 1) {
							result = lstSimplifiedOperands.get(0);
						}
						else {
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
			ReferencingCollectableSearchCondition res = new ReferencingCollectableSearchCondition(refcond.getReferencingField(), simplified(refcond.getSubCondition()));
			res.setConditionName(refcond.getConditionName());
			return res;
		}

		@Override
		public CollectableSearchCondition visitSubCondition(CollectableSubCondition subcond) {
			CollectableSubCondition res = new CollectableSubCondition(subcond.getSubEntityName(), subcond.getForeignKeyFieldName(), simplified(subcond.getSubCondition()));
			res.setConditionName(subcond.getConditionName());
			return res;
		}

		@Override
		public CollectableSearchCondition visitSelfSubCondition(CollectableSelfSubCondition subcond) {
			CollectableSelfSubCondition res = new CollectableSelfSubCondition(subcond.getForeignKeyFieldName(), simplified(subcond.getSubCondition()), subcond.getSubEntityName());
			res.setConditionName(subcond.getConditionName());
			return res;
		}

		@Override
		public CollectableSearchCondition visitPlainSubCondition(PlainSubCondition subcond) {
			return subcond;
		}

		private static List<CollectableSearchCondition> getSimplifiedOperands(CompositeCollectableSearchCondition compositecond) {
			final List<CollectableSearchCondition> result = new LinkedList<CollectableSearchCondition>();
			for (CollectableSearchCondition condChild : compositecond.getOperands()) {
				assert condChild != null;
				final CollectableSearchCondition condSimplifiedChild = simplified(condChild);
				if (condSimplifiedChild != null) {
					boolean bChildAdded = false;
					// if the child's logical operator is the same as this' operator and the operator is associative,
					// just add the child's operands to this' operands:
					if (compositecond.getLogicalOperator().isAssociative() && condSimplifiedChild instanceof CompositeCollectableSearchCondition)
					{
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
        public CollectableSearchCondition visitIdListCondition(CollectableIdListCondition collectableIdListCondition) throws RuntimeException {
	        return collectableIdListCondition;
        }

	}	// inner class SimplifiedVisitor

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

	private static class CompareByFieldNameOrLabelAtomicVisitor implements Visitor<Integer, RuntimeException>, CompositeVisitor<Integer, RuntimeException> {

		private final CollectableSearchCondition cond2;
		private final boolean bSortByLabels;

		/**
		 * @param cond2 the second condition - must have the same type as the visited condition.
		 */
		CompareByFieldNameOrLabelAtomicVisitor(CollectableSearchCondition cond2, boolean bSortByLabels) {
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
			return LangUtils.compare(getFieldNameOrLabel(atomiccond1, bSortByLabels), getFieldNameOrLabel(atomiccond2, bSortByLabels));
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

	}	// inner class CompareByFieldNameOrLabelAtomicVisitor

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
	 * inner class ContainsVisitor
	 */
	private static class ContainsVisitor implements Visitor<Boolean, RuntimeException> {

		private final Predicate<CollectableSearchCondition> predicate;
		private final boolean bTraverseSubConditions;

		ContainsVisitor(Predicate<CollectableSearchCondition> predicate, boolean bTraverseSubConditions) {
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
			return predicate.evaluate(compositecond) ||
					CollectionUtils.exists(compositecond.getOperands(), new Contains(predicate, bTraverseSubConditions));
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
				result = (condSub != null) && contains(condSub, predicate, bTraverseSubConditions);
			}
			return result;
		}

		@Override
		public Boolean visitReferencingCondition(ReferencingCollectableSearchCondition refcond) {
			boolean result = predicate.evaluate(refcond);
			if (!result && bTraverseSubConditions) {
				final CollectableSearchCondition condSub = refcond.getSubCondition();
				result = (condSub != null) && contains(condSub, predicate, bTraverseSubConditions);
			}
			return result;
		}

		@Override
        public Boolean visitIdListCondition(CollectableIdListCondition collectableIdListCondition) throws RuntimeException {
	        return predicate.evaluate(collectableIdListCondition);
        }

	}	// inner class ContainsVisitor

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
			return contains(cond, predicate, bTraverseSubConditions);
		}

	}  // inner class Contains

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
