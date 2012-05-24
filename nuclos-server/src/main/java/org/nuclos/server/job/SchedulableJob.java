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
package org.nuclos.server.job;

import java.util.Date;

import org.apache.log4j.Logger;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.job.JobType;
import org.nuclos.common2.KeyEnum;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.job.ejb3.JobControlFacadeLocal;
import org.nuclos.server.job.valueobject.JobVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.report.NuclosQuartzJob;
import org.nuclos.server.ruleengine.jobs.TimelimitJob;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Contains the necessary logic to control quartz job.
 * <br>
 * <b>Responsibilities:</b>
 * <ul>
 *   <li>Prepare a job execution - set job state to running, delete old log data, create a data record in protocol table (calls <code>JobControlFacadeBean</code> for separate transaction)</li>
 *   <li>Execute a job - this must be implemented in specific job classes</li>
 *   <li>Set job execution result (calls <code>JobControlFacadeBean</code> for separate transaction)</li>
 * </ul>
 * All Nuclos Quartz Job must extend this class.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version 01.00.00
 *
 */
public class SchedulableJob extends NuclosQuartzJob {

	private static Logger logger = Logger.getLogger(SchedulableJob.class);

	private static JobControlFacadeLocal jobfacade;

	public SchedulableJob() {
		super(new SchedulableJobImpl());
	}

	public static void process(Object oId, Date dFireTime, Date dNextFireTime) {
		Integer iSessionId = null;
		try {
			//prepare job execution: clean protocol table, set 'running' true, create jobrun data record
			Pair<JobVO, MasterDataVO> pair = getJobFacade().prepare(oId);
			iSessionId = pair.getY().getIntId();

			//result of job execution
			String sResult = null;

			JobType type = KeyEnum.Utils.findEnum(JobType.class, pair.getX().getType());

			//execute TimelimitJob or HealthCheckJob
			if (JobType.TIMELEIMIT.equals(type)) {
				sResult = new TimelimitJob().execute(pair.getX(), iSessionId);
			}
			else if (JobType.HEALTHCHECK.equals(type)) {
				sResult = new HealthCheckJob().execute(pair.getX(), iSessionId);
			}

			//set 'running' false, get execution result from protocol table
			getJobFacade().setJobExecutionResult(sResult, dFireTime, dNextFireTime, pair.getX(), pair.getY());

		}
		catch (Exception e) {
			try {
				getJobFacade().setJobExecutionResultError(oId, dFireTime, dNextFireTime, iSessionId, e);
			}
			catch (Exception ex) {
				// do not throw exception to quartz, just log it instead and finish the job execution normally
				// throw new NuclosFatalException(e);
				logger.warn("An error occurred while setting the job execution error result.", ex);
			}
		}
	}

	private static class SchedulableJobImpl implements Job {
		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			JobDetail jobDetail = context.getJobDetail();
			logger.debug("Start executing job " +jobDetail.getName());

			//get the id of data record from jobcontroller
			Object oId = jobDetail.getJobDataMap().get(jobDetail.getName());

			process(oId, context.getFireTime(), context.getNextFireTime());

			logger.info("END executing Job " + context.getJobDetail().getName());
		}
	}

	private static JobControlFacadeLocal getJobFacade() {
		if (jobfacade == null)
			jobfacade = ServerServiceLocator.getInstance().getFacade(JobControlFacadeLocal.class);
		return jobfacade;
	}
}
