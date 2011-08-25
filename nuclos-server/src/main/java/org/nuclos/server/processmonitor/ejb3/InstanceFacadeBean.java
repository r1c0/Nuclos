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
import java.util.Date;
import java.util.GregorianCalendar;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common.dblayer.JoinType;
import org.nuclos.common2.DateTime;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.common.InstanceConstants;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.query.DbCondition;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.genericobject.GenericObjectMetaDataCache;
import org.nuclos.server.genericobject.ejb3.GeneratorFacadeLocal;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.masterdata.MasterDataWrapper;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.processmonitor.valueobject.ProcessTransitionVO;
import org.nuclos.server.processmonitor.valueobject.SubProcessVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.statemodel.valueobject.StateModelVO;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade bean for instance management.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@novabit.de">Maik Stueker</a>
 * @version 00.01.000
 * @todo restrict
 */
@Stateless
@Local(InstanceFacadeLocal.class)
@Remote(InstanceFacadeRemote.class)
@Transactional
public class InstanceFacadeBean extends NuclosFacadeBean implements InstanceFacadeLocal, InstanceFacadeRemote, InstanceConstants{
	
	private GeneratorFacadeLocal generatorFacade;
	private ProcessMonitorFacadeLocal processMonitorFacade;
	
	/**
	 * notify instance about state change if any instance is set.
	 * 
	 * if more than one transition links to the generating new subprocess
	 * a check of the other transitions objectgeneration runs is needed. 
	 * if all objectgenerations pass through the new subprocess could
	 * be generated.
	 * 
	 * @param genericObjectId
	 * @param targetStateId
	 */
	@Override
	public void notifyInstanceAboutStateChange(Integer genericObjectId, Integer targetStateId){
		try {
			final GenericObjectVO goVO = this.getGenericObjectFacade().get(genericObjectId, false);
			final Integer instanceId = goVO.getInstanceId();
			if (instanceId == null){
				// no instance -> no need to handle
				return;
			}
			
			final Integer processmodelId = getProcessmodelFromInstance(instanceId);	
			Collection<ProcessTransitionVO> colTransitions = getProcessMonitorFacade().findProcessTransitionByTargetStateAndProcessmodel(targetStateId, processmodelId);
			
			if (colTransitions.isEmpty()){
				// no more transitions? -> end is near...
				
				// only set real end und runtime if necessary (state is final)
				DateTime dateRealEnd = this.setInstanceAttributesInSource(goVO, targetStateId);
				if (dateRealEnd != null){
					this.setRealEndInInstance(goVO, targetStateId, dateRealEnd);
				}
			}
			
			for (ProcessTransitionVO transition : colTransitions){
				// if generation is not set continue with next transition
				if (transition.getGenerationId() == null){
					continue;
				}
				
				// vergleiche  module und prozess des herkunfts prozessmodel'state' mit dem Objekt welches gerade im Status ge\u00e4ndert wurde 
				MasterDataVO sourceSubProcessMD = getMasterDataFacade().get(NuclosEntity.PROCESSSTATEMODEL.getEntityName(), transition.getStateSource());
				StateModelVO stateModelVO = MasterDataWrapper.getStateModelVO(getMasterDataFacade().get(NuclosEntity.STATEMODEL.getEntityName(), sourceSubProcessMD.getField("stateModelId")));
				SubProcessVO sourceSubProcess = MasterDataWrapper.getSubProcessVO(sourceSubProcessMD, stateModelVO);
				
				MasterDataVO statemodelUsageMD = getMasterDataFacade().get(NuclosEntity.STATEMODELUSAGE.getEntityName(), sourceSubProcess.getStateModelUsageId());
				if (!Integer.valueOf(goVO.getModuleId()).equals(statemodelUsageMD.getField("moduleId"))) {
					continue;
				}
				
				Integer iProzessId = null;
				try {
					DynamicAttributeVO attrProzess = goVO.getAttribute(NuclosEOField.PROCESS.getMetaData().getField(), AttributeCache.getInstance());
					iProzessId = attrProzess.getValueId();
				} catch (Exception e) {
					// do nothing here if not exists...
				}
				if (iProzessId != null && statemodelUsageMD.getField("processId") != null) {
					if (!iProzessId.equals(statemodelUsageMD.getField("processId"))) {
						continue;
					}
				} else if (iProzessId == null && statemodelUsageMD.getField("processId") != null) {
					continue;
				} else if (iProzessId != null && statemodelUsageMD.getField("processId") == null) {
					continue;
				} 
				
				// 1. check for other generators (n:1)
				Collection<MasterDataVO> otherGenerations = getProcessMonitorFacade().findGeneratorsWhichArePointingToSameSubProcess(transition.getGenerationId(), transition.getStateTarget());
				if (otherGenerations.isEmpty()){
					// no other ... same procedure as every year ;-)
					MasterDataVO mdGaVO = getMasterDataFacade().get(NuclosEntity.GENERATION.getEntityName(), transition.getGenerationId());
					GeneratorActionVO gaVO = MasterDataWrapper.getGeneratorActionVO(mdGaVO, ServiceLocator.getInstance().getFacade(GeneratorFacadeLocal.class).getGeneratorUsages(mdGaVO.getIntId()));
					final GenericObjectVO createdGoVO = this.getGeneratorFacade().generateGenericObjectWithoutCheckingPermission(
							genericObjectId, 
							gaVO);
					// instance would be automaticly transfered to target
					// but we have to set the attributes
					setInstanceAttributes(genericObjectId, createdGoVO.getId(), targetStateId, transition);
					
					// mark this generation with "run"
					this.createRunOfObjectGeneration(instanceId, transition.getGenerationId(), new Boolean(true));
					
				} else {
					// check other generators finishing...
					boolean notready = false;
					
					for (MasterDataVO generation : otherGenerations){
						// all other generations marked with "run" -> nothing to do here					
						// at least one still in "not run" -> notready=true

						Boolean isObjectGenerated = this.isObjectGenerated(instanceId, (Integer) generation.getId());
						if (isObjectGenerated == null){
							notready = true;
						} else if (isObjectGenerated.equals(Boolean.TRUE)){
							// something went wrong! Why is a object generated and this transition 
							// wants to generate it, too?
							
							throw new CommonFatalException("Teil-Prozess Generierung bereits durchgef\u00fchrt. Es gab einen internen Ablauffehler!");
						}
					}
					
					if (notready){
						// only set real end und runtime if necessary
						this.setInstanceAttributesInSource(goVO, targetStateId);
						
					} else {
						// generate ...
						MasterDataVO mdGaVO = getMasterDataFacade().get(NuclosEntity.GENERATION.getEntityName(), transition.getGenerationId());
						GeneratorActionVO gaVO = MasterDataWrapper.getGeneratorActionVO(mdGaVO, ServiceLocator.getInstance().getFacade(GeneratorFacadeLocal.class).getGeneratorUsages(mdGaVO.getIntId()));
						
						final GenericObjectVO createdGoVO = this.getGeneratorFacade().generateGenericObjectWithoutCheckingPermission(
								genericObjectId, 
								gaVO);
						// instance would be automaticly transfered to target
						// but we have to set the attributes
						setInstanceAttributes(genericObjectId, createdGoVO.getId(), targetStateId, transition);
					}
					// mark this generation with "run"
					this.createRunOfObjectGeneration(instanceId, transition.getGenerationId(), new Boolean(!notready));
						
				}
			}
		} catch (Exception e) {
			throw new CommonFatalException("Ein Teil-Prozess Nachfolger konnte nicht gestartet werden!", e);
		} 
	}
	
