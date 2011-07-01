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
package org.nuclos.server.attribute.ejb3;

import java.util.Collection;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;

import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.attribute.valueobject.LayoutVO;

@Remote
public interface LayoutFacadeRemote {

	/**
	 * imports the given layouts, adding new and overwriting existing layouts. The other existing layouts are untouched.
	 * Currently, only the layoutml description is imported, not the usages.
	 * @param colllayoutvo
	 */
	@RolesAllowed("UseManagementConsole")
	public abstract void importLayouts(
		Collection<LayoutVO> colllayoutvo) throws CommonBusinessException;

	/**
	 * refreshes the module attribute relation table and all generic object views (console function)
	 */
	@RolesAllowed("UseManagementConsole")
	public abstract void refreshAll();
	
	/**
	 * @param sEntity
	 * @return true, if detail layout is available for the given entity name, otherwise false
	 */
	@RolesAllowed("Login")
	public abstract boolean isMasterDataLayoutAvailable(String sEntity);

}
