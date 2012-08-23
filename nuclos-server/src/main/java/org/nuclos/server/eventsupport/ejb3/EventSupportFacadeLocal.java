package org.nuclos.server.eventsupport.ejb3;

import java.util.Collection;

import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.eventsupport.valueobject.EventSupportTransitionVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportSourceVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO;

public interface EventSupportFacadeLocal {

	public RuleObjectContainerCVO fireEventSupports(Integer iSourceStateId, Integer iTargetStateId,
			RuleObjectContainerCVO loccvoBefore, boolean b) throws NuclosBusinessRuleException ;

	public Collection<EventSupportSourceVO> getAllEventSupports() throws CommonPermissionException;
	
	public Collection<EventSupportTransitionVO> getEventSupportsByTransitionId(Integer transId) throws CommonFinderException, CommonPermissionException  ;
	
}
