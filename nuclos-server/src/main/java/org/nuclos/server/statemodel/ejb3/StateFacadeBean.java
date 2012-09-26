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
package org.nuclos.server.statemodel.ejb3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;

import org.apache.log4j.Logger;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIsNullCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common.dblayer.JoinType;
import org.nuclos.common.entityobject.CollectableEOEntityField;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common.statemodel.Statemodel;
import org.nuclos.common.statemodel.StatemodelClosure;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.common.LocalCachesUtil;
import org.nuclos.server.common.LocaleUtils;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.common.SessionUtils;
import org.nuclos.server.common.StateCache;
import org.nuclos.server.common.ejb3.LocaleFacadeLocal;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.dal.DalSupportForGO;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.eventsupport.ejb3.EventSupportFacadeLocal;
import org.nuclos.server.eventsupport.valueobject.EventSupportTransitionVO;
import org.nuclos.server.genericobject.GenericObjectMetaDataCache;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.genericobject.ejb3.GenericObjectFacadeLocal;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.jms.NuclosJMSUtils;
import org.nuclos.server.masterdata.MasterDataWrapper;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMapImpl;
import org.nuclos.server.masterdata.valueobject.DependantWrapper;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;
import org.nuclos.server.masterdata.valueobject.RoleTransitionVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.ejb3.RuleEngineFacadeLocal;
import org.nuclos.server.ruleengine.valueobject.RuleEngineTransitionVO;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVOImpl;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVOImpl.Event;
import org.nuclos.server.statemodel.NuclosNoAdequateStatemodelException;
import org.nuclos.server.statemodel.NuclosSubsequentStateNotLegalException;
import org.nuclos.server.statemodel.valueobject.AttributegroupPermissionVO;
import org.nuclos.server.statemodel.valueobject.EntityFieldPermissionVO;
import org.nuclos.server.statemodel.valueobject.MandatoryColumnVO;
import org.nuclos.server.statemodel.valueobject.MandatoryFieldVO;
import org.nuclos.server.statemodel.valueobject.StateGraphVO;
import org.nuclos.server.statemodel.valueobject.StateHistoryVO;
import org.nuclos.server.statemodel.valueobject.StateModelLayout;
import org.nuclos.server.statemodel.valueobject.StateModelUsages;
import org.nuclos.server.statemodel.valueobject.StateModelUsages.StateModelUsage;
import org.nuclos.server.statemodel.valueobject.StateModelUsagesCache;
import org.nuclos.server.statemodel.valueobject.StateModelVO;
import org.nuclos.server.statemodel.valueobject.StateTransitionVO;
import org.nuclos.server.statemodel.valueobject.StateVO;
import org.nuclos.server.statemodel.valueobject.SubformColumnPermissionVO;
import org.nuclos.server.statemodel.valueobject.SubformPermissionVO;
import org.nuclos.server.validation.ValidationSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


/**
 * Facade bean for state management and state machine issues.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @todo restrict
 */
@Transactional(noRollbackFor= {Exception.class})
@RolesAllowed("Login")
public class StateFacadeBean extends NuclosFacadeBean implements StateFacadeRemote {
	
	private static final Logger LOG = Logger.getLogger(StateFacadeBean.class);

	private final static String STATE_TABLE = "t_md_state";

	private LocaleFacadeLocal localeFacade;
	
	private SessionUtils utils;
	
	private StateCache stateCache;
	
	private GenericObjectFacadeLocal genericObjectFacade;
	
	private MasterDataFacadeLocal masterDataFacade;
	
	private ValidationSupport validationSupport;
	
	public StateFacadeBean() {
	}
	
	@Autowired
	final void setSessionUtils(SessionUtils utils) {
		this.utils = utils;
	}
	
	@Autowired
	final void setStateCache(StateCache stateCache) {
		this.stateCache = stateCache;
	}

	@Autowired
	final void setGenericObjectFacade(GenericObjectFacadeLocal genericObjectFacade) {
		this.genericObjectFacade = genericObjectFacade;
	}
	
	private final MasterDataFacadeLocal getMasterDataFacade() {
		if (masterDataFacade == null) {
			masterDataFacade = ServerServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
		}
		return masterDataFacade;
	}
	
	private final LocaleFacadeLocal getLocaleFacade() {
		return localeFacade;
	}

	@Autowired
	final void setLocaleFacade(LocaleFacadeLocal localeFacade) {
		this.localeFacade = localeFacade;
	}
	
	@Autowired
	public void setValidationSupport(ValidationSupport validationSupport) {
		this.validationSupport = validationSupport;
	}

	/**
	 * gets a complete state graph for a state model
	 * @param iModelId id of state model to get graph for
	 * @return state graph cvo containing the state graph information for the model with the given id
	 * @throws CommonPermissionException
	 */
    public StateGraphVO getStateGraph(Integer iModelId) throws CommonFinderException, CommonPermissionException, NuclosBusinessException {
		checkReadAllowed(NuclosEntity.STATEMODEL);
		StateGraphVO result;

		//get state model
		StateModelVO stateModel = findStateModelById(iModelId);
		result = new StateGraphVO(stateModel);

		//get states (with attributegroup permissions) and transitions for state model
		Set<StateVO> ststatevo = new HashSet<StateVO>();
		Set<StateTransitionVO> sttransitionvo = new HashSet<StateTransitionVO>();
		for (StateVO statevo : stateCache.getStatesByModel(iModelId)) {
			StateVO.UserRights userRights = new StateVO.UserRights();
			for (AttributegroupPermissionVO permission : findAttributegroupPermissionsByStateId(statevo.getId()))
				userRights.addValue(permission.getRoleId(),permission);
			statevo.setUserRights(userRights);

			StateVO.UserFieldRights userFieldRights = new StateVO.UserFieldRights();
			for (EntityFieldPermissionVO permission : findEntityFieldPermissionsByStateId(statevo.getId()))
				userFieldRights.addValue(permission.getRoleId(),permission);
			statevo.setUserFieldRights(userFieldRights);

			StateVO.UserSubformRights userSubformRights = new StateVO.UserSubformRights();
			for (SubformPermissionVO permission : findSubformPermissionsByStateId(statevo.getId())) {
				permission.setColumnPermissions(findSubformColumnPermissionsBySubformPermission(permission.getId()));
				userSubformRights.addValue(permission.getRoleId(),permission);
			}
			statevo.setUserSubformRights(userSubformRights);

			statevo.setMandatoryFields(findMandatoryFieldsByStateId(statevo.getId()));
			statevo.setMandatoryColumns(findMandatoryColumnsByStateId(statevo.getId()));

			ststatevo.add(statevo);

			CollectableComparison cond = SearchConditionUtils.newMDReferenceComparison(
	      	MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.STATETRANSITION),"state2", statevo.getId());
			Collection<MasterDataVO> mdList = getMasterDataFacade().getMasterData(NuclosEntity.STATETRANSITION.getEntityName(), cond, true);

			for (MasterDataVO mdVO : mdList) {
				sttransitionvo.add(MasterDataWrapper.getStateTransitionVO(getMasterDataFacade().getWithDependants(NuclosEntity.STATETRANSITION.getEntityName(), mdVO.getIntId(), null)));
			}
		}
		result.setStates(ststatevo);
		result.setTransitions(sttransitionvo);

