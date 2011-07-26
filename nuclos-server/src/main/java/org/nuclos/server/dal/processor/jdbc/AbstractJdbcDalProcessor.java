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
package org.nuclos.server.dal.processor.jdbc;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.nuclos.common.NuclosImage;
import org.nuclos.common.NuclosPassword;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.common.dal.exception.DalBusinessException;
import org.nuclos.common.dal.vo.IDalVO;
import org.nuclos.common2.DateTime;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.processor.AbstractDalProcessor;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.expression.DbNull;
import org.nuclos.server.dblayer.impl.standard.StandardSqlDBAccess;
import org.nuclos.server.dblayer.impl.util.PreparedString;
import org.nuclos.server.dblayer.query.DbCondition;
import org.nuclos.server.dblayer.query.DbExpression;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.dblayer.statements.DbDeleteStatement;
import org.nuclos.server.dblayer.statements.DbInsertStatement;
import org.nuclos.server.dblayer.statements.DbStatement;
import org.nuclos.server.dblayer.statements.DbTableStatement;
import org.nuclos.server.dblayer.statements.DbUpdateStatement;
import org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile;
import org.nuclos.server.report.ByteArrayCarrier;
import org.nuclos.server.resource.valueobject.ResourceFile;

public abstract class AbstractJdbcDalProcessor<T, DalVO extends IDalVO<T>> extends AbstractDalProcessor<DalVO> {

   protected final List<ColumnToVOMapping<?>> allColumns = new ArrayList<ColumnToVOMapping<?>>();

   public AbstractJdbcDalProcessor() {
      super();
   }

   public AbstractJdbcDalProcessor(int maxFieldCount, int maxFieldIdCount) {
      super(maxFieldCount, maxFieldIdCount);
   }

   /**
    *
    * @return
    */
   protected abstract String getDbSourceForDML();

   /**
    *
    * @return
    */
   protected abstract String getDbSourceForSQL();

   /**
    *
    * @return
    */
   protected abstract ColumnToVOMapping<Long> getPrimaryKeyColumn();

   /**
    *
    * @return
    */
   protected List<DalVO> getAll() {
      return getAll(allColumns);
   }

   /**
    *
    * @return
    */
	public List<Long> getAllIds() {
		DbQuery<Long> query = createSingleColumnQuery(getPrimaryKeyColumn(), true);
		return DataBaseHelper.getDbAccess().executeQuery(query);
	}

   /**
    *
    * @param columns
    * @return
    */
   protected List<DalVO> getAll(final List<ColumnToVOMapping<?>> columns) {
      DbQuery<Object[]> query = createQuery(columns);
      return DataBaseHelper.getDbAccess().executeQuery(query, createResultTransformer(columns));
   }

   /**
    *
    * @param id
    * @return
    */
   protected DalVO getByPrimaryKey(final Long id) {
      return getByPrimaryKey(allColumns, id);
   }

   /**
    *
    * @param columns
    * @param id
    * @return
    */
   protected DalVO getByPrimaryKey(final List<ColumnToVOMapping<?>> columns, final Long id) {
      List<DalVO> result = getByIdColumn(columns, getPrimaryKeyColumn(), id);

      if (result.size() == 1)
         return result.get(0);
      if (result.size() == 0)
         return null;

      throw new CommonFatalException("Primary key is not unique!");
   }

   protected List<DalVO> getByIdColumn(final List<ColumnToVOMapping<?>> columns, ColumnToVOMapping<Long> column, final Long id) {
      DbQuery<Object[]> query = createQuery(columns);
      DbFrom from = CollectionUtils.getFirst(query.getRoots());
      query.where(query.getBuilder().equal(getDbColumn(from, column), id));
      return DataBaseHelper.getDbAccess().executeQuery(query, createResultTransformer(columns));
   }

