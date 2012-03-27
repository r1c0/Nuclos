package org.nuclos.client.ui.gc;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.Reference;
import java.util.EventListener;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QueueSingleton {
	
	private static final Logger LOG = Logger.getLogger(QueueSingleton.class);

	// 10 sec
	private static final long INTERVAL = 10 * 1000L;
	
	private static QueueSingleton INSTANCE;
	
	//
	
	private Timer timer;
	
	private ReferenceQueue<EventListener> queue = new ReferenceQueue<EventListener>();
	
	private Map<Reference<EventListener>, IRegister> map 
		= new ConcurrentHashMap<Reference<EventListener>, IRegister>();
	
	QueueSingleton() {
		INSTANCE = this;
	}
	
	@Autowired
	final void setTimer(Timer timer) {
		this.timer = timer;
	}
	
	@PostConstruct
	final void init() {
		timer.schedule(new QueueTask(), INTERVAL, INTERVAL);
	}
	
	public static QueueSingleton getInstance() {
		return INSTANCE;
	}
	
	public void register(IRegister register) {
		map.put(register.getReference(), register);
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
					final IRegister c = map.remove(ref);
					if (c != null) {
						c.unregister();
					}
					LOG.info("unregistered " + c);
				}
			} while (ref != null);
		}
		
	}

}
