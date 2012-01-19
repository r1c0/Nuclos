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
package org.nuclos.client.datasource;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.database.query.definition.Schema;
import org.nuclos.common.database.query.definition.Table;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.report.ejb3.DatasourceFacadeRemote;
import org.nuclos.server.report.valueobject.DatasourceParameterVO;
import org.nuclos.server.report.valueobject.DatasourceVO;
import org.nuclos.server.report.valueobject.DynamicEntityVO;
import org.nuclos.server.report.valueobject.RecordGrantVO;
import org.nuclos.server.report.valueobject.ResultVO;
import org.nuclos.server.report.valueobject.ValuelistProviderVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;

/**
 * The Delegate for DatasourceFacadeBean.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars Rueckemann</a>
 * @version 01.00.00
 */
public class DatasourceDelegate {
	private static DatasourceDelegate instance;

	private DatasourceFacadeRemote dataSourceFacade;

	public static synchronized DatasourceDelegate getInstance() {
		if (instance == null) {
			instance = new DatasourceDelegate();
		}
		return instance;
	}

	private DatasourceDelegate() {
		super();
	}

	private DatasourceFacadeRemote getDatasourceFacade() throws NuclosFatalException {
		if (dataSourceFacade == null) {
			try {
				dataSourceFacade = ServiceLocator.getInstance().getFacade(DatasourceFacadeRemote.class);
			}
			catch (RuntimeException ex) {
				throw new CommonFatalException(ex);
			}
		}
		return dataSourceFacade;
	}

