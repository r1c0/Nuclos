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
package org.nuclos.server.dal.processor.jdbc.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableSorting;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparisonWithOtherField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparisonWithParameter;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdListCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIsNullCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableLikeCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSelfSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.PivotJoinCondition;
import org.nuclos.common.collect.collectable.searchcondition.PlainSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.RefJoinCondition;
import org.nuclos.common.collect.collectable.searchcondition.ReferencingCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.TrueCondition;
import org.nuclos.common.collect.collectable.searchcondition.visit.AtomicVisitor;
import org.nuclos.common.collect.collectable.searchcondition.visit.CompositeVisitor;
import org.nuclos.common.collect.collectable.searchcondition.visit.Visitor;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.PivotInfo;
import org.nuclos.common.dblayer.JoinType;
import org.nuclos.common2.RelativeDate;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.SessionUtils;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.processor.jdbc.TableAliasSingleton;
import org.nuclos.server.dblayer.EntityObjectMetaDbHelper;
import org.nuclos.server.dblayer.query.DbColumnExpression;
import org.nuclos.server.dblayer.query.DbCompoundColumnExpression;
import org.nuclos.server.dblayer.query.DbCondition;
import org.nuclos.server.dblayer.query.DbExpression;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbJoin;
import org.nuclos.server.dblayer.query.DbOrder;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.dblayer.query.DbSelection;

public class EOSearchExpressionUnparser {

	private static final Logger LOG = Logger.getLogger(EOSearchExpressionUnparser.class);
	
	private final Set<String> pivotTableAliases = new HashSet<String>();

	private final DbQueryBuilder queryBuilder;
	private final DbQuery<?> query;
	private DbFrom table;
	private final EntityMetaDataVO entity;

	public EOSearchExpressionUnparser(DbQuery<?> query, EntityMetaDataVO entityMeta) {
		this.entity = entityMeta;
		this.query = query;
		this.queryBuilder = query.getBuilder();
		this.table = CollectionUtils.getFirst(query.getRoots());
	}

	public void unparseSearchCondition(CollectableSearchCondition clctcond) {
		if (clctcond != null) {
			if (!clctcond.isSyntacticallyCorrect()) {
				throw new IllegalArgumentException("mdsearch.unparser.error.invalid.expression");
			}

			DbCondition condition = clctcond.accept(new UnparseVisitor());
			query.where(condition);
		}
	}

	public void unparseSortingOrder(final List<CollectableSorting> sorting) {
		final List<DbOrder> orderList = new ArrayList<DbOrder>();
		boolean containsId = false;
		if (sorting != null) {
			for (CollectableSorting cs : sorting) {
				DbColumnExpression<?> column = getDbColumn(cs);
				orderList.add(cs.isAscending() ? queryBuilder.asc(column) : queryBuilder.desc(column));
				if (column.getColumnName().equalsIgnoreCase("INTID"))
					containsId = true;
			}
		}
		if (!containsId) {
			orderList.add(queryBuilder.asc(table.baseColumn("INTID", Integer.class)));
		}
		query.orderBy(orderList);
	}

	private DbColumnExpression<?> getDbColumn(CollectableSorting sort) {
		final EntityFieldMetaDataVO entityField = MetaDataServerProvider.getInstance().getEntityField(
				sort.getEntity(), sort.getFieldName());
		final String calcFunction = entityField.getCalcFunction();
		final Class<?> type = normalizeJavaType(entityField.getDataType());
		final DbColumnExpression<?> result;
		if (sort.isBaseEntity()) {
			if (calcFunction != null) {
				final List<DbSelection<Object>> selects = table.getQuery().getSelections();
				// only INTID in SELECT part of SQL query
				if (selects.size() == 1) {
					// we must add the sorting function to the SELECT part in order to get ORDER BY to work
					final DbSelection<Object> select = (DbSelection<Object>) table.getQuery().getBuilder().plainExpression(type, 
							calcFunction + "(" + table.getAlias() + ".INTID) " + entityField.getDbColumn());
					selects.add(select);
				}
				// no tableAlias, only select alias (i.e. unqualified)...
				result = table.column(null, entityField.getDbColumn(), type);
			}
			else if (entity.isDynamic()) {
				result = table.baseColumnCaseSensitive(entityField.getDbColumn(), type);
			}
			else {
				result = table.baseColumn(entityField.getDbColumn(), type);
			}
		}
		else {
			if (calcFunction != null) {
				throw new IllegalStateException();
			}
			else {
				result = table.column(sort.getTableAlias(), entityField.getDbColumn(), type);
			}
		}
		return result;
	}

