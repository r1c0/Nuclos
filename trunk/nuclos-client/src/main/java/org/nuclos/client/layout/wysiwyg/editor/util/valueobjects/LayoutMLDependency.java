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
//package org.nuclos.client.layout.wysiwyg.editor.util.valueobjects;
//
//
//public class LayoutMLDependency {
//	/**
//	 * <!ELEMENT property EMPTY> <!ATTLIST property name CDATA #REQUIRED value
//	 * CDATA #REQUIRED >
//	 */
//
//	private String dependentField = null;
//	private String dependsOnField = null;
//
//	public LayoutMLDependency() {
//		this.dependentField = "";
//		this.dependsOnField = "";
//	}
//
//	public LayoutMLDependency(String dependentField, String dependsOnField) {
//		this.dependentField = dependentField;
//		this.dependsOnField = dependsOnField;
//	}
//	
//	public String getDependendField() {
//		return dependentField;
//	}
//
//	public void setDependendField(String dependendField) {
//		this.dependentField = dependendField;
//	}
//
//	public String getDependsOnField() {
//		return dependsOnField;
//	}
//
//	public void setDependsOnField(String dependsOnField) {
//		this.dependsOnField = dependsOnField;
//	}
//
//	@Override
//	public String toString() {
//		StringBuffer fubber = new StringBuffer();
//		
//		fubber.append("<dependency dependant-field=\"" + this.dependentField + "\" depends-on=\"" + dependsOnField + "\"/>");
//		fubber.append("\n");
//		
//		return fubber.toString();
//	}
//}
