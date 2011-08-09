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
package org.nuclos.server.autosync;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparisonWithOtherField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparisonWithParameter;
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
import org.nuclos.common.collect.collectable.searchcondition.PlainSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.ReferencingCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.TrueCondition;
import org.nuclos.common.collect.collectable.searchcondition.visit.AtomicVisitor;
import org.nuclos.common.collect.collectable.searchcondition.visit.CompositeVisitor;
import org.nuclos.common.collect.collectable.searchcondition.visit.Visitor;
import org.nuclos.common.collection.BinaryPredicate;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.PredicateUtils;
import org.nuclos.common.collection.SymmetricBinaryPredicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.NuclosEntity;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

public class SystemDataCache {
	
	private final NuclosEntity entity;
	private final Map<Object, MasterDataVO> elements;
	
	public SystemDataCache(NuclosEntity entity) {
		this.entity = entity;
		this.elements = new HashMap<Object, MasterDataVO>();
	}
	
	protected void addAll(Iterable<MasterDataVO> mdvos) {		
		for (MasterDataVO mdvo : mdvos) {
			Object id = mdvo.getId();
			if (id == null || elements.containsKey(id))
				throw new IllegalArgumentException("Invalid/duplicate id " + id + " for system entity " + entity.getEntityName());
			elements.put(id, mdvo);
		}
	}
	
	public Collection<MasterDataVO> getAll() {
		return Collections.unmodifiableCollection(elements.values());
	}

	public Set<Object> getAllIds() {
		return Collections.unmodifiableSet(elements.keySet());
	}

	public MasterDataVO getById(Object id) {
		return elements.get(id);
	}
	
	public MasterDataVO findVO(String field, Object value) {
		return CollectionUtils.findFirst(elements.values(), new FieldEqualsPredicate(field, value));
	}
	
	public MasterDataVO findVO(String field1, Object value1, Object...opt) {
		return CollectionUtils.findFirst(elements.values(), makeFieldEqualsPredicate(field1, value1, opt));
	}
	
	public List<MasterDataVO> findAllVO(String field1, Object value1) {
		return findAllVO(new FieldEqualsPredicate(field1, value1));
	}	
	
	public List<MasterDataVO> findAllVO(String field1, Object value1, Object...opt) {
		return findAllVO(makeFieldEqualsPredicate(field1, value1, opt));
	}
	
	@SuppressWarnings("unchecked")
	private static Predicate<MasterDataVO> makeFieldEqualsPredicate(String field, Object value, Object[] opt) {		
		Predicate<MasterDataVO>[] predicates = new Predicate[1 + (opt.length / 2)]; 
		predicates[0] = new FieldEqualsPredicate(field, value);
		for (int i = 0, k = 1; i < opt.length; i += 2) {
			predicates[k++] = new FieldEqualsPredicate((String) opt[i], opt[i + 1]); 
		}
		return PredicateUtils.and(predicates);
	}
	
	public List<MasterDataVO> findAllVOIn(String field1, Collection<?> col1) {
		return findAllVO(new FieldInPredicate(field1, col1));
	}	
	
	// TODO: clone to restrict manipulation
	public List<MasterDataVO> findAllVO(Predicate<MasterDataVO> predicate) {
		return CollectionUtils.<MasterDataVO>applyFilter(elements.values(), predicate);
	}
	
	public Collection<MasterDataVO> findAllVO(CollectableSearchCondition cond) {
		if (cond == null)
			return getAll();
		return findAllVO(cond.accept(new ConditionToPredicateVisitor(null)));
	}
	
	private static class FieldEqualsPredicate implements Predicate<MasterDataVO> {
		
		private final String	field;
		private final Object value;

		public FieldEqualsPredicate(String field, Object value) {
			this.field = field;
			this.value = value;
		}
		
		@Override
		public boolean evaluate(MasterDataVO mdvo) {
			return ObjectUtils.equals(mdvo.getField(field), value);
		}
	}
	
	private static class FieldPredicate<T> implements Predicate<MasterDataVO> {

