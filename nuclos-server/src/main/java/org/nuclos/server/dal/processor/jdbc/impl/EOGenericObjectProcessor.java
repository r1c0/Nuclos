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
import org.nuclos.common.dal.vo.EOGenericObjectVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.server.dal.processor.IColumnToVOMapping;
import org.nuclos.server.dal.processor.jdbc.AbstractJdbcDalProcessor;
import org.nuclos.server.dal.processor.nuclet.IEOGenericObjectProcessor;
import org.nuclos.server.dblayer.DbException;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class EOGenericObjectProcessor extends AbstractJdbcDalProcessor<EOGenericObjectVO>
	implements IEOGenericObjectProcessor {
	
	private IColumnToVOMapping<Long> idColumn; 
	private IColumnToVOMapping<Long> moduleColumn;
	
	public EOGenericObjectProcessor(List<IColumnToVOMapping<? extends Object>> allColumns, 
			IColumnToVOMapping<Long> moduleColumn, IColumnToVOMapping<Long> idColumn) {
		super(EOGenericObjectVO.class, allColumns);
		this.moduleColumn = moduleColumn;
		this.idColumn = idColumn;
	}

	@Override
	protected String getDbSourceForDML() {
		return "T_UD_GENERICOBJECT";
	}

	@Override
	protected String getDbSourceForSQL() {
		return "T_UD_GENERICOBJECT";
	}

	@Override
	protected IColumnToVOMapping<Long> getPrimaryKeyColumn() {
		return idColumn;
	}

	@Override
	public DalCallResult batchDelete(Collection<Long> colId, boolean failAfterBatch) {
		return super.batchDelete(colId, failAfterBatch);
	}

	@Override
	public DalCallResult batchInsertOrUpdate(Collection<EOGenericObjectVO> colDalVO, boolean failAfterBatch) {
		return super.batchInsertOrUpdate(colDalVO, failAfterBatch);
	}

	@Override
	public void delete(Long id) throws DbException {
		super.delete(id);
	}

	@Override
	public void insertOrUpdate(EOGenericObjectVO dalVO) {
		super.insertOrUpdate(dalVO);
	}

	@Override
	public List<EOGenericObjectVO> getAll() {
		return super.getAll();
	}

	@Override
	public EOGenericObjectVO getByPrimaryKey(Long id) {
		return super.getByPrimaryKey(id);
	}

	@Override
	public List<EOGenericObjectVO> getByPrimaryKeys(List<Long> ids) {
		return super.getByPrimaryKeys(allColumns, ids);
	}

	@Override
	public List<EOGenericObjectVO> getByParent(Long parentId) {
		return super.getByIdColumn(allColumns, moduleColumn, parentId);
	}
	
	@Override
	public void checkLogicalUniqueConstraint(EntityObjectVO dalVO) throws DbException {
		throw new NotImplementedException();
	}
}
