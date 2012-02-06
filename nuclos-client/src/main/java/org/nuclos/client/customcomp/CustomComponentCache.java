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

import javax.annotation.PostConstruct;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.nuclos.client.common.TopicNotificationReceiver;
import org.nuclos.client.main.Main;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.customcomp.valueobject.CustomComponentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class CustomComponentCache {
	
	private static final Logger LOG = Logger.getLogger(CustomComponentCache.class);

	private static CustomComponentCache INSTANCE;

	public static synchronized CustomComponentCache getInstance() {
		if (INSTANCE == null) {
			try {
				INSTANCE = new CustomComponentCache();
			}
			catch (RuntimeException ex) {
				throw new CommonFatalException(ex);
			}
		}
		return INSTANCE;
	}
	
	//

	private Map<String, CustomComponentVO> customComponents;
	
	private TopicNotificationReceiver tnr;
	
	private final MessageListener messagelistener = new MessageListener() {
		@Override
		public void onMessage(Message msg) {
			revalidate();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						Main.getInstance().getMainController().refreshMenus();
					}
					catch (Exception e) {
						LOG.error("onMessage failed: " + e, e);
					}
				}
			});
		}
	};

	private CustomComponentCache() {
	}
	
	@PostConstruct
	void init() {
		tnr.subscribe(JMSConstants.TOPICNAME_CUSTOMCOMPONENTCACHE, messagelistener);
		revalidate();
	}
	
	@Autowired
	void setTopicNotificationReceiver(TopicNotificationReceiver tnr) {
		this.tnr = tnr;
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
