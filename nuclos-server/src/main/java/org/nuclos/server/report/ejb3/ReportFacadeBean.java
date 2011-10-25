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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.ejb.FinderException;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.swing.table.TableModel;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRReportFont;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.apache.commons.lang.SerializationUtils;
import org.nuclos.common.MarshalledValue;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.NuclosFile;
import org.nuclos.common.PDFHelper;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common.dblayer.JoinType;
import org.nuclos.common.querybuilder.NuclosDatasourceException;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.XMLUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.NuclosDataSources;
import org.nuclos.server.common.NuclosSystemParameters;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.ejb3.LocaleFacadeLocal;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.query.DbColumnExpression;
import org.nuclos.server.dblayer.query.DbCondition;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.masterdata.MasterDataWrapper;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.report.ByteArrayCarrier;
import org.nuclos.server.report.JRDefaultNuclosDataSource;
import org.nuclos.server.report.JREmptyNuclosDataSource;
import org.nuclos.server.report.JRFileResolver;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.SearchResultDataSource;
import org.nuclos.server.report.TableModelDataSource;
import org.nuclos.server.report.api.JRNuclosDataSource;
import org.nuclos.server.report.csv.TemplatedCsvExport;
import org.nuclos.server.report.valueobject.ReportOutputVO;
import org.nuclos.server.report.valueobject.ReportVO;
import org.nuclos.server.report.valueobject.ReportVO.ReportType;
import org.nuclos.server.report.valueobject.ResultVO;
import org.nuclos.server.report.valueobject.SubreportVO;
import org.nuclos.server.resource.ResourceCache;
import org.nuclos.server.resource.valueobject.ResourceVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Transactional;

/**
* Report facade encapsulating report management.
* <br>
* <br>Created by Novabit Informationssysteme GmbH
* <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
*/
@Stateless
@Local(ReportFacadeLocal.class)
@Remote(ReportFacadeRemote.class)
@Transactional
public class ReportFacadeBean extends NuclosFacadeBean implements ReportFacadeLocal, ReportFacadeRemote {

   public static final String ALIAS_INTID = "intid";

   private static final String CHARENCODING = "UTF-8";

   @PostConstruct
   @RolesAllowed("Login")
   @Override
   public void postConstruct() {
      super.postConstruct();

      // Determine classpath dynamically
      String jrClasspath = getClassPathFor(JasperReport.class, JRNuclosDataSource.class);
      info("Set JasperReports compile class-path to " + jrClasspath);
      System.setProperty("jasper.reports.compile.class.path", jrClasspath);
      System.setProperty("jasper.reports.compile.keep.java.file", NuclosSystemParameters.getString(NuclosSystemParameters.JASPER_REPORTS_COMPILE_KEEP_JAVA_FILE));
      System.setProperty("jasper.reports.compile.temp", NuclosSystemParameters.getString(NuclosSystemParameters.JASPER_REPORTS_COMPILE_TMP));
      // System.setProperty("jasper.reports.compiler.class", NuclosSystemParameters.getString(NuclosSystemParameters.JASPER_REPORTS_COMPILER_CLASS));
      System.setProperty("jasper.reports.compiler.class", JRJavaxToolsCompiler.class.getName());

      // Properties
      //
      // System.setProperty("jasper.reports.compile.class.path", NuclosSystemParameters.getString(NuclosSystemParameters.JASPER_REPORTS_COMPILE_CLASS_PATH));
      // System.setProperty("jasper.reports.compile.keep.java.file", NuclosSystemParameters.getString(NuclosSystemParameters.JASPER_REPORTS_COMPILE_KEEP_JAVA_FILE));
      // System.setProperty("jasper.reports.compile.temp", NuclosSystemParameters.getString(NuclosSystemParameters.JASPER_REPORTS_COMPILE_TMP));
      // System.setProperty("jasper.reports.compiler.class", NuclosSystemParameters.getString(NuclosSystemParameters.JASPER_REPORTS_COMPILER_CLASS));
   }

