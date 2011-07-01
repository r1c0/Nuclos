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
package org.nuclos.common.dbtransfer;

import java.io.Serializable;

import org.nuclos.common2.LangUtils;

public class TransferNuclet implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
	private String label;
	
	public TransferNuclet(Long id, String label) {
		super();
		this.id = id;
		this.label = label;
	}	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public int hashCode() {
		return LangUtils.hashCode(label) ^ LangUtils.hashCode(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TransferNuclet)
			return LangUtils.equals(((TransferNuclet)obj).id, id);
		
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return getLabel();
	}
}
