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
package org.nuclos.client.layout.wysiwyg.component;

import java.util.HashMap;
import java.util.Map;

import org.nuclos.common.NuclosTranslationMap;

public class TranslationMap extends HashMap<String, String> implements NuclosTranslationMap {

	public TranslationMap() {
	}

	public TranslationMap(Map<String, String> map) {
		super(map);
	}
	
	@Override
	public String put(String key, String value) {
		return super.put(key, value);
	}

	public void merge(Map<String, String> map) {
		for (Map.Entry<String, String> e : map.entrySet()) {
			String tag = e.getKey();
			String translation = e.getValue();
			if (translation != null) {
				put(tag, translation);
			} else {
				remove(tag);
			}
		}
	}

	@Override
	public String getTranslation(String language) {
		return super.get(language);
	}

	@Override
	public void putTranslation(String language, String translation) {
		super.put(language, translation);
	}
}
