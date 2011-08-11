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
package org.nuclos.server.job.valueobject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Value object representing a job.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version 00.01.000
 */
public class JobVO extends NuclosValueObject {

	private static final long serialVersionUID = 1L;
	private String sName;
	private String sType;
	private String sDescription;
	private Date dStartdate;
	private String sStarttime;
	private Integer iInterval;
	private String sUnit;
	private Boolean bUseCronExpression;
	private String sCronExpression;
	private String sUser;
	private Integer iUser;
	private String sLevel;
	private Integer iDeleteInDays;
	private String sLastState;
	private Boolean bRunning;
	private String dNextFireTime;
	private String dLastFireTime;
	private String sResult;
	private String sResultDetails;
	private Integer iSessionId;
	private Integer nucletId;

	// map for dependant child subform data
	private DependantMasterDataMap mpDependants = new DependantMasterDataMap();

	public JobVO(MasterDataVO mdVO) {
		this(mdVO, null);
	}

	public JobVO(MasterDataVO mdVO, DependantMasterDataMap mpDependants) {
		super(mdVO.getIntId(), mdVO.getCreatedAt(), mdVO.getCreatedBy(), mdVO.getChangedAt(), mdVO.getChangedBy(), mdVO.getVersion());
		this.sName = (String)mdVO.getField("name");
		this.sType = (String)mdVO.getField("type");
		this.sDescription = (String)mdVO.getField("description");
		this.dStartdate = (Date)mdVO.getField("startdate");
		this.sStarttime = (String)mdVO.getField("starttime");
		this.iInterval = (Integer)mdVO.getField("interval");
		this.sUnit = (String)mdVO.getField("unit");
		this.sUser = (String)mdVO.getField("user");
		this.iUser = (Integer)mdVO.getField("userId");
		this.bUseCronExpression = (Boolean)mdVO.getField("usecronexpression");
		this.sCronExpression = (String)mdVO.getField("cronexpression");
		this.sLevel = (String)mdVO.getField("level");
		this.iDeleteInDays = (Integer)mdVO.getField("days");
		this.sLastState = (String)mdVO.getField("laststate");
		this.bRunning = (Boolean)mdVO.getField("running");
		this.dNextFireTime = (String)mdVO.getField("nextfiretime");
		this.dLastFireTime = (String)mdVO.getField("lastfiretime");
		this.sResult = (String)mdVO.getField("result");
		this.sResultDetails = (String)mdVO.getField("resultdetails");
		this.nucletId = (Integer)mdVO.getField("nucletId");
		this.mpDependants = mpDependants;
	}

	/**
	 * constructor to be called by server only
	 * @param sName name of underlying database record
	 * @param sType type of underlying database record
	 */
	public JobVO(String sName, String sType, String sDescription, Date dStartdate, String sStarttime, Integer iInterval,
			String sUnit, String sUser, String sLevel, Integer iDays) {
		this.sName = sName;
		this.sType = sType;
		this.sDescription = sDescription;
		this.dStartdate = dStartdate;
		this.sStarttime = sStarttime;
		this.iInterval = iInterval;
		this.sUnit = sUnit;
		this.sUser = sUser;
		this.sLevel = sLevel;
		this.iDeleteInDays = iDays;
		this.sLastState = null;
		this.bRunning = false;
		this.dLastFireTime = null;
		this.dNextFireTime = null;
		this.sResult = null;
		this.sResultDetails = null;
	}


	public JobVO(String sName, String sType, String sDescription, Date dStartdate, String sStarttime, Integer iInterval,
			String sUnit) {
		this.sName = sName;
		this.sType = sType;
		this.sDescription = sDescription;
		this.dStartdate = dStartdate;
		this.sStarttime = sStarttime;
		this.iInterval = iInterval;
		this.sUnit = sUnit;
		this.sUser = null;
		this.sLevel = null;
		this.iDeleteInDays = null;
		this.sLastState = null;
		this.bRunning = false;
		this.dLastFireTime = null;
		this.dNextFireTime = null;
		this.sResult = null;
		this.sResultDetails = null;
	}

	/**
	 * get job name of underlying database record
	 * @return job name of underlying database record
	 */
	public String getName() {
		return this.sName;
	}

	/**
	 * set job name of underlying database record
	 * @param sName job name of underlying database record
	 */
	public void setName(String sName) {
		this.sName = sName;
	}

	/**
	 * get job type of underlying database record
	 * @return job type of underlying database record
	 */
	public String getType() {
		return this.sType;
	}

	/**
	 * set job type.
	 * @param sType
	 */
	public void setType(String sType) {
		this.sType = sType;
	}

	/**
	 * get job description of underlying database record
	 * @return job description of underlying database record
	 */
	public String getDescription() {
		return this.sDescription;
	}

	/**
	 * set job description of underlying database record
	 * @param sDescription job description of underlying database record
	 */
	public void setDescription(String sDescription) {
		this.sDescription = sDescription;
	}

	/**
	 * get job start date of underlying database record
	 * @return job start date of underlying database record
	 */
	public Date getStartdate() {
		return this.dStartdate;
	}

