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
/**
 *
 */
package org.nuclos.server.report.valueobject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;

/**
 *
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:thomas.schiffmann@novabit.de">thomas.schiffmann</a>
 * @version 01.00.00
 */
public class DatasourceParameterValuelistproviderVO implements Serializable {

	private String type;
	private Map<String, String> parameters;

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Map<String, String> getParameters() {
		return parameters;
	}
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public DatasourceParameterValuelistproviderVO(String type) {
		this.type = type;
		this.parameters = new HashMap<String, String>();
	}

	public void addParameter(String name, String value) {
		this.parameters.put(name, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DatasourceParameterValuelistproviderVO) {
			DatasourceParameterValuelistproviderVO other = (DatasourceParameterValuelistproviderVO) obj;
			return LangUtils.equals(type, other.type) && LangUtils.equals(parameters, other.parameters);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return LangUtils.hashCode(this.type) ^ LangUtils.hashCode(this.parameters);
	}

	@Override
	public String toString() {
		return StringUtils.emptyIfNull(this.type) + " (" + StringUtils.emptyIfNull(LangUtils.toString(this.parameters)) + ")";
	}
}