	public List<DatasourceVO> getUsagesForDatasource(Integer iDatasourceId) throws CommonFinderException, CommonPermissionException {
		try {
			return this.getDatasourceFacade().getUsagesForDatasource(iDatasourceId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

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
   public List<DatasourceVO> getUsagesForDatasource(DatasourceVO datasourceVO) throws CommonFinderException, CommonPermissionException {
   	try {
			return this.getDatasourceFacade().getUsagesForDatasource(datasourceVO);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
   }

	public List<DatasourceVO> getUsingByForDatasource(Integer iDatasourceId) throws CommonFinderException, CommonPermissionException {
		try {
			return this.getDatasourceFacade().getUsingByForDatasource(iDatasourceId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public void setInvalid(final List<DatasourceVO> lstdatasourcevo) throws CommonFinderException, CommonPermissionException, CommonStaleVersionException, CommonValidationException, NuclosBusinessRuleException {
		try {
			for (DatasourceVO datasourcevo : lstdatasourcevo) {
				datasourcevo.setValid(false);
				this.getDatasourceFacade().modify(datasourcevo);
			}
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * Get datasource with the specified name.
	 * @param sName
	 * @return DatasourceVO
	 */
	public DatasourceVO getDatasourceByName(String sName) throws CommonBusinessException {
		try {
			return getDatasourceFacade().get(sName);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * Get dynamic entity with the specified name.
	 * @param sDynamicEntity
	 * @return DynamicEntityVO
	 */
	public DynamicEntityVO getDynamicEntityByName(String sDynamicEntity) throws CommonBusinessException {
		try {
			return getDatasourceFacade().getDynamicEntity(sDynamicEntity);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * Get valuelist provider with the specified name.
	 * @param sValuelistProvider
	 * @return ValuelistProviderVO
	 */
	public ValuelistProviderVO getValuelistProviderByName(String sValuelistProvider) throws CommonBusinessException {
		try {
			return getDatasourceFacade().getValuelistProvider(sValuelistProvider);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public List<DatasourceParameterVO> getParameters(String sDatasourceName) throws CommonBusinessException {
		try {
			DatasourceVO dsvo = getDatasourceByName(sDatasourceName);
			return this.getDatasourceFacade().getParameters(dsvo.getId());
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @return collection of all DynamicEntityVOs
	 */
	public Collection<DynamicEntityVO> getAllDynamicEntities() {
		try {
			return getDatasourceFacade().getDynamicEntities();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
    * get dynamic entity value object
    *
    * @param iDynamicEntityId
    *                primary key of dynamic entity
    * @return DynamicEntityVO
    */
   public DynamicEntityVO getDynamicEntity(Integer iDynamicEntityId) throws CommonPermissionException {
   	try {
			return getDatasourceFacade().getDynamicEntity(iDynamicEntityId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
   }

	/**
	 * @return collection of all ValuelistProviderVOs
	 */
	public Collection<ValuelistProviderVO> getAllValuelistProvider() throws CommonPermissionException {
		try {
			return getDatasourceFacade().getValuelistProvider();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
    * get valuelist provider value object
    *
    * @param iValuelistProviderId
    *                primary key of valuelist provider
    * @return ValuelistProviderVO
    */
   public ValuelistProviderVO getValuelistProvider(Integer iValuelistProviderId) throws CommonPermissionException {
   	try {
			return getDatasourceFacade().getValuelistProvider(iValuelistProviderId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
   }

	/**
	 * @param dynamicEntityVOs
	 * @param lstUsedDynamicEntities
	 * @return the newly created dynamic entity
	 */
	public DynamicEntityVO createDynamicEntity(DynamicEntityVO dynamicEntityVO, List<String> lstUsedDynamicEntities) throws CommonBusinessException {
		try {
			return getDatasourceFacade().createDynamicEntity(dynamicEntityVO, lstUsedDynamicEntities);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @param valuelistProviderVO
	 * @param lstUsedValuelistProvider
	 * @return the newly created valuelist provider
	 */
	public ValuelistProviderVO createValuelistProvider(ValuelistProviderVO valuelistProviderVO, List<String> lstUsedValuelistProvider) throws CommonBusinessException {
		try {
			return getDatasourceFacade().createValuelistProvider(valuelistProviderVO, lstUsedValuelistProvider);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @param recordGrantVO
	 * @param lstUsedRecordGrant
	 * @return the newly created RecordGrant
	 */
	public RecordGrantVO createRecordGrant(RecordGrantVO recordGrantVO, List<String> lstUsedRecordGrant) throws CommonBusinessException {
		try {
			return getDatasourceFacade().createRecordGrant(recordGrantVO, lstUsedRecordGrant);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @param sDatasourceXML
	 * @return the parameters of the datasource
	 */
	public List<DatasourceParameterVO> getParametersFromXML(String sDatasourceXML) throws CommonBusinessException {
		try {
			return getDatasourceFacade().getParameters(sDatasourceXML);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @param dsvo
	 * @return the newly created datasource
	 */
	public DatasourceVO createDatasource(DatasourceVO dsvo, List<String> lstUsedDatasources) throws CommonBusinessException {
		try {
			return getDatasourceFacade().create(dsvo, lstUsedDatasources);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @param datasourceVO
	 */
	public void removeDatasource(DatasourceVO datasourceVO) throws CommonBusinessException {
		try {
			getDatasourceFacade().remove(datasourceVO);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @param dynamicEntityVO
	 */
	public void removeDynamicEntity(DynamicEntityVO dynamicEntityVO) throws CommonBusinessException {
		try {
			getDatasourceFacade().removeDynamicEntity(dynamicEntityVO);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @param valuelistProviderVO
	 */
	public void removeValuelistProvider(ValuelistProviderVO valuelistProviderVO) throws CommonBusinessException {
		try {
			getDatasourceFacade().removeValuelistProvider(valuelistProviderVO);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @param recordGrantVO
	 */
	public void removeRecordGrant(RecordGrantVO recordGrantVO) throws CommonBusinessException {
		try {
			getDatasourceFacade().removeRecordGrant(recordGrantVO);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @param dsvo
	 * @return the modified datasource
	 */
	public DatasourceVO modifyDatasource(DatasourceVO dsvo, List<String> lstUsedDatasources) throws CommonBusinessException {
		try {
			return getDatasourceFacade().modify(dsvo, lstUsedDatasources);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @param dynamicEntityVO
	 * @param lstUsedDynamicEntities
	 * @return the modified dynamic entity
	 */
	public DynamicEntityVO modifyDynamicEntity(DynamicEntityVO dynamicEntityVO, List<String> lstUsedDynamicEntities) throws CommonBusinessException {
		try {
			return getDatasourceFacade().modifyDynamicEntity(dynamicEntityVO, lstUsedDynamicEntities);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @param valuelistProviderVO
	 * @param lstUsedValuelistProvider
	 * @return the modified valuelist provider
	 */
	public ValuelistProviderVO modifyValuelistProvider(ValuelistProviderVO valuelistProviderVO, List<String> lstUsedValuelistProvider) throws CommonBusinessException {
		try {
			return getDatasourceFacade().modifyValuelistProvider(valuelistProviderVO, lstUsedValuelistProvider);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @param recordGrantVO
	 * @param lstUsedRecordGrant
	 * @return the modified RecordGrant
	 */
	public RecordGrantVO modifyRecordGrant(RecordGrantVO recordGrantVO, List<String> lstUsedRecordGrant) throws CommonBusinessException {
		try {
			return getDatasourceFacade().modifyRecordGrant(recordGrantVO, lstUsedRecordGrant);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * execute a datasource by datasource id
	 * @param iDatasourceId
	 * @param mpParams
	 * @return
	 * @throws NuclosBusinessException
	 */
	public ResultVO executeQuery(Integer iDatasourceId, Map<String, Object> mpParams, Integer iMaxRowCount) throws CommonBusinessException {
		try {
			return getDatasourceFacade().executeQuery(iDatasourceId, mpParams, iMaxRowCount);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * execute a datasource by datasource xml
	 * @param sDatasourceXML
	 * @param mpParams
	 * @param iMaxRowCount
	 * @return
	 * @throws CommonBusinessException
	 */
	public ResultVO executeQuery(String sDatasourceXML, Map<String, Object> mpParams, Integer iMaxRowCount) throws CommonBusinessException {
		try {
			return getDatasourceFacade().executeQuery(sDatasourceXML, mpParams, iMaxRowCount);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @param sDatasourceXML
	 * @return the newly created sql string
	 */
	public String createSQL(String sDatasourceXML, Map<String, Object> mpParams) throws NuclosBusinessException {
		try {
			return getDatasourceFacade().createSQL(sDatasourceXML, mpParams);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public String createSQL(String sDatasourceXML) throws NuclosBusinessException {
		try {
			return getDatasourceFacade().createSQL(sDatasourceXML);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @return collection of all datasources the current user has access to
	 */
	public Collection<DatasourceVO> getAllDatasources() throws CommonPermissionException {
		try {
			return getDatasourceFacade().getDatasources();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @return collection of all datasources the current user is the owner og
	 */
	public Collection<DatasourceVO> getOwnDatasources() {
		try {
			return getDatasourceFacade().getDatasourcesForCurrentUser();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * Get datasource with the specified id.
	 * @param iId
	 * @return the datasource with the specified id.
	 */
	public DatasourceVO getDatasource(Integer iId) throws CommonBusinessException {
		try {
			return getDatasourceFacade().get(iId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 *
	 * @return
	 */
	public Schema getSchemaTables() {
		Schema result = null;
		try {
			result = getDatasourceFacade().getSchemaTables();
		}
		catch (RuntimeException e) {
			throw new NuclosFatalException(e);
		}
		return result;
	}

	/**
	 *
	 * @param table
	 * @return
	 */
	public Table getSchemaColumns(Table table) {
		try {
			return getDatasourceFacade().getSchemaColumns(table);
		}
		catch (RuntimeException e) {
			throw new NuclosFatalException(e);
		}
	}

	/**
	 * @return collection of all RecordGrantVOs
	 */
	public Collection<RecordGrantVO> getAllRecordGrant() throws CommonPermissionException {
		try {
			return getDatasourceFacade().getRecordGrant();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
    * get RecordGrant value object
    *
    * @param iRecordGrantId
    *                primary key of RecordGrant
    * @return RecordGrantVO
    */
   public RecordGrantVO getRecordGrant(Integer iRecordGrantId) throws CommonPermissionException {
   	try {
			return getDatasourceFacade().getRecordGrant(iRecordGrantId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
   }

	/**
	 * Get RecordGrant with the specified name.
	 * @param sRecordGrant
	 * @return RecordGrantVO
	 */
	public RecordGrantVO getRecordGrantByName(String sRecordGrant) throws CommonBusinessException {
		try {
			return getDatasourceFacade().getRecordGrant(sRecordGrant);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

}	// class DatasourceDelegate
