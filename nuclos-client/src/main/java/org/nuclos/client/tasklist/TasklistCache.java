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
package org.nuclos.client.tasklist;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.nuclos.client.LocalUserCaches.AbstractLocalUserCache;
import org.nuclos.client.jms.TopicNotificationReceiver;
import org.nuclos.client.main.Main;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.tasklist.TasklistDefinition;
import org.springframework.beans.factory.InitializingBean;

public class TasklistCache extends AbstractLocalUserCache implements InitializingBean {
	
	private static final Logger LOG = Logger.getLogger(TasklistCache.class);

	private static TasklistCache INSTANCE;

	public static TasklistCache getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}

	private Map<String, TasklistDefinition> tasklists;
	private Map<Integer, TasklistDefinition> tasklistsById;
	
	private transient TopicNotificationReceiver tnr;
	private transient MessageListener messageListener;
	
	private TasklistCache() {
		INSTANCE = this;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// Constructor might not be called - as this instance might be deserialized (tp)
		if (INSTANCE == null) {
			INSTANCE = this;
		}
		if (!wasDeserialized() || !isValid())
			revalidate();
		messageListener = new MessageListener() {
			@Override
			public void onMessage(Message msg) {
				LOG.info("onMessage " + this + " revalidate cache...");
				if (msg instanceof TextMessage) {
					TextMessage tmsg = (TextMessage) msg;
					try {
						if (NuclosEntity.TASKLIST.getEntityName().equals(tmsg.getText())) {
							revalidate();
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									try {
										LOG.info("onMessage " + this + " refreshMenus...");
										Main.getInstance().getMainController().refreshMenus();
									}
									catch (Exception e) {
										LOG.error("onMessage failed: " + e, e);
									}
								}
							});
						}
					}
					catch (JMSException e) {
						LOG.error(e);
					}
				}
			}
		};
		tnr.subscribe(getCachingTopic(), messageListener);
	}
	
	@Override
	public String getCachingTopic() {
		return JMSConstants.TOPICNAME_MASTERDATACACHE;
	}
	
	public void setTopicNotificationReceiver(TopicNotificationReceiver tnr) {
		this.tnr = tnr;
	}
	
	public Collection<TasklistDefinition> getTasklists() {
		return tasklists.values();
	}

	public TasklistDefinition getByName(String name) {
		TasklistDefinition result = tasklists.get(name);
		if (result == null)
			throw new NuclosFatalException("No task list with name " + name);
		return result;
	}
	
	public TasklistDefinition getById(Integer id) {
		TasklistDefinition result = tasklistsById.get(id);
		if (result == null)
			throw new NuclosFatalException("No task list with id " + id);
		return result;
	}
	
	private synchronized void revalidate() {
		tasklists = new ConcurrentHashMap<String, TasklistDefinition>();
		tasklistsById = new ConcurrentHashMap<Integer, TasklistDefinition>();
		LOG.info("Cleared cache " + this);
		for (TasklistDefinition def : TasklistDelegate.getInstance().getUsersTasklists()) {
			tasklists.put(def.getName(), def);
			tasklistsById.put(def.getId(), def);
		}
		tasklists = Collections.unmodifiableMap(tasklists);
		tasklistsById = Collections.unmodifiableMap(tasklistsById);
		LOG.info("Revalidated (filled) cache " + this);
	}
}
