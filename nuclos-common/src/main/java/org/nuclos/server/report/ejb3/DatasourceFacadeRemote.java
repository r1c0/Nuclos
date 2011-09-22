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

import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;

import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoteException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.database.query.definition.Schema;
import org.nuclos.common.database.query.definition.Table;
import org.nuclos.common.querybuilder.NuclosDatasourceException;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.valueobject.DatasourceParameterVO;
import org.nuclos.server.report.valueobject.DatasourceVO;
import org.nuclos.server.report.valueobject.DynamicEntityVO;
import org.nuclos.server.report.valueobject.RecordGrantVO;
import org.nuclos.server.report.valueobject.ResultVO;
import org.nuclos.server.report.valueobject.ValuelistProviderVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;

@Remote
public interface DatasourceFacadeRemote {

	/**
	 * get all datasources
	 *
	 * @return set of datasources
	 * @throws CommonPermissionException
	 */
	public abstract Collection<DatasourceVO> getDatasources()
		throws CommonPermissionException;

	/**
	 * get all datasources
	 *
	 * @return set of datasources
	 */
	public abstract Collection<DatasourceVO> getDatasourcesForCurrentUser();

	/**
	 * get datasource value object
	 *
	 * @param iId
	 *                primary key of datasource
	 * @return datasource value object
	 */
	@RolesAllowed("Login")
	public abstract DatasourceVO get(Integer iId) throws CommonFinderException,
		CommonPermissionException;

	/**
	 * get datasource value object
	 *
	 * @param sDatasourceName
	 *                name of datasource
	 * @return datasource value object
	 */
	@RolesAllowed("Login")
	public abstract DatasourceVO get(String sDatasourceName)
		throws CommonFinderException, CommonPermissionException;

	/**
	 * get a Datasource by id regardless of permisssions
	 * @param iDatasourceId
	 * @return
	 * @throws CommonPermissionException
	 */
	@RolesAllowed("Login")
	public abstract DatasourceVO getDatasourceById(Integer iDatasourceId);

	/**
	 * create new datasource
	 *
	 * @param datasourcevo
	 *                value object
	 * @return new datasource
	 */
	public abstract DatasourceVO create(DatasourceVO datasourcevo,
		List<String> lstUsedDatasources) throws CommonCreateException,
		CommonValidationException, NuclosBusinessRuleException,
		CommonPermissionException;

	/**
	 * modify an existing datasource without usages
	 *
	 * @param datasourcevo
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 * @throws CommonStaleVersionException
	 * @throws CommonValidationException
	 */
	public abstract void modify(DatasourceVO datasourcevo)
		throws CommonFinderException, CommonPermissionException,
		CommonStaleVersionException, CommonValidationException,
		NuclosBusinessRuleException;

	/**
	 * modify an existing datasource
	 *
	 * @param datasourcevo
	 *                value object
	 * @return modified datasource
	 */
	public abstract DatasourceVO modify(DatasourceVO datasourcevo,
		List<String> lstUsedDatasources) throws CommonFinderException,
		CommonPermissionException, CommonStaleVersionException,
		CommonValidationException, NuclosBusinessRuleException,
		CommonRemoteException;


	/**
	 * get a list of DatasourceCVO which uses the datasource with the given id
	 *
	 * @param iDatasourceId
	 * @return
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 */
	@RolesAllowed("Login")
	public abstract List<DatasourceVO> getUsagesForDatasource(
		final Integer iDatasourceId) throws CommonFinderException,
		CommonPermissionException;

   /**
    * get a list of DatasourceVO which uses the datasource
    *
    * @param datasourceVO
    * 						could also be an instance of
    * 						<code>DynamicEntityVO</code> or
    * 						<code>ValuelistProviderVO</code>
    * @return
    * @throws CommonPermissionException
    * @throws CommonFinderException
    */
   @RolesAllowed("Login")
   public List<DatasourceVO> getUsagesForDatasource(DatasourceVO datasourceVO) throws
   CommonFinderException, CommonPermissionException;

	/**
	 * get a list of DatasourceCVO which are used by the datasource with the
	 * given id
	 *
	 * @param iDatasourceId
	 * @return
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 */
	public abstract List<DatasourceVO> getUsingByForDatasource(
		final Integer iDatasourceId) throws CommonFinderException,
		CommonPermissionException;

	/**
	 * delete an existing datasource
	 *
	 * @param datasourcevo
	 *                value object
	 */
	public abstract void remove(DatasourceVO datasourcevo)
		throws CommonFinderException, CommonRemoveException,
		CommonPermissionException, CommonStaleVersionException,
		NuclosBusinessRuleException;

	/**
	 * Retrieve the parameters a datasource accepts.
	 * @param sDatasourceXML
	 * @return
	 * @throws NuclosFatalException
	 * @throws NuclosDatasourceException
	 */
	@RolesAllowed("Login")
	public abstract List<DatasourceParameterVO> getParameters(
		String sDatasourceXML) throws NuclosFatalException,
		NuclosDatasourceException;

