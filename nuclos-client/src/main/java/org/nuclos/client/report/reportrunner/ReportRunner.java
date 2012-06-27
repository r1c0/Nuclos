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
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.Future;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.ServiceUI;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.apache.xbean.spring.context.SpringApplicationContext;
import org.nuclos.client.datasource.admin.ParameterPanel;
import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.main.Main;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.report.ReportDelegate;
import org.nuclos.client.report.reportrunner.source.DefaultReportSource;
import org.nuclos.client.report.reportrunner.source.ResultVoReportSource;
import org.nuclos.client.report.reportrunner.source.SearchExpressionReportSource;
import org.nuclos.client.report.reportrunner.source.WordReportSource;
import org.nuclos.client.ui.CommonInterruptibleProcess;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.*;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.SystemUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.NuclosReportPrintJob;
import org.nuclos.server.report.NuclosReportRemotePrintService;
import org.nuclos.server.report.ejb3.DatasourceFacadeRemote;
import org.nuclos.server.report.print.CSVPrintJob;
import org.nuclos.server.report.print.DOCPrintJob;
import org.nuclos.server.report.print.FilePrintJob;
import org.nuclos.server.report.print.PDFPrintJob;
import org.nuclos.server.report.print.XLSPrintJob;
import org.nuclos.server.report.valueobject.DatasourceParameterVO;
import org.nuclos.server.report.valueobject.DatasourceVO;
import org.nuclos.server.report.valueobject.ReportOutputVO;
import org.nuclos.server.report.valueobject.ReportOutputVO.Destination;
import org.nuclos.server.report.valueobject.ReportOutputVO.Format;
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
 * @author <a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @author <a href="mailto:rostislav.maksymovskyi@novabit.de">rostislav.maksymovskyi</a>
 * @version 02.00.00
 */
@Configurable
public class ReportRunner implements Runnable, BackgroundProcessInfo, CommonInterruptibleProcess {

	private final static Logger log = Logger.getLogger(ReportRunner.class);

	private final ReportSource source;

	private final Component parent;

	private final String reportname;
	private final String directory;
	private final String filename;

	private final ReportOutputVO.Format format;
	private final ReportOutputVO.Destination destination;

	private volatile Status status = Status.NOTRUNNING;
	private volatile Date dateStartTime;
	private volatile String message;

	private Future<?> future = null;
	private Observable observable = null;

	private final ReportAttachmentInfo attachmentInfo;
	
	// Spring injection

	private SpringLocaleDelegate localeDelegate;
	
	private DatasourceFacadeRemote datasourceFacadeRemote;
	
	// end of Spring injection

