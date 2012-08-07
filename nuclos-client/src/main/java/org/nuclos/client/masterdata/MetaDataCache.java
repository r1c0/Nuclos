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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.nuclos.client.LocalUserCaches.AbstractLocalUserCache;
import org.nuclos.client.jms.TopicNotificationReceiver;
import org.nuclos.client.main.Main;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.springframework.beans.factory.InitializingBean;


/**
 * Caches the meta data from all master data entities.
 * Retrieves notifications about changes from the server (singleton).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Corina.Mandoki@novabit.de">Corina.Mandoki</a>
 * @version 01.00.00
 *
 */
// @Component
public class MetaDataCache extends AbstractLocalUserCache implements InitializingBean {

	private static final Logger LOG = Logger.getLogger(MetaDataCache.class);

	private static MetaDataCache INSTANCE;
	
	//
	
	private Map<String, MasterDataMetaVO> mp;
	
	private transient TopicNotificationReceiver tnr;
	private transient MessageListener messageListener;
	
	private transient MasterDataDelegate masterDataDelegate;
	

	public MetaDataCache() {
		INSTANCE = this;
	}
	
	public static MetaDataCache getInstance() {
		if (INSTANCE == null) {
			// throw new IllegalStateException("too early");
			// lazy support
			INSTANCE = (MetaDataCache)SpringApplicationContextHolder.getBean("metaDataCache");
		}
		return INSTANCE;
	}
	
	// @PostConstruct
	public final void afterPropertiesSet() {
		final Runnable run = new Runnable() {
			@Override
			public void run() {
				if (!wasDeserialized() || !isValid()) {
					LOG.debug("Initializing metadata cache");
					final Collection<MasterDataMetaVO> coll = masterDataDelegate.getMetaData();
					mp = new ConcurrentHashMap<String, MasterDataMetaVO>(coll.size());
					for (MasterDataMetaVO mdmetavo : coll) {
						mp.put(mdmetavo.getEntityName(), mdmetavo);
					}
				}
				messageListener = new MessageListener() {
					@Override
					public void onMessage(Message msg) {
						LOG.info("onMessage: Received notification from server: meta data changed.");
						MetaDataCache.this.invalidate();
						if (msg instanceof TextMessage) {
							try {
								NuclosEntity entity = NuclosEntity.getByName(((TextMessage)msg).getText());
								if (!(entity == NuclosEntity.DYNAMICENTITY || entity == NuclosEntity.LAYOUT)) {
									Main.getInstance().getMainController().refreshMenusLater();
								}
							}
							catch (JMSException ex) {
								LOG.warn("onMessage: Exception thrown in JMS message listener.", ex);
							}
						}
						else {
							LOG.warn("onMessage: Message of type " + msg.getClass().getName() + " received, while a TextMessage was expected.");
						}			
					}
				};
				tnr.subscribe(getCachingTopic(), messageListener);
			}
		};
		// new Thread(run, "MetaDataCache.init").start();
		run.run();
	}
	
	// @Autowired
	public final void setTopicNotificationReceiver(TopicNotificationReceiver tnr) {
		this.tnr = tnr;
	}
	
	// @Autowired
	public final void setMasterDataDelegate(MasterDataDelegate masterDataDelegate) {
		this.masterDataDelegate = masterDataDelegate;
	}
	
	@Override
	public String getCachingTopic() {
		return JMSConstants.TOPICNAME_METADATACACHE;
	}
	
	/**
	 * @return the meta data for all master data tables.
	 */
	public Collection<MasterDataMetaVO> getMetaData() {
		Collection<MasterDataMetaVO> coll = mp.values();
		if(coll == null || coll.isEmpty())
			return masterDataDelegate.getMetaData();
		
		return coll;
	}

	/**
	 * @param sEntity
	 * @return the meta data for the given entity, if any.
	 */
	public MasterDataMetaVO getMetaData(String sEntity) {
		LOG.debug("Metadata cache hit");		
		MasterDataMetaVO result = mp.get(sEntity);
		if(result == null) {
			result = masterDataDelegate.getMetaData(sEntity);
			mp.put(sEntity, result);
		}
		
		return result;
	}
	
	public MasterDataMetaVO getMetaData(NuclosEntity entity) {
		return getMetaData(entity.getEntityName());
	}
	
	public MasterDataMetaVO getMetaDataById(Integer iId) {
		LOG.debug("Metadata cache hit");
		for (MasterDataMetaVO result : mp.values()) {
			if (iId.equals(result.getId())) {
				return result;
			}
		}
		MasterDataMetaVO result = masterDataDelegate.getMetaData(iId);
		if (result != null)
			mp.put(result.getEntityName(), result);
		return result;
	}
	
	public synchronized void invalidate() {
		LOG.info("Invalidating meta data cache.");
		mp.clear();
		masterDataDelegate.invalidateCaches();
	}
		
} // class MetaDataCache
