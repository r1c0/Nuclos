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
package org.nuclos.client.genericobject;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;
import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.datasource.DatasourceDelegate;
import org.nuclos.client.datasource.admin.DatasourceCollectController;
import org.nuclos.client.report.ReportDelegate;
import org.nuclos.client.report.reportrunner.BackgroundProcessStatusController;
import org.nuclos.client.report.reportrunner.ReportAttachmentInfo;
import org.nuclos.client.report.reportrunner.ReportRunner;
import org.nuclos.client.report.reportrunner.ReportThread;
import org.nuclos.client.ui.Controller;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.format.FormattingTransformer;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.ejb3.ReportFacadeRemote;
import org.nuclos.server.report.valueobject.DatasourceParameterVO;
import org.nuclos.server.report.valueobject.ReportOutputVO;
import org.nuclos.server.report.valueobject.ReportVO;
import org.nuclos.server.report.valueobject.ResultColumnVO;
import org.nuclos.server.report.valueobject.ResultVO;

/**
 * ReportController for exporting leased object forms.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class ReportController extends Controller<JComponent> {

	private static final Logger LOG = Logger.getLogger(ReportController.class);
	
	private final ReportDelegate delegate;

	private String sLastGeneratedFileName;

	private final Map<Integer, List<String>> mpGeneratedFileNames = CollectionUtils.newHashMap();

	private boolean bFilesBeAttached = false;

	Integer iModuleId;

	/**
	 * @param parent
	 */
	public ReportController(JComponent parent) throws NuclosFatalException {
		super(parent);
		this.delegate = new ReportDelegate();
	}

	private String getDirectory(CollectableGenericObject clctlo) {
		Integer iModuleId = clctlo.getGenericObjectCVO().getModuleId();
		String sNewPath = (String)Modules.getInstance().getModuleById(iModuleId).getField("documentpath");
		return getPath(StringUtils.emptyIfNull(sNewPath), clctlo);
	}
	
	private static class ReportSelectionPanelMouseListener extends MouseAdapter {
		
        final JOptionPane pane;
        final JDialog dialog;
        
        private ReportSelectionPanelMouseListener(JOptionPane pane, JDialog dialog) {
        	this.pane = pane;
        	this.dialog = dialog;
        }
        
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				pane.setValue(JOptionPane.OK_OPTION);
				dialog.hide();
			}
		}
	}

	/**
	 * shows dialog to choose export form for a list of selected leased objects, using only common forms of selection
	 * @param lstclctlo
	 * @param usagecriteria
	 * @param documentFieldNames
	 * @throws NuclosFatalException
	 * @throws NuclosReportException
	 */
	public void exportForm(final List<? extends CollectableGenericObject> lstclctlo, UsageCriteria usagecriteria,final  String sDocumentEntityName, final String[] documentFieldNames)
			throws NuclosFatalException, NuclosReportException {
		try {
			final ReportSelectionPanel pnlSelection = prepareReportSelectionPanel(usagecriteria, lstclctlo.size());
			if (pnlSelection.getReportsTable().getModel().getRowCount() == 1) {
				executeForm(pnlSelection, lstclctlo, sDocumentEntityName, documentFieldNames);
				return;
			}
			
			String sDialogTitle = getSpringLocaleDelegate().getMessage("ReportController.15","Verf\u00fcgbare Formulare");
			//int btnValue = JOptionPane.showConfirmDialog(this.getParent(), pnlSelection, sDialogTitle, JOptionPane.OK_CANCEL_OPTION);
	        final JOptionPane pane = new JOptionPane(pnlSelection, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, null, null);
	        final JDialog dialog = pane.createDialog(getParent(), sDialogTitle);
	        	    
	        pnlSelection.addDoubleClickListener(new ReportSelectionPanelMouseListener(pane, dialog));
	       
	        dialog.setResizable(true);
	        dialog.setVisible(true);
			if ((pane.getValue() == null? -1: (Integer)pane.getValue()) == JOptionPane.OK_OPTION)
			{
				executeForm(pnlSelection, lstclctlo, sDocumentEntityName, documentFieldNames);
			}
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * prepares the report selection panel for use in print (or better: export) dialog box
	 * @param facade
	 * @param quadruple quadruple of one leased object or greatest common quadruple if more objects are selected
	 * @param iObjectCount
	 * @return created selection panel
	 * @throws RemoteException
	 * @throws NuclosReportException
	 */
	public static ReportSelectionPanel prepareReportSelectionPanel(UsageCriteria quadruple,
			int iObjectCount) throws NuclosReportException {

		ReportFacadeRemote facade = ServiceLocator.getInstance().getFacade(ReportFacadeRemote.class);
		final Collection<ReportVO> collReports = facade.findReportsByUsage(quadruple);
		final boolean bSingleSelection = (iObjectCount == 1);
		if (collReports.size() <= 0) {
			final String sExceptionText = (bSingleSelection ?
				SpringLocaleDelegate.getInstance().getMessage("ReportController.9","Es ist noch kein Formular zugeordnet.") : 
				SpringLocaleDelegate.getInstance().getMessage("ReportController.8","Dieser Auswahl sind keine gemeinsamen Formulare zugeordnet."));
			throw new NuclosReportException(sExceptionText);
		}

		return newReportSelectionPanel(collReports, true);
	}

	/**
	 * get if exists assigned forms for the given module
	 * @param quadruple
	 */
	public boolean hasFormsAssigned(UsageCriteria quadruple){
		try {
			ReportFacadeRemote facade = ServiceLocator.getInstance().getFacade(ReportFacadeRemote.class);
			return (facade.findReportsByUsage(quadruple).size() > 0);
		}
		catch (RuntimeException e) {
			throw new CommonFatalException(e);
		}
	}

	// show Background Process dialog for file attachment information.
	private static class StatusDialogRunnable implements Runnable {
		
		private final ReportSelectionPanel pnlSelection;
		
		private StatusDialogRunnable(ReportSelectionPanel pnlSelection) {
			this.pnlSelection = pnlSelection;
		}
		
		@Override
		public void run() {
			try {
				BackgroundProcessStatusController.getStatusDialog(UIUtils.getFrameForComponent(pnlSelection)).setVisible(true);
			}
			catch (Exception e) {
				LOG.error("executeForm failed: " + e, e);
			}																									
		}
	}
	
	/**
	 * executes the selected form for each single leased object, one after the other
	 * @param pnlSelection
	 * @param lstclctlo
	 * @param documentFieldNames
	 * @param facade
	 * @throws RemoteException
	 */
	private void executeForm(final ReportSelectionPanel pnlSelection, final List<? extends CollectableGenericObject> lstclctlo, final String sDocumentEntityName, final String[] documentFieldNames) {

		final ReportSelectionPanel.ReportEntry entry = pnlSelection.getSelectedReport();
		/** @todo this is a precondition */
		if (entry == null) {
			return;
		}
		iModuleId = null;

		this.bFilesBeAttached = pnlSelection.getAttachReport();

		// show Background Process dialog for file attachment information.
		if (bFilesBeAttached) {
			SwingUtilities.invokeLater(new StatusDialogRunnable(pnlSelection));
		}

		final ReportVO reportvo = entry.getReport();

		final Thread thread;
		switch (reportvo.getOutputType()) {
			case SINGLE: {
				final ReportOutputVO outputvo = entry.getOutput();
				thread = new Thread() {
					@Override
					public void run() {
						try {
							// loop over list of Leased Objects
							for (CollectableGenericObject clctlo : lstclctlo) {
								final String sGenericObjectId = clctlo.getId().toString();
								final String sGenericObjectIdentifier = clctlo.getGenericObjectCVO().getSystemIdentifier();

								final String sDirectory = getDirectory(clctlo);

								ReportAttachmentInfo info = null;
								if (bFilesBeAttached) {
									info = new ReportAttachmentInfo(clctlo.getId(), sGenericObjectIdentifier, sDocumentEntityName, documentFieldNames, sDirectory);
								}

								final Map<String, Object> params = CollectionUtils.newHashMap();
								List<DatasourceParameterVO> lstParameters = DatasourceDelegate.getInstance().getParametersFromXML(DatasourceDelegate.getInstance().getDatasource(reportvo.getDatasourceId()).getSource());
								List<DatasourceParameterVO> lstNewParameters = new ArrayList<DatasourceParameterVO>();
								for (DatasourceParameterVO dspvo: lstParameters) {
									if (!dspvo.getParameter().equals("intid")) {
										lstNewParameters.add(dspvo);
									}
								}
								boolean result = DatasourceCollectController.createParamMap(lstNewParameters, params, getParent());
								if (result) {
									params.put("GenericObjectID", sGenericObjectId);
									params.put("intid", sGenericObjectId);
									if (!StringUtils.isNullOrEmpty(sGenericObjectIdentifier)) {
										params.put("GenericObjectIdentifier", sGenericObjectIdentifier);
									}
									iModuleId = clctlo.getGenericObjectCVO().getModuleId();

									String sReportFilename = (String)Modules.getInstance().getModuleById(iModuleId).getField("reportfilename");

									sReportFilename = getPath(StringUtils.emptyIfNull(sReportFilename), clctlo);

									final ReportThread reportThread = ReportRunner.createJob(getParent(), params, reportvo, outputvo, true, null, sReportFilename, info);
									//we need to wait, because excel/pdf can't open many files in 1 second (NUCLEUSINT-333)
									Thread.sleep(1000);
									reportThread.start();
									reportThread.join();
								}
							}
						}
						catch (final Exception ex) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									Errors.getInstance().showExceptionDialog(getParent(), 
											getSpringLocaleDelegate().getMessage("ReportController.10","Fehler beim Ausf\u00fchren des Reports") + ":", ex);
								}
							});
						}
					}
				};
				break;
			}
			default: {
				// COLLECTIVE or EXCEL:
				thread = new Thread() {
					private Collection<ReportOutputVO> collFormat;
					{
						try {
							collFormat = ServiceLocator.getInstance().getFacade(ReportFacadeRemote.class).getReportOutputs(reportvo.getId());
						}
						catch (RuntimeException ex) {
							throw new CommonFatalException(ex);
						}
					}
					@Override
					public void run() {
						try {
							for (CollectableGenericObject clctlo : lstclctlo) {
								final String sGenericObjectId = clctlo.getId().toString();
								final String sGenericObjectIdentifier = clctlo.getGenericObjectCVO().getSystemIdentifier();

								final String sDirectory = getDirectory(clctlo);

								ReportAttachmentInfo info = null;
								if (bFilesBeAttached) {
									info = new ReportAttachmentInfo(clctlo.getId(), sGenericObjectIdentifier, sDocumentEntityName, documentFieldNames, sDirectory);
								}

								final Map<String, Object> mpParams = CollectionUtils.newHashMap();
								mpParams.put("GenericObjectID", sGenericObjectId);
								mpParams.put("intid", sGenericObjectId);
								if (!StringUtils.isNullOrEmpty(sGenericObjectIdentifier)) {
									mpParams.put("GenericObjectIdentifier", sGenericObjectIdentifier);
								}
								// if the report is a collective report, execute all contained outputs
								boolean bIsFirstOfMany = true;
								for (Iterator<ReportOutputVO> iterOutput = collFormat.iterator(); iterOutput.hasNext();) {
									final ReportOutputVO outputvo = iterOutput.next();
									outputvo.setIsFirstOfMany(bIsFirstOfMany);
									outputvo.setIsLastOfMany(!iterOutput.hasNext());

									// Remember Excel document name for Excel collective reports
									if (!bIsFirstOfMany && reportvo.getOutputType() == ReportVO.OutputType.EXCEL && getLastGeneratedFileName() != null)
									{
										outputvo.setParameter(getLastGeneratedFileName());
									}
									else {
										setLastGeneratedFileName(null);
									}

									final ReportThread threadReport = ReportRunner.createJob(getParent(), mpParams, reportvo, outputvo, true,
											Integer.getInteger(ClientParameterProvider.getInstance().getValue(ParameterProvider.KEY_REPORT_MAXROWCOUNT)), null, info);
									threadReport.start();
									threadReport.join();
									final String sFileName = threadReport.getDocumentName();
									if (sFileName != null) {
										setLastGeneratedFileName(sFileName);
									}

									bIsFirstOfMany = false;
								}
							}
						}
						catch (final InterruptedException ex) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									Errors.getInstance().showExceptionDialog(getParent(), 
											getSpringLocaleDelegate().getMessage(
													"ReportController.6","Die Ausf\u00fchrung des Reports wurde unerwartet unterbrochen") + ": ", ex);
								}
							});
						}
						catch (final Exception ex) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									Errors.getInstance().showExceptionDialog(getParent(), 
											getSpringLocaleDelegate().getMessage(
													"ReportController.11","Fehler beim Ausf\u00fchren des Reports") + ":", ex);
								}
							});
						}
					}
				};
			}
		}	// switch
		thread.start();
	}

	private String getPath(String path, final CollectableGenericObject oParent) {
		final String entity = Modules.getInstance().getEntityNameByModuleId(oParent.getGenericObjectCVO().getModuleId());
		return StringUtils.replaceParameters(path, new FormattingTransformer() {
			@Override
			protected Object getValue(String field) {
				return oParent.getField(field).getValue();
			}

			@Override
			protected String getEntity() {
				return entity;
			}
		});
	}

	/**
	 * preparation of ReportSelectionPanel for futher use
	 * @param collreportvo Collection of appropriate reports
	 * @param facade Report facade
	 * @param bShowAttachReport Decides, whether checkbox 'Dokument anh\u00e4ngen' will be shown.
	 * @return ReportSelectionPanel
	 * @throws RemoteException
	 */
	private static ReportSelectionPanel newReportSelectionPanel(Collection<ReportVO> collreportvo, boolean bShowAttachReport) {

		final ReportFacadeRemote facade = ServiceLocator.getInstance().getFacade(ReportFacadeRemote.class);
		final ReportSelectionPanel result = new ReportSelectionPanel(bShowAttachReport);

		try {
			for (ReportVO reportvo : collreportvo) {
				if (reportvo.getOutputType() == ReportVO.OutputType.SINGLE) {
					for (ReportOutputVO outputvo : facade.getReportOutputs(reportvo.getId())) {
						result.addReport(reportvo, outputvo);
					}
				}
				else {
					// collective reports/forms should occur only once in list
					result.addReport(reportvo, null);
				}
				result.setPreferredColumnWidth(10, 10);
			}
			result.selectFirstReport();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}

		return result;
	}

	public Map<Integer, List<String>> getGeneratedFileNames() {
		return this.mpGeneratedFileNames;
	}

	public void setLastGeneratedFileName(String sFileName) {
		this.sLastGeneratedFileName = sFileName;
	}

	public String getLastGeneratedFileName() {
		return this.sLastGeneratedFileName;
	}

	public boolean getFilesBeAttached() {
		return this.bFilesBeAttached;
	}

	/**
	 *
	 * @param clcteMain the main collectable entity
	 * @param searchexpr
	 * @param lstclctefweSelected List<CollectableEntityFieldWithEntity> the collectable entity fields (along with their entities) that are to be exported.
	 * @param lstclctlo
	 * @param usagecriteria
	 * @param bIncludeSubModules
	 * @param sDocumentEntityName
	 * @throws NuclosBusinessException
	 */
	public void export(CollectableEntity clcteMain, CollectableSearchExpression searchexpr,
			List<? extends CollectableEntityField> lstclctefweSelected,
			List<? extends CollectableGenericObject> lstclctlo, UsageCriteria usagecriteria, boolean bIncludeSubModules,
			String sDocumentEntityName, String[] documentFieldNames)
			throws NuclosBusinessException {
		final ReportFormatController formatController;
		if (lstclctlo.isEmpty()) {
			formatController = new ReportFormatController(getParent());
			if (formatController.run(getSpringLocaleDelegate().getMessage("ReportController.13","Suchergebnis exportieren"))) {
				export(clcteMain, searchexpr, lstclctefweSelected, bIncludeSubModules, formatController.getFormat());
			}
		}
		else {
			formatController = new ChoiceListOrReportExportController(getParent(), usagecriteria, lstclctlo.size());
			final boolean bSearchDialog = formatController.run(getSpringLocaleDelegate().getMessage(
					"ReportController.12","Suchergebnis exportieren / Formulardruck"));
			final ChoiceListOrReportExportPanel pnlChoiceExport = ((ChoiceListOrReportExportController) formatController).pnlChoiceExport;
			if (bSearchDialog && pnlChoiceExport.getReportButton().isSelected()) {
				try {
					executeForm(pnlChoiceExport.getSelectionPanel(), lstclctlo, sDocumentEntityName, documentFieldNames);
				}
				catch (RuntimeException ex) {
					throw new NuclosBusinessException("RemoteException: Collection of report outputs couldn't be created.", ex);
				}
			}
			else if (bSearchDialog && pnlChoiceExport.getListButton().isSelected()) {
				export(clcteMain, searchexpr, lstclctefweSelected, bIncludeSubModules, formatController.getFormat());
			}
		}
	}

	/**
	 * @param clcteMain the main collectable entity
	 * @param searchexpr
	 * @param lstclctefweSelected List<CollectableEntityFieldWithEntity> the collectable entity fields (along with their entities) that are to be exported.
	 * @param bIncludeSubModules
	 * @param format
	 * @throws NuclosBusinessException
	 */
	private void export(CollectableEntity clcteMain, CollectableSearchExpression searchexpr, List<? extends CollectableEntityField> lstclctefweSelected,
			boolean bIncludeSubModules, ReportOutputVO.Format format) throws NuclosBusinessException {
		try {
			getParent().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			final String sMainEntityName = clcteMain.getName();
			final Integer iModuleId = Modules.getInstance().getModuleIdByEntityName(sMainEntityName);
			
			ReportRunner.createExportJob(getParent(), format, searchexpr, lstclctefweSelected, iModuleId, bIncludeSubModules).start();
		}
		finally {
			getParent().setCursor(Cursor.getDefaultCursor());
		}
	}

	public void export(JTable table, String sDatasourceName) throws NuclosBusinessException {
		try {
			final ReportFormatController formatctl = new ReportFormatController(getParent());
			if (formatctl.run(getSpringLocaleDelegate().getMessage("ReportController.14","Tabelle exportieren"))) {
				UIUtils.showWaitCursorForFrame(getParent(), true);
				final ResultVO resultvo = convertJTableToResultVO(table);
				ReportRunner.createExportJob(getParent(), resultvo, formatctl.getFormat(), null, sDatasourceName).start();
			}
		}
		finally {
			UIUtils.showWaitCursorForFrame(getParent(), false);
		}
	}

	private ResultVO convertJTableToResultVO(JTable tbl) {
		final ResultVO result = new ResultVO();

		// fill the columns:
		final TableColumnModel columnmodel = tbl.getTableHeader().getColumnModel();
		for (int iColumn = 0; iColumn < columnmodel.getColumnCount(); iColumn++) {
			final ResultColumnVO resultcolumnvo = new ResultColumnVO();
			resultcolumnvo.setColumnLabel(columnmodel.getColumn(iColumn).getHeaderValue().toString());
			resultcolumnvo.setColumnClassName("java.lang.Object");
			result.addColumn(resultcolumnvo);
		}

		// fill the rows:
		for (int iRow = 0; iRow < tbl.getRowCount(); iRow++) {
			final Object[] aoData = new Object[tbl.getColumnCount()];
			for (int iColumn = 0; iColumn < tbl.getColumnCount(); iColumn++) {
				final Object oValue = tbl.getValueAt(iRow, iColumn);
				if (oValue instanceof CollectableField) {
					aoData[iColumn] = ((CollectableField) oValue).getValue();
				}
				else if (oValue instanceof String) {
					aoData[iColumn] = (oValue == null) ? null : ((String) oValue).replace('\n', ' ').trim();
				}
				else {
					aoData[iColumn] = oValue;
				}
				final ResultColumnVO resultcolumnvo = result.getColumns().get(iColumn);
				if (resultcolumnvo.getColumnClassName().equals("java.lang.Object") && aoData[iColumn] != null) {
					resultcolumnvo.setColumnClassName(aoData[iColumn].getClass().getName());
				}
			}
			result.addRow(aoData);
		}

		return result;
	}

}	// class ReportController
