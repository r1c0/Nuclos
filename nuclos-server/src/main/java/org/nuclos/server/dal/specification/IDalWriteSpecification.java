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
package org.nuclos.server.dal.specification;

import java.util.Collection;

import org.nuclos.common.dal.DalCallResult;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.IDalVO;
import org.nuclos.server.dblayer.DbException;

public interface IDalWriteSpecification<DalVO extends IDalVO> {
	
	void insertOrUpdate(DalVO dalVO) throws DbException;
	
	void checkLogicalUniqueConstraint(EntityObjectVO dalVO) throws DbException;
	
	void delete(Long id) throws DbException;
	
	DalCallResult batchInsertOrUpdate(Collection<DalVO> colDalVO, boolean failAfterBatch) throws DbException;
	
	DalCallResult batchDelete(Collection<Long> colId, boolean failAfterBatch) throws DbException;
}
