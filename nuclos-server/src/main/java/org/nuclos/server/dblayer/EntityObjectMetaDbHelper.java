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
package org.nuclos.server.dblayer;

import static org.nuclos.server.dblayer.structure.DbColumnType.DbGenericType.DATETIME;
import static org.nuclos.server.dblayer.structure.DbColumnType.DbGenericType.NUMERIC;
import static org.nuclos.server.dblayer.structure.DbColumnType.DbGenericType.VARCHAR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.nuclos.common.CryptUtil;
import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.NuclosDateTime;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.NuclosPassword;
import org.nuclos.common.NuclosScript;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.structure.DbColumn;
import org.nuclos.server.dblayer.structure.DbColumnType;
import org.nuclos.server.dblayer.structure.DbColumnType.DbGenericType;
import org.nuclos.server.dblayer.structure.DbConstraint.DbForeignKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbPrimaryKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbUniqueConstraint;
import org.nuclos.server.dblayer.structure.DbIndex;
import org.nuclos.server.dblayer.structure.DbNullable;
import org.nuclos.server.dblayer.structure.DbSimpleView;
import org.nuclos.server.dblayer.structure.DbSimpleView.DbSimpleViewColumn;
import org.nuclos.server.dblayer.structure.DbTable;
import org.nuclos.server.dblayer.structure.DbTableArtifact;

public class EntityObjectMetaDbHelper {

	private static Map<String, DbColumnType> SYSTEM_COLUMNS = new LinkedHashMap<String, DbColumnType>();

	static DbColumnType ID_COLUMN_TYPE = new DbColumnType(NUMERIC, 20, 0);

	static {
		SYSTEM_COLUMNS.put("INTID",      ID_COLUMN_TYPE);
		SYSTEM_COLUMNS.put("DATCREATED", new DbColumnType(DATETIME));
		SYSTEM_COLUMNS.put("STRCREATED", new DbColumnType(VARCHAR, 30));
		SYSTEM_COLUMNS.put("DATCHANGED", new DbColumnType(DATETIME));
		SYSTEM_COLUMNS.put("STRCHANGED", new DbColumnType(VARCHAR, 30));
		SYSTEM_COLUMNS.put("INTVERSION", new DbColumnType(NUMERIC, 9, 0));
	}

	private final MetaDataProvider provider;
	private final DbAccess dbAccess;

	public EntityObjectMetaDbHelper(MetaDataProvider provider) {
		this(DataBaseHelper.getInstance().getDbAccess(), provider);
	}

	public EntityObjectMetaDbHelper(DbAccess dbAccess, MetaDataProvider provider) {
		this.dbAccess = dbAccess;
		this.provider = provider;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("access=").append(dbAccess);
		result.append(", mdProv=").append(provider);
		result.append("]");
		return result.toString();
	}

	public Map<String, DbTable> getSchema() {
		Map<String, DbTable> tables = new LinkedHashMap<String, DbTable>();
		for (EntityMetaDataVO entityMeta : provider.getAllEntities()) {
			if (!entityMeta.isDynamic()) {
				DbTable dbTable = getDbTable(entityMeta);
				tables.put(dbTable.getTableName(), dbTable);
			}
		}
		return tables;
	}

