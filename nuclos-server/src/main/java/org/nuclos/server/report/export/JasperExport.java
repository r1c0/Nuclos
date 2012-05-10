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
package org.nuclos.server.report.export;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.sql.DataSource;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRReport;
import net.sf.jasperreports.engine.JRReportFont;
import net.sf.jasperreports.engine.JRTextElement;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignElement;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignImage;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.nuclos.common.MarshalledValue;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.NuclosFile;
import org.nuclos.common.NuclosImage;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.report.ByteArrayCarrier;
import org.nuclos.server.report.Export;
import org.nuclos.server.report.JRDefaultNuclosDataSource;
import org.nuclos.server.report.JREmptyNuclosDataSource;
import org.nuclos.server.report.JRFileResolver;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.ReportFieldDefinition;
import org.nuclos.server.report.ResultVODataSource;
import org.nuclos.server.report.ejb3.DatasourceFacadeLocal;
import org.nuclos.server.report.ejb3.ReportFacadeLocal;
import org.nuclos.server.report.valueobject.ReportOutputVO;
import org.nuclos.server.report.valueobject.ResultVO;
import org.nuclos.server.report.valueobject.SubreportVO;
import org.nuclos.server.resource.ResourceCache;
import org.nuclos.server.resource.valueobject.ResourceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@Configurable
public class JasperExport implements Export {

	private DataSource dataSource;

	private MasterDataFacadeLocal masterDatafacade;

	private ReportFacadeLocal reportfacade;

	private DatasourceFacadeLocal datasourceFacade;

	private final JRDataSource jrdatasource;

	public JasperExport() {
		this(null);
	}

	public JasperExport(JRDataSource jrdatasource) {
		super();
		this.jrdatasource = jrdatasource;
	}

	@Autowired
	@Qualifier("nuclos")
	void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Autowired
	public void setMasterDatafacade(MasterDataFacadeLocal masterDatafacade) {
		this.masterDatafacade = masterDatafacade;
	}

	@Autowired
	public void setReportfacade(ReportFacadeLocal reportfacade) {
		this.reportfacade = reportfacade;
	}

	@Autowired
	public void setDatasourceFacade(DatasourceFacadeLocal datasourceFacade) {
		this.datasourceFacade = datasourceFacade;
	}

	@Override
	public NuclosFile test(ReportOutputVO output) throws NuclosReportException {
		try {
			final JasperReport jr = deserializeJasperReportObject(output.getReportCLS());
			final JRDataSource jrdatasource = new JREmptyNuclosDataSource(1);

			final Map<String, Object> params = new HashMap<String, Object>();

			params.put("REPORT_DATA_SOURCE", jrdatasource);
			setDefaultsParameters(params, output);

			for (SubreportVO subreport : reportfacade.getSubreports(output.getId())) {
				String parametername = subreport.getParameter();
				final JasperReport jrsubreport = deserializeJasperReportObject(subreport.getReportCLS());
				params.put(parametername, jrsubreport);
			}

			JasperPrint jp = JasperFillManager.fillReport(jr, params, jrdatasource);
			String name = (output.getDescription() != null ? output.getDescription() : "Report") + output.getFormat().getExtension();
			return new NuclosFile(name, JasperExportManager.exportReportToPdf(jp));
		}
		catch (JRException ex) {
			throw new NuclosReportException(ex);
		}
	}
	
	private boolean isForPrinting(ReportOutputVO output) {
		switch (output.getDestination()) {
			case DEFAULT_PRINTER_CLIENT:
			case DEFAULT_PRINTER_SERVER:
			case PRINTER_CLIENT:
			case PRINTER_SERVER:
				return true;
			default:
				return false;
		}
	}

