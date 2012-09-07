package org.nuclos.client.eventsupport;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nuclos.client.statemodel.StateDelegate;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.eventsupport.ejb3.EventSupportFacadeRemote;
import org.nuclos.server.eventsupport.valueobject.EventSupportEventVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportGenerationVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportJobVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportSourceVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportTransitionVO;
import org.nuclos.server.eventsupport.valueobject.ProcessVO;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.job.valueobject.JobVO;
import org.nuclos.server.statemodel.valueobject.StateModelVO;
import org.nuclos.server.statemodel.valueobject.StateTransitionVO;
import org.springframework.beans.factory.InitializingBean;

public class EventSupportRepository implements InitializingBean {

	private static EventSupportRepository INSTANCE;
	private static final Logger LOG = Logger.getLogger(EventSupportRepository.class);	
	
	// Spring injection
	private final List<EventSupportSourceVO> lstEventSupportTypes = new ArrayList<EventSupportSourceVO>();
	private final List<EventSupportSourceVO> lstEventSupports = new ArrayList<EventSupportSourceVO>();
	
	private final Map<String, List<EventSupportSourceVO>> mpEventSupportsByType = CollectionUtils.newHashMap();
	private final Map<String, List<EventSupportEventVO>> mpEventSupportEventsByClass = CollectionUtils.newHashMap();
	private final Map<String, List<StateModelVO>> mpStatemodelsByClass = CollectionUtils.newHashMap();
	private final Map<String, List<JobVO>> mpJobsByClass = CollectionUtils.newHashMap();
	private final Map<String, List<GeneratorActionVO>> mpGenerationsByClass = CollectionUtils.newHashMap();
	private final Map<String, EventSupportSourceVO> mpEventSupportsByClass = CollectionUtils.newHashMap();
	private final Map<Integer, List<EventSupportEventVO>> mpEventSupportsByEntity = CollectionUtils.newHashMap();
	private final Map<Integer, List<EventSupportJobVO>> mpEventSupportsByJob = CollectionUtils.newHashMap();
	private final Map<Integer, List<ProcessVO>> mpProcessesByEntity = CollectionUtils.newHashMap();
			
	private final Map<Integer, Collection<EventSupportTransitionVO>> mpEventSupportsByTransition = CollectionUtils.newHashMap();
	private final Map<Integer, Collection<EventSupportGenerationVO>> mpEventSupportsByGeneration = CollectionUtils.newHashMap();
	private final Map<Integer, List<String>> mpEventSupportTypesByGeneration = CollectionUtils.newHashMap();
	private final Map<Integer, List<String>> mpEventSupportTypesByStatemodel = CollectionUtils.newHashMap();
	
	private EventSupportFacadeRemote eventSupportFacadeRemote;

	public final void setEventSupportFacadeRemote(EventSupportFacadeRemote eventSupportFacadeRemote) {
		this.eventSupportFacadeRemote = eventSupportFacadeRemote;
	}
	
	// end of Spring injection

	EventSupportRepository() {
		
	}

	public static EventSupportRepository getInstance() throws RemoteException {
		if (INSTANCE == null) {
			INSTANCE = (EventSupportRepository)SpringApplicationContextHolder.getBean("eventSupportRepository");
		}
		return INSTANCE;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		updateEventSupports();
	}
	
