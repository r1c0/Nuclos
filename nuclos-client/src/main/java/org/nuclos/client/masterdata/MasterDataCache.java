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
package org.nuclos.client.masterdata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.nuclos.client.entityobject.EntityFacadeDelegate;
import org.nuclos.client.jms.TopicNotificationReceiver;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Caches whole contents from master data entities. It is not used for data dependant on a foreign key.
 * Retrieves notifications about changes from the server (singleton).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 * @todo the caller has to decide whether an entity is cacheable or not. This is bad.
 */
@Component
public class MasterDataCache {
	
	private static final Logger LOG = Logger.getLogger(MasterDataCache.class);

	private static MasterDataCache INSTANCE;

	/**
	 * maps an entity name to the contents of the entity.
	 */
	private final Map<String, List<MasterDataVO>> mp 
		= new ConcurrentHashMap<String, List<MasterDataVO>>();

	private final Map<CollectableFieldsByNameKey, List<CollectableField>> mpCollectableFieldsByName 
		= new ConcurrentHashMap<MasterDataCache.CollectableFieldsByNameKey, List<CollectableField>>();
	
	private TopicNotificationReceiver tnr;
	
	private MasterDataDelegate masterDataDelegate;
	
	private EntityFacadeDelegate entityFacadeDelegate;

	private final MessageListener messagelistener = new MessageListener() {
		@Override
		public void onMessage(Message msg) {
			String sEntity;
			if (msg instanceof TextMessage) {
				try {
					sEntity = ((TextMessage) msg).getText();
					LOG.info("onMessage: JMS message is of type TextMessage, text is: " + sEntity);
				}
				catch (JMSException ex) {
					LOG.warn("onMessage: Exception thrown in JMS message listener.", ex);
					sEntity = null;
				}
			}
			else {
				LOG.warn("onMessage: Message of type " + msg.getClass().getName() + " received, while a TextMessage was expected.");
				sEntity = null;
			}

			MasterDataCache.this.invalidate(sEntity);
		}
	};

	public static MasterDataCache getInstance() {
		return INSTANCE;
	}

	MasterDataCache() {
		INSTANCE = this;
	}
	
	@PostConstruct
	void init() {
		tnr.subscribe(JMSConstants.TOPICNAME_MASTERDATACACHE, messagelistener);
	}
	
	@Autowired
	void setTopicNotificationReceiver(TopicNotificationReceiver tnr) {
		this.tnr = tnr;
	}
	
	@Autowired
	void setMasterDataDelegate(MasterDataDelegate masterDataDelegate) {
		this.masterDataDelegate = masterDataDelegate;
	}
	
	@Autowired
	void setEntityFacadeDelegate(EntityFacadeDelegate entityFacadeDelegate) {
		this.entityFacadeDelegate = entityFacadeDelegate;
	}

	/**
	 * fetches all data from the entity with the given name from the cache.
	 * If the entity is known not to be cacheable, ignores the cache.
	 * @param sEntityName
	 * @return the current contents (data) of the entity with the given name.
	 * @throws CommonFinderException
	 */
	public List<MasterDataVO> get(String sEntityName) throws CommonFinderException {
		List<MasterDataVO> result = mp.get(sEntityName);
		if (result == null) {
			result = new ArrayList<MasterDataVO>(masterDataDelegate.getMasterData(sEntityName));
			if (!Boolean.FALSE.equals(isCacheable(sEntityName))) {
				mp.put(sEntityName, result);
			}
		}
		return result;
	}

	/**
	 * fetch data with the given id from the entity with the given name from the cache.
	 * If the entity is known not to be cacheable, ignores the cache.
	 * @param sEntityName
	 * @param id
	 * @return the data (if found)
	 * @throws CommonFinderException
	 */
	public MasterDataVO get(String sEntityName, Integer id) throws CommonFinderException {
		List<MasterDataVO> result = get(sEntityName);
		for (MasterDataVO md : result) {
			if (LangUtils.equals(id,  md.getIntId())) {
				return md;
			}
		}
		throw new CommonFinderException();
	}