   /**
    *
    * @param columns
    * @param ids
    * @return
    */
   protected List<DalVO> getByPrimaryKeys(final List<ColumnToVOMapping<?>> columns, final List<Long> ids) {
      DbQuery<Object[]> query = createQuery(columns);
      DbFrom from = CollectionUtils.getFirst(query.getRoots());
      DbExpression<?> pkExpr = getDbColumn(from, getPrimaryKeyColumn());
      Transformer<Object[], DalVO> transformer = createResultTransformer(columns);

      List<DalVO> result = new ArrayList<DalVO>(ids.size());
      for (List<Long> idSubList : CollectionUtils.splitEvery(ids, query.getBuilder().getInLimit())) {
         query.where(pkExpr.as(Long.class).in(idSubList));
         result.addAll(DataBaseHelper.getDbAccess().executeQuery(query, transformer));
      }
      return result;
   }

   /**
    *
    * @param dalVO
    * @return
    */
   protected DalCallResult insertOrUpdate(final DalVO dalVO) {
      return insertOrUpdate(allColumns, dalVO);
   }

   /**
    *
    * @param columns
    * @param dalVO
    * @return
    */
   protected DalCallResult insertOrUpdate(final List<ColumnToVOMapping<?>> columns, final DalVO dalVO) {
      return batchInsertOrUpdate(columns, CollectionUtils.asList(dalVO));
   }

   /**
    *
    * @param colDalVO
    * @return
    */
   protected DalCallResult batchInsertOrUpdate(final Collection<DalVO> colDalVO) {
      return batchInsertOrUpdate(allColumns, colDalVO);
   }

   /**
    *
    * @param columns
    * @param colDalVO
    * @return
    */
   protected DalCallResult batchInsertOrUpdate(final List<ColumnToVOMapping<?>> columns, final Collection<DalVO> colDalVO) {
      DalCallResult dcr = new DalCallResult();
      for (DalVO dalVO : colDalVO) {
         Map<String, Object> columnValueMap = getColumnValuesMap(columns, dalVO, false);
         DbTableStatement stmt = null;
         if (dalVO.isFlagNew()) {
            stmt = new DbInsertStatement(getDbSourceForDML(), columnValueMap);
         } else if (dalVO.isFlagUpdated()) {
            stmt = new DbUpdateStatement(getDbSourceForDML(), columnValueMap, getPrimaryKeyMap(dalVO.getId()));
         }
         if (stmt != null) {
            try {
               DataBaseHelper.getDbAccess().execute(stmt);
            } catch (DbException ex) {
            	// TODO: readable message
				try {
					dcr.addBusinessException(new DalBusinessException(dalVO.getId(), getReadableMessage(ex), getLogStatements(DataBaseHelper.getDbAccess().getPreparedSqlFor(stmt)), ex));
				} catch (Exception e) {
					error(e);
					dcr.addBusinessException(new DalBusinessException(dalVO.getId(), getReadableMessage(ex), ex));
				}
            }
         }
      }
      return dcr;
   }

   /**
    *
    * @param colId
    * @return
    */
   protected DalCallResult batchDelete(final Collection<Long> colId) {
		DalCallResult dcr = new DalCallResult();
		for(Long id : colId) {
			DbStatement stmt = new DbDeleteStatement(getDbSourceForDML(), getPrimaryKeyMap(id));
			try {
				DataBaseHelper.getDbAccess().execute(stmt);
			} catch(DbException ex) {
				// TODO: readable message
				try {
					dcr.addBusinessException(new DalBusinessException(id, getReadableMessage(ex), getLogStatements(DataBaseHelper.getDbAccess().getPreparedSqlFor(stmt)), ex));
				} catch (Exception e) {
					error(e);
					dcr.addBusinessException(new DalBusinessException(id, getReadableMessage(ex), ex));
				}
			}
		}
      return dcr;
   }

   /**
    *
    * @param id
    */
   protected DalCallResult delete(final Long id) {
      return batchDelete(Collections.singletonList(id));
   }
   
   protected DbQuery<Object[]> createQuery(List<ColumnToVOMapping<?>> columns) {
	   return createQuery(columns, false);
   }

   protected DbQuery<Object[]> createQuery(List<ColumnToVOMapping<?>> columns, boolean overrideDbSourceUseDML) {
      DbQuery<Object[]> query = DataBaseHelper.getDbAccess().getQueryBuilder().createQuery(Object[].class);
      DbFrom from = query.from(overrideDbSourceUseDML ? getDbSourceForDML() : getDbSourceForSQL()).alias("t");
      List<DbExpression<?>> selections = new ArrayList<DbExpression<?>>();
      for (ColumnToVOMapping<?> column : columns) {
         selections.add(getDbColumn(from, column));
      }
      query.multiselect(selections);
      return query;
   }

