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
package org.nuclos.server.dblayer.impl.db2;

import static org.nuclos.common2.StringUtils.join;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.DbType;
import org.nuclos.server.dblayer.EBatchType;
import org.nuclos.server.dblayer.IBatch;
import org.nuclos.server.dblayer.IPart;
import org.nuclos.server.dblayer.IPart.NextPartHandling;
import org.nuclos.server.dblayer.impl.BatchImpl;
import org.nuclos.server.dblayer.impl.PartImpl;
import org.nuclos.server.dblayer.impl.SqlConditionalUnit;
import org.nuclos.server.dblayer.impl.SqlSequentialUnit;
import org.nuclos.server.dblayer.impl.SubPartImpl;
import org.nuclos.server.dblayer.impl.standard.MetaDataSchemaExtractor;
import org.nuclos.server.dblayer.impl.standard.StandardSqlDBAccess;
import org.nuclos.server.dblayer.impl.util.PreparedString;
import org.nuclos.server.dblayer.impl.util.PreparedStringBuilder;
import org.nuclos.server.dblayer.incubator.DbExecutor.ResultSetRunner;
import org.nuclos.server.dblayer.query.DbExpression;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.dblayer.structure.DbArtifact;
import org.nuclos.server.dblayer.structure.DbCallable;
import org.nuclos.server.dblayer.structure.DbColumn;
import org.nuclos.server.dblayer.structure.DbColumnType;
import org.nuclos.server.dblayer.structure.DbColumnType.DbGenericType;
import org.nuclos.server.dblayer.structure.DbConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbPrimaryKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbUniqueConstraint;
import org.nuclos.server.dblayer.structure.DbIndex;
import org.nuclos.server.dblayer.structure.DbNullable;
import org.nuclos.server.dblayer.structure.DbSequence;
import org.nuclos.server.dblayer.structure.DbSimpleView;
import org.nuclos.server.dblayer.structure.DbSimpleView.DbSimpleViewColumn;
import org.nuclos.server.dblayer.structure.DbTable;

/**
 * @author Thomas Pasch
 * @since Nuclos 3.2.0
 */
public class DB2DBAccess extends StandardSqlDBAccess {

	public static final String AUTOSAVEPOINT = "autosavepoint";

    private static final Logger LOG = Logger.getLogger(DB2DBAccess.class);

	private boolean autoSavepoint = true;
	
	public DB2DBAccess() {
	}
	
	@Override
	public void init(DbType type, DataSource dataSource, Map<String, String> config) {
		if (config.containsKey(AUTOSAVEPOINT)) {
			autoSavepoint = Boolean.valueOf(config.get(AUTOSAVEPOINT));
		}
		if (autoSavepoint) {
			LOG.info("Auto savepoint activated");
		}
		this.executor = new DB2Executor(dataSource, config.get(USERNAME), config.get(PASSWORD)); 
		super.init(type, dataSource, config);
	}
	
	@Override
	protected boolean setStatementHint(Statement stmt, String hint, Object value) throws SQLException {
		if (QUERY_TIMEOUT_HINT.equals(hint)) {
			// not supported by PostgreSQL driver
			return false;
		}
		return super.setStatementHint(stmt, hint, value);
	}

	@Override
	public <T> T executeFunction(String name, final Class<T> resultType, Object ... args) throws DbException {
		String sql = String.format("SELECT %s(%s)", name, StringUtils.join(",", CollectionUtils.replicate("?", args.length)));
		return this.executeQuery(sql, null, new ResultSetRunner<T>() {
			@Override
			public T perform(ResultSet rs) throws SQLException { return rs.next() ? getResultSetValue(rs, 1, resultType) : null; }
		}, args);
	}

	@Override
	public void runWithDisabledChecksAndTriggers(Runnable runnable) throws DbException {
		// TODO:
		throw new UnsupportedOperationException();
	}

	@Override
	public String generateName(String base, String...affixes) {
		return generateName(30, base, affixes);
	}

	@Override
	public DbQueryBuilder getQueryBuilder() {
		return new DB2QueryBuilder(this);
	}

	@Override
	protected String getDataType(DbColumnType columnType) throws DbException {
		if (columnType.getTypeName() != null) {
			return columnType.getTypeName() + columnType.getParametersString();
		} else {
			switch (columnType.getGenericType()) {
			case VARCHAR:
				return String.format("VARCHAR(%d)", columnType.getLength());
			case NUMERIC:
				return String.format("NUMERIC(%d,%d)", columnType.getPrecision(), columnType.getScale());
			case BOOLEAN:
				return String.format("VARCHAR(%d)", 1);
			case BLOB:
				return "BLOB";
			case CLOB:
				return "CLOB";
			case DATE:
				return "DATE";
			case DATETIME:
				return "TIMESTAMP";
			default:
				throw new DbException("Unsupported column type " + columnType.getGenericType());
			}
		}
	}

