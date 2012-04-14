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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.nuclos.common.collection.Pair;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.DbNotUniqueException;
import org.nuclos.server.dblayer.DbReferentialIntegrityException;
import org.nuclos.server.dblayer.DbType;
import org.nuclos.server.dblayer.IBatch;
import org.nuclos.server.dblayer.impl.BatchImpl;
import org.nuclos.server.dblayer.impl.util.PreparedString;
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

    private static final Logger LOG = Logger.getLogger(TransactSqlDbAccess.class);

	private static String IDENT_REGEX = "(\\w+)";
	private static Pattern EXCEPTION_IDENT = Pattern.compile("'" + IDENT_REGEX + "'");

	/**
	 * Hint used {@link DbSequence} object for associating the tablename.
	 */
	public static final String HINT_SEQUENCE_TABLE = "sequenceTable";
	
	//
	
	public TransactSqlDbAccess() {
	}
	
	@Override
	public void init(DbType type, DataSource dataSource, Map<String, String> config) {
		this.executor = new TransactSqlExecutor(dataSource, config.get(USERNAME), config.get(PASSWORD)); 
		super.init(type, dataSource, config);
	}
	
	@Override
	public String generateName(String base, String...affixes) {
		return generateName(30, base, affixes);
	}

	@Override
	public DbQueryBuilder getQueryBuilder() throws DbException {
		return new TransactSqlQueryBuilder(this);
	}

	@Override
	protected DbException wrapSQLException(Long id, String message, SQLException ex) {
		try {
			if ("23000".equals(ex.getSQLState())) {
				Matcher matcher;
				switch (ex.getErrorCode()) {
				case 2627: // unique constraint
					matcher = EXCEPTION_IDENT.matcher(ex.getMessage());
					if (matcher.find()) {
						return new DbNotUniqueException(id, null, makeIdent(matcher.group(1)), ex);
					}
					break;
				case 547: // delete referential entry
					matcher = EXCEPTION_IDENT.matcher(ex.getMessage());
					if (matcher.find()) {
						return new DbReferentialIntegrityException(id, null, makeIdent(matcher.group(1)), ex);
					}
					break;
				}
			}
		} catch (Exception ex2) {
			// log this exception...
			LOG.warn("Exception thrown during wrapSQLException", ex2);
			// ...but throw the original SQLException
		}
		return super.wrapSQLException(id, message, ex);
	}	

	@Override
	public boolean validateObjects() throws SQLException {
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
	protected IBatch getSqlForCreateIndex(DbIndex index) {
		return BatchImpl.simpleBatch(PreparedString.format("CREATE INDEX %s ON %s (%s)",
			getName(index.getIndexName()),
			getQualifiedName(index.getTableName()),
			join(",", index.getColumnNames())));
	}

	@Override
	protected IBatch getSqlForDropIndex(DbIndex index) {
		return BatchImpl.simpleBatch(PreparedString.format("DROP INDEX %s.%s",
			getQualifiedName(index.getTableName()),
			getName(index.getIndexName())));
	}

	@Override
	protected abstract IBatch getSqlForCreateSequence(DbSequence sequence);

	@Override
	protected abstract IBatch getSqlForDropSequence(DbSequence sequence);

	@Override
	protected IBatch getSqlForAlterSequence(DbSequence sequence1, DbSequence sequence2) {
		// Alter sequence is a drop/create combination
		long restartWith = Math.max(sequence1.getStartWith(), sequence2.getStartWith());
		final IBatch sql = getSqlForDropSequence(sequence1);
		sql.append(getSqlForCreateSequence(new DbSequence(sequence2.getSequenceName(), restartWith)));
		return sql;
	}

	@Override
	public String getSqlForConcat(String x, String y) {
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

	protected static abstract class TransactSqlMetaDataExtractor extends MetaDataSchemaExtractor {

		@Override
		public Map<String, String> getDatabaseParameters() throws SQLException {
			return null;
		}

		@Override
		protected DbGenericType getDbGenericType(int sqlType, String typeName) {
			return TransactSqlDbAccess.getDbGenericType(sqlType, typeName);
		}
	}

	static class TransactSqlQueryBuilder extends StandardQueryBuilder {
		
		public TransactSqlQueryBuilder(StandardSqlDBAccess dbAccess) {
			super(dbAccess);
		}

		@Override
		public DbExpression<Date> currentDate() {
			// convert GETDATE() to VARCHAR and then to DATETIME because there is no trunc function in sql server
			return buildExpressionSql(java.util.Date.class, PreparedStringBuilder.valueOf("CONVERT(DATETIME,CONVERT(VARCHAR(10),GETDATE(),104),104)"));
		}

		@Override
		public DbExpression<String> convertDateToString(DbExpression<java.util.Date> x, String pattern) {
			if (pattern.equalsIgnoreCase(DATE_PATTERN_GERMAN)) {
				return buildExpressionSql(String.class, "CONVERT(VARCHAR(10),", x, ",104)");
			} else if (pattern.equalsIgnoreCase(DATE_PATTERN_ENGLISH)) {
				return buildExpressionSql(String.class, "CONVERT(VARCHAR(10),", x, ",103)");
			} else if (pattern.equalsIgnoreCase(DATE_PATTERN_USA1)) {
				return buildExpressionSql(String.class, "CONVERT(VARCHAR(10),", x, ",101)");
			} else if (pattern.equalsIgnoreCase(DATE_PATTERN_USA2)) {
				return buildExpressionSql(String.class, "CONVERT(VARCHAR(10),", x, ",110)");
			} else if (pattern.equalsIgnoreCase(DATE_PATTERN_OTHER)) {
				return buildExpressionSql(String.class, "CONVERT(VARCHAR(10),", x, ",105)");
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
		
		@Override
		protected PreparedStringBuilder buildPreparedString(DbQuery<?> query) {
			PreparedStringBuilder ps = super.buildPreparedString(query);
			if (query.getOffset() != null) {
				ps.appendf(" OFFSET %d ROWS", query.getOffset()); //SQL Server 2012 only?
			}
			return ps;
		}
	}
}
