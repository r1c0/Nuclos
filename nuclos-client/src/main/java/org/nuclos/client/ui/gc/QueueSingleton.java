//Copyright (C) 2012  Novabit Informationssysteme GmbH
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
//
package org.nuclos.client.ui.gc;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.EventListener;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

// @Component
public class QueueSingleton implements InitializingBean {
	
	private static final Logger LOG = Logger.getLogger(QueueSingleton.class);

	// 10 sec
	private static final long INTERVAL = 10 * 1000L;
	
	private static QueueSingleton INSTANCE;
	
	//
	
	private Timer timer;
	
	private final ReferenceQueue<EventListener> queue = new ReferenceQueue<EventListener>();
	
	private final Map<Reference<EventListener>, IRegister> eventListener2Register 
		= new ConcurrentHashMap<Reference<EventListener>, IRegister>();
	
	QueueSingleton() {
		INSTANCE = this;
	}
	
	// @Autowired
	public final void setTimer(Timer timer) {
		this.timer = timer;
	}
	
	// @PostConstruct
	public final void afterPropertiesSet() {
		timer.schedule(new QueueTask(), INTERVAL, INTERVAL);
	}
	
	public static QueueSingleton getInstance() {
		return INSTANCE;
	}
	
	public void register(IRegister register) {
		eventListener2Register.put(register.getReference(), register);
	}
	
	public ReferenceQueue<EventListener> getQueue() {
		return queue;
	}
	
	private class QueueTask extends TimerTask {
		
		private QueueTask() {
		}
		
		@Override
		public void run() {
			Reference<? extends Object> ref;
			do {
				ref = queue.poll();
				if (ref != null) {
					// final Object o = ref.get();
					final IRegister c = eventListener2Register.remove(ref);
					if (c != null) {
						c.unregister();
						LOG.debug("unregistered " + c + ", mapSize=" + eventListener2Register.size());
					}
					else {
						LOG.debug("removed gc'ed ref " + ref + ", mapSize=" + eventListener2Register.size());
					}
				}
			} while (ref != null);
		}
		
	}

}
