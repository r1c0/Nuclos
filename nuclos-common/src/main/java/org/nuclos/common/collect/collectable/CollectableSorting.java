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

import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.PivotInfo;
import org.nuclos.common.dal.vo.SystemFields;

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
	
	private static final long serialVersionUID = 1002927873365829614L;

	private final String entity;
	
	private final boolean isBaseEntity;
	
	private final String field;
	
	// maybe null
	private final EntityFieldMetaDataVO mdField;
	
	private final boolean asc;
	
	// lazy initialization needed
	private String tableAlias = null;

	/**
	 * @param field name of the field to sort
	 * @param asc Sort ascending? (false: sort descending)
	 * @precondition sFieldName != null
	 */
	public CollectableSorting(String tableAlias, String entity, boolean isBaseEntity, String field, boolean asc) {
		if (entity == null || field == null) throw new NullPointerException();
		this.tableAlias = tableAlias;
		this.entity = entity;
		this.isBaseEntity = isBaseEntity;
		this.field = field;
		this.mdField = null;
		this.asc = asc;
	}

	/**
	 * Attention: Only use this for pivot sorting! (tp)
	 * 
	 * @author Thomas Pasch
	 * @since Nuclos 3.1.01
	 */
	public CollectableSorting(EntityFieldMetaDataVO mdField, boolean isBaseEntity, boolean asc) {
		if (mdField == null) throw new NullPointerException();
		
		final PivotInfo pinfo = mdField.getPivotInfo();
		if (pinfo == null) {
			throw new IllegalArgumentException("Constructor only defined for pivot fields");
		}
		
		this.entity = pinfo.getSubform();
		this.isBaseEntity = isBaseEntity;
		this.field = pinfo.getValueField();
		this.mdField = mdField;
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
	
	/**
	 * Attention: A call to this method is only valid on the <em>server</em> side!
	 */
	private static String initTableAlias(EntityFieldMetaDataVO mdField) {
		final String result;
		if (mdField == null) {
			throw new IllegalStateException();
		}
		else {
			final PivotInfo pinfo = mdField.getPivotInfo();
			if (pinfo == null) {
				result = SystemFields.BASE_ALIAS;
			}
			else {
				// The join table alias must be unique in the SQL
				result = pinfo.getPivotTableAlias(mdField.getField());
			}
		}
		return result;
	}
	
	public String getTableAlias() {
		if (tableAlias == null) {
			tableAlias = initTableAlias(mdField);
		}
		return tableAlias;
	}
	
	public String getEntity() {
		return entity;
	}

	public boolean isBaseEntity() {
		return isBaseEntity;
	}	
	
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof CollectableSorting)) return false;
		final CollectableSorting other = (CollectableSorting) o;
		return entity.equals(other.entity) && field.equals(other.field);
	}
	
	public int hashCode() {
		int result = 3 * entity.hashCode() + 7;
		result += 11 * field.hashCode();
		return result;
	}

    @Override
    public String toString() {
    	final StringBuilder result = new StringBuilder();
    	result.append("CollectableSorting[");
    	result.append(", field=").append(field);
    	result.append(", isBase=").append(isBaseEntity);
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

}  // class CollectableSorting
