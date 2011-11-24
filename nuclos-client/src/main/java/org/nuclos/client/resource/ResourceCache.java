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

import org.nuclos.client.common.TopicNotificationReceiver;
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
public class ResourceCache {
	/**
	 * @param resourceName
	 * @return the <code>ImageIcon</code>
	 */
	public static ImageIcon getIconResource(String resourceName) {
		return _getInstance()._getIconResource(resourceName);
	}

	public static ImageIcon getIconResource(Integer resourceId) {
		return _getInstance()._getIconResource(resourceId);
	}

	/**
	 * @param resourceName
	 * @return the <code>byte[]</code>
	 */
	public static byte[] getResource(String resourceName) {
		return _getInstance()._getResource(resourceName);
	}

	public static ResourceVO getResourceByName(String resourceName) {
		return _getInstance()._getResourceByName(resourceName);
	}

	public static ResourceVO getResourceById(Integer resourceId) {
		return _getInstance()._getResourceById(resourceId);
	}

	public static void invalidate() {
		_getInstance()._invalidate();
	}

	
	// Implementation details...:
	
	private MessageListener messagelistener = new MessageListener() {
		@Override
		public void onMessage(Message msg) {
			invalidate();			
		}
	};

	private static ResourceCache     instance;
	
	private ResourceDelegate         delegate;
	private Map<String, Object>      resources;
	private Map<Integer, Object>     resourcesById;
	private Map<String, ResourceVO>  voByName;
	private Map<Integer, ResourceVO> voById;
	
	private ResourceCache() {
		resources = CollectionUtils.newHashMap();
		resourcesById = CollectionUtils.newHashMap();
		voByName = CollectionUtils.newHashMap();
		voById = CollectionUtils.newHashMap();
		delegate = ResourceDelegate.getInstance();
		TopicNotificationReceiver.subscribe(JMSConstants.TOPICNAME_RESOURCECACHE, messagelistener);
	}
	
	private static ResourceCache _getInstance() {
		if(instance == null)
			instance = new ResourceCache();
		return instance;
	}
	
	private static enum ResourcePrep {
		ICON     { @Override public Object prepare(byte[] in) { return in == null ? null : new ImageIcon(in); }},
		BYTE_ARR { @Override public Object prepare(byte[] in) { return in; }};
		public abstract Object prepare(byte[] in);
	};

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
	
	
	private ImageIcon _getIconResource(String resourceName) {
		if(!resources.containsKey(resourceName))
			encache(delegate.getResource(resourceName), ResourcePrep.ICON, resourceName, null);
		return (ImageIcon) resources.get(resourceName);
	}

	private ImageIcon _getIconResource(Integer resourceId) {
		if(!resourcesById.containsKey(resourceId))
			encache(delegate.getResource(resourceId), ResourcePrep.ICON, null, resourceId);
		return (ImageIcon) resourcesById.get(resourceId);
	}

	
	/**
	 * @param resourceName
	 * @return the <code>byte[]</code>
	 */
	private byte[] _getResource(String resourceName) {
		if(!resources.containsKey(resourceName))
			encache(delegate.getResource(resourceName), ResourcePrep.BYTE_ARR, resourceName, null);
		return (byte[]) resources.get(resourceName);
	}

	private ResourceVO _getResourceByName(String resourceName) {
		if(!voByName.containsKey(resourceName)) {
			ResourceVO vo = delegate.getResourceByName(resourceName);
			voByName.put(resourceName, vo);
			if(vo != null)
				voById.put(vo.getId(), vo);
		}
		return voByName.get(resourceName);
	}

	private ResourceVO _getResourceById(Integer resourceId) {
		if(!voById.containsKey(resourceId)) {
			ResourceVO vo = delegate.getResourceById(resourceId);
			voById.put(resourceId, vo);
			if(vo != null)
				voByName.put(vo.getName(), vo);
		}
		return voById.get(resourceId);
	}

	private void _invalidate() {
		resources.clear();
		resourcesById.clear();
		voById.clear();
		voByName.clear();
	}
}