	protected <T> DbQuery<T> createSingleColumnQuery(ColumnToVOMapping<T> column, boolean overrideDbSourceUseDML) {
      DbQuery<T> query = DataBaseHelper.getDbAccess().getQueryBuilder().createQuery(column.dataType);
		DbFrom from = query.from(overrideDbSourceUseDML?getDbSourceForDML():getDbSourceForSQL()).alias("t");
      DbExpression<T> dbColumn = this.<T>getDbColumn(from, column);
      query.select(dbColumn);
      return query;
   }

   protected DbQuery<Long> createCountQuery(ColumnToVOMapping<Long> column) {
      DbQuery<Long> query = DataBaseHelper.getDbAccess().getQueryBuilder().createQuery(column.dataType);
      DbFrom from = query.from(getDbSourceForSQL()).alias("t");
      query.select(query.getBuilder().count(getDbColumn(from, column)));
      return query;
   }

   protected Map<String, Object> getColumnValuesMap(List<ColumnToVOMapping<?>> columns, DalVO dalVO, boolean withReadonly) {
      Map<String, Object> map = new LinkedHashMap<String, Object>();
      for (Entry<ColumnToVOMapping<?>, Object> entry : getColumnValuesMapWithMapping(columns, dalVO, withReadonly).entrySet()) {
      	map.put(entry.getKey().column, entry.getValue());
      }
      return map;
   }

   protected Map<ColumnToVOMapping<?>, Object> getColumnValuesMapWithMapping(List<ColumnToVOMapping<?>> columns, DalVO dalVO, boolean withReadonly) {
      Map<ColumnToVOMapping<?>, Object> map = new LinkedHashMap<ColumnToVOMapping<?>, Object>();
      for (ColumnToVOMapping<?> column : columns) {
         if (!withReadonly && column.isReadonly)
            continue;

         if (column.isField) {
            if (column.isFieldId) {
               // @TODO GOREF: is this correct? (column.dataType but field is id)
               map.put(column, convertToDbValue(column.dataType, dalVO.getFieldIds().get(column.field)));
            } else {
               map.put(column, convertToDbValue(column.dataType, dalVO.getFields().get(column.field)));
            }
         } else {
            try {
               map.put(column, convertToDbValue(column.dataType, column.getMethod.invoke(dalVO)));
            } catch(Exception e) {
               throw new CommonFatalException(e);
            }
         }
      }
      return map;
   }

   private Map<String, Object> getPrimaryKeyMap(Long id) {
      return Collections.<String, Object>singletonMap(getPrimaryKeyColumn().column, id);
   }

   protected Transformer<Object[], DalVO> createResultTransformer(List<ColumnToVOMapping<?>> columns) {
      return getResultTransformer(columns.toArray(new ColumnToVOMapping[columns.size()]));
   }

   protected Transformer<Object[], DalVO> getResultTransformer(final ColumnToVOMapping<?> ... columns) {
      return new Transformer<Object[], DalVO>() {
         @Override
         public DalVO transform(Object[] result) {
            try {
               DalVO dalVO = newDalVOInstance();
               for (int i = 0, n = columns.length; i < n; i++) {
                  ColumnToVOMapping<?> column = columns[i];
                  Object value = result[i];
                  if (column.isField) {
                     if (column.isFieldId) {
                        dalVO.getFieldIds().put(column.field, (Long) convertFromDbValue(value, column.column, DT_LONG, dalVO.getId()));
                     } else {
                        dalVO.getFields().put(column.field, (T) convertFromDbValue(value, column.column, column.dataType, dalVO.getId()));
                     }
                  } else {
                     column.setMethod.invoke(dalVO, convertFromDbValue(value, column.column, column.dataType, dalVO.getId()));
                  }
               }
               dalVO.processor(getProcessor());
               return dalVO;
            } catch (Exception e) {
               throw new CommonFatalException(e);
            }
         }
      };
   }

