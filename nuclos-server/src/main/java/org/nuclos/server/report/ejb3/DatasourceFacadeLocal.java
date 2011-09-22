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
package org.nuclos.server.report.ejb3;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;

import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.querybuilder.NuclosDatasourceException;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.valueobject.DatasourceParameterVO;
import org.nuclos.server.report.valueobject.DatasourceVO;
import org.nuclos.server.report.valueobject.DynamicEntityVO;
import org.nuclos.server.report.valueobject.ResultVO;

@Local
public interface DatasourceFacadeLocal {

	/**
	 * get datasource value object
	 *
	 * @param iId
	 *                primary key of datasource
	 * @return datasource value object
	 */
	@RolesAllowed("Login")
	public abstract DatasourceVO get(Integer iId) throws CommonFinderException,
		CommonPermissionException;

	/**
	 * get datasource value object
	 *
	 * @param sDatasourceName
	 *                name of datasource
	 * @return datasource value object
	 */
	@RolesAllowed("Login")
	public abstract DatasourceVO get(String sDatasourceName)
		throws CommonFinderException, CommonPermissionException;

	/**
	 * get a Datasource by id regardless of permisssions
	 * @param iDatasourceId
	 * @return
	 * @throws CommonPermissionException
	 */
	@RolesAllowed("Login")
	public abstract DatasourceVO getDatasourceById(Integer iDatasourceId);

	/**
	 * get sql string for datasource definition
	 * @param iDatasourceId id of datasource
	 * @return string containing sql
	 */
	public abstract String createSQL(Integer iDatasourceId,
		Map<String, Object> mpParams) throws NuclosDatasourceException;

	/**
	 * get sql string for report execution.
	 * check that this method is only called by the local interface as there is no authorization applied.
	 *
	 * @param iDatasourceId id of datasource
	 * @return string containing sql
	 */
	public abstract String createSQLForReportExecution(String name, Map<String, Object> mpParams) throws NuclosDatasourceException;

	/**
	 * get sql string for datasource definition
	 *
	 * @param sDatasourceXML
	 *                xml of datasource
	 * @return string containing sql
	 */
	@RolesAllowed("Login")
	public abstract String createSQL(String sDatasourceXML,
		Map<String, Object> mpParams) throws NuclosDatasourceException;

   /**
    *
    * @param newDEs (dynamic entities of this list would be created)
    * @param oldDEs (dynamic entities of this list would be deleted)
    * @param bExecute
    * @param script
    */
	@RolesAllowed("Login")
   public abstract void processChangingDynamicEntities(Collection<DynamicEntityVO> newDEs, Collection<DynamicEntityVO> oldDEs, boolean bExecute, List<String> script);

	/**
	 * get a datasource result by datasource id
	 * @param iDatasourceId
	 * @param mpParams
	 * @param iMaxRowCount
	 * @return
	 * @throws NuclosReportException
	 * @throws CommonFinderException
	 */
   @RolesAllowed("Login")
	public abstract ResultVO executeQuery(Integer iDatasourceId,
		Map<String, Object> mpParams, Integer iMaxRowCount)
		throws NuclosDatasourceException, CommonFinderException;

   /**
	 * Retrieve the parameters a datasource accepts.
	 * @param iDatasourceId
	 * @return
	 * @throws NucleusFatalException
	 * @throws NucleusDatasourceException
	 */
	@RolesAllowed("Login")
	public abstract List<DatasourceParameterVO> getParameters(
		Integer iDatasourceId) throws NuclosFatalException,
		NuclosDatasourceException;

	@RolesAllowed("Login")
	public Collection<DynamicEntityVO> getDynamicEntities();
}
