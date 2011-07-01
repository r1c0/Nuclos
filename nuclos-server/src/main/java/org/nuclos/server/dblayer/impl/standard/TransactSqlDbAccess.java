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
package org.nuclos.server.dblayer.impl.standard;

import static org.nuclos.common2.StringUtils.join;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nuclos.common.collection.Pair;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.DbNotUniqueException;
import org.nuclos.server.dblayer.DbReferentialIntegrityException;
import org.nuclos.server.dblayer.impl.util.PreparedStringBuilder;
import org.nuclos.server.dblayer.incubator.DbExecutor.ResultSetRunner;
import org.nuclos.server.dblayer.query.DbExpression;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.dblayer.structure.DbArtifact;
import org.nuclos.server.dblayer.structure.DbColumnType.DbGenericType;
import org.nuclos.server.dblayer.structure.DbConstraint;
import org.nuclos.server.dblayer.structure.DbIndex;
import org.nuclos.server.dblayer.structure.DbSequence;

/**
 * Base class for T-SQL databases.
 */
public abstract class TransactSqlDbAccess extends StandardSqlDBAccess {

	/**
	 * Hint used {@link DbSequence} object for associating the tablename.
	 */
	public static final String HINT_SEQUENCE_TABLE = "sequenceTable";

	@Override
	public Long getNextId(String sequenceName) throws DbException {
		return executor.executeQuery("EXECUTE " + sequenceName, new ResultSetRunner<Long>() {
			@Override
			public Long perform(ResultSet rs) throws SQLException { return rs.next() ? rs.getLong(1) : null; }
		});
	}

	@Override
	public String generateName(String base, String...affixes) {
		return generateName(30, base, affixes);
	}

	@Override
	protected void setStatementParameter(PreparedStatement stmt, int index, Object value, Class<?> javaType) throws SQLException {
		if (javaType == Boolean.class) {
			javaType = Integer.class;
			if (value != null)
				value = ((Boolean) value).booleanValue() ? 1 : 0;
		}
		super.setStatementParameter(stmt, index, value, javaType);
	}

	@Override
	public DbQueryBuilder getQueryBuilder() throws DbException {
		return new TransactSqlQueryBuilder();
	}

	@Override
	protected DbException wrapSQLException(SQLException ex) {
		try {
			if ("23000".equals(ex.getSQLState())) {
				Matcher matcher;
				switch (ex.getErrorCode()) {
				case 2627: // unique constraint
					matcher = EXCEPTION_IDENT.matcher(ex.getMessage());
					if (matcher.find()) {
						return new DbNotUniqueException(null, makeIdent(matcher.group(1)), ex);
					}
					break;
				case 547: // delete referential entry
					matcher = EXCEPTION_IDENT.matcher(ex.getMessage());
					if (matcher.find()) {
						return new DbReferentialIntegrityException(null, makeIdent(matcher.group(1)), ex);
					}
					break;
				}
			}
		} catch (Exception ex2) {
			// log this exception...
			log.warn("Exception thrown during wrapSQLException", ex2);
			// ...but throw the original SQLException
		}
		return super.wrapSQLException(ex);
	}	

	private static String IDENT_REGEX = "(\\w+)";
	private static Pattern EXCEPTION_IDENT = Pattern.compile("'" + IDENT_REGEX + "'");

	@Override
	public boolean validateObjects() throws DbException {
		List<String> validateStmts = executor.executeQuery(
			"select table_name, table_type from information_schema.tables where table_schema = '" + getSchemaName() + "'",
			new ResultSetRunner<List<String>>() {
				@Override
				public List<String> perform(ResultSet rs) throws SQLException {
					List<String> cmds = new ArrayList<String>();
					while (rs.next()) {
						final String sObjectName = rs.getString("table_name");
						final String sObjectType = rs.getString("table_type");

						if ("BASE TABLE".equals(sObjectType)) {
							cmds.add("execute sp_recompile '"+sObjectName+"'");
						} else if ("VIEW".equals(sObjectType)) {
							cmds.add("execute sp_recompile '"+sObjectName+"'");
							cmds.add("execute sp_refreshview '"+sObjectName+"'");
						}
					}
					return cmds;
				}
			});

		for (String validateStmt : validateStmts) {
			executor.executeUpdate(validateStmt);
		}

		return true;
	}

