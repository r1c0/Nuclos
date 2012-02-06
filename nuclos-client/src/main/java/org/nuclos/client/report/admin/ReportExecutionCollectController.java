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
package org.nuclos.client.report.admin;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.common.KeyBindingProvider;
import org.nuclos.client.genericobject.ReportSelectionPanel;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.CollectableMasterData;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.report.reportrunner.BackgroundProcessStatusController;
import org.nuclos.client.report.reportrunner.ReportRunner;
import org.nuclos.client.report.reportrunner.ReportThread;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectStateConstants;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.KeyEnum;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.ejb3.ReportFacadeRemote;
import org.nuclos.server.report.valueobject.ReportOutputVO;
import org.nuclos.server.report.valueobject.ReportVO;
import org.nuclos.server.report.valueobject.ReportVO.OutputType;

/**
 * <code>MasterDataCollectController</code> for entity "reportExecution".
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @author	<a href="mailto:rostislav.maksymovskyi@novabit.de">rostislav.maksymovskyi</a>
 * @version 02.00.00
 */
public class ReportExecutionCollectController extends MasterDataCollectController {

	protected class ExecuteAction extends AbstractAction {

		public ExecuteAction() {
			super(getCommonLocaleDelegate().getMessage("ReportExecutionCollectController.2","Ausf\u00fchren..."));
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			execReport();
		}
	}

