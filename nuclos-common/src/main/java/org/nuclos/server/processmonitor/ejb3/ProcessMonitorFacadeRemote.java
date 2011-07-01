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

import java.util.Collection;
import java.util.List;

import javax.ejb.Remote;

import org.nuclos.common2.DateTime;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.processmonitor.valueobject.ProcessMonitorGraphVO;
import org.nuclos.server.processmonitor.valueobject.ProcessMonitorVO;
import org.nuclos.server.processmonitor.valueobject.ProcessStateRuntimeFormatVO;
import org.nuclos.server.processmonitor.valueobject.ProcessTransitionVO;
import org.nuclos.server.processmonitor.valueobject.SubProcessUsageCriteriaVO;
import org.nuclos.server.processmonitor.valueobject.SubProcessVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.statemodel.valueobject.StateVO;

@Remote
public interface ProcessMonitorFacadeRemote {

	public ProcessMonitorVO create(ProcessMonitorGraphVO graphvo) throws CommonCreateException, CommonValidationException, NuclosBusinessRuleException, CommonPermissionException ;
	
	public SubProcessVO findStartingSubProcess(Integer iProcessMonitorId) throws CommonBusinessException ;
	
	public Integer getGenerationIdFromSubProcessTransition(Integer iSubProcessTransitionId);
	
	public DateTime getPlanEnd(SubProcessVO subProcess, DateTime datePlanStart);
	
	public DateTime getPlanStart(SubProcessVO subProcess, DateTime dateOrigin);
	
	public List<ProcessStateRuntimeFormatVO> getPossibleRuntimeFormats() ;
	
	public Collection<ProcessMonitorVO> getProcessModels() ;
	
	public DateTime getProcessPlanEnd(Integer iProcessMonitorId, DateTime datePlanStart) throws CommonBusinessException;
	
	public Collection<StateVO> getStateByModelId(Integer modelId) ;
	
	public ProcessMonitorGraphVO getStateGraph(Integer iModelId) throws CommonFinderException ;
	
	public List<SubProcessUsageCriteriaVO> getSubProcessUsageCriterias(Integer stateModelId);
	
	public Boolean isFinalState(Integer targetStateId);
	
	public ProcessMonitorVO modify(ProcessMonitorVO vo);
	
	public Integer setStateGraph(ProcessMonitorGraphVO processgraphcvo) throws CommonCreateException , CommonFinderException, CommonRemoveException, CommonValidationException /*, CommonStaleVersionException */, CommonPermissionException;
	
	public Collection<ProcessTransitionVO> findProcessTransitionByTargetStateAndProcessmodel(Integer targetStateId, Integer processmodelId) throws CommonPermissionException;
	
	public Collection<MasterDataVO> findGeneratorsWhichArePointingToSameSubProcess(Integer generationId, Integer targetCaseId) throws CommonPermissionException;
}
