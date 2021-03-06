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
package org.nuclos.client.resource;

import java.util.Map;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.nuclos.client.LocalUserCaches.AbstractLocalUserCache;
import org.nuclos.client.jms.TopicNotificationReceiver;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.server.resource.valueobject.ResourceVO;

/**
 * A (client) cache for resources.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version 00.01.000
 */
public class ResourceCache extends AbstractLocalUserCache {
	
	private static final Logger LOG = Logger.getLogger(ResourceCache.class);
	
	
	// Implementation details...:
	
	private static ResourceCache INSTANCE;
	
	private transient ResourceDelegate delegate;
	private Map<String, Object>      resources;
	private Map<Integer, Object>     resourcesById;
	private Map<String, ResourceVO>  voByName;
	private Map<Integer, ResourceVO> voById;
	
	private transient TopicNotificationReceiver tnr;
	private transient MessageListener messageListener;
	
	private ResourceCache() {
		resources = CollectionUtils.newHashMap();
		resourcesById = CollectionUtils.newHashMap();
		voByName = CollectionUtils.newHashMap();
		voById = CollectionUtils.newHashMap();
		
		INSTANCE = this;
	}
	
	public final void afterPropertiesSet() {
		// Constructor might not be called - as this instance might be deserialized (tp)
		if (INSTANCE == null) {
			INSTANCE = this;
		}
		if (!wasDeserialized() || !isValid())
			invalidate();
		messageListener = new MessageListener() {
			@Override
			public void onMessage(Message msg) {
				LOG.info("onMessage " + this + " invalidate...");
				invalidate();			
			}
		};
		tnr.subscribe(getCachingTopic(), messageListener);
	}
	
	@Override
	public String getCachingTopic() {
		return JMSConstants.TOPICNAME_RESOURCECACHE;
	}
	
	public final void setResourceDelegate(ResourceDelegate delegate) {
		this.delegate = delegate;
	}
	
	public final void setTopicNotificationReceiver(TopicNotificationReceiver tnr) {
		this.tnr = tnr;
	}
	
	public static ResourceCache getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}
	
	private static enum ResourcePrep {
		ICON     { @Override public Object prepare(byte[] in) { return in == null ? null : new ImageIcon(in); }},
		BYTE_ARR { @Override public Object prepare(byte[] in) { return in; }};
		public abstract Object prepare(byte[] in);
	}

	private void encache(Pair<ResourceVO, byte[]> v, ResourcePrep prep, String nullString, Integer nullId) {
		if(v.x == null) {
			if(nullString != null)
				resources.put(nullString, null);
			if(nullId != null)
				resourcesById.put(nullId, null);
			return;
		}
		Object cacheObject = prep.prepare(v.y);
		resources.put(v.x.getName(), cacheObject);
		resourcesById.put(v.x.getId(), cacheObject);
		voByName.put(v.x.getName(), v.x);
		voById.put(v.x.getId(), v.x);
	}
	
	
	public ImageIcon getIconResource(String resourceName) {
		if(!resources.containsKey(resourceName))
			encache(delegate.getResource(resourceName), ResourcePrep.ICON, resourceName, null);
		return (ImageIcon) resources.get(resourceName);
	}

	public ImageIcon getIconResource(Integer resourceId) {
		if(!resourcesById.containsKey(resourceId))
			encache(delegate.getResource(resourceId), ResourcePrep.ICON, null, resourceId);
		return (ImageIcon) resourcesById.get(resourceId);
	}

	
	/**
	 * @param resourceName
	 * @return the <code>byte[]</code>
	 */
	public byte[] getResource(String resourceName) {
		if(!resources.containsKey(resourceName))
			encache(delegate.getResource(resourceName), ResourcePrep.BYTE_ARR, resourceName, null);
		return (byte[]) resources.get(resourceName);
	}

	public ResourceVO getResourceByName(String resourceName) {
		if(!voByName.containsKey(resourceName)) {
			ResourceVO vo = delegate.getResourceByName(resourceName);
			voByName.put(resourceName, vo);
			if(vo != null)
				voById.put(vo.getId(), vo);
		}
		return voByName.get(resourceName);
	}

	public ResourceVO getResourceById(Integer resourceId) {
		if(!voById.containsKey(resourceId)) {
			ResourceVO vo = delegate.getResourceById(resourceId);
			voById.put(resourceId, vo);
			if(vo != null)
				voByName.put(vo.getName(), vo);
		}
		return voById.get(resourceId);
	}

	public void invalidate() {
		resources.clear();
		resourcesById.clear();
		voById.clear();
		voByName.clear();
		LOG.info("Invalidated (cleared) cache " + this);
	}
}
