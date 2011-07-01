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
package org.nuclos.client.searchfilter;

import java.util.Date;

import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.server.searchfilter.valueobject.SearchFilterVO;

/**
 * An abstract search filter (base class for local and global search filters).
 * @invariant this.isDefaultFilter() --> this.getSearchCondition() == null
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public abstract class SearchFilter {
	
	private boolean bValid = true;

	private SearchFilterVO searchfilterVO;

	private CollectableSearchCondition searchcond;

	public SearchFilter() {
		searchfilterVO = new SearchFilterVO();
	}

	/**
	 * @return Is this filter a default filter? The default filter's search condition is always <code>null</code>.
	 */
	public boolean isDefaultFilter() {
		return false;
	}
	
	public void setSearchFilterVO(SearchFilterVO searchfilterVO) {
		this.searchfilterVO = searchfilterVO;
	}
	
	public SearchFilterVO getSearchFilterVO() {
		return this.searchfilterVO;
	}
	
	/**
	 * @return the filter's id
	 */
	public Integer getId() {
		return this.searchfilterVO.getId();
	}
	
	/**
	 * @return this filter's name
	 */
	public String getName() {
		return this.searchfilterVO.getFilterName();
	}

	public void setName(String sName) {
		this.searchfilterVO.setFilterName(sName);
	}

	/**
	 * @return a description of this filter
	 */
	public String getDescription() {
		return this.searchfilterVO.getDescription();
	}

	public void setDescription(String sDescription) {
		this.searchfilterVO.setDescription(sDescription);
	}
	
	/**
	 * @return the owner of this filter
	 */
	public String getOwner() {
		return this.searchfilterVO.getOwner();
	}

	public Integer getSearchDeleted() {
		return this.searchfilterVO.getSearchDeleted();
	}

	public void setSearchDeleted(Integer searchDeleted) {
		this.searchfilterVO.setSearchDeleted(searchDeleted);
	}
	
	public Boolean isEditable() {
		return this.searchfilterVO.isEditable();
	}
	
	public void setEditable(Boolean bEditable) {
		this.searchfilterVO.setEditable(bEditable);
	}
	
	public void setValidFrom(Date datValidFrom) {
		getSearchFilterVO().setValidFrom(datValidFrom);
	}
	
	public Date getValidFrom() {
		return getSearchFilterVO().getValidFrom();
	}
	
	public void setValidUntil(Date datValidUntil) {
		getSearchFilterVO().setValidUntil(datValidUntil);
	}

	public Date getValidUntil() {
		return getSearchFilterVO().getValidUntil();
	}
	
	/**
	 * @return this filter's search condition
	 */
	public CollectableSearchCondition getSearchCondition() {
		return this.searchcond;
	}

	public void setSearchCondition(CollectableSearchCondition searchcond) {
		this.searchcond = searchcond;
	}

	/**
	 * @return the internal search condition that is to be used for the actual search.
	 */
	public abstract CollectableSearchCondition getInternalSearchCondition();

	@Override
	public String toString() {
		return this.getName();
	}

	/**
	 * Two <code>SearchFilter</code>s are equal iff their names are equal.
	 * @param o
	 * @return {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SearchFilter)) {
			return false;
		}

		return LangUtils.equals(this.getId(), ((SearchFilter) o).getId());
//		return LangUtils.equals(this.getName(), ((SearchFilter) o).getName());
	}

	@Override
	public int hashCode() {
		return LangUtils.hashCode(this.getName());
	}

	public void validate() throws IllegalStateException {
		/** @todo use custom business exception */
		SearchFilter.validate(this.getName());
	}

	public static void validate(String sFilterName) throws IllegalStateException {
		if (StringUtils.isNullOrEmpty(sFilterName)) {
			throw new IllegalStateException(CommonLocaleDelegate.getMessage("SearchFilter.1", "Filtername darf nicht leer sein."));
		}
		if (sFilterName.matches(".*\\\\.*")) {
			throw new IllegalStateException(CommonLocaleDelegate.getMessage("SearchFilter.2", "Filtername darf keinen Backslash (\"\\\") enthalten."));
		}
	}

	void put() throws NuclosBusinessException {
		try {
			this.validate();
		}
		catch (Exception ex) {
			throw new NuclosBusinessException(ex);
		}
			
		SearchFilterDelegate.getInstance().insertSearchFilter(this);
	}

	/**
	 * @param sFilterName
	 * @return
	 * @precondition sFilterName != null
	 */
	static String encoded(String sFilterName) {
		return sFilterName.replace('/', '\\');
	}

	/**
	 * @param sFilterName
	 * @return
	 * @precondition sFilterName != null
	 */
	static String decoded(String sFilterName) {
		return sFilterName.replace('\\', '/');
	}
	
	public boolean isValid() {
		return bValid;
	}

	public void setValid(boolean bValid) {
		this.bValid = bValid;
	}

}	// class SearchFilter
