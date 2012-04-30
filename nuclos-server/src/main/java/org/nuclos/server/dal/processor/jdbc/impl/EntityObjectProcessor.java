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

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableSorting;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collect.collectable.searchcondition.RefJoinCondition;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common.dblayer.IFieldRef;
import org.nuclos.common.entityobject.CollectableEOEntityField;
import org.nuclos.common.querybuilder.NuclosDatasourceException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.common.DatasourceServerUtils;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.dal.processor.IColumnToVOMapping;
import org.nuclos.server.dal.processor.IColumnWithMdToVOMapping;
import org.nuclos.server.dal.processor.ProcessorConfiguration;
import org.nuclos.server.dal.processor.jdbc.AbstractJdbcWithFieldsDalProcessor;
import org.nuclos.server.dal.processor.jdbc.TableAliasSingleton;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityObjectProcessor;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.DbInvalidResultSizeException;
import org.nuclos.server.dblayer.EntityObjectMetaDbHelper;
import org.nuclos.server.dblayer.query.DbCondition;
import org.nuclos.server.dblayer.query.DbExpression;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbJoin;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.dblayer.util.ForeignEntityFieldParser;
import org.nuclos.server.genericobject.searchcondition.CollectableGenericObjectSearchExpression;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/** 
 * TODO: @Autowired (and @Configurable ?) do not work here - but why?
 * 		 Instead we hard-wire in ProcessorFactorySingleton at present. (tp)
 */
