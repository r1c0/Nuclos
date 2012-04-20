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
package org.nuclos.common.tasklist;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TasklistDefinition implements Serializable {

	private final Integer id;
	private String name;
	private String description;
	private String labelResourceId;
	private String descriptionResourceId;
	private String menupathResourceId;
	private Integer dynamicTasklistId;
	private String taskEntity;
	private String dynamicTasklistIdFieldname;
	private String dynamicTasklistEntityFieldname;

	public TasklistDefinition(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLabelResourceId() {
		return labelResourceId;
	}

	public void setLabelResourceId(String labelResourceId) {
		this.labelResourceId = labelResourceId;
	}

	public String getDescriptionResourceId() {
		return descriptionResourceId;
	}

	public void setDescriptionResourceId(String descriptionResourceId) {
		this.descriptionResourceId = descriptionResourceId;
	}

	public String getMenupathResourceId() {
		return menupathResourceId;
	}

	public void setMenupathResourceId(String menupathResourceId) {
		this.menupathResourceId = menupathResourceId;
	}

	public Integer getDynamicTasklistId() {
		return dynamicTasklistId;
	}

	public void setDynamicTasklistId(Integer dynamicTasklistId) {
		this.dynamicTasklistId = dynamicTasklistId;
	}

	public String getTaskEntity() {
		return taskEntity;
	}

	public void setTaskEntity(String taskEntity) {
		this.taskEntity = taskEntity;
	}

	public String getDynamicTasklistIdFieldname() {
		return dynamicTasklistIdFieldname;
	}

	public void setDynamicTasklistIdFieldname(String dynamicTasklistIdFieldname) {
		this.dynamicTasklistIdFieldname = dynamicTasklistIdFieldname;
	}

	public String getDynamicTasklistEntityFieldname() {
		return dynamicTasklistEntityFieldname;
	}

	public void setDynamicTasklistEntityFieldname(String dynamicTasklistEntityFieldname) {
		this.dynamicTasklistEntityFieldname = dynamicTasklistEntityFieldname;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TasklistDefinition other = (TasklistDefinition) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		}
		else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TasklistDefinition [id=" + id + ", name=" + name + ", description=" + description + ", labelResourceId=" + labelResourceId + ", descriptionResourceId=" + descriptionResourceId
				+ ", menupathResourceId=" + menupathResourceId + ", dynamicTasklistId=" + dynamicTasklistId + ", dynamicTasklistIdFieldname=" + dynamicTasklistIdFieldname
				+ ", dynamicTasklistEntityFieldname=" + dynamicTasklistEntityFieldname + "]";
	}
}
