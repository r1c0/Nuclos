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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common.database.query.definition.Schema;
import org.nuclos.common.database.query.definition.Table;
import org.nuclos.common.querybuilder.DatasourceUtils;
import org.nuclos.common.querybuilder.NuclosDatasourceException;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoteException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.DatasourceCache;
import org.nuclos.server.common.DatasourceServerUtils;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.dblayer.DbAccess;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.dblayer.statements.DbPlainStatement;
import org.nuclos.server.masterdata.MasterDataWrapper;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.SchemaCache;
import org.nuclos.server.report.valueobject.ChartVO;
import org.nuclos.server.report.valueobject.DatasourceParameterVO;
import org.nuclos.server.report.valueobject.DatasourceVO;
import org.nuclos.server.report.valueobject.DynamicEntityVO;
import org.nuclos.server.report.valueobject.DynamicTasklistVO;
import org.nuclos.server.report.valueobject.RecordGrantVO;
import org.nuclos.server.report.valueobject.ResultColumnVO;
import org.nuclos.server.report.valueobject.ResultVO;
import org.nuclos.server.report.valueobject.ValuelistProviderVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Datasource facade encapsulating datasource management. <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Transactional(noRollbackFor = { Exception.class })
public class DatasourceFacadeBean extends NuclosFacadeBean implements DatasourceFacadeRemote {

	private static final Logger LOG = Logger.getLogger(DatasourceFacadeBean.class);

	private static enum DataSourceType {
		DYNAMICENTITY(NuclosEntity.DYNAMICENTITY, NuclosEntity.DYNAMICENTITYUSAGE, "dynamicEntity"), 
		VALUELISTPROVIDER(NuclosEntity.VALUELISTPROVIDER, NuclosEntity.VALUELISTPROVIDERUSAGE, "valuelistProvider"), 
		RECORDGRANT(NuclosEntity.RECORDGRANT, NuclosEntity.RECORDGRANTUSAGE, "recordGrant"), 
		DATASOURCE(NuclosEntity.DATASOURCE, NuclosEntity.DATASOURCEUSAGE, "datasource"), 
		CHART(NuclosEntity.CHART, NuclosEntity.CHARTUSAGE, "chart"), 
		DYNAMICTASKLIST(NuclosEntity.DYNAMICTASKLIST, NuclosEntity.DYNAMICTASKLISTUSAGE, "dynamictasklist");

		final NuclosEntity entity;
		final NuclosEntity entityUsage;
		final String fieldEntity;
		final String fieldEntityUsed;

		private DataSourceType(NuclosEntity entity, NuclosEntity entityUsage, String fieldEntity) {
			this.entity = entity;
			this.entityUsage = entityUsage;
			this.fieldEntity = fieldEntity;
			this.fieldEntityUsed = fieldEntity + "Used";
		}

		public DatasourceVO wrap(MasterDataVO deVO, String userName) {
			switch (this) {
			case DYNAMICENTITY:
				return MasterDataWrapper.getDynamicEntityVO(deVO);
			case VALUELISTPROVIDER:
				return MasterDataWrapper.getValuelistProviderVO(deVO);
			case RECORDGRANT:
				return MasterDataWrapper.getRecordGrantVO(deVO);
			case DYNAMICTASKLIST:
				return MasterDataWrapper.getDynamicTasklistVO(deVO);
			case CHART:
					return MasterDataWrapper.getChartVO(deVO);
			default:
				return MasterDataWrapper.getDatasourceVO(deVO, userName);
			}
		}

		public MasterDataVO unwrap(DatasourceVO dsVO) {
			return MasterDataWrapper.wrapDatasourceVO(dsVO);
		}

		public static DataSourceType getFromDatasourceVO(DatasourceVO datasourceVO) {
			if (datasourceVO instanceof DynamicEntityVO) {
				return DataSourceType.DYNAMICENTITY;
			}
			else if (datasourceVO instanceof ValuelistProviderVO) {
				return DataSourceType.VALUELISTPROVIDER;
			}
			else if (datasourceVO instanceof RecordGrantVO) {
				return DataSourceType.RECORDGRANT;
			}
			else if (datasourceVO instanceof DynamicTasklistVO) {
				return DataSourceType.DYNAMICTASKLIST;
			}
			else if (datasourceVO instanceof ChartVO) {
				return DataSourceType.CHART;
			}
			else {
				return DataSourceType.DATASOURCE;
			}
		}
	}

	private DatasourceServerUtils utils;

	private MasterDataFacadeLocal masterDataFacade;

	public DatasourceFacadeBean() {
	}

	@Autowired
	void setDatasourceServerUtils(DatasourceServerUtils utils) {
		this.utils = utils;
	}

	@Autowired
	final void setMasterDataFacade(MasterDataFacadeLocal masterDataFacade) {
		this.masterDataFacade = masterDataFacade;
	}

	private final MasterDataFacadeLocal getMasterDataFacade() {
		return masterDataFacade;
	}

	/**
	 * get all datasources
	 *
	 * @return set of datasources
	 * @throws CommonPermissionException
	 */
	public Collection<DatasourceVO> getDatasources() throws CommonPermissionException {
		this.checkReadAllowed(NuclosEntity.REPORT, NuclosEntity.DATASOURCE);
		return DatasourceCache.getInstance().getAllDatasources(getCurrentUserName());
	}

	/**
	 * get all datasources
	 *
	 * @return set of datasources
	 */
	public Collection<DatasourceVO> getDatasourcesForCurrentUser() {
		return DatasourceCache.getInstance().getDatasourcesByCreator(getCurrentUserName());
	}

