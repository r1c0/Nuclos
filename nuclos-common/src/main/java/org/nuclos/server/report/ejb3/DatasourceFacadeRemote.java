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
package org.nuclos.server.report.ejb3;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;

import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.database.query.definition.Schema;
import org.nuclos.common.database.query.definition.Table;
import org.nuclos.common.querybuilder.NuclosDatasourceException;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoteException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.valueobject.ChartVO;
import org.nuclos.server.report.valueobject.DatasourceParameterVO;
import org.nuclos.server.report.valueobject.DatasourceVO;
import org.nuclos.server.report.valueobject.DynamicEntityVO;
import org.nuclos.server.report.valueobject.DynamicTasklistVO;
import org.nuclos.server.report.valueobject.RecordGrantVO;
import org.nuclos.server.report.valueobject.ResultVO;
import org.nuclos.server.report.valueobject.ValuelistProviderVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;

@RolesAllowed("Login")
public interface DatasourceFacadeRemote {

	/**
	 * get all datasources
	 *
	 * @return set of datasources
	 * @throws CommonPermissionException
	 */
	Collection<DatasourceVO> getDatasources() throws CommonPermissionException;

	/**
	 * get all datasources
	 *
	 * @return set of datasources
	 */
	Collection<DatasourceVO> getDatasourcesForCurrentUser();

	/**
	 * get datasource value object
	 *
	 * @param iId
	 *            primary key of datasource
	 * @return datasource value object
	 */
	@RolesAllowed("Login")
	DatasourceVO get(Integer iId) throws CommonFinderException, CommonPermissionException;

	/**
	 * get datasource value object
	 *
	 * @param sDatasourceName
	 *            name of datasource
	 * @return datasource value object
	 */
	@RolesAllowed("Login")
	DatasourceVO get(String sDatasourceName) throws CommonFinderException, CommonPermissionException;

	/**
	 * get a Datasource by id regardless of permisssions
	 *
	 * @param iDatasourceId
	 * @return
	 * @throws CommonPermissionException
	 */
	@RolesAllowed("Login")
	DatasourceVO getDatasourceById(Integer iDatasourceId);

	/**
	 * create new datasource
	 *
	 * @param datasourcevo
	 *            value object
	 * @return new datasource
	 */
	DatasourceVO create(DatasourceVO datasourcevo, DependantMasterDataMap dependants, List<String> lstUsedDatasources) throws CommonCreateException, CommonValidationException, NuclosBusinessRuleException, CommonPermissionException;

	/**
	 * modify an existing datasource without usages
	 *
	 * @param datasourcevo
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 * @throws CommonStaleVersionException
	 * @throws CommonValidationException
	 */
	void modify(DatasourceVO datasourcevo) throws CommonFinderException, CommonPermissionException, CommonStaleVersionException, CommonValidationException, NuclosBusinessRuleException;

	/**
	 * modify an existing datasource
	 *
	 * @param datasourcevo
	 *            value object
	 * @return modified datasource
	 */
	DatasourceVO modify(DatasourceVO datasourcevo, DependantMasterDataMap dependants, List<String> lstUsedDatasources) throws CommonFinderException, CommonPermissionException, CommonStaleVersionException, CommonValidationException,
			NuclosBusinessRuleException, CommonRemoteException;

	/**
	 * get a list of DatasourceCVO which uses the datasource with the given id
	 *
	 * @param iDatasourceId
	 * @return
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 */
	@RolesAllowed("Login")
	List<DatasourceVO> getUsagesForDatasource(final Integer iDatasourceId) throws CommonFinderException, CommonPermissionException;

	/**
	 * get a list of DatasourceVO which uses the datasource
	 *
	 * @param datasourceVO
	 *            could also be an instance of <code>DynamicEntityVO</code> or
	 *            <code>ValuelistProviderVO</code>
	 * @return
	 * @throws CommonPermissionException
	 * @throws CommonFinderException
	 */
	@RolesAllowed("Login")
	List<DatasourceVO> getUsagesForDatasource(DatasourceVO datasourceVO) throws CommonFinderException, CommonPermissionException;

	/**
	 * get a list of DatasourceCVO which are used by the datasource with the
	 * given id
	 *
	 * @param iDatasourceId
	 * @return
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 */
	List<DatasourceVO> getUsingByForDatasource(final Integer iDatasourceId) throws CommonFinderException, CommonPermissionException;