	/**
	 * Retrieve the parameters a datasource accepts.
	 * @param iDatasourceId
	 * @return
	 * @throws NuclosFatalException
	 * @throws NuclosDatasourceException
	 */
	@RolesAllowed("Login")
	public abstract List<DatasourceParameterVO> getParameters(
		Integer iDatasourceId) throws NuclosFatalException,
		NuclosDatasourceException;

	/**
    * validate the given DatasourceXML
    *
    * @param sDatasourceXML
    * @throws CommonValidationException
    * @throws NuclosReportException
    */
   @RolesAllowed("Login")
   public void validateSqlFromXML(String sDatasourceXML)
		throws CommonValidationException, NuclosDatasourceException;

   /**
    * validate the given SQL
    *
    * @param sql
    * @throws CommonValidationException
    * @throws NuclosReportException
    */
   @RolesAllowed("Login")
   public void validateSql(String sql)
   	throws CommonValidationException, NuclosDatasourceException;

	/**
	 * get sql string for datasource definition
	 * @param iDatasourceId id of datasource
	 * @return string containing sql
	 */
	public abstract String createSQL(Integer iDatasourceId,
		Map<String, Object> mpParams) throws NuclosDatasourceException;

	/**
	 * get sql string for datasource definition without parameter definition
	 *
	 * @param sDatasourceXML
	 *                xml of datasource
	 * @return string containing sql
	 */
	@RolesAllowed("Login")
	public abstract String createSQL(String sDatasourceXML)
		throws NuclosDatasourceException;

	/**
	 * get sql string for datasource definition
	 *
	 * @param sDatasourceXML
	 *                xml of datasource
	 * @return string containing sql
	 */
	@RolesAllowed("Login")
	public abstract String createSQL(String sDatasourceXML,
		Map<String, Object> mpParams) throws NuclosDatasourceException;

	/**
	 * get sql string for datasource definition
	 *
	 * @param sDatasourceXML
	 *                xml of datasource
	 * @return string containing sql
	 */
	@RolesAllowed("Login")
	public abstract String createSQLOriginalParameter(String sDatasourceXML) throws NuclosDatasourceException;


	/**
	 * invalidate datasource cache
	 */
	@RolesAllowed("Login")
	public abstract void invalidateCache();

   /**
    * get all DynamicEntities
    *
    * @return set of DynamicEntityVO
    * @throws CommonPermissionException
    */
	@RolesAllowed("Login")
   public Collection<DynamicEntityVO> getDynamicEntities();

   /**
    * get dynamic entity value object
    *
    * @param iDynamicEntityId
    *                primary key of dynamic entity
    * @return DynamicEntityVO
    */
   @RolesAllowed("Login")
   public DynamicEntityVO getDynamicEntity(Integer iDynamicEntityId) throws CommonPermissionException;

   /**
    * get all ValuelistProvider
    *
    * @return set of ValuelistProviderVO
    * @throws CommonPermissionException
    */
   @RolesAllowed("Login")
   public Collection<ValuelistProviderVO> getValuelistProvider() throws CommonPermissionException;

   /**
    * get valuelist provider value object
    *
    * @param iValuelistProviderId
    *                primary key of valuelist provider
    * @return ValuelistProviderVO
    */
   @RolesAllowed("Login")
   public ValuelistProviderVO getValuelistProvider(Integer iValuelistProviderId) throws CommonPermissionException;

   /**
    * create new dynamic entity
    *
    * @param dynamicEntityVO
    *                value object
    * @param lstUsedDynamicEntities
    * 					list of used dynamic entity names
    * @return new dynamic entity
    */
   @RolesAllowed("Login")
   public DynamicEntityVO createDynamicEntity(DynamicEntityVO dynamicEntityVO, List<String> lstUsedDynamicEntities) throws CommonCreateException, CommonValidationException, NuclosBusinessRuleException, CommonPermissionException;

   /**
    * create new valuelist provider
    *
    * @param valuelistProviderVO
    *                value object
    * @param lstUsedValuelistProvider
    * 					list of used valuelist provider names
    * @return new valuelist provider
    */
   @RolesAllowed("Login")
   public ValuelistProviderVO createValuelistProvider(ValuelistProviderVO valuelistProviderVO, List<String> lstUsedValuelistProvider) throws CommonCreateException, CommonValidationException, NuclosBusinessRuleException, CommonPermissionException;

   /**
    * delete an existing dynamic entity
    *
    * @param dynamicEntityVO
    *                value object
    */
   @RolesAllowed("Login")
   public void removeDynamicEntity(DynamicEntityVO dynamicEntityVO) throws CommonFinderException, CommonRemoveException, CommonPermissionException, CommonStaleVersionException, NuclosBusinessRuleException ;

   /**
    * delete an existing valuelist provider
    *
    * @param valuelistProviderVO
    *                value object
    */
   @RolesAllowed("Login")
   public void removeValuelistProvider(ValuelistProviderVO valuelistProviderVO) throws CommonFinderException, CommonRemoveException, CommonPermissionException, CommonStaleVersionException, NuclosBusinessRuleException;