	public void updateEventSupports()
	{
		
		lstEventSupportTypes.clear();
		lstEventSupports.clear();
		
		mpEventSupportsByType.clear();
		mpEventSupportsByClass.clear();
		mpEventSupportsByEntity.clear();
		mpEventSupportsByTransition.clear();
		mpProcessesByEntity.clear();
		mpEventSupportsByJob.clear();
		mpEventSupportsByGeneration.clear();
		mpEventSupportTypesByGeneration.clear();
		mpEventSupportTypesByStatemodel.clear();
		mpEventSupportEventsByClass.clear();
		mpStatemodelsByClass.clear();
		mpJobsByClass.clear();
		mpGenerationsByClass.clear();
		
		try {
			// Cache all useable EventSupport ordered by type
			Collection<EventSupportSourceVO> allEventSupports = eventSupportFacadeRemote.getAllEventSupports();
			for (EventSupportSourceVO esVO : allEventSupports)
			{
				mpEventSupportsByClass.put(esVO.getClassname(), esVO);
				
				if (!mpEventSupportsByType.containsKey(esVO.getInterface()))
				{
					mpEventSupportsByType.put(esVO.getInterface(), new ArrayList<EventSupportSourceVO>());
				}
				mpEventSupportsByType.get(esVO.getInterface()).add(
						new EventSupportSourceVO(esVO.getName(),esVO.getDescription(),esVO.getClassname(), esVO.getInterface(), esVO.getPackage(), esVO.getCreatedAt()));
			}
			 
			// Cache all registered EventSupport Types
			for (EventSupportSourceVO c: eventSupportFacadeRemote.getAllEventSupportTypes())
			{
				lstEventSupportTypes.add(c);
			}
			
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
	}
	
	public List<EventSupportSourceVO> getAllEventSupports() {
		List<EventSupportSourceVO> retVal = new ArrayList<EventSupportSourceVO>();
		
		if (!lstEventSupports.isEmpty()) {
			retVal = lstEventSupports;
		}
		else {
			try {
				retVal.addAll(eventSupportFacadeRemote.getAllEventSupports());
				lstEventSupports.addAll(retVal);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}			
		}
			 
		return retVal;
	}
	
	public List<EventSupportSourceVO> selectEventSupportsById(String EventSupportType) {
		return mpEventSupportsByType.get(EventSupportType);
	}
		
	public List<EventSupportSourceVO> getEventSupportTypes()
	{
		return lstEventSupportTypes;
	}
	
	public List<EventSupportSourceVO> getEventSupportsByType(String typename)
	{
		return mpEventSupportsByType.containsKey(typename) ? mpEventSupportsByType.get(typename) : new ArrayList<EventSupportSourceVO>();
	}
	
	public List<EventSupportSourceVO> getEventSupportsByTypes(String... typename)
	{
		List<EventSupportSourceVO> retVal = new ArrayList<EventSupportSourceVO>();
		for (int idx=0; idx < typename.length; idx++) {
			List<EventSupportSourceVO> lstEseVo = 
					mpEventSupportsByType.containsKey(typename[idx]) ? mpEventSupportsByType.get(typename[idx]) : new ArrayList<EventSupportSourceVO>();
			for (EventSupportSourceVO ese : lstEseVo) {
				retVal.add(ese);
			}
		}
		return retVal;
	}
	
	public Map<String, List<EventSupportSourceVO>> getEventSupportsByType()
	{
		return mpEventSupportsByType;
	}
	
	public EventSupportSourceVO getEventSupportByClassname(String classname)
	{
		EventSupportSourceVO retVal = null;
		if (classname != null && mpEventSupportsByClass.containsKey(classname))
		{
			retVal = mpEventSupportsByClass.get(classname);
		}
		return retVal;
	}
	
	public EventSupportSourceVO getEventSupportTypeByName(String classname)
	{
		EventSupportSourceVO retVal = null;
		
		for (EventSupportSourceVO esvo : lstEventSupportTypes)
		{
			if (esvo.getClassname().equals(classname))
			{
				retVal = esvo;
				break;
			}
		}
		
		return retVal;
	}
	
	public List<String> getEventSupportTypesByGenerationId(Integer genId)
	{
		List<String> retVal = new ArrayList<String>();
		
		if (mpEventSupportTypesByGeneration.containsKey(genId)) {
			retVal = mpEventSupportTypesByGeneration.get(genId);
		} 
		else {
			Collection<EventSupportGenerationVO> lstesgVO = this.getEventSupportsByGenerationId(genId);
			for (EventSupportGenerationVO esgVO : lstesgVO) {
				if (!retVal.contains(esgVO.getEventSupportClass()))
					retVal.add(this.getEventSupportByClassname(esgVO.getEventSupportClass()).getInterface());
			}
			mpEventSupportTypesByGeneration.put(genId, retVal);
		}
		
		return retVal;
	}
	

	public List<String> getEventSupportTypesByStateModelId(Integer modelId)
	{
		List<String> retVal = new ArrayList<String>();
		
		if (mpEventSupportTypesByStatemodel.containsKey(modelId)) {
			retVal = mpEventSupportTypesByStatemodel.get(modelId);
		} 
		else {
			List<EventSupportTransitionVO> lstesgVO = this.getEventSupportsByStateModelId(modelId);
			for (EventSupportTransitionVO esgVO : lstesgVO) {
				String esType = this.getEventSupportByClassname(esgVO.getEventSupportClass()).getInterface();
				if (!retVal.contains(esType))
					retVal.add(esType);
			}
			mpEventSupportTypesByStatemodel.put(modelId, retVal);
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
				LOG.error(e.getMessage(), e);
			}
		}
		return retVal;
	}
	
