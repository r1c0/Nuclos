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
package org.nuclos.common.database.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.database.query.definition.Column;
import org.nuclos.common.database.query.definition.Table;
import org.nuclos.common.database.query.statement.AndCondition;
import org.nuclos.common.database.query.statement.CloseBracketCondition;
import org.nuclos.common.database.query.statement.Condition;
import org.nuclos.common.database.query.statement.OpenBracketCondition;
import org.nuclos.common.database.query.statement.Operand;
import org.nuclos.common.database.query.statement.OrCondition;
import org.nuclos.common.database.query.statement.Predicate;
import org.nuclos.common.querybuilder.NuclosDatasourceException;
import org.nuclos.server.report.ejb3.DatasourceFacadeRemote;

/**
 * Container for assembled SELECT statements. <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author <a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00
 */
public class SelectQuery {

	private final List<Column> lstSelect = new ArrayList<Column>();
	private final List<Table> lstFrom = new ArrayList<Table>();
	private final List<Condition> lstWhere = new ArrayList<Condition>();
	private final List<OrderBy> lstOrderBy = new ArrayList<OrderBy>();
	private final List<GroupBy> lstGroupBy = new ArrayList<GroupBy>();
	private Map<Column, String> mpGroupBy = CollectionUtils.newHashMap();

	private final Map<String, String> mpTranslateGroupBy = CollectionUtils.newHashMap();

	private static class OrderBy {
		private Column column;
		private boolean bAscending;

		OrderBy(Column column, boolean bAscending) {
			this.bAscending = bAscending;
			this.column = column;
		}

		public boolean isAscending() {
			return bAscending;
		}

		public Column getColumn() {
			return column;
		}
	}

	private static class GroupBy {
		private Column column;
		private String sGroupType;

		GroupBy(Column column, String sGroupType) {
			this.column = column;
			this.sGroupType = sGroupType;
		}

		public Column getColumn() {
			return column;
		}

		public String getGroupType() {
			return sGroupType;
		}
	}

	public SelectQuery(String sUserName) {
		mpTranslateGroupBy.put("Gruppe", "GROUP BY");
		mpTranslateGroupBy.put("Minimum", "MIN");
		mpTranslateGroupBy.put("Maximum", "MAX");
		mpTranslateGroupBy.put("Summe", "SUM");
		mpTranslateGroupBy.put("Mittelwert", "AVG");
		mpTranslateGroupBy.put("Standardabweichung", "STD");
		mpTranslateGroupBy.put("Varianz", "VAR");
		mpTranslateGroupBy.put("Anzahl", "COUNT");
	}

	public void addToFromClause(Table table) {
		if (!lstFrom.contains(table)) {
			lstFrom.add(table);
		}
	}

	public void addToWhereClause(Condition condition) {
		final Predicate predicate = condition.getPredicate();
		if (predicate != null && predicate.getOperand().getObject() instanceof Column) {
			final Column column = (Column) predicate.getOperand().getObject();
			this.addToFromClause(column.getTable());
		}
		lstWhere.add(condition);
	}

	public void addToOrderByClause(Column column, boolean bAscending) {
		this.addToFromClause(column.getTable());
		lstOrderBy.add(new OrderBy(column, bAscending));
	}

	public void addToSelectClause(Column column, String alias) {
		column.setAlias(alias);
		this.addToFromClause(column.getTable());
		lstSelect.add(column);
	}

	public String getSelectStatement(boolean isDynamicEntity) throws NuclosDatasourceException {
		final StringBuffer sb = new StringBuffer("SELECT \n");
		if (lstSelect.isEmpty()) {
			return "";
		}

		for (GroupBy groupBy : lstGroupBy) {
			mpGroupBy.put(groupBy.getColumn(), translateGroupBy(groupBy.getGroupType()));
		}

		appendSelectClause(sb, isDynamicEntity);

		appendFromClause(sb);

		appendWhereClause(sb);

		appendGroupByClause(sb);

		appendOrderByClause(sb);

		return sb.toString();
	}