	@Override
	public NuclosFile export(ReportOutputVO output, Map<String, Object> params, Locale locale, int maxrows) throws NuclosReportException {
		try {
			Map<String, Object> mpParams2 = new HashMap<String, Object>(params);
			ByteArrayCarrier reportCLS = output.getReportCLS();
			final JasperReport jr = (reportCLS) != null ? deserializeJasperReportObject(reportCLS) : null;
			final String sSourceFileName = output.getSourceFile();
			if (sSourceFileName == null) {
				throw new NuclosReportException("report.error.missing.template.2");
			}
			if (jr == null) {
				throw new NuclosReportException("report.error.invalid.template");
			}

			for (SubreportVO subreport : reportfacade.getSubreports(output.getId())) {
				String parametername = subreport.getParameter();

				ByteArrayCarrier subreportCLS = subreport.getReportCLS();
				final JasperReport jrsubreport = (reportCLS) != null ? deserializeJasperReportObject(subreportCLS) : null;
				final String subreportSourceFileName = subreport.getSourcefileName();
				if (subreportSourceFileName == null) {
					throw new NuclosReportException("subreport.error.missing.template");
				}
				if (jr == null) {
					throw new NuclosReportException("subreport.error.invalid.template");
				}

				mpParams2.put(parametername, jrsubreport);
			}

			JasperPrint jprint = null;
			if (jrdatasource == null && output.getDatasourceId() != null) {
				// get existing connection (enlisted in current transaction)
				Connection conn = DataSourceUtils.getConnection(dataSource);
				try {
					JRDefaultNuclosDataSource ds = new JRDefaultNuclosDataSource(datasourceFacade.get(output.getDatasourceId()).getName(), params, conn);

					setDefaultsParameters(mpParams2, output);
					mpParams2.put(JRParameter.REPORT_DATA_SOURCE, ds);
					mpParams2.put(JRParameter.REPORT_LOCALE, locale);

					try {
						jprint = JasperFillManager.fillReport(jr, mpParams2, ds);
					}
					catch (JRException e) {
						throw new NuclosReportException(e.getMessage());
					}
					catch (DbException e) {
						throw new NuclosReportException(e);
					}
					catch (RuntimeException e) {
						throw new NuclosReportException(e.getMessage());
					}
				}
				catch (CommonFinderException ex) {
					throw new NuclosFatalException(ex);
				}
				catch (CommonPermissionException ex) {
					throw new NuclosReportException(ex);
				}
			}
			else {
				jprint = JasperFillManager.fillReport(jr, mpParams2, jrdatasource != null ? jrdatasource : new JREmptyDataSource(1));
			}
			String name = (output.getDescription() != null ? output.getDescription() : "Report") + output.getFormat().getExtension();
			if (isForPrinting(output)) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = null; 
				try {
					oos = new ObjectOutputStream(baos);
					oos.writeObject(jprint);
					return new NuclosFile(name, baos.toByteArray());
				}
				catch (IOException ex) {
					throw new NuclosReportException(ex);
				}
				finally {
					try {
						if (oos != null) {
							oos.close();
						}
					}
					catch (IOException e) { }
					try {
						baos.close();
					}
					catch (IOException e) { }
				}
			}
			else {
				return new NuclosFile(name, JasperExportManager.exportReportToPdf(jprint));
			}
		}
		catch (JRException ex) {
			throw new NuclosReportException(ex);
		}
	}

	@Override
	public NuclosFile export(ResultVO result, List<ReportFieldDefinition> fields) throws NuclosReportException {
		try {
			final JasperDesign jrdesign = getJrDesignForSearchResult();
			createFields(jrdesign, fields);
			JasperPrint jp = JasperFillManager.fillReport(JasperCompileManager.compileReport(jrdesign), null, new ResultVODataSource(result, fields));
			return new NuclosFile("Export" + ReportOutputVO.Format.PDF.getExtension(), JasperExportManager.exportReportToPdf(jp));
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
		catch (IOException e) {
			throw new CommonFatalException(e);
		}
		catch (ClassNotFoundException e) {
			throw new CommonFatalException(e);
		}
		if (obj instanceof MarshalledValue) {
			obj = ((MarshalledValue) obj).get();
		}
		return (JasperReport) obj;
	}

	private void setDefaultsParameters(Map<String, Object> parameters, ReportOutputVO output) {
		parameters.put(JRParameter.REPORT_FILE_RESOLVER, new JRFileResolver());
		String username = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		parameters.put("NUCLOS_USER_NAME", username);
		final CollectableComparison cond = SearchConditionUtils.newMDComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.USER), "name", ComparisonOperator.EQUAL, username);
		final Collection<MasterDataVO> collmdvo = masterDatafacade.getMasterData(NuclosEntity.USER.getEntityName(), cond, false);
		if (collmdvo.size() == 1) {
			MasterDataVO user = collmdvo.iterator().next();
			parameters.put("NUCLOS_USER_EMAIL", user.getField("email"));
			parameters.put("NUCLOS_USER_FIRSTNAME", user.getField("firstname"));
			parameters.put("NUCLOS_USER_LASTNAME", user.getField("lastname"));
		}
	}

	@SuppressWarnings("deprecation")
	public JasperDesign getJrDesignForSearchResult() throws JRException, NuclosReportException {
		InputStream input;

		byte[] data = ResourceCache.getInstance().getResource(ResourceVO.NCL_SEARCH_TEMPLATE);
		if (data != null) {
			input = new ByteArrayInputStream(data);
		}
		else {
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
			throw new NuclosReportException("report.error.invalid.font.regular");// "Font mit dem Bezeichner 'Regular' muss in der Suchergebnisvorlage definiert sein.");
		}
		final JRReportFont reportfontBold = (JRReportFont) jrdesignTemp.getFontsMap().get("Bold");
		if (reportfontBold == null) {
			throw new NuclosReportException("report.error.invalid.font.bold");// "Font mit dem Bezeichner 'Bold' muss in der Suchergebnisvorlage definiert sein.");
		}
		result.addFont(reportfontRegular);
		result.addFont(reportfontBold);
		result.setDefaultFont(reportfontRegular);

		return result;
	}

	@SuppressWarnings("deprecation")
	public static void createFields(JasperDesign jrdesign, List<ReportFieldDefinition> fields) {
		final int DIN_A4_HEIGHT = 842;
		final int DIN_A4_WIDTH = 595;
		final int MAX_DATE_WIDTH = 10;
		final int MAX_STRING_WIDTH = 25; // extended = long strings width char >
											// 25
		final int MAX_BOOLEAN_WIDTH = 4;
		final int MAX_INTEGER_WIDTH = 5;
		final int MAX_DOUBLE_WIDTH = 7;

		final JRDesignBand pageHeader = (JRDesignBand) jrdesign.getPageHeader();
		final JRDesignBand columnHeader = (JRDesignBand) jrdesign.getColumnHeader();
		final JRDesignBand detail = (JRDesignBand) jrdesign.getDetail();
		final JRReportFont regularFont = (JRReportFont) jrdesign.getFontsMap().get("Regular");
		final JRReportFont boldFont = (JRReportFont) jrdesign.getFontsMap().get("Bold");
		final Font fontPlain = new Font(regularFont.getName(), Font.PLAIN, regularFont.getSize());
		final Font fontBold = new Font(regularFont.getName(), Font.BOLD, boldFont.getSize());
		if (pageHeader == null) {
			throw new CommonFatalException(SpringLocaleDelegate.getInstance().getMessage("ReportController.5", "Bereich <PageHeader> muss in der Suchergebnisvorlage definiert sein."));
		}
		if (columnHeader == null) {
			throw new CommonFatalException(SpringLocaleDelegate.getInstance().getMessage("ReportController.3", "Bereich <ColumnHeader> muss in der Suchergebnisvorlage definiert sein."));
		}
		if (detail == null) {
			throw new CommonFatalException(SpringLocaleDelegate.getInstance().getMessage("ReportController.4", "Bereich <Detail> muss in der Suchergebnisvorlage definiert sein."));
		}

		// set the column headers:
		final FontRenderContext fontrenderctx = new FontRenderContext(null, false, true);
		int iLabelWidth = 0;
		int iFieldWidth = 0;
		int iCurrentX = 0;

		final double dCharWidth = fontPlain.getStringBounds("M", fontrenderctx).getWidth() + 1;
		final int iIntegerFieldWidth = (int) dCharWidth * MAX_INTEGER_WIDTH;
		final int iDoubleFieldWidth = (int) dCharWidth * MAX_DOUBLE_WIDTH;
		final int iDateFieldWidth = (int) dCharWidth * MAX_DATE_WIDTH;
		final int iBooleanFieldWidth = (int) dCharWidth * MAX_BOOLEAN_WIDTH;
		final int iStringFieldWidth = (int) dCharWidth * MAX_STRING_WIDTH;

		for (ReportFieldDefinition f : fields) {
			final String sLabel = f.getLabel();
			iLabelWidth = (int) fontBold.getStringBounds(sLabel, fontrenderctx).getWidth() + 1;

			byte textAlign = JRTextElement.TEXT_ALIGN_LEFT;

			final String sClassName = f.getJavaClass().getName();
			if (Date.class.isAssignableFrom(f.getJavaClass())) {
				iFieldWidth = Math.max(iLabelWidth, iDateFieldWidth);
			}
			else if (sClassName.equals("java.lang.String")) {
				final Integer iScale = f.getMaxLength();
				if (iScale != null && iScale > MAX_STRING_WIDTH) {
					iFieldWidth = Math.max(iLabelWidth, iStringFieldWidth);
				}
				else {
					iFieldWidth = iLabelWidth + 10;
				}
			}
			else if (sClassName.equals("java.lang.Boolean")) {
				iFieldWidth = Math.max(iLabelWidth, iBooleanFieldWidth);
			}
			else if (sClassName.equals("java.lang.Integer")) {
				iFieldWidth = Math.max(iLabelWidth, iIntegerFieldWidth);
				textAlign = JRTextElement.TEXT_ALIGN_RIGHT;
			}
			else if (sClassName.equals("java.lang.Double")) {
				iFieldWidth = Math.max(iLabelWidth, iDoubleFieldWidth);
				textAlign = JRTextElement.TEXT_ALIGN_RIGHT;
			}
			else if (Timestamp.class.isAssignableFrom(f.getJavaClass())) {
				iFieldWidth = Math.max(iLabelWidth, iDateFieldWidth);
			}
			else {
				iFieldWidth = MAX_STRING_WIDTH;
			}

			final String sFieldName = f.getName();

			final JRDesignField jrdesignfield = new JRDesignField();
			jrdesignfield.setName(sFieldName);
			try {
				if (!sClassName.equals(NuclosImage.class.getName())) {
					jrdesignfield.setValueClass(String.class);
				}
				else {
					jrdesignfield.setValueClass(java.awt.Image.class);
				}
				jrdesign.addField(jrdesignfield);
			}
			catch (JRException ex) {
				throw new CommonFatalException(ex);
			}

			detail.setSplitAllowed(true);

			JRDesignStaticText staticField = new JRDesignStaticText();
			staticField.setX(iCurrentX);
			staticField.setWidth(iFieldWidth);
			staticField.setHeight(14);
			staticField.setTextAlignment(textAlign);
			staticField.setText(sLabel);
			staticField.setFont(boldFont);
			columnHeader.addElement(staticField);

			JRDesignElement dataField;
			if (!sClassName.equals(NuclosImage.class.getName())) {
				dataField = new JRDesignTextField();

				((JRDesignTextField) dataField).setTextAlignment(textAlign);
				((JRDesignTextField) dataField).setVerticalAlignment(JRDesignImage.VERTICAL_ALIGN_MIDDLE);
				((JRDesignTextField) dataField).setFont(regularFont);
				((JRDesignTextField) dataField).setBlankWhenNull(true);

				final String sFieldValue = "$F{" + sFieldName + "}";

				JRDesignExpression expression = new JRDesignExpression();
				expression.setText(sFieldValue);
				expression.setValueClass(String.class);
				((JRDesignTextField) dataField).setExpression(expression);
				((JRDesignTextField) dataField).setStretchWithOverflow(true);
			}
			else {
				dataField = new JRDesignImage(jrdesign);

				((JRDesignImage) dataField).setHorizontalAlignment(JRDesignImage.HORIZONTAL_ALIGN_CENTER);
				((JRDesignImage) dataField).setVerticalAlignment(JRDesignImage.VERTICAL_ALIGN_MIDDLE);

				final String sFieldValue = "$F{" + sFieldName + "}";

				JRDesignExpression expression = new JRDesignExpression();
				expression.setText(sFieldValue);
				expression.setValueClass(java.awt.Image.class);
				((JRDesignImage) dataField).setExpression(expression);
				((JRDesignImage) dataField).setMode(JRElement.MODE_TRANSPARENT);
			}

			dataField.setHeight(14);
			dataField.setX(iCurrentX);
			dataField.setWidth(iFieldWidth);

			detail.addElement(dataField);

			iCurrentX += iFieldWidth + 10;
		}
		iCurrentX += 50;

		if (iCurrentX < DIN_A4_WIDTH) {
			jrdesign.setOrientation(JRReport.ORIENTATION_PORTRAIT);
			jrdesign.setPageWidth(DIN_A4_WIDTH);
			jrdesign.setPageHeight(DIN_A4_HEIGHT);
		}
		else if (iCurrentX < DIN_A4_HEIGHT) {
			jrdesign.setOrientation(JRReport.ORIENTATION_LANDSCAPE);
			jrdesign.setPageWidth(DIN_A4_HEIGHT);
			jrdesign.setPageHeight(DIN_A4_WIDTH);
		}
		else {
			jrdesign.setOrientation(JRReport.ORIENTATION_LANDSCAPE);
			jrdesign.setPageWidth(iCurrentX);
			jrdesign.setPageHeight((int) (iCurrentX / 1.41));
		}
	}
}
