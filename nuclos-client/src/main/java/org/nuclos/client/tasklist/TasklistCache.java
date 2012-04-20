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

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.nuclos.client.jms.TopicNotificationReceiver;
import org.nuclos.client.main.Main;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.tasklist.TasklistDefinition;
import org.nuclos.common2.exception.CommonFatalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class TasklistCache {
	
	private static final Logger LOG = Logger.getLogger(TasklistCache.class);

	private static TasklistCache INSTANCE;

	public static synchronized TasklistCache getInstance() {
		if (INSTANCE == null) {
			try {
				INSTANCE = new TasklistCache();
			}
			catch (RuntimeException ex) {
				throw new CommonFatalException(ex);
			}
		}
		return INSTANCE;
	}

	private Map<String, TasklistDefinition> tasklists;
	private Map<Integer, TasklistDefinition> tasklistsById;
	
	private TopicNotificationReceiver tnr;
	
	private final MessageListener messagelistener = new MessageListener() {
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

	private TasklistCache() {
	}
	
	@PostConstruct
	void init() {
		tnr.subscribe(JMSConstants.TOPICNAME_MASTERDATACACHE, messagelistener);
		revalidate();
	}
	
	@Autowired
	void setTopicNotificationReceiver(TopicNotificationReceiver tnr) {
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
