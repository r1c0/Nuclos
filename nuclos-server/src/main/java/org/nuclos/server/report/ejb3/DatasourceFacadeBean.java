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
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.database.query.definition.Schema;
import org.nuclos.common.database.query.definition.Table;
import org.nuclos.common.querybuilder.DatasourceUtils;
import org.nuclos.common.querybuilder.NuclosDatasourceException;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
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
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.dal.processor.ProcessorFactorySingleton;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbAccess;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.dblayer.statements.DbPlainStatement;
import org.nuclos.server.masterdata.MasterDataWrapper;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.SchemaCache;
import org.nuclos.server.report.valueobject.DatasourceParameterVO;
import org.nuclos.server.report.valueobject.DatasourceVO;
import org.nuclos.server.report.valueobject.DynamicEntityVO;
import org.nuclos.server.report.valueobject.RecordGrantVO;
import org.nuclos.server.report.valueobject.ResultVO;
import org.nuclos.server.report.valueobject.ValuelistProviderVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Datasource facade encapsulating datasource management. <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Stateless
@Local(DatasourceFacadeLocal.class)
@Remote(DatasourceFacadeRemote.class)
@Transactional
public class DatasourceFacadeBean extends NuclosFacadeBean implements DatasourceFacadeLocal, DatasourceFacadeRemote {

   private static enum DataSourceType {
		DYNAMICENTITY(NuclosEntity.DYNAMICENTITY, NuclosEntity.DYNAMICENTITYUSAGE, "dynamicEntity"),
		VALUELISTPROVIDER(NuclosEntity.VALUELISTPROVIDER, NuclosEntity.VALUELISTPROVIDERUSAGE, "valuelistProvider"),
		RECORDGRANT(NuclosEntity.RECORDGRANT, NuclosEntity.RECORDGRANTUSAGE, "recordGrant"),
		DATASOURCE(NuclosEntity.DATASOURCE, NuclosEntity.DATASOURCEUSAGE, "datasource");

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
			default:
				return MasterDataWrapper.getDatasourceVO(deVO, userName);
			}
		}

	   public static DataSourceType getFromDatasourceVO(DatasourceVO datasourceVO) {
	   	if (datasourceVO instanceof DynamicEntityVO) {
	   		return DataSourceType.DYNAMICENTITY;
	   	} else if (datasourceVO instanceof ValuelistProviderVO) {
	   		return DataSourceType.VALUELISTPROVIDER;
	   	} else if (datasourceVO instanceof RecordGrantVO) {
	   		return DataSourceType.RECORDGRANT;
	   	} else {
	   		return DataSourceType.DATASOURCE;
	   	}
	   }
   }

   /**
    * get all datasources
    *
    * @return set of datasources
    * @throws CommonPermissionException
    */
   @Override
public Collection<DatasourceVO> getDatasources() throws CommonPermissionException {
   	this.checkReadAllowed(NuclosEntity.REPORT, NuclosEntity.DATASOURCE);
   	return DatasourceCache.getInstance().getAllDatasources(getCurrentUserName());
   }

   /**
    * get all datasources
    *
    * @return set of datasources
    */
   @Override
public Collection<DatasourceVO> getDatasourcesForCurrentUser() {
      return DatasourceCache.getInstance().getDatasourcesByCreator(getCurrentUserName());
   }

   /**
    * get datasource value object
    *
    * @param iId
    *                primary key of datasource
    * @return datasource value object
    */
   @Override
@RolesAllowed("Login")
   public DatasourceVO get(Integer iId) throws CommonFinderException, CommonPermissionException {
   	return DatasourceCache.getInstance().getDatasourcesById(iId, getCurrentUserName());

     /* todo how should we handle permissions here? - It is necessary, that
     // all users have the right to execute inline datasources
     if (result.getPermission() == DatasourceVO.PERMISSION_NONE) {
         throw new CommonPermissionException();
     }
     return result;
     */
   }

   /**
    * get datasource value object
    *
    * @param sDatasourceName
    *                name of datasource
    * @return datasource value object
    */
   @Override
@RolesAllowed("Login")
   public DatasourceVO get(String sDatasourceName) throws CommonFinderException, CommonPermissionException {
   	return DatasourceCache.getInstance().getDatasourceByName(sDatasourceName);
   }

   /**
    * get valuelist provider value object
    *
    * @param sValuelistProvider
    *                name of valuelist provider
    * @return valuelist provider value object
    */
   @Override
@RolesAllowed("Login")
   public ValuelistProviderVO getValuelistProvider(String sValuelistProvider) throws CommonFinderException, CommonPermissionException {
   	return DatasourceCache.getInstance().getValuelistProviderByName(sValuelistProvider);
   }

   /**
    * get dynamic entity value object
    *
    * @param sDynamicEntity
    *                name of valuelist provider
    * @return dynamic entity value object
    */
   @Override
@RolesAllowed("Login")
   public DynamicEntityVO getDynamicEntity(String sDynamicEntity) throws CommonFinderException, CommonPermissionException {
   	return DatasourceCache.getInstance().getDynamicEntityByName(sDynamicEntity);
   }

   /**
   * get a Datasource by id regardless of permisssions
   * @param iDatasourceId
   * @return
   * @throws CommonPermissionException
   */
   @Override
@RolesAllowed("Login")
   public DatasourceVO getDatasourceById(Integer iDatasourceId) {
      return DatasourceCache.getInstance().get(iDatasourceId);
   }

   /**
    * create new datasource
    *
    * @param datasourcevo
    *                value object
    * @return new datasource
    */
   @Override
