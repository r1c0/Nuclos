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

import static org.nuclos.server.dal.processor.AbstractDalProcessor.DT_BOOLEAN;
import static org.nuclos.server.dal.processor.AbstractDalProcessor.DT_INTEGER;
import static org.nuclos.server.dal.processor.AbstractDalProcessor.DT_INTERNALTIMESTAMP;
import static org.nuclos.server.dal.processor.AbstractDalProcessor.DT_LONG;
import static org.nuclos.server.dal.processor.AbstractDalProcessor.DT_NUCLOSSCRIPT;
import static org.nuclos.server.dal.processor.AbstractDalProcessor.DT_STRING;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.nuclos.common.EntityLafParameterVO;
import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.WorkspaceVO;
import org.nuclos.common.dal.vo.EOGenericObjectVO;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.IDalVO;
import org.nuclos.common.dal.vo.PivotInfo;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common.dblayer.JoinType;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.common.DatasourceServerUtils;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.SessionUtils;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.processor.jdbc.TableAliasSingleton;
import org.nuclos.server.dal.processor.jdbc.impl.ChartEntityObjectProcessor;
import org.nuclos.server.dal.processor.jdbc.impl.DynamicEntityObjectProcessor;
import org.nuclos.server.dal.processor.jdbc.impl.EOGenericObjectProcessor;
import org.nuclos.server.dal.processor.jdbc.impl.EntityFieldMetaDataProcessor;
import org.nuclos.server.dal.processor.jdbc.impl.EntityLafParameterProcessor;
import org.nuclos.server.dal.processor.jdbc.impl.EntityMetaDataProcessor;
import org.nuclos.server.dal.processor.jdbc.impl.EntityObjectProcessor;
import org.nuclos.server.dal.processor.jdbc.impl.ImportObjectProcessor;
import org.nuclos.server.dal.processor.jdbc.impl.WorkspaceProcessor;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityObjectProcessor;
import org.nuclos.server.database.SpringDataBaseHelper;
import org.nuclos.server.fileimport.ImportStructure;
import org.springframework.beans.factory.annotation.Autowired;

public class ProcessorFactorySingleton {

	private static ProcessorFactorySingleton INSTANCE;

	private static final Set<String> staticSystemFields;
	static {
		final Set<String> set = new HashSet<String>();
		set.add(NuclosEOField.CHANGEDAT.getMetaData().getField());
		set.add(NuclosEOField.CHANGEDBY.getMetaData().getField());
		set.add(NuclosEOField.CREATEDAT.getMetaData().getField());
		set.add(NuclosEOField.CREATEDBY.getMetaData().getField());
		staticSystemFields = Collections.unmodifiableSet(set);
	}
	
	//
	
	private TableAliasSingleton tableAliasSingleton;
	
	private SpringDataBaseHelper dataBaseHelper;
	
	private SessionUtils utils;
	
	private DatasourceServerUtils datasourceServerUtils;

	private ProcessorFactorySingleton() {
		INSTANCE = this;
	}

	public static ProcessorFactorySingleton getInstance() {
		return INSTANCE;
	}
	
	@Autowired
	void setTableAliasSingleton(TableAliasSingleton tableAliasSingleton) {
		this.tableAliasSingleton = tableAliasSingleton;
	}
	
	@Autowired
	void setSpringDataBaseHelper(SpringDataBaseHelper dataBaseHelper) {
		this.dataBaseHelper = dataBaseHelper;
	}
	
	@Autowired
	void setSessionUtils(SessionUtils utils) {
		this.utils = utils;
	}
	
