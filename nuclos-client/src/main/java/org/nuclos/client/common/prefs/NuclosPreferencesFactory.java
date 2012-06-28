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
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.common.ejb3.PreferencesFacadeRemote;
import org.nuclos.server.common.valueobject.PreferencesVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

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
@Configurable
public class NuclosPreferencesFactory implements PreferencesFactory {
	
	private static final Logger LOG = Logger.getLogger(NuclosPreferencesFactory.class);
	
	//
	
	// Spring injection

	private PreferencesFacadeRemote preferencesFacadeRemote;
	
	// end of Spring injection
	
	private Preferences prefsUser;
	
	private Preferences prefsSystem;
	
	private Flush flush;

	public NuclosPreferencesFactory() {
		// do nothing here
	}
	
	@Autowired
	final void setPreferencesFacadeRemote(PreferencesFacadeRemote preferencesFacadeRemote) {
		this.preferencesFacadeRemote = preferencesFacadeRemote;
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

			final String sErrorMsg = SpringLocaleDelegate.getInstance().getMessage(
					"NuclosPreferencesFactory.1", "Die Benutzereinstellungen konnten nicht geladen werden.");

//			try {
			PreferencesVO prefsvo;
			try {
				prefsvo = preferencesFacadeRemote.getUserPreferences();
			}
			catch (CommonFinderException ex) {
				// ignore: this happens when the user logs in the first time - there are no preferences yet.
				prefsvo = null;
			}
			catch (Exception e) {
				LOG.warn("userRoot failed: " + e, e);
				prefsvo = null;
			}
			this.prefsUser = new NuclosPreferencesRoot(preferencesFacadeRemote);

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
			// register shutdown action: sync at system exit:
			flush = new Flush(this.prefsUser);
			ShutdownActions.getInstance().registerShutdownAction(ShutdownActions.SHUTDOWNORDER_SAVEPREFERENCES, flush);
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
				LOG.info("saved user prerferences");
			}
			catch (BackingStoreException ex) {
				throw new NuclosFatalException(SpringLocaleDelegate.getInstance().getMessage(
						"NuclosPreferencesFactory.2", "Die Benutzer-Einstellungen konnten nicht gespeichert werden."), ex);
			}
		}
	}

}	// class NuclosPreferencesFactory