	@Override
	protected IBatch getSqlForCreateIndex(DbIndex index) {
		return BatchImpl.simpleBatch(PreparedString.format("CREATE INDEX %s ON %s (%s)",
			getName(index.getIndexName()),
			getQualifiedName(index.getTableName()),
			join(",", index.getColumnNames())));
	}

	@Override
	protected IBatch getSqlForAlterTableColumn(DbColumn column1, DbColumn column2) throws SQLException {
		final String sColumnSpec = getColumnSpecForAlterTableColumn(column2, column1);
		final IBatch result;
		if(sColumnSpec != null) {
			final PreparedString ps = PreparedString.format("ALTER TABLE %s %s",
				getQualifiedName(column2.getTableName()),
				sColumnSpec);
			
			if(column2.getDefaultValue() != null && column2.getNullable().equals(DbNullable.NOT_NULL)) {
				result = getSqlForUpdateNotNullColumn(column2);
				result.append(new SqlSequentialUnit(ps));
			}
			else {
				result = BatchImpl.simpleBatch(ps);
			}
		}
		else {
			result = null;
		}
		return result;
	}
	
	@Override
	protected IBatch getSqlForAlterTableNotNullColumn(DbColumn column) {
		String columnSpec = String.format("ALTER %s TYPE %s",
			column.getColumnName(),
			getDataType(column.getColumnType()));
		

			columnSpec += String.format(", ALTER %s %s NOT NULL",
				column.getColumnName(), "SET");
		
		PreparedString str = PreparedString.format("ALTER TABLE %s %s",
			getQualifiedName(column.getTableName()),
			columnSpec);
		
    	return BatchImpl.simpleBatch(str);
    }

	@Override
	protected String getColumnSpecForAlterTableColumn(DbColumn column, DbColumn oldColumn) {
		if(!getDataType(column.getColumnType()).equals(getDataType(oldColumn.getColumnType()))) {
			String columnSpec = String.format("ALTER %s TYPE %s",
				column.getColumnName(),
				getDataType(column.getColumnType()));
			if (column.getNullable() != oldColumn.getNullable()) {
				// if nullability is changed, add another 
				columnSpec += String.format(", ALTER %s %s NOT NULL",
					column.getColumnName(),
					column.getNullable() == DbNullable.NOT_NULL ? "SET" : "DROP");
			}
			return columnSpec;
		}
		else {			
			if (column.getNullable() != oldColumn.getNullable()) {
				// if nullability is changed, add another 
				return String.format("ALTER %s %s NOT NULL",
					column.getColumnName(),
					column.getNullable() == DbNullable.NOT_NULL ? "SET" : "DROP");
			}
			else {
				return null;
			}			
		}
	}

	@Override
	protected IBatch getSqlForDropColumn(DbColumn column) {
		return BatchImpl.simpleBatch(PreparedString.format("ALTER TABLE %s DROP COLUMN %s CASCADE",
			getQualifiedName(column.getTableName()),
			column.getColumnName()));
	}

    /**
     * @deprecated Views has always been problematic (especially with PostgreSQL).
     * 	Avoid whenever possible.
     */
    @Override
    protected IBatch getSqlForCreateSimpleView(DbSimpleView view) throws DbException {
    	return BatchImpl.simpleBatch(_getSqlForCreateSimpleView("CREATE VIEW", view, ""));
    }

