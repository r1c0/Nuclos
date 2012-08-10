package org.nuclos.client.eventsupport;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jfree.util.Log;
import org.nuclos.client.statemodel.SortedRuleVO;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.server.eventsupport.ejb3.EventSupportFacadeRemote;
import org.nuclos.server.eventsupport.valueobject.EventSupportEventVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportVO;

public class EventSupportDelegate {

	private static EventSupportDelegate INSTANCE;
	
	private EventSupportDelegate() {}
	
	public static EventSupportDelegate getInstance()
	{
		if (INSTANCE == null) {
			INSTANCE = SpringApplicationContextHolder.getBean(EventSupportDelegate.class);
		}
		return INSTANCE;
	}
	
	private EventSupportFacadeRemote eventSupportFacadeRemote;

	public final void setEventSupportFacadeRemote(EventSupportFacadeRemote eventSupportFacadeRemote) {
		this.eventSupportFacadeRemote = eventSupportFacadeRemote;
	}
	
	public List<SortedRuleVO> selectEventSupportById(List<Pair<String, Boolean>> eventsupportsWithRunAfterwards) {
		List<String> classnames = new ArrayList<String>();
		List<String> withRunAfterwards = new ArrayList<String>();
		for (Pair<String, Boolean> rule : eventsupportsWithRunAfterwards) {
			classnames.add(rule.x);
			if (rule.y != null && rule.y)
				withRunAfterwards.add(rule.x);
		}
		return selectEventSupportById(classnames);
	}
	
	/**
	 * @param collsortedrulevoFilter
	 */
	public List<SortedRuleVO> filterEventSupportByVO(Collection<SortedRuleVO> collsortedrulevoFilter) {
		final List<SortedRuleVO> result = new LinkedList<SortedRuleVO>();

		try {
			final Map<String, SortedRuleVO> filterMap = CollectionUtils.newHashMap();
			for (SortedRuleVO sortedrulevo : collsortedrulevoFilter) {
				filterMap.put(sortedrulevo.getName(), sortedrulevo);
			}
			Map<String, List<EventSupportVO>> eventSupportsByType = EventSupportRepository.getInstance().getEventSupportsByType();
			for (List<EventSupportVO> stateModelRuleVO : eventSupportsByType.values()) {
				for (EventSupportVO esvo : stateModelRuleVO) {
					if (!filterMap.containsKey(esvo.getName())) {
						result.add(new SortedRuleVO(esvo));
					}				
				}
			}			
		}
		catch (Exception e) {
			Log.error(e.getMessage(), e);
		}
		
		return result;
	}
	
	/**
	 * @param filterID
	 */
	public List<SortedRuleVO> selectEventSupportById(Collection<String> filterID) {
		final List<SortedRuleVO> result = new LinkedList<SortedRuleVO>();

		for (String s: filterID) {
			try {
				Map<String, List<EventSupportVO>> eventSupportsByType = EventSupportRepository.getInstance().getEventSupportsByType();
				for (List<EventSupportVO> stateModelRuleVO : eventSupportsByType.values()) {
					for (EventSupportVO esvo : stateModelRuleVO) {					
						if (esvo.getName() != null && s.equals(esvo.getName())) {
							SortedRuleVO sortedRuleVO = new SortedRuleVO(esvo);
							result.add(sortedRuleVO);
						}									
					}
				}			
			}	
			catch (Exception e) {
				Log.error(e.getMessage(), e);
			}
		}
		return result;
	}
	
	public EventSupportEventVO create(EventSupportEventVO eseVOToInsert)
	{
		EventSupportEventVO retVal = null;
	
		try {
			retVal = eventSupportFacadeRemote.create(eseVOToInsert);
			
		} catch (Exception e) {
			Log.error(e.getMessage(), e);
		}
		return retVal;
	}
	
	public Collection<EventSupportEventVO> getEventSupportsForEntity(Integer entityId)
	{
		Collection<EventSupportEventVO> retVal = null;
		
		try {
			retVal = EventSupportRepository.getInstance().getEventSupportsForEntity(entityId);
			
		} catch (RemoteException e) {
			Log.error(e.getMessage(), e);
		}
		
		return retVal;
	}
	
}
