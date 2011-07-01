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

	private Integer iGenericObjectId;
	private Integer iTaskId;
	private String sIdentifier;
	private Integer iModuleId;
	private String sEntityName;

	public TaskObjectVO(Integer iId, Integer iGenericObjectId, Integer iTaskId, Integer iModuleId, String sEntityName, String sIdentifier,
			Date dateCreatedAt, String sCreatedBy, Date dateChangedAt, String sChangedBy, Integer iVersion) {
		super(iId, dateCreatedAt, sCreatedBy, dateChangedAt, sChangedBy, iVersion);
		this.iGenericObjectId = iGenericObjectId;
		this.iTaskId = iTaskId;
		this.iModuleId = iModuleId;
		this.sIdentifier = sIdentifier;
		this.sEntityName = sEntityName;
	}

	public String getIdentifier() {
		return this.sIdentifier;
	}

	public void setIdentifier(String sIdentifier) {
		this.sIdentifier = sIdentifier;
	}

	public Integer getGenericObjectId() {
		return this.iGenericObjectId;
	}

	public void setGenericObjectId(Integer iGenericObjectId) {
		this.iGenericObjectId = iGenericObjectId;
	}

	public Integer getTaskId() {
		return this.iTaskId;
	}

	public void setTaskId(Integer iTaskId) {
		this.iTaskId = iTaskId;
	}

	public Integer getModuleId() {
		return this.iModuleId;
	}

	public void setModuleId(Integer iModuleId) {
		this.iModuleId = iModuleId;
	}

	public String getEntityName() {
		return sEntityName;
	}

	public void setEntityName(String sEntityName) {
		this.sEntityName = sEntityName;
	}
	
}	// class TaskObjectVO
