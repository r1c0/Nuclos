package org.nuclos.server.eventsupport.valueobject;

import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.valueobject.NuclosValueObject;

/**
 * @author reichama
 * @version 00.01.000
 */
public class EventSupportTransitionVO extends EventSupportVO 
{	
	private Integer iTransitionId;
	private String  sTransitionName;
	private Boolean bRunAfterwards;
	
	
	public EventSupportTransitionVO(NuclosValueObject nvo, String sEventSupportClass, String pEventSupportType, 
			Integer iTransitionId, Integer iOrder, Boolean bRunAfterwards) {
		super(nvo, iOrder, sEventSupportClass, pEventSupportType);
		this.iTransitionId = iTransitionId;
		this.bRunAfterwards = bRunAfterwards;
	}

	public EventSupportTransitionVO(String sEventSupportClass,String pEventSupportType,
			Integer iTransitionId, Integer iOrder, Boolean bRunAfterwards) {
		super(iOrder, sEventSupportClass, pEventSupportType);
		this.iTransitionId = iTransitionId;
		this.bRunAfterwards = bRunAfterwards;
	}

	@Override
	public String toString() {
		return " ID: " + this.getId() + " Generation Id: " + this.getTransitionId()
				+ " ES-Class: " + this.getEventSupportClass();
	}

	public Integer getTransitionId() {
		return iTransitionId;
	}

	public void setTransitionId(Integer iTransitionId) {
		this.iTransitionId = iTransitionId;
	}

	public String getTransitionName() {
		return sTransitionName;
	}

	public void setTransitionName(String sTransitionName) {
		this.sTransitionName = sTransitionName;
	}

	public Boolean isRunAfterwards() {
		return bRunAfterwards;
	}


	public void setRunAfterwards(Boolean bRunAfterwards) {
		this.bRunAfterwards = bRunAfterwards;
	}


	/**
	 * validity checker
	 */
	@Override
	public void validate() throws CommonValidationException {
		super.validate();
		if (getTransitionId() == null || getTransitionId().intValue() == 0) {
			throw new CommonValidationException("ruleengine.error.validation.rule.name");
		}
	}
	
}
