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

import javax.ejb.Local;

import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.job.valueobject.JobVO;
import org.quartz.Trigger;

@Local
public interface SchedulerControlFacadeLocal {

	/**
	 * schedules the Job at the given time
	 *
	 * @param jobVO
	 * @return
	 */
	public abstract Trigger scheduleJob(JobVO jobVO) throws CommonBusinessException;

	/**
	 * unschedules job by name
	 *
	 * @param jobVO
	 * @return true if unscheduling was successful, otherwise false
	 */
	public abstract boolean unscheduleJob(JobVO jobVO) throws CommonBusinessException;

	/**
	 * @return the names of all scheduled jobs.
	 */
	public abstract String[] getJobNames();
}
