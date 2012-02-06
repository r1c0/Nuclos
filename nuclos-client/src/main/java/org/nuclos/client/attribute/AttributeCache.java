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
package org.nuclos.client.attribute;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.common.AttributeProvider;
import org.nuclos.common.CacheableListener;
import org.nuclos.common.NuclosAttributeNotFoundException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Client cache for all attributes (Singleton pattern).
 * The cache must be initialized before its first use.
 * @todo It is possible to change the name of an AttributeCVO outside the cache and thus to make the
 * cache inconsistent. Fix is deferred until the attribute administration is implemented by using
 * the masterdata mechanism.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
@Component
public class AttributeCache implements AttributeProvider {
	
	private static final Logger LOG = Logger.getLogger(AttributeCache.class);
	
	/**
	 * the one (and only) instance of AttributeCache
	 */
	private static AttributeCache INSTANCE;
	
	private AttributeDelegate attributeDelegate;
	
	//

	/** @todo change this to an unmodifiable collection */
    private final Map<Integer, AttributeCVO> mpAttributesByIds = new ConcurrentHashMap<Integer, AttributeCVO>();

	private final LinkedList<CacheableListener> lstCacheableListeners = new LinkedList<CacheableListener>();
	/**
	 * @return the one (and only) instance of AttributeCache
	 */
	public static AttributeCache getInstance() {
		// return (AttributeCache) SpringApplicationContextHolder.getBean("attributeProvider");
		return INSTANCE;
	}

	/**
	 * creates the cache. Fills in all the attributes from the server.
	 * @throws NuclosFatalException
	 */
	AttributeCache() {
		INSTANCE = this;
	}
	
	@Autowired
	void setAttributeDelegate(AttributeDelegate attributeDelegate) {
		this.attributeDelegate = attributeDelegate;
	}

	/**
	 * revalidates this cache: clears it, then fills in all the attributes 
	 * from the server again.
	 * <p>
	 * Must be synchronized to avoid attributecache.uniquekey.id.error in 
	 * {@link #addImpl(AttributeCVO)}.
	 * </p>
	 */
	public synchronized void revalidate() {
		mpAttributesByIds.clear();
		LOG.info("Cleared cache " + this);
		fill();
	}

	/**
	 * @param iAttributeId
	 * @postcondition result != null
	 */
	@Override
    public AttributeCVO getAttribute(int iAttributeId) {
		if (mpAttributesByIds.isEmpty()) {
			fill();
		}
		final AttributeCVO result = mpAttributesByIds.get(iAttributeId);
		if (result == null) {
			throw new NuclosAttributeNotFoundException(iAttributeId);
		}

		assert result != null;
		return result;
	}

	/**
	 * @param iAttributeId
	 * @return Does this cache contain an attribute with the given id?
	 */
	public boolean contains(int iAttributeId) {
		return mpAttributesByIds.containsKey(iAttributeId);
	}

	@Override
    public AttributeCVO getAttribute(Integer iEntityId, String sAttributeName) throws NuclosAttributeNotFoundException {
		return getAttribute(Modules.getInstance().getEntityNameByModuleId(iEntityId), sAttributeName);
	}

	/**
	 * @param sAttributeName
	 * @return the attribute with the given name.
	 * @throws NuclosAttributeNotFoundException if there is no attribute with the given name
	 * @precondition sAttributeName != null
	 * @postcondition result != null
	 */
	@Override
    public AttributeCVO getAttribute(String sEntity, String sAttributeName) throws NuclosAttributeNotFoundException {
		if (sEntity == null) {
			throw new NullArgumentException("sEntity");
		}
		if (sAttributeName == null) {
			throw new NullArgumentException("sAttributeName");
		}
		final AttributeCVO result = getAttribute(MetaDataClientProvider.getInstance().getEntityField(sEntity, sAttributeName).getId().intValue());
		if (result == null) {
			throw new NuclosAttributeNotFoundException(sAttributeName);
		}
		assert result != null;
		return result;
	}

