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

import static org.nuclos.server.dal.processor.jdbc.AbstractJdbcDalProcessor.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.dal.vo.EOGenericObjectVO;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.IDalVO;
import org.nuclos.common.dal.vo.PivotInfo;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.processor.jdbc.impl.DynamicEntityObjectProcessor;
import org.nuclos.server.dal.processor.jdbc.impl.EntityObjectProcessor;
import org.nuclos.server.dal.processor.jdbc.impl.EntityFieldMetaDataProcessor;
import org.nuclos.server.dal.processor.jdbc.impl.EntityMetaDataProcessor;
import org.nuclos.server.dal.processor.jdbc.impl.EOGenericObjectProcessor;
import org.nuclos.server.dal.processor.jdbc.impl.ImportObjectProcessor;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityObjectProcessor;
import org.nuclos.server.fileimport.ImportStructure;

public class ProcessorFactorySingleton {
	
	private static final ProcessorFactorySingleton INSTANCE = new ProcessorFactorySingleton();
	
	private static final Set<String> staticSystemFields;
	static {
		final Set<String> set = new HashSet<String>();
		set.add(NuclosEOField.CHANGEDAT.getMetaData().getField());
		set.add(NuclosEOField.CHANGEDBY.getMetaData().getField());
		set.add(NuclosEOField.CREATEDAT.getMetaData().getField());
		set.add(NuclosEOField.CREATEDBY.getMetaData().getField());
		staticSystemFields = Collections.unmodifiableSet(set);
	}
	
	private ProcessorFactorySingleton() {
	}
	
	public static ProcessorFactorySingleton getInstance() {
		return INSTANCE;
	}
	
	private static int countFieldsForInitiatingFieldMap(Collection<EntityFieldMetaDataVO> colEfMeta) {
		int result = colEfMeta.size();
		for (EntityFieldMetaDataVO efMeta : colEfMeta)  {
			if (staticSystemFields.contains(efMeta.getField())) {
				// only subtract a static system field if it exists in colEfMeta.
				// static fields are not stored in field map.
				result--;
			}
		}
		return result;
	}

	private static int countIdFieldsForInitiatingFieldMap(Collection<EntityFieldMetaDataVO> colEfMeta) {
		int result = 0;
		for (EntityFieldMetaDataVO efMeta : colEfMeta)  {
			if (efMeta.getForeignEntity() != null) {
				result++;
			}
		}
		return result;
	}

	private static <S> IColumnToVOMapping<S> createBeanMapping(Class<? extends IDalVO> type, final String column, final String field,
			final Class<S> dataType) {
		return createBeanMapping(type, column, field, dataType, false);
	}

	private static <S> IColumnToVOMapping<S> createBeanMapping(Class<? extends IDalVO> type, final String column, final String field,
			final Class<S> dataType, boolean isReadonly) {
		final String xetterSuffix = field.substring(0, 1).toUpperCase() + field.substring(1);
		// final Class<?> clazz = getDalVOClass();
		try {
			return new ColumnToBeanVOMapping<S>(column, field, type.getMethod("set" + xetterSuffix, dataType),
					type.getMethod((DT_BOOLEAN.equals(dataType) ? "is" : "get") + xetterSuffix), dataType, isReadonly);
		} catch (Exception e) {
			throw new CommonFatalException("On " + type + ": " + e);
		}
	}

	private static boolean isIdColumnInList(List<IColumnToVOMapping<?>> list, String columnName) {
		return getColumnFromList(list, columnName) != null;
	}

	private static IColumnToVOMapping<?> getColumnFromList(List<IColumnToVOMapping<?>> list, String columnName) {
		for (IColumnToVOMapping<?> column : list) {
			if (column.getColumn().equals(columnName)) {
				return column;
			}
		}
		return null;
	}

	protected static <S extends Object> IColumnToVOMapping<S> createFieldMapping(final String column,
			final String field, final String dataType, Boolean isReadonly, boolean caseSensitive) {
		try {
			return (IColumnToVOMapping<S>) new ColumnToFieldVOMapping<S>(column, field,
					(Class<S>) Class.forName(dataType), isReadonly, caseSensitive);
		} catch (ClassNotFoundException e) {
			throw new CommonFatalException(e);
		}
	}