	/**
	 * Called for forms and reports (with real ReportVO and ReportOutputVO)
	 * 
	 * @param parent
	 * @param mpParameter
	 * @param outputVO
	 * @param bExecuteDatasource
	 * @param iMaxRowCount
	 * @param attachmentInfo
	 * @return the newly created report job thread
	 */
	public static ReportThread createJob(Component parent, Map<String, Object> mpParameter, ReportVO reportVO, ReportOutputVO outputVO, boolean bExecuteDatasource, Integer iMaxRowCount, String sReportFilename,
			ReportAttachmentInfo attachmentInfo) {
		final ReportSource source;
		if (outputVO.getFormat() == Format.DOC) {
			source = new WordReportSource(outputVO, mpParameter, iMaxRowCount);
		}
		else {
			source = new DefaultReportSource(outputVO, mpParameter, iMaxRowCount);
		}

		final ReportRunner runner = new ReportRunner(parent, reportVO, outputVO, source, sReportFilename, attachmentInfo);
		final BackgroundProcessStatusTableModel model = BackgroundProcessStatusController.getStatusDialog(UIUtils.getFrameForComponent(parent)).getStatusPanel().getModel();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				model.addEntry(runner);
			}
		});
		return new ReportThread(runner);
	}

	public static ReportThread createExportJob(Component parent, ReportOutputVO.Format format, CollectableSearchExpression expr, List<? extends CollectableEntityField> lstclctefweSelected, Integer iModuleId, boolean bIncludeSubModules) {
		final ReportSource source = new SearchExpressionReportSource(expr, lstclctefweSelected, iModuleId, bIncludeSubModules, format);
		final ReportRunner runner = new ReportRunner(parent, source, format, SpringLocaleDelegate.getInstance().getMessage("ReportRunner.2", "Suchergebnis"));
		return new ReportThread(runner);
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
		final ReportSource source = new ResultVoReportSource(resultVO, format);
		final ReportRunner runner = new ReportRunner(parent, source, format, (sDatasourceName != null) ? sDatasourceName : SpringLocaleDelegate.getInstance().getMessage("ReportRunner.2", "Suchergebnis"));
		return new ReportThread(runner);
	}

	private ReportRunner(Component parent, ReportVO reportvo, ReportOutputVO reportoutputvo, ReportSource source, String reportFilename, ReportAttachmentInfo attachmentInfo) {
		this.parent = parent;
		if (reportoutputvo != null && reportoutputvo.getParameter() != null) {
			directory = reportoutputvo.getParameter();
		}
		else {
			directory = System.getProperty("java.io.tmpdir");
		}
		if (reportFilename != null) {
			filename = reportFilename;
		}
		else if (reportoutputvo.getDescription() != null) {
			filename = reportoutputvo.getDescription();
		}
		else {
			filename = reportvo.getName();
		}
		this.source = source;
		this.reportname = reportvo.getName();
		this.format = reportoutputvo.getFormat();
		this.destination = reportoutputvo.getDestination();
		this.attachmentInfo = attachmentInfo;
	}

	private ReportRunner(Component parent, ReportSource source, ReportOutputVO.Format format, String filename) {
		this.parent = parent;
		this.directory = System.getProperty("java.io.tmpdir");
		this.filename = filename;
		this.source = source;
		this.reportname = filename;
		this.format = format;
		this.destination = Destination.SCREEN;
		this.attachmentInfo = null;
	}

	@Autowired
	final void setSpringLocaleDelegate(SpringLocaleDelegate cld) {
		this.localeDelegate = cld;
	}
	
	@Autowired
	final void setDatasourceFacadeRemote(DatasourceFacadeRemote datasourceFacadeRemote) {
		this.datasourceFacadeRemote = datasourceFacadeRemote;
	}

	@Override
	public void setBackgroundProcessInterruptionIntervalForCurrentThread() throws InterruptedException {
		Thread.sleep(GENERAL_INTERRUPTION_INTERVAL);
	}

	@Override
	public void addObservable(Observable observable) {
		this.observable = observable;
	}

	@Override
	public void cancelProzess() {
		if (this.future != null) {
			boolean cancelled = this.future.cancel(true);
			log.debug("cancelProzess>>>>>>>>>> cancelled future: " + cancelled);
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
			this.setStatus(Status.RUNNING);
			this.dateStartTime = new Date(Calendar.getInstance().getTimeInMillis());

			this.execute();

			if (attachmentInfo != null) {
				String filename = attachDocument();
				this.setStatus(Status.DONE);
				this.setMessage(localeDelegate.getMessage("ReportRunner.fileattached", "Document {0} has been attached to object {1}.", filename, attachmentInfo.getGenericObjectIdentifier()));
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
		finally {
			// release the job - avoid memory leak:
			// this.job = null;
		}
	}

	private void execute() throws NuclosReportException, InterruptedException {
		try {
			setBackgroundProcessInterruptionIntervalForCurrentThread();
			String d = createExportDir(directory);
			String f = getFileName(d, filename);
			NuclosFile result = source.getReport();
			open(destination, result, f);
		}
		catch (CommonBusinessException ex) {
			throw new NuclosReportException(ex);
		}
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
			// NUCLEUSINT-182 Arraylists instead of Sets
			final List<String> liParamNamesInThisDatasource = new ArrayList<String>();
			final List<String> liParamNamesInAllDatasources = new ArrayList<String>();
			final List<DatasourceParameterVO> liParamsEmpty = new ArrayList<DatasourceParameterVO>();

			for (Integer iDatasourceId : collDatasourceId) {
				if (iDatasourceId == null)
					throw new NuclosReportException(localeDelegate.getMessage("ReportRunner.4", "Keine Datenquelle angegeben"));

				final DatasourceFacadeRemote datasourceFacadeRemote = SpringApplicationContextHolder.getBean(DatasourceFacadeRemote.class);
				final DatasourceVO datasourcevo = datasourceFacadeRemote.get(iDatasourceId);
				for (DatasourceParameterVO paramvo : datasourceFacadeRemote.getParameters(datasourcevo.getSource())) {
					final String sParamName = paramvo.getParameter();
					liParamNamesInThisDatasource.add(sParamName);
					if (!liParamNamesInAllDatasources.contains(sParamName)) {
						liParamsEmpty.add(paramvo);
						liParamNamesInAllDatasources.add(sParamName);
					}
				}
				if (mpParams.containsKey("intid")) {
					if (!liParamNamesInThisDatasource.contains("intid")) {
						throw new NuclosReportException(localeDelegate.getMessage("ReportRunner.5", "Die dem Formular zugrundeliegende Datenquelle \"{0}\" muss den Parameter intid definieren.", datasourcevo.getName()));
					}
				}
				liParamNamesInThisDatasource.clear();
			}

			if (!liParamsEmpty.isEmpty()) {
				final ParameterPanel panel = new ParameterPanel(liParamsEmpty);

				result = (JOptionPane.showOptionDialog(Main.getInstance().getMainFrame(), panel, localeDelegate.getMessage("ReportRunner.8", "Parameter"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.OK_OPTION);
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
			if (NuclosEntity.GENERALSEARCHDOCUMENT.getEntityName().equals(attachmentInfo.getDocumentEntityName())) {
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
			throw new NuclosReportException(localeDelegate.getMessage("ReportController.1", "Anh\u00e4ngen der Datei \"{0}\" an GenericObject \"{1}\" fehlgeschlagen.", getDocumentName(), attachmentInfo.getGenericObjectIdentifier()), ex);
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
		if (this.observable != null) {
			this.observable.notifyObservers();
		}
	}

	/**
	 * @return the name of the report job
	 */
	@Override
	public String getJobName() {
		return localeDelegate.getMessage("ReportRunner.6", "Report: \"{0}\"", reportname);
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

	protected final String getFileName(String sExportPath, String filename) {
		String result;
		final String extension = format.getExtension();
		final File file = new File(sExportPath);
		final DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.getDefault());
		if (file.isDirectory()) {
			result = filename + "_" + dateformat.format(Calendar.getInstance(Locale.getDefault()).getTime());
			result = result.replaceAll("[/+*%?#!.:]", "-");
			result = sExportPath + ((sExportPath.endsWith(File.separator)) ? "" : File.separator) + result + extension;
		}
		else {
			result = sExportPath.substring(0, sExportPath.lastIndexOf("."));
			result = result + "_" + dateformat.format(Calendar.getInstance(Locale.getDefault()).getTime()) + extension;
		}
		// set the name of the generated report in the thread for further use
		if (Thread.currentThread().getClass().equals(ReportThread.class)) {
			/** @todo refactor! */
			((ReportThread) Thread.currentThread()).setDocumentName(result);
		}

		return result;
	}

	/**
	 * @param sExportPath
	 *            destination
	 * @return directory for export or filename (if filename was given)
	 */
	protected static String createExportDir(String sExportPath) throws NuclosReportException {
		final String result;

		if (sExportPath == null) {
			result = System.getProperty("java.io.tmpdir");
		}
		else {
			result = sExportPath;
			final int lastDotPos = sExportPath.lastIndexOf(".");
			final int lastSlashPos = sExportPath.lastIndexOf(File.separator);
			if (lastSlashPos >= lastDotPos) {
				final File fileExportDir = new File(sExportPath);
				if (!fileExportDir.exists()) {
					if (!fileExportDir.mkdir()) {
						throw new NuclosReportException(SpringLocaleDelegate.getInstance().getMessage("AbstractReportExporter.1", "Das Verzeichnis {0} konnte nicht angelegt werden.", sExportPath));
					}
				}
			}
		}
		return result;
	}

	protected void open(ReportOutputVO.Destination destination, NuclosFile file, String filename) throws NuclosReportException {
		switch (destination) {
		case FILE:
			openFile(file, filename, false);
			break;
		case PRINTER_CLIENT:
			openPrintDialog(file, filename, true, false);
			break;
		case PRINTER_SERVER:
			openPrintDialog(file, filename, false, false);
			break;
		case DEFAULT_PRINTER_CLIENT:
			openPrintDialog(file, filename, true, true);
			break;
		case DEFAULT_PRINTER_SERVER:
			openPrintDialog(file, filename, false, true);
			break;
		default:
			// TYPE SCREEN
			openFile(file, filename, true);
			break;
		}
	}

	private void openFile(NuclosFile file, String sFileName, boolean bOpenFile) throws NuclosReportException {
		try {
			saveFile(file, sFileName);
			if (bOpenFile) {
				SystemUtils.open(sFileName);
			}
			else {
				log.debug("NOT opening " + sFileName);
			}
		}
		catch (IOException ex) {
			throw new NuclosReportException(SpringLocaleDelegate.getInstance().getMessage("AbstractReportExporter.4", "Die Datei {0} konnte nicht ge\u00f6ffnet werden.", sFileName), ex);
		}
	}
	
	private void saveFile(NuclosFile file, String filename) throws IOException {
		IOUtils.writeToBinaryFile(new File(filename), file.getFileContents());
	}

	private void openPrintDialog(NuclosFile file, String sFileName, boolean bIsClient, boolean bIsDefault) throws NuclosReportException {
		try {
			PrintService prservDflt;
			if (bIsClient) {
				prservDflt = PrintServiceLookup.lookupDefaultPrintService();
			}
			else {
				prservDflt = ReportDelegate.getInstance().lookupDefaultPrintService();
			}

			PrintService[] prservices;
			if (bIsClient) {
				prservices = PrintServiceLookup.lookupPrintServices(null, null);
			}
			else {
				prservices = ReportDelegate.getInstance().lookupPrintServices(null, null);
			}

			if (null == prservices || 0 >= prservices.length) {
				if (null != prservDflt) {
					prservices = new PrintService[] { prservDflt };
				}
				else {
					throw new NuclosReportException(SpringLocaleDelegate.getInstance().getMessage("AbstractReportExporter.5", "Es ist kein passender Print-Service installiert."));
				}
			}

			PrintService prserv = null;
			PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
			if (prservDflt == null || !bIsDefault) {
				Rectangle gcBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
				prserv = ServiceUI.printDialog(null, (gcBounds.width / 2) - 200, (gcBounds.height / 2) - 200, prservices, prservDflt, null, aset);
			}
			else
				prserv = prservDflt;

			if (null != prserv) {
				if (bIsClient) {
					saveFile(file, sFileName);
					getNuclosReportPrintJob().print(prserv, sFileName, aset);
				}
				else {
					ReportDelegate.getInstance().printViaPrintService((NuclosReportRemotePrintService) prserv, getNuclosReportPrintJob(), aset, file.getFileContents());
				}
			}
		}
		catch (Exception e) {
			throw new NuclosReportException(e);
		}
	}
	
	private NuclosReportPrintJob getNuclosReportPrintJob() {
		switch (format) {
		case PDF:
			return new PDFPrintJob();
		case CSV:
			return new CSVPrintJob();
		case XLS:
		case XLSX:
			return new XLSPrintJob();
		case DOC:
			return new DOCPrintJob();
		default:
			return new FilePrintJob();
		}
	}

} // class ReportRunner