		private final String	field;
		private final Class<T> cls;
		private final Predicate<? super T>	predicate;

		public FieldPredicate(String field, Class<T> cls, Predicate<? super T> predicate) {
			this.field = field;
			this.cls = cls;
			this.predicate = predicate;
		}
		
		@Override
		public boolean evaluate(MasterDataVO mdvo) {
			return predicate.evaluate(mdvo.getField(field, cls));
		}
	}
	
	static class MdvoPredicate<T> implements Predicate<MasterDataVO> {

		private final Transformer<? super MasterDataVO, T> transformer;
		private final Predicate<? super T>	predicate;

		public MdvoPredicate(Transformer<? super MasterDataVO, T> transformer, Predicate<? super T> predicate) {
			this.transformer = transformer;
			this.predicate = predicate;
		}
		
		@Override
		public boolean evaluate(MasterDataVO mdvo) {
			return predicate.evaluate(transformer.transform(mdvo));
		}
	}

	static class ComparisonOperatorPredicate<T> implements SymmetricBinaryPredicate<T> {

		protected final ComparisonOperator operator;
		protected final Class<T> clazz;
		protected final Comparator<? super T> comparator;
		
		public ComparisonOperatorPredicate(ComparisonOperator operator, Class<T> clazz, Comparator<? super T> comparator) {
			this.operator = operator;
			this.clazz = clazz;
			this.comparator = comparator;
		}
		
		@Override
		public boolean evaluate(T t1, T t2) {
			if (t1 == null || t2 == null)
				return false;
			switch (operator) {
			case EQUAL:
				return t1.equals(t2);
			case NOT_EQUAL:
				return !t1.equals(t2);					
			case GREATER:
				return compare(t1, t2, comparator) > 0;
			case GREATER_OR_EQUAL:
				return compare(t1, t2, comparator) >= 0;
			case LESS:
				return compare(t1, t2, comparator) < 0;
			case LESS_OR_EQUAL:
				return compare(t1, t2, comparator) <= 0;
			}
			throw new IllegalStateException("Invalid comparison operator " + operator);
		}
		
		@SuppressWarnings("unchecked")
		private static <T> int compare(T t1, T t2, Comparator<? super T> comparator) {
			return (comparator != null) ? comparator.compare(t1, t2) : ((Comparable) t1).compareTo(t2);
		}
	}
	
	private static class FieldIdPredicate implements Predicate<MasterDataVO> {
		
		private final Object id;

		public FieldIdPredicate(Object id) {
			this.id = id;
		}
		
		@Override
		public boolean evaluate(MasterDataVO mdvo) {
			return ObjectUtils.equals(mdvo.getId(), id);
		}
	}
	
	private static class FieldInPredicate implements Predicate<MasterDataVO> {
		
		private final String	field;
		private final Collection<?> values;

		public FieldInPredicate(String field, Collection<?> values) {
			this.field = field;
			this.values = values;
		}
		
		@Override
		public boolean evaluate(MasterDataVO mdvo) {
			return values.contains(mdvo.getField(field));
		}
	}
	
	static class ConditionToPredicateVisitor implements Visitor<Predicate<MasterDataVO>, RuntimeException>, CompositeVisitor<Predicate<MasterDataVO>, RuntimeException>, Transformer<CollectableSearchCondition, Predicate<MasterDataVO>> {
		
		ConditionToPredicateVisitor(MasterDataMetaVO mdmetavo) {
			// parameter is used until now
		}

		@Override
		public Predicate<MasterDataVO> visitTrueCondition(TrueCondition truecond) {
			return PredicateUtils.alwaysTrue();
		}
		
		@Override
		public Predicate<MasterDataVO> visitAtomicCondition(AtomicCollectableSearchCondition atomiccond) throws RuntimeException {
			// Note that we call the overloaded method accept(AtomicThingy...) which will then call one
			// of the specialized visit from the AtomicVisitor interface.
			return atomiccond.accept(new AtomicConditionToPredicateVisitor());
		}

