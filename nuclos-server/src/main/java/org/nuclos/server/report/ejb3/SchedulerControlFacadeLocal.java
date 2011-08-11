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
	 * Create a quartz job.
	 *
	 * @param job Job to create
	 * @throws CommonBusinessException
	 */
	public void addJob(JobVO job) throws CommonBusinessException;

	/**
	 * Delete a quartz job including all existing triggers.
	 *
	 * @param jobname The name of the job to delete.
	 * @throws CommonBusinessException
	 */
	public void deleteJob(String jobname) throws CommonBusinessException;

	/**
	 * Schedule the job with a cron expression.
	 *
	 * @param jobVO The job object containing jobname, cron expression and start time.
	 * @return Quartz trigger with calculated next fire time.
	 */
	public abstract Trigger scheduleJob(JobVO jobVO) throws CommonBusinessException;

	/**
	 * Unschedule a job (remove all triggers).
	 *
	 * @param jobVO
	 * @return true if unscheduling was successful, otherwise false
	 * @throws CommonBusinessException
	 */
	public abstract void unscheduleJob(JobVO jobVO) throws CommonBusinessException;

	/**
	 * Unschedule a job by name (remove all triggers).
	 *
	 * @param jobName
	 * @return true if unscheduling was successful, otherwise false
	 * @throws CommonBusinessException
	 */
	public abstract void unscheduleJob(String jobName) throws CommonBusinessException;

	/**
	 * @return the names of all scheduled jobs.
	 */
	public abstract String[] getJobNames();

	/**
	 * Check if job is scheduled
	 *
	 * @param jobName
	 * @return
	 */
	public boolean isScheduled(String jobName);

	/**
	 * Trigger immediate job execution
	 *
	 * @param jobVO
	 * @throws CommonBusinessException
	 */
	public void triggerJob(JobVO jobVO) throws CommonBusinessException;
}
