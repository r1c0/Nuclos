package org.nuclos.client.eventsupport;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nuclos.client.masterdata.MasterDataCache;
import org.nuclos.client.statemodel.StateDelegate;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.eventsupport.ejb3.EventSupportFacadeRemote;
import org.nuclos.server.eventsupport.valueobject.EventSupportEventVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportGenerationVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportJobVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportSourceVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportTransitionVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportTypeVO;
import org.nuclos.server.eventsupport.valueobject.ProcessVO;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.job.valueobject.JobVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.statemodel.valueobject.StateModelVO;
import org.nuclos.server.statemodel.valueobject.StateTransitionVO;
import org.springframework.beans.factory.InitializingBean;

public class EventSupportRepository implements InitializingBean {

	private static EventSupportRepository INSTANCE;
	private static final Logger LOG = Logger.getLogger(EventSupportRepository.class);	
	
	// Spring injection
	private final List<EventSupportTypeVO> lstEventSupportTypes = new ArrayList<EventSupportTypeVO>();
	private final List<EventSupportSourceVO> lstEventSupports = new ArrayList<EventSupportSourceVO>();
	
	private final Map<String, List<EventSupportSourceVO>> mpEventSupportsByType = CollectionUtils.newHashMap();
	private final Map<String, List<EventSupportEventVO>> mpEventSupportEventsByClass = CollectionUtils.newHashMap();
	private final Map<String, List<StateModelVO>> mpStatemodelsByClass = CollectionUtils.newHashMap();
	private final Map<String, EventSupportSourceVO> mpEventSupportsByClass = CollectionUtils.newHashMap();
	private final Map<Integer, List<EventSupportEventVO>> mpEventSupportsByEntity = CollectionUtils.newHashMap();
	private final Map<Integer, List<EventSupportJobVO>> mpEventSupportsByJob = CollectionUtils.newHashMap();
	private final Map<String, List<EventSupportSourceVO>> mpEventSupportsByNuclet = CollectionUtils.newHashMap();
	private final Map<Integer, List<ProcessVO>> mpProcessesByEntity = CollectionUtils.newHashMap();
			
	private final Map<String, List<GeneratorActionVO>> mpGenerationsByClass = CollectionUtils.newHashMap();
	private final Map<String, List<JobVO>> mpJobsByClass = CollectionUtils.newHashMap();
	
	private final Map<Integer, Collection<EventSupportTransitionVO>> mpEventSupportsByTransition = CollectionUtils.newHashMap();
	private final Map<Integer, Collection<EventSupportGenerationVO>> mpEventSupportsByGeneration = CollectionUtils.newHashMap();
	private final Map<Integer, List<String>> mpEventSupportTypesByGeneration = CollectionUtils.newHashMap();
	private final Map<Integer, List<String>> mpEventSupportTypesByStatemodel = CollectionUtils.newHashMap();
	
	private Map<String, List<EventSupportGenerationVO>> mpGenerationsByNuclet = CollectionUtils.newHashMap();
	private Map<String, List<EventSupportEventVO>> mpEntitiesByNuclet = CollectionUtils.newHashMap();
	private Map<String, List<EventSupportTransitionVO>> mpTransitionsByNuclet = CollectionUtils.newHashMap();
	private Map<String, List<EventSupportJobVO>> mpJobsByNuclet = CollectionUtils.newHashMap();
	
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
		mpEventSupportsByNuclet.clear();
		mpGenerationsByNuclet.clear();
		
		try {
			List<MasterDataVO> list = MasterDataCache.getInstance().get(NuclosEntity.NUCLET.getEntityName());
			List<String> lstValidNucletPackages = new ArrayList<String>();
			
			mpGenerationsByNuclet = eventSupportFacadeRemote.getGenerationsByUsage();
			mpJobsByNuclet = eventSupportFacadeRemote.getJobsByUsage();
			mpEntitiesByNuclet = eventSupportFacadeRemote.getEntitiesByUsage();
			mpTransitionsByNuclet = eventSupportFacadeRemote.getTransitionsByUsage();
			
			for (MasterDataVO mdvo : list) {
				String curPackage = mdvo.getField("package", String.class);
				mpEventSupportsByNuclet.put(curPackage, new ArrayList<EventSupportSourceVO>());
				lstValidNucletPackages.add(curPackage);
			}
			mpEventSupportsByNuclet.put("<Default>", new ArrayList<EventSupportSourceVO>());
			
			// Cache all useable EventSupport ordered by type
			Collection<EventSupportSourceVO> allEventSupports = eventSupportFacadeRemote.getAllEventSupports();
			for (EventSupportSourceVO esVO : allEventSupports)
			{
				boolean toNucletPackage = false;
				for (String sPackage : lstValidNucletPackages) {
					if (esVO.getPackage().toLowerCase().startsWith(sPackage.toLowerCase())) {
						mpEventSupportsByNuclet.get(sPackage).add(esVO);
						toNucletPackage = true;
						break;
					}
				}
				if (!toNucletPackage) {
					mpEventSupportsByNuclet.get("<Default>").add(esVO);
				}
				
				mpEventSupportsByClass.put(esVO.getClassname(), esVO);
				
				for (String sInterfaces : esVO.getInterface()) {
					if (!mpEventSupportsByType.containsKey(sInterfaces))
					{
						mpEventSupportsByType.put(sInterfaces, new ArrayList<EventSupportSourceVO>());
					}
					mpEventSupportsByType.get(sInterfaces).add(
							new EventSupportSourceVO(esVO.getName(),esVO.getDescription(),esVO.getClassname(), esVO.getInterface(), esVO.getPackage(), esVO.getCreatedAt()));
				}
			}
			 
			// Cache all registered EventSupport Types
			for (EventSupportTypeVO c: eventSupportFacadeRemote.getAllEventSupportTypes())
			{
				String mspLocaledName = SpringLocaleDelegate.getInstance().getMessage(c.getName(), c.getName());
				String mspLocaledDesc = SpringLocaleDelegate.getInstance().getMessage(c.getDescription(), c.getDescription());
				c.setName(mspLocaledName);
				c.setDescription(mspLocaledDesc);
				lstEventSupportTypes.add(c);
			}
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
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
		
	public List<EventSupportTypeVO> getEventSupportTypes()
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
	
	public List<EventSupportSourceVO> searchForEventSupports(String searchString)
	{
		List<EventSupportSourceVO> retVal = new ArrayList<EventSupportSourceVO>();
	
		for (EventSupportSourceVO eseVO : lstEventSupports ) {
			if (eseVO.getName().toLowerCase().contains(searchString.toLowerCase())) {
				retVal.add(eseVO);
			}
		}
		return retVal;
	}

	public EventSupportTypeVO getEventSupportTypeByName(String classname)
	{
		EventSupportTypeVO retVal = null;
		
		for (EventSupportTypeVO esvo : lstEventSupportTypes)
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
				retVal.add(esgVO.getEventSupportClassType());
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
				if (!retVal.contains(esgVO.getEventSupportClassType()))
					retVal.add(esgVO.getEventSupportClassType());
			}
			mpEventSupportTypesByStatemodel.put(modelId, retVal);
		}
		
