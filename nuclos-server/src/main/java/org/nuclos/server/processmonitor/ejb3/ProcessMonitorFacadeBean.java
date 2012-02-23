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

import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common.collect.collectable.CollectableSorting;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common.dblayer.JoinType;
import org.nuclos.common2.DateTime;
import org.nuclos.common2.SeriesUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.common.StateCache;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.genericobject.ejb3.GeneratorFacadeLocal;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.masterdata.MasterDataWrapper;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.processmonitor.valueobject.ProcessMonitorGraphVO;
import org.nuclos.server.processmonitor.valueobject.ProcessMonitorVO;
import org.nuclos.server.processmonitor.valueobject.ProcessStateRuntimeFormatVO;
import org.nuclos.server.processmonitor.valueobject.ProcessTransitionVO;
import org.nuclos.server.processmonitor.valueobject.SubProcessUsageCriteriaVO;
import org.nuclos.server.processmonitor.valueobject.SubProcessVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.statemodel.valueobject.StateModelLayout;
import org.nuclos.server.statemodel.valueobject.StateModelUsages;
import org.nuclos.server.statemodel.valueobject.StateModelUsagesCache;
import org.nuclos.server.statemodel.valueobject.StateModelVO;
import org.nuclos.server.statemodel.valueobject.StateTransitionVO;
import org.nuclos.server.statemodel.valueobject.StateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;



/**
 * Facade bean for processmonitor management and processmonitor issues.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:marc.finke@novabit.de">Marc Finke</a>
 * @version 00.01.000
 *
 * @todo restrict
 */
@Transactional
public class ProcessMonitorFacadeBean extends NuclosFacadeBean implements ProcessMonitorFacadeRemote {

	private MasterDataFacadeLocal masterDataFacade;
	
	public ProcessMonitorFacadeBean() {
	}
	
	@Autowired
	final void setMasterDataFacade(MasterDataFacadeLocal masterDataFacade) {
		this.masterDataFacade = masterDataFacade;
	}
	
	private final MasterDataFacadeLocal getMasterDataFacade() {
		return masterDataFacade;
	}

	/**
	 * unused the setStateGraph method makes the job
	 * create a new ProcessModel in the database
	 * @return same task as value object
	 * @throws CommonPermissionException 
	 * @throws NuclosBusinessRuleException 
	 */
	public ProcessMonitorVO create(ProcessMonitorGraphVO graphvo) throws CommonCreateException, CommonValidationException, NuclosBusinessRuleException, CommonPermissionException {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		
		MasterDataVO mdVO = new MasterDataVO(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.PROCESSMONITOR), true);
		mdVO.setFields(mpFields);
		getMasterDataFacade().create(NuclosEntity.PROCESSMONITOR.getEntityName(), mdVO, null);
		