	/**
	 * get datasource value object
	 *
	 * @param iId
	 *            primary key of datasource
	 * @return datasource value object
	 */
	@RolesAllowed("Login")
	public DatasourceVO get(Integer iId) throws CommonFinderException, CommonPermissionException {
		return DatasourceCache.getInstance().getDatasourcesById(iId, getCurrentUserName());

		/*
		 * todo how should we handle permissions here? - It is necessary, that
		 * // all users have the right to execute inline datasources if
		 * (result.getPermission() == DatasourceVO.PERMISSION_NONE) { throw new
		 * CommonPermissionException(); } return result;
		 */
	}

	/**
	 * get datasource value object
	 *
	 * @param sDatasourceName
	 *            name of datasource
	 * @return datasource value object
	 */
	@RolesAllowed("Login")
	public DatasourceVO get(String sDatasourceName) throws CommonFinderException, CommonPermissionException {
		return DatasourceCache.getInstance().getDatasourceByName(sDatasourceName);
	}

	/**
	 * get valuelist provider value object
	 *
	 * @param sValuelistProvider
	 *            name of valuelist provider
	 * @return valuelist provider value object
	 */
	@RolesAllowed("Login")
	public ValuelistProviderVO getValuelistProvider(String sValuelistProvider) throws CommonFinderException, CommonPermissionException {
		return DatasourceCache.getInstance().getValuelistProviderByName(sValuelistProvider);
	}

	/**
	 * get dynamic entity value object
	 *
	 * @param sDynamicEntity
	 *            name of dynamic entity 
	 * @return dynamic entity value object
	 */
	@RolesAllowed("Login")
	public DynamicEntityVO getDynamicEntity(String sDynamicEntity) throws CommonFinderException, CommonPermissionException {
		return DatasourceCache.getInstance().getDynamicEntityByName(sDynamicEntity);
	}

	/**
	 * get chart value object
	 * 
	 * @param sChart
	 *            name of chart
	 * @return chart value object
	 */
	@RolesAllowed("Login")
	public ChartVO getChart(String sChart) throws CommonFinderException, CommonPermissionException {
		return DatasourceCache.getInstance().getChartByName(sChart);
	}

	/**
	 * get all charts
	 * 
	 * @return set of ChartVO
	 * @throws CommonPermissionException
	 */
	@RolesAllowed("Login")
	public Collection<ChartVO> getCharts() {
		// this.checkReadAllowed(ENTITY_NAME_CHARTENTITY);
		return DatasourceCache.getInstance().getAllCharts();
	}

	/**
	 * get chart value object
	 * 
	 * @param iChartId
	 *            primary key of chart
	 * @return ChartVO
	 */
	@RolesAllowed("Login")
	public ChartVO getChart(Integer iChartId) throws CommonPermissionException {
		// this.checkReadAllowed(ENTITY_NAME_CHARTENTITY);
		return DatasourceCache.getInstance().getChart(iChartId);
	}

	/**
	 * get a Datasource by id regardless of permisssions
	 *
	 * @param iDatasourceId
	 * @return
	 * @throws CommonPermissionException
	 */
	@RolesAllowed("Login")
	public DatasourceVO getDatasourceById(Integer iDatasourceId) {
		return DatasourceCache.getInstance().get(iDatasourceId);
	}

	/**
	 * create new datasource
	 *
	 * @param datasourcevo
	 *            value object
	 * @return new datasource
	 */
	public DatasourceVO create(DatasourceVO datasourcevo, DependantMasterDataMap dependants, List<String> lstUsedDatasources) throws CommonCreateException, CommonValidationException, NuclosBusinessRuleException, CommonPermissionException {
		DataSourceType type = DataSourceType.getFromDatasourceVO(datasourcevo);
		String entity = type.entity.getEntityName();
		this.checkWriteAllowed(entity);

		datasourcevo.validate();
		updateValidFlag(datasourcevo);

		if (NuclosEntity.RECORDGRANT.getEntityName().equals(entity)) {
			RecordGrantVO rg = (RecordGrantVO) datasourcevo;
			try {
				if (rg.getValid()) {
					DatasourceUtils.validateRecordGrantSQL(rg.getEntity(), createSQL(rg.getSource()));
				}
			}
			catch (NuclosDatasourceException e1) {
				throw new CommonFatalException(e1);
			}
		}

		if (NuclosEntity.DYNAMICENTITY.getEntityName().equals(entity)) {
			processChangingDynamicEntity((DynamicEntityVO) datasourcevo, null, true);
		}

		if (NuclosEntity.CHART.getEntityName().equals(entity)) {
			processChangingChartEntity((ChartVO) datasourcevo, null, true);
		}
		
		MasterDataVO mdVO = getMasterDataFacade().create(entity, type.unwrap(datasourcevo), null);
		getMasterDataFacade().notifyClients(entity);

		DatasourceVO dbDataSourceVO = MasterDataWrapper.getDatasourceVO(mdVO, getCurrentUserName());

		try {
			replaceUsedDatasourceList(dbDataSourceVO, lstUsedDatasources);
		}
		catch (CommonFinderException e) {
			throw new CommonFatalException(e);
		}
		catch (CommonRemoveException e) {
			throw new CommonFatalException(e);
		}
		catch (CommonStaleVersionException e) {
			throw new CommonFatalException(e);
		}

		invalidateCaches(type);

		return type.wrap(mdVO, getCurrentUserName());
	}

	/**
	 * modify an existing datasource without usages
	 *
	 * @param datasourcevo
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 * @throws CommonStaleVersionException
	 * @throws CommonValidationException
	 */
	public void modify(DatasourceVO datasourcevo) throws CommonFinderException, CommonPermissionException, CommonStaleVersionException, CommonValidationException, NuclosBusinessRuleException {
		modify(datasourcevo, null, null, false);
	}