	private static String sLastGeneratedFileName = null;


	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} 
	 * to get an instance.
	 * 
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public ReportExecutionCollectController(JComponent parent, MainFrameTab tabIfAny) {
		super(parent, NuclosEntity.REPORTEXECUTION, tabIfAny);
		setupResultToolBar();
		setExecuteState();
	}

	@Override
	protected void setupResultToolBar(){
		super.setupResultToolBar();

		getResultPanel().btnEdit.setVisible(false);
		getResultPanel().btnBookmark.setVisible(false);
		getResultPanel().btnClone.setVisible(false);
		getResultPanel().btnDelete.setVisible(false);
		getResultPanel().btnNew.setVisible(false);

		getSearchPanel().btnNew.setVisible(false);
	}



	/**
	 * @throws CommonBusinessException
	 */
	public final void runReportExecutionCollectController() throws CommonBusinessException {
		setExecuteState();

		runSearch();
		this.setTitle(getCommonLocaleDelegate().getMessage("ReportExecutionCollectController.1","{0} - Suche", getEntityLabel()));
		getCollectPanel().setTabbedPaneEnabledAt(CollectStateConstants.OUTERSTATE_DETAILS, false);
	}

	/**
	 * Switches the controller from administrative to execute state
	 */
	private void setExecuteState() {

		/** @todo this is ugly: */
		// remove mouse listener for double click in table:
		final JTable tbl = getResultTable();
		tbl.removeMouseListener(getMouseListenerForTableDoubleClick());

		// add alternative mouse listener for foreign key lookup:
		final MouseListener listener = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent ev) {
				if (ev.getClickCount() == 2)
					execReport();
			}
		};

		final JPopupMenu menu = getResultPanel().popupmenuRow;
		menu.removeAll();
		menu.add(new ExecuteAction());

		//KeyBindingProvider.removeActionFromComponent(KeyBindingProvider.EDIT_1, getResultTable());
		KeyBindingProvider.removeActionFromComponent(KeyBindingProvider.EDIT_2, getResultTable());
		KeyBindingProvider.bindActionToComponent(KeyBindingProvider.EDIT_2, new ExecuteAction(), getResultTable());


		tbl.addMouseListener(listener);
	}


	@Override
	protected String getEntityLabel() {
		return getCommonLocaleDelegate().getMessage("ReportExecutionCollectController.4","Reporting - Ausf\u00fchrung");
	}

	private void execReport() {
		final CollectableMasterData clctSelected = ReportExecutionCollectController.this.getSelectedCollectable();
		if (clctSelected != null) {
			execReport(getFrame(), clctSelected.getCollectableEntity().getName(), clctSelected.getMasterDataCVO());
		}
	}
	
	public static void execReport(final Component parent, final String entity, final MasterDataVO mdReport) {
		final ExecutorService cachedThreadPoolExecutor = Executors.newCachedThreadPool();
		final CommonLocaleDelegate cld = CommonLocaleDelegate.getInstance();
		
		UIUtils.showWaitCursorForFrame(parent, true);
		try {
			final ReportFacadeRemote facade =ServiceLocator.getInstance().getFacade(ReportFacadeRemote.class);
			
			if (mdReport != null) {
				final Collection<ReportOutputVO> collFormats = facade.getReportOutputs((Integer) mdReport.getId());
				OutputType outputType = KeyEnum.Utils.findEnum(OutputType.class, mdReport.getField("outputtype", String.class));
				
				if (outputType == ReportVO.OutputType.SINGLE) {
					final ReportSelectionPanel pnlSelection = new ReportSelectionPanel();
					for (ReportOutputVO formatVO : collFormats)
						pnlSelection.addReport(new ReportVO(mdReport), formatVO);
					pnlSelection.selectFirstReport();
					if (JOptionPane.showConfirmDialog(parent, pnlSelection, 
							cld.getMessage(
									"ReportExecutionCollectController.6","Verf\u00fcgbare Ausgabeformate"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
						final ReportSelectionPanel.ReportEntry entry = pnlSelection.getSelectedReport();
						if (entry == null)
							return;
						try {
							final Map<String, Object> mpParams = new HashMap<String, Object>();
							if (!ReportRunner.prepareParameters(collFormats, mpParams))
								return;
							final String sMaxRowCount = ClientParameterProvider.getInstance().getValue(ParameterProvider.KEY_REPORT_MAXROWCOUNT);
							final Integer iMaxRowCount = (sMaxRowCount == null) ? null : Integer.getInteger(sMaxRowCount);
							final ReportThread thread;
							if (NuclosEntity.REPORTEXECUTION.checkEntityName(entity))
								thread = ReportRunner.createJob(parent, mpParams, entry.getReport(), entry.getOutput(), true, iMaxRowCount);
							else
								thread = null;
							if (thread != null) {
								BackgroundProcessStatusController.getStatusDialog(UIUtils.getFrameForComponent(parent)).setVisible(true);
								Future<?> future = cachedThreadPoolExecutor.submit(thread);
								thread.getReportRunner().setProcessFuture(future);
							}
						} catch (NuclosReportException ex) {
							String msg = ex.getLocalizedMessage();
							Errors.getInstance().showExceptionDialog(parent,
								new NuclosFatalException(cld.getMessage(
										"ReportExecutionCollectController.8", "Fehler beim Ausf\u00fchren des Reports {0}", (msg != null ? ": " + msg : "")), ex));
						}
					}
				} else {
					final Thread collectiveThread = new Thread() {
						{
							BackgroundProcessStatusController.getStatusDialog(UIUtils.getFrameForComponent(parent)).setVisible(true);
						}

						@Override
						public void run() {
							try {
								final Map<String, Object> mpParams = new HashMap<String, Object>();
								if (!ReportRunner.prepareParameters(collFormats, mpParams))
									return;
								final ReportVO reportvo = new ReportVO(mdReport);
								boolean bIsFirstOfMany = true;
								for (Iterator<ReportOutputVO> j = collFormats.iterator(); j.hasNext();) {
									final ReportOutputVO formatVO = j.next();
									formatVO.setIsFirstOfMany(bIsFirstOfMany);
									formatVO.setIsLastOfMany(!j.hasNext());
									if (!bIsFirstOfMany && reportvo.getOutputType() == ReportVO.OutputType.EXCEL &&
										sLastGeneratedFileName != null)
										formatVO.setParameter(sLastGeneratedFileName);
									else
										sLastGeneratedFileName = null;
									final ReportThread thread;
									if (NuclosEntity.REPORTEXECUTION.checkEntityName(entity))
										thread = ReportRunner.createJob(parent, mpParams, reportvo, formatVO, true, Integer.getInteger(ClientParameterProvider.getInstance().getValue(ParameterProvider.KEY_REPORT_MAXROWCOUNT)));
									else if (NuclosEntity.REPORT.checkEntityName(entity))
										thread = ReportRunner.createJob(parent, mpParams, reportvo, formatVO, false, Integer.getInteger(ClientParameterProvider.getInstance().getValue(ParameterProvider.KEY_REPORT_MAXROWCOUNT)));
									else {
										thread = null;
										throw new NuclosFatalException(cld.getMessage(
												"ReportExecutionCollectController.5","Unbekannter Reporttyp: {0}", entity));
									}
									if (thread != null) {
										thread.start();
										try {
											thread.join();
										} catch (InterruptedException ex) {
											throw new NuclosFatalException(cld.getMessage(
													"ReportExecutionCollectController.3","Die Ausf\u00fchrung des Reports wurde unerwartet unterbrochen: "), ex);
										}
										sLastGeneratedFileName = thread.getDocumentName();
									}
									bIsFirstOfMany = false;
								}
								BackgroundProcessStatusController.getStatusDialog(UIUtils.getFrameForComponent(parent)).setVisible(true);
							} catch (final Exception ex) {
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										Errors.getInstance().showExceptionDialog(parent, new NuclosFatalException(
												cld.getMessage("ReportExecutionCollectController.7", "Fehler beim Ausf\u00fchren des Sammelreports."), ex));
									}
								});
							}
						}
					};
					collectiveThread.start();
				}
			}
		} catch (RuntimeException ex) {
			Errors.getInstance().showExceptionDialog(parent, ex.getMessage(), ex);
		} finally {
			UIUtils.showWaitCursorForFrame(parent, false);
		}
	}

}	// class ReportExecutionCollectController
