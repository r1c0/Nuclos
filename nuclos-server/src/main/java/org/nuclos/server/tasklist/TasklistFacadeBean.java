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
package org.nuclos.server.tasklist;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.security.RolesAllowed;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.tasklist.TasklistDefinition;
import org.nuclos.common.tasklist.TasklistFacadeRemote;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.searchfilter.valueobject.SearchFilterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@RolesAllowed("Login")
@Transactional
public class TasklistFacadeBean extends NuclosFacadeBean implements TasklistFacadeRemote {

	private final static Logger LOG = Logger.getLogger(TasklistFacadeBean.class);

	private MasterDataFacadeLocal mdfacade;

	@Autowired
	public void setMasterDataFacade(MasterDataFacadeLocal facade) {
		this.mdfacade = facade;
	}

	@Override
	public Collection<TasklistDefinition> getUsersTasklists() {
		Collection<TasklistDefinition> result = new ArrayList<TasklistDefinition>();

		for (Integer iRoleId : SecurityCache.getInstance().getUserRoles(getCurrentUserName())) {
			final CollectableSearchCondition cond = SearchConditionUtils.newMDReferenceComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.TASKLISTROLE), "role", iRoleId);

			for (MasterDataVO tasklistrole : mdfacade.getMasterData(NuclosEntity.TASKLISTROLE.getEntityName(), cond, true)) {
				MasterDataVO tasklist;
				try {
					tasklist = mdfacade.get(NuclosEntity.TASKLIST.getEntityName(), tasklistrole.getField("tasklistId"));
				}
				catch (CommonBusinessException e) {
					LOG.warn(e);
					continue;
				}

				TasklistDefinition def = new TasklistDefinition(tasklist.getIntId());
				def.setName(tasklist.getField("name", String.class));
				def.setDescription(tasklist.getField("description", String.class));
				def.setLabelResourceId(tasklist.getField("labelres", String.class));
				def.setDescriptionResourceId(tasklist.getField("descriptionres", String.class));
				def.setMenupathResourceId(tasklist.getField("menupathres", String.class));
				def.setDynamicTasklistId(tasklist.getField("datasourceId", Integer.class));
				def.setDynamicTasklistIdFieldname(tasklist.getField("datasourceIdField", String.class));
				def.setDynamicTasklistEntityFieldname(tasklist.getField("datasourceEntityField", String.class));

				result.add(def);
			}
		}
		return result;
	}

}
