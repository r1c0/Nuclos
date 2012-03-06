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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.annotation.PostConstruct;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.nuclos.client.NuclosIcons;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.main.mainframe.MainFrameTabbedPane;
import org.nuclos.client.wizard.WizardFrame;
import org.nuclos.common.dbtransfer.Transfer;
import org.nuclos.common.dbtransfer.TransferNuclet;
import org.nuclos.common.dbtransfer.TransferOption;
import org.nuclos.common2.SpringLocaleDelegate;
import org.pietschy.wizard.I18n;
import org.pietschy.wizard.PanelWizardStep;
import org.pietschy.wizard.WizardEvent;
import org.pietschy.wizard.WizardListener;
import org.pietschy.wizard.models.StaticModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class DBTransferExport {
	
	private final Long nucletId;
	
	private final DBTransferUtils utils = new DBTransferUtils();
	
	private DBTransferWizard wizard;
	private StaticModel model;
	private PanelWizardStep step1, step2;
	
	private Exception exportException;
	
	private SpringLocaleDelegate localeDelegate;
	
	private MainFrameTab ifrm;
	
	public DBTransferExport(Long nucletId) {
		this.nucletId = nucletId;
	}
	
	@PostConstruct
	final void init() {
		I18n.setBundle(DBTransferWizard.getResourceBundle());
		
		ifrm = Main.getInstance().getMainController().newMainFrameTab(
				null, localeDelegate.getMessage("dbtransfer.export.title", "Konfiguration exportieren"));
		ifrm.setTabIcon(NuclosIcons.getInstance().getDefaultFrameIcon());
		
		step1 = newStep1(ifrm);
		step2 = newStep2(ifrm);

		model = new StaticModel(){
			@Override
			public boolean isLastVisible() {
				return false;
			}

			@Override
			public void refreshModelState() {
				super.refreshModelState();
				if (wizard != null) {

				}
			}
		};
		
		model.add(step1);
		model.add(step2);

		wizard = new DBTransferWizard(model);
      
      wizard.addWizardListener(new WizardListener() {
            @Override
            public void wizardClosed(WizardEvent e) {
            	ifrm.dispose();
            }
            @Override
            public void wizardCancelled(WizardEvent e) {
            	ifrm.dispose();             
            }
      });
	}
	
	@Autowired
	void setSpringLocaleDelegate(SpringLocaleDelegate cld) {
		this.localeDelegate = cld;
	}
	
	public void showWizard(MainFrameTabbedPane homePane) {
      ifrm.setLayeredComponent(WizardFrame.createFrameInScrollPane(wizard));
      
      homePane.add(ifrm);
      
      ifrm.setVisible(true);
	}

	private PanelWizardStep newStep1(final MainFrameTab ifrm) {
		final PanelWizardStep step = new PanelWizardStep(localeDelegate.getMessage(
				"dbtransfer.export.step1.1", "Export"), 
				localeDelegate.getMessage("dbtransfer.export.step1.2", 
						"Bitte w\u00e4hlen Sie den Speicherort f\u00fcr die Konfigurationsdatei aus und die Optionen."));
		utils.initJPanel(step,
			new double[] {TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL},
			new double[] {20,				  
							  20,
							  20,
							  20,
							  20,
							  20,
							  20,
							  20,
							  TableLayout.PREFERRED,
							  TableLayout.PREFERRED});
		
		final JLabel lbNuclet = new JLabel("Nuclet");
		final JComboBox comboNuclet = new JComboBox(utils.getAvaiableNuclets());
		final JLabel lbFile = new JLabel(localeDelegate.getMessage(
				"dbtransfer.import.step1.3", "Datei"));
		final JTextField tfTransferFile = new JTextField(50);
		final JButton btnBrowse = new JButton("...");
		final JCheckBox chbxIsNuclonImportAllowed = new JCheckBox(localeDelegate.getMessage(
				"configuration.transfer.export.option.isnuclonallowed", "Import als Nuclon gestatten"));
		final JCheckBox chbxIncludeUser = new JCheckBox(localeDelegate.getMessage(
				"configuration.transfer.export.option.user", "Benutzer exportieren"));
		final JCheckBox chbxIncludeLdap = new JCheckBox(localeDelegate.getMessage(
				"configuration.transfer.export.option.includeldap", "LDAP Konfiguration exportieren"));
		final JCheckBox chbxIncludeImportFile = new JCheckBox(localeDelegate.getMessage(
				"configuration.transfer.export.option.includeimportfile", "Objektimport exportieren"));
//		final JCheckBox chbxFreezeConfiguration = new JCheckBox(getMessage("configuration.transfer.freeze", "Konfiguration nach dem Export einfrieren"));
		final JButton btnStartExport = new JButton(localeDelegate.getMessage(
				"dbtransfer.export.step1.4", "exportieren")+"...");
//		final JProgressBar progressBar = new JProgressBar(0, 200);
		
		step.add(lbNuclet, "0,0");
		step.add(comboNuclet, "1,0");
		step.add(lbFile, "0,1");
		step.add(tfTransferFile, "1,1");
		step.add(btnBrowse, "2,1");
		step.add(chbxIsNuclonImportAllowed, "1,3");
		step.add(chbxIncludeUser, "1,4");
		step.add(chbxIncludeLdap, "1,5");
		step.add(chbxIncludeImportFile, "1,6");
		step.add(btnStartExport, "1,8");
//		step.add(progressBar, "1,9");
		
		// Freeze ist bisher noch nicht vollst\u00e4ndig implementiert.
		// Das Setzen des Flags w\u00fcrde nur unn\u00f6tig Erwartungen wecken.
//		chbxFreezeConfiguration.setEnabled(false);
		
		ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chbxIsNuclonImportAllowed.setSelected(comboNuclet.getSelectedIndex()!=0);
				chbxIsNuclonImportAllowed.setEnabled(comboNuclet.getSelectedIndex()!=0);
				chbxIncludeUser.setSelected(comboNuclet.getSelectedIndex()==0);
				chbxIncludeUser.setEnabled(comboNuclet.getSelectedIndex()==0);
				chbxIncludeLdap.setSelected(comboNuclet.getSelectedIndex()==0);
				chbxIncludeLdap.setEnabled(comboNuclet.getSelectedIndex()==0);
				chbxIncludeImportFile.setSelected(comboNuclet.getSelectedIndex()==0);
				chbxIncludeImportFile.setEnabled(comboNuclet.getSelectedIndex()==0);
			}
		};
		if (nucletId == null) {
			comboNuclet.setSelectedIndex(0);
		} else {
			comboNuclet.setSelectedItem(new TransferNuclet(nucletId, null));
		}
		al.actionPerformed(new ActionEvent(comboNuclet, 0, "init"));
		comboNuclet.addActionListener(al);
		
		btnBrowse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				final JFileChooser filechooser = utils.getFileChooser(localeDelegate.getMessage(
						"configuration.transfer.file.nuclet", "Nuclet-Dateien"), ".nuclet");
				TransferNuclet tn = (TransferNuclet) comboNuclet.getSelectedItem();
				final String filename = tn.getId()==null ? "Nuclos.instance" : tn.getLabel();
				filechooser.setSelectedFile(new File(filename + ".nuclet"));
				final int iBtn = filechooser.showSaveDialog(ifrm);

				if (iBtn == JFileChooser.APPROVE_OPTION) {
					final File file = filechooser.getSelectedFile();
					if (file != null) {
						String fileName = file.getPath();

						if (!fileName.toLowerCase().endsWith(".nuclet")) {
							fileName += ".nuclet";
						}
						tfTransferFile.setText(fileName);
						btnStartExport.setEnabled(true);
					}
				}
			}
		});
		btnStartExport.setEnabled(false);
		btnStartExport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
