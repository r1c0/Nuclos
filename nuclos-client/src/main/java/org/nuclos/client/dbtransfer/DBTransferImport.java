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
package org.nuclos.client.dbtransfer;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.collections.Closure;
import org.nuclos.client.NuclosIcons;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.wizard.WizardFrame;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dbtransfer.PreviewPart;
import org.nuclos.common.dbtransfer.Transfer;
import org.nuclos.common.dbtransfer.TransferOption;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.pietschy.wizard.I18n;
import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.PanelWizardStep;
import org.pietschy.wizard.WizardEvent;
import org.pietschy.wizard.WizardListener;
import org.pietschy.wizard.models.StaticModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class DBTransferImport {

	public static final String IMPORT_EXECUTED = "import_executed";

	private CommonLocaleDelegate cld;

	private boolean isNuclon;
	private final ActionListener notifyParent;

	private Transfer importTransferObject = null;
	private boolean blnImportStarted, blnSaveOfScriptRecommend, blnSaveOfLogRecommend = false;
	private Transfer.Result importTransferResult = null;

	private final DBTransferUtils utils = new DBTransferUtils();
	private final DBTransferWizard wizard;
	private final StaticModel model;
	private final PanelWizardStep step1, step2, step3, step4, step5;

	private final MainFrameTab ifrm = Main.getInstance().getMainController().newMainFrameTab(
			null, cld.getMessage("dbtransfer.import.title", "Konfiguration importieren"));
	
	public DBTransferImport(ActionListener notifyParent) {
		if (notifyParent == null) {
			throw new IllegalArgumentException("notifyParent must not be null");
		}
		this.notifyParent = notifyParent;

		I18n.setBundle(DBTransferWizard.getResourceBundle());

		ifrm.setTabIcon(NuclosIcons.getInstance().getDefaultFrameIcon());

		step1 = newStep1(ifrm);
		step2 = newStep2(ifrm);
		step3 = newStep3(ifrm);
		step4 = newStep4(ifrm);
		step5 = newStep5(ifrm);
		model = new StaticModel(){
			@Override
			public boolean isLastVisible() {
				return false;
			}

			@Override
			public void refreshModelState() {
				super.refreshModelState();
				if (wizard != null) {
					wizard.setCancelEnabled(!blnImportStarted);
					super.setPreviousAvailable(!blnImportStarted &&  !step1.equals(getActiveStep()));
				}
			}
		};

		model.add(step1);
		model.add(step2);
		model.add(step3);
		model.add(step4);
		model.add(step5);
		wizard = new DBTransferWizard(model);

      wizard.addWizardListener(new WizardListener() {
            @Override
            public void wizardClosed(WizardEvent e) {
            	closeWizard();
            }
            @Override
            public void wizardCancelled(WizardEvent e) {
            	closeWizard();
            }
      });
	}
	
	@Autowired
	void setCommonLocaleDelegate(CommonLocaleDelegate cld) {
		this.cld = cld;
	}

	private void closeWizard(){
		if (blnImportStarted) {
			if (model.allStepsComplete()) {
				ifrm.dispose();
			} else {
				if (blnSaveOfLogRecommend || blnSaveOfScriptRecommend) {
					String titel = cld.getMessage("dbtransfer.import.closewizard.title", "Wizard kann noch nicht geschlossen werden");
					String message = "";
					if (blnSaveOfLogRecommend) {
						message = cld.getMessage("dbtransfer.import.closewizard.log", "Es ist ein Problem beim \u00c4ndern des Datenbankschemas aufgetreten.\n" +
								"Bitte speichern Sie die Log-Datei und analysieren Sie die Probleme!");
					} else if (blnSaveOfScriptRecommend) {
						message = cld.getMessage("dbtransfer.import.closewizard.script", "Da die \u00c4nderungen am Datenbankschema nicht automatisch durchgef\u00fchrt wurden\n" +
								"m\u00fcssen Sie diese noch manuell nachholen.\n" +
								"Bitte speichern Sie das Script!");
					}
					JOptionPane.showMessageDialog(ifrm, message, titel, JOptionPane.INFORMATION_MESSAGE);
				}
			}
		} else {
			ifrm.dispose();
		}
	}

	public void showWizard(JTabbedPane desktopPane) {
      ifrm.setLayeredComponent(WizardFrame.createFrameInScrollPane(wizard));
      int x = desktopPane.getWidth()/2-wizard.getPreferredSize().width/2;
      int y = desktopPane.getHeight()/2-wizard.getPreferredSize().height/2;
      x = x<0?0:x;
      y = y<0?0:y;
      ifrm.setBounds(x, y, wizard.getWidth(), wizard.getHeight());
//      ifrm.pack();

      desktopPane.add(ifrm);

      ifrm.setVisible(true);
	}

	/*
	 * Begin Step 1
	 */
	private JPanel jpnPreviewContent = new JPanel();
	private JPanel jpnPreviewHeader = new JPanel();
	private JPanel jpnPreviewFooter = new JPanel();
	private final JTextField tfTransferFile = new JTextField(50);
	
	private PanelWizardStep newStep1(final MainFrameTab ifrm) {
		final PanelWizardStep step = new PanelWizardStep(cld.getMessage(
				"dbtransfer.import.step1.1", "Konfigurationsdatei"), 
				cld.getMessage("dbtransfer.import.step1.2", "Bitte w\u00e4hlen Sie eine Konfigurationsdatei aus."));
		
		final JLabel lbFile = new JLabel(cld.getMessage("dbtransfer.import.step1.3", "Datei"));
		
		utils.initJPanel(step,
			new double[] {TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL},
			new double[] {20,
							  TableLayout.PREFERRED,
							  lbFile.getPreferredSize().height,
							  TableLayout.FILL});
		
		final JButton btnBrowse = new JButton("...");
		//final JProgressBar progressBar = new JProgressBar(0, 230);
		final JCheckBox chbxImportAsNuclon = new JCheckBox(cld.getMessage(
				"configuration.transfer.import.as.nuclon", "Import als Nuclon"));
		chbxImportAsNuclon.setEnabled(false);
		final JEditorPane editWarnings = new JEditorPane();
		editWarnings.setContentType("text/html");
		editWarnings.setEditable(false);
		editWarnings.setBackground(Color.WHITE);
		
		final JScrollPane scrollWarn = new JScrollPane(editWarnings);
		scrollWarn.setPreferredSize(new Dimension(680, 250));
		scrollWarn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		scrollWarn.getVerticalScrollBar().setUnitIncrement(20);
		scrollWarn.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollWarn.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		final JScrollPane scrollPrev = new JScrollPane(jpnPreviewContent);
		scrollPrev.setPreferredSize(new Dimension(680, 250));
		scrollPrev.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
		scrollPrev.getVerticalScrollBar().setUnitIncrement(20);
		scrollPrev.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPrev.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		final JPanel jpnPreview = new JPanel(new BorderLayout());
		jpnPreview.add(jpnPreviewHeader, BorderLayout.NORTH);
		jpnPreview.add(scrollPrev, BorderLayout.CENTER);
		jpnPreview.add(jpnPreviewFooter, BorderLayout.SOUTH);
		jpnPreview.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		jpnPreview.setBackground(Color.WHITE);
		jpnPreviewHeader.setBackground(Color.WHITE);
		jpnPreviewContent.setBackground(Color.WHITE);
		jpnPreviewFooter.setBackground(Color.WHITE);
		
		final JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab(cld.getMessage("configuration.transfer.prepare.warnings.tab", "Warnungen"), scrollWarn);
		final String sDefaultPreparePreviewTabText = cld.getMessage(
				"configuration.transfer.prepare.preview.tab", "Vorschau der Schema Aenderungen");
		tabbedPane.addTab(sDefaultPreparePreviewTabText, jpnPreview);
		
		final JLabel lbNewUser = new JLabel();
		
		step.add(lbFile, "0,0");
		step.add(tfTransferFile, "1,0");
		step.add(btnBrowse, "2,0");
		step.add(chbxImportAsNuclon, "1,1");//step.add(progressBar, "1,1");
		step.add(lbNewUser, "0,2,3,2");
		step.add(tabbedPane, "0,3,3,3");
		
		tfTransferFile.setEditable(false);
		
		final ActionListener prepareImportAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				ifrm.lockLayerWithProgress(Transfer.TOPIC_CORRELATIONID_PREPARE);
						
				Thread t = new Thread(){
					@Override
					public void run() {
						step.setComplete(false);
						boolean blnTransferWithWarnings = false;
						//progressBar.setValue(0);
						//progressBar.setVisible(true);
						try {								
							String fileName = tfTransferFile.getText();
							if (StringUtils.isNullOrEmpty(fileName)) {
								return;
							}
							File f = new File(fileName);
							long size = f.length();
	
							FileInputStream fin = new FileInputStream(f);
							byte[] transferFile = utils.getBytes(fin, (int) size);

							resetStep2();
							importTransferObject = utils.getTransferFacade().prepareTransfer(isNuclon, transferFile);
							chbxImportAsNuclon.setEnabled(importTransferObject.getTransferOptions().containsKey(TransferOption.IS_NUCLON_IMPORT_ALLOWED));

							step.setComplete(!importTransferObject.result.hasCriticals());
							
							if (!importTransferObject.result.hasCriticals() && !importTransferObject.result.hasWarnings()) {
								editWarnings.setText(cld.getMessage(
										"configuration.transfer.prepare.no.warnings", "Keine Warnungen"));
							} else {
								editWarnings.setText(
									"<html><body><font color=\"#800000\">" + importTransferObject.result.getCriticals() + "</font>" +
									(importTransferObject.result.hasCriticals()?"<br />":"") + importTransferObject.result.getWarnings() + 
									"</body></html>");
							}
							
							int iPreviewSize = importTransferObject.getPreviewParts().size();
							blnTransferWithWarnings = setupPreviewPanel(importTransferObject.getPreviewParts());
							tabbedPane.setTitleAt(1, sDefaultPreparePreviewTabText + (iPreviewSize==0?"":" ("+iPreviewSize+")"));
							lbNewUser.setText("Neue Benutzer" + ": " + (importTransferObject.getNewUserCount()==0?"keine":importTransferObject.getNewUserCount()));
						} catch (Exception e) {
//										progressBar.setVisible(false);
							Errors.getInstance().showExceptionDialog(ifrm, e);
						} finally {
							btnBrowse.setEnabled(true);
//										progressBar.setVisible(false);
							ifrm.unlockLayer();
						}
						if(blnTransferWithWarnings) {
							JOptionPane.showMessageDialog(jpnPreviewContent, cld.getMessage(
									"dbtransfer.import.step1.19", "Nicht alle Statements können durchgeführt werden!\nBitte kontrollieren Sie die mit rot markierten Einträge!", "Warning", JOptionPane.WARNING_MESSAGE));									
						}
					}
					
				};
			
				t.start();
			}
		};
		final ActionListener browseAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				final JFileChooser filechooser = utils.getFileChooser(cld.getMessage(
						"configuration.transfer.file.nuclet", "Nuclet-Dateien"), ".nuclet");
				final int iBtn = filechooser.showOpenDialog(ifrm);

				if (iBtn == JFileChooser.APPROVE_OPTION) {
					
					final File file = filechooser.getSelectedFile();
					if (file != null) {
						tfTransferFile.setText("");
						btnBrowse.setEnabled(false);
						//progressBar.setVisible(true);
							
						String fileName = file.getPath();
						if (StringUtils.isNullOrEmpty(fileName)) {
							return;
						}
						
						tfTransferFile.setText(fileName);
						
						prepareImportAction.actionPerformed(new ActionEvent(this, 0, "prepare"));
					}
				}
			}
		};
		final ActionListener importAsNuclonAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				isNuclon = chbxImportAsNuclon.isSelected();
				prepareImportAction.actionPerformed(new ActionEvent(this, 0, "prepare"));
			}
		};
		btnBrowse.addActionListener(browseAction);
		chbxImportAsNuclon.addActionListener(importAsNuclonAction);
		
