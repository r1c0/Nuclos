package org.nuclos.common;

import java.util.Collection;

import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.exception.CommonFatalException;

public interface EventSupportProvider {

	public Collection<EntityObjectVO> getDependants(String sEntity) throws CommonFatalException;
}