   /**
    * @return all reports
    * @throws CommonPermissionException
    */
   @Override
   public Collection<ReportVO> getReports() throws CommonPermissionException {
      this.checkReadAllowed(NuclosEntity.REPORT);
      final Collection<ReportVO> collreport = new ArrayList<ReportVO>();

      for (MasterDataVO mdVO : getMasterDataFacade().getMasterData(NuclosEntity.REPORT.getEntityName(), null, true)) {
         Collection<Integer> readableReports = SecurityCache.getInstance().getReadableReports(getCurrentUserName()).get(ReportType.REPORT);
         if (readableReports == null) {
        	 return collreport;
         }
         if (readableReports.contains(mdVO.getIntId()))
            collreport.add(MasterDataWrapper.getReportVO(mdVO,getCurrentUserName()));
      }

      return collreport;
   }

   /**
    * Get all reports which have outputs containing the given datasourceId and have the given type (report, form or template).
    * We go a little indirection so that we can use the security mechanism of the ReportBean.
    * @param iDataSourceId
    * @param iReportType
    * @return set of reports
    * @throws CommonPermissionException
    */
   @Override
public Collection<ReportVO> getReportsForDatasourceId(Integer iDataSourceId, final ReportType type) throws CommonPermissionException {
      this.checkReadAllowed(NuclosEntity.DATASOURCE);
      final Collection<ReportVO> collreport = new ArrayList<ReportVO>();

      DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
      DbQuery<Integer> query = builder.createQuery(Integer.class);
      DbFrom r = query.from("T_UD_REPORT").alias("r");
      DbFrom o = r.join("T_UD_REPORTOUTPUT", JoinType.INNER).alias("o").on("INTID", "INTID_T_UD_REPORT", Integer.class);
      query.select(r.baseColumn("INTID", Integer.class));
      query.where(builder.and(
         builder.equal(r.baseColumn("INTTYPE", Integer.class), type.getValue()),
         builder.equal(o.baseColumn("INTID_T_UD_DATASOURCE", String.class), iDataSourceId)));

      for (Integer intid : DataBaseHelper.getDbAccess().executeQuery(query)) {
         try {
            MasterDataVO mdVO = getMasterDataFacade().get(NuclosEntity.REPORT.getEntityName(), intid);
            Collection<Integer> readableReports = SecurityCache.getInstance().getReadableReports(getCurrentUserName()).get(type);
            if (mdVO != null && readableReports.contains(mdVO.getIntId()))
               collreport.add(MasterDataWrapper.getReportVO(mdVO,getCurrentUserName()));
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
    * @param mdvo value object
    * @param mpDependants
    * @return new report
    */
   @Override
public MasterDataVO create(MasterDataVO mdvo, DependantMasterDataMap mpDependants)
         throws CommonCreateException, NuclosReportException, CommonPermissionException, NuclosBusinessRuleException {
      this.checkReadAllowed(NuclosEntity.REPORT);
      final MasterDataVO result = getMasterDataFacade().create(NuclosEntity.REPORT.getEntityName(), mdvo, mpDependants);
      compileAndSaveAllXML(result);
      SecurityCache.getInstance().invalidate();
      return result;
   }

   /**
    * modify an existing report
    * @param sEntity
    * @param mdvo value object
    * @param mpDependants
    * @return modified report
    */
   @Override
public Integer modify(MasterDataVO mdvo, DependantMasterDataMap mpDependants) throws CommonBusinessException {
      this.checkReadAllowed(NuclosEntity.REPORT);
      final Integer result = (Integer) getMasterDataFacade().modify(NuclosEntity.REPORT.getEntityName(), mdvo, mpDependants);
      this.compileAndSaveAllXML(mdvo);

      return result;
   }

   /**
    * delete an existing report
    * @param sEntity
    * @param mdvo value object
    */
   @Override
public void remove(MasterDataVO mdvo)
         throws CommonFinderException, CommonRemoveException, CommonStaleVersionException, CommonPermissionException, CommonCreateException, NuclosBusinessRuleException {
      this.checkReadAllowed(NuclosEntity.REPORT);
      getMasterDataFacade().remove(NuclosEntity.REPORT.getEntityName(), mdvo, true);
      SecurityCache.getInstance().invalidate();
   }

   private void compileAndSaveAllXML(MasterDataVO mdvo) throws NuclosReportException {
      for (ReportOutputVO reportoutput : getReportOutputs(mdvo.getIntId()))
      {
         // Format is null when and only when it is the search output template, which has to be PDF/XML
         if ( reportoutput.getFormat() == null || "PDF".equals(reportoutput.getFormat().getValue()) ) {
            if (reportoutput.getSourceFile() != null) {
               compileAndSaveXML(reportoutput);

               // subreports are only allowed for forms and reports
               if (reportoutput.getFormat() != null) {
                  for (SubreportVO subreport : getSubreports(reportoutput.getId())) {
                     compileAndSaveXML(subreport);
                  }
               }
            }
            else {
               // PDF must have a template
               throw new NuclosReportException("report.error.missing.template.1");//"F\u00fcr eine PDF-Ausgabe muss eine Vorlage angegeben werden.");
            }
         }
         else {
            reportoutput.setReportCLS(null);
            // reportoutput.setSourceFileContent(null);
         }
      }
   }

   private void compileAndSaveXML(ReportOutputVO reportOutput) throws NuclosReportException {
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
         getMasterDataFacade().modify(NuclosEntity.REPORTOUTPUT.getEntityName(), mdvo, null);
      } catch (Exception e) {
         throw new NuclosReportException(e);
      }
   }

   private void compileAndSaveXML(SubreportVO subreport) throws NuclosReportException {
      final String sReportXML;
      try {
         sReportXML = new String(subreport.getSourcefileContent().getData(), CHARENCODING);
      }
      catch (UnsupportedEncodingException ex) {
         throw new NuclosFatalException(ex);
      }
      final JasperReport jr = compileReport(sReportXML);
      subreport.setReportCLS(new ByteArrayCarrier(SerializationUtils.serialize(jr)));

      MasterDataVO mdvo = MasterDataWrapper.wrapSubreportVO(subreport);

      try {
         getMasterDataFacade().modify(NuclosEntity.SUBREPORT.getEntityName(), mdvo, null);
      } catch (Exception e) {
         throw new NuclosReportException(e);
      }
   }

   /**
    * compiles a report xml definition (jasperreports)
    * @param sReportXml report layout definition
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
    * @param iReportId id of report
    * @return collection of output formats
    */
   @Override
   public Collection<ReportOutputVO> getReportOutputs(Integer iReportId) {
      List<ReportOutputVO> outputs = new ArrayList<ReportOutputVO>();

      CollectableComparison cond = SearchConditionUtils.newMDReferenceComparison(
         MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.REPORTOUTPUT),"parent", iReportId);
      Collection<MasterDataVO> mdOutputs = getMasterDataFacade().getMasterData(NuclosEntity.REPORTOUTPUT.getEntityName(), cond, true);

      for (MasterDataVO mdVO : mdOutputs)
         outputs.add(MasterDataWrapper.getReportOutputVO(mdVO));

      return outputs;
   }

   @Override
public Collection<SubreportVO> getSubreports(Integer reportoutputId) {
      List<SubreportVO> subreports = new ArrayList<SubreportVO>();

      CollectableComparison cond = SearchConditionUtils.newMDReferenceComparison(
         MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.SUBREPORT), "reportoutput", reportoutputId);
      Collection<MasterDataVO> mdSubreports = getMasterDataFacade().getMasterData(NuclosEntity.SUBREPORT.getEntityName(), cond, true);

      for (MasterDataVO mdVO : mdSubreports) {
         subreports.add(new SubreportVO(mdVO));
      }

      return subreports;
   }

