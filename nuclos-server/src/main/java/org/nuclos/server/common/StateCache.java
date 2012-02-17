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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.dblayer.JoinType;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.database.SpringDataBaseHelper;
import org.nuclos.server.dblayer.query.DbFrom;
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
	
	private static final Logger LOG = Logger.getLogger(StateCache.class);

	private static StateCache INSTANCE;

	//
	
	private MasterDataFacadeLocal mdLocal;
	
	private StateFacadeLocal statefacade;
	
	private SpringDataBaseHelper dataBaseHelper;

	private final Map<Integer, StateVO> mpStatessById
		= new ConcurrentHashMap<Integer, StateVO>();
	
	private final Map<Integer, List<StateVO>> mpStatesByModel
		= new ConcurrentHashMap<Integer, List<StateVO>>();
	
	private final Map<Integer, StateVO> mpStateByModelAndInitialState
		= new ConcurrentHashMap<Integer, StateVO>();
	
	private final Map<Integer, StateTransitionVO> mpInitialTransitionByModel
		= new ConcurrentHashMap<Integer, StateTransitionVO>();
	
	private final Map<Integer, Collection<StateVO>> mpStatesByModule
		= new ConcurrentHashMap<Integer, Collection<StateVO>>();

	StateCache() {
		INSTANCE = this;
	}
	
	public void setStateFacadeLocal(StateFacadeLocal stateFacadeLocal) {
		this.statefacade = stateFacadeLocal;
	}
	
	public void setMasterDataFacadeLocal(MasterDataFacadeLocal masterDataFacadeLocal) {
		this.mdLocal = masterDataFacadeLocal;
	}
	
	public void setDataBaseHelper(SpringDataBaseHelper dataBaseHelper) {
		this.dataBaseHelper = dataBaseHelper;
	}
	
	public static StateCache getInstance() {
		return INSTANCE;
	}

	private StateFacadeLocal getStateFacade() {
		/*
		if (statefacade == null) {
			statefacade = ServiceLocator.getInstance().getFacade(StateFacadeLocal.class);
		}
		 */
		return statefacade;
	}

	private MasterDataFacadeLocal getMasterDataFacade() {
		/*
		if (mdLocal == null)
			mdLocal = ServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
		 */
		return mdLocal;
	}

	/**
	 * get a single State by id
	 * @param iStateId
	 * @return StateVO with given intid
	 * @throws CommonPermissionException
	 * @throws CommonFinderException
	 */
	public StateVO getState(Integer iStateId) {
		try {
			if (!mpStatessById.containsKey(iStateId)) {
				LOG.info("Initializing StateCache for StateId " + iStateId);

				StateVO stateVO = MasterDataWrapper.getStateVO(
						getMasterDataFacade().get(NuclosEntity.STATE.getEntityName(), iStateId));
				stateVO.setMandatoryFields(getStateFacade().findMandatoryFieldsByStateId(stateVO.getId()));
				stateVO.setMandatoryColumns(getStateFacade().findMandatoryColumnsByStateId(stateVO.getId()));
				mpStatessById.put(stateVO.getId(), stateVO);

				LOG.info("FINISHED initializing State cache for StateId " + iStateId);
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
	public Collection<StateVO> getStatesByModel(Integer iModelId) {
		if (!mpStatesByModel.containsKey(iModelId)) {
			LOG.info("Initializing StateCache for ModelID " + iModelId);
			final List<StateVO> lststatevo = new ArrayList<StateVO>();

			Collection<MasterDataVO> mdVOList = getMasterDataFacade().getDependantMasterData(NuclosEntity.STATE.getEntityName(), "model", iModelId);

			for(MasterDataVO vo : mdVOList) {
				StateVO stateVO = MasterDataWrapper.getStateVO(vo);
				stateVO.setMandatoryFields(getStateFacade().findMandatoryFieldsByStateId(stateVO.getId()));
				stateVO.setMandatoryColumns(getStateFacade().findMandatoryColumnsByStateId(stateVO.getId()));
				lststatevo.add(stateVO);
			}

			mpStatesByModel.put(iModelId, lststatevo);

			LOG.info("FINISHED initializing State cache for ModelId "
				+ iModelId);
		}

		return mpStatesByModel.get(iModelId);
	}
	
	public StateTransitionVO getInitialTransistionByModel(Integer iModelId) {
		if (!mpInitialTransitionByModel.containsKey(iModelId)) {
			StateTransitionVO initialTransition = getStateFacade().findStateTransitionByNullAndTargetState(getStateByModelAndInitialState(iModelId).getId());
			
			if (initialTransition == null)
				throw new NuclosFatalException("getInitialTransistionByModel failed for modelId = "+iModelId);

			mpInitialTransitionByModel.put(iModelId, initialTransition);
			LOG.info("FINISHED initializing Transistion cache for ModelId " + iModelId);
		}	
		return mpInitialTransitionByModel.get(iModelId);
	}

	public StateVO getStateByModelAndInitialState(Integer iModelId) {
		if (!mpStateByModelAndInitialState.containsKey(iModelId)) {
			LOG.info("Initializing StateCache for ModelID " + iModelId);

			DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
			DbQuery<Integer> query = builder.createQuery(Integer.class);
			DbFrom s = query.from("T_MD_STATE").alias("s");
			DbFrom st = s.join("T_MD_STATE_TRANSITION", JoinType.INNER).alias("st").on("INTID", "INTID_T_MD_STATE_2", Integer.class);
			query.select(s.baseColumn("INTID", Integer.class));
			query.where(builder.and(
				builder.equal(s.baseColumn("INTID_T_MD_STATEMODEL", Integer.class), iModelId),
				st.baseColumn("INTID_T_MD_STATE_1", Integer.class).isNull()));

			List<Integer> stateIds = dataBaseHelper.getDbAccess().executeQuery(query.distinct(true));

			if (stateIds.size() == 0)
				throw new NuclosFatalException("getStateByModelAndInitialState failed for modelId = "+iModelId);

			mpStateByModelAndInitialState.put(iModelId,getState(stateIds.iterator().next()));
			LOG.info("FINISHED initializing State cache for ModelId " + iModelId);
		}
		return mpStateByModelAndInitialState.get(iModelId);
	}

	public Collection<StateVO> getStatesByModule(Integer iModuleId) {
		// special case for general search (all states for all modules)
		if (iModuleId == null) {
			iModuleId = new Integer(-1);
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
				LOG.info("Initializing StateCache for ModuleID " + iModuleId);
				// First get all models for a given module

				final Collection<StateVO> collstatevo = new ArrayList<StateVO>();
				for (Integer iModel : StateModelUsagesCache.getInstance().getStateUsages().getStateModelIdsByModuleId(iModuleId))
				{
					collstatevo.addAll(this.getStatesByModel(iModel));
				}
				mpStatesByModule.put(iModuleId, collstatevo);
				LOG.info("FINISHED initializing State cache for ModuleId " + iModuleId);
			}
		}
		return mpStatesByModule.get(iModuleId);
	}

	/**
	 * Invalidate the whole cache
	 */
	public void invalidate() {
		LOG.info("Invalidating StateCache");
		mpStatessById.clear();
		mpStatesByModel.clear();
		mpStateByModelAndInitialState.clear();
		mpInitialTransitionByModel.clear();
		mpStatesByModule.clear();
	}

}	// class StateCache