		return retVal;
	}
	
	public Collection<EventSupportEventVO> getEventSupportsForEntity(Integer entityId)
	{
		Collection<EventSupportEventVO> retVal = null;
		if (mpEventSupportsByEntity.containsKey(entityId))
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

	public List<EventSupportEventVO> getEventSupportEntitiesByClassname(String classname, String classtype) {
		List<EventSupportEventVO> retVal = new ArrayList<EventSupportEventVO>();
		
		if (mpEventSupportEventsByClass.containsKey(classname)) {
			List<EventSupportEventVO> lstEventSuppotEntities = mpEventSupportEventsByClass.get(classname);
			if (classtype != null) {
				for (EventSupportEventVO eseVO : lstEventSuppotEntities) {
					if (eseVO.getEventSupportClassType().equals(classtype)) {
						retVal.add(eseVO);
					}
				}				
			}
			else {
				retVal = lstEventSuppotEntities;
			}
		}
		else
		{
			List<EventSupportEventVO> lstEventSuppotEntities = eventSupportFacadeRemote.getEventSupportEntitiesByClassname(classname);
			mpEventSupportEventsByClass.put(classname, lstEventSuppotEntities);
			if (classtype != null) {
				for (EventSupportEventVO eseVO : lstEventSuppotEntities) {
					if (eseVO.getEventSupportClassType().equals(classtype)) {
						retVal.add(eseVO);
					}
				}	
			}
			else
			{
				retVal.addAll(eventSupportFacadeRemote.getEventSupportEntitiesByClassname(classname));				
			}
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
	
	public List<EventSupportSourceVO> getEventSupportSourcesByPackage(String sPackage, String sSeachText) {
		List<EventSupportSourceVO> retVal;
		
		if (sSeachText == null ) {
			retVal = mpEventSupportsByNuclet.get(sPackage);
		}
		else
		{
			retVal = new ArrayList<EventSupportSourceVO>();
			for (EventSupportSourceVO eseVO : mpEventSupportsByNuclet.get(sPackage)) {
				if (eseVO.getName().toLowerCase().contains(sSeachText.toLowerCase())) {
					retVal.add(eseVO);
				}
			}
		}
		
		return retVal;
	}
	
	public List<EventSupportGenerationVO> getGenerationsByUsage(String nucletid) {
		
		List<EventSupportGenerationVO> retVal = new ArrayList<EventSupportGenerationVO>();
		
		if (mpGenerationsByNuclet.containsKey(nucletid)) {
			retVal = mpGenerationsByNuclet.get(nucletid);
		}
		
		return retVal;
	}
	
	public List<EventSupportTransitionVO> getTransitionsByUsage(String nucletid) {
		
		List<EventSupportTransitionVO> retVal = new ArrayList<EventSupportTransitionVO>();
		
		if (mpTransitionsByNuclet.containsKey(nucletid)) {
			retVal = mpTransitionsByNuclet.get(nucletid);
		}
		
		return retVal;
	}
	
	public List<EventSupportEventVO> getEntitiesByUsage(String nucletid) {
		
		List<EventSupportEventVO> retVal = new ArrayList<EventSupportEventVO>();
		
		if (mpEntitiesByNuclet.containsKey(nucletid)) {
			retVal = mpEntitiesByNuclet.get(nucletid);
		}
		
		return retVal;
	}

	public List<EventSupportJobVO> getJobsByUsage(String nucletid) {
		
		List<EventSupportJobVO> retVal = new ArrayList<EventSupportJobVO>();
		
		if (mpJobsByNuclet.containsKey(nucletid)) {
			retVal = mpJobsByNuclet.get(nucletid);
		}
		
		return retVal;
	}
}
