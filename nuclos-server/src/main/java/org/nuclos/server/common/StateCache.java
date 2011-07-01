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
package org.nuclos.server.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbJoin.JoinType;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.masterdata.MasterDataWrapper;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.statemodel.ejb3.StateFacadeLocal;
import org.nuclos.server.statemodel.valueobject.StateModelUsagesCache;
import org.nuclos.server.statemodel.valueobject.StateTransitionVO;
import org.nuclos.server.statemodel.valueobject.StateVO;

/**
 * A cache for States.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:lars.rueckemann@novabit.de">Lars R\u00fcckemann</a>
 * @version 00.01.000
 */
public class StateCache {
	private static final Logger log = Logger.getLogger(StateCache.class);

	private static StateCache singleton;

	private MasterDataFacadeLocal mdLocal = null;
	private StateFacadeLocal statefacade;

	private Map<Integer, StateVO> mpStatessById;
	private Map<Integer, List<StateVO>> mpStatesByModel;
	private Map<Integer, StateVO> mpStateByModelAndInitialState;
	private Map<Integer, StateTransitionVO> mpInitialTransitionByModel;
	private Map<Integer, Collection<StateVO>> mpStatesByModule;

	private StateCache() {

	}
	
	private StateFacadeLocal getStateFacade() {
		if (statefacade == null) {
			statefacade = ServiceLocator.getInstance().getFacade(StateFacadeLocal.class);
		}
		return statefacade;
	}

	private MasterDataFacadeLocal getMasterDataFacade() {
		if (mdLocal == null)
			mdLocal = ServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
		return mdLocal;
	}

	public static synchronized StateCache getInstance() {
		if (singleton == null) {
			singleton = new StateCache();
		}
		return singleton;
	}

	/**
	 * get a single State by id
	 * @param iStateId
	 * @return StateVO with given intid
	 * @throws CommonPermissionException
	 * @throws CommonFinderException
	 */
	public synchronized StateVO getState(Integer iStateId) {
		try {
			if (mpStatessById == null) {
				mpStatessById = Collections.synchronizedMap(new HashMap<Integer, StateVO>());
			}
			if (!mpStatessById.containsKey(iStateId)) {
				log.info("Initializing StateCache for StateId " + iStateId);

				StateVO stateVO = MasterDataWrapper.getStateVO(getMasterDataFacade().get(NuclosEntity.STATE.getEntityName(), iStateId));
				stateVO.setMandatoryFields(getStateFacade().findMandatoryFieldsByStateId(stateVO.getId()));
				stateVO.setMandatoryColumns(getStateFacade().findMandatoryColumnsByStateId(stateVO.getId()));
				mpStatessById.put(stateVO.getId(), stateVO);

				log.info("FINISHED initializing State cache for StateId " + iStateId);
			}
		}
		catch (CommonFinderException ex) {
			throw new NuclosFatalException(ex);
		}
		catch (CommonPermissionException ex) {
			throw new NuclosFatalException(ex);
		}

		return mpStatessById.get(iStateId);
	}

	/**
	 * get a Collection of StateVO by modelId
	 * @param iModelId
	 * @return List<StateVO> of all states in model with given id
	 */
	public synchronized Collection<StateVO> getStatesByModel(Integer iModelId) {
		if (mpStatesByModel == null) {
			mpStatesByModel = Collections.synchronizedMap(new HashMap<Integer, List<StateVO>>());
		}
		if (!mpStatesByModel.containsKey(iModelId)) {
			log.info("Initializing StateCache for ModelID " + iModelId);
			final List<StateVO> lststatevo = new ArrayList<StateVO>();

			Collection<MasterDataVO> mdVOList = getMasterDataFacade().getDependantMasterData(NuclosEntity.STATE.getEntityName(), "model", iModelId);

			for(MasterDataVO vo : mdVOList) {
				StateVO stateVO = MasterDataWrapper.getStateVO(vo);
				stateVO.setMandatoryFields(getStateFacade().findMandatoryFieldsByStateId(stateVO.getId()));
				stateVO.setMandatoryColumns(getStateFacade().findMandatoryColumnsByStateId(stateVO.getId()));
				lststatevo.add(stateVO);
			}

			mpStatesByModel.put(iModelId, lststatevo);

			log.info("FINISHED initializing State cache for ModelId "
				+ iModelId);
		}

		return mpStatesByModel.get(iModelId);
	}
	