public DatasourceVO create(DatasourceVO datasourcevo, List<String> lstUsedDatasources) throws CommonCreateException, CommonValidationException, NuclosBusinessRuleException, CommonPermissionException {
   	datasourcevo.validate();
   	updateValidFlag(datasourcevo);

   	MasterDataVO mdVO = getMasterDataFacade().create(NuclosEntity.DATASOURCE.getEntityName(), MasterDataWrapper.wrapDatasourceVO(datasourcevo), null);
   	DatasourceVO dbDataSourceVO = MasterDataWrapper.getDatasourceVO(mdVO, getCurrentUserName());
   	getMasterDataFacade().notifyClients(NuclosEntity.DATASOURCE.getEntityName());

   	SecurityCache.getInstance().invalidate();
   	try {
			replaceUsedDatasourceList(dbDataSourceVO, lstUsedDatasources);
		}
		catch(CommonFinderException e) {
			throw new CommonFatalException(e);
		}
		catch(CommonRemoveException e) {
			throw new CommonFatalException(e);
		}
		catch(CommonStaleVersionException e) {
			throw new CommonFatalException(e);
		}
   	DatasourceCache.getInstance().invalidate();
   	SchemaCache.getInstance().invalidate();

   	return dbDataSourceVO;
   }

   /**
    * create new dynamic entity
    *
    * @param dynamicEntityVO
    *                value object
    * @param lstUsedDynamicEntities
    * 					list of used dynamic entity names
    * @return new dynamic entity
    */
   @Override
public DynamicEntityVO createDynamicEntity(DynamicEntityVO dynamicEntityVO, List<String> lstUsedDynamicEntities) throws CommonCreateException, CommonValidationException, NuclosBusinessRuleException, CommonPermissionException {
   	dynamicEntityVO.validate();
   	updateValidFlag(dynamicEntityVO);

   	processChangingDynamicEntity(dynamicEntityVO, null, true);
   	MasterDataVO mdVO = getMasterDataFacade().create(NuclosEntity.DYNAMICENTITY.getEntityName(), MasterDataWrapper.wrapDatasourceVO(dynamicEntityVO), null);
   	DynamicEntityVO dbDynamicEntityVO = MasterDataWrapper.getDynamicEntityVO(mdVO);
   	getMasterDataFacade().notifyClients(NuclosEntity.DYNAMICENTITY.getEntityName());

   	try {
			replaceUsedDatasourceList(dbDynamicEntityVO, lstUsedDynamicEntities);
		}
		catch(CommonFinderException e) {
			throw new CommonFatalException(e);
		}
		catch(CommonRemoveException e) {
			throw new CommonFatalException(e);
		}
		catch(CommonStaleVersionException e) {
			throw new CommonFatalException(e);
		}
   	DatasourceCache.getInstance().invalidate();
   	SchemaCache.getInstance().invalidate();
   	MetaDataServerProvider.getInstance().revalidate();

   	return dbDynamicEntityVO;
   }

   /**
    * create new valuelist provider
    *
    * @param valuelistProviderVO
    *                value object
    * @param lstUsedValuelistProvider
    * 					list of used valuelist provider names
    * @return new valuelist provider
    */
   @Override
