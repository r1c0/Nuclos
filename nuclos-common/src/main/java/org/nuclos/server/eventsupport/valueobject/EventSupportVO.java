package org.nuclos.server.eventsupport.valueobject;

import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.valueobject.NuclosValueObject;

public class EventSupportVO extends NuclosValueObject {
	
	private String  sEventSupportClass;
	private Integer iOrder;
	
	public EventSupportVO(NuclosValueObject nvo, Integer pOrder, String pEventsupportClass) {
		super(nvo);
		this.iOrder = pOrder;
		this.sEventSupportClass = pEventsupportClass;
	}


	public EventSupportVO(Integer pOrder, String pEventsupportClass) {
		this.iOrder = pOrder;
		this.sEventSupportClass = pEventsupportClass;
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
	
	@Override
	public void validate() throws CommonValidationException {
		if (StringUtils.isNullOrEmpty(this.getEventSupportClass())) {
			throw new CommonValidationException("ruleengine.error.validation.rule.description");
		}
	}
}
