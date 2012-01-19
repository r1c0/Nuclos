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

import java.io.ByteArrayOutputStream;
import java.util.prefs.BackingStoreException;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.preferences.AbstractMapBasedPreferences;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.server.common.ejb3.PreferencesFacadeRemote;
import org.nuclos.server.common.valueobject.PreferencesVO;

/**
 * XML file implementation of the preferences API.<p>
 * For safety reasons, everything is synchronized across all instances of
 * <code>NuclosPreferences</code>.  That way nobody can change something in
 * the middle of writing to or reading from the file.
 * <br>Based on Code found in Sun's Bug Database, Bug #4788410.
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class NuclosPreferencesRoot extends AbstractMapBasedPreferences.Root {
	/**
	 * the file in case this node is a root node
	 */
	private final PreferencesFacadeRemote facade;

	/**
	 * creates a root node for Nucleus preferences
	 * @param facade
	 */
	NuclosPreferencesRoot(PreferencesFacadeRemote facade) {
		if (facade == null) {
			throw new NullArgumentException("facade");
		}
		this.facade = facade;
	}

	/**
	 * flushes the whole tree
	 */
	@Override
	protected void flushSpi() throws BackingStoreException {
		try {
			if (this.isDirty()) {
				if (facade == null) {
					throw new NuclosFatalException(CommonLocaleDelegate.getMessage("NuclosPreferences.1", "Benutzereinstellungen k\u00f6nnen nicht gespeichert werden."));
				}
				else {
					final ByteArrayOutputStream baos = new ByteArrayOutputStream();
					this.exportSubtree(baos);
					final String sXml = baos.toString("UTF-8");
					log.debug("sXml = " + sXml);
					byte[] bytes = baos.toByteArray();
					facade.modifyUserPreferences(new PreferencesVO(bytes));
					this.setDirty(false);
				}
			}
		}
		catch (Exception ex) {
			throw new BackingStoreException(ex);
		}
	}

}