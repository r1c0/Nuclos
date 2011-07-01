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
import java.util.Map;

public class TranslationVO implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static String labelsEntity[] = {"titel","menupath","treeview","treeviewdescription"};
	public static String labelsField[] = {"label","description"};
	
	Integer iLanguage;
	String sLanguage;
	String sCountry;
	Map<String, String> mpValues;

	public TranslationVO(Integer iLanguage, String country, String sLanguage, Map<String, String> lstLanguage) {
		super();
		this.iLanguage = iLanguage;
		this.sCountry = country;
		this.sLanguage = sLanguage;
		this.mpValues = lstLanguage;
	}
	
	public String getLanguage() {
		return sLanguage;
	}
	
	public void setLanguage(String sLanguage) {
		this.sLanguage = sLanguage;
	}
	
	public String getCountry() {
		return this.sCountry;
	}
	
	public Map<String, String> getLabels() {
		return mpValues;
	}
	
	public void setLabels(Map<String, String> lstLabels) {
		this.mpValues = lstLabels;
	}
	
	public Integer getLocaleId() {
		return iLanguage;
	}

}
