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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.common.dal.exception.DalBusinessException;
import org.nuclos.common.dal.vo.IDalVO;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.processor.AbstractDalProcessor;
import org.nuclos.server.dal.processor.ColumnToBeanVOMapping;
import org.nuclos.server.dal.processor.ColumnToFieldIdVOMapping;
import org.nuclos.server.dal.processor.ColumnToFieldVOMapping;
import org.nuclos.server.dal.processor.IColumnToVOMapping;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.expression.DbNull;
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

public abstract class AbstractJdbcDalProcessor<DalVO extends IDalVO> extends AbstractDalProcessor<DalVO> {
	
	private static final Logger LOG = Logger.getLogger(AbstractJdbcDalProcessor.class);

   // This must be clone and hence cannot be final.
   protected List<IColumnToVOMapping<? extends Object>> allColumns;

   public AbstractJdbcDalProcessor(Class<DalVO> type, List<IColumnToVOMapping<? extends Object>> allColumns) {
      super(type);
      this.allColumns = allColumns;
   }

   protected abstract String getDbSourceForDML();

   protected abstract String getDbSourceForSQL();

   protected abstract IColumnToVOMapping<Long> getPrimaryKeyColumn();

   protected List<DalVO> getAll() {
      return getAll(allColumns);
   }

	public List<Long> getAllIds() {
		DbQuery<Long> query = createSingleColumnQuery(getPrimaryKeyColumn(), true);
		return DataBaseHelper.getDbAccess().executeQuery(query);
	}

   protected List<DalVO> getAll(final List<IColumnToVOMapping<? extends Object>> columns) {
      DbQuery<Object[]> query = createQuery(columns);
      return DataBaseHelper.getDbAccess().executeQuery(query, createResultTransformer(columns));
   }

   protected DalVO getByPrimaryKey(final Long id) {
      return getByPrimaryKey(allColumns, id);
   }

   protected DalVO getByPrimaryKey(final List<IColumnToVOMapping<? extends Object>> columns, final Long id) {
      List<DalVO> result = getByIdColumn(columns, getPrimaryKeyColumn(), id);

      if (result.size() == 1)
         return result.get(0);
      if (result.size() == 0)
         return null;

      throw new CommonFatalException("Primary key is not unique!");
   }

   protected List<DalVO> getByIdColumn(final List<IColumnToVOMapping<? extends Object>> columns, IColumnToVOMapping<Long> column, final Long id) {
      DbQuery<Object[]> query = createQuery(columns);
      DbFrom from = CollectionUtils.getFirst(query.getRoots());
      query.where(query.getBuilder().equal(getDbColumn(from, column), id));
      return DataBaseHelper.getDbAccess().executeQuery(query, createResultTransformer(columns));
   }