	@Autowired
	void setDatasourceServerUtils(DatasourceServerUtils datasourceServerUtils) {
		this.datasourceServerUtils = datasourceServerUtils;
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

	private static <S> IColumnToVOMapping<S> createBeanMapping(String alias, Class<? extends IDalVO> type, String column, String field,
			Class<S> dataType) {
		return createBeanMapping(alias, type, column, field, dataType, false);
	}

	private static <S> IColumnToVOMapping<S> createBeanMapping(String alias, Class<? extends IDalVO> type, String column, String field,
			Class<S> dataType, boolean isReadonly) {
		final String xetterSuffix = field.substring(0, 1).toUpperCase() + field.substring(1);
		// final Class<?> clazz = getDalVOClass();
		try {
			return new ColumnToBeanVOMapping<S>(alias, column, field, type.getMethod("set" + xetterSuffix, dataType),
					type.getMethod((DT_BOOLEAN.equals(dataType) ? "is" : "get") + xetterSuffix), dataType, isReadonly);
		} catch (Exception e) {
			throw new CommonFatalException("On " + type + ": " + e);
		}
	}

	private static <S> IColumnToVOMapping<S> createBeanRefMapping(String refColumn, String table, String tableAlias, JoinType jt, Class<? extends IDalVO> type, String column, String alias, String field, Class<S> dataType) {
		final String xetterSuffix = field.substring(0, 1).toUpperCase() + field.substring(1);
		try {
			return new ColumnToBeanVORefMapping<S>(refColumn, table, tableAlias, jt, column, alias, type.getMethod("set" + xetterSuffix, dataType), dataType);
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

	protected static <S extends Object> IColumnToVOMapping<S> createFieldMapping(String alias, EntityFieldMetaDataVO field) {
		try {
			return new ColumnToFieldVOMapping<S>(alias, field);
		} catch (ClassNotFoundException e) {
			throw new CommonFatalException(e);
		}
	}

	protected <S extends Object> IColumnToVOMapping<S> createRefFieldMapping(EntityFieldMetaDataVO field) {
		try {
			final String alias = tableAliasSingleton.getAlias(field);
			return new ColumnToRefFieldVOMapping<S>(alias, field);
		} catch (ClassNotFoundException e) {
			throw new CommonFatalException(e);
		}
	}

	protected static <S extends Object> IColumnToVOMapping<S> createFieldIdMapping(String alias, EntityFieldMetaDataVO field) {
		try {
			return new ColumnToFieldIdVOMapping<S>(alias, field);
		} catch (ClassNotFoundException e) {
			throw new CommonFatalException(e);
		}
	}

	protected static <S extends Object> IColumnToVOMapping<S> createPivotJoinMapping(String alias,
			EntityFieldMetaDataVO mdField, String joinEntity) {
		try {
			return new PivotJoinEntityFieldVOMapping<S>(alias, mdField, joinEntity);
		} catch (ClassNotFoundException e) {
			throw new CommonFatalException(e);
		}
	}

	public JdbcEntityObjectProcessor newEntityObjectProcessor(EntityMetaDataVO eMeta, Collection<EntityFieldMetaDataVO> colEfMeta, boolean addSystemColumns) {
		final Class<? extends IDalVO> type = EntityObjectVO.class;
		final ProcessorConfiguration config = newProcessorConfiguration(type, eMeta, colEfMeta, addSystemColumns);
		final EntityObjectProcessor result = new EntityObjectProcessor(config);
		
		// HACK: force spring, as @Autowired on EntityObjectProcessor does not work (tp)
		result.setDataBaseHelper(dataBaseHelper);
		result.setTableAliasSingleton(tableAliasSingleton);
		result.setSessionUtils(utils);
		result.setDatasourceServerUtils(datasourceServerUtils);
		
		return result;
	}

	private ProcessorConfiguration newProcessorConfiguration(Class<? extends IDalVO> type, EntityMetaDataVO eMeta, Collection<EntityFieldMetaDataVO> colEfMeta, boolean addSystemColumns) {
		final int maxFieldCount = countFieldsForInitiatingFieldMap(colEfMeta);
		final int maxFieldIdCount = countIdFieldsForInitiatingFieldMap(colEfMeta);
		final List<IColumnToVOMapping<? extends Object>> allColumns = new ArrayList<IColumnToVOMapping<? extends Object>>();

		final IColumnToVOMapping<Long> idColumn = createBeanMapping(SystemFields.BASE_ALIAS, type, "INTID", "id", DT_LONG);
		allColumns.add(idColumn);
		final IColumnToVOMapping<Integer> versionColumn;

		if(addSystemColumns) {
			allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "DATCREATED", "createdAt", DT_INTERNALTIMESTAMP));
			allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRCREATED", "createdBy", DT_STRING));
			allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "DATCHANGED", "changedAt", DT_INTERNALTIMESTAMP));
			allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRCHANGED", "changedBy", DT_STRING));
			versionColumn = createBeanMapping(SystemFields.BASE_ALIAS, type, "INTVERSION", "version", DT_INTEGER);
			allColumns.add(versionColumn);
		} else {
			versionColumn = null;
		}

