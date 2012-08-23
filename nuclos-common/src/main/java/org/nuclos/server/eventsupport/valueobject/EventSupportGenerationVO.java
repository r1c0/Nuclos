package org.nuclos.server.eventsupport.valueobject;

import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.valueobject.NuclosValueObject;

public class EventSupportGenerationVO extends EventSupportVO {

	private Boolean bRunAfterwards;
	private Integer iGeneration;
	
	
	public EventSupportGenerationVO(NuclosValueObject nvo, Integer pOrder, Integer pGeneration, String pEventSupportClass, Boolean pRunAfterwards) {
		super(nvo, pOrder, pEventSupportClass);
		this.bRunAfterwards = pRunAfterwards;
		this.iGeneration = pGeneration;
	}
	
	public EventSupportGenerationVO(Integer pOrder, Integer pGeneration, String pEventSupportClass, Boolean pRunAfterwards) {
		super(pOrder, pEventSupportClass);
		this.bRunAfterwards = pRunAfterwards;
		this.iGeneration = pGeneration;
	}
	
	public Boolean isRunAfterwards() {
		return bRunAfterwards;
	}

	public void setRunAfterwards(Boolean bRunAfterwards) {
		this.bRunAfterwards = bRunAfterwards;
	}

	public Integer getGeneration() {
		return iGeneration;
	}

	public void setGeneration(Integer iGeneration) {
		this.iGeneration = iGeneration;
	}

	/**
	 * validity checker
	 */
	@Override
	public void validate() throws CommonValidationException {
		
		super.validate();
		
		if (getGeneration() == null || getGeneration().intValue() == 0) {
			throw new CommonValidationException("ruleengine.error.validation.rule.name");
		}
		if (StringUtils.isNullOrEmpty(this.getEventSupportClass())) {
			throw new CommonValidationException("ruleengine.error.validation.rule.description");
		}
	
	}
	
}
