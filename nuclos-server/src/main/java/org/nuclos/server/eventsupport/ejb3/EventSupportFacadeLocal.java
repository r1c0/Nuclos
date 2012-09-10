package org.nuclos.server.eventsupport.ejb3;

import java.util.Collection;
import java.util.List;

import org.nuclos.common.PropertiesMap;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.eventsupport.valueobject.EventSupportJobVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportSourceVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportTransitionVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO;
import org.nuclos.server.ruleengine.valueobject.RuleVO;

public interface EventSupportFacadeLocal {

	public Collection<EventSupportSourceVO> getAllEventSupports() throws CommonPermissionException;
	
	public Collection<EventSupportTransitionVO> getEventSupportsByTransitionId(Integer transId) throws CommonFinderException, CommonPermissionException  ;
	
	public Collection<RuleVO> findEntityEventSupportByUsage(String sEventclass, UsageCriteria usagecriteria);
	
	public void fireTimelimitEventSupport(Integer jobId);
	
	public RuleObjectContainerCVO fireSaveEventSupport(Integer iEntityId, String sEventSupportType, RuleObjectContainerCVO loccvoCurrent);
	
	public void fireDeleteEventSupport(Integer iEntityId, String sEventSupportType, RuleObjectContainerCVO loccvoCurrent, boolean isLogical) ;
	
	public RuleObjectContainerCVO fireCustomEventSupport(Integer iEntityId, List<RuleVO> lstRules, RuleObjectContainerCVO loccvoCurrent, boolean bIgnoreExceptions);
	
	public RuleObjectContainerCVO fireStateTransitionEventSupport(Integer iSourceStateId, Integer iTargetStateId, RuleObjectContainerCVO loccvoBefore, boolean b) throws NuclosBusinessRuleException;
	
	public void fireGenerationEventSupport(Integer id, RuleObjectContainerCVO loccvoTargetBeforeRules, Collection<RuleObjectContainerCVO> loccvoSourceObjects,
			RuleObjectContainerCVO loccvoParameter, List<String> lstActions, PropertiesMap properties, Boolean after);
	
}
