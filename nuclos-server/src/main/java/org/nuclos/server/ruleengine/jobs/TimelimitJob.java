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

import java.util.Collection;

import org.apache.log4j.Logger;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.job.NuclosJob;
import org.nuclos.server.job.SchedulableJob;
import org.nuclos.server.job.valueobject.JobVO;
import org.nuclos.server.ruleengine.ejb3.TimelimitRuleFacadeLocal;

//import java.util.Iterator;

/**
 * Quartz job to execute all rules with event type "Frist".
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars Rueckemann</a>
 * @version 01.00.00
 */

public class TimelimitJob extends SchedulableJob implements NuclosJob {//NuclosQuartzJob {
	private static Logger logger = Logger.getLogger(TimelimitJob.class);

	@Override
	public String execute(JobVO jobVO, Integer iSessionId) {
		//execute job rules

		final TimelimitRuleFacadeLocal ruleFacade = ServerServiceLocator.getInstance().getFacade(TimelimitRuleFacadeLocal.class);

		Collection<String> ruleNames = ruleFacade.getJobRules(jobVO.getId());
		logger.info("Rules to execute: " + ruleNames);
		for (String sRuleName : ruleNames) {
			logger.info("Executing rule " + sRuleName + " for Job " + jobVO.getName());
			ruleFacade.executeRule(sRuleName, iSessionId);
		}

		return null;
	}

}	// class TimeLimitJob
