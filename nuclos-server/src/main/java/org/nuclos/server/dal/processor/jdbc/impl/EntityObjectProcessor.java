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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.entityobject.CollectableEOEntityField;
import org.nuclos.common.querybuilder.NuclosDatasourceException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.common.DatasourceServerUtils;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.dal.DalUtils;
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

public class EntityObjectProcessor extends AbstractJdbcWithFieldsDalProcessor<Object,EntityObjectVO>
implements JdbcEntityObjectProcessor {

	private final EntityMetaDataVO eMeta;

	private final String dbSourceForSQL;
	private final String dbSourceForDML;

	private final ColumnToVOMapping<Long> idColumn;
	private final ColumnToVOMapping<Integer> versionColumn;

	/**
	 * TODO: Is null for entity name ok?
	 * 
	 * @deprecated This is not really a field of an entity - avoid this!
	 */
	private static final CollectableEntityField clctEOEFdeleted = new CollectableEOEntityField(NuclosEOField.LOGGICALDELETED.getMetaData(), "<dummy>");

	private final Collection<List<ColumnToVOMapping<?>>> logicalUniqueConstraintCombinations = new ArrayList<List<ColumnToVOMapping<?>>>();

	private static Set<String> staticSystemFields = new HashSet<String>();
	static {
		staticSystemFields.add(NuclosEOField.CHANGEDAT.getMetaData().getField());
		staticSystemFields.add(NuclosEOField.CHANGEDBY.getMetaData().getField());
		staticSystemFields.add(NuclosEOField.CREATEDAT.getMetaData().getField());
		staticSystemFields.add(NuclosEOField.CREATEDBY.getMetaData().getField());
	}

	public EntityObjectProcessor(EntityMetaDataVO eMeta, Collection<EntityFieldMetaDataVO> colEfMeta) {
		this(eMeta, colEfMeta, true);
	}

	protected EntityObjectProcessor(EntityMetaDataVO eMeta, Collection<EntityFieldMetaDataVO> colEfMeta, boolean addSystemColumns) {
		super(countFieldsForInitiatingFieldMap(colEfMeta), countIdFieldsForInitiatingFieldMap(colEfMeta));

		idColumn = createSimpleStaticMapping("INTID", "id", DT_LONG);
		allColumns.add(idColumn);

		if(addSystemColumns) {
			allColumns.add(createSimpleStaticMapping("DATCREATED", "createdAt", DT_INTERNALTIMESTAMP));
			allColumns.add(createSimpleStaticMapping("STRCREATED", "createdBy", DT_STRING));
			allColumns.add(createSimpleStaticMapping("DATCHANGED", "changedAt", DT_INTERNALTIMESTAMP));
			allColumns.add(createSimpleStaticMapping("STRCHANGED", "changedBy", DT_STRING));
			versionColumn = createSimpleStaticMapping("INTVERSION", "version", DT_INTEGER);
			allColumns.add(versionColumn);
		} else {
			versionColumn = null;
		}

		this.eMeta = eMeta;

		for (EntityFieldMetaDataVO efMeta : colEfMeta) {
			if (staticSystemFields.contains(efMeta.getField())) {
				// hier nur dynamische Zuweisungen
				continue;
			}

			if (efMeta.getForeignEntity() == null) {
				allColumns.add(createSimpleDynamicMapping(efMeta.getDbColumn(), efMeta.getField(), efMeta.getDataType(), efMeta.isReadonly(), false, efMeta.isDynamic()));
			} else {
				if (efMeta.getDbColumn().toUpperCase().startsWith("INTID_")) {
					// kein join n\u00f6tig!
					if (!isIdColumnInList(allColumns, efMeta.getDbColumn()))
						allColumns.add(createSimpleDynamicMapping(efMeta.getDbColumn(), efMeta.getField(), DT_LONG.getName(), efMeta.isReadonly(), true, efMeta.isDynamic()));
				} else {
					allColumns.add(createSimpleDynamicMapping(efMeta.getDbColumn(), efMeta.getField(), efMeta.getDataType(), true, false, efMeta.isDynamic()));
					String dbIdFieldName = DalUtils.getDbIdFieldName(efMeta.getDbColumn());
					if (!isIdColumnInList(allColumns, dbIdFieldName)) {
						allColumns.add(createSimpleDynamicMapping(dbIdFieldName, efMeta.getField(), DT_LONG.getName(), efMeta.isReadonly(), true, efMeta.isDynamic()));
					}
					else {
						ColumnToVOMapping<?> col = getColumnFromList(allColumns, dbIdFieldName);
						if (col.isReadonly && !efMeta.isReadonly()) {
							allColumns.remove(col);
							allColumns.add(createSimpleDynamicMapping(dbIdFieldName, efMeta.getField(), DT_LONG.getName(), efMeta.isReadonly(), true, efMeta.isDynamic()));
						}
					}
				}
			}
		}

		dbSourceForSQL = this.eMeta.getDbEntity();
		dbSourceForDML = "T_" + eMeta.getDbEntity().substring(2);

		/**
		 * Logical Unique Constraints
		 */
		if (eMeta.getLogicalUniqueFieldCombinations() != null) {
			for (Set<String> lufCombination : eMeta.getLogicalUniqueFieldCombinations()) {
				List<ColumnToVOMapping<?>> ctovMappings = new ArrayList<ColumnToVOMapping<?>>(lufCombination.size());

				for (String luField : lufCombination) {
					for (ColumnToVOMapping<?> ctovMap : allColumns) {
						if (!ctovMap.isReadonly && luField.equals(ctovMap.field)) {
							ctovMappings.add(ctovMap);
						}
					}
				}

				logicalUniqueConstraintCombinations.add(ctovMappings);
			}
		}
	}

	private boolean isIdColumnInList(List<ColumnToVOMapping<?>> list, String columnName) {
		return getColumnFromList(list, columnName) != null;
	}

	private ColumnToVOMapping<?> getColumnFromList(List<ColumnToVOMapping<?>> list, String columnName) {
		for (ColumnToVOMapping<?> column : list) {
			if (column.column.equals(columnName)) {
				return column;
			}
		}
		return null;
	}

	private static int countFieldsForInitiatingFieldMap(Collection<EntityFieldMetaDataVO> colEfMeta) {
		int result = colEfMeta.size();
		for (EntityFieldMetaDataVO efMeta : colEfMeta)  {
			if (staticSystemFields.contains(efMeta.getField())) {
				// only subtract a static system field if it exists in colEfMeta.
				// static fields are not stored in field map.
				result--;
			}
		}
		return result;
	}

	private static int countIdFieldsForInitiatingFieldMap(Collection<EntityFieldMetaDataVO> colEfMeta) {
		int result = 0;
		for (EntityFieldMetaDataVO efMeta : colEfMeta)  {
			if (efMeta.getForeignEntity() != null) {
				result++;
			}
		}
		return result;
	}

	@Override
	protected EntityObjectVO newDalVOInstance() {
		EntityObjectVO newDalVO = super.newDalVOInstance();
		newDalVO.setEntity(eMeta.getEntity());
		return newDalVO;
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
	protected ColumnToVOMapping<Long> getPrimaryKeyColumn() {
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
			query.where(pkExpr.as(Long.class).in(idSubList));
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
		List<ColumnToVOMapping<?>> columns = fields == null ? allColumns : getColumns(fields, bSearchInDMLSource);

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
        DbQuery<Long> query = DataBaseHelper.getDbAccess().getQueryBuilder().<Long>createQuery(getPrimaryKeyColumn().dataType);

		DbFrom from = query.from(getDbSourceForSQL()).alias("t");

		query.select(this.<Long>getDbColumn(from, getPrimaryKeyColumn()));

		EOSearchExpressionUnparser unparser = new EOSearchExpressionUnparser(query, eMeta);
		unparser.unparseSearchCondition(getSearchConditionWithDeletedAndVLP(searchExpression));
		unparser.unparseSortingOrder(searchExpression.getSortingOrder());

		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Long> groupsubquery = builder.createQuery(getPrimaryKeyColumn().dataType);
		DbFrom sqfrom = groupsubquery.from("t_ud_genericobject").alias("g");
		sqfrom.innerJoin(getDbSourceForDML()).on("intid", "intid").alias("eo");
		DbJoin role_mod = sqfrom.innerJoin("t_md_role_module").on("intid_t_md_module", "intid_t_md_module").alias("rm");
		DbJoin role_user = role_mod.innerJoin("t_md_role_user").on("intid_t_md_role", "intid_t_md_role").alias("ru");
		DbJoin md_user = role_user.innerJoin("t_md_user").on("intid_t_md_user", "intid").alias("us");
		DbJoin go_group = sqfrom.leftJoin("t_ud_go_group").on("INTID", "INTID_T_UD_GENERICOBJECT").alias("gg");

		groupsubquery.select(sqfrom.column("intid", Long.class));

		DbCondition usercondition = builder.equal(builder.upper(md_user.column("STRUSER", String.class)), builder.upper(builder.literal(user)));
		DbCondition groupcondition = groupsubquery.getBuilder().or(
			groupsubquery.getBuilder().isNull(role_mod.column("intid_t_ud_group", Long.class)),
			groupsubquery.getBuilder().and(
				groupsubquery.getBuilder().isNotNull(go_group.column("intid_t_ud_group", Long.class)),
				groupsubquery.getBuilder().equal(go_group.column("intid_t_ud_group", Long.class), role_mod.column("intid_t_ud_group", Long.class))));

		DbCondition userandgroup = query.getBuilder().and(usercondition, groupcondition);
		groupsubquery.where(userandgroup);

		DbCondition objectgroup = from.column("intid", Long.class).in(groupsubquery);

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

	/**
	 *
	 * @param result
	 * @param dalVO
	 * @return
	 */
	private DalCallResult checkLogicalUniqueConstraint(DalCallResult result, EntityObjectVO dalVO) {
		if (!logicalUniqueConstraintCombinations.isEmpty()) {
			for (List<ColumnToVOMapping<?>> columns : logicalUniqueConstraintCombinations) {

				final Map<ColumnToVOMapping<?>, Object> checkValues = super.getColumnValuesMapWithMapping(columns, dalVO, false);
				final DalBusinessException checkResult = super.checkLogicalUniqueConstraint(checkValues, dalVO.getId());
				if (checkResult != null)
					result.addBusinessException(checkResult);
			}
		}
		return result;
	}

	/**
	 *
	 * @param fields
	 * @param bSearchInDMLSource
	 * @return
	 */
	private List<ColumnToVOMapping<?>> getColumns(final Collection<String> fields, final boolean bSearchInDMLSource) {
		return CollectionUtils.select(allColumns, new Predicate<ColumnToVOMapping<?>>() {
			@Override
			public boolean evaluate(ColumnToVOMapping<?> t) {
				return fields.contains(t.field) && (!bSearchInDMLSource || (bSearchInDMLSource && !t.isReadonly));
			}});
	}

}