	/**
	 * modify an existing datasource
	 *
	 * @param datasourcevo
	 *            value object
	 * @return modified datasource
	 */
	public DatasourceVO modify(DatasourceVO datasourcevo, DependantMasterDataMap dependants, List<String> lstUsedDatasources) throws CommonFinderException, CommonPermissionException, CommonStaleVersionException, CommonValidationException,
			NuclosBusinessRuleException, CommonRemoteException {

		return modify(datasourcevo, dependants, lstUsedDatasources, true);
	}

	private DatasourceVO modify(DatasourceVO datasourcevo, DependantMasterDataMap dependants, List<String> lstUsedDatasources, boolean modifyUsedDatasources) throws CommonFinderException, CommonPermissionException,
			CommonStaleVersionException, CommonValidationException, NuclosBusinessRuleException, CommonRemoteException {
		DataSourceType type = DataSourceType.getFromDatasourceVO(datasourcevo);
		String entity = type.entity.getEntityName();
		this.checkWriteAllowed(entity);

		datasourcevo.validate();
		updateValidFlag(datasourcevo);

		try {
			validateUniqueConstraint(datasourcevo);

			MasterDataVO dsAsMd = getMasterDataFacade().get(entity, datasourcevo.getId());

			if (NuclosEntity.DATASOURCE.getEntityName().equals(entity)) {
				if (DatasourceCache.getInstance().getPermission(dsAsMd.getIntId(), getCurrentUserName()) != DatasourceVO.PERMISSION_READWRITE) {
					throw new CommonPermissionException();
				}
			}

			if (NuclosEntity.DYNAMICENTITY.getEntityName().equals(entity)) {
				processChangingDynamicEntity((DynamicEntityVO) datasourcevo, (DynamicEntityVO) type.wrap(dsAsMd, getCurrentUserName()), true);
			}

			if (NuclosEntity.CHART.getEntityName().equals(entity)) {
				processChangingChartEntity((ChartVO) datasourcevo, (ChartVO) type.wrap(dsAsMd, getCurrentUserName()), true);
			}
			
			if (dsAsMd.getVersion() != datasourcevo.getVersion()) {
				throw new CommonStaleVersionException(entity, datasourcevo.toString(), dsAsMd.toString());
			}
			getMasterDataFacade().modify(entity, type.unwrap(datasourcevo), null);

			if (modifyUsedDatasources) {
				// store the list of used datasources
				replaceUsedDatasourceList(datasourcevo, lstUsedDatasources);
			}

			invalidateCaches(type);

			dsAsMd = getMasterDataFacade().get(entity, datasourcevo.getId());
			return type.wrap(dsAsMd, getCurrentUserName());
		}
		catch (CommonRemoveException ex) {
			throw new NuclosBusinessRuleException(ex);
		}
		catch (CommonCreateException ex) {
			throw new NuclosFatalException(ex);
		}
		catch (CommonFatalException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	/**
	 *
	 * @param newDEs
	 *            (dynamic entities of this list would be created)
	 * @param oldDEs
	 *            (dynamic entities of this list would be deleted)
	 */
	public void processChangingDynamicEntities(Collection<DynamicEntityVO> newDEs, Collection<DynamicEntityVO> oldDEs, boolean bExecute, List<String> script) {
		for (DynamicEntityVO oldDEVO : oldDEs) {
			try {
				this.processChangingDynamicEntity(null, oldDEVO, false, false, bExecute, script);
			}
			catch (CommonValidationException e) {
				// no validation here
			}
		}
		for (DynamicEntityVO newDEVO : newDEs) {
			try {
				this.processChangingDynamicEntity(newDEVO, null, false, false, bExecute, script);
			}
			catch (CommonValidationException e) {
				// no validation here
			}
		}
		// revalidate meta cache
		MasterDataMetaCache.getInstance().revalidate();
	}

	/**
	 * revalidates MasterDataMetaCache after process
	 *
	 * @param newDEVO
	 * @param oldDEVO
	 * @param validate
	 * @throws CommonValidationException
	 *             (only if validate is true)
	 */
	private void processChangingDynamicEntity(DynamicEntityVO newDEVO, DynamicEntityVO oldDEVO, boolean validate) throws CommonValidationException {
		this.processChangingDynamicEntity(newDEVO, oldDEVO, validate, true, true, new ArrayList<String>());
	}

	/**
	 *
	 * @param newDEVO
	 * @param oldDEVO
	 * @param validate
	 * @param revalidateMasterDataMetaCache
	 * @throws CommonValidationException
	 *             (only if validate is true)
	 */
	private void processChangingDynamicEntity(DynamicEntityVO newDEVO, DynamicEntityVO oldDEVO, boolean validate, boolean revalidateMasterDataMetaCache, boolean bExecute, List<String> script) throws CommonValidationException {
		String sqlSelect = null;
		if (newDEVO != null) {
			if (validate) {
				try {
					LOG.debug("validate dynamic entity name \"" + newDEVO.getName() + "\"");
					DatasourceUtils.validateDynEntityName(newDEVO.getName());
					sqlSelect = createSQL(newDEVO.getSource());
					LOG.debug("validate dynamic entity sql <SQL>" + sqlSelect + "</SQL>");
					DatasourceUtils.validateDynEntitySQL(sqlSelect);
				}
				catch (NuclosDatasourceException e) {
					throw new CommonFatalException(e);
				}
			}
		}

		DbAccess dbAccess = dataBaseHelper.getDbAccess();
		if (oldDEVO != null) {
			String sqlToExecute = null;
			try {
				sqlToExecute = "drop view " + MasterDataMetaVO.DYNAMIC_ENTITY_VIEW_PREFIX + oldDEVO.getName();
				script.add(sqlToExecute);
				if (bExecute)
					dbAccess.execute(new DbPlainStatement(sqlToExecute));
				LOG.info("deleted dynamic entity <SQL>" + sqlToExecute + "</SQL>");
			}
			catch (DbException e) {
				LOG.error("could not deleted dynamic entity <SQL>" + sqlToExecute + "</SQL> ERROR: " + e.getMessage());
			}
		}
		if (newDEVO != null) {
			String sqlToExecute = null;
			try {
				LOG.info("creating dynamic entity " + newDEVO.getName() + "...");
				sqlToExecute = "create view " + MasterDataMetaVO.DYNAMIC_ENTITY_VIEW_PREFIX + newDEVO.getName() + " as " + (sqlSelect != null ? sqlSelect : createSQL(newDEVO.getSource()));
				script.add(sqlToExecute);
				if (bExecute)
					dbAccess.execute(new DbPlainStatement(sqlToExecute));
				LOG.info("created dynamic entity <SQL>" + sqlToExecute + "</SQL>");
			}
			catch (Exception e) {
				LOG.error("could not create dynamic entity <SQL>" + sqlToExecute + "</SQL> ERROR: " + e.getMessage());
			}
		}
		if (revalidateMasterDataMetaCache)
			MasterDataMetaCache.getInstance().revalidate();
	}
	/**
	 * 
	 * @param newDEs
	 *            (chart entities of this list would be created)
	 * @param oldDEs
	 *            (chart entities of this list would be deleted)
	 */
	public void processChangingChartEntities(Collection<ChartVO> newDEs, Collection<ChartVO> oldDEs, boolean bExecute, List<String> script) {
		for (ChartVO oldDEVO : oldDEs) {
			try {
				this.processChangingChartEntity(null, oldDEVO, false, false, bExecute, script);
			}
			catch (CommonValidationException e) {
				// no validation here
			}
		}
		for (ChartVO newDEVO : newDEs) {
			try {
				this.processChangingChartEntity(newDEVO, null, false, false, bExecute, script);
			}
			catch (CommonValidationException e) {
				// no validation here
			}
		}
		// revalidate meta cache
		MasterDataMetaCache.getInstance().revalidate();
	}

	/**
	 * revalidates MasterDataMetaCache after process
	 * 
	 * @param newDEVO
	 * @param oldDEVO
	 * @param validate
	 * @throws CommonValidationException
	 *             (only if validate is true)
	 */
	private void processChangingChartEntity(ChartVO newDEVO, ChartVO oldDEVO, boolean validate) throws CommonValidationException {
		this.processChangingChartEntity(newDEVO, oldDEVO, validate, true, true, new ArrayList<String>());
	}

	/**
	 * 
	 * @param newDEVO
	 * @param oldDEVO
	 * @param validate
	 * @param revalidateMasterDataMetaCache
	 * @throws CommonValidationException
	 *             (only if validate is true)
	 */
	private void processChangingChartEntity(ChartVO newDEVO, ChartVO oldDEVO, boolean validate, boolean revalidateMasterDataMetaCache, boolean bExecute, List<String> script)
			throws CommonValidationException {
		String sqlSelect = null;
		if (newDEVO != null) {
			if (validate) {
				try {
					LOG.debug("validate chart entity name \"" + newDEVO.getName() + "\"");
					DatasourceUtils.validateChartEntityName(newDEVO.getName());
					sqlSelect = createSQL(newDEVO.getSource());
					LOG.debug("validate chart entity sql <SQL>" + sqlSelect + "</SQL>");
					DatasourceUtils.validateChartEntitySQL(sqlSelect);
					DatasourceUtils.validateChartEntitySQLWithParameters(createSQL(newDEVO.getSource(), getTestParameters(newDEVO.getSource())));
				}
				catch (NuclosDatasourceException e) {
					throw new CommonFatalException(e);
				}
			}
		}

		DbAccess dbAccess = dataBaseHelper.getDbAccess();
		if (oldDEVO != null) {
			String sqlToExecute = null;
			try {
				sqlToExecute = "drop view " + MasterDataMetaVO.CHART_ENTITY_VIEW_PREFIX + oldDEVO.getName();
				script.add(sqlToExecute);
				if (bExecute)
					dbAccess.execute(new DbPlainStatement(sqlToExecute));
				LOG.info("deleted chart entity <SQL>" + sqlToExecute + "</SQL>");
			}
			catch (DbException e) {
				LOG.error("could not deleted chart entity <SQL>" + sqlToExecute + "</SQL> ERROR: " + e.getMessage());
			}
		}
		if (newDEVO != null) {
			String sqlToExecute = null;
			try {
				LOG.info("creating chart entity " + newDEVO.getName() + "...");
				sqlToExecute = "create view " + MasterDataMetaVO.CHART_ENTITY_VIEW_PREFIX + newDEVO.getName() + " as " + createSQL(newDEVO.getSource(), getTestParameters(newDEVO.getSource()));
				script.add(sqlToExecute);
				if (bExecute)
					dbAccess.execute(new DbPlainStatement(sqlToExecute));
				LOG.info("created dynamic entity <SQL>" + sqlToExecute + "</SQL>");
			}
			catch (Exception e) {
				LOG.error("could not create chart entity <SQL>" + sqlToExecute + "</SQL> ERROR: " + e.getMessage());
			}
		}
		if (revalidateMasterDataMetaCache)
			MasterDataMetaCache.getInstance().revalidate();
	}

	private void validateUniqueConstraint(DatasourceVO datasourcevo) throws CommonValidationException {
		try {
			// final DatasourceLocal datasource =
			// datasourceHome.findByName(datasourcevo.getDatasource());
			DatasourceVO foundDatasourceVO = this.get(datasourcevo.getName());
			// if(datasourcevo.getId().intValue() !=
			// datasource.getId().intValue()){
			if (foundDatasourceVO != null && datasourcevo.getId().intValue() != foundDatasourceVO.getId().intValue()) {
				throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("validation.unique.constraint", "Name", "Data source"));
			}
		}
		catch (CommonFinderException e) {
			// No element found -> validation O.K.
		}
		catch (CommonPermissionException e) {
			// No element found -> validation O.K.
		}
	}

	/**
	 * @param datasourceVO
	 * @param referencedDatasources
	 * @throws CommonPermissionException
	 * @throws CommonFinderException
	 * @throws CommonStaleVersionException
	 * @throws CommonRemoveException
	 * @throws NuclosBusinessRuleException
	 * @throws CommonCreateException
	 */
	private void replaceUsedDatasourceList(DatasourceVO datasourceVO, List<String> referencedDatasources) throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException, CommonRemoveException,
			CommonStaleVersionException, CommonCreateException {
		DataSourceType datasourceType = DataSourceType.getFromDatasourceVO(datasourceVO);
		MasterDataMetaCache metaCache = MasterDataMetaCache.getInstance();
		MasterDataMetaVO usageMeta = metaCache.getMetaData(datasourceType.entityUsage);
		String usageEntityName = datasourceType.entityUsage.getEntityName();

		// 1. remove all entries for this id:
		CollectableComparison condUsage = SearchConditionUtils.newMDReferenceComparison(usageMeta, datasourceType.fieldEntity, datasourceVO.getId());
		for (MasterDataVO mdVO : getMasterDataFacade().getMasterData(usageEntityName, condUsage, true))
			getMasterDataFacade().remove(usageEntityName, mdVO, false);

		// 2. insert the new entries:
		for (String dataSourceName : referencedDatasources) {
			CollectableComparison condDatasource = SearchConditionUtils.newMDComparison(metaCache.getMetaData(datasourceType.entity.getEntityName()), "name", org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator.EQUAL,
					dataSourceName);
			Collection<MasterDataVO> usedDatasources = getMasterDataFacade().getMasterData(datasourceType.entity.getEntityName(), condDatasource, true);

			for (MasterDataVO usedDatasource : usedDatasources) {
				MasterDataVO newEntryVO = new MasterDataVO(usageMeta, true);
				newEntryVO.setField(datasourceType.fieldEntity + "Id", datasourceVO.getId());
				newEntryVO.setField(datasourceType.fieldEntityUsed + "Id", usedDatasource.getId());

				getMasterDataFacade().create(usageEntityName, newEntryVO, null);
			}
		}
	}

	/**
	 * get a list of DatasourceVO which uses the datasource with the given id
	 *
	 * ONLY for datasources, NOT for dynamic entities and valuelist provider
	 *
	 * @see also <code>getUsagesForDatasource(DatasourceVO datasourceVO)</code>
	 *
	 * @param iDatasourceId
	 * @return
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 */
	@RolesAllowed("Login")
	public List<DatasourceVO> getUsagesForDatasource(final Integer iDatasourceId) throws CommonFinderException, CommonPermissionException {
		return getUsagesForDatasource(get(iDatasourceId));
	}

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
	public List<DatasourceVO> getUsagesForDatasource(DatasourceVO datasourceVO) throws CommonFinderException, CommonPermissionException {
		final DataSourceType datasourceType = DataSourceType.getFromDatasourceVO(datasourceVO);

		List<DatasourceVO> result = new ArrayList<DatasourceVO>();

		CollectableComparison cond = SearchConditionUtils.newMDReferenceComparison(MasterDataMetaCache.getInstance().getMetaData(datasourceType.entityUsage), datasourceType.fieldEntityUsed, datasourceVO.getId());

		for (MasterDataVO usageVO : getMasterDataFacade().getMasterData(datasourceType.entityUsage.getEntityName(), cond, true)) {
			MasterDataVO deVO = getMasterDataFacade().get(datasourceType.entity.getEntityName(), usageVO.getField(datasourceType.fieldEntityUsed + "Id"));
			if (!datasourceVO.getId().equals(deVO.getId())) {
				result.add(datasourceType.wrap(deVO, getCurrentUserName()));
			}
		}

		return result;
	}

	/**
	 * get a list of DatasourceCVO which are used by the datasource with the
	 * given id
	 *
	 * @param iDatasourceId
	 * @return
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 */
	public List<DatasourceVO> getUsingByForDatasource(final Integer iDatasourceId) throws CommonFinderException, CommonPermissionException {

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("T_UD_DATASOURCEUSAGE").alias(SystemFields.BASE_ALIAS);
		query.multiselect(t.baseColumn("INTID", Integer.class), t.baseColumn("INTID_T_UD_DATASOURCE", Integer.class), t.baseColumn("INTID_T_UD_DATASOURCE_USED", Integer.class));
		query.where(builder.equal(t.baseColumn("INTID_T_UD_DATASOURCE", Integer.class), iDatasourceId));

		List<DatasourceVO> result = new ArrayList<DatasourceVO>();
		for (DbTuple tuple : dataBaseHelper.getDbAccess().executeQuery(query)) {
			try {
				Integer dataSourceUsed = tuple.get(2, Integer.class);
				MasterDataVO mdVO = getMasterDataFacade().get(NuclosEntity.DATASOURCE.getEntityName(), dataSourceUsed);
				result.add(MasterDataWrapper.getDatasourceVO(mdVO, getCurrentUserName()));
			}
			catch (CommonFinderException ex) {
				throw new NuclosFatalException(ex);
			}
			catch (CommonPermissionException ex) {
				throw new NuclosFatalException(ex);
			}
		}
		return result;
	}

	/**
	 * delete an existing datasource
	 *
	 * @param datasourcevo
	 *            value object
	 */
	public void remove(DatasourceVO datasourcevo) throws CommonFinderException, CommonRemoveException, CommonPermissionException, CommonStaleVersionException, NuclosBusinessRuleException {
		DataSourceType type = DataSourceType.getFromDatasourceVO(datasourcevo);
		String entity = type.entity.getEntityName();
		this.checkDeleteAllowed(entity);

		MasterDataVO dsAsMd = getMasterDataFacade().get(entity, datasourcevo.getId());

		if (NuclosEntity.DATASOURCE.getEntityName().equals(entity)) {
			if (DatasourceCache.getInstance().getPermission(dsAsMd.getIntId(), getCurrentUserName()) != DatasourceVO.PERMISSION_READWRITE) {
				throw new CommonPermissionException();
			}
		}

		if (NuclosEntity.DYNAMICENTITY.getEntityName().equals(entity)) {
			try {
				processChangingDynamicEntity(null, (DynamicEntityVO) datasourcevo, false);
			}
			catch (CommonValidationException e) {
				// no validation here
				LOG.info("removeDynamicEntity: " + e);
			}
		}

		if (NuclosEntity.CHART.getEntityName().equals(entity)) {
			try {
				processChangingChartEntity(null, (ChartVO) datasourcevo, false);
			}
			catch (CommonValidationException e) {
				// no validation here
				LOG.info("removeDynamicEntity: " + e);
			}
		}

		if (dsAsMd.getVersion() != datasourcevo.getVersion()) {
			throw new CommonStaleVersionException(entity, dsAsMd.toString(), datasourcevo.toString());
		}

		getMasterDataFacade().remove(entity, type.unwrap(datasourcevo), false);
		getMasterDataFacade().notifyClients(entity);

		try {
			replaceUsedDatasourceList(datasourcevo, Collections.<String> emptyList());
		}
		catch (CommonCreateException e) {
			throw new CommonFatalException(e);
		}

		invalidateCaches(type);
	}

	/**
	 * @param datasourcevo
	 */
	private void updateValidFlag(DatasourceVO datasourcevo) {
		try {
			this.validateSqlFromXML(datasourcevo.getSource());
			datasourcevo.setValid(Boolean.TRUE);
		}
		catch (CommonValidationException e) {
			LOG.info("updateValidFlag: " + e);
			datasourcevo.setValid(Boolean.FALSE);
		}
		catch (NuclosDatasourceException e) {
			LOG.info("updateValidFlag: " + e);
			datasourcevo.setValid(Boolean.FALSE);
		}
	}

	/**
	 * Retrieve the parameters a datasource accepts.
	 *
	 * @param sDatasourceXML
	 * @return
	 * @throws NuclosFatalException
	 * @throws NuclosDatasourceException
	 */
	@RolesAllowed("Login")
	public List<DatasourceParameterVO> getParameters(String sDatasourceXML) throws NuclosFatalException, NuclosDatasourceException {
		return utils.getParameters(sDatasourceXML);
	}

	/**
	 * Retrieve the parameters a datasource accepts.
	 *
	 * @param iDatasourceId
	 * @return
	 * @throws NuclosFatalException
	 * @throws NuclosDatasourceException
	 */
	@RolesAllowed("Login")
	public List<DatasourceParameterVO> getParameters(Integer iDatasourceId) throws NuclosFatalException, NuclosDatasourceException {
		return utils.getParameters(iDatasourceId);
	}

	/**
	 * validate the given DatasourceXML
	 *
	 * @param sDatasourceXML
	 * @throws CommonValidationException
	 * @throws NuclosReportException
	 */
	@RolesAllowed("Login")
	public void validateSqlFromXML(String sDatasourceXML) throws CommonValidationException, NuclosDatasourceException {
		final String sSql = this.createSQL(sDatasourceXML, this.getTestParameters(sDatasourceXML));

		this.validateSql(sSql);
	}

	/**
	 * validate the given SQL
	 *
	 * @param sql
	 * @throws CommonValidationException
	 * @throws NuclosReportException
	 */
	@RolesAllowed("Login")
	public void validateSql(String sql) throws CommonValidationException, NuclosDatasourceException {
		try {
			dataBaseHelper.getDbAccess().checkSyntax(sql);
		}
		catch (DbException e) {
			throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("datasource.error.invalid.statement", e.getMessage()), e);// "Die Abfrage ist ung\u00ef\u00bf\u00bdltig.\n"
		}
	}

