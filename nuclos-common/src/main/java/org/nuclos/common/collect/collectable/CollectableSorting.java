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
 * <p>
 * TODO: Incorporate a real (foreign) table ref instead of just the entity.
 * </p><p>
 * TODO: Consider including an entity field rather than the mere field name.
 * </p>
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public class CollectableSorting implements Serializable {
	
	private static final long serialVersionUID = 1002927873365829613L;

	private final String entity;
	
	private final boolean isBaseEntity;
	
	private final String field;
	
	private final boolean asc;

	/**
	 * @param field name of the field to sort
	 * @param asc Sort ascending? (false: sort descending)
	 * @precondition sFieldName != null
	 */
	public CollectableSorting(String entity, boolean isBaseEntity, String field, boolean asc) {
		this.entity = entity;
		this.isBaseEntity = isBaseEntity;
		this.field = field;
		this.asc = asc;
	}

	/**
	 * @return name of the field to sort.
	 * @postcondition result != null
	 */
	public String getFieldName() {
		return this.field;
	}

	/**
	 * @return Sort ascending? (false: sort descending)
	 */
	public boolean isAscending() {
		return this.asc;
	}
	
    @Override
    public String toString() {
    	final StringBuilder result = new StringBuilder();
    	result.append("CollectableSorting[");
    	result.append("field=").append(field);
    	result.append(",");
    	if (asc) {
    		result.append("ASC");
    	}
    	else {
    		result.append("DSC");
    	}
    	result.append("]");
    	return result.toString();
    }

	public String getEntity() {
		return entity;
	}

	public boolean isBaseEntity() {
		return isBaseEntity;
	}	

}  // class CollectableSorting