	private class UnparseVisitor implements Visitor<DbCondition, RuntimeException>, CompositeVisitor<DbCondition, RuntimeException>, AtomicVisitor<DbCondition, RuntimeException> {

		UnparseVisitor() {
		}

		@Override
		public DbCondition visitTrueCondition(TrueCondition truecond) {
			return queryBuilder.alwaysTrue();
		}

		@Override
		public DbCondition visitAtomicCondition(AtomicCollectableSearchCondition atomiccond) {
			// the cast is needed for the correct method dispatch
			return atomiccond.accept((AtomicVisitor<DbCondition, RuntimeException>) this);
		}

		@Override
		public DbCondition visitCompositeCondition(CompositeCollectableSearchCondition compositecond) {
			List<CollectableSearchCondition> operands = compositecond.getOperands();
			DbCondition[] conditions = new DbCondition[operands.size()];
			for (int i = 0; i < operands.size(); i++)
				conditions[i] = operands.get(i).accept(this);
			switch (compositecond.getLogicalOperator()) {
			case NOT:
				if (conditions.length != 1)
					throw new IllegalArgumentException("mdsearch.unparser.error.invalid.condition");//"Eine Suchbedingung mit NICHT darf nicht mehr als einen Operand haben.");
				return queryBuilder.not(conditions[0]);
			case AND:
				return queryBuilder.and(conditions);
			case OR:
				if (conditions.length == 0)
					return queryBuilder.alwaysTrue();
				return queryBuilder.or(conditions);
			default:
				throw new IllegalArgumentException("Invalid logical operator " + compositecond.getLogicalOperator());
			}
		}

		@Override
		public DbCondition visitIdCondition(CollectableIdCondition idcond) {
			/** @todo This only works for entities that have an INTID field. */
			DbExpression<Integer> intid = getDbIntIdColumn();
			return queryBuilder.equal(intid, queryBuilder.literal(idcond.getId()));
		}

		@Override
		public DbCondition visitSubCondition(CollectableSubCondition subcond) {
			EntityMetaDataVO subEntityMeta =  MetaDataServerProvider.getInstance().getEntity(subcond.getSubEntityName());

			DbQuery<Integer> subQuery = query.subquery(Integer.class);
			DbFrom subTable = subQuery.from(subEntityMeta.getDbEntity()).alias("sub");
			subQuery.distinct(true);
			if(subEntityMeta.isDynamic())
				subQuery.select(subTable.baseColumnCaseSensitive(DalUtils.getDbIdFieldName(MetaDataServerProvider.getInstance().getEntityField(subEntityMeta.getEntity(), subcond.getForeignKeyFieldName()).getDbColumn()), Integer.class));
			else
				subQuery.select(subTable.baseColumn(DalUtils.getDbIdFieldName(MetaDataServerProvider.getInstance().getEntityField(subEntityMeta.getEntity(), subcond.getForeignKeyFieldName()).getDbColumn()), Integer.class));
			EOSearchExpressionUnparser subUnparser = new EOSearchExpressionUnparser(subQuery, subEntityMeta);
			subUnparser.unparseSearchCondition(subcond.getSubCondition());

			return table.baseColumn("INTID", Integer.class).in(subQuery);
		}

		@Override
		public DbCondition visitPivotJoinCondition(PivotJoinCondition joincond) {
			final MetaDataProvider mdProv = MetaDataServerProvider.getInstance();

			final EntityMetaDataVO mdSubEntity = joincond.getJoinEntity();
			final String subEntity = mdSubEntity.getEntity();
			final EntityFieldMetaDataVO field = joincond.getField();
			final PivotInfo pinfo = field.getPivotInfo();

			// The join table alias must be unique in the SQL
			final String joinAlias = pinfo.getPivotTableAlias(field.getField());
			if (pivotTableAliases.contains(joinAlias)) {
				// the join condition has already been added to the SQL clause
				return queryBuilder.alwaysTrue();
			}
			pivotTableAliases.add(joinAlias);

			final EntityFieldMetaDataVO ref = mdProv.getRefField(entity.getEntity(), subEntity);
			String foreignEntityField = ref.getForeignEntityField();
			// TODO: ???
			if (foreignEntityField == null) {
				foreignEntityField = "INTID";
			}

			final String joinTable = EntityObjectMetaDbHelper.getViewName(mdSubEntity);
			final String keyColumn = mdProv.getEntityField(subEntity, pinfo.getKeyField()).getDbColumn();

			final DbJoin join = table.join(joinTable, JoinType.LEFT).alias(joinAlias).onAnd("INTID", DalUtils.getDbIdFieldName(ref.getDbColumn()), Long.class,
					queryBuilder.equal(table.column(joinAlias, keyColumn, String.class), queryBuilder.literal(field.getField())));

			// DbCondition cond;
			// pivot key matches
			// cond = queryBuilder.equal(join.baseColumn(keyColumn, String.class), queryBuilder.literal(field.getField()));
			// pivot key is NULL (i.e. does not exist). This could happen as this is an outer join
			// cond = queryBuilder.or(cond, queryBuilder.isNull(join.baseColumn(keyColumn, String.class)));

			// ??? We have no real condition, all is said within the (non-equi) join...
			return queryBuilder.alwaysTrue();
		}