   /**
    * modify an existing dynamic entity
    *
    * @param dynamicEntityVO
    * @param lstUsedDynamicEntities
    * @return modified dynamic entity
    * @throws CommonFinderException
    * @throws CommonPermissionException
    * @throws CommonStaleVersionException
    * @throws CommonValidationException
    */
   @RolesAllowed("Login")
   public DynamicEntityVO modifyDynamicEntity(DynamicEntityVO dynamicEntityVO, List<String> lstUsedDynamicEntities) throws CommonFinderException, CommonPermissionException, CommonStaleVersionException, CommonValidationException, NuclosBusinessRuleException;

   /**
    * modify an existing valuelist provider
    *
    * @param datasourcevo
    * @param lstUsedValuelistProvider
    * @return modified valuelist provider
    * @throws CommonFinderException
    * @throws CommonPermissionException
    * @throws CommonStaleVersionException
    * @throws CommonValidationException
    */
   @RolesAllowed("Login")
   public ValuelistProviderVO modifyValuelistProvider(ValuelistProviderVO valuelistProviderVO, List<String> lstUsedValuelistProvider) throws CommonFinderException, CommonPermissionException, CommonStaleVersionException, CommonValidationException, NuclosBusinessRuleException;

	/**
	 * get a datasource result by datasource id
	 * @param iDatasourceId
	 * @param mpParams
	 * @param iMaxRowCount
	 * @return
	 * @throws NuclosReportException
	 * @throws CommonFinderException
	 */
   @RolesAllowed("Login")
	public abstract ResultVO executeQuery(Integer iDatasourceId,
		Map<String, Object> mpParams, Integer iMaxRowCount)
		throws NuclosDatasourceException, CommonFinderException;

	/**
	 * gets a datasource result by datasource xml
	 * @param sDatasourceXML datasource id
	 * @param mpParams parameters
	 * @param iMaxRowCount
	 * @return report/form filled with data
	 */
   @RolesAllowed("Login")
	public abstract ResultVO executeQuery(String sDatasourceXML,
		Map<String, Object> mpParams, Integer iMaxRowCount)
		throws CommonFinderException, NuclosDatasourceException;

   /**
    *
    * @return
    */
	public abstract Schema getSchemaTables();

	/**
	 * @throws CommonPermissionException
	 */
	public abstract Table getSchemaColumns(Table table);

	/**
    * get valuelist provider value object
    *
    * @param sValuelistProvider
    *                name of valuelist provider
    * @return valuelist provider value object
    */
   @RolesAllowed("Login")
   public ValuelistProviderVO getValuelistProvider(String sValuelistProvider) throws CommonFinderException, CommonPermissionException;

   /**
    * get dynamic entity value object
    *
    * @param sDynamicEntity
    *                name of valuelist provider
    * @return dynamic entity value object
    */
   @RolesAllowed("Login")
   public DynamicEntityVO getDynamicEntity(String sDynamicEntity) throws CommonFinderException, CommonPermissionException;

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
    *                primary key of RecordGrant
    * @return RecordGrantVO
    */
   @RolesAllowed("Login")
   RecordGrantVO getRecordGrant(Integer iRecordGrantId) throws CommonPermissionException;

	/**
	 * get RecordGrant value object
	 *
	 * @param sRecordGrant
	 *                name of RecordGrant
	 * @return RecordGrant value object
	 */
   @RolesAllowed("Login")
	RecordGrantVO getRecordGrant(String sRecordGrant) throws CommonFinderException, CommonPermissionException;

   /**
    * delete an existing RecordGrant
    *
    * @param recordGrantVO
    *                value object
    */
   @RolesAllowed("Login")
   public void removeRecordGrant(RecordGrantVO recordGrantVO) throws CommonFinderException, CommonRemoveException, CommonPermissionException, CommonStaleVersionException, NuclosBusinessRuleException;

   /**
    * modify an existing RecordGrant
    *
    * @param recordGrantVO
    * @param lstUsedRecordGrant
    * @return modified valuelist provider
    * @throws CommonFinderException
    * @throws CommonPermissionException
    * @throws CommonStaleVersionException
    * @throws CommonValidationException
    */
   @RolesAllowed("Login")
   public RecordGrantVO modifyRecordGrant(RecordGrantVO recordGrantVO, List<String> lstUsedRecordGrant) throws CommonFinderException, CommonPermissionException, CommonStaleVersionException, CommonValidationException, NuclosBusinessRuleException;

   /**
    * create new RecordGrant
    *
    * @param recordGrantVO
    *                value object
    * @param lstUsedRecordGrant
    * 					list of used RecordGrant names
    * @return new RecordGrant
    */
   @RolesAllowed("Login")
   public RecordGrantVO createRecordGrant(RecordGrantVO recordGrantVO, List<String> lstUsedRecordGrant) throws CommonCreateException, CommonValidationException, NuclosBusinessRuleException, CommonPermissionException;

}
