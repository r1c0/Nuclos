package org.nuclos.server.eventsupport.valueobject;

import org.nuclos.server.common.valueobject.NuclosValueObject;

/**
 * @author reichama
 * @version 00.01.000
 */
public class EventSupportTransitionVO extends NuclosValueObject 
{
	private String  sEventSupportClass;
	private Integer iTransitionId;
	private Integer iOrder;
	private Boolean bRunAfterwards;
	
	
	public EventSupportTransitionVO(NuclosValueObject nvo, String sEventSupportClass,
			Integer iTransitionId, Integer iOrder, Boolean bRunAfterwards) {
		super(nvo);
		this.sEventSupportClass = sEventSupportClass;
		this.iTransitionId = iTransitionId;
		this.iOrder = iOrder;
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


	public Integer getOrder() {
		return iOrder;
	}


	public void setOrder(Integer iOrder) {
		this.iOrder = iOrder;
	}


	public Boolean isRunAfterwards() {
		return bRunAfterwards;
	}


	public void setRunAfterwards(Boolean bRunAfterwards) {
		this.bRunAfterwards = bRunAfterwards;
	}


	public String getEventSupportClass() {
		return sEventSupportClass;
	}


	public void setEventSupportClass(String sEventSupportClass) {
		this.sEventSupportClass = sEventSupportClass;
	}


	
	
}
