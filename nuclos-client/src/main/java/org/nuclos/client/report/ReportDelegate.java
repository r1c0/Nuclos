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
package org.nuclos.client.report;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ejb.FinderException;
import javax.swing.table.TableModel;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.design.JasperDesign;

import org.nuclos.common.CollectableEntityFieldWithEntity;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.ejb3.ReportFacadeRemote;
import org.nuclos.server.report.valueobject.ReportVO;
import org.nuclos.server.report.valueobject.ReportVO.ReportType;

/**
 * Report delegate.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class ReportDelegate {

	private static ReportDelegate singleton;

	private ReportFacadeRemote reportfacade;

	public static synchronized ReportDelegate getInstance() {
		if (singleton == null)
			singleton = new ReportDelegate();
		return singleton;
	}

	/**
	 * gets the report facade once for this object and stores it in a member variable.
	 */
	private ReportFacadeRemote getReportFacade() throws NuclosFatalException {
		if (reportfacade == null)
			try {
				reportfacade = ServiceLocator.getInstance().getFacade(ReportFacadeRemote.class);
			}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
		return reportfacade;
	}

	public Collection<Integer> getReadableReportIdsForCurrentUser() throws NuclosFatalException {
		try {
			return getReportFacade().getReadableReportIdsForCurrentUser();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	private CollectableSearchCondition getUserReadableReportsSubCondition(CollectableMasterDataEntity clmde) throws NuclosFatalException {
		Collection<Integer> readableReportIdsForCurrentUser = getReadableReportIdsForCurrentUser();
		CollectableSearchCondition res = SearchConditionUtils.getCollectableSearchConditionForIds(readableReportIdsForCurrentUser);
		if(!SearchConditionUtils.isAlwaysFalseCondition(res)){
			res.setConditionName("user readable");
		}
		return res;
	}

	public CollectableSearchCondition getCollectableSearchCondition(CollectableMasterDataEntity clctmde, CollectableSearchCondition superclctcond) {
		final CollectableSearchCondition result;
		final CollectableSearchCondition userReadableReportsCond = getUserReadableReportsSubCondition(clctmde);
		final CollectableSearchCondition cond = superclctcond;
		if (cond != null) {
			final Collection<CollectableSearchCondition> collOperands = Arrays.asList(new CollectableSearchCondition[] {cond, userReadableReportsCond});
			result = new CompositeCollectableSearchCondition(LogicalOperator.AND, collOperands);
		}
		else
			result = userReadableReportsCond;
		return result;
	}

	/**
	 * @param sEntity
	 * @param mdvoInserted
	 * @param mpDependants
	 * @return
	 * @throws NuclosBusinessException
	 */
	public MasterDataVO create(MasterDataVO mdvoInserted, DependantMasterDataMap mpDependants) throws CommonBusinessException {
		try {
			return getReportFacade().create(mdvoInserted, mpDependants);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
		catch (CommonCreateException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @return collection of all reports the current user has access to
	 * @throws CommonPermissionException
	 */
	public Collection<ReportVO> getReports() throws CommonPermissionException {
		try {
			return getReportFacade().getReports();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @return collection of all reports the datasource with the specified id is used in.
	 * @throws CommonPermissionException
	 */
	public Collection<ReportVO> getReportsForDatasourceId(Integer aDatasourceId) throws CommonPermissionException {
		try {
			return getReportFacade().getReportsForDatasourceId(aDatasourceId, ReportType.REPORT);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @return collection of all reports the datasource with the specified id is used in.
	 */
	public Collection<ReportVO> getFormularsForDatasourceId(Integer aDatasourceId) throws CommonPermissionException {
		try {
			return getReportFacade().getReportsForDatasourceId(aDatasourceId, ReportType.FORM);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public Integer modify(MasterDataVO mdvo, DependantMasterDataMap mpDependants) throws CommonBusinessException {
		try {
			return getReportFacade().modify(mdvo, mpDependants);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public void removeReport(MasterDataVO mdvo) throws CommonBusinessException {
		try {
			getReportFacade().remove(mdvo);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}


	/**
	 *
	 * @param id
	 * @param params
	 * @param iMaxRowCount
	 * @return result of ReportFacade method
	 * @throws CommonBusinessException
	 */
	public JasperPrint prepareReport(Integer id, Map<String, Object> params, Integer iMaxRowCount) throws CommonBusinessException {
		try {
			return getReportFacade().prepareReport(id, params, iMaxRowCount);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @param iReportOutputId
	 * @return
	 * @throws NuclosReportException
	 */
	public JasperPrint prepareEmptyReport(Integer iReportOutputId) throws CommonBusinessException {
		try {
			return getReportFacade().prepareEmptyReport(iReportOutputId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public JasperPrint prepareSearchResult(CollectableSearchExpression clctexpr,
		List<CollectableEntityFieldWithEntity> lstclctefweSelected, Integer iModuleId, boolean bIncludeSubModules) throws NuclosReportException {
		try {
			return getReportFacade().prepareSearchResult(clctexpr, lstclctefweSelected, iModuleId, bIncludeSubModules);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public JasperDesign getJrDesignForSearchResult() throws NuclosReportException {
		try {
			return getReportFacade().getJrDesignForSearchResult();
		}
		catch (FinderException ex) {
			throw new CommonFatalException(CommonLocaleDelegate.getMessage("ReportDelegate.1", "Keine Vorlage f\u00fcr den Ausdruck von Suchergebnissen angegeben."), ex);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
		catch (JRException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public JasperPrint prepareTableModel(TableModel tableModel) throws NuclosReportException {
		try {
			return getReportFacade().prepareTableModel(tableModel);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @param iReportId report/form id
	 * @return Is save allowed for the report/form with the given id?
	 */
	public boolean isSaveAllowed(Integer iReportId) {
		try {
			return getReportFacade().isSaveAllowed(iReportId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

}	// class ReportDelegate