   /**
    * get output format for reportoutput id
    * @param iReportOutputId
    * @return reportoutput
    */
   @Override
public ReportOutputVO getReportOutput(Integer iReportOutputId) throws CommonFinderException, CommonPermissionException {
      return MasterDataWrapper.getReportOutputVO(getMasterDataFacade().get(NuclosEntity.REPORTOUTPUT.getEntityName(), iReportOutputId));
   }

   /**
    * finds reports (forms) by usage criteria
    * @param usagecriteria
    * @return collection of reports (forms)
    */
   @Override
@RolesAllowed("Login")
   public Collection<ReportVO> findReportsByUsage(UsageCriteria usagecriteria) {

      List<ReportVO> reports = new ArrayList<ReportVO>();

      DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
      DbQuery<Integer> query = builder.createQuery(Integer.class);
      DbFrom t = query.from("T_UD_REPORTUSAGE").alias(SystemFields.BASE_ALIAS);
      query.select(t.baseColumn("INTID_T_UD_REPORT", Integer.class));
      DbCondition cond = builder.equal(t.baseColumn("INTID_T_MD_MODULE", Integer.class), usagecriteria.getModuleId());

      DbColumnExpression<Integer> c = t.baseColumn("INTID_T_MD_PROCESS", Integer.class);
      final Integer iProcessId = usagecriteria.getProcessId();
      if (iProcessId == null) {
         query.where(builder.and(cond, c.isNull()));
      } else {
         query.where(builder.and(cond, builder.or(c.isNull(), builder.equal(c, iProcessId))));
      }

      List<Integer> collUsableReportIds = DataBaseHelper.getDbAccess().executeQuery(query);

      Map<ReportType,Collection<Integer>> readableReports = SecurityCache.getInstance().getReadableReports(getCurrentUserName());

      for (ReportType rt : readableReports.keySet()) {
         for (Integer reportId : CollectionUtils.intersection(collUsableReportIds, readableReports.get(rt))) {
            try {
               reports.add(MasterDataWrapper.getReportVO(getMasterDataFacade().get(NuclosEntity.REPORT.getEntityName(), reportId), getCurrentUserName()));
            }
            catch (CommonPermissionException ex) {
               throw new CommonFatalException(ex);
            }
            catch (CommonFinderException ex) {
               throw new CommonFatalException(ex);
            }
         }
      }

      Collections.sort(reports, new Comparator<ReportVO>(){

			@Override
			public int compare(ReportVO o1, ReportVO o2) {
				return StringUtils.emptyIfNull(o1.getName()).compareToIgnoreCase(StringUtils.emptyIfNull(o2.getName()));
			}});


      return reports;
   }

