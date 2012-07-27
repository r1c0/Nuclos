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
package org.nuclos.client.genericobject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.log4j.Logger;
import org.nuclos.client.LocalUserCaches.AbstractLocalUserCache;
import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.jms.TopicNotificationReceiver;
import org.nuclos.common.AttributeProvider;
import org.nuclos.common.CacheableListener;
import org.nuclos.common.GenericObjectMetaDataProvider;
import org.nuclos.common.GenericObjectMetaDataVO;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosAttributeNotFoundException;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.attribute.valueobject.AttributeCVO;

/**
 * Client side leased object meta data cache (singleton).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
// @Component
public class GenericObjectMetaDataCache extends AbstractLocalUserCache implements GenericObjectMetaDataProvider {

	private static final Logger LOG = Logger.getLogger(GenericObjectMetaDataCache.class);
	
	private static GenericObjectMetaDataCache INSTANCE;

	//
	
	private transient GenericObjectDelegate genericObjectDelegate;

	private GenericObjectMetaDataVO lometacvo;

	private List<CacheableListener> lstCacheableListeners;
	
	private transient TopicNotificationReceiver tnr;
	private transient MessageListener messageListener; 

	private GenericObjectMetaDataCache() {
		INSTANCE = this;
	}
	
	@PostConstruct
	final void init() {
		final Runnable run = new Runnable() {
			@Override
			public void run() {
				if (!wasDeserialized() || !isValid())
					setup();
				messageListener = new MessageListener() {
					@Override
					public void onMessage(Message msg) {
						LOG.info("onMessage: Received notification from server: meta data changed, revalidate...");
						GenericObjectMetaDataCache.this.revalidate();
					}
				};
				tnr.subscribe(getCachingTopic(), messageListener);
			}
		};
		new Thread(run, "GenericObjectMetaDataCache.init").start();
	}

	// @Autowired
	public final void setTopicNotificationReceiver(TopicNotificationReceiver tnr) {
		this.tnr = tnr;
	}
	
	// @Autowired
	public final void setGenericObjectDelegate(GenericObjectDelegate genericObjectDelegate) {
		this.genericObjectDelegate = genericObjectDelegate;
	}

	public static GenericObjectMetaDataCache getInstance() {
		if (INSTANCE == null) {
			// throw new IllegalStateException("too early");
			// lazy support
			INSTANCE = SpringApplicationContextHolder.getBean(GenericObjectMetaDataCache.class);
		}
		return INSTANCE;
	}
	
	@Override
	public String getCachingTopic() {
		return JMSConstants.TOPICNAME_METADATACACHE;
	}

	private GenericObjectMetaDataVO getMetaDataCVO() {
		return lometacvo;
	}

	public AttributeProvider getAttributeProvider() {
		return AttributeCache.getInstance();
	}

	@Override
	public AttributeCVO getAttribute(int iAttributeId) {
		return this.getAttributeProvider().getAttribute(iAttributeId);
	}
	
	@Override
	public AttributeCVO getAttribute(Integer iEntityId, String sAttributeName)
		throws NuclosAttributeNotFoundException {
		return this.getAttribute(Modules.getInstance().getEntityNameByModuleId(iEntityId), sAttributeName);
	}

	@Override
	public AttributeCVO getAttribute(String sEntity, String sAttributeName) throws NuclosAttributeNotFoundException {
		return this.getAttributeProvider().getAttribute(sEntity, sAttributeName);
	}
	
	@Override
	public EntityFieldMetaDataVO getEntityField(String entity, String field) throws NuclosAttributeNotFoundException {
		return MetaDataClientProvider.getInstance().getEntityField(entity, field);
	}

	@Override
	public Collection<AttributeCVO> getAttributes() {
		return this.getAttributeProvider().getAttributes();
	}

	@Override
	public Collection<AttributeCVO> getAttributeCVOsByModuleId(Integer iModuleId, Boolean bSearchable) {
		return this.getMetaDataCVO().getAttributeCVOsByModuleId(this.getAttributeProvider(), iModuleId, bSearchable);
	}

	@Override
	public Set<String> getAttributeNamesByModuleId(Integer iModuleId, Boolean bSearchable) {
		return this.getMetaDataCVO().getAttributeNamesByModuleId(iModuleId, bSearchable);
	}

//	public Collection<AttributeCVO> getAttributeCVOsByLayoutId(int iLayoutId) {
//		return this.getMetaDataCVO().getAttributeCVOsByLayoutId(this.getAttributeProvider(), iLayoutId);
//	}

	@Override
	public Set<String> getSubFormEntityNamesByLayoutId(int iLayoutId) {
		return this.getMetaDataCVO().getSubFormEntityNamesByLayoutId(iLayoutId);
	}

	@Override
	public Collection<EntityAndFieldName> getSubFormEntityAndForeignKeyFieldNamesByLayoutId(int iLayoutId) {
		return this.getMetaDataCVO().getSubFormEntityAndForeignKeyFieldNamesByLayoutId(iLayoutId);
	}

	/**
	 * @param iModuleId may be <code>null</code>.
	 * @return the names of subform entities used in the module (details only) with the given id. If module id is <code>null</code>,
	 * the names of all subform entities used in any module (details only).
	 */
	public Set<String> getSubFormEntityNamesByModuleId(Integer iModuleId) {
		return this.getMetaDataCVO().getSubFormEntityNamesByModuleId(iModuleId);
	}

	@Override
	public Set<String> getBestMatchingLayoutAttributeNames(UsageCriteria usagecriteria) throws CommonFinderException {
		return this.getMetaDataCVO().getBestMatchingLayoutAttributeNames(usagecriteria);
	}

	@Override
	public int getBestMatchingLayoutId(UsageCriteria usagecriteria, boolean bSearchScreen) throws CommonFinderException {
		return this.getMetaDataCVO().getBestMatchingLayoutId(usagecriteria, bSearchScreen);
	}

	@Override
	public Set<Integer> getLayoutIdsByModuleId(int iModuleId, boolean bSearchScreen) {
		return this.getMetaDataCVO().getLyoutIdsByModuleId(iModuleId, bSearchScreen);
	}
	
	@Override
	public String getLayoutML(int iLayoutId) {
		return this.getMetaDataCVO().getLayoutML(iLayoutId);
	}

	public void revalidate() {
		AttributeCache.getInstance().revalidate();
		Modules.getInstance().invalidate();
		this.setup();
	}

	private void setup() {
		this.lometacvo = genericObjectDelegate.getMetaDataCVO();
		this.fireCacheableChanged();
	}

	private synchronized List<CacheableListener> getCacheableListeners() {
		if (this.lstCacheableListeners == null) {
			this.lstCacheableListeners = new LinkedList<CacheableListener>();
		}
		return this.lstCacheableListeners;
	}

	public synchronized void addCacheableListener(CacheableListener cacheablelistener) {
		this.getCacheableListeners().add(cacheablelistener);
	}

	public synchronized void removeCacheableListener(CacheableListener cacheablelistener) {
		this.getCacheableListeners().remove(cacheablelistener);
	}

	private synchronized void fireCacheableChanged() {
		for (CacheableListener listener : this.getCacheableListeners()) {
			listener.cacheableChanged();
		}
	}

}	// class GenericObjectMetaDataCache
