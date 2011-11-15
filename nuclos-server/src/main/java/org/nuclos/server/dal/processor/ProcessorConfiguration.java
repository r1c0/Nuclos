//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.server.dal.processor;

import java.util.List;

import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.IDalVO;

public class ProcessorConfiguration {
	
	private final Class<? extends IDalVO> type;

	private final EntityMetaDataVO eMeta;
	
	private final List<IColumnToVOMapping<? extends Object>> allColumns;
	
	private final IColumnToVOMapping<Long> idColumn;
	
	private final IColumnToVOMapping<Integer> versionColumn;
	
	private final boolean addSystemColumns;
	
	private final int maxFieldCount;
	
	private final int maxFieldIdCount;
	
	public ProcessorConfiguration(Class<? extends IDalVO> type, EntityMetaDataVO eMeta, List<IColumnToVOMapping<? extends Object>> allColumns, 
			IColumnToVOMapping<Long> idColumn, IColumnToVOMapping<Integer> versionColumn, boolean addSystemColumns, 
			int maxFieldCount, int maxFieldIdCount) {
		
		this.type = type;
		this.eMeta = eMeta;
		this.allColumns = allColumns;
		this.idColumn = idColumn;
		this.versionColumn = versionColumn;
		this.addSystemColumns = addSystemColumns;
		this.maxFieldCount = maxFieldCount;
		this.maxFieldIdCount = maxFieldIdCount;
	}
	
	public Class<? extends IDalVO> getDalType() {
		return type;
	}

	public EntityMetaDataVO geteMeta() {
		return eMeta;
	}

	public List<IColumnToVOMapping<? extends Object>> getAllColumns() {
		return allColumns;
	}

	public IColumnToVOMapping<Long> getIdColumn() {
		return idColumn;
	}

	public IColumnToVOMapping<Integer> getVersionColumn() {
		return versionColumn;
	}

	public boolean isAddSystemColumns() {
		return addSystemColumns;
	}

	public int getMaxFieldCount() {
		return maxFieldCount;
	}

	public int getMaxFieldIdCount() {
		return maxFieldIdCount;
	}
	
}