   /**
    * gets a report/form filled with data
    * @param iReportOutputId
    * @param mpParams parameters
    * @return report/form filled with data
    */
   @Override
   public JasperPrint prepareReport(Integer iReportOutputId, Map<String, Object> mpParams, Integer iMaxRowCount) throws CommonFinderException, NuclosReportException, CommonPermissionException {
      try {
         final ReportOutputVO reportoutput = getReportOutput(iReportOutputId);

         ByteArrayCarrier reportCLS = reportoutput.getReportCLS();
         final JasperReport jr = (reportCLS) != null ? deserializeJasperReportObject(reportCLS) : null;
         final String sSourceFileName = reportoutput.getSourceFile();
         if (sSourceFileName == null) {
            throw new NuclosReportException("report.error.missing.template.2");
               //"Es wurde keine JasperReport-Vorlage angegeben.\nBitte \u00fcberpr\u00fcfen Sie die Report-/Formular-Definition.");
         }
         if (jr == null) {
            throw new NuclosReportException("report.error.invalid.template");
               //"Die JasperReport-Vorlage wurde noch nicht kompiliert.\nBitte speichern Sie die Report-/Formular-Definition erneut.");
         }

         final Map<String, Object> mpParams2 = mpParams;

         for (SubreportVO subreport : getSubreports(iReportOutputId)) {
            String parametername = subreport.getParameter();

            ByteArrayCarrier subreportCLS = subreport.getReportCLS();
            final JasperReport jrsubreport = (reportCLS) != null ? deserializeJasperReportObject(subreportCLS) : null;
            final String subreportSourceFileName = subreport.getSourcefileName();
            if (subreportSourceFileName == null) {
               throw new NuclosReportException("subreport.error.missing.template");
                  //"Es wurde keine JasperReport-Vorlage angegeben.\nBitte \u00fcberpr\u00fcfen Sie die Report-/Formular-Definition.");
            }
            if (jr == null) {
               throw new NuclosReportException("subreport.error.invalid.template");
                  //"Die JasperReport-Vorlage wurde noch nicht kompiliert.\nBitte speichern Sie die Report-/Formular-Definition erneut.");
            }

            mpParams2.put(parametername, jrsubreport);
         }

         JasperPrint jprint = null;
         if (reportoutput.getDatasourceId() != null) {
        	// get existing connection (enlisted in current transaction)
        	Connection conn = DataSourceUtils.getConnection(NuclosDataSources.getDefaultDS());//DataBaseHelper.getConnection(NuclosDataSources.getDefaultDS());
            try {
               DatasourceFacadeLocal facade = ServiceLocator.getInstance().getFacade(DatasourceFacadeLocal.class);

               JRDefaultNuclosDataSource ds = new JRDefaultNuclosDataSource(facade.get(reportoutput.getDatasourceId()).getName(), mpParams, conn);

               mpParams2.put(JRParameter.REPORT_DATA_SOURCE, ds);
               mpParams2.put(JRParameter.REPORT_FILE_RESOLVER, new JRFileResolver());
               mpParams2.put(JRParameter.REPORT_LOCALE, getLocale(reportoutput.getLocale(), CommonLocaleDelegate.getLocale()));

               try {
                  jprint = JasperFillManager.fillReport(jr, mpParams2, ds);
               } catch (JRException e) {
                  throw new NuclosReportException(e.getMessage());
               } catch (DbException e) {
            	  throw new NuclosReportException(e);
               } catch (RuntimeException e) {
            	   throw new NuclosReportException(e.getMessage());
               }
               return jprint;
            }
            catch (CommonFinderException ex) {
               throw new NuclosFatalException(ex);
            }
            catch (CommonPermissionException ex) {
               throw new NuclosReportException(ex);
            }
            // connection will be closed by container after commit/rollback.
            /*finally {
            	try {
            		conn.close();
            	} catch (SQLException ex) {
            		error(ex);
            	}
            }*/
         }
         else {
            jprint = JasperFillManager.fillReport(jr, mpParams2, new JREmptyDataSource(1));
         }
         return jprint;
      }
      catch (JRException ex) {
         throw new NuclosReportException(ex);
      }
   }