	private void appendSelectClause(StringBuffer sb, boolean isDynamicEntity) {
		for (Column column : lstSelect) {
			final Table table = column.getTable();
			if (table.getAlias() == null) {
				System.err.println("Processing error: table " + table.getName() + " is referenced in SELECT clause, but does not have alias");
			}
			else if ((!lstGroupBy.isEmpty() || !mpGroupBy.isEmpty()) && !column.isHidden() && !column.isExpression()) {
				final String sGroupBy = mpGroupBy.get(column);
				if (sGroupBy != null && !sGroupBy.equals("GROUP BY")) {
					sb.append("\t" + sGroupBy + "(" + table.getAlias());
					sb.append('.');
					sb.append("\"" + column.getName() + "\")" + getAlias(column));
					sb.append(",\n");
				}
				else if (sGroupBy != null) {
					sb.append("\t" + table.getAlias());
					sb.append('.');
					sb.append("\"" + column.getName() + "\"" + getAlias(column));
					sb.append(",\n");
				}
				else {
					sb.append("\t" + "MAX" + "(" + table.getAlias());
					sb.append('.');
					sb.append("\"" + column.getName() + "\")" + getAlias(column));
					sb.append(",\n");
				}
			}
			else if (column.isHidden()) {
				sb.append("null ").append(column.getAlias() != null && column.getAlias().length() > 0 ? (" \"" + column.getAlias() + "\"") : (""));
				sb.append(",\n");
			}
			else {
				sb.append("\t");
				
				boolean bQuoteColumnName = true;
				if (("INTID".equalsIgnoreCase(column.getName()) || "INTID_T_UD_GENERICOBJECT".equalsIgnoreCase(column.getName()))
						&&	isDynamicEntity && table.isQuery()) {
					bQuoteColumnName = false;
				} 
				
				if (!column.isExpression()) {
					sb.append(table.getAlias());
					sb.append(".");
					sb.append(bQuoteColumnName?"\"":"");
				}
				sb.append(column.getName());
				if (!column.isExpression()) {
					sb.append(bQuoteColumnName?"\"":"");
				}
				if (column.getAlias() != null && column.getAlias().length() > 0) {
					boolean bQuoteAlias = true;
					if (("INTID".equalsIgnoreCase(column.getAlias()) || "INTID_T_UD_GENERICOBJECT".equalsIgnoreCase(column.getAlias()))
							&& isDynamicEntity) {
						bQuoteAlias = false;
					} 
					sb.append(bQuoteAlias?" \"":" ");
					sb.append(column.getAlias());
					sb.append(bQuoteAlias?"\"":"");
				}
				sb.append(",\n");
			}
		}
		if (sb.charAt(sb.length() - 2) == ',') {
			sb.deleteCharAt(sb.length() - 2);
		}
	}

	private void appendFromClause(StringBuffer sb) throws NuclosDatasourceException {
		sb.append("FROM \n");
		if (lstFrom.isEmpty()) {
			// If there is no entry in lstFrom, then we cannot
			// create SQL code
			sb.deleteCharAt(0);
		}
		else {
			final Iterator<Table> iterFrom = lstFrom.iterator();
			while (iterFrom.hasNext()) {
				final Table table = iterFrom.next();
				if (table.getAlias() == null) {
					System.err.println("Processing error: table " + table.getName() + " is referenced in FROM clause, but does not have alias");
				}
				else {
					if (table.isQuery()) {
						sb.append("\t(");

						final DatasourceFacadeRemote dataSourceFacade = ServiceLocator.getInstance().getFacade(DatasourceFacadeRemote.class);
						String sQuery = dataSourceFacade.createSQLOriginalParameter(table.getDatasourceXML());
						sQuery = sQuery.replace('\t', ' ');
						sb.append(sQuery.replace('\n', ' '));
						sb.append(") ");
						sb.append(table.getAlias());
					}
					else if (table.isJoin()) {
						sb.append("\t").append(table.getName());
					}
					else {
						sb.append("\t");
						sb.append(table.getName());
						sb.append(' ');
						sb.append(table.getAlias());
					}
					sb.append(iterFrom.hasNext() ? ",\n" : "\n");
				}
			}
			if (sb.charAt(sb.length() - 2) == ',') {
				sb.deleteCharAt(sb.length() - 2);
			}
		}
	}

	private void appendWhereClause(StringBuffer sb) {
		if (!lstWhere.isEmpty()) {
			if (sb.length() != 0) {
				sb.append("WHERE \n");
				for (Condition condition : lstWhere) {
					sb.append("\t").append(condition.toString()).append("\n");
				}
			}
		}
	}