	public synchronized StateTransitionVO getInitialTransistionByModel(Integer iModelId) {
		if (mpInitialTransitionByModel == null) {
			mpInitialTransitionByModel = Collections.synchronizedMap(new HashMap<Integer, StateTransitionVO>());
		}
		if (!mpInitialTransitionByModel.containsKey(iModelId)) {
			StateTransitionVO initialTransition = getStateFacade().findStateTransitionByNullAndTargetState(getStateByModelAndInitialState(iModelId).getId());
			
			if (initialTransition == null)
				throw new NuclosFatalException("getInitialTransistionByModel failed for modelId = "+iModelId);

			mpInitialTransitionByModel.put(iModelId, initialTransition);

			log.info("FINISHED initializing Transistion cache for ModelId " + iModelId);
		}
		
		return mpInitialTransitionByModel.get(iModelId);
	}

	public synchronized StateVO getStateByModelAndInitialState(Integer iModelId) {
		if (mpStateByModelAndInitialState == null) {
			mpStateByModelAndInitialState = Collections.synchronizedMap(new HashMap<Integer, StateVO>());
		}
		if (!mpStateByModelAndInitialState.containsKey(iModelId)) {
			log.info("Initializing StateCache for ModelID " + iModelId);

			DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
			DbQuery<Integer> query = builder.createQuery(Integer.class);
			DbFrom s = query.from("T_MD_STATE").alias("s");
			DbFrom st = s.join("T_MD_STATE_TRANSITION", JoinType.INNER).on("INTID", "INTID_T_MD_STATE_2").alias("st");
			query.select(s.column("INTID", Integer.class));
			query.where(builder.and(
				builder.equal(s.column("INTID_T_MD_STATEMODEL", Integer.class), iModelId),
				st.column("INTID_T_MD_STATE_1", Integer.class).isNull()));

			List<Integer> stateIds = DataBaseHelper.getDbAccess().executeQuery(query.distinct(true));

			if (stateIds.size() == 0)
				throw new NuclosFatalException("getStateByModelAndInitialState failed for modelId = "+iModelId);

			mpStateByModelAndInitialState.put(iModelId,getState(stateIds.iterator().next()));

			log.info("FINISHED initializing State cache for ModelId " + iModelId);
		}

		return mpStateByModelAndInitialState.get(iModelId);
	}

	public synchronized Collection<StateVO> getStatesByModule(Integer iModuleId) {
		// special case for general search (all states for all modules)
		if (iModuleId == null) {
			iModuleId = new Integer(-1);
		}
		if (mpStatesByModule == null) {
			mpStatesByModule = Collections.synchronizedMap(new HashMap<Integer, Collection<StateVO>>());
		}
		if (!mpStatesByModule.containsKey(iModuleId)) {
			if (iModuleId.intValue() == -1) {
				final List<StateVO> lstStates = new ArrayList<StateVO>();
				final Collection<MasterDataVO> colStates = getMasterDataFacade().getMasterData(NuclosEntity.STATE.getEntityName(), null, true);
				for (MasterDataVO mdVO : colStates) {
					StateVO stateVO = MasterDataWrapper.getStateVO(mdVO);
					stateVO.setMandatoryFields(getStateFacade().findMandatoryFieldsByStateId(stateVO.getId()));
					stateVO.setMandatoryColumns(getStateFacade().findMandatoryColumnsByStateId(stateVO.getId()));
					lstStates.add(stateVO);
				}
				mpStatesByModule.put(iModuleId, lstStates);
			}
			else {
				log.info("Initializing StateCache for ModuleID " + iModuleId);
				// First get all models for a given module

				final Collection<StateVO> collstatevo = new ArrayList<StateVO>();
				for (Integer iModel : StateModelUsagesCache.getInstance().getStateUsages().getStateModelIdsByModuleId(iModuleId))
				{
					collstatevo.addAll(this.getStatesByModel(iModel));
				}

				mpStatesByModule.put(iModuleId, collstatevo);
				log.info("FINISHED initializing State cache for ModuleId " + iModuleId);
			}
		}

		return mpStatesByModule.get(iModuleId);
	}

	/**
	 * Invalidate the whole cache
	 */
	public synchronized void invalidate() {
		log.info("Invalidating StateCache");
		mpStatessById = null;
		mpStatesByModel = null;
		mpStateByModelAndInitialState = null;
		mpInitialTransitionByModel = null;
		mpStatesByModule = null;
	}

}	// class StateCache
