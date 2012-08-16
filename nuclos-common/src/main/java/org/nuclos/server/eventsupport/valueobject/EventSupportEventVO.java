package org.nuclos.server.eventsupport.valueobject;

import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.valueobject.NuclosValueObject;

public class EventSupportEventVO extends NuclosValueObject
{
	// Foreing keys
	private Integer iEntity;
	private Integer iProcessId;
	private Integer iStateId;
	// Attributes
	private String  sEventSupportClass;
	private	String  sEventSupportType;
	private Integer iOrder;
	private String  sEntityName;
	private String  sStateName;
	private String  sProcessName;
	
	public EventSupportEventVO(NuclosValueObject nvo, String sEventSupportClass, String sEventSupportType, Integer iEntity,
			Integer iProcessId, Integer iStateId, Integer iOrder,String pEntityName,String pStateName, String pProcessName) {
		super(nvo);
		
		this.sEventSupportClass = sEventSupportClass;
		this.sEventSupportType = sEventSupportType;
		this.iEntity = iEntity;
		this.iProcessId = iProcessId;
		this.iStateId = iStateId;
		this.iOrder = iOrder;
		this.sProcessName = pProcessName;
		this.sStateName = pStateName;
		this.sEntityName = pEntityName;
	}
	
	public EventSupportEventVO(String sEventSupportClass, String sEventSupportType, Integer iEntity,
			Integer iProcessId, Integer iStateId, Integer iOrder,String pEntityName,String pStateName, String pProcessName) {
		
		this.sEventSupportClass = sEventSupportClass;
		this.sEventSupportType = sEventSupportType;
		this.iEntity = iEntity;
		this.iProcessId = iProcessId;
		this.iStateId = iStateId;
		this.iOrder = iOrder;
		this.sProcessName = pProcessName;
		this.sStateName = pStateName;
		this.sEntityName = pEntityName;
	}
	

	public String getEntityName() {
		return sEntityName;
	}



	public void setEntityName(String sEntityName) {
		this.sEntityName = sEntityName;
	}



	public String getStateName() {
		return sStateName;
	}



	public void setStateName(String sStateName) {
		this.sStateName = sStateName;
	}



	public String getProcessName() {
		return sProcessName;
	}



	public void setProcessName(String sProcessName) {
		this.sProcessName = sProcessName;
	}



	public String getEventSupportType() {
		return sEventSupportType;
	}


	public void setEventSupportType(String sEventSupportType) {
		this.sEventSupportType = sEventSupportType;
	}

	public String getEventSupportClass() {
		return sEventSupportClass;
	}
	public void setEventSupportClass(String sEventSupportClass) {
		this.sEventSupportClass = sEventSupportClass;
	}
	public Integer getEntity() {
		return iEntity;
	}
	public void setEntity(Integer iEntity) {
		this.iEntity = iEntity;
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
		
		if (getEntity() == null || getEntity().intValue() == 0) {
			throw new CommonValidationException("ruleengine.error.validation.rule.name");
		}
		if (StringUtils.isNullOrEmpty(this.getEventSupportClass())) {
			throw new CommonValidationException("ruleengine.error.validation.rule.description");
		}
	
	}

}
