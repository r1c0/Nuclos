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

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.common.NuclosScheduler;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.job.SchedulableJob;
import org.nuclos.server.job.valueobject.JobVO;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.transaction.annotation.Transactional;

/**
* <tt>SchedulerControlFacadeBean</tt> provides functionality to control quartz job execution services.
* All asynchronous tasks (i.e. tasks that do not return a result to the user) should be executed as a job.
*
* Currently known types are:
* <ul>
* 	<li>Jobs</li>
* 	<li>Imports</li>
* </ul>
*
* <br>
* <br>Created by Novabit Informationssysteme GmbH
* <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
*/
@Transactional
@RolesAllowed("UseManagementConsole")
public class SchedulerControlFacadeBean extends NuclosFacadeBean implements SchedulerControlFacadeLocal, SchedulerControlFacadeRemote {

	public void addJob(JobVO job) throws CommonBusinessException {
		final Scheduler scheduler = NuclosScheduler.getInstance().getScheduler();
		if (exists(job.getName())) {
			try {
				JobDetail jd = scheduler.getJobDetail(job.getName(), Scheduler.DEFAULT_GROUP);
				if (!job.getId().equals(jd.getJobDataMap().getIntValue(job.getName()))) {
					throw new NuclosBusinessException("scheduler.error.alreadyscheduled");
				}
			} catch (SchedulerException e) {
				throw new NuclosFatalException("");
			}
		}
		final JobDetail jobDetail = new JobDetail(job.getName(), Scheduler.DEFAULT_GROUP, SchedulableJob.class);
		jobDetail.getJobDataMap().put(job.getName(), job.getId());
		jobDetail.setDurability(true);

		try {
			scheduler.addJob(jobDetail, true);
		} catch (SchedulerException e) {
			throw new NuclosFatalException("");
		}
		info(getSchedulerSummary());
	}

	public void deleteJob(String jobname) throws CommonBusinessException {
		if (exists(jobname)) {
			final Scheduler scheduler = NuclosScheduler.getInstance().getScheduler();
			try {
				if (scheduler.deleteJob(jobname, Scheduler.DEFAULT_GROUP)) {
					debug("Deleted job: " + jobname);
				}
				else {
					warn("Failed to delete job: " + jobname);
					throw new NuclosBusinessException("scheduler.error.delete");
				}
			}
			catch (SchedulerException ex) {
				error(ex);
				throw new NuclosFatalException("scheduler.error.delete");
			}
			info(getSchedulerSummary());
		}
	}

	/**
	 * schedules the Job at the given time
	 * @param jobVO
	 * @return
	 */
	@Override
	public Trigger scheduleJob(JobVO jobVO) throws CommonBusinessException {
		final Scheduler scheduler = NuclosScheduler.getInstance().getScheduler();

		if (isScheduled(jobVO.getName())) {
			// remove existing triggers
			try {
				for (Trigger t : scheduler.getTriggersOfJob(jobVO.getName(), Scheduler.DEFAULT_GROUP)) {
					scheduler.unscheduleJob(t.getName(), t.getGroup());
				}
			}
			catch (SchedulerException ex) {
				throw new CommonBusinessException("scheduler.error.reschedule");
			}
		}

		if (!exists(jobVO.getName())) {
			addJob(jobVO);
		}

		int iHour = Integer.parseInt(jobVO.getStarttime().substring(0, 2));
		int iMinute = Integer.parseInt(jobVO.getStarttime().substring(3));
		Calendar startDate = new GregorianCalendar();
		startDate.setTime(jobVO.getStartdate());
		startDate.set(Calendar.HOUR_OF_DAY, iHour);
		startDate.set(Calendar.MINUTE, iMinute);

		Trigger jobTrigger;
        try {
	        jobTrigger = new CronTrigger(jobVO.getName(), Scheduler.DEFAULT_GROUP, jobVO.getName(), Scheduler.DEFAULT_GROUP, jobVO.getCronExpression());
        }
        catch(ParseException e) {
        	// fatal exception because cron expression is validated before
        	throw new NuclosFatalException("scheduler.error.cronexpression");
        }

		jobTrigger.setName(jobVO.getName());
		jobTrigger.setStartTime(startDate.getTime());
		jobTrigger.setEndTime(null);
		jobTrigger.setJobName(jobVO.getName());
		jobTrigger.setJobGroup(Scheduler.DEFAULT_GROUP);

		try {
			scheduler.scheduleJob(jobTrigger);
		}
		catch (SchedulerException ex) {
			throw new NuclosFatalException("scheduler.error.scheduling");
		}
		debug("Successfully scheduled Job. Job will start at " + jobTrigger.getNextFireTime().toString());
		info(getSchedulerSummary());
		return jobTrigger;
	}