		@Override
		public DbCondition visitRefJoinCondition(RefJoinCondition joincond) {
			final MetaDataProvider mdProv = MetaDataServerProvider.getInstance();
			
			final EntityFieldMetaDataVO field = joincond.getField();
			final EntityMetaDataVO refEntity = mdProv.getEntity(field.getForeignEntity());
			
			if (LOG.isDebugEnabled()) {
				LOG.debug("visitRefJoinCondition: apply " + joincond + " on " + table);
			}
			// If somehow the db name for a virtual entity is set to a table ("T_..."),
			// then the initialization sequence of EntityMetaDataVO setters is wrong. (tp)
			final String dbEntity;
			if (refEntity.isVirtual()) {
				assert refEntity.getDbEntity().startsWith("V_");
				dbEntity = refEntity.getDbEntity().replaceFirst("^T_", "V_");
			}
			else {
				dbEntity = refEntity.getDbEntity();
			}
			final DbJoin join = table.join(dbEntity, JoinType.LEFT).alias(joincond.getTableAlias()).on(
					DalUtils.getDbIdFieldName(field.getDbColumn()), "INTID", Long.class);

			// ??? We have no real condition, all is said within the join...
			return queryBuilder.alwaysTrue();
		}
		
		@Override
		public DbCondition visitReferencingCondition(ReferencingCollectableSearchCondition refcond) {
			DbColumnExpression<Integer> refColumn = getDbIdColumn(refcond.getReferencingField());
			EntityMetaDataVO refEntityMeta = MetaDataServerProvider.getInstance().getEntity(refcond.getReferencedEntityName());

			DbQuery<Integer> subQuery = query.subquery(Integer.class);
			DbFrom refTable = subQuery.from(refEntityMeta.getDbEntity()).alias("ref");
			subQuery.distinct(true);
			subQuery.select(refTable.baseColumn("INTID", Integer.class));
			EOSearchExpressionUnparser subUnparser = new EOSearchExpressionUnparser(subQuery, refEntityMeta);
			subUnparser.unparseSearchCondition(refcond.getSubCondition());

			return refColumn.in(subQuery);
		}

		@Override
		public DbCondition visitPlainSubCondition(PlainSubCondition subcond) {
			DbExpression<Integer> intid = getDbIntIdColumn();
			// TODO: get rid of plain sql conditions
			return queryBuilder.isMember(intid, queryBuilder.plainExpression(Integer.class, subcond.getPlainSQL()));
		}

		@Override
		public DbCondition visitSelfSubCondition(CollectableSelfSubCondition subcond) {
			// TODO: what does it mean that this returns null
			return null;
		}

		@Override
		public DbCondition visitComparison(CollectableComparison comparison) {
			final CollectableField clctfComparand = comparison.getComparand();
			final Object valueId = clctfComparand.isIdField() ? clctfComparand.getValueId() : null;

			switch (comparison.getComparisonOperator()) {
			case EQUAL:
				if (valueId != null) {
					DbExpression<?> x = getDbIdColumn(comparison.getEntityField());
					return queryBuilder.equal(x, queryBuilder.literal(valueId));
				}
			case NOT_EQUAL:
				if (valueId != null) {
					DbExpression<?> x = getDbIdColumn(comparison.getEntityField());
					return queryBuilder.notEqual(x, queryBuilder.literal(valueId));
				}
			default:
				DbExpression<?> x = getDbColumn(comparison.getEntityField());
				DbExpression<?> y = normalizedLiteral(clctfComparand.getValue());

				if (x.getJavaType() == String.class) {
					x = queryBuilder.upper(x.as(String.class));
					y = queryBuilder.upper(y.as(String.class));
				}

				// InternalTimestamp should be interpreted with time, so do not truncate time!
				/*if (x.getJavaType() == InternalTimestamp.class) {
					x = queryBuilder.convertInternalTimestampToDate(x.as(InternalTimestamp.class));
				}*/
				return compare(comparison.getComparisonOperator(), x, y);
			}
		}

