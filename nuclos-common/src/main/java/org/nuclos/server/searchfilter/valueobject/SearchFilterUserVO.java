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
package org.nuclos.server.searchfilter.valueobject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Value object representing a searchfilteruser.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:martin.weber@novabit.de">Martin Weber</a>
 * @version 00.01.000
 */
public class SearchFilterUserVO extends NuclosValueObject {

	private Integer iSearchFilterId;
	private Integer iUserId;
	private Boolean bEditable;
	private Boolean bForced;
	private Boolean bCompulsory;
	private Date dValidFrom;
	private Date dValidUntil;
	
	public SearchFilterUserVO() {
		super();
	}
	
	public SearchFilterUserVO(Integer iId, Integer iSearchFilterId, Integer iUserId, Boolean bEditable, Boolean bForced, Boolean bCompulsory, Date dValidFrom, Date dValidUntil,
			java.util.Date dCreated, String sCreated, java.util.Date dChanged, String sChanged, Integer iVersion) {
		super(iId, dCreated, sCreated, dChanged, sChanged, iVersion);
		
		setSearchFilter(iSearchFilterId);
		setUser(iUserId);
		setEditable(bEditable);
		setForced(bForced);
		setCompulsory(bCompulsory);
		setValidFrom(dValidFrom);
		setValidUntil(dValidUntil);
	}

	public void setSearchFilter(Integer iSearchFilterId) {
		this.iSearchFilterId = iSearchFilterId;
	}
	
	public Integer getSearchFilter() {
		return this.iSearchFilterId;
	}
	
	public void setUser(Integer iUserId) {
		this.iUserId = iUserId;
	}
	
	public Integer getUser() {
		return this.iUserId;
	}
	
	public void setEditable(Boolean bEditable) {
		this.bEditable = bEditable;
	}
	
	public Boolean isEditable() {
		return this.bEditable;
	}
	
	public void setForced(Boolean bForced) {
		this.bForced = bForced;
	}
	
	public Boolean isForced() {
		return this.bForced;
	}

	public Boolean isCompulsory() {
		return bCompulsory;
	}
	
	public void setCompulsory(Boolean b) {
		this.bCompulsory = b;
	}
	
	public void setValidFrom(Date dValidFrom) {
		this.dValidFrom = dValidFrom;
	}
	
	public Date getValidFrom() {
		return this.dValidFrom;
	}
	
	public void setValidUntil(Date dValidUntil) {
		this.dValidUntil = dValidUntil;
	}
	
	public Date getValidUntil() {
		return this.dValidUntil;
	}
	
	public static SearchFilterUserVO transformToSearchFilterUser(MasterDataVO mdVO_searchFilteruser) {
		SearchFilterUserVO searchFilterUser = new SearchFilterUserVO(
				mdVO_searchFilteruser.getIntId(),
		(Integer)mdVO_searchFilteruser.getField("searchfilterId"),
		(Integer)mdVO_searchFilteruser.getField("userId"),
		(Boolean)mdVO_searchFilteruser.getField("editable"),
		(Boolean)mdVO_searchFilteruser.getField("forcefilter"),
		(Boolean)mdVO_searchFilteruser.getField("compulsoryFilter"),
		(Date)mdVO_searchFilteruser.getField("validFrom"),
		(Date)mdVO_searchFilteruser.getField("validUntil"),
		mdVO_searchFilteruser.getCreatedAt(),
		mdVO_searchFilteruser.getCreatedBy(),
		mdVO_searchFilteruser.getChangedAt(),
		mdVO_searchFilteruser.getChangedBy(),
		mdVO_searchFilteruser.getVersion());
		
		return searchFilterUser;
	}
	
	public static MasterDataVO transformToMasterData(SearchFilterUserVO searchFilterUser) {
		Map<String, Object> mpField = new HashMap<String, Object>();
		mpField.put("searchfilterId", searchFilterUser.getSearchFilter());
		mpField.put("userId", searchFilterUser.getUser());
		mpField.put("editable", searchFilterUser.isEditable());
		mpField.put("forcefilter", searchFilterUser.isForced());
		mpField.put("compulsoryFilter", searchFilterUser.isCompulsory());
		mpField.put("validFrom", searchFilterUser.getValidFrom());
		mpField.put("validUntil", searchFilterUser.getValidUntil());
		
		MasterDataVO mdVO = new MasterDataVO(
				searchFilterUser.getId(),
				searchFilterUser.getCreatedAt(),
				searchFilterUser.getCreatedBy(),
				searchFilterUser.getChangedAt(),
				searchFilterUser.getChangedBy(),
				searchFilterUser.getVersion(),
				mpField);
		
		return mdVO;
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("id=").append(getId());
		result.append(",user=").append(getUser());
		result.append(",forced=").append(isForced());
		result.append(",filterId=").append(getSearchFilter());
		result.append("]");
		return result.toString();
	}

}
