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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.nuclos.common.EntityTreeViewVO;
import org.nuclos.common.MasterDataMetaProvider;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.server.database.SpringDataBaseHelper;
import org.nuclos.server.dblayer.structure.DbArtifact;
import org.nuclos.server.dblayer.structure.DbColumn;
import org.nuclos.server.dblayer.structure.DbTable;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaFieldVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.mbean.MBeanAgent;
import org.nuclos.server.mbean.MasterDataMetaCacheMBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Master data cache containing meta information about masterdata entities.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph Radig</a>
 * @version 00.01.000
 */
public class MasterDataMetaCache implements MasterDataMetaCacheMBean, MasterDataMetaProvider, InitializingBean {
	
	private static final Logger LOG = Logger.getLogger(MasterDataMetaCache.class);
	
	private static MasterDataMetaCache INSTANCE;
	
	// 
	
	private final Map<String, MasterDataMetaVO> mp
		= new ConcurrentHashMap<String, MasterDataMetaVO>();
	
	private final Map<String, Collection<MasterDataVO>> mpSubNodes 
		= new ConcurrentHashMap<String, Collection<MasterDataVO>>();
	
	private final Map<String, Collection<EntityTreeViewVO>> subNodes 
		= new ConcurrentHashMap<String, Collection<EntityTreeViewVO>>();
	
	private SpringDataBaseHelper dataBaseHelper;
	
	private MetaDataServerProvider metaDataServerProvider;

	MasterDataMetaCache() {
		INSTANCE = this;
	}

	/**
	 * @return the one and only instance of the <code>MasterDataMetaCache</code>.
	 */
	public static MasterDataMetaCache getInstance() {
		return INSTANCE;
	}
	
	@Autowired
	void setDataBaseHelper(SpringDataBaseHelper dataBaseHelper) {
		this.dataBaseHelper = dataBaseHelper;
	}
	
	@Autowired
	void setMetaDataServerProvider(MetaDataServerProvider metaDataServerProvider) {
		this.metaDataServerProvider = metaDataServerProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		MBeanAgent.registerCache(this, MasterDataMetaCacheMBean.class);
	}

	/**
	 * revalidates the cache. This may be used for development purposes only, in order to rebuild the cache
	 * after metadata entries in the database were changed.
	 */
	@Override
	public synchronized void revalidate() {
		mpSubNodes.clear();
		subNodes.clear();
		mp.clear();
		mp.putAll(buildMap());
	}

	/**
	 * @return Collection<MasterDataMetaVO> the masterdata meta information for the all entities.
	 */
	public Collection<MasterDataMetaVO> getAllMetaData() {
		// Note that we need to return a copy here as mp.values() is not serializable:
		return new ArrayList<MasterDataMetaVO>(getMp().values());
	}

	/**
	 * @param sEntityName
	 * @return the masterdata meta information for the given entity.
	 * @postcondition result != null
	 */
	@Override
	public MasterDataMetaVO getMetaData(String sEntityName) {
		final MasterDataMetaVO result = getMp().get(sEntityName);
		if (result == null) {
			throw new NuclosFatalException("Master data meta information for entity \"" + sEntityName + "\" is not available.");
		}
		assert result != null;
		return result;
	}

	public MasterDataMetaVO getMetaData(NuclosEntity entity) {
		return getMetaData(entity.getEntityName());
	}

	public MasterDataMetaVO findMetaData(String sEntityName) {
		return getMp().get(sEntityName);
	}

	/**
	 * @param iId id of the metadata entry
	 * @return the masterdata meta information for the given entity, if any.
	 */
	public MasterDataMetaVO getMasterDataMetaById(Integer iId) {
		for (MasterDataMetaVO result : getMp().values()) {
			if (iId.equals(result.getId())) {
				return result;
			}
		}
		return null;
	}

	/**
	 * does the given entity exists
     */
	public boolean exist(String sEntityName) {
		return getMp().containsKey(sEntityName) && !Modules.getInstance().isModuleEntity(sEntityName);
	}

	/**
	 * @param sEntityName
	 * @return Does the entity with the given name use the rule engine?
	 */
	public boolean getUsesRuleEngine(String sEntityName) {
		Modules moduleprovider = Modules.getInstance();

		return (!isSubForm(sEntityName)) ||
		   (moduleprovider.isModuleEntity(sEntityName) && moduleprovider.getUsesRuleEngine(moduleprovider.getModuleIdByEntityName(sEntityName)));
	}

	/**
	 * @param sEntityName
	 * @return Is the entity with the given name a subform?
	 */
	public boolean isSubForm(String sEntityName) {
		return this.getMetaData(sEntityName).getMenuPath() == null;
	}

	/**
	 * @param iId id of the metadata entry
	 * @return the masterdata meta information for the given entity, if any.
	 */
	@Override
	public MasterDataMetaVO getMetaDataById(Integer iId) {
		for (MasterDataMetaVO result : getMp().values()) {
			if (iId.equals(result.getId())) {
				return result;
			}
		}
		return null;
	}

