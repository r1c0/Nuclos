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

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.common.dal.vo.EOGenericObjectVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.processor.IColumnToVOMapping;
import org.nuclos.server.dal.processor.ProcessorConfiguration;
import org.nuclos.server.dal.processor.jdbc.AbstractJdbcDalProcessor;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.expression.DbIncrement;
import org.nuclos.server.dblayer.statements.DbInsertStatement;
import org.nuclos.server.dblayer.statements.DbTableStatement;
import org.nuclos.server.dblayer.statements.DbUpdateStatement;
import org.nuclos.server.fileimport.ImportStructure;

/**
 * Implementation for importing objects from files into database.
 * If the importstructure is configured to to updates, an update is tried at first. If no rows where updated, an insert is tried.
 *
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class ImportObjectProcessor extends EntityObjectProcessor {

	private static final Logger LOG = Logger.getLogger(AbstractJdbcDalProcessor.class);

	// private final EntityMetaDataVO meta;
	private final ImportStructure importStructure;
	private final List<IColumnToVOMapping<? extends Object>> valueColumnsInsert = new ArrayList<IColumnToVOMapping<? extends Object>>();
	private final List<IColumnToVOMapping<? extends Object>> valueColumnsUpdate = new ArrayList<IColumnToVOMapping<? extends Object>>();
	private final List<IColumnToVOMapping<? extends Object>> conditionColumns = new ArrayList<IColumnToVOMapping<? extends Object>>();

	private static final Set<String> systemFieldsForUpdate = new HashSet<String>();
	static {
		systemFieldsForUpdate.add(NuclosEOField.CHANGEDAT.getMetaData().getDbColumn());
		systemFieldsForUpdate.add(NuclosEOField.CHANGEDBY.getMetaData().getDbColumn());
		systemFieldsForUpdate.add("INTVERSION");
	}

	private int inserted = 0;
	private int updated = 0;

	public ImportObjectProcessor(ProcessorConfiguration config, ImportStructure structure) {
		super(config);
		this.importStructure = structure;
		for(IColumnToVOMapping<? extends Object> column : allColumns) {
			valueColumnsInsert.add(column);
			if(structure.getItems().containsKey(column.getField())) {
				valueColumnsUpdate.add(column);
			}
			else if (systemFieldsForUpdate.contains(column.getColumn())) {
				valueColumnsUpdate.add(column);
			}

			if(structure.getIdentifiers().contains(column.getField())) {
				conditionColumns.add(column);
			}
		}
	}

	@Override
	protected DalCallResult batchInsertOrUpdate(List<IColumnToVOMapping<? extends Object>> columns, Collection<EntityObjectVO> colDalVO) {
		DalCallResult dcr = new DalCallResult();
		for(EntityObjectVO dalVO : colDalVO) {
			Map<String, Object> columnValueMap = getColumnValuesMap(valueColumnsUpdate, dalVO, false);
			Map<String, Object> columnConditionMap = getColumnValuesMap(conditionColumns, dalVO, false);

			DbTableStatement stmt = null;
			try {
				boolean updated = false;
				if(importStructure.isUpdate()) {
					columnValueMap.put("INTVERSION", DbIncrement.INCREMENT);
					stmt = new DbUpdateStatement(getDbSourceForDML(), columnValueMap, columnConditionMap);
					updated = DataBaseHelper.getDbAccess().execute(stmt) > 0;
					if (updated) this.updated++;
				}

				dalVO.setId(DalUtils.getNextId());
				columnValueMap = getColumnValuesMap(valueColumnsInsert, dalVO, false);
				if(importStructure.isInsert() && !updated) {
					stmt = new DbInsertStatement(getDbSourceForDML(), columnValueMap);
					DataBaseHelper.getDbAccess().execute(stmt);
					inserted++;
				}

				if(eMeta.isStateModel() && !updated) {
					EOGenericObjectVO eogo = new EOGenericObjectVO();
					eogo.setId(dalVO.getId());
					eogo.setCreatedBy(dalVO.getCreatedBy());
					eogo.setCreatedAt(dalVO.getCreatedAt());
					eogo.setChangedBy(dalVO.getChangedBy());
					eogo.setChangedAt(dalVO.getChangedAt());
					eogo.setModuleId(eMeta.getId());
					eogo.setVersion(1);
					eogo.flagNew();
					NucletDalProvider.getInstance().getEOGenericObjectProcessor().insertOrUpdate(eogo);
				}
			}
			catch(DbException ex) {
            	ex.setIdIfNull(dalVO.getId());
				dcr.addBusinessException(ex);
			}
		}
		return dcr;
	}

	public int getInserted() {
		return inserted;
	}

	public int getUpdated() {
		return updated;
	}
	
}
