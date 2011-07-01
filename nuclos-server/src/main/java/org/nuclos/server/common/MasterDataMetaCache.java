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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.nuclos.common.EntityTreeViewVO;
import org.nuclos.common.MasterDataMetaProvider;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.server.database.DataBaseHelper;
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
	
	private final Logger log = Logger.getLogger(this.getClass());

	private Map<String, MasterDataMetaVO> mp;
	
	private Map<String, Collection<MasterDataVO>> mpSubNodes = new HashMap<String, Collection<MasterDataVO>>();
	
	private Map<String, Collection<EntityTreeViewVO>> subNodes = new HashMap<String, Collection<EntityTreeViewVO>>();

	private static final Set<String> stExcludedFieldNamesForDynamicGeneration = new HashSet<String>(
			Arrays.asList("INTID", "DATCREATED", "STRCREATED", "DATCHANGED", "STRCHANGED", "INTVERSION", "BLNDELETED")
	);

	/**
	 * @return the one and only instance of the <code>MasterDataMetaCache</code>.
	 * @todo OPTIMIZE: remove synchronized by explicitly building the cache when the server starts.
	 */
	public static synchronized MasterDataMetaCache getInstance() {
		return (MasterDataMetaCache) SpringApplicationContextHolder.getBean(MasterDataMetaProvider.class);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		MBeanAgent.registerCache(this, MasterDataMetaCacheMBean.class);
	}

	private MasterDataMetaCache() {
		this.revalidate();
	}

	/**
	 * revalidates the cache. This may be used for development purposes only, in order to rebuild the cache
	 * after metadata entries in the database were changed.
	 */
	@Override
	public synchronized void revalidate() {
		this.mpSubNodes.clear();
		this.subNodes.clear();
		this.mp = this.buildMap();
	}

	/**
	 * @return Collection<MasterDataMetaVO> the masterdata meta information for the all entities.
	 */
	public synchronized Collection<MasterDataMetaVO> getAllMetaData() {
		// Note that we need to return a copy here as mp.values() is not serializable:
		return new ArrayList<MasterDataMetaVO>(mp.values());
	}

	/**
	 * @param sEntityName
	 * @return the masterdata meta information for the given entity.
	 * @postcondition result != null
	 */
	@Override
	public synchronized MasterDataMetaVO getMetaData(String sEntityName) {
		final MasterDataMetaVO result = mp.get(sEntityName);
		if (result == null) {
			throw new NuclosFatalException("Master data meta information for entity \"" + sEntityName + "\" is not available.");
		}
		assert result != null;
		return result;
	}

	public MasterDataMetaVO getMetaData(NuclosEntity entity) {
		return getMetaData(entity.getEntityName());
	}

	public synchronized MasterDataMetaVO findMetaData(String sEntityName) {
		return mp.get(sEntityName);
	}

	/**
	 * @param iId id of the metadata entry
	 * @return the masterdata meta information for the given entity, if any.
	 */
	public synchronized MasterDataMetaVO getMasterDataMetaById(Integer iId) {
		for (MasterDataMetaVO result : mp.values()) {
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
		return mp.containsKey(sEntityName) && !Modules.getInstance().isModuleEntity(sEntityName);
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
	public synchronized MasterDataMetaVO getMetaDataById(Integer iId) {
		for (MasterDataMetaVO result : mp.values()) {
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
	public synchronized List<MasterDataMetaVO> getMetaDataByDBEntityNames(List<String> dbEntities) {
		List<MasterDataMetaVO> result = new ArrayList<MasterDataMetaVO>();
		for (MasterDataMetaVO current : mp.values()) {
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
	private synchronized Map<String, MasterDataMetaVO> buildMap() {
		final Logger logger = Logger.getLogger(this.getClass());
		logger.debug("START building masterdata cache.");

		final Map<String, MasterDataMetaVO> result = new HashMap<String, MasterDataMetaVO>();

		// Add static masterdata entity meta information:
//		for (MasterDataMetaVO mdmetavo : getStaticEntities()) {
//			result.put(mdmetavo.getEntityName(), mdmetavo);
//		}

		// Add dynamic masterdata entity meta information:
//		for (MasterDataMetaVO mdmetavo : getDynamicEntities()) {
//			final String sEntityName = mdmetavo.getEntityName();
//			if (result.containsKey(sEntityName)) {
//				throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("metacache.exception", sEntityName));
//					//"Dynamische Entit\u00e4t \"" + sEntityName + "\" schon unter demselben Namen als statische Entit\u00e4t vorhanden.");
//			}
//			result.put(sEntityName, mdmetavo);
//		}

		for (EntityMetaDataVO eMeta : MetaDataServerProvider.getInstance().getAllEntities()) {
			result.put(eMeta.getEntity(), DalSupportForMD.wrapEntityMetaDataVOInMasterData(eMeta,
				MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(eMeta.getEntity()).values()));
		}

		logger.debug("FINISHED building masterdata cache.");
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
			catch (Exception ex) {
				ex.printStackTrace();
				// continue with the loop...
			}
		}
	}

	private void validate(final MasterDataMetaVO mdmetavo) throws SQLException {
		final String sEntityName = mdmetavo.getEntityName();
		System.out.println("sEntityName = " + sEntityName);
		final String sDbEntityName = mdmetavo.getDBEntity();
		DbTable table = DataBaseHelper.getDbAccess().getTableMetaData(sDbEntityName);

		Map<String, DbColumn> columnsByName = new TreeMap<String, DbColumn>(String.CASE_INSENSITIVE_ORDER);
		columnsByName.putAll(DbArtifact.makeSimpleNameMap(table.getTableColumns()));
		for (MasterDataMetaFieldVO mdmetafieldvos : mdmetavo.getFields()) {
			final String sFieldName = mdmetafieldvos.getFieldName();
			final String sColumnName = mdmetafieldvos.getDBFieldName();
			final DbColumn column = columnsByName.get(sColumnName);
			if (column != null) {
				System.out.println("  sColumnName = " + sColumnName);
				System.out.println("  rsmd.getColumnTypeName(iColumn) = " + column.getColumnType().getTypeName());
				System.out.println("  rsmd.getScale(iColumn) = " + column.getColumnType().getScale());
				System.out.println("  rsmd.getPrecision(iColumn) = " + column.getColumnType().getPrecision());
			} else {
				System.out.println("Ung\u00fcltiger Spaltenname " + sColumnName + " in Entit\u00e4t " + sEntityName + ", Feld " + sFieldName + ".");
			}
		}
	}

	/**
	 * @deprecated Todo: Get rid of it! (Thomas Pasch)
	 */
	public Collection<MasterDataVO> getSubnodesMD(String sEntity, Object oId) {
		if(mpSubNodes.get(sEntity) == null) {
			log.info("Initilizing SubnodeMD Metainformation for entity " + sEntity);

			final MasterDataFacadeLocal mdLocal = ServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
			final Collection<MasterDataVO> colSubNodes = mdLocal.getDependantMasterData(NuclosEntity.ENTITYSUBNODES.getEntityName(), EntityTreeViewVO.ENTITY_FIELD, oId);
			mpSubNodes.put(sEntity, colSubNodes);
		}
		return mpSubNodes.get(sEntity);
	}

	public Collection<EntityTreeViewVO> getSubnodesETV(String sEntity, Object oId) {
		if(subNodes.get(sEntity) == null) {
			log.info("Initilizing SubnodeETV Metainformation for entity " + sEntity);

			final MasterDataFacadeLocal mdLocal = ServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
			final Collection<EntityTreeViewVO> colSubNodes = mdLocal.getDependantSubnodes(NuclosEntity.ENTITYSUBNODES.getEntityName(), EntityTreeViewVO.ENTITY_FIELD, oId);
			subNodes.put(sEntity, colSubNodes);
		}
		return subNodes.get(sEntity);
	}

	@Override
	public int getEntityCount() {
		return mp != null ? mp.size() : 0;
	}

	@Override
	public int getSubEntityCount() {
		return mpSubNodes != null ? mpSubNodes.size() : 0;
	}

	@Override
	public Collection<String> showEntities() {
		return mp != null ? new ArrayList<String>(mp.keySet()) : null;
	}

	@Override
	public Collection<String> showSubEntities() {
		return mpSubNodes != null ? new ArrayList<String>(mpSubNodes.keySet()) : null;
	}

}	// class MasterDataMetaCache
