package org.nuclos.server.api.eventsupport;

import org.nuclos.api.eventsupport.JobEventObject;

public class TimelimitEventObjectImpl implements JobEventObject {

	private Integer iSessionId;
	
	public TimelimitEventObjectImpl(Integer pSessionId) {
		this.iSessionId = pSessionId;
	}
	
	@Override
	public void setSessionId(Integer pSessionId) {
		this.iSessionId = pSessionId;
	}

	@Override
	public Integer getSessionId() {
		return this.iSessionId;
	}

}
