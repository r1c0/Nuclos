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

import org.nuclos.common2.exception.CommonValidationException;
import java.util.Date;

/**
 * Value Object for TimelimitTask.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars.Rueckemann</a>
 * @version 01.00.00
 */
public class TimelimitTaskVO extends NuclosValueObject {

	private String sDescription;
	private java.util.Date dateExpired;
	private java.util.Date dateCompleted;
	private Integer iGenericObjectId;
	private Integer iModuleId;
	private String sIdentifier;

	private String sStatus;
	private String sProcess;

	/**
	 * constructor to be called by server only
	 *
	 * @param iId
	 * @param sDescription
	 * @param dExpired
	 * @param dCompleted
	 * @param iGenericObjectId
	 * @param iModuleId
	 * @param sIdentifier
	 * @param dCreated
	 * @param sCreated
	 * @param dChanged
	 * @param sChanged
	 * @param iVersion
	 */
	public TimelimitTaskVO(Integer iId, String sDescription, Date dExpired, Date dCompleted, Integer iGenericObjectId,
			Integer iModuleId, String sIdentifier, String sStatus, String sProcess,
			Date dCreated, String sCreated, Date dChanged, String sChanged, Integer iVersion) {
		super(iId, dCreated, sCreated, dChanged, sChanged, iVersion);
		this.sDescription = sDescription;
		this.dateExpired = dExpired;
		this.dateCompleted = dCompleted;
		this.iGenericObjectId = iGenericObjectId;
		this.iModuleId = iModuleId;
		this.sIdentifier = sIdentifier;
		this.sStatus = sStatus;
		this.sProcess = sProcess;
	}

	/**
	 * constructor to be called by client only
	 *
	 * @param sDescription	strdescription from database
	 * @param dateExpired		datscheduled from database
	 * @param dateCompleted		datcompleted from database
	 * @param iGenericObjectId intid_t_ud_genericobject from database
	 */
	public TimelimitTaskVO(String sDescription, Date dateExpired, Date dateCompleted, Integer iGenericObjectId) {
		super();
		this.sDescription = sDescription;
		this.dateExpired = dateExpired;
		this.dateCompleted = dateCompleted;
		this.iGenericObjectId = iGenericObjectId;
		this.iModuleId = null;
		this.sIdentifier = null;
		this.sStatus = null;
		this.sProcess = null;
	}

	/**
	 * default constructor to be called by client only
	 */
	public TimelimitTaskVO() {
		super();
		this.sDescription = null;
		this.dateExpired = null;
		this.dateCompleted = null;
		this.iGenericObjectId = null;
		this.iModuleId = null;
		this.sIdentifier = null;
		this.sStatus = null;
		this.sProcess = null;
	}

	/**
	 * get description for task
	 *
	 * @return description for task
	 */
	public String getDescription() {
		return sDescription;
	}

	/**
	 * set description for task
	 *
	 * @param sDescripton description for task
	 */
	public void setDescription(String sDescripton) {
		this.sDescription = sDescripton;
	}

	/**
	 * get expire date for task
	 *
	 * @return expire date for for task
	 */
	public Date getExpired() {
		return dateExpired;
	}

	/**
	 * set expire date for task
	 *
	 * @param dExpired until date for task
	 */
	public void setExpired(Date dExpired) {
		this.dateExpired = dExpired;
	}

	/**
	 * get completed date for task
	 *
	 * @return completed date for for task
	 */
	public Date getCompleted() {
		return dateCompleted;
	}

	/**
	 * set completed date for task
	 *
	 * @param dCompleted until date for task
	 */
	public void setCompleted(Date dCompleted) {
		this.dateCompleted = dCompleted;
	}

	/**
	 * get leased object id for task
	 *
	 * @return leased object id for task
	 */
	public Integer getGenericObjectId() {
		return iGenericObjectId;
	}

	/**
	 * set leased object id for task
	 *
	 * @param iGenericObjectId leased object id
	 */
	public void setGenericObjectId(Integer iGenericObjectId) {
		this.iGenericObjectId = iGenericObjectId;
	}

	/**
	 * get module id for leased object in task
	 *
	 * @return module id for leased object in task
	 */
	public Integer getModuleId() {
		return iModuleId;
	}

	public void setModuleId(Integer iModuleId) {
		this.iModuleId = iModuleId;
	}
	
	/**
	 * get system identifier for leased object in task
	 *
	 * @return system identifier for leased object in task
	 */
	public String getIdentifier() {
		return sIdentifier;
	}
	
	public void setIdentifier(String sIdentifier) {
		this.sIdentifier = sIdentifier;
	}

	public String getProcess() {
		return sProcess;
	}

	public void setProcess(String sProcess) {
		this.sProcess = sProcess;
	}

	public String getStatus() {
		return sStatus;
	}

	public void setStatus(String sStatus) {
		this.sStatus = sStatus;
	}

	/**
	 * validity checker
	 */
	@Override
	public void validate() throws CommonValidationException {

	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("id=").append(getId());
		result.append(",identifier=").append(getIdentifier());
		result.append(",process=").append(getProcess());
		result.append(",status=").append(getStatus());
		result.append(",goId=").append(getGenericObjectId());
		result.append(",moduleId=").append(getModuleId());
		result.append("]");
		return result.toString();
	}

}	// class TimelimitTaskVO
