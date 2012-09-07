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

import org.nuclos.common.dal.vo.AbstractDalVOWithVersion;
import org.nuclos.common2.LangUtils;

public class EntityLafParameterVO extends AbstractDalVOWithVersion {

	/**
	 * 
	 */
	private static final long serialVersionUID = -86095740980627807L;
	
	private Long entity;
	private String parameter;
	private String value;
	
	public EntityLafParameterVO() {
		super();
	}
	
	public Long getEntity() {
		return entity;
	}

	public void setEntity(Long entity) {
		this.entity = entity;
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		if (getId() == null)
			return 0;
		return getId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EntityLafParameterVO) {
			EntityLafParameterVO other = (EntityLafParameterVO) obj;
			return LangUtils.equals(getId(), other.getId());
		}
		return super.equals(obj);
	}
	
	
	
}