	public Collection<EventSupportJobVO> getEventSupportsForJob(Integer jobId)
	{
		Collection<EventSupportJobVO> retVal = null;
		if (mpEventSupportsByJob.containsValue(jobId))
		{
			retVal = mpEventSupportsByJob.get(jobId);
		}
		else
		{
			try {
				retVal = eventSupportFacadeRemote.getAllEventSupportsForJob(jobId);
				mpEventSupportsByJob.put(jobId, new ArrayList(retVal));				
			} catch (CommonPermissionException e) { 
				LOG.error(e.getMessage(), e);
			}
		}
		return retVal;
	}
	
	public Collection<EventSupportGenerationVO> getEventSupportsForGeneration(Integer genId)
	{
		Collection<EventSupportGenerationVO> retVal = null;
		if (mpEventSupportsByGeneration.containsValue(genId))
		{
			retVal = mpEventSupportsByGeneration.get(genId);
		}
		else
		{
			try {
				retVal = eventSupportFacadeRemote.getEventSupportsByGenerationId(genId);
				mpEventSupportsByJob.put(genId, new ArrayList(retVal));				
			} catch (CommonPermissionException e) { 
				LOG.error(e.getMessage(), e);
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
				LOG.error(e.getMessage(), e);
			}
		}
		
		return retVal;
	}
	
	public List<EventSupportTransitionVO> getEventSupportsByStateModelId(Integer modelId) {
		List<EventSupportTransitionVO> retVal = null;
		
		List<StateTransitionVO> orderedTrans = StateDelegate.getInstance().getOrderedStateTransitionsByStatemodel(modelId);
		if (orderedTrans.size() > 0)
			retVal = new ArrayList<EventSupportTransitionVO>();
		
		for (StateTransitionVO stVO : orderedTrans) {
			Collection<EventSupportTransitionVO> eventSupportsByTransitionId = this.getEventSupportsByTransitionId(stVO.getId());
			for (EventSupportTransitionVO vo : eventSupportsByTransitionId) {
				retVal.add(vo);
			}
		}
		
		return retVal;
	}
	
	public Collection<EventSupportGenerationVO> getEventSupportsByGenerationId(Integer genId) {
		Collection<EventSupportGenerationVO> retVal = null;
		
		if (mpEventSupportsByGeneration.containsKey(genId)) {
			retVal = mpEventSupportsByGeneration.get(genId);
		}
		else {
			try {
				retVal = eventSupportFacadeRemote.getEventSupportsByGenerationId(genId);				
				mpEventSupportsByGeneration.put(genId, retVal);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
	
		return retVal;
	}

	public List<EventSupportEventVO> getEventSupportEventsByClassname(String classname) {
		List<EventSupportEventVO> retVal;
		
		if (mpEventSupportEventsByClass.containsKey(classname)) {
			retVal = mpEventSupportEventsByClass.get(classname);
		}
		else
		{
			retVal  = new ArrayList<EventSupportEventVO>();
			retVal.addAll(eventSupportFacadeRemote.getEventSupportEventsByClassname(classname));
			mpEventSupportEventsByClass.put(classname, retVal);
		}
		return retVal;
	}
	
	public List<StateModelVO> getStateModelByEventSupportClassname(String classname) {
		List<StateModelVO> retVal ;
		
		if (mpStatemodelsByClass.containsKey(classname)) {
			retVal = mpStatemodelsByClass.get(classname);
		}
		else
		{
			retVal  = eventSupportFacadeRemote.getStateModelsByEventSupportClassname(classname);
			mpStatemodelsByClass.put(classname, retVal);
		}
		
		return retVal;
	}
	
	public List<JobVO> getJobsByEventSupportClassname(String classname) {
		List<JobVO> retVal ;
		
		if (mpJobsByClass.containsKey(classname)) {
			retVal = mpJobsByClass.get(classname);
		}
		else
		{
			retVal  = eventSupportFacadeRemote.getJobsByClassname(classname);
			mpJobsByClass.put(classname, retVal);
		}
		
		return retVal;
	}
	
	public List<GeneratorActionVO> getGenerationsByEventSupportClassname(String classname) {
		List<GeneratorActionVO> retVal ;
		
		if (mpGenerationsByClass.containsKey(classname)) {
			retVal = mpGenerationsByClass.get(classname);
		}
		else
		{
			retVal  = eventSupportFacadeRemote.getGenerationsByClassname(classname);
			mpGenerationsByClass.put(classname, retVal);
		}
		
		return retVal;
	}
	
}
