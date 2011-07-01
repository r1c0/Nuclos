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
/**
 * 
 */
package org.nuclos.server.processmonitor.valueobject;

import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.statemodel.valueobject.StateModelVO;

/**
 * @author Marc.Finke
 * 
 * value object of a subprocess
 *
 */
public class SubProcessVO extends NuclosValueObject{
	
	/**
	 * This id is used before a new object is saved.
	 * A dirty id is < 0
	 */
	private Integer iDirtyId;
	
	private StateModelVO stateModelVO;
	private Integer iStateModelUsageId;
	private Integer iRuntime;
	private Integer iRuntimeFormat;
	private String sStatename;
	private String sDescription;
	private String sGuarantor;
	private String sSecondGuarator;
	private String sSupervisor;
	private String sOriginalSystem;
	private String sPlanStartSeries;
	private String sPlanEndSeries;
	private Integer iProcessMonitorId;
	
	/**
	 * 
	 * @param id
	 * @param iStateModelId
	 * @param iStateModelUsageId
	 * @param sStatename
	 * @param sDescription
	 * @param strGuarantor
	 * @param strSecondGuarantor
	 * @param strSupervisor
	 * @param strOriginalSystem
	 * @param strPlanStartSeries
	 * @param strPlanEndSeries
	 * @param iRuntime
	 * @param iRuntimeFormat
	 */
	public SubProcessVO(Integer id, StateModelVO stateModelVO, Integer iStateModelUsageId, String sStatename, String sDescription, String strGuarantor, String strSecondGuarantor, String strSupervisor, String strOriginalSystem, String strPlanStartSeries, String strPlanEndSeries, Integer iRuntime, Integer iRuntimeFormat, Integer iProcessMonitorId) {
		this(new NuclosValueObject(id!=null?(id<=0?null:id):null, null, null, null, null, null), stateModelVO, iStateModelUsageId, sStatename, sDescription, strGuarantor, strSecondGuarantor, strSupervisor, strOriginalSystem, strPlanStartSeries, strPlanEndSeries, iRuntime, iRuntimeFormat, iProcessMonitorId);
		if (id != null && id <= 0){
			iDirtyId = id;
		}
	}
	
	/**
	 * 
	 * @param that
	 * @param iStateModelId
	 * @param iStateModelUsageId
	 * @param sStatename
	 * @param sDescription
	 * @param strGuarantor
	 * @param strSecondGuarantor
	 * @param strSupervisor
	 * @param strOriginalSystem
	 * @param strPlanStartSeries
	 * @param strPlanEndSeries
	 * @param iRuntime
	 * @param iRuntimeFormat
	 */
	public SubProcessVO(NuclosValueObject that, StateModelVO stateModelVO, Integer iStateModelUsageId, String sStatename, String sDescription, String strGuarantor, String strSecondGuarantor, String strSupervisor, String strOriginalSystem, String strPlanStartSeries, String strPlanEndSeries, Integer iRuntime, Integer iRuntimeFormat, Integer iProcessMonitorId) {
		super(that);
		this.iStateModelUsageId = iStateModelUsageId;
		this.sStatename = sStatename;
		this.sDescription = sDescription;
		this.sGuarantor = strGuarantor;
		this.sSecondGuarator = strSecondGuarantor;
		this.sSupervisor = strSupervisor;
		this.sOriginalSystem = strOriginalSystem;
		this.sPlanStartSeries = strPlanStartSeries;
		this.sPlanEndSeries = strPlanEndSeries;
		this.iRuntime = iRuntime;
		this.iRuntimeFormat = iRuntimeFormat;
		this.iProcessMonitorId = iProcessMonitorId;
		this.setStateModelVO(stateModelVO);
	}
	
	/**
	 * get mnemonic of underlying database record
	 *
	 * @return mnemonic of underlying database record
	 */
	public Integer getRuntime() {
		return iRuntime;
	}

	/**
	 * set mnemonic of underlying database record
	 *
	 * @param iNumeral mnemonic of underlying database record
	 */
	public void setRuntime(Integer iRuntime) {
		this.iRuntime = iRuntime;
	}

	/**
	 * get state name of underlying database record
	 *
	 * @return state name of underlying database record
	 */
	public String getStatename() {
		return sStatename;
	}

	/**
	 * set state name of underlying database record
	 *
	 * @param sStatename state name of underlying database record
	 */
	public void setStatename(String sStatename) {
		this.sStatename = sStatename;
	}

	/**
	 * get state description of underlying database record
	 *
	 * @return state description of underlying database record
	 */
	public String getDescription() {
		return sDescription;
	}

	/**
	 * set state description of underlying database record
	 *
	 * @param sDescription state description of underlying database record
	 */
	public void setDescription(String sDescription) {
		this.sDescription = sDescription;
	}

	public String getGuarantor() {
		return sGuarantor;
	}

	public void setGuarantor(String guarantor) {
		sGuarantor = guarantor;
	}

	public String getSecondGuarator() {
		return sSecondGuarator;
	}

	public void setSecondGuarator(String secondGuarator) {
		sSecondGuarator = secondGuarator;
	}

	public String getSupervisor() {
		return sSupervisor;
	}

	public void setSupervisor(String supervisor) {
		sSupervisor = supervisor;
	}
	
	public String getOriginalSystem() {
		return sOriginalSystem;
	}

	public void setOriginalSystem(String orignalSystem) {
		sOriginalSystem = orignalSystem;
	}

	public Integer getRuntimeFormat() {
		return iRuntimeFormat;
	}

	public void setRuntimeFormat(Integer runtimeFormat) {
		iRuntimeFormat = runtimeFormat;
	}

	public String getPlanStartSeries() {
		return sPlanStartSeries;
	}

	public void setPlanStartSeries(String planStartSeries) {
		sPlanStartSeries = planStartSeries;
	}

	public String getPlanEndSeries() {
		return sPlanEndSeries;
	}

	public void setPlanEndSeries(String planEndSeries) {
		sPlanEndSeries = planEndSeries;
	}

	public Integer getStateModelUsageId() {
		return iStateModelUsageId;
	}

	public void setStateModelUsageId(Integer stateModelUsageId) {
		iStateModelUsageId = stateModelUsageId;
	}

	public StateModelVO getStateModelVO() {
		return stateModelVO;
	}

	public void setStateModelVO(StateModelVO stateModel) {
		stateModelVO = stateModel;
		if (stateModel != null){
			this.sStatename = stateModel.getName();
			this.sDescription = stateModel.getDescription();
		} 
	}

	/**
	 * 
	 * @return dirty ID if set, otherwise returns <code>getId()</code>
	 */
	public Integer getWorkingId() {
		if (super.getId() == null && this.iDirtyId != null && this.iDirtyId <= 0){
			return this.iDirtyId;
		}
		return super.getId();
	}
	
	public Integer getProcessMonitorId() {
		return iProcessMonitorId;
	}

	public void setProcessMonitorId(Integer iProcessMonitorId) {
		this.iProcessMonitorId = iProcessMonitorId;
	}

}