@Configurable
public class EntityObjectProcessor extends AbstractJdbcWithFieldsDalProcessor<EntityObjectVO>
	implements JdbcEntityObjectProcessor {

	// static variables
	
	private static final Logger LOG = Logger.getLogger(EntityObjectProcessor.class);

	/**
	 * TODO: Is null for entity name ok?
	 *
	 * @deprecated This is not really a field of an entity - avoid this!
	 */
	private static final CollectableEntityField clctEOEFdeleted = new CollectableEOEntityField(NuclosEOField.LOGGICALDELETED.getMetaData(), "<dummy>");
	
	private final Collection<List<IColumnToVOMapping<?>>> logicalUniqueConstraintCombinations =
		new ArrayList<List<IColumnToVOMapping<?>>>();
	
	// instance variables

	protected final EntityMetaDataVO eMeta;

	/**
	 * Table or view to use when reading from DB.
	 */
	private final String dbSourceForSQL;
	
	/**
	 * Table (or view) to use when writing to DB.
	 * <p>
	 * This is only different from {@link #getDbSourceForSQL()} for 
	 * DB read delegates.
	 * </p>
	 */
	private final String dbSourceForDML;

	private final IColumnToVOMapping<Long> idColumn;
	private final IColumnToVOMapping<Integer> versionColumn;
	
	private DatasourceServerUtils datasourceServerUtils;
	
	private TableAliasSingleton tableAliasSingleton; 

	public EntityObjectProcessor(ProcessorConfiguration config)
	{
		super((Class<EntityObjectVO>) config.getDalType(), config.getAllColumns(), config.getMaxFieldCount(), config.getMaxFieldIdCount());
		this.eMeta = config.geteMeta();
		this.idColumn = config.getIdColumn();
		this.versionColumn = config.getVersionColumn();

		// dbSourceForSQL = this.eMeta.getDbEntity();
		dbSourceForSQL = EntityObjectMetaDbHelper.getTableOrViewForSelect(eMeta);
		if (eMeta.getReadDelegate() == null) {
			dbSourceForDML = dbSourceForSQL;
		}
		else {
			dbSourceForDML = eMeta.getDbEntity();
		}

		/**
		 * Logical Unique Constraints
		 */
		if (eMeta.getLogicalUniqueFieldCombinations() != null) {
			for (Set<String> lufCombination : eMeta.getLogicalUniqueFieldCombinations()) {
				List<IColumnToVOMapping<? extends Object>> ctovMappings =
					new ArrayList<IColumnToVOMapping<?>>(lufCombination.size());

				for (String luField : lufCombination) {
					for (IColumnToVOMapping<?> ctovMap : allColumns) {
						if (!ctovMap.isReadonly() && luField.equals(ctovMap.getField())) {
							ctovMappings.add(ctovMap);
						}
					}
				}
				logicalUniqueConstraintCombinations.add(ctovMappings);
			}
		}
	}
	
	@Autowired
	public void setDatasourceServerUtils(DatasourceServerUtils datasourceServerUtils) {
		this.datasourceServerUtils = datasourceServerUtils;
	}
	
	@Autowired
	public void setTableAliasSingleton(TableAliasSingleton tableAliasSingleton) {
		this.tableAliasSingleton = tableAliasSingleton;
	}

	@Override
	public EntityMetaDataVO getMeta() {
		return eMeta;
	}

	@Override
	protected EntityObjectVO newDalVOInstance() {
		try {
			final EntityObjectVO newInstance = super.newDalVOInstance();
			newInstance.setEntity(eMeta.getEntity());
			return newInstance;
		} catch (Exception e) {
			throw new CommonFatalException(e);
		}
	}

	/**
	 * @deprecated We want to get rid of views.
	 */
	@Override
	public String getDbSourceForDML() {
		return dbSourceForDML;
	}

	/**
	 * @deprecated We want to get rid of views.
	 */
	@Override
	public String getDbSourceForSQL() {
		return dbSourceForSQL;
	}

	@Override
	protected IColumnToVOMapping<Long> getPrimaryKeyColumn() {
		return idColumn;
	}

	@Override
	public void delete(Long id) throws DbException {
		super.delete(id);
	}

	/**
	 * Includes joins for (former) views.
	 */
	@Override
	public List<EntityObjectVO> getAll() {
		/*
		if (eMeta.getEntity().equals(NuclosEntity.DATASOURCE.getEntityName())) {
			return super.getAll();
		}
		 */
		return getBySearchExpression(CollectableSearchExpression.TRUE_SEARCH_EXPR);
	}

	/**
	 * Includes joins for (former) views.
	 */
	@Override
	public EntityObjectVO getByPrimaryKey(Long id) {
		return CollectionUtils.getSingleIfExist(getBySearchExpressionAndPrimaryKeys(
				CollectableSearchExpression.TRUE_SEARCH_EXPR, Collections.singletonList(id)));
	}

	/**
	 * Includes joins for (former) views.
	 */
	@Override
	public List<EntityObjectVO> getByPrimaryKeys(List<Long> ids) {
		return getBySearchExpressionAndPrimaryKeys(CollectableSearchExpression.TRUE_SEARCH_EXPR, ids);
	}

	/**
	 * Includes joins for (former) views.
	 */
	@Override
    public List<EntityObjectVO> getBySearchExpressionAndPrimaryKeys(CollectableSearchExpression clctexpr, List<Long> ids) {
		DbQuery<Object[]> query = createQuery(allColumns);
		
		// we modify the search expr here...
		clctexpr = addJoinedRefs(null, clctexpr);
		
		EOSearchExpressionUnparser unparser = new EOSearchExpressionUnparser(query, eMeta);
		unparser.unparseSearchCondition(getSearchConditionWithDeletedAndVLP(clctexpr));

		DbFrom from = CollectionUtils.getFirst(query.getRoots());
		DbExpression<?> pkExpr = getPrimaryKeyColumn().getDbColumn(from);
		Transformer<Object[], EntityObjectVO> transformer = createResultTransformer(allColumns);

		List<EntityObjectVO> result = new ArrayList<EntityObjectVO>(ids.size());
		for(List<Long> idSubList : CollectionUtils.splitEvery(ids,
		    query.getBuilder().getInLimit())) {
			query.addToWhereAsAnd(pkExpr.as(Long.class).in(idSubList));
			result.addAll(dataBaseHelper.getDbAccess().executeQuery(query, transformer));
		}
		return result;
    }

	@Override
	public void insertOrUpdate(EntityObjectVO dalVO) {
		super.insertOrUpdate(dalVO);
		final DalCallResult result = new DalCallResult();
		checkLogicalUniqueConstraint(result, dalVO);
		result.throwFirstException();
	}

	@Override
	public DalCallResult batchDelete(Collection<Long> colId, boolean failAfterBatch) {
		return super.batchDelete(colId, failAfterBatch);
	}

	@Override
	public DalCallResult batchInsertOrUpdate(Collection<EntityObjectVO> colDalVO, boolean failAfterBatch) {
		if (!logicalUniqueConstraintCombinations.isEmpty()) {
			final DalCallResult result = super.batchInsertOrUpdate(colDalVO, failAfterBatch);
			for(EntityObjectVO dalVO : colDalVO) {
				checkLogicalUniqueConstraint(result, dalVO);
			}
			return result;
		} else
			return super.batchInsertOrUpdate(colDalVO, failAfterBatch);
	}

	/**
	 * Includes joins for (former) views.
	 */
	@Override
	public List<EntityObjectVO> getBySearchExpression(CollectableSearchExpression clctexpr) {
		return getBySearchExpression(clctexpr, null, false);
	}

	/**
	 * Includes joins for (former) views.
	 */
	@Override
	public List<EntityObjectVO> getBySearchExpression(CollectableSearchExpression clctexpr, boolean bSortResult) {
		return getBySearchExpression(clctexpr, null, bSortResult);
	}

	/**
	 * Includes joins for (former) views.
	 */
	@Override
	public List<EntityObjectVO> getBySearchExpression(CollectableSearchExpression clctexpr, Integer iMaxRowCount, boolean bSortResult) {
		return getBySearchExpression(null, clctexpr, iMaxRowCount, bSortResult, false);
	}

	/**
	 * Includes joins for (former) views.
	 * 
	 * @deprecated fields doesn't support fields from joined entities, hence we need something better... 
	 */
	@Override
	public List<EntityObjectVO> getBySearchExpression(Collection<String> fields, CollectableSearchExpression clctexpr, Integer iMaxRowCount, boolean bSortResult, boolean bSearchInDMLSource) {
		List<IColumnToVOMapping<? extends Object>> columns = fields == null ? allColumns : getColumns(fields, bSearchInDMLSource);

		if (fields != null && !columns.contains(idColumn)) {
			columns.add(idColumn);
		}

		// we modify the search expr here...
		clctexpr = addJoinedRefs(fields, clctexpr);
		
		DbQuery<Object[]> query = createQuery(columns, bSearchInDMLSource);
		EOSearchExpressionUnparser unparser = new EOSearchExpressionUnparser(query, eMeta);
		unparser.unparseSearchCondition(getSearchConditionWithDeletedAndVLP(clctexpr));
		if (bSortResult)
			unparser.unparseSortingOrder(clctexpr.getSortingOrder());
		if (iMaxRowCount != null)
			query.maxResults(iMaxRowCount);
		return dataBaseHelper.getDbAccess().executeQuery(query, createResultTransformer(columns));
	}

	/**
	 * Includes joins for (former) views (needed for sorting order).
	 */
	@Override
	public List<Long> getIdsBySearchExpression(CollectableSearchExpression clctexpr) {
		// we modify the search expr here...
		clctexpr = addJoinedRefs(clctexpr);
		
		DbQuery<Long> query = createSingleColumnQuery(getPrimaryKeyColumn(), false);
		EOSearchExpressionUnparser unparser = new EOSearchExpressionUnparser(query, eMeta);
		unparser.unparseSearchCondition(getSearchConditionWithDeletedAndVLP(clctexpr));
		unparser.unparseSortingOrder(clctexpr.getSortingOrder());
		return dataBaseHelper.getDbAccess().executeQuery(query);
	}

	/**
	 * Includes joins for (former) views (needed for sorting order).
	 */
	@Override
	public List<Long> getIdsBySearchExprUserGroups(CollectableSearchExpression searchExpression, Long moduleId, String user) {
		// if user is super-user or has a module permission that is not restricted to a user group, object groups can be ignored
		if(SecurityCache.getInstance().isSuperUser(user) || SecurityCache.getInstance().getModulePermissions(user).getMaxPermissionForObjectGroup(eMeta.getEntity(), null) != null) {
			return getIdsBySearchExpression(searchExpression);
		}
        DbQuery<Long> query = dataBaseHelper.getDbAccess().getQueryBuilder().<Long>createQuery(getPrimaryKeyColumn().getDataType());

		DbFrom from = query.from(getDbSourceForSQL()).alias(SystemFields.BASE_ALIAS);

		query.select(getPrimaryKeyColumn().getDbColumn(from));

		// we modify the search expr here...
		searchExpression = addJoinedRefs(searchExpression);
		
		EOSearchExpressionUnparser unparser = new EOSearchExpressionUnparser(query, eMeta);
		CollectableSearchCondition searchConditionWithDeletedAndVLP = getSearchConditionWithDeletedAndVLP(searchExpression);
		unparser.unparseSearchCondition(searchConditionWithDeletedAndVLP);
		unparser.unparseSortingOrder(searchExpression.getSortingOrder());

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Long> groupsubquery = builder.createQuery(getPrimaryKeyColumn().getDataType());
		DbFrom sqfrom = groupsubquery.from("t_ud_genericobject").alias("g");
		sqfrom.innerJoin(getDbSourceForDML()).alias("eo").on("intid", "intid", Long.class);
		DbJoin role_mod = sqfrom.innerJoin("t_md_role_module").alias("rm").on("intid_t_md_module", "intid_t_md_module", Long.class);
		DbJoin role_user = role_mod.innerJoin("t_md_role_user").alias("ru").on("intid_t_md_role", "intid_t_md_role", Long.class);
		DbJoin md_user = role_user.innerJoin("t_md_user").alias("us").on("intid_t_md_user", "intid", Long.class);
		DbJoin go_group = sqfrom.leftJoin("t_ud_go_group").alias("gg").on("INTID", "INTID_T_UD_GENERICOBJECT", Long.class);

		groupsubquery.select(sqfrom.baseColumn("intid", Long.class));

		DbCondition usercondition = builder.equal(builder.upper(md_user.baseColumn("STRUSER", String.class)), builder.upper(builder.literal(user)));
		DbCondition groupcondition = groupsubquery.getBuilder().or(
			groupsubquery.getBuilder().isNull(role_mod.baseColumn("intid_t_ud_group", Long.class)),
			groupsubquery.getBuilder().and(
				groupsubquery.getBuilder().isNotNull(go_group.baseColumn("intid_t_ud_group", Long.class)),
				groupsubquery.getBuilder().equal(go_group.baseColumn("intid_t_ud_group", Long.class), role_mod.baseColumn("intid_t_ud_group", Long.class))));

		DbCondition userandgroup = query.getBuilder().and(usercondition, groupcondition);
		groupsubquery.where(userandgroup);

		DbCondition objectgroup = from.baseColumn("intid", Long.class).in(groupsubquery);

		if (searchConditionWithDeletedAndVLP != null)
			query.where(query.getRestriction() != null ? (query.getBuilder().and(query.getRestriction(), objectgroup)) : objectgroup);
		else
			query.addToWhereAsAnd(query.getRestriction() != null ? (query.getBuilder().and(query.getRestriction(), objectgroup)) : objectgroup);
		return dataBaseHelper.getDbAccess().executeQuery(query);
	}


	@Override
	public List<Long> getAllIds() {
		DbQuery<Long> query = createSingleColumnQuery(getPrimaryKeyColumn(), true);
		return dataBaseHelper.getDbAccess().executeQuery(query);
	}

	@Override
	public Integer getVersion(Long id) {
		if (id == null || versionColumn == null)
			return null;

		DbQuery<Integer> query = createSingleColumnQuery(versionColumn, true);
		DbFrom from = CollectionUtils.getFirst(query.getRoots());
		query.where(query.getBuilder().equal(getPrimaryKeyColumn().getDbColumn(from), id));
		try {
			return dataBaseHelper.getDbAccess().executeQuerySingleResult(query);
		} catch (DbInvalidResultSizeException e) {
			if (e.wasEmpty()) {
				throw new CommonFatalException("No record with id " + id + " in table " + eMeta.getDbEntity());
			} else {
				throw new CommonFatalException("Primary key is not unique!");
			}
		}
	}

	@Override
	public Integer count(CollectableSearchExpression clctexpr) {
		DbQuery<Long> query = createCountQuery(getPrimaryKeyColumn());
		EOSearchExpressionUnparser unparser = new EOSearchExpressionUnparser(query, eMeta);
		unparser.unparseSearchCondition(getSearchConditionWithDeletedAndVLP(clctexpr));
		Long count = dataBaseHelper.getDbAccess().executeQuerySingleResult(query);
		return count.intValue();
	}

	@Override
	public void checkLogicalUniqueConstraint(EntityObjectVO dalVO) throws DbException {
		final DalCallResult result = new DalCallResult();
		checkLogicalUniqueConstraint(result, dalVO);
		result.throwFirstException();
	}

	private CollectableSearchCondition getSearchConditionWithDeletedAndVLP(CollectableSearchExpression clctexpr) {
		CollectableSearchCondition result = null;
		if (clctexpr.getValueListProviderDatasource() != null) {
				try {
					if(clctexpr.getValueListProviderDatasource().getValid())
						result = datasourceServerUtils.getConditionWithIdForInClause(
							clctexpr.getValueListProviderDatasource().getSource(),
							clctexpr.getValueListProviderDatasourceParameter());
				}
				catch(NuclosDatasourceException e) {
					throw new NuclosFatalException("datasource.error.valuelistprovider.invalid", e);
				}
		}

		if (clctexpr instanceof CollectableGenericObjectSearchExpression) {

			CollectableGenericObjectSearchExpression clctGOexpr = (CollectableGenericObjectSearchExpression) clctexpr;
			if (!clctGOexpr.getSearchDeleted().equals(CollectableGenericObjectSearchExpression.SEARCH_BOTH)) {

				final Boolean bSearchDeleted = CollectableGenericObjectSearchExpression.SEARCH_DELETED == clctGOexpr.getSearchDeleted();
				CollectableSearchCondition condSearchDeleted = new CollectableComparison(clctEOEFdeleted, ComparisonOperator.EQUAL, new CollectableValueField(bSearchDeleted));

				result = result!=null ? SearchConditionUtils.and(condSearchDeleted, result) : condSearchDeleted;
			}
		}

		if (result == null) {
			return clctexpr.getSearchCondition() == null ? null : clctexpr.getSearchCondition();
		} else {
			return clctexpr.getSearchCondition() == null ? result : SearchConditionUtils.and(clctexpr.getSearchCondition(), result);
		}
	}

	private void checkLogicalUniqueConstraint(DalCallResult result, EntityObjectVO dalVO) {
		if (!logicalUniqueConstraintCombinations.isEmpty()) {
			for (List<IColumnToVOMapping<?>> columns : logicalUniqueConstraintCombinations) {
				final Map<IColumnToVOMapping<?>, Object> checkValues = super.getColumnValuesMapWithMapping(columns, dalVO, false);
				final SQLIntegrityConstraintViolationException checkResult = super.checkLogicalUniqueConstraint(checkValues, dalVO.getId());
				if (checkResult != null)
					result.addBusinessException(dalVO.getId(), null, checkResult);
			}
		}
	}

	private List<IColumnToVOMapping<? extends Object>> getColumns(final Collection<String> fields, final boolean bSearchInDMLSource) {
		return CollectionUtils.select(allColumns, new Predicate<IColumnToVOMapping<? extends Object>>() {
			@Override
			public boolean evaluate(IColumnToVOMapping<? extends Object> column) {
				return fields.contains(column.getField()) && (!bSearchInDMLSource || (bSearchInDMLSource && !column.isReadonly()));
			}});
	}
	
	/**
	 * Includes joins for (former) views.
	 */
	@Override
	protected List<EntityObjectVO> getByIdColumn(final List<IColumnToVOMapping<? extends Object>> columns,
			IColumnToVOMapping<Long> column, final Long id) {
		return getBySearchExpressionAndPrimaryKeys(CollectableSearchExpression.TRUE_SEARCH_EXPR, Collections.singletonList(id));
	}

	private CollectableSearchExpression addJoinedRefs(Collection<String> fields, CollectableSearchExpression clctexpr) {
		// ???
		final List<IColumnToVOMapping<? extends Object>> columns;
		if (fields == null) {
			columns = allColumns;
		}
		else {
			// TODO: ???
			columns = new ArrayList<IColumnToVOMapping<? extends Object>>(fields.size());
			// defensive copy
			final Set<String> fieldSet = new HashSet<String>(fields);
			for (IColumnToVOMapping<?> m: allColumns) {
				if (fieldSet.remove(m.getField())) {
					columns.add(m);
				}
			}
			if (!fieldSet.isEmpty()) {
				throw new IllegalStateException("Unable to map the following fields: " + fieldSet + " in entity " + eMeta);
			}
		}
		final List<CollectableSearchCondition> joins = getJoinsForColumns(columns);
		return composeSearchCondition(clctexpr, joins);
	}
	
	private CollectableSearchExpression composeSearchCondition(CollectableSearchExpression clctexpr, List<CollectableSearchCondition> joins) {
		addJoinsForSortingOrder(clctexpr, joins);
		
		CollectableSearchExpression result = clctexpr;
		if (!joins.isEmpty()) {
			// add old condition
			if (clctexpr != null && clctexpr.getSearchCondition() != null) {
				joins.add(clctexpr.getSearchCondition());
			}
			result = new CollectableSearchExpression(
					new CompositeCollectableSearchCondition(LogicalOperator.AND, joins), 
					clctexpr.getSortingOrder());
		}
		return result;
	}
	
	/*
	private CollectableSearchExpression getRefFieldsSearchExpression(final List<IColumnToVOMapping<? extends Object>> columns) {
		final List<CollectableSearchCondition> joins = getJoinsForColumns(columns);
		if (!joins.isEmpty()) {
			return new CollectableSearchExpression(
					new CompositeCollectableSearchCondition(LogicalOperator.AND, joins));
		}
		return null;
	}
	 */
	
	private List<CollectableSearchCondition> getJoinsForColumns(final List<IColumnToVOMapping<? extends Object>> columns) {
		final List<CollectableSearchCondition> result = new ArrayList<CollectableSearchCondition>();
		final boolean debug = LOG.isDebugEnabled();
		for (IColumnToVOMapping<?> m: columns) {
			boolean add = false;
			String alias = null;
			final String f = m.getColumn();
			if (m instanceof IColumnWithMdToVOMapping) {
				final IColumnWithMdToVOMapping<?> mapping = (IColumnWithMdToVOMapping<?>) m;
				if (!mapping.constructJoinForStringifiedRefs()) {
					continue;
				}
				final EntityFieldMetaDataVO meta = mapping.getMeta();
				final String fentity = meta.getForeignEntity();
				if (fentity != null) {
					alias = tableAliasSingleton.getAlias(meta);					
					if (f.startsWith("STRVALUE_")) {
						add = true;
					}					
					else if (f.startsWith("INTVALUE_")) {
						// We used to have *2* joins on nuclos_state, one for STRVALUE_ and INTVALUE_...
						// Hence we omit the INTVALUE case
						if (!meta.getForeignEntity().equals(NuclosEntity.STATE.getEntityName())) {
							add = true;
						}
					}
				}
			}
			// ???
			else {
				if (f.startsWith("STRVALUE_") || f.startsWith("INTVALUE_")) {
					alias = tableAliasSingleton.getAlias(m);
					add = true;
				}
			}
			if (add) {
				if (debug) {
					LOG.debug("getJoinsForColumns: apply RefJoinCondition for " + m + " alias " + alias);
				}
				result.add(tableAliasSingleton.getRefJoinCondition(m));
			}			
		}
		return result;
	}
	
	private CollectableSearchExpression addJoinedRefs(CollectableSearchExpression clctexpr) {
		final List<CollectableSearchCondition> joins = new ArrayList<CollectableSearchCondition>();
		addJoinsForSortingOrder(clctexpr, joins);
		return composeSearchCondition(clctexpr, joins);
	}
	
	private void addJoinsForSortingOrder(CollectableSearchExpression searchExpression, List<CollectableSearchCondition> result) {
		final List<CollectableSorting> sortingOrder = searchExpression.getSortingOrder();
		// 
		// Also add join conditions needed by sorting order. (tp)
		if (sortingOrder != null) {
			final MetaDataProvider mdProv = MetaDataServerProvider.getInstance();
			final List<CollectableSorting> newSortingOrder = new ArrayList<CollectableSorting>(sortingOrder.size());
			final String baseEntity = eMeta.getEntity();
			for (CollectableSorting cs: sortingOrder) {
				// This could happen only for the base entity, for foreign entity fields ('STRVALUE_...'),
				// because sorting is only available for the base entity (and the pivot table).
				if (cs.getEntity().equals(baseEntity)) {
					assert cs.getTableAlias().equals(SystemFields.BASE_ALIAS);
					final EntityFieldMetaDataVO mdField = mdProv.getEntityField(cs.getEntity(), cs.getFieldName());
					final String fentity = mdField.getForeignEntity(); 
					if (fentity == null) {
						// copy sorting order
						newSortingOrder.add(cs);
						continue;
					}
					final RefJoinCondition join = tableAliasSingleton.getRefJoinCondition(mdField);
					if (!result.contains(join)) {
						result.add(join);
					}
					
					// retrieve sorting order
					for (IFieldRef ref: new ForeignEntityFieldParser(mdField)) {
						if (!ref.isConstant()) {
							newSortingOrder.add(new CollectableSorting(
									join.getTableAlias(), fentity, false, ref.getContent(), cs.isAscending()));
						}
					}
				}
				else {
					// copy sorting order
					newSortingOrder.add(cs);
				}
			}
			// modify sorting order
			searchExpression.setSortingOrder(newSortingOrder);
		}
	}

}