		for (EntityFieldMetaDataVO efMeta : colEfMeta) {
			if (staticSystemFields.contains(efMeta.getField())) {
				// hier nur dynamische Zuweisungen
				continue;
			}

			// normal (non-reference) field
			if (efMeta.getForeignEntity() == null) {
				allColumns.add(createFieldMapping(SystemFields.BASE_ALIAS, efMeta));
			}
			// column is ref to foreign table
			else {
				// only an primary key ref to foreign table
				if (efMeta.getDbColumn().toUpperCase().startsWith("INTID_")) {
					// kein join nötig!
					if (!isIdColumnInList(allColumns, efMeta.getDbColumn()))
						allColumns.add(createFieldIdMapping(SystemFields.BASE_ALIAS, efMeta));
				}
				// normal case: key ref and 'stringified' ref to foreign table
				else {
					// add 'stringified' ref to column mapping

					// The 'if' is temporary HACK. We MUST get rid of it! (tp)
					if (!eMeta.getEntity().equals(NuclosEntity.DATASOURCE) && !efMeta.getDbColumn().equalsIgnoreCase("STRVALUE_T_MD_NUCLET")) {
						allColumns.add(createRefFieldMapping(efMeta));
					}

					// Also add foreign key
					final String dbIdFieldName = DalUtils.getDbIdFieldName(efMeta.getDbColumn());
					if (!isIdColumnInList(allColumns, dbIdFieldName)) {
						allColumns.add(createFieldIdMapping(SystemFields.BASE_ALIAS, efMeta));
					}
					// id column is already in allColumns:
					// Replace the id column if the one present is read-only and the current is not read-only
					// This in effect only switched the read-only flag to false.
					else {
						final IColumnToVOMapping<?> col = getColumnFromList(allColumns, dbIdFieldName);
						if (col.isReadonly() && !efMeta.isReadonly()) {
							allColumns.remove(col);
							allColumns.add(createFieldIdMapping(SystemFields.BASE_ALIAS, efMeta));
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

		final IColumnToVOMapping<Long> idColumn = createBeanMapping(SystemFields.BASE_ALIAS, type, "INTID", "id", DT_LONG);
		allColumns.add(idColumn);
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "DATCREATED", "createdAt", DT_INTERNALTIMESTAMP));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRCREATED", "createdBy", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "DATCHANGED", "changedAt", DT_INTERNALTIMESTAMP));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRCHANGED", "changedBy", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "INTVERSION", "version", DT_INTEGER));

		final IColumnToVOMapping<Long> entityIdColumn = createBeanMapping(SystemFields.BASE_ALIAS, type, "INTID_T_MD_ENTITY", "entityId", DT_LONG);
		allColumns.add(entityIdColumn);
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "INTID_T_MD_ENTITY_FIELD_GROUP", "fieldGroupId", DT_LONG));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRFIELD", "field", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRDBFIELD", "dbColumn", DT_STRING));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRFOREIGNENTITY", "foreignEntity", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRFOREIGNENTITYFIELD", "foreignEntityField", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRLOOKUPENTITY", "lookupEntity", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRLOOKUPENTITYFIELD", "lookupEntityField", DT_STRING));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRDATATYPE", "dataType", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRDEFAULTCOMPONENTTYPE", "defaultComponentType", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "INTDATASCALE", "scale", DT_INTEGER));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "INTDATAPRECISION", "precision", DT_INTEGER));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRFORMATINPUT", "formatInput", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRFORMATOUTPUT", "formatOutput", DT_STRING));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "INTID_FOREIGN_DEFAULT", "defaultForeignId", DT_LONG));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRVALUE_DEFAULT", "defaultValue", DT_STRING));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "BLNREADONLY", "readonly", DT_BOOLEAN));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "BLNUNIQUE", "unique", DT_BOOLEAN));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "BLNNULLABLE", "nullable", DT_BOOLEAN));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "BLNINDEXED", "indexed", DT_BOOLEAN));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "BLNSEARCHABLE", "searchable", DT_BOOLEAN));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "BLNMODIFIABLE", "modifiable", DT_BOOLEAN));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "BLNINSERTABLE", "insertable", DT_BOOLEAN));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "BLNLOGBOOKTRACKING", "logBookTracking", DT_BOOLEAN));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "BLNSHOWMNEMONIC", "showMnemonic", DT_BOOLEAN));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRCALCFUNCTION", "calcFunction", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRSORTATIONASC", "sortorderASC", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRSORTATIONDESC", "sortorderDESC", DT_STRING));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STR_LOCALERESOURCE_L", "localeResourceIdForLabel", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STR_LOCALERESOURCE_D", "localeResourceIdForDescription", DT_STRING));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STR_DEFAULT_MANDATORY", "defaultMandatory", DT_STRING));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "BLNONDELETECASCADE", "onDeleteCascade", DT_BOOLEAN));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "INTORDER", "order", DT_INTEGER));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRCALCULATIONSCRIPT", "calculationScript", DT_NUCLOSSCRIPT));

		return new EntityFieldMetaDataProcessor(allColumns, entityIdColumn, idColumn);
	}

	public EntityMetaDataProcessor newEntityMetaDataProcessor() {
		final Class<? extends IDalVO> type = EntityMetaDataVO.class;
		final List<IColumnToVOMapping<? extends Object>> allColumns = new ArrayList<IColumnToVOMapping<? extends Object>>();
		final IColumnToVOMapping<Long> idColumn = createBeanMapping(SystemFields.BASE_ALIAS, type, "INTID", "id", DT_LONG);

		/*
		 * Attention: The sequence of initialization is important. 
		 * <p>
		 * As a general rule the last 3 items to set are:
		 * <ol>
		 *   <li>virtualEntity</li>
		 *   <li>entity</li>
		 *   <li>dbEntity</li>
		 * </ol>
		 * </p>
		 */
		allColumns.add(idColumn);
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "DATCREATED", "createdAt", DT_INTERNALTIMESTAMP));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRCREATED", "createdBy", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "DATCHANGED", "changedAt", DT_INTERNALTIMESTAMP));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRCHANGED", "changedBy", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "INTVERSION", "version", DT_INTEGER));

		allColumns.add(createBeanRefMapping("INTID_T_MD_NUCLET", "T_AD_APPLICATION", "NUC", JoinType.LEFT, type, "STRNAMESPACE", "STRVALUE_T_MD_NUCLET", "nuclet", DT_STRING));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRSYSTEMIDPREFIX", "systemIdPrefix", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRMENUSHORTCUT", "menuShortcut", DT_STRING));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "BLNEDITABLE", "editable", DT_BOOLEAN));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "BLNUSESSTATEMODEL", "stateModel", DT_BOOLEAN));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "BLNLOGBOOKTRACKING", "logBookTracking", DT_BOOLEAN));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "BLNCACHEABLE", "cacheable", DT_BOOLEAN));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "BLNSEARCHABLE", "searchable", DT_BOOLEAN));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "BLNTREERELATION", "treeRelation", DT_BOOLEAN));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "BLNTREEGROUP", "treeGroup", DT_BOOLEAN));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "BLNIMPORTEXPORT", "importExport", DT_BOOLEAN));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "BLNFIELDVALUEENTITY", "fieldValueEntity", DT_BOOLEAN));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRACCELERATOR", "accelerator", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "INTACCELERATORMODIFIER", "acceleratorModifier", DT_INTEGER));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRFIELDS_FOR_EQUALITY", "fieldsForEquality", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "INTID_T_MD_RESOURCE", "resourceId", DT_INTEGER));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRNUCLOSRESOURCE", "nuclosResource", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STR_LOCALERESOURCE_L", "localeResourceIdForLabel", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STR_LOCALERESOURCE_M", "localeResourceIdForMenuPath", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STR_LOCALERESOURCE_D", "localeResourceIdForDescription", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STR_LOCALERESOURCE_TW", "localeResourceIdForTreeView", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STR_LOCALERESOURCE_TT", "localeResourceIdForTreeViewDescription", DT_STRING));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STR_DOCUMENTPATH", "documentPath", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STR_REPORTFILENAME", "reportFilename", DT_STRING));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STR_IDFACTORY", "idFactory", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STR_READDELEGATE", "readDelegate", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRROWCOLORSCRIPT", "rowColorScript", DT_NUCLOSSCRIPT));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRVIRTUALENTITY", "virtualentity", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRENTITY", "entity", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRDBENTITY", "dbEntity", DT_STRING));

		return new EntityMetaDataProcessor(allColumns, idColumn);
	}
	
	public EntityLafParameterProcessor newEntityLafParameterProcessor() {
		final Class<? extends IDalVO> type = EntityLafParameterVO.class;
		final List<IColumnToVOMapping<? extends Object>> allColumns = new ArrayList<IColumnToVOMapping<? extends Object>>();
		final IColumnToVOMapping<Long> idColumn = createBeanMapping(SystemFields.BASE_ALIAS, type, "INTID", "id", DT_LONG);

		allColumns.add(idColumn);
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "DATCREATED", "createdAt", DT_INTERNALTIMESTAMP));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRCREATED", "createdBy", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "DATCHANGED", "changedAt", DT_INTERNALTIMESTAMP));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRCHANGED", "changedBy", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "INTVERSION", "version", DT_INTEGER));

