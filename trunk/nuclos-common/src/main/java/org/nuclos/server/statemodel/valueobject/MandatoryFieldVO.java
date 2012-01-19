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
 * Value object representing a mandatory entity field.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 01.00.00
 */
public class MandatoryFieldVO extends NuclosValueObject {

	private Integer fieldId;
	private Integer stateId;

	/**
	 * constructor
	 * @param fieldId id of entity field
	 * @param stateId id of state entity
	 */
	public MandatoryFieldVO(Integer fieldId, Integer stateId) {
		super();
		this.fieldId = fieldId;
		this.stateId = stateId;
	}

	/**
	 * constructor
	 * @param evo contains the common fields
	 * @param fieldId id of entity field
	 * @param stateId id of state entity
	 */
	public MandatoryFieldVO(NuclosValueObject evo, Integer fieldId, Integer stateId) {
		super(evo);
		this.fieldId = fieldId;
		this.stateId = stateId;
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
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("id=").append(getId());
		result.append(",fieldId=").append(getFieldId());
		result.append(",stateId=").append(getStateId());
		result.append("]");
		return result.toString();
	}

}
