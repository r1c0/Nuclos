//Copyright (C) 2012  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.server.common.ejb3;

import javax.annotation.security.RolesAllowed;

import org.nuclos.api.Message;
import org.nuclos.api.service.MessageContextService;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.api.ApiMessageImpl;
import org.nuclos.server.common.MessageReceiverContext;
import org.nuclos.server.jms.NuclosJMSUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

@Transactional(noRollbackFor= {Exception.class})
@RolesAllowed("Login")
public class ApiMessageContextService implements MessageContextService {

	@Override
	public void sendMessage(Message message) {
		ApiMessageImpl apiMsg = new ApiMessageImpl(message, MessageReceiverContext.getInstance().getId());
		NuclosJMSUtils.sendObjectMessageAfterCommit(apiMsg, JMSConstants.TOPICNAME_RULENOTIFICATION, SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
	}

}
