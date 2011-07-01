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
package org.nuclos.client.wizard.model;

import java.util.Date;


public class ValueList {
	
	Long id;
	Integer iVersion;
	Long internalId;
	String label;
	String description;
	String mnemonic;
	Date validFrom;
	Date validUntil;
	
	public ValueList() {
		label = new String();
		description = new String();
		mnemonic = new String();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
		this.internalId = id;
	}
	
	public Integer getVersionId() {
		return iVersion;
	}

	public void setVersionId(Integer id) {
		this.iVersion = id;
	}
	
	public Long getInternalId() {
		return this.internalId;
	}
	
	public void setInternalId(Long id) {
		this.internalId = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMnemonic() {
    	return mnemonic;
    }

	public void setMnemonic(String mnemonic) {
    	this.mnemonic = mnemonic;
    }

	public Date getValidFrom() {
    	return validFrom;
    }

	public void setValidFrom(Date validForm) {
    	this.validFrom = validForm;
    }

	public Date getValidUntil() {
    	return validUntil;
    }

	public void setValidUntil(Date validUntil) {
    	this.validUntil = validUntil;
    }


}
