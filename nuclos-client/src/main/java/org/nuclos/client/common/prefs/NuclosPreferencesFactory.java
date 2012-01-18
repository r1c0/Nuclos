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
package org.nuclos.client.common.prefs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

import org.apache.log4j.Logger;
import org.nuclos.client.common.ShutdownActions;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.common.ejb3.PreferencesFacadeRemote;
import org.nuclos.server.common.valueobject.PreferencesVO;

/**
 * <code>PreferencesFactory</code> for <code>NuclosPreferences</code>.
 * In order to use <code>NuclosPreferences</code> as <code>Preferences</code>,
 * you have to set the system property
 * <code>java.util.prefs.PreferencesFactory</code> to
 * <code>NuclosPreferencesFactory.class.getName()</code>.
 * <br>Based on Code found in Sun's Bug Database, Bug #4788410.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class NuclosPreferencesFactory implements PreferencesFactory {
	
	private static final Logger LOG = Logger.getLogger(NuclosPreferencesFactory.class);

	private PreferencesFacadeRemote facade;
	private Preferences prefsUser;
	private Preferences prefsSystem;

	public NuclosPreferencesFactory() {
		// do nothing here
	}

	private synchronized PreferencesFacadeRemote getFacade() throws RemoteException {
		if (this.facade == null) {
			try {
				facade = ServiceLocator.getInstance().getFacade(PreferencesFacadeRemote.class);
			}
			catch (RuntimeException e) {
				throw new CommonFatalException(e);
			}
		}
		return this.facade;
	}

	/**
	 * provides dummy preferences for those who insist on using the system preferences.
	 * @return dummy (system) preferences
	 */
	@Override
	public synchronized Preferences systemRoot() {
		Logger.getLogger(NuclosPreferencesFactory.class).warn("NuclosPreferencesFactory.systemRoot() wird nicht unterst\u00fctzt.");

		if (prefsSystem == null) {
			prefsSystem = new DummyPreferences();
		}
		return prefsSystem;
	}

	/**
	 * Someone in java.awt tries to access the userRoot() before the user was logged in. So we *try*
	 * to get the preferences from the server, but in case of a security exception leave the userRoot()
	 * uninitialized. java.awt prints a stack trace, but continues.
	 * @return the user root. May be null, if the user was not logged in.
	 */
	@Override
	public synchronized Preferences userRoot() {
		if (this.prefsUser == null) {

			final String sErrorMsg = CommonLocaleDelegate.getInstance().getMessage("NuclosPreferencesFactory.1", "Die Benutzereinstellungen konnten nicht geladen werden.");

//			try {
			PreferencesVO prefsvo;
			try {
				prefsvo = this.getFacade().getUserPreferences();
			}
			catch (CommonFinderException ex) {
				// ignore: this happens when the user logs in the first time - there are no preferences yet.
				prefsvo = null;
			}
			catch (RemoteException ex) {
				// This might happen if somebody tries to access the userRoot() before a login
				// was successfully performed.
				throw new NuclosFatalException(sErrorMsg, ex);
			}
			catch (Exception e) {
				LOG.warn("userRoot failed: " + e, e);
				prefsvo = null;
			}
			this.prefsUser = new NuclosPreferencesRoot(this.facade);

			if (prefsvo != null) {
				// import the read preferences:
				try {
					final byte[] abXml = prefsvo.getPreferencesBytes();
					final ByteArrayInputStream is = new ByteArrayInputStream(abXml);
					Preferences.importPreferences(is);
					is.close();
				}
				catch (IOException ex) {
					throw new NuclosFatalException(sErrorMsg, ex);
				}
				catch (InvalidPreferencesFormatException ex) {
					throw new NuclosFatalException(sErrorMsg, ex);
				}
			}
//			}
//			catch (RuntimeException ex) {
//				Errors.getInstance().showExceptionDialog(null, ex);
//				final String sMessage = "Sollen Ihre pers\u00f6nlichen Benutzereinstellungen zur\u00fcckgesetzt werden?\n" +
//				    "(Achtung: Ihre bisherigen Einstellungen (Suchfilter etc.) gehen dadurch verloren.)";
//				int iBtn = JOptionPane.showConfirmDialog(null, sMessage, "Fehler beim Laden der Benutzereinstellungen",
//				    JOptionPane.YES_NO_OPTION);
//				if (iBtn == JOptionPane.YES_OPTION) {
//					// do nothing. prefsUser is initialized already.
//				}
//				else {
//					throw ex;
//				}
//			}

			// register shutdown action: sync at system exit:
			ShutdownActions.getInstance().registerShutdownAction(ShutdownActions.SHUTDOWNORDER_SAVEPREFERENCES, new Flush(this.prefsUser));
		}	// if

		assert this.prefsUser != null;

		return this.prefsUser;
	}

	private static class Flush implements Runnable {
		private final Preferences prefsUser;

		Flush(Preferences prefsUser) {
			this.prefsUser = prefsUser;
		}

		@Override
		public void run() {
			try {
				prefsUser.flush();
			}
			catch (BackingStoreException ex) {
				throw new NuclosFatalException(CommonLocaleDelegate.getInstance().getMessage(
						"NuclosPreferencesFactory.2", "Die Benutzer-Einstellungen konnten nicht gespeichert werden."), ex);
			}
		}
	}

}	// class NuclosPreferencesFactory
