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

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.sql.DataSource;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;

import org.apache.commons.lang.SerializationUtils;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.NuclosFile;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.PivotInfo;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common.dblayer.JoinType;
import org.nuclos.common.entityobject.CollectableEOEntityField;
import org.nuclos.common.genericobject.GenericObjectUtils;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.XMLUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.NuclosSystemParameters;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.common.ejb3.LocaleFacadeLocal;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.dblayer.query.DbColumnExpression;
import org.nuclos.server.dblayer.query.DbCondition;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.genericobject.ejb3.GenericObjectFacadeLocal;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.masterdata.MasterDataWrapper;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.report.ByteArrayCarrier;
import org.nuclos.server.report.Export;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.NuclosReportPrintJob;
import org.nuclos.server.report.NuclosReportRemotePrintService;
import org.nuclos.server.report.ReportFieldDefinition;
import org.nuclos.server.report.ReportFieldDefinitionFactory;
import org.nuclos.server.report.api.JRNuclosDataSource;
import org.nuclos.server.report.export.CsvExport;
import org.nuclos.server.report.export.ExcelExport;
import org.nuclos.server.report.export.JasperExport;
import org.nuclos.server.report.valueobject.ReportOutputVO;
import org.nuclos.server.report.valueobject.ReportOutputVO.Format;
import org.nuclos.server.report.valueobject.ReportVO;
import org.nuclos.server.report.valueobject.ReportVO.ReportType;
import org.nuclos.server.report.valueobject.ResultColumnVO;
import org.nuclos.server.report.valueobject.ResultVO;
import org.nuclos.server.report.valueobject.SubreportVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