		return graphvo.getStateModel();
	}

	
	/**
	 * unused at the moment setStateGraph makes the job
	 * method to modify a ProcessModel 
	 */
	public ProcessMonitorVO modify(ProcessMonitorVO vo) {		
		return vo;		
	}
	
	/**
	 * method to get all process models
	 * @return collection of process model vo
	 */
	public Collection<StateVO> getStateByModelId(Integer modelId) {
		return StateCache.getInstance().getStatesByModel(modelId);
	}
	
	/**
	 * method to get all process models
	 * @return collection of process model vo
	 */
	public Collection<ProcessMonitorVO> getProcessModels() {
		final Collection<ProcessMonitorVO> result = new HashSet<ProcessMonitorVO>();
		
		for (Object oId : getMasterDataFacade().getMasterDataIds(NuclosEntity.PROCESSMONITOR.getEntityName())) {
			try {
				result.add(MasterDataWrapper.getProcessMonitorVO(getMasterDataFacade().get(NuclosEntity.PROCESSMONITOR.getEntityName(), oId)));
			}
			catch(CommonFinderException e) {
				throw new CommonFatalException(e);
			}
			catch(CommonPermissionException e) {
				throw new CommonFatalException(e);
			}
		}
		return result;
	}
	
	/**
	 * method to insert, update or remove a complete process model in the database at once
	 * @param processgraphcvo state graph representation
	 * @return state model id
	 * @throws CommonPermissionException 
	 */
	public Integer setStateGraph(ProcessMonitorGraphVO processgraphcvo) 
			throws CommonCreateException , CommonFinderException, CommonRemoveException, CommonValidationException, CommonPermissionException {
		final Integer result;
		// check state graph for validity:
		final ProcessMonitorVO statemodelvo = processgraphcvo.getStateModel();
		if (!statemodelvo.isRemoved()) {
			//stategraphcvo.validate();	// throws CommonValidationException
		}

		// set state model:
		if (statemodelvo.getId() == null) {
			// TODO processmodel security
			//this.checkWriteAllowed(NuclosEntity.STATEMODEL);
			result = this.createStateGraph(processgraphcvo);				
		}
		else {
			result = statemodelvo.getId();

			if (statemodelvo.isRemoved()) {
				// remove process model graph:
				// TODO processmodel security
				//this.checkDeleteAllowed(NuclosEntity.STATEMODEL);
				this.removeStateGraph(processgraphcvo);
			}
			else {
				// update process model graph:
				// TODO processmodel security
				//this.checkWriteAllowed(NuclosEntity.STATEMODEL);
				this.updateStateGraph(processgraphcvo);
			}
		}
		SecurityCache.getInstance().invalidate();
		return result;
	}
	
	/**
	 * gets a complete process graph for a process model
	 * @param iModelId id of state model to get graph for
	 * @return state graph cvo containing the state graph information for the model with the given id
	 * @throws CommonPermissionException 
	 */
	public ProcessMonitorGraphVO getStateGraph(Integer iModelId) throws CommonFinderException {
		
		ProcessMonitorGraphVO result = null;
		
		try {
			result = new ProcessMonitorGraphVO(MasterDataWrapper.getProcessMonitorVO(getMasterDataFacade().get(NuclosEntity.PROCESSMONITOR.getEntityName(), iModelId)));
			
			
			Collection<SubProcessVO> colSubProcess = findProcessModelByCase(result.getStateModel().getId());
			Set<SubProcessVO> setSubProcess = new HashSet<SubProcessVO>();
			for (SubProcessVO vo : colSubProcess) {
				setSubProcess.add(vo);
			}
			
			Collection<ProcessTransitionVO> colProcessTransition = findProcessTransitionByCase(result.getStateModel().getId());			
			Set<ProcessTransitionVO> setTransition = new HashSet<ProcessTransitionVO>();
			for (ProcessTransitionVO vo : colProcessTransition) {
				setTransition.add(vo);
			}
			result.setStates(setSubProcess);
			result.setTransitions(setTransition);
		}
		catch(Exception ex) {
			throw new CommonFatalException(ex);
		}
		return result;
	}
	
	/*
	 * insert a complete process model into database
	 */
	private Integer createStateGraph(ProcessMonitorGraphVO stategraphvo) {
		try{
		ProcessMonitorVO processmonitorvo = stategraphvo.getStateModel();
		// process itself
//		final ProcessMonitorModelLocal pmlocal = monitorHome.create(processmonitorvo);
		final MasterDataVO createdVO = getMasterDataFacade().create(NuclosEntity.PROCESSMONITOR.getEntityName(), MasterDataWrapper.wrapProcessMonitorVO(processmonitorvo), null);
		processmonitorvo = MasterDataWrapper.getProcessMonitorVO(createdVO);
		final Integer processMonitorId = createdVO.getIntId();
		
		final Map<Integer, Integer> mpStates = new HashMap<Integer, Integer>();
		final Map<Integer, Integer> mpTransitions = new HashMap<Integer, Integer>();
		final StateModelLayout layoutinfo = processmonitorvo.getLayout();	
		
		// the subprocesses
		for (SubProcessVO statevo : stategraphvo.getStates()) {			
//			mpStates.put(statevo.getWorkingId(), processstatemodelHome.create(pmlocal.getId(), statevo.getStateModelVO().getId(), statevo.getGuarantor(), statevo.getSecondGuarator(), statevo.getSupervisor(), statevo.getOriginalSystem(), statevo.getPlanStartSeries(), statevo.getPlanEndSeries(), statevo.getRuntime(), statevo.getRuntimeFormat()).getId());
//			layoutinfo.updateStateId(statevo.getWorkingId(), mpStates.get(statevo.getWorkingId()));
			statevo.setProcessMonitorId(processMonitorId);
			MasterDataVO createdMDStateVO = getMasterDataFacade().create(NuclosEntity.PROCESSSTATEMODEL.getEntityName(), MasterDataWrapper.wrapSubProcessVO(statevo), null);
			
			mpStates.put(statevo.getWorkingId(), (Integer) createdMDStateVO.getId());		//prepare mapping table for state transition inserts/updates
			layoutinfo.updateStateId(statevo.getWorkingId(), mpStates.get(statevo.getWorkingId()));
		}
		
		// the transitions
		for (ProcessTransitionVO processtransitionvo : stategraphvo.getTransitions()) {
//			mpTransitions.put(processtransitionvo.getClientId(), transitionHome.create(pmlocal.getId(), mpStates.get(processtransitionvo.getStateSource()), (Integer) mpStates.get(processtransitionvo.getStateTarget()), processtransitionvo.getDescription(), processtransitionvo.isAutomatic(), processtransitionvo.getState(), processtransitionvo.getRuleIds(), processtransitionvo.getRoleIds()).getId());			
//			layoutinfo.updateTransitionId(processtransitionvo.getClientId(), mpTransitions.get(processtransitionvo.getClientId()));
			
			processtransitionvo.setProcessMonitorId(processMonitorId);
			MasterDataVO createdMDTransitionVO = getMasterDataFacade().create(NuclosEntity.PROCESSTRANSITION.getEntityName(), MasterDataWrapper.wrapProcessTransitionVO(processtransitionvo), null);
			
			mpTransitions.put(processtransitionvo.getClientId(), (Integer) createdMDTransitionVO.getId());		//prepare mapping table for state transition inserts/updates
			layoutinfo.updateTransitionId(processtransitionvo.getId(), mpTransitions.get(processtransitionvo.getId()));
		}
		
		processmonitorvo.setLayout(layoutinfo);
		getMasterDataFacade().modify(NuclosEntity.PROCESSMONITOR.getEntityName(), MasterDataWrapper.wrapProcessMonitorVO(processmonitorvo), null);
		
		
		return processMonitorId;
		}
		catch (Exception e) {
			throw new CommonFatalException(e);
		}
	}
	
	/*
	 * TODO
	 */
	private void removeStateGraph(ProcessMonitorGraphVO stategraphvo) {
		try {
		for (ProcessTransitionVO processtransitionvo : stategraphvo.getTransitions()) {
			if (processtransitionvo.getId() != null) {
				getMasterDataFacade().remove(NuclosEntity.PROCESSTRANSITION.getEntityName(), getMasterDataFacade().get(NuclosEntity.PROCESSTRANSITION.getEntityName(), processtransitionvo.getId()), false);
			}
		}
		for (SubProcessVO statevo : stategraphvo.getStates()) {
			if (statevo.getId() != null) {
				getMasterDataFacade().remove(NuclosEntity.PROCESSSTATEMODEL.getEntityName(), getMasterDataFacade().get(NuclosEntity.PROCESSSTATEMODEL.getEntityName(), statevo.getId()), false);
			}
		}
		//StateCache.getInstance().invalidate();
		getMasterDataFacade().remove(NuclosEntity.PROCESSMONITOR.getEntityName(), MasterDataWrapper.wrapProcessMonitorVO(stategraphvo.getStateModel()), false);
		}
		catch(Exception e) {
			throw new CommonFatalException(e);
		}
	}

	// update a complete process model
	private void updateStateGraph(ProcessMonitorGraphVO stategraphcvo) {
		try {
		final ProcessMonitorVO statemodelvo = stategraphcvo.getStateModel();
		final StateModelLayout layoutinfo = statemodelvo.getLayout();
		final Integer processMonitorId = statemodelvo.getId();
		
		// process itself
//		Integer processMonitorId = (Integer) getMasterDataFacade().modify(NuclosEntity.PROCESSMONITOR.getEntityName(), MasterDataWrapper.wrapProcessMonitorVO(statemodelvo), null);
		
		//validateUniqueConstraint(statemodelvo);
		
		// update subprocesses:
		final Map<Integer, Integer> mpStates = new HashMap<Integer, Integer>();
		for (SubProcessVO statevo : stategraphcvo.getStates()) {
			if (!statevo.isRemoved()) {
				if (statevo.getId() == null) {
					// insert state:
					statevo.setProcessMonitorId(processMonitorId);
					MasterDataVO createdMDStateVO = getMasterDataFacade().create(NuclosEntity.PROCESSSTATEMODEL.getEntityName(), MasterDataWrapper.wrapSubProcessVO(statevo), null);
					
					mpStates.put(statevo.getWorkingId(), (Integer) createdMDStateVO.getId());		//prepare mapping table for state transition inserts/updates
					layoutinfo.updateStateId(statevo.getWorkingId(), mpStates.get(statevo.getWorkingId()));
				}
				else {
					// update state:
					getMasterDataFacade().modify(NuclosEntity.PROCESSSTATEMODEL.getEntityName(), MasterDataWrapper.wrapSubProcessVO(statevo), null);
					mpStates.put(statevo.getId(), statevo.getId());	//prepare mapping table for state transition inserts/updates

				}
			}
		}

//		List<GeneratorActionVO> listNormalGeneration = new ArrayList<GeneratorActionVO>();
//		List<GeneratorActionVO> listDependentGeneration = new ArrayList<GeneratorActionVO>();
		
		// update transitions:
		final Map<Integer, Integer> mpTransitions = new HashMap<Integer, Integer>();
		for (ProcessTransitionVO processtransitionvo : stategraphcvo.getTransitions()) {
			if (!processtransitionvo.isRemoved()) {
				
				final Integer iStateSource = processtransitionvo.getStateSource();
				if (iStateSource != null && iStateSource.intValue() < 0) {	//newly inserted state referenced?
					processtransitionvo.setStateSource(mpStates.get(iStateSource));				//map newly inserted state temp id to real state id
				}
				final Integer iStateTarget = processtransitionvo.getStateTarget();
				if (iStateSource != null && iStateTarget.intValue() < 0) {														//newly inserted state referenced?
					processtransitionvo.setStateTarget(mpStates.get(iStateTarget));				//map newly inserted state temp id to real state id
				}

				if (processtransitionvo.getId() == null) {
					// insert transition:
					processtransitionvo.setProcessMonitorId(processMonitorId);
					MasterDataVO createdMDTransitionVO = getMasterDataFacade().create(NuclosEntity.PROCESSTRANSITION.getEntityName(), MasterDataWrapper.wrapProcessTransitionVO(processtransitionvo), null);
					
					mpTransitions.put(processtransitionvo.getClientId(), (Integer) createdMDTransitionVO.getId());		//prepare mapping table for state transition inserts/updates
					layoutinfo.updateTransitionId(processtransitionvo.getClientId(), mpTransitions.get(processtransitionvo.getClientId()));
					
				// check object generator...
					if (iStateSource != null && iStateSource.intValue() > 0 &&
							iStateTarget != null && iStateTarget.intValue() > 0) {
						
//						ProcessStateModelLocal processStateSource = processstatemodelHome.findByPrimaryKey(mpStates.get(iStateSource));
//						ProcessStateModelLocal processStateTarget = processstatemodelHome.findByPrimaryKey(mpStates.get(iStateTarget));
//						
						MasterDataVO processStateSource = getMasterDataFacade().get(NuclosEntity.PROCESSSTATEMODEL.getEntityName(), mpStates.get(iStateSource));
						MasterDataVO processStateTarget = getMasterDataFacade().get(NuclosEntity.PROCESSSTATEMODEL.getEntityName(), mpStates.get(iStateTarget));
						
						Integer stateModelSourceId = (Integer) processStateSource.getField("stateModelId");//processStateSource.getStateModelId();
						Integer stateModelTargetId = (Integer) processStateTarget.getField("stateModelId");//processStateTarget.getStateModelId();
						if (stateModelSourceId != null && stateModelTargetId != null &&
								processStateSource.getField("stateModelUsageId") != null &&
								processStateTarget.getField("stateModelUsageId") != null){
							
//							StateModelLocal stateModelSource = statemodelHome.findByPrimaryKey(stateModelSourceId);
//							StateModelLocal stateModelTarget = statemodelHome.findByPrimaryKey(stateModelTargetId);
							
//							StateModelVO stateModelSource = MasterDataWrapper.getStateModelVO(getMasterDataFacade().get(NuclosEntity.STATEMODEL.getEntityName(), stateModelSourceId));
//							StateModelVO stateModelTarget = MasterDataWrapper.getStateModelVO(getMasterDataFacade().get(NuclosEntity.STATEMODEL.getEntityName(), stateModelTargetId));
							
							try {
								MasterDataVO stateModelUsageSource = masterDataFacade.get(NuclosEntity.STATEMODELUSAGE.getEntityName(), processStateSource.getField("stateModelUsageId"));
								MasterDataVO stateModelUsageTarget = masterDataFacade.get(NuclosEntity.STATEMODELUSAGE.getEntityName(), processStateTarget.getField("stateModelUsageId"));
								
								GeneratorActionVO newGenerationVO = new GeneratorActionVO(
									null,
									statemodelvo.getName()+"_"+processStateSource.getField("guarantor")+"_"+processStateTarget.getField("guarantor"),
									statemodelvo.getName()+"_"+processStateSource.getField("guarantor")+"_"+processStateTarget.getField("guarantor"),
//									statemodelvo.getName()+"_"+stateModelSource.getName()+"_"+stateModelTarget.getName(),
//									statemodelvo.getName()+"_"+stateModelSource.getName()+"_"+stateModelTarget.getName(),
									(Integer) stateModelUsageSource.getField("moduleId"),
									(Integer) stateModelUsageTarget.getField("moduleId"),
									null,
									(Integer) stateModelUsageTarget.getField("processId"),
									processtransitionvo.getId(),
									null);
								
//								newGenerationVO.setName(statemodelvo.getName()+"_"+stateModelSource.getName()+"_"+stateModelTarget.getName());
//								newGenerationVO.setLabel(statemodelvo.getName()+"_"+stateModelSource.getName()+"_"+stateModelTarget.getName());
//								newGenerationVO.setDescription("automaticly generated by process designer");
//								newGenerationVO.setSourceModuleId((Integer) stateModelUsageSource.getField("moduleId"));
//								newGenerationVO.setTargetModuleId((Integer) stateModelUsageTarget.getField("moduleId"));
//								newGenerationVO.setTargetProcessId((Integer) stateModelUsageTarget.getField("processId"));
//								newGenerationVO.setCaseTransitionId(processtransitionvo.getId());
//								newGenerationVO.setRuleonly(Boolean.TRUE);
//								newGenerationVO.setShowDetailsDialog(Boolean.FALSE);
								
								// remove existing object generator if necessary
								if (processtransitionvo.getGenerationId() != null){
//									MDGenerationLocal oldGenerationLocal = mdGenerationHome.findByPrimaryKey(processtransitionvo.getGenerationId());
//									GeneratorActionVO oldGenerationVO = oldGenerationLocal.getValueObject();
									GeneratorActionVO oldGenerationVO = MasterDataWrapper.getGeneratorActionVO(getMasterDataFacade().get(NuclosEntity.GENERATION.getEntityName(), processtransitionvo.getGenerationId()), 
											ServerServiceLocator.getInstance().getFacade(GeneratorFacadeLocal.class).getGeneratorUsages(processtransitionvo.getGenerationId()));
									
									boolean sourceModuleNoChange =  (newGenerationVO.getSourceModuleId()==null && oldGenerationVO.getSourceModuleId()==null) ||
															   	    (newGenerationVO.getSourceModuleId()!=null && 
																	   newGenerationVO.getSourceModuleId().equals(oldGenerationVO.getSourceModuleId()));
									boolean targetModuleNoChange =  (newGenerationVO.getTargetModuleId()==null && oldGenerationVO.getTargetModuleId()==null) ||
							   		 							    (newGenerationVO.getTargetModuleId()!=null && 
							   		 								   newGenerationVO.getTargetModuleId().equals(oldGenerationVO.getTargetModuleId()));
									boolean targetProcessNoChange = (newGenerationVO.getTargetProcessId()==null && oldGenerationVO.getTargetProcessId()==null) ||
		 								 							(newGenerationVO.getTargetProcessId()!=null && 
		 								 							   newGenerationVO.getTargetProcessId().equals(oldGenerationVO.getTargetProcessId()));
									if (!(sourceModuleNoChange && targetModuleNoChange && targetProcessNoChange)){
										// remove...
										processtransitionvo.setGenerationId(null);
//										oldGenerationLocal.remove();
										getMasterDataFacade().remove(NuclosEntity.GENERATION.getEntityName(), getMasterDataFacade().get(NuclosEntity.GENERATION.getEntityName(), processtransitionvo.getGenerationId()), false);
									} 
								}
								
								// add new generator
								if (processtransitionvo.getGenerationId() == null){
									MasterDataVO newMDGenerationVO = MasterDataWrapper.wrapGeneratorActionVO(newGenerationVO);
									newMDGenerationVO.setField("ruleonly", Boolean.TRUE);
									MasterDataVO createdMDNewGenerationVO = getMasterDataFacade().create(NuclosEntity.GENERATION.getEntityName(), newMDGenerationVO, null);
									Integer iGenerationId = (Integer) createdMDNewGenerationVO.getId();
									processtransitionvo.setGenerationId(iGenerationId);
								}
								
//								boolean dublicateTargetStateInDepending = false;
//								for (GeneratorActionVO generationVO : listDependentGeneration){
//									if (generationVO.getTargetModuleId().equals(newGenerationVO.getTargetModuleId()) &&
//										 (
//										   ((generationVO.getTargetProcessId() == null) && (newGenerationVO.getTargetProcessId() == null)) ||
//										   ((generationVO.getTargetProcessId() != null) && (generationVO.getTargetProcessId().equals(newGenerationVO.getTargetProcessId())))
//										 )
//									   ){
//										dublicateTargetStateInDepending = true;
//										listDependentGeneration.add(newGenerationVO);
//										break;
//									}
//								}
//								
//								boolean duplicateTargetStateInNormal = false;
//								for (GeneratorActionVO generationVO : listNormalGeneration){
//									if (generationVO.getTargetModuleId().equals(newGenerationVO.getTargetModuleId())&&
//										 (
//										   ((generationVO.getTargetProcessId() == null) && (newGenerationVO.getTargetProcessId() == null)) ||
//										   ((generationVO.getTargetProcessId() != null) && (generationVO.getTargetProcessId().equals(newGenerationVO.getTargetProcessId())))
//										 )
//									   ){
//										duplicateTargetStateInNormal = true;
//										listDependentGeneration.add(newGenerationVO);
//										listDependentGeneration.add(generationVO);
//										listNormalGeneration.remove(generationVO);
//										break;
//									}
//								}
//								
//								if (!duplicateTargetStateInNormal &&
//										!dublicateTargetStateInDepending){
//									listNormalGeneration.add(newGenerationVO);
//								}
							} catch (CommonFinderException e) {
								throw new CommonFatalException(e);
							} catch (CommonPermissionException e) {
								throw new CommonFatalException(e);
							}
							
						}
					}
				}
				else {
					// update transition:
//					final ProcessTransitionLocal processtransition = transitionHome.findByPrimaryKey(processtransitionvo.getId());
									
//					processtransition.setValueObject(processtransitionvo);
					modifyProcessTransition(processtransitionvo);
				}

			}
		}
		
//		for (GeneratorActionVO generationVO : listNormalGeneration){
//			//TODO do someting usefull ...
//		}
//		for (GeneratorActionVO generationVO : listDependentGeneration){
//			//TODO do someting usefull ...
//		}

		// remove transitions:
		
		for (ProcessTransitionVO processtransitionvo : stategraphcvo.getTransitions()) {
			if (processtransitionvo.isRemoved() && processtransitionvo.getId() != null) {
//				final ProcessTransitionLocal processtransition = transitionHome.findByPrimaryKey(processtransitionvo.getId());
//				processtransition.remove();
				getMasterDataFacade().remove(NuclosEntity.PROCESSTRANSITION.getEntityName(), getMasterDataFacade().get(NuclosEntity.PROCESSTRANSITION.getEntityName(), processtransitionvo.getId()), false);
			}
		}
		

		// remove states:
		
		for (SubProcessVO statevo : stategraphcvo.getStates()) {
			if (statevo.isRemoved() && statevo.getId() != null) {
//				final ProcessStateModelLocal localProcessState = processstatemodelHome.findByPrimaryKey(statevo.getId());
//				localProcessState.remove();
				getMasterDataFacade().remove(NuclosEntity.PROCESSSTATEMODEL.getEntityName(), getMasterDataFacade().get(NuclosEntity.PROCESSSTATEMODEL.getEntityName(), statevo.getId()), false);
			}
		}
		//StateCache.getInstance().invalidate();
		
		statemodelvo.setLayout(layoutinfo);
//		statemodel.setValueObject(statemodelvo);
		getMasterDataFacade().modify(NuclosEntity.PROCESSMONITOR.getEntityName(), MasterDataWrapper.wrapProcessMonitorVO(statemodelvo), null);
		}
		catch (Exception e) {
			throw new CommonFatalException(e);
		}
	}
	
	public List<SubProcessUsageCriteriaVO> getSubProcessUsageCriterias(Integer stateModelId){
		final List<SubProcessUsageCriteriaVO> result = new ArrayList<SubProcessUsageCriteriaVO>();
		
		if (stateModelId != null){
			final StateModelUsages allUsages = StateModelUsagesCache.getInstance().getStateUsages();
			final List<UsageCriteria> usageCriterias = allUsages.getUsageCriteriaByStateModelId(stateModelId);
			for (UsageCriteria uc : usageCriterias) {
				result.add(new SubProcessUsageCriteriaVO(uc));
			}
		}
		
		return result;
	}
	
	public SubProcessVO findStartingSubProcess(Integer iProcessMonitorId) {
		// find subprocess with no linking transistion on it -> start element!
		SubProcessVO startSubProcessVO = null;
		try {
//			final Collection<ProcessStateModelLocal> colSubProcesses = processstatemodelHome.findByCase(iProcessMonitorId);
			final Collection<SubProcessVO> colSubProcesses = findProcessModelByCase(iProcessMonitorId);
			
			for (SubProcessVO subProcess : colSubProcesses) {
//				final Collection<ProcessTransitionLocal> colTargetTransitions = transitionHome.findByTargetCase(subProcess.getId());
				final Collection<ProcessTransitionVO> colTargetTransitions = findProcessTransitionByTargetCase(subProcess.getId());
				
				if (colTargetTransitions.isEmpty()){
					
					if (startSubProcessVO != null) {
						// more than one start found...
						throw new CommonBusinessException("Startender Teil-Prozess konnte nicht ermittelt werden: Es kommen mehrere in Frage.");						
					}
					
					startSubProcessVO = subProcess;
				}
			}
			
			return startSubProcessVO;
		} catch (Exception e) {
			throw new CommonFatalException(e);
		}
	}
	
	public Boolean isFinalState(Integer targetStateId){
		Boolean result = new Boolean(false);
		
		try {
			final Collection<?> colResult = findStateTransitionBySourceState(targetStateId);
			if (colResult.isEmpty()){
				result = new Boolean(true);
			}
		} catch (CommonFinderException e) {
			return result;
		}
		
		return result;
	}
	
	public Integer getGenerationIdFromSubProcessTransition(Integer iSubProcessTransitionId){
		try {
//			return transitionHome.findByPrimaryKey(iSubProcessTransitionId).getGenerationId();
			return (Integer) getMasterDataFacade().get(NuclosEntity.PROCESSTRANSITION.getEntityName(), iSubProcessTransitionId).getField("generationId");
			
		} catch (Exception e) {
			return null;
		}
	}
	
	public DateTime getProcessPlanEnd(Integer iProcessMonitorId, DateTime datePlanStart) throws CommonBusinessException{
		final SubProcessVO startSubProcessVO = this.findStartingSubProcess(iProcessMonitorId);
		return getMaxPlanEndUntilEnd(startSubProcessVO, datePlanStart);
	}
	
	private DateTime getMaxPlanEndUntilEnd(SubProcessVO subProcess, DateTime datePlanStart){
		try {
			// max value of this step
			DateTime dateStepResult = null;
			
			// calculate plan end
			final DateTime datePlanEnd = this.getPlanEnd(subProcess, datePlanStart);		
			
			// search next
//			final Collection<ProcessTransitionLocal> nextTransitions = transitionHome.findBySourceCase(subProcess.getId());
			final Collection<ProcessTransitionVO> nextTransitions = findProcessTransitionBySourceCase(subProcess.getId());
			
			for (ProcessTransitionVO nextTransition : nextTransitions){
//				final SubProcessVO nextSubProcess = processstatemodelHome.findByPrimaryKey(nextTransition.getTargetCaseId()).getValueObject();
				MasterDataVO nextSubProcessMD = getMasterDataFacade().get(NuclosEntity.PROCESSSTATEMODEL.getEntityName(), nextTransition.getStateTarget());
				StateModelVO stateModelVO = MasterDataWrapper.getStateModelVO(getMasterDataFacade().get(NuclosEntity.STATEMODEL.getEntityName(), nextSubProcessMD.getField("stateModelId")));
				final SubProcessVO nextSubProcess = MasterDataWrapper.getSubProcessVO(nextSubProcessMD, stateModelVO);
				
				// calculate plan start of next sub process. if no series is set same date would be returned
				final DateTime dateNextPlanStart = this.getPlanStart(nextSubProcess, datePlanEnd);
				
				// calculate max plan end for next sub process
				final DateTime dateTemp = getMaxPlanEndUntilEnd(nextSubProcess, dateNextPlanStart);
				
				// get max
				if (dateStepResult == null){
					dateStepResult = dateTemp;
				} else if (dateStepResult.before(dateTemp)){
					dateStepResult = dateTemp;
				}
			}
			
			// no transitions
			if (dateStepResult == null){
				dateStepResult = datePlanEnd;
			}
			
			return dateStepResult;
		} catch (Exception e){
			throw new CommonFatalException(e);
		}
	}
	
	/**
	 * 
	 * @param subProcess
	 * @param datePlanStart
	 * @return max value of:
	 * 			DatePlanEnd (seriesNext from planEndSeries + datePlanStart)
	 * 		and DateRuntimeEnd (datePlanStart + runtime)
	 */
	public DateTime getPlanEnd(SubProcessVO subProcess, DateTime datePlanStart){

		final DateTime datePlanEnd = SeriesUtils.getSeriesNext(subProcess.getPlanEndSeries(), datePlanStart);
		
		final GregorianCalendar calRuntimeEnd = new GregorianCalendar();
		calRuntimeEnd.setTime(datePlanStart.getDate());
		calRuntimeEnd.add(subProcess.getRuntimeFormat().intValue(), subProcess.getRuntime());
		
		if (datePlanEnd.after(calRuntimeEnd.getTime())){
			return datePlanEnd;
		} else {
			return new DateTime(calRuntimeEnd.getTimeInMillis());
		}

	}
	
	public DateTime getPlanStart(SubProcessVO subProcess, DateTime dateOrigin) {
		return SeriesUtils.getSeriesNext(subProcess.getPlanStartSeries(), dateOrigin);
	}
	
	public List<ProcessStateRuntimeFormatVO> getPossibleRuntimeFormats() {
		ArrayList<ProcessStateRuntimeFormatVO> result = new ArrayList<ProcessStateRuntimeFormatVO>();
		
		result.add(new ProcessStateRuntimeFormatVO("Minuten", ProcessStateRuntimeFormatVO.MINUTE));
		result.add(new ProcessStateRuntimeFormatVO("Stunden", ProcessStateRuntimeFormatVO.HOUR));
		result.add(new ProcessStateRuntimeFormatVO("Tage", ProcessStateRuntimeFormatVO.DAY));
		result.add(new ProcessStateRuntimeFormatVO("Wochen", ProcessStateRuntimeFormatVO.WEEK));
		result.add(new ProcessStateRuntimeFormatVO("Monate", ProcessStateRuntimeFormatVO.MONTH));
		
		return result;
	}
	
	public void modifyProcessTransition(ProcessTransitionVO ptVO) {
		try {
			getMasterDataFacade().modify(NuclosEntity.PROCESSTRANSITION.getEntityName(), MasterDataWrapper.wrapProcessTransitionVO(ptVO), null);
			
			//add rules to transition
//			for (Iterator i = getRuleTransitionHome().findByTransition(this.getId()).iterator(); i.hasNext();) {
//				RuleEngineTransitionLocal ruleTransition = (RuleEngineTransitionLocal) i.next();
//				ruleTransition.remove();
//			}
//			int iOrderId = 1;
//			for (Integer iRuleId : processtransitionvo.getRuleIds()) {
//				getRuleTransitionHome().create(this.getId(), iRuleId, iOrderId);
//				iOrderId++;
//			}
			for (Object id : getMasterDataFacade().getMasterDataIds(NuclosEntity.RULETRANSITION.getEntityName(), new CollectableSearchExpression(
				SearchConditionUtils.newMDComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.RULETRANSITION), "transition", ComparisonOperator.EQUAL, ptVO.getId())))) {
				getMasterDataFacade().remove(NuclosEntity.RULETRANSITION.getEntityName(), getMasterDataFacade().get(NuclosEntity.RULETRANSITION.getEntityName(), id), false);
			}
			int iOrderId = 1;
			for (Integer iRuleId : ptVO.getRuleIds()) {
				Map<String, Object> mpFields = new HashMap<String,Object>();
				mpFields.put("transitionId", ptVO.getId());
				mpFields.put("ruleId", iRuleId);
				mpFields.put("order", iOrderId);
				MasterDataVO newRuleTransistionVO = new MasterDataVO(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.RULETRANSITION), false);
				getMasterDataFacade().create(NuclosEntity.RULETRANSITION.getEntityName(), newRuleTransistionVO, null);
				
				iOrderId++;
			}
			
			//add roles to transition