    /*protected PreparedString _getSqlForCreateSimpleView(String prefix, DbSimpleView view, String suffix) throws DbException {
    	final TableAliasForSimpleView aliasFactory = new TableAliasForSimpleView();
        final StringBuilder fromClause = new StringBuilder();
        fromClause.append(getQualifiedName(view.getTableName()) + " " + SystemFields.BASE_ALIAS);

        int fkIndex = 1;
        for (DbSimpleViewColumn vc : view.getReferencingViewColumns()) {
        	final String tableAlias = aliasFactory.getTableAlias(vc);
            final DbReference fk = vc.getReference();
            fromClause.append(String.format("\nLEFT OUTER JOIN %s %s ON (%s)",
            	getQualifiedName(fk.getReferencedTableName()),
                tableAlias,
				join(" AND ", transform(fk.getReferences(), new Transformer<Pair<String, String>, String>() {
					@Override
					public String transform(Pair<String, String> p) {
						return String.format("t.%s=%s.%s", p.x, tableAlias, p.y);
					}
				}))));
        }
        return PreparedString.format("%s %s(\n%s\n) AS SELECT\n%s\nFROM\n%s %s",
        	prefix,
            getQualifiedName(view.getViewName()),
            join(",\n", view.getViewColumnNames()),
            join(",\n", transform(view.getViewColumns(), new Transformer<DbSimpleViewColumn, String>() {
                // int fkIndex = 1;
                @Override
                public String transform(DbSimpleViewColumn vc) {
                	String sql;
                    switch (vc.getViewColumnType()) {
                    case TABLE:
                        // ignore column type (must be the same as the original column)
                        return String.format("t.%s", vc.getColumnName());
                    case FOREIGN_REFERENCE:
                        // int fkN = fkIndex++;
                        sql = null;
                        for (Object obj : vc.getViewPattern()) {
                            String sql2;
                            if (obj instanceof DbIdent) {
                                sql2 = String.format("%s.%s", aliasFactory.getTableAlias(vc), ((DbIdent) obj).getName());
                            } else {
                                String s = (String) obj;
                                if (s.isEmpty())
                                    continue;
                                sql2 = "'" + SQLUtils2.escape(s) +"'";
                            }
                            sql = (sql != null) ? getSqlForConcat(sql, sql2) : sql2;
                        }
                        sql = getSqlForCast(sql, vc.getColumnType());
                        return getSqlForNullCheck(String.format("t.%s", getFirst(vc.getReference().getReferences()).x), sql);
                    case FUNCTION:
                        sql = String.format("%s(%s)",
                            getFunctionNameForUseInView(vc.getFunctionName()),
                            StringUtils.join(",", CollectionUtils.transform(vc.getArgColumns(),
                                new Transformer<String, String>() {
                                @Override public String transform(String c) { return "t." + c; }
                            })));
                        return getSqlForCast(sql, vc.getColumnType());
                    default:
                        throw new DbException("Unsupported view column type " + vc.getViewColumnType());
                    }
                }
            })),
            fromClause,
            suffix);
    }*/

    
    /**
     * With PostgreSQL views sometimes get deleted when one of the underlying tables
     * is alter. Because of this drop not always succeed. Hence we <em>ignore</em> any
     * problem when dropping a view.
     * 
     * @author Thomas Pasch
     * @since Nuclos 3.2.0
     */
    @Override
    protected IBatch getSqlForDropSimpleView(DbSimpleView view) {
    	return new BatchImpl(new SqlConditionalUnit(_getSqlForDropSimpleView(view)));
    }

    protected PartImpl _getSqlForDropSimpleView(DbSimpleView view) {
    	final PartImpl part = new PartImpl(PreparedString.format("DROP VIEW %s",
                getQualifiedName(view.getViewName())), 
                EBatchType.FAIL_NEVER_IGNORE_EXCEPTION, NextPartHandling.ALWAYS);
    	return part;
    }

