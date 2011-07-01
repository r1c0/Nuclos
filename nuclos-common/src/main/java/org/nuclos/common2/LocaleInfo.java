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
package org.nuclos.common2;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Locale;


public class LocaleInfo implements Serializable {
	
	/** The standardized (IETF) "i-default" language tag which represents our null locale */ 
	public static LocaleInfo I_DEFAULT = new LocaleInfo(null, null, "i-default", null);
	public static final String I_DEFAULT_TAG = "i-default";
	
	public static Comparator<LocaleInfo> DESCRIPTION_COMPARATOR = DescriptionComparator.INSTANCE;

	
	public final String title;
	public final Integer localeId;
	public final String language;
	public final String country;

	public LocaleInfo(String title, Integer localeId, String language, String country) {
		this.language = language;
		this.country  = country;
		this.localeId = localeId;
		this.title = title;
	}
	
	public String getTag() {
		if (language == null) {
			return I_DEFAULT_TAG;
		} else if (country == null) {
			return language;
		} else {
			return language + "-" + country;
		}
	}	
	
	public Locale toLocale() {
		if (language == null) {
			return null;
		} else {
			return new Locale(language, country != null ? country : "");
		}
	}
	
	@Override
	public int hashCode() {
		return LangUtils.hashCode(language) ^ LangUtils.hashCode(country);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LocaleInfo) {
			LocaleInfo that = (LocaleInfo) obj;
			return LangUtils.equals(language, that.language) && LangUtils.equals(country, that.country);
		}
		return false;
	}

	@Override
	public String toString() {
		return (title != null) ? title : getTag();
	}
	
	public static LocaleInfo parseTag(String tag) {
		if (tag == null || tag.isEmpty())
			return LocaleInfo.I_DEFAULT;
		int i = tag.indexOf('-', 2);
		if (i == -1) {
			return new LocaleInfo(null, null, tag, null);
		} else {
			return new LocaleInfo(null, null, tag.substring(0, i), tag.substring(i+1));
		}
	}
	
	public static String getStandardParentTag(String tag) {
		LocaleInfo li = LocaleInfo.parseTag(tag);
		if (li != null && li.country != null) {
   		return li.language;
   	} else {
   		return null;
   	}
	}

	private static enum DescriptionComparator implements Comparator<LocaleInfo> {
		INSTANCE;

		@Override
		public int compare(LocaleInfo o1, LocaleInfo o2) {
			return o1.toString().compareTo(o2.toString());
		}
	}
}
