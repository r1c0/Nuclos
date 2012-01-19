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

import org.nuclos.server.common.valueobject.NuclosValueObject;

/**
 * Value object representing a attribute group permission.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 01.00.00
 */
public class AttributegroupPermissionVO extends NuclosValueObject {

	private Integer attributegroupId;
	private String attributegroup;
	private Integer roleId;
	private String role;
	private Integer stateId;
	private String state;
	private boolean writeable;

	/**
	 * constructor
	 * @param attributegroupId id of attribute group
	 * @param attributegroup name of attribute group
	 * @param roleId id of the role entity
	 * @param role name of the role entity
	 * @param stateId id of state entity
	 * @param state name of the state entity
	 * @param writeable is attribute group writable?
	 */
	public AttributegroupPermissionVO(Integer attributegroupId, String attributegroup, Integer roleId, String role,
			Integer stateId, String state, boolean writeable) {
		super();
		this.attributegroupId = attributegroupId;
		this.attributegroup = attributegroup;
		this.roleId = roleId;
		this.role = role;
		this.stateId = stateId;
		this.state = state;
		this.writeable = writeable;
	}

	/**
	 * constructor
	 * @param evo contains the common fields
	 * @param attributegroupId id of attribute group
	 * @param attributegroup name of attribute group
	 * @param roleId id of the role entity
	 * @param role name of the role entity
	 * @param stateId id of state entity
	 * @param state name of the state entity
	 * @param writeable is attribute group writable?
	 */
	public AttributegroupPermissionVO(NuclosValueObject evo, Integer attributegroupId, String attributegroup, Integer roleId, String role,
			Integer stateId, String state, boolean writeable) {
		super(evo);
		this.attributegroupId = attributegroupId;
		this.attributegroup = attributegroup;
		this.roleId = roleId;
		this.role = role;
		this.stateId = stateId;
		this.state = state;
		this.writeable = writeable;
	}

	/**
	 * get attribute group id
	 * @return attribute group id
	 */
	public Integer getAttributegroupId() {
		return attributegroupId;
	}

	/**
	 * set attribute group id
	 * @param attributegroupId attribute group id
	 */
	public void setAttributegroupId(Integer attributegroupId) {
		this.attributegroupId = attributegroupId;
	}

	/**
	 * get attribute group name
	 * @return attribute group name
	 */
	public String getAttributegroup() {
		return attributegroup;
	}

	/**
	 * set attribute group name
	 * @param attributegroup attribute group name
	 */
	public void setAttributegroup(String attributegroup) {
		this.attributegroup = attributegroup;
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
	public boolean isWritable() {
		return writeable;
	}

	/**
	 * set writable flag
	 * @param bWriteable writeable flag
	 */
	public void setWritable(boolean writeable) {
		this.writeable = writeable;
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("id=").append(getId());
		result.append(",role=").append(getRole());
		result.append(",roleId=").append(getRoleId());
		result.append(",state=").append(getState());
		result.append(",stateId=").append(getStateId());
		result.append(",agroup=").append(getAttributegroup());
		result.append(",agroupId=").append(getAttributegroupId());
		result.append("]");
		return result.toString();
	}

}
