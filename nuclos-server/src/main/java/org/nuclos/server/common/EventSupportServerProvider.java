package org.nuclos.server.common;

import java.util.Collection;

import org.nuclos.common.EventSupportProvider;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityObjectProcessor;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.springframework.stereotype.Component;

@Component("eventSupportProvider") 
public class EventSupportServerProvider implements EventSupportProvider {

	@Override
	public Collection<EntityObjectVO> getDependants(String sEntity) throws CommonFatalException {
		JdbcEntityObjectProcessor entityObjectProcessor = NucletDalProvider.getInstance().getEntityObjectProcessor(sEntity);
		return entityObjectProcessor.getAll();
	}

}
