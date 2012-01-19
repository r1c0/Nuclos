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
package org.nuclos.server.processmonitor.valueobject;

import org.nuclos.common2.DateTime;
import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

public class InstanceVO extends NuclosValueObject{
	
	private MasterDataVO mdVO;

	private String name;
	private Integer processmonitorId;
	private String processmonitor;
	private DateTime planstart;
	private DateTime planend;
	private DateTime realstart;
	private DateTime realend;
	
	public InstanceVO() {
		super();
	}
	
	public InstanceVO(NuclosValueObject that, String name, Integer processmonitorId, String processmonitor, DateTime planstart, DateTime planend, DateTime realstart, DateTime realend) {
		super(that);
		this.name = name;
		this.processmonitorId = processmonitorId;
		this.processmonitor = processmonitor;
		this.planstart = planstart;
		this.planend = planend;
		this.realstart = realstart;
		this.realend = realend;
	}
	
	public InstanceVO(MasterDataVO mdVO) {
		this(mdVO.getNuclosValueObject(),
			 (String) mdVO.getField("name"),
			 (Integer) mdVO.getField("processmonitorId"),
			 (String) mdVO.getField("processmonitor"),
			 (DateTime) mdVO.getField("planstart"),
			 (DateTime) mdVO.getField("planend"),
			 (DateTime) mdVO.getField("realstart"),
			 (DateTime) mdVO.getField("realend"));
		this.mdVO = mdVO;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public Integer getProcessmonitorId() {
		return processmonitorId;
	}

	public void setProcessmonitorId(Integer processmonitorId) {
		this.processmonitorId = processmonitorId;
	}

	public String getProcessmonitor() {
		return processmonitor;
	}

	public void setProcessmonitor(String processmonitor) {
		this.processmonitor = processmonitor;
	}

	public DateTime getPlanstart() {
		return planstart;
	}

	public void setPlanstart(DateTime planstart) {
		this.planstart = planstart;
	}

	public DateTime getPlanend() {
		return planend;
	}

	public void setPlanend(DateTime planend) {
		this.planend = planend;
	}

	public DateTime getRealstart() {
		return realstart;
	}

	public void setRealstart(DateTime realstart) {
		this.realstart = realstart;
	}

	public DateTime getRealend() {
		return realend;
	}

	public void setRealend(DateTime realend) {
		this.realend = realend;
	}

	public MasterDataVO getMasterDataVO() {
		if (this.mdVO == null){
//			this.mdVO = new MasterDataVO(MasterDataDelegate.getInstance().getMetaData(NuclosEntity.INSTANCE),false);	
		}
		updateMasterDataVO();
		return mdVO;
	}
	
	private void updateMasterDataVO() {
		this.mdVO.setField("name", this.getName());
		this.mdVO.setField("processmonitorId", this.getProcessmonitorId());
		this.mdVO.setField("processmonitor", this.getProcessmonitor());
		this.mdVO.setField("planstart", this.getPlanstart());
		this.mdVO.setField("planend", this.getPlanend());
		this.mdVO.setField("realstart", this.getRealstart());
		this.mdVO.setField("realend", this.getRealend());
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("id=").append(getId());
		result.append(",name=").append(getName());
		result.append(",mdVo=").append(getMasterDataVO());
		result.append(",pm=").append(getProcessmonitor());
		result.append(",pmId=").append(getProcessmonitorId());
		result.append("]");
		return result.toString();
	}

}
