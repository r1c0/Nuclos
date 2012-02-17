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

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.server.dal.processor.IColumnToVOMapping;
import org.nuclos.server.dal.processor.jdbc.AbstractJdbcDalProcessor;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityFieldMetaDataProcessor;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;

public class EntityFieldMetaDataProcessor extends AbstractJdbcDalProcessor<EntityFieldMetaDataVO> 
implements JdbcEntityFieldMetaDataProcessor{
	
	private final IColumnToVOMapping<Long> idColumn;
	private final IColumnToVOMapping<Long> entityIdColumn;
	
	public EntityFieldMetaDataProcessor(List<IColumnToVOMapping<? extends Object>> allColumns, 
			IColumnToVOMapping<Long> entityIdColumn, IColumnToVOMapping<Long> idColumn) {
		super(EntityFieldMetaDataVO.class, allColumns);
		this.entityIdColumn = entityIdColumn;
		this.idColumn = idColumn;
	}
	
	@Override
	public String getDbSourceForDML() {
		return "T_MD_ENTITY_FIELD";
	}

	@Override
	public String getDbSourceForSQL() {
		return "T_MD_ENTITY_FIELD";
	}
	
	@Override
	protected IColumnToVOMapping<Long> getPrimaryKeyColumn() {
		return idColumn;
	}

	@Override
	public List<EntityFieldMetaDataVO> getAll() {
		return super.getAll();
	}

	@Override
	public List<EntityFieldMetaDataVO> getByParent(String entity) {

		DbQuery<Long> query = dataBaseHelper.getDbAccess().getQueryBuilder().createQuery(Long.class);
		DbFrom from = query.from("T_MD_ENTITY");
		from.alias("entity");
		query.select(from.baseColumn("INTID", DT_LONG));
		query.where(query.getBuilder().equal(from.baseColumn("STRENTITY", DT_STRING), entity));
		
		return super.getByIdColumn(allColumns, entityIdColumn, dataBaseHelper.getDbAccess().executeQuerySingleResult(query));
	}

	@Override
	public void delete(Long id) throws DbException {
		super.delete(id);
	}

	@Override
	public EntityFieldMetaDataVO getByPrimaryKey(Long id) {
		return super.getByPrimaryKey(id);
	}
	
	@Override
	public List<EntityFieldMetaDataVO> getByPrimaryKeys(List<Long> ids) {
		return super.getByPrimaryKeys(allColumns, ids);
	}

	@Override
	public void insertOrUpdate(EntityFieldMetaDataVO dalVO) {
		super.insertOrUpdate(dalVO);
	}
	
	@Override
	public DalCallResult batchDelete(Collection<Long> colId, boolean failAfterBatch) {
		return super.batchDelete(colId, failAfterBatch);
	}

	@Override
	public DalCallResult batchInsertOrUpdate(Collection<EntityFieldMetaDataVO> colDalVO, boolean failAfterBatch) {
		return super.batchInsertOrUpdate(colDalVO, failAfterBatch);
	}

	@Override
	public void checkLogicalUniqueConstraint(EntityObjectVO dalVO) throws DbException {
		throw new NotImplementedException();
	}

}