   protected DalBusinessException checkLogicalUniqueConstraint(final Map<ColumnToVOMapping<?>, Object> values, final Long id) {
      DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Long> query = builder.createQuery(Long.class);
      DbFrom from = query.from(getDbSourceForSQL()).alias("t");
      query.select(query.getBuilder().countRows());
      List<DbCondition> conditions = new ArrayList<DbCondition>();

      boolean bFullIsNullCondition = true;
      for (Map.Entry<ColumnToVOMapping<?>, Object> e : values.entrySet()) {
      	Object value = e.getValue();
			DbExpression<?> c = getDbColumn(from, e.getKey());
			if (DbNull.isNull(value)) {
				conditions.add(builder.isNull(c));
			} else {
				conditions.add(builder.equal(c, value));
				bFullIsNullCondition = false;
			}
      }

      if (bFullIsNullCondition) {
      	// If all unique key fields are null, no exception is thrown (Reference: Oracle)
      	return null;
      }

      query.where(builder.and(conditions.toArray(new DbCondition[conditions.size()])));
      Long count = DataBaseHelper.getDbAccess().executeQuerySingleResult(query);
      if (count > 1L) {
      	return new DalBusinessException(id, "dal.logical.unique.constraint.violated");
      }
      return null;
   }

   /**
    *
    * @param column
    * @param rs
    * @param column
    * @param dataType
    * @return
    */
   private <S> S convertFromDbValue(Object value, String column, final Class<S> dataType, final Long recordId) {
      if (dataType == ByteArrayCarrier.class) {
         return value==null? null : (S) new ByteArrayCarrier((byte[]) value);
      } else if (dataType == NuclosImage.class) {
          NuclosImage ni = new NuclosImage("", (byte[]) value, null, false);
          return (S) ni;
      } else if (dataType == ResourceFile.class) {
      	return (S) new ResourceFile((String) value, LangUtils.convertId(recordId));
      } else if (dataType == GenericObjectDocumentFile.class) {
      	if(value == null){
      		return null;
      	}
      	return (S) new GenericObjectDocumentFile((String) value, LangUtils.convertId(recordId));
      } else if (dataType == DateTime.class) {
      	return (S) new DateTime((java.util.Date) value);
      } else if (dataType == NuclosPassword.class) {
    	  if(value instanceof NuclosPassword)
    		  return (S) value;
      	try {
	        return (S) new NuclosPassword(StandardSqlDBAccess.decrypt((String) value));
        }
        catch(SQLException e) {
	        throw new CommonFatalException(e);
        }
      } else {
      	return dataType.cast(value);
      }
   }

   private Object convertToDbValue(Class<?> javaType, Object value) {
      if (value == null) {
         return DbNull.forType(DalUtils.getDbType(javaType));
      } else if (value instanceof ByteArrayCarrier) {
         return ((ByteArrayCarrier) value).getData();
      } else if (value instanceof NuclosImage) { 
          NuclosImage ni = (NuclosImage)value;
          if(ni.getContent() != null) {
	          ByteArrayCarrier bac = new ByteArrayCarrier(ni.getContent());
	          return bac.getData();
          }
          else {
        	  return DbNull.forType(DalUtils.getDbType(javaType));
          }
      } else if (value instanceof ResourceFile) {
         return ((ResourceFile) value).getFilename();
      } else if (value instanceof GenericObjectDocumentFile) {
         return ((GenericObjectDocumentFile) value).getFilename();
      } else if (value instanceof DateTime) {
      	return new InternalTimestamp(((DateTime) value).getTime());
       } else if (value instanceof NuclosPassword) {
      	 try {
	        String encrypted = StandardSqlDBAccess.encrypt(((NuclosPassword) value).getValue());
	        if (encrypted == null) {
	        	return DbNull.forType(java.lang.String.class);
	        }
	        else {
	        	return encrypted;
	        }
        }
        catch(SQLException e) {
        	throw new CommonFatalException(e);
        }
       } else {
         return value;
      }
   }

   private List<String> getLogStatements(List<PreparedString> statements) {
	   if (statements == null)
		   return null;

	   return CollectionUtils.transform(statements, new Transformer<PreparedString, String>(){
		@Override
        public String transform(PreparedString ps) {
	        return ps.toString() + " <[" + Arrays.toString(ps.getParameters()) + "]>";
        }});
   }