	/**
	 * delete an existing datasource
	 *
	 * @param datasourcevo
	 *            value object
	 */
	void remove(DatasourceVO datasourcevo) throws CommonFinderException, CommonRemoveException, CommonPermissionException, CommonStaleVersionException, NuclosBusinessRuleException;

	/**
	 * Retrieve the parameters a datasource accepts.
	 *
	 * @param sDatasourceXML
	 * @return
	 * @throws NuclosFatalException
	 * @throws NuclosDatasourceException
	 */
	@RolesAllowed("Login")
	List<DatasourceParameterVO> getParameters(String sDatasourceXML) throws NuclosFatalException, NuclosDatasourceException;

	/**
	 * Retrieve the parameters a datasource accepts.
	 *
	 * @param iDatasourceId
	 * @return
	 * @throws NuclosFatalException
	 * @throws NuclosDatasourceException
	 */
	@RolesAllowed("Login")
	List<DatasourceParameterVO> getParameters(Integer iDatasourceId) throws NuclosFatalException, NuclosDatasourceException;

	/**
	 * validate the given DatasourceXML
	 *
	 * @param sDatasourceXML
	 * @throws CommonValidationException
	 * @throws NuclosReportException
	 */
	@RolesAllowed("Login")
	void validateSqlFromXML(String sDatasourceXML) throws CommonValidationException, NuclosDatasourceException;

	/**
	 * validate the given SQL
	 *
	 * @param sql
	 * @throws CommonValidationException
	 * @throws NuclosReportException
	 */
	@RolesAllowed("Login")
	void validateSql(String sql) throws CommonValidationException, NuclosDatasourceException;

	/**
	 * get sql string for datasource definition
	 *
	 * @param iDatasourceId
	 *            id of datasource
	 * @return string containing sql
	 */
	String createSQL(Integer iDatasourceId, Map<String, Object> mpParams) throws NuclosDatasourceException;

	/**
	 * get sql string for datasource definition without parameter definition
	 *
	 * @param sDatasourceXML
	 *            xml of datasource
	 * @return string containing sql
	 */
	@RolesAllowed("Login")
	String createSQL(String sDatasourceXML) throws NuclosDatasourceException;

	/**
	 * get sql string for datasource definition
	 *
	 * @param sDatasourceXML
	 *            xml of datasource
	 * @return string containing sql
	 */
	@RolesAllowed("Login")
	String createSQL(String sDatasourceXML, Map<String, Object> mpParams) throws NuclosDatasourceException;

	/**
	 * get sql string for datasource definition
	 *
	 * @param sDatasourceXML
	 *            xml of datasource
	 * @return string containing sql
	 */
	@RolesAllowed("Login")
	String createSQLOriginalParameter(String sDatasourceXML) throws NuclosDatasourceException;

	/**
	 * invalidate datasource cache
	 */
	@RolesAllowed("Login")
	void invalidateCache();

	/**
	 * get all DynamicEntities
	 *
	 * @return set of DynamicEntityVO
	 * @throws CommonPermissionException
	 */
	@RolesAllowed("Login")
	Collection<DynamicEntityVO> getDynamicEntities();

	/**
	 * get dynamic entity value object
	 *
	 * @param iDynamicEntityId
	 *            primary key of dynamic entity
	 * @return DynamicEntityVO
	 */
	@RolesAllowed("Login")
	DynamicEntityVO getDynamicEntity(Integer iDynamicEntityId) throws CommonPermissionException;

	/**
	 * get all charts
	 * 
	 * @return set of ChartVO
	 * @throws CommonPermissionException
	 */
	@RolesAllowed("Login")
	Collection<ChartVO> getCharts();

	/**
	 * get chart value object
	 * 
	 * @param iChartId
	 *            primary key of chart
	 * @return ChartVO
	 */
	@RolesAllowed("Login")
	ChartVO getChart(Integer iChartId) throws CommonPermissionException;

	/**
	 * get chart value object
	 * 
	 * @param sChart
	 *            name of chart
	 * @return chart value object
	 */
	@RolesAllowed("Login")
	public ChartVO getChart(String sChart) throws CommonFinderException, CommonPermissionException;
	
