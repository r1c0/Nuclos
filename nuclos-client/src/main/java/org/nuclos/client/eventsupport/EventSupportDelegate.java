package org.nuclos.client.eventsupport;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.nuclos.client.statemodel.SortedRuleVO;
import org.nuclos.client.statemodel.StateDelegate;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.collection.Pair;
import org.nuclos.server.eventsupport.ejb3.EventSupportFacadeRemote;
import org.nuclos.server.eventsupport.valueobject.EventSupportEventVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportGenerationVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportJobVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportSourceVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportTransitionVO;
import org.nuclos.server.eventsupport.valueobject.ProcessVO;
import org.nuclos.server.statemodel.valueobject.StateTransitionVO;

public class EventSupportDelegate {

	private static EventSupportDelegate INSTANCE;
	private static final Logger LOG = Logger.getLogger(EventSupportManagementController.class);	
	
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
	
	public List<SortedRuleVO> selectEventSupportByESTransition(List<Pair<EventSupportTransitionVO, Boolean>> eventSupportsWithRunAfterwards) {
		List<SortedRuleVO> retVal = new ArrayList<SortedRuleVO> ();
		try {
			for (Pair<EventSupportTransitionVO, Boolean> pair : eventSupportsWithRunAfterwards) {
				EventSupportTransitionVO x = pair.getX();
				EventSupportSourceVO esvo = EventSupportRepository.getInstance().getEventSupportByClassname(x.getEventSupportClass());
				SortedRuleVO sortedRuleVO = new SortedRuleVO(null, esvo.getName(), esvo.getDescription(), x.getOrder(), pair.y);
				sortedRuleVO.setClassname(esvo.getClassname());
				retVal.add(sortedRuleVO);
			}			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return retVal;
	}
	
	public void forceEventSupportCompilation() {
		this.eventSupportFacadeRemote.forceEventSupportCompilation();
	}
	
	public List<SortedRuleVO> selectEventSupportByClassname(Integer iTransId, List<Pair<EventSupportTransitionVO, Boolean>> eventsupportsWithRunAfterwards) {
		List<SortedRuleVO> retVal = new ArrayList<SortedRuleVO> ();
		try {
			Collection<EventSupportTransitionVO> esetById = EventSupportRepository.getInstance().getEventSupportsByTransitionId(iTransId);
			 
			for (Pair<EventSupportTransitionVO, Boolean> pair : eventsupportsWithRunAfterwards) {
				
				for (EventSupportTransitionVO eset : esetById) {
					if (eset.getEventSupportClass().equals(pair.getX().getEventSupportClass()) && 
							eset.getTransitionId().equals(pair.getX().getTransitionId())) {
						EventSupportSourceVO esvo = EventSupportRepository.getInstance().getEventSupportByClassname(pair.getX().getEventSupportClass());
						SortedRuleVO sortedRuleVO = new SortedRuleVO(null, esvo.getName(),esvo.getDescription(),eset.getOrder(), eset.isRunAfterwards());
						sortedRuleVO.setClassname(esvo.getClassname());
						retVal.add(sortedRuleVO);
						break;
					}
				}				
			}
			
		} catch (RemoteException e) {
			LOG.error(e.getMessage(), e);
		}
		return retVal;
	}
	
//	/**
//	 * @param collsortedrulevoFilter
//	 */
//	public List<SortedRuleVO> filterEventSupportByVO(Collection<SortedRuleVO> collsortedrulevoFilter) {
//		final List<SortedRuleVO> result = new LinkedList<SortedRuleVO>();
//
//		try {
//			final Map<String, SortedRuleVO> filterMap = CollectionUtils.newHashMap();
//			for (SortedRuleVO sortedrulevo : collsortedrulevoFilter) {
//				filterMap.put(sortedrulevo.getName(), sortedrulevo);
//			}
//			Map<String, List<EventSupportVO>> eventSupportsByType = EventSupportRepository.getInstance().getEventSupportsByType();
//			for (List<EventSupportVO> stateModelRuleVO : eventSupportsByType.values()) {
//				for (EventSupportVO esvo : stateModelRuleVO) {
//					if (!filterMap.containsKey(esvo.getName())) {
//						result.add(new SortedRuleVO(esvo));
//					}				
//				}
//			}			
//		}
//		catch (Exception e) {
//			LOG.error(e.getMessage(), e);
//		}
//		
//		return result;
//	}
	
	/**
	 * @param filterID
	 */
	public SortedRuleVO selectEventSupportByClassname(Pair<String, Boolean> pair ) {
		
		SortedRuleVO sortedRuleVO = null;
		try {
			EventSupportSourceVO esvo = EventSupportRepository.getInstance().getEventSupportByClassname(pair.getX());
			sortedRuleVO = new SortedRuleVO(esvo, 0, pair.getY());
			
		} catch (RemoteException e) {
			LOG.error(e.getMessage(), e);
		}
		
		return sortedRuleVO;
	}
	
	public EventSupportJobVO createEventSupportJob(EventSupportJobVO esjVOToInsert) {
		EventSupportJobVO retVal = null;
		
		try {
			retVal = eventSupportFacadeRemote.createEventSupportJob(esjVOToInsert);			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return retVal;
	}
	
	public EventSupportTransitionVO createEventSupportTransition(EventSupportTransitionVO eseVOToInsert) {
		EventSupportTransitionVO retVal = null;
		try {
			retVal = eventSupportFacadeRemote.createEventSupportTransition(eseVOToInsert);			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return retVal;
	}
	
	public EventSupportGenerationVO createEventSupportGeneration(EventSupportGenerationVO eseVOToInsert) {
		EventSupportGenerationVO retVal = null;
		
		try {
			retVal = eventSupportFacadeRemote.createEventSupportGeneration(eseVOToInsert);			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return retVal;
	}
	
	public EventSupportEventVO createEventSupportEvent(EventSupportEventVO eseVOToInsert)
	{
		EventSupportEventVO retVal = null;
		try {
			retVal = eventSupportFacadeRemote.createEventSupportEvent(eseVOToInsert);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	
		return retVal;
	}
	
	public void deleteEventSupportTransition(EventSupportTransitionVO eseVOToUpdate) {
		
		try {
			eventSupportFacadeRemote.deleteEventSupportTransition(eseVOToUpdate);
			EventSupportRepository.getInstance().updateEventSupports();
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		
	}
	
	public void deleteEventSupportJob(EventSupportJobVO eseVOToUpdate) {
		
		try {
			eventSupportFacadeRemote.deleteEventSupportJob(eseVOToUpdate);
			EventSupportRepository.getInstance().updateEventSupports();
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		
	}

	public void deleteEventSupportGeneration(EventSupportGenerationVO eseVOToUpdate) {
		
		try {
			eventSupportFacadeRemote.deleteEventSupportGeneration(eseVOToUpdate);
			EventSupportRepository.getInstance().updateEventSupports();
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		
	}

	public void deleteEventSupportEvents(Integer entityId, String eventSupportType) {
		
		try {
			eventSupportFacadeRemote.deleteEventSupportEvents(entityId, eventSupportType);
			EventSupportRepository.getInstance().updateEventSupports();
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		
	}

	public void deleteEventSupportEvent(EventSupportEventVO eseVOToUpdate) {
		
		try {
			eventSupportFacadeRemote.deleteEventSupportEvent(eseVOToUpdate);
			EventSupportRepository.getInstance().updateEventSupports();
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		
	}

		
	public EventSupportEventVO modifyEventSupportEvent(EventSupportEventVO eseVOToUpdate) {
		EventSupportEventVO retVal = null;
		
		try {
			retVal = eventSupportFacadeRemote.modifyEventSupportEvent(eseVOToUpdate);
			EventSupportRepository.getInstance().updateEventSupports();
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return retVal;
	}
	
	public EventSupportTransitionVO modifyEventSupportTransition(EventSupportTransitionVO eseVOToUpdate) {
		EventSupportTransitionVO retVal = null;
		
		try {
			retVal = eventSupportFacadeRemote.modifyEventSupportTransition(eseVOToUpdate);
			EventSupportRepository.getInstance().updateEventSupports();
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return retVal;
	}
	
	public EventSupportJobVO modifyEventSupportJob(EventSupportJobVO eseVOToUpdate) {
		EventSupportJobVO retVal = null;
		
		try {
			retVal = eventSupportFacadeRemote.modifyEventSupportJob(eseVOToUpdate);
			EventSupportRepository.getInstance().updateEventSupports();
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return retVal;
	}
	
	public EventSupportGenerationVO modifyEventSupportGeneration(EventSupportGenerationVO eseVOToUpdate) {
		EventSupportGenerationVO retVal = null;
		
		try {
			retVal = eventSupportFacadeRemote.modifyEventSupportGeneration(eseVOToUpdate);
			EventSupportRepository.getInstance().updateEventSupports();
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return retVal;
	}
	
	public Collection<EventSupportEventVO> getEventSupportsForEntity(Integer entityId)
	{
		Collection<EventSupportEventVO> retVal = null;
		
		try {
			retVal = EventSupportRepository.getInstance().getEventSupportsForEntity(entityId);
			
		} catch (RemoteException e) {
			LOG.error(e.getMessage(), e);
		}
		
		return retVal;
	}
	
	public Collection<ProcessVO> getProcessesByModuleId(Integer entityId)
	{
		Collection<ProcessVO> retVal = null;
		
		try {
			retVal = EventSupportRepository.getInstance().getProcessesByModuleId(entityId);
			
		} catch (RemoteException e) {
			LOG.error(e.getMessage(), e);
		}
		
		return retVal;
	}
	

	public List<EventSupportTransitionVO> getStateTransitionsBySupportType(Integer moduleId, String supporttype) {
		List<EventSupportTransitionVO> retVal = new ArrayList<EventSupportTransitionVO>();
		
		try {
			for (StateTransitionVO stVO : StateDelegate.getInstance().getOrderedStateTransitionsByStatemodel(moduleId)) {
				Collection<EventSupportTransitionVO> eventSupportsByTransitionId = EventSupportRepository.getInstance().getEventSupportsByTransitionId(stVO.getId());
				for (EventSupportTransitionVO estVO : eventSupportsByTransitionId) {
					EventSupportSourceVO eventSupportByClassname = EventSupportRepository.getInstance().getEventSupportByClassname(estVO.getEventSupportClass());
					if (eventSupportByClassname.getInterface().equals(supporttype)) {
						retVal.add(estVO);
					}
				}	
			}			
		} catch (Exception e) {
			Log.error(e.getMessage(), e);
		}
		
		return retVal;
	}
	
}
