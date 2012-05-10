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
package org.nuclos.server.report;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ReportFieldDefinition implements Serializable {

	private String name;
	private Class<?> clazz;
	private int maxlength;
	private String label;
	private String outputformat;

	public ReportFieldDefinition(String name, Class<?> clazz, String label) {
		super();
		this.name = name;
		this.clazz = clazz;
		this.label = label;
	}

	public String getName() {
		return name;
	}

	public Class<?> getJavaClass() {
		return clazz;
	}

	public void setMaxLength(int i) {
		this.maxlength = i;
	}
	
	public int getMaxLength() {
		return maxlength;
	}

	public String getLabel() {
		return label;
	}
	
	public String getOutputformat() {
		return outputformat;
	}

	public void setOutputformat(String outputformat) {
		this.outputformat = outputformat;
	}

	@Override
	public String toString() {
		return "FieldDefinition [name=" + name + ", clazz=" + clazz
			+ ", maxlength=" + maxlength + "]";
	}
}