	/**
	 * get all ValuelistProvider
	 *
	 * @return set of ValuelistProviderVO
	 * @throws CommonPermissionException
	 */
	@RolesAllowed("Login")
	Collection<ValuelistProviderVO> getValuelistProvider() throws CommonPermissionException;

	/**
	 * get valuelist provider value object
	 *
	 * @param iValuelistProviderId
	 *            primary key of valuelist provider
	 * @return ValuelistProviderVO
	 */
	@RolesAllowed("Login")
	ValuelistProviderVO getValuelistProvider(Integer iValuelistProviderId) throws CommonPermissionException;

	/**
	 * get a datasource result by datasource id
	 *
	 * @param iDatasourceId
	 * @param mpParams
	 * @param iMaxRowCount
	 * @return
	 * @throws NuclosReportException
	 * @throws CommonFinderException
	 */
	@RolesAllowed("Login")
	ResultVO executeQuery(Integer iDatasourceId, Map<String, Object> mpParams, Integer iMaxRowCount) throws NuclosDatasourceException, CommonFinderException;

	/**
	 * gets a datasource result by datasource xml
	 *
	 * @param sDatasourceXML
	 *            datasource id
	 * @param mpParams
	 *            parameters
	 * @param iMaxRowCount
	 * @return report/form filled with data
	 */
	@RolesAllowed("Login")
	ResultVO executeQuery(String sDatasourceXML, Map<String, Object> mpParams, Integer iMaxRowCount) throws CommonFinderException, NuclosDatasourceException;

	/**
	 *
	 * @return
	 */
	Schema getSchemaTables();

	/**
	 * @throws CommonPermissionException
	 */
	Table getSchemaColumns(Table table);

	/**
	 * get valuelist provider value object
	 *
	 * @param sValuelistProvider
	 *            name of valuelist provider
	 * @return valuelist provider value object
	 */
	@RolesAllowed("Login")
	ValuelistProviderVO getValuelistProvider(String sValuelistProvider) throws CommonFinderException, CommonPermissionException;

	/**
	 * get dynamic entity value object
	 *
	 * @param sDynamicEntity
	 *            name of valuelist provider
	 * @return dynamic entity value object
	 */
	@RolesAllowed("Login")
	DynamicEntityVO getDynamicEntity(String sDynamicEntity) throws CommonFinderException, CommonPermissionException;

	/**
	 * get all RecordGrant
	 *
	 * @return set of RecordGrantVO
	 * @throws CommonPermissionException
	 */
	@RolesAllowed("Login")
	Collection<RecordGrantVO> getRecordGrant() throws CommonPermissionException;

	/**
	 * get RecordGrant value object
	 *
	 * @param iRecordGrantId
	 *            primary key of RecordGrant
	 * @return RecordGrantVO
	 */
	@RolesAllowed("Login")
	RecordGrantVO getRecordGrant(Integer iRecordGrantId) throws CommonPermissionException;

	/**
	 * get RecordGrant value object
	 *
	 * @param sRecordGrant
	 *            name of RecordGrant
	 * @return RecordGrant value object
	 */
	@RolesAllowed("Login")
	RecordGrantVO getRecordGrant(String sRecordGrant) throws CommonFinderException, CommonPermissionException;

	@RolesAllowed("Login")
	Collection<DynamicTasklistVO> getDynamicTasklists() throws CommonPermissionException;

	@RolesAllowed("Login")
	DynamicTasklistVO getDynamicTasklist(Integer id) throws CommonPermissionException;

	@RolesAllowed("Login")
	Set<String> getDynamicTasklistAttributes(Integer dtlId) throws CommonPermissionException, NuclosDatasourceException;

	@RolesAllowed("Login")
	ResultVO getDynamicTasklistData(Integer dtlId) throws CommonPermissionException, NuclosDatasourceException;

	CollectableField getDefaultValue(String datasource, String valuefield, String idfield, String defaultfield, Map<String, Object> params) throws CommonBusinessException;
	
	@RolesAllowed("Login")
	public List<String> getColumns(String sql);
	
	@RolesAllowed("Login")
	List<String> getColumnsFromXml(String sDatasourceXML) throws NuclosBusinessException;
}