	/**
	 * set job start date of underlying database record
	 * @param dStartdate job start date of underlying database record
	 */
	public void setStartdate(Date dStartdate) {
		this.dStartdate = dStartdate;
	}

	/**
	 * get job start time of underlying database record
	 * @return job start time of underlying database record
	 */
	public String getStarttime() {
		return this.sStarttime;
	}

	/**
	 * set job start time of underlying database record
	 * @param iStarttime job start time of underlying database record
	 */
	public void setStarttime(String sStarttime) {
		this.sStarttime = sStarttime;
	}

	/**
	 * get job interval start time of underlying database record
	 * @return job interval start time of underlying database record
	 */
	public Integer getInterval() {
		return this.iInterval;
	}

	/**
	 * set job interval start time of underlying database record
	 * @param iInterval job interval start time of underlying database record
	 */
	public void setInterval(Integer iInterval) {
		this.iInterval = iInterval;
	}

	/**
	 * get job unit interval start time of underlying database record
	 * @return job unit interval start time of underlying database record
	 */
	public String getUnit() {
		return this.sUnit;
	}

	/**
	 * set job unit interval start time of underlying database record
	 * @param sUnit job unit interval start time of underlying database record
	 */
	public void setUnit(String sUnit) {
		this.sUnit = sUnit;
	}

	public String getUser() {
		return this.sUser;
	}

	public void setUser(String sUser) {
		this.sUser = sUser;
	}

	public Integer getUserId() {
		return this.iUser;
	}

	public void setUserId(Integer iUser) {
		this.iUser = iUser;
	}

	public String getLevel() {
		return this.sLevel;
	}

	public void setLevel(String sLevel) {
		this.sLevel = sLevel;
	}

	public Integer getDeleteInDays() {
		return this.iDeleteInDays;
	}

	public void setDeleteInDays(Integer iDeleteInDays) {
		this.iDeleteInDays = iDeleteInDays;
	}

	public String getLastState() {
		return this.sLastState;
	}

	public void setLastState(String sLastState) {
		this.sLastState = sLastState;
	}

	public Boolean isRunning() {
		return this.bRunning;
	}

	public void setRunning(Boolean bRunning) {
		this.bRunning = bRunning;
	}

	public String getLastFireTime() {
		return this.dLastFireTime;
	}

	public void setLastFireTime(String dLastFireTime) {
		this.dLastFireTime = dLastFireTime;
	}

	public String getNextFireTime() {
		return this.dNextFireTime;
	}

	public void setNextFireTime(String dNextFireTime) {
		this.dNextFireTime = dNextFireTime;
	}

	public String getResult() {
		return this.sResult;
	}

	public void setResult(String sResult) {
		this.sResult = sResult;
	}

	public String getResultDetails() {
		return this.sResultDetails;
	}


	public void setResultDetails(String sResultDetails) {
		this.sResultDetails = sResultDetails;
	}

	public Integer getSessionId() {
		return this.iSessionId;
	}

	public void setSessionId(Integer iSessionId) {
		this.iSessionId = iSessionId;
	}

	public Boolean getUseCronExpression() {
    	return bUseCronExpression;
    }

	public void setUseCronExpression(Boolean bUseCronExpression) {
    	this.bUseCronExpression = bUseCronExpression;
    }

	public String getCronExpression() {
    	return sCronExpression;
    }

	public void setCronExpression(String sCronExpression) {
    	this.sCronExpression = sCronExpression;
    }

	public Integer getNucletId() {
		return nucletId;
	}

	public void setNucletId(Integer nucletId) {
		this.nucletId = nucletId;
	}

	@Override
	public int hashCode() {
		return (getName() != null ? getName().hashCode() : 0);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof JobVO) {
			final JobVO that = (JobVO) o;
			// resources are equal if there names are equal
			return this.getName().equals(that.getName());
		}
		return false;
	}

	@Override
	public String toString() {
		return this.getName();
	}

	public MasterDataVO toMasterDataVO() {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("name", getName());
		fields.put("type", getType());
		fields.put("description", getDescription());
		fields.put("startdate", getStartdate());
		fields.put("starttime", getStarttime());
		fields.put("interval", getInterval());
		fields.put("unit", getUnit());
		fields.put("usecronexpression", getUseCronExpression());
		fields.put("cronexpression", getCronExpression());
		fields.put("user", getUser());
		fields.put("userId", getUserId());
		fields.put("level", getLevel());
		fields.put("days", getDeleteInDays());
		fields.put("laststate", getLastState());
		fields.put("running", isRunning());
		fields.put("nextfiretime", getNextFireTime());
		fields.put("lastfiretime", getLastFireTime());
		fields.put("result", getResult());
		fields.put("resultdetails", getResultDetails());
		fields.put("nucletId", getNucletId());
		return new MasterDataVO(getId(), getCreatedAt(), getCreatedBy(), getChangedAt(), getChangedBy(), getVersion(), fields);
	}

	public DependantMasterDataMap getDependants() {
		return this.mpDependants;
	}

	public void setDependants(DependantMasterDataMap mpDependants) {
		this.mpDependants = mpDependants;
	}

}
