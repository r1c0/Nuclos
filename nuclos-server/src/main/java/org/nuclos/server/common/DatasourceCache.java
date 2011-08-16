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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.dal.processor.ProcessorFactorySingleton;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.masterdata.MasterDataWrapper;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.report.valueobject.DatasourceVO;
import org.nuclos.server.report.valueobject.DynamicEntityVO;
import org.nuclos.server.report.valueobject.RecordGrantVO;
import org.nuclos.server.report.valueobject.ValuelistProviderVO;

/**
 * Cache for all Datasources.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars Rueckemann</a>
 * @version	01.00.00
 */
public class DatasourceCache {
	private static final Logger log = Logger.getLogger(DatasourceCache.class);

	private static DatasourceCache singleton;

	/** map which contains all datasources */
	private Map<Integer, DatasourceVO> mpDatasourcesById = null;

	/** map which contains the datasources where the user has at least read permission */
	private Map<String, List<DatasourceVO>> mpDatasourcesByCreator = null;

	/** map which contains all datasources */
	private Map<Integer, ValuelistProviderVO> mpValuelistProviderById = null;

	/** map which contains all record grants */
	private Map<Integer, RecordGrantVO> mpRecordGrantById = null;

	/** map which contains all datasources */
	private Map<Integer, DynamicEntityVO> mpDynamicEntitiesById = null;


	private DatasourceCache() {
		findDatasourcesById();
	}

	public static synchronized DatasourceCache getInstance() {
		if (singleton == null) {
			singleton = new DatasourceCache();
		}
		return singleton;
	}