//		allColumns.add(createBeanRefMapping("INTID_T_MD_ENTITY", "T_MD_ENTITY", "ENTITY", JoinType.LEFT, type, "STRENTITY", "STRVALUE_T_MD_ENTITY", "entity", DT_STRING));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "INTID_T_MD_ENTITY", "entity", DT_LONG));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRPARAMETER", "parameter", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRVALUE", "value", DT_STRING));
		
		return new EntityLafParameterProcessor(allColumns, idColumn);
	}

	public EOGenericObjectProcessor newEOGenericObjectProcessor() {
		final Class<? extends IDalVO> type = EOGenericObjectVO.class;
		final List<IColumnToVOMapping<? extends Object>> allColumns = new ArrayList<IColumnToVOMapping<? extends Object>>();
		final IColumnToVOMapping<Long> idColumn = createBeanMapping(SystemFields.BASE_ALIAS, type, "INTID", "id", DT_LONG);

		allColumns.add(idColumn);
		final IColumnToVOMapping<Long> moduleColumn = createBeanMapping(SystemFields.BASE_ALIAS, type, "INTID_T_MD_MODULE", "moduleId", DT_LONG);
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "DATCREATED", "createdAt", DT_INTERNALTIMESTAMP));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRCREATED", "createdBy", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "DATCHANGED", "changedAt", DT_INTERNALTIMESTAMP));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRCHANGED", "changedBy", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "INTVERSION", "version", DT_INTEGER));
		allColumns.add(moduleColumn);

		return new EOGenericObjectProcessor(allColumns, moduleColumn, idColumn);
	}

	public WorkspaceProcessor newWorkspaceProcessor() {
		final Class<? extends IDalVO> type = WorkspaceVO.class;
		final List<IColumnToVOMapping<? extends Object>> allColumns = new ArrayList<IColumnToVOMapping<? extends Object>>();
		final IColumnToVOMapping<Long> idColumn = createBeanMapping(SystemFields.BASE_ALIAS, type, "INTID", "id", DT_LONG);
		allColumns.add(idColumn);
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "DATCREATED", "createdAt", DT_INTERNALTIMESTAMP));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRCREATED", "createdBy", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "DATCHANGED", "changedAt", DT_INTERNALTIMESTAMP));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "STRCHANGED", "changedBy", DT_STRING));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "INTVERSION", "version", DT_INTEGER));

		final IColumnToVOMapping<String> nameColumn = createBeanMapping(SystemFields.BASE_ALIAS, type, "STRNAME", "name", DT_STRING);
		allColumns.add(nameColumn);
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, "CLBWORKSPACE", "clbworkspace", DT_STRING));
		final IColumnToVOMapping<Long> userColumn = createBeanMapping(SystemFields.BASE_ALIAS, type, "INTID_T_MD_USER", "user", DT_LONG);
		allColumns.add(userColumn);
		final IColumnToVOMapping<Long> assignedColumn = createBeanMapping(SystemFields.BASE_ALIAS, type, "INTID_T_MD_WORKSPACE", "assignedWorkspace", DT_LONG);
		allColumns.add(assignedColumn);

		return new WorkspaceProcessor(allColumns, idColumn, userColumn, nameColumn, assignedColumn);
	}

	public DynamicEntityObjectProcessor newDynamicEntityObjectProcessor(EntityMetaDataVO eMeta, Collection<EntityFieldMetaDataVO> colEfMeta) {
		final Class<? extends IDalVO> type = EntityObjectVO.class;
		final ProcessorConfiguration config = newProcessorConfiguration(type, eMeta, colEfMeta, false);
		
		final DynamicEntityObjectProcessor result = new DynamicEntityObjectProcessor(config);
		
		// HACK: force spring, as @Autowired on EntityObjectProcessor does not work (tp)
		result.setDataBaseHelper(dataBaseHelper);
		result.setTableAliasSingleton(tableAliasSingleton);
		result.setSessionUtils(utils);
		result.setDatasourceServerUtils(datasourceServerUtils);
		
		return result;
	}

	public ChartEntityObjectProcessor newChartEntityObjectProcessor(EntityMetaDataVO eMeta, Collection<EntityFieldMetaDataVO> colEfMeta) {
		final Class<? extends IDalVO> type = EntityObjectVO.class;
		final ProcessorConfiguration config = newProcessorConfiguration(type, eMeta, colEfMeta, false);
		
		final ChartEntityObjectProcessor result = new ChartEntityObjectProcessor(config);
		
		// HACK: force spring, as @Autowired on EntityObjectProcessor does not work (tp)
		result.setDataBaseHelper(dataBaseHelper);
		result.setTableAliasSingleton(tableAliasSingleton);
		result.setSessionUtils(utils);
		result.setDatasourceServerUtils(datasourceServerUtils);
		
		return result;
	}

	public ImportObjectProcessor newImportObjectProcessor(EntityMetaDataVO eMeta, Collection<EntityFieldMetaDataVO> colEfMeta, ImportStructure structure) {
		final Class<? extends IDalVO> type = EntityObjectVO.class;
		final ProcessorConfiguration config = newProcessorConfiguration(type, eMeta, colEfMeta, true);
		return new ImportObjectProcessor(config, structure);
	}

	public void addToColumns(JdbcEntityObjectProcessor processor, EntityFieldMetaDataVO field) {
		final IColumnToVOMapping<?> mapping;
		final PivotInfo pinfo = field.getPivotInfo();
		final MetaDataProvider mdProv = MetaDataServerProvider.getInstance();
		final EntityMetaDataVO mdEnitiy = mdProv.getEntity(field.getEntityId());

		final String alias;
		if (mdEnitiy.equals(processor.getMeta())) {
			alias = SystemFields.BASE_ALIAS;
		}
		// The join table alias must be unique in the SQL
		else if (pinfo != null) {
			alias = pinfo.getPivotTableAlias(field.getField());
		}
		else {
			alias = mdEnitiy.getEntity();
		}

		if (pinfo == null) {
			mapping = createFieldMapping(alias, field);
		}
		else {
			final EntityFieldMetaDataVO vField = mdProv.getEntityField(pinfo.getSubform(), pinfo.getValueField());
			final IColumnToVOMapping<?> mapping2 = createPivotJoinMapping(alias, vField, pinfo.getSubform());
			processor.addToColumns(mapping2);
			// Also add the key field so that the gui result table could find the pivot
			final EntityFieldMetaDataVO kField = mdProv.getEntityField(pinfo.getSubform(), pinfo.getKeyField());
			mapping = createPivotJoinMapping(alias, kField, pinfo.getSubform());
		}
		processor.addToColumns(mapping);
	}

}
