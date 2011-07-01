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
package org.nuclos.client.main;

import java.awt.Component;

import javax.swing.JOptionPane;

import org.nuclos.client.ui.Errors;
import org.nuclos.common2.CommonLocaleDelegate;

/**
 * <code>CriticalErrorHandler</code> for the Nucleus client.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public class NuclosCriticalErrorHandler implements Errors.CriticalErrorHandler {

	private final boolean bStrict;

	/**
	 * @param bStrict <code>true</code>: always abort on critical errors. <code>false</code>: Let the user decide.
	 */
	public NuclosCriticalErrorHandler(boolean bStrict) {
		this.bStrict = bStrict;
	}

	@Override
	public void handleCriticalError(Component parent, Error error) {
		error.printStackTrace(System.err);
		if (bStrict) {
			// avoid multiple messages:
			synchronized (this) {
				final String sMessage = CommonLocaleDelegate.getMessage("NuclosCriticalErrorHandler.1", "Es ist ein kritischer Systemfehler aufgetreten.") + "\n" +
						CommonLocaleDelegate.getMessage("NuclosCriticalErrorHandler.2", "Aus Sicherheitsgr\u00fcnden muss die Applikation beendet werden.");
				Errors.getInstance().showExceptionDialog(parent, sMessage, error);
				Main.exit(Main.ExitResult.ABNORMAL);
			}
		}
		else {
			final String sMessage = CommonLocaleDelegate.getMessage("NuclosCriticalErrorHandler.1", "Es ist ein kritischer Systemfehler aufgetreten.") + "\n" +
			CommonLocaleDelegate.getMessage("NuclosCriticalErrorHandler.3", "Aus Sicherheitsgr\u00fcnden wird empfohlen, die Applikation sofort und ohne Speichern zu beenden.");
			Errors.getInstance().showExceptionDialog(parent, sMessage, error);
			final int iBtn = JOptionPane.showConfirmDialog(parent, CommonLocaleDelegate.getMessage("NuclosCriticalErrorHandler.4", "Applikation beenden? (Empfohlen)"),
				CommonLocaleDelegate.getMessage("NuclosCriticalErrorHandler.5", "Kritischer Systemfehler"), JOptionPane.YES_NO_OPTION);
			if (iBtn == JOptionPane.YES_OPTION) {
				Main.exit(Main.ExitResult.ABNORMAL);
			}
		}
	}

}	// class NuclosCriticalErrorHandler
