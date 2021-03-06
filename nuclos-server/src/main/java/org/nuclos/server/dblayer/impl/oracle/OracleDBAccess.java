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
package org.nuclos.server.dblayer.impl.oracle;

import static org.nuclos.common2.StringUtils.join;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.DbNotNullableException;
import org.nuclos.server.dblayer.DbNotUniqueException;
import org.nuclos.server.dblayer.DbReferentialIntegrityException;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.dblayer.DbType;
import org.nuclos.server.dblayer.IBatch;
import org.nuclos.server.dblayer.impl.BatchImpl;
import org.nuclos.server.dblayer.impl.SQLUtils2;
import org.nuclos.server.dblayer.impl.SqlSequentialUnit;
import org.nuclos.server.dblayer.impl.standard.MetaDataSchemaExtractor;
import org.nuclos.server.dblayer.impl.standard.StandardSqlDBAccess;
import org.nuclos.server.dblayer.impl.util.PreparedString;
import org.nuclos.server.dblayer.impl.util.PreparedStringBuilder;
import org.nuclos.server.dblayer.incubator.DbExecutor.ResultSetRunner;
import org.nuclos.server.dblayer.query.DbExpression;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.dblayer.statements.AbstractDbStatementVisitor;
import org.nuclos.server.dblayer.statements.DbInsertStatement;
import org.nuclos.server.dblayer.statements.DbUpdateStatement;
import org.nuclos.server.dblayer.structure.DbArtifact;
import org.nuclos.server.dblayer.structure.DbColumn;
import org.nuclos.server.dblayer.structure.DbColumnType;
import org.nuclos.server.dblayer.structure.DbColumnType.DbGenericType;
import org.nuclos.server.dblayer.structure.DbConstraint.DbPrimaryKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbUniqueConstraint;
import org.nuclos.server.dblayer.structure.DbIndex;
import org.nuclos.server.dblayer.structure.DbNullable;
import org.nuclos.server.dblayer.structure.DbSequence;
import org.nuclos.server.dblayer.structure.DbSimpleView;
import org.nuclos.server.dblayer.structure.DbTable;

public class OracleDBAccess extends StandardSqlDBAccess {

    private static final Logger LOG = Logger.getLogger(OracleDBAccess.class);

	public OracleDBAccess() {
	}

	@Override
	public void init(DbType type, DataSource dataSource, Map<String, String> config) {
		this.executor = new OracleDBExecutor(dataSource, config.get(USERNAME), config.get(PASSWORD));
		super.init(type, dataSource, config);
	}

	@Override
	public String generateName(String base, String...affixes) {
		return generateName(30, base, affixes);
	}

	@Override
	public DbQueryBuilder getQueryBuilder() {
		return new OracleQueryBuilder(this);
	}

	@Override
	protected String getDataType(DbColumnType columnType) throws DbException {
		if (columnType.getTypeName() != null) {
			return columnType.getTypeName() + columnType.getParametersString();
		} else {
			DbGenericType genericType = columnType.getGenericType();
			switch (genericType) {
			case VARCHAR:
				return String.format("VARCHAR2(%d)", columnType.getLength());
			case NUMERIC:
				return String.format("NUMBER(%d,%d)", columnType.getPrecision(), columnType.getScale());
			case BOOLEAN:
				return "NUMBER(1,0)";
			case BLOB:
				return "BLOB";
			case CLOB:
				return "CLOB";
			case DATE:
			case DATETIME:
				return "DATE";
			default:
				throw new DbException("Unsupported column type " + genericType);
			}
		}
	}

	@Override
	protected IBatch getSqlForAlterTableColumn(DbColumn column1, DbColumn column2) throws SQLException {
		final PreparedString ps = PreparedString.format("ALTER TABLE %s MODIFY (%s)",
			getQualifiedName(column2.getTableName()),
			getColumnSpecForAlterTableColumn(column2, column1));
		final IBatch result;
		if(column2.getDefaultValue() != null && column2.getNullable().equals(DbNullable.NOT_NULL)) {
			result = getSqlForUpdateNotNullColumn(column2);
			result.append(new SqlSequentialUnit(ps));
		}
		else {
			result = BatchImpl.simpleBatch(ps);
		}
		return result;
	}

