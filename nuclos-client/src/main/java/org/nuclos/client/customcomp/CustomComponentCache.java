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

package org.nuclos.client.customcomp;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.log4j.Logger;
import org.nuclos.client.LocalUserCaches.AbstractLocalUserCache;
import org.nuclos.client.jms.TopicNotificationReceiver;
import org.nuclos.client.main.Main;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.server.customcomp.valueobject.CustomComponentVO;

public class CustomComponentCache extends AbstractLocalUserCache {
	
	private static final Logger LOG = Logger.getLogger(CustomComponentCache.class);

	private static CustomComponentCache INSTANCE;

	public static CustomComponentCache getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}
	
	//

	private Map<String, CustomComponentVO> customComponents;
	
	private transient TopicNotificationReceiver tnr;
	private transient MessageListener messageListener;
	

	private CustomComponentCache() {
		INSTANCE = this;
	}
	
	public void afterPropertiesSet() throws Exception {
		// Constructor might not be called - as this instance might be deserialized (tp)
		if (INSTANCE == null) {
			INSTANCE = this;
		}
		if (!wasDeserialized() || !isValid())
			customComponents = new ConcurrentHashMap<String, CustomComponentVO>();
		messageListener = new MessageListener() {
			@Override
			public void onMessage(Message msg) {
				LOG.info("onMessage " + this + " revalidate cache...");
				revalidate();
				Main.getInstance().getMainController().refreshMenusLater();
			}
		};
		tnr.subscribe(getCachingTopic(), messageListener);
		if (!wasDeserialized() || !isValid())
			revalidate();
	}
	
	public void setTopicNotificationReceiver(TopicNotificationReceiver tnr) {
		this.tnr = tnr;
	}
	
	@Override
	public String getCachingTopic() {
		return JMSConstants.TOPICNAME_CUSTOMCOMPONENTCACHE;
	}
	
	public Collection<CustomComponentVO> getAll() {
		return customComponents.values();
	}

	public Collection<String> getAllNames() {
		return customComponents.keySet();
	}

	public CustomComponentVO getByName(String internalName) {
		CustomComponentVO customComponentVO = customComponents.get(internalName);
		if (customComponentVO == null)
			throw new NuclosFatalException("No component with name " + internalName);
		return customComponentVO.clone();
	}
	
	private synchronized void revalidate() {
		customComponents = new ConcurrentHashMap<String, CustomComponentVO>();
		LOG.info("Cleared cache " + this);
		for (CustomComponentVO vo : CustomComponentDelegate.getInstance().getAll()) {
			customComponents.put(vo.getInternalName(), vo);
		}
		customComponents = Collections.unmodifiableMap(customComponents);
		LOG.info("Revalidated (filled) cache " + this);
	}
}