		@Override
		public DbCondition visitComparisonWithParameter(CollectableComparisonWithParameter comparisonwp) {
			final ComparisonOperator compop = comparisonwp.getComparisonOperator();
			final CollectableEntityField field = comparisonwp.getEntityField();
			final CollectableField comparand;
			// Reduce parameter to a regular comparand value...
			switch (comparisonwp.getParameter()) {
			case TODAY:
				comparand = new CollectableValueField(RelativeDate.today());
				break;
			case USER:
				String userName = SessionUtils.getCurrentUserName();
				if (field.isIdField()) {
					Integer userId = SecurityCache.getInstance().getUserId(userName);
					comparand = new CollectableValueIdField(userId, userName);
				} else {
					comparand = new CollectableValueField(userName);
				}
				break;
			default:
				throw new UnsupportedOperationException("Unsupported parameter " + comparisonwp.getParameter());
			}
			// ...and delegate to that handler method
			return visitComparison(new CollectableComparison(field, compop, comparand));
		}

		@Override
		public DbCondition visitComparisonWithOtherField(CollectableComparisonWithOtherField comparisonwf) {
			final ComparisonOperator compop = comparisonwf.getComparisonOperator();
			final CollectableEntityField left = comparisonwf.getEntityField();
			final CollectableEntityField right = comparisonwf.getOtherField();

			DbExpression<?> x, y;
			if (left.isIdField() && right.isIdField() && (compop == ComparisonOperator.EQUAL || compop == ComparisonOperator.NOT_EQUAL))
			{
				x = getDbIdColumn(left);
				y = getDbIdColumn(right);
			} else {
				x = getDbColumn(left);
				y = getDbColumn(right);
				if (x.getJavaType() == String.class) {
					x = queryBuilder.upper(x.as(String.class));
					y = queryBuilder.upper(y.as(String.class));
				}
			}
			return compare(compop, x, y);
		}

		@Override
		public DbCondition visitLikeCondition(CollectableLikeCondition cond) {
			DbExpression<String> x = getDbColumn(cond.getEntityField()).as(String.class);
			if (cond.getEntityField().getJavaClass() == java.util.Date.class) {
				// TODO: ...
				x = queryBuilder.convertDateToString(x.as(java.util.Date.class), DbQueryBuilder.DATE_PATTERN_GERMAN);
			} else {
				x = queryBuilder.upper(x.as(String.class));
			}
			String pattern = cond.getSqlCompatibleLikeComparand();
			switch (cond.getComparisonOperator()) {
			case LIKE:
				return queryBuilder.like(x, StringUtils.toUpperCase(pattern));
			case NOT_LIKE:
				return queryBuilder.notLike(x, StringUtils.toUpperCase(pattern));
			default:
				throw new IllegalArgumentException("Invalid like operator " + cond.getComparisonOperator());
			}
		}

		@Override
		public DbCondition visitIsNullCondition(CollectableIsNullCondition cond) {
			if (cond.isPositive())
				return queryBuilder.isNull(getDbColumn(cond.getEntityField()));
			else
				return queryBuilder.isNotNull(getDbColumn(cond.getEntityField()));
		}

		private DbCondition compare(ComparisonOperator op, DbExpression<?> x, DbExpression<?> y) {
			switch (op) {
			case LIKE:
				return queryBuilder.like(x.as(String.class), y.as(String.class));
			case NOT_LIKE:
				return queryBuilder.notLike(x.as(String.class), y.as(String.class));
			case EQUAL:
				return queryBuilder.equal(x, y);
			case NOT_EQUAL:
				return queryBuilder.notEqual(x, y);
			case LESS:
				return queryBuilder.lessThan(x, y);
			case LESS_OR_EQUAL:
				return queryBuilder.lessThanOrEqualTo(x, y);
			case GREATER:
				return queryBuilder.greaterThan(x, y);
			case GREATER_OR_EQUAL:
				return queryBuilder.greaterThanOrEqualTo(x, y);
				// This should not happen because op's arity should be 2, ...
			case IS_NULL:
				return queryBuilder.isNull(x);
			case IS_NOT_NULL:
				return queryBuilder.isNotNull(x);
			default:
				throw new IllegalArgumentException("Invalid comparison operator " + op);
			}
		}

