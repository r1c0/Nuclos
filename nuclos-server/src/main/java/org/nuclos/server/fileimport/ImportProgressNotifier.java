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
package org.nuclos.server.fileimport;

import org.apache.log4j.Logger;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.ProgressNotification;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.jms.NuclosJMSUtils;

/**
 * Utility class to notify clients of current import progress.
 *
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class ImportProgressNotifier {

	private static final Logger LOG = Logger.getLogger(ImportProgressNotifier.class);

	private String correlationId;

	private int progressCurrent;
	private int progressMaximum;
	private int percent;

	private String message;
	private int currentState;

	private int lastNotifiedProgress = 0;

	public ImportProgressNotifier(String correaltionId) {
		super();
		this.correlationId = correaltionId;
		this.progressCurrent = 0;		
	}

	public void setNextStep(int nextstep, int percent) {
		this.progressCurrent = Double.valueOf(Double.valueOf(nextstep * this.percent) / Double.valueOf(percent - this.percent)).intValue();
		this.progressMaximum = progressCurrent + nextstep;
		this.percent = percent;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void start() {
		notify(new ProgressNotification("import.notification.started", 0, 100, 0, ProgressNotification.RUNNING));
	}

	public void increment() {
		progressCurrent++;
		notifyClients();
	}

	public void stop(String reason) {
		notify(new ProgressNotification(reason == null ? "import.notification.stopped" : reason, 0, 100, lastNotifiedProgress, ProgressNotification.STOPPED));
	}

	public void finish() {
		notify(new ProgressNotification("import.notification.finished", 0, 100, 100, ProgressNotification.FINISHED));
	}

	private void notifyClients() {
		int newvalue = Double.valueOf((Double.valueOf(progressCurrent) / Double.valueOf(progressMaximum)) * percent).intValue();
		if (lastNotifiedProgress < newvalue) {
			lastNotifiedProgress = newvalue;
			notify(new ProgressNotification(message, 0, 100, lastNotifiedProgress, currentState));
		}
	}

	private void notify(ProgressNotification notification) {
		if (!StringUtils.isNullOrEmpty(correlationId)) {
			LOG.info("JMS send import progress notification " + notification + ": " + this);
			NuclosJMSUtils.sendObjectMessageAfterCommit(notification, JMSConstants.TOPICNAME_PROGRESSNOTIFICATION, correlationId);
		}
	}

}
