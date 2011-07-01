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

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.CreateException;

import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.ServiceLocator;
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

	private static JobControlDelegate singleton;

	JobControlFacadeRemote facade;

	public JobControlDelegate() throws RemoteException, CreateException {
		this.facade = ServiceLocator.getInstance().getFacade(JobControlFacadeRemote.class);
	}

	public static synchronized JobControlDelegate getInstance() {
		if (singleton == null) {
			try {
				singleton = new JobControlDelegate();
			}
			catch (RemoteException ex) {
				throw new CommonRemoteException(ex);
			}
			catch (CreateException ex) {
				throw new NuclosFatalException(ex);
			}
		}
		return singleton;
	}

	public JobControlFacadeRemote getFacade() {
		return this.facade;
	}

	public MasterDataVO create(JobVO job) throws CommonBusinessException {
	    return facade.create(job);
    }

	public Object modify(JobVO job) throws CommonBusinessException {
	    return facade.modify(job);
    }

	public void remove(JobVO job) throws CommonBusinessException {
		facade.remove(job);
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
