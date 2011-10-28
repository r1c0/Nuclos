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
 * Value object representing a subform column permission.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version	01.00.00
 */
public class SubformColumnPermissionVO extends NuclosValueObject {

	private Integer roleSubformId;
	private String column;
	private boolean writeable;

	/**
	 * constructor
	 * @param roleSubformId
	 * @param column
	 * @param writeable is attribute group writable?
	 */
	public SubformColumnPermissionVO(Integer roleSubformId, String column, boolean writeable) {
		super();
		this.roleSubformId = roleSubformId;
		this.setColumn(column);
		this.writeable = writeable;
	}

	/**
	 * constructor
	 * @param evo contains the common fields
	 * @param roleSubformId
	 * @param column
	 * @param writeable is attribute group writable?
	 */
	public SubformColumnPermissionVO(NuclosValueObject evo, Integer roleSubformId, String column, boolean writeable) {
		super(evo);
		this.roleSubformId = roleSubformId;
		this.setColumn(column);
		this.writeable = writeable;
	}

	/**
	 * @return
	 */
	public Integer getRoleSubformId() {
		return roleSubformId;
	}

	/**
	 * @param roleSubformId
	 */
	public void setRoleSubformId(Integer subformId) {
		this.roleSubformId = subformId;
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

	/**
     * @param column the column to set
     */
    public void setColumn(String column) {
	    this.column = column;
    }

	/**
     * @return the column
     */
    public String getColumn() {
	    return column;
    }
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("id=").append(getId());
		result.append(",column=").append(getColumn());
		result.append(",roleId=").append(getRoleSubformId());
		result.append("]");
		return result.toString();
	}

}