	@Override
	protected IBatch getSqlForAlterSimpleView(DbSimpleView oldView, DbSimpleView newView) {
		if (!oldView.getViewName().equals(newView.getViewName())) {
			throw new IllegalArgumentException();
		}
		if (!existsTableOrView(newView.getViewName())) {
			// view does not exist any more (PostgreSQL sometimes drops views when underlying
			// tables have been modified.) (tp)
			return getSqlForAlterSimpleViewFallback(oldView, newView);
		}

		// http://www.postgresql.org/docs/9.1/static/sql-createview.html
		// Create or replace is very restricted...
		
		final List<DbSimpleViewColumn> oldColumns = oldView.getViewColumns();
		final List<DbSimpleViewColumn> newColumns = newView.getViewColumns();
		
		if (oldColumns.size() > newColumns.size()) {
			// deleting columns is not possible
			return getSqlForAlterSimpleViewFallback(oldView, newView);
		}
		final Set<DbSimpleViewColumn> addSet = new HashSet<DbSimpleViewColumn>();
		final Set<DbSimpleViewColumn> deleteSet = new HashSet<DbSimpleViewColumn>(oldColumns);
		for (DbSimpleViewColumn nc: newColumns) {
			if (!deleteSet.remove(nc)) {
				addSet.add(nc);
			}
		}
		
		if (!deleteSet.isEmpty()) {
			// deleting columns is not possible
			return getSqlForAlterSimpleViewFallback(oldView, newView);
		}
		// Modify new column list in place.
		newColumns.clear();
		newColumns.addAll(ensureNaturalSequence(newView, oldColumns));
		newColumns.addAll(addSet);
		
		// Sometimes 'CREATE OR REPLACE VIEW' fails (even) for unknown reason ...
		final PreparedString ps = _getSqlForCreateSimpleView("CREATE OR REPLACE VIEW", newView, "");
		final List<IPart> parts = new ArrayList<IPart>(2);
		parts.add(new PartImpl(
				ps, EBatchType.FAIL_NEVER_IGNORE_EXCEPTION, NextPartHandling.ONLY_IF_THIS_FAILS));
		
		// ... in this case we fall back to first DROP and then CREATE. (tp)
		final List<IPart> subParts = new ArrayList<IPart>(2);
		subParts.add(_getSqlForDropSimpleView(oldView));
		subParts.add(new PartImpl(_getSqlForCreateSimpleView("CREATE VIEW", newView, ""), 
				EBatchType.FAIL_LATE, NextPartHandling.ALWAYS));
		parts.add(new SubPartImpl(subParts, EBatchType.FAIL_LATE, NextPartHandling.ALWAYS));
		
		final SqlConditionalUnit unit = new SqlConditionalUnit(parts);
		final IBatch result = new BatchImpl(unit);
		return result;
	}

    @Override
    protected IBatch getSqlForCreateUniqueConstraint(DbUniqueConstraint constraint) {
    	return BatchImpl.simpleBatch(PreparedString.format("ALTER TABLE %s ADD CONSTRAINT %s UNIQUE (%s) %s",
            getQualifiedName(constraint.getTableName()),
            constraint.getConstraintName(),
            join(",", constraint.getColumnNames()),
            getUsingIndex(constraint)));
    }


	private IBatch getSqlForAlterSimpleViewFallback(DbSimpleView oldView, DbSimpleView newView) {
		final IBatch result = getSqlForDropSimpleView(oldView);
		result.append(getSqlForCreateSimpleView(newView));
		return result;
	}
	