//				progressBar.setValue(0);
				ifrm.lockLayerWithProgress(Transfer.TOPIC_CORRELATIONID_CREATE);
				
				Thread t = new Thread(){
					@Override
					public void run() {
						try {
							step.setComplete(false);
							btnStartExport.setEnabled(false);
//							progressBar.setVisible(true);
							
							int iShowingProgressSpeed = 20;
							TransferOption.Map exportOptions = new TransferOption.HashMap();
							if (chbxIsNuclonImportAllowed.isEnabled() && chbxIsNuclonImportAllowed.isSelected()) {
								exportOptions.put(TransferOption.IS_NUCLON_IMPORT_ALLOWED, null);
								iShowingProgressSpeed += 20;
							}
							if (chbxIncludeUser.isEnabled() && chbxIncludeUser.isSelected()) {
								exportOptions.put(TransferOption.INCLUDES_USER, null);
								iShowingProgressSpeed += 20;
							}
							if (chbxIncludeLdap.isEnabled() && chbxIncludeLdap.isSelected()) {
								exportOptions.put(TransferOption.INCLUDES_LDAP, null);
								iShowingProgressSpeed += 5;
							}
							if (chbxIncludeImportFile.isEnabled() && chbxIncludeImportFile.isSelected()) {
								exportOptions.put(TransferOption.INCLUDES_IMPORTFILE, null);
								iShowingProgressSpeed += 5;
							}
//							if (chbxFreezeConfiguration.isSelected()) {
//								exportOptions.put(TransferOption.FREEZE_CONFIGURATION, null);
//							}
							
							byte[] transferFile = utils.getTransferFacade().createTransferFile(((TransferNuclet)comboNuclet.getSelectedItem()).getId(), exportOptions);
							File f = new File(tfTransferFile.getText());
							FileOutputStream fout;
							fout = new FileOutputStream(f);
							fout.write(transferFile);
							fout.close();

							exportException = null;
						} catch (Exception e) {
							exportException = e;
						} finally {
//							progressBar.setVisible(false);
							ifrm.unlockLayer();
							
							btnStartExport.setEnabled(true);
							step.setComplete(true);
							model.nextStep();
						}
					}
				};
				
				t.start();
			}
		});
