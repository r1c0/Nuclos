package org.nuclos.server.eventsupport.valueobject;

import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.valueobject.NuclosValueObject;

public class EventSupportJobVO extends EventSupportVO{
	
	private String  sDescription;
	private Integer iJobControllerId;
	
	public EventSupportJobVO(NuclosValueObject nvo, String pDescription, String pEventSupportClass, String pEventSupportType, Integer pOrder, Integer pJobControllerId) {
		super(nvo, pOrder, pEventSupportClass,pEventSupportType);
		this.sDescription = pDescription;
		this.iJobControllerId = pJobControllerId;
	}
	
	public EventSupportJobVO(String pDescription, String pEventSupportClass, String pEventSupportType, Integer pOrder, Integer pJobControllerId) {
		super(pOrder, pEventSupportClass, pEventSupportType);
		this.sDescription = pDescription;
		this.iJobControllerId = pJobControllerId;
	}

	public String getDescription() {
		return sDescription;
	}

	public void setDescription(String sDescription) {
		this.sDescription = sDescription;
	}

	public Integer getJobControllerId() {
		return iJobControllerId;
	}

	public void setJobControllerId(Integer iJobControllerId) {
		this.iJobControllerId = iJobControllerId;
	}
	

	/**
	 * validity checker
	 */
	@Override
	public void validate() throws CommonValidationException {
		
		super.validate();
		
		if (getJobControllerId() == null || getJobControllerId().intValue() == 0) {
			throw new CommonValidationException("ruleengine.error.validation.rule.name");
		}
	}	
}
