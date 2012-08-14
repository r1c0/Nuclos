package org.nuclos.client.eventsupport;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jfree.util.Log;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTransferVO;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.eventsupport.ejb3.EventSupportFacadeRemote;
import org.nuclos.server.eventsupport.valueobject.EventSupportEventVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportTransitionVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportVO;
import org.nuclos.server.eventsupport.valueobject.ProcessVO;
import org.nuclos.server.statemodel.valueobject.StateTransitionVO;
import org.springframework.beans.factory.InitializingBean;

public class EventSupportRepository implements InitializingBean {

	private static EventSupportRepository INSTANCE;
	
	// Spring injection
	private final Map<String, List<EventSupportVO>> mpEventSupportsByType = CollectionUtils.newHashMap();
	private final Map<String, EventSupportVO> mpEventSupportsByClass = CollectionUtils.newHashMap();
	private final Map<Integer, List<EventSupportEventVO>> mpEventSupportsByEntity = CollectionUtils.newHashMap();
	private final Map<Integer, List<ProcessVO>> mpProcessesByEntity = CollectionUtils.newHashMap();
	private final List<EventSupportVO> lstEventSupportTypes = new ArrayList<EventSupportVO>();
	private final Map<Integer, Collection<EventSupportTransitionVO>> mpEventSupportsByTransition = CollectionUtils.newHashMap();
	
	private EventSupportFacadeRemote eventSupportFacadeRemote;

	public final void setEventSupportFacadeRemote(EventSupportFacadeRemote eventSupportFacadeRemote) {
		this.eventSupportFacadeRemote = eventSupportFacadeRemote;
	}
	
	// end of Spring injection

	EventSupportRepository() {
		
	}

	public static EventSupportRepository getInstance() throws RemoteException {
		if (INSTANCE == null) {
			INSTANCE = SpringApplicationContextHolder.getBean(EventSupportRepository.class);
		}
		return INSTANCE;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		updateEventSupports();
	}
	
	public void updateEventSupports()
	{
		mpEventSupportsByType.clear();
		lstEventSupportTypes.clear();
		mpEventSupportsByClass.clear();
		mpEventSupportsByEntity.clear();
		mpEventSupportsByTransition.clear();
		
		try {
			// Cache all useable EventSupport ordered by type
			Collection<EventSupportVO> allEventSupports = eventSupportFacadeRemote.getAllEventSupports();
			for (EventSupportVO esVO : allEventSupports)
			{
				mpEventSupportsByClass.put(esVO.getClassname(), esVO);
				
				if (!mpEventSupportsByType.containsKey(esVO.getInterface()))
				{
					mpEventSupportsByType.put(esVO.getInterface(), new ArrayList<EventSupportVO>());
				}
				mpEventSupportsByType.get(esVO.getInterface()).add(
						new EventSupportVO(esVO.getName(),esVO.getDescription(),esVO.getClassname(), esVO.getInterface(), esVO.getPackage(), esVO.getCreatedAt()));
			}
			 
			// Cache all registered EventSupport Types
			for (EventSupportVO c: eventSupportFacadeRemote.getAllEventSupportTypes())
			{
				lstEventSupportTypes.add(c);
			}
			
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
	}
	
	
	
	public List<EventSupportVO> selectEventSupportsById(String EventSupportType) {
		return mpEventSupportsByType.get(EventSupportType);
	}
		
	public List<EventSupportVO> getEventSupportTypes()
	{
		return lstEventSupportTypes;
	}
	
	public List<EventSupportVO> getEventSupportsByType(String typename)
	{
		return mpEventSupportsByType.containsKey(typename) ? mpEventSupportsByType.get(typename) : new ArrayList<EventSupportVO>();
	}
	
	public Map<String, List<EventSupportVO>> getEventSupportsByType()
	{
		return mpEventSupportsByType;
	}
	
	public EventSupportVO getEventSupportByClassname(String classname)
	{
		EventSupportVO retVal = null;
		if (classname != null && mpEventSupportsByClass.containsKey(classname))
		{
			retVal = mpEventSupportsByClass.get(classname);
		}
		return retVal;
	}
	
	public EventSupportVO getEventSupportTypeByName(String classname)
	{
		EventSupportVO retVal = null;
		
		for (EventSupportVO esvo : lstEventSupportTypes)
		{
			if (esvo.getClassname().equals(classname))
			{
				retVal = esvo;
				break;
			}
		}
		
		return retVal;
	}
	
	public Collection<EventSupportEventVO> getEventSupportsForEntity(Integer entityId)
	{
		Collection<EventSupportEventVO> retVal = null;
		if (mpEventSupportsByEntity.containsValue(entityId))
		{
			retVal = mpEventSupportsByEntity.get(entityId);
		}
		else
		{
			try {
				retVal = eventSupportFacadeRemote.getAllEventSupportsForEntity(entityId);
				mpEventSupportsByEntity.put(entityId, new ArrayList(retVal));				
			} catch (CommonPermissionException e) { 
				Log.error(e.getMessage(), e);
			}
		}
		return retVal;
	}
	
	public Collection<ProcessVO> getProcessesByModuleId(Integer entityId)
	{
		Collection<ProcessVO> retVal = null;
		if (mpProcessesByEntity.containsValue(entityId))
		{
			retVal = mpProcessesByEntity.get(entityId);
		}
		else
		{
			retVal = eventSupportFacadeRemote.getProcessesByModuleId(entityId);
			mpProcessesByEntity.put(entityId, new ArrayList(retVal));				

		}
		return retVal;
	}

	public Collection<EventSupportTransitionVO> getEventSupportsByTransitionId(Integer transid) {
		Collection<EventSupportTransitionVO> retVal = null;
		
		if (mpEventSupportsByTransition.containsKey(transid)) {
			retVal = mpEventSupportsByTransition.get(transid);
		}
		else {
			try {
				retVal = eventSupportFacadeRemote.getEventSupportsByTransitionId(transid);				
				mpEventSupportsByTransition.put(transid, retVal);
			} catch (Exception e) {
				Log.error(e.getMessage(), e);
			}
		}
		
		return retVal;
	}
}
