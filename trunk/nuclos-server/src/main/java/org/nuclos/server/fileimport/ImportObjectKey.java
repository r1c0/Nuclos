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
package org.nuclos.server.fileimport;

import java.util.Map;

public class ImportObjectKey {

	private final Map<String, Object> keyMap;

	public ImportObjectKey(Map<String, Object> keyMap) {
		this.keyMap = keyMap;
		// ignore case for key string attributes
		for (Map.Entry<String, Object> entry : this.keyMap.entrySet()) {
			if (entry.getValue() != null && entry.getValue() instanceof String) {
				entry.setValue(((String)entry.getValue()).toUpperCase());
			}
		}
	}

	public int getSize() {
		return keyMap.size();
	}

	public Object get(String key) {
		return keyMap.get(key);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ImportObjectKey) {
			return keyMap.equals(((ImportObjectKey)obj).keyMap);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return keyMap.hashCode();
	}

	@Override
	public String toString() {
		return keyMap.toString();
	}
}
