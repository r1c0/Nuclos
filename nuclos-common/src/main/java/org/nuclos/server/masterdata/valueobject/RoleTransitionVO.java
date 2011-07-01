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
package org.nuclos.server.masterdata.valueobject;

import org.nuclos.server.common.valueobject.NuclosValueObject;

/**
 * Value object representing a role transision
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christian.niehues@novabit.de">christian.niehues</a>
 * @version 00.01.000
 */
public class RoleTransitionVO extends NuclosValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer roleId;
	private Integer transitionId;

	/**
	 * 	constructor to be called by server only
	 * @param id
	 * @param generationId
	 * @param ruleId
	 * @param order
	 * @param createdAt
	 * @param createdBy
	 * @param changedAt
	 * @param changedBy
	 * @param version
	 */
	public RoleTransitionVO(Integer id, Integer transitionId, Integer roleId
			, java.util.Date createdAt, String createdBy, java.util.Date changedAt, String changedBy, Integer version) {
		super(id, createdAt, createdBy, changedAt, changedBy, version);

		this.transitionId = transitionId;
		this.roleId = roleId;
	}

	public RoleTransitionVO(NuclosValueObject evo, Integer transitionId, Integer ruleId) {
		super(evo);
		this.transitionId = transitionId;
		this.roleId = ruleId;
	}

	@Override
	public String toString() {
		return " ID: " + this.getId() + " Transition Id: " + this.getTransitionId()
				+ " Role Id: " + this.getRoleId();
	}

	public Integer getTransitionId() {
		return transitionId;
	}

	protected void setTransitionId(Integer generationId) {
		transitionId = generationId;
	}

	public Integer getRoleId() {
		return roleId;
	}

	protected void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

}
