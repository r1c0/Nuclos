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
package org.nuclos.client.transfer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.ui.CommonClientWorkerAdapter;
import org.nuclos.client.ui.CommonMultiThreader;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.transfer.ejb3.XmlExportImportProtocolFacadeRemote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Controller for export/import protocol.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 *
 * @author	<a href="mailto:Martin.Weber@novabit.de">Martin Weber</a>
 * @version 01.00.00
 * @todo activate this controller, if you want to save the export and import files
 * in the database
 *
 * NOTE: this controller should not be used, if the transfered data is not compressed
 *       because of memory problems
 */
@Configurable(preConstruction=true)
public class ExportImportCollectController extends MasterDataCollectController {

	private final JButton btnCopyFile = new JButton();

	private final String ZIP_EXTENSION = ".zip";
	
	// Spring injection

	private XmlExportImportProtocolFacadeRemote xmlExportImportFacade;
	
	// end of Spring injection

	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} 
	 * to get an instance.
	 * 
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public ExportImportCollectController(MainFrameTab tabIfAny) {
		super(NuclosEntity.IMPORTEXPORT, tabIfAny);
		this.setupDetailsToolBar();
	}
	
	@Autowired
	final void setXmlExportImportFacade(XmlExportImportProtocolFacadeRemote xmlExportImportFacade) {
		this.xmlExportImportFacade = xmlExportImportFacade;
	}

	private void setupDetailsToolBar(){
		//final JToolBar toolbarCustomDetails = UIUtils.createNonFloatableToolBar();

		this.btnCopyFile.setIcon(Icons.getInstance().getIconExport16());
		this.btnCopyFile.setToolTipText(getSpringLocaleDelegate().getMessage(
				"ExportImportCollectController.1", "Archivierte Export/Import Datei vom Server holen"));
		this.btnCopyFile.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ev){
				cmdCopyFile(ExportImportCollectController.this);
			}
		});

		//toolbarCustomDetails.add(btnCopyFile);
		//toolbarCustomDetails.add(Box.createHorizontalGlue());
		//this.getDetailsPanel().setCustomToolBarArea(toolbarCustomDetails);
		
		this.getDetailsPanel().addToolBarComponent(btnCopyFile);
	}

	private void cmdCopyFile(final CollectController<CollectableMasterDataWithDependants> clctctl){
		final JFileChooser filechooser = this.getFileChooser(getSpringLocaleDelegate().getMessage(
				"ExportImportCollectController.2", "Export/Import Datei speichern"), 
				getSpringLocaleDelegate().getMessage(
						"ExportImportCollectController.3", "Kopieren der archivierten Export/Import Datei"));

		final int iBtn = filechooser.showSaveDialog(getTabbedPane().getComponentPanel());
		if (iBtn == JFileChooser.APPROVE_OPTION) {
			if (filechooser.getSelectedFile() == null) {
				throw new NuclosFatalException(getSpringLocaleDelegate().getMessage(
						"ExportImportCollectController.4", "Bitte geben Sie einen Dateinamen ein!"));
			}

			CommonMultiThreader.getInstance().execute(new CommonClientWorkerAdapter<CollectableMasterDataWithDependants>(clctctl) {
				@Override
				public void work() throws CommonBusinessException {
					try {
						org.nuclos.common2.File fZip = xmlExportImportFacade.getFile((Integer)ExportImportCollectController.this.getSelectedCollectable().getId());

						String sFileName = filechooser.getSelectedFile().getAbsolutePath();
						if (!sFileName.toLowerCase().endsWith(ZIP_EXTENSION)) {
							sFileName = sFileName += ZIP_EXTENSION;
						}

						IOUtils.writeToBinaryFile(new File(sFileName), fZip.getContents());
					}
					catch (RuntimeException e) {
						throw new NuclosFatalException(getSpringLocaleDelegate().getMessage(
								"ExportImportCollectController.5", "Ein Fehler beim Kopieren der Datei ist aufgetreten") + ": "+e);
					}
					catch (IOException e){
						throw new NuclosFatalException(getSpringLocaleDelegate().getMessage(
								"ExportImportCollectController.5", "Ein Fehler beim Kopieren der Datei ist aufgetreten") + ": "+e);
					}
				}
			});
		}
	}

	/**
	 * @return FileChooser to copy the file to the local syste,
	 */
	private final JFileChooser getFileChooser(String sTitle, String sTootltip) {
		final JFileChooser result = new JFileChooser();
		result.setApproveButtonText(sTitle);
		result.setApproveButtonMnemonic(sTitle.toCharArray()[0]);
		result.setApproveButtonToolTipText(sTootltip);
		result.addChoosableFileFilter(filefilter);
		return result;
	}

	protected final FileFilter filefilter = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return file.isDirectory() || file.getName().toLowerCase().endsWith(ZIP_EXTENSION);
		}

		@Override
		public String getDescription() {
			return getSpringLocaleDelegate().getMessage(
					"ExportImportCollectController.6", "Komprimierte Dateien (*{0})", ZIP_EXTENSION);
		}
	};
}
