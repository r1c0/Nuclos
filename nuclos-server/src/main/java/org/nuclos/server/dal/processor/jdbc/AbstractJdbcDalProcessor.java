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

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nuclos.common.CloneUtils;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.IDalVO;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.dal.processor.AbstractDalProcessor;
import org.nuclos.server.dal.processor.ColumnToBeanVORefMapping;
import org.nuclos.server.dal.processor.ColumnToRefFieldVOMapping;
import org.nuclos.server.dal.processor.IColumnToVOMapping;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbAccess;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.IBatch;
import org.nuclos.server.dblayer.expression.DbNull;
import org.nuclos.server.dblayer.query.DbCondition;
import org.nuclos.server.dblayer.query.DbExpression;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbJoin;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.dblayer.statements.DbDeleteStatement;
import org.nuclos.server.dblayer.statements.DbInsertStatement;
import org.nuclos.server.dblayer.statements.DbStatement;
import org.nuclos.server.dblayer.statements.DbTableStatement;
import org.nuclos.server.dblayer.statements.DbUpdateStatement;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractJdbcDalProcessor<DalVO extends IDalVO> extends AbstractDalProcessor<DalVO> {

	private static final Logger LOG = Logger.getLogger(AbstractJdbcDalProcessor.class);
	
	protected DataBaseHelper dataBaseHelper;

   // This must be clone and hence cannot be final.
   protected List<IColumnToVOMapping<? extends Object>> allColumns;
   private Set<IColumnToVOMapping<? extends Object>> allColumnsAsSet;

   public AbstractJdbcDalProcessor(Class<DalVO> type, List<IColumnToVOMapping<? extends Object>> allColumns) {
      super(type);
      this.allColumns = allColumns;
      this.allColumnsAsSet = new HashSet<IColumnToVOMapping<? extends Object>>(allColumns);
      checkColumns();
   }
   
   @Autowired
   void setDataBaseHelper(DataBaseHelper dataBaseHelper) {
	   this.dataBaseHelper = dataBaseHelper;
   }

	public Object clone() {
		final AbstractJdbcDalProcessor<DalVO> clone;
		try {
			clone = (AbstractJdbcDalProcessor<DalVO>) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e.toString());
		}
		clone.allColumns = (List<IColumnToVOMapping<? extends Object>>) CloneUtils.cloneCollection(allColumns);
		clone.allColumnsAsSet = (Set<IColumnToVOMapping<? extends Object>>) CloneUtils.cloneCollection(allColumnsAsSet);
		return clone;
	}

   private void checkColumns() {
      if (allColumns.size() != allColumnsAsSet.size()) {
    	  LOG.warn("Duplicates in columns, size is "  + allColumns.size() + " but only " + allColumnsAsSet.size() + " unique elements");
    	  assert false;
      }
   }

   /**
    * Table (or view) to use when writing to DB.
    * <p>
    * This is only different from {@link #getDbSourceForSQL()} for 
    * DB read delegates.
    * </p>
    */
   protected abstract String getDbSourceForDML();

   /**
    * Table or view to use when reading from DB.
    */
   protected abstract String getDbSourceForSQL();

   protected abstract IColumnToVOMapping<Long> getPrimaryKeyColumn();

   protected List<DalVO> getAll() {
      return getAll(allColumns);
   }

	public List<Long> getAllIds() {
		DbQuery<Long> query = createSingleColumnQuery(getPrimaryKeyColumn(), true);
		return dataBaseHelper.getDbAccess().executeQuery(query);
	}

   protected List<DalVO> getAll(final List<IColumnToVOMapping<? extends Object>> columns) {
      DbQuery<Object[]> query = createQuery(columns);
      return dataBaseHelper.getDbAccess().executeQuery(query, createResultTransformer(columns));
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
      query.where(query.getBuilder().equal(column.getDbColumn(from), id));
      return dataBaseHelper.getDbAccess().executeQuery(query, createResultTransformer(columns));
   }

   protected List<DalVO> getByPrimaryKeys(final List<IColumnToVOMapping<? extends Object>> columns, final List<Long> ids) {
      DbQuery<Object[]> query = createQuery(columns);
      DbFrom from = CollectionUtils.getFirst(query.getRoots());
      DbExpression<?> pkExpr = getPrimaryKeyColumn().getDbColumn(from);
      Transformer<Object[], DalVO> transformer = createResultTransformer(columns);

      List<DalVO> result = new ArrayList<DalVO>(ids.size());
      for (List<Long> idSubList : CollectionUtils.splitEvery(ids, query.getBuilder().getInLimit())) {
         query.where(pkExpr.as(Long.class).in(idSubList));
         result.addAll(dataBaseHelper.getDbAccess().executeQuery(query, transformer));
      }
      return result;
   }

   protected void insertOrUpdate(final DalVO dalVO) {
      insertOrUpdate(allColumns, dalVO);
   }

   protected <S> void insertOrUpdate(final List<IColumnToVOMapping<? extends Object>> columns, final DalVO dalVO) {
	   final DalCallResult result = batchInsertOrUpdate(columns, CollectionUtils.asList(dalVO), false);
	   result.throwFirstException();
   }

   protected DalCallResult batchInsertOrUpdate(final Collection<DalVO> colDalVO, boolean failAfterBatch) {
      return batchInsertOrUpdate(allColumns, colDalVO, failAfterBatch);
   }

   protected DalCallResult batchInsertOrUpdate(final List<IColumnToVOMapping<? extends Object>> columns, final Collection<DalVO> colDalVO, boolean failAfterBatch) {
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
               dataBaseHelper.getDbAccess().execute(stmt);
            } catch (DbException ex) {
            	if (!failAfterBatch) {
            		throw ex;
            	}
            	// TODO: readable message
            	ex.setIdIfNull(dalVO.getId());
            	ex.setStatementsIfNull(getLogStatements(stmt));
				dcr.addBusinessException(ex);
            }
         }
      }
      return dcr;
   }

   protected DalCallResult batchDelete(final Collection<Long> colId, boolean failAfterBatch) {
		DalCallResult dcr = new DalCallResult();
		for(Long id : colId) {
			DbStatement stmt = new DbDeleteStatement(getDbSourceForDML(), getPrimaryKeyMap(id));
			try {
				dataBaseHelper.getDbAccess().execute(stmt);
			} catch(DbException ex) {
				if (!failAfterBatch) {
					throw ex;
				}
				// TODO: readable message
            	ex.setIdIfNull(id);
            	ex.setStatementsIfNull(getLogStatements(stmt));
				dcr.addBusinessException(ex);
			}
		}
      return dcr;
   }

   protected void delete(final Long id) throws DbException {
	   final DalCallResult result = batchDelete(Collections.singletonList(id), false);
	   result.throwFirstException();
   }

   protected DbQuery<Object[]> createQuery(List<IColumnToVOMapping<? extends Object>> columns) {
	   return createQuery(columns, false);
   }

   protected DbQuery<Object[]> createQuery(List<IColumnToVOMapping<? extends Object>> columns, boolean overrideDbSourceUseDML) {
      DbQuery<Object[]> query = dataBaseHelper.getDbAccess().getQueryBuilder().createQuery(Object[].class);
      DbFrom from = query.from(overrideDbSourceUseDML ? getDbSourceForDML() : getDbSourceForSQL()).alias(SystemFields.BASE_ALIAS);
      List<DbExpression<?>> selections = new ArrayList<DbExpression<?>>();
      for (IColumnToVOMapping<?> column : columns) {
         if (column instanceof ColumnToBeanVORefMapping<?>) {
        	 ColumnToBeanVORefMapping<?> refColumn = (ColumnToBeanVORefMapping<?>) column;
        	 String tableAlias = refColumn.getTableAlias();
        	 DbJoin join = null;
        	 for (DbJoin j : from.getJoins()) {
        		 if (j.getAlias() != null && j.getAlias().equals(tableAlias)) {
        			 join = j;
        		 }
        	 }
        	 if (join == null) {
        		 join = from.join(refColumn.getTable(), refColumn.getType()).alias(refColumn.getTableAlias()).on(refColumn.getRefColumn(), "INTID", Long.class);
        	 }
        	 selections.add(refColumn.getDbColumn(join));
         }
         else {
        	 selections.add(column.getDbColumn(from));
         }
      }
      query.multiselect(selections);
      return query;
   }

	protected <S> DbQuery<S> createSingleColumnQuery(IColumnToVOMapping<S> column, boolean overrideDbSourceUseDML) {
		DbQuery<S> query = dataBaseHelper.getDbAccess().getQueryBuilder().createQuery(column.getDataType());
		DbFrom from = query.from(overrideDbSourceUseDML ? getDbSourceForDML() : getDbSourceForSQL()).alias(SystemFields.BASE_ALIAS);
		query.select(column.getDbColumn(from));
		return query;
	}

   protected DbQuery<Long> createCountQuery(IColumnToVOMapping<Long> column) {
      DbQuery<Long> query = dataBaseHelper.getDbAccess().getQueryBuilder().createQuery(column.getDataType());
      DbFrom from = query.from(getDbSourceForSQL()).alias(SystemFields.BASE_ALIAS);
      query.select(query.getBuilder().count(column.getDbColumn(from)));
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
			// also ignore stringified references
			if (column instanceof ColumnToRefFieldVOMapping) {
				final EntityFieldMetaDataVO meta = ((ColumnToRefFieldVOMapping<?>) column).getMeta();
				final String dbColumn = meta.getDbColumn();
				if (meta.getForeignEntity() != null && (dbColumn.startsWith("STRVALUE_") || dbColumn.startsWith("INTVALUE_"))) {
					continue;
				}
			}
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

	protected SQLIntegrityConstraintViolationException checkLogicalUniqueConstraint(
			final Map<IColumnToVOMapping<?>, Object> values, final Long id) {
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Long> query = builder.createQuery(Long.class);
		DbFrom from = query.from(getDbSourceForSQL()).alias(SystemFields.BASE_ALIAS);
		query.select(query.getBuilder().countRows());
		List<DbCondition> conditions = new ArrayList<DbCondition>();

		boolean bFullIsNullCondition = true;
		for (Map.Entry<IColumnToVOMapping<?>, Object> e : values.entrySet()) {
			Object value = e.getValue();
			DbExpression<?> c = e.getKey().getDbColumn(from);
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
		Long count = dataBaseHelper.getDbAccess().executeQuerySingleResult(query);
		if (count > 1L) {
			return new SQLIntegrityConstraintViolationException("Unique constraint violated in query '"
					+ query + "' with id=" + id + ", number of result is " + count);
		}
		return null;
	}

	private List<String> getLogStatements(DbStatement stmt) {
		List<String> statements = null;
		try {
			final DbAccess dbAccess = dataBaseHelper.getDbAccess();
			final IBatch batch = dbAccess.getBatchFor(stmt);
			statements = dbAccess.getStatementsForLogging(batch);
		} catch (SQLException e) {
			LOG.warn("getLogStatements failed", e);
		}
		return statements;
		/*
		return CollectionUtils.transform(statements, new Transformer<PreparedString, String>() {
			@Override
			public String transform(PreparedString ps) {
				return ps.toString() + " <[" + Arrays.toString(ps.getParameters()) + "]>";
			}
		});
		*/
	}

   // TODO:
   protected String getReadableMessage(DbException ex) {
      // return ex.getMessage();
      return ex.toString();
   }

	public void addToColumns(IColumnToVOMapping<? extends Object> column) {
		if (allColumnsAsSet.add(column)) {
			allColumns.add(column);
		}
	}

	public void setAllColumns(List<IColumnToVOMapping<? extends Object>> columns) {
		allColumns.clear();
		allColumns.addAll(columns);

		allColumnsAsSet.clear();
		allColumnsAsSet.addAll(columns);

		checkColumns();
	}

}
