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
package org.nuclos.server.report;

import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.database.query.definition.Column;
import org.nuclos.common.database.query.definition.DataType;
import org.nuclos.common.querybuilder.NuclosDatasourceException;
import org.nuclos.common2.StringUtils;

/**
 * WHERE condition parser.
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de </a>
 * @author <a href="mailto:Boris.Sander@novabit.de">Boris Sander </a>
 * @version 01.00.00
 */
public class WhereConditionParser {

	private final Set<String> stOperators = CollectionUtils.asSet("AND", "OR", "NOT");

	private final Set<String> stKeywords = CollectionUtils.asSet("NULL", "USER", "LIKE", "SYSDATE", "BETWEEN");

	private final Map<String, String> mpReplace = new HashMap<String, String>();

	private final Stack<String> stkOperands = new Stack<String>();

	private final Stack<String> stkOperators = new Stack<String>();

	public WhereConditionParser() {
		mpReplace.put("UND", "AND");
		mpReplace.put("ODER", "OR");
		mpReplace.put("WIE", "LIKE");
		mpReplace.put("LEER", "NULL");
		mpReplace.put("GLEICH", "=");
		mpReplace.put("IST", "IS");
		mpReplace.put("NICHT", "NOT");
		mpReplace.put("!", "NOT");
		mpReplace.put("ZWISCHEN", "BETWEEN");
	}

	/**
	 * @param column
	 * @param sCondition
	 * @return parsed where condition
	 * @throws NuclosReportException
	 */
	public String parseCondition(Column column, String sCondition) throws NuclosDatasourceException {
		final String sTable = column.getTable().getAlias();
		final String sColumn = column.getName();
		final String sTableColumn = column.isExpression() ? column.getName() : column.getTable().getAlias() + ".\"" + column.getName() + "\"";
		String result = "";

		final StreamTokenizer st = getTokenizer(sCondition);

		try {
			int token = -1;
			while ((token = st.nextToken()) != StreamTokenizer.TT_EOF) {

				if (st.ttype == StreamTokenizer.TT_WORD) {
					// handle word token
					final String sWordToken = replaceToken(st.sval);
					if (isOracleExpression(sCondition, sWordToken)) {
						return sCondition;
					}
					result = handleWordToken(sTableColumn, result, sWordToken);

				}
				else if (st.ttype == '\'') {
					// handle quoted string token:
					result = handleQuotedString(column, result, st.sval);
				}
				else {
					// handle special character token:
					if (token == '(' || token == ')') {
						result += (char) token;
					} // token ( )

					else
					if (token == '=' && stkOperators.size() > 0 && (stkOperators.peek().equals(">") || stkOperators.peek().equals("<")))
					{
						String sTop = stkOperators.pop();
						sTop += (char) token;
						stkOperators.push(sTop);
					} // token <= >=

					else if (token != ';' && token != ' ') {
						final char[] acToken = {(char) token};
						final String sToken = new String(acToken);
						stkOperators.push(sToken.equals("!") ? "NOT" : sToken);
					}

				} // special char token
			}
		}
		catch (Exception e) {
			throw new NuclosDatasourceException(StringUtils.getParameterizedExceptionMessage("datasource.error.invalid.expression", sTable, sColumn), e);
				//"Fehlerhafter Ausdruck in Bedingung der Datenquelle (Tabelle: " + sTable + ", Spalte: " + sColumn, e);
		}

		if (!stkOperands.isEmpty()) {
			result += stkOperands.pop();
		}
		result = validateWhereCondition(result);
		return result;
	}

	private boolean isOracleExpression(String sCondition, String sWordToken) {
		return !(mpReplace.containsValue(sWordToken.toUpperCase()) ||
				sCondition.startsWith("=") ||
				sCondition.startsWith("!") ||
				sCondition.startsWith("<") ||
				sCondition.startsWith(">") ||
				sCondition.toUpperCase().startsWith("LIKE") ||
				sCondition.toUpperCase().contains("BETWEEN") ||
				sCondition.toUpperCase().contains("ZWISCHEN"));
	}