	private synchronized void findDatasourcesById() {
		//log.info("Initializing DatasourceCache");
		mpDatasourcesById = Collections.synchronizedMap(new HashMap<Integer, DatasourceVO>());
//		try {
//			MasterDataFacadeLocalHome mdFacadeHome = ((MasterDataFacadeLocalHome)ServiceLocator.getInstance().getLocalHome(MasterDataFacadeLocalHome.JNDI_NAME));
//			MasterDataFacadeLocal mdFacade = mdFacadeHome.create();
			for (EntityObjectVO eoVO : NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.DATASOURCE).getAll()) {
				mpDatasourcesById.put(eoVO.getId().intValue(), MasterDataWrapper.getDatasourceVO(DalSupportForMD.wrapEntityObjectVO(eoVO), "INITIAL"));
			}
//		}
//		catch (CreateException ex) {
//			throw new NuclosFatalException(ex);
//		}
			mpValuelistProviderById = Collections.synchronizedMap(new HashMap<Integer, ValuelistProviderVO>());
			for (EntityObjectVO eoVO : NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.VALUELISTPROVIDER).getAll()) {
				mpValuelistProviderById.put(eoVO.getId().intValue(), MasterDataWrapper.getValuelistProviderVO(DalSupportForMD.wrapEntityObjectVO(eoVO)));
			}
			mpDynamicEntitiesById = Collections.synchronizedMap(new HashMap<Integer, DynamicEntityVO>());
			for (EntityObjectVO eoVO : NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.DYNAMICENTITY).getAll()) {
				mpDynamicEntitiesById.put(eoVO.getId().intValue(), MasterDataWrapper.getDynamicEntityVO(DalSupportForMD.wrapEntityObjectVO(eoVO)));
			}
			mpRecordGrantById = Collections.synchronizedMap(new HashMap<Integer, RecordGrantVO>());
			for (EntityObjectVO eoVO : NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.RECORDGRANT).getAll()) {
				mpRecordGrantById.put(eoVO.getId().intValue(), MasterDataWrapper.getRecordGrantVO(DalSupportForMD.wrapEntityObjectVO(eoVO)));
			}
		//log.info("Finished initializing DatasourceCache.");
	}

	/**
	 * initialize the map of datasources by creator
	 * @param sCreator
	 */
	private synchronized void findDataSourcesByCreator(String sCreator) {
		log.debug("Initializing DatasourceCacheByCreator");
		mpDatasourcesByCreator = Collections.synchronizedMap(new HashMap<String, List<DatasourceVO>>());
		try {
			MasterDataFacadeLocal mdFacade = ServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
			List<DatasourceVO> datasources = new ArrayList<DatasourceVO>();

			DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
			DbQuery<Integer> query = builder.createQuery(Integer.class);
			DbFrom t = query.from("T_UD_DATASOURCE").alias(ProcessorFactorySingleton.BASE_ALIAS);
			query.select(t.column("INTID", Integer.class));
			query.where(builder.equal(t.column("STRCREATED", String.class), sCreator));

	      for (Integer id : DataBaseHelper.getDbAccess().executeQuery(query.distinct(true))) {
	      	MasterDataVO mdVO = mdFacade.get(NuclosEntity.DATASOURCE.getEntityName(),id);
	      	if (getPermission(mdVO.getIntId(), sCreator) != DatasourceVO.PERMISSION_NONE) {
					datasources.add(MasterDataWrapper.getDatasourceVO(mdVO, sCreator));
	      	}
	      }

			mpDatasourcesByCreator.put(sCreator,datasources);
		}
		catch (CommonPermissionException ex) {
			throw new NuclosFatalException(ex);
		}
		catch (CommonFinderException ex) {
		}
		log.debug("Finished initializing DatasourceCacheByCreator.");
	}

	/**
	 * Invalidate the cache
	 */
	public synchronized void invalidate() {
		log.debug("Invalidating DatasourceCache");
		mpDatasourcesById = null;
		mpDatasourcesByCreator = null;
		mpValuelistProviderById = null;
		mpRecordGrantById = null;
		mpDynamicEntitiesById = null;

		DatasourceServerUtils.SQLCACHE.invalidate();
		findDatasourcesById();
	}

	/**
	 * get a collection of all Datasources where the given user has at least read permission
	 * @return Collection<DatasourceVO>
	 */
	public synchronized Collection<DatasourceVO> getAllDatasources(final String sUser) {
		List<DatasourceVO>result = new ArrayList<DatasourceVO>();

		if (mpDatasourcesById == null)
			findDatasourcesById();

		for (DatasourceVO datasourceVO : mpDatasourcesById.values()) {
			if(getPermission(datasourceVO.getId(), sUser) != DatasourceVO.PERMISSION_NONE) {
				result.add(datasourceVO);
			}
		}
		return result;
	}

	/**
	 * get all Datasources without checking user permissions
	 * @return
	 */
	public synchronized Collection<DatasourceVO> getAllDatasources() {
		List<DatasourceVO>result = new ArrayList<DatasourceVO>();

		if (mpDatasourcesById == null)
			findDatasourcesById();

		result.addAll(mpDatasourcesById.values());

		return result;
	}

	/**
	 * get all valuelist provider
	 * @return
	 */
	public synchronized Collection<ValuelistProviderVO> getAllValuelistProvider() {
		List<ValuelistProviderVO>result = new ArrayList<ValuelistProviderVO>();

		if (mpValuelistProviderById == null)
			findDatasourcesById();

		result.addAll(mpValuelistProviderById.values());

		return result;
	}

	/**
	 * get a ValuelistProvider
	 * @param iValuelistProviderId
	 * @return
	 */
	public synchronized ValuelistProviderVO getValuelistProvider(Integer iValuelistProviderId) {
		if (mpValuelistProviderById == null)
			findDatasourcesById();

		return mpValuelistProviderById.get(iValuelistProviderId);
	}

	/**
	 * get all record grant
	 * @return
	 */
	public synchronized Collection<RecordGrantVO> getAllRecordGrant() {
		List<RecordGrantVO>result = new ArrayList<RecordGrantVO>();

		if (mpRecordGrantById == null)
			findDatasourcesById();

		result.addAll(mpRecordGrantById.values());

		return result;
	}

	/**
	 * get a record grant
	 * @param iRecordGrantId
	 * @return
	 */
	public synchronized RecordGrantVO getRecordGrant(Integer iRecordGrantId) {
		if (mpRecordGrantById == null)
			findDatasourcesById();

		return mpRecordGrantById.get(iRecordGrantId);
	}

	/**
	 * get all dynamic entities
	 * @return
	 */
	public synchronized Collection<DynamicEntityVO> getAllDynamicEntities() {
		List<DynamicEntityVO>result = new ArrayList<DynamicEntityVO>();

		if (mpDynamicEntitiesById == null)
			findDatasourcesById();

		result.addAll(mpDynamicEntitiesById.values());

		return result;
	}

	/**
	 * get a DynamicEntity
	 * @param iDynamicEntityId
	 * @return
	 */
	public synchronized DynamicEntityVO getDynamicEntity(Integer iDynamicEntityId) {
		if (mpDynamicEntitiesById == null)
			findDatasourcesById();

		return mpDynamicEntitiesById.get(iDynamicEntityId);
	}

	/**
	 * get a Datasource by id regardless of permisssions
	 * @param iDatasourceId
	 * @return
	 * @throws CommonPermissionException
	 */
	public synchronized DatasourceVO get(Integer iDatasourceId) {
		if (mpDatasourcesById == null)
			findDatasourcesById();

		return mpDatasourcesById.get(iDatasourceId);
	}

	/**
	 * get a Datasource by id where the given user has at least read permission
	 * @param iDatasourceId
	 * @param sUserName
	 * @return
	 * @throws CommonPermissionException
	 */
	public synchronized DatasourceVO getDatasourcesById(Integer iDatasourceId, String sUserName) throws CommonPermissionException {
		if (mpDatasourcesById == null)
			findDatasourcesById();

		if (getPermission(iDatasourceId, sUserName) == DatasourceVO.PERMISSION_NONE) {
			throw new CommonPermissionException("datasource.cach.missing.permission");//"Sie haben kein Recht die Datenquelle zu lesen.");
		}
		return mpDatasourcesById.get(iDatasourceId);
	}

	/**
	 * get datasources by creator
	 * @param sUserName
	 * @return
	 */
	public synchronized Collection<DatasourceVO> getDatasourcesByCreator(String sUserName) {
		if(mpDatasourcesByCreator == null || mpDatasourcesByCreator.get(sUserName) == null) {
			findDataSourcesByCreator(sUserName);
		}
		return CollectionUtils.emptyIfNull(mpDatasourcesByCreator.get(sUserName));
	}

	/**
	 * get a datasource by name (used to check if different datasource exists with the same name).
	 * @param sDatasourceName
	 * @return DatasourceVO (may be null)
	 */
	public synchronized DatasourceVO getDatasourceByName(String sDatasourceName) {
		if (mpDatasourcesById == null)
			findDatasourcesById();

		DatasourceVO result = null;
		for(DatasourceVO dsvo : mpDatasourcesById.values()) {
			if(dsvo.getName().equals(sDatasourceName)) {
				result = dsvo;
				break;
			}
		}

		return result;
	}

	/**
	 * get a valuelist provider by name.
	 * @param sValuelistProviderName
	 * @return ValuelistProviderVO (may be null)
	 */
	public synchronized ValuelistProviderVO getValuelistProviderByName(String sValuelistProviderName) {
		if (mpDatasourcesById == null)
			findDatasourcesById();

		ValuelistProviderVO result = null;
		for(ValuelistProviderVO vpvo : mpValuelistProviderById.values()) {
			if(vpvo.getName().equals(sValuelistProviderName)) {
				result = vpvo;
				break;
			}
		}

		return result;
	}

	/**
	 * get a record grant by name.
	 * @param sRecordGrantName
	 * @return RecordGrantVO (may be null)
	 */
	public synchronized RecordGrantVO getRecordGrantByName(String sRecordGrantName) {
		if (mpRecordGrantById == null)
			findDatasourcesById();

		RecordGrantVO result = null;
		for(RecordGrantVO vpvo : mpRecordGrantById.values()) {
			if(vpvo.getName().equals(sRecordGrantName)) {
				result = vpvo;
				break;
			}
		}

		return result;
	}

	/**
	 * get a dynamic entity by name.
	 * @param sDynamicEntityName
	 * @return DynamicEntityVO (may be null)
	 */
	public synchronized DynamicEntityVO getDynamicEntityByName(String sDynamicEntityName) {
		if (mpDatasourcesById == null)
			findDatasourcesById();

		DynamicEntityVO result = null;
		for(DynamicEntityVO devo : mpDynamicEntitiesById.values()) {
			if(devo.getName().equalsIgnoreCase(sDynamicEntityName)) {
				result = devo;
				break;
			}
		}

		return result;
	}

	/**
	 * helper function to get the permission for a given user from the SecurityCache
	 * @param iDatasourceId
	 * @param sUserName
	 * @return
	 */
	public int getPermission(Integer iDatasourceId, String sUserName) {
		final int result;

		if (SecurityCache.getInstance().getWritableDataSourceIds(sUserName).contains(iDatasourceId)) {
			result = DatasourceVO.PERMISSION_READWRITE;
		}
		else if (SecurityCache.getInstance().getReadableDataSources(sUserName).contains(iDatasourceId)) {
			result = DatasourceVO.PERMISSION_READONLY;
		}
		else {
			result = DatasourceVO.PERMISSION_NONE;
		}
		return result;
		//return DatasourceVO.PERMISSION_READWRITE;
	}

}
