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

package org.nuclos.server.dblayer.impl.sybase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.dblayer.impl.standard.MetaDataSchemaExtractor;
import org.nuclos.server.dblayer.impl.standard.TransactSqlDbAccess;
import org.nuclos.server.dblayer.statements.DbBatchStatement;
import org.nuclos.server.dblayer.statements.DbDeleteStatement;
import org.nuclos.server.dblayer.statements.DbInsertStatement;
import org.nuclos.server.dblayer.statements.DbPlainStatement;
import org.nuclos.server.dblayer.statements.DbStatementVisitor;
import org.nuclos.server.dblayer.statements.DbStructureChange;
import org.nuclos.server.dblayer.statements.DbUpdateStatement;
import org.nuclos.server.dblayer.structure.DbColumn;
import org.nuclos.server.dblayer.structure.DbColumnType;
import org.nuclos.server.dblayer.structure.DbColumnType.DbGenericType;
import org.nuclos.server.dblayer.structure.DbNullable;
import org.nuclos.server.dblayer.structure.DbSequence;
import org.nuclos.server.dblayer.structure.DbTable;
import org.nuclos.server.dblayer.structure.DbTableType;

public class SybaseDbAccess extends TransactSqlDbAccess {

	static final String SEQUENCE_COMMENT_PREFIX = "Nuclos Sequence";
	static final Pattern SEQUENCE_COMMENT_PATTERN = Pattern.compile("Nuclos Sequence \\((\\w+),\\s*(\\w+)\\)");

	@Override
	protected String getDataType(DbColumnType columnType) {
		if (columnType.getTypeName() != null) {
			return columnType.getTypeName() + columnType.getParametersString();
		} else {
			DbGenericType genericType = columnType.getGenericType();
			switch (genericType) {
			case VARCHAR:
				return String.format("VARCHAR(%d)", columnType.getLength());
			case NUMERIC:
				return String.format("NUMERIC(%d,%d)", columnType.getPrecision(), columnType.getScale());
			case BOOLEAN:
				return "NUMERIC(1,0)";
			case BLOB:
				return "LONG BINARY";
			case CLOB:
				return "LONG VARCHAR";
			case DATE:
			case DATETIME:
				return "DATETIME";
			default:
				throw new DbException("Unsupported column type " + genericType);
			}
		}
	}	

	@Override
	protected MetaDataSchemaExtractor getMetaDataExtractor() {
		return new SybaseMetaDataExtractor();
	}

	@Override
	protected List<String> getSqlForAlterTableColumn(DbColumn column1, DbColumn column2) throws SQLException {
		List<String> lstSQL = new ArrayList<String>();
		lstSQL.add(String.format("ALTER TABLE %s ALTER %s",
			getQualifiedName(column2.getTableName()),
			getColumnSpecForAlterTableColumn(column2, column1)));
		
		if(column2.getDefaultValue() != null && column2.getNullable().equals(DbNullable.NOT_NULL)) {
			String sPlainUpdate = getSqlForUpdateNotNullColumn(column2);

			lstSQL.add(0, sPlainUpdate);
		}
		
		return lstSQL;
	}
	
	

	@Override
	protected List<String> getSqlForAlterTableNotNullColumn(DbColumn column) {
		String columnSpec = String.format("%s %s NOT NULL", column.getColumnName(), getDataType(column.getColumnType()));
		
		return Collections.singletonList(String.format("ALTER TABLE %s ALTER %s",
			getQualifiedName(column.getTableName()), columnSpec));
	}

	@Override
	protected List<String> getSqlForDropColumn(DbColumn column) {
		return Collections.singletonList(String.format("ALTER TABLE %s DROP %s",
			getQualifiedName(column.getTableName()),
			column.getColumnName()));
	}