//		progressBar.setVisible(false);
	
		return step;
	}

	private PanelWizardStep newStep2(final MainFrameTab ifrm) {
		final JLabel lbResult = new JLabel();
		final JTextArea taLog = new JTextArea();
		final JScrollPane scrollLog = new JScrollPane(taLog);
		
		final PanelWizardStep step = new PanelWizardStep(localeDelegate.getMessage(
				"dbtransfer.import.step5.4", "Ergebnis"), 
				localeDelegate.getMessage("dbtransfer.export.step2.1", "Hier wird Ihnen das Ergebnis des Exports angezeigt.")){

				@Override
				public void prepare() {
					if (exportException == null) {
						setComplete(true);
						wizard.setCancelEnabled(false);
						lbResult.setText(localeDelegate.getMessage(
								"dbtransfer.export.step2.2", "Die Konfigurationsdaten wurden erfolgreich exportiert."));
						scrollLog.setVisible(false);
					} else {
						setComplete(false);
						wizard.setCancelEnabled(true);
						lbResult.setText(localeDelegate.getMessage(
								"dbtransfer.import.step5.7", "Ein Problem ist aufgetreten!"));
						scrollLog.setVisible(true);
						if (exportException instanceof FileNotFoundException){
							taLog.setText(localeDelegate.getMessage(
									"dbtransfer.export.step2.3", "Die ausgew\u00e4hlte Datei konnte nicht geschrieben bzw. ersetzt werden.\n" +
									"M\u00f6glicherweise ist diese noch ge\u00f6ffnet oder Ihnen fehlt eine Berechtigung."));
						} else {
							StringWriter sw = new StringWriter();
					      PrintWriter pw = new PrintWriter(sw, true);
					      exportException.printStackTrace(pw);
					      pw.flush();
					      sw.flush();
					      taLog.setText(sw.toString());
						}
					}
				}
		};
		
		utils.initJPanel(step,
			new double[] {TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL},
			new double[] {20,
							  TableLayout.FILL});
		
		step.add(lbResult, "0,0,3,0");
		step.add(scrollLog, "0,1,3,1");
		
		return step;
	}
}