	protected static <S extends Object> IColumnToVOMapping<S> createFieldIdMapping(final String column,
			final String field, final String dataType, Boolean isReadonly, boolean caseSensitive) {
		try {
			return (IColumnToVOMapping<S>) new ColumnToFieldIdVOMapping<S>(column, field,
					(Class<S>) Class.forName(dataType), isReadonly, caseSensitive);
		} catch (ClassNotFoundException e) {
			throw new CommonFatalException(e);
		}
	}

	public JdbcEntityObjectProcessor newEntityObjectProcessor(EntityMetaDataVO eMeta, Collection<EntityFieldMetaDataVO> colEfMeta, boolean addSystemColumns) {
		final Class<? extends IDalVO> type = EntityObjectVO.class;
		final ProcessorConfiguration config = newProcessorConfiguration(type, eMeta, colEfMeta, addSystemColumns);
		return new EntityObjectProcessor(config);
	}
	
	private ProcessorConfiguration newProcessorConfiguration(Class<? extends IDalVO> type, EntityMetaDataVO eMeta, Collection<EntityFieldMetaDataVO> colEfMeta, boolean addSystemColumns) {
		final int maxFieldCount = countFieldsForInitiatingFieldMap(colEfMeta);
		final int maxFieldIdCount = countIdFieldsForInitiatingFieldMap(colEfMeta);
		final List<IColumnToVOMapping<? extends Object>> allColumns = new ArrayList<IColumnToVOMapping<? extends Object>>();

		final IColumnToVOMapping<Long> idColumn = createBeanMapping(type, "INTID", "id", DT_LONG);
		allColumns.add(idColumn);
		final IColumnToVOMapping<Integer> versionColumn;

		if(addSystemColumns) {
			allColumns.add(createBeanMapping(type, "DATCREATED", "createdAt", DT_INTERNALTIMESTAMP));
			allColumns.add(createBeanMapping(type, "STRCREATED", "createdBy", DT_STRING));
			allColumns.add(createBeanMapping(type, "DATCHANGED", "changedAt", DT_INTERNALTIMESTAMP));
			allColumns.add(createBeanMapping(type, "STRCHANGED", "changedBy", DT_STRING));
			versionColumn = createBeanMapping(type, "INTVERSION", "version", DT_INTEGER);
			allColumns.add(versionColumn);
		} else {
			versionColumn = null;
		}

		for (EntityFieldMetaDataVO efMeta : colEfMeta) {
			if (staticSystemFields.contains(efMeta.getField())) {
				// hier nur dynamische Zuweisungen
				continue;
			}

			if (efMeta.getForeignEntity() == null) {
				allColumns.add(createFieldMapping(efMeta.getDbColumn(), efMeta.getField(), efMeta.getDataType(), efMeta.isReadonly(), efMeta.isDynamic()));
			} 
			// column is ref to foreign table
			else {
				// only an primary key ref to foreign table
				if (efMeta.getDbColumn().toUpperCase().startsWith("INTID_")) {
					// kein join nötig!
					if (!isIdColumnInList(allColumns, efMeta.getDbColumn()))
						allColumns.add(createFieldIdMapping(efMeta.getDbColumn(), efMeta.getField(), DT_LONG.getName(), efMeta.isReadonly(), efMeta.isDynamic()));
				} 
				// normal case: key ref and 'stringified' ref to foreign table
				else {
					// add 'stringified' ref to column mapping
					allColumns.add(createFieldMapping(efMeta.getDbColumn(), efMeta.getField(), efMeta.getDataType(), true, efMeta.isDynamic()));
					String dbIdFieldName = DalUtils.getDbIdFieldName(efMeta.getDbColumn());
					if (!isIdColumnInList(allColumns, dbIdFieldName)) {
						allColumns.add(createFieldIdMapping(dbIdFieldName, efMeta.getField(), DT_LONG.getName(), efMeta.isReadonly(), efMeta.isDynamic()));
					}
					// id column is already in allColumns:
					// Replace the id column if the one present is read-only and the current is not read-only
					// This in effect only switched the read-only flag to false.
					else {
						IColumnToVOMapping<?> col = getColumnFromList(allColumns, dbIdFieldName);
						if (col.isReadonly() && !efMeta.isReadonly()) {
							allColumns.remove(col);
							allColumns.add(createFieldIdMapping(dbIdFieldName, efMeta.getField(), DT_LONG.getName(), efMeta.isReadonly(), efMeta.isDynamic()));
						}
					}
				}
			}
		}

		return new ProcessorConfiguration(type, eMeta, allColumns, idColumn, versionColumn, addSystemColumns, maxFieldCount, maxFieldIdCount);
	}
	