	@Override
	protected List<String> getSqlForCreateSequence(DbSequence sequence) {
		List<String> sql = new ArrayList<String>();
		// p.x = procedure name / p.y = table name
		Pair<String, String> p = getObjectNamesForSequence(sequence);
		// Create table
		sql.add(String.format(StringUtils.join("\n",
			"CREATE TABLE %1$s ("+
			"SEQID INT IDENTITY PRIMARY KEY," +
			"SEQVAL VARCHAR(1)"+
			")\n" +
			"INSERT INTO %1$s  (seqid,seqval) VALUES (%2$d, \'a\')\n"+
		"DELETE FROM %1$s \n"),
		getQualifiedName(p.y),
		sequence.getStartWith()));

		// Create procedure
		sql.add(String.format(StringUtils.join("\n",
			"CREATE PROCEDURE %1$s AS",
			"BEGIN",
			"  DECLARE @NewSeqValue INT",
			"  SET NOCOUNT ON",
			"  INSERT INTO %2$s (SEQVAL) VALUES ('a')",
			"",
			"  SET @NewSeqValue = @@identity",
			"",
			//TODO Pr\u00fcfen ob ISOLATION_LEVEL 0 das gleiche macht wie READPAST
			"  DELETE FROM %2$s OPTION ( ISOLATION_LEVEL = 0  )",
			"  SELECT @NewSeqValue",
		"END"),
		getQualifiedName(p.x),
		getQualifiedName(p.y)));

		// Add comment 'Nuclos Sequence (sequence, table)' to procedure/table
		// for sequence reification
		String comment = String.format("%s (%s, %s)", SEQUENCE_COMMENT_PREFIX, p.x, p.y);

		sql.add(String.format("COMMENT ON PROCEDURE %s IS \"%s\"",
			getQualifiedName(p.x),
			comment));

		sql.add(String.format("COMMENT ON TABLE %s IS \"%s\"",
			getQualifiedName(p.y),
			comment));
		return sql;
	}   

	@Override
	protected List<String> getSqlForDropSequence(DbSequence sequence) {
		List<String> sql = new ArrayList<String>();
		// p.x = procedure name / p.y = table name
		Pair<String, String> p = getObjectNamesForSequence(sequence);
		sql.add("DROP PROCEDURE " + getQualifiedName(p.x));
		sql.add("DROP TABLE " + getQualifiedName(p.y));
		return sql;
	}
	
	@Override
	protected String getSqlForUpdateNotNullColumn(final DbColumn column) throws SQLException {
		DbUpdateStatement stmt = DbStatementUtils.getDbUpdateStatementWhereFieldIsNull(getQualifiedName(column.getTableName()), column.getColumnName(), column.getDefaultValue());
		final String sUpdate = this.getSqlForUpdate(stmt).get(0).toString();

		String sPlainUpdate = stmt.build().accept(new DbStatementVisitor<String>() {

			@Override
			public String visitBatch(DbBatchStatement batch) {
				// only update in this context
				return null;
			}

			@Override
			public String visitDelete(DbDeleteStatement delete) {
				// only update in this context
				return null;
			}

			@Override
			public String visitInsert(DbInsertStatement insert) {
				// only update in this context
				return null;
			}

			@Override
			public String visitPlain(DbPlainStatement command) {
				// only update in this context
				return null;
			}

			@Override
			public String visitStructureChange(DbStructureChange structureChange) {
				// only update in this context
				return null;
			}

			@Override
			public String visitUpdate(DbUpdateStatement update) {				
				String updateString = new String(sUpdate);
				for(Object obj : update.getColumnValues().values()) {
					if(column.getColumnType().getGenericType().equals(DbGenericType.BOOLEAN)){						
						Boolean bTrue = new Boolean((String)obj);
						updateString = org.apache.commons.lang.StringUtils.replace(updateString, "?", bTrue ? "1" : "0");
					}
					else {
						updateString = org.apache.commons.lang.StringUtils.replace(updateString, "?", "'"+obj.toString()+"'");
					}
				}
				return updateString;
			}
			
			
		});
		return sPlainUpdate;
	}


	class SybaseMetaDataExtractor extends TransactSqlMetaDataExtractor {

		@Override
		protected Collection<DbSequence> getSequences() throws SQLException {
			List<DbSequence> sequences = new ArrayList<DbSequence>();
			List<String> sequenceTables = CollectionUtils.applyFilter(getTableNames(DbTableType.TABLE), 
				new Predicate<String>() {
				@Override public boolean evaluate(String t) { return t.startsWith("T_AD_SEQUENCE"); };
			});
			for (String sequenceTable : sequenceTables) {
				DbTable table = getTableMetaData(sequenceTable);
				String comment = table.getComment();
				if (comment != null) {
					Matcher matcher = SEQUENCE_COMMENT_PATTERN.matcher(comment);
					if (matcher.matches()) {
						String sequenceName = matcher.group(1);
						sequences.add(getSequence(sequenceName, sequenceTable)); 
					}
				}
			}
			return sequences;
		}

		protected DbSequence getSequence(String sequenceName, String tableName) throws SQLException {
			long startWith = 0L;
			try {
				startWith = SybaseDbAccess.this.getNextId(sequenceName);
			} catch (DbException e) {
				log.warn("Could not determine next id for sequence " + sequenceName, e);
			}
			DbSequence sequence = new DbSequence(sequenceName, startWith);
			sequence.setHint(HINT_SEQUENCE_TABLE, tableName);
			return sequence;
		}
	}
}