	/**
	 * get sql string for datasource definition
	 *
	 * @param iDatasourceId
	 *            id of datasource
	 * @return string containing sql
	 */
	public String createSQL(Integer iDatasourceId, Map<String, Object> mpParams) throws NuclosDatasourceException {
		return utils.createSQL(iDatasourceId, mpParams);
	}

	/**
	 * get sql string for datasource definition without parameter definition
	 *
	 * @param sDatasourceXML
	 *            xml of datasource
	 * @return string containing sql
	 */
	@RolesAllowed("Login")
	public String createSQL(String sDatasourceXML) throws NuclosDatasourceException {
		return utils.createSQL(sDatasourceXML);
	}

	/**
	 * get sql string for datasource definition
	 *
	 * @param sDatasourceXML
	 *            xml of datasource
	 * @return string containing sql
	 */
	@RolesAllowed("Login")
	public String createSQL(String sDatasourceXML, Map<String, Object> mpParams) throws NuclosDatasourceException {
		return utils.createSQL(sDatasourceXML, mpParams);
	}

	/**
	 * get sql string for report execution. check that this method is only
	 * called by the local interface as there is no authorization applied.
	 *
	 * @param iDatasourceId
	 *            id of datasource
	 * @return string containing sql
	 */
	public String createSQLForReportExecution(String name, Map<String, Object> mpParams) throws NuclosDatasourceException {
		if (isCalledRemotely()) {
			// just to ensure it won't be used in remote interface
			throw new NuclosFatalException("Invalid remote call");
		}

		return utils.createSQLForReportExecution(name, mpParams);
	}