	@Override
	protected List<String> getSqlForCreateIndex(DbIndex index) {
		return Collections.singletonList(String.format("CREATE INDEX %s ON %s (%s)",
			getName(index.getIndexName()),
			getQualifiedName(index.getTableName()),
			join(",", index.getColumnNames())));
	}

	@Override
	protected List<String> getSqlForDropIndex(DbIndex index) {
		return Collections.singletonList(String.format("DROP INDEX %s.%s",
			getQualifiedName(index.getTableName()),
			getName(index.getIndexName())));
	}

	@Override
	protected abstract List<String> getSqlForCreateSequence(DbSequence sequence);

	@Override
	protected abstract List<String> getSqlForDropSequence(DbSequence sequence);

	@Override
	protected List<String> getSqlForAlterSequence(DbSequence sequence1, DbSequence sequence2) {
		// Alter sequence is a drop/create combination
		long restartWith = Math.max(sequence1.getStartWith(), sequence2.getStartWith());
		List<String> sql = new ArrayList<String>();
		sql.addAll(getSqlForDropSequence(sequence1));
		sql.addAll(getSqlForCreateSequence(new DbSequence(sequence2.getSequenceName(), restartWith)));
		return sql;
	}

	@Override
	protected String getSqlForConcat(String x, String y) {
		return String.format("convert(varchar, %s)+convert(varchar, %s)", x, y);
	}   

	protected Pair<String, String> getObjectNamesForSequence(DbSequence sequence) {
		String tableName = sequence.getHint(HINT_SEQUENCE_TABLE);
		if (tableName == null) {
			tableName = generateName("T_AD_SEQUENCE", sequence.getSequenceName());   		
		} else {
			tableName = generateName(tableName);
		}
		return Pair.makePair(sequence.getSequenceName(), tableName);
	}	

	@Override
	protected String getTablespaceSuffix(DbArtifact artifact) {
		return "";
	}

	@Override
	protected String getUsingIndex(DbConstraint constraint) {
		return "";
	}

	@Override
	public void runWithDisabledChecksAndTriggers(Runnable runnable) throws DbException {
		// Just like before
		throw new UnsupportedOperationException();
	}

	protected abstract class TransactSqlMetaDataExtractor extends MetaDataSchemaExtractor {

		@Override
		public Map<String, String> getDatabaseParameters() throws SQLException {
			return null;
		}

		@Override
		protected DbGenericType getDbGenericType(int sqlType, String typeName) {
			return TransactSqlDbAccess.this.getDbGenericType(sqlType, typeName);
		}
	}

	class TransactSqlQueryBuilder extends QueryBuilder {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public DbExpression<Date> currentDate() {
			// convert GETDATE() to VARCHAR and then to DATETIME because there is no trunc function in sql server
			return buildExpressionSql(java.util.Date.class, PreparedStringBuilder.valueOf("CONVERT(DATETIME,CONVERT(VARCHAR(10),GETDATE(),104),104)"));
		}

		@Override
		public DbExpression<String> convertDateToString(DbExpression<java.util.Date> x, String pattern) {
			if (pattern.equals(DATE_PATTERN_GERMAN)) {
				return buildExpressionSql(String.class, "CONVERT(VARCHAR(10),", x, ",104)");
			} else {
				throw new UnsupportedOperationException("Unsupported date pattern '" +pattern + "'");
			}
		}

		@Override
		protected void prepareSelect(PreparedStringBuilder ps, DbQuery<?> query) {
			ps.append("SELECT ");
			if (query.isDistinct())
				ps.append("DISTINCT ");
			if (query.getMaxResults() != -1) {
				ps = ps.append(String.format("TOP %d ", query.getMaxResults()));
			}
		}
	}
}
