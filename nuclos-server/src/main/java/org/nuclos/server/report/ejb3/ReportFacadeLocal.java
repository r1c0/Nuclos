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
import java.util.Map;

import javax.ejb.Local;
import javax.print.DocFlavor;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

import net.sf.jasperreports.engine.JasperPrint;

import org.nuclos.common.NuclosFile;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.NuclosReportPrintJob;
import org.nuclos.server.report.NuclosReportRemotePrintService;
import org.nuclos.server.report.valueobject.ReportOutputVO;
import org.nuclos.server.report.valueobject.ReportVO;
import org.nuclos.server.report.valueobject.SubreportVO;

// @Local
public interface ReportFacadeLocal {

	/**
	 * @return all reports
	 * @throws CommonPermissionException
	 */
	Collection<ReportVO> getReports()
		throws CommonPermissionException;

	/**
	 * get output formats for report
	 * @param iReportId id of report
	 * @return collection of output formats
	 */
	Collection<ReportOutputVO> getReportOutputs(Integer iReportId);

	/**
	 * get output format for reportoutput id
	 * @param iReportOutputId
	 * @return reportoutput
	 */
	ReportOutputVO getReportOutput(Integer iReportOutputId)
		throws CommonFinderException, CommonPermissionException;

	/**
	 * get subreports for reportoutput
	 * @param reportoutputId
	 * @return collection of subreports
	 */
	Collection<SubreportVO> getSubreports(Integer reportoutputId);

	/**
	 * gets a report/form filled with data
	 * @param iReportOutputId
	 * @param mpParams parameters
	 * @return report/form filled with data
	 */
	JasperPrint prepareReport(Integer iReportOutputId,
		Map<String, Object> mpParams, Integer iMaxRowCount)
		throws CommonFinderException, NuclosReportException,
		CommonPermissionException;

	NuclosFile prepareCsvReport(Integer iReportOutputId, Map<String, Object> mpParams, Integer iMaxRowCount) throws CommonFinderException, NuclosReportException, CommonPermissionException;

	public NuclosReportRemotePrintService lookupDefaultPrintService();
	
	public NuclosReportRemotePrintService[] lookupPrintServices(DocFlavor flavor, AttributeSet as) throws NuclosReportException;
	
	public void printViaPrintService(NuclosReportRemotePrintService ps, NuclosReportPrintJob pj, PrintRequestAttributeSet aset, byte[] data) throws NuclosReportException;
}