	/**
	 * set test values for every parameter for sysntax check
	 *
	 * @param sDatasourceXML
	 * @return Map<String sName, String sValue>
	 */
	private Map<String, Object> getTestParameters(final String sDatasourceXML) {
		final Map<String, Object> result = new HashMap<String, Object>();

		final List<DatasourceParameterVO> lstParams;
		try {
			lstParams = this.getParameters(sDatasourceXML);
		}
		catch (NuclosDatasourceException e) {
			// No parameters defined?
			LOG.info("getTestParameters: " + e);
			return result;
		}

		for (DatasourceParameterVO paramvo : lstParams) {
			final String sValue;
			if ("java.lang.String".equals(paramvo.getDatatype())) {
				sValue = "abc";
			}
			else if ("java.util.Date".equals(paramvo.getDatatype())) {
				sValue = "01.01.2000";
			}
			else {
				sValue = "123434";
			}
			result.put(paramvo.getParameter(), sValue);
			if (paramvo.getValueListProvider() != null) {
				result.put(paramvo.getParameter() + "Id", "123434");
			}
		}

		result.put("username", getCurrentUserName());

		return result;
	}

	/**
	 * invalidate datasource cache
	 */
	@RolesAllowed("Login")
	public void invalidateCache() {
		DatasourceCache.getInstance().invalidate();
	}

