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
import java.util.Date;
import java.util.GregorianCalendar;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.DateUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.common.NuclosScheduler;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.job.SchedulableJob;
import org.nuclos.server.job.valueobject.JobVO;
import org.nuclos.server.report.NuclosReportJob;
import org.nuclos.server.ruleengine.jobs.TimelimitJob;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.springframework.transaction.annotation.Transactional;

/**
* SchedulerControlFacadeBean.
* <br>
* <br>Created by Novabit Informationssysteme GmbH
* <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
*/
@Stateless
@Local(SchedulerControlFacadeLocal.class)
@Remote(SchedulerControlFacadeRemote.class)
@Transactional
@RolesAllowed("UseManagementConsole")
public class SchedulerControlFacadeBean extends NuclosFacadeBean implements SchedulerControlFacadeLocal, SchedulerControlFacadeRemote {

	@Override
	public Date scheduleReportJob(String reportName, int iHour, int iMinute) {
		final Scheduler scheduler = NuclosScheduler.getInstance().getScheduler();
		final String sMinute = iMinute < 10 ? "0" + iMinute : String.valueOf(iMinute);

		final JobDetail jobDetail = new JobDetail(reportName + " " + iHour + ":" + sMinute,
				Scheduler.DEFAULT_GROUP, NuclosReportJob.class);

		if (reportName != null && reportName.compareTo("") != 0) {
			jobDetail.getJobDataMap().put("ReportName", reportName);
		}
		else {
			jobDetail.getJobDataMap().put("ReportName", "Report");
		}

		final Trigger jobTrigger = TriggerUtils.makeDailyTrigger(iHour, iMinute);
		TriggerUtils.setTriggerIdentity(jobTrigger, "trigger_" + reportName + " " + iHour + ":" + sMinute, Scheduler.DEFAULT_GROUP);
		jobTrigger.setStartTime(DateUtils.now());
		jobTrigger.setEndTime(null);

		try {
			scheduler.scheduleJob(jobDetail, jobTrigger);
		}
		catch (SchedulerException e) {
			throw new NuclosFatalException("scheduler.error.scheduling");
		}
		debug("Successfully scheduled job for report " + reportName + ". Job will start at " + jobTrigger.getNextFireTime().toString());
		return jobTrigger.getNextFireTime();
	}

	@Override
	public boolean unscheduleJob(String jobName) throws CommonBusinessException {
		if (!isScheduled(jobName)) {
			throw new CommonBusinessException("scheduler.error.notscheduled");
		}

		boolean result = false;

		final Scheduler scheduler = NuclosScheduler.getInstance().getScheduler();
		try {
			result = scheduler.deleteJob(jobName, Scheduler.DEFAULT_GROUP);
			if (result) {
				debug("Disabled scheduling of job: " + jobName);
			}
			else {
				warn("Failed to delete job: " + jobName);
			}
		}
		catch (SchedulerException ex) {
			throw new NuclosFatalException("scheduler.error.unscheduling");
		}
		return result;
	}

	/**
	 * schedules the TimelimitJob daily at the given time
	 * @param iHour
	 * @param iMinute
	 * @return
	 */
	@Override
	public Date scheduleTimelimitJob(int iHour, int iMinute) {
		final Scheduler scheduler = NuclosScheduler.getInstance().getScheduler();
		final String sMinute = iMinute < 10 ? "0" + iMinute : String.valueOf(iMinute);

		final JobDetail jobDetail = new JobDetail("TimelimitJob " + iHour + ":" + sMinute,
				Scheduler.DEFAULT_GROUP, TimelimitJob.class);

		final Trigger jobTrigger = TriggerUtils.makeDailyTrigger(iHour, iMinute);
		TriggerUtils.setTriggerIdentity(jobTrigger, "trigger_TimelimitJob " + iHour + ":" + sMinute, Scheduler.DEFAULT_GROUP);
		jobTrigger.setStartTime(DateUtils.now());
		jobTrigger.setEndTime(null);

		try {
			scheduler.scheduleJob(jobDetail, jobTrigger);
		}
		catch (SchedulerException ex) {
			throw new NuclosFatalException("scheduler.error.scheduling");
		}
		debug("Successfully scheduled TimelimitJob. Job will start at " + jobTrigger.getNextFireTime().toString());
		return jobTrigger.getNextFireTime();
	}

	/**
	 * schedules the Job at the given time
	 * @param jobVO
	 * @return
	 */
	@Override
	public Trigger scheduleJob(JobVO jobVO) throws CommonBusinessException {
		if (isScheduled(jobVO.getName())) {
			throw new CommonBusinessException("scheduler.error.alreadyscheduled");
		}
		final Scheduler scheduler = NuclosScheduler.getInstance().getScheduler();

		final JobDetail jobDetail = new JobDetail(jobVO.getName(), Scheduler.DEFAULT_GROUP, SchedulableJob.class);

		jobDetail.getJobDataMap().put(jobVO.getName(), jobVO.getId());
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

		try {
			scheduler.scheduleJob(jobDetail, jobTrigger);
		}
		catch (SchedulerException ex) {
			throw new NuclosFatalException("scheduler.error.scheduling");
		}
		debug("Successfully scheduled Job. Job will start at " + jobTrigger.getNextFireTime().toString());
		return jobTrigger;
	}

	@Override
	public boolean unscheduleJob(JobVO jobVO) throws CommonBusinessException {
		return unscheduleJob(jobVO.getName());
	}

	private boolean isScheduled(String jobName) {
		for (String job : getJobNames()) {
			if (job.equals(jobName)) {
				return true;
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
}