   protected List<DalVO> getByPrimaryKeys(final List<IColumnToVOMapping<? extends Object>> columns, final List<Long> ids) {
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

   protected DalCallResult insertOrUpdate(final DalVO dalVO) {
      return insertOrUpdate(allColumns, dalVO);
   }

   protected <S> DalCallResult insertOrUpdate(final List<IColumnToVOMapping<? extends Object>> columns, final DalVO dalVO) {
      return batchInsertOrUpdate(columns, CollectionUtils.asList(dalVO));
   }

   protected DalCallResult batchInsertOrUpdate(final Collection<DalVO> colDalVO) {
      return batchInsertOrUpdate(allColumns, colDalVO);
   }

   protected DalCallResult batchInsertOrUpdate(final List<IColumnToVOMapping<? extends Object>> columns, final Collection<DalVO> colDalVO) {
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
					LOG.error(e);
					dcr.addBusinessException(new DalBusinessException(dalVO.getId(), getReadableMessage(ex), ex));
				}
            }
         }
      }
      return dcr;
   }

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
					LOG.error(e);
					dcr.addBusinessException(new DalBusinessException(id, getReadableMessage(ex), ex));
				}
			}
		}
      return dcr;
   }

   protected DalCallResult delete(final Long id) {
      return batchDelete(Collections.singletonList(id));
   }
   
   protected DbQuery<Object[]> createQuery(List<IColumnToVOMapping<? extends Object>> columns) {
	   return createQuery(columns, false);
   }

   protected DbQuery<Object[]> createQuery(List<IColumnToVOMapping<? extends Object>> columns, boolean overrideDbSourceUseDML) {
      DbQuery<Object[]> query = DataBaseHelper.getDbAccess().getQueryBuilder().createQuery(Object[].class);
      DbFrom from = query.from(overrideDbSourceUseDML ? getDbSourceForDML() : getDbSourceForSQL()).alias("t");
      List<DbExpression<?>> selections = new ArrayList<DbExpression<?>>();
      for (IColumnToVOMapping<?> column : columns) {
         selections.add(getDbColumn(from, column));
      }
      query.multiselect(selections);
      return query;
   }

	protected <S> DbQuery<S> createSingleColumnQuery(IColumnToVOMapping<S> column, boolean overrideDbSourceUseDML) {
		DbQuery<S> query = DataBaseHelper.getDbAccess().getQueryBuilder().createQuery(column.getDataType());
		DbFrom from = query.from(overrideDbSourceUseDML ? getDbSourceForDML() : getDbSourceForSQL()).alias("t");
		DbExpression<S> dbColumn = getDbColumn(from, column);
		query.select(dbColumn);
		return query;
	}

   protected DbQuery<Long> createCountQuery(IColumnToVOMapping<Long> column) {
      DbQuery<Long> query = DataBaseHelper.getDbAccess().getQueryBuilder().createQuery(column.getDataType());
      DbFrom from = query.from(getDbSourceForSQL()).alias("t");
      query.select(query.getBuilder().count(getDbColumn(from, column)));
      return query;
   }

   protected <S> Map<String, Object> getColumnValuesMap(List<IColumnToVOMapping<? extends Object>> columns, DalVO dalVO, boolean withReadonly) {
      Map<String, Object> map = new LinkedHashMap<String, Object>();
      for (Entry<IColumnToVOMapping<?>, Object> entry : getColumnValuesMapWithMapping(columns, dalVO, withReadonly).entrySet()) {
      	map.put(entry.getKey().getColumn(), entry.getValue());
      }
      return map;
   }

   protected <S> Map<IColumnToVOMapping<?>, Object> getColumnValuesMapWithMapping(List<IColumnToVOMapping<? extends Object>> columns,
		   DalVO dalVO, boolean withReadonly) {
		final Map<IColumnToVOMapping<?>, Object> map = new LinkedHashMap<IColumnToVOMapping<?>, Object>();
		for (IColumnToVOMapping<?> column : columns) {
			if (!withReadonly && column.isReadonly()) continue;
			map.put(column, column.convertFromDalFieldToDbValue(dalVO));
		}
		return map;
	}

   private Map<String, Object> getPrimaryKeyMap(Long id) {
      return Collections.<String, Object>singletonMap(getPrimaryKeyColumn().getColumn(), id);
   }

   protected Transformer<Object[], DalVO> createResultTransformer(List<IColumnToVOMapping<? extends Object>> columns) {
      return getResultTransformer(columns.toArray(new IColumnToVOMapping[columns.size()]));
   }
   
   protected <S> Transformer<Object[], DalVO> getResultTransformer(final IColumnToVOMapping<Object>... columns) {
		return new Transformer<Object[], DalVO>() {
			@Override
			public DalVO transform(Object[] result) {
				try {
					final DalVO dalVO = (DalVO) newDalVOInstance();
					for (int i = 0, n = columns.length; i < n; i++) {
						final IColumnToVOMapping<Object> column = columns[i];
						final Object value = result[i];
						column.convertFromDbValueToDalField(dalVO, value);						
					}
					dalVO.processor(getProcessor());
					return dalVO;
				} catch (Exception e) {
					throw new CommonFatalException(e);
				}
			}
		};
	}

   protected DalBusinessException checkLogicalUniqueConstraint(final Map<IColumnToVOMapping<?>, Object> values, final Long id) {
      DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Long> query = builder.createQuery(Long.class);
      DbFrom from = query.from(getDbSourceForSQL()).alias("t");
      query.select(query.getBuilder().countRows());
      List<DbCondition> conditions = new ArrayList<DbCondition>();

      boolean bFullIsNullCondition = true;
      for (Map.Entry<IColumnToVOMapping<?>, Object> e : values.entrySet()) {
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

   private List<String> getLogStatements(List<PreparedString> statements) {
	   if (statements == null)
		   return null;

	   return CollectionUtils.transform(statements, new Transformer<PreparedString, String>(){
		@Override
        public String transform(PreparedString ps) {
	        return ps.toString() + " <[" + Arrays.toString(ps.getParameters()) + "]>";
        }});
   }

   protected <S> DbExpression<S> getDbColumn(DbFrom table, IColumnToVOMapping<?> mapping) {
	   if(mapping.isCaseSensitive())
		   return table.columnCaseSensitive(mapping.getColumn(), (Class<S>) DalUtils.getDbType(mapping.getDataType()));
	   else
		   return table.column(mapping.getColumn(), (Class<S>) DalUtils.getDbType(mapping.getDataType()));
   }

   // TODO:
   protected String getReadableMessage(DbException ex) {
      return ex.getMessage();
   }

	public void addToColumns(IColumnToVOMapping<? extends Object> column) {
		allColumns.add(column);
	}
	
	public void setAllColumns(List<IColumnToVOMapping<? extends Object>> columns) {
		allColumns.clear();
		allColumns.addAll(columns);
	}
	
}