	/**
	 * gets a report/form filled with data
	 *
	 * @param iReportOutputId
	 * @param mpParams parameters
	 * @return report/form filled with data
	 */
	@Override
	public NuclosFile prepareCsvReport(Integer iReportOutputId, Map<String, Object> mpParams, Integer iMaxRowCount) throws CommonFinderException, NuclosReportException, CommonPermissionException {
		final ReportOutputVO reportoutput = getReportOutput(iReportOutputId);

		final String sSourceFileName = reportoutput.getSourceFile();
		if (sSourceFileName == null) {
			throw new NuclosReportException("report.error.missing.template.2");
		}

		TemplatedCsvExport export = new TemplatedCsvExport(reportoutput);
		try {
			DatasourceFacadeLocal facade = ServiceLocator.getInstance().getFacade(DatasourceFacadeLocal.class);
			ResultVO rvo = facade.executeQuery(reportoutput.getDatasourceId(), mpParams, iMaxRowCount);

			return export.export(rvo, getLocale(reportoutput.getLocale(), CommonLocaleDelegate.getLocale()));
		} catch (CommonFinderException ex) {
			throw new NuclosFatalException(ex);
		} catch (NuclosDatasourceException ex) {
			throw new NuclosReportException(ex);
		}
	}

   private Locale getLocale(String locale, Locale def) {
	   if (!StringUtils.isNullOrEmpty(locale)) {
		   for (LocaleInfo li : ServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class).getAllLocales(true)) {
			   if (locale.equalsIgnoreCase(li.name)) {
				   return li.toLocale();
			   }
		   }
	   }
	   return def;
   }

   /**
    * gets an empty report/form
    * @param iReportOutputId report output id
    * @return empty report/form
    */
   @Override
