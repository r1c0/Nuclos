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
import org.nuclos.server.genericobject.searchcondition.CollectableGenericObjectSearchExpression;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Value object representing a searchfilter.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:martin.weber@novabit.de">Martin Weber</a>
 * @version 00.01.000
 */
public class SearchFilterVO extends NuclosValueObject {

	private static final long serialVersionUID = 1L;
	private String sFilterName;
	private String sDescription;
	private String sFilterPrefs;
	private String sOwner;
	private String sEntity;
	private Integer iSearchDeleted;
	private String labelResourceId;
	private String descriptionResourceId;

	private SearchFilterUserVO searchFilterUserVO;

	public SearchFilterVO() {
		super();

		this.searchFilterUserVO = new SearchFilterUserVO();
	}

	public SearchFilterVO(Integer iId, String sFilterName, String sDescription, String sFilterPrefs, String sOwner, String sEntity,
			Integer iSearchDeleted, String labelResourceId, String descriptionResourceId, SearchFilterUserVO searchFilterUserVO,
			java.util.Date dCreated, String sCreated, java.util.Date dChanged, String sChanged, Integer iVersion) {
		super(iId, dCreated, sCreated, dChanged, sChanged, iVersion);

		setFilterName(sFilterName);
		setDescription(sDescription);
		setFilterPrefs(sFilterPrefs);
		setOwner(sOwner);
		setEntity(sEntity);
		setSearchDeleted(iSearchDeleted);
		setLabelResourceId(labelResourceId);
		setDescriptionResourceId(descriptionResourceId);
		setSearchFilterUser(searchFilterUserVO);
	}

	public SearchFilterVO (SearchFilterVO searchFilterVO) {
		this(searchFilterVO.getId(), searchFilterVO.getFilterName(), searchFilterVO.getDescription(), searchFilterVO.getFilterPrefs(),
				searchFilterVO.getOwner(), searchFilterVO.getEntity(), searchFilterVO.getSearchDeleted(), searchFilterVO.getLabelResourceId(), searchFilterVO.getDescriptionResourceId(),
				searchFilterVO.getSearchFilterUser(), searchFilterVO.getChangedAt(), searchFilterVO.getCreatedBy(), searchFilterVO.getChangedAt(),
				searchFilterVO.getCreatedBy(), searchFilterVO.getVersion());
	}

	public void setFilterName(String sFilterName) {
		this.sFilterName = sFilterName;
	}

	public String getFilterName() {
		return this.sFilterName;
	}

	public void setDescription(String sDescription) {
		this.sDescription = sDescription;
	}

	public String getDescription() {
		return this.sDescription;
	}

	public void setFilterPrefs(String sFilterPrefs) {
		this.sFilterPrefs = sFilterPrefs;
	}

	public String getFilterPrefs() {
		return this.sFilterPrefs;
	}

	public void setOwner(String sOwner) {
		this.sOwner = sOwner;
	}

	public String getOwner() {
		return this.sOwner;
	}

	public void setEntity(String sEntity) {
		this.sEntity = sEntity;
	}

	public String getEntity() {
		return this.sEntity;
	}

	public void setSearchDeleted(Integer iSearchDeleted) {
		if (iSearchDeleted == null) {
			iSearchDeleted = CollectableGenericObjectSearchExpression.SEARCH_UNDELETED;
		}

		this.iSearchDeleted = iSearchDeleted;
	}

	public Integer getSearchDeleted() {
		return this.iSearchDeleted;
	}

	public void setSearchFilterUser(SearchFilterUserVO searchFilterUserVO) {
		this.searchFilterUserVO = searchFilterUserVO;
	}

	public SearchFilterUserVO getSearchFilterUser() {
		return this.searchFilterUserVO;
	}

	public void setEditable(Boolean bEditable) {
		this.getSearchFilterUser().setEditable(bEditable);
	}

	public Boolean isEditable() {
		return this.getSearchFilterUser().isEditable();
	}

	public void setForced(Boolean bForced) {
		this.getSearchFilterUser().setForced(bForced);
	}

	public Boolean isForced() {
		return this.getSearchFilterUser().isForced();
	}

	public void setValidFrom(Date dValidFrom) {
		this.getSearchFilterUser().setValidFrom(dValidFrom);
	}

	public Date getValidFrom() {
		return this.getSearchFilterUser().getValidFrom();
	}

	public void setValidUntil(Date dValidUntil) {
		this.getSearchFilterUser().setValidUntil(dValidUntil);
	}

	public Date getValidUntil() {
		return this.getSearchFilterUser().getValidUntil();
	}

	public String getLabelResourceId() {
		return labelResourceId;
	}

	public void setLabelResourceId(String labelResourceId) {
		this.labelResourceId = labelResourceId;
	}

	public String getDescriptionResourceId() {
		return descriptionResourceId;
	}

	public void setDescriptionResourceId(String descriptionResourceId) {
		this.descriptionResourceId = descriptionResourceId;
	}

	/**
	 * transforms a MasterDataVO into a SearchFilterVO
	 * @param mdVO_searchfilter
	 * @param mdVO_searchFilteruser
	 * @return SearchFilterVO
	 */
	public static SearchFilterVO transformToSearchFilter(MasterDataVO mdVO_searchfilter, MasterDataVO mdVO_searchFilteruser) {
		SearchFilterUserVO searchFilerUserVO = SearchFilterUserVO.transformToSearchFilterUser(mdVO_searchFilteruser);

		SearchFilterVO searchFilter = new SearchFilterVO(
				mdVO_searchfilter.getIntId(),
		(String)mdVO_searchfilter.getField("name"),
		(String)mdVO_searchfilter.getField("description"),
		(String)mdVO_searchfilter.getField("clbsearchfilter"),
		mdVO_searchfilter.getCreatedBy(),
		(String)mdVO_searchfilter.getField("entity"),
		(Integer)mdVO_searchfilter.getField("searchDeleted"),
		(String)mdVO_searchfilter.getField("labelres"),
		(String)mdVO_searchfilter.getField("descriptionres"),
		searchFilerUserVO,
		mdVO_searchfilter.getCreatedAt(),
		mdVO_searchfilter.getCreatedBy(),
		mdVO_searchfilter.getChangedAt(),
		mdVO_searchfilter.getChangedBy(),
		mdVO_searchfilter.getVersion());

		return searchFilter;
	}

	/**
	 * transforms a SearchFilterVO into a MasterDataVO
	 * @param searchFilter
	 * @return MasterDataVO
	 */
	public static MasterDataVO transformToMasterData(SearchFilterVO searchFilter) {
		Map<String, Object> mpField = new HashMap<String, Object>();
		mpField.put("name", searchFilter.getFilterName());
		mpField.put("description", searchFilter.getDescription());
		mpField.put("clbsearchfilter", searchFilter.getFilterPrefs());
		mpField.put("entity", searchFilter.getEntity());
		mpField.put("searchDeleted", searchFilter.getSearchDeleted());

		MasterDataVO mdVO = new MasterDataVO(
				searchFilter.getId(),
				searchFilter.getCreatedAt(),
				searchFilter.getCreatedBy(),
				searchFilter.getChangedAt(),
				searchFilter.getChangedBy(),
				searchFilter.getVersion(),
				mpField);

		return mdVO;
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("id=").append(getId());
		result.append(",name=").append(getFilterName());
		result.append(",entity=").append(getEntity());
		result.append(",owner=").append(getOwner());
		result.append(",forced=").append(isForced());
		result.append("]");
		return result.toString();
	}

}