	/**
	 * @param dbEntities a list of DB entity names of the metadata entry
	 * @return a list of masterdata meta information for the given entities, if any.
	 */
	public List<MasterDataMetaVO> getMetaDataByDBEntityNames(List<String> dbEntities) {
		List<MasterDataMetaVO> result = new ArrayList<MasterDataMetaVO>();
		for (MasterDataMetaVO current : getMp().values()) {
			if (dbEntities.contains(current.getDBEntity())) {
				result.add(current);
			}
		}
		return result;
	}

	/**
	 * @return a map that may only be read from, never written to.
	 * @postcondition result != null
	 */
	private Map<String, MasterDataMetaVO> buildMap() {
		LOG.debug("START building masterdata cache.");

		final Map<String, MasterDataMetaVO> result = new HashMap<String, MasterDataMetaVO>();

		for (EntityMetaDataVO eMeta : metaDataServerProvider.getAllEntities()) {
			result.put(eMeta.getEntity(), DalSupportForMD.wrapEntityMetaDataVOInMasterData(eMeta,
				metaDataServerProvider.getAllEntityFieldsByEntity(eMeta.getEntity()).values()));
		}

		LOG.debug("FINISHED building masterdata cache.");
		assert result != null;
		return result;
	}

	/**
	 * validates the meta data in the database.
	 */
	public void validateMetaDataInDB() {
		for (MasterDataMetaVO mdmetavo : getAllMetaData()) {
			try {
				this.validate(mdmetavo);
			}
			catch (Exception e) {
				LOG.warn("validateMetaDataInDB failed: " + e, e);
				// continue with the loop...
			}
		}
	}

	private void validate(final MasterDataMetaVO mdmetavo) throws SQLException {
		final boolean log = LOG.isDebugEnabled();
		final String sEntityName = mdmetavo.getEntityName();
		if (log) LOG.debug("sEntityName = " + sEntityName);
		final String sDbEntityName = mdmetavo.getDBEntity();
		DbTable table = dataBaseHelper.getDbAccess().getTableMetaData(sDbEntityName);

		Map<String, DbColumn> columnsByName = new TreeMap<String, DbColumn>(String.CASE_INSENSITIVE_ORDER);
		columnsByName.putAll(DbArtifact.makeSimpleNameMap(table.getTableColumns()));
		for (MasterDataMetaFieldVO mdmetafieldvos : mdmetavo.getFields()) {
			final String sFieldName = mdmetafieldvos.getFieldName();
			final String sColumnName = mdmetafieldvos.getDBFieldName();
			final DbColumn column = columnsByName.get(sColumnName);
			if (column != null) {
				if (log) {
					LOG.debug("  sColumnName = " + sColumnName);
					LOG.debug("  rsmd.getColumnTypeName(iColumn) = " + column.getColumnType().getTypeName());
					LOG.debug("  rsmd.getScale(iColumn) = " + column.getColumnType().getScale());
					LOG.debug("  rsmd.getPrecision(iColumn) = " + column.getColumnType().getPrecision());
				}
			} else {
				if (log)
					LOG.debug("Ung\u00fcltiger Spaltenname " + sColumnName + " in Entit\u00e4t " + sEntityName + ", Feld " + sFieldName + ".");
			}
		}
	}

	/**
	 * @deprecated Todo: Get rid of it! (Thomas Pasch)
	 */
	public Collection<MasterDataVO> getSubnodesMD(String sEntity, Object oId) {
		if(mpSubNodes.get(sEntity) == null) {
			LOG.info("Initilizing SubnodeMD Metainformation for entity " + sEntity);

			final MasterDataFacadeLocal mdLocal = ServerServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
			final Collection<MasterDataVO> colSubNodes = mdLocal.getDependantMasterData(NuclosEntity.ENTITYSUBNODES.getEntityName(), EntityTreeViewVO.ENTITY_FIELD, oId);
			mpSubNodes.put(sEntity, colSubNodes);
		}
		return mpSubNodes.get(sEntity);
	}

	public Collection<EntityTreeViewVO> getSubnodesETV(String sEntity, Object oId) {
		if(subNodes.get(sEntity) == null) {
			LOG.info("Initilizing SubnodeETV Metainformation for entity " + sEntity);

			final MasterDataFacadeLocal mdLocal = ServerServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
			final Collection<EntityTreeViewVO> colSubNodes = mdLocal.getDependantSubnodes(NuclosEntity.ENTITYSUBNODES.getEntityName(), EntityTreeViewVO.ENTITY_FIELD, oId);
			subNodes.put(sEntity, colSubNodes);
		}
		return subNodes.get(sEntity);
	}

	@Override
	public int getEntityCount() {
		// return mp != null ? mp.size() : 0;
		return getMp().size();
	}

	@Override
	public int getSubEntityCount() {
		return mpSubNodes != null ? mpSubNodes.size() : 0;
	}

	@Override
	public Collection<String> showEntities() {
		// return mp != null ? new ArrayList<String>(mp.keySet()) : null;
		return new ArrayList<String>(getMp().keySet());
	}

	@Override
	public Collection<String> showSubEntities() {
		return mpSubNodes != null ? new ArrayList<String>(mpSubNodes.keySet()) : null;
	}
	
	private Map<String, MasterDataMetaVO> getMp() {
		if (mp.isEmpty()) {
			revalidate();
		}
		return mp;
	}

}	// class MasterDataMetaCache