		return result;
	}

	/**
	 * method to insert, update or remove a complete state model in the database at once
	 * @param stategraphcvo state graph representation
	 * @return state model id
	 * @throws CommonPermissionException
	 */
    public Integer setStateGraph(StateGraphVO stategraphcvo, DependantMasterDataMap mpDependants) 
    		throws CommonCreateException, CommonFinderException, CommonRemoveException, CommonValidationException, 
    		CommonStaleVersionException, CommonPermissionException, NuclosBusinessRuleException {
    	
		Integer result;

		// check state graph for validity:
		StateModelVO statemodelvo = stategraphcvo.getStateModel();
		if (!statemodelvo.isRemoved()) {
			stategraphcvo.validate();	// throws CommonValidationException
		}

		// set state model:
		if (statemodelvo.getId() == null) {
			checkWriteAllowed(NuclosEntity.STATEMODEL);
			result = createStateGraph(stategraphcvo);
			if (mpDependants != null) {
				for (EntityObjectVO mdvoDependant : mpDependants.getAllData()) {
					mdvoDependant.getFieldIds().put("statemodel", IdUtils.toLongId(result));
				}
				getMasterDataFacade().modifyDependants(NuclosEntity.STATEMODEL.getEntityName(),result,stategraphcvo.getStateModel().isRemoved(),mpDependants, null);
			}
		}
		else {
			StateModelVO dbStateModel = findStateModelById(statemodelvo.getId());
			result = dbStateModel.getId();

		 	checkForStaleVersion(dbStateModel, statemodelvo);

			if (statemodelvo.isRemoved()) {
				// remove state model graph:
				checkDeleteAllowed(NuclosEntity.STATEMODEL);
				removeStateGraph(stategraphcvo, dbStateModel);
			}
			else {
				// update state model graph:
				checkWriteAllowed(NuclosEntity.STATEMODEL);
				updateStateGraph(stategraphcvo, dbStateModel);
			}

			if (mpDependants != null) {
				getMasterDataFacade().modifyDependants(NuclosEntity.STATEMODEL.getEntityName(),stategraphcvo.getStateModel().getId(),stategraphcvo.getStateModel().isRemoved(),mpDependants, null);
			}
		}

		if (mpDependants != null) {
			StateModelUsagesCache.getInstance().revalidate();
			
			for (EntityObjectVO eovo : mpDependants.getData(NuclosEntity.STATEMODELUSAGE.getEntityName())) {
				String sEntityName = (String)eovo.getField("nuclos_module");
				updateInitialStates(sEntityName);
			}
		}

		SecurityCache.getInstance().invalidate();
		
		return result;
	}

	private Integer createStateGraph(StateGraphVO stategraphvo) throws NuclosBusinessRuleException, CommonPermissionException, CommonCreateException, CommonFinderException {
		StateModelVO statemodelvo = stategraphvo.getStateModel();

		MasterDataVO mdVO = getMasterDataFacade().create(NuclosEntity.STATEMODEL.getEntityName(), MasterDataWrapper.wrapStateModelVO(statemodelvo), null, null);
		StateModelVO dbStateModel = MasterDataWrapper.getStateModelVO(mdVO);

		Map<Integer, Integer> mpStates = new HashMap<Integer, Integer>();
		Map<Integer, Integer> mpTransitions = new HashMap<Integer, Integer>();
		StateModelLayout layoutinfo = statemodelvo.getLayout();
		for (StateVO statevo : stategraphvo.getStates()) {
			if(!statevo.isRemoved()) {
				statevo.setModelId(mdVO.getIntId());
				MasterDataVO createdState = getMasterDataFacade().create(NuclosEntity.STATE.getEntityName(), MasterDataWrapper.wrapStateVO(statevo), null, null);
				mpStates.put(statevo.getClientId(), createdState.getIntId());		//prepare mapping table for state transition inserts/updates
				layoutinfo.updateStateId(statevo.getClientId(), mpStates.get(statevo.getClientId()));
				createUserRights(statevo, mpStates);
				createUserFieldRights(statevo, mpStates);
				createUserSubformRights(statevo, mpStates);
				createMandatoryFields(statevo, mpStates);
				createMandatoryColumns(statevo, mpStates);

				LocaleUtils.setResourceIdForField(STATE_TABLE, createdState.getIntId(), LocaleUtils.FIELD_LABEL, getLocaleFacade().setDefaultResource(null, statevo.getStatename()));
				LocaleUtils.setResourceIdForField(STATE_TABLE, createdState.getIntId(), LocaleUtils.FIELD_DESCRIPTION, getLocaleFacade().setDefaultResource(null, statevo.getDescription()));
				LocaleUtils.setResourceIdForField(STATE_TABLE, createdState.getIntId(), LocaleUtils.FIELD_BUTTON, getLocaleFacade().setDefaultResource(null, statevo.getButtonLabel()));
			}
		}
		for (StateTransitionVO statetransitionvo : stategraphvo.getTransitions()) {
			if(!statetransitionvo.isRemoved()) {
				StateTransitionVO newVO = createStateTransition(statetransitionvo,mpStates);

				//prepare mapping table for state transition inserts/updates
				mpTransitions.put(statetransitionvo.getClientId(),newVO.getId());

				int orderId = 1;
				for (Pair<Integer, Boolean> rule : statetransitionvo.getRuleIdsWithRunAfterwards()) {
					RuleEngineTransitionVO retVO = new RuleEngineTransitionVO(new NuclosValueObject(),statetransitionvo.getId(), rule.x, orderId, rule.y);
					getMasterDataFacade().create(NuclosEntity.RULETRANSITION.getEntityName(),MasterDataWrapper.wrapRuleEngineTransitionVO(retVO),null, null);
					orderId++;
				}

				layoutinfo.updateTransitionId(statetransitionvo.getClientId(), mpTransitions.get(statetransitionvo.getClientId()));
			}
		}

		// Speichern der korrekten State- und Transition-IDs der Layouts
		dbStateModel.setLayout(layoutinfo);

		try {
			getMasterDataFacade().modify(NuclosEntity.STATEMODEL.getEntityName(), MasterDataWrapper.wrapStateModelVO(dbStateModel), null, null);
		}
		catch (CommonValidationException ex) {
			throw new CommonFatalException(ex);
		}
		catch (CommonStaleVersionException ex) {
			throw new CommonFatalException(ex);
		}
		catch (CommonRemoveException ex) {
			throw new CommonFatalException(ex);
		}

		return dbStateModel.getId();
	}

	private void removeStateGraph(StateGraphVO stateGraphVO, StateModelVO stateModelVO) 
			throws CommonRemoveException, CommonFinderException, CommonPermissionException, NuclosBusinessRuleException, CommonStaleVersionException {

		// remove the transitions
		for (StateTransitionVO statetransitionvo : stateGraphVO.getTransitions())
			if (statetransitionvo.getId() != null)
				getMasterDataFacade().remove(NuclosEntity.STATETRANSITION.getEntityName(), MasterDataWrapper.wrapStateTransitionVO(statetransitionvo), false, null);

		// remove the states
		for (StateVO statevo : stateGraphVO.getStates()) {
			if (statevo.getId() != null) {
				getMasterDataFacade().remove(NuclosEntity.STATE.getEntityName(), MasterDataWrapper.wrapStateVO(statevo), false, null);
				getLocaleFacade().deleteResource(LocaleUtils.getResourceIdForField(STATE_TABLE, statevo.getId(), LocaleUtils.FIELD_LABEL));
				getLocaleFacade().deleteResource(LocaleUtils.getResourceIdForField(STATE_TABLE, statevo.getId(), LocaleUtils.FIELD_DESCRIPTION));
				getLocaleFacade().deleteResource(LocaleUtils.getResourceIdForField(STATE_TABLE, statevo.getId(), LocaleUtils.FIELD_BUTTON));
			}
		}

		stateCache.invalidate();

		// remove the model
		if (stateModelVO.getId() != null)
			getMasterDataFacade().remove(NuclosEntity.STATEMODEL.getEntityName(), MasterDataWrapper.wrapStateModelVO(stateModelVO), false, null);
	}

	private void updateStateGraph(StateGraphVO stategraphcvo, StateModelVO dbStateModel) throws CommonFinderException, CommonCreateException,
		NuclosBusinessRuleException, CommonPermissionException, CommonValidationException, CommonStaleVersionException, CommonRemoveException
	{
		StateModelVO statemodelvo = stategraphcvo.getStateModel();
		StateModelLayout layoutinfo = statemodelvo.getLayout();
		String sXmlLayout = statemodelvo.getXMLLayout();

		validateUniqueConstraint(statemodelvo, dbStateModel);


		// remove transitions:
		for(StateTransitionVO statetransitionvo : stategraphcvo.getTransitions()) {
			if(statetransitionvo.isRemoved() && statetransitionvo.getId() != null) {
				getMasterDataFacade().remove(NuclosEntity.STATETRANSITION.getEntityName(), MasterDataWrapper.wrapStateTransitionVO(statetransitionvo), true, null);
			}
			else if(statetransitionvo.getId() != null) {
				// As old states must be deleted before new states can be
				// inserted due to unique constraints on name an numeral, we
				// have to temporarily delete the state references from the
				// transitions. (Basically, we remove all transitions but keep
				// the objects for the dependent stuff like rights and rules)
				// The references will be re-inserted in the "update transitions"
				// part below
				MasterDataVO refClearMock = MasterDataWrapper.wrapStateTransitionVO(statetransitionvo);
				for(String field : new String[] {"state1", "state2", "state1Id", "state2Id"})
					refClearMock.setField(field, null);
				getMasterDataFacade().modify(NuclosEntity.STATETRANSITION.getEntityName(), refClearMock, null, null);
			}
		}

		// remove states:
		for (StateVO statevo : stategraphcvo.getStates()) {
			final Integer stateId = statevo.getId();
			
			if (statevo.isRemoved() && stateId != null) {
				/*
				 * http://support.novabit.de/browse/NUCLOSINT-1274
				 * 
				 * The hole old code here is problematic: As all relational tables 
				 * rights are retrieved without the id, the code here doesn't work.
				 * Instead we now use the code from the update branch that will 
				 * retrieve the relation table rights directly from the db. (tp) 
				 * 
				UserSubformRights rights = statevo.getUserSubformRights();
				for(Integer iKey : rights.asMap().keySet()) {
					for(SubformPermissionVO voPermission : rights.asMap().get(iKey)) {
						for(SubformColumnPermissionVO voColPermission : voPermission.getColumnPermissions()) {
							voColPermission.remove();
							getMasterDataFacade().remove(NuclosEntity.ROLESUBFORMCOLUMN.getEntityName(), MasterDataWrapper.wrapSubformColumnPermissionVO(voColPermission), false);
						}
						voPermission.remove();
						getMasterDataFacade().remove(NuclosEntity.ROLESUBFORM.getEntityName(), MasterDataWrapper.wrapSubformPermissionVO(voPermission), false);
					}
				}

				UserRights userRights = statevo.getUserRights();
				for(Integer iKey : userRights.asMap().keySet()) {
					for(AttributegroupPermissionVO voPermission : userRights.asMap().get(iKey)) {
						voPermission.remove();
						getMasterDataFacade().remove(NuclosEntity.ROLEATTRIBUTEGROUP.getEntityName(), MasterDataWrapper.wrapAttributegroupPermissionVO((voPermission)), false);
					}
				}

				UserFieldRights userFieldRights = statevo.getUserFieldRights();
				for(Integer iKey : userFieldRights.asMap().keySet()) {
					for(EntityFieldPermissionVO voPermission : userFieldRights.asMap().get(iKey)) {
						voPermission.remove();
						getMasterDataFacade().remove(NuclosEntity.ROLEENTITYFIELD.getEntityName(), MasterDataWrapper.wrapEntityFieldPermissionVO((voPermission)), false);
					}
				}
				*/
				for (MandatoryFieldVO vo : findMandatoryFieldsByStateId(stateId)) {
					getMasterDataFacade().remove(NuclosEntity.STATEMANDATORYFIELD.getEntityName(), MasterDataWrapper.wrapMandatoryFieldVO(vo), false, null);
				}
				for (MandatoryColumnVO vo : findMandatoryColumnsByStateId(stateId)) {
					getMasterDataFacade().remove(NuclosEntity.STATEMANDATORYCOLUMN.getEntityName(), MasterDataWrapper.wrapMandatoryColumnVO(vo), false, null);
				}

				for (AttributegroupPermissionVO vo : findAttributegroupPermissionsByStateId(stateId))
					getMasterDataFacade().remove(NuclosEntity.ROLEATTRIBUTEGROUP.getEntityName(), MasterDataWrapper.wrapAttributegroupPermissionVO(vo), false, null);

				for (EntityFieldPermissionVO vo : findEntityFieldPermissionsByStateId(stateId))
					getMasterDataFacade().remove(NuclosEntity.ROLEENTITYFIELD.getEntityName(), MasterDataWrapper.wrapEntityFieldPermissionVO(vo), false, null);

				for (SubformPermissionVO vo : findSubformPermissionsByStateId(stateId)) {
					for (SubformColumnPermissionVO colVO : findSubformColumnPermissionsBySubformPermission(vo.getId())) {
						getMasterDataFacade().remove(NuclosEntity.ROLESUBFORMCOLUMN.getEntityName(), MasterDataWrapper.wrapSubformColumnPermissionVO(colVO), false, null);
					}
					getMasterDataFacade().remove(NuclosEntity.ROLESUBFORM.getEntityName(), MasterDataWrapper.wrapSubformPermissionVO(vo), false, null);
				}

				/*
				for(MandatoryFieldVO mandatoryVO : statevo.getMandatoryFields()) {
					mandatoryVO.remove();
					getMasterDataFacade().remove(NuclosEntity.STATEMANDATORYFIELD.getEntityName(), MasterDataWrapper.wrapMandatoryFieldVO(mandatoryVO), false);
				}
				for(MandatoryColumnVO mandatoryVO : statevo.getMandatoryColumns()) {
					mandatoryVO.remove();
					getMasterDataFacade().remove(NuclosEntity.STATEMANDATORYCOLUMN.getEntityName(), MasterDataWrapper.wrapMandatoryColumnVO(mandatoryVO), false);
				}
				 */

				getMasterDataFacade().remove(NuclosEntity.STATE.getEntityName(), MasterDataWrapper.wrapStateVO(statevo), false, null);

				getLocaleFacade().deleteResource(LocaleUtils.getResourceIdForField(STATE_TABLE, statevo.getId(), LocaleUtils.FIELD_LABEL));
				getLocaleFacade().deleteResource(LocaleUtils.getResourceIdForField(STATE_TABLE, statevo.getId(), LocaleUtils.FIELD_DESCRIPTION));
				getLocaleFacade().deleteResource(LocaleUtils.getResourceIdForField(STATE_TABLE, statevo.getId(), LocaleUtils.FIELD_BUTTON));
			}
		}

		// update states:
		Map<Integer, Integer> mpStates = new HashMap<Integer, Integer>();
		for (StateVO statevo : stategraphcvo.getStates()) {
			if (!statevo.isRemoved()) {
				final Integer stateId = statevo.getId();
				if (stateId == null) {
					// insert state:
					statevo.setModelId(statemodelvo.getId());
					MasterDataVO createdState = getMasterDataFacade().create(NuclosEntity.STATE.getEntityName(), MasterDataWrapper.wrapStateVO(statevo), null, null);
					mpStates.put(statevo.getClientId(), createdState.getIntId());		//prepare mapping table for state transition inserts/updates
					layoutinfo.updateStateId(statevo.getClientId(), mpStates.get(statevo.getClientId()));
					LocaleUtils.setResourceIdForField(STATE_TABLE, createdState.getIntId(), LocaleUtils.FIELD_LABEL, getLocaleFacade().setDefaultResource(null, statevo.getStatename()));
					LocaleUtils.setResourceIdForField(STATE_TABLE, createdState.getIntId(), LocaleUtils.FIELD_DESCRIPTION, getLocaleFacade().setDefaultResource(null, statevo.getDescription()));
					LocaleUtils.setResourceIdForField(STATE_TABLE, createdState.getIntId(), LocaleUtils.FIELD_BUTTON, getLocaleFacade().setDefaultResource(null, statevo.getButtonLabel()));
				}
				else {
					// update state:
					mpStates.put(statevo.getClientId(), stateId);	//prepare mapping table for state transition inserts/updates

					updateState(statevo,statemodelvo);

					stateCache.invalidate();

					for (MandatoryFieldVO vo : findMandatoryFieldsByStateId(stateId)) {
						getMasterDataFacade().remove(NuclosEntity.STATEMANDATORYFIELD.getEntityName(), MasterDataWrapper.wrapMandatoryFieldVO(vo), false, null);
					}
					for (MandatoryColumnVO vo : findMandatoryColumnsByStateId(stateId)) {
						getMasterDataFacade().remove(NuclosEntity.STATEMANDATORYCOLUMN.getEntityName(), MasterDataWrapper.wrapMandatoryColumnVO(vo), false, null);
					}

					for (AttributegroupPermissionVO vo : findAttributegroupPermissionsByStateId(stateId))
						getMasterDataFacade().remove(NuclosEntity.ROLEATTRIBUTEGROUP.getEntityName(), MasterDataWrapper.wrapAttributegroupPermissionVO(vo), false, null);

					for (EntityFieldPermissionVO vo : findEntityFieldPermissionsByStateId(stateId))
						getMasterDataFacade().remove(NuclosEntity.ROLEENTITYFIELD.getEntityName(), MasterDataWrapper.wrapEntityFieldPermissionVO(vo), false, null);

					for (SubformPermissionVO vo : findSubformPermissionsByStateId(stateId)) {
						for (SubformColumnPermissionVO colVO : findSubformColumnPermissionsBySubformPermission(vo.getId())) {
							getMasterDataFacade().remove(NuclosEntity.ROLESUBFORMCOLUMN.getEntityName(), MasterDataWrapper.wrapSubformColumnPermissionVO(colVO), false, null);
						}
						getMasterDataFacade().remove(NuclosEntity.ROLESUBFORM.getEntityName(), MasterDataWrapper.wrapSubformPermissionVO(vo), false, null);
					}
				}
				createUserRights(statevo, mpStates);
				createUserFieldRights(statevo, mpStates);
				createUserSubformRights(statevo, mpStates);
				createMandatoryFields(statevo, mpStates);
				createMandatoryColumns(statevo, mpStates);
			}
		}

		// update transitions:
		Map<Integer, Integer> mpTransitions = new HashMap<Integer, Integer>();
		for (StateTransitionVO statetransitionvo : stategraphcvo.getTransitions()) {
			if (!statetransitionvo.isRemoved()) {
				if (statetransitionvo.getId() == null) {
					// insert transition:
					StateTransitionVO newVO = createStateTransition(statetransitionvo,mpStates);
					mpTransitions.put(statetransitionvo.getClientId(), newVO.getId());		//prepare mapping table for state transition inserts/updates
					layoutinfo.updateTransitionId(statetransitionvo.getClientId(), mpTransitions.get(statetransitionvo.getClientId()));
				}
				else {
					// update transition:
					MasterDataVO mdVO = getMasterDataFacade().get(NuclosEntity.STATETRANSITION.getEntityName(), statetransitionvo.getId());
					StateTransitionVO transitionVO = MasterDataWrapper.getStateTransitionVOWithoutDependants(mdVO);

					Integer iStateSource = statetransitionvo.getStateSource();
					if (iStateSource != null && iStateSource.intValue() < 0) {	//newly inserted state referenced?
						statetransitionvo.setStateSource(mpStates.get(iStateSource));				//map newly inserted state temp id to real state id
					}
					Integer iStateTarget = statetransitionvo.getStateTarget();
					if (iStateTarget.intValue() < 0) {														//newly inserted state referenced?
						statetransitionvo.setStateTarget(mpStates.get(iStateTarget));				//map newly inserted state temp id to real state id
					}

					transitionVO.setAutomatic(statetransitionvo.isAutomatic());
					transitionVO.setDefault(statetransitionvo.isDefault());
					transitionVO.setDescription(statetransitionvo.getDescription());
					transitionVO.setStateSource(statetransitionvo.getStateSource());
					transitionVO.setStateTarget(statetransitionvo.getStateTarget());
					getMasterDataFacade().modify(NuclosEntity.STATETRANSITION.getEntityName(), MasterDataWrapper.wrapStateTransitionVO(transitionVO),
						createStateTransitionDependants(statetransitionvo), null);
				}
			}
		}

		stateCache.invalidate();

		dbStateModel.setName(statemodelvo.getName());
		dbStateModel.setDescription(statemodelvo.getDescription());
		dbStateModel.setLayout(layoutinfo);
		dbStateModel.setXMLLayout(sXmlLayout);

		getMasterDataFacade().modify(NuclosEntity.STATEMODEL.getEntityName(), MasterDataWrapper.wrapStateModelVO(dbStateModel), null, null);
	}

	private void updateInitialStates(String sEntityName) throws CommonCreateException {
		if (Modules.getInstance().isModuleEntity(sEntityName)) {
			Integer iModuleId =	Modules.getInstance().getModuleIdByEntityName(sEntityName);
			final StateModelUsage stateModelUsage
				= StateModelUsagesCache.getInstance().getStateUsages().getStateModelUsage(new UsageCriteria(iModuleId, null, null, null));
			if (stateModelUsage != null) {
				StateVO stateVO = stateCache.getState(stateModelUsage.getInitialStateId());
				if (stateVO != null) {
					CollectableSearchCondition cond = new CollectableIsNullCondition(
							new CollectableEOEntityField(NuclosEOField.STATE.getMetaData(), sEntityName));
					List<Integer> genvos = genericObjectFacade.getGenericObjectIds(iModuleId, cond);
					for (Integer id : genvos) {
						try {
							EntityObjectVO dalVO = DalSupportForGO.getEntityObjectProcessor(iModuleId).getByPrimaryKey(IdUtils.toLongId(id));
		
							dalVO.getFieldIds().put(NuclosEOField.STATE.getMetaData().getField(), IdUtils.toLongId(stateVO.getId()));

							dalVO.getFields().put(NuclosEOField.STATE.getMetaData().getField(), stateVO.getStatename());
							dalVO.getFields().put(NuclosEOField.STATEICON.getMetaData().getField(), stateVO.getIcon());
							dalVO.getFields().put(NuclosEOField.STATENUMBER.getMetaData().getField(), stateVO.getNumeral());
		
							dalVO.flagUpdate();
							DalSupportForGO.getEntityObjectProcessor(iModuleId).insertOrUpdate(dalVO);
						}
						catch (DbException e) {
							throw new CommonCreateException(e);
						}
					}
				}
			}
		}
	}

	private void updateState(StateVO clientStateVO, StateModelVO modelVO) throws CommonFinderException, CommonPermissionException, NuclosBusinessRuleException, CommonCreateException, CommonRemoveException, CommonStaleVersionException, CommonValidationException {
		StateVO dbStateVO = MasterDataWrapper.getStateVO(getMasterDataFacade().get(NuclosEntity.STATE.getEntityName(), clientStateVO.getId()));

		// if the state name or the state numeral has changed make all attributes which have references
		// on this state consistent
		Map<String, Object> mpChangedFields = new HashMap<String, Object>();
		if(clientStateVO.getNumeral().compareTo(dbStateVO.getNumeral()) != 0) {
			mpChangedFields.put(NuclosEOField.STATENUMBER.getMetaData().getField(), dbStateVO.getNumeral());
		}
		if (clientStateVO.getIcon() != dbStateVO.getIcon()) {
			mpChangedFields.put(NuclosEOField.STATEICON.getMetaData().getField(), dbStateVO.getIcon());
		}
		if (!clientStateVO.getStatename().equals(dbStateVO.getStatename())) {
			mpChangedFields.put(NuclosEOField.STATE.getMetaData().getField(), dbStateVO.getStatename());
		}
		// not needed/available
		// ServerServiceLocator.getInstance().getFacade(AttributeFacadeLocal.class).makeConsistent(NuclosEntity.STATE.getEntityName(), dbStateVO.getId(), mpChangedFields);

		dbStateVO.setNumeral(clientStateVO.getNumeral());
		dbStateVO.setStatename(clientStateVO.getStatename());
		dbStateVO.setIcon(clientStateVO.getIcon());
		dbStateVO.setDescription(clientStateVO.getDescription());
		dbStateVO.setModelId(modelVO.getId());
		dbStateVO.setTabbedPaneName(clientStateVO.getTabbedPaneName());
		dbStateVO.setColor(clientStateVO.getColor());
		dbStateVO.setButtonIcon(clientStateVO.getButtonIcon());
		
		String labelResId = LocaleUtils.getResourceIdForField(STATE_TABLE, dbStateVO.getId(), LocaleUtils.FIELD_LABEL);
		String descriptionResId = LocaleUtils.getResourceIdForField(STATE_TABLE, dbStateVO.getId(), LocaleUtils.FIELD_DESCRIPTION);
		String buttonLabelResId = LocaleUtils.getResourceIdForField(STATE_TABLE, dbStateVO.getId(), LocaleUtils.FIELD_BUTTON);
		
		MasterDataVO mdvo = MasterDataWrapper.wrapStateVO(dbStateVO);
		mdvo.setField("labelres", labelResId);
		mdvo.setField("descriptionres", descriptionResId);
		mdvo.setField("labelRes", buttonLabelResId);
		getMasterDataFacade().modify(NuclosEntity.STATE.getEntityName(), mdvo, null, null);

		if (labelResId != null) {
			getLocaleFacade().setResourceForLocale(labelResId, getLocaleFacade().getUserLocale(), dbStateVO.getStatename());
		} else {
			LocaleUtils.setResourceIdForField(STATE_TABLE, dbStateVO.getId(), LocaleUtils.FIELD_LABEL, getLocaleFacade().setDefaultResource(null, dbStateVO.getStatename()));
		}
		
		if (descriptionResId != null) {
			getLocaleFacade().setResourceForLocale(descriptionResId, getLocaleFacade().getUserLocale(), dbStateVO.getDescription());
		} else {
			LocaleUtils.setResourceIdForField(STATE_TABLE, dbStateVO.getId(), LocaleUtils.FIELD_DESCRIPTION, getLocaleFacade().setDefaultResource(null, dbStateVO.getDescription()));
		}
		
		if (buttonLabelResId != null) {
			getLocaleFacade().setResourceForLocale(buttonLabelResId, getLocaleFacade().getUserLocale(), clientStateVO.getButtonLabel());
		} else {
			LocaleUtils.setResourceIdForField(STATE_TABLE, dbStateVO.getId(), LocaleUtils.FIELD_BUTTON, getLocaleFacade().setDefaultResource(null, clientStateVO.getButtonLabel()));
		}
	}

    private void validateUniqueConstraint(StateModelVO statemodelvo, StateModelVO dbStateModel) throws CommonValidationException, CommonPermissionException {
		try {
			StateModelVO vo = findStateModelByName(dbStateModel.getName());
			if(statemodelvo.getId().intValue() != vo.getId().intValue()){
				throw new CommonValidationException(
					StringUtils.getParameterizedExceptionMessage("validation.unique.constraint", "Name", "State model"));
			}
		} catch (CommonFinderException e) {
			// No element found -> validation O.K.
		}
	}

	private void createUserRights(StateVO statevo, Map<Integer, Integer> mpStates) throws NuclosBusinessRuleException, CommonPermissionException, CommonCreateException {
		for (Integer iRoleId : statevo.getUserRights().keySet()) {
			for (AttributegroupPermissionVO attrgrouppermissionvo : statevo.getUserRights().getValues(iRoleId)) {
				AttributegroupPermissionVO permissionVO = new AttributegroupPermissionVO(attrgrouppermissionvo.getAttributegroupId(), null,
					iRoleId, null, mpStates.get(statevo.getClientId()) ,null, attrgrouppermissionvo.isWritable());
				getMasterDataFacade().create(NuclosEntity.ROLEATTRIBUTEGROUP.getEntityName(), MasterDataWrapper.wrapAttributegroupPermissionVO(permissionVO), null, null);
			}
		}
	}

	private void createMandatoryFields(StateVO statevo, Map<Integer, Integer> mpStates) throws NuclosBusinessRuleException, CommonPermissionException, CommonCreateException {
		for (MandatoryFieldVO mandatoryVO : statevo.getMandatoryFields()) {
			getMasterDataFacade().create(NuclosEntity.STATEMANDATORYFIELD.getEntityName(), MasterDataWrapper.wrapMandatoryFieldVO(
				new MandatoryFieldVO(mandatoryVO.getFieldId(), mpStates.get(statevo.getClientId()))), null, null);
		}
	}

	private void createMandatoryColumns(StateVO statevo, Map<Integer, Integer> mpStates) throws NuclosBusinessRuleException, CommonPermissionException, CommonCreateException {
		for (MandatoryColumnVO mandatoryVO : statevo.getMandatoryColumns()) {
			getMasterDataFacade().create(NuclosEntity.STATEMANDATORYCOLUMN.getEntityName(), MasterDataWrapper.wrapMandatoryColumnVO(
				new MandatoryColumnVO(mandatoryVO.getEntity(), mandatoryVO.getColumn(), mpStates.get(statevo.getClientId()))), null, null);
		}
	}

	private void createUserFieldRights(StateVO statevo, Map<Integer, Integer> mpStates) throws NuclosBusinessRuleException, CommonPermissionException, CommonCreateException {
		for (Integer iRoleId : statevo.getUserFieldRights().keySet()) {
			for (EntityFieldPermissionVO entityfieldpermissionvo : statevo.getUserFieldRights().getValues(iRoleId)) {
				EntityFieldPermissionVO permissionVO = new EntityFieldPermissionVO(entityfieldpermissionvo.getFieldId(),
					iRoleId, mpStates.get(statevo.getClientId()), entityfieldpermissionvo.isReadable(), entityfieldpermissionvo.isWriteable());
				getMasterDataFacade().create(NuclosEntity.ROLEENTITYFIELD.getEntityName(), MasterDataWrapper.wrapEntityFieldPermissionVO(permissionVO), null, null);
			}
		}
	}

	private void createUserSubformRights(StateVO statevo, Map<Integer, Integer> mpStates) throws NuclosBusinessRuleException, CommonPermissionException, CommonCreateException {
		for(Integer iRoleId : statevo.getUserSubformRights().keySet()) {
			for(SubformPermissionVO subformpermissionvo : statevo.getUserSubformRights().getValues(iRoleId)) {
				SubformPermissionVO permissionVO = new SubformPermissionVO(subformpermissionvo.getSubform(),
					iRoleId, null, mpStates.get(statevo.getClientId()) ,null, subformpermissionvo.isWriteable(), subformpermissionvo.getColumnPermissions());
				LOG.info("SubformPermissionVO... stateId=" + permissionVO.getStateId() + " roleId=" + permissionVO.getRoleId() 
						+ " subform=" + permissionVO.getSubform());
				MasterDataVO createdRoleSubformPermission = getMasterDataFacade().create(
						NuclosEntity.ROLESUBFORM.getEntityName(), MasterDataWrapper.wrapSubformPermissionVO(permissionVO), null, null);
				for (SubformColumnPermissionVO subformcolumnpermissionvo : subformpermissionvo.getColumnPermissions()) {
					getMasterDataFacade().create(NuclosEntity.ROLESUBFORMCOLUMN.getEntityName(), MasterDataWrapper.wrapSubformColumnPermissionVO(
						new SubformColumnPermissionVO(
							subformcolumnpermissionvo.getRoleSubformId() == null ? createdRoleSubformPermission.getIntId() : subformcolumnpermissionvo.getRoleSubformId(),
							subformcolumnpermissionvo.getColumn(), subformcolumnpermissionvo.isWriteable())), null, null);
				}
			}
		}
	}

	/**
	 * method to remove a complete state model with all usages in the database at once
	 * @param statemodelvo state model value object
	 * @throws CommonPermissionException
	 */
    public void removeStateGraph(StateModelVO statemodelvo) 
    		throws CommonFinderException, CommonRemoveException, CommonStaleVersionException, CommonPermissionException, 
    		NuclosBusinessRuleException, NuclosBusinessException {
    	
		checkDeleteAllowed(NuclosEntity.STATEMODEL);
		try {
			StateModelVO dbStateModel = findStateModelById(statemodelvo.getId());

			checkForStaleVersion(dbStateModel, statemodelvo);

			// get and delete state graph:
			StateGraphVO stategraphcvo = getStateGraph(statemodelvo.getId());
			stategraphcvo.getStateModel().remove();
			setStateGraph(stategraphcvo, null);
		}
		catch (CommonCreateException ex) {
			throw new NuclosFatalException(ex);
		}
		catch (CommonValidationException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	private void checkForStaleVersion(StateModelVO dbStateModel, StateModelVO clientStateModel) throws CommonStaleVersionException {
		if (dbStateModel.getVersion() != clientStateModel.getVersion()) {
			throw new CommonStaleVersionException("state model", clientStateModel.toString(), dbStateModel.toString());
		}
	}

	/**
	 * @param usagecriteria
	 * @return the initial state of the statemodel corresponding to <code>usagecriteria</code>.
	 */
    public StateVO getInitialState(UsageCriteria usagecriteria) {
		return stateCache.getState(getInitialStateId(usagecriteria));
	}

	/**
	 * method to return the initial state for a given generic object
	 * @param iGenericObjectId id of leased object to get initial state for
	 * @return state id of initial state for given generic object
	 */
    public StateVO getInitialState(Integer iGenericObjectId) throws NuclosFatalException {
		UsageCriteria usagecriteria = getRelevantStateUsageCritera(iGenericObjectId);
		return getInitialState(usagecriteria);
		// @todo We need to make sure there is exactly one initial state for each state model. This must be verified when storing a state model.
	}

	/**
	 * @param usagecriteria
	 * @return the id of the initial state of the statemodel corresponding to <code>usagecriteria</code>.
	 */
    @RolesAllowed("Login")
	public Integer getInitialStateId(UsageCriteria usagecriteria) {
		return StateModelUsagesCache.getInstance().getStateUsages().getInitialStateId(usagecriteria);
	}

	/**
	 * @param usagecriteria
	 * @return the id of the statemodel corresponding to <code>usagecriteria</code>.
	 */
    @RolesAllowed("Login")
	public Integer getStateModelId(UsageCriteria usagecriteria) {
		return StateModelUsagesCache.getInstance().getStateUsages().getStateModel(usagecriteria);
	}

    
    /**
     * @param stateId
     * @return
     */
    public StateVO getState(Integer stateId) {
    	return stateCache.getState(stateId);
    }
	/**
	 * @param iStateModelId
	 * @return the id of the statemodel corresponding to <code>usagecriteria</code>.
	 */
    @RolesAllowed("Login")
	public Collection<StateVO> getStatesByModel(Integer iStateModelId) {
		return stateCache.getStatesByModel(iStateModelId);
	}

	/**
	 * method to get all state models
	 * @return collection of state model vo
	 */
    public Collection<StateModelVO> getStateModels() {
		Collection<StateModelVO> result = new HashSet<StateModelVO>();
		Collection<MasterDataVO> mdVOList = ServerServiceLocator.getInstance().getFacade(
				MasterDataFacadeLocal.class).getMasterData(NuclosEntity.STATEMODEL.getEntityName(), null, true);
		for (MasterDataVO mdVO : mdVOList) {
			result.add(MasterDataWrapper.getStateModelVO(mdVO));
		}
		return result;
	}

	/**
	 * method to return the sorted list of state history entries for a given leased object
	 * @param iModuleId id of module for plausibility check
	 * @param iGenericObjectId id of leased object
	 * @return set of state history entries
	 * @nucleus.permission mayRead(module)
	 */
    @RolesAllowed("Login")
	public Collection<StateHistoryVO> getStateHistory(Integer iModuleId, Integer iGenericObjectId)
			throws CommonFinderException, CommonPermissionException {

		if (!genericObjectFacade.isGenericObjectInModule(iModuleId, iGenericObjectId)) {
			throw new IllegalArgumentException();
		}
		checkReadAllowedForModule(iModuleId, iGenericObjectId);

		return findStateHistoryByGenericObjectId(iGenericObjectId);
	}

	private UsageCriteria getRelevantStateUsageCritera(Integer iGenericObjectId) {
		GenericObjectVO govo;
		try {
			govo = ServerServiceLocator.getInstance().getFacade(GenericObjectFacadeLocal.class).get(iGenericObjectId);
		}
		catch (CommonFinderException ex) {
			throw new NuclosFatalException(ex);
		}
		catch (CommonPermissionException ex) {
			throw new NuclosFatalException(ex);
		}

		return govo.getUsageCriteria(AttributeCache.getInstance(), null);
	}

	/**
	 * Retrieve all states in all state models for the module with the given id
	 * @param iModuleId id of module to retrieve states for
	 * @return Collection of all states for the given module
	 */
    @RolesAllowed("Login")
	public Collection<StateVO> getStatesByModule(Integer iModuleId) {
		return stateCache.getStatesByModule(iModuleId);
	}

	/**
	 * method to return the actual state for a given leased object
	 * @param iModuleId module id for plausibility check
	 * @param iGenericObjectId id of leased object to get actual state for
	 * @return state id of actual state for given leased object
	 * @nucleus.permission mayRead(module)
	 */
    @RolesAllowed("Login")
	public StateVO getCurrentState(Integer iModuleId, Integer iGenericObjectId) throws CommonFinderException {

		Long iActualState = DalSupportForGO.getEntityObject(iGenericObjectId, iModuleId).getFieldIds().get(NuclosEOField.STATE.getMetaData().getField());

		return (iActualState == null) ? null : stateCache.getState(iActualState.intValue());
	}

	/**
	 * method to return the possible subsequent states for a given leased object
	 * @param iModuleId module id for plausibility check
	 * @param iGenericObjectId id of leased object to get subsequent states for
	 * @param bGetAutomaticStatesAlso should method also return states for automatic only transitions? false for returning subsequent states to client, which generates buttons for manual state changes
	 * @return set of possible subsequent states for given leased object
	 * @nucleus.permission mayRead(module)
	 */
    @RolesAllowed("Login")
	public Collection<StateVO> getSubsequentStates(Integer iModuleId, Integer iGenericObjectId, boolean bGetAutomaticStatesAlso)
			throws NuclosNoAdequateStatemodelException, CommonFinderException {

		Collection<StateVO> result = new HashSet<StateVO>();
		StateVO statevoCurrent = getCurrentState(iModuleId, iGenericObjectId);
		if (statevoCurrent == null) {
			//if there is no current state, then subsequent state equals to initial state
			result.add(stateCache.getState(getInitialState(iGenericObjectId).getId()));
		}
		else {
			Collection<StateTransitionVO> collStates = bGetAutomaticStatesAlso ?
				findStateTransitionBySourceState(statevoCurrent.getId()) :
					findStateTransitionBySourceStateNonAutomatic(statevoCurrent.getId());

			Collection<Integer> collTransitionIds = SecurityCache.getInstance().getTransitionIds(utils.getCurrentUserName());
			for (StateTransitionVO stateTransition : collStates) {
				if (bGetAutomaticStatesAlso || collTransitionIds.contains(stateTransition.getId()))
				{ //may transition be retrieved by actual user?
					result.add(stateCache.getState(stateTransition.getStateTarget()));
				}
			}
		}
		return result;
	}

	/**
	 * method to change the status of a given leased object (called by server only)
	 * @param iModuleId module id for plausibility check
	 * @param iGenericObjectId leased object id to change status for
	 * @param iNumeral legal subsequent status numeral to set for given leased object
	 */
    public void changeStateByRule(Integer iModuleId, Integer iGenericObjectId, int iNumeral, String customUsage)
			throws NuclosNoAdequateStatemodelException, NuclosSubsequentStateNotLegalException,
			NuclosBusinessException, CommonFinderException, CommonPermissionException, CommonCreateException {

		changeState(iModuleId, iGenericObjectId, getTargetStateId(iModuleId, iGenericObjectId, iNumeral), customUsage);
	}

	/**
	 * method to change the status of a given leased object (called by server only)
	 * @param iModuleId module id for plausibility check
	 * @param iGenericObjectId leased object id to change status for
	 * @param iTargetStateId legal subsequent status id to set for given leased object
	 */
    public void changeStateByRule(Integer iModuleId, Integer iGenericObjectId, Integer iTargetStateId, String customUsage)
			throws NuclosNoAdequateStatemodelException, NuclosSubsequentStateNotLegalException,
			NuclosBusinessException, CommonFinderException, CommonPermissionException, CommonCreateException {

		changeState(iModuleId, iGenericObjectId, iTargetStateId, true, customUsage);
	}
    
    public void enterInitialState(GenericObjectVO goVO, DependantWrapper depWrapper, String customUsage) throws NuclosBusinessException, NuclosNoAdequateStatemodelException, CommonCreateException, CommonPermissionException {
    	Integer iInitialStateId = getInitialState(goVO.getId()).getId();
    	changeState(Integer.valueOf(goVO.getModuleId()), goVO, depWrapper, iInitialStateId, customUsage);
    }

	/**
	 * method to change the status of a given leased object
	 * @param iModuleId module id for plausibility check
	 * @param iGenericObjectId leased object id to change status for
	 * @param iTargetStateId legal subsequent status id to set for given leased object
	 * @nucleus.permission mayWrite(module)
	 */
    @RolesAllowed("Login")
	public void changeStateByUser(Integer iModuleId, Integer iGenericObjectId, Integer iTargetStateId, String customUsage)
			throws NuclosBusinessException, CommonPermissionException, CommonPermissionException, CommonCreateException,
			NuclosNoAdequateStatemodelException, NuclosSubsequentStateNotLegalException, CommonFinderException {

		checkWriteAllowedForModule(iModuleId, iGenericObjectId);
		changeState(iModuleId, iGenericObjectId, iTargetStateId, false, customUsage);
	}

	/**
	 * method to modify and change state of a given object
	 * @param iModuleId module id for plausibility check
	 * @param govo object to change status for
	 * @param iTargetStateId legal subsequent status id to set for given object
	 * @nucleus.permission mayWrite(module)
	 */
    @RolesAllowed("Login")
	public void changeStateAndModifyByUser(Integer iModuleId,
		GenericObjectWithDependantsVO gowdvo, Integer iTargetStateId, String customUsage)
		throws NuclosBusinessException, CommonPermissionException, CommonPermissionException, CommonCreateException,
		NuclosNoAdequateStatemodelException, NuclosSubsequentStateNotLegalException, CommonFinderException, CommonRemoveException,
		CommonStaleVersionException, CommonValidationException, CommonFatalException {

		checkWriteAllowedForModule(iModuleId, gowdvo.getId());
		genericObjectFacade.modify(iModuleId, gowdvo, customUsage);
		changeState(iModuleId, gowdvo.getId(), iTargetStateId, false, customUsage);
	}

	private StateTransitionVO createStateTransition(StateTransitionVO vo, Map<Integer, Integer> states) throws CommonPermissionException, CommonFinderException, NuclosBusinessRuleException, CommonCreateException {
		if (vo.getStateSource() != null && vo.getStateSource().intValue() < 0)	//newly inserted state referenced?
			vo.setStateSource(states.get(vo.getStateSource()));						//map newly inserted state temp id to real state id
		if (vo.getStateTarget().intValue() < 0)											//newly inserted state referenced?
			vo.setStateTarget(states.get(vo.getStateTarget()));						//map newly inserted state temp id to real state id

		final DependantMasterDataMap dependants = new DependantMasterDataMapImpl();
		final String entity = NuclosEntity.RULETRANSITION.getEntityName();

		int order = 1;
		for (Pair<Integer, Boolean> rule : vo.getRuleIdsWithRunAfterwards()) {
			RuleEngineTransitionVO ruleTransitionVO = new RuleEngineTransitionVO(new NuclosValueObject(),vo.getId(),rule.x,order++,rule.y);
			dependants.addData(entity,
				DalSupportForMD.getEntityObjectVO(entity, MasterDataWrapper.wrapRuleEngineTransitionVO(ruleTransitionVO)));
		}
		for (Integer id : vo.getRoleIds()) {
			RoleTransitionVO roleTransitionVO = new RoleTransitionVO(new NuclosValueObject(),vo.getId(),id);
			dependants.addData(NuclosEntity.ROLETRANSITION.getEntityName(),
				DalSupportForMD.getEntityObjectVO(entity, MasterDataWrapper.wrapRoleTransitionVO(roleTransitionVO)));
		}

		MasterDataVO mdVO = getMasterDataFacade().create(NuclosEntity.STATETRANSITION.getEntityName(), MasterDataWrapper.wrapStateTransitionVO(vo), dependants, null);
		return MasterDataWrapper.getStateTransitionVO(new MasterDataWithDependantsVO(mdVO,dependants));
	}

	private DependantMasterDataMap createStateTransitionDependants(StateTransitionVO vo) {
		DependantMasterDataMapImpl dependants = new DependantMasterDataMapImpl();

		// --- create RuleEngineTransitions ---
		Collection<RuleEngineTransitionVO> dbRules;
		Collection<EventSupportTransitionVO> dbESTrans;
		
		try {
			dbRules = ServerServiceLocator.getInstance().getFacade(RuleEngineFacadeLocal.class).getAllRuleTransitionsForTransitionId(vo.getId());
			dbESTrans = ServerServiceLocator.getInstance().getFacade(EventSupportFacadeLocal.class).getEventSupportsByTransitionId(vo.getId());
		}
		catch(Exception e) {
			throw new CommonFatalException(e);
		}

		List<Integer> dbRuleIds = new ArrayList<Integer>();
		List<Integer> dbESTransitionIds = new ArrayList<Integer>();
		
		List<Pair<Integer, Boolean>> clientRules = vo.getRuleIdsWithRunAfterwards();
		List<Pair< EventSupportTransitionVO, Boolean>> clientEventSupports = vo.getEventSupportWithRunAfterwards();
		
		int order = 1;

		final String ruleTransition = NuclosEntity.RULETRANSITION.getEntityName();
		final String eventSupportTransition = NuclosEntity.EVENTSUPPORTTRANSITION.getEntityName();
		
		for (Iterator<RuleEngineTransitionVO> iter = dbRules.iterator(); iter.hasNext();) {
			RuleEngineTransitionVO retVO = iter.next();
			dbRuleIds.add(retVO.getRuleId());
			//order = Math.max(order, retVO.getOrder());
			MasterDataVO mdVO = MasterDataWrapper.wrapRuleEngineTransitionVO(retVO);
			//remove all old rules for transition because or new ordering
			//if (!clientRuleIds.contains(retVO.getRuleId())) {
				mdVO.remove();
			//}
			dependants.addData(ruleTransition, 
					DalSupportForMD.getEntityObjectVO(ruleTransition, mdVO));
		}

		for (Iterator<EventSupportTransitionVO> iterES = dbESTrans.iterator();iterES.hasNext();) {
			EventSupportTransitionVO nextestVO = iterES.next();	
			dbESTransitionIds.add(nextestVO.getId());
			MasterDataVO mdVO = MasterDataWrapper.wrapEventSupportTransitionVO(nextestVO);
			mdVO.remove();
			
			dependants.addData(eventSupportTransition, 
					DalSupportForMD.getEntityObjectVO(eventSupportTransition, mdVO));
		}
		
		//add all new rules for transition because or new ordering
		//clientRuleIds.removeAll(dbRuleIds);

		// Rules
		for (Pair<Integer, Boolean> rule : clientRules) {
			RuleEngineTransitionVO ruleTransitionVO = new RuleEngineTransitionVO(new NuclosValueObject(),vo.getId(),rule.x,order++,rule.y);
			dependants.addData(ruleTransition,
				DalSupportForMD.getEntityObjectVO(ruleTransition, 
						MasterDataWrapper.wrapRuleEngineTransitionVO(ruleTransitionVO)));
		}

		// Eventsupports
		for (Pair<EventSupportTransitionVO, Boolean> rule : clientEventSupports) {
			dependants.addData(eventSupportTransition,
				DalSupportForMD.getEntityObjectVO(eventSupportTransition, 
						MasterDataWrapper.wrapEventSupportTransitionVO(rule.getX())));
		}
	
		// --- create RoleTransitions ---
		Collection<RoleTransitionVO> dbRoles;
		try {
			dbRoles = getAllRoleTransitionsForTransitionId(vo.getId());
		}
		catch(CommonPermissionException e) {
			throw new CommonFatalException(e);
		}

		List<Integer> dbRoleIds = new ArrayList<Integer>();
		List<Integer> clientRoleIds = vo.getRoleIds();

		final String roleTransition = NuclosEntity.ROLETRANSITION.getEntityName();
		for (Iterator<RoleTransitionVO> iter = dbRoles.iterator(); iter.hasNext();) {
			RoleTransitionVO rtVO = iter.next();
			dbRoleIds.add(rtVO.getRoleId());
			MasterDataVO mdVO = MasterDataWrapper.wrapRoleTransitionVO(rtVO);
			if (!vo.getRoleIds().contains(rtVO.getRoleId())) {
				mdVO.remove();
			}
			dependants.addData(roleTransition, DalSupportForMD.getEntityObjectVO(roleTransition, mdVO));
		}

		clientRoleIds.removeAll(dbRoleIds);

		for (Integer id : clientRoleIds) {
			RoleTransitionVO roleTransitionVO = new RoleTransitionVO(new NuclosValueObject(),vo.getId(),id);
			dependants.addData(roleTransition,
				DalSupportForMD.getEntityObjectVO(roleTransition,
						MasterDataWrapper.wrapRoleTransitionVO(roleTransitionVO)));
		}

		return dependants;
	}

	private Collection<RoleTransitionVO> getAllRoleTransitionsForTransitionId(Integer transitionId) throws CommonPermissionException {
      checkReadAllowed(NuclosEntity.ROLE);
      Collection<RoleTransitionVO> result = new HashSet<RoleTransitionVO>();

      CollectableComparison cond = SearchConditionUtils.newMDReferenceComparison(
         MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.ROLETRANSITION),"transition", transitionId);
      Collection<MasterDataVO> mdVOList = getMasterDataFacade().getMasterData(NuclosEntity.ROLETRANSITION.getEntityName(), cond, true);

      for (MasterDataVO mdVO : mdVOList)
         result.add(MasterDataWrapper.getRoleTransitionVO(mdVO));

      return result;
   }

	/**
	 * returns a StateTransitionVO for the given transitionId
	 */
	@Deprecated
    public StateTransitionVO findStateTransitionById(Integer transitionId)
	{
		CollectableComparison cond = SearchConditionUtils.newMDComparison(
      	MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.STATETRANSITION),"id", ComparisonOperator.EQUAL, transitionId);

		return findStateTransition(cond,true,false).iterator().next();
	}

	/**
	 * returns a StateTransitionVO for the given sourceStateId
	 */
    public Collection<StateTransitionVO> findStateTransitionBySourceState(Integer sourceStateId)
	{
		CollectableComparison cond = SearchConditionUtils.newMDReferenceComparison(
      	MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.STATETRANSITION),"state1", sourceStateId);

		return findStateTransition(cond,false,false);
	}

	/**
	 * returns a StateTransitionVO for the given sourceStateId without automatic
	 */
    public Collection<StateTransitionVO> findStateTransitionBySourceStateNonAutomatic(Integer sourceStateId)
	{
		CollectableComparison condSource = SearchConditionUtils.newMDReferenceComparison(
      	MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.STATETRANSITION),"state1", sourceStateId);
		CollectableComparison condAuto = SearchConditionUtils.newMDComparison(
      	MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.STATETRANSITION),"automatic", ComparisonOperator.EQUAL, false);

		return findStateTransition(new CompositeCollectableSearchCondition(LogicalOperator.AND, CollectionUtils.asSet(condSource,condAuto)),false,false);
	}

	/**
	 * returns a StateTransitionVO for the given targetStateId without a sourceStateId
	 */
    public StateTransitionVO findStateTransitionByNullAndTargetState(Integer targetStateId)
	{
		CollectableEntity entity = new CollectableMasterDataEntity(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.STATETRANSITION));

		CollectableComparison condTarget = SearchConditionUtils.newMDReferenceComparison(
      	MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.STATETRANSITION),"state2", targetStateId);

		Set<CollectableSearchCondition> conditions = new HashSet<CollectableSearchCondition>();
		conditions.add(new CollectableIsNullCondition(entity.getEntityField("state1")));
		conditions.add(condTarget);

		Collection<StateTransitionVO> stateTransitions =  findStateTransition(
			new CompositeCollectableSearchCondition(LogicalOperator.AND,conditions),true,true);

		return stateTransitions.isEmpty() ? null : stateTransitions.iterator().next();
	}

	/**
	 * returns a StateTransitionVO for the given sourceStateId and targetStateId
	 */
    public StateTransitionVO findStateTransitionBySourceAndTargetState(Integer sourceStateId, Integer targetStateId)
	{
		CollectableComparison condSource = SearchConditionUtils.newMDReferenceComparison(
      	MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.STATETRANSITION),"state1", sourceStateId);
		CollectableComparison condTarget = SearchConditionUtils.newMDReferenceComparison(
      	MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.STATETRANSITION),"state2", targetStateId);

		Collection<StateTransitionVO> stateTransitions =  findStateTransition(
			new CompositeCollectableSearchCondition(LogicalOperator.AND,CollectionUtils.asSet(condSource,condTarget)),true,false);

		return stateTransitions.isEmpty() ? null : stateTransitions.iterator().next();
	}

	private Collection<StateTransitionVO> findStateTransition(CollectableSearchCondition cond, boolean checkUniqueness, boolean withDependants) {
		List<StateTransitionVO> result = new ArrayList<StateTransitionVO>();

		if (withDependants) {
			Collection<MasterDataWithDependantsVO> mdList = getMasterDataFacade().getWithDependantsByCondition(NuclosEntity.STATETRANSITION.getEntityName(), cond, null);

			if (mdList != null && !mdList.isEmpty()) {
		      if (checkUniqueness && mdList.size() > 1)
		      	throw new CommonFatalException("statemodel.error.notunique.transition");
		      		//"Es wurde mehr als eine Transition gefunden, obwohl eine Eindeutigkeit gegeben sein m\u00fcsste.");

		      for (MasterDataWithDependantsVO mdVO : mdList) {
		      	result.add(MasterDataWrapper.getStateTransitionVO(mdVO));
		      }
			}
		} else {
			Collection<MasterDataVO> mdList = getMasterDataFacade().getMasterData(NuclosEntity.STATETRANSITION.getEntityName(), cond, true);

			if (mdList != null && !mdList.isEmpty()) {
		      if (checkUniqueness && mdList.size() > 1)
		      	throw new CommonFatalException("statemodel.error.notunique.transition");
		      		//"Es wurde mehr als eine Transition gefunden, obwohl eine Eindeutigkeit gegeben sein m\u00fcsste.");

		      for (MasterDataVO mdVO : mdList) {
		      	result.add(MasterDataWrapper.getStateTransitionVOWithoutDependants(mdVO));
		      }
			}
		}
      return result;
	}

	/**
	 * returns the StateModelVO for the given Id
	 */
    public StateModelVO findStateModelById(Integer id) throws CommonPermissionException, CommonFinderException
	{
		return MasterDataWrapper.getStateModelVO(getMasterDataFacade().get(NuclosEntity.STATEMODEL.getEntityName(), id));
	}

	/**
	 * returns the StateModelVO for the given statemodel-name
	 */
    public StateModelVO findStateModelByName(String name) throws CommonPermissionException, CommonFinderException
	{
		CollectableComparison cond = SearchConditionUtils.newMDComparison(
      	MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.STATEMODEL),"name", ComparisonOperator.EQUAL, name);
      Collection<MasterDataVO> mdStateModelsVO = getMasterDataFacade().getMasterData(NuclosEntity.STATEMODEL.getEntityName(), cond, true);

      if (mdStateModelsVO != null && mdStateModelsVO.size() > 0)
      	 return MasterDataWrapper.getStateModelVO(mdStateModelsVO.iterator().next());

      return null;
	}

	/**
	 * returns a Collection of StateModelVO which contains rule-transitions with the given ruleId
	 */
    public Collection<StateModelVO> findStateModelsByRuleId(Integer ruleId) throws CommonPermissionException
	{
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom s = query.from("T_MD_STATE").alias("s");
		DbFrom t = s.join("T_MD_STATE_TRANSITION", JoinType.INNER).alias(SystemFields.BASE_ALIAS).on("INTID", "INTID_T_MD_STATE_2", Integer.class);
		DbFrom rt = t.join("T_MD_RULE_TRANSITION", JoinType.INNER).alias("rt").on("INTID", "INTID_T_MD_STATE_TRANSITION", Integer.class);
		query.select(s.baseColumn("INTID_T_MD_STATEMODEL", Integer.class));
		query.where(builder.equal(rt.baseColumn("INTID_T_MD_RULE", Integer.class), ruleId));

		List<StateModelVO> statemodels = new ArrayList<StateModelVO>();

		try {
			for (Integer id : dataBaseHelper.getDbAccess().executeQuery(query.distinct(true))) {
				statemodels.add(findStateModelById(id));
			}
		}
		catch (CommonFinderException ex) {
			// Dateninkonsistenz?
			throw new CommonFatalException(ex);
		}
		return statemodels;
	}

	/**
	 * returns a Collection of StateHistories for the given genericObjectId
	 */
    public Collection<StateHistoryVO> findStateHistoryByGenericObjectId(Integer genericObjectId)
	{
		List<StateHistoryVO> histories = new ArrayList<StateHistoryVO>();

		MasterDataMetaVO metaVO = MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.STATEHISTORY);
		CollectableComparison cond = SearchConditionUtils.newMDReferenceComparison(metaVO,"genericObject", genericObjectId);
      Collection<MasterDataVO> stateHistories = getMasterDataFacade().getMasterData(NuclosEntity.STATEHISTORY.getEntityName(), cond, true);

   	for (MasterDataVO mdVO : stateHistories)
   		histories.add(MasterDataWrapper.getStateHistoryVO(mdVO));

      return histories;
	}

	private void changeState(Integer iModuleId, Integer iGenericObjectId, Integer iTargetStateId, boolean bGetAutomaticStatesAlso, String customUsage)
			throws NuclosNoAdequateStatemodelException, NuclosSubsequentStateNotLegalException, NuclosBusinessException, CommonFinderException, CommonPermissionException, CommonCreateException {
		// @todo OPTIMIZE: Must this be done here?
		checkTargetState(iModuleId, iGenericObjectId, bGetAutomaticStatesAlso, iTargetStateId);

		changeState(iModuleId, iGenericObjectId, iTargetStateId, customUsage);
	}

	/**
	 * method to change the status of a given leased object
	 * @param iModuleId module id for plausibility check
	 * @param iGenericObjectId leased object id to change status for
	 * @param iTargetStateId legal subsequent status id to set for given leased object
	 */
	private void changeState(Integer iModuleId, Integer iGenericObjectId, Integer iTargetStateId, String customUsage)
			throws NuclosBusinessException, CommonFinderException, CommonCreateException, CommonPermissionException {
		
		GenericObjectFacadeLocal goFacade = ServerServiceLocator.getInstance().getFacade(GenericObjectFacadeLocal.class);
		RuleObjectContainerCVO loccvoBefore = goFacade.getRuleObjectContainerCVO(Event.CHANGE_STATE_BEFORE, iGenericObjectId, customUsage);
		changeState(iModuleId, loccvoBefore, new DependantWrapper(), iTargetStateId, customUsage);
	}
	
	private void changeState(Integer iModuleId, GenericObjectVO govo, DependantWrapper depWrapper, Integer iTargetStateId, String customUsage) 
			throws NuclosBusinessException, CommonCreateException, CommonPermissionException {
			
		RuleObjectContainerCVO loccvoBefore = new RuleObjectContainerCVOImpl(Event.CHANGE_STATE_BEFORE, govo, depWrapper.getDependants());
		changeState(iModuleId, loccvoBefore, depWrapper, iTargetStateId, customUsage);
	}
	
	private void changeState(Integer iModuleId, RuleObjectContainerCVO loccvoBefore, DependantWrapper depWrapper, Integer iTargetStateId, String customUsage)
		throws NuclosBusinessException, CommonCreateException, CommonPermissionException {
		// get source state id for later rule identification:
//		StateVO statevo = getCurrentState(iModuleId, iGenericObjectId);
//		Integer iSourceStateId = (statevo == null) ? null : statevo.getId();

		String entity = MetaDataServerProvider.getInstance().getEntity(iModuleId.longValue()).getEntity();
		Integer iAttributeStateId = MetaDataServerProvider.getInstance().getEntityField(entity, NuclosEOField.STATE.getName()).getId().intValue();
		Integer iAttributeStatenumberId = MetaDataServerProvider.getInstance().getEntityField(entity, NuclosEOField.STATENUMBER.getName()).getId().intValue();
		GenericObjectVO govo = loccvoBefore.getGenericObject();
		DynamicAttributeVO attrState = govo.getAttribute(iAttributeStateId);
		DynamicAttributeVO attrStatenumber = govo.getAttribute(iAttributeStatenumberId);
		Integer iSourceStateId = attrState == null? null: attrState.getValueId();
		
		StateVO stateVO = stateCache.getState(iTargetStateId);

		// change the status of leased object now:
		getMasterDataFacade().create(NuclosEntity.STATEHISTORY.getEntityName(),
			MasterDataWrapper.wrapStateHistoryVO(new StateHistoryVO(govo.getId(),iTargetStateId,
					stateCache.getState(iTargetStateId).getStatename())), null, null);

		// copy status name to leased object attributes:
		try {
//			final GenericObjectVO go = genericObjectFacade.get(iGenericObjectId);
			final GenericObjectMetaDataCache prov = GenericObjectMetaDataCache.getInstance();
			
			if (attrState == null) {
				attrState = new DynamicAttributeVO(iAttributeStateId, null, null);
				govo.addAndSetAttribute(attrState);
			}
			if (attrStatenumber == null) {
				attrStatenumber = new DynamicAttributeVO(iAttributeStatenumberId, null, null);
				govo.addAndSetAttribute(attrStatenumber);
			}
//			final DynamicAttributeVO state = go.getAttribute(NuclosEOField.STATE.getMetaData().getField(), prov);
			attrState.setParsedValue(stateVO.getStatename(), prov);
			attrState.setValueId(stateVO.getId());
			// goFacade.setAttribute(iGenericObjectId, NuclosEOField.STATE.getMetaData().getField(), stateVO.getId(), stateVO.getStatename());
			
//			final DynamicAttributeVO statenumber = go.getAttribute(NuclosEOField.STATENUMBER.getMetaData().getField(), prov);
			attrStatenumber.setParsedValue(stateVO.getNumeral(), prov);
			attrStatenumber.setValueId(stateVO.getId());
			// goFacade.setAttribute(iGenericObjectId, NuclosEOField.STATENUMBER.getMetaData().getField(), stateVO.getId(), stateVO.getNumeral());
			
			govo = genericObjectFacade.modify(govo, null, false);
			loccvoBefore.setGenericObject(govo);
		}
		catch (CommonValidationException ex) {
			throw new NuclosFatalException(ex);
		}
		catch (CommonStaleVersionException ex) {
			throw new NuclosFatalException(ex);
		}
		catch (CommonRemoveException ex) {
			throw new NuclosFatalException(ex);
		}
		catch (CommonFinderException ex) {
			throw new NuclosFatalException(ex);
		}

		// handle instance "state change"
//		ServerServiceLocator.getInstance().getFacade(InstanceFacadeLocal.class).notifyInstanceAboutStateChange(iGenericObjectId, iTargetStateId);

		// fire rules attached to the executed state transition:
		fireStateChangedEvent(loccvoBefore, depWrapper, iSourceStateId, iTargetStateId, customUsage);
	}

	/**
	 * checks if the given target state id is contained in the list of subsequent states for the given leased objects:
	 * @param iModuleId
	 * @param iGenericObjectId
	 * @param iTargetStateId
	 * @throws NuclosNoAdequateStatemodelException
	 * @throws NuclosSubsequentStateNotLegalException
	 */
	public boolean checkTargetState(Integer iModuleId, Integer iGenericObjectId, Integer iTargetStateId)
			throws NuclosNoAdequateStatemodelException, CommonFinderException, CommonPermissionException {
		try {
			checkTargetState(iModuleId, iGenericObjectId, false, iTargetStateId);
		} catch (NuclosSubsequentStateNotLegalException e) {
			return false;
		}
		return true;
	}



	/**
	 * checks if the given target state id is contained in the list of subsequent states for the given leased objects:
	 * @param iModuleId
	 * @param iGenericObjectId
	 * @param bGetAutomaticStatesAlso
	 * @param iTargetStateId
	 * @throws NuclosNoAdequateStatemodelException
	 * @throws NuclosSubsequentStateNotLegalException
	 */
	private void checkTargetState(Integer iModuleId, Integer iGenericObjectId, boolean bGetAutomaticStatesAlso,
			Integer iTargetStateId)
			throws NuclosNoAdequateStatemodelException, NuclosSubsequentStateNotLegalException, CommonFinderException, CommonPermissionException {

		boolean bLegal = false;

		for (StateVO statevo : getSubsequentStates(iModuleId, iGenericObjectId, bGetAutomaticStatesAlso)) {
			Integer iStateId = statevo.getId();
			if (iStateId.compareTo(iTargetStateId) == 0) {
				bLegal = true;
				break;
			}
		}
		if (!bLegal) {
			throw new NuclosSubsequentStateNotLegalException("Requested target state id " + iTargetStateId + " is not a legal subsequent state for genericobject id " + iGenericObjectId);
		}
	}

	private Integer getTargetStateId(Integer iModuleId, Integer iGenericObjectId, int iNumeral)
			throws NuclosNoAdequateStatemodelException, NuclosSubsequentStateNotLegalException, CommonFinderException, CommonPermissionException {

		Integer result = null;

		for (StateVO statevo : getSubsequentStates(iModuleId, iGenericObjectId, true)) {
			Integer iStateID = statevo.getId();
			if ((statevo.getNumeral() != null) && (statevo.getNumeral().equals(new Integer(iNumeral)))) {
				result = iStateID;
				break;
			}
		}
		if (result == null) {
			throw new NuclosSubsequentStateNotLegalException("Requested target state numeral " + iNumeral + " is not a legal subsequent state for genericobject id " + iGenericObjectId);
		}
		return result;
	}

	private void fireStateChangedEvent(RuleObjectContainerCVO loccvoBefore, DependantWrapper depWrapper, Integer iSourceStateId, Integer iTargetStateId, String customUsage)
			throws NuclosBusinessRuleException {

		GenericObjectFacadeLocal goFacade = ServerServiceLocator.getInstance().getFacade(GenericObjectFacadeLocal.class);

		try {
			RuleEngineFacadeLocal facade = ServerServiceLocator.getInstance().getFacade(RuleEngineFacadeLocal.class);
			EventSupportFacadeLocal evtSupFacade = ServerServiceLocator.getInstance().getFacade(EventSupportFacadeLocal.class);
			
			final StateVO targetState = iTargetStateId==null? null: stateCache.getState(iTargetStateId);
			final Integer iTargetStateNum = targetState==null? null: targetState.getNumeral();
			final String sTargetStateName = targetState==null? null: targetState.getStatename();
			final StateVO sourceState = iSourceStateId==null? null: stateCache.getState(iSourceStateId);
			final Integer iSourceStateNum = sourceState==null? null: sourceState.getNumeral();
			final String sSourceStateName = sourceState==null? null: sourceState.getStatename();

			loccvoBefore.setTargetStateId(iTargetStateId);
			loccvoBefore.setTargetStateNum(iTargetStateNum);
			loccvoBefore.setTargetStateName(sTargetStateName);
			loccvoBefore.setSourceStateId(iSourceStateId);
			loccvoBefore.setSourceStateNum(iSourceStateNum);
			loccvoBefore.setSourceStateName(sSourceStateName);

			RuleObjectContainerCVO loccvoAfter = facade.fireRule(iSourceStateId, iTargetStateId, loccvoBefore, false, customUsage);
			loccvoAfter = evtSupFacade.fireStateTransitionEventSupport(iSourceStateId, iTargetStateId, loccvoAfter, false);
			depWrapper.setDependants(loccvoAfter.getDependants(true));
			
			
			// check mandatory fields and subform columns
			EntityObjectVO validation = DalSupportForGO.wrapGenericObjectVO(loccvoAfter.getGenericObject());
			validation.getFieldIds().put(NuclosEOField.STATE.getName(), IdUtils.toLongId(iTargetStateId));
			validationSupport.validate(validation, null);

			// Write back the possibly changed GenericObjectVO along with its dependants.
			// We deliberately allow changing the GenericObjectVO from the rule in "state change" events
			// so the rule writer doesn't have to distinguish them from other events (like "save").
			// Note that the modification of the leased object does not fire another save event here:
			GenericObjectVO modifiedGoVO = goFacade.modify(loccvoAfter.getGenericObject(), depWrapper.getDependants(), false, false, false, customUsage);

			RuleObjectContainerCVO loccvoAfterModified = new RuleObjectContainerCVOImpl(Event.CHANGE_STATE_AFTER, modifiedGoVO, loccvoAfter.getDependants());
			loccvoAfterModified.setTargetStateId(iTargetStateId);
			loccvoAfterModified.setTargetStateNum(iTargetStateNum);
			loccvoAfterModified.setTargetStateName(sTargetStateName);
			loccvoAfterModified.setSourceStateId(iSourceStateId);
			loccvoAfterModified.setSourceStateNum(iSourceStateNum);
			loccvoAfterModified.setSourceStateName(sSourceStateName);

			loccvoAfterModified = facade.fireRule(iSourceStateId, iTargetStateId, loccvoAfterModified, true, null);
			depWrapper.setDependants(evtSupFacade.fireStateTransitionEventSupport(iSourceStateId, iTargetStateId, loccvoAfterModified, true).getDependants(true));

		}
		catch (Exception ex) {
			throw new NuclosBusinessRuleException(ex.getMessage(), ex);
		}
	}

	private Collection<AttributegroupPermissionVO> findAttributegroupPermissionsByStateId(Integer stateId) {
		List<AttributegroupPermissionVO> permissions = new ArrayList<AttributegroupPermissionVO>();

		CollectableComparison cond = SearchConditionUtils.newMDReferenceComparison(
      	MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.ROLEATTRIBUTEGROUP),"state", stateId);
      Collection<MasterDataVO> mdPermissions = getMasterDataFacade().getMasterData(NuclosEntity.ROLEATTRIBUTEGROUP.getEntityName(), cond, true);

      for (MasterDataVO mdVO : mdPermissions)
      	permissions.add(MasterDataWrapper.getAttributegroupPermissionVO(mdVO));

      return permissions;
	}

	private Collection<EntityFieldPermissionVO> findEntityFieldPermissionsByStateId(Integer stateId) {
		List<EntityFieldPermissionVO> permissions = new ArrayList<EntityFieldPermissionVO>();

		CollectableComparison cond = SearchConditionUtils.newMDReferenceComparison(
      	MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.ROLEENTITYFIELD),"state", stateId);
      Collection<MasterDataVO> mdPermissions = getMasterDataFacade().getMasterData(NuclosEntity.ROLEENTITYFIELD.getEntityName(), cond, true);

      for (MasterDataVO mdVO : mdPermissions)
      	permissions.add(MasterDataWrapper.getEntityFieldPermissionVO(mdVO));

      return permissions;
	}

	private Collection<SubformPermissionVO> findSubformPermissionsByStateId(Integer stateId) {
		List<SubformPermissionVO> permissions = new ArrayList<SubformPermissionVO>();

		CollectableComparison cond = SearchConditionUtils.newMDReferenceComparison(
      	MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.ROLESUBFORM),"state", stateId);
      Collection<MasterDataVO> mdPermissions = getMasterDataFacade().getMasterData(NuclosEntity.ROLESUBFORM.getEntityName(), cond, true);

      for (MasterDataVO mdVO : mdPermissions)
      	permissions.add(MasterDataWrapper.getSubformPermissionVO(mdVO));

      return permissions;
	}

	private Set<SubformColumnPermissionVO> findSubformColumnPermissionsBySubformPermission(Integer roleSubformId) {
		Set<SubformColumnPermissionVO> permissions = new HashSet<SubformColumnPermissionVO>();

		CollectableComparison cond = SearchConditionUtils.newMDReferenceComparison(
      	MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.ROLESUBFORMCOLUMN),"rolesubform", roleSubformId);
      Collection<MasterDataVO> mdPermissions = getMasterDataFacade().getMasterData(NuclosEntity.ROLESUBFORMCOLUMN.getEntityName(), cond, true);

      for (MasterDataVO mdVO : mdPermissions)
      	permissions.add(MasterDataWrapper.getSubformColumnPermissionVO(mdVO));

      return permissions;
	}

	public Set<MandatoryFieldVO> findMandatoryFieldsByStateId(Integer stateId) {
		Set<MandatoryFieldVO> mandatory = new HashSet<MandatoryFieldVO>();

		CollectableComparison cond = SearchConditionUtils.newMDReferenceComparison(
      	MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.STATEMANDATORYFIELD),"state", stateId);
      Collection<MasterDataVO> mdPermissions = getMasterDataFacade().getMasterData(NuclosEntity.STATEMANDATORYFIELD.getEntityName(), cond, true);

      for (MasterDataVO mdVO : mdPermissions)
    	  mandatory.add(MasterDataWrapper.getMandatoryFieldVO(mdVO));

      return mandatory;
	}

	public Set<MandatoryColumnVO> findMandatoryColumnsByStateId(Integer stateId) {
		Set<MandatoryColumnVO> mandatory = new HashSet<MandatoryColumnVO>();

		CollectableComparison cond = SearchConditionUtils.newMDReferenceComparison(
      	MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.STATEMANDATORYCOLUMN),"state", stateId);
      Collection<MasterDataVO> mdPermissions = getMasterDataFacade().getMasterData(NuclosEntity.STATEMANDATORYCOLUMN.getEntityName(), cond, true);

      for (MasterDataVO mdVO : mdPermissions)
    	  mandatory.add(MasterDataWrapper.getMandatoryColumnVO(mdVO));

      return mandatory;
	}

    public void invalidateCache() {
		LOG.info("invalidatesCache: state cache and state model usages cache invalidated/revalidated");
		stateCache.invalidate();
		StateModelUsagesCache.getInstance().revalidate();

		LOG.info("JMS send: notify clients that state models changed:" + this);
		LocalCachesUtil.getInstance().updateLocalCacheRevalidation(JMSConstants.TOPICNAME_STATEMODEL);
		NuclosJMSUtils.sendOnceAfterCommitDelayed(null, JMSConstants.TOPICNAME_STATEMODEL);
	}

    public String getResourceSIdForName(Integer iStateId) {
		return LocaleUtils.getResourceIdForField(STATE_TABLE, iStateId, LocaleUtils.FIELD_LABEL);
	}

    public String getResourceSIdForDescription(Integer iStateId) {
		return LocaleUtils.getResourceIdForField(STATE_TABLE, iStateId, LocaleUtils.FIELD_DESCRIPTION);
	}
    
    public String getResourceSIdForButtonLabel(Integer iStateId) {
		return LocaleUtils.getResourceIdForField(STATE_TABLE, iStateId, LocaleUtils.FIELD_BUTTON);
	}

	public Statemodel getStatemodel(UsageCriteria usageCriteria) {
		Statemodel res = new Statemodel(usageCriteria);
		res.setAllStates(stateCache.getStatesByModule(usageCriteria.getModuleId()));
		res.setInitialStateId(getInitialStateId(usageCriteria));
		for(Integer stateId : res.getStateIDs()) {
			res.setTransitionsForState(stateId, findStateTransitionBySourceState(stateId));
			res.setResourceSIDsForState(stateId, getResourceSIdForName(stateId), getResourceSIdForDescription(stateId));
		}
		res.setUserTransitionIDs(SecurityCache.getInstance().getTransitionIds(utils.getCurrentUserName()));
		assert(res.isComplete());
		return res;
	}

	public StatemodelClosure getStatemodelClosureForModule(Integer moduleId) {
		StatemodelClosure res = new StatemodelClosure(moduleId);
		StateModelUsages stateUsages = StateModelUsagesCache.getInstance().getStateUsages();
		List<Integer> stateModelsForModule = stateUsages.getStateModelIdsByModuleId(moduleId);
		for(Integer modelId : stateModelsForModule) {
			List<UsageCriteria> usageCrits = stateUsages.getUsageCriteriaByStateModelId(modelId);
			for(UsageCriteria uc : usageCrits)
				if(uc.getModuleId().equals(moduleId))
					res.addStatemodel(uc, getStatemodel(uc));
		}
		return res;
	}
	
	public List<StateTransitionVO> getOrderedStateTransitionsByStatemodel(Integer moduleId)
	{
		List<StateTransitionVO> retVal = new ArrayList<StateTransitionVO>();
		
		StateTransitionVO initialTransistionByModel = stateCache.getInitialTransistionByModel(moduleId);
		retVal.add(initialTransistionByModel);
		
		Integer curTransStateInChain = initialTransistionByModel.getStateTarget();
		boolean completeTransitionChain = false;
		
		while (!completeTransitionChain)
		{
			CollectableComparison cond = SearchConditionUtils.newMDReferenceComparison(
		      	MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.STATETRANSITION),"state1", curTransStateInChain);
			Collection<MasterDataVO> mdList = getMasterDataFacade().getMasterData(NuclosEntity.STATETRANSITION.getEntityName(), cond, true);

			if (mdList.isEmpty())
			{
				completeTransitionChain = true;
			}
			else
			{
				for (MasterDataVO mdVO : mdList) {
					try {
						StateTransitionVO newStateTrans = MasterDataWrapper.getStateTransitionVOWithoutDependants(mdVO);
						retVal.add(newStateTrans);
						curTransStateInChain = newStateTrans.getStateTarget();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return retVal;
	}

}
