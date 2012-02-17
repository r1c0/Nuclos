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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common.dal.util.DalTransformations;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.database.SpringDataBaseHelper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Cache for all Datasources.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars Rueckemann</a>
 * 
 * TODO: Re-check if all methods must be synchronized! (tp)
 */
@Component
public class DatasourceCache {
	
	private static final Logger LOG = Logger.getLogger(DatasourceCache.class);

	private static DatasourceCache INSTANCE;
	
	//

	/** map which contains all datasources */
	private final Map<Integer, DatasourceVO> mpDatasourcesById 
		= new ConcurrentHashMap<Integer, DatasourceVO>();

	/** map which contains the datasources where the user has at least read permission */
	private final Map<String, List<DatasourceVO>> mpDatasourcesByCreator 
		= new ConcurrentHashMap<String, List<DatasourceVO>>();

	/** map which contains all datasources */
	private final Map<Integer, ValuelistProviderVO> mpValuelistProviderById 
		= new ConcurrentHashMap<Integer, ValuelistProviderVO>();

	/** map which contains all record grants */
	private final Map<Integer, RecordGrantVO> mpRecordGrantById 
		= new ConcurrentHashMap<Integer, RecordGrantVO>();

	/** map which contains all datasources */
	private final Map<Integer, DynamicEntityVO> mpDynamicEntitiesById 
		= new ConcurrentHashMap<Integer, DynamicEntityVO>();
	
	private final Map<String, DynamicEntityVO> mapDynamicEntities
		= new ConcurrentHashMap<String, DynamicEntityVO>();
	
	private DatasourceServerUtils datasourceServerUtils;
	
	private SpringDataBaseHelper dataBaseHelper;
	
	private SecurityCache securityCache;
	
	private NucletDalProvider nucletDalProvider;
	

	DatasourceCache() {
		INSTANCE = this;
	}

	public static DatasourceCache getInstance() {
		return INSTANCE;
	}
	
	@PostConstruct
	public final void init() {
		findDatasourcesById();		
	}
	
	@Autowired
	void setDatasourceServerUtils(DatasourceServerUtils datasourceServerUtils) {
		this.datasourceServerUtils = datasourceServerUtils;
	}
	
	@Autowired
	void setDataBaseHelper(SpringDataBaseHelper dataBaseHelper) {
		this.dataBaseHelper = dataBaseHelper;
	}
	
	@Autowired
	void setSecurityCache(SecurityCache securityCache) {
		this.securityCache = securityCache;
	}
	
	@Autowired
	void setNucletDalProvider(NucletDalProvider nucletDalProvider) {
		this.nucletDalProvider = nucletDalProvider;
	}

	private void findDatasourcesById() {
		LOG.info("Initializing DatasourceCache");
		// Check if it is 'too early'
		NucletDalProvider.getInstance();
		for (EntityObjectVO eoVO : 
			nucletDalProvider.getEntityObjectProcessor(NuclosEntity.DATASOURCE).getAll()) {
			mpDatasourcesById.put(eoVO.getId().intValue(),
					MasterDataWrapper.getDatasourceVO(DalSupportForMD.wrapEntityObjectVO(eoVO), "INITIAL"));
		}
		for (EntityObjectVO eoVO : 
			nucletDalProvider.getEntityObjectProcessor(NuclosEntity.VALUELISTPROVIDER).getAll()) {
			mpValuelistProviderById.put(eoVO.getId().intValue(),
					MasterDataWrapper.getValuelistProviderVO(DalSupportForMD.wrapEntityObjectVO(eoVO)));
		}
		for (EntityObjectVO eoVO : 
			nucletDalProvider.getEntityObjectProcessor(NuclosEntity.DYNAMICENTITY).getAll()) {
			mpDynamicEntitiesById.put(eoVO.getId().intValue(),
					MasterDataWrapper.getDynamicEntityVO(DalSupportForMD.wrapEntityObjectVO(eoVO)));
		}
		for (EntityObjectVO eoVO : 
			nucletDalProvider.getEntityObjectProcessor(NuclosEntity.RECORDGRANT).getAll()) {
			mpRecordGrantById.put(eoVO.getId().intValue(),
					MasterDataWrapper.getRecordGrantVO(DalSupportForMD.wrapEntityObjectVO(eoVO)));
		}
		if (mapDynamicEntities.isEmpty()) {
			mapDynamicEntities.putAll(Collections.unmodifiableMap(CollectionUtils.generateLookupMap(
					mpDynamicEntitiesById.values() /*getAllDynamicEntities()*/, 
					DalTransformations.getDynamicEntityName())));
		}
		LOG.info("Finished initializing DatasourceCache.");
	}