public JasperPrint prepareEmptyReport(Integer iReportOutputId) throws CommonFinderException, NuclosReportException, CommonBusinessException {
      try {
         final ReportOutputVO reportoutput = getReportOutput(iReportOutputId);
         final JasperReport jr = deserializeJasperReportObject(reportoutput.getReportCLS());
         final JRDataSource jrdatasource = new JREmptyNuclosDataSource(1);

         final Map<String, Object> params = new HashMap<String, Object>();

         params.put("REPORT_DATA_SOURCE", jrdatasource);
         params.put("REPORT_FILE_RESOLVER", new JRFileResolver());

         for (SubreportVO subreport : getSubreports(iReportOutputId)) {
            String parametername = subreport.getParameter();
            final JasperReport jrsubreport = deserializeJasperReportObject(subreport.getReportCLS());
            params.put(parametername, jrsubreport);
         }

         return JasperFillManager.fillReport(jr, params, jrdatasource);
      }
      catch (CommonPermissionException ex) {
         throw new CommonBusinessException(ex);
      }
      catch (JRException ex) {
         throw new NuclosReportException(ex);
      }
   }

	private JasperReport deserializeJasperReportObject(ByteArrayCarrier b) throws CommonFatalException {
		Object obj;
		try {
			obj = IOUtils.fromByteArray(b.getData());
		}
		catch(IOException e) {
			throw new CommonFatalException(e);
		}
		catch(ClassNotFoundException e) {
			throw new CommonFatalException(e);
		}
		if(obj instanceof MarshalledValue) {
			obj = ((MarshalledValue) obj).get();
		}
		return (JasperReport) obj;
	}

   /**
    * gets search result report filled with data
	* <p>
	* TODO: Don't serialize CollectableEntityField and/or CollectableEntity! (tp)
	* Refer to {@link org.nuclos.common.CollectableEntityFieldWithEntity#readObject(ObjectInputStream)} for details.
	* </p>
    * @param jrDesign prepared design template
    * @param clctexpr search expression
    * @param iModuleId module id of module to be displayed
    * @param bIncludeSubModules Include submodules in search?
    * @return search result report filled with data
    */
   @Override
   @RolesAllowed("Login")
   public JasperPrint prepareSearchResult(CollectableSearchExpression clctexpr,
         List<? extends CollectableEntityField> lstclctefweSelected, Integer iModuleId, boolean bIncludeSubModules) throws NuclosReportException {
      try {
    	 final JasperDesign jrdesign = getJrDesignForSearchResult();
    	 PDFHelper.createFields(jrdesign, lstclctefweSelected);
         final JasperReport jr = JasperCompileManager.compileReport(jrdesign);
         return JasperFillManager.fillReport(jr, null, new SearchResultDataSource(clctexpr, lstclctefweSelected, iModuleId, bIncludeSubModules));
      }
      catch (FinderException ex) {
          throw new NuclosReportException("report.error.missing.template.3");//"Reportvorlage kann nicht gefunden werden.");
       }
      catch (JRException ex) {
         throw new NuclosReportException(ex);
      }
   }

   /**
    * @return search result report filled with data from the JTable
    * @throws NuclosReportException
    */
   @Override
