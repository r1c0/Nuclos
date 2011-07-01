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
 * Value object representing a entity field permission.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 01.00.00
 */
public class EntityFieldPermissionVO extends NuclosValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer fieldId;
	private Integer roleId;
	private Integer stateId;
	private boolean readable;
	private boolean writeable;

	/**
	 * constructor
	 * @param fieldId id of entity field
	 * @param roleId id of the role entity
	 * @param stateId id of state entity
	 * @param readable is attribute readable?
	 * @param writeable is attribute writable?
	 */
	public EntityFieldPermissionVO(Integer fieldId, Integer roleId,	Integer stateId, boolean readable, boolean writeable) {
		super();
		this.fieldId = fieldId;
		this.roleId = roleId;
		this.stateId = stateId;
		this.readable = readable;
		this.writeable = writeable;
	}

	/**
	 * constructor
	 * @param evo contains the common fields
	 * @param fieldId id of entity field
	 * @param roleId id of the role entity
	 * @param stateId id of state entity
	 * @param readable is attribute readable?
	 * @param writeable is attribute writable?
	 */
	public EntityFieldPermissionVO(NuclosValueObject evo, Integer fieldId, Integer roleId, Integer stateId, boolean readable, boolean writeable) {
		super(evo);
		this.fieldId = fieldId;
		this.roleId = roleId;
		this.stateId = stateId;
		this.readable = readable;
		this.writeable = writeable;
	}

	/**
     * @return the fieldId
     */
    public Integer getFieldId() {
    	return fieldId;
    }

	/**
     * @param fieldId the fieldId to set
     */
    public void setFieldId(Integer fieldId) {
    	this.fieldId = fieldId;
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
	 * get readable flag
	 * @return the readable flag
	 */
	public boolean isReadable() {
		return readable;
	}

	/**
	 * set readable flag
	 * @param readable the readable to set
	 */
	public void setReadable(boolean readable) {
		this.readable = readable;
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
