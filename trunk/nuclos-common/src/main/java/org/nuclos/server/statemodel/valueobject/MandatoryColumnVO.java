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
 * Value object representing a mandatory subform column.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 01.00.00
 */
public class MandatoryColumnVO extends NuclosValueObject {

	private String entity;
	private String column;
	private Integer stateId;

	/**
	 * constructor
	 * @param entity
	 * @param column
	 * @param stateId id of state entity
	 */
	public MandatoryColumnVO(String entity, String column, Integer stateId) {
		super();
		this.setEntity(entity);
		this.column = column;
		this.stateId = stateId;
	}

	/**
	 * constructor
	 * @param evo contains the common fields
	 * @param entity
	 * @param column
	 * @param stateId id of state entity
	 */
	public MandatoryColumnVO(NuclosValueObject evo, String entity, String column, Integer stateId) {
		super(evo);
		this.setEntity(entity);
		this.column = column;
		this.stateId = stateId;
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

	/**
     * @param entity the entity to set
     */
    public void setEntity(String entity) {
	    this.entity = entity;
    }

	/**
     * @return the entity
     */
    public String getEntity() {
	    return entity;
    }
    
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("id=").append(getId());
		result.append(",entity=").append(getEntity());
		result.append(",column=").append(getColumn());
		result.append(",stateId=").append(getStateId());
		result.append("]");
		return result.toString();
	}

}