	@Override
	protected IBatch getSqlForAlterTableNotNullColumn(final DbColumn column) {
		String columnSpec = String.format("%s %s NOT NULL", column.getColumnName(), getDataType(column.getColumnType()));

		return BatchImpl.simpleBatch(PreparedString.format("ALTER TABLE %s MODIFY (%s)",
			getQualifiedName(column.getTableName()), columnSpec));
	}

	@Override
	protected IBatch getSqlForUpdateNotNullColumn(final DbColumn column) throws SQLException {
		final DbUpdateStatement stmt = DbStatementUtils.getDbUpdateStatementWhereFieldIsNull(getQualifiedName(column.getTableName()), column.getColumnName(), column.getDefaultValue());

		final PreparedString sPlainUpdate = stmt.build().accept(new AbstractDbStatementVisitor<PreparedString>() {
			@Override
			public PreparedString visitUpdate(DbUpdateStatement update) {
				String sUpdate = getPreparedStringForUpdate(stmt).toString();
				for(Object obj : update.getColumnValues().values()) {
					if(column.getColumnType().getGenericType().equals(DbGenericType.DATE) || column.getColumnType().getGenericType().equals(DbGenericType.DATETIME)){
						String dateSql = new String("to_date('" + obj + "', 'yyyy.mm.dd')");
						sUpdate = org.apache.commons.lang.StringUtils.replace(sUpdate, "?", dateSql);
					}
					else if(column.getColumnType().getGenericType().equals(DbGenericType.BOOLEAN)){
						Boolean bTrue = new Boolean((String)obj);
						sUpdate = org.apache.commons.lang.StringUtils.replace(sUpdate, "?", bTrue ? "1" : "0");
					}
					else if(column.getColumnType().getGenericType().equals(DbGenericType.NUMERIC)){
						String numberSql = new String("to_number(" + obj + ")");
						sUpdate = org.apache.commons.lang.StringUtils.replace(sUpdate, "?", numberSql);
					}
					else {
						sUpdate = org.apache.commons.lang.StringUtils.replace(sUpdate, "?", "'"+obj.toString()+"'");
					}
				}
				return new PreparedString(sUpdate);
			}
		});
		return BatchImpl.simpleBatch(sPlainUpdate);
	}

	@Override
	protected IBatch getSqlForAlterSequence(DbSequence sequence1, DbSequence sequence2) {
		long restartWith = Math.max(sequence1.getStartWith(), sequence2.getStartWith());
		// Oracle doesn't support sequence restarting, so drop and then (re-)create it
		IBatch sql = getSqlForDropSequence(sequence1);
		sql.append(getSqlForCreateSequence(new DbSequence(sequence2.getSequenceName(), restartWith)));
		return sql;
	};

	@Override
	protected IBatch getSqlForCreateIndex(DbIndex index) {
		return BatchImpl.simpleBatch(PreparedString.format("CREATE INDEX %s ON %s (%s) %s",
			getQualifiedName(index.getIndexName()),
			getQualifiedName(index.getTableName()),
			join(",", index.getColumnNames()),
			getTablespaceSuffix(index)));
	}
	