	public DbTable getDbTable(EntityMetaDataVO entityMeta) {
		String tableName = getTableName(entityMeta);
		String dbTableName = generateDbName(tableName);

		Map<String, DbColumn> dbColumns = new LinkedHashMap<String, DbColumn>();
		Map<String, DbColumn> dbColumnsByField = new HashMap<String, DbColumn>();
		Set<DbForeignKeyConstraint> fkConstraints = new LinkedHashSet<DbForeignKeyConstraint>();
		Map<String, DbIndex> indexes = new LinkedHashMap<String, DbIndex>();
		List<DbSimpleViewColumn> dbExtraViewColumns = new ArrayList<DbSimpleViewColumn>();
		SortedMap<String, String> simpleUniqueColumns = new TreeMap<String, String>();

		// Dummy placeholder (LinkedHashMap guarantees that the order)
		dbColumns.put("INTID", null);

		for (EntityFieldMetaDataVO fieldMeta : provider.getAllEntityFieldsByEntity(entityMeta.getEntity()).values()) {
			DbColumnType columnType = createDbColumnType(fieldMeta.getDataType(), fieldMeta.getScale(), fieldMeta.getPrecision());
			boolean isCalculated = (fieldMeta.getCalcFunction() != null);
			boolean isForeignReference = (fieldMeta.getForeignEntity() != null);

			DbColumn dbColumn = null;
			DbSimpleViewColumn dbExtraViewColumn = null;
			if (isCalculated) {
				dbExtraViewColumn = new DbSimpleViewColumn(
					generateDbName(StringUtils.toUpperCase(fieldMeta.getDbColumn())),
					columnType, fieldMeta.getCalcFunction(), "INTID");
			} else if (isForeignReference) {
				String dbColumnName = generateDbName(DalUtils.getDbIdFieldName(fieldMeta.getDbColumn()));
				dbColumn = new DbColumn(dbTableName, dbColumnName,
					ID_COLUMN_TYPE,
					DbNullable.of(Boolean.TRUE.equals(fieldMeta.isNullable())), fieldMeta.getDefaultMandatory());

				EntityMetaDataVO foreignEntity = provider.getEntity(fieldMeta.getForeignEntity());
				if (foreignEntity == null) {
					throw new IllegalArgumentException("Entity " + entityMeta.getEntity() + ": Foreign entity " + fieldMeta.getForeignEntity() + " does not exist");
				}

				String dbForeignTableName = generateDbName(getTableName(foreignEntity));

				boolean onDeleteCascade = fieldMeta.isOnDeleteCascade();

				// TODO: was XR_<ID> but this does not work consistently for system entities => ...
				DbForeignKeyConstraint fkConstraint = new DbForeignKeyConstraint(dbTableName,
					generateDbName("XR_" + tableName, dbColumnName),
					Arrays.asList(dbColumnName), dbForeignTableName, null, Arrays.asList("INTID"), onDeleteCascade);

				// @TODO GOREF: delete if not needed
				if (foreignEntity.getEntity().equals("nuclos_generalsearch")) {
//					log.warn("Entity field " + entityMeta.getEntity() + "." + fieldMeta.getField() + " references nuclos_genericobject or nuclos_generalsearch");
					continue;
				}

				if (!fieldMeta.isReadonly() && !foreignEntity.isVirtual())
					fkConstraints.add(fkConstraint);

				boolean isJoin = !StringUtils.toUpperCase(fieldMeta.getDbColumn()).startsWith("INTID_");
				if (isJoin) {
					dbExtraViewColumn = new DbSimpleViewColumn(
						generateDbName(StringUtils.toUpperCase(fieldMeta.getDbColumn())),
						columnType, fkConstraint, getViewPatternForField(fieldMeta, provider));
				} else if (fieldMeta.getForeignEntityField() != null) {
//					log.info("Join for entity field " + entityMeta.getEntity() + "." + fieldMeta.getField() + " skipped");
				}
			} else {
				String dbColumnName = StringUtils.toUpperCase(fieldMeta.getDbColumn());
				dbColumn = new DbColumn(dbTableName, dbColumnName,
					columnType, DbNullable.of(Boolean.TRUE.equals(fieldMeta.isNullable())), fieldMeta.getDefaultMandatory());
			}

			if (dbColumn != null) {
				if (Boolean.TRUE.equals(fieldMeta.isUnique())) {
					simpleUniqueColumns.put(fieldMeta.getField(), dbColumn.getColumnName());
				}
				if (Boolean.TRUE.equals(fieldMeta.isIndexed())) {
					DbIndex index = new DbIndex(dbTableName,
						generateDbName("XIE_" + tableName, dbColumn.getColumnName()),
						Arrays.asList(dbColumn.getColumnName()));
					indexes.put(dbColumn.getColumnName(), index);
				}

				dbColumns.put(dbColumn.getColumnName(), dbColumn);
				dbColumnsByField.put(fieldMeta.getField(), dbColumn);
			}

			if (dbExtraViewColumn != null) {
				dbExtraViewColumns.add(dbExtraViewColumn);
			}
		}

		// Overwrite columns if necessary
		// TODO: check that the types matches if a user mapping exists
		for (Map.Entry<String, DbColumnType> e : SYSTEM_COLUMNS.entrySet()) {
			String dbColumnName = e.getKey();
			dbColumns.put(dbColumnName, new DbColumn(dbTableName, dbColumnName, e.getValue(), DbNullable.NOT_NULL, null));
		}

		List<DbTableArtifact> tableArtifacts = new ArrayList<DbTableArtifact>();
		if (!entityMeta.isVirtual()) {
			// Columns
			tableArtifacts.addAll(dbColumns.values());
			// Primary Key
			tableArtifacts.add(new DbPrimaryKeyConstraint(dbTableName, generateDbName("PK_" + tableName), Arrays.asList("INTID")));
			// Foreign Keys
			tableArtifacts.addAll(fkConstraints);
			// Unique Columns
			List<List<String>> uniqueColumnsList = new ArrayList<List<String>>();
			// - System entities support multiple unique combinations
			Collection<Set<String>> uniqueFieldCombinations = entityMeta.getUniqueFieldCombinations();
			if (uniqueFieldCombinations != null) {
				for (Set<String> uniqueFields : uniqueFieldCombinations) {
					uniqueColumnsList.add(mapFieldList(uniqueFields, dbColumnsByField));
				}
			}

			// - Legacy behaviour: generate one combined unique key for all flagged columns
			if (uniqueColumnsList.isEmpty() && simpleUniqueColumns.size() > 0) {
				uniqueColumnsList.add(new ArrayList<String>(simpleUniqueColumns.values()));
			}
			// - Generate unique constraints (and remove index for the same columns)
			for (List<String> uniqueColumns : uniqueColumnsList) {
				tableArtifacts.add(new DbUniqueConstraint(dbTableName, generateDbName("XAK_" + tableName, uniqueColumns), uniqueColumns));
				if (uniqueColumns.size() == 1) {
					indexes.remove(uniqueColumns.get(0));
				}
			}

			// Indexes
			tableArtifacts.addAll(indexes.values());
		}

		// View
		String viewName = getViewName(entityMeta);
		if (viewName != null) {

			DbObjectHelper dbObjectHelper = new DbObjectHelper(dbAccess);
			if (!dbObjectHelper.hasUserdefinedEntityView(entityMeta)) {

				List<DbSimpleViewColumn> dbViewColumns = CollectionUtils.transform(dbColumns.values(), new Transformer<DbColumn, DbSimpleViewColumn>() {
					@Override
					public DbSimpleViewColumn transform(DbColumn c) { return new DbSimpleViewColumn(c.getColumnName()); }
				});
				dbViewColumns.addAll(dbExtraViewColumns);

				tableArtifacts.add(new DbSimpleView(dbTableName, generateDbName(viewName), dbViewColumns));
			}
		}

		DbTable dbTable = new DbTable(dbTableName, tableArtifacts, entityMeta.isVirtual());
		return dbTable;
	}
	
