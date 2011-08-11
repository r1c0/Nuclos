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
import javax.ejb.FinderException;
import javax.ejb.Remote;
import javax.swing.table.TableModel;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.design.JasperDesign;

import org.nuclos.common.UsageCriteria;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.valueobject.ReportOutputVO;
import org.nuclos.server.report.valueobject.ReportVO;
import org.nuclos.server.report.valueobject.ReportVO.ReportType;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;

@Remote
public interface ReportFacadeRemote {

	/**
	 * @return all reports
	 * @throws CommonPermissionException
	 */
	public abstract Collection<ReportVO> getReports()
		throws CommonPermissionException;

	/**
	 * Get all reports which have outputs containing the given datasourceId and have the given type (report, form or template).
	 * We go a little indirection so that we can use the security mechanism of the ReportBean.
	 * @param iDataSourceId
	 * @param iReportType
	 * @return set of reports
	 * @throws CommonPermissionException
	 */
	public abstract Collection<ReportVO> getReportsForDatasourceId(
		Integer iDataSourceId, final ReportType type)
		throws CommonPermissionException;

	/**
	 * create new report
	 * @param mdvo value object
	 * @param mpDependants
	 * @return new report
	 */
	public abstract MasterDataVO create(MasterDataVO mdvo,
		DependantMasterDataMap mpDependants) throws CommonCreateException,
		NuclosReportException, CommonPermissionException,
		NuclosBusinessRuleException;

	/**
	 * modify an existing report
	 * @param sEntity
	 * @param mdvo value object
	 * @param mpDependants
	 * @return modified report
	 */
	public abstract Integer modify(MasterDataVO mdvo,
		DependantMasterDataMap mpDependants) throws CommonBusinessException;

	/**
	 * delete an existing report
	 * @param sEntity
	 * @param mdvo value object
	 */
	public abstract void remove(MasterDataVO mdvo)
		throws CommonFinderException, CommonRemoveException,
		CommonStaleVersionException, CommonPermissionException,
		CommonCreateException, NuclosBusinessRuleException;

	/**
	 * get output formats for report
	 * @param iReportId id of report
	 * @return collection of output formats
	 */
	public abstract Collection<ReportOutputVO> getReportOutputs(Integer iReportId);

	/**
	 * get output format for reportoutput id
	 * @param iReportOutputId
	 * @return reportoutput
	 */
	public abstract ReportOutputVO getReportOutput(Integer iReportOutputId)
		throws CommonFinderException, CommonPermissionException;

	/**
	 * gets a report/form filled with data
	 * @param iReportOutputId
	 * @param mpParams parameters
	 * @return report/form filled with data
	 */
	public abstract JasperPrint prepareReport(Integer iReportOutputId,
		Map<String, Object> mpParams, Integer iMaxRowCount)
		throws CommonFinderException, NuclosReportException,
		CommonPermissionException;

	/**
	 * gets an empty report/form
	 * @param iReportOutputId report output id
	 * @return empty report/form
	 */
	public abstract JasperPrint prepareEmptyReport(Integer iReportOutputId)
		throws CommonFinderException, NuclosReportException,
		CommonBusinessException;

	/**
	 * gets search result report filled with data
	 * @param jrDesign prepared design template
	 * @param clctexpr search expression
	 * @param iModuleId module id of module to be displayed
	 * @param bIncludeSubModules Include submodules in search?
	 * @return search result report filled with data
	 */
	@RolesAllowed("Login")
	public abstract JasperPrint prepareSearchResult(CollectableSearchExpression clctexpr,
		List<? extends CollectableEntityField> lstclctefweSelected,
		Integer iModuleId, boolean bIncludeSubModules)
		throws NuclosReportException;

	/**
	 * @return search result report filled with data from the JTable
	 * @throws NuclosReportException
	 */
	@RolesAllowed("Login")
	public abstract JasperPrint prepareTableModel(TableModel tableModel)
		throws NuclosReportException;

	/**
	 * @return jasper design for the search result
	 * @throws NuclosReportException
	 * @throws FinderException
	 */
	@RolesAllowed("Login")
	public abstract JasperDesign getJrDesignForSearchResult()
		throws JRException, NuclosReportException, FinderException;

	/**
	 * @param iReportId report/form id
	 * @return Is save allowed for the report/form with the given id?
	 */
	@RolesAllowed("Login")
	public abstract boolean isSaveAllowed(Integer iReportId);

	/**
	 * finds reports readable for current user
	 * @return collection of report ids
	 */
	public abstract Collection<Integer> getReadableReportIdsForCurrentUser();

	/**
	 * finds reports (forms) by usage criteria
	 * @param usagecriteria
	 * @return collection of reports (forms)
	 */
	@RolesAllowed("Login")
	public abstract Collection<ReportVO> findReportsByUsage(
		UsageCriteria usagecriteria);

}
