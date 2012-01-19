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
 * Value object representing a state history entry.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 01.00.00
 */
public class StateHistoryVO extends NuclosValueObject {

	private Integer iGenericObjectId;
	private Integer iStateId;
	private String sStateName;

	/**
	 * constructor to be called by server only
	 * @param iId primary key of underlying database record
	 * @param iGenericObjectId id of the related generic object
	 * @param iStateId id of state in history
	 * @param sStateName name of state in history
	 * @param dCreated creation date of underlying database record
	 * @param sCreated creator of underlying database record
	 * @param dChanged last changed date of underlying database record
	 * @param sChanged last changer of underlying database record
	 */
	public StateHistoryVO(Integer iId, Integer iGenericObjectId, Integer iStateId, String sStateName, java.util.Date dCreated, String sCreated,
			java.util.Date dChanged, String sChanged, Integer iVersion) {
		super(iId, dCreated, sCreated, dChanged, sChanged, iVersion);
		this.iGenericObjectId = iGenericObjectId;
		this.iStateId = iStateId;
		this.sStateName = sStateName;
	}

	/**
	 * constructor to be called by client only
	 * @param iStateId id of state in history
	 * @param sStateName name of state in history
	 */
	public StateHistoryVO(Integer iGenericObjectId, Integer iStateId, String sStateName) {
		super();
		this.iGenericObjectId = iGenericObjectId;
		this.iStateId = iStateId;
		this.sStateName = sStateName;
	}

	/**
	 * get generic object id of underlying database record
	 * @return generic object id of underlying database record
	 */
	public Integer getGenericObjectId() {
		return iGenericObjectId;
	}

	/**
	 * set generic object id of underlying database record
	 * @return generic object id of underlying database record
	 */
	public void setGenericObjectId(Integer iGenericObjectId) {
		this.iGenericObjectId = iGenericObjectId;
	}

	/**
	 * get state id of underlying database record
	 * @return state id of underlying database record
	 */
	public Integer getStateId() {
		return iStateId;
	}

	/**
	 * set state id of underlying database record
	 * @param iStateId state id of underlying database record
	 */
	public void setStateId(Integer iStateId) {
		this.iStateId = iStateId;
	}

	/**
	 * get state name of underlying database record
	 * @return state name of underlying database record
	 */
	public String getStateName() {
		return sStateName;
	}

	/**
	 * set state name of underlying database record
	 * @param sStateName state name of underlying database record
	 */
	public void setStateName(String sStateName) {
		this.sStateName = sStateName;
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("id=").append(getId());
		result.append(",state=").append(getStateName());
		result.append(",goId=").append(getGenericObjectId());
		result.append("]");
		return result.toString();
	}

}
