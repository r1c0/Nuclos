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
package org.nuclos.server.masterdata.ejb3;

import javax.annotation.security.RolesAllowed;
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

@Remote
public interface MasterDataModuleFacadeRemote {

	/**
	 * create a new master data record with the given id
	 * @param mdvo the master data record to be created
	 * @param mpDependants map containing dependant masterdata, if any
	 * @return master data value object containing the newly created record
	 * @throws NuclosBusinessRuleException
	 * @precondition sEntityName != null
	 * @precondition mdvo.getId() == null
	 * @precondition (mpDependants != null) --> mpDependants.areAllDependantsNew()
	 * @nucleus.permission checkWriteAllowed(sEntityName)
	 */
	@RolesAllowed("Login")
	public abstract MasterDataVO create(String sEntityName, MasterDataVO mdvo,
		DependantMasterDataMap mpDependants) throws CommonCreateException,
		CommonPermissionException, NuclosBusinessRuleException;

	/**
	 * modifies an existing master data record.
	 * @param sEntityName name of current entity
	 * @param mdvo the master data record
	 * @param mpDependants map containing dependant masterdata, if any
	 * @param mpDependants map containing dependant masterdata and its id
	 * @return id of the modified master data record
	 * @throws NuclosBusinessRuleException
	 * @precondition sEntityName != null
	 * @nucleus.permission checkWriteAllowed(sEntityName)
	 */
	@RolesAllowed("Login")
	public abstract Object modify(String sEntityName, MasterDataVO mdvo,
		DependantMasterDataMap mpDependants) throws CommonCreateException,
		CommonFinderException, CommonRemoveException,
		CommonStaleVersionException, CommonValidationException,
		CommonPermissionException, NuclosBusinessRuleException;

	/**
	 * method to delete an existing master data record
	 * @param mdvo containing the master data record
	 * @param bRemoveDependants remove all dependants if true, else remove only given (single) mdvo record
	 * 			this is helpful for entities which have no layout
	 * @precondition sEntityName != null
	 * @nucleus.permission checkDeleteAllowed(sEntityName)
	 */
	@RolesAllowed("Login")
	public abstract void remove(String sEntityName, MasterDataVO mdvo,
		boolean bRemoveDependants) throws NuclosBusinessRuleException,
		CommonPermissionException, CommonStaleVersionException,
		CommonRemoveException, CommonFinderException;

	public abstract String getResourceSIdForLabel(Integer iId);

	public abstract String getResourceSIdForDescription(Integer iId);

	public abstract String getResourceSIdForTreeView(Integer iId);

	public abstract String getResourceSIdForTreeViewDescription(Integer iId);

	public abstract String getResourceSIdForMenuPath(Integer iId);
}
