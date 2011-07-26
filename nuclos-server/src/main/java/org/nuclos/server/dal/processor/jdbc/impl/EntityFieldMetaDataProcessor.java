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
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.server.dal.processor.jdbc.AbstractJdbcDalProcessor;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityFieldMetaDataProcessor;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;

public class EntityFieldMetaDataProcessor extends AbstractJdbcDalProcessor<Object,EntityFieldMetaDataVO> 
implements JdbcEntityFieldMetaDataProcessor{
	
	private final ColumnToVOMapping<Long> idColumn;
	private final ColumnToVOMapping<Long> entityIdColumn;
	
	public EntityFieldMetaDataProcessor() {
		super();
		
		idColumn = createSimpleStaticMapping("INTID", "id", DT_LONG);
		allColumns.add(idColumn);
		allColumns.add(createSimpleStaticMapping("DATCREATED", "createdAt", DT_INTERNALTIMESTAMP));
		allColumns.add(createSimpleStaticMapping("STRCREATED", "createdBy", DT_STRING));
		allColumns.add(createSimpleStaticMapping("DATCHANGED", "changedAt", DT_INTERNALTIMESTAMP));
		allColumns.add(createSimpleStaticMapping("STRCHANGED", "changedBy", DT_STRING));
		allColumns.add(createSimpleStaticMapping("INTVERSION", "version", DT_INTEGER));
		
		entityIdColumn = createSimpleStaticMapping("INTID_T_MD_ENTITY", "entityId", DT_LONG);
		allColumns.add(entityIdColumn);
		allColumns.add(createSimpleStaticMapping("INTID_T_MD_ENTITY_FIELD_GROUP", "fieldGroupId", DT_LONG));
		allColumns.add(createSimpleStaticMapping("STRFIELD", "field", DT_STRING));
		allColumns.add(createSimpleStaticMapping("STRDBFIELD", "dbColumn", DT_STRING));
		
		allColumns.add(createSimpleStaticMapping("STRFOREIGNENTITY", "foreignEntity", DT_STRING));
		allColumns.add(createSimpleStaticMapping("STRFOREIGNENTITYFIELD", "foreignEntityField", DT_STRING));
		
		allColumns.add(createSimpleStaticMapping("STRDATATYPE", "dataType", DT_STRING));
		allColumns.add(createSimpleStaticMapping("INTDATASCALE", "scale", DT_INTEGER));
		allColumns.add(createSimpleStaticMapping("INTDATAPRECISION", "precision", DT_INTEGER));
		allColumns.add(createSimpleStaticMapping("STRFORMATINPUT", "formatInput", DT_STRING));
		allColumns.add(createSimpleStaticMapping("STRFORMATOUTPUT", "formatOutput", DT_STRING));
		
		allColumns.add(createSimpleStaticMapping("INTID_FOREIGN_DEFAULT", "defaultForeignId", DT_LONG));
		allColumns.add(createSimpleStaticMapping("STRVALUE_DEFAULT", "defaultValue", DT_STRING));
		
		allColumns.add(createSimpleStaticMapping("BLNREADONLY", "readonly", DT_BOOLEAN));
		allColumns.add(createSimpleStaticMapping("BLNUNIQUE", "unique", DT_BOOLEAN));
		allColumns.add(createSimpleStaticMapping("BLNNULLABLE", "nullable", DT_BOOLEAN));
		allColumns.add(createSimpleStaticMapping("BLNINDEXED", "indexed", DT_BOOLEAN));
		allColumns.add(createSimpleStaticMapping("BLNSEARCHABLE", "searchable", DT_BOOLEAN));
		allColumns.add(createSimpleStaticMapping("BLNMODIFIABLE", "modifiable", DT_BOOLEAN));
		allColumns.add(createSimpleStaticMapping("BLNINSERTABLE", "insertable", DT_BOOLEAN));
		allColumns.add(createSimpleStaticMapping("BLNLOGBOOKTRACKING", "logBookTracking", DT_BOOLEAN));
		allColumns.add(createSimpleStaticMapping("BLNSHOWMNEMONIC", "showMnemonic", DT_BOOLEAN));
		
		allColumns.add(createSimpleStaticMapping("STRCALCFUNCTION", "calcFunction", DT_STRING));
		allColumns.add(createSimpleStaticMapping("STRSORTATIONASC", "sortorderASC", DT_STRING));
		allColumns.add(createSimpleStaticMapping("STRSORTATIONDESC", "sortorderDESC", DT_STRING));
		
		allColumns.add(createSimpleStaticMapping("STR_LOCALERESOURCE_L", "localeResourceIdForLabel", DT_STRING));
		allColumns.add(createSimpleStaticMapping("STR_LOCALERESOURCE_D", "localeResourceIdForDescription", DT_STRING));
		
		allColumns.add(createSimpleStaticMapping("STR_DEFAULT_MANDATORY", "defaultMandatory", DT_STRING));
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
	protected ColumnToVOMapping<Long> getPrimaryKeyColumn() {
		return idColumn;
	}

	@Override
	public List<EntityFieldMetaDataVO> getAll() {
		return super.getAll();
	}

	@Override
	public List<EntityFieldMetaDataVO> getByParent(String entity) {

		DbQuery<Long> query = DataBaseHelper.getDbAccess().getQueryBuilder().createQuery(Long.class);
		DbFrom from = query.from("T_MD_ENTITY");
		from.alias("entity");
		query.select(from.column("INTID", DT_LONG));
		query.where(query.getBuilder().equal(from.column("STRENTITY", DT_STRING), entity));
		
		return super.getByIdColumn(allColumns, entityIdColumn, DataBaseHelper.getDbAccess().executeQuerySingleResult(query));
	}

	@Override
	public DalCallResult delete(Long id) {
		return super.delete(id);
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
	public DalCallResult insertOrUpdate(EntityFieldMetaDataVO dalVO) {
		return super.insertOrUpdate(dalVO);
	}
	
	@Override
	public DalCallResult batchDelete(Collection<Long> colId) {
		return super.batchDelete(colId);
	}

	@Override
	public DalCallResult batchInsertOrUpdate(Collection<EntityFieldMetaDataVO> colDalVO) {
		return super.batchInsertOrUpdate(colDalVO);
	}

}
