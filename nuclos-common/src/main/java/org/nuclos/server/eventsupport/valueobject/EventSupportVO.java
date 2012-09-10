package org.nuclos.server.eventsupport.valueobject;

import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.valueobject.NuclosValueObject;

public class EventSupportVO extends NuclosValueObject {
	
	private String  sEventSupportClass;
	private String  sEventSupportClassType;
	private Integer iOrder;
	
	public EventSupportVO(NuclosValueObject nvo, Integer pOrder, String pEventsupportClass, String pEventSupportClassType) {
		super(nvo);
		this.iOrder = pOrder;
		this.sEventSupportClass = pEventsupportClass;
		this.sEventSupportClassType = pEventSupportClassType;
	}


	public EventSupportVO(Integer pOrder, String pEventsupportClass, String pEventSupportClassType) {
		this.iOrder = pOrder;
		this.sEventSupportClass = pEventsupportClass;
		this.sEventSupportClassType = pEventSupportClassType;
	}
	
	public String getEventSupportClass() {
		return sEventSupportClass;
	}

	public void setEventSupportClass(String sEventSupportClass) {
		this.sEventSupportClass = sEventSupportClass;
	}

	public Integer getOrder() {
		return iOrder;
	}

	public void setOrder(Integer iOrder) {
		this.iOrder = iOrder;
	}
		
	public String getEventSupportClassType() {
		return sEventSupportClassType;
	}

	public void setEventSupportClassType(String sEventSupportClassType) {
		this.sEventSupportClassType = sEventSupportClassType;
	}

	@Override
	public void validate() throws CommonValidationException {
		if (StringUtils.isNullOrEmpty(this.getEventSupportClass())) {
			throw new CommonValidationException("ruleengine.error.validation.rule.description");
		}
		if (StringUtils.isNullOrEmpty(this.getEventSupportClassType())) {
			throw new CommonValidationException("ruleengine.error.validation.rule.description");
		}
	}
}
