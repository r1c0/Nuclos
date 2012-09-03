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
package org.nuclos.server.common.ejb3;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import javax.annotation.security.RolesAllowed;

import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.common.valueobject.TimelimitTaskVO;
import org.nuclos.server.dal.DalSupportForGO;
import org.nuclos.server.genericobject.ejb3.GenericObjectFacadeLocal;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.masterdata.MasterDataWrapper;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade for managing timelimit tasks.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Transactional(noRollbackFor= {Exception.class})
@RolesAllowed("Login")
public class TimelimitTaskFacadeBean extends NuclosFacadeBean implements TimelimitTaskFacadeRemote {
	
	private GenericObjectFacadeLocal genericObjectFacade;
	
	private MasterDataFacadeLocal masterDataFacade;
	
	public TimelimitTaskFacadeBean() {
	}

	@Autowired
	final void setGenericObjectFacade(GenericObjectFacadeLocal genericObjectFacade) {
		this.genericObjectFacade = genericObjectFacade;
	}
	
	private final GenericObjectFacadeLocal getGenericObjectFacade() {
		return genericObjectFacade;
	}

	@Autowired
	final void setMasterDataFacade(MasterDataFacadeLocal masterDataFacade) {
		this.masterDataFacade = masterDataFacade;
	}
	
	private final MasterDataFacadeLocal getMasterDataFacade() {
		return masterDataFacade;
	}

	/**
	 * get all timelimit tasks (or only unfinished tasks)
	 * @param bOnlyUnfinishedTasks get only unfinished tasks
	 * @return collection of task value objects
	 */
	public Collection<TimelimitTaskVO> getTimelimitTasks(boolean bOnlyUnfinishedTasks) {
		final Collection<TimelimitTaskVO> result = new HashSet<TimelimitTaskVO>();

		Collection<MasterDataVO> mdVOList;

		if (bOnlyUnfinishedTasks) {
			mdVOList = getMasterDataFacade().getMasterData(NuclosEntity.TIMELIMITTASK.getEntityName(), SearchConditionUtils.newMDIsNullCondition(
				MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.TIMELIMITTASK), "completed"), true);
		}
		else {
			mdVOList = getMasterDataFacade().getMasterData(NuclosEntity.TIMELIMITTASK.getEntityName(), null, true);
		}

		for (MasterDataVO mdVO : mdVOList) {
			try {
				result.add(getTimelimitTaskVO(mdVO));
			}
			catch(CommonFinderException e) {
				throw new CommonFatalException(e);
			}
		}
		return result;
	}

	/**
	 * create a new TimelimitTask in the database
	 * @return same task as value object
	 */
	public TimelimitTaskVO create(TimelimitTaskVO voTimelimitTask) throws CommonValidationException, NuclosBusinessException {

		//check attribute for validity
		voTimelimitTask.validate();	//throws CommonValidationException

		try {
			MasterDataVO mdvo = getMasterDataFacade().create(NuclosEntity.TIMELIMITTASK.getEntityName(), MasterDataWrapper.wrapTimelimitTaskVO(voTimelimitTask), null, null);
			return getTimelimitTaskVO(getMasterDataFacade().get(NuclosEntity.TIMELIMITTASK.getEntityName(), mdvo.getId()));
		}
		catch (CommonPermissionException ex) {
			throw new NuclosBusinessException(ex);
		}
		catch (CommonFinderException ex) {
			throw new CommonFatalException(ex);
		}
		catch(CommonCreateException ex) {
			throw new NuclosBusinessException(ex);
		}
	}

	/**
	 * modify an existing TimelimitTask in the database
	 * @param voTimelimitTask containing the task data
	 * @return new task id
	 */
	public TimelimitTaskVO modify(TimelimitTaskVO voTimelimitTask) throws CommonFinderException, CommonStaleVersionException, CommonValidationException, NuclosBusinessException {

		//check attribute for validity
		voTimelimitTask.validate();	//throws CommonValidationException

		try {
			getMasterDataFacade().modify(NuclosEntity.TIMELIMITTASK.getEntityName(), MasterDataWrapper.wrapTimelimitTaskVO(voTimelimitTask), null, null);
			return getTimelimitTaskVO(getMasterDataFacade().get(NuclosEntity.TIMELIMITTASK.getEntityName(), voTimelimitTask.getId()));
		}
		catch (CommonPermissionException ex) {
			throw new NuclosBusinessException(ex);
		}
		catch(CommonRemoveException ex) {
			throw new NuclosBusinessException(ex);
		}
		catch(CommonCreateException ex) {
			throw new NuclosBusinessException(ex);
		}
	}

	/**
	 * delete TimelimitTask from database
	 * @param voTimelimitTask containing the task data
	 */
	public void remove(TimelimitTaskVO voTimelimitTask) throws CommonFinderException, CommonRemoveException, CommonStaleVersionException, NuclosBusinessException {
		try {
			getMasterDataFacade().remove(NuclosEntity.TIMELIMITTASK.getEntityName(), MasterDataWrapper.wrapTimelimitTaskVO(voTimelimitTask), false, null);
		}
		catch (CommonPermissionException ex) {
			throw new NuclosBusinessException(ex);
		}
	}

	private TimelimitTaskVO getTimelimitTaskVO(MasterDataVO mdVO) throws CommonFinderException {
		final Integer iGenericObjectId = mdVO.getField("genericobjectId", Integer.class);
		final EntityObjectVO eo = DalSupportForGO.getEntityObject(iGenericObjectId);
		final String sIdentifier = (String) eo.getFields().get(NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getField());
		final String sStatus = (String) eo.getFields().get(NuclosEOField.STATE.getMetaData().getField());
		final String sProcess = (String) eo.getFields().get(NuclosEOField.PROCESS.getMetaData().getField());
		TimelimitTaskVO timelimitTaskVO = MasterDataWrapper.getTimelimitTaskVO(mdVO, sIdentifier, sStatus, sProcess);
		
		return augmentModuleData(timelimitTaskVO);		
	}
	
   private TimelimitTaskVO augmentModuleData(TimelimitTaskVO timelimitTaskVO) throws CommonFinderException {
   	// TODO_AUTOSYNC: A little bit hackish -- but since it will somehow be replaced with the new GO mechanism, 
   	// I leave it this way...
   	if (timelimitTaskVO.getGenericObjectId() != null) {
   		int moduleId = getGenericObjectFacade().getModuleContainingGenericObject(timelimitTaskVO.getGenericObjectId());
   		timelimitTaskVO.setModuleId(moduleId);
	   	GenericObjectWithDependantsVO go = CollectionUtils.getFirst(getGenericObjectFacade().getGenericObjectsMore(
	   		moduleId, Collections.singletonList(timelimitTaskVO.getGenericObjectId()),
	   		new HashSet<Integer>(Arrays.asList(100000, 100001, 100003)), Collections.<String>emptySet(), ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY), false));
	   	for (DynamicAttributeVO attr : go.getAttributes()) {
	   		switch (attr.getAttributeId()) {
	   		case 100000: timelimitTaskVO.setIdentifier((String) attr.getValue()); break;
	   		case 100001: timelimitTaskVO.setStatus((String) attr.getValue()); break;
	   		case 100003: timelimitTaskVO.setProcess((String) attr.getValue()); break;
	   		}
	   	}
	   }
   	return timelimitTaskVO;
   }
}
