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
import java.util.HashMap;
import java.util.Map;

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

public class CustomComponentCache {
	
	private static final Logger LOG = Logger.getLogger(CustomComponentCache.class);

	private static CustomComponentCache singleton;

	public static synchronized CustomComponentCache getInstance() {
		if (singleton == null) {
			try {
				singleton = new CustomComponentCache();
			}
			catch (RuntimeException ex) {
				throw new CommonFatalException(ex);
			}
		}
		return singleton;
	}

	private Map<String, CustomComponentVO> customComponents;
	
	private final MessageListener messagelistener = new MessageListener() {
		@Override
		public void onMessage(Message msg) {
			revalidate();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						Main.getMainController().refreshMenus();
					}
					catch (Exception e) {
						LOG.error("onMessage failed: " + e, e);
					}
				}
			});
		}
	};

	private CustomComponentCache() {
		TopicNotificationReceiver.subscribe(JMSConstants.TOPICNAME_CUSTOMCOMPONENTCACHE, messagelistener);
		revalidate();
	}
	
	public synchronized Collection<CustomComponentVO> getAll() {
		return customComponents.values();
	}

	public synchronized Collection<String> getAllNames() {
		return customComponents.keySet();
	}

	public synchronized CustomComponentVO getByName(String internalName) {
		CustomComponentVO customComponentVO = customComponents.get(internalName);
		if (customComponentVO == null)
			throw new NuclosFatalException("No component with name " + internalName);
		return customComponentVO.clone();
	}
	
	private synchronized void revalidate() {
		customComponents = new HashMap<String, CustomComponentVO>();
		for (CustomComponentVO vo : CustomComponentDelegate.getInstance().getAll()) {
			customComponents.put(vo.getInternalName(), vo);
		}
		customComponents = Collections.unmodifiableMap(customComponents);
	}
}