@RolesAllowed("Login")
   public JasperPrint prepareTableModel(TableModel tableModel) throws NuclosReportException {
      try {
         final JasperDesign jrdesign = getJrDesignForSearchResult();
         PDFHelper.createFields(jrdesign, tableModel);
         return JasperFillManager.fillReport(JasperCompileManager.compileReport(jrdesign), null, new TableModelDataSource(tableModel));
      }
      catch (FinderException ex) {
         throw new NuclosReportException("report.error.missing.template.3");//"Reportvorlage kann nicht gefunden werden.");
      }
      catch (JRException ex) {
         throw new NuclosReportException(ex);
      }
   }

   /**
    * @return jasper design for the search result
    * @throws NuclosReportException
    * @throws FinderException
    */
   @Override
   @RolesAllowed("Login")
   @SuppressWarnings("deprecation")
   public JasperDesign getJrDesignForSearchResult() throws JRException, NuclosReportException, FinderException {
      InputStream input;

      byte[] data = ResourceCache.getInstance().getResource(ResourceVO.NCL_SEARCH_TEMPLATE);
      if (data != null) {
         input = new ByteArrayInputStream(data);
      } else {
         input = this.getClass().getClassLoader().getResourceAsStream("resources/reports/JR_SearchResultTemplate.xml");
         if (input == null) {
            throw new NuclosFatalException("Default search result template missing");
         }
      }

      JasperDesign jrdesignTemp = null;
      jrdesignTemp = JRXmlLoader.load(input);

      final JasperDesign result = new JasperDesign();
      result.setName(jrdesignTemp.getName());
      result.setTopMargin(jrdesignTemp.getTopMargin());
      result.setBottomMargin(jrdesignTemp.getBottomMargin());
      result.setLeftMargin(jrdesignTemp.getLeftMargin());
      result.setRightMargin(jrdesignTemp.getRightMargin());

      result.setColumnCount(jrdesignTemp.getColumnCount());
      result.setOrientation(jrdesignTemp.getOrientation());
      result.setTitleNewPage(jrdesignTemp.isTitleNewPage());
      result.setWhenNoDataType(jrdesignTemp.getWhenNoDataType());
      result.setTitle(jrdesignTemp.getTitle());
      result.setPageHeader(jrdesignTemp.getPageHeader());
      result.setColumnHeader(jrdesignTemp.getColumnHeader());
      result.setPageFooter(jrdesignTemp.getPageFooter());
      result.setDetail(jrdesignTemp.getDetail());

      final JRReportFont reportfontRegular = (JRReportFont) jrdesignTemp.getFontsMap().get("Regular");
      if (reportfontRegular == null) {
         throw new NuclosReportException("report.error.invalid.font.regular");//"Font mit dem Bezeichner 'Regular' muss in der Suchergebnisvorlage definiert sein.");
      }
      final JRReportFont reportfontBold = (JRReportFont) jrdesignTemp.getFontsMap().get("Bold");
      if (reportfontBold == null) {
         throw new NuclosReportException("report.error.invalid.font.bold");//"Font mit dem Bezeichner 'Bold' muss in der Suchergebnisvorlage definiert sein.");
      }
      result.addFont(reportfontRegular);
      result.addFont(reportfontBold);
      result.setDefaultFont(reportfontRegular);

      return result;
   }

   /**
    * @param iReportId report/form id
    * @return Is save allowed for the report/form with the given id?
    */
   @Override
@RolesAllowed("Login")
   public boolean isSaveAllowed(Integer iReportId) {
      return SecurityCache.getInstance().getWritableReportIds(getCurrentUserName()).contains(iReportId);
   }

    /**
     * finds reports readable for current user
     * @return collection of report ids
     */
   @Override
public Collection<Integer> getReadableReportIdsForCurrentUser() {
      Collection<Integer> result = new HashSet<Integer>();
      Map<ReportType,Collection<Integer>> readableReports = SecurityCache.getInstance().getReadableReports(getCurrentUserName());
      for (ReportType rt : readableReports.keySet())
         result.addAll(readableReports.get(rt));
      return result;
   }

   public static String getClassPathFor(Class<?>...classes) {
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
	      } catch (Exception e) {
	         throw new NuclosFatalException("Cannot configure JasperReports classpath ", e);
	      }
   	}
   	return sb.toString();
   }
}
