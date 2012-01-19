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

/**
 * NUCLEUSINT-1142
 *
 */
public class NuclosPassword implements Serializable, NuclosAttributeExternalValue {

	private String value;
	
	public NuclosPassword() {
		this("");
	}

	public NuclosPassword(String value) {
	    this.value = value;
    }

	public String getValue() {
    	return value;
    }

	public void setValue(String value) {
    	this.value = value;
    }

	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((value == null) ? 0 : value.hashCode());
	    return result;
    }

	@Override
    public boolean equals(Object obj) {
	    if(this == obj)
		    return true;
	    if(obj == null)
		    return false;
	    if(getClass() != obj.getClass())
		    return false;
	    NuclosPassword other = (NuclosPassword) obj;
	    if(value == null) {
		    if(other.value != null)
			    return false;
	    }
	    else if(!value.equals(other.value))
		    return false;
	    return true;
    }

	@Override
    public String toString() {
	    return "NuclosPassword [" + value + "]";
    }

	
// Anm. Base64 ist ein Message-Digest-Format zur Übertragung von Binärdaten über ASCII.
// Es hat nicht das geringste mit einer Verschlüsselung zu tun.
}