	/**
	 * @param sEntityName
	 * @return Ist the entity with the given name cacheable? <code>null</code> means unknown.
	 * @todo Whether an entity is cacheable or not should be known for each entity, not only for master data entities.
	 */
	public static Boolean isCacheable(String sEntityName) {
		final CollectableEntity clcte = DefaultCollectableEntityProvider.getInstance().getCollectableEntity(sEntityName);
		if (clcte instanceof CollectableMasterDataEntity) {
			final CollectableMasterDataEntity clctmde = (CollectableMasterDataEntity) clcte;
			return clctmde.getMasterDataMetaCVO().isCacheable();
		}
		return null;
	}

	/**
	 * invalidates the cache for the given entity or the whole cache, if <code>sEntityName == null</code>.
	 * @param sEntityName
	 */
	public void invalidate(String sEntityName) {
		if (sEntityName == null) {
			LOG.info("Invalidating the whole masterdata cache.");
			this.mp.clear();
			this.mpCollectableFieldsByName.clear();
		}
		else {
			LOG.info("Removing entity " + sEntityName + " from masterdata cache.");
			this.mp.remove(sEntityName);

			for (CollectableFieldsByNameKey key : mpCollectableFieldsByName.keySet()) {
				if (sEntityName.equals(key.sEntityName)) {
					mpCollectableFieldsByName.remove(key);
				}
			}

			NuclosEntity entity = NuclosEntity.getByName(sEntityName);
			if (entity != null) {
				switch (entity) {
				case LAYOUT:
				case LAYOUTUSAGE:
					// invalidate the master data layout cache if a md layout (usage) has been changed:
					masterDataDelegate.invalidateLayoutCache();
					break;
				}
			}
		}
	}

	/**
	 * get masterdata for entity as collectable fields.
	 * @param sEntityName masterdata entity name.
	 * @param bCheckValidity Test for active sign and validFrom/validUntil
	 * @return list of collectable fields
	 */
	public List<CollectableField> getCollectableFields(String sEntityName, boolean bCheckValidity) throws CommonBusinessException {
		/** @todo use CollectableEntity.getIdentifierFieldName rather than CollectableMasterData.FIELDNAME_NAME */
		return getCollectableFieldsByName(sEntityName, CollectableMasterData.FIELDNAME_NAME, bCheckValidity);
	}

	/**
	 * get masterdata for entity as collectable fields.
	 * @param sEntityName masterdata entity name
	 * @param sFieldName
	 * @param bCheckValidity Test for active sign and validFrom/validUntil
	 * @return list of collectable fields
	 */
	public List<CollectableField> getCollectableFieldsByName(String sEntityName, String sFieldName, boolean bCheckValidity) 
			throws CommonBusinessException {
		CollectableFieldsByNameKey cacheKey = new CollectableFieldsByNameKey();
		cacheKey.sEntityName = sEntityName;
		cacheKey.sFieldName = sFieldName;
		cacheKey.bCheckValidity = bCheckValidity;

		List<CollectableField> result = mpCollectableFieldsByName.get(cacheKey);
		if (result == null) {
			result = entityFacadeDelegate.getCollectableFieldsByName(sEntityName, sFieldName, bCheckValidity);
			if (Boolean.TRUE.equals(isCacheable(sEntityName))) {
				mpCollectableFieldsByName.put(cacheKey, result);
			}
		}
		return result;
	}

	private class CollectableFieldsByNameKey {
		String sEntityName;
		String sFieldName;
		boolean bCheckValidity;

		@Override
		public boolean equals(Object obj) {

			if (obj instanceof CollectableFieldsByNameKey) {
				CollectableFieldsByNameKey other = (CollectableFieldsByNameKey) obj;
				return LangUtils.equals(this.sEntityName, other.sEntityName)
				    && LangUtils.equals(this.sFieldName, other.sFieldName)
				    && LangUtils.equals(this.bCheckValidity, other.bCheckValidity);
			}

			return super.equals(obj);
		}

	}

}	// class MasterDataCache
