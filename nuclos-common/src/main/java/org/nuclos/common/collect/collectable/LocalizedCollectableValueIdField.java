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
package org.nuclos.common.collect.collectable;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.Localizable;

/**
 * A localizable CollectableValueField which contains a value and additionally a useful
 * representation. 
 */
public class LocalizedCollectableValueIdField extends CollectableValueIdField implements Serializable {

	public static LocalizedCollectableValueIdField fromResourceId(Integer oValueId, Object oValue, String resId) {
		final String text = SpringLocaleDelegate.getInstance().getTextFallback(resId, "<[" + oValue + "]>");
		return new LocalizedCollectableValueIdField(oValueId, oValue, text);		
	}
	
	public static LocalizedCollectableValueIdField fromLocalizable(Integer oValueId, Object oValue, Localizable loc) {
		final String text = SpringLocaleDelegate.getInstance().getText(loc);
		return new LocalizedCollectableValueIdField(oValueId, oValue, text);		
	}
	
	private String label;
	
	public LocalizedCollectableValueIdField(Integer oValueId, Object oValue, String label) {
		super(oValueId, oValue);
		this.label = label;
	}
	
	public String toLocalizedString() {
		return label;
	}
	
	@Override
	public String toString() {
		return toLocalizedString();
	}

	@Override
	public String toDescription() {
		final ToStringBuilder b = new ToStringBuilder(this).append(label).append(oValueId).append(oValue);
		return b.toString();
	}

	@Override
	public int compareTo(CollectableField that) {
		final boolean thatIsLocalized = (that instanceof LocalizedCollectableValueIdField);
		if (thatIsLocalized) {
			final String thatLabel = ((LocalizedCollectableValueIdField) that).toLocalizedString();
			if (this.label != null && thatLabel != null) {
				return this.label.compareToIgnoreCase(thatLabel);
			} else {
				return LangUtils.compare(this.label, thatLabel);
			}
		} else
			return super.compareTo(that);
	}
	
	
}
