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
package org.nuclos.server.processmonitor.ejb3;

import javax.ejb.Local;

import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonPermissionException;

@Local
public interface InstanceFacadeLocal {

	public void createProcessInstance(Integer iProcessMonitorId, Integer iInstanceId) throws CommonBusinessException;
	
	public void createRunOfObjectGeneration(Integer iInstanceId, Integer iGenerationId, Boolean bResult) throws CommonCreateException, CommonPermissionException;
	
	public int getInstanceStatus(Integer iInstanceId, Integer iStateModelUsageId);
	
	public Integer getObjectId(Integer iInstanceId, Integer iStateModelUsageId);
	
	public Boolean isObjectGenerated(Integer iInstanceId, Integer iGenerationId);
	
	public Boolean isProcessInstanceStarted(Integer iInstanceId);
	
	public void notifyInstanceAboutStateChange(Integer genericObjectId, Integer targetStateId);
}
