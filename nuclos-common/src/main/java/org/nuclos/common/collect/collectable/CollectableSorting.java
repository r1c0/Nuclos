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
package org.nuclos.common.collect.collectable;

import java.io.Serializable;

/**
 * Sorting (German: "Sortierung") of a <code>Collectable</code>, consisting of a field name and a direction.
 * Typically, this is an element in a <code>List</code> containing the complete sorting order.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 * @todo Consider including an entity field rather than the mere field name.
 */
public class CollectableSorting implements Serializable {
	private static final long serialVersionUID = 1002927873365829612L;

	private final String sFieldName;
	private final boolean bAscending;

	/**
	 * @param sFieldName name of the field to sort
	 * @param bAscending Sort ascending? (false: sort descending)
	 * @precondition sFieldName != null
	 */
	public CollectableSorting(String sFieldName, boolean bAscending) {
		this.sFieldName = sFieldName;
		this.bAscending = bAscending;
	}

	/**
	 * @return name of the field to sort.
	 * @postcondition result != null
	 */
	public String getFieldName() {
		return this.sFieldName;
	}

	/**
	 * @return Sort ascending? (false: sort descending)
	 */
	public boolean isAscending() {
		return this.bAscending;
	}
	
    @Override
    public String toString() {
    	final StringBuilder result = new StringBuilder();
    	result.append("CollectableSorting[");
    	result.append("field=").append(sFieldName);
    	result.append(",");
    	if (bAscending) {
    		result.append("ASC");
    	}
    	else {
    		result.append("DSC");
    	}
    	result.append("]");
    	return result.toString();
    }	

}  // class CollectableSorting
