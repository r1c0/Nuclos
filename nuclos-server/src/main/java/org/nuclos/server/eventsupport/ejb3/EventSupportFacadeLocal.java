package org.nuclos.server.eventsupport.ejb3;

import java.util.Collection;

import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.eventsupport.valueobject.EventSupportVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO;

public interface EventSupportFacadeLocal {

	public RuleObjectContainerCVO fireEventSupports(Integer iSourceStateId, Integer iTargetStateId,
			RuleObjectContainerCVO loccvoBefore, boolean b) throws NuclosBusinessRuleException ;

	public Collection<EventSupportVO> getAllEventSupports() throws CommonPermissionException;
}