		@Override
        public DbCondition visitIdListCondition(CollectableIdListCondition collectableIdListCondition) throws RuntimeException {
			/** @todo This only works for entities that have an INTID field. */
			DbExpression<Integer> intid = getDbIntIdColumn();

			List<List<Long>> split = CollectionUtils.splitEvery(collectableIdListCondition.getLongIds(), query.getBuilder().getInLimit());
			DbCondition[] inConditions = new DbCondition[split.size()];
			for(int i = 0 ; i < split.size() ; i++) {
				inConditions[i] = intid.as(Long.class).in(split.get(i));
			}

			return queryBuilder.or(inConditions);
        }
		
		/**
		 * Get the DBExpression for use in the condition part of the SQL.
		 * <p>
		 * TODO: {@link org.nuclos.server.dal.processor.IColumnToVOMapping#getDbColumn(DbFrom)}
		 * does the same for the SELECT part of the SQL.
		 * </p>
		 */
		private DbExpression<?> getDbColumn(CollectableEntityField field) {
			final MetaDataProvider mdProv = MetaDataServerProvider.getInstance();
			final EntityFieldMetaDataVO entityField = mdProv.getEntityField(entity.getEntity(), field.getName());
			String dbColumn = entityField.getDbColumn();
			final String tableAlias;
			if (entityField.getCalcFunction() == null) {
				// 'stringified' nuclos reference field case 
				if (entityField.getForeignEntity() != null && (dbColumn.startsWith("STRVALUE_") || dbColumn.startsWith("INTVALUE_"))) {
					tableAlias = TableAliasSingleton.getInstance().getAlias(entityField);
					
					// It might be that the stringified reference used in the condition is not used for a join so far... (tp)
					if (!table.containsAlias(tableAlias)) {
						visitRefJoinCondition(new RefJoinCondition(entityField, tableAlias));
					}
					// We must repeat the SQL 'case when ...' expression here
					return new DbCompoundColumnExpression<Object>(table, entityField, false);
				}
				// normal field case
				else {
					tableAlias = table.getAlias();
				}
			}
			// db calc function case
			else {
				tableAlias = table.getAlias();
				return queryBuilder.plainExpression(field.getJavaClass(), entityField.getCalcFunction() 
						+ "(" + tableAlias + ".INTID)");
			}
			if(entity.isDynamic() && entityField.isDynamic()) {
				return table.columnCaseSensitive(tableAlias, dbColumn, field.getJavaClass());
			}
			else {
				return table.column(tableAlias, dbColumn, field.getJavaClass());
			}
		}

	}

	/**
	 * Some high-level data types are represented by more primitive values in the
	 * database.  For example, on the database layer {@link org.nuclos.common2.File}
	 * objects are treated as strings (paths).
	 */
	private Class<?> normalizeJavaType(Class<?> clazz) {
		if (clazz == org.nuclos.common2.File.class) {
			return String.class;
		}
		return clazz;
	}

	private Class<?> normalizeJavaType(String className) {
		try {
			return normalizeJavaType(Class.forName(className));
		}
		catch(ClassNotFoundException e) {
			throw new NuclosFatalException(e);
		}
	}

	private DbExpression<?> normalizedLiteral(Object value) {
		if (value == RelativeDate.today()) {
			return queryBuilder.currentDate();
		} else if (value instanceof org.nuclos.common2.File) {
			value = ((org.nuclos.common2.File) value).getFilename();
		}
		return queryBuilder.literal(value);

	}

	private DbColumnExpression<Integer> getDbIdColumn(CollectableEntityField field) {
		EntityFieldMetaDataVO entityField = MetaDataServerProvider.getInstance().getEntityField(entity.getEntity(), field.getName());
		if(entity.isDynamic() && entityField.isDynamic())
			return table.baseColumnCaseSensitive(DalUtils.getDbIdFieldName(entityField.getDbColumn()), Integer.class);
		else
			return table.baseColumn(DalUtils.getDbIdFieldName(entityField.getDbColumn()), Integer.class);
	}

	private DbExpression<Integer> getDbIntIdColumn() {
		return table.baseColumn("INTID", Integer.class);
	}
}
