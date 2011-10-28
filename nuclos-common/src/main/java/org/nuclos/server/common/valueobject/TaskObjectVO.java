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
package org.nuclos.server.common.valueobject;

import java.util.Date;

/**
 * Value object representing a leased object related to a personal task.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */
public class TaskObjectVO extends NuclosValueObject {

	private Long iObjectId;
	private Long iTaskId;
	private String sIdentifier;
	private String sEntityName;

	public TaskObjectVO(Integer iId, Long iObjectId, Long iTaskId, String sEntityName, String sIdentifier,
			Date dateCreatedAt, String sCreatedBy, Date dateChangedAt, String sChangedBy, Integer iVersion) {
		super(iId, dateCreatedAt, sCreatedBy, dateChangedAt, sChangedBy, iVersion);
		this.iObjectId = iObjectId;
		this.iTaskId = iTaskId;
		this.sIdentifier = sIdentifier;
		this.sEntityName = sEntityName;
	}

	public String getIdentifier() {
		return this.sIdentifier;
	}

	public void setIdentifier(String sIdentifier) {
		this.sIdentifier = sIdentifier;
	}

	public Long getObjectId() {
		return this.iObjectId;
	}

	public void setObjectId(Long iObjectId) {
		this.iObjectId = iObjectId;
	}

	public Long getTaskId() {
		return this.iTaskId;
	}

	public void setTaskId(Long iTaskId) {
		this.iTaskId = iTaskId;
	}

	public String getEntityName() {
		return sEntityName;
	}

	public void setEntityName(String sEntityName) {
		this.sEntityName = sEntityName;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("id=").append(getId());
		result.append(",identifier=").append(getIdentifier());
		result.append(",gobjectId=").append(getObjectId());
		result.append(",entityName=").append(getEntityName());
		result.append(",taskId=").append(getTaskId());
		result.append("]");
		return result.toString();
	}

}	// class TaskObjectVO
