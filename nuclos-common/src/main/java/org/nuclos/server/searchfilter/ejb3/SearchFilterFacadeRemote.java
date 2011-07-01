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
package org.nuclos.server.searchfilter.ejb3;

import java.util.Collection;

import javax.annotation.security.RolesAllowed;
import javax.ejb.CreateException;
import javax.ejb.Remote;

import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.searchfilter.valueobject.SearchFilterVO;

@Remote
public interface SearchFilterFacadeRemote {

	public Object modify(String sEntityName, MasterDataVO mdvo, DependantMasterDataMap mpDependants)
		throws CommonCreateException, CommonFinderException, CommonRemoveException, 
		CommonStaleVersionException,	CommonValidationException, CommonPermissionException, 
		NuclosBusinessRuleException;
	
	/**
	 * @return all searchfilters for the given user
	 * @throws CreateException
	 * @throws CommonPermissionException
	 * @throws CommonFinderException
	 */
	@RolesAllowed("Login")
	public abstract Collection<SearchFilterVO> getAllSearchFilterByUser(
		String sUser) throws CreateException, CommonFinderException,
		CommonPermissionException;

	/**
	 * creates the given search filter for the current user as owner
	 * @throws CreateException
	 * @throws CommonPermissionException
	 * @throws CommonCreateException
	 * @throws NuclosBusinessRuleException
	 */
	@RolesAllowed("Login")
	public abstract SearchFilterVO createSearchFilter(SearchFilterVO filterVO)
		throws NuclosBusinessRuleException, CommonCreateException,
		CommonPermissionException, CreateException;

	/**
	 * modifies the given searchfilter
	 * ATTENTION: this will not modify the searchfilteruser, only the searchfilter will be modified
	 * @throws CreateException
	 * @throws CommonPermissionException
	 * @throws CommonValidationException
	 * @throws CommonStaleVersionException
	 * @throws CommonRemoveException
	 * @throws CommonFinderException
	 * @throws CommonCreateException
	 * @throws NuclosBusinessRuleException
	 */
	@RolesAllowed("Login")
	public abstract SearchFilterVO modifySearchFilter(SearchFilterVO filterVO)
		throws NuclosBusinessRuleException, CommonCreateException,
		CommonFinderException, CommonRemoveException,
		CommonStaleVersionException, CommonValidationException,
		CommonPermissionException, CreateException;

	/**
	 * deletes the given searchfilter
	 */
	@RolesAllowed("Login")
	public abstract void removeSearchFilter(SearchFilterVO filterVO)
		throws NuclosBusinessRuleException, CommonCreateException,
		CommonFinderException, CommonRemoveException,
		CommonStaleVersionException, CommonValidationException,
		CommonPermissionException, CreateException;

	/**
	 * updates the createdBy field of the given searchfilter
	 * ATTENTION: this is only used by the migration process
	 */
	@RolesAllowed("Login")
	public abstract void changeCreatedUser(Integer iId, String sUserName)
		throws NuclosBusinessRuleException, CommonCreateException,
		CommonFinderException, CommonRemoveException,
		CommonStaleVersionException, CommonValidationException,
		CommonPermissionException, CreateException;

}