	@Override
	public void unscheduleJob(JobVO jobVO) throws CommonBusinessException {
		unscheduleJob(jobVO.getName());
	}

	@Override
	public void unscheduleJob(String jobName) throws CommonBusinessException {
		if (!isScheduled(jobName)) {
			throw new CommonBusinessException("scheduler.error.notscheduled");
		}


		final Scheduler scheduler = NuclosScheduler.getInstance().getScheduler();
		try {
			Trigger[] triggers = scheduler.getTriggersOfJob(jobName, Scheduler.DEFAULT_GROUP);
			if (triggers != null && triggers.length > 0) {
				for (Trigger t : triggers) {
					if (!scheduler.unscheduleJob(t.getName(), t.getGroup())) {
						throw new NuclosBusinessException("scheduler.error.unscheduling");
					}
				}
			}
		}
		catch (SchedulerException ex) {
			throw new NuclosFatalException("scheduler.error.unscheduling");
		}
		info(getSchedulerSummary());
	}

	/**
	 * trigger immediate job execution
	 * @param jobVO
	 * @return
	 */
	@Override
	public void triggerJob(JobVO jobVO) throws CommonBusinessException {
		final Scheduler scheduler = NuclosScheduler.getInstance().getScheduler();
		if (!exists(jobVO.getName())) {
			addJob(jobVO);
		}

		boolean running = false;
		List<?> executingJobs;
		try {
			executingJobs = scheduler.getCurrentlyExecutingJobs();
			for (Object o : executingJobs) {
				if (o instanceof JobExecutionContext) {
					JobExecutionContext job = (JobExecutionContext) o;
					if (Scheduler.DEFAULT_GROUP.equals(job.getJobDetail().getGroup()) && job.getJobDetail().getName().equals(jobVO.getName())) {
						running = true;
					}
				}
			}
		}
		catch (SchedulerException e) {
			warn(e);
		}

		if (!running) {
			try {
				scheduler.triggerJob(jobVO.getName(), Scheduler.DEFAULT_GROUP);
			} catch (SchedulerException e) {
				throw new CommonBusinessException("scheduler.error.trigger.immediate");
			}
		}
		else {
			throw new CommonBusinessException("scheduler.error.running");
		}
	}

	@Override
	public boolean isScheduled(String jobName) {
		for (String job : getJobNames()) {
			if (job.equals(jobName)) {
				final Scheduler scheduler = NuclosScheduler.getInstance().getScheduler();
				try {
					Trigger[] triggers = scheduler.getTriggersOfJob(jobName, Scheduler.DEFAULT_GROUP);
					if (triggers != null && triggers.length > 0) {
						return true;
					}
				}
				catch (SchedulerException e) {
					throw new NuclosFatalException("");
				}
			}
		}
		return false;
	}

	/**
	 * @return the names of all scheduled jobs.
	 */
	@Override
	public String[] getJobNames() {
		final Scheduler scheduler = NuclosScheduler.getInstance().getScheduler();
		try {
			return scheduler.getJobNames(Scheduler.DEFAULT_GROUP);
		}
		catch (SchedulerException ex) {
			error(ex);
			return new String[0];
		}
	}

	private boolean exists(String jobName) {
		for (String job : getJobNames()) {
			if (job.equals(jobName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getSchedulerSummary() {
		try {
			final Scheduler scheduler = NuclosScheduler.getInstance().getScheduler();
			StringBuilder sb = new StringBuilder();
			sb.append("Nuclos Scheduler Summary:");
			for (String job : scheduler.getJobNames(Scheduler.DEFAULT_GROUP)) {
				sb.append("\n  " + job + "(" + scheduler.getJobDetail(job, Scheduler.DEFAULT_GROUP) + "):");
				Trigger[] triggers = scheduler.getTriggersOfJob(job, Scheduler.DEFAULT_GROUP);
				if (triggers == null || triggers.length == 0) {
					sb.append(" not scheduled;");
				}
				else {
					for (Trigger t : triggers) {
						sb.append("\n    " + t.toString());
					}
				}
			}
			return sb.toString();
		}
		catch (SchedulerException ex) {
			return ex.toString();
		}
	}


}
