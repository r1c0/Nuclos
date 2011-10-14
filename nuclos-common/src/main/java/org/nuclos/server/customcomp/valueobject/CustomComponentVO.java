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

package org.nuclos.server.customcomp.valueobject;

import org.nuclos.server.common.valueobject.NuclosValueObject;

public class CustomComponentVO extends NuclosValueObject implements Cloneable {

	private static final long serialVersionUID = 1L;

	String internalName;

	String labelResourceId;
	String menupathResourceId;

	String componentType;
	String componentVersion;
	byte[] data;
	
	Long nucletId;

	public CustomComponentVO() {
	}

	public CustomComponentVO(NuclosValueObject vo) {
		super(vo);
	}

	public String getInternalName() {
		return internalName;
	}

	public void setInternalName(String internalName) {
		this.internalName = internalName;
	}

	public String getLabelResourceId() {
		return labelResourceId != null ? labelResourceId : internalName;
	}

	public void setLabelResourceId(String label) {
		this.labelResourceId = label;
	}

	public void setMenupathResourceId(String menuPath) {
		this.menupathResourceId = menuPath;
	}

	public String getMenupathResourceId() {
		return menupathResourceId;
	}

	public String getComponentType() {
		return componentType;
	}

	public void setComponentType(String componentType) {
		this.componentType = componentType;
	}

	public String getComponentVersion() {
		return componentVersion;
	}

	public void setComponentVersion(String componentVersion) {
		this.componentVersion = componentVersion;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}

	public Long getNucletId() {
		return nucletId;
	}

	public void setNucletId(Long nucletId) {
		this.nucletId = nucletId;
	}

	@Override
	public String toString() {
		return internalName;
	}

	@Override
	public CustomComponentVO clone() {
		CustomComponentVO clone = (CustomComponentVO) super.clone();
		if (data != null)
			clone.data = data.clone();
		return clone;
	}
}
