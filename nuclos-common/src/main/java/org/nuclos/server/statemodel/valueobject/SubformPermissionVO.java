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
package org.nuclos.server.statemodel.valueobject;

import java.util.HashSet;
import java.util.Set;

import org.nuclos.server.common.valueobject.NuclosValueObject;

/**
 * Value object representing a subform permission.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version	01.00.00
 */
public class SubformPermissionVO extends NuclosValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String subform;
	private Integer roleId;
	private String role;
	private Integer stateId;
	private String state;
	private boolean writeable;
	
	private Set<SubformColumnPermissionVO> columnPermissions = new HashSet<SubformColumnPermissionVO>();

	/**
	 * constructor
	 * @param subform name of the subform entity
	 * @param roleId id of the role entity
	 * @param role name of the role entity
	 * @param stateId id of state entity
	 * @param state name of the state entity
	 * @param writeable is attribute group writable?
	 */
	public SubformPermissionVO(String subform, Integer roleId, String role,
			Integer stateId, String state, boolean writeable, Set<SubformColumnPermissionVO> columnPermissions) {
		super();
		this.subform = subform;
		this.roleId = roleId;
		this.role = role;
		this.stateId = stateId;
		this.state = state;
		this.writeable = writeable;
		setColumnPermissions(columnPermissions);
	}

	/**
	 * constructor
	 * @param evo contains the common fields
	 * @param subformId id of the subform entity
	 * @param subform name of the subform entity
	 * @param roleId id of the role entity
	 * @param role name of the role entity
	 * @param stateId id of state entity
	 * @param state name of the state entity
	 * @param writeable is attribute group writable?
	 */
	public SubformPermissionVO(NuclosValueObject evo, String subform, Integer roleId, String role,
			Integer stateId, String state, boolean writeable, Set<SubformColumnPermissionVO> columnPermissions) {
		super(evo);
		this.subform = subform;
		this.roleId = roleId;
		this.role = role;
		this.stateId = stateId;
		this.state = state;
		this.writeable = writeable;
		setColumnPermissions(columnPermissions);
	}
	
	/**
     * @return the columnPermissions
     */
    public Set<SubformColumnPermissionVO> getColumnPermissions() {
    	return columnPermissions;
    }

	/**
     * @param columnPermissions the columnPermissions to set
     */
    public void setColumnPermissions(
        Set<SubformColumnPermissionVO> columnPermissions) {
    	this.columnPermissions.clear();
    	if (columnPermissions != null) {
    		this.columnPermissions.addAll(columnPermissions);
        	setIdInColumnPermissions();
    	}
    }
    
    private void setIdInColumnPermissions() {
    	for (SubformColumnPermissionVO scp : columnPermissions) {
    		scp.setRoleSubformId(getId());
    	}
    }

	/**
	 * get subform entity name
	 * @return subform entity name
	 */
	public String getSubform() {
		return subform;
	}

	/**
	 * set subform entity name
	 * @param subform subform entity name
	 */
	public void setSubform(String subform) {
		this.subform = subform;
	}

	/**
	 * get role entity id
	 * @return role entity id
	 */
	public Integer getRoleId() {
		return roleId;
	}

	/**
	 * set role entity id
	 * @param role entity id
	 */
	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	/**
	 * get role entity name
	 * @return role entity name
	 */
	public String getRole() {
		return role;
	}

	/**
	 * set role entity name
	 * @param role entity name
	 */
	public void setRole(String role) {
		this.role = role;
	}

	/**
	 * get state entity id
	 * @return state entity id
	 */
	public Integer getStateId() {
		return stateId;
	}

	/**
	 * set state entity id
	 * @param state entity id
	 */
	public void setStateId(Integer stateId) {
		this.stateId = stateId;
	}

	/**
	 * get state entity name
	 * @return state entity name
	 */
	public String getState() {
		return state;
	}

	/**
	 * set state entity name
	 * @param state entity name
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * get writeable flag
	 * @return writeable flag
	 */
	public boolean isWriteable() {
		return writeable;
	}

	/**
	 * set writable flag
	 * @param bWriteable writeable flag
	 */
	public void setWriteable(boolean writeable) {
		this.writeable = writeable;
	}
}

