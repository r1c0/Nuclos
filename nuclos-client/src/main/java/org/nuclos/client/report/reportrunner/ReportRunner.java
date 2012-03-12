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
package org.nuclos.client.report.reportrunner;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.Future;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import net.sf.jasperreports.engine.JasperPrint;

import org.apache.log4j.Logger;
import org.nuclos.client.datasource.DatasourceDelegate;
import org.nuclos.client.datasource.admin.ParameterPanel;
import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.main.Main;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.report.ReportDelegate;
import org.nuclos.client.report.reportrunner.export.CSVExport;
import org.nuclos.client.report.reportrunner.export.DOCExport;
import org.nuclos.client.report.reportrunner.export.FileExport;
import org.nuclos.client.report.reportrunner.export.PDFExport;
import org.nuclos.client.report.reportrunner.export.XLSExport;
import org.nuclos.client.ui.CommonInterruptibleProcess;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.NuclosFile;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.ejb3.DatasourceFacadeRemote;
import org.nuclos.server.report.valueobject.DatasourceParameterVO;
import org.nuclos.server.report.valueobject.DatasourceVO;
import org.nuclos.server.report.valueobject.ReportOutputVO;
import org.nuclos.server.report.valueobject.ReportVO;
import org.nuclos.server.report.valueobject.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Create a new thread in which a <code>ReportExporter<code> is executed.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @author	<a href="mailto:rostislav.maksymovskyi@novabit.de">rostislav.maksymovskyi</a>
 * @version 02.00.00
 */
@Configurable
public class ReportRunner implements Runnable, BackgroundProcessInfo, CommonInterruptibleProcess {

	private final static Logger log = Logger.getLogger(ReportRunner.class);

	public static final String KEY_GENERICOBJECTIDENTIFIER = "GenericObjectIdentifier";

	private final Component parent;
	private final Map<String, Object> mpParams;

	private final String sJobName;
	private final String sDatasourceName;
	private volatile Status status = Status.NOTRUNNING;
	private volatile Date dateStartTime;
	private volatile String message;

	private static final int REPORT_JOB = 1;		// Either a report or a form
	private static final int SHOW_EXPORT_JOB = 2;	// Export of a search result list, task list, timelimit list etc.

	private final int iJobType;
	private final ReportVO reportvo;
	private final ReportOutputVO reportoutputvo;
	private final ReportOutputVO.Format format;
	private final boolean bExecuteDatasource;
	private final Integer iMaxRowCount;

	private String reportFilename;

	private JasperPrint jasperPrint = null;
	private ResultVO resultVO = null;

	private boolean bInitialized = false;

	private Future<?> future = null;
	private Observable observable = null;

	private final ReportAttachmentInfo attachmentInfo;
	
	private SpringLocaleDelegate localeDelegate;
	
