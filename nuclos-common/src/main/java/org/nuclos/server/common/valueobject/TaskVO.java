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

import java.util.Collection;
import java.util.Collections;

import org.nuclos.common2.exception.CommonValidationException;

/**
 * Value object representing a personal task.
 */
public class TaskVO extends NuclosValueObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String sName;
	private Integer iVisibility;
	private Integer iPriority;
	private java.util.Date dScheduled;
	private java.util.Date dCompleted;
	private Integer iDelegatorId;
	private String sDelegator;
	private Integer iStatusId;
	private String sStatus;
	private String sDescription;
	private String sComment;
	private String sAssignees;
	private Collection<TaskObjectVO> colRelatedObjects;

	public static enum TaskVisibility {
		PRIVATE(1),
		PUBLIC(2);

		private final Integer value;

		TaskVisibility(Integer iValue) {
			this.value = iValue;
		}

		public Integer getValue() {
			return this.value;
		}
	}

	/**
	 * constructor to be called by server only
	 */
	public TaskVO(Integer iId, String sTask, Integer iVisibility, Integer iPriority, java.util.Date dScheduled, java.util.Date dCompleted, Integer iDelegatorId, String sDelegator, Integer iStatusId, String sStatus, String sDescription, String sComment, Collection<TaskObjectVO> colRelatedObjects, java.util.Date dCreated, String sCreated, java.util.Date dChanged, String sChanged, Integer iVersion) {
		super(iId, dCreated, sCreated, dChanged, sChanged, iVersion);
		this.sName = sTask;
		this.iVisibility = iVisibility;
		this.iPriority = iPriority;
		this.dScheduled = dScheduled;
		this.dCompleted = dCompleted;
		this.iDelegatorId = iDelegatorId;
		this.sDelegator = sDelegator;
		this.iStatusId = iStatusId;
		this.sStatus = sStatus;
		this.sDescription = sDescription;
		this.sComment = sComment;
		this.sAssignees = null;
		this.colRelatedObjects = colRelatedObjects;
	}

	/**
	 * constructor to be called by client only
	 */
	public TaskVO(String sTask, Integer iVisibility, Integer iPriority, java.util.Date dScheduled, java.util.Date dCompleted, Integer iDelegatorId, String sDelegator, Integer iStatusId, String sStatus, String sDescription, String sComment, Collection<TaskObjectVO> colRelatedObjects) {
		super();
		this.sName = sTask;
		this.iVisibility = iVisibility;
		this.iPriority = iPriority;
		this.dScheduled = dScheduled;
		this.dCompleted = dCompleted;
		this.iDelegatorId = iDelegatorId;
		this.sDelegator = sDelegator;
		this.iStatusId = iStatusId;
		this.sStatus = sStatus;
		this.sDescription = sDescription;
		this.sComment = sComment;
		this.sAssignees = null;
		this.colRelatedObjects = colRelatedObjects;
	}

	/**
	 * default constructor to be called by client only
	 */
	public TaskVO() {
		super();
		this.sName = null;
		this.iVisibility = null;
		this.iPriority = null;
		this.dScheduled = null;
		this.dCompleted = null;
		this.iDelegatorId = null;
		this.sDelegator = null;
		this.iStatusId = null;
		this.sStatus = null;
		this.sDescription = null;
		this.sComment = null;
		this.sAssignees = null;
		this.colRelatedObjects = Collections.emptySet();
	}

	public TaskVO cloneTaskVO(){
		TaskVO taskVO = new TaskVO();
		taskVO.setName(this.getName());
		taskVO.setVisibility(this.getVisibility());
		taskVO.setPriority(this.getPriority());
		taskVO.setScheduled(this.getScheduled());
		taskVO.setCompleted(this.getCompleted());
		taskVO.setDelegatorId(this.getDelegatorId());
		taskVO.setDelegator(this.getDelegator());
		taskVO.setTaskstatusId(this.getTaskstatusId());
		taskVO.setTaskstatus(this.getTaskstatus());
		taskVO.setDescription(this.getDescription());
		taskVO.setComment(this.getComment());
		taskVO.setAssignees(this.getAssignees());
		taskVO.setRelatedObjects(this.getRelatedObjects());
		return taskVO;
	}

	public String getName() {
		return sName;
	}

	public void setName(String sTask) {
		this.sName = sTask;
	}

	public Integer getPriority() {
		return iPriority;
	}

	public void setPriority(Integer iPriority) {
		this.iPriority = iPriority;
	}

	public Integer getVisibility() {
		return iVisibility;
	}

	public void setVisibility(Integer iVisibility) {
		this.iVisibility = iVisibility;
	}

	public java.util.Date getScheduled() {
		return dScheduled;
	}

	public void setScheduled(java.util.Date dScheduled) {
		this.dScheduled = dScheduled;
	}

	public java.util.Date getCompleted() {
		return dCompleted;
	}

	public void setCompleted(java.util.Date dCompleted) {
		this.dCompleted = dCompleted;
	}

	public Integer getDelegatorId() {
		return iDelegatorId;
	}

	public void setDelegatorId(Integer iDelegatorId) {
		this.iDelegatorId = iDelegatorId;
	}

	public String getDelegator() {
		return sDelegator;
	}

	public void setDelegator(String sDelegator) {
		this.sDelegator = sDelegator;
	}

	public Integer getTaskstatusId() {
		return iStatusId;
	}

	public void setTaskstatusId(Integer iStatusId) {
		this.iStatusId = iStatusId;
	}

	public String getTaskstatus() {
		return sStatus;
	}

	public void setTaskstatus(String sStatus) {
		this.sStatus = sStatus;
	}

	public String getDescription() {
		return sDescription;
	}

	public void setDescription(String sDescription) {
		this.sDescription = sDescription;
	}

	public String getComment() {
		return sComment;
	}

	public void setComment(String sComment) {
		this.sComment = sComment;
	}

	public String getAssignees() {
		return sAssignees;
	}

	public void setAssignees(String sAssignees) {
		this.sAssignees = sAssignees;
	}

	public Collection<TaskObjectVO> getRelatedObjects() {
		return colRelatedObjects;
	}

	public void setRelatedObjects(Collection<TaskObjectVO> collTaskObjects) {
		this.colRelatedObjects = collTaskObjects;
	}

	/* @todo refactor to entities */
	public void addRelatedObject(Integer iGenericObjectId) {
		this.colRelatedObjects.add(new TaskObjectVO(null, iGenericObjectId, this.getId(), null, null, null, null, null, null, null, null));
	}

	/* @todo refactor to entities */
	public void addRelatedObject(Integer iMasterDataId, String sEntityName) {
		this.colRelatedObjects.add(new TaskObjectVO(null, iMasterDataId, this.getId(), null, sEntityName, null, null, null, null, null, null));
	}

	/* @todo delete this */
	public String getDelegatorDisplayName() {
		return getDelegator();
	}

	/* @todo delete this */
	public String getOwnerDisplayName() {
		return getAssignees();
	}

	/* @todo delete this */
	public String getIdentifier() {
		StringBuilder sb = new StringBuilder();
		for (TaskObjectVO tovo : colRelatedObjects) {
			sb.append(tovo.getIdentifier()).append(", ");
		}
		String result = sb.toString();
		if (result.endsWith(", ")) {
			result = result.substring(0, result.length() - 2);
		}
		return result;
	}

	public boolean hasRelatedObjects() {
		return !this.colRelatedObjects.isEmpty();
	}

	@Override
	public void validate() throws CommonValidationException {
		if ((this.getName() == null) || (this.getName().equals(""))) {
			throw new CommonValidationException("task.error.validation.task");
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof TaskVO) {
			final TaskVO that = (TaskVO) o;
			return this.getId().equals(that.getId());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (this.getId() != null ? this.getId().hashCode() : 0);
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("id=").append(getId());
		result.append(",name=").append(getName());
		result.append(",identifier=").append(getIdentifier());
		result.append(",prio=").append(getPriority());
		result.append(",status=").append(getTaskstatus());
		result.append("]");
		return result.toString();
	}

}