	public EntityFieldMetaDataProcessor newEntityFieldMetaDataProcessor() {
		final Class<? extends IDalVO> type = EntityFieldMetaDataVO.class;
		final List<IColumnToVOMapping<? extends Object>> allColumns = new ArrayList<IColumnToVOMapping<? extends Object>>();
		
		final IColumnToVOMapping<Long> idColumn = createBeanMapping(type, "INTID", "id", DT_LONG);
		allColumns.add(idColumn);
		allColumns.add(createBeanMapping(type, "DATCREATED", "createdAt", DT_INTERNALTIMESTAMP));
		allColumns.add(createBeanMapping(type, "STRCREATED", "createdBy", DT_STRING));
		allColumns.add(createBeanMapping(type, "DATCHANGED", "changedAt", DT_INTERNALTIMESTAMP));
		allColumns.add(createBeanMapping(type, "STRCHANGED", "changedBy", DT_STRING));
		allColumns.add(createBeanMapping(type, "INTVERSION", "version", DT_INTEGER));
		
		final IColumnToVOMapping<Long> entityIdColumn = createBeanMapping(type, "INTID_T_MD_ENTITY", "entityId", DT_LONG);
		allColumns.add(entityIdColumn);
		allColumns.add(createBeanMapping(type, "INTID_T_MD_ENTITY_FIELD_GROUP", "fieldGroupId", DT_LONG));
		allColumns.add(createBeanMapping(type, "STRFIELD", "field", DT_STRING));
		allColumns.add(createBeanMapping(type, "STRDBFIELD", "dbColumn", DT_STRING));
		
		allColumns.add(createBeanMapping(type, "STRFOREIGNENTITY", "foreignEntity", DT_STRING));
		allColumns.add(createBeanMapping(type, "STRFOREIGNENTITYFIELD", "foreignEntityField", DT_STRING));
		
		allColumns.add(createBeanMapping(type, "STRDATATYPE", "dataType", DT_STRING));
		allColumns.add(createBeanMapping(type, "INTDATASCALE", "scale", DT_INTEGER));
		allColumns.add(createBeanMapping(type, "INTDATAPRECISION", "precision", DT_INTEGER));
		allColumns.add(createBeanMapping(type, "STRFORMATINPUT", "formatInput", DT_STRING));
		allColumns.add(createBeanMapping(type, "STRFORMATOUTPUT", "formatOutput", DT_STRING));
		
		allColumns.add(createBeanMapping(type, "INTID_FOREIGN_DEFAULT", "defaultForeignId", DT_LONG));
		allColumns.add(createBeanMapping(type, "STRVALUE_DEFAULT", "defaultValue", DT_STRING));
		
		allColumns.add(createBeanMapping(type, "BLNREADONLY", "readonly", DT_BOOLEAN));
		allColumns.add(createBeanMapping(type, "BLNUNIQUE", "unique", DT_BOOLEAN));
		allColumns.add(createBeanMapping(type, "BLNNULLABLE", "nullable", DT_BOOLEAN));
		allColumns.add(createBeanMapping(type, "BLNINDEXED", "indexed", DT_BOOLEAN));
		allColumns.add(createBeanMapping(type, "BLNSEARCHABLE", "searchable", DT_BOOLEAN));
		allColumns.add(createBeanMapping(type, "BLNMODIFIABLE", "modifiable", DT_BOOLEAN));
		allColumns.add(createBeanMapping(type, "BLNINSERTABLE", "insertable", DT_BOOLEAN));
		allColumns.add(createBeanMapping(type, "BLNLOGBOOKTRACKING", "logBookTracking", DT_BOOLEAN));
		allColumns.add(createBeanMapping(type, "BLNSHOWMNEMONIC", "showMnemonic", DT_BOOLEAN));
		
		allColumns.add(createBeanMapping(type, "STRCALCFUNCTION", "calcFunction", DT_STRING));
		allColumns.add(createBeanMapping(type, "STRSORTATIONASC", "sortorderASC", DT_STRING));
		allColumns.add(createBeanMapping(type, "STRSORTATIONDESC", "sortorderDESC", DT_STRING));
		
		allColumns.add(createBeanMapping(type, "STR_LOCALERESOURCE_L", "localeResourceIdForLabel", DT_STRING));
		allColumns.add(createBeanMapping(type, "STR_LOCALERESOURCE_D", "localeResourceIdForDescription", DT_STRING));
		
		allColumns.add(createBeanMapping(type, "STR_DEFAULT_MANDATORY", "defaultMandatory", DT_STRING));
		
		return new EntityFieldMetaDataProcessor(allColumns, entityIdColumn, idColumn);
	}
	
