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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.common.AttributeProvider;
import org.nuclos.common.CacheableListener;
import org.nuclos.common.NuclosAttributeNotFoundException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.server.attribute.valueobject.AttributeCVO;

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
public class AttributeCache implements AttributeProvider {
	/**
	 * the one (and only) instance of AttributeCache
	 */
	private static AttributeCache singleton;

	/** @todo change this to an unmodifiable collection */
    private final Map<Integer, AttributeCVO> mpAttributesByIds = CollectionUtils.newHashMap();

	private List<CacheableListener> lstCacheableListeners;

	/**
	 * Must be called before the first call to getInstance(). This way, <code>getInstance()</code>,
	 * is guaranteed not to throw any exceptions.
	 */
	public static synchronized void initialize() throws NuclosFatalException {
		getInstance().fill();
	}

	/**
	 * @return the one (and only) instance of AttributeCache
	 */
	public static synchronized AttributeCache getInstance() {
		return (AttributeCache) SpringApplicationContextHolder.getBean("attributeProvider");
	}

	/**
	 * creates the cache. Fills in all the attributes from the server.
	 * @throws NuclosFatalException
	 */
	protected AttributeCache() throws NuclosFatalException {
		//this.fill();
	}

	/**
	 * revalidates this cache: clears it, then fills in all the attributes from the server again.
	 */
	public synchronized void revalidate() {
		this.mpAttributesByIds.clear();
		this.fill();
	}

	/**
	 * @param iAttributeId
	 * @postcondition result != null
	 */
	@Override
    public synchronized AttributeCVO getAttribute(int iAttributeId) {
		final AttributeCVO result = this.mpAttributesByIds.get(iAttributeId);
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
	public synchronized boolean contains(int iAttributeId) {
		return this.mpAttributesByIds.containsKey(iAttributeId);
	}

	@Override
    public synchronized AttributeCVO getAttribute(Integer iEntityId, String sAttributeName) throws NuclosAttributeNotFoundException {
		return this.getAttribute(Modules.getInstance().getEntityNameByModuleId(iEntityId), sAttributeName);
	}

	/**
	 * @param sAttributeName
	 * @return the attribute with the given name.
	 * @throws NuclosAttributeNotFoundException if there is no attribute with the given name
	 * @precondition sAttributeName != null
	 * @postcondition result != null
	 */
	@Override
    public synchronized AttributeCVO getAttribute(String sEntity, String sAttributeName) throws NuclosAttributeNotFoundException {
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

	@Override
	public EntityFieldMetaDataVO getEntityField(String entity, String field) throws NuclosAttributeNotFoundException {
		return MetaDataClientProvider.getInstance().getEntityField(entity, field);
	}


	/**
	 * adds a single attribute to this cache and notifies CacheableListeners.
	 * @param attrcvo
	 * @precondition attrcvo != null
	 */
	public synchronized void add(AttributeCVO attrcvo) {
		this.addImpl(attrcvo);
		this.fireCacheableChanged();
	}

	/**
	 * adds a single attribute to this cache.
	 * @param attrcvo
	 * @precondition attrcvo != null
	 */
	private void addImpl(AttributeCVO attrcvo) {
		if (attrcvo == null) {
			throw new NullArgumentException("attrcvo");
		}
		if (this.mpAttributesByIds.containsKey(attrcvo.getId())) {
			throw new NuclosFatalException("attributecache.uniquekey.id.error");
				//"Ein Attribut mit dieser Id ist schon im Cache vorhanden.");
		}
		this.mpAttributesByIds.put(attrcvo.getId(), attrcvo);
		// old entry (attribute which has the same id) is overwritten
	}

	/**
	 * removes the attribute with the given id from the cache and notifies CacheableListeners.
	 * Does nothing if the cache doesn't contain an attribute with the given id.
	 * Note that the name of the given attribute is ignored. Only the id is relevant.
	 * @param iAttributeId
	 * @postcondition !this.contains(iAttributeId)
	 */
	public synchronized void remove(Integer iAttributeId) {
		this.removeImpl(iAttributeId);
		this.fireCacheableChanged();
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

		this.mpAttributesByIds.remove(iAttributeId);

		// postcondition:
		assert !this.contains(iAttributeId);
	}

	/**
	 * replaces the given attribute in the cache and notifies CacheableListeners.
	 * @param attrcvo
	 */
	public synchronized void replace(AttributeCVO attrcvo) {
		if (attrcvo == null) {
			throw new NullArgumentException("attrcvo");
		}
		this.removeImpl(attrcvo.getId());
		this.addImpl(attrcvo);
		this.fireCacheableChanged();
	}

	/**
	 * @return Collection<AttributeCVO> a collection containing all attributes in the cache.
	 */
	@Override
    public synchronized Collection<AttributeCVO> getAttributes() {
		return this.mpAttributesByIds.values();
	}

	/**
	 * fills this cache.
	 * @throws NuclosFatalException
	 */
	private void fill() throws NuclosFatalException {
		for (AttributeCVO attrcvo : AttributeDelegate.getInstance().getAllAttributeCVOs(null)) {
			this.addImpl(attrcvo);
		}
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
		/** @todo Is this still necessary? Won't the GenericObjectMetaDataCache always fireCachableChanged() anyway? */
		for (CacheableListener listener : this.getCacheableListeners()) {
			listener.cacheableChanged();
		}
	}

}	// class AttributeCache