	/**
	 * @param parent
	 * @param mpParameter
	 * @param outputVO
	 * @param bExecuteDatasource
	 * @param iMaxRowCount
	 * @param attachmentInfo
	 * @return the newly created report job thread
	 */
	public static ReportThread createJob(Component parent, Map<String, Object> mpParameter, ReportVO reportVO, ReportOutputVO outputVO, boolean bExecuteDatasource, Integer iMaxRowCount, String sReportFilename, ReportAttachmentInfo attachmentInfo) {
		ReportThread result = null;
		final ReportRunner runner = new ReportRunner(parent, mpParameter, reportVO, outputVO, bExecuteDatasource, iMaxRowCount, sReportFilename, attachmentInfo);
		result = new ReportThread(runner);

		final BackgroundProcessStatusTableModel model = BackgroundProcessStatusController.getStatusDialog(UIUtils.getFrameForComponent(parent)).getStatusPanel().getModel();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				model.addEntry(runner);
			}
		});

		return result;
	}

	public static ReportThread createJob(Component parent, Map<String, Object> mpParameter, ReportVO reportVO, ReportOutputVO outputVO, boolean bExecuteDatasource, Integer iMaxRowCount) {
		return createJob(parent, mpParameter, reportVO, outputVO, bExecuteDatasource, iMaxRowCount, null, null);
	}

	public static ReportThread createJob(Component parent, Map<String, Object> mpParameter, ReportVO reportVO, ReportOutputVO outputVO, boolean bExecuteDatasource, Integer iMaxRowCount, ReportAttachmentInfo attachmentInfo) {
		return createJob(parent, mpParameter, reportVO, outputVO, bExecuteDatasource, iMaxRowCount, null, attachmentInfo);
	}

	/**
	 * @param parent
	 * @param printObj
	 * @param format
	 * @param iMaxRowCount
	 */
	public static ReportThread createExportJob(Component parent, JasperPrint printObj, ReportOutputVO.Format format, Integer iMaxRowCount, String sDatasourceName ) {
		return new ReportThread(new ReportRunner(parent, new HashMap<String, Object>(), printObj, format, true, iMaxRowCount, sDatasourceName));
	}

	/**
	 * used for CSV and XLS export
	 *
	 * @param parent
	 * @param resultVO the reports data
	 * @param format
	 * @param iMaxRowCount
	 */
	public static ReportThread createExportJob(Component parent, ResultVO resultVO, ReportOutputVO.Format format, Integer iMaxRowCount, String sDatasourceName) {
		return new ReportThread(new ReportRunner(parent, new HashMap<String, Object>(), resultVO, format, true, iMaxRowCount, sDatasourceName));
	}

	/**
	 * @param parent
	 * @param mpParams
	 * @param reportvo
	 * @param outputvo
	 * @param bExecuteDatasource
	 * @param iMaxRowCount
	 */
	private ReportRunner(Component parent, Map<String, Object> mpParams, ReportVO reportvo, ReportOutputVO outputvo, boolean bExecuteDatasource, Integer iMaxRowCount, String sReportFilename, ReportAttachmentInfo attachmentInfo) {
		this.parent = parent;
		this.mpParams = mpParams;
		this.sJobName = reportvo.getName();
		this.sDatasourceName = (reportvo.getOutputType() == ReportVO.OutputType.SINGLE) ? null : outputvo.getDatasource();

		this.format = null;
		this.reportvo = reportvo;
		this.reportoutputvo = outputvo;
		this.iJobType = REPORT_JOB;
		this.bExecuteDatasource = bExecuteDatasource;
		this.iMaxRowCount = iMaxRowCount;

		this.reportFilename = sReportFilename;

		this.attachmentInfo = attachmentInfo;

		assert this.jasperPrint == null;

		bInitialized = true;
	}

	/**
	 * @param parent
	 * @param printObj
	 * @param format
	 * @param bExecuteDatasource
	 * @param iMaxRowCount
	 */
	private ReportRunner(Component parent, Map<String, Object> mpParams, JasperPrint printObj, ReportOutputVO.Format format, boolean bExecuteDatasource, Integer iMaxRowCount, String sDatasourceName) {
		this.parent = parent;
		this.mpParams = mpParams;
		this.sJobName = "";
		this.sDatasourceName = sDatasourceName;

		this.format = format;
		this.jasperPrint = printObj;
		this.reportvo = null;
		this.reportoutputvo = null;
		this.iJobType = SHOW_EXPORT_JOB;
		this.bExecuteDatasource = bExecuteDatasource;
		this.iMaxRowCount = iMaxRowCount;

		this.attachmentInfo = null;

		bInitialized = true;
	}

	/**
	 * @param parent
	 * @param resultvo
	 * @param format
	 * @param bExecuteDatasource
	 * @param iMaxRowCount
	 */
	private ReportRunner(Component parent, Map<String, Object> mpParams, ResultVO resultvo, ReportOutputVO.Format format, boolean bExecuteDatasource, Integer iMaxRowCount, String sDatasourceName) {
		this.parent = parent;
		this.mpParams = mpParams;
		this.sJobName = "";
		this.sDatasourceName = sDatasourceName;

		this.resultVO = resultvo;
		this.format = format;
		this.reportvo = null;
		this.reportoutputvo = null;
		this.iJobType = SHOW_EXPORT_JOB;
		this.bExecuteDatasource = bExecuteDatasource;
		this.iMaxRowCount = iMaxRowCount;

		this.attachmentInfo = null;

		assert this.jasperPrint == null;

		bInitialized = true;
	}
	
	@Autowired
	void setSpringLocaleDelegate(SpringLocaleDelegate cld) {
		this.localeDelegate = cld;
	}

	@Override
	public void setBackgroundProcessInterruptionIntervalForCurrentThread() throws InterruptedException {
		Thread.sleep(GENERAL_INTERRUPTION_INTERVAL);
	}

	@Override
	public void addObservable(Observable observable){
		this.observable = observable;
	}

	@Override
	public void cancelProzess(){
		if(this.future != null){
			boolean cancelled = this.future.cancel(true);
			log.debug("cancelProzess>>>>>>>>>> cancelled future: "+cancelled);
		}
	}

	@Override
	public Future<?> getProcessFuture() {
		return future;
	}

	public void setProcessFuture(Future<?> future) {
		this.future = future;
	}

	@Override
	public void run() {
		try {
			if (!bInitialized) {
				// Note that each instance of this class can be run only once:
				throw new NuclosFatalException("job == null");
			}

			this.setStatus(Status.RUNNING);
			this.dateStartTime = new Date(Calendar.getInstance().getTimeInMillis());

			this.execute();

			if (attachmentInfo != null) {
				String filename = attachDocument();
				this.setStatus(Status.DONE);
				this.setMessage(localeDelegate.getMessage(
						"ReportRunner.fileattached", "Document {0} hast been attached to object {1}.", 
						filename, attachmentInfo.getGenericObjectIdentifier()));
			}
			else {
				this.setStatus(Status.DONE);
			}
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			this.setStatus(Status.CANCELLED);
			this.setMessage(localeDelegate.getMessage("ReportRunner.1", "Der Prozess wurde abgebrochen."));
		}
		catch (final Exception ex) {
			this.setStatus(Status.ERROR);
			this.setMessage(ex.getMessage());
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					Errors.getInstance().showExceptionDialog(UIUtils.getFrameForComponent(parent), ex);
				}
			});
		}
		//only for development