	public EntityMetaDataProcessor newEntityMetaDataProcessor() {
		final Class<? extends IDalVO> type = EntityMetaDataVO.class;
		final List<IColumnToVOMapping<? extends Object>> allColumns = new ArrayList<IColumnToVOMapping<? extends Object>>();
		final IColumnToVOMapping<Long> idColumn = createBeanMapping(type, "INTID", "id", DT_LONG);
		
		allColumns.add(idColumn);
		allColumns.add(createBeanMapping(type, "DATCREATED", "createdAt", DT_INTERNALTIMESTAMP));
		allColumns.add(createBeanMapping(type, "STRCREATED", "createdBy", DT_STRING));
		allColumns.add(createBeanMapping(type, "DATCHANGED", "changedAt", DT_INTERNALTIMESTAMP));
		allColumns.add(createBeanMapping(type, "STRCHANGED", "changedBy", DT_STRING));
		allColumns.add(createBeanMapping(type, "INTVERSION", "version", DT_INTEGER));
		
		allColumns.add(createBeanMapping(type, "STRENTITY", "entity", DT_STRING));
		allColumns.add(createBeanMapping(type, "STRDBENTITY", "dbEntity", DT_STRING));
		
		allColumns.add(createBeanMapping(type, "STRSYSTEMIDPREFIX", "systemIdPrefix", DT_STRING));
		allColumns.add(createBeanMapping(type, "STRMENUSHORTCUT", "menuShortcut", DT_STRING));
		
		allColumns.add(createBeanMapping(type, "BLNEDITABLE", "editable", DT_BOOLEAN));
		allColumns.add(createBeanMapping(type, "BLNUSESSTATEMODEL", "stateModel", DT_BOOLEAN));
		allColumns.add(createBeanMapping(type, "BLNLOGBOOKTRACKING", "logBookTracking", DT_BOOLEAN));
		allColumns.add(createBeanMapping(type, "BLNCACHEABLE", "cacheable", DT_BOOLEAN));
		allColumns.add(createBeanMapping(type, "BLNSEARCHABLE", "searchable", DT_BOOLEAN));
		allColumns.add(createBeanMapping(type, "BLNTREERELATION", "treeRelation", DT_BOOLEAN));
		allColumns.add(createBeanMapping(type, "BLNTREEGROUP", "treeGroup", DT_BOOLEAN));
		allColumns.add(createBeanMapping(type, "BLNIMPORTEXPORT", "importExport", DT_BOOLEAN));
		allColumns.add(createBeanMapping(type, "BLNFIELDVALUEENTITY", "fieldValueEntity", DT_BOOLEAN));
		
		allColumns.add(createBeanMapping(type, "STRACCELERATOR", "accelerator", DT_STRING));
		allColumns.add(createBeanMapping(type, "INTACCELERATORMODIFIER", "acceleratorModifier", DT_INTEGER));
		allColumns.add(createBeanMapping(type, "STRFIELDS_FOR_EQUALITY", "fieldsForEquality", DT_STRING));
		allColumns.add(createBeanMapping(type, "INTID_T_MD_RESOURCE", "resourceId", DT_INTEGER));
		allColumns.add(createBeanMapping(type, "STRNUCLOSRESOURCE", "nuclosResource", DT_STRING));
		allColumns.add(createBeanMapping(type, "STR_LOCALERESOURCE_L", "localeResourceIdForLabel", DT_STRING));
		allColumns.add(createBeanMapping(type, "STR_LOCALERESOURCE_M", "localeResourceIdForMenuPath", DT_STRING));
		allColumns.add(createBeanMapping(type, "STR_LOCALERESOURCE_D", "localeResourceIdForDescription", DT_STRING));
		allColumns.add(createBeanMapping(type, "STR_LOCALERESOURCE_TW", "localeResourceIdForTreeView", DT_STRING));
		allColumns.add(createBeanMapping(type, "STR_LOCALERESOURCE_TT", "localeResourceIdForTreeViewDescription", DT_STRING));
		
		allColumns.add(createBeanMapping(type, "STR_DOCUMENTPATH", "documentPath", DT_STRING));
		allColumns.add(createBeanMapping(type, "STR_REPORTFILENAME", "reportFilename", DT_STRING));
		
		return new EntityMetaDataProcessor(allColumns, idColumn);
	}
	