	/**
	 * 
	 * @param iInstanceId
	 * @return
	 */
	private Integer getProcessmodelFromInstance(Integer iInstanceId){
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom t = query.from("T_MD_INSTANCE").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("INTID_T_MD_CASE", Integer.class));
		query.where(builder.equal(t.baseColumn("INTID", Integer.class), iInstanceId));
		return CollectionUtils.getFirst(DataBaseHelper.getDbAccess().executeQuery(query));
	}
	
	/**
	 * @param sourceGenericObjectId
	 * @param targetGenericObjectId
	 * @param targetStateId
	 * @param processTransitionVO
	 * @throws NuclosBusinessRuleException
	 * @throws CommonStaleVersionException
	 * 
	 * source:
	 * [real_end], [real_runtime]
	 * Only if target state is the final state
	 * 
	 * target:
	 * [plan_start], [plan_end], [plan_runtime], [real_start]
	 */
	private void setInstanceAttributes(Integer sourceGenericObjectId, Integer targetGenericObjectId, Integer targetStateId, ProcessTransitionVO processTransitionVO) throws NuclosBusinessRuleException, CommonStaleVersionException{
		try {
			final AttributeCache attrprovider = AttributeCache.getInstance();
			final GenericObjectVO sourceGoVO = getGenericObjectFacade().get(sourceGenericObjectId, false);
			final GenericObjectVO targetGoVO = getGenericObjectFacade().get(targetGenericObjectId, false);
			
			//--------------------------------------------------------------------------------------------
			// log to source
			this.setInstanceAttributesInSource(sourceGoVO, targetStateId);
			
			//--------------------------------------------------------------------------------------------
			// log to target
			MasterDataVO targetSubProcessMD = getMasterDataFacade().get(NuclosEntity.PROCESSSTATEMODEL.getEntityName(), processTransitionVO.getStateTarget());
			StateModelVO stateModelVO = MasterDataWrapper.getStateModelVO(getMasterDataFacade().get(NuclosEntity.STATEMODEL.getEntityName(), targetSubProcessMD.getField("stateModelId")));
			final SubProcessVO targetSubProcess = MasterDataWrapper.getSubProcessVO(targetSubProcessMD, stateModelVO);
			final DateTime dateOrigin = (DateTime) sourceGoVO.getAttribute("[plan_end]", attrprovider).getValue();
			this.setInstanceAttributesInTarget(targetGoVO, targetSubProcess, dateOrigin, false);
			
		} catch (CommonFinderException e) {
			throw new CommonFatalException(e);
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e);
		} 
		
	}
	
	/**
	 * 
	 * @param sourceGenericObjectId
	 * @param targetStateId
	 * @throws NuclosBusinessRuleException
	 * @throws CommonStaleVersionException
	 * @return Real End (if it is the final state, otherwise null)
	 * 
	 * source:
	 * [real_end], [real_runtime]
	 * Only if target state is the final state
	 */
	private DateTime setInstanceAttributesInSource(GenericObjectVO sourceGoVO, Integer targetStateId) throws NuclosBusinessRuleException, CommonStaleVersionException{
		try {
			// if is final state
			if (getProcessMonitorFacade().isFinalState(targetStateId).booleanValue()){
				final AttributeCache attrprovider = AttributeCache.getInstance();
				
				final DateTime dateRealEnd = new DateTime();
				
				// calculate real runtime in millis
				final GregorianCalendar calendar = new GregorianCalendar();
				
				calendar.setTime(((DateTime) sourceGoVO.getAttribute("[real_start]", attrprovider).getValue()).getDate());
				final long realStartMillis = calendar.getTimeInMillis();
				
				calendar.setTime(dateRealEnd.getDate());
				final long realEndMillis = calendar.getTimeInMillis();
				
				final Integer iRealEndId	     = attrprovider.getAttribute(sourceGoVO.getModuleId(), "[real_end]").getId();
				final Integer iRealRuntimeId  	 = attrprovider.getAttribute(sourceGoVO.getModuleId(), "[real_runtime]").getId();
				
				if (!sourceGoVO.wasAttributeIdLoaded(iRealEndId)){
					sourceGoVO.addAttribute(iRealEndId);
				} 
				if (!sourceGoVO.wasAttributeIdLoaded(iRealRuntimeId)){
					sourceGoVO.addAttribute(iRealRuntimeId);
				}
				
				if (sourceGoVO.getAttribute(iRealEndId) != null){
					sourceGoVO.getAttribute(iRealEndId).setValue(dateRealEnd);
				} else {
					sourceGoVO.setAttribute(new DynamicAttributeVO(iRealEndId,     null, dateRealEnd));
				}
				if (sourceGoVO.getAttribute(iRealRuntimeId) != null){
					sourceGoVO.getAttribute(iRealRuntimeId).setValue(new Double(realEndMillis-realStartMillis));
				} else {
					sourceGoVO.setAttribute(new DynamicAttributeVO(iRealRuntimeId, null, new Double(realEndMillis-realStartMillis)));
				}
				
				getGenericObjectFacade().modify(sourceGoVO, null, false, false);
				
				return dateRealEnd;
			}
			return null;
			
		} catch (CommonFinderException e) {
			throw new CommonFatalException(e);
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e);
		} catch (CommonValidationException e) {
			throw new CommonFatalException(e);
		} catch (CommonCreateException e) {
			throw new CommonFatalException(e);
		} catch (CommonRemoveException e) {
			throw new CommonFatalException(e);
		}
		catch(NuclosBusinessException e) {
			throw new CommonFatalException(e);
		}
	}
	
	/**
	 * 
	 * @param targetGenericObjectId
	 * @param targetSubProcess
	 * @param dateOrigin
	 * @param useDateOriginAsPlanStart
	 * @throws NuclosBusinessRuleException
	 * @throws CommonStaleVersionException
	 * 
	 * sets in target:
	 * [plan_start], [plan_end], [plan_runtime], [real_start]
	 */
	private void setInstanceAttributesInTarget(GenericObjectVO targetGoVO, SubProcessVO targetSubProcess, DateTime dateOrigin, boolean useDateOriginAsPlanStart) throws NuclosBusinessRuleException, CommonStaleVersionException{
		try {
			final AttributeCache attrprovider = AttributeCache.getInstance();
			
			final Integer iPlanStartId       = attrprovider.getAttribute(targetGoVO.getModuleId(), "[plan_start]").getId();
			final Integer iPlanEndId	     = attrprovider.getAttribute(targetGoVO.getModuleId(), "[plan_end]").getId();
			final Integer iPlanRuntimeId	 = attrprovider.getAttribute(targetGoVO.getModuleId(), "[plan_runtime]").getId();
			final Integer iRealStartId	     = attrprovider.getAttribute(targetGoVO.getModuleId(), "[real_start]").getId();
			
			targetGoVO.addAttribute(iPlanStartId);
			targetGoVO.addAttribute(iPlanEndId);
			targetGoVO.addAttribute(iPlanRuntimeId);
			targetGoVO.addAttribute(iRealStartId);
			
			final DateTime dateTargetPlanStart = useDateOriginAsPlanStart? dateOrigin: getProcessMonitorFacade().getPlanStart(targetSubProcess, dateOrigin);
			final DateTime dateTargetPlanEnd   = getProcessMonitorFacade().getPlanEnd(targetSubProcess, dateTargetPlanStart);		

			// calculate plan runtime in millis	
			final GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(dateTargetPlanStart.getDate());
			final long planStartMillis = calendar.getTimeInMillis();
			
			if (targetSubProcess.getRuntime() != null 
					&& targetSubProcess.getRuntimeFormat() != null
					&& targetSubProcess.getRuntime().intValue() > 0){
				
				calendar.add(targetSubProcess.getRuntimeFormat().intValue(), targetSubProcess.getRuntime().intValue());
			} else {
				calendar.setTime(dateTargetPlanEnd.getDate());
			}
			
			// set the attributes
			targetGoVO.setAttribute(new DynamicAttributeVO(iPlanStartId,   null, dateTargetPlanStart));
			targetGoVO.setAttribute(new DynamicAttributeVO(iPlanEndId,     null, dateTargetPlanEnd));
			targetGoVO.setAttribute(new DynamicAttributeVO(iPlanRuntimeId, null, new Double(calendar.getTimeInMillis() - planStartMillis)));
			targetGoVO.setAttribute(new DynamicAttributeVO(iRealStartId,   null, new DateTime(targetGoVO.getCreatedAt())));
			
			getGenericObjectFacade().modify(targetGoVO, null, false, false);
			
		} catch (CommonFinderException e) {
			throw new CommonFatalException(e);
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e);
		} catch (CommonValidationException e) {
			throw new CommonFatalException(e);
		} catch (CommonCreateException e) {
			throw new CommonFatalException(e);
		} catch (CommonRemoveException e) {
			throw new CommonFatalException(e);
		}
		catch(NuclosBusinessException e) {
			throw new CommonFatalException(e);
		}
	}
	
	/**
	 * 
	 * @param sourceGoVO
	 * @param targetStateId
	 * 
	 * Only if target state is the final state
	 * @param dateRealEnd 
	 */
	private void setRealEndInInstance(GenericObjectVO goVO, Integer targetStateId, DateTime dateRealEnd){
		// if is final state
		if (getProcessMonitorFacade().isFinalState(targetStateId).booleanValue()){
			try {
				MasterDataVO instanceVO = getMasterDataFacade().get(NuclosEntity.INSTANCE.getEntityName(), goVO.getInstanceId());
				instanceVO.setField("realend", dateRealEnd);
				getMasterDataFacade().modify(NuclosEntity.INSTANCE.getEntityName(), instanceVO, null);
				
			} catch (CommonFinderException e) {
				throw new CommonFatalException(e);
			} catch (CommonPermissionException e) {
				throw new CommonFatalException(e);
			} catch (CommonCreateException e) {
				throw new CommonFatalException(e);
			} catch (CommonRemoveException e) {
				throw new CommonFatalException(e);
			} catch (CommonStaleVersionException e) {
				throw new CommonFatalException(e);
			} catch (CommonValidationException e) {
				throw new CommonFatalException(e);
			}
			catch(NuclosBusinessRuleException e) {
				throw new CommonFatalException(e);
			}
		}
	}

	/**
	 * 
	 * @param iInstanceId
	 * @param iGenerationId
	 * @return
	 * 			Boolean.TRUE   if generation for given instance is run and has generated a generic object
	 * 			Boolean.FALSE  if generation for given instance is run but has NOT generated a generic object
	 * 			null		   if generation for given instance is not yet run
	 * 
	 */
	@Override
	public Boolean isObjectGenerated(Integer iInstanceId, Integer iGenerationId){
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Boolean> query = builder.createQuery(Boolean.class);
		DbFrom t = query.from("T_MD_INSTANCE_OBJGENERATION").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("BLNOBJECTGENERATED", Boolean.class));
		query.where(builder.and(
			builder.equal(t.baseColumn("INTID_T_MD_INSTANCE", Integer.class), iInstanceId),
			builder.equal(t.baseColumn("INTID_T_MD_GENERATION", Integer.class), iGenerationId)));
		return CollectionUtils.getFirst(DataBaseHelper.getDbAccess().executeQuery(query));
	}
	
	/**
	 * 
	 * @param iInstanceId
	 * @return
	 * 
	 */
	@Override
	public Boolean isProcessInstanceStarted(Integer iInstanceId){
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Long> query = builder.createQuery(Long.class);
		DbFrom t = query.from("T_UD_GENERICOBJECT").alias(SystemFields.BASE_ALIAS);
		query.select(builder.count(t.baseColumn("INTID", Integer.class)));
		query.where(builder.equal(t.baseColumn("INTID_T_MD_INSTANCE", Integer.class), iInstanceId));
		return DataBaseHelper.getDbAccess().executeQuerySingleResult(query) > 0L;
	}
	
	/**
	 * 
	 * @param iInstanceId
	 * @param iGenerationId
	 * @param bResult
	 * @throws CommonPermissionException 
	 * @throws CommonCreateException 
	 * 
	 */
	@Override
	public void createRunOfObjectGeneration(Integer iInstanceId, Integer iGenerationId, Boolean bResult) throws CommonCreateException, CommonPermissionException{
		MasterDataVO mdvo = new MasterDataVO(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.INSTANCEOBJECTGENERATION), false);
		mdvo.setField("instanceId", iInstanceId);
		mdvo.setField("generationId", iGenerationId);
		mdvo.setField("objectgenerated", bResult);
		try {
			getMasterDataFacade().create(NuclosEntity.INSTANCEOBJECTGENERATION.getEntityName(), mdvo, null);
		}
		catch(NuclosBusinessRuleException e) {
			throw new CommonFatalException(e);
		}
	}
	
	/**
	 * 
	 * @param iProcessMonitorId
	 * @param iInstanceId
	 * @throws CommonBusinessException 
	 */
	@Override
	public void createProcessInstance(Integer iProcessMonitorId, Integer iInstanceId) throws CommonBusinessException{
		
		MasterDataVO instanceVO = getMasterDataFacade().get(NuclosEntity.INSTANCE.getEntityName(), iInstanceId);
		DateTime datePlanStart  = (DateTime) instanceVO.getField("planstart");
		DateTime datePlanEnd    = (DateTime) instanceVO.getField("planstart");
		
		if (this.isProcessInstanceStarted(iInstanceId).booleanValue()){
			throw new CommonBusinessException("Instance already started!");
		}
		if (datePlanStart == null || datePlanEnd == null){
			throw new CommonBusinessException("Plan start and plan end has to be set in order to start an instance!");
		}
		
		SubProcessVO startSubProcess = getProcessMonitorFacade().findStartingSubProcess(iProcessMonitorId);
		MasterDataVO stateModelUsage = getMasterDataFacade().get(NuclosEntity.STATEMODELUSAGE.getEntityName(), startSubProcess.getStateModelUsageId());
				
		GenericObjectVO goVO = new GenericObjectVO((Integer)stateModelUsage.getField("moduleId"), null, iInstanceId, GenericObjectMetaDataCache.getInstance());
		if (stateModelUsage.getField("process") != null){
			DynamicAttributeVO dynProzess = new DynamicAttributeVO(AttributeCache.getInstance().getAttribute(goVO.getModuleId(), NuclosEOField.PROCESS.getMetaData().getField()).getId(), (Integer) stateModelUsage.getField("processId"), stateModelUsage.getField("process"));
			goVO.setAttribute(dynProzess);
		}
		goVO = getGenericObjectFacade().create(new GenericObjectWithDependantsVO(goVO, new DependantMasterDataMap()));
		
//		Integer iGOProcessId = (Integer)stateModelUsage.getField("processId");
//		if (iGOProcessId != null){
//			MasterDataVO processVO = getMasterDataFacade().get(NuclosEntity.PROCESS.getEntityName(), iGOProcessId);
//			goVO.setAttribute(new DynamicAttributeVO(null, AttributeCache.getInstance().getAttribute(NuclosEOField.PROCESS.getMetaData().getField()).getId(), iGOProcessId, processVO.getField("name").toString()));
//			goVO = getGenericObjectFacade().modify(goVO, null, false);
//		}
		
		this.setInstanceAttributesInTarget(goVO, startSubProcess, (DateTime) instanceVO.getField("planstart"), true);
		
		instanceVO.setField("realstart", new DateTime(goVO.getCreatedAt()));
		getMasterDataFacade().modify(NuclosEntity.INSTANCE.getEntityName(), instanceVO, null);
	}
	
	/**
	 * 
	 * @param iInstanceId
	 * @param iStateModelUsageId
	 * @return object id (could be null)
	 */
	@Override
	public Integer getObjectId(Integer iInstanceId, Integer iStateModelUsageId){
		MasterDataVO stateModelUsage;
		try {
			stateModelUsage = getMasterDataFacade().get(NuclosEntity.STATEMODELUSAGE.getEntityName(), iStateModelUsageId);
		} catch (CommonFinderException e) {
			throw new CommonFatalException(e);
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e);
		}
		final Integer iModuleId = (Integer) stateModelUsage.getField("moduleId");
		final Integer iProcessId = (Integer) stateModelUsage.getField("processId");

		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom t = query.from("T_UD_GENERICOBJECT").alias("go");
		query.select(t.baseColumn("INTID", Integer.class));
		DbCondition cond = builder.and(
			builder.equal(t.baseColumn("INTID_T_MD_INSTANCE", Integer.class), iInstanceId),
			builder.equal(t.baseColumn("INTID_T_MD_MODULE", Integer.class), iModuleId));
		if (true) {
			DbFrom t2 = t.join("T_UD_GO_ATTRIBUTE", JoinType.INNER).on("INTID", "INTID_T_UD_GENERICOBJECT").alias("at");
			cond = builder.and(cond,
				builder.equal(t2.baseColumn("INTID_T_MD_ATTRIBUTE", Integer.class), 100003),
				builder.equal(t2.baseColumn("INTID_EXTERNAL", Integer.class), iProcessId));
		}
		query.where(cond);
		
		return CollectionUtils.getFirst(DataBaseHelper.getDbAccess().executeQuery(query));
	}
	
	/**
	 * 
	 * @param iInstanceId
	 * @param iStateModelUsageId
	 */
	@Override
	public int getInstanceStatus(Integer iInstanceId, Integer iStateModelUsageId){
		try {
			final Date now = new Date();

			final Integer iObjectId = getObjectId(iInstanceId, iStateModelUsageId);
			
			if (iObjectId != null){
				GenericObjectVO goVO = getGenericObjectFacade().get(iObjectId, false);
				
				DynamicAttributeVO attrRealend = goVO.getAttribute("[real_end]", getAttributeProvider());
				DynamicAttributeVO attrPlanend = goVO.getAttribute("[plan_end]", getAttributeProvider());
				DateTime datePlanend = (DateTime) attrPlanend.getValue();
				
				if (attrRealend != null){
					DateTime dateRealend = (DateTime) attrRealend.getValue();
					if (datePlanend.after(dateRealend)){
						return STATUS_ENDED_INTIME;
					} else {
						return STATUS_ENDED_DELAYED;
					}
				}
				
				if (datePlanend.after(now)){
					return STATUS_RUNNING_INTIME;
				} else {
					return STATUS_RUNNING_DELAYED;
				}
			}
			
			return STATUS_NOT_STARTED;

		} catch (CommonFinderException e) {
			throw new CommonFatalException(e);
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e);
		}
	}
	
	private static AttributeCache getAttributeProvider() {
		return AttributeCache.getInstance();
	}
	
	/**
	 * 
	 * @return
	 */
	private GeneratorFacadeLocal getGeneratorFacade() {
			if (generatorFacade == null)
				generatorFacade = ServiceLocator.getInstance().getFacade(GeneratorFacadeLocal.class);
		return generatorFacade;
	}
	
	/**
	 * 
	 * @return
	 */
	private ProcessMonitorFacadeLocal getProcessMonitorFacade() {
			if (processMonitorFacade == null)
				processMonitorFacade = ServiceLocator.getInstance().getFacade(ProcessMonitorFacadeLocal.class);
		return processMonitorFacade;
	}
}
