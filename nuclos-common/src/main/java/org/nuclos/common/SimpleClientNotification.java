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
package org.nuclos.common;

import org.nuclos.common2.DateUtils;
import java.util.Date;

/**
 * Simple implementation of ClientNotification containing the most important information.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public class SimpleClientNotification implements ClientNotification {

	private final Priority priority;
	private final String sMessage;
	private final Date dateSent;

	public SimpleClientNotification(Priority priority, String sMessage) {
		this.priority = priority;
		this.sMessage = sMessage;
		this.dateSent = DateUtils.now();
	}

	@Override
	public Priority getPriority() {
		return priority;
	}

	@Override
	public String getMessage() {
		return sMessage;
	}

	@Override
	public Date getTimestamp() {
		return dateSent;
	}

}	// class AbstractClientNotification
