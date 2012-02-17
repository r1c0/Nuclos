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
package org.nuclos.server.ruleengine.jobs;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.nuclos.common2.ServiceLocator;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.report.NuclosQuartzJob;
import org.nuclos.server.ruleengine.ejb3.RuleInterfaceFacadeLocal;

/**
 * Quartz job that can be scheduled to change the state of any generic object.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars Rueckemann</a>
 * @version 01.00.00
 */

public class StateChangeJob extends NuclosQuartzJob {
	private final static Logger logger = Logger.getLogger(StateChangeJob.class);

	public static final String PARAM_LEASEDOBJECTID = "iGenericObjectId";
	public static final String PARAM_NEWSTATE = "iNewState";

	public StateChangeJob() {
		super(new StateChangeJobImpl());
	}

	/**
	 * inner class StateChangeJobImpl: implementation of StateChangeJob
	 */
	private static class StateChangeJobImpl implements Job {
		/**
		 * tries to change the state of a leased object.
		 * It's necessary to specify the following arguments in the jobDataMap of the JobExecutionContext:
		 * <ul>
		 * 	<li><code>iGenericObjectId</code>: intid of the leased object
		 * 	<li><code>iNewState</code>: the target state of the leased object
		 * </ul>
		 * @param context
		 * @throws JobExecutionException
		 */
		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			final JobDataMap dataMap = context.getJobDetail().getJobDataMap();
			final Integer iGenericObjectId = (Integer) dataMap.get(PARAM_LEASEDOBJECTID);
			final int iNewState = ((Integer) dataMap.get(PARAM_NEWSTATE)).intValue();

			logger.debug("Executing StateChangeJob(iGenericObjectId=" + iGenericObjectId + ", iNewState=" + iNewState + ")");

			try {
				ServerServiceLocator.getInstance().getFacade(RuleInterfaceFacadeLocal.class).changeState(null, iGenericObjectId, iNewState);
			}
			catch (org.nuclos.server.ruleengine.NuclosBusinessRuleException ex) {
				throw new JobExecutionException(ex);
			}
			logger.debug("Successfully executed StateChangeJob.");
		}

	}	// inner class StateChangeJobImpl

}	// class StateChangeJob
