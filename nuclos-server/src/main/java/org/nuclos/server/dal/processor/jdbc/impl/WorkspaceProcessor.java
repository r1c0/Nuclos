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
import java.util.List;
import java.util.Map;

import org.nuclos.common.Actions;
import org.nuclos.common.WorkspaceVO;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.dal.processor.IColumnToVOMapping;
import org.nuclos.server.dal.processor.jdbc.AbstractJdbcDalProcessor;
import org.nuclos.server.dal.processor.nuclet.IWorkspaceProcessor;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbJoin;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;

public class WorkspaceProcessor extends AbstractJdbcDalProcessor<WorkspaceVO> implements IWorkspaceProcessor{
	
	private final IColumnToVOMapping<Long> idColumn; 
	private final IColumnToVOMapping<Long> userColumn;
	private final IColumnToVOMapping<String> nameColumn;
	private final IColumnToVOMapping<Long> assignedColumn;
	
	List<IColumnToVOMapping<?>> logicalUniqueConstraintCombinations;

	private final List<IColumnToVOMapping<? extends Object>> updateCheckColumnList;
	
	public WorkspaceProcessor(List<IColumnToVOMapping<? extends Object>> allColumns, IColumnToVOMapping<Long> idColumn, IColumnToVOMapping<Long> userColumn, IColumnToVOMapping<String> nameColumn, IColumnToVOMapping<Long> assignedColumn) {
		super(WorkspaceVO.class, allColumns);
		
		this.idColumn = idColumn;
		this.userColumn = userColumn;
		this.nameColumn = nameColumn;
		this.assignedColumn = assignedColumn;
		
		updateCheckColumnList = new ArrayList<IColumnToVOMapping<? extends Object>>();
		updateCheckColumnList.add(idColumn);
		
		logicalUniqueConstraintCombinations = new ArrayList<IColumnToVOMapping<?>>();
		logicalUniqueConstraintCombinations.add(this.userColumn);
		logicalUniqueConstraintCombinations.add(this.nameColumn);
		logicalUniqueConstraintCombinations.add(this.assignedColumn);
	}

	@Override
	public WorkspaceVO getByPrimaryKey(Long id) {
		return super.getByPrimaryKey(id);
	}

	@Override
	public void insertOrUpdate(WorkspaceVO dalVO) throws DbException {
		if (dalVO.isFlagUpdated()) {
			// test if exists
			if (getByPrimaryKey(updateCheckColumnList, dalVO.getId()) == null) {
				throw new DbException("Workspace.not.found");
			}
		}
		super.insertOrUpdate(dalVO);
		checkLogicalUniqueConstraint(dalVO);
	}

	@Override
	public DalCallResult batchInsertOrUpdate(Collection<WorkspaceVO> colDalVO, boolean failAfterBatch)
			throws DbException {
		return super.batchInsertOrUpdate(colDalVO, failAfterBatch);
	}

	@Override
	public void delete(Long id) throws DbException {
		super.delete(id);
	}
	
	@Override
	public DalCallResult batchDelete(Collection<Long> colId, boolean failAfterBatch) throws DbException {
		for (Long id : colId) {
			deleteAllByAssigned(id);
		}
		return super.batchDelete(colId, failAfterBatch);
	}
	
	@Override
	public List<WorkspaceVO> getAll() {
		return super.getAll();
	}
	
	@Override
	public List<Long> getAllIds() {
		return super.getAllIds();
	}
	
	@Override
	public List<WorkspaceVO> getByPrimaryKeys(List<Long> ids) {
		return super.getByPrimaryKeys(allColumns, ids);
	}

	@Override
	protected String getDbSourceForDML() {
		return "T_MD_WORKSPACE";
	}

	@Override
	protected String getDbSourceForSQL() {
		return "T_MD_WORKSPACE";
	}

	@Override
	protected IColumnToVOMapping<Long> getPrimaryKeyColumn() {
		return idColumn;
	}
	
	@Override
	public void deleteByAssigned(Long assignedId) throws DbException {
		final DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		final DbQuery<DbTuple> query = builder.createTupleQuery();
		final DbFrom from = query.from(getDbSourceForSQL()).alias(SystemFields.BASE_ALIAS);
		final DbJoin join = from.innerJoin("T_MD_USER").alias("t2").on("INTID_T_MD_USER", "INTID", Long.class);
		
		query.multiselect(
				from.baseColumn(idColumn.getColumn(), idColumn.getDataType()),
				join.baseColumn("STRUSER", String.class));
		
		final DbQuery<Long> rousQuery = builder.createQuery(Long.class);
		final DbFrom rousFrom = rousQuery.from("T_MD_ROLE_USER").alias("ROUS");
		rousQuery.select(rousFrom.baseColumn("INTID_T_MD_ROLE", Long.class));
		rousQuery.where(builder.equal(rousFrom.baseColumn("INTID_T_MD_USER", Long.class), from.baseColumn(userColumn.getColumn(), userColumn.getDataType())));
		
		final DbQuery<Long> rowoQuery = builder.createQuery(Long.class);
		final DbFrom rowoFrom = rowoQuery.from("T_MD_ROLE_WORKSPACE").alias("ROWO");
		rowoQuery.select(rowoFrom.baseColumn("INTID_T_MD_WORKSPACE", Long.class));
		rowoQuery.where(rowoFrom.baseColumn("INTID_T_MD_ROLE", Long.class).in(rousQuery));
		
		query.where(builder.equal(from.baseColumn(assignedColumn.getColumn(), assignedColumn.getDataType()), assignedId));
		query.addToWhereAsAnd(builder.not(from.baseColumn(assignedColumn.getColumn(), assignedColumn.getDataType()).in(rowoQuery)));
		
		for (DbTuple tuple : DataBaseHelper.getDbAccess().executeQuery(query)) {
			Long id = (Long) tuple.get(0);
			String user = (String) tuple.get(1);
			if (!SecurityCache.getInstance().getAllowedActions(user).contains(Actions.ACTION_WORKSPACE_ASSIGN)) {
				this.delete(id);
			}
		}
	}
	
