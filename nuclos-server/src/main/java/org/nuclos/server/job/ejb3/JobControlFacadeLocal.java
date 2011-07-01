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
package org.nuclos.server.job.ejb3;

import java.util.Date;

import javax.ejb.Local;

import org.nuclos.common.collection.Pair;
import org.nuclos.server.job.valueobject.JobVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

@Local
public interface JobControlFacadeLocal {

	Pair<JobVO, MasterDataVO> prepare(Object oId);

	void setJobExecutionResult(Object oResult, Date dFireTime, Date dNextFireTime, JobVO jobVO, MasterDataVO jobRun);

	void setJobExecutionResultError(Object oId, Date dFireTime, Date sNextFireTime, Integer iSessionId, String sErrorMessage);

	void writeToJobRunMessages(Integer iSessionId, String sLevel, String sMessage, String sRuleName);
}