	@Override
    protected IBatch getSqlForInsert(DbInsertStatement insertStmt) {
    	if (insertStmt.getTableName().equals("T_MD_LOCALERESOURCE")) // just a hack. but preventing strtext of t_md_localeresource being not empty does not change already stored values.
    	{
    		Map<String, Object> values = new HashMap<String, Object>(insertStmt.getColumnValues());
    		for (Entry<String, Object> entry : values.entrySet()) {
				if (entry.getValue().equals("")) {
					insertStmt.getColumnValues().put(entry.getKey(), " ");
				}
			}
    	}
    	String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
            insertStmt.getTableName(),
            StringUtils.join(", ", insertStmt.getColumnValues().keySet()),
            StringUtils.join(", ", CollectionUtils.replicate("?", insertStmt.getColumnValues().size())));
        Object[] params = insertStmt.getColumnValues().values().toArray();
        return BatchImpl.simpleBatch(new PreparedString(sql, params));
    }

	@Override
	protected IBatch getSqlForCreateSequence(DbSequence sequence) {
		return BatchImpl.simpleBatch(PreparedString.format(
			"CREATE SEQUENCE %s INCREMENT BY 1 MINVALUE 1 MAXVALUE 999999999 START WITH %d NOCYCLE NOORDER NOCACHE",
			getQualifiedName(sequence.getSequenceName()), sequence.getStartWith()));
	}

	@Override
	protected IBatch getSqlForDropTable(DbTable table) {
		if (!table.isVirtual()) {
			return BatchImpl.simpleBatch(PreparedString.format("DROP TABLE %s PURGE",
				getQualifiedName(table.getTableName())));
		}
		else {
			return null;
		}
	}

	@Override
	protected IBatch getSqlForAlterSimpleView(DbSimpleView oldView, DbSimpleView newView) {
		if (!oldView.getViewName().equals(newView.getViewName())) {
			throw new IllegalArgumentException();
		}
		return BatchImpl.simpleBatch(_getSqlForCreateSimpleView("CREATE OR REPLACE VIEW", newView, ""));
	}

	@Override
	protected IBatch getSqlForDropPrimaryKey(DbPrimaryKeyConstraint constraint) {
		return BatchImpl.simpleBatch(PreparedString.format("ALTER TABLE %s DROP CONSTRAINT %s CASCADE DROP INDEX",
			getQualifiedName(constraint.getTableName()),
			constraint.getConstraintName()));
	}

	@Override
	protected IBatch getSqlForDropUniqueConstraint(DbUniqueConstraint constraint) {
		return BatchImpl.simpleBatch(PreparedString.format("ALTER TABLE %s DROP CONSTRAINT %s CASCADE DROP INDEX",
			getQualifiedName(constraint.getTableName()),
			constraint.getConstraintName()));
	}

	@Override
	protected String getTablespaceSuffix(DbArtifact artifact) {
		String tablespace = getConfigParameter(TABLESPACE, null);
		if (tablespace != null) {
			if (artifact instanceof DbTable) {
				return String.format(
					"\nTABLESPACE %s\n" +
					"PCTFREE 10 PCTUSED 85 INITRANS 2 MAXTRANS 255\n" +
					"STORAGE (INITIAL 128 MINEXTENTS 1 MAXEXTENTS UNLIMITED)",
					tablespace);
			} else if (LangUtils.isInstanceOf(artifact, DbIndex.class, DbPrimaryKeyConstraint.class, DbUniqueConstraint.class)) {
				return String.format(
					"\nTABLESPACE %s\n" +
					"PCTFREE 10 INITRANS 2 MAXTRANS 255\n" +
					"STORAGE (INITIAL 128K MINEXTENTS 1)",
					getConfigParameter(TABLESPACE_INDEX, tablespace));
			} else if (artifact instanceof DbUniqueConstraint) {

			}
		}
		return "";
	}

	static class OracleQueryBuilder extends StandardQueryBuilder {

		public OracleQueryBuilder(StandardSqlDBAccess dbAccess) {
			super(dbAccess);
		}

		@Override
		public DbExpression<Date> currentDate() {
			return buildExpressionSql(java.util.Date.class, PreparedStringBuilder.valueOf("TRUNC(SYSDATE)"));
		}

		@Override
		public DbExpression<Date> convertInternalTimestampToDate(DbExpression<InternalTimestamp> x) {
			return buildExpressionSql(Date.class, "TRUNC(", x, ")");
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

	static class OracleMetaData extends MetaDataSchemaExtractor {

		@Override
		protected void initMetaData() throws SQLException {
			try {
				Class<?> clazz = Class.forName("oracle.jdbc.OracleConnection");
				Object oracleConnection = SQLUtils2.unwrap(connection, clazz);

				Method m = clazz.getMethod("setRemarksReporting", boolean.class);
				m.invoke(oracleConnection, true);
			}
			catch (ClassNotFoundException ex) {
				throw new CommonFatalException("oracle.jdbc.OracleConnection not found in classpath.", ex);
			}
			catch (NoSuchMethodException ex) {
				throw new CommonFatalException("Error in reflection call.", ex);
			}
			catch (InvocationTargetException ex) {
				throw new SQLException(ex.getTargetException());
			}
			catch (Exception ex) {
				throw new SQLException(ex);
			}
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
			Map<String, String> result = new HashMap<String, String>();

			Statement stmt = connection.createStatement();
			try {
				ResultSet rs = stmt.executeQuery("select * from (select * from V$NLS_PARAMETERS union select NAME as PARAMETER, VALUE as VALUE from v$spparameter) order by parameter");
				try {
					while (rs.next()) {
						result.put(rs.getString(1), rs.getString(2));
					}
				} finally {
					rs.close();
				}
			} finally {
				stmt.close();
			}

			return result;
		}

		@Override
		protected DbGenericType getDbGenericType(int sqlType, String typeName) {
			return OracleDBAccess.getDbGenericType(sqlType, typeName);
		}
	}

	@Override
	protected MetaDataSchemaExtractor getMetaDataExtractor() {
		return new OracleMetaData();
	}

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
	public boolean checkSyntax(String sql) {
		try {
			return super.checkSyntax(sql);
		} catch (DbException e) {
			if (e.getErrorCode() == 1013) {
				// Timeout, so we can't know if the sql succeeds...
				return false;
			}
			throw e;
		}
	}

	@Override
	protected String makeIdent(String name) {
		int len = name.length();
		if (len == 0)
			throw new IllegalArgumentException();
		if (name.charAt(0) == '"' && name.charAt(len-1) == '"') {
			return name.substring(1, len-1);
		} else {
			return name;
		}
	}

	@Override
	protected DbException wrapSQLException(Long id, String message, SQLException ex) {
		try {
			if ("23000".equals(ex.getSQLState())) {
				Matcher matcher;
				switch (ex.getErrorCode()) {
				case 1: // unique constraint
					matcher = EXCEPTION_IDENTS_2.matcher(ex.getMessage());
					if (matcher.find()) {
						return new DbNotUniqueException(id,
							makeIdent(matcher.group(1)), makeIdent(matcher.group(2)), ex);
					}
					break;
				case 1400: // insert null
					matcher = EXCEPTION_IDENTS_3.matcher(ex.getMessage());
					if (matcher.find()) {
						return new DbNotNullableException(id,
							makeIdent(matcher.group(1)), makeIdent(matcher.group(2)), makeIdent(matcher.group(3)), ex);
					}
					break;
				case 2292: // delete referential entry
					matcher = EXCEPTION_IDENTS_2.matcher(ex.getMessage());
					if (matcher.find()) {
						return new DbReferentialIntegrityException(id,
							makeIdent(matcher.group(1)), makeIdent(matcher.group(2)), ex);
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

	private static String IDENT_REGEX = "(\\w+|\\\"[^\\\"]+\\\")";
	private static Pattern EXCEPTION_IDENTS_2 = Pattern.compile("\\(" + IDENT_REGEX + "\\." + IDENT_REGEX + "\\)");
	private static Pattern EXCEPTION_IDENTS_3 = Pattern.compile("\\(" + IDENT_REGEX + "\\." + IDENT_REGEX + "\\." + IDENT_REGEX + "\\)");

	//
	// Old code from the TransferFacadeBean, not nice but working
	//

	@Override
	public void runWithDisabledChecksAndTriggers(Runnable runnable) throws SQLException {
		// Deactive constraints & triggers
		List<String> triggers = disableTriggers();
		List<Constraint> constraints = disableConstraints();
		try {
			runnable.run();
		} finally {
			// Re-activate constraints & triggers
			enableConstraints(constraints);
			enableTriggers(triggers);
		}
	}

	/*
	 * Constraints aktivieren/deaktivieren - Caveat: in der Liste haben zuerst
	 * die PK-, dann die FK-Constraints. Deaktiviert wird in der umgekehrten Reihenfolge.
	 */

	private static class Constraint {
		String owner, tablename, constraintname;
	}

	private static final String ORACLE_ENABLED_CONSTRAINTS =
		"SELECT c.owner, c.table_name, c.constraint_name " +
		"FROM user_constraints c, user_tables t " +
		"WHERE c.table_name = t.table_name " +
		"  AND c.status = 'ENABLED' AND c.constraint_type in ('P','R') " +
		"ORDER by c.constraint_type";

	private static final String ORACLE_ALTER_CONSTRAINT =
		"dbms_utility.exec_ddl_statement('alter table %s.%s %s constraint %s');\n";

	private List<Constraint> disableConstraints() throws SQLException {
		LinkedList<Constraint> constraints = getEnabledConstraints();

		if (constraints.size() == 0) {
			return constraints;
		}

		String cmd = getAlterConstraintsCmd(constraints.descendingIterator(), false);
		executor.executeUpdate(cmd);
		return constraints;
	}

	private void enableConstraints(List<Constraint> constraints) throws SQLException {
		if (constraints.size() == 0) {
			return;
		}

		String cmd = getAlterConstraintsCmd(constraints.iterator(), true);
		executor.executeUpdate(cmd);
	}

	private static String getAlterConstraintsCmd(Iterator<Constraint> it, boolean enable)
	{
		StringBuilder cmds = new StringBuilder();
		cmds.append("BEGIN\n");
		String op = (enable ? "enable" : "disable");
		while (it.hasNext()) {
			Constraint c = it.next();
			cmds.append(String.format(ORACLE_ALTER_CONSTRAINT,
				c.owner, c.tablename, op, c.constraintname));
		}
		cmds.append("END;\n");
		return cmds.toString();
	}

	private LinkedList<Constraint> getEnabledConstraints() throws SQLException {
		return executor.executeQuery(ORACLE_ENABLED_CONSTRAINTS,
			new ResultSetRunner<LinkedList<Constraint>>() {
			@Override
			public LinkedList<Constraint> perform(ResultSet rs) throws SQLException {
				LinkedList<Constraint> constraints = new LinkedList<Constraint>();
				while (rs.next()) {
					Constraint c = new Constraint();
					c.owner = rs.getString(1);
					c.tablename = rs.getString(2);
					c.constraintname = rs.getString(3);
					constraints.add(c);
				}
				return constraints;
			}
		});
	}


	// TODO: ist das ausreichend?
	private static final String ORACLE_ENABLED_TRIGGERS =
		"select trigger_name from user_triggers where status='ENABLED'";

	private static final String ORACLE_ALTER_TRIGGER =
		"dbms_utility.exec_ddl_statement('alter trigger %s %s');\n";


	private List<String> disableTriggers() throws SQLException {
		List<String> triggers = getEnabledTriggers();

		if (triggers.size() == 0) {
			return triggers;
		}

		String cmd = getAlterTriggersCmd(triggers.iterator(), false);
		executor.executeUpdate(cmd);
		return triggers;
	}

	private void enableTriggers(List<String> triggers) throws SQLException {
		if (triggers.size() == 0) {
			return;
		}

		String cmd = getAlterTriggersCmd(triggers.iterator(), true);
		executor.executeUpdate(cmd);
	}

	private static String getAlterTriggersCmd(Iterator<String> it, boolean enable)
	{
		StringBuilder cmds = new StringBuilder();
		cmds.append("BEGIN\n");
		String op = (enable ? "enable" : "disable");
		while (it.hasNext())
			cmds.append(String.format(ORACLE_ALTER_TRIGGER, it.next(), op));
		cmds.append("END;\n");
		return cmds.toString();
	}

	private List<String> getEnabledTriggers() throws SQLException {
		return executor.executeQuery(ORACLE_ENABLED_TRIGGERS,
			new ResultSetRunner<List<String>>() {
			@Override
			public List<String> perform(ResultSet rs) throws SQLException {
				List<String> triggers = new LinkedList<String>();
				while (rs.next())
					triggers.add(rs.getString(1));
				return triggers;
			}
		});
	}

	@Override
	public String getSqlForCast(String x, DbColumnType columnType) {
		// Oracle's CAST does not support LOB's, but this should not be necessary
		if (DbGenericType.BLOB.equals(columnType.getGenericType()) || DbGenericType.CLOB.equals(columnType.getGenericType())) {
			return x;
		}
		return super.getSqlForCast(x, columnType);
	}
}