/**
 * Report facade encapsulating report management. <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Transactional(noRollbackFor = { Exception.class })
public class ReportFacadeBean extends NuclosFacadeBean implements ReportFacadeRemote {

	public static final String ALIAS_INTID = "intid";

	private static final String CHARENCODING = "UTF-8";

	//

	private DataSource dataSource;

	private MasterDataFacadeLocal masterDataFacade;

	private GenericObjectFacadeLocal genericObjectFacade;
	
	public ReportFacadeBean() {
	}

	@Autowired
	@Qualifier("nuclos")
	void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Autowired
	final void setMasterDataFacade(MasterDataFacadeLocal masterDataFacade) {
		this.masterDataFacade = masterDataFacade;
	}

	private final MasterDataFacadeLocal getMasterDataFacade() {
		return masterDataFacade;
	}

	public GenericObjectFacadeLocal getGenericObjectFacade() {
		return genericObjectFacade;
	}

	@Autowired
	public void setGenericObjectFacade(GenericObjectFacadeLocal genericObjectFacade) {
		this.genericObjectFacade = genericObjectFacade;
	}

	@PostConstruct
	@RolesAllowed("Login")
	public void postConstruct() {
		// Determine classpath dynamically
		String jrClasspath = getClassPathFor(JasperReport.class, JRNuclosDataSource.class);
		info("Set JasperReports compile class-path to " + jrClasspath);
		System.setProperty("jasper.reports.compile.class.path", jrClasspath);
		System.setProperty("jasper.reports.compile.keep.java.file", NuclosSystemParameters.getString(NuclosSystemParameters.JASPER_REPORTS_COMPILE_KEEP_JAVA_FILE));
		System.setProperty("jasper.reports.compile.temp", NuclosSystemParameters.getString(NuclosSystemParameters.JASPER_REPORTS_COMPILE_TMP));
		// System.setProperty("jasper.reports.compiler.class",
		// NuclosSystemParameters.getString(NuclosSystemParameters.JASPER_REPORTS_COMPILER_CLASS));
		System.setProperty("jasper.reports.compiler.class", JRJavaxToolsCompiler.class.getName());

		// Properties
		//
		// System.setProperty("jasper.reports.compile.class.path",
		// NuclosSystemParameters.getString(NuclosSystemParameters.JASPER_REPORTS_COMPILE_CLASS_PATH));
		// System.setProperty("jasper.reports.compile.keep.java.file",
		// NuclosSystemParameters.getString(NuclosSystemParameters.JASPER_REPORTS_COMPILE_KEEP_JAVA_FILE));
		// System.setProperty("jasper.reports.compile.temp",
		// NuclosSystemParameters.getString(NuclosSystemParameters.JASPER_REPORTS_COMPILE_TMP));
		// System.setProperty("jasper.reports.compiler.class",
		// NuclosSystemParameters.getString(NuclosSystemParameters.JASPER_REPORTS_COMPILER_CLASS));
	}

	/**
	 * @return all reports
	 * @throws CommonPermissionException
	 */
	public Collection<ReportVO> getReports() throws CommonPermissionException {
		this.checkReadAllowed(NuclosEntity.REPORT);

		final Collection<ReportVO> collreport = new ArrayList<ReportVO>();

		for (MasterDataVO mdVO : getMasterDataFacade().getMasterData(NuclosEntity.REPORT.getEntityName(), null, true)) {
			Collection<Integer> readableReports = SecurityCache.getInstance().getReadableReports(getCurrentUserName()).get(ReportType.REPORT);
			if (readableReports == null) {
				return collreport;
			}
			if (readableReports.contains(mdVO.getIntId()))
				collreport.add(MasterDataWrapper.getReportVO(mdVO, getCurrentUserName()));
		}

		return collreport;
	}

	/**
	 * Get all reports which have outputs containing the given datasourceId and
	 * have the given type (report, form or template). We go a little
	 * indirection so that we can use the security mechanism of the ReportBean.
	 * 
	 * @param iDataSourceId
	 * @param iReportType
	 * @return set of reports
	 * @throws CommonPermissionException
	 */
	public Collection<ReportVO> getReportsForDatasourceId(Integer iDataSourceId, final ReportType type) throws CommonPermissionException {
		this.checkReadAllowed(NuclosEntity.DATASOURCE);
		final Collection<ReportVO> collreport = new ArrayList<ReportVO>();

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom r = query.from("T_UD_REPORT").alias("r");
		DbFrom o = r.join("T_UD_REPORTOUTPUT", JoinType.INNER).alias("o").on("INTID", "INTID_T_UD_REPORT", Integer.class);
		query.select(r.baseColumn("INTID", Integer.class));
		query.where(builder.and(builder.equal(r.baseColumn("INTTYPE", Integer.class), type.getValue()), builder.equal(o.baseColumn("INTID_T_UD_DATASOURCE", String.class), iDataSourceId)));

		for (Integer intid : dataBaseHelper.getDbAccess().executeQuery(query)) {
			try {
				MasterDataVO mdVO = getMasterDataFacade().get(type == ReportType.REPORT ? NuclosEntity.REPORT.getEntityName() : NuclosEntity.FORM.getEntityName(), intid);
				Collection<Integer> readableReports = SecurityCache.getInstance().getReadableReports(getCurrentUserName()).get(type);
				if (mdVO != null && readableReports.contains(mdVO.getIntId()))
					collreport.add(MasterDataWrapper.getReportVO(mdVO, getCurrentUserName()));
			}
			catch (CommonPermissionException ex) {
				throw new NuclosFatalException(ex);
			}
			catch (CommonFinderException ex) {
				// nothing found, do nothing
			}
		}

		return collreport;
	}

	/**
	 * create new report
	 * 
	 * @param mdvo
	 *            value object
	 * @param mpDependants
	 * @return new report
	 */
	public MasterDataVO create(MasterDataVO mdvo, DependantMasterDataMap mpDependants) throws CommonCreateException, NuclosReportException, CommonPermissionException, NuclosBusinessRuleException {
		NuclosEntity entity = NuclosEntity.REPORT;
		if (ReportType.FORM.getValue().equals(mdvo.getField("type")))
			entity = NuclosEntity.FORM;

		this.checkReadAllowed(entity);
		final MasterDataVO result = getMasterDataFacade().create(entity.getEntityName(), mdvo, mpDependants);
		compileAndSaveAllXML(entity, result);
		SecurityCache.getInstance().invalidate();
		return result;
	}

	/**
	 * modify an existing report
	 * 
	 * @param sEntity
	 * @param mdvo
	 *            value object
	 * @param mpDependants
	 * @return modified report
	 */
	public Integer modify(MasterDataVO mdvo, DependantMasterDataMap mpDependants) throws CommonBusinessException {
		NuclosEntity entity = NuclosEntity.REPORT;
		if (ReportType.FORM.getValue().equals(mdvo.getField("type")))
			entity = NuclosEntity.FORM;

		this.checkReadAllowed(entity);
		final Integer result = (Integer) getMasterDataFacade().modify(entity.getEntityName(), mdvo, mpDependants);
		this.compileAndSaveAllXML(entity, mdvo);

		return result;
	}

	/**
	 * delete an existing report
	 * 
	 * @param sEntity
	 * @param mdvo
	 *            value object
	 */
	public void remove(MasterDataVO mdvo) throws CommonFinderException, CommonRemoveException, CommonStaleVersionException, CommonPermissionException, CommonCreateException, NuclosBusinessRuleException {
		NuclosEntity entity = NuclosEntity.REPORT;
		if (ReportType.FORM.getValue().equals(mdvo.getField("type")))
			entity = NuclosEntity.FORM;

		this.checkReadAllowed(entity);
		getMasterDataFacade().remove(entity.getEntityName(), mdvo, true);
		SecurityCache.getInstance().invalidate();
	}

	private void compileAndSaveAllXML(NuclosEntity entity, MasterDataVO mdvo) throws NuclosReportException {
		for (ReportOutputVO reportoutput : getReportOutputs(mdvo.getIntId())) {
			// Format is null when and only when it is the search output
			// template, which has to be PDF/XML
			if (reportoutput.getFormat() == null || "PDF".equals(reportoutput.getFormat().getValue())) {
				if (reportoutput.getSourceFile() != null) {
					compileAndSaveXML(entity, reportoutput);

					// subreports are only allowed for forms and reports
					if (reportoutput.getFormat() != null) {
						for (SubreportVO subreport : getSubreports(reportoutput.getId())) {
							compileAndSaveXML(entity, subreport);
						}
					}
				}
				else {
					// PDF must have a template
					throw new NuclosReportException("report.error.missing.template.1");// "F\u00fcr eine PDF-Ausgabe muss eine Vorlage angegeben werden.");
				}
			}
			else {
				reportoutput.setReportCLS(null);
				// reportoutput.setSourceFileContent(null);
			}
		}
	}

	private void compileAndSaveXML(NuclosEntity entity, ReportOutputVO reportOutput) throws NuclosReportException {
		final String sReportXML;
		try {
			sReportXML = new String(reportOutput.getSourceFileContent().getData(), CHARENCODING);
		}
		catch (UnsupportedEncodingException ex) {
			throw new NuclosFatalException(ex);
		}
		final JasperReport jr = compileReport(sReportXML);
		reportOutput.setReportCLS(new ByteArrayCarrier(SerializationUtils.serialize(jr)));

		MasterDataVO mdvo = MasterDataWrapper.wrapReportOutputVO(reportOutput);

		try {
			getMasterDataFacade().modify(entity.equals(NuclosEntity.REPORT) ? NuclosEntity.REPORTOUTPUT.getEntityName() : NuclosEntity.FORMOUTPUT.getEntityName(), mdvo, null);
		}
		catch (Exception e) {
			throw new NuclosReportException(e);
		}
	}

	private void compileAndSaveXML(NuclosEntity entity, SubreportVO subreport) throws NuclosReportException {
		final String sReportXML;
		try {
			sReportXML = new String(subreport.getSourcefileContent().getData(), CHARENCODING);
		}
		catch (UnsupportedEncodingException ex) {
			throw new NuclosFatalException(ex);
		}
		final JasperReport jr = compileReport(sReportXML);
		subreport.setReportCLS(new ByteArrayCarrier(SerializationUtils.serialize(jr)));

		MasterDataVO mdvo = MasterDataWrapper.wrapSubreportVO(entity, subreport);

		try {
			getMasterDataFacade().modify(entity.equals(NuclosEntity.REPORT) ? NuclosEntity.SUBREPORT.getEntityName() : NuclosEntity.SUBFORM.getEntityName(), mdvo, null);
		}
		catch (Exception e) {
			throw new NuclosReportException(e);
		}
	}

	/**
	 * compiles a report xml definition (jasperreports)
	 * 
	 * @param sReportXml
	 *            report layout definition
	 * @return compiled jasper report
	 */
	private JasperReport compileReport(String sReportXml) {
		try {
			return JasperCompileManager.compileReport(new ByteArrayInputStream(sReportXml.getBytes(XMLUtils.getXMLEncoding(sReportXml))));
		}
		catch (JRException ex) {
			throw new NuclosFatalException(ex);
		}
		catch (UnsupportedEncodingException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	/**
	 * get output formats for report
	 * 
	 * @param iReportId
	 *            id of report
	 * @return collection of output formats
	 */
	public Collection<ReportOutputVO> getReportOutputs(Integer iReportId) {
		List<ReportOutputVO> outputs = new ArrayList<ReportOutputVO>();

		CollectableComparison cond = SearchConditionUtils.newMDReferenceComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.REPORTOUTPUT), "parent", iReportId);
		Collection<MasterDataVO> mdOutputs = getMasterDataFacade().getMasterData(NuclosEntity.REPORTOUTPUT.getEntityName(), cond, true);

		for (MasterDataVO mdVO : mdOutputs)
			outputs.add(MasterDataWrapper.getReportOutputVO(mdVO));

		return outputs;
	}

	public Collection<SubreportVO> getSubreports(Integer reportoutputId) {
		List<SubreportVO> subreports = new ArrayList<SubreportVO>();

		CollectableComparison cond = SearchConditionUtils.newMDReferenceComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.SUBREPORT), "reportoutput", reportoutputId);
		Collection<MasterDataVO> mdSubreports = getMasterDataFacade().getMasterData(NuclosEntity.SUBREPORT.getEntityName(), cond, true);

		for (MasterDataVO mdVO : mdSubreports) {
			subreports.add(new SubreportVO(mdVO));
		}

		return subreports;
	}

	/**
	 * get output format for reportoutput id
	 * 
	 * @param iReportOutputId
	 * @return reportoutput
	 */
	public ReportOutputVO getReportOutput(Integer iReportOutputId) throws CommonFinderException, CommonPermissionException {
		return MasterDataWrapper.getReportOutputVO(getMasterDataFacade().get(NuclosEntity.REPORTOUTPUT.getEntityName(), iReportOutputId));
	}

	/**
	 * finds reports (forms) by usage criteria
	 * 
	 * @param usagecriteria
	 * @return collection of reports (forms)
	 */
	@RolesAllowed("Login")
	public Collection<ReportVO> findReportsByUsage(UsageCriteria usagecriteria) {

		List<ReportVO> reports = new ArrayList<ReportVO>();

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom t = query.from("T_UD_REPORTUSAGE").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("INTID_T_UD_REPORT", Integer.class));
		DbCondition cond = builder.equal(t.baseColumn("INTID_T_MD_MODULE", Integer.class), usagecriteria.getModuleId());

		DbColumnExpression<Integer> cp = t.baseColumn("INTID_T_MD_PROCESS", Integer.class);
		final Integer iProcessId = usagecriteria.getProcessId();
		if (iProcessId == null) {
			query.where(builder.and(cond, cp.isNull()));
		}
		else {
			query.where(builder.and(cond, builder.or(cp.isNull(), builder.equal(cp, iProcessId))));
		}
		DbColumnExpression<Integer> cs = t.baseColumn("INTID_T_MD_STATE", Integer.class);
		final Integer iStatusId = usagecriteria.getStatusId();
		if (iStatusId == null) {
			query.addToWhereAsAnd(builder.and(cond, cs.isNull()));
		}
		else {
			query.addToWhereAsAnd(builder.and(cond, builder.or(cs.isNull(), builder.equal(cs, iStatusId))));
		}

		List<Integer> collUsableReportIds = dataBaseHelper.getDbAccess().executeQuery(query);

		Map<ReportType, Collection<Integer>> readableReports = SecurityCache.getInstance().getReadableReports(getCurrentUserName());

		for (ReportType rt : readableReports.keySet()) {
			for (Integer reportId : CollectionUtils.intersection(collUsableReportIds, readableReports.get(rt))) {
				try {
					reports.add(MasterDataWrapper.getReportVO(getMasterDataFacade().get(NuclosEntity.FORM.getEntityName(), reportId), getCurrentUserName()));
				}
				catch (CommonPermissionException ex) {
					throw new CommonFatalException(ex);
				}
				catch (CommonFinderException ex) {
					throw new CommonFatalException(ex);
				}
			}
		}

		Collections.sort(reports, new Comparator<ReportVO>() {

			@Override
			public int compare(ReportVO o1, ReportVO o2) {
				return StringUtils.emptyIfNull(o1.getName()).compareToIgnoreCase(StringUtils.emptyIfNull(o2.getName()));
			}
		});

		return reports;
	}

	private static Export getExportInstance(ReportOutputVO.Format format) {
		switch (format) {
		case PDF:
			return new JasperExport();
		case CSV:
			return new CsvExport();
		case TSV:
			return new CsvExport('\t', 0, ' ', false);
		case XLS:
		case XLSX:
			return new ExcelExport(format);
		default:
			throw new NuclosFatalException("Format " + format + " is not supported for server-side execution.");
		}
	}
	
	@Override
	public NuclosFile testReport(Integer iReportOutputId) throws NuclosReportException {
		ReportOutputVO reportoutput;
		try {
			reportoutput = getReportOutput(iReportOutputId);
		}
		catch (CommonBusinessException e) {
			throw new NuclosReportException(e);
		}
		final Export export = getExportInstance(reportoutput.getFormat());
		return export.test(reportoutput);
	}

	/**
	 * gets a report/form filled with data
	 * 
	 * @param iReportOutputId
	 * @param mpParams
	 *            parameters
	 * @return report/form filled with data
	 */
	public NuclosFile prepareReport(Integer iReportOutputId, Map<String, Object> mpParams, Integer iMaxRowCount) throws CommonFinderException, NuclosReportException, CommonPermissionException {
		final ReportOutputVO reportoutput = getReportOutput(iReportOutputId);
		final Locale locale = getLocale(reportoutput.getLocale(), SpringLocaleDelegate.getInstance().getLocale());
		final Export export = getExportInstance(reportoutput.getFormat());
		return export.export(reportoutput, mpParams, locale, LangUtils.defaultIfNull(iMaxRowCount, -1));
	}

	private Locale getLocale(String locale, Locale def) {
		if (!StringUtils.isNullOrEmpty(locale)) {
			for (LocaleInfo li : ServerServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class).getAllLocales(true)) {
				if (locale.equalsIgnoreCase(li.name)) {
					return li.toLocale();
				}
			}
		}
		return def;
	}

	/**
	 * gets search result report filled with data
	 * <p>
	 * TODO: Don't serialize CollectableEntityField and/or CollectableEntity!
	 * (tp) Refer to
	 * {@link org.nuclos.common.CollectableEntityFieldWithEntity#readObject(ObjectInputStream)}
	 * for details.
	 * </p>
	 * 
	 * @param jrDesign
	 *            prepared design template
	 * @param clctexpr
	 *            search expression
	 * @param iModuleId
	 *            module id of module to be displayed
	 * @param bIncludeSubModules
	 *            Include submodules in search?
	 * @return search result report filled with data
	 */
	@RolesAllowed("Login")
	public NuclosFile prepareSearchResult(CollectableSearchExpression clctexpr, List<? extends CollectableEntityField> lstclctefweSelected, Integer iModuleId, boolean bIncludeSubModules, ReportOutputVO.Format format) throws NuclosReportException {
		final String entity = MetaDataServerProvider.getInstance().getEntity(IdUtils.toLongId(iModuleId)).getEntity();
		final List<Integer> lstAttributeIds = GenericObjectUtils.getAttributeIds(lstclctefweSelected, entity, AttributeCache.getInstance());
		final Set<String> subentities = new HashSet<String>();
		for (CollectableEntityField cef : lstclctefweSelected) {
			if (!entity.equals(cef.getEntityName())) {
				subentities.add(cef.getEntityName());
			}
		}
		final List<GenericObjectWithDependantsVO> lstlowdcvo = getGenericObjectFacade().getPrintableGenericObjectsWithDependants(iModuleId,
				clctexpr, new HashSet<Integer>(lstAttributeIds), subentities, false, bIncludeSubModules);
		final ResultVO resultvo = convertGenericObjectListToResultVO(entity, lstclctefweSelected, lstlowdcvo);
		final List<ReportFieldDefinition> fields = ReportFieldDefinitionFactory.getFieldDefinitions(lstclctefweSelected);
		final Export export = getExportInstance(format);
		return export.export(resultvo, fields);
	}
	
	/**
	 * creates a new ResultVO object from a list of selected collectable entities.
	 *
	 * @param clcteMain main collectable entity
	 * @param lstclctefweSelected List<CollectableEntityFieldWithEntity> attributes, subform or parent columns which are part of the content
	 * @param lstlowdcvo List<GenericObjectWithDependantsVO> the data contained in the selected fields
	 */
	private ResultVO convertGenericObjectListToResultVO(String sMainEntityName, List<? extends CollectableEntityField> lstclctefweSelected,
			List<GenericObjectWithDependantsVO> lstlowdcvo) {

		final ResultVO result = new ResultVO();

		// fill the columns:
		for (CollectableEntityField clctefwe : lstclctefweSelected) {
			final ResultColumnVO resultcolumnvo = new ResultColumnVO();
			resultcolumnvo.setColumnLabel(clctefwe.getLabel());
			resultcolumnvo.setColumnClassName(clctefwe.getJavaClass().getName());
			result.addColumn(resultcolumnvo);
		}

		// fill the rows:
		for (GenericObjectWithDependantsVO lowdcvo : lstlowdcvo) {
			final Object[] aoData = new Object[lstclctefweSelected.size()];

			int iColumn = 0;
			for (CollectableEntityField clctefwe : lstclctefweSelected) {
				final String sFieldName = clctefwe.getName();
				final String sFieldEntityName = clctefwe.getEntityName();

				if (sFieldEntityName.equals(sMainEntityName)) {
					// own attribute:
					final DynamicAttributeVO davo = lowdcvo.getAttribute(sFieldName, AttributeCache.getInstance());
					aoData[iColumn] = davo != null ? davo.getValue() : null;
				}
				else {
					final PivotInfo pinfo;
					if (clctefwe instanceof CollectableEOEntityField) {
						final CollectableEOEntityField f = (CollectableEOEntityField) clctefwe;
						pinfo = f.getMeta().getPivotInfo();
					}
					else {
						pinfo = null;
					}
					// pivot field:
					if (pinfo != null) {
						final List<Object> values = new ArrayList<Object>(1);
						final Collection<EntityObjectVO> items = lowdcvo.getDependants().getData(sFieldEntityName);

						for (EntityObjectVO k: items) {
							if (sFieldName.equals(k.getRealField(pinfo.getKeyField(), String.class))) {
								values.add(k.getRealField(pinfo.getValueField(), pinfo.getValueType()));
							}
						}
						if (values.isEmpty()) {
							aoData[iColumn] = null;
						}
						else {
							assert values.size() == 1 : "Expected 1 value, got " + values;
							aoData[iColumn] = values.get(0);
						}
					}
					// subform field:
					else {
						final Collection<EntityObjectVO> collmdvo = lowdcvo.getDependants().getData(sFieldEntityName);
						final List<Object> values = CollectionUtils.transform(collmdvo, new Transformer<EntityObjectVO, Object>() {
							@Override
							public Object transform(EntityObjectVO i) {
								return i.getRealField(sFieldName);
							}
						});
						aoData[iColumn] = values;
					}
				}

				iColumn++;
			}
			result.addRow(aoData);
		}

		return result;
	}

	@Override
	public NuclosFile prepareExport(ResultVO resultvo, Format format) throws NuclosReportException {
		final List<ReportFieldDefinition> fields = ReportFieldDefinitionFactory.getFieldDefinitions(resultvo);
		final Export export = getExportInstance(format);
		return export.export(resultvo, fields);
	}

	/**
	 * @param iReportId
	 *            report/form id
	 * @return Is save allowed for the report/form with the given id?
	 */
	@RolesAllowed("Login")
	public boolean isSaveAllowed(Integer iReportId) {
		return SecurityCache.getInstance().getWritableReportIds(getCurrentUserName()).contains(iReportId);
	}

	/**
	 * finds reports readable for current user
	 * 
	 * @return collection of report ids
	 */
	public Collection<Integer> getReadableReportIdsForCurrentUser() {
		Collection<Integer> result = new HashSet<Integer>();
		Map<ReportType, Collection<Integer>> readableReports = SecurityCache.getInstance().getReadableReports(getCurrentUserName());
		for (ReportType rt : readableReports.keySet())
			result.addAll(readableReports.get(rt));
		return result;
	}

	public static String getClassPathFor(Class<?>... classes) {
		StringBuilder sb = new StringBuilder();
		for (Class<?> clazz : classes) {
			if (sb.length() > 0) {
				sb.append(File.pathSeparator);
			}
			try {
				ProtectionDomain protectionDomain = clazz.getProtectionDomain();
				CodeSource codeSource = protectionDomain.getCodeSource();
				URL location = codeSource.getLocation();
				String path = location.getFile();
				path = URLDecoder.decode(path, "UTF-8");
				sb.append(path);
			}
			catch (Exception e) {
				throw new NuclosFatalException("Cannot configure JasperReports classpath ", e);
			}
		}
		return sb.toString();
	}

	public NuclosReportRemotePrintService lookupDefaultPrintService() throws NuclosReportException {
		PrintService ps = PrintServiceLookup.lookupDefaultPrintService();
		if (ps == null) throw new NuclosReportException("Es ist kein passender Default Print-Service installiert."); // @todo
		return new NuclosReportRemotePrintService(ps);
	}

	public NuclosReportRemotePrintService[] lookupPrintServices(DocFlavor flavor, AttributeSet as) throws NuclosReportException {
		PrintService prservDflt = PrintServiceLookup.lookupDefaultPrintService();
		PrintService[] prservices = PrintServiceLookup.lookupPrintServices(flavor, as);
		if (null == prservices || 0 >= prservices.length) {
			if (null != prservDflt) {
				prservices = new PrintService[] { prservDflt };
			}
			else {
				throw new NuclosReportException("Es ist kein passender Print-Service installiert."); // @todo
			}
		}

		NuclosReportRemotePrintService[] rprservices = new NuclosReportRemotePrintService[prservices.length];
		for (int i = 0; i < prservices.length; i++) {
			rprservices[i] = new NuclosReportRemotePrintService(prservices[i]);
		}
		return rprservices;
	}

	public void printViaPrintService(NuclosReportRemotePrintService ps, NuclosReportPrintJob pj, PrintRequestAttributeSet aset, byte[] data) throws NuclosReportException {
		try {
			File prntFile = getFileFromBytes(data);
			pj.print(ps, prntFile.getAbsolutePath(), aset);
		}
		catch (Exception e) {
			throw new NuclosReportException(e.getMessage());
		}
	}

	private static File getFileFromBytes(byte[] data) throws IOException {
		File file = File.createTempFile("report_", ".tmp");
		file.deleteOnExit();

		OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
		try {
			os.write(data);
		}
		finally {
			os.close();
		}
		return file;
	}

}