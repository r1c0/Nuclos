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
package org.nuclos.common.querybuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.report.ejb3.DatasourceFacadeRemote;

public class DatasourceUtils {
	
	private static final Logger LOG = Logger.getLogger(DatasourceUtils.class);
	
	private DatasourceUtils() {
	}
	
	public static void validateDynEntityName(String sName) throws CommonValidationException {
		final int iMaxNameLength = 30; //if you change this value, change the exception text <datasource.validation.dynamic.entity.name.1> too.
		if ((sName.length() + MasterDataMetaVO.DYNAMIC_ENTITY_VIEW_PREFIX.length()) > iMaxNameLength) {
			throw new CommonValidationException("datasource.validation.dynamic.entity.name.1");
		}
		
		if (sName.contains(" ")) {
			throw new CommonValidationException("datasource.validation.dynamic.entity.name.2");
		}
		
		if (!sName.matches("[0-9a-zA-Z_]*")) {
			throw new CommonValidationException("datasource.validation.dynamic.entity.name.3");
		}
	}
	
	public static void validateDynEntitySQL(String sql) throws CommonValidationException {
		if (StringUtils.looksEmpty(sql)) {
			throw new CommonValidationException("datasource.validation.dynamic.entity.sql.1");
		}
		
		boolean foundIntid1 = false;
		boolean foundIntid2 = false;
		
		for (String column : getColumns(sql)) {
			if (column.toUpperCase().equals("INTID"))
				foundIntid1 = true;
			if (column.toUpperCase().equals("INTID_T_UD_GENERICOBJECT"))
				foundIntid2 = true;
		}
		
		if (!foundIntid1) {
			throw new CommonValidationException("datasource.validation.dynamic.entity.sql.2");
		}
		if (!foundIntid2) {
			throw new CommonValidationException("datasource.validation.dynamic.entity.sql.3");
		}
		if (!getParametersFromString(sql).isEmpty()) {
			throw new CommonValidationException("datasource.validation.dynamic.entity.sql.4");
		}
		
		try {
			ServiceLocator.getInstance().getFacade(DatasourceFacadeRemote.class).validateSql(sql);
		} catch (Exception e) {
			throw new CommonValidationException("datasource.validation.dynamic.entity.sql.5");
		}
		
	}
	
	public static void validateRecordGrantSQL(String entity, String sql) throws CommonValidationException {
		if (entity != null && !StringUtils.looksEmpty(sql)) {
			
			boolean foundIntid = false;
			boolean foundMoreIntid = false;
			for (String column : getColumns(sql)) {
				if (column.equals("\"intid\"")) {
					if (foundIntid)
						foundMoreIntid = true;
					foundIntid = true;
				}
			}
			
			if (!foundIntid) {
				throw new CommonValidationException("datasource.validation.recordgrant.sql.1");
			}
			if (foundMoreIntid) {
				throw new CommonValidationException("datasource.validation.recordgrant.sql.2");
			}
		}
	}
	
	/** @TODO Find a better way: Don't extract them by parsing the plain SQL (may be user-defined and using DB-specific extensions). */
	public static List<String> getColumnsWithoutQuotes(final String sql) {
		List<String> result = new ArrayList<String>();
		
		for (String column : getColumns(sql)) {
			if (column.length()>2 && column.charAt(0)=='"' && column.charAt(column.length()-1)=='"') {
				result.add(column.substring(1, column.length()-1));
			} else {
				result.add(column);
			}
		}
		
		return result;
	}
	
