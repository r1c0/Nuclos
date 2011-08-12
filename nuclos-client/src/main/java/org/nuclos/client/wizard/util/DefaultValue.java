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
package org.nuclos.client.wizard.util;

import org.apache.commons.lang.ObjectUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;

public class DefaultValue implements Comparable<DefaultValue> {

	Integer iId;
	String sValue;


	public DefaultValue(Integer iId, String value) {
		this.iId = iId;
		this.sValue = value;
	}

	public Integer getId() {
		return iId;
	}
	public String getValue() {
		return sValue;
	}

	@Override
	public String toString() {
		return StringUtils.emptyIfNull(this.sValue);
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof DefaultValue))
			return false;
		DefaultValue that = (DefaultValue)obj;
		Integer i1 = this.iId;
		Integer i2 = that.iId;
		String s1 = this.sValue;
		String s2 = that.sValue;
		return ObjectUtils.equals(i1, i2) && ObjectUtils.equals(s1, s2);
	}

	@Override
	public int hashCode() {
		String str = this.iId + sValue;
		return str.hashCode();
	}

	@Override
	public int compareTo(DefaultValue o) {
		return LangUtils.compare(this.getValue(), o.getValue());
	}
}