	private void deleteAllByAssigned(Long assignedId) throws DbException {
		DataBaseHelper.getDbAccess().execute(DbStatementUtils.deleteFrom(getDbSourceForDML(), assignedColumn.getColumn(), assignedId));
	}

	@Override
	public List<WorkspaceVO> getByUser(String user) {
		final Long userId = SecurityCache.getInstance().getUserId(user).longValue();
		
		final DbQuery<Object[]> query = createQuery(allColumns);
		final DbFrom from = CollectionUtils.getSingleIfExist(query.getRoots());
		final DbQueryBuilder builder = query.getBuilder();
		if (userId == null) {
			query.where(builder.isNull(from.baseColumn(userColumn.getColumn(), userColumn.getDataType())));
		} else {
			query.where(builder.equal(from.baseColumn(userColumn.getColumn(), userColumn.getDataType()), userId));
		}
		
		return DataBaseHelper.getDbAccess().executeQuery(query, createResultTransformer(allColumns));
	}
	
	@Override
	public List<WorkspaceVO> getNewAssigned(String user) {
		final Long userId = SecurityCache.getInstance().getUserId(user).longValue();
		final boolean assigner = SecurityCache.getInstance().getAllowedActions(user).contains(Actions.ACTION_WORKSPACE_ASSIGN);
		
		final DbQuery<Object[]> query = createQuery(allColumns);
		final DbFrom from = CollectionUtils.getSingleIfExist(query.getRoots());
		final DbQueryBuilder builder = query.getBuilder();
		
		final DbQuery<Long> rousQuery = builder.createQuery(Long.class);
		final DbFrom rousFrom = rousQuery.from("T_MD_ROLE_USER").alias("ROUS");
		rousQuery.select(rousFrom.baseColumn("INTID_T_MD_ROLE", Long.class));
		rousQuery.where(builder.equal(rousFrom.baseColumn("INTID_T_MD_USER", Long.class), userId));
		
		final DbQuery<Long> rowoQuery = builder.createQuery(Long.class);
		final DbFrom rowoFrom = rowoQuery.from("T_MD_ROLE_WORKSPACE").alias("ROWO");
		rowoQuery.select(rowoFrom.baseColumn("INTID_T_MD_WORKSPACE", Long.class));
		rowoQuery.where(rowoFrom.baseColumn("INTID_T_MD_ROLE", Long.class).in(rousQuery));
		
		final DbQuery<Long> aswoQuery = builder.createQuery(Long.class);
		final DbFrom aswoFrom = aswoQuery.from("T_MD_WORKSPACE").alias("ASWO");
		aswoQuery.select(aswoFrom.baseColumn("INTID_T_MD_WORKSPACE", Long.class));
		aswoQuery.where(builder.equal(aswoFrom.baseColumn("INTID_T_MD_USER", Long.class), userId));
		aswoQuery.addToWhereAsAnd(builder.isNotNull(aswoFrom.baseColumn("INTID_T_MD_WORKSPACE", Long.class)));
		
		query.where(builder.or(assigner? builder.alwaysTrue() : builder.alwaysFalse(),
				               from.baseColumn(idColumn.getColumn(), idColumn.getDataType()).in(rowoQuery)));
		query.addToWhereAsAnd(builder.not(from.baseColumn(idColumn.getColumn(), idColumn.getDataType()).in(aswoQuery)));
		query.addToWhereAsAnd(builder.isNull(from.baseColumn(userColumn.getColumn(), userColumn.getDataType())));
		
		return DataBaseHelper.getDbAccess().executeQuery(query, createResultTransformer(allColumns));
	}
	
	private void checkLogicalUniqueConstraint(WorkspaceVO wovo) throws DbException {
		final Map<IColumnToVOMapping<?>, Object> checkValues = super.getColumnValuesMapWithMapping(logicalUniqueConstraintCombinations, wovo, false);
		final SQLIntegrityConstraintViolationException checkResult = super.checkLogicalUniqueConstraint(checkValues, wovo.getId());
		if (checkResult != null)
			throw new DbException("Workspace.name.in.use");
	}
}
