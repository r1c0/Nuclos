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
package org.nuclos.client.common.fileimport;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.concurrent.Future;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.DateUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.fileimport.CommonParseException;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.report.reportrunner.BackgroundProcessInfo;
import org.nuclos.client.report.reportrunner.BackgroundProcessStatusController;
import org.nuclos.client.report.reportrunner.BackgroundProcessStatusDialog;
import org.nuclos.client.report.reportrunner.BackgroundProcessTableEntry;
import org.nuclos.client.ui.CommonBackgroundProcessClientWorkerAdapter;
import org.nuclos.client.ui.CommonMultiThreader;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.ValidatingJOptionPane;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Import dialog questioning import file and import structure.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @author	<a href="mailto:rostislav.maksymovskyi@novabit.de">rostislav.maksymovskyi</a>
 * @version 02.00.00
 */
public class FileImportController<Clct extends Collectable> {
	private static final String PREFS_KEY_FILECHOOSERPATH = "FileChooserPath";
	private final CollectController<Clct> clct;
	private final Preferences userPreferences;
	private final FileFilter filefilter = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return file.isDirectory() || file.getName().toLowerCase().endsWith(".csv") || file.getName().toLowerCase().endsWith(".asc") || file.getName().toLowerCase().endsWith(".txt");
		}

		@Override
		public String getDescription() {
			return CommonLocaleDelegate.getMessage("FileImport.1", "Strukturierte ASCII-Dateien (*.cvs,*.asc,*.txt)");
		}
	};

	public FileImportController(CollectController<Clct> clct, Preferences userPreferences) {
		this.clct = clct;
		this.userPreferences = userPreferences;
	}

	private JFileChooser getFileChooser() {
		final String sCurrentDirectory = this.userPreferences.get(PREFS_KEY_FILECHOOSERPATH, null);
		final JFileChooser result = new JFileChooser(sCurrentDirectory);
		result.addChoosableFileFilter(filefilter);
		result.setFileFilter(filefilter);
		return result;
	}

	public void run(final String sTarget) {
		final FileImportPanel pnlImport = new FileImportPanel();

		Integer iTargetId = null;
		String sTargetLabel = "";

		final Collection<MasterDataVO> collmdcvoImportTargets = MasterDataDelegate.getInstance().getMasterData("importtarget",
				SearchConditionUtils.newComparison("importtarget", "table", ComparisonOperator.EQUAL, sTarget));

		for (MasterDataVO mdcvo : collmdcvoImportTargets) {
			/** @todo If the entity does not have an intid as primary key, a ClassCastException will be thrown here. */
			iTargetId = mdcvo.getIntId();
			sTargetLabel = (String) mdcvo.getField("name");
		}
		if (iTargetId == null) {
			JOptionPane.showMessageDialog(clct.getFrame(), CommonLocaleDelegate.getMessage("FileImport.2", "Es kann kein Importziel f\u00fcr {0} gefunden werden.", sTargetLabel), 
				CommonLocaleDelegate.getMessage("FileImport.3", "Fehler beim Import"), JOptionPane.ERROR_MESSAGE);
		}

		final CollectableEntityField clctef = DefaultCollectableEntityProvider.getInstance().getCollectableEntity(NuclosEntity.IMPORT.getEntityName()).getEntityField("importtarget");
		final CollectableSearchCondition cond = new CollectableComparison(clctef, ComparisonOperator.EQUAL, new CollectableValueIdField(iTargetId, null));
		pnlImport.cbImportStructure.setModel(new DefaultComboBoxModel(MasterDataDelegate.getInstance().getMasterData(NuclosEntity.IMPORT.getEntityName(), cond).toArray()));

		pnlImport.btnBrowse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				JFileChooser filechooser = getFileChooser();
				final int iBtn = filechooser.showOpenDialog(clct.getFrame());
				if (iBtn == JFileChooser.APPROVE_OPTION) {
					final File file = filechooser.getSelectedFile();
					if (file != null) {
						pnlImport.tfFile.setText(file.getPath());
					}
				}
			}
		});

		final ValidatingJOptionPane optionPane = new ValidatingJOptionPane(clct.getFrame(), CommonLocaleDelegate.getMessage("FileImport.6", "Import"), pnlImport) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected void validateInput() throws ValidatingJOptionPane.ErrorInfo {
				if (pnlImport.tfFile.getText().equals("")) {
					throw new ValidatingJOptionPane.ErrorInfo(CommonLocaleDelegate.getMessage("FileImport.4", "Es wurde keine zu importierende Datei angegeben."), pnlImport.tfFile);
				}
				else {
					if (pnlImport.cbImportStructure.getSelectedItem() == null) {
						throw new ValidatingJOptionPane.ErrorInfo(CommonLocaleDelegate.getMessage("FileImport.5", "Es wurde keine anzuwendende Strukturtabelle angegeben."), pnlImport.cbImportStructure);
					}
					else {
						final File file = new File(pnlImport.tfFile.getText());
						final Integer iId = ((MasterDataVO) pnlImport.cbImportStructure.getSelectedItem()).getIntId();
						userPreferences.put(PREFS_KEY_FILECHOOSERPATH, file.getAbsolutePath());
						
						CommonBackgroundProcessClientWorkerAdapter<Clct> workerAdapter = new CommonBackgroundProcessClientWorkerAdapter<Clct>(clct) {
							
							final Logger log = Logger.getLogger(FileImportController.class);
							BackgroundProcessTableEntry entry;
							FileImport fileImport;
							
							@Override
							public void work() throws CommonBusinessException{
								log.debug("START import");
								try {
									fileImport = new FileImport(file, sTarget, new FileImportStructure(iId));
								}
								catch (CommonParseException ex) {
									throw new CommonBusinessException (ex);
								}
								catch (InterruptedException e) {
									Thread.currentThread().interrupt();
									setBackgroundProcessFinishedStatus(entry, BackgroundProcessInfo.Status.CANCELLED, CommonLocaleDelegate.getMessage("FileImport.7", "Der Prozess wird unterbrochen."));
								}
							}

							@Override
							public void setBackgroundProcessTableEntry(BackgroundProcessTableEntry backgroundProcessTableEntry) {
								this.entry = backgroundProcessTableEntry;
							}
							
							private void setBackgroundProcessFinishedStatus(final BackgroundProcessTableEntry entry, final BackgroundProcessInfo.Status status, final String statusMessage) {
								// set the status in status dialog:
								UIUtils.runCommandLater(null, new Runnable() {
									@Override
									public void run() {
										entry.setStatus(status);
										entry.setMessage(statusMessage);
										log.debug("Set import status to "+status + " because "+statusMessage);
									}
								});
							}
							
							@Override
							public void paint() throws CommonBusinessException {
								super.paint();
								if(this.entry != null && this.entry.getStatus() == BackgroundProcessInfo.Status.CANCELLED){
									setBackgroundProcessFinishedStatus(entry, BackgroundProcessInfo.Status.ERROR, CommonLocaleDelegate.getMessage("FileImport.8", "Der Import wurde abgebrochen. Es konnten nicht alle Daten importiert werden."));
								} else {
									setBackgroundProcessFinishedStatus(entry, BackgroundProcessInfo.Status.DONE, getImportSuccessMessage(fileImport));
									final JPanel pnlResult = getImportResultPanel(fileImport);
									JOptionPane.showMessageDialog(clct.getFrame(), pnlResult, CommonLocaleDelegate.getMessage("FileImport.6", "Import"), JOptionPane.INFORMATION_MESSAGE);
									//JOptionPane.showMessageDialog(parent, "Import erfolgreich abgeschlossen. Es wurden " + fileImport.getCountCreated() + " Datens\u00e4tze erstellt und " + fileImport.getCountUpdated() + " Datens\u00e4tze aktualisiert.\n" + fileImport.getCountError() + " Datens\u00e4tze konnten nicht importiert werden.", "Import", JOptionPane.INFORMATION_MESSAGE);
								}
								clct.getResultController().cmdRefreshResult();
							}
							
							@Override
							public void handleError(Exception ex) {
								setBackgroundProcessFinishedStatus(entry, BackgroundProcessInfo.Status.ERROR, CommonLocaleDelegate.getMessage("FileImport.9", "Import fehlgeschlagen."));
								Errors.getInstance().showExceptionDialog(clct.getFrame(), ex);
							}
							
							private JPanel getImportResultPanel(FileImport fileImport) {
								final Font font = new JLabel().getFont();
								final JPanel pnlResult = new JPanel(new BorderLayout());
								final JTextArea taMessage = new JTextArea();
								taMessage.setBackground(pnlResult.getBackground());
								taMessage.setEditable(false);
								taMessage.setFont(font);
								taMessage.setText(getImportSuccessMessage(fileImport));
								pnlResult.add(taMessage, BorderLayout.NORTH);
								
								if(fileImport.getCountError() > 0) {
									final JTextArea taErrors = new JTextArea(10,60);
									taErrors.setBackground(pnlResult.getBackground());
									taErrors.setEditable(false);
									taErrors.setFont(font);
									for (String sMessage : fileImport.getErrorMessages()) {
										taErrors.append(sMessage + "\n");
									}
									final JScrollPane scrPane = new JScrollPane();
									scrPane.getViewport().add(taErrors);
									scrPane.setBorder(BorderFactory.createTitledBorder(CommonLocaleDelegate.getMessage("FileImport.10", "Liste der Fehlermeldungen")));
									pnlResult.add(scrPane, BorderLayout.CENTER);
								}
								pnlResult.validate();
								return pnlResult;
							}

							private String getImportSuccessMessage(FileImport fileImport) {
								return CommonLocaleDelegate.getMessage("FileImport.11", "Import erfolgreich abgeschlossen. Es wurden {0} Datens\u00e4tze erstellt und {1} Datens\u00e4tze aktualisiert.\n{2} Datens\u00e4tze konnten nicht importiert werden.", fileImport.getCountCreated(), fileImport.getCountUpdated(), fileImport.getCountError());
								//"Import erfolgreich abgeschlossen. Es wurden " + fileImport.getCountCreated() +
									//	" Datens\u00e4tze erstellt und " + fileImport.getCountUpdated() + " Datens\u00e4tze aktualisiert.\n" +
										//fileImport.getCountError() + " Datens\u00e4tze konnten nicht importiert werden.";
							}
						};					
						final BackgroundProcessStatusDialog dlgStatus = BackgroundProcessStatusController.getStatusDialog(UIUtils.getFrameForComponent(clct.getFrame().getParent()));
						Future<?> future = CommonMultiThreader.getInstance().executeInterruptible(workerAdapter);		
						BackgroundProcessTableEntry entry = new BackgroundProcessTableEntry(CommonLocaleDelegate.getMessage("FileImport.12", "Dateiimport: {0}", file.getName()), 
							BackgroundProcessInfo.Status.RUNNING, DateUtils.now(), future);
						workerAdapter.setBackgroundProcessTableEntry(entry);
						dlgStatus.getStatusPanel().getModel().addEntry(entry);
						dlgStatus.setVisible(true);
					}
				}
			}
		};
		optionPane.showDialog();
	}

	private static class FileImportPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public final JTextField tfFile = new JTextField();
		public final JComboBox cbImportStructure = new JComboBox();
		private final JPanel pnlFileChooser = new JPanel(new BorderLayout(0, 0));
		public final JButton btnBrowse = new JButton("...");

		FileImportPanel() {
			super(new BorderLayout(10, 0));

			tfFile.setPreferredSize(new Dimension(200, 0));
			tfFile.setEditable(false);
			btnBrowse.setMargin(new Insets(0, 2, 0, 2));

			JPanel pnlWest = new JPanel(new GridLayout(2, 1, 0, 5));
			JPanel pnlEast = new JPanel(new GridLayout(2, 1, 0, 5));

			this.add(pnlWest, BorderLayout.WEST);
			this.add(pnlEast, BorderLayout.CENTER);

			pnlWest.add(new JLabel(CommonLocaleDelegate.getMessage("FileImport.13", "Zu importierende Datei:")));
			pnlWest.add(new JLabel(CommonLocaleDelegate.getMessage("FileImport.14", "Anzuwendende Strukturtabelle:")));

			pnlEast.add(pnlFileChooser);
			pnlEast.add(cbImportStructure);

			pnlFileChooser.add(tfFile, BorderLayout.CENTER);
			pnlFileChooser.add(btnBrowse, BorderLayout.EAST);
		}
	}
}

