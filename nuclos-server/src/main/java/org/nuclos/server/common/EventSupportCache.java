package org.nuclos.server.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.eventsupport.ejb3.EventSupportFacadeLocal;
import org.nuclos.server.eventsupport.valueobject.EventSupportVO;
import org.nuclos.server.mbean.MBeanAgent;

public class EventSupportCache 
{

	private static final Logger LOG = Logger.getLogger(EventSupportCache.class);
	
	private static EventSupportCache INSTANCE;

	private final Map<String, EventSupportVO> mpEvtSupportsByClass	= new ConcurrentHashMap<String, EventSupportVO>();
	
	EventSupportCache() {
		INSTANCE = this;
	}
	
	public static EventSupportCache getInstance()
	{
		return INSTANCE;
	}
	
		
	public EventSupportVO getEventSupport(String classname)
	{
		if (mpEvtSupportsByClass.isEmpty())
		{
			loadEventSupportClasses();
		}
		
		return mpEvtSupportsByClass.get(classname);
	}
	
	private void loadEventSupportClasses ()
	{
		EventSupportFacadeLocal facade = ServerServiceLocator.getInstance().getFacade(EventSupportFacadeLocal.class);
		try {
			for (EventSupportVO ev : facade.getAllEventSupports())
			{
				mpEvtSupportsByClass.put(ev.getClassname(), ev);
			}
			
		} catch (CommonPermissionException e) 
		{
			LOG.error("Error while reading registered SupportEvents from server");
		}
	}
	
	
}