	/**
	 * initialize the map of datasources by creator
	 * @param sCreator
	 */
	private void findDataSourcesByCreator(String sCreator) {
		LOG.debug("Initializing DatasourceCacheByCreator");
		try {
			MasterDataFacadeLocal mdFacade = ServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
			List<DatasourceVO> datasources = new ArrayList<DatasourceVO>();

			DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
			DbQuery<Integer> query = builder.createQuery(Integer.class);
			DbFrom t = query.from("T_UD_DATASOURCE").alias(SystemFields.BASE_ALIAS);
			query.select(t.baseColumn("INTID", Integer.class));
			query.where(builder.equal(t.baseColumn("STRCREATED", String.class), sCreator));

			for (Integer id : dataBaseHelper.getDbAccess().executeQuery(query.distinct(true))) {
				MasterDataVO mdVO = mdFacade.get(NuclosEntity.DATASOURCE.getEntityName(), id);
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
		LOG.debug("Finished initializing DatasourceCacheByCreator.");
	}

	/**
	 * Invalidate the cache
	 */
	public void invalidate() {
		LOG.debug("Invalidating DatasourceCache");
		mpDatasourcesById.clear();
		mpDatasourcesByCreator.clear();
		mpValuelistProviderById.clear();
		mpRecordGrantById.clear();
		mpDynamicEntitiesById.clear();

		datasourceServerUtils.invalidateCache();
		findDatasourcesById();
	}

	/**
	 * get a collection of all Datasources where the given user has at least read permission
	 * @return Collection<DatasourceVO>
	 */
	public Collection<DatasourceVO> getAllDatasources(final String sUser) {
		List<DatasourceVO>result = new ArrayList<DatasourceVO>();

		if (mpDatasourcesById.isEmpty())
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
	public Collection<DatasourceVO> getAllDatasources() {
		List<DatasourceVO>result = new ArrayList<DatasourceVO>();

		if (mpDatasourcesById.isEmpty())
			findDatasourcesById();

		result.addAll(mpDatasourcesById.values());

		return result;
	}

	/**
	 * Get the base entity name of a dynamic entity.
	 *
	 * @param dynamicentityname The name of the dynamic entity.
	 * @return Returns the base entity name. Returns the original entity name if there is no dynamic entity with the given name.
	 */
	public String getBaseEntity(String dynamicentityname) {
		if (mapDynamicEntities.containsKey(dynamicentityname)) {
			return mapDynamicEntities.get(dynamicentityname).getEntity();
		}
		else {
			return dynamicentityname;
		}
	}
	
	/**
	 * get all valuelist provider
	 * @return
	 */
	public Collection<ValuelistProviderVO> getAllValuelistProvider() {
		List<ValuelistProviderVO> result = new ArrayList<ValuelistProviderVO>();

		if (mpValuelistProviderById.isEmpty())
			findDatasourcesById();

		result.addAll(mpValuelistProviderById.values());

		return result;
	}

	/**
	 * get a ValuelistProvider
	 * @param iValuelistProviderId
	 * @return
	 */
	public ValuelistProviderVO getValuelistProvider(Integer iValuelistProviderId) {
		if (iValuelistProviderId == null) {
			return null;
		}
		if (mpValuelistProviderById.isEmpty()) {
			findDatasourcesById();
		}
		return mpValuelistProviderById.get(iValuelistProviderId);
	}

	/**
	 * get all record grant
	 * @return
	 */
	public Collection<RecordGrantVO> getAllRecordGrant() {
		List<RecordGrantVO> result = new ArrayList<RecordGrantVO>();

		if (mpRecordGrantById.isEmpty())
			findDatasourcesById();

		result.addAll(mpRecordGrantById.values());

		return result;
	}

	/**
	 * get a record grant
	 * @param iRecordGrantId
	 * @return
	 */
	public RecordGrantVO getRecordGrant(Integer iRecordGrantId) {
		if (mpRecordGrantById.isEmpty())
			findDatasourcesById();

		return mpRecordGrantById.get(iRecordGrantId);
	}

	/**
	 * get all dynamic entities
	 * @return
	 */
	public Collection<DynamicEntityVO> getAllDynamicEntities() {
		List<DynamicEntityVO>result = new ArrayList<DynamicEntityVO>();

		if (mpDynamicEntitiesById.isEmpty())
			findDatasourcesById();

		result.addAll(mpDynamicEntitiesById.values());

		return result;
	}

	/**
	 * get a DynamicEntity
	 * @param iDynamicEntityId
	 * @return
	 */
	public DynamicEntityVO getDynamicEntity(Integer iDynamicEntityId) {
		if (mpDynamicEntitiesById.isEmpty())
			findDatasourcesById();

		return mpDynamicEntitiesById.get(iDynamicEntityId);
	}

	/**
	 * get a Datasource by id regardless of permisssions
	 * @param iDatasourceId
	 * @return
	 * @throws CommonPermissionException
	 */
	public DatasourceVO get(Integer iDatasourceId) {
		if (mpDatasourcesById.isEmpty())
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
	public DatasourceVO getDatasourcesById(Integer iDatasourceId, String sUserName) throws CommonPermissionException {
		if (mpDatasourcesById.isEmpty())
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
	public Collection<DatasourceVO> getDatasourcesByCreator(String sUserName) {
		if(mpDatasourcesByCreator.isEmpty() || mpDatasourcesByCreator.get(sUserName) == null) {
			findDataSourcesByCreator(sUserName);
		}
		return CollectionUtils.emptyIfNull(mpDatasourcesByCreator.get(sUserName));
	}

	/**
	 * get a datasource by name (used to check if different datasource exists with the same name).
	 * @param sDatasourceName
	 * @return DatasourceVO (may be null)
	 */
	public DatasourceVO getDatasourceByName(String sDatasourceName) {
		if (mpDatasourcesById.isEmpty())
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
	public ValuelistProviderVO getValuelistProviderByName(String sValuelistProviderName) {
		if (mpDatasourcesById.isEmpty())
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
	public RecordGrantVO getRecordGrantByName(String sRecordGrantName) {
		if (mpRecordGrantById.isEmpty())
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
	public DynamicEntityVO getDynamicEntityByName(String sDynamicEntityName) {
		if (mpDatasourcesById.isEmpty())
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

		if (securityCache.getWritableDataSourceIds(sUserName).contains(iDatasourceId)) {
			result = DatasourceVO.PERMISSION_READWRITE;
		}
		else if (securityCache.getReadableDataSources(sUserName).contains(iDatasourceId)) {
			result = DatasourceVO.PERMISSION_READONLY;
		}
		else {
			result = DatasourceVO.PERMISSION_NONE;
		}
		return result;
		//return DatasourceVO.PERMISSION_READWRITE;
	}

}