	@Override
	protected IBatch getSqlForDropCallable(DbCallable callable) throws DbException {
		String code = callable.getCode();
		if (code == null)
			throw new DbException("No code for callable " + callable.getCallableName());
		Pattern pattern = Pattern.compile(
				String.format("\\s*(?:CREATE\\s+(?:OR\\s+REPLACE\\s+)?)?(%s\\s+%s\\s*\\([^)]*\\))",
					callable.getType(), callable.getCallableName()),
					Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(code);
		boolean success = matcher.lookingAt();
		if (!success)
			throw new DbException("Cannot interpret header for callable " + callable.getCallableName());

		String[] args = matcher.group(2).replaceAll("\\(", "").replaceAll("\\)", "").split(",");
		String stmt = callable.getType() + " " + callable.getCallableName() + " (";
		for (int i = 0; i < args.length; i++) {
			String[] dtype = args[i].trim().split(" ");
			if (dtype.length == 2)
				stmt += dtype[1];
			else
				throw new DbException("Cannot interpret header for callable " + callable.getCallableName());
			if (i < args.length - 1)
				stmt += ", ";
		}
		stmt += ")";

		return BatchImpl.simpleBatch(PreparedString.format("DROP %s", stmt));
	}
	
	/*@Override
	public String getSqlForConcat(String x, String y) {
		return String.format("COALESCE(CAST(%s as text), '') || COALESCE(CAST(%s as text), '')", x, y);
	}*/

	/*@Override
	public String getSqlForConcat(List<String> l) {
		if (l == null || l.isEmpty()) {
			throw new IllegalArgumentException();
		}
		final StringBuilder result = new StringBuilder("COALESCE(CAST(");
		for (Iterator<String> it = l.iterator(); it.hasNext();) {
			result.append(it.next()).append(" as text), '')");
			if (it.hasNext()) {
				result.append(" || COALESCE(CAST(");
			}
		}
		return result.toString();
	}*/

	@Override
	public boolean validateObjects() throws SQLException {
		Set<String> invalidObjects = executor.executeQuery(
			"select object_name, object_type from user_objects where object_type in ('FUNCTION', 'VIEW') and status = 'INVALID'",
			new ResultSetRunner<Set<String>>() {
				@Override
				public Set<String> perform(ResultSet rs) throws SQLException {
					Set<String> result = new LinkedHashSet<String>();
					while (rs.next())
						result.add(rs.getString("object_name"));
					return result;
				}
			});

		for (String invalidObject : invalidObjects) {
			executor.executeUpdate("alter " + invalidObject + " compile");
		}

		return true;
	}

    
    @Override
	protected MetaDataSchemaExtractor getMetaDataExtractor() {
		return new DB2MetaData();
	}

	@Override
	protected String getTablespaceSuffix(DbArtifact artifact) {
		String tablespace = getTablespace();
		if (tablespace != null) {
			if (artifact instanceof DbTable) {
				return "TABLESPACE " + tablespace;
			} else if (artifact instanceof DbPrimaryKeyConstraint) {
				return "TABLESPACE " + tablespace;
			}
		}
		return "";
	}

	@Override
	protected String getUsingIndex(DbConstraint constraint) {
		return "";
	}

	protected String getTablespace() {
		return StringUtils.nullIfEmpty(config.get(TABLESPACE));
	}
	
	static class DB2QueryBuilder extends StandardQueryBuilder {
		
		public DB2QueryBuilder(StandardSqlDBAccess dbAccess) {
			super(dbAccess);
		}

		@Override
		public DbExpression<String> upper(DbExpression<String> x) {
			return buildExpressionSql(String.class, "UPPER(CAST(", x, " AS VARCHAR))");
		}

		@Override
		public DbExpression<Date> currentDate() {
			return buildExpressionSql(java.util.Date.class, PreparedStringBuilder.valueOf("CURRENT_DATE"));
		}

		@Override
		protected void prepareSelect(PreparedStringBuilder ps, DbQuery<?> query) {
			if (query.getOffset() == null) {
				super.prepareSelect(ps, query);
			} else {
				ps.append("SELECT ");
				if (query.isDistinct())
					ps.append("DISTINCT ");
				ps = ps.append("* FROM (SELECT ");
			}
		}

		@Override
		protected void postprocessSelect(PreparedStringBuilder ps, DbQuery<?> query) {
			if (query.getOffset() == null) {
				super.postprocessSelect(ps, query);
			} else {
				ps = ps.append(", ROW_NUMBER() OVER (");
				super.prepareOrderBy(ps, query);
				ps = ps.append(") AS RN ");
			}
		}

		@Override
		protected void prepareOrderBy(PreparedStringBuilder ps, DbQuery<?> query) {
			if (query.getOffset() == null) {
				super.prepareOrderBy(ps, query);
			}
			// else, do nothing here
			// order by in <code>prepareSelect</code>
		}

		@Override
		protected PreparedStringBuilder buildPreparedString(DbQuery<?> query) {
			PreparedStringBuilder ps = super.buildPreparedString(query);
			//if (query.getMaxResults() != -1) {
			//ps.appendf(" FETCH FIRST %d ROWS ONLY", query.getMaxResults());
			//}
			if (query.getMaxResults() != -1 && query.getOffset() == null) {
				ps.prepend("SELECT * FROM (");
				ps.appendf(") WHERE ROWNUM <= %d", query.getMaxResults());
			} else if (query.getOffset() != null) {
				ps.appendf(") WHERE RN > %d", query.getOffset());
				if (query.getMaxResults() != -1){
					ps.appendf(" AND RN <= %d", query.getOffset().intValue() + query.getMaxResults());
				}
			}
			return ps;
		}
	}

	static class DB2MetaData extends MetaDataSchemaExtractor {

		@Override
		protected void initMetaData() throws SQLException {
			supportsJDBC4getFunction = false;
			super.initMetaData();
		}

		@Override
		protected Collection<DbSequence> getSequences() throws SQLException {
			Collection<DbSequence> sequences = new ArrayList<DbSequence>();
			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM DBA_SEQUENCES WHERE SEQUENCE_OWNER = ?");
			ResultSet rs = null;
			try {
				stmt.setString(1, schema);
				rs = stmt.executeQuery();
				while (rs.next()) {
					sequences.add(new DbSequence(rs.getString("SEQUENCE_NAME"), rs.getLong("LAST_NUMBER")));
				}
			} finally {
				if (rs != null)
					rs.close();
				stmt.close();
			}
			return sequences;
		}
		
		@Override
		public Map<String, String> getDatabaseParameters() throws SQLException {
			// TODO:
			return Collections.emptyMap();
		}

		@Override
		protected DbGenericType getDbGenericType(int sqlType, String typeName) {
			return DB2DBAccess.getDbGenericType(sqlType, typeName);
		}
	}
}
