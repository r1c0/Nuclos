package org.nuclos.server.eventsupport.ejb3;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.nuclos.common.UsageCriteria;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.eventsupport.valueobject.EventSupportEventVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportGenerationVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportJobVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportTransitionVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportSourceVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportTypeVO;
import org.nuclos.server.eventsupport.valueobject.ProcessVO;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.job.valueobject.JobVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.nuclos.server.statemodel.valueobject.StateModelVO;

/**
 * @author reichama
 *
 */
// @Remote
public interface EventSupportFacadeRemote 
{

	
	/**
	 * This Method returns all available Classes that can be used a EventSupport-Files
	 * due to the given interface(s)
	 * 
	 * @param liste - List of all interfaces to searched for
	 * @return
	 * @throws CommonPermissionException
	 */
	public Collection<EventSupportSourceVO> getAllEventSupports() throws CommonPermissionException;
	
	public List<EventSupportTypeVO> getAllEventSupportTypes() throws CommonPermissionException;
	
	public Collection<EventSupportSourceVO> getEventSupportsByClasstype(List<Class<?>> listOfinterfaces)
			throws CommonPermissionException;

	public List<EventSupportEventVO> getAllEventSupportsForEntity(Integer entityname) throws CommonPermissionException;
	
	public EventSupportJobVO createEventSupportJob(EventSupportJobVO esjVOToInsert) throws CommonPermissionException, CommonValidationException, NuclosBusinessRuleException, CommonCreateException;

	public EventSupportJobVO modifyEventSupportJob(EventSupportJobVO esjVOToUpdate);
	
	public void deleteEventSupportJob(EventSupportJobVO esjVOToUpdate);
	
	public EventSupportEventVO createEventSupportEvent(EventSupportEventVO eseVOToInsert) throws CommonPermissionException, CommonValidationException, NuclosBusinessRuleException, CommonCreateException;

	public EventSupportEventVO modifyEventSupportEvent(EventSupportEventVO eseVOToUpdate);
	
	public void deleteEventSupportEvent(EventSupportEventVO eseVOToUpdate);
	
	public EventSupportTransitionVO createEventSupportTransition(EventSupportTransitionVO eseVOToInsert) throws CommonPermissionException, CommonValidationException, NuclosBusinessRuleException, CommonCreateException;

	public EventSupportTransitionVO modifyEventSupportTransition(EventSupportTransitionVO eseVOToUpdate);
	
	public void deleteEventSupportTransition(EventSupportTransitionVO eseVOToUpdate);
	
	public List<ProcessVO> getProcessesByModuleId(Integer moduleId);
	
	public Collection<EventSupportTransitionVO> getEventSupportsByTransitionId(Integer transId) throws CommonFinderException, CommonPermissionException  ;

	public void deleteEventSupportEvents(Integer entityId,String eventSupportType) throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException, CommonRemoveException, CommonStaleVersionException;		
	
	public List<EventSupportJobVO> getAllEventSupportsForJob(Integer jobId) throws CommonPermissionException;

	public List<EventSupportGenerationVO> getEventSupportsByGenerationId(Integer genId) throws CommonPermissionException;
	
	public EventSupportGenerationVO createEventSupportGeneration(EventSupportGenerationVO eseVOToInsert) throws CommonPermissionException, CommonValidationException, NuclosBusinessRuleException, CommonCreateException;

	public EventSupportGenerationVO modifyEventSupportGeneration(EventSupportGenerationVO eseVOToUpdate);
	
	public void deleteEventSupportGeneration(EventSupportGenerationVO eseVOToUpdate);

	public List<EventSupportEventVO> getEventSupportEntitiesByClassname(String classname);
	
	public List<StateModelVO> getStateModelsByEventSupportClassname(String classname);
	
	public List<JobVO> getJobsByClassname(String classname);
	
	public List<GeneratorActionVO> getGenerationsByClassname(String classname);

	public void forceEventSupportCompilation();
	
	public Collection<RuleVO> findEventSupportsByUsageAndEntity(String sEventclass, UsageCriteria usagecriteria);

	public Map<String, List<EventSupportGenerationVO>> getGenerationsByUsage();
	
	public Map<String, List<EventSupportJobVO>> getJobsByUsage();
	
	public Map<String, List<EventSupportEventVO>> getEntitiesByUsage();
	
	public Map<String, List<EventSupportTransitionVO>> getTransitionsByUsage();
	
}
