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
package org.nuclos.client.common;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.log4j.Logger;
import org.nuclos.client.masterdata.MetaDataDelegate;
import org.nuclos.common.AbstractProvider;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.util.DalTransformations;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.PivotInfo;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonFatalException;
import org.springframework.beans.factory.InitializingBean;

/**
 * An caching singleton for remotely accessing the meta data information 
 * from the client side.
 * <p>
 * For accessing the remote (server) side, this implementation uses 
 * {@link org.nuclos.client.masterdata.MetaDataDelegate}.
 * </p>
 */
public class MetaDataClientProvider extends AbstractProvider implements MetaDataProvider, InitializingBean {
	
	private static final Logger LOG = Logger.getLogger(MetaDataClientProvider.class);

	private final DataCache dataCache = new DataCache();

	private MetaDataClientProvider(){
	}

	public static void initialize() {
		getInstance().dataCache.buildMaps();
	}

	public static MetaDataClientProvider getInstance() {
		return (MetaDataClientProvider) SpringApplicationContextHolder.getBean("metaDataProvider");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		TopicNotificationReceiver.subscribe(JMSConstants.TOPICNAME_METADATACACHE, messagelistener);
	}

	/**
	 *
	 * @return
	 */
	@Override
    public Collection<EntityMetaDataVO> getAllEntities() {
		return dataCache.getMapMetaDataByEntity().values();
	}

	/**
	 *
	 * @param id
	 * @return
	 */
	@Override
    public EntityMetaDataVO getEntity(Long id) {
		final EntityMetaDataVO result = dataCache.getMapMetaDataById().get(id);
		if (result == null) {
			throw new CommonFatalException("entity with id " + id + " does not exists.");
		}
		return result;
	}

	/**
	 *
	 * @param entity
	 * @return
	 */
	@Override
    public EntityMetaDataVO getEntity(String entity) {
		final EntityMetaDataVO result = dataCache.getMapMetaDataByEntity().get(entity);
		if (result == null) {
			throw new CommonFatalException("entity " + entity + " does not exists.");
		}
		return result;
	}

	/**
	 *
	 * @param entity
	 * @return
	 */
	@Override
    public EntityMetaDataVO getEntity(NuclosEntity entity) {
		final EntityMetaDataVO result = dataCache.getMapMetaDataByEntity().get(entity.getEntityName());
		if (result == null) {
			throw new CommonFatalException("entity " + entity + " does not exists.");
		}
		return result;
	}

	/**
	 *
	 * @param entity
	 * @return
	 */
	@Override
    public Map<String, EntityFieldMetaDataVO> getAllEntityFieldsByEntity(String entity) {
		final Map<String, EntityFieldMetaDataVO> result = dataCache.getMapFieldMetaData().get(entity);
		if (result == null) {
			return Collections.emptyMap();
		}
		return result;
	}
	
	@Override
    public Map<String, EntityFieldMetaDataVO> getAllPivotEntityFields(PivotInfo info) {
    	Map<String, EntityFieldMetaDataVO> result = dataCache.getMapPivotMetaData().get(info);
    	if (result == null) {
    		// load data lazy
    		result = MetaDataDelegate.getInstance().getAllPivotEntityFields(info);
    		// localize name for client
			final EntityFieldMetaDataVO keyField = getEntityField(info.getSubform(), info.getKeyField());
    		for (EntityFieldMetaDataVO ef: result.values()) {
    			ef.setFallbacklabel("<html>" +
						"<font color=\"red\">" + CommonLocaleDelegate.getLabelFromMetaFieldDataVO(keyField) + ":" + "</font>" +
						"<font color=\"black\">" + ef.getField() + "</font>" +
						"</html>");
    		}
    		dataCache.getMapPivotMetaData().put(info, result);
    	}
    	return result;
	}
	
	public EntityFieldMetaDataVO getPivotKeyField(String baseEntity, String subform) {
		final Map<String, EntityFieldMetaDataVO> fields = getAllEntityFieldsByEntity(subform);
		final Set<EntityFieldMetaDataVO> uniqueFields = new HashSet<EntityFieldMetaDataVO>();
		boolean refToBase = false;
		for (String f : fields.keySet()) {
			final EntityFieldMetaDataVO mdF = fields.get(f);
			if (mdF.isUnique()) {
				if (baseEntity.equals(mdF.getForeignEntity())) {
					refToBase = true;
				} else if (mdF.getForeignEntity() != null) {
					uniqueFields.add(mdF);
				}
			}
		}
		if (refToBase && uniqueFields.size() == 1) {
			return uniqueFields.iterator().next();
		}
		else { 
			return null;
		}
	}

