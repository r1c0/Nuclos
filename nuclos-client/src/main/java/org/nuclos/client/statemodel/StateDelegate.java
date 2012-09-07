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
package org.nuclos.client.statemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Message;
import javax.jms.MessageListener;

import org.nuclos.client.LocalUserCaches.AbstractLocalUserCache;
import org.nuclos.client.gef.shapes.AbstractShape;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.jms.TopicNotificationReceiver;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common.statemodel.Statemodel;
import org.nuclos.common.statemodel.StatemodelClosure;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.statemodel.NuclosNoAdequateStatemodelException;
import org.nuclos.server.statemodel.NuclosSubsequentStateNotLegalException;
import org.nuclos.server.statemodel.ejb3.StateFacadeRemote;
import org.nuclos.server.statemodel.valueobject.StateGraphVO;
import org.nuclos.server.statemodel.valueobject.StateHistoryVO;
import org.nuclos.server.statemodel.valueobject.StateModelLayout;
import org.nuclos.server.statemodel.valueobject.StateModelVO;
import org.nuclos.server.statemodel.valueobject.StateTransitionVO;
import org.nuclos.server.statemodel.valueobject.StateVO;
import org.nuclos.server.statemodel.valueobject.TransitionLayout;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Business Delegate for <code>StateFacadeBean</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public class StateDelegate extends AbstractLocalUserCache implements MessageListener, DisposableBean {
	
	private static StateDelegate INSTANCE;
	
	//
	
	private transient Map<Integer, StateGraphVO> mpStateGraphVO;
	private transient Map<Integer, StatemodelClosure> mpStatemodelClosure;
	
	// Spring injection
	
	private transient StateFacadeRemote stateFacadeRemote;
	
	private transient TopicNotificationReceiver tnr;

	
	// end of Spring injection

	StateDelegate() {
		INSTANCE = this;
	}
	
	public final void setTopicNotificationReceiver(TopicNotificationReceiver tnr) {
		this.tnr = tnr;
	}

	public final void setStateFacadeRemote(StateFacadeRemote stateFacadeRemote) {
		this.stateFacadeRemote = stateFacadeRemote;
	}

	@Override
	public void afterPropertiesSet() {
		// Constructor might not be called - as this instance might be deserialized (tp)
		if (INSTANCE == null) {
			INSTANCE = this;
		}
		mpStateGraphVO = new ConcurrentHashMap<Integer, StateGraphVO>();
		mpStatemodelClosure = new ConcurrentHashMap<Integer, StatemodelClosure>();
		
		if (!wasDeserialized() || !isValid()) {
			// we could not do a complete invalidation here.
			// statemodels and graphs needs the current user name to get allowed transition.
			// so we can not remember statemodels and transitions here. this depends on user. load it lazy as it was bevor. 
			//invalidate(); 
		}
		tnr.subscribe(getCachingTopic(), this);
	}
	
	public void invalidate() {
		mpStateGraphVO = new ConcurrentHashMap<Integer, StateGraphVO>();
		for (StateModelVO smvo : stateFacadeRemote.getStateModels()) {
			try {
				mpStateGraphVO.put(smvo.getId(), getStateGraph(smvo.getId()));				
			} catch (Exception e) {
				// ignore here.
			}
		}
		mpStatemodelClosure = new ConcurrentHashMap<Integer, StatemodelClosure>();
		for (MasterDataVO mdvo : Modules.getInstance().getModules()) {
			mpStatemodelClosure.put(mdvo.getIntId(), stateFacadeRemote.getStatemodelClosureForModule(mdvo.getIntId()));
		}
	}
	
	@Override
	public String getCachingTopic() {
		return JMSConstants.TOPICNAME_STATEMODEL;
	}

	public static StateDelegate getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}

	@Override
	public void onMessage(Message arg0) {
		invalidate();
	}


	/**
	 * @param iGenericObjectId
	 * @return the state history of the given leased object. The list is sorted by date, ascending.
	 * @throws NuclosFatalException
	 * @postcondition result != null
	 * @see StateFacadeRemote#getStateHistory(Integer, Integer)
	 */
	public List<StateHistoryVO> getStateHistory(int iModuleId, int iGenericObjectId)
			throws CommonPermissionException, CommonFinderException {
		try {
			final List<StateHistoryVO> result = new ArrayList<StateHistoryVO>(
					stateFacadeRemote.getStateHistory(iModuleId, iGenericObjectId));

			// sort by date, ascending:
			Collections.sort(result, new Comparator<StateHistoryVO>() {
				@Override
                public int compare(StateHistoryVO sh1, StateHistoryVO sh2) {
					return sh1.getCreatedAt().compareTo(sh2.getCreatedAt());
				}
			});

			assert result != null;
			return result;
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 *
	 * @param iModuleId
	 * @return Collection<StateVO> of all states in all state models for the given module
	 */
	public Collection<StateVO> getStatesByModule(Integer iModuleId) {
		return getStatemodelClosure(iModuleId).getAllStates();
	}
	
	/**
	 * 
	 * @param iModuleId
	 * @param iStateId
	 * @return
	 */
	public StateVO getState(Integer iModuleId, Integer iStateId) {
		return getStatemodelClosure(iModuleId).getState(iStateId);
	}

	/**
	 *
	 * @param iModuleId
	 * @return Collection<StateVO> of all states in all state models for the given module
	 */
	public Collection<StateVO> getStatesByModel(Integer iStateModelId) {
		try {
			return stateFacadeRemote.getStatesByModel(iStateModelId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 *
	 * @param iModuleId
	 * @return Collection<StateVO> of all states in all state models for the given module
	 */
	public StateVO getStatesByModel(Integer iStateModelId, Integer iStateId) {
		StateVO retVal = null;
		
		try {
			for(StateVO sVO : stateFacadeRemote.getStatesByModel(iStateModelId))
			{
				if (sVO.getId().equals(iStateId))
				{
					retVal = sVO;
					break;
				}
			}
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
		
		return retVal;
	}

	
	/**
	 * @param targetStateId
	 * @return
	 */
	public StateTransitionVO getStateTransitionByNullAndTargetState(Integer targetStateId)
	{
		return stateFacadeRemote.findStateTransitionByNullAndTargetState(targetStateId);		
	}
	
	
	public StateTransitionVO getStateTransitionBySourceAndTargetState(Integer sourceStateId, Integer targetStateId)
	{
		return stateFacadeRemote.findStateTransitionBySourceAndTargetState(sourceStateId, targetStateId);
	}
	
	/**
	 * @param iGenericObjectId
	 * @param iNewStateId
	 * @throws NuclosFatalException
	 * @throws NuclosSubsequentStateNotLegalException
	 * @see StateFacadeRemote#changeStateByUser(Integer, Integer, Integer)
	 */
	public void changeState(int iModuleId, int iGenericObjectId, int iNewStateId, String customUsage)
			throws CommonPermissionException, NuclosSubsequentStateNotLegalException, NuclosNoAdequateStatemodelException,
			CommonFinderException, NuclosBusinessException {
		try {
			stateFacadeRemote.changeStateByUser(iModuleId, iGenericObjectId, iNewStateId, customUsage);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
		catch (CommonCreateException ex) {
			throw new CommonFatalException(ex);
		}
	}	
	
	/**
	 * checks if the given target state id is contained in the list of subsequent states for the given leased objects:
	 * @param iModuleId
	 * @param iGenericObjectId
	 * @param iTargetStateId
	 * @return true/false if state change is allowed
	 */
	public boolean checkTargetState(Integer iModuleId, Integer iGenericObjectId, Integer iTargetStateId) {
		try {
			return stateFacadeRemote.checkTargetState(iModuleId, iGenericObjectId, iTargetStateId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
		catch (CommonFinderException e) {
			throw new CommonFatalException(e.getMessage(), e);
		} 
		catch (CommonPermissionException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
		catch (NuclosNoAdequateStatemodelException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
	}

	/**
	 * method to modify and change state of a given object
	 * @param iModuleId module id for plausibility check
	 * @param govo object to change status for
	 * @param iNewStateId legal subsequent status id to set for given object
	 * @see StateFacadeRemote# changeStateAndModifyByUser(Integer, GenericObjectWithDependantsVO, Integer)
	 */
	public void changeStateAndModify(int iModuleId,GenericObjectWithDependantsVO gowdvo, int iNewStateId, String customUsage)
	throws NuclosNoAdequateStatemodelException, NuclosSubsequentStateNotLegalException, NuclosBusinessException,
	CommonPermissionException, CommonFinderException, CommonRemoveException, CommonStaleVersionException, CommonValidationException {
		try {
			stateFacadeRemote.changeStateAndModifyByUser(iModuleId, gowdvo, iNewStateId, customUsage);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
		catch (CommonCreateException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @return Collection<StateModelVO>
	 */
	public Collection<StateModelVO> getAllStateModels() {
		try {
			return stateFacadeRemote.getStateModels();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public StateGraphVO getStateGraph(int iModelId) throws CommonFinderException, CommonBusinessException {
		try {
			if (!mpStateGraphVO.containsKey(iModelId)) {
				final StateGraphVO stateGraphVO = stateFacadeRemote.getStateGraph(iModelId);
	
				// moved from StateGraphVO: NUCLOSINT-844 (b) correct the wrong StateModel-Layouts (after migration due MigrationVm2m5.java)
				final StateModelLayout layoutinfo = stateGraphVO.getStateModel().getLayout();
				for (StateTransitionVO statetransitionvo : stateGraphVO.getTransitions()) {
					if(layoutinfo.getTransitionLayout(statetransitionvo.getId()) == null){
						//insert default layout
						layoutinfo.insertTransitionLayout(statetransitionvo.getId(),
							new TransitionLayout(statetransitionvo.getId(), AbstractShape.CONNECTION_NE, AbstractShape.CONNECTION_N));
					}
				}
				mpStateGraphVO.put(iModelId, stateGraphVO);
			}
			return mpStateGraphVO.get(iModelId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
	}

	public Integer setStateGraph(StateGraphVO stategraphvo, DependantMasterDataMap mpDependants) throws CommonBusinessException {
		try {
			return stateFacadeRemote.setStateGraph(stategraphvo, mpDependants);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}


	/**
	 * @return StateModelId
	 */
	public Integer getStateModelId(UsageCriteria usagecriteria) {
		try {
			return stateFacadeRemote.getStateModelId(usagecriteria);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public void removeStateModel(StateModelVO smvo) throws CommonRemoveException, CommonStaleVersionException,
			CommonFinderException, NuclosBusinessRuleException, CommonBusinessException {
		try {
			stateFacadeRemote.removeStateGraph(smvo);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
	}

	public void invalidateCache(){
		try {
			stateFacadeRemote.invalidateCache();
		} catch (RuntimeException e) {
			throw new CommonFatalException(e);
		}
	}

	public String getResourceSIdForName(Integer iStateId) {
		try {
			return stateFacadeRemote.getResourceSIdForName(iStateId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public String getResourceSIdForDescription(Integer iStateId) {
		try {
			return stateFacadeRemote.getResourceSIdForDescription(iStateId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}
	
	public String getResourceSIdForButtonLabel(Integer iStateId) {
		try {
			return stateFacadeRemote.getResourceSIdForButtonLabel(iStateId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public StatemodelClosure getStatemodelClosure(Integer moduleId) {
		if (!mpStatemodelClosure.containsKey(moduleId))
			mpStatemodelClosure.put(moduleId, stateFacadeRemote.getStatemodelClosureForModule(moduleId));
		return mpStatemodelClosure.get(moduleId);
	}

	public Statemodel getStatemodel(UsageCriteria ucrit) {
		return getStatemodelClosure(ucrit.getModuleId()).getStatemodel(ucrit);
	}

	@Override
	public synchronized void destroy() {
		tnr.unsubscribe(this);
	}
	
	public List<StateTransitionVO> getOrderedStateTransitionsByStatemodel(Integer moduleId) {
		return stateFacadeRemote.getOrderedStateTransitionsByStatemodel(moduleId);
	}

}	// class StateDelegate
