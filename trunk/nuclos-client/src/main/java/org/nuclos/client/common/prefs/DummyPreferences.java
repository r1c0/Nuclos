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

import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

/**
 * Dummy preferences to handle this <em>stupid</em> <code>Preferences</code> class.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public class DummyPreferences extends AbstractPreferences {

	public DummyPreferences() {
		this(null, "");
	}

	private DummyPreferences(AbstractPreferences parent, String name) {
		super(parent, name);
	}

	@Override
	protected String[] childrenNamesSpi()
			throws BackingStoreException {
		return new String[0];
	}

	@Override
	protected AbstractPreferences childSpi(String name) {
		return null;
	}

	@Override
	protected void syncSpi() throws BackingStoreException {
		// do nothing
	}

	@Override
	protected void flushSpi() throws BackingStoreException {
		// do nothing
	}

	@Override
	protected String getSpi(String key) {
		return null;
	}

	@Override
	protected String[] keysSpi() throws BackingStoreException {
		return new String[0];
	}

	@Override
	protected void putSpi(String key, String value) {
		// do nothing
	}

	@Override
	protected void removeNodeSpi() throws BackingStoreException {
		// do nothing
	}

	@Override
	protected void removeSpi(String key) {
		// do nothing
	}

}	// class DummyPreferences
