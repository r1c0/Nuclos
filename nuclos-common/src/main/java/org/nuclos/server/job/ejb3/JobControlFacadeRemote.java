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
package org.nuclos.server.job.ejb3;

import java.util.Collection;

import javax.ejb.Remote;

import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.job.valueobject.JobVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

@Remote
public interface JobControlFacadeRemote {

	/**
	 * Create a new (unscheduled) job.
	 *
	 * @param job The JobVO with the job's configuration.
	 * @return The created job as <code>MasterDataVO</code>
	 */
	MasterDataVO create(JobVO job) throws CommonBusinessException;

	/**
	 * Modify an existing job.
	 * If the job is already scheduled with quartz, it will be unscheduled (deleted) first and rescheduled afterwards.
	 *
	 * @param job The JobVO with the job's new configuration.
	 * @return The job's id.
	 */
	Object modify(JobVO job) throws CommonBusinessException;

	/**
	 * Remove an existing job.
	 * If the job is scheduled with quartz, it will be unscheduled (deleted).
	 *
	 * @param job The JobVO to remove.
	 */
	void remove(JobVO job) throws CommonBusinessException;

	/**
	 * @param jobVO
	 * @throws CommonBusinessException
	 */
	public abstract void scheduleJob(Object oId) throws CommonBusinessException;

	/**
	 * @param jobVO
	 * @throws CommonBusinessException
	 */
	public abstract void unscheduleJob(Object oId) throws CommonBusinessException;

	/**
	 * @param oId - id of job to execute
	 */
	public abstract void startJobImmediately(Object oId) throws CommonBusinessException;

	/**
	 * get job procedures/functions
	 * @param sType
	 * @return
	 */
	public abstract Collection<String> getDBObjects() throws CommonPermissionException;

}
