package org.nuclos.server.eventsupport.valueobject;

import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.valueobject.NuclosValueObject;

public class EventSupportEventVO extends NuclosValueObject
{
	private String  sEventSupportClass;
	private String  sEntity;
	private Integer iProcessId;
	private Integer iStateId;
	private Integer iOrder;
	
	
	public EventSupportEventVO(String sEventSupportClass, String sEntity,
			Integer iProcessId, Integer iStateId, Integer iOrder) {
		super();
		this.sEventSupportClass = sEventSupportClass;
		this.sEntity = sEntity;
		this.iProcessId = iProcessId;
		this.iStateId = iStateId;
		this.iOrder = iOrder;
	}
	
	
	public String getEventSupportClass() {
		return sEventSupportClass;
	}
	public void setEventSupportClass(String sEventSupportClass) {
		this.sEventSupportClass = sEventSupportClass;
	}
	public String getEntity() {
		return sEntity;
	}
	public void setEntity(String sEntity) {
		this.sEntity = sEntity;
	}
	public Integer getProcessId() {
		return iProcessId;
	}
	public void setProcessId(Integer iProcessId) {
		this.iProcessId = iProcessId;
	}
	public Integer getStateId() {
		return iStateId;
	}
	public void setStateId(Integer iStateId) {
		this.iStateId = iStateId;
	}
	public Integer getOrder() {
		return iOrder;
	}
	public void setOrder(Integer iOrder) {
		this.iOrder = iOrder;
	}
	
	/**
	 * validity checker
	 */
	@Override
	public void validate() throws CommonValidationException {
		if (StringUtils.isNullOrEmpty(getEntity())) {
			throw new CommonValidationException("ruleengine.error.validation.rule.name");
		}
		if (StringUtils.isNullOrEmpty(this.getEventSupportClass())) {
			throw new CommonValidationException("ruleengine.error.validation.rule.description");
		}
	
	}
	
}
