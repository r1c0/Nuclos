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
package org.nuclos.common.ldap;

import org.nuclos.common2.KeyEnum;
import org.nuclos.common2.Localizable;

/**
 * LDAP search scopes.
 * See <code>javax.naming.directory.SearchControls</code>
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public enum SearchScope implements KeyEnum<Integer>, Localizable {

	OBJECT_SCOPE(0, "ldap.searchscope.object"),
	ONELEVEL_SCOPE(1, "ldap.searchscope.onelevel"),
	SUBTREE_SCOPE(2, "ldap.searchscope.subtree");

	private final Integer value;
	private final String resourceId;

	private SearchScope(Integer value, String resourceId) {
		this.value = value;
		this.resourceId = resourceId;
	}

	@Override
	public Integer getValue() {
		return this.value;
	}

	@Override
	public String getResourceId() {
		return resourceId;
	}
}	// enum ImportMode
