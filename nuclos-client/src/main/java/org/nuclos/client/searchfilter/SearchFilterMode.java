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

import org.nuclos.common2.KeyEnum;
import org.nuclos.common2.Localizable;

/**
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public enum SearchFilterMode implements KeyEnum<Integer>, Localizable {

	UNDELETED(0, "SearchFilterCollectController.1"),
	DELETED(1, "SearchFilterCollectController.2"),
	ALL(2, "SearchFilterCollectController.3");

	private final Integer value;
	private final String resourceId;

	private SearchFilterMode(Integer value, String resourceId) {
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
}
