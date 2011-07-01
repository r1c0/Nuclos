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
package org.nuclos.server.report.ejb3;

import java.util.Date;

import javax.ejb.Remote;

import org.nuclos.common2.exception.CommonBusinessException;

@Remote
public interface SchedulerControlFacadeRemote {

	public abstract Date scheduleReportJob(String reportName, int iHour,
		int iMinute);

	public abstract boolean unscheduleJob(String jobName) throws CommonBusinessException;

	/**
	 * schedules the TimelimitJob daily at the given time
	 * @param iHour
	 * @param iMinute
	 * @return
	 */
	public abstract Date scheduleTimelimitJob(int iHour, int iMinute);

	/**
	 * @return the names of all scheduled jobs.
	 */
	public abstract String[] getJobNames();

}
