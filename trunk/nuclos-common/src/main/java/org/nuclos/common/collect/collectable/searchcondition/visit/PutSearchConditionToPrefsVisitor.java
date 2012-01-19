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

import java.util.List;
import java.util.prefs.Preferences;

import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableEntityProvider;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparisonWithOtherField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparisonWithParameter;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparisonWithParameter.ComparisonParameter;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdListCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIsNullCondition;
import org.nuclos.common.collect.collectable.searchcondition.PivotJoinCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableLikeCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSelfSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collect.collectable.searchcondition.PlainSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.RefJoinCondition;
import org.nuclos.common.collect.collectable.searchcondition.ReferencingCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.TrueCondition;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.PreferencesException;

/**
 * inner class PutSearchConditionToPrefsVisitor
 */
public class PutSearchConditionToPrefsVisitor implements Visitor<Void, PreferencesException>,
		CompositeVisitor<Void, RuntimeException> {

	private static final String PREFS_KEY_PLAINSUBCONDITION_NAME = "plainSearchConditionName";
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
	private static final String PREFS_NODE_JOINCONDITION = "joinCondition";
	private static final String PREFS_KEY_ID = "id";

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

	} // inner class PreferencesIO

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

	} // inner class PutSearchConditionToPrefsAtomicVisitor

	private final Preferences prefs;

	public PutSearchConditionToPrefsVisitor(Preferences prefs) {
		this.prefs = prefs;
	}

	/**
	 * writes the given searchcondition to the given preferences
	 * @param prefs
	 * @param cond may be <code>null</code>
	 */
	public static void putSearchCondition(Preferences prefs, CollectableSearchCondition cond)
			throws PreferencesException {
		if (cond == null) {
			prefs.remove(PREFS_KEY_TYPE);
		} else {
			prefs.putInt(PREFS_KEY_TYPE, cond.getType());

			cond.accept(new PutSearchConditionToPrefsVisitor(prefs));
		}
	}

	public static CollectableSearchCondition getSearchCondition(Preferences prefs, String sEntityName,
			CollectableEntityProvider clcteprovider) throws PreferencesException {

		final CollectableSearchCondition result;
		final int iType = prefs.getInt(PutSearchConditionToPrefsVisitor.PREFS_KEY_TYPE,
				CollectableSearchCondition.TYPE_UNDEFINED);
		/** @todo replace switch statement with Strategy */
		switch (iType) {
		case CollectableSearchCondition.TYPE_ATOMIC:
			result = getAtomicSearchCondition(prefs, clcteprovider, sEntityName);
			break;
		case CollectableSearchCondition.TYPE_COMPOSITE:
			result = getCompositeSearchCondition(prefs, clcteprovider, sEntityName);
			break;
		case CollectableSearchCondition.TYPE_SUB:
			if (isPlainSubCondition(prefs)) {
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
					result = new CollectableComparisonWithParameter(clctef, compop,
							ComparisonParameter.parse(parameter));
				} else {
					final String sOtherFieldName = prefs.get(PREFS_KEY_FIELDNAME_COMPARAND, null);
					result = new CollectableComparisonWithOtherField(clctef, compop,
							clcte.getEntityField(sOtherFieldName));
				}
			} else if (clctfComparand.isNull() && (compop == ComparisonOperator.EQUAL)) {
				// This is for compatibility reasons: It used to be possible to specify a CollectableComparison with null comparand.
				result = new CollectableIsNullCondition(clctef);
			} else {
				result = new CollectableComparison(clctef, compop, clctfComparand);
			}
		}

		return result;
	}

	private static CompositeCollectableSearchCondition getCompositeSearchCondition(Preferences prefs,
			CollectableEntityProvider clcteprovider, String sEntityName) throws PreferencesException {
		final int iLogicalOperator = prefs.getInt(PREFS_KEY_LOGICAL_OPERATOR,
				CompositeCollectableSearchCondition.UNDEFINED);

		final List<CollectableSearchCondition> lstOperands = PreferencesUtils.getGenericList(prefs,
				PREFS_NODE_COMPOSITESEARCHCONDITION, new PreferencesIO(sEntityName, clcteprovider));

		return new CompositeCollectableSearchCondition(LogicalOperator.getInstance(iLogicalOperator), lstOperands);
	}

	private static CollectableSubCondition getSubCondition(Preferences prefs, CollectableEntityProvider clcteprovider)
			throws PreferencesException {
		final String sSubEntityName = prefs.get(PREFS_KEY_ENTITYNAME, null);
		final String sForeignKeyFieldName = prefs.get(PREFS_KEY_FOREIGNKEYFIELDNAME, null);
		final CollectableSearchCondition condSub = getSearchCondition(prefs.node(PREFS_NODE_SUBCONDITION),
				sSubEntityName, clcteprovider);
		return new CollectableSubCondition(sSubEntityName, sForeignKeyFieldName, condSub);
	}

	private static PlainSubCondition getPlainSubCondition(Preferences prefs) throws PreferencesException {
		final String sPlainName = (String) PreferencesUtils.getSerializable(prefs, PREFS_KEY_PLAINSUBCONDITION_NAME);
		//final String sPlainSQL = (String)PreferencesUtils.getSerializable(prefs, PREFS_KEY_PLAINSUBCONDITION_SQL);
		//return new PlainSubCondition(sPlainSQL, sPlainName);
		return new PlainSubCondition(null, sPlainName);
	}

	private static CollectableSearchCondition getReferencingCondition(Preferences prefs,
			CollectableEntityProvider clcteprovider, String sEntityName) throws PreferencesException {
		final String sFieldName = prefs.get(PREFS_KEY_FIELDNAME, null);
		final CollectableEntityField clctefReferencing = clcteprovider.getCollectableEntity(sEntityName)
				.getEntityField(sFieldName);
		final CollectableSearchCondition condSub = getSearchCondition(prefs.node(PREFS_NODE_SUBCONDITION),
				clctefReferencing.getReferencedEntityName(), clcteprovider);
		return new ReferencingCollectableSearchCondition(clctefReferencing, condSub);
	}

	private static CollectableSearchCondition getIdCondition(Preferences prefs) throws PreferencesException {
		return new CollectableIdCondition(PreferencesUtils.getSerializable(prefs, PREFS_KEY_ID));
	}

	@Override
	public Void visitTrueCondition(TrueCondition truecond) {
		throw new IllegalArgumentException("truecond");
	}

	@Override
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
		PreferencesUtils.putGenericList(prefs, PREFS_NODE_COMPOSITESEARCHCONDITION, compositecond.getOperands(),
				new PreferencesIO(null, DefaultCollectableEntityProvider.getInstance()));
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
	public Void visitPivotJoinCondition(PivotJoinCondition joincond) throws PreferencesException {
		PreferencesUtils.putSerializableObjectXML(prefs, PREFS_NODE_JOINCONDITION, joincond);
		return null;
	}

	@Override
	public Void visitRefJoinCondition(RefJoinCondition joincond) throws PreferencesException {
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
	
} // class PutSearchConditionToPrefsVisitor
