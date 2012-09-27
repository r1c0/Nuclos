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
import org.nuclos.common2.XStreamSupport;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class WorkspaceVO extends AbstractDalVOWithVersion {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8609574098062721807L;
	
	private Long user;
	private String name;
	private Long assignedWorkspace;
	
	private WorkspaceDescription workspaceDescription;
	
	public WorkspaceVO() {
		super();
		this.workspaceDescription = new WorkspaceDescription();
	}
	
	public WorkspaceVO(WorkspaceDescription wd) {
		super();
		this.workspaceDescription = wd;
	}

	public Long getUser() {
		return user;
	}

	public void setUser(Long user) {
		this.user = user;
	}

	public String getClbworkspace() {
		final XStream xstream = XStreamSupport.getInstance().getXStreamUtf8();
		return xstream.toXML(workspaceDescription);
	}

	public void setClbworkspace(String clbworkspace) {
		final XStream xstream = XStreamSupport.getInstance().getXStreamUtf8();
		this.workspaceDescription = (WorkspaceDescription) xstream.fromXML(clbworkspace);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		this.workspaceDescription.setName(name);
	}

	public Long getAssignedWorkspace() {
		return assignedWorkspace;
	}

	public void setAssignedWorkspace(Long assignedWorkspace) {
		this.assignedWorkspace = assignedWorkspace;
	}
	
	public boolean isAssigned() {
		return this.assignedWorkspace != null;
	}

	public WorkspaceDescription getWoDesc() {
		return workspaceDescription;
	}

	public void setWoDesc(WorkspaceDescription wd) {
		this.workspaceDescription = wd;
	}
	
	public void importHeader(WorkspaceDescription importWD) {
		setName(importWD.getName());
		this.workspaceDescription.importHeader(importWD);
	}

	@Override
	public int hashCode() {
		if (getId() == null)
			return 0;
		return getId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof WorkspaceVO) {
			WorkspaceVO other = (WorkspaceVO) obj;
			return LangUtils.equals(getId(), other.getId());
		}
		return super.equals(obj);
	}
	
	
	
}