//		catch (final Throwable ex) {
//			SwingUtilities.invokeLater(new Runnable() {
//				public void run() {
//					Errors.getInstance().showExceptionDialog(UIUtils.getFrameForComponent(parent), ex);
//				}
//			});
//		}
		finally {
			// release the job - avoid memory leak:
//			this.job = null;
		}
	}

	private void execute() throws NuclosReportException, InterruptedException {
		try {
			final ReportExporter exporter;
			switch (iJobType) {
				case REPORT_JOB:
					final ReportOutputVO.Format outputFormat = reportoutputvo.getFormat();
					exporter = getExporter(outputFormat);

					if (mpParams.containsKey(KEY_GENERICOBJECTIDENTIFIER)) {
						exporter.setGenericObjectIdentifier(mpParams.get(KEY_GENERICOBJECTIDENTIFIER).toString());
					}

					if(reportFilename != null)
						exporter.setReportFileName(reportFilename);

					if (outputFormat == ReportOutputVO.Format.PDF) {
						// "graphical" report (means JasperReport...)
						if (bExecuteDatasource) {
							jasperPrint = ReportDelegate.getInstance().prepareReport(reportoutputvo.getId(), mpParams, iMaxRowCount);
						}
						else {
							jasperPrint = ReportDelegate.getInstance().prepareEmptyReport(reportoutputvo.getId());
						}
						setBackgroundProcessInterruptionIntervalForCurrentThread();
						new PDFExport().export((reportoutputvo.getDescription() != null) ? reportoutputvo.getDescription() : reportvo.getName(), jasperPrint, reportoutputvo.getParameter(), reportoutputvo.getDestination());
					}
					else if (outputFormat == ReportOutputVO.Format.CSV && reportoutputvo.getSourceFile() != null) {
						NuclosFile result = ReportDelegate.getInstance().prepareCsvReport(reportoutputvo.getId(), mpParams, iMaxRowCount);
						setBackgroundProcessInterruptionIntervalForCurrentThread();
						new FileExport(result).export((reportoutputvo.getDescription() != null) ? reportoutputvo.getDescription() : reportvo.getName(), reportoutputvo.getParameter(), reportoutputvo.getDestination());
					}
					else {
						// report without a layout
						if (bExecuteDatasource) {
							resultVO = DatasourceDelegate.getInstance().executeQuery(reportoutputvo.getDatasourceId(), mpParams, null);
						}
						else {
							resultVO = new ResultVO(); // empty ResultSet
						}
						setBackgroundProcessInterruptionIntervalForCurrentThread();
						exporter.export(resultVO, reportvo, reportoutputvo);
					}
					break;

				case SHOW_EXPORT_JOB:
					exporter = getExporter(format);

					if (this.jasperPrint != null) {
						// is called for printing the search result with PDF format.
						assert exporter.getClass().equals(PDFExport.class);

						setBackgroundProcessInterruptionIntervalForCurrentThread();
						new PDFExport().export((sDatasourceName != null) ? sDatasourceName : localeDelegate.getMessage("ReportRunner.2", "Suchergebnis"), 
								jasperPrint, null, reportoutputvo != null ? reportoutputvo.getDestination() : ReportOutputVO.Destination.SCREEN);

					}
					else if (this.resultVO != null) {
						setBackgroundProcessInterruptionIntervalForCurrentThread();
						exporter.export(resultVO, new ReportVO((sDatasourceName != null) ? sDatasourceName 
								: localeDelegate.getMessage("ReportRunner.2", "Suchergebnis")), 
								new ReportOutputVO(format, ReportOutputVO.Destination.SCREEN, null));
					}
					else {
						throw new NuclosReportException(localeDelegate.getMessage("ReportRunner.3", "Report nicht initialisiert."));
					}
					break;

				default:
					throw new NuclosReportException(localeDelegate.getMessage("ReportRunner.3", "Report nicht initialisiert."));
			}
		}
		catch (CommonBusinessException ex) {
			throw new NuclosReportException(ex);
		}
	}

	/** @todo refactor: use OutputFormat as parameter */
	private static ReportExporter getExporter(ReportOutputVO.Format format) throws NuclosReportException {
		if (format != null) {
			switch (format) {
			case PDF:
				return new PDFExport();
			case XLS:
				return new XLSExport();
			case CSV:
				return new CSVExport();
			case TSV:
				return new CSVExport('\t', 0, ' ', false, ".tsv");
			case DOC:
				return new DOCExport();
			}
		}
		throw new NuclosReportException("Unsupported report format: " + format);
	}

	/**
	 * get parameter values from user
	 *
	 * @param collFormat
	 * @param mpParams
	 */
	public static boolean prepareParameters(Collection<ReportOutputVO> collFormat, Map<String, Object> mpParams) throws NuclosReportException {
		final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
		boolean result = true;

		final Collection<Integer> collDatasourceId = new HashSet<Integer>();
		for (ReportOutputVO rovo : collFormat) {
			collDatasourceId.add(rovo.getDatasourceId());
		}

		try {
			//NUCLEUSINT-182 Arraylists instead of Sets
			final List<String> liParamNamesInThisDatasource = new ArrayList<String>();
			final List<String> liParamNamesInAllDatasources = new ArrayList<String>();
			final List<DatasourceParameterVO> liParamsEmpty = new ArrayList<DatasourceParameterVO>();

			final DatasourceFacadeRemote datasourcefacade = ServiceLocator.getInstance().getFacade(DatasourceFacadeRemote.class);
			for (Integer iDatasourceId : collDatasourceId) {
				if (iDatasourceId == null)
					throw new NuclosReportException(localeDelegate.getMessage("ReportRunner.4", "Keine Datenquelle angegeben"));

				final DatasourceVO datasourcevo = datasourcefacade.get(iDatasourceId);

				for (DatasourceParameterVO paramvo : datasourcefacade.getParameters(datasourcevo.getSource())) {
					final String sParamName = paramvo.getParameter();
					liParamNamesInThisDatasource.add(sParamName);
					if (!liParamNamesInAllDatasources.contains(sParamName)) {
						liParamsEmpty.add(paramvo);
						liParamNamesInAllDatasources.add(sParamName);
					}
				}
				if (mpParams.containsKey("intid")) {
					if (!liParamNamesInThisDatasource.contains("intid")) {
						throw new NuclosReportException(
							localeDelegate.getMessage("ReportRunner.5", "Die dem Formular zugrundeliegende Datenquelle \"{0}\" muss den Parameter intid definieren.", datasourcevo.getName()));
					}
				}
				liParamNamesInThisDatasource.clear();
			}

			if (!liParamsEmpty.isEmpty()) {
				final ParameterPanel panel = new ParameterPanel(liParamsEmpty);

				result = (JOptionPane.showOptionDialog(Main.getInstance().getMainFrame(), panel, 
						localeDelegate.getMessage("ReportRunner.8", "Parameter"), 
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.OK_OPTION);
				if (result) {
					panel.fillParameterMap(liParamsEmpty, mpParams);
				}
			}
		}
		catch (RuntimeException ex) {
			throw new NuclosFatalException(ex);
		}
		catch (CommonBusinessException ex) {
			throw new NuclosReportException(ex);
		}

		return result;
	}

	private String attachDocument() throws NuclosBusinessException {
		if (attachmentInfo == null) {
			throw new NuclosFatalException("ReportAttachmentInfo not set.");
		}

		try {
			final File file = new File(getDocumentName());
			final byte[] abFileContent = IOUtils.readFromBinaryFile(file);
			final GenericObjectDocumentFile loFile = new GenericObjectDocumentFile(file.getName(), abFileContent);
			loFile.setDirectoryPath(attachmentInfo.getDirectory());

			final MasterDataVO mdvo = new MasterDataVO(MasterDataDelegate.getInstance().getMetaData(attachmentInfo.getDocumentEntityName()), false);
			mdvo.setField("genericObject", attachmentInfo.getGenericObjectId());
			mdvo.setField("entity", attachmentInfo.getDocumentEntityName());
			mdvo.setField(attachmentInfo.getDocumentFieldNames()[0], localeDelegate.getMessage("ReportController.2", "Automatisch angef\u00fcgtes Dokument"));
			mdvo.setField(attachmentInfo.getDocumentFieldNames()[1], new Date());
			mdvo.setField(attachmentInfo.getDocumentFieldNames()[2], Main.getInstance().getMainController().getUserName());
			if(NuclosEntity.GENERALSEARCHDOCUMENT.getEntityName().equals(attachmentInfo.getDocumentEntityName())) {
				mdvo.setField("file", loFile);
				mdvo.setField("path", attachmentInfo.getDirectory());
			}
			else {
				mdvo.setField(attachmentInfo.getDocumentFieldNames()[3], loFile);
			}

			GenericObjectDelegate.getInstance().attachDocumentToObject(mdvo);
			return file.getName();
		}
		catch (Exception ex) {
			throw new NuclosReportException(localeDelegate.getMessage(
					"ReportController.1","Anh\u00e4ngen der Datei \"{0}\" an GenericObject \"{1}\" fehlgeschlagen.", 
					getDocumentName(), attachmentInfo.getGenericObjectIdentifier()), ex);
		}
	}

	private String getDocumentName() {
		if (Thread.currentThread().getClass().equals(ReportThread.class)) {
			/** @todo refactor! */
			return ((ReportThread) Thread.currentThread()).getDocumentName();
		}
		throw new NuclosFatalException("Invalid thread type.");
	}

	/**
	 * @return the current status
	 */
	@Override
	public Status getStatus() {
		return this.status;
	}

	private void setStatus(Status status) {
		this.status = status;
		if(this.observable != null){
			this.observable.notifyObservers();
		}
	}

	/**
	 * @return the name of the report job
	 */
	@Override
	public String getJobName() {
		String result = localeDelegate.getMessage("ReportRunner.6", "Report: \"{0}\"", this.sJobName);
		if (sDatasourceName != null) {
			result = localeDelegate.getMessage("ReportRunner.7", "Report: \"{0}\", Datenquelle: {1}", this.sJobName, sDatasourceName);
		}
		return result;
	}

	/**
	 * @return the start time of the job
	 */
	@Override
	public Date getStartedAt() {
		return this.dateStartTime;
	}

	/** @todo use error message from exception */
	@Override
	public String getMessage() {
		return message;
	}

	private void setMessage(String message) {
		this.message = message;
	}

}	// class ReportRunner
