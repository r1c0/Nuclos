package org.nuclos.server.eventsupport.ejb3;

import java.util.Collection;
import java.util.List;

import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.eventsupport.valueobject.EventSupportEventVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportTransitionVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportVO;
import org.nuclos.server.eventsupport.valueobject.ProcessVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.statemodel.valueobject.StateTransitionVO;

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
	public Collection<EventSupportVO> getAllEventSupports() throws CommonPermissionException;
	
	public List<EventSupportVO> getAllEventSupportTypes() throws CommonPermissionException;
	
	public Collection<EventSupportVO> getEventSupportsByClasstype(List<Class<?>> listOfinterfaces)
			throws CommonPermissionException;

	public List<EventSupportEventVO> getAllEventSupportsForEntity(Integer entityname) throws CommonPermissionException;
	
	public EventSupportEventVO createEventSupportEvent(EventSupportEventVO eseVOToInsert) throws CommonPermissionException, CommonValidationException, NuclosBusinessRuleException, CommonCreateException;

	public EventSupportEventVO modifyEventSupportEvent(EventSupportEventVO eseVOToUpdate);
	
	public void deleteEventSupportEvent(EventSupportEventVO eseVOToUpdate);
	
	public EventSupportTransitionVO createEventSupportTransition(EventSupportTransitionVO eseVOToInsert) throws CommonPermissionException, CommonValidationException, NuclosBusinessRuleException, CommonCreateException;

	public EventSupportTransitionVO modifyEventSupportTransition(EventSupportTransitionVO eseVOToUpdate);
	
	public void deleteEventSupportTransition(EventSupportTransitionVO eseVOToUpdate);
	
	public List<ProcessVO> getProcessesByModuleId(Integer moduleId);
	
	public Collection<EventSupportTransitionVO> getEventSupportsByTransitionId(Integer transId) throws CommonFinderException, CommonPermissionException  ;

	public void deleteEventSupportEvents(Integer entityId,String eventSupportType) throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException, CommonRemoveException, CommonStaleVersionException;		
	
}
