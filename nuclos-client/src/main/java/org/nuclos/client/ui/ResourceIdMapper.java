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

package org.nuclos.client.ui;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;
import org.jdesktop.swingx.renderer.StringValue;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.Localizable;
import org.nuclos.common2.StringUtils;

/**
 * A helper class for use with SwingX renderers and/or auto-completion.
 */
public class ResourceIdMapper<V> extends ObjectToStringConverter implements StringValue, Comparator<V> {

	private final Map<Object, String> translations;

	public ResourceIdMapper(Map<V, String> map) {
		this.translations = new HashMap<Object, String>();
		for (Map.Entry<?, String> e : map.entrySet()) {
			String text = SpringLocaleDelegate.getInstance().getTextFallback(e.getValue(), null);
			if (text != null)
				translations.put(e.getKey(), text);
		}
	}

	public ResourceIdMapper(Collection<V> values) {
		this.translations = new HashMap<Object, String>();
		for (V value : values) {
			if (value instanceof Localizable) {
				Localizable localizable = (Localizable) value;
				String text = SpringLocaleDelegate.getInstance().getTextFallback(localizable.getResourceId(), null);
				if (text != null)
					translations.put(value, text);
			}
		}
	}

	public ResourceIdMapper(V[] values) {
		this(Arrays.asList(values));
	}

	@Override
	public String getPreferredStringForItem(Object /* should be V */ item) {
		String text = translations.get(item);
		if (text != null)
			return text;
		return (item != null) ? String.valueOf(item) : null;
	}

	@Override
	public String getString(Object /* should be V */ value) {
		String text = getPreferredStringForItem(value);
		// According to the SwingX documentation, this method must not return null
		return (text != null) ? text : "";
	}

	@Override
	public int compare(V o1, V o2) {
		return StringUtils.compare(getString(o1), getString(o2));
	}
}