	/**
	 * @deprecated In the general case there could be more than one ref between entities, hence
	 * 		we must replace this...
	 */
	public EntityFieldMetaDataVO getRefField(String baseEntity, String subform) {
		// TODO: caching
		final Map<String, EntityFieldMetaDataVO>  fields = getAllEntityFieldsByEntity(baseEntity);
		EntityFieldMetaDataVO result = null;
		for (EntityFieldMetaDataVO f: fields.values()) {
			if (baseEntity.equals(f.getForeignEntity())) {
				result = f;
				break;
			}
		}
		return result;
	}
	
	/**
	 *
	 * @param entity
	 * @param field
	 * @return
	 */
	@Override
    public EntityFieldMetaDataVO getEntityField(String entity, String field) {
		final EntityFieldMetaDataVO result = getAllEntityFieldsByEntity(entity).get(field);
		if (result == null) {
			throw new CommonFatalException("entity field " + field + " in " + entity+ " does not exists.");
		}
		return result;
	}
	
	/**
	 * 
	 * @param entity
	 * @param field
	 * @return
	 */
	public EntityFieldMetaDataVO getEntityField(NuclosEntity entity, String field) {
		return getEntityField(entity.getEntityName(), field);
	}

	@Override
	public EntityFieldMetaDataVO getEntityField(String entity, Long fieldId) {
		for(EntityFieldMetaDataVO fieldMeta : getAllEntityFieldsByEntity(entity).values())
			if(fieldMeta.getId().equals(fieldId))
				return fieldMeta;
		throw new CommonFatalException("entity field with id=" + fieldId + " in " + entity + " does not exists.");
	}


	public synchronized void revalidate(){
		dataCache.buildMaps();
	}

	private final MessageListener messagelistener = new MessageListener() {
		@Override
        public void onMessage(Message msg) {
			debug("Received notification from server: meta data changed.");
			MetaDataClientProvider.this.revalidate();
		}
	};

	/**
	 *
	 *
	 */
	class DataCache {
		private boolean revalidating = false;

		private long startRevalidating;

		private Map<String, EntityMetaDataVO> mapMetaDataByEntity = null;
		private Map<Long, EntityMetaDataVO> mapMetaDataById = null;
		private Map<String, Map<String, EntityFieldMetaDataVO>> mapFieldMetaData = null;
		private Map<PivotInfo, Map<String, EntityFieldMetaDataVO>> mapPivotMetaData = new ConcurrentHashMap<PivotInfo, Map<String,EntityFieldMetaDataVO>>();

		public Map<String, EntityMetaDataVO> getMapMetaDataByEntity() {
			if (isRevalidating()) {
				return getMapMetaDataByEntity();
			} else {
				return mapMetaDataByEntity;
			}
		}

		public Map<Long, EntityMetaDataVO> getMapMetaDataById() {
			if (isRevalidating()) {
				return getMapMetaDataById();
			} else {
				return mapMetaDataById;
			}
		}

		public Map<String, Map<String, EntityFieldMetaDataVO>> getMapFieldMetaData() {
			if (isRevalidating()) {
				return getMapFieldMetaData();
			} else {
				return mapFieldMetaData;
			}
		}

		public Map<PivotInfo, Map<String, EntityFieldMetaDataVO>> getMapPivotMetaData() {
			if (isRevalidating()) {
				return getMapPivotMetaData();
			} else {
				return mapPivotMetaData;
			}
		}

		private Map<String, Map<String, EntityFieldMetaDataVO>> buildMapFieldMetaData(Collection<EntityMetaDataVO> allEntities) {
			return MetaDataDelegate.getInstance().getAllEntityFieldsByEntitiesGz(
				CollectionUtils.transform(allEntities, DalTransformations.getEntity()));
		}

		public synchronized void buildMaps() {
			startRevalidating = System.currentTimeMillis();
			revalidating = true;
			Collection<EntityMetaDataVO> allEntities = MetaDataDelegate.getInstance().getAllEntities();
			mapMetaDataByEntity = Collections.unmodifiableMap(CollectionUtils.generateLookupMap(allEntities, DalTransformations.getEntity()));
			mapMetaDataById = Collections.unmodifiableMap(CollectionUtils.generateLookupMap(allEntities, DalTransformations.<EntityMetaDataVO>getId()));
			mapFieldMetaData = Collections.unmodifiableMap(buildMapFieldMetaData(allEntities));
			mapPivotMetaData.clear();
			revalidating = false;
		}

		private boolean isRevalidating() {
			if (revalidating) {
				try {
					if (startRevalidating + 1000l*60 < System.currentTimeMillis())
						throw new NuclosFatalException("nuclos.metadata.revalidation.error.1");
					Thread.sleep(1000);
					return true;
				}
				catch(InterruptedException e) {
					throw new NuclosFatalException(e);
				}
			} else {
				return false;
			}
		}
	} //class DataCache

	public boolean isEntity(String entity) {
		try {
			getEntity(entity);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}
}
