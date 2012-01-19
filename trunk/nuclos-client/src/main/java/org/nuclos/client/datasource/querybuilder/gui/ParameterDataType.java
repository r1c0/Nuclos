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
package org.nuclos.client.datasource.querybuilder.gui;

/**
 * @todo enter class description.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris@novabit.de">Boris</a>
 * @version 01.00.00
 */
public class ParameterDataType {
	String sLabel;
	String sDataType;

	public ParameterDataType(String sDataType, String sLabel) {
		this.sDataType = sDataType;
		this.sLabel = sLabel;
	}

	public String getDataType() {
		return sDataType;
	}

	public void setDataType(String sDataType) {
		this.sDataType = sDataType;
	}

	public String getLabel() {
		return sLabel;
	}

	public void setLabel(String sLabel) {
		this.sLabel = sLabel;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ParameterDataType)) {
			return false;
		}
		final ParameterDataType parameterDataType = (ParameterDataType) o;
		if (!sDataType.equals(parameterDataType.sDataType)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int result;
		result = sLabel.hashCode();
		result = 29 * result + sDataType.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return sLabel;
	}
}
