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

import javax.ejb.Local;

import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.report.valueobject.ReportOutputVO;
import org.nuclos.server.report.valueobject.ReportVO;
import org.nuclos.server.report.valueobject.SubreportVO;

@Local
public interface ReportFacadeLocal {

	/**
	 * @return all reports
	 * @throws CommonPermissionException
	 */
	public abstract Collection<ReportVO> getReports()
		throws CommonPermissionException;

	/**
	 * get output format for reportoutput id
	 * @param iReportOutputId
	 * @return reportoutput
	 */
	public abstract ReportOutputVO getReportOutput(Integer iReportOutputId)
		throws CommonFinderException, CommonPermissionException;

	/**
	 * get subreports for reportoutput
	 * @param reportoutputId
	 * @return collection of subreports
	 */
	public abstract Collection<SubreportVO> getSubreports(Integer reportoutputId);

}