	/**
	 * @deprecated Use MetaDataClientProvider.getInstance().getEntityField(entity, field)
	 */
	@Override
	public EntityFieldMetaDataVO getEntityField(String entity, String field) throws NuclosAttributeNotFoundException {
		return MetaDataClientProvider.getInstance().getEntityField(entity, field);
	}

	/**
	 * adds a single attribute to this cache and notifies CacheableListeners.
	 * @param attrcvo
	 * @precondition attrcvo != null
	 */
	public void add(AttributeCVO attrcvo) {
		addImpl(attrcvo);
		fireCacheableChanged();
	}

	/**
	 * adds a single attribute to this cache.
	 * <p>
	 * Must be synchronized to avoid attributecache.uniquekey.id.error in 
	 * {@link #fill()}.
	 * </p>
	 * @param attrcvo
	 * @precondition attrcvo != null
	 */
	private synchronized void addImpl(AttributeCVO attrcvo) {
		if (attrcvo == null) {
			throw new NullArgumentException("attrcvo");
		}
		if (mpAttributesByIds.containsKey(attrcvo.getId())) {
			throw new NuclosFatalException("attributecache.uniquekey.id.error");
				//"Ein Attribut mit dieser Id ist schon im Cache vorhanden.");
		}
		mpAttributesByIds.put(attrcvo.getId(), attrcvo);
		// old entry (attribute which has the same id) is overwritten
	}

	/**
	 * removes the attribute with the given id from the cache and notifies CacheableListeners.
	 * Does nothing if the cache doesn't contain an attribute with the given id.
	 * Note that the name of the given attribute is ignored. Only the id is relevant.
	 * @param iAttributeId
	 * @postcondition !this.contains(iAttributeId)
	 */
	public void remove(Integer iAttributeId) {
		removeImpl(iAttributeId);
		fireCacheableChanged();
		assert !this.contains(iAttributeId);
	}

	/**
	 * removes the attribute with the given id from the cache.
	 * Does nothing if the cache doesn't contain an attribute with the given id.
	 * Note that the name of the given attribute is ignored. Only the id is relevant.
	 * @param iAttributeId
	 * @postcondition !this.contains(iAttributeId)
	 */
	private void removeImpl(Integer iAttributeId) {
		if (iAttributeId == null) {
			throw new NullArgumentException("iAttributeId");
		}
		mpAttributesByIds.remove(iAttributeId);
		// postcondition:
		assert !this.contains(iAttributeId);
	}

	/**
	 * @return Collection<AttributeCVO> a collection containing all attributes in the cache.
	 */
	@Override
    public Collection<AttributeCVO> getAttributes() {
		return mpAttributesByIds.values();
	}

	/**
	 * fills this cache.
	 * <p>
	 * Must be synchronized to avoid attributecache.uniquekey.id.error in 
	 * {@link #addImpl(AttributeCVO)}.
	 * </p>
	 * @throws NuclosFatalException
	 */
	public synchronized void fill() throws NuclosFatalException {
		for (AttributeCVO attrcvo : attributeDelegate.getAllAttributeCVOs(null)) {
			addImpl(attrcvo);
		}
		LOG.info("Validated (filled) cache " + this);
	}

	public void addCacheableListener(CacheableListener cacheablelistener) {
		synchronized(lstCacheableListeners) {
			lstCacheableListeners.add(cacheablelistener);
		}
	}

	public void removeCacheableListener(CacheableListener cacheablelistener) {
		synchronized (lstCacheableListeners) {
			lstCacheableListeners.remove(cacheablelistener);
		}
	}

	private void fireCacheableChanged() {
		// defensive copy
		final LinkedList<CacheableListener> clone;
		synchronized (lstCacheableListeners) {
			clone = (LinkedList<CacheableListener>) lstCacheableListeners.clone();
		}
		/** @todo Is this still necessary? Won't the GenericObjectMetaDataCache always fireCachableChanged() anyway? */
		for (CacheableListener listener : clone) {
			listener.cacheableChanged();
		}
	}

}	// class AttributeCache
