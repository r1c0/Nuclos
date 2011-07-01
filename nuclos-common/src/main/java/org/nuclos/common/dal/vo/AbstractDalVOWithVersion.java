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
package org.nuclos.common.dal.vo;

import org.nuclos.common2.InternalTimestamp;

public abstract class AbstractDalVOWithVersion extends AbstractDalVOBasic {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private InternalTimestamp createdAt;
	private String createdBy;
	private InternalTimestamp changedAt;
	private String changedBy;
	private Integer version;
	
	public AbstractDalVOWithVersion() {
		super();
	}
	
	public AbstractDalVOWithVersion(AbstractDalVOWithVersion dalVO) {
		super(dalVO);
		this.setChangedAt(dalVO.getChangedAt());
		this.setChangedBy(dalVO.getChangedBy());
		this.setCreatedAt(dalVO.getCreatedAt());
		this.setCreatedBy(dalVO.getCreatedBy());
		this.setVersion(dalVO.getVersion());
	}
	
	public InternalTimestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(InternalTimestamp createdAt) {
		this.createdAt = createdAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public InternalTimestamp getChangedAt() {
		return changedAt;
	}

	public void setChangedAt(InternalTimestamp changedAt) {
		this.changedAt = changedAt;
	}

	public String getChangedBy() {
		return changedBy;
	}

	public void setChangedBy(String changedBy) {
		this.changedBy = changedBy;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	
}
