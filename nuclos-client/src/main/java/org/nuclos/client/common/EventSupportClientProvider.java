package org.nuclos.client.common;

import java.util.Collection;

import org.nuclos.common.EventSupportProvider;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.exception.CommonFatalException;
import org.springframework.stereotype.Component;

@Component("eventSupportProvider") 
public class EventSupportClientProvider implements EventSupportProvider {

	@Override
	public Collection<EntityObjectVO> getDependants(String sEntity) throws CommonFatalException{
		// TODO Auto-generated method stub
		return null;
	}

}
