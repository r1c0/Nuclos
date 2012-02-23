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
package org.nuclos.server.dbtransfer.content;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.report.ejb3.SchedulerControlFacadeLocal;

public class JobControllerNucletContent extends DefaultNucletContent {

	public JobControllerNucletContent(List<INucletContent> contentTypes) {
		super(NuclosEntity.JOBCONTROLLER, null, contentTypes);
	}

	@Override
	public void deleteNcObject(DalCallResult result, EntityObjectVO ncObject) {
		unschedule(ncObject);
		super.deleteNcObject(result, ncObject);
	}

	@Override
	public void insertOrUpdateNcObject(DalCallResult result, EntityObjectVO ncObject, boolean isNuclon) throws SQLIntegrityConstraintViolationException {
		if (ncObject.isFlagUpdated()) {
			EntityObjectVO job = NucletDalProvider.getInstance().getEntityObjectProcessor(getEntity().getEntityName()).getByPrimaryKey(ncObject.getId());
			unschedule(job);
		}
		ncObject.getFields().put("laststate", "Deaktiviert");
		ncObject.getFields().put("running", false);
		ncObject.getFields().put("lastfiretime", null);
		ncObject.getFields().put("result", null);
		ncObject.getFields().put("resultdetails", null);
		ncObject.getFields().put("nextfiretime", null);

		super.insertOrUpdateNcObject(result, ncObject, isNuclon);
	}

	private void unschedule(EntityObjectVO job) {
		if (job != null && ServerServiceLocator.getInstance().getFacade(SchedulerControlFacadeLocal.class).isScheduled(job.getField("name", String.class))) {
			try {
				ServerServiceLocator.getInstance().getFacade(SchedulerControlFacadeLocal.class).deleteJob(job.getField("name", String.class));
			}
			catch (Exception e) {
				warn(e);
			}
		}
	}
}
