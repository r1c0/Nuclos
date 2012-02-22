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
package org.nuclos.client.help.internalinfo;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.JComponent;

import org.apache.log4j.Logger;
import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.ui.Controller;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common2.ClientPreferences;

/**
 * Controller for displaying the internal info page.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */

public class InternalInfoController extends Controller {

	private static final Logger LOG = Logger.getLogger(InternalInfoController.class);
	
	private static final String PREFS_KEY_FILEDATE = "version";
	private static final String PREFS_NODE_INTERNALINFO = "internalInfo";

	private final Preferences prefs = ClientPreferences.getUserPreferences().node(PREFS_NODE_INTERNALINFO);

	private final String sFilePath = ClientParameterProvider.getInstance().getValue(ParameterProvider.KEY_INTERNAL_INFORMATION_FILE_PATH);
	private final File fileInternalInfoFile;

	/**
	 * @param parent
	 * @param parentMdi the parent for MDI windows
	 */
	public InternalInfoController(Component parent, JComponent parentMdi) {
		super(parent);

		fileInternalInfoFile = sFilePath != null ? new File(sFilePath) : null;
	}

	/**
	 * shows the internal info page if the date of this file has changed since last time.
	 */
	public void showInternalInfoIfChanged() {
		final long lLastFileDate = prefs.getLong(PREFS_KEY_FILEDATE, 0L);
		if (fileInternalInfoFile != null && lLastFileDate != fileInternalInfoFile.lastModified()) {
			this.showInternalInfo();
			prefs.putLong(PREFS_KEY_FILEDATE, fileInternalInfoFile.lastModified());
		}
	}

	/**
	 * shows the internal info page.
	 */
	public void showInternalInfo() {
		if (fileInternalInfoFile != null) {
			UIUtils.runCommandLater(this.getParent(), new Runnable() {
				@Override
				public void run() {
					try {
						if (!fileInternalInfoFile.exists()) {
							final String sMessage = getSpringLocaleDelegate().getMessage(
									"InternalInfoController.1", "Die Datei {0} f\u00fcr die Onlinehilfe existiert nicht.", fileInternalInfoFile.getAbsolutePath());
							Errors.getInstance().showExceptionDialog(InternalInfoController.this.getParent(), new IOException(sMessage));
						}
						else {
							try {
								Runtime.getRuntime().exec("cmd /c " + " \"" + fileInternalInfoFile.getAbsolutePath() + "\"");
							}
							catch (IOException ex) {
								final String sMessage = getSpringLocaleDelegate().getMessage(
										"InternalInfoController.2", "Die Informationen k\u00f6nnen nicht angezeigt werden.");
								Errors.getInstance().showExceptionDialog(InternalInfoController.this.getParent(), sMessage, ex);
							}
						}
					}
					catch (Exception e) {
						LOG.error("showInternalInfo failed: " + e, e);
					}
				}
			});
		}
	}

}	// class InternalInfoController
