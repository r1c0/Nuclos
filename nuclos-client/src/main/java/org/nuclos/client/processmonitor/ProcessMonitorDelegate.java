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

import java.util.Collection;
import java.util.List;

import org.nuclos.common2.DateTime;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonRemoteException;
import org.nuclos.server.processmonitor.ejb3.ProcessMonitorFacadeRemote;
import org.nuclos.server.processmonitor.valueobject.ProcessMonitorGraphVO;
import org.nuclos.server.processmonitor.valueobject.ProcessMonitorVO;
import org.nuclos.server.processmonitor.valueobject.ProcessStateRuntimeFormatVO;
import org.nuclos.server.processmonitor.valueobject.SubProcessUsageCriteriaVO;
import org.nuclos.server.processmonitor.valueobject.SubProcessVO;
import org.nuclos.server.statemodel.valueobject.StateVO;

/**
 * Business Delegate for <code>ProcessMonitorFacadeBean</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:marc.finke@novabit.de">Marc Finke</a>
 * @version 01.00.00
 */
public class ProcessMonitorDelegate {

	private static ProcessMonitorDelegate INSTANCE;
	
	//

	private ProcessMonitorFacadeRemote processMonitorFacadeRemote; 

	ProcessMonitorDelegate() {
		INSTANCE = this;
	}

	public static ProcessMonitorDelegate getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}
	
	public final void setProcessMonitorFacadeRemote(ProcessMonitorFacadeRemote processMonitorFacadeRemote) {
		this.processMonitorFacadeRemote = processMonitorFacadeRemote;
	}
	
	public Collection<StateVO> getStateByModelId(Integer stateModelId) {		
		return processMonitorFacadeRemote.getStateByModelId(stateModelId);
	}
	
	/*
	 * retrieve all process models
	 */
	public Collection<ProcessMonitorVO> getProcessModels() {
		return processMonitorFacadeRemote.getProcessModels();
	}
	
	/*
	 * unused at the moment
	 */
	public ProcessMonitorVO create(ProcessMonitorGraphVO vo) {
		try {
			return processMonitorFacadeRemote.create(vo);		
		} catch (Exception ex) {
			throw new CommonFatalException(ex);
		} 
	}
	
	/*
	 * unused at the moment
	 */
	public ProcessMonitorVO modify(ProcessMonitorVO vo) {
		return processMonitorFacadeRemote.modify(vo);		

	}
	
	/*
	 * put processmodel into database
	 */
	public Integer setStateGraph(ProcessMonitorGraphVO stategraphvo) throws CommonBusinessException {
		try {
			return this.processMonitorFacadeRemote.setStateGraph(stategraphvo);
		}
		catch (Exception ex) {
			throw new CommonRemoteException(ex);
		}
	}
	
	/*
	 * retrieve processmodel from database
	 */
	public ProcessMonitorGraphVO getStateGraph(int iModelId) throws CommonFinderException {
		return this.processMonitorFacadeRemote.getStateGraph(iModelId);
			
		
	}
	
	/**
	 * 
	 * @return list of possible values:
	 * 
	 * minutes = 1
	 * hours = 2
	 * days = 3
	 * weeks = 4
	 * months = 5
	 */
	public List<ProcessStateRuntimeFormatVO> getPossibleRuntimeFormats() {
		return this.processMonitorFacadeRemote.getPossibleRuntimeFormats();

	}
	
	/**
	 * 
	 * @param stateModelId
	 * @return
	 */
	public List<SubProcessUsageCriteriaVO>  getSubProcessUsageCriterias(Integer stateModelId){
		return this.processMonitorFacadeRemote.getSubProcessUsageCriterias(stateModelId);

	}

	/**
	 * 
	 * @param iProcessMonitorId
	 * @return
	 * @throws CommonBusinessException
	 */
	public SubProcessVO findStartingSubProcess(java.lang.Integer iProcessMonitorId) throws CommonBusinessException{
		return this.processMonitorFacadeRemote.findStartingSubProcess(iProcessMonitorId);
		
	}
	
	/**
	 * 
	 * @param iProcessMonitorId
	 * @param datePlanStart
	 * @return
	 * @throws CommonBusinessException 
	 */
	public DateTime getProcessPlanEnd(Integer iProcessMonitorId, DateTime datePlanStart) throws CommonBusinessException{
		return this.processMonitorFacadeRemote.getProcessPlanEnd(iProcessMonitorId, datePlanStart);
		
	}
	
	/**
	 * 
	 * @param iSubProcessTransitionId
	 * @return id or null
	 */
	public Integer getGenerationIdFromSubProcessTransition(Integer iSubProcessTransitionId){
		return this.processMonitorFacadeRemote.getGenerationIdFromSubProcessTransition(iSubProcessTransitionId);
		
	}

}	// class ProcessmonitorDelegate