//		progressBar.setVisible(false);
		
		return step;
	}
	
	private boolean setupPreviewPanel(List<PreviewPart> previewParts) {	
		boolean blnTransferWithWarnings = false;
		jpnPreviewHeader.removeAll();
		jpnPreviewFooter.removeAll();
		
		// setup parameter scroll pane
		jpnPreviewContent.removeAll();
		
		double[] rowContraints = new double[previewParts.size()];
		for (int i = 0; i < previewParts.size(); i++)
			rowContraints[i] = TableLayout.PREFERRED;
		final int iWidthBeginnigSpace = 3;
		final int iWidthSeparator = 6;
		
		JLabel lbPreviewHeaderEntity = new JLabel(cld.getMessage("dbtransfer.import.step1.11", "Entit\u00e4t"));
		JLabel lbPreviewHeaderTable = new JLabel(cld.getMessage("dbtransfer.import.step1.12", "Tabellenname"));
		JLabel lbPreviewHeaderRecords = new JLabel(cld.getMessage("dbtransfer.import.step1.4", "Datens\u00e4tze"));
		lbPreviewHeaderRecords.setToolTipText(cld.getMessage("dbtransfer.import.step1.5", "Anzahl der betroffenen Datens\u00e4tze"));
		utils.initJPanel(jpnPreviewContent, 
			new double[] {iWidthBeginnigSpace, TableLayout.PREFERRED, iWidthSeparator, TableLayout.PREFERRED, iWidthSeparator, TableLayout.PREFERRED, iWidthSeparator, TableLayout.PREFERRED, TableLayout.PREFERRED, iWidthSeparator, TableLayout.PREFERRED},
			rowContraints);
		
		int iWidthEntityLabelSize = 0;
		int iWidthTableLabelSize = 0;
		int iWidthRecordsLabelSize = 0;
		
		int iCountNew = 0;
		int iCountDeleted = 0;
		int iCountChanged = 0;
		
		int iRow = 0;
		for (final PreviewPart pp : previewParts) {
			String tooltip = "";
			JLabel lbEntity = new JLabel(pp.getEntity());
			JLabel lbTable = new JLabel(pp.getTable());
			JLabel lbRecords = new JLabel(String.valueOf(pp.getDataRecords()));
			lbRecords.setHorizontalAlignment(SwingConstants.RIGHT);
			if (lbEntity.getPreferredSize().width<lbPreviewHeaderEntity.getPreferredSize().width)
				lbEntity.setPreferredSize(lbPreviewHeaderEntity.getPreferredSize());
			if (lbTable.getPreferredSize().width<lbPreviewHeaderTable.getPreferredSize().width)
				lbTable.setPreferredSize(lbPreviewHeaderTable.getPreferredSize());
			if (lbRecords.getPreferredSize().width<lbPreviewHeaderRecords.getPreferredSize().width)
				lbRecords.setPreferredSize(lbPreviewHeaderRecords.getPreferredSize());
			iWidthEntityLabelSize = iWidthEntityLabelSize<lbEntity.getPreferredSize().width?lbEntity.getPreferredSize().width:iWidthEntityLabelSize;
			iWidthTableLabelSize = iWidthTableLabelSize<lbTable.getPreferredSize().width?lbTable.getPreferredSize().width:iWidthTableLabelSize;
			iWidthRecordsLabelSize = iWidthRecordsLabelSize<lbRecords.getPreferredSize().width?lbRecords.getPreferredSize().width:iWidthRecordsLabelSize;
			
			Icon icoStatement = null;
			switch (pp.getType()) {
			case PreviewPart.NEW:
				tooltip = cld.getMessage("dbtransfer.import.step1.6", "Entit\u00e4t wird hinzugef\u00fcgt");
				icoStatement = ParameterEditor.COMPARE_ICON_NEW;
				iCountNew++;
				break;
			case PreviewPart.CHANGE:
				tooltip = cld.getMessage("dbtransfer.import.step1.7", "Entit\u00e4t wird ge\u00e4ndert");
				icoStatement = ParameterEditor.COMPARE_ICON_VALUE_CHANGED;
				iCountChanged++;
				break;
			case PreviewPart.DELETE:
				tooltip = cld.getMessage("dbtransfer.import.step1.8", "Entit\u00e4t wird gel\u00f6scht");
				icoStatement = ParameterEditor.COMPARE_ICON_DELETED;
				iCountDeleted++;
				break;
			}
			
			JLabel lbIcon = new JLabel(icoStatement);
			JLabel lbStatemnts = new JLabel("<html><u>" + cld.getMessage("dbtransfer.import.step1.9", "Script anzeigen") + "...</u></html>");
			lbStatemnts.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));			
			lbStatemnts.addMouseListener(new MouseListener() {
				@Override
				public void mouseReleased(MouseEvent e) {}
				@Override
				public void mousePressed(MouseEvent e) {}
				@Override
				public void mouseExited(MouseEvent e) {}
				@Override
				public void mouseEntered(MouseEvent e) {}
				@Override
				public void mouseClicked(MouseEvent e) {
					String statements = "";
					for (String statement : pp.getStatements()) {
						statements = statements+statement+";\n\n";
					}
					JTextArea txtArea = new JTextArea(statements);
					
					txtArea.setEditable(false);
					txtArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					JScrollPane scroll = new JScrollPane(txtArea);
					scroll.getVerticalScrollBar().setUnitIncrement(20);
					scroll.setPreferredSize(new Dimension(600, 300));
					scroll.setBorder(BorderFactory.createEmptyBorder());
					MainFrameTab overlayFrame = new MainFrameTab(cld.getMessage(
							"dbtransfer.import.step1.10", "Script f\u00fcr") + " " + pp.getEntity() + " (" + pp.getTable() + ")");
					overlayFrame.setLayeredComponent(scroll);
					ifrm.add(overlayFrame);
				}
			});
			
			
			
			lbIcon.setToolTipText(tooltip);
			lbStatemnts.setToolTipText(tooltip);
			lbEntity.setToolTipText(tooltip);
			lbTable.setToolTipText(tooltip);
			
			jpnPreviewContent.add(lbEntity, "1," + iRow + ",l,c");
			jpnPreviewContent.add(lbTable, "3," + iRow + ",l,c");
			jpnPreviewContent.add(lbRecords, "5," + iRow + ",r,c");
			jpnPreviewContent.add(lbIcon, "7," + iRow + ",l,c");
			jpnPreviewContent.add(lbStatemnts, "8," + iRow + ",l,c");
			if(pp.getWarning() > 0) {
				lbIcon.setIcon(Icons.getInstance().getIconPriorityCancel16());
				blnTransferWithWarnings = true;
			}
			iRow++;
		}
		
		jpnPreviewContent.add(new JSeparator(JSeparator.VERTICAL), "2,0,2,"+(iRow-1));
		jpnPreviewContent.add(new JSeparator(JSeparator.VERTICAL), "4,0,4,"+(iRow-1));
		jpnPreviewContent.add(new JSeparator(JSeparator.VERTICAL), "6,0,6,"+(iRow-1));
		
		// setup preview header	
		utils.initJPanel(jpnPreviewHeader, 
			new double[] {iWidthBeginnigSpace, iWidthEntityLabelSize, iWidthSeparator, iWidthTableLabelSize, iWidthSeparator, iWidthRecordsLabelSize, iWidthSeparator, TableLayout.PREFERRED, iWidthSeparator, TableLayout.PREFERRED, TableLayout.PREFERRED}, 
			new double[] {TableLayout.PREFERRED});
		
		if (previewParts.isEmpty()) {
			jpnPreviewHeader.add(new JLabel(cld.getMessage(
					"dbtransfer.import.step1.18", "Keine Struktur\u00e4nderungen am Datenbankschema.")), "0,0,8,0");
			return blnTransferWithWarnings;
		}
		
		jpnPreviewHeader.add(lbPreviewHeaderEntity, "1,0");
		jpnPreviewHeader.add(lbPreviewHeaderTable, "3,0");
		jpnPreviewHeader.add(lbPreviewHeaderRecords, "5,0");		
		
		jpnPreviewHeader.add(new JLabel(cld.getMessage(
				"dbtransfer.import.step1.13", "\u00c4nderung")), "7,0");

		// setup preview footer
		utils.initJPanel(jpnPreviewFooter, 
			new double[] {TableLayout.FILL, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}, 
			new double[] {TableLayout.PREFERRED});
		
		final JLabel lbCompare = new JLabel(cld.getMessage(
				"dbtransfer.import.step1.14", "\u00c4nderungen")+":");
		final JLabel lbCompareNew = new JLabel(iCountNew+"");
		final JLabel lbCompareDeleted = new JLabel(iCountDeleted+"");
		final JLabel lbCompareValueChanged = new JLabel(iCountChanged+"");
		
		lbCompareNew.setIcon(ParameterEditor.COMPARE_ICON_NEW);
		lbCompareDeleted.setIcon(ParameterEditor.COMPARE_ICON_DELETED);
		lbCompareValueChanged.setIcon(ParameterEditor.COMPARE_ICON_VALUE_CHANGED);
		
		lbCompareNew.setToolTipText(cld.getMessage(
				"dbtransfer.import.step1.15", "Neue Entit\u00e4ten"));
		lbCompareDeleted.setToolTipText(cld.getMessage(
				"dbtransfer.import.step1.17", "Gel\u00f6schte Entit\u00e4ten"));
		lbCompareValueChanged.setToolTipText(cld.getMessage(
				"dbtransfer.import.step1.16", "Ge\u00e4nderte Entit\u00e4ten"));
		
		jpnPreviewFooter.add(lbCompare, "1,0,r,c");
		jpnPreviewFooter.add(lbCompareNew, "2,0,r,c");
		jpnPreviewFooter.add(lbCompareValueChanged, "3,0,r,c");
		jpnPreviewFooter.add(lbCompareDeleted, "4,0,r,c");
		
		return blnTransferWithWarnings;		
	}
		
	/*
	 * Begin Step 2
	 */
	private String sDefaultIncludeUserText;
	private String sDefaultIncludeLDAPText;
	private String sDefaultIncludeObjectimportText;
	
	private final JCheckBox chbxIncludeUser = new JCheckBox();
	private final JCheckBox chbxIncludeLDAP = new JCheckBox();
	private final JCheckBox chbxIncludeObjectimport = new JCheckBox();
	private final JCheckBox chbxAlternativeDBLogin = new JCheckBox();
	private final JTextField tfAlternativeDBLogin = new JTextField(15);
	private final JPasswordField pfAlternativeDBPassword = new JPasswordField(15);
	
	private boolean validateStep2(){	
		if (chbxAlternativeDBLogin.isSelected()){
			if (StringUtils.looksEmpty(tfAlternativeDBLogin.getText())){
				return false;
			}
			if (StringUtils.looksEmpty(new String(pfAlternativeDBPassword.getPassword()))){
				return false;
			}
		}
		
		return true;
	}
	
	private void resetStep2() {
		chbxIncludeUser.setSelected(false);
		chbxIncludeLDAP.setSelected(false);
		chbxIncludeObjectimport.setSelected(false);
		step2.setComplete(false);
	}
	
	private PanelWizardStep newStep2(final MainFrameTab ifrm) {
		final PanelWizardStep step = new PanelWizardStep(cld.getMessage(
				"configuration.transfer.options", "Optionen"), 
				cld.getMessage("dbtransfer.import.step2.1", "Bitte w\u00e4hlen Sie die Import Optionen aus.")){

				@Override
				public void prepare() {
					Map<TransferOption,Serializable> exportOptions = TransferOption.copyOptionMap(importTransferObject.getTransferOptions());
					
					chbxIncludeUser.setEnabled(exportOptions.containsKey(TransferOption.INCLUDES_USER));
					chbxIncludeUser.setText(sDefaultIncludeUserText + (chbxIncludeUser.isEnabled()?"":" ("
							+ cld.getMessage("dbtransfer.import.step2.2", "nicht in Konfigurationsdatei enthalten")+")"));
					chbxIncludeLDAP.setEnabled(exportOptions.containsKey(TransferOption.INCLUDES_USER));
					chbxIncludeLDAP.setText(sDefaultIncludeLDAPText + (chbxIncludeLDAP.isEnabled()?"":" ("
							+ cld.getMessage("dbtransfer.import.step2.2", "nicht in Konfigurationsdatei enthalten")+")"));
					chbxIncludeObjectimport.setEnabled(exportOptions.containsKey(TransferOption.INCLUDES_USER));
					chbxIncludeObjectimport.setText(sDefaultIncludeObjectimportText + (chbxIncludeObjectimport.isEnabled()?"":" ("
							+ cld.getMessage("dbtransfer.import.step2.2", "nicht in Konfigurationsdatei enthalten")+")"));
					
					tfAlternativeDBLogin.setEnabled(chbxAlternativeDBLogin.isSelected());
					pfAlternativeDBPassword.setEnabled(chbxAlternativeDBLogin.isSelected());
					
					this.setComplete(validateStep2());
				}
		};
		utils.initJPanel(step,
			new double[] {30, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL},
			new double[] {20,
							  20,
							  20,
							  20,
							  20,
							  20,
							  20,
							  TableLayout.PREFERRED});
		
		sDefaultIncludeUserText = cld.getMessage(
				"dbtransfer.import.step2.3", "Benutzer importieren");
		sDefaultIncludeLDAPText = cld.getMessage(
				"configuration.transfer.import.option.ldap", "LDAP Konfiguration importieren");
		sDefaultIncludeObjectimportText = cld.getMessage(
				"configuration.transfer.import.option.objectimport", "Objektimport importieren");
		chbxIncludeUser.setText(sDefaultIncludeUserText);
		chbxIncludeLDAP.setText(sDefaultIncludeLDAPText);
		chbxIncludeObjectimport.setText(sDefaultIncludeObjectimportText);
		chbxAlternativeDBLogin.setText(cld.getMessage(
				"dbtransfer.import.step2.5", "verwende alternativen Datenbanklogin"));
		
		final JLabel lbAlternativeDBLogin = new JLabel(cld.getMessage(
				"dbtransfer.import.step2.7", "Login"));
		final JLabel lbAlternativeDBPassword = new JLabel(cld.getMessage(
				"dbtransfer.import.step2.8", "Passwort"));
		
		step.add(chbxIncludeUser, "0,0,2,0");
		step.add(chbxIncludeLDAP, "0,1,2,1");
		step.add(chbxIncludeObjectimport, "0,2,2,2");
		step.add(chbxAlternativeDBLogin, "0,4,2,4");
		step.add(lbAlternativeDBLogin, "1,5,l,c");
		step.add(tfAlternativeDBLogin, "2,5,l,c");
		step.add(lbAlternativeDBPassword, "1,6,l,c");
		step.add(pfAlternativeDBPassword, "2,6,l,c");
//		step.add(lbWarningWhenNoStructureChange, "0,7,3,0");
		
		ActionListener validateAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				step.setComplete(validateStep2());
			}
		};
		chbxIncludeUser.addActionListener(validateAction);
		chbxIncludeLDAP.addActionListener(validateAction);
		chbxIncludeObjectimport.addActionListener(validateAction);
		chbxAlternativeDBLogin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tfAlternativeDBLogin.setText("");
				tfAlternativeDBLogin.setEnabled(chbxAlternativeDBLogin.isSelected());
				pfAlternativeDBPassword.setText("");
				pfAlternativeDBPassword.setEnabled(chbxAlternativeDBLogin.isSelected());
				step.setComplete(validateStep2());
			}
		});
		tfAlternativeDBLogin.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {step.setComplete(validateStep2());}
			@Override
			public void insertUpdate(DocumentEvent e)  {step.setComplete(validateStep2());}
			@Override
			public void changedUpdate(DocumentEvent e) {step.setComplete(validateStep2());}
		});
		pfAlternativeDBPassword.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {step.setComplete(validateStep2());}
			@Override
			public void insertUpdate(DocumentEvent e) {step.setComplete(validateStep2());}
			@Override
			public void changedUpdate(DocumentEvent e) {step.setComplete(validateStep2());}
		});
		
		return step;
	}
	
	/*
	 * Begin Step 3
	 */
	private final JPanel jpnParameter = new JPanel();
	private final JPanel jpnParameterHeader = new JPanel();
	private final JPanel jpnParameterFooter = new JPanel();
	private final JRadioButton rbCurrentAll = new JRadioButton();
	private final JRadioButton rbIncomingAll = new JRadioButton();
	
	private final Set<String> setSelectedIncomingParameter = new HashSet<String>();
	private final Set<String> setSelectedCurrentParameter = new HashSet<String>();
	private final Map<String, String> mapSelectedOtherParameter = new HashMap<String, String>();
	
	private final List<ParameterEditor> lstParameterEditors = new ArrayList<ParameterEditor>();
	
	private PanelWizardStep newStep3(final MainFrameTab ifrm) {
		final PanelWizardStep step = new PanelWizardStep(cld.getMessage(
				"dbtransfer.import.step3.1", "System Parameter"), 
				cld.getMessage(
						"dbtransfer.import.step3.2", "Bestimmen Sie die Parameter dieses Systems. Sie k\u00f6nnen w\u00e4hlen zwischen dem aktuellen Zustand und dem aus der Konfigurationsdatei importierten Zustand (default). Sollte keine der beiden Vorgaben stimmen, so k\u00f6nnen Sie auch einen anderen Wert setzen.")){

				@Override
				public void prepare() {
					setupParameterPanel(importTransferObject.getParameter());
				}
				
				@Override
				public void applyState() throws InvalidStateException {
					setSelectedIncomingParameter.clear();
					setSelectedCurrentParameter.clear();
					mapSelectedOtherParameter.clear();
					
					for (ParameterEditor pe : lstParameterEditors) {
						if (pe.isCurrentValue())
							setSelectedCurrentParameter.add(pe.getName());
						if (pe.isIncomingValue())
							setSelectedIncomingParameter.add(pe.getName());
						if (pe.isOtherValue())
							mapSelectedOtherParameter.put(pe.getName(), pe.getValue());
					}
				}
		};
		step.setComplete(true);
		
		utils.initJPanel(step,
			new double[] {TableLayout.FILL},
			new double[] {TableLayout.PREFERRED,
							  TableLayout.FILL,
							  TableLayout.PREFERRED});
		
		rbCurrentAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetParameterEditors(true);
			}
		});
		rbIncomingAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetParameterEditors(false);
			}
		});
		
		final JScrollPane scroll = new JScrollPane(jpnParameter);
		scroll.setPreferredSize(new Dimension(680, 250));
		scroll.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
		scroll.getVerticalScrollBar().setUnitIncrement(20);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.getViewport().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				Dimension parameterPanelSize = new Dimension(scroll.getViewport().getWidth(), jpnParameter.getPreferredSize().height);
				jpnParameter.setPreferredSize(parameterPanelSize);
			}
		});
		
		step.add(jpnParameterHeader, "0,0");
		step.add(scroll, "0,1");
		step.add(jpnParameterFooter, "0,2");
		
		return step;
	}
	
	private void setupParameterPanel(Collection<EntityObjectVO> incomingParameterVOs) {
		final String entity = NuclosEntity.PARAMETER.getEntityName();
		List<EntityObjectVO> currentParameterVOs = new ArrayList<EntityObjectVO>(CollectionUtils.transform(
			MasterDataDelegate.getInstance().getMasterData(entity), 
			DalSupportForMD.getTransformerToEntityObjectVO(entity)));
		int iCountNew = 0;
		int iCountDeleted = 0;
		int iCountValueChanged = 0;
		
		// find new parameter and existing
		List<ParameterComparison> allParameter = new ArrayList<ParameterComparison>();
		for (EntityObjectVO incomingParameterVO : incomingParameterVOs) {
			boolean blnParamFound = false;
			String sCurrentValue = null;
			for (EntityObjectVO currentParameterVO : currentParameterVOs) {
				if ((incomingParameterVO.getField("name", String.class)).equals(currentParameterVO.getField("name", String.class))) {
					blnParamFound = true;
					sCurrentValue = currentParameterVO.getField("value", String.class);
					break;
				}
			}
			if (!blnParamFound) {
				allParameter.add(new ParameterComparison(incomingParameterVO, true, false, null));
				iCountNew++;
			} else {
				ParameterComparison parameter = new ParameterComparison(incomingParameterVO, false, false, sCurrentValue);
				allParameter.add(parameter);
				if (parameter.isValueChanged())
					iCountValueChanged++;
			}
		}
		
		// find deleted parameter
		for (EntityObjectVO currentParameterVO : currentParameterVOs) {
			boolean blnParamFound = false;
			for (EntityObjectVO incomingParamVO : incomingParameterVOs) {
				if ((incomingParamVO.getField("name", String.class)).equals(currentParameterVO.getField("name", String.class))) {
					blnParamFound = true;
					break;
				}
			}
			if (!blnParamFound) {
				allParameter.add(new ParameterComparison(currentParameterVO, false, true, currentParameterVO.getField("value", String.class)));
				iCountDeleted++;
			}
		}
		
		// sort parameter VOs
		List<ParameterComparison> allParameterSorted = new ArrayList<ParameterComparison>(allParameter);
		Collections.sort(allParameterSorted, new Comparator<ParameterComparison>() {
			@Override
			public int compare(ParameterComparison o1, ParameterComparison o2) {
				return StringUtils.emptyIfNull(o1.getField("name", String.class)).compareTo(
					StringUtils.emptyIfNull(o2.getField("name", String.class)));
			}});
		
		jpnParameterHeader.removeAll();
		jpnParameterFooter.removeAll();
		
		// setup parameter scroll pane
		jpnParameter.removeAll();
		
		double[] rowContraints = new double[allParameterSorted.size()];
		for (int i = 0; i < allParameterSorted.size(); i++)
			rowContraints[i] = TableLayout.PREFERRED;
		final int iWidthBeginnigSpace = 3;
		final int iWidthSeparator = 6;
		final int iWidthRadioButton = 20;
		
		utils.initJPanel(jpnParameter, 
			new double[] {iWidthBeginnigSpace, TableLayout.PREFERRED, iWidthSeparator, iWidthRadioButton, iWidthRadioButton, iWidthRadioButton, TableLayout.FILL},
			rowContraints);
		
		int iWidthLabelSize = 0;
		
		int iRow = 0;
		lstParameterEditors.clear();
		for (ParameterComparison parameter : allParameterSorted) {
			final ParameterEditor pe = new ParameterEditor(parameter);
			
			// init last state
			if (setSelectedCurrentParameter.contains(pe.getName()))
				pe.setCurrentValue();
			if (setSelectedIncomingParameter.contains(pe.getName()))
				pe.setIncomingValue();
			if (mapSelectedOtherParameter.containsKey(pe.getName()))
				pe.setOtherValue(mapSelectedOtherParameter.get(pe.getName()));
			
			// add change listener to update the radio buttons in footer
			pe.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					updateParameterAllSelection();
				}
			});
			
			pe.addToStepContent(jpnParameter, iRow);
			lstParameterEditors.add(pe);
			
			if (pe.getLabelPrefferedSize().width > iWidthLabelSize)
				iWidthLabelSize = pe.getLabelPrefferedSize().width;
			iRow++;
		}
		updateParameterAllSelection();
		jpnParameter.add(new JSeparator(JSeparator.VERTICAL), "2,0,2,"+(iRow-1));
		
		// setup parameter header	
		utils.initJPanel(jpnParameterHeader, 
			new double[] {iWidthBeginnigSpace, iWidthLabelSize+iWidthSeparator+(iWidthRadioButton/2)+utils.TABLE_LAYOUT_H_GAP+1/*Border*/, iWidthRadioButton, iWidthRadioButton, TableLayout.FILL}, 
			new double[] {TableLayout.PREFERRED,
							  TableLayout.PREFERRED,
							  TableLayout.PREFERRED,
							  4});
		
		if (allParameterSorted.isEmpty()) {
			jpnParameterHeader.add(new JLabel(cld.getMessage(
					"dbtransfer.import.parameterpanel.18", "Keine Parameter vorhanden.")), "1,2,4,2");
			return;
		}
		
		jpnParameterHeader.add(new JLabel(cld.getMessage(
				"dbtransfer.import.parameterpanel.1", "Parameter")), "1,2");
		jpnParameterHeader.add(new JLabel(cld.getMessage(
				"dbtransfer.import.parameterpanel.2", "aktuellen Zustand beibehalten")), "2,0,4,0");
		jpnParameterHeader.add(new JSeparator(JSeparator.VERTICAL), "2,1,2,3");
		jpnParameterHeader.add(new JLabel(cld.getMessage(
				"dbtransfer.import.parameterpanel.3", "importierten Zustand \u00fcbernehmen")), "3,1,4,1");
		jpnParameterHeader.add(new JSeparator(JSeparator.VERTICAL), "3,2,3,3");
		jpnParameterHeader.add(new JLabel(cld.getMessage(
				"dbtransfer.import.parameterpanel.4", "anderen Wert setzen")), "4,2");
		jpnParameterHeader.add(new JSeparator(JSeparator.VERTICAL), "4,3");
		
		// setup parameter footer		
		utils.initJPanel(jpnParameterFooter, 
			new double[] {iWidthBeginnigSpace, iWidthLabelSize+iWidthSeparator+utils.TABLE_LAYOUT_H_GAP+1/*Border*/, iWidthRadioButton, iWidthRadioButton, TableLayout.FILL, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}, 
			new double[] {TableLayout.PREFERRED});
		
		final JLabel lbCompare = new JLabel(cld.getMessage(
				"dbtransfer.import.parameterpanel.5", "\u00c4nderungen")+":");
		final JLabel lbCompareNew = new JLabel(iCountNew+"");
		final JLabel lbCompareDeleted = new JLabel(iCountDeleted+"");
		final JLabel lbCompareValueChanged = new JLabel(iCountValueChanged+"");
		
		lbCompareNew.setIcon(ParameterEditor.COMPARE_ICON_NEW);
		lbCompareDeleted.setIcon(ParameterEditor.COMPARE_ICON_DELETED);
		lbCompareValueChanged.setIcon(ParameterEditor.COMPARE_ICON_VALUE_CHANGED);
		
		lbCompare.setToolTipText(cld.getMessage(
				"dbtransfer.import.parameterpanel.6", "Vergleich von Aktueller- und Importkonfiguration"));
		lbCompareNew.setToolTipText(ParameterEditor.COMPARE_DESCRIPTION_NEW);
		lbCompareDeleted.setToolTipText(ParameterEditor.COMPARE_DESCRIPTION_DELETED);
		lbCompareValueChanged.setToolTipText(ParameterEditor.COMPARE_DESCRIPTION_VALUE_CHANGED);
		
		jpnParameterFooter.add(new JLabel(cld.getMessage(
				"dbtransfer.import.parameterpanel.7", "auf alle Parameter anwenden")), "1,0,r,c");
		jpnParameterFooter.add(rbCurrentAll, "2,0,l,c");
		jpnParameterFooter.add(rbIncomingAll, "3,0,l,c");
		jpnParameterFooter.add(lbCompare, "4,0,r,c");
		jpnParameterFooter.add(lbCompareNew, "5,0,r,c");
		jpnParameterFooter.add(lbCompareValueChanged, "6,0,r,c");
		jpnParameterFooter.add(lbCompareDeleted, "7,0,r,c");
	}
	
	private void resetParameterEditors(boolean toCurrent) {
		for (ParameterEditor pe : lstParameterEditors) {
			pe.reset(toCurrent);
		}
	}
	
	private void updateParameterAllSelection() {
		boolean blnIsCurrentAll = true;
		boolean blnIsIncomingAll = true;
		boolean blnIsNothingChanged = true;
		
		for (ParameterEditor pe : lstParameterEditors) {
			if (pe.isOtherValue()) {
				blnIsCurrentAll = false;
				blnIsIncomingAll = false;
				break;
			}
			if (pe.isCurrentValue() && pe.getParameter().isDeleted()) 
				blnIsIncomingAll = false;
			if (pe.isCurrentValue() && pe.getParameter().isValueChanged())
				blnIsIncomingAll = false;
			if (pe.isIncomingValue() && pe.getParameter().isDeleted())
				blnIsCurrentAll = false;
			if (pe.isIncomingValue() && pe.getParameter().isValueChanged() && !pe.getParameter().isNew())
				blnIsCurrentAll = false;
			
			if (pe.getParameter().isValueChanged())
				blnIsNothingChanged = false;
		}
		
		//zeige nur einen Button wenn Auswahl f\u00fcr beide gleich
		if (blnIsCurrentAll && blnIsIncomingAll) {
			if (blnIsNothingChanged) {
				rbIncomingAll.setVisible(false);
			} else {
				rbCurrentAll.setVisible(false);
			}
		}

		rbCurrentAll.setSelected(blnIsCurrentAll);
		rbIncomingAll.setSelected(blnIsIncomingAll);
	}
	
	/*
	 * Begin Step 4
	 */
	private PanelWizardStep newStep4(final MainFrameTab ifrm) {
		final PanelWizardStep step = new PanelWizardStep(cld.getMessage(
				"dbtransfer.import.step4.1", "Import"), 
				cld.getMessage(
						"dbtransfer.import.step4.2", "Der Import der Konfiguration kann nun gestartet werden."));
		
		utils.initJPanel(step,
			new double[] {180, TableLayout.FILL},
			new double[] {TableLayout.PREFERRED,
							  10,
							  TableLayout.PREFERRED,
							  TableLayout.PREFERRED});
		
		final JLabel lbWarning = new JLabel("<html>"+ cld.getMessage(
				"dbtransfer.import.step4.3", " <b>Achtung</b>: Durch den Konfigurationsimport werden Teile der Konfiguration auf diesem System \u00fcberschrieben!<br>" +
			"Vor dem Import wird dringend empfohlen ein Backup der Datenbank durchzuf\u00fchren.<br><br>" +
			"Wollen Sie fortfahren und den Konfigurationimport starten?")+"</html>");
		final JButton btnStartImport = new JButton(cld.getMessage(
				"dbtransfer.import.step4.4", "importieren")+"...");
//		final JProgressBar progressBar = new JProgressBar(0, 200);
		
		step.add(lbWarning, "0,0,1,0");
		step.add(btnStartImport, "0,2");
//		step.add(progressBar, "0,3");
		
		btnStartImport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
//				progressBar.setValue(0);
				ifrm.lockLayerWithProgress(Transfer.TOPIC_CORRELATIONID_RUN);
				
				Thread t = new Thread(){
					@Override
					public void run() {
						try {
							btnStartImport.setEnabled(false);
							blnImportStarted = true;
							wizard.getModel().refreshModelState();
//							progressBar.setVisible(true);
							
							// start configuration import
							
							if (chbxIncludeUser.isSelected()) {
								importTransferObject.getTransferOptions().put(TransferOption.IMPORT_USER, null);
							} else {
								importTransferObject.getTransferOptions().remove(TransferOption.IMPORT_USER);
							}
							if (chbxIncludeLDAP.isSelected()) {
								importTransferObject.getTransferOptions().put(TransferOption.IMPORT_USER, null);
							} else {
								importTransferObject.getTransferOptions().remove(TransferOption.IMPORT_USER);
							}
							if (chbxIncludeObjectimport.isSelected()) {
								importTransferObject.getTransferOptions().put(TransferOption.IMPORT_USER, null);
							} else {
								importTransferObject.getTransferOptions().remove(TransferOption.IMPORT_USER);
							}
							if (chbxAlternativeDBLogin.isSelected()) {
								importTransferObject.getTransferOptions().put(TransferOption.DBADMIN, tfAlternativeDBLogin.getText());
								importTransferObject.getTransferOptions().put(TransferOption.DBADMIN_PASSWORD, new String(pfAlternativeDBPassword.getPassword()));
							} else {
								importTransferObject.getTransferOptions().remove(TransferOption.DBADMIN);
								importTransferObject.getTransferOptions().remove(TransferOption.DBADMIN_PASSWORD);
							}
							
							// create new parameter list
							Collection<EntityObjectVO> parameterSelection = new ArrayList<EntityObjectVO>();
							for (ParameterEditor pe : lstParameterEditors) {
								if (pe.getParameter().isDeleted() && pe.isIncomingValue()) {
									// nothing to do. if parameter not in list runTransfer would delete it.
								} else {
									EntityObjectVO parameterCopy = new EntityObjectVO();
									parameterCopy.initFields(pe.getParameter().getFields().size(), 0);
									parameterCopy.getFields().putAll(pe.getParameter().getFields());
									parameterCopy.getFields().put("value", pe.getValue());
									parameterSelection.add(parameterCopy);
								}
							}
							
							// copy transfer to add parameter list.
							// only temporary in case we go back one step, then we need the old list. 
							Transfer transfer = new Transfer(importTransferObject);
							transfer.setParameter(parameterSelection);
							
							importTransferResult = utils.getTransferFacade().runTransfer(transfer);
							
							// Nicht invalidateAllClientCaches() aufrufen! Nach einem Aufruf sind die Menus solange deaktiviert, 
							// bis alle NovabitInternalFrames geschlossen wurden... BUG?
							// 
							// Main.getMainController().invalidateAllClientCaches();
							
							notifyParent.actionPerformed(new ActionEvent(DBTransferImport.this, 0, IMPORT_EXECUTED));
							step.setComplete(true);
							model.nextStep();
						} catch (Exception e) {
//							progressBar.setVisible(false);
							btnStartImport.setEnabled(true);
							blnImportStarted = false;
							wizard.getModel().refreshModelState();
							Errors.getInstance().showExceptionDialog(ifrm, e);
						} finally {
//							progressBar.setVisible(false);
							ifrm.unlockLayer();
							
							//invalidateAllCaches muss mit runCommandLater aufgerufen werden, da irgendeine Methode setzt den GlassPane
							//auf visible und nach dem Beenden der Aktion nicht mehr auf invisible. Aus disem Grund ist der Men\u00fc nicht "erreichbar".
							UIUtils.runCommandLater(Main.getInstance().getMainFrame(), new CommonRunnable() {			
								@Override
								public void run() throws CommonBusinessException {
									Main.getInstance().getMainController().invalidateAllClientCaches();
								}
							});	
						}
					}
				};
				
				t.start();
			}
		});
