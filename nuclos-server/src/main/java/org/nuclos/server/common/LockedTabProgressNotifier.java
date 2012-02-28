//Copyright (C) 2010  Novabit Informationssysteme GmbH
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
package org.nuclos.server.common;

import org.apache.log4j.Logger;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.LockedTabProgressNotification;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.jms.NuclosJMSUtils;

/**
 * Utility class to notify clients of locked tab progress.
 *
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class LockedTabProgressNotifier {
	
	private static final Logger log = Logger.getLogger(LockedTabProgressNotifier.class);

	private final String correlationId;

	public LockedTabProgressNotifier(String correaltionId) {
		super();
		this.correlationId = correaltionId;		
	}

	public void notify(String message, Integer percent) {
		if (!StringUtils.isNullOrEmpty(correlationId)) {
			LockedTabProgressNotification notification = new LockedTabProgressNotification(message, percent);
			log.info("JMS send LockedTabProgressNotification " + notification + ": " + this);
			NuclosJMSUtils.sendObjectMessageAfterCommit(notification, JMSConstants.TOPICNAME_LOCKEDTABPROGRESSNOTIFICATION, correlationId);
		}
	}

}