	public EOGenericObjectProcessor newEOGenericObjectProcessor() {
		final Class<? extends IDalVO> type = EOGenericObjectVO.class;
		final List<IColumnToVOMapping<? extends Object>> allColumns = new ArrayList<IColumnToVOMapping<? extends Object>>();
		final IColumnToVOMapping<Long> idColumn = createBeanMapping(type, "INTID", "id", DT_LONG);
		
		allColumns.add(idColumn);
		final IColumnToVOMapping<Long> moduleColumn = createBeanMapping(type, "INTID_T_MD_MODULE", "moduleId", DT_LONG);
		allColumns.add(createBeanMapping(type, "DATCREATED", "createdAt", DT_INTERNALTIMESTAMP));
		allColumns.add(createBeanMapping(type, "STRCREATED", "createdBy", DT_STRING));
		allColumns.add(createBeanMapping(type, "DATCHANGED", "changedAt", DT_INTERNALTIMESTAMP));
		allColumns.add(createBeanMapping(type, "STRCHANGED", "changedBy", DT_STRING));
		allColumns.add(createBeanMapping(type, "INTVERSION", "version", DT_INTEGER));
		allColumns.add(moduleColumn);
		
		return new EOGenericObjectProcessor(allColumns, moduleColumn, idColumn);
	}
	
	public DynamicEntityObjectProcessor newDynamicEntityObjectProcessor(EntityMetaDataVO eMeta, Collection<EntityFieldMetaDataVO> colEfMeta) {
		final Class<? extends IDalVO> type = EntityObjectVO.class;
		final ProcessorConfiguration config = newProcessorConfiguration(type, eMeta, colEfMeta, false);
		return new DynamicEntityObjectProcessor(config);
	}
	
	public ImportObjectProcessor newImportObjectProcessor(EntityMetaDataVO eMeta, Collection<EntityFieldMetaDataVO> colEfMeta, ImportStructure structure) {
		final Class<? extends IDalVO> type = EntityObjectVO.class;
		final ProcessorConfiguration config = newProcessorConfiguration(type, eMeta, colEfMeta, true);
		return new ImportObjectProcessor(config, structure);
	}

	public void addToColumns(JdbcEntityObjectProcessor processor, EntityFieldMetaDataVO field) {
		final IColumnToVOMapping<?> mapping;
		final PivotInfo pinfo = field.getPivotInfo();
		if (pinfo == null) {
			mapping = createFieldMapping(
					field.getDbColumn(), field.getField(), field.getDataType(), field.isReadonly(), false);
		}
		else {
			final MetaDataProvider mdProv = MetaDataServerProvider.getInstance();
			final EntityFieldMetaDataVO vField = mdProv.getEntityField(pinfo.getSubform(), pinfo.getValueField());
			mapping = createFieldMapping(
					vField.getDbColumn(), vField.getField(), vField.getDataType(), vField.isReadonly(), false);
		}
		processor.addToColumns(mapping);
	}

}
