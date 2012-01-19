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
package org.nuclos.common;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.ResourceBundle;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.transport.GzipMap;

public class HashResourceBundle extends ResourceBundle implements Serializable {
	private static final long serialVersionUID = -2336811374855686329L;
	
	private final GzipMap<String, String> map;
	
	public HashResourceBundle() {
		this.map = new GzipMap<String, String>();
	}

	@Override
	public Enumeration<String> getKeys() {
		return CollectionUtils.asEnumeration(map.keySet());
	}

	@Override
	protected Object handleGetObject(String key) {
		return map.get(key);
	}
	
	public void putProperty(String key, String value) {
		map.put(key, value);
	}
}