public ValuelistProviderVO createValuelistProvider(ValuelistProviderVO valuelistProviderVO, List<String> lstUsedValuelistProvider) throws CommonCreateException, CommonValidationException, NuclosBusinessRuleException, CommonPermissionException {
   	valuelistProviderVO.validate();
   	updateValidFlag(valuelistProviderVO);

   	MasterDataVO mdVO = getMasterDataFacade().create(NuclosEntity.VALUELISTPROVIDER.getEntityName(), MasterDataWrapper.wrapDatasourceVO(valuelistProviderVO), null);
   	ValuelistProviderVO dbValuelistProviderVO = MasterDataWrapper.getValuelistProviderVO(mdVO);
   	getMasterDataFacade().notifyClients(NuclosEntity.VALUELISTPROVIDER.getEntityName());

   	try {
			replaceUsedDatasourceList(dbValuelistProviderVO, lstUsedValuelistProvider);
		}
		catch(CommonFinderException e) {
			throw new CommonFatalException(e);
		}
		catch(CommonRemoveException e) {
			throw new CommonFatalException(e);
		}
		catch(CommonStaleVersionException e) {
			throw new CommonFatalException(e);
		}
   	DatasourceCache.getInstance().invalidate();
   	SchemaCache.getInstance().invalidate();

   	return dbValuelistProviderVO;
   }

   /**
    * create new RecordGrant
    *
    * @param recordGrantVO
    *                value object
    * @param lstUsedRecordGrant
    * 					list of used RecordGrant names
    * @return new RecordGrant
    */
   @Override
   public RecordGrantVO createRecordGrant(RecordGrantVO recordGrantVO, List<String> lstUsedRecordGrant) throws CommonCreateException, CommonValidationException, NuclosBusinessRuleException, CommonPermissionException {
	   recordGrantVO.validate();
   	updateValidFlag(recordGrantVO);

   	try {
   		if (recordGrantVO.getValid())
   			DatasourceUtils.validateRecordGrantSQL(recordGrantVO.getEntity(), createSQL(recordGrantVO.getSource()));
    }
    catch(NuclosDatasourceException e1) {
    	throw new CommonFatalException(e1);
    }
   	MasterDataVO mdVO = getMasterDataFacade().create(NuclosEntity.RECORDGRANT.getEntityName(), MasterDataWrapper.wrapDatasourceVO(recordGrantVO), null);
   	RecordGrantVO dbRecordGrantVO = MasterDataWrapper.getRecordGrantVO(mdVO);
   	getMasterDataFacade().notifyClients(NuclosEntity.RECORDGRANT.getEntityName());

   	try {
			replaceUsedDatasourceList(dbRecordGrantVO, lstUsedRecordGrant);
		}
		catch(CommonFinderException e) {
			throw new CommonFatalException(e);
		}
		catch(CommonRemoveException e) {
			throw new CommonFatalException(e);
		}
		catch(CommonStaleVersionException e) {
			throw new CommonFatalException(e);
		}
   	DatasourceCache.getInstance().invalidate();
   	SchemaCache.getInstance().invalidate();

   	return dbRecordGrantVO;
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
   @Override
public void modify(DatasourceVO datasourcevo) throws CommonFinderException, CommonPermissionException, CommonStaleVersionException, CommonValidationException, NuclosBusinessRuleException {
   	DataSourceType nuclosEntity = DataSourceType.getFromDatasourceVO(datasourcevo);
   	switch (nuclosEntity) {
   		case DATASOURCE:
   			modify(datasourcevo, null, false);
   			break;
   		case DYNAMICENTITY:
   			modifyDynamicEntity((DynamicEntityVO) datasourcevo, null, false);
   			break;
   		case VALUELISTPROVIDER:
   			modifyValuelistProvider((ValuelistProviderVO) datasourcevo, null, false);
   			break;
   	}
   }

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
   @Override
public DynamicEntityVO modifyDynamicEntity(DynamicEntityVO dynamicEntityVO, List<String> lstUsedDynamicEntities) throws CommonFinderException, CommonPermissionException, CommonStaleVersionException, CommonValidationException, NuclosBusinessRuleException {
   	return modifyDynamicEntity(dynamicEntityVO, lstUsedDynamicEntities, true);
   }

   private DynamicEntityVO modifyDynamicEntity(DynamicEntityVO dynamicEntityVO, List<String> lstUsedDynamicEntities, boolean modifyUsedDatasources) throws CommonFinderException, CommonPermissionException, CommonStaleVersionException, CommonValidationException, NuclosBusinessRuleException {
   	dynamicEntityVO.validate();
   	updateValidFlag(dynamicEntityVO);

   	DynamicEntityVO dbDynamicEntityVO;
   	try {
   		dbDynamicEntityVO = MasterDataWrapper.getDynamicEntityVO(
				getMasterDataFacade().get(NuclosEntity.DYNAMICENTITY.getEntityName(),dynamicEntityVO.getId()));
   		this.checkWriteAllowed(NuclosEntity.DYNAMICENTITY);

			if(dbDynamicEntityVO.getVersion() != dynamicEntityVO.getVersion()) {
				throw new CommonStaleVersionException();
			}

			processChangingDynamicEntity(dynamicEntityVO, dbDynamicEntityVO, true);
			getMasterDataFacade().modify(NuclosEntity.DYNAMICENTITY.getEntityName(), MasterDataWrapper.wrapDatasourceVO(dynamicEntityVO), null);

			if (modifyUsedDatasources) {
				// store the list of used datasources
				replaceUsedDatasourceList(dbDynamicEntityVO, lstUsedDynamicEntities);
			}
		}
		catch(CommonCreateException ex) {
			throw new NuclosFatalException(ex);
		}
		catch(CommonRemoveException ex) {
			throw new NuclosBusinessRuleException(ex);
		}

		DatasourceCache.getInstance().invalidate();
		SchemaCache.getInstance().invalidate();
		MetaDataServerProvider.getInstance().revalidate();

		return DatasourceCache.getInstance().getDynamicEntity(dbDynamicEntityVO.getId());
   }

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
   @Override
public ValuelistProviderVO modifyValuelistProvider(ValuelistProviderVO valuelistProviderVO, List<String> lstUsedValuelistProvider) throws CommonFinderException, CommonPermissionException, CommonStaleVersionException, CommonValidationException, NuclosBusinessRuleException {
   	return modifyValuelistProvider(valuelistProviderVO, lstUsedValuelistProvider, true);
   }

   private ValuelistProviderVO modifyValuelistProvider(ValuelistProviderVO valuelistProviderVO, List<String> lstUsedValuelistProvider, boolean modifyUsedDatasources) throws CommonFinderException, CommonPermissionException, CommonStaleVersionException, CommonValidationException, NuclosBusinessRuleException {
   	valuelistProviderVO.validate();
   	updateValidFlag(valuelistProviderVO);

   	ValuelistProviderVO dbValuelistProviderVO;
   	try {
   		dbValuelistProviderVO = MasterDataWrapper.getValuelistProviderVO(
				getMasterDataFacade().get(NuclosEntity.VALUELISTPROVIDER.getEntityName(),valuelistProviderVO.getId()));
   		this.checkWriteAllowed(NuclosEntity.VALUELISTPROVIDER);

			if(dbValuelistProviderVO.getVersion() != valuelistProviderVO.getVersion()) {
				throw new CommonStaleVersionException();
			}

			getMasterDataFacade().modify(NuclosEntity.VALUELISTPROVIDER.getEntityName(), MasterDataWrapper.wrapDatasourceVO(valuelistProviderVO), null);

			if (modifyUsedDatasources) {
				// store the list of used datasources
				replaceUsedDatasourceList(dbValuelistProviderVO, lstUsedValuelistProvider);
			}
		}
		catch(CommonCreateException ex) {
			throw new NuclosFatalException(ex);
		}
		catch(CommonRemoveException ex) {
			throw new NuclosBusinessRuleException(ex);
		}

		DatasourceCache.getInstance().invalidate();
		SchemaCache.getInstance().invalidate();

		return DatasourceCache.getInstance().getValuelistProvider(dbValuelistProviderVO.getId());
   }

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
   @Override
   public RecordGrantVO modifyRecordGrant(RecordGrantVO recordGrantVO, List<String> lstUsedRecordGrant) throws CommonFinderException, CommonPermissionException, CommonStaleVersionException, CommonValidationException, NuclosBusinessRuleException {
   	return modifyRecordGrant(recordGrantVO, lstUsedRecordGrant, true);
   }

   private RecordGrantVO modifyRecordGrant(RecordGrantVO recordGrantVO, List<String> lstUsedRecordGrant, boolean modifyUsedDatasources) throws CommonFinderException, CommonPermissionException, CommonStaleVersionException, CommonValidationException, NuclosBusinessRuleException {
	recordGrantVO.validate();
   	updateValidFlag(recordGrantVO);

   	RecordGrantVO dbRecordGrantVO;
   	try {
   		dbRecordGrantVO = MasterDataWrapper.getRecordGrantVO(
				getMasterDataFacade().get(NuclosEntity.RECORDGRANT.getEntityName(),recordGrantVO.getId()));
   		this.checkWriteAllowed(NuclosEntity.RECORDGRANT);

			if(dbRecordGrantVO.getVersion() != recordGrantVO.getVersion()) {
				throw new CommonStaleVersionException();
			}

			try {
		   		if (recordGrantVO.getValid())
		   			DatasourceUtils.validateRecordGrantSQL(recordGrantVO.getEntity(), createSQL(recordGrantVO.getSource()));
		    }
		    catch(NuclosDatasourceException e1) {
		    	throw new CommonFatalException(e1);
		    }
			getMasterDataFacade().modify(NuclosEntity.RECORDGRANT.getEntityName(), MasterDataWrapper.wrapDatasourceVO(recordGrantVO), null);

			if (modifyUsedDatasources) {
				// store the list of used datasources
				replaceUsedDatasourceList(dbRecordGrantVO, lstUsedRecordGrant);
			}
		}
		catch(CommonCreateException ex) {
			throw new NuclosFatalException(ex);
		}
		catch(CommonRemoveException ex) {
			throw new NuclosBusinessRuleException(ex);
		}

		DatasourceCache.getInstance().invalidate();
		SchemaCache.getInstance().invalidate();

		return DatasourceCache.getInstance().getRecordGrant(dbRecordGrantVO.getId());
   }

   /**
    * modify an existing datasource
    *
    * @param datasourcevo
    *                value object
    * @return modified datasource
    */
   @Override
public DatasourceVO modify(DatasourceVO datasourcevo, List<String> lstUsedDatasources) throws CommonFinderException, CommonPermissionException, CommonStaleVersionException, CommonValidationException, NuclosBusinessRuleException, CommonRemoteException {
   	return modify(datasourcevo, lstUsedDatasources, true);
   }

   private DatasourceVO modify(DatasourceVO datasourcevo, List<String> lstUsedDatasources, boolean modifyUsedDatasources) throws CommonFinderException, CommonPermissionException, CommonStaleVersionException, CommonValidationException, NuclosBusinessRuleException, CommonRemoteException {
   	datasourcevo.validate();
   	updateValidFlag(datasourcevo);

   	try {
   		validateUniqueConstraint(datasourcevo);

   		DatasourceVO dbDatasourceVO = MasterDataWrapper.getDatasourceVO(
   			getMasterDataFacade().get(NuclosEntity.DATASOURCE.getEntityName(), datasourcevo.getId()), getCurrentUserName());

   		if (DatasourceCache.getInstance().getPermission(dbDatasourceVO.getId(), getCurrentUserName()) != DatasourceVO.PERMISSION_READWRITE) {
   			throw new CommonPermissionException();
   		}
   		if (dbDatasourceVO.getVersion() != datasourcevo.getVersion()) {
   			throw new CommonStaleVersionException();
   		}
   		getMasterDataFacade().modify(NuclosEntity.DATASOURCE.getEntityName(), MasterDataWrapper.wrapDatasourceVO(datasourcevo), null);

   		if (modifyUsedDatasources) {
   			// store the list of used datasources
   			replaceUsedDatasourceList(datasourcevo, lstUsedDatasources);
   		}

   		DatasourceCache.getInstance().invalidate();
   		SchemaCache.getInstance().invalidate();

   		return DatasourceCache.getInstance().get(datasourcevo.getId());
   	}
		catch(CommonRemoveException ex) {
			throw new NuclosBusinessRuleException(ex);
		}
		catch(CommonCreateException ex) {
			throw new NuclosFatalException(ex);
		}
		catch(CommonFatalException ex) {
			throw new NuclosFatalException(ex);
		}
   }

   /**
    *
    * @param newDEs (dynamic entities of this list would be created)
    * @param oldDEs (dynamic entities of this list would be deleted)
    */
   @Override
public void processChangingDynamicEntities(Collection<DynamicEntityVO> newDEs, Collection<DynamicEntityVO> oldDEs, boolean bExecute, List<String> script) {
   	for (DynamicEntityVO oldDEVO : oldDEs) {
   		try {
				this.processChangingDynamicEntity(null, oldDEVO, false, false, bExecute, script);
			} catch (CommonValidationException e) {
				// no validation here
			}
   	}
   	for (DynamicEntityVO newDEVO : newDEs) {
   		try {
				this.processChangingDynamicEntity(newDEVO, null, false, false, bExecute, script);
			} catch (CommonValidationException e) {
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
    * @throws CommonValidationException (only if validate is true)
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
    * @throws CommonValidationException (only if validate is true)
    */
   private void processChangingDynamicEntity(DynamicEntityVO newDEVO, DynamicEntityVO oldDEVO, boolean validate, boolean revalidateMasterDataMetaCache, boolean bExecute, List<String> script) throws CommonValidationException {
   	String sqlSelect = null;
   	if (newDEVO != null) {
			if (validate) {
				try {
					debug("validate dynamic entity name \"" + newDEVO.getName() + "\"");
					DatasourceUtils.validateDynEntityName(newDEVO.getName());
					sqlSelect = createSQL(newDEVO.getSource());
					debug("validate dynamic entity sql <SQL>" + sqlSelect + "</SQL>");
					DatasourceUtils.validateDynEntitySQL(sqlSelect);
				} catch(NuclosDatasourceException e) {
					throw new CommonFatalException(e);
				}
			}
   	}

		DbAccess dbAccess = DataBaseHelper.getDbAccess();
		if (oldDEVO != null) {
			String sqlToExecute = null;
			try {
				sqlToExecute = "drop view " + MasterDataMetaVO.DYNAMIC_ENTITY_VIEW_PREFIX + oldDEVO.getName();
				script.add(sqlToExecute);
				if (bExecute) dbAccess.execute(new DbPlainStatement(sqlToExecute));
				info("deleted dynamic entity <SQL>" + sqlToExecute + "</SQL>");
			} catch (DbException e) {
				error("could not deleted dynamic entity <SQL>" + sqlToExecute + "</SQL> ERROR: " + e.getMessage());
			}
		}
		if (newDEVO != null) {
			String sqlToExecute = null;
			try {
				info("creating dynamic entity " + newDEVO.getName() + "...");
				sqlToExecute = "create view " + MasterDataMetaVO.DYNAMIC_ENTITY_VIEW_PREFIX + newDEVO.getName() + " as " +
				(sqlSelect!=null?sqlSelect:createSQL(newDEVO.getSource()));
				script.add(sqlToExecute);
				if (bExecute) dbAccess.execute(new DbPlainStatement(sqlToExecute));
				info("created dynamic entity <SQL>" + sqlToExecute + "</SQL>");
			} catch (Exception e) {
				error("could not create dynamic entity <SQL>" + sqlToExecute + "</SQL> ERROR: " + e.getMessage());
			}
		}
		if (revalidateMasterDataMetaCache) MasterDataMetaCache.getInstance().revalidate();
   }

   private void validateUniqueConstraint(DatasourceVO datasourcevo) throws CommonValidationException {
   	try {
   		//final DatasourceLocal datasource = datasourceHome.findByName(datasourcevo.getDatasource());
   		DatasourceVO foundDatasourceVO = this.get(datasourcevo.getName());
   		//if(datasourcevo.getId().intValue() != datasource.getId().intValue()){
   		if(foundDatasourceVO != null && datasourcevo.getId().intValue() != foundDatasourceVO.getId().intValue()){
				throw new CommonValidationException(
					StringUtils.getParameterizedExceptionMessage("validation.unique.constraint", "Name", "Data source"));
   		}
   	} catch (CommonFinderException e) {
     		// No element found -> validation O.K.
     	} catch (CommonPermissionException e) {
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
   private void replaceUsedDatasourceList(DatasourceVO datasourceVO, List<String> referencedDatasources) throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException, CommonRemoveException, CommonStaleVersionException, CommonCreateException {
	   DataSourceType datasourceType = DataSourceType.getFromDatasourceVO(datasourceVO);
	   MasterDataMetaCache metaCache = MasterDataMetaCache.getInstance();
	   MasterDataMetaVO usageMeta = metaCache.getMetaData(datasourceType.entityUsage);
	   String usageEntityName = datasourceType.entityUsage.getEntityName();

	   // 1. remove all entries for this id:
	   CollectableComparison condUsage = SearchConditionUtils.newMDReferenceComparison(
		   usageMeta,
		   datasourceType.fieldEntity,
		   datasourceVO.getId());
	   for(MasterDataVO mdVO : getMasterDataFacade().getMasterData(usageEntityName, condUsage, true))
		   getMasterDataFacade().remove(usageEntityName, mdVO, false);

	   // 2. insert the new entries:
	   for(String dataSourceName : referencedDatasources) {
		   CollectableComparison condDatasource = SearchConditionUtils.newMDComparison(
			   metaCache.getMetaData(datasourceType.entity.getEntityName()),
			   "name",
			   org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator.EQUAL,
			   dataSourceName);
		   Collection<MasterDataVO> usedDatasources
		   = getMasterDataFacade().getMasterData(
			   datasourceType.entity.getEntityName(),
			   condDatasource,
			   true);

		   for(MasterDataVO usedDatasource : usedDatasources) {
			   MasterDataVO newEntryVO = new MasterDataVO(usageMeta, true);
			   newEntryVO.setField(datasourceType.fieldEntity+"Id", datasourceVO.getId());
			   newEntryVO.setField(datasourceType.fieldEntityUsed+"Id", usedDatasource.getId());

			   getMasterDataFacade().create(usageEntityName, newEntryVO, null);
		   }
	   }
   }

   /**
    * get a list of DatasourceVO which uses the datasource with the given id
    *
    * ONLY for datasources, NOT for dynamic entities and valuelist provider
    * @see also <code>getUsagesForDatasource(DatasourceVO datasourceVO)</code>
    *
    * @param iDatasourceId
    * @return
    * @throws CommonFinderException
    * @throws CommonPermissionException
    */
   @Override
@RolesAllowed("Login")
   public List<DatasourceVO> getUsagesForDatasource(final Integer iDatasourceId) throws CommonFinderException, CommonPermissionException {
   	return getUsagesForDatasource(get(iDatasourceId));
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
   @Override
@RolesAllowed("Login")
   public List<DatasourceVO> getUsagesForDatasource(DatasourceVO datasourceVO) throws CommonFinderException, CommonPermissionException {
   	final DataSourceType datasourceType = DataSourceType.getFromDatasourceVO(datasourceVO);

   	List<DatasourceVO> result = new ArrayList<DatasourceVO>();

   	CollectableComparison cond = SearchConditionUtils.newMDReferenceComparison(
         MasterDataMetaCache.getInstance().getMetaData(datasourceType.entityUsage),datasourceType.fieldEntityUsed, datasourceVO.getId());

   	for (MasterDataVO usageVO : getMasterDataFacade().getMasterData(datasourceType.entityUsage.getEntityName(), cond, true)) {
   		MasterDataVO deVO = getMasterDataFacade().get(datasourceType.entity.getEntityName(), usageVO.getField(datasourceType.fieldEntityUsed+"Id"));
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
	@Override
	public List<DatasourceVO> getUsingByForDatasource(final Integer iDatasourceId)
		throws CommonFinderException, CommonPermissionException {

		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("T_UD_DATASOURCEUSAGE").alias(ProcessorFactorySingleton.BASE_ALIAS);
		query.multiselect(
			t.column("INTID", Integer.class),
			t.column("INTID_T_UD_DATASOURCE", Integer.class),
			t.column("INTID_T_UD_DATASOURCE_USED", Integer.class));
		query.where(builder.equal(t.column("INTID_T_UD_DATASOURCE", Integer.class), iDatasourceId));

		List<DatasourceVO> result = new ArrayList<DatasourceVO>();
		for (DbTuple tuple : DataBaseHelper.getDbAccess().executeQuery(query)) {
			try {
				Integer dataSourceUsed = tuple.get(2, Integer.class);
				MasterDataVO mdVO = getMasterDataFacade().get(NuclosEntity.DATASOURCE.getEntityName(), dataSourceUsed);
				result.add(MasterDataWrapper.getDatasourceVO(mdVO, getCurrentUserName()));
			}
			catch(CommonFinderException ex) {
				throw new NuclosFatalException(ex);
			}
			catch(CommonPermissionException ex) {
				throw new NuclosFatalException(ex);
			}
		}
		return result;
	}

   /**
    * delete an existing datasource
    *
    * @param datasourcevo
    *                value object
    */
   @Override
public void remove(DatasourceVO datasourcevo) throws CommonFinderException, CommonRemoveException, CommonPermissionException, CommonStaleVersionException, NuclosBusinessRuleException {
   	DatasourceVO dbDatasourceVO = MasterDataWrapper.getDatasourceVO(getMasterDataFacade().get(NuclosEntity.DATASOURCE.getEntityName(), datasourcevo.getId()), getCurrentUserName());

  		if (DatasourceCache.getInstance().getPermission(dbDatasourceVO.getId(), getCurrentUserName()) != DatasourceVO.PERMISSION_READWRITE) {
  			throw new CommonPermissionException();
  		}
  		if (dbDatasourceVO.getVersion() != datasourcevo.getVersion()) {
  			throw new CommonStaleVersionException();
  		}

  		getMasterDataFacade().remove(NuclosEntity.DATASOURCE.getEntityName(), MasterDataWrapper.wrapDatasourceVO(datasourcevo), false);
  		getMasterDataFacade().notifyClients(NuclosEntity.DATASOURCE.getEntityName());
		try {
			replaceUsedDatasourceList(datasourcevo, Collections.<String>emptyList());
		}
		catch(CommonCreateException e) {
			throw new CommonFatalException(e);
		}
		SecurityCache.getInstance().invalidate();
		DatasourceCache.getInstance().invalidate();
		SchemaCache.getInstance().invalidate();
   }

   /**
    * delete an existing dynamic entity
    *
    * @param dynamicEntityVO
    *                value object
    */
   @Override
public void removeDynamicEntity(DynamicEntityVO dynamicEntityVO) throws CommonFinderException, CommonRemoveException, CommonPermissionException, CommonStaleVersionException, NuclosBusinessRuleException {
   	DynamicEntityVO dbDynamicEntityVO = MasterDataWrapper.getDynamicEntityVO(getMasterDataFacade().get(NuclosEntity.DYNAMICENTITY.getEntityName(), dynamicEntityVO.getId()));
   	this.checkDeleteAllowed(NuclosEntity.DYNAMICENTITY);

  		if (dbDynamicEntityVO.getVersion() != dynamicEntityVO.getVersion()) {
  			throw new CommonStaleVersionException();
  		}

  		try {
			processChangingDynamicEntity(null, dbDynamicEntityVO, false);
		}
		catch(CommonValidationException e) {
			// no validation here
		}
  		getMasterDataFacade().remove(NuclosEntity.DYNAMICENTITY.getEntityName(), MasterDataWrapper.wrapDatasourceVO(dbDynamicEntityVO), false);
  		getMasterDataFacade().notifyClients(NuclosEntity.DYNAMICENTITY.getEntityName());

  		try {
			replaceUsedDatasourceList(dbDynamicEntityVO, Collections.<String>emptyList());
		}
		catch(CommonCreateException e) {
			throw new CommonFatalException(e);
		}
  		DatasourceCache.getInstance().invalidate();
  		SchemaCache.getInstance().invalidate();
  		MetaDataServerProvider.getInstance().revalidate();
   }

   /**
    * delete an existing valuelist provider
    *
    * @param valuelistProviderVO
    *                value object
    */
   @Override
public void removeValuelistProvider(ValuelistProviderVO valuelistProviderVO) throws CommonFinderException, CommonRemoveException, CommonPermissionException, CommonStaleVersionException, NuclosBusinessRuleException {
   	ValuelistProviderVO dbValuelistProviderVO = MasterDataWrapper.getValuelistProviderVO(getMasterDataFacade().get(NuclosEntity.VALUELISTPROVIDER.getEntityName(), valuelistProviderVO.getId()));
   	this.checkDeleteAllowed(NuclosEntity.VALUELISTPROVIDER);

  		if (dbValuelistProviderVO.getVersion() != valuelistProviderVO.getVersion()) {
  			throw new CommonStaleVersionException();
  		}

  		getMasterDataFacade().remove(NuclosEntity.VALUELISTPROVIDER.getEntityName(), MasterDataWrapper.wrapDatasourceVO(dbValuelistProviderVO), false);
  		getMasterDataFacade().notifyClients(NuclosEntity.VALUELISTPROVIDER.getEntityName());

  		try {
			replaceUsedDatasourceList(dbValuelistProviderVO, Collections.<String>emptyList());
		}
		catch(CommonCreateException e) {
			throw new CommonFatalException(e);
		}
  		DatasourceCache.getInstance().invalidate();
  		SchemaCache.getInstance().invalidate();
   }

   /**
    * delete an existing RecordGrant
    *
    * @param recordGrantVO
    *                value object
    */
   @Override
public void removeRecordGrant(RecordGrantVO recordGrantVO) throws CommonFinderException, CommonRemoveException, CommonPermissionException, CommonStaleVersionException, NuclosBusinessRuleException {
	   RecordGrantVO dbRecordGrantVO = MasterDataWrapper.getRecordGrantVO(getMasterDataFacade().get(NuclosEntity.RECORDGRANT.getEntityName(), recordGrantVO.getId()));
   	this.checkDeleteAllowed(NuclosEntity.RECORDGRANT);

  		if (dbRecordGrantVO.getVersion() != recordGrantVO.getVersion()) {
  			throw new CommonStaleVersionException();
  		}

  		getMasterDataFacade().remove(NuclosEntity.RECORDGRANT.getEntityName(), MasterDataWrapper.wrapDatasourceVO(dbRecordGrantVO), false);
  		getMasterDataFacade().notifyClients(NuclosEntity.RECORDGRANT.getEntityName());

  		try {
			replaceUsedDatasourceList(dbRecordGrantVO, Collections.<String>emptyList());
		}
		catch(CommonCreateException e) {
			throw new CommonFatalException(e);
		}
  		DatasourceCache.getInstance().invalidate();
  		SchemaCache.getInstance().invalidate();
   }

   /**
    * @param datasourcevo
    */
   private void updateValidFlag(DatasourceVO datasourcevo) {
   	try {
   		this.validateSqlFromXML(datasourcevo.getSource());
   		datasourcevo.setValid(Boolean.TRUE);
		}
		catch(CommonValidationException ex) {
			datasourcevo.setValid(Boolean.FALSE);
		}
		catch(NuclosDatasourceException ex) {
			datasourcevo.setValid(Boolean.FALSE);
		}
   }

   /**
    * Retrieve the parameters a datasource accepts.
    * @param sDatasourceXML
    * @return
    * @throws NuclosFatalException
    * @throws NuclosDatasourceException
    */
   @Override
@RolesAllowed("Login")
   public List<DatasourceParameterVO> getParameters(String sDatasourceXML) throws NuclosFatalException, NuclosDatasourceException {
		return DatasourceServerUtils.getParameters(sDatasourceXML);
   }

  /**
   * Retrieve the parameters a datasource accepts.
   * @param iDatasourceId
   * @return
   * @throws NuclosFatalException
   * @throws NuclosDatasourceException
   */
   @Override
   @RolesAllowed("Login")
  public List<DatasourceParameterVO> getParameters(Integer iDatasourceId) throws NuclosFatalException, NuclosDatasourceException {
		return DatasourceServerUtils.getParameters(iDatasourceId);
  }

   /**
    * validate the given DatasourceXML
    *
    * @param sDatasourceXML
    * @throws CommonValidationException
    * @throws NuclosReportException
    */
   @Override
@RolesAllowed("Login")
   public void validateSqlFromXML(String sDatasourceXML) throws CommonValidationException, NuclosDatasourceException {
   	final String sSql = this.createSQL(sDatasourceXML,this.getTestParameters(sDatasourceXML));

   	this.validateSql(sSql);
   }

   /**
    * validate the given SQL
    *
    * @param sql
    * @throws CommonValidationException
    * @throws NuclosReportException
    */
   @Override
   @RolesAllowed("Login")
   public void validateSql(String sql) throws CommonValidationException, NuclosDatasourceException {
		try {
			DataBaseHelper.getDbAccess().checkSyntax(sql);
		} catch (DbException e) {
			throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("datasource.error.invalid.statement", e.getMessage()), e);//"Die Abfrage ist ung\u00ef\u00bf\u00bdltig.\n" + e.getMessage(), e);
		}
   }

  /**
   * get sql string for datasource definition
   * @param iDatasourceId id of datasource
   * @return string containing sql
   */
  @Override
  public String createSQL(Integer iDatasourceId, Map<String, Object> mpParams) throws NuclosDatasourceException {
     return DatasourceServerUtils.createSQL(iDatasourceId, mpParams);
  }

   /**
    * get sql string for datasource definition without parameter definition
    *
    * @param sDatasourceXML
    *                xml of datasource
    * @return string containing sql
    */
  	@Override
	@RolesAllowed("Login")
   public String createSQL(String sDatasourceXML) throws NuclosDatasourceException {
      return DatasourceServerUtils.createSQL(sDatasourceXML);
   }

   /**
    * get sql string for datasource definition
    *
    * @param sDatasourceXML
    *                xml of datasource
    * @return string containing sql
    */
  	@Override
	@RolesAllowed("Login")
   public String createSQL(String sDatasourceXML, Map<String, Object> mpParams) throws NuclosDatasourceException {
  		return DatasourceServerUtils.createSQL(sDatasourceXML, mpParams);
   }

  	/**
	 * get sql string for report execution.
	 * check that this method is only called by the local interface as there is no authorization applied.
	 *
	 * @param iDatasourceId id of datasource
	 * @return string containing sql
	 */
	@Override
	public String createSQLForReportExecution(String name, Map<String, Object> mpParams) throws NuclosDatasourceException {
		if (isCalledRemotely()) {
			// just to ensure it won't be used in remote interface
			throw new NuclosFatalException("Invalid remote call");
		}

		return DatasourceServerUtils.createSQLForReportExecution(name, mpParams);
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
		catch(NuclosDatasourceException ex) {
			// No parameters defined?
			return result;
		}

		for(DatasourceParameterVO paramvo : lstParams) {
			final String sValue;
			if("java.lang.String".equals(paramvo.getDatatype())) {
				sValue = "abc";
			}
			else if("java.util.Date".equals(paramvo.getDatatype())) {
				sValue = "01.01.2000";
			}
			else {
				sValue = "123434";
			}
			result.put(paramvo.getParameter(), sValue);
		}

		return result;
   }

   /**
    * invalidate datasource cache
    */
   @Override
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
   @Override
@RolesAllowed("Login")
   public Collection<DynamicEntityVO> getDynamicEntities() throws CommonPermissionException {
//   	this.checkReadAllowed(ENTITY_NAME_DYNAMICENTITY);
   	return DatasourceCache.getInstance().getAllDynamicEntities();
   }

   /**
    * get dynamic entity value object
    *
    * @param iDynamicEntityId
    *                primary key of dynamic entity
    * @return DynamicEntityVO
    */
   @Override
@RolesAllowed("Login")
   public DynamicEntityVO getDynamicEntity(Integer iDynamicEntityId) throws CommonPermissionException {
//   	this.checkReadAllowed(ENTITY_NAME_DYNAMICENTITY);
   	return DatasourceCache.getInstance().getDynamicEntity(iDynamicEntityId);
   }

   /**
    * get all ValuelistProvider
    *
    * @return set of ValuelistProviderVO
    * @throws CommonPermissionException
    */
   @Override
@RolesAllowed("Login")
   public Collection<ValuelistProviderVO> getValuelistProvider() throws CommonPermissionException {
//   	this.checkReadAllowed(ENTITY_NAME_VALUELISTPROVIDER);
   	return DatasourceCache.getInstance().getAllValuelistProvider();
   }

   /**
    * get valuelist provider value object
    *
    * @param iValuelistProviderId
    *                primary key of valuelist provider
    * @return ValuelistProviderVO
    */
   @Override
@RolesAllowed("Login")
   public ValuelistProviderVO getValuelistProvider(Integer iValuelistProviderId) throws CommonPermissionException {
//   	this.checkReadAllowed(ENTITY_NAME_VALUELISTPROVIDER);
   	return DatasourceCache.getInstance().getValuelistProvider(iValuelistProviderId);
   }

   /**
    * get all RecordGrant
    *
    * @return set of RecordGrantVO
    * @throws CommonPermissionException
    */
   @Override
@RolesAllowed("Login")
   public Collection<RecordGrantVO> getRecordGrant() throws CommonPermissionException {
//   	this.checkReadAllowed(ENTITY_NAME_VALUELISTPROVIDER);
   	return DatasourceCache.getInstance().getAllRecordGrant();
   }

   /**
    * get RecordGrant value object
    *
    * @param iRecordGrantId
    *                primary key of RecordGrant
    * @return RecordGrantVO
    */
   @Override
@RolesAllowed("Login")
   public RecordGrantVO getRecordGrant(Integer iRecordGrantId) throws CommonPermissionException {
//   	this.checkReadAllowed(ENTITY_NAME_VALUELISTPROVIDER);
   	return DatasourceCache.getInstance().getRecordGrant(iRecordGrantId);
   }

   /**
    * get RecordGrant value object
    *
    * @param sRecordGrant
    *                name of RecordGrant
    * @return RecordGrant value object
    */
   @Override
@RolesAllowed("Login")
   public RecordGrantVO getRecordGrant(String sRecordGrant) throws CommonFinderException, CommonPermissionException {
   	return DatasourceCache.getInstance().getRecordGrantByName(sRecordGrant);
   }

	/**
	 * get a datasource result by datasource id
	 * @param iDatasourceId
	 * @param mpParams
	 * @param iMaxRowCount
	 * @return
	 * @throws NuclosReportException
	 * @throws CommonFinderException
	 */
	@Override
	public ResultVO executeQuery(Integer iDatasourceId, Map<String, Object> mpParams, Integer iMaxRowCount) throws NuclosDatasourceException, CommonFinderException {
		final ResultVO result;
		DatasourceFacadeLocal facade = ServiceLocator.getInstance().getFacade(DatasourceFacadeLocal.class);
		result = executeQuery(facade.getDatasourceById(iDatasourceId).getSource(), mpParams, iMaxRowCount);

		return result;
	}

	/**
	 * gets a datasource result by datasource xml
	 * @param sDatasourceXML datasource id
	 * @param mpParams parameters
	 * @param iMaxRowCount
	 * @return report/form filled with data
	 */
	@Override
	public ResultVO executeQuery(String sDatasourceXML, Map<String, Object> mpParams, Integer iMaxRowCount) throws CommonFinderException, NuclosDatasourceException {
		final ResultVO result = new ResultVO();
		final String sQuery = createSQL(sDatasourceXML, mpParams);

		return DataBaseHelper.getDbAccess().executePlainQueryAsResultVO(sQuery, iMaxRowCount==null?-1:iMaxRowCount);
	}

	@Override
	public Schema getSchemaTables() {
		//return SchemaCache.getSchema(ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_NUCLOS_SCHEMA));
		return SchemaCache.getInstance().getCurrentSchema();
	}

	/**
	 * @throws CommonPermissionException
	 */
	@Override
	public Table getSchemaColumns(Table table) {
		//SchemaCache.getColumns(ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_NUCLOS_SCHEMA), table);
		//SchemaCache.getConstraints(ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_NUCLOS_SCHEMA), table);
		SchemaCache.fillTableColumnsAndConstraints(table);
		return table;
	}

	@Override
    public String createSQLOriginalParameter(String sDatasourceXML)
        throws NuclosDatasourceException {
	    return DatasourceServerUtils.createSQLOriginalParameter(sDatasourceXML);
    }

}