	/** @TODO find a better way: Don't extract them by parsing the plain SQL (may be user-defined and using DB-specific extensions). */
	public static List<String> getColumns(final String sql) {
		final List<String> result = new ArrayList<String>();
		
		/* find columns in sql */
		final String upperSQL = sql.toUpperCase();
		final int indexOfSelect = upperSQL.indexOf("SELECT");
		final int indexOfFrom = upperSQL.indexOf("FROM");
		if(indexOfSelect == -1 || indexOfFrom == -1 || sql.length() <= (indexOfSelect+6)){
			return result;
		}
		/* complete columns from sql */
		final String columns = sql.substring(indexOfSelect+6,indexOfFrom);
		
		/* switch between quoting and not quoting beginnig with not quoting */
		boolean quoting = false;
		
		/* column cursor */
		StringBuffer column = new StringBuffer();
		
		for (int i = 0; i < columns.length(); i++) {
			final char c1 = columns.charAt(i);
			final boolean isLastChar = i == columns.length()-1;
			if (c1 == '"') {
				/* switch between quoting and not quoting beginnig with not quoting */
				quoting = !quoting;
			}
			if ((c1 == ',' || isLastChar )&& !quoting) {
				/* column ends here */
				
				/* add last char to column if it is the last one 
				 * (this is the last 'for'-loop) */
				if (isLastChar)
					column.append(c1);
				
				column = new StringBuffer(column.toString().trim());
				
				/* find "as" in unquoted column chars*/
				boolean aFound = false;
				final Vector<Integer> as = new Vector<Integer>(1);
				/* find unquoted blanks... */
				boolean columnQuoting = false;
				final Vector<Integer> blanks = new Vector<Integer>(2);
				for (int j = 0; j < column.length(); j++) {
					final char c2 = column.charAt(j);
					if (c2 == '"') {
						columnQuoting = !columnQuoting;
						aFound = false;
					} else {
						if (c2 == ' ' && !columnQuoting) {
							blanks.add(j);
						}
						if ((c2 == 's' || c2 == 'S') && !columnQuoting && aFound) {
							/* "as" found in unquoted column chars 
							 * if it stands alone this indicates the alias:
							 * [ as ]
							 * ["as ]
							 * [ as"]
							 * ["as"] */
							if (j-2>=0 && j+1<column.length()) {
								final boolean starts = column.charAt(j-2)==' '||column.charAt(j-2)=='"';
								final boolean ends   = column.charAt(j+1)==' '||column.charAt(j+1)=='"';
								
								if (starts && ends) {
									as.add(j-1);
								}
							}
						}
						if ((c2 == 'a' || c2 == 'A') && !columnQuoting) {
							aFound = true;
						} else {
							aFound = false;
						}
					}
				}
				
				if (!blanks.isEmpty() || !as.isEmpty()) {
					/* column has alias*/
					
					final int indexOfLastBlank = blanks.isEmpty()?-1:blanks.lastElement();
					final int indexOfLastAs = as.isEmpty()?-1:as.lastElement();
					final String alias = column.substring(indexOfLastBlank>indexOfLastAs?
																		indexOfLastBlank+1:
																		indexOfLastAs+2);
					result.add(alias);
				} else {
					/* column has no alias, trim is allowed */
					
					result.add(column.toString().trim());
				}
				
				column = new StringBuffer();
			} else {
				column.append(c1);
			}
		}
		
		return result;
	}
	
	private static void test() {
		int i = 1;
		for (String column : getColumns(
			"SELECT "+ 
			"T1.\"intid\" \"intid\"    ,    " +
			"T1.\"ku_kundenname\" || T1.\"ku_kundenname\"as\"Tages,summen   \"" +
			"FROM " + 
			"NUCMAIK1.v_ud_go_KUNDE T1")) {
			LOG.debug("Column " + i++ + ": [" + column + "]");
		}
	}
	
	public static void main(String[] args) {
		test();
	}
	
	public static Collection<String> getParametersFromString(String sql) {
		final List<String> result = new ArrayList<String>();
		final Reader reader = new StringReader(sql);
		final StreamTokenizer tokenizer = new StreamTokenizer(reader);
		tokenizer.resetSyntax();
		// tokenizer.quoteChar('\'');
		tokenizer.wordChars('0', '9');
		tokenizer.wordChars('A', 'Z');
		tokenizer.wordChars('a', 'z');
		tokenizer.wordChars('.', '.');
		tokenizer.wordChars('-', '-');
		// tokenizer.wordChars('%', '%');
		tokenizer.wordChars('$', '$');
	
		try {
			while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
				if (tokenizer.ttype == StreamTokenizer.TT_WORD /*
	                     * ||
	                     * tokenizer.ttype ==
	                     * '\''
	                     */)
				{
					final String sParam = tokenizer.sval;
					if (sParam != null && sParam.startsWith("$")) {
						result.add(sParam.replaceFirst("\\x24", ""));
					}
				}
			}
		} catch (IOException ex) {
			throw new NuclosFatalException(ex.getMessage(), ex);
		}
		return result;
	}
}