//		progressBar.setVisible(false);
		
		return step;
	}
	
	/*
	 * Begin Step 5
	 */
	private PanelWizardStep newStep5(final MainFrameTab ifrm) {
		final JLabel lbResult = new JLabel();
		final JEditorPane editLog = new JEditorPane();
		final JScrollPane scrollLog = new JScrollPane(editLog);
		final JLabel lbStructureChangeResult = new JLabel(cld.getMessage(
				"dbtransfer.import.step5.1", "\u00c4nderungen am Datenbankschema"));
		final JButton btnSaveStructureChangeScript = new JButton(cld.getMessage(
				"dbtransfer.import.step5.2", "Script speichern")+"...");
		final JButton btnSaveStructureChangeLog = new JButton(cld.getMessage(
				"dbtransfer.import.step5.3", "Log speichern")+"...");
		
		editLog.setContentType("text/html");
		editLog.setEditable(false);
		
		final PanelWizardStep step = new PanelWizardStep(cld.getMessage("dbtransfer.import.step5.4", "Ergebnis"), 
				cld.getMessage("dbtransfer.import.step5.5", "Hier wird Ihnen das Ergebnis des Imports angezeigt.")){

				@Override
				public void prepare() {
					if (!importTransferResult.hasWarnings() && !importTransferResult.hasCriticals()){
						lbResult.setText(cld.getMessage("dbtransfer.import.step5.6", "Import erfolgreich!"));
						this.setComplete(true);	
					} else {
						lbResult.setText(cld.getMessage("dbtransfer.import.step5.7", "Ein Problem ist aufgetreten!"));
						blnSaveOfLogRecommend = true;
					}
					StringBuffer sbLog = new StringBuffer();
					sbLog.append("<html><body>");
					if (!importTransferResult.foundReferences.isEmpty()) {
						sbLog.append(cld.getMessage(
								"dbtransfer.import.step5.8", "Folgende Konfigurationsobjekte sollten entfernt werden, werden aber noch verwendet") + ":<br />");
						for (Pair<String, String> reference : importTransferResult.foundReferences) {
							sbLog.append("- " + reference.y + " (" 
									+ cld.getLabelFromMetaDataVO(MetaDataClientProvider.getInstance().getEntity(reference.x)) + ")<br />");
						}
						sbLog.append("<br />" + cld.getMessage(
								"dbtransfer.import.step5.9", "Passen Sie Ihre Konfiguration dahingehend an oder bearbeiten Sie die Daten,\nwelche noch auf die Konfigurationsobjekte verweisen."));
						sbLog.append("<br />");
					}
					sbLog.append("<font color=\"#800000\">" + importTransferObject.result.getCriticals() + "</font>" +
						(importTransferObject.result.hasCriticals()?"<br />":""));
					sbLog.append(importTransferResult.getWarnings());
					sbLog.append("</body></html>");
					editLog.setText(sbLog.toString());
				}
		};
		
		utils.initJPanel(step,
			new double[] {TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL},
			new double[] {20,
							  TableLayout.FILL,
							  TableLayout.PREFERRED});
		
		step.add(lbResult, "0,0,3,0");
		step.add(scrollLog, "0,1,3,1");
		step.add(lbStructureChangeResult, "0,2");
		step.add(btnSaveStructureChangeScript, "1,2");
		step.add(btnSaveStructureChangeLog, "2,2");
		
		btnSaveStructureChangeScript.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					UIUtils.showWaitCursorForFrame(ifrm, true);
					
					final StringBuffer sbSql = new StringBuffer();
					org.apache.commons.collections.CollectionUtils.forAllDo(importTransferResult.script, new Closure() {
						@Override
						public void execute(Object element) {
							sbSql.append("<DDL>" + element + "</DDL>\n\n");
						}
					});
					
					final JFileChooser filechooser = utils.getFileChooser(cld.getMessage(
							"configuration.transfer.file.sql", "SQL-Dateien"), ".import-sql.txt");
					filechooser.setSelectedFile(new File(tfTransferFile.getText() + ".import-sql.txt"));
					final int iBtn = filechooser.showSaveDialog(step);

					if (iBtn == JFileChooser.APPROVE_OPTION) {
						final File file = filechooser.getSelectedFile();
						if (file != null) {
							String fileName = file.getPath();

							if (!fileName.toLowerCase().endsWith(".import-sql.txt")) {
								fileName += ".import-sql.txt";
							}
							
							File outFile = new File(fileName);
		               FileWriter out = new FileWriter(outFile);
		               out.write(sbSql.toString());
		               out.close();
		               
		               if (blnSaveOfScriptRecommend) {
		               	step.setComplete(true);
		               }
						}
					}
					
				}
				catch (Exception e) {
					Errors.getInstance().showExceptionDialog(ifrm, e);
				}
				finally {
					UIUtils.showWaitCursorForFrame(ifrm, false);
				}
			}
		});
		btnSaveStructureChangeLog.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					UIUtils.showWaitCursorForFrame(ifrm, true);
					
					final JFileChooser filechooser = utils.getFileChooser(cld.getMessage(
							"configuration.transfer.file.log", "Log-Dateien"), ".import-log.html");
					filechooser.setSelectedFile(new File(tfTransferFile.getText() + ".import-log.html"));
					final int iBtn = filechooser.showSaveDialog(step);

					if (iBtn == JFileChooser.APPROVE_OPTION) {
						final File file = filechooser.getSelectedFile();
						if (file != null) {
							String fileName = file.getPath();

							if (!fileName.toLowerCase().endsWith(".import-log.html")) {
								fileName += ".import-log.html";
							}
							
							File outFile = new File(fileName);
		               FileWriter out = new FileWriter(outFile);
		               out.write(editLog.getText());
		               out.close();
		               
		               if (blnSaveOfLogRecommend) {
		               	step.setComplete(true);
		               }
						}
					}
					
				}
				catch (Exception e) {
					Errors.getInstance().showExceptionDialog(ifrm, e);
				}
				finally {
					UIUtils.showWaitCursorForFrame(ifrm, false);
				}
			}
		});
		
		return step;
	}
	
}