	/**
	 * @param column
	 * @param sOut
	 * @param sWordToken
	 * @return normalized quoted string
	 */
	private String handleQuotedString(final Column column, String sOut, String sWordToken) {
		final String sTableColumn = column.isExpression() ? column.getName() : column.getTable().getAlias() + ".\"" + column.getName() + "\"";
		final DataType type = column.getType();
		String sValue = "";

		// TODO_AUTOSYNC: Database-specific...
		switch (type) {
		case INTEGER:
		case NUMERIC:
			sValue = sWordToken;
			break;
		case DATE:
		case TIMESTAMP:
			// We use ISO date format, so we use JDBC's date escape syntax
			String[] s = sWordToken.split("\\."); 
			if (s.length == 3) {
				// handle special case 'dd.mm.yyyy' (convert to 'yyyy-mm-dd')
				sWordToken = String.format("%s-%s-%s", s[2], s[1], s[0]); 
			}
			sValue = String.format("{d '%s'}", sWordToken);
			break;
			/** @todo case boolean */
		default:
			sValue = "'" + sWordToken + "'";
		}
		
		
		if (!stkOperators.isEmpty()) {
			final String sOperator = stkOperators.pop();

			if ((sValue.indexOf('%') >= 0 || sValue.indexOf('_') >= 0) &&
					!sValue.startsWith("to_date(") && !stKeywords.contains(sValue) &&
					!sOperator.equals("BETWEEN")) {
				//handle LIKE operator for integer columns
				
				if (type == DataType.INTEGER || type == DataType.NUMERIC) {
					sValue = "'" + sValue + "'";
				}
				sValue = sTableColumn + " LIKE " + sValue;
			}
			else {
				sValue = sTableColumn + " " + sOperator + " " + sValue;
			}
			// clean the operator stack
			final String sOperators = cleanOperatorStack();
			sOut += sOperators + sValue;
		}
		else {
			stkOperands.push(sValue);
		}
		return sOut;
	}

	/**
	 * @param sTableColumn
	 * @param sOut
	 * @param sWordToken
	 * @return
	 */
	private String handleWordToken(final String sTableColumn, String sOut, final String sWordToken) {
		String sValue = "";
		if (stOperators.contains(sWordToken.toUpperCase())) {
			// is an operator
			stkOperators.push(sWordToken.toUpperCase());
		}
		else {
			sValue = sWordToken;
		}

		if (!stkOperators.isEmpty()) {
			final String sOperator = stkOperators.pop();
			if (stKeywords.contains(sValue.toUpperCase())) {
				sValue = sTableColumn + " " + sOperator + " " + sValue.toUpperCase();
			}
			else if (sValue.startsWith("T") && sValue.indexOf(".") > 1 && sValue.indexOf(".") < 3) {
				sValue = sTableColumn + " " + sOperator + " " + sValue;
			}
			else {
				sValue = " " + sOperator + " " + sValue;
			}
			// clean the operator stack
			final String sOperators = cleanOperatorStack();
			sOut += sOperators + sValue;
		}
		else {
			if (sValue.toUpperCase().equals("LIKE") || sValue.toUpperCase().equals("BETWEEN")) {
				stkOperators.push(sValue.toUpperCase());
			}
			else {
				stkOperands.push(sValue);
			}
		}
		return sOut;
	}

	/**
	 * @return
	 */
	private String cleanOperatorStack() {
		String sOperators = "";
		while (!stkOperators.empty()) {
			sOperators = " " + stkOperators.pop() + " " + sOperators;
		}
		return sOperators;
	}

	/**
	 * @param sCondition
	 * @return
	 */
	public static StreamTokenizer getTokenizer(final String sCondition) {
		final Reader reader = new StringReader(sCondition);
		final StreamTokenizer result = new StreamTokenizer(reader);
		result.resetSyntax();
		result.quoteChar('\'');
		result.wordChars('0', '9');
		result.wordChars('A', 'Z');
		result.wordChars('a', 'z');
		result.wordChars('\u00e4', '\u00e4');
		result.wordChars('\u00c4', '\u00c4');
		result.wordChars('\u00f6', '\u00f6');
		result.wordChars('\u00d6', '\u00d6');
		result.wordChars('\u00fc', '\u00fc');
		result.wordChars('\u00dc', '\u00dc');
		result.wordChars('\u00df', '\u00df');
		result.wordChars('.', '.');
		result.wordChars('-', '-');
		result.wordChars('%', '%');
		result.wordChars('*', '*');
		result.wordChars('_', '_');
		result.wordChars('?', '?');
		result.wordChars('$', '$');
		result.wordChars('#', '#');
		result.wordChars('[', ']');
		result.wordChars('"', '"');

		return result;
	}

	/**
	 * do some special replacements in a where condition
	 *
	 * @param sCondition
	 * @return
	 * @todo remove use of replaceAll
	 */
	private static String validateWhereCondition(String sCondition) {
		// handele IS NULL
		if (sCondition.indexOf("NULL") > 0) {
			// @todo; critical use of replaceAll
			if (sCondition.indexOf("=") > 0) {
				sCondition = sCondition.replaceAll("=", "");
			}
			if (sCondition.indexOf("IS") <= 0) {
				sCondition = sCondition.replaceAll("NULL", "IS NULL");
			}
		}

		// handle SYSDATE
		if (sCondition.indexOf("SYSDATE") > 0) {
			sCondition = sCondition.replaceAll("SYSDATE", "TO_DATE(TO_CHAR(SYSDATE, 'DD.MM.YYYY'), 'DD.MM.YYYY')");
		}

		return sCondition;
	}

	private String replaceToken(String sToken) {
		final String sUpperToken = sToken.toUpperCase();
		if (mpReplace.containsKey(sUpperToken)) {
			return mpReplace.get(sUpperToken);
		}
		return sToken;
	}

}	// class WhereConditionParser