		@Override
		public Predicate<MasterDataVO> visitCompositeCondition(CompositeCollectableSearchCondition compositecond) {
			@SuppressWarnings("unchecked")
			Predicate<MasterDataVO>[] operands = CollectionUtils.transform(compositecond.getOperands(), this).toArray(new Predicate[0]);
			switch (compositecond.getLogicalOperator()) {
			case NOT:
				if (operands.length != 1) {
					throw new IllegalArgumentException("mdsearch.unparser.error.invalid.condition");
				}
				return PredicateUtils.not(operands[0]);
			case AND:
				return PredicateUtils.and(operands);
			case OR:
				return PredicateUtils.or(operands);
			default:
				throw new IllegalArgumentException("Illegal logical operator " + compositecond.getLogicalOperator());
			}
		}

		@Override
		public Predicate<MasterDataVO> visitIdCondition(CollectableIdCondition idcond) {
			return new FieldIdPredicate(idcond.getId());
		}

		@Override
		public Predicate<MasterDataVO> visitSubCondition(CollectableSubCondition subcond) {
			throw new IllegalArgumentException("subcond");
		}

		@Override
		public Predicate<MasterDataVO> visitPivotJoinCondition(PivotJoinCondition joincond) {
			throw new IllegalArgumentException("joincond");
		}

		@Override
		public Predicate<MasterDataVO> visitReferencingCondition(ReferencingCollectableSearchCondition refcond) {
			throw new IllegalArgumentException("refcond");
		}
		
		//
		// CompositeVisitor
		//
		
		@Override
		public Predicate<MasterDataVO> visitPlainSubCondition(PlainSubCondition subcond) {
			throw new IllegalArgumentException("Plain SQL sub queries are not supported for system data");
		}
		
		@Override
		public Predicate<MasterDataVO> visitSelfSubCondition(CollectableSelfSubCondition subcond) {
			throw new IllegalArgumentException("Self-sub queries are not supported for system data");
		}

		@Override
		public Predicate<MasterDataVO> transform(CollectableSearchCondition cond) {
			return cond.accept(this);
		}

		@Override
        public Predicate<MasterDataVO> visitIdListCondition(CollectableIdListCondition collectableIdListCondition) throws RuntimeException {
	        throw new IllegalArgumentException("collectableIdListCondition");
        }
	}

	static class AtomicConditionToPredicateVisitor implements AtomicVisitor<Predicate<MasterDataVO>, RuntimeException>{
		
		@Override
		@SuppressWarnings("unchecked")
		public Predicate<MasterDataVO> visitComparison(CollectableComparison comparison) {
			CollectableEntityField entityField = comparison.getEntityField();
			CollectableField comparand = comparison.getComparand();
			final Object id = comparand.isIdField() ? comparand.getValueId() : null;			
			if (id != null) { /* && operator == EQUALS, but the SQL unparser doesn't check either */ 
				return new FieldPredicate<Object>(entityField.getName() + "Id", Object.class, PredicateUtils.isEqual(id));
			} else {
				if (comparand.getValue() == null) {
					// We're are simulating SQL semantics where NULL comparisons yield always false.
					return PredicateUtils.alwaysFalse();
				}
				Class<?> javaClass = entityField.getJavaClass();
				BinaryPredicate pred = new ComparisonOperatorPredicate(comparison.getComparisonOperator(), javaClass, null);
				return new FieldPredicate(entityField.getName(), javaClass, PredicateUtils.bindSecond(pred,comparand.getValue()));
			}
		}
		
		@Override
		public Predicate<MasterDataVO> visitComparisonWithParameter(CollectableComparisonWithParameter comparisonwp) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public Predicate<MasterDataVO> visitComparisonWithOtherField(CollectableComparisonWithOtherField comparisonwf) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public Predicate<MasterDataVO> visitIsNullCondition(CollectableIsNullCondition isnullcond) {
			return new FieldPredicate<Object>(isnullcond.getFieldName(), Object.class, PredicateUtils.isNull());
		}
		
		@Override
		public Predicate<MasterDataVO> visitLikeCondition(CollectableLikeCondition likecond) {
			throw new UnsupportedOperationException();
		}
	}
}