	/**
	 * get all DynamicEntities
	 *
	 * @return set of DynamicEntityVO
	 * @throws CommonPermissionException
	 */
	@RolesAllowed("Login")
	public Collection<DynamicEntityVO> getDynamicEntities() {
		// this.checkReadAllowed(ENTITY_NAME_DYNAMICENTITY);
		return DatasourceCache.getInstance().getAllDynamicEntities();
	}

	/**
	 * get dynamic entity value object
	 *
	 * @param iDynamicEntityId
	 *            primary key of dynamic entity
	 * @return DynamicEntityVO
	 */
	@RolesAllowed("Login")
	public DynamicEntityVO getDynamicEntity(Integer iDynamicEntityId) throws CommonPermissionException {
		// this.checkReadAllowed(ENTITY_NAME_DYNAMICENTITY);
		return DatasourceCache.getInstance().getDynamicEntity(iDynamicEntityId);
	}

	/**
	 * get all ValuelistProvider
	 *
	 * @return set of ValuelistProviderVO
	 * @throws CommonPermissionException
	 */
	@RolesAllowed("Login")
	public Collection<ValuelistProviderVO> getValuelistProvider() throws CommonPermissionException {
		// this.checkReadAllowed(ENTITY_NAME_VALUELISTPROVIDER);
		return DatasourceCache.getInstance().getAllValuelistProvider();
	}

