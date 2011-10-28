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
package org.nuclos.server.attribute.valueobject;

import org.nuclos.common.UsageCriteria;
import java.io.Serializable;

/**
 * Value object representing a layout usage entry. Immutable.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 00.01.000
 */
public class LayoutUsageVO implements Serializable {

	private final Integer iLayoutId;
	private final UsageCriteria usagecriteria;
	private final boolean bSearchScreen;

	/**
	 * constructor to be called by server only.
	 * @param iLayoutId id of layout
	 * @param usagecriteria
	 * @param bSearchScreen
	 */
	public LayoutUsageVO(Integer iLayoutId, UsageCriteria usagecriteria, boolean bSearchScreen) {
		this.iLayoutId = iLayoutId;
		this.usagecriteria = usagecriteria;
		this.bSearchScreen = bSearchScreen;
	}

	public Integer getLayoutId() {
		return iLayoutId;
	}

	public UsageCriteria getUsageCriteria() {
		return this.usagecriteria;
	}

	/**
	 * @return is layout intended for search screen?
	 */
	public boolean isSearchScreen() {
		return bSearchScreen;
	}

}	// class LayoutUsageVO
