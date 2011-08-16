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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.dal.util.DalTransformations;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.dal.specification.IDalReadSpecification;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.structure.DbColumn;
import org.nuclos.server.dblayer.structure.DbColumnType;
import org.nuclos.server.dblayer.structure.DbTable;
import org.nuclos.server.dblayer.structure.DbTableType;

public class DynamicMetaDataProcessor implements IDalReadSpecification<EntityMetaDataVO> {
	
	public static final String DYNAMIC_ENTITY_VIEW_PREFIX = "V_DE_";//if you change this value, change the exception text <datasource.validation.dynamic.entity.name.1> too.
	public static final String DYNAMIC_ENTITY_PREFIX = "dyn_";

	private static final Logger LOG = Logger.getLogger(DynamicMetaDataProcessor.class);
	
	public DynamicMetaDataProcessor() {
	}

	@Override
    public List<EntityMetaDataVO> getAll() {
	    return getDynamicEntities();
    }

	@Override
	public EntityMetaDataVO getByPrimaryKey(final Long id) {
		return CollectionUtils.findFirst(getAll(), new Predicate<EntityMetaDataVO>() {
			@Override
            public boolean evaluate(EntityMetaDataVO t) {
	            return id.equals(t.getId());
            }});
	}

	@Override
	public List<EntityMetaDataVO> getByPrimaryKeys(final List<Long> ids) {
		return CollectionUtils.applyFilter(getAll(), new Predicate<EntityMetaDataVO>() {
			@Override
            public boolean evaluate(EntityMetaDataVO t) {
	            return ids.contains(t.getId());
            }});
	}

	@Override
	public List<Long> getAllIds() {
		return CollectionUtils.transform(getAll(), DalTransformations.getId());
	}

	public static Collection<String> getDynamicEntityViews() {
		return CollectionUtils.applyFilter(DataBaseHelper.getDbAccess().getTableNames(DbTableType.VIEW), new Predicate<String>() {
			@Override
			public boolean evaluate(String t) {
				return t.toUpperCase().startsWith(DYNAMIC_ENTITY_VIEW_PREFIX);
			}
		});
	}

	public static List<EntityMetaDataVO> getDynamicEntities() {
		ArrayList<EntityMetaDataVO> res = new ArrayList<EntityMetaDataVO>();
		long id = -1000l;
		for(String viewName : getDynamicEntityViews()) {
			EntityMetaDataVO v = new EntityMetaDataVO();
			v.setId(id);
			String entityName = getEntityNameFromDynamicViewName(viewName);
			v.setEntity(entityName);
			v.setDbEntity(viewName);
			v.setSearchable(true);
			v.setEditable(false);
			v.setCacheable(false);
			v.setImportExport(false);
			v.setStateModel(false);
			v.setLogBookTracking(false);
			v.setTreeGroup(false);
			v.setTreeRelation(false);
			v.setFieldValueEntity(false);
			v.setDynamic(true);

			res.add(v);
			id = id - 1000l;
		}
		return res;
	}

	public static String getEntityNameFromDynamicViewName(String viewName) {
        return DYNAMIC_ENTITY_PREFIX + viewName.substring(DYNAMIC_ENTITY_VIEW_PREFIX.length()).toLowerCase();
    }

	public static final Set<String> stExcludedFieldNamesForDynamicGeneration = new HashSet<String>(
		Arrays.asList("INTID", "DATCREATED", "STRCREATED", "DATCHANGED", "STRCHANGED", "INTVERSION", "BLNDELETED")
	);

	public static Map<String, EntityFieldMetaDataVO> getDynamicFieldsForView(String viewName, long entityId) {
		Map<String, EntityFieldMetaDataVO> res = new HashMap<String, EntityFieldMetaDataVO>();
		String entityName = getEntityNameFromDynamicViewName(viewName);
		DbTable tableMetaData = DataBaseHelper.getDbAccess().getTableMetaData(viewName);

		long columnId = entityId - 1;
		for(DbColumn column : tableMetaData.getTableArtifacts(DbColumn.class)) {
			String columnName = column.getColumnName();
			if(!stExcludedFieldNamesForDynamicGeneration.contains(StringUtils.toUpperCase(columnName))) {
				EntityFieldMetaDataVO meta = newDynamicFieldVO(column, columnId, entityName, entityId);
				res.put(meta.getField(), meta);
			}
			columnId = columnId - 1;
		}
		return res;
	}

	public static EntityFieldMetaDataVO newDynamicFieldVO(DbColumn dbColumn, long columnId, String entity, long entityId) {
		String fieldName = dbColumn.getColumnName();
		EntityFieldMetaDataVO result = null;

		if("INTID_T_UD_GENERICOBJECT".equals(fieldName.toUpperCase())) {
			result = new EntityFieldMetaDataVO();
			result.setId(columnId);
			result.setEntityId(entityId);
			result.setField("genericObject");
			result.setDbColumn("INTID_T_UD_GENERICOBJECT");
			result.setDataType("java.lang.String");
			// NUCLOS-9: field must be reference field (i.e. foreign entity must be set!)
			result.setForeignEntity(NuclosEntity.GENERICOBJECT.getEntityName());
			result.setScale(255);
			result.setPrecision(null);
			// this is a special column so we do not mark it as dynamic (esp. it's not case-sensitive)
			// (Note: if you need to distinguish it, use the entity's dynamic flag)
			result.setDynamic(false);
		}
		else {
			Class<?> cls = String.class;
			Integer scale = null;
			Integer precision = null;
			String outputformat = null;
			DbColumnType columnType = dbColumn.getColumnType();
			LOG.debug("Create dynamic field metadata for " + entity + "." + dbColumn.getColumnName() + ": " + dbColumn.toString());
			if(columnType.getGenericType() != null) {
				switch (columnType.getGenericType()) {
				case DATE:
				case DATETIME:
					cls = Date.class;
					break;
				case NUMERIC:
					precision = columnType.getPrecision();
					scale = columnType.getScale();
					// this seems wrong because it can be floating point number, too...
					if(columnType.getPrecision() != null && columnType.getPrecision() == 1
						&& columnType.getScale() != null && columnType.getScale() == 0) {
						// TODO
						// booleans are mapped as NUMBER(1), but it's possible that there are
						// other columns which are not meant as a boolean.
						cls = Boolean.class;
					}
					else if (columnType.getScale() != null && columnType.getScale() > 0) {
						cls = Double.class;
						outputformat = "#,##0.";
						for (int i = 0; i < scale; i++) {
							outputformat += "0";
						}
					}
					else {
						cls = Integer.class;
					}
					break;
				case VARCHAR:
					cls = String.class;
					scale = columnType.getLength();
					break;
				}
			}
			result = new EntityFieldMetaDataVO();
			result.setId(columnId);
			result.setEntityId(entityId);
			result.setField(fieldName);
			result.setDbColumn(fieldName);
			result.setDataType(cls.getName());
			result.setScale(scale);
			result.setPrecision(precision);
			result.setFormatOutput(outputformat);
			result.setDynamic(true);
		}

		result.setFallbacklabel(fieldName);
		result.setNullable(true);
		result.setSearchable(true);
		result.setUnique(false);
		result.setIndexed(false);
		result.setLogBookTracking(false);
		result.setInsertable(false);
		result.setReadonly(true);

		return result;
	}
}