	/**
	 * get valuelist provider value object
	 *
	 * @param iValuelistProviderId
	 *            primary key of valuelist provider
	 * @return ValuelistProviderVO
	 */
	@RolesAllowed("Login")
	public ValuelistProviderVO getValuelistProvider(Integer iValuelistProviderId) throws CommonPermissionException {
		// this.checkReadAllowed(ENTITY_NAME_VALUELISTPROVIDER);
		return DatasourceCache.getInstance().getValuelistProvider(iValuelistProviderId);
	}

	/**
	 * get all RecordGrant
	 *
	 * @return set of RecordGrantVO
	 * @throws CommonPermissionException
	 */
	@RolesAllowed("Login")
	public Collection<RecordGrantVO> getRecordGrant() throws CommonPermissionException {
		// this.checkReadAllowed(ENTITY_NAME_VALUELISTPROVIDER);
		return DatasourceCache.getInstance().getAllRecordGrant();
	}

	/**
	 * get RecordGrant value object
	 *
	 * @param iRecordGrantId
	 *            primary key of RecordGrant
	 * @return RecordGrantVO
	 */
	@RolesAllowed("Login")
	public RecordGrantVO getRecordGrant(Integer iRecordGrantId) throws CommonPermissionException {
		// this.checkReadAllowed(ENTITY_NAME_VALUELISTPROVIDER);
		return DatasourceCache.getInstance().getRecordGrant(iRecordGrantId);
	}

	/**
	 * get RecordGrant value object
	 *
	 * @param sRecordGrant
	 *            name of RecordGrant
	 * @return RecordGrant value object
	 */
	@RolesAllowed("Login")
	public RecordGrantVO getRecordGrant(String sRecordGrant) throws CommonFinderException, CommonPermissionException {
		return DatasourceCache.getInstance().getRecordGrantByName(sRecordGrant);
	}

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
	public ResultVO executeQuery(Integer iDatasourceId, Map<String, Object> mpParams, Integer iMaxRowCount) throws NuclosDatasourceException, CommonFinderException {
		final ResultVO result;
		DatasourceFacadeLocal facade = ServerServiceLocator.getInstance().getFacade(DatasourceFacadeLocal.class);
		result = executeQuery(facade.getDatasourceById(iDatasourceId).getSource(), mpParams, iMaxRowCount);

		return result;
	}

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
	public ResultVO executeQuery(String sDatasourceXML, Map<String, Object> mpParams, Integer iMaxRowCount) throws CommonFinderException, NuclosDatasourceException {
		final String sQuery = createSQL(sDatasourceXML, mpParams);

		return dataBaseHelper.getDbAccess().executePlainQueryAsResultVO(sQuery, iMaxRowCount == null ? -1 : iMaxRowCount);
	}
	