	private void appendGroupByClause(StringBuffer sb) {
		if (!lstGroupBy.isEmpty()) {
			sb.append("GROUP BY \n");
			for (GroupBy groupBy : lstGroupBy) {
				sb.append("\t");
				if (!groupBy.getColumn().isExpression()) {
					sb.append(groupBy.getColumn().getTable().getAlias());
					sb.append(".\"");
				}
				sb.append(groupBy.getColumn().getName());
				if (!groupBy.getColumn().isExpression()) {
					sb.append("\"");
				}
				sb.append(",\n");
			}
			if (sb.charAt(sb.length() - 2) == ',') {
				sb.deleteCharAt(sb.length() - 2);
			}
		}
	}

	private void appendOrderByClause(StringBuffer sb) {
		if (!lstOrderBy.isEmpty()) {
			if (sb.length() != 0) {
				sb.append("ORDER BY \n");
				for (OrderBy orderBy : lstOrderBy) {
					final Column column = orderBy.getColumn();
					final Table table = column.getTable();
					if (table.getAlias() == null) {
						System.err.println("Processing error: table " + table.getName() + " is referenced in ORDER BY clause, but not added as alias");
					}
					else {
						if (column.getAlias() != null) {
							sb.append("\t").append("\"").append(column.getAlias()).append("\" ");
						}
						else {
							sb.append("\t").append(table.getAlias());
							sb.append('.');
							sb.append("\"").append(column.getName()).append("\" ");
						}
						sb.append(orderBy.isAscending() ? "ASC" : "DESC");
						sb.append(",\n");
					}
				}
				if (sb.charAt(sb.length() - 2) == ',') {
					sb.deleteCharAt(sb.length() - 2);
				}
			}
		}
	}

	private static String getAlias(Column column) {
		return StringUtils.isNullOrEmpty(column.getAlias()) ? "" : " \"" + column.getAlias() + "\"";
	}

	private String translateGroupBy(String groupType) {
		return mpTranslateGroupBy.get(groupType);
	}

	public String getAlias(Table table) {
		for (Table t : lstFrom) {
			if (t.equals(table)) {
				return t.getAlias();
			}
		}
		return "";
	}

	public void addWhereClause(Condition condition, String sCondition) {
		if (!lstWhere.isEmpty()) {
			addToWhereClause(condition);
		}
		addToWhereClause(new Condition(new Predicate(new Operand(sCondition))));
	}

	public void addColumnWhereClauses(List<String> lstColumnConditions) {

		if (!lstWhere.isEmpty()) {
			addToWhereClause(new AndCondition());
		}
		addToWhereClause(new OpenBracketCondition());

// int iCondCount = lstColumnConditions.size();
		boolean bFirst = true;
		for (String sCondition : lstColumnConditions) {
// /*@todo this is a rather dirty workaround for Bug 1278 */
// if(iCondCount > 1) {
// //delete join type in case of OR-Condition
// sCondition = sCondition.replaceFirst("\\(\\+\\)", "");
// }
			if (!bFirst) {
				addToWhereClause(new OrCondition());
			}
			addToWhereClause(new Condition(new Predicate(new Operand(sCondition))));
			bFirst = false;
		}
		addToWhereClause(new CloseBracketCondition());
	}

	public void addToGroupByClause(Column col, String sGroupType) {
		lstGroupBy.add(new GroupBy(col, sGroupType));
	}

	public void addToGroupByMap(Column col, String groupBy) {
		mpGroupBy.put(col, translateGroupBy(groupBy));
	}

	/**
	 * Method needed for MSSQL Server, because the join condition is not in
	 * the where block, but in the from block. With this Method the
	 * duplicate Tables are removed which are already used by the join
	 * condition. otherwise the tables are used twice.
	 *
	 * @param toDelete
	 */
        public void deleteItemFromLstFrom(String tblToDelete) {
        	String tablename = tblToDelete;
        	for (int i = 0; i < lstFrom.size(); i++) {
        	    Table t = lstFrom.get(i);
        	    if (t.getName().equals(tablename))
        		lstFrom.remove(i);
        	}

        }

}	// class SelectQuery
