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
package org.nuclos.server.common;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.searchcondition.PlainSubCondition;
import org.nuclos.common.database.query.SelectQuery;
import org.nuclos.common.database.query.definition.Column;
import org.nuclos.common.database.query.definition.Join;
import org.nuclos.common.database.query.definition.Join.JoinType;
import org.nuclos.common.database.query.definition.Schema;
import org.nuclos.common.database.query.definition.Table;
import org.nuclos.common.dblayer.IFieldRef;
import org.nuclos.common.querybuilder.DatasourceParameterParser;
import org.nuclos.common.querybuilder.DatasourceXMLParser;
import org.nuclos.common.querybuilder.DatasourceXMLParser.XMLConnector;
import org.nuclos.common.querybuilder.DatasourceXMLParser.XMLTable;
import org.nuclos.common.querybuilder.NuclosDatasourceException;
import org.nuclos.common2.KeyEnum;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.database.SpringDataBaseHelper;
import org.nuclos.server.dblayer.DbType;
import org.nuclos.server.report.SchemaCache;
import org.nuclos.server.report.WhereConditionParser;
import org.nuclos.server.report.valueobject.DatasourceParameterVO;
import org.nuclos.server.report.valueobject.DatasourceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class DatasourceServerUtils {
	
	private static final Logger LOG	= Logger.getLogger(DatasourceServerUtils.class);
	
	//

	private final SQLCache sqlCache = new SQLCache();
	
	private SessionUtils utils;
	
	private SchemaCache schemaCache;
	
	public DatasourceServerUtils() {
	}
	
	@Autowired
	void setSessionUtils(SessionUtils utils) {
		this.utils = utils;
	}
	
	@Autowired
	void setSchemaCache(SchemaCache schemaCache) {
		this.schemaCache = schemaCache;
	}
	
	public void invalidateCache() {
		sqlCache.invalidate();
	}

	/**
	 * get sql string for datasource definition
	 *
	 * @param iDatasourceId id of datasource
	 * @return string containing sql
	 */
	public String createSQL(Integer iDatasourceId,
	    Map<String, Object> mpParams) throws NuclosDatasourceException {
		try {
			// final DatasourceLocal datasource =
			// datasourceHome.findByPrimaryKey(iDatasourceId);
			/** @todo consider encoding (UTF-8) */
			// return createSQL(new String(datasource.getObjDatasourceXML()),
			// mpParams);
			DatasourceVO vo = DatasourceCache.getInstance().getDatasourcesById(
			    iDatasourceId, utils.getCurrentUserName());
			return createSQL(vo.getSource(), mpParams);
		}
		catch(CommonPermissionException e) {
			throw new NuclosDatasourceException("createSQL failed: " + e, e);
		}
	}

	/**
	 * get sql string for report execution.
	 * There is no authorization as the authorization is applied to the report.
	 *
	 * @param iDatasourceId id of datasource
	 * @return string containing sql
	 */
	public String createSQLForReportExecution(String name, Map<String, Object> mpParams) throws NuclosDatasourceException {
		DatasourceVO vo = DatasourceCache.getInstance().getDatasourceByName(name);
		return createSQL(vo.getSource(), mpParams);
	}

	/**
	 * get sql string for datasource definition without parameter definition
	 *
	 * @param sDatasourceXML xml of datasource
	 * @return string containing sql
	 */
	public String createSQL(String sDatasourceXML) throws NuclosDatasourceException {
		return sqlCache.getSQL(sDatasourceXML);
	}

	/**
	 *
	 * @param sDatasourceXML
	 * @param mpParams
	 * @return new PlainSubCondition with '(SELECT intid FROM ( ... ) ds )' condition
	 * @throws NuclosDatasourceException
	 */
	public PlainSubCondition getConditionWithIdForInClause(String sDatasourceXML, Map<String, Object> mpParams) throws NuclosDatasourceException {
		return new PlainSubCondition(getSqlWithIdForInClause(sDatasourceXML, mpParams));
	}

	/**
	 *
	 * @param sDatasourceXML
	 * @param mpParams
	 * @return
	 * @throws NuclosDatasourceException
	 */
	public String getSqlWithIdForInClause(String sDatasourceXML, Map<String, Object> mpParams) throws NuclosDatasourceException {
		//@see NUCLOSINT-1347
		String sql = createSQL(sDatasourceXML, mpParams);
		if (sql.indexOf(" \"intid\"") != -1)
			return "(SELECT \"intid\" FROM (" + sql + ") ds )";
		else if (sql.indexOf(" \"INTID\"") != -1)
			return "(SELECT \"INTID\" FROM (" + sql + ") ds )";
		else if (sql.indexOf(" intid") != -1)
			return "(SELECT intid FROM (" + sql + ") ds )";
		else if (sql.indexOf(" INTID") != -1)
			return "(SELECT INTID FROM (" + sql + ") ds )";
		return "(SELECT \"intid\" FROM (" + sql + ") ds )"; // as it was before...
	}

	/**
	 *
	 * @param sDatasourceXML
	 * @param mpParams
	 * @return
	 * @throws NuclosDatasourceException
	 */
	public String getSqlQueryForId(String sDatasourceXML, Map<String, Object> mpParams, Long id) throws NuclosDatasourceException {
		return MessageFormat.format("SELECT * FROM ({1}) ds WHERE ds.{0} = {2}", "\"intid\"", createSQL(sDatasourceXML, mpParams), String.valueOf(id));
	}

	/**
	 * get sql string for datasource definition
	 *
	 * @param sDatasourceXML xml of datasource
	 * @return string containing sql
	 */
	public String createSQL(String sDatasourceXML, Map<String, Object> mpParams) throws NuclosDatasourceException {
		String result = sqlCache.getSQL(sDatasourceXML);
		if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() != null) {
			if (mpParams.get("username") == null) // username is already set. seems to be from preview of the datasource. @see NUCLOSINT-1560
				mpParams.put("username", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		}
		result = replaceParameters(result, mpParams==null?(new HashMap<String, Object>()):mpParams);
		LOG.debug(result);

		return result;
	}

	/**
	 *
	 * @param sDatasourceXML
	 * @return sql with original parameter placeholders
	 * @throws NuclosDatasourceException
	 */
	public String createSQLOriginalParameter(String sDatasourceXML) throws NuclosDatasourceException {
		return sqlCache.getSQL(sDatasourceXML);
	}

	/**
	 * get sql string for datasource definition
	 *
	 * @param sDatasourceXML xml of datasource
	 * @return string containing sql
	 */
	private String createSQLForCaching(String sDatasourceXML) throws NuclosDatasourceException {
		String result = null;
		final SelectQuery query = new SelectQuery(utils.getCurrentUserName());

		final DatasourceXMLParser.Result parseresult = parseDatasourceXML(sDatasourceXML);

		if(parseresult.isModelUsed()) {
			// final Schema schema =
			// SchemaCache.getSchema(ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_NUCLOS_SCHEMA));
			final Schema schema = schemaCache.getCurrentSchema();

			// create from clause
			final Map<String, Table> fromTables = new HashMap<String, Table>();
			for (XMLTable xmlTable : parseresult.getMapXMLTables().values()) {
				Table t = createTable(schema, xmlTable);
				fromTables.put(t.getAlias(), t);
				query.addToFromClause(t);
			}

			for(XMLConnector xmlconnector : parseresult.getLstConnectors()) {
				final Table srcTable = (Table) (fromTables.get(xmlconnector.getSrcTable())).clone();
				final Column srcColumn = new Column(srcTable.getColumn(xmlconnector.getSrcColumn()));
				final Table dstTable = (Table) (fromTables.get(xmlconnector.getDstTable())).clone();
				final Column dstColumn = new Column(dstTable.getColumn(xmlconnector.getDstColumn()));
				final JoinType joinType = xmlconnector.getJoinType() == null ? JoinType.INNER_JOIN :
					KeyEnum.Utils.findEnum(JoinType.class, xmlconnector.getJoinType());
				Join join = new Join(srcColumn, joinType == null ? JoinType.INNER_JOIN : joinType, dstColumn);
				query.addJoin(join);
			}

			// create select, where, group by, order by claues
			final boolean bGroupBy = checkGroupBy(parseresult.getLstColumns());
			final List<String> lstColumnConditions = new ArrayList<String>();

			for(DatasourceXMLParser.XMLColumn xmlcolumn : parseresult.getLstColumns()) {
				final Column column = createColumn(fromTables, xmlcolumn);
				if(xmlcolumn.isVisible()) {
					query.addToSelectClause(column, xmlcolumn.getAlias());
				}
				if(bGroupBy) {
					if(xmlcolumn.getGroup() == null || xmlcolumn.getGroup().length() == 0) {
						xmlcolumn.setGroup(DatasourceVO.GroupBy.MAX.getLabel());
					}
					else if(xmlcolumn.getGroup().equals(DatasourceVO.GroupBy.GROUP.getLabel())) {
						query.addToGroupByClause(column, xmlcolumn.getGroup());
					}
					else {
						query.addToGroupByMap(column, xmlcolumn.getGroup());
					}
				}
				lstColumnConditions.clear();
				final WhereConditionParser whereConditionParser = new WhereConditionParser();
				for(DatasourceXMLParser.XMLCondition xmlcondition : xmlcolumn.getLstConditions()) {
					if(xmlcondition.getCondition() != null && xmlcondition.getCondition().length() > 0) {
						final String sCondition = whereConditionParser.parseCondition(column, xmlcondition.getCondition());
						lstColumnConditions.add(sCondition);
					}
				}

				if(lstColumnConditions.size() > 0) {
					query.addColumnWhereClauses(lstColumnConditions);
				}
				if(xmlcolumn.getSort() != null && xmlcolumn.getSort().length() > 0) {
					query.addToOrderByClause(column, xmlcolumn.getSort().equals(DatasourceVO.OrderBy.ASCENDING.getLabel()));
				}
			}
			result = query.getSelectStatement(parseresult.isEntityOptionDynamic(),
						SpringDataBaseHelper.getInstance().getDbAccess().getDbType().equals(DbType.MSSQL));
		}
		else {
			result = parseresult.getQueryStringFromXml();
		}

		LOG.debug(result);
		return result;
	}

	/**
	 * parses the datasource XML
	 *
	 * @param sDatasourceXML
	 * @throws NuclosFatalException
	 * @throws NuclosDatasourceException
	 */
	private DatasourceXMLParser.Result parseDatasourceXML(
	    String sDatasourceXML) throws NuclosFatalException,
	    NuclosDatasourceException {
		return DatasourceXMLParser.parse(sDatasourceXML);
	}

	/**
	 * return a copy of a Table in Schema because more than one instance is
	 * possible in the Datasource
	 *
	 * @param schema
	 * @param xmltable
	 * @return a copy of the specified table from the schema.
	 */
	private Table createTable(Schema schema,
	    DatasourceXMLParser.XMLTable xmltable) {
		final Table table = schema.getTable(xmltable.getEntity());
		if(table != null) {
			final Table resultTable = (Table) table.clone();
			resultTable.setAlias(xmltable.getId());
			schemaCache.getColumns(resultTable);
			return resultTable;
		}
		return new Table(schema, "");
	}

	private Column createColumn(Map<String, Table> mpTables,
	    DatasourceXMLParser.XMLColumn column) {
		final Table table = mpTables.get(column.getTable());
		final Column result = new Column(table.getColumn(column.getColumn()));
		result.setTable(mpTables.get(column.getTable()));
		result.setAlias(column.getAlias());

		return result;
	}

	/**
	 * small helper method for creating a unique table alias for duplicate
	 * tables in mssql joins
	 *
	 * @return nextNumber _<number> T2 T2_1
	 */
	private String getNextNumberForTableAlias(String alias,
	    int tableAliasNumber) {
		String cut = alias;
		if(cut.indexOf("_") != -1) {
			cut = alias.substring(0, (alias.indexOf("_") + 1));
		}
		else {
			cut = alias + "_";
		}
		return cut + tableAliasNumber;
	}

	private boolean checkGroupBy(
	    List<DatasourceXMLParser.XMLColumn> lstColumns) {
		for(DatasourceXMLParser.XMLColumn xmlcolumn : lstColumns) {
			if(xmlcolumn.getGroup() != null
			    || (xmlcolumn.getGroup() != null && xmlcolumn.getGroup().length() > 0)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * replace all parameters in sSql with the values from mpParams
	 *
	 * @param sSql
	 * @param mpParams
	 * @return sql string with parameters replaced.
	 */
	private String replaceParameters(String sSql, Map<String, ?> mpParams) {
		final StringBuilder result = new StringBuilder(sSql.length());
		final DatasourceParameterParser p = new DatasourceParameterParser(sSql);
		for (IFieldRef token: p) {
			final String content = token.getContent();
			if (token.isConstant()) {
				result.append(content);
			}
			else {
				result.append(replaceParam("$" + content, mpParams));
			}
		}
		return result.toString();
		/*
		String result = sSql;
		for(String sParameter : DatasourceUtils.getParametersFromString(sSql)) {
			String value = replaceParam("$" + sParameter, mpParams);
			// quoted
			if (value == null) {
				result = replaceAll(result, "'$" + sParameter + "'", String.valueOf(value));
			}
			// unquoted
			result = replaceAll(result, "$" + sParameter, String.valueOf(value));
		}

		return result;
		*/
	}

	/**
	 * replace get the value for sParameter from mapParams
	 *
	 * @param sParameter
	 * @param mapParams
	 * @return the value for sParameter from the map, or sParameter if no value
	 *         is not found in the map
	 */
	private String replaceParam(String sParameter,
	    Map<String, ?> mapParams) {
		String sKey = sParameter.replace('$', ' ').trim();
		if(!mapParams.containsKey(sKey)) {
			return sParameter;
		}

		Object param = mapParams.get(sKey);
		if(param instanceof Date) {
			// ISO-format 'yyyy-mm-dd', matches JDBC escape syntax
			param = String.format("%tF", param);
		}
		if(param != null) {
			return param.toString();
		}
		else {
			return null;
		}
	}

	/**
	    * Retrieve the parameters a datasource accepts.
	    * @param sDatasourceXML
	    * @return
	    * @throws NuclosFatalException
	    * @throws NuclosDatasourceException
	    */
	   public List<DatasourceParameterVO> getParameters(String sDatasourceXML) throws NuclosFatalException, NuclosDatasourceException {
			// @todo we need a getParameters(Integer iDatasourceId) method for the
			// remote interface to prevent that every user needs the right to
			// execute the get method
			final List<DatasourceParameterVO> result = new ArrayList<DatasourceParameterVO>();
			final DatasourceXMLParser.Result parseresult = parseDatasourceXML(sDatasourceXML);
			result.addAll(parseresult.getLstParameters());
			return result;
	   }

	   /**
	    * Retrieve the parameters a datasource accepts.
	    * @param iDatasourceId
	    * @return
	    * @throws NuclosFatalException
	    * @throws NuclosDatasourceException
	    */
	   public List<DatasourceParameterVO> getParameters(Integer iDatasourceId) throws NuclosFatalException, NuclosDatasourceException {
	 		final List<DatasourceParameterVO> result = new ArrayList<DatasourceParameterVO>();
	 		DatasourceVO dsvo = DatasourceCache.getInstance().get(iDatasourceId);
	 		final DatasourceXMLParser.Result parseresult = parseDatasourceXML(dsvo.getSource());
	 		result.addAll(parseresult.getLstParameters());
	 		return result;
	   }


	   /**
	    * SQL Cache
	    */
	   public class SQLCache {

		   private final Map<String, String> mpDatasourcesSQLByXML 
		   	= new ConcurrentHashMap<String, String>();

		   private String getSQL(String sDatasourceXML) throws NuclosDatasourceException {
			   if (!mpDatasourcesSQLByXML.containsKey(sDatasourceXML)) {
				   mpDatasourcesSQLByXML.put(sDatasourceXML, createSQLForCaching(sDatasourceXML));
			   }

			   return mpDatasourcesSQLByXML.get(sDatasourceXML);
		   }

		   public void invalidate() {
			   mpDatasourcesSQLByXML.clear();
		   }
	   }

}
