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
package org.nuclos.server.report;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFile;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.report.ejb3.DatasourceFacadeLocal;
import org.nuclos.server.report.ejb3.ReportFacadeLocal;
import org.nuclos.server.report.valueobject.DatasourceParameterVO;
import org.nuclos.server.report.valueobject.ReportOutputVO;
import org.nuclos.server.report.valueobject.ReportVO;
import org.nuclos.server.report.valueobject.ReportVO.ReportType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ReportExportController {

	private static final Logger LOG = Logger.getLogger(ReportExportController.class);

	/**
	 * Export a report via HTTP.
	 * TODO add support for forms (respect object level permissions).
	 *
	 * @param report
	 * @param output
	 * @param request
	 * @param response
	 * @return
	 */
	@RolesAllowed("Login")
	@RequestMapping("/{report}/{output}")
	public ModelAndView export(@PathVariable String report, @PathVariable String output, HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			// find report by name in user's readable reports
			ReportFacadeLocal facade = ServiceLocator.getInstance().getFacade(ReportFacadeLocal.class);
			for (ReportVO reportvo : facade.getReports()) {
				if (reportvo.getName().equalsIgnoreCase(report)) {
					if (reportvo.getType() != ReportType.REPORT) {
						response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "Only reports are available for http export.");
					}

					for (ReportOutputVO outputvo : facade.getReportOutputs(reportvo.getId())) {
						if (output.equalsIgnoreCase(outputvo.getDescription())) {
							NuclosFile result = export(reportvo, outputvo, request.getParameterMap());
							response.setContentType("text/csv");
							response.setHeader("Content-disposition", "attachment; filename=" + result.getFileName());
							response.setContentLength(result.getFileContents().length);

							ServletOutputStream sos = response.getOutputStream();
							sos.write(result.getFileContents());
							sos.flush();
							sos.close();
							return null;
						}
					}
					response.sendError(HttpServletResponse.SC_NOT_FOUND, "Requested report output does not exist.");
					return null;
				}
			}
			// report not found -> does not exist or permission was denied.
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Requested report does not exist or permission was denied.");
			return null;
		}
		catch (Exception e) {
			LOG.error("export failed: " + e, e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
		return null;
	}

	private NuclosFile export(ReportVO report, ReportOutputVO output, Map<?,?> parameters) throws CommonBusinessException, ClassNotFoundException {
		final Map<String, Object> params = CollectionUtils.newHashMap();
		List<DatasourceParameterVO> lstParameters = ServiceLocator.getInstance().getFacade(DatasourceFacadeLocal.class).getParameters(report.getDatasourceId());
		for (DatasourceParameterVO dspvo: lstParameters) {
			if (parameters.containsKey(dspvo.getParameter())) {
				params.put(dspvo.getParameter(), CollectableFieldFormat.getInstance(Class.forName(dspvo.getDatatype())).parse(null, ((String[])parameters.get(dspvo.getParameter()))[0]));
			}
			else {
				params.put(dspvo.getParameter(), null);
			}
		}
		if (output.getFormat() == ReportOutputVO.Format.CSV) {
			return exportCSV(output.getId(), params);
		}
		else if (output.getFormat() == ReportOutputVO.Format.PDF) {
			return exportPDF(output.getId(), params);
		}
		else {
			throw new CommonBusinessException("Only csv and pdf output available");
		}
	}

	private NuclosFile exportCSV(Integer reportOutputId, Map<String, Object> params) throws CommonBusinessException {
		return ServiceLocator.getInstance().getFacade(ReportFacadeLocal.class).prepareCsvReport(reportOutputId, params, null);
	}

	private NuclosFile exportPDF(Integer reportOutputId, Map<String, Object> params) throws CommonBusinessException {
		JasperPrint jp = ServiceLocator.getInstance().getFacade(ReportFacadeLocal.class).prepareReport(reportOutputId, params, null);
		try {
			return new NuclosFile("test.pdf", JasperExportManager.exportReportToPdf(jp));
		} catch (JRException e) {
			throw new CommonBusinessException(e);
		}
	}
}
