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

import org.nuclos.common.dal.DalCallResult;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.server.dal.processor.jdbc.AbstractJdbcDalProcessor;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityMetaDataProcessor;

public class EntityMetaDataProcessor extends AbstractJdbcDalProcessor<EntityMetaDataVO> 
implements JdbcEntityMetaDataProcessor {	
	private final ColumnToVOMapping<Long> idColumn;
	
	public EntityMetaDataProcessor() {
		super();
		
		idColumn = createSimpleStaticMapping("INTID", "id", DT_LONG);
		allColumns.add(idColumn);
		allColumns.add(createSimpleStaticMapping("DATCREATED", "createdAt", DT_INTERNALTIMESTAMP));
		allColumns.add(createSimpleStaticMapping("STRCREATED", "createdBy", DT_STRING));
		allColumns.add(createSimpleStaticMapping("DATCHANGED", "changedAt", DT_INTERNALTIMESTAMP));
		allColumns.add(createSimpleStaticMapping("STRCHANGED", "changedBy", DT_STRING));
		allColumns.add(createSimpleStaticMapping("INTVERSION", "version", DT_INTEGER));
		
		allColumns.add(createSimpleStaticMapping("STRENTITY", "entity", DT_STRING));
		allColumns.add(createSimpleStaticMapping("STRDBENTITY", "dbEntity", DT_STRING));
		
		allColumns.add(createSimpleStaticMapping("STRSYSTEMIDPREFIX", "systemIdPrefix", DT_STRING));
		allColumns.add(createSimpleStaticMapping("STRMENUSHORTCUT", "menuShortcut", DT_STRING));
		
		allColumns.add(createSimpleStaticMapping("BLNEDITABLE", "editable", DT_BOOLEAN));
		allColumns.add(createSimpleStaticMapping("BLNUSESSTATEMODEL", "stateModel", DT_BOOLEAN));
		allColumns.add(createSimpleStaticMapping("BLNLOGBOOKTRACKING", "logBookTracking", DT_BOOLEAN));
		allColumns.add(createSimpleStaticMapping("BLNCACHEABLE", "cacheable", DT_BOOLEAN));
		allColumns.add(createSimpleStaticMapping("BLNSEARCHABLE", "searchable", DT_BOOLEAN));
		allColumns.add(createSimpleStaticMapping("BLNTREERELATION", "treeRelation", DT_BOOLEAN));
		allColumns.add(createSimpleStaticMapping("BLNTREEGROUP", "treeGroup", DT_BOOLEAN));
		allColumns.add(createSimpleStaticMapping("BLNIMPORTEXPORT", "importExport", DT_BOOLEAN));
		allColumns.add(createSimpleStaticMapping("BLNFIELDVALUEENTITY", "fieldValueEntity", DT_BOOLEAN));
		
		allColumns.add(createSimpleStaticMapping("STRACCELERATOR", "accelerator", DT_STRING));
		allColumns.add(createSimpleStaticMapping("INTACCELERATORMODIFIER", "acceleratorModifier", DT_INTEGER));
		allColumns.add(createSimpleStaticMapping("STRFIELDS_FOR_EQUALITY", "fieldsForEquality", DT_STRING));
		allColumns.add(createSimpleStaticMapping("INTID_T_MD_RESOURCE", "resourceId", DT_INTEGER));
		allColumns.add(createSimpleStaticMapping("STRNUCLOSRESOURCE", "nuclosResource", DT_STRING));
		allColumns.add(createSimpleStaticMapping("STR_LOCALERESOURCE_L", "localeResourceIdForLabel", DT_STRING));
		allColumns.add(createSimpleStaticMapping("STR_LOCALERESOURCE_M", "localeResourceIdForMenuPath", DT_STRING));
		allColumns.add(createSimpleStaticMapping("STR_LOCALERESOURCE_D", "localeResourceIdForDescription", DT_STRING));
		allColumns.add(createSimpleStaticMapping("STR_LOCALERESOURCE_TW", "localeResourceIdForTreeView", DT_STRING));
		allColumns.add(createSimpleStaticMapping("STR_LOCALERESOURCE_TT", "localeResourceIdForTreeViewDescription", DT_STRING));
		
		allColumns.add(createSimpleStaticMapping("STR_DOCUMENTPATH", "documentPath", DT_STRING));
		allColumns.add(createSimpleStaticMapping("STR_REPORTFILENAME", "reportFilename", DT_STRING));
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
	protected ColumnToVOMapping<Long> getPrimaryKeyColumn() {
		return idColumn;
	}

	@Override
	public DalCallResult delete(Long id) {
		return super.delete(id);
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
	public DalCallResult insertOrUpdate(EntityMetaDataVO dalVO) {
		return super.insertOrUpdate(dalVO);
	}

	@Override
	public DalCallResult batchInsertOrUpdate(Collection<EntityMetaDataVO> colDalVO) {
		return super.batchInsertOrUpdate(colDalVO);
	}

	@Override
	public DalCallResult batchDelete(Collection<Long> colId) {
		return super.batchDelete(colId);
	}	
	
}
