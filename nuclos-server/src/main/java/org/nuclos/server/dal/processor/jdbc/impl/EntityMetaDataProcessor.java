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
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.server.dal.processor.IColumnToVOMapping;
import org.nuclos.server.dal.processor.jdbc.AbstractJdbcDalProcessor;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityMetaDataProcessor;
import org.nuclos.server.dblayer.DbException;

public class EntityMetaDataProcessor extends AbstractJdbcDalProcessor<EntityMetaDataVO> 
implements JdbcEntityMetaDataProcessor {
	
	private final IColumnToVOMapping<Long> idColumn;
	
	public EntityMetaDataProcessor(List<IColumnToVOMapping<? extends Object>> allColumns, 
			IColumnToVOMapping<Long> idColumn) {
		super(EntityMetaDataVO.class, allColumns);
		this.idColumn = idColumn;
	}
	
	@Override
	protected String getDbSourceForDML() {
		return "T_MD_ENTITY";
	}

	@Override
	protected String getDbSourceForSQL() {
		return "T_MD_ENTITY";
	}
	
	@Override
	protected IColumnToVOMapping<Long> getPrimaryKeyColumn() {
		return idColumn;
	}

	@Override
	public void delete(Long id) throws DbException {
		super.delete(id);
	}

	@Override
	public List<EntityMetaDataVO> getAll() {
		return super.getAll();
	}

	@Override
	public EntityMetaDataVO getByPrimaryKey(Long id) {
		return super.getByPrimaryKey(id);
	}
	
	@Override
	public List<EntityMetaDataVO> getByPrimaryKeys(List<Long> ids) {
		return super.getByPrimaryKeys(allColumns, ids);
	}

	@Override
	public void insertOrUpdate(EntityMetaDataVO dalVO) {
		super.insertOrUpdate(dalVO);
	}

	@Override
	public DalCallResult batchInsertOrUpdate(Collection<EntityMetaDataVO> colDalVO, boolean failAfterBatch) {
		return super.batchInsertOrUpdate(colDalVO, failAfterBatch);
	}

	@Override
	public DalCallResult batchDelete(Collection<Long> colId, boolean failAfterBatch) {
		return super.batchDelete(colId, failAfterBatch);
	}	
	
	@Override
	public void checkLogicalUniqueConstraint(EntityObjectVO dalVO) throws DbException {
		throw new NotImplementedException();
	}
	
}
