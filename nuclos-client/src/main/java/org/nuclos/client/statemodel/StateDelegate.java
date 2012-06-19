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

import org.nuclos.client.caching.JMSFlushingCache;
import org.nuclos.client.gef.shapes.AbstractShape;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common.caching.GenCache;
import org.nuclos.common.statemodel.Statemodel;
import org.nuclos.common.statemodel.StatemodelClosure;
import org.nuclos.common2.ServiceLocator;
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

/**
 * Business Delegate for <code>StateFacadeBean</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class StateDelegate {
	private static StateDelegate singleton;
	private final StateFacadeRemote facade;

	private StateDelegate() {
		try {
			this.facade = ServiceLocator.getInstance().getFacade(StateFacadeRemote.class);
				//ServiceLocator.getInstance().getFacade(StateFacadeRemote.class);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public static synchronized StateDelegate getInstance() {
		if (singleton == null) {
			singleton = new StateDelegate();
		}
		return singleton;
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
					this.getStateFacade().getStateHistory(iModuleId, iGenericObjectId));

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
			return this.getStateFacade().getStatesByModel(iStateModelId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @param iGenericObjectId
	 * @param iNewStateId
	 * @throws NuclosFatalException
	 * @throws NuclosSubsequentStateNotLegalException
	 * @see StateFacadeRemote#changeStateByUser(Integer, Integer, Integer)
	 */
	public void changeState(int iModuleId, int iGenericObjectId, int iNewStateId)
			throws CommonPermissionException, NuclosSubsequentStateNotLegalException, NuclosNoAdequateStatemodelException,
			CommonFinderException, NuclosBusinessException {
		try {
			this.getStateFacade().changeStateByUser(iModuleId, iGenericObjectId, iNewStateId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
		catch (CommonCreateException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * method to modify and change state of a given object
	 * @param iModuleId module id for plausibility check
	 * @param govo object to change status for
	 * @param iNewStateId legal subsequent status id to set for given object
	 * @see StateFacadeRemote# changeStateAndModifyByUser(Integer, GenericObjectWithDependantsVO, Integer)
	 */
	public void changeStateAndModify(int iModuleId,GenericObjectWithDependantsVO gowdvo, int iNewStateId)
	throws NuclosNoAdequateStatemodelException, NuclosSubsequentStateNotLegalException, NuclosBusinessException,
	CommonPermissionException, CommonFinderException, CommonRemoveException, CommonStaleVersionException, CommonValidationException {
		try {
			this.getStateFacade().changeStateAndModifyByUser(iModuleId, gowdvo, iNewStateId);
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
			return this.getStateFacade().getStateModels();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public StateGraphVO getStateGraph(int iModelId) throws CommonFinderException, CommonBusinessException {
		try {
			final StateGraphVO result = this.getStateFacade().getStateGraph(iModelId);

			// moved from StateGraphVO: NUCLOSINT-844 (b) correct the wrong StateModel-Layouts (after migration due MigrationVm2m5.java)
			final StateModelLayout layoutinfo = result.getStateModel().getLayout();
			for (StateTransitionVO statetransitionvo : result.getTransitions()) {
				if(layoutinfo.getTransitionLayout(statetransitionvo.getId()) == null){
					//insert default layout
					layoutinfo.insertTransitionLayout(statetransitionvo.getId(),
						new TransitionLayout(statetransitionvo.getId(), AbstractShape.CONNECTION_NE, AbstractShape.CONNECTION_N));
				}
			}

			return result;
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
	}

	public Integer setStateGraph(StateGraphVO stategraphvo, DependantMasterDataMap mpDependants) throws CommonBusinessException {
		try {
			return this.getStateFacade().setStateGraph(stategraphvo, mpDependants);
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
			return this.getStateFacade().getStateModelId(usagecriteria);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public void removeStateModel(StateModelVO smvo) throws CommonRemoveException, CommonStaleVersionException,
			CommonFinderException, NuclosBusinessRuleException, CommonBusinessException {
		try {
			this.getStateFacade().removeStateGraph(smvo);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		} catch (CommonPermissionException e) {
			throw new CommonFatalException(e.getMessage(), e);
		}
	}

	private StateFacadeRemote getStateFacade() {
		return ServiceLocator.getInstance().getFacade(StateFacadeRemote.class);
		//return this.facade;
	}

	public void invalidateCache(){
		try {
			facade.invalidateCache();
		} catch (RuntimeException e) {
			throw new CommonFatalException(e);
		}
	}

	public String getResourceSIdForName(Integer iStateId) {
		try {
			return facade.getResourceSIdForName(iStateId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public String getResourceSIdForDescription(Integer iStateId) {
		try {
			return facade.getResourceSIdForDescription(iStateId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	private JMSFlushingCache<Integer, StatemodelClosure> closureCache
		= new JMSFlushingCache<Integer, StatemodelClosure>(
			JMSConstants.TOPICNAME_STATEMODEL, 
			new GenCache.LookupProvider<Integer, StatemodelClosure>() {
				@Override
	            public StatemodelClosure lookup(Integer moduleId) {
					return facade.getStatemodelClosureForModule(moduleId);
	            }
			});

	public StatemodelClosure getStatemodelClosure(Integer moduleId) {
		return closureCache.get(moduleId);
	}

	public Statemodel getStatemodel(UsageCriteria ucrit) {
		return getStatemodelClosure(ucrit.getModuleId()).getStatemodel(ucrit);
	}

}	// class StateDelegate