	public Schema getSchemaTables() {
		// return
		// SchemaCache.getSchema(ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_NUCLOS_SCHEMA));
		return SchemaCache.getInstance().getCurrentSchema();
	}

	/**
	 * @throws CommonPermissionException
	 */
	public Table getSchemaColumns(Table table) {
		// SchemaCache.getColumns(ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_NUCLOS_SCHEMA),
		// table);
		// SchemaCache.getConstraints(ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_NUCLOS_SCHEMA),
		// table);
		SchemaCache.getInstance().fillTableColumnsAndConstraints(table);
		return table;
	}

	public String createSQLOriginalParameter(String sDatasourceXML) throws NuclosDatasourceException {
		return utils.createSQLOriginalParameter(sDatasourceXML);
	}

	@RolesAllowed("Login")
	public DynamicTasklistVO getDynamicTasklist(Integer id) throws CommonPermissionException {
		return DatasourceCache.getInstance().getDynamicTasklist(id);
	}

	private void invalidateCaches(DataSourceType type) {
		DatasourceCache.getInstance().invalidate();
		SchemaCache.getInstance().invalidate(true);
		switch (type) {
		case DATASOURCE:
			SecurityCache.getInstance().invalidate();
			break;
		case CHART:
		case DYNAMICENTITY:
			MetaDataServerProvider.getInstance().revalidate(true);
			break;
		}
	}

	@Override
	public Collection<DynamicTasklistVO> getDynamicTasklists() throws CommonPermissionException {
		return DatasourceCache.getInstance().getAllDynamicTasklists();
	}

	@Override
	public Set<String> getDynamicTasklistAttributes(Integer dtlId) throws CommonPermissionException, NuclosDatasourceException {
		checkReadAllowed(NuclosEntity.TASKLIST);
		DynamicTasklistVO dtl = DatasourceCache.getInstance().getDynamicTasklist(dtlId);

		String sQuery = createSQL(dtl.getSource(), new HashMap<String, Object>());

		ResultVO result = dataBaseHelper.getDbAccess().executePlainQueryAsResultVO(sQuery, 1);

		Set<String> attributes = new HashSet<String>();
		for (ResultColumnVO col : result.getColumns()) {
			attributes.add(col.getColumnLabel());
		}
		return attributes;
	}

	@Override
	public ResultVO getDynamicTasklistData(Integer dtlId) throws CommonPermissionException, NuclosDatasourceException {
		if (!SecurityCache.getInstance().isSuperUser(getCurrentUserName()) && !SecurityCache.getInstance().getDynamicTasklists(getCurrentUserName()).contains(dtlId)) {
			throw new CommonPermissionException("Permission denied for dynamic task list with id " + dtlId);
		}

		DynamicTasklistVO dtl = DatasourceCache.getInstance().getDynamicTasklist(dtlId);
		String sQuery = createSQL(dtl.getSource(), new HashMap<String, Object>());
		return dataBaseHelper.getDbAccess().executePlainQueryAsResultVO(sQuery, -1);
	}

	@Override
	public CollectableField getDefaultValue(String datasource, String valuefield, String idfield, String defaultfield, Map<String, Object> params) throws CommonBusinessException {
		DatasourceVO dsvo = getValuelistProvider(datasource);
		List<DatasourceParameterVO> collParameters = getParameters(dsvo.getSource());
		Map<String, Object> queryParams = new HashMap<String, Object>(params);
		for (DatasourceParameterVO dpvo : collParameters) {
			if (queryParams.get(dpvo.getParameter()) == null) {
				queryParams.put(dpvo.getParameter(), null);
			}
		}

		final ResultVO result = executeQuery(dsvo.getSource(), queryParams, null);
		int iIndexValue = -1;
		int iIndexId = -1;
		int iIndexDefaultMarker = -1;
		final List<ResultColumnVO> columns = result.getColumns();
		final int len = columns.size();
		for (int iIndex = 0; iIndex < len; ++iIndex) {
			final ResultColumnVO rcvo = columns.get(iIndex);
			final String label = rcvo.getColumnLabel();
			if (label.equalsIgnoreCase(valuefield)) {
				iIndexValue = iIndex;
			}
			else if (label.equalsIgnoreCase(idfield)) {
				iIndexId = iIndex;
			}
			else if (label.equalsIgnoreCase(defaultfield)) {
				iIndexDefaultMarker = iIndex;
			}
		}
		if (iIndexValue < 0) {
			throw new IllegalArgumentException("In data source '" + dsvo + "', there is no field '" + valuefield + "'.");
		}

		for (Object[] oValue : result.getRows()) {
			if (oValue[iIndexValue] != null) {
				CollectableField cf;
				if (iIndexId == -1) {
					cf = new CollectableValueField(oValue[iIndexValue]);
				}
				else if (oValue[iIndexId] != null) {
					final Integer iId = ((Number) (oValue[iIndexId])).intValue();
					cf = new CollectableValueIdField(iId, oValue[iIndexValue]);
				}
				else {
					cf = CollectableValueIdField.NULL;
				}
				if (iIndexDefaultMarker != -1) {
					if (oValue[iIndexDefaultMarker] != null) {
						try {
							if ((Boolean) oValue[iIndexDefaultMarker]) {
								return cf;
							}
						}
						catch (Exception ex) {
							throw new CommonFatalException("error.vlp.defaultvalue");
						}
					}
				}
			}
		}
		return iIndexId != -1 ? CollectableValueIdField.NULL : CollectableValueField.NULL;
	}
}
