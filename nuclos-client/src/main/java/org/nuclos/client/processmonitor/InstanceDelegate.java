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
package org.nuclos.client.processmonitor;

import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.processmonitor.ejb3.InstanceFacadeRemote;

/**
 * Business Delegate for <code>InstanceFacadeBean</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@novabit.de">Maik Stueker</a>
 * @version 01.00.00
 */
public class InstanceDelegate {

	private static InstanceDelegate singleton;

	private InstanceFacadeRemote facade = null; 

	private InstanceDelegate() {
		facade = ServiceLocator.getInstance().getFacade(InstanceFacadeRemote.class);
		
	}

	public static synchronized InstanceDelegate getInstance() {
		if (singleton == null) {
			singleton = new InstanceDelegate();
		}
		return singleton;
	}
	
	/**
	 * 
	 * @param iProcessMonitorId
	 * @param iInstanceId
	 * @throws CommonBusinessException 
	 */
	public void createProcessInstance(Integer iProcessMonitorId, Integer iInstanceId) throws CommonBusinessException{
		facade.createProcessInstance(iProcessMonitorId, iInstanceId);
	}
	
	/**
	 * 
	 * @param iInstanceId
	 * @param iStateModelUsageId
	 * @return
	 */
	public int getInstanceStatus(Integer iInstanceId, Integer iStateModelUsageId){
		return facade.getInstanceStatus(iInstanceId, iStateModelUsageId);
	}
	
	/**
	 * 
	 * @param iInstanceId
	 * @param iStateModelUsageId
	 * @return object id (could be null)
	 */
	public Integer getObjectId(Integer iInstanceId, Integer iStateModelUsageId){
		return facade.getObjectId(iInstanceId, iStateModelUsageId);
	}
	
	/**
	 * 
	 * @param iInstanceId
	 * @return
	 */
	public Boolean isProcessInstanceStarted(Integer iInstanceId){
		return facade.isProcessInstanceStarted(iInstanceId);
	}
	
	
}	// class InstanceDelegate