   @SuppressWarnings("unchecked")
   protected <T> DbExpression<T> getDbColumn(DbFrom table, ColumnToVOMapping<?> mapping) {
	   if(mapping.caseSensitive)
		   return table.columnCaseSensitive(mapping.column, (Class<T>) DalUtils.getDbType(mapping.dataType));
	   else
		   return table.column(mapping.column, (Class<T>) DalUtils.getDbType(mapping.dataType));
   }

   /**
    *
    * @param column
    * @param field
    * @param dataType
    * @return
    */
   protected <T> ColumnToVOMapping<T> createSimpleStaticMapping(final String column, final String field, final Class<T> dataType) {
      return this.createSimpleStaticMapping(column, field, dataType, false);
   }

   /**
    *
    * @param column
    * @param field
    * @param dataType
    * @param isReadonly;
    * @return
    */
   protected <T> ColumnToVOMapping<T> createSimpleStaticMapping(final String column, final String field, final Class<T> dataType, boolean isReadonly) {
      final String xetterSuffix = field.substring(0,1).toUpperCase() + field.substring(1);
      final Class<?> clazz = getDalVOClass();
      try {
         return new ColumnToVOMapping<T>(column,
            clazz.getMethod("set"+xetterSuffix, dataType),
            clazz.getMethod((DT_BOOLEAN.equals(dataType)?"is":"get")+xetterSuffix), dataType, isReadonly);
      }
      catch(Exception e) {
         throw new CommonFatalException("On " + clazz + ": " + e);
      }
   }

   /**
    *
    * @param column
    * @param field
    * @param isFieldId
    * @param dataType
    * @param isReadonly

    * @return
    */
   @SuppressWarnings("unchecked")
   protected static <T> ColumnToVOMapping<T> createSimpleDynamicMapping(final String column, final String field, final String dataType, boolean isReadonly, boolean isFieldId, boolean caseSensitive) {
      try {
         return new ColumnToVOMapping<T>(column, field, isFieldId, (Class<T>) Class.forName(dataType), isReadonly, caseSensitive);
      }
      catch(ClassNotFoundException e) {
         throw new CommonFatalException(e);
      }
   }

   // TODO:
   protected String getReadableMessage(DbException ex) {
      return ex.getMessage();
   }


   /**
    * Type parameter T is the java type
    */
   protected final static class ColumnToVOMapping<T> {
      public final String column;
      public final Class<T> dataType;
      public final boolean isField;
      public final boolean isFieldId;
      public final Method setMethod;
      public final Method getMethod;
      public final String field;
      public final boolean isReadonly;
      public final boolean caseSensitive;


      /**
       * Konstruktor f\u00fcr statische VO Werte (Aufruf von Methoden zum setzen und lesen von Werten)
       * @param column
       * @param setMethod
       * @param getMethod
       * @param dataType
       * @param isReadonly
       */
      public ColumnToVOMapping(String column, Method setMethod, Method getMethod, Class<T> dataType, boolean isReadonly) {
         this.column = column;
         this.isField = false;
         this.isFieldId = false;
         this.setMethod = setMethod;
         this.getMethod = getMethod;
         this.field = null;
         this.dataType = dataType;
         this.isReadonly = isReadonly;
         this.caseSensitive = false;
      }

      /**
       * Konstruktor f\u00fcr dynamische VO Werte (Die Werte werden in einer "Field"-Liste gespeichert)
       * @param column
       * @param field
       * @param dataType
       * @param isReadonly
       * @param isFieldId
       */
      public ColumnToVOMapping(String column, String field, boolean isFieldId, Class<T> dataType, boolean isReadonly, boolean caseSensitive) {
         this.column = column;
         this.isField = true;
         this.setMethod = null;
         this.getMethod = null;
         this.field = field;
         this.dataType = dataType;
         this.isReadonly = isReadonly;
         this.isFieldId = isFieldId;
         this.caseSensitive = caseSensitive;
      }

      @Override
      public String toString() {
    	  return column + " [" + dataType + "]";
      }
   }
}
