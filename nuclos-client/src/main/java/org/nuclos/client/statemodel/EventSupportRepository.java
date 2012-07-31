package org.nuclos.client.statemodel;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.eventsupport.ejb3.EventSupportFacadeRemote;
import org.nuclos.server.eventsupport.valueobject.EventSupportVO;
import org.springframework.beans.factory.InitializingBean;

public class EventSupportRepository implements InitializingBean {

	private static EventSupportRepository INSTANCE;
	
	// Spring injection
	private final Map<String, List<EventSupportVO>> mpEventSupportsByType = CollectionUtils.newHashMap();
	private final List<EventSupportVO> lstEventSupportTypes = new ArrayList<EventSupportVO>();
	
	
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
	
		try {
			// Cache all useable EventSupport ordered by type
			Collection<EventSupportVO> allEventSupports = eventSupportFacadeRemote.getAllEventSupports();
			for (EventSupportVO esVO : allEventSupports)
			{
				if (!mpEventSupportsByType.containsKey(esVO.getInterface()))
				{
					mpEventSupportsByType.put(esVO.getInterface(), new ArrayList<EventSupportVO>());
				}
				mpEventSupportsByType.get(esVO.getInterface()).add(
						new EventSupportVO(esVO.getName(),esVO.getDescription(),esVO.getClassname(), esVO.getInterface()));
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
	
	public List<SortedRuleVO> selectEventSupportById(List<Pair<String, Boolean>> eventsupportsWithRunAfterwards) {
		List<String> classnames = new ArrayList<String>();
		List<String> withRunAfterwards = new ArrayList<String>();
		for (Pair<String, Boolean> rule : eventsupportsWithRunAfterwards) {
			classnames.add(rule.x);
			if (rule.y != null && rule.y)
				withRunAfterwards.add(rule.x);
		}
		return selectEventSupportById(classnames, withRunAfterwards);
	}
	
	public List<EventSupportVO> selectEventSupportsById(String EventSupportType) {
		return mpEventSupportsByType.get(EventSupportType);
	}
	
	/**
	 * @param filterID
	 */
	public List<SortedRuleVO> selectEventSupportById(Collection<String> filterID, Collection<String> rulesRunAfterwards) {
		final List<SortedRuleVO> result = new LinkedList<SortedRuleVO>();

		int iCount = 1;
		for (Iterator<String> i = filterID.iterator(); i.hasNext(); iCount++) {
			final String iId = i.next();
			
			for (List<EventSupportVO> stateModelRuleVO : mpEventSupportsByType.values()) {
				for (EventSupportVO esvo : stateModelRuleVO) {					
					if (esvo.getName() != null && iId.equals(esvo.getName())) {
						SortedRuleVO sortedRuleVO = new SortedRuleVO(esvo);
						result.add(sortedRuleVO);
					}									
				}
			}			
		}
		return result;
	}
	
	/**
	 * @param collsortedrulevoFilter
	 */
	public List<SortedRuleVO> filterEventSupportByVO(Collection<SortedRuleVO> collsortedrulevoFilter) {
		final List<SortedRuleVO> result = new LinkedList<SortedRuleVO>();

		final Map<String, SortedRuleVO> filterMap = CollectionUtils.newHashMap();
		for (SortedRuleVO sortedrulevo : collsortedrulevoFilter) {
			filterMap.put(sortedrulevo.getName(), sortedrulevo);
		}

		for (List<EventSupportVO> stateModelRuleVO : mpEventSupportsByType.values()) {
			for (EventSupportVO esvo : stateModelRuleVO) {
				if (!filterMap.containsKey(esvo.getName())) {
					result.add(new SortedRuleVO(esvo));
				}				
			}
		}
		return result;
	}
	
	public List<EventSupportVO> getEventSupportTypes()
	{
		return lstEventSupportTypes;
	}
	
	public List<EventSupportVO> getEventSupportsByType(String typename)
	{
		return mpEventSupportsByType.containsKey(typename) ? mpEventSupportsByType.get(typename) : new ArrayList<EventSupportVO>();
	}
	
}
