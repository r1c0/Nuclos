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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nuclos.common.CloneUtils;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.common.dal.exception.DalBusinessException;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common.entityobject.CollectableEOEntityField;
import org.nuclos.common.querybuilder.NuclosDatasourceException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.common.DatasourceServerUtils;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.dal.processor.IColumnToVOMapping;
import org.nuclos.server.dal.processor.ProcessorConfiguration;
import org.nuclos.server.dal.processor.jdbc.AbstractJdbcWithFieldsDalProcessor;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityObjectProcessor;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbInvalidResultSizeException;
import org.nuclos.server.dblayer.query.DbCondition;
import org.nuclos.server.dblayer.query.DbExpression;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbJoin;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.genericobject.searchcondition.CollectableGenericObjectSearchExpression;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;

public class EntityObjectProcessor extends AbstractJdbcWithFieldsDalProcessor<EntityObjectVO>
	implements JdbcEntityObjectProcessor {

	// static variables

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

	private final String dbSourceForSQL;
	private final String dbSourceForDML;

	private final IColumnToVOMapping<Long> idColumn;
	private final IColumnToVOMapping<Integer> versionColumn;

	public EntityObjectProcessor(ProcessorConfiguration config)
	{
		super((Class<EntityObjectVO>) config.getDalType(), config.getAllColumns(), config.getMaxFieldCount(), config.getMaxFieldIdCount());
		this.eMeta = config.geteMeta();
		this.idColumn = config.getIdColumn();
		this.versionColumn = config.getVersionColumn();

		dbSourceForSQL = this.eMeta.getDbEntity();
		dbSourceForDML = this.eMeta.isVirtual() ? this.eMeta.getVirtualentity() : "T_" + this.eMeta.getDbEntity().substring(2);

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

	public Object clone() {
		final EntityObjectProcessor clone;
		try {
			clone = (EntityObjectProcessor) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e.toString());
		}
		clone.allColumns = (List<IColumnToVOMapping<? extends Object>>) CloneUtils.cloneCollection(allColumns);
		return clone;
	}

	@Override
	public String getDbSourceForDML() {
		return dbSourceForDML;
	}

	@Override
	public String getDbSourceForSQL() {
		return dbSourceForSQL;
	}

	@Override
	protected IColumnToVOMapping<Long> getPrimaryKeyColumn() {
		return idColumn;
	}

	@Override
	public DalCallResult delete(Long id) {
		return super.delete(id);
	}

	@Override
	public List<EntityObjectVO> getAll() {
		return super.getAll();
	}

	@Override
	public EntityObjectVO getByPrimaryKey(Long id) {
		return super.getByPrimaryKey(id);
	}

	@Override
	public List<EntityObjectVO> getByPrimaryKeys(List<Long> ids) {
		return super.getByPrimaryKeys(allColumns, ids);
	}

	@Override
    public List<EntityObjectVO> getBySearchExpressionAndPrimaryKeys(CollectableSearchExpression clctexpr, List<Long> ids) {
		DbQuery<Object[]> query = createQuery(allColumns);
		EOSearchExpressionUnparser unparser = new EOSearchExpressionUnparser(query, eMeta);
		unparser.unparseSearchCondition(getSearchConditionWithDeletedAndVLP(clctexpr));

		DbFrom from = CollectionUtils.getFirst(query.getRoots());
		DbExpression<?> pkExpr = getDbColumn(from, getPrimaryKeyColumn());
		Transformer<Object[], EntityObjectVO> transformer = createResultTransformer(allColumns);

		List<EntityObjectVO> result = new ArrayList<EntityObjectVO>(ids.size());
		for(List<Long> idSubList : CollectionUtils.splitEvery(ids,
		    query.getBuilder().getInLimit())) {
			query.addToWhereAsAnd(pkExpr.as(Long.class).in(idSubList));
			result.addAll(DataBaseHelper.getDbAccess().executeQuery(query, transformer));
		}
		return result;
    }

	@Override
	public DalCallResult insertOrUpdate(EntityObjectVO dalVO) {
		return checkLogicalUniqueConstraint(super.insertOrUpdate(dalVO), dalVO);
	}

	@Override
	public DalCallResult batchDelete(Collection<Long> colId) {
		return super.batchDelete(colId);
	}

	@Override
	public DalCallResult batchInsertOrUpdate(Collection<EntityObjectVO> colDalVO) {
		if (!logicalUniqueConstraintCombinations.isEmpty()) {
			DalCallResult result = super.batchInsertOrUpdate(colDalVO);
			for(EntityObjectVO dalVO : colDalVO) {
				result = checkLogicalUniqueConstraint(result, dalVO);
			}
			return result;
		} else
			return super.batchInsertOrUpdate(colDalVO);
	}

	@Override
	public List<EntityObjectVO> getBySearchExpression(CollectableSearchExpression clctexpr) {
		return getBySearchExpression(clctexpr, null, false);
	}

	@Override
	public List<EntityObjectVO> getBySearchExpression(CollectableSearchExpression clctexpr, boolean bSortResult) {
		return getBySearchExpression(clctexpr, null, bSortResult);
	}

	@Override
	public List<EntityObjectVO> getBySearchExpression(CollectableSearchExpression clctexpr, Integer iMaxRowCount, boolean bSortResult) {
		return getBySearchExpression(null, clctexpr, iMaxRowCount, bSortResult, false);
	}

	@Override
	public List<EntityObjectVO> getBySearchExpression(Collection<String> fields, CollectableSearchExpression clctexpr, Integer iMaxRowCount, boolean bSortResult, boolean bSearchInDMLSource) {
		List<IColumnToVOMapping<? extends Object>> columns = fields == null ? allColumns : getColumns(fields, bSearchInDMLSource);

		if (fields != null && !columns.contains(idColumn)) {
			columns.add(idColumn);
		}

		DbQuery<Object[]> query = createQuery(columns, bSearchInDMLSource);
		EOSearchExpressionUnparser unparser = new EOSearchExpressionUnparser(query, eMeta);
		unparser.unparseSearchCondition(getSearchConditionWithDeletedAndVLP(clctexpr));
		if (bSortResult)
			unparser.unparseSortingOrder(clctexpr.getSortingOrder());
		if (iMaxRowCount != null)
			query.maxResults(iMaxRowCount);
		return DataBaseHelper.getDbAccess().executeQuery(query, createResultTransformer(columns));
	}

	@Override
	public List<Long> getIdsBySearchExpression(CollectableSearchExpression clctexpr) {
		DbQuery<Long> query = createSingleColumnQuery(getPrimaryKeyColumn(), false);
		EOSearchExpressionUnparser unparser = new EOSearchExpressionUnparser(query, eMeta);
		unparser.unparseSearchCondition(getSearchConditionWithDeletedAndVLP(clctexpr));
		unparser.unparseSortingOrder(clctexpr.getSortingOrder());
		return DataBaseHelper.getDbAccess().executeQuery(query);
	}

	@Override
	public List<Long> getIdsBySearchExprUserGroups(CollectableSearchExpression searchExpression, Long moduleId, String user) {
		// if user is super-user or has a module permission that is not restricted to a user group, object groups can be ignored
		if(SecurityCache.getInstance().isSuperUser(user) || SecurityCache.getInstance().getModulePermissions(user).getMaxPermissionForObjectGroup(eMeta.getEntity(), null) != null) {
			return getIdsBySearchExpression(searchExpression);
		}
        DbQuery<Long> query = DataBaseHelper.getDbAccess().getQueryBuilder().<Long>createQuery(getPrimaryKeyColumn().getDataType());

		DbFrom from = query.from(getDbSourceForSQL()).alias(SystemFields.BASE_ALIAS);

		query.select(this.<Long>getDbColumn(from, getPrimaryKeyColumn()));

		EOSearchExpressionUnparser unparser = new EOSearchExpressionUnparser(query, eMeta);
		unparser.unparseSearchCondition(getSearchConditionWithDeletedAndVLP(searchExpression));
		unparser.unparseSortingOrder(searchExpression.getSortingOrder());

		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
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

		query.where(query.getRestriction() != null ? (query.getBuilder().and(query.getRestriction(), objectgroup)) : objectgroup);
		return DataBaseHelper.getDbAccess().executeQuery(query);
	}


	@Override
	public List<Long> getAllIds() {
		DbQuery<Long> query = createSingleColumnQuery(getPrimaryKeyColumn(), true);
		return DataBaseHelper.getDbAccess().executeQuery(query);
	}

	@Override
	public Integer getVersion(Long id) {
		if (id == null || versionColumn == null)
			return null;

		DbQuery<Integer> query = createSingleColumnQuery(versionColumn, true);
		DbFrom from = CollectionUtils.getFirst(query.getRoots());
		query.where(query.getBuilder().equal(getDbColumn(from, getPrimaryKeyColumn()), id));
		try {
			return DataBaseHelper.getDbAccess().executeQuerySingleResult(query);
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
		Long count = DataBaseHelper.getDbAccess().executeQuerySingleResult(query);
		return count.intValue();
	}

	/**
	 *
	 * @param clctexpr
	 * @return
	 */
	private CollectableSearchCondition getSearchConditionWithDeletedAndVLP(CollectableSearchExpression clctexpr) {
		CollectableSearchCondition result = null;
		if (clctexpr.getValueListProviderDatasource() != null) {
				try {
					if(clctexpr.getValueListProviderDatasource().getValid())
						result = DatasourceServerUtils.getConditionWithIdForInClause(
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

	private DalCallResult checkLogicalUniqueConstraint(DalCallResult result, EntityObjectVO dalVO) {
		if (!logicalUniqueConstraintCombinations.isEmpty()) {
			for (List<IColumnToVOMapping<?>> columns : logicalUniqueConstraintCombinations) {
				final Map<IColumnToVOMapping<?>, Object> checkValues = super.getColumnValuesMapWithMapping(columns, dalVO, false);
				final DalBusinessException checkResult = super.checkLogicalUniqueConstraint(checkValues, dalVO.getId());
				if (checkResult != null)
					result.addBusinessException(checkResult);
			}
		}
		return result;
	}

	private List<IColumnToVOMapping<? extends Object>> getColumns(final Collection<String> fields, final boolean bSearchInDMLSource) {
		return CollectionUtils.select(allColumns, new Predicate<IColumnToVOMapping<? extends Object>>() {
			@Override
			public boolean evaluate(IColumnToVOMapping<? extends Object> column) {
				return fields.contains(column.getField()) && (!bSearchInDMLSource || (bSearchInDMLSource && !column.isReadonly()));
			}});
	}

}
