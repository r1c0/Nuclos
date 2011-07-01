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

package org.nuclos.common.preferences;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * A {@link Preferences} implementation which provides read-only access to
 * a given (exported) preferences XML structure.
 * Useful for evaluating preferences fragments on the server-side.
 */
public class ReadOnlyPreferences extends AbstractMapBasedPreferences.Root {

	public ReadOnlyPreferences(InputStream is) throws IOException {
		this(PreferencesConverter.loadPreferences(is));
	}
	
	public ReadOnlyPreferences(String xml, String encoding) throws IOException {
		this(new ByteArrayInputStream(xml.getBytes(encoding)));
	}
	
	public ReadOnlyPreferences(Map<String, Map<String, String>> prefMap) {
		for (Map.Entry<String, Map<String, String>> nodeAndValues : prefMap.entrySet()) {
			Preferences node = node(nodeAndValues.getKey());
			Map<String, String> values = nodeAndValues.getValue();
			for (Map.Entry<String, String> e : values.entrySet()) {
				node.put(e.getKey(), e.getValue());
			}
		}
	}
	
	@Override
	protected void flushSpi() throws BackingStoreException {
		throw new BackingStoreException("Preferences are read-only");
	}
}