	public static String getDbRefColumn(EntityFieldMetaDataVO field) {
		if (field.getForeignEntity() == null) {
			throw new IllegalArgumentException();
		}
		final String dbColumn = field.getDbColumn().toUpperCase();
		final String result;
		if (dbColumn.startsWith("STRVALUE_")) {
			result = "INTID_" + dbColumn.substring(9);
		}
		else {
			throw new IllegalArgumentException();
		}
		return result;
	}

	/**
	 * @deprecated Stringified refs are now dereferenced by table joins. Hence the whole method is 
	 * 		obsolete. (tp)
	 */
	private static List<?> getViewPatternForField(EntityFieldMetaDataVO fieldMeta, MetaDataProvider provider) {
		List<Object> result = new ArrayList<Object>();
		boolean isForeignReference = (fieldMeta.getForeignEntity() != null || fieldMeta.getLookupEntity() != null);

		if (isForeignReference) {
			boolean isJoin = !StringUtils.toUpperCase(fieldMeta.getDbColumn()).startsWith("INTID_");
			if (isJoin) {

				EntityMetaDataVO foreignEntity = provider.getEntity(fieldMeta.getForeignEntity() != null ? fieldMeta.getForeignEntity() : fieldMeta.getLookupEntity());
				
				String foreignFieldName = null;
				if (fieldMeta.getForeignEntity() != null)
					foreignFieldName = fieldMeta.getForeignEntityField();
				else if (fieldMeta.getLookupEntity() != null)
					foreignFieldName = fieldMeta.getLookupEntityField();
				
				if (foreignFieldName == null || foreignFieldName.isEmpty())
					foreignFieldName = "name";

				String[] parts = StringUtils.splitWithMatches(PARAM_PATTERN, 1, foreignFieldName);
				if (parts.length == 1) {
					// no parameter pattern, so the whole text is interpreted as field name
					parts = new String[] {"", parts[0]};
				}
				for (int i = 0; i < parts.length; i++) {
					if (i % 2 == 0 && !parts[i].isEmpty()) {
						// (Non-empty) text fragment
						result.add(parts[i]);
					} else if (i % 2 == 1) {
						// Parameter pattern -> field name
						String foreignDbColumn = null;
						try {
							foreignDbColumn = provider.getEntityField(foreignEntity.getEntity(), parts[i]).getDbColumn();
						} catch (CommonFatalException e) {
							if (DalUtils.isDbIdField(fieldMeta.getDbColumn())) {
								foreignDbColumn = "INTID";
							} else {
								throw e;
							}
						}
						result.add(DbIdent.makeIdent(foreignDbColumn));
					}
				}
			} else {
				result.add(DbIdent.makeIdent(fieldMeta.getDbColumn()));
			}
		} else {
			result.add(DbIdent.makeIdent(fieldMeta.getDbColumn()));
		}

		return result;
	}

