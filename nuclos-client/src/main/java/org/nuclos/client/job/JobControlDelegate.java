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
package org.nuclos.client.job;

import java.util.Collection;

import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonRemoteException;
import org.nuclos.server.job.ejb3.JobControlFacadeRemote;
import org.nuclos.server.job.valueobject.JobVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Business Delegate for <code>JobControlFacadeBean</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version 01.00.00
 */
public class JobControlDelegate {

	private static JobControlDelegate INSTANCE;
	
	// Spring injection

	private JobControlFacadeRemote jobControlFacadeRemote;
	
	// end of Spring injection

	JobControlDelegate() {
		INSTANCE = this;
	}

	public static JobControlDelegate getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}
	
	public final void setJobControlFacadeRemote(JobControlFacadeRemote jobControlFacadeRemote) {
		this.jobControlFacadeRemote = jobControlFacadeRemote;
	}

	private JobControlFacadeRemote getFacade() {
		return this.jobControlFacadeRemote;
	}

	public MasterDataVO create(JobVO job) throws CommonBusinessException {
	    return jobControlFacadeRemote.create(job);
    }

	public Object modify(JobVO job) throws CommonBusinessException {
	    return jobControlFacadeRemote.modify(job);
    }

	public void remove(JobVO job) throws CommonBusinessException {
		jobControlFacadeRemote.remove(job);
	}

	public void scheduleJob(Object oId) throws CommonBusinessException {
		try {
			this.getFacade().scheduleJob(oId);
		}
		catch (Exception ex) {
			throw new CommonBusinessException(ex);
		}
	}

	public void unscheduleJob(Object oId) throws CommonBusinessException {
		try {
			this.getFacade().unscheduleJob(oId);
		}
		catch (Exception ex) {
			throw new CommonBusinessException(ex);
		}
	}

	public void startJobImmediately(Object oId) throws CommonBusinessException {
		try {
			this.getFacade().startJobImmediately(oId);
		}
		catch (Exception ex) {
			throw new CommonBusinessException(ex);
		}
	}

	/**
	 * get job procedures/functions
	 * @param sType
	 */
	public Collection<String> getDBObjects() {
		try {
			return this.getFacade().getDBObjects();
		}
		catch (Exception ex) {
			throw new CommonFatalException(ex);
		}
	}
}
