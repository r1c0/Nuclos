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

// @Remote
public interface ProcessMonitorFacadeRemote {

	ProcessMonitorVO create(ProcessMonitorGraphVO graphvo) 
			throws CommonCreateException, CommonValidationException, NuclosBusinessRuleException, CommonPermissionException ;
	
	SubProcessVO findStartingSubProcess(Integer iProcessMonitorId) throws CommonBusinessException ;
	
	Integer getGenerationIdFromSubProcessTransition(Integer iSubProcessTransitionId);
	
	DateTime getPlanEnd(SubProcessVO subProcess, DateTime datePlanStart);
	
	DateTime getPlanStart(SubProcessVO subProcess, DateTime dateOrigin);
	
	List<ProcessStateRuntimeFormatVO> getPossibleRuntimeFormats() ;
	
	Collection<ProcessMonitorVO> getProcessModels() ;
	
	DateTime getProcessPlanEnd(Integer iProcessMonitorId, DateTime datePlanStart) throws CommonBusinessException;
	
	Collection<StateVO> getStateByModelId(Integer modelId) ;
	
	ProcessMonitorGraphVO getStateGraph(Integer iModelId) throws CommonFinderException ;
	
	List<SubProcessUsageCriteriaVO> getSubProcessUsageCriterias(Integer stateModelId);
	
	Boolean isFinalState(Integer targetStateId);
	
	ProcessMonitorVO modify(ProcessMonitorVO vo);
	
	Integer setStateGraph(ProcessMonitorGraphVO processgraphcvo) 
			throws CommonCreateException , CommonFinderException, CommonRemoveException, CommonValidationException, CommonPermissionException;
	
	Collection<ProcessTransitionVO> findProcessTransitionByTargetStateAndProcessmodel(Integer targetStateId, Integer processmodelId) 
			throws CommonPermissionException;
	
	Collection<MasterDataVO> findGeneratorsWhichArePointingToSameSubProcess(Integer generationId, Integer targetCaseId) 
			throws CommonPermissionException;
}
