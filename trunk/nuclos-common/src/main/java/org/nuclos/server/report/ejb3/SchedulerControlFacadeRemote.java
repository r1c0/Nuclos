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

import org.nuclos.common2.exception.CommonBusinessException;

// @Remote
public interface SchedulerControlFacadeRemote {

	void deleteJob(String jobName) throws CommonBusinessException;

	void unscheduleJob(String jobName) throws CommonBusinessException;

	String[] getJobNames();

	/**
	 * Check if job is scheduled
	 *
	 * @param jobName
	 * @return
	 */
	boolean isScheduled(String jobName);

	String getSchedulerSummary();
}