//			for (Iterator i = getRoleTransitionHome().findByTransition(this.getId()).iterator(); i.hasNext();) {
//				RoleTransitionLocal roleTransition = (RoleTransitionLocal) i.next();
//				roleTransition.remove();
//			}
//			for (Integer iRoleId : processtransitionvo.getRoleIds()) {
//				getRoleTransitionHome().create(this.getId(), iRoleId);
//			}
			for (Object id : getMasterDataFacade().getMasterDataIds(NuclosEntity.ROLETRANSITION.getEntityName(), new CollectableSearchExpression(
				SearchConditionUtils.newMDComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.ROLETRANSITION), "transition", ComparisonOperator.EQUAL, ptVO.getId())))) {
				getMasterDataFacade().remove(NuclosEntity.ROLETRANSITION.getEntityName(), getMasterDataFacade().get(NuclosEntity.ROLETRANSITION.getEntityName(), id), false);
			}
			for (Integer iRoleId : ptVO.getRoleIds()) {
				Map<String, Object> mpFields = new HashMap<String,Object>();
				mpFields.put("transitionId", ptVO.getId());
				mpFields.put("roleId", iRoleId);
				MasterDataVO newRoleTransistionVO = new MasterDataVO(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.ROLETRANSITION), false);
				getMasterDataFacade().create(NuclosEntity.ROLETRANSITION.getEntityName(), newRoleTransistionVO, null);
			}
			
			
		}
		catch(Exception e) {
			throw new CommonFatalException(e);
		}		
	}
	
	public ProcessTransitionVO getProcessTransition(Integer ptId) throws CommonPermissionException {
		ProcessTransitionVO result;
		try {
			result = MasterDataWrapper.getProcessTransitionVO(getMasterDataFacade().get(NuclosEntity.PROCESSTRANSITION.getEntityName(), ptId));
		}
		catch(CommonFinderException e) {
			throw new CommonFatalException(e);
		}
		
		final List<Integer> lstRuleIds = new ArrayList<Integer>();
		final List<Integer> lstRoleIds = new ArrayList<Integer>();
		
		//get rules attached to transition
		final List<CollectableSorting> lstSortingOrder = new ArrayList<CollectableSorting>();
		final NuclosEntity ruleTransition = NuclosEntity.RULETRANSITION;
		lstSortingOrder.add(new CollectableSorting(SystemFields.BASE_ALIAS, ruleTransition.getEntityName(), true, "order", true));
		for (Object id : getMasterDataFacade().getMasterDataIds(ruleTransition.getEntityName(), new CollectableSearchExpression(
			SearchConditionUtils.newMDComparison(MasterDataMetaCache.getInstance().getMetaData(ruleTransition), "transition", ComparisonOperator.EQUAL, ptId), 
			lstSortingOrder))) {
			lstRuleIds.add((Integer) id);
		}
		result.setRuleIds(lstRuleIds);
		
		//get roles attached to transition
		for (Object id : getMasterDataFacade().getMasterDataIds(NuclosEntity.ROLETRANSITION.getEntityName(), new CollectableSearchExpression(
			SearchConditionUtils.newMDComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.ROLETRANSITION), "transition", ComparisonOperator.EQUAL, ptId)))) {
			lstRoleIds.add((Integer) id);
		}
		result.setRoleIds(lstRoleIds);
		return result;
	}

	public Collection<ProcessTransitionVO> findProcessTransitionByTargetStateAndProcessmodel(Integer targetStateId, Integer processmodelId) throws CommonPermissionException {
		/**
		 * old ejb finder:
		 * SELECT OBJECT(t) FROM ProcessTransition AS t WHERE t.stateId = ?1 AND t.caseId = ?2
		 */
		Collection<ProcessTransitionVO> result = new ArrayList<ProcessTransitionVO>();
		for (Object id : getMasterDataFacade().getMasterDataIds(NuclosEntity.PROCESSTRANSITION.getEntityName(), new CollectableSearchExpression(SearchConditionUtils.and(
			SearchConditionUtils.newMDComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.PROCESSTRANSITION), "stateId", ComparisonOperator.EQUAL, targetStateId),
			SearchConditionUtils.newMDComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.PROCESSTRANSITION), "caseId", ComparisonOperator.EQUAL, processmodelId))))) {
			result.add(getProcessTransition((Integer) id));
		}
		
		return result;
	}
	
	public Collection<ProcessTransitionVO> findProcessTransitionByCase(Integer caseId) throws CommonPermissionException {
		/**
		 * old ejb finder:
		 * SELECT OBJECT(t) FROM ProcessTransition AS t WHERE t.caseId = ?1
		 */
		Collection<ProcessTransitionVO> result = new ArrayList<ProcessTransitionVO>();
		for (Object id : getMasterDataFacade().getMasterDataIds(NuclosEntity.PROCESSTRANSITION.getEntityName(), new CollectableSearchExpression(
			SearchConditionUtils.newMDComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.PROCESSTRANSITION), "caseId", ComparisonOperator.EQUAL, caseId)))) {
			try {
				result.add(MasterDataWrapper.getProcessTransitionVO(getMasterDataFacade().get(NuclosEntity.PROCESSTRANSITION.getEntityName(), id)));
			}
			catch(CommonFinderException e) {
				throw new CommonFatalException(e);
			}
		}
		return result;
	}
	
	public Collection<ProcessTransitionVO> findProcessTransitionByTargetCase(Integer caseId) throws CommonPermissionException {
		/**
		 * old ejb finder:
		 * SELECT OBJECT(t) FROM ProcessTransition AS t WHERE t.targetCaseId = ?1
		 */
		Collection<ProcessTransitionVO> result = new ArrayList<ProcessTransitionVO>();
		for (Object id : getMasterDataFacade().getMasterDataIds(NuclosEntity.PROCESSTRANSITION.getEntityName(), new CollectableSearchExpression(
			SearchConditionUtils.newMDComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.PROCESSTRANSITION), "targetCaseId", ComparisonOperator.EQUAL, caseId)))) {
			try {
				result.add(MasterDataWrapper.getProcessTransitionVO(getMasterDataFacade().get(NuclosEntity.PROCESSTRANSITION.getEntityName(), id)));
			}
			catch(CommonFinderException e) {
				throw new CommonFatalException(e);
			}
		}
		return result;
	}
	
	public Collection<ProcessTransitionVO> findProcessTransitionBySourceCase(Integer caseId) throws CommonPermissionException {
		/**
		 * old ejb finder:
		 * SELECT OBJECT(t) FROM ProcessTransition AS t WHERE t.sourceCaseId = ?1
		 */
		Collection<ProcessTransitionVO> result = new ArrayList<ProcessTransitionVO>();
		for (Object id : getMasterDataFacade().getMasterDataIds(NuclosEntity.PROCESSTRANSITION.getEntityName(), new CollectableSearchExpression(
			SearchConditionUtils.newMDComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.PROCESSTRANSITION), "sourceCaseId", ComparisonOperator.EQUAL, caseId)))) {
			try {
				result.add(MasterDataWrapper.getProcessTransitionVO(getMasterDataFacade().get(NuclosEntity.PROCESSTRANSITION.getEntityName(), id)));
			}
			catch(CommonFinderException e) {
				throw new CommonFatalException(e);
			}
		}
		return result;
	}
	
	public Collection<SubProcessVO> findProcessModelByCase(Integer caseId) throws CommonPermissionException {
		/**
		 * old ejb finder:
		 * SELECT OBJECT(t) FROM ProcessStateModel AS t WHERE t.caseId = ?1
		 */
		Collection<SubProcessVO> result = new ArrayList<SubProcessVO>();
		for (Object id : getMasterDataFacade().getMasterDataIds(NuclosEntity.PROCESSSTATEMODEL.getEntityName(), new CollectableSearchExpression(
			SearchConditionUtils.newMDComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.PROCESSSTATEMODEL.getEntityName()), "caseId", ComparisonOperator.EQUAL, caseId)))) {
			try {
				MasterDataVO mdSubProcess = getMasterDataFacade().get(NuclosEntity.PROCESSSTATEMODEL.getEntityName(), id);
				StateModelVO stateModelVO = MasterDataWrapper.getStateModelVO(getMasterDataFacade().get(NuclosEntity.STATEMODEL.getEntityName(), mdSubProcess.getField("stateModelId")));
				result.add(MasterDataWrapper.getSubProcessVO(mdSubProcess, stateModelVO));
			}
			catch(CommonFinderException e) {
				throw new CommonFatalException(e);
			}
		}
		return result;
	}
	
	public Collection<MasterDataVO> findGeneratorsWhichArePointingToSameSubProcess(Integer generationId, Integer targetCaseId) throws CommonPermissionException {
		Collection<MasterDataVO> result = new ArrayList<MasterDataVO>();

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom g = query.from("T_MD_GENERATION").alias("g");
		DbFrom pt = g.join("T_MD_CASE_TRANSITION", JoinType.INNER).alias("pt").on("INTID_T_MD_CASE_TRANSITION", "INTID", Integer.class);
		query.select(g.baseColumn("INTID", Integer.class));
		query.where(builder.and(
			builder.equal(g.baseColumn("INTID", Integer.class), generationId).not(),
			builder.equal(pt.baseColumn("INTID_T_MD_CASE_2", Integer.class), targetCaseId)));
		
		List<Integer> ids = dataBaseHelper.getDbAccess().executeQuery(query);
		
		for (Integer id : ids) {
			try {
				result.add(getMasterDataFacade().get(NuclosEntity.GENERATION.getEntityName(), id));
			}
			catch(CommonFinderException e) {
				throw new CommonFatalException(e);
			}
		}
		return result;
	}
	
	public Collection<StateTransitionVO> findStateTransitionBySourceState(Integer stateId) throws CommonFinderException {
		/**
		 * old ejb finder:
		 * SELECT OBJECT(t) FROM StateTransition AS t WHERE t.sourceStateId = ?1
		 */
		Collection<StateTransitionVO> result = new ArrayList<StateTransitionVO>();
		for (Object id : getMasterDataFacade().getMasterDataIds(NuclosEntity.STATETRANSITION.getEntityName(), new CollectableSearchExpression(
			SearchConditionUtils.newMDComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.STATETRANSITION), "state1", ComparisonOperator.EQUAL, stateId)))) {
			try {
				result.add(MasterDataWrapper.getStateTransitionVO(getMasterDataFacade().getWithDependants(NuclosEntity.PROCESSTRANSITION.getEntityName(), (Integer) id)));
			}
			catch(CommonFinderException nfe) {
				throw nfe;
			}
			catch(Exception e) {
				throw new CommonFatalException(e);
			}
		}
		return result;
	}	

}	// class StateFacadeBean