	private static List<String> mapFieldList(Collection<String> fields, Map<String, DbColumn> dbColumnsByField) {
		List<String> list = new ArrayList<String>();
		for (String field : fields) {
			DbColumn dbColumn = dbColumnsByField.get(field);
			if (dbColumn == null)
				throw new IllegalArgumentException("Field " + field + " does not exists");
			list.add(dbColumn.getColumnName());
		}
		return list;
	}

	private String generateDbName(String name, String...affixes) {
		return dbAccess.generateName(name, affixes);
	}

	private String generateDbName(String name, List<String> affixes) {
		return dbAccess.generateName(name, affixes.toArray(new String[affixes.size()]));
	}

	public static String getTableName(EntityMetaDataVO entityMeta) {
		if (entityMeta.isVirtual()) {
			return entityMeta.getVirtualentity();
		}
		String tableName = StringUtils.toUpperCase(entityMeta.getDbEntity());
		if (tableName.startsWith("V_")) {
			tableName = "T_" + tableName.substring(2);
		}
		else if (tableName.startsWith("T_")){
			// do nothing
		}
		// could this really happen? (tp)
		else {
			assert false : tableName;
		}
		return tableName;
	}

	/**
	 * @deprecated Consider that the auto-generated views of nuclos are deprecated.
	 * 		Hence there should be no need to use this method. (tp)
	 */
	public static String getViewName(EntityMetaDataVO entityMeta) {
		String tableName = StringUtils.toUpperCase(entityMeta.getDbEntity());
		if (tableName.startsWith("V_")) {
			return tableName;
		}
		else if (tableName.startsWith("T_")) {
			return "V_" + tableName.substring(2);
		}
		return null;
	}
	
	public static String getTableOrViewForSelect(EntityMetaDataVO eMeta) {
		// dbSourceForDML = eMeta.isVirtual() ? eMeta.getVirtualentity() : "T_" + eMeta.getDbEntity().substring(2);
		final String result;
		if (eMeta.isVirtual()) {
			result = eMeta.getVirtualentity();
		}
		else if (eMeta.isDynamic()) {
			result = eMeta.getDbEntity();
		}
		else if (eMeta.getReadDelegate() != null) {
			result = eMeta.getReadDelegate();
		}
		else {
			result = "T_" + eMeta.getDbEntity().substring(2);
		}
		return result;
	}

	public static DbColumnType createDbColumnType(EntityFieldMetaDataVO fieldMeta) {
		return createDbColumnType(fieldMeta.getDataType(), fieldMeta.getScale(), fieldMeta.getPrecision());
	}

	private static DbColumnType createDbColumnType(String javaClass, Integer oldScale, Integer oldPrecision) {
		try {
			return createDbColumnType(Class.forName(javaClass), oldScale, oldPrecision);
		} catch(ClassNotFoundException e) {
			throw new NuclosFatalException(e);
		}
	}

	private static DbColumnType createDbColumnType(Class<?> javaClass, Integer oldScale, Integer oldPrecision) {
		javaClass = DalUtils.getDbType(javaClass);
		DbGenericType genericType;
		Integer length = null, scale = null, precision = null;
		if (javaClass == String.class && oldScale == null) {
			genericType = DbGenericType.CLOB;
		} else if (javaClass == String.class) {
			genericType = DbGenericType.VARCHAR;
			length = oldScale;
		} else if (javaClass == Integer.class || javaClass == Long.class) {
			genericType = DbGenericType.NUMERIC;
			precision = oldScale;
			scale = 0;
		} else if (javaClass == Double.class) {
			genericType = DbGenericType.NUMERIC;
			precision = oldScale;
			scale = oldPrecision;
		} else if (javaClass == Boolean.class) {
			genericType = DbGenericType.BOOLEAN;
		} else if (javaClass == Date.class) {
			genericType = DbGenericType.DATE;
		} else if (javaClass == InternalTimestamp.class || javaClass == NuclosDateTime.class) {
			genericType = DbGenericType.DATETIME;
		} else if (javaClass == byte[].class) {
			genericType = DbGenericType.BLOB;
		} else if (javaClass == NuclosPassword.class) {
			genericType = DbGenericType.VARCHAR;
			length = CryptUtil.calcSizeForAESHexInputLength(oldScale);
		} else if (javaClass == NuclosScript.class) {
			genericType = DbGenericType.CLOB;
		} else {
			throw new IllegalArgumentException("Unsupported DB column type mapping for " + javaClass);
		}
		return new DbColumnType(genericType, null, length, precision, scale);
	}

	/**
	 * @deprecated Please use {@link org.nuclos.server.dblayer.util.ForeignEntityFieldParser}. (tp)
	 */
	private static final Pattern PARAM_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
}
