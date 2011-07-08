//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.client.data.pivot;

/**
 * For displaying pivot subforms as part of the result list of a entity, we
 * need some additional information.
 *
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 */
public class PivotInfo implements Comparable<PivotInfo> {
	
	private final String subform;
	
	private final String keyField;
	
	private final String valueField;
	
	public PivotInfo(String subform, String keyField, String valueField) {
		if (subform == null) throw new NullPointerException();
		this.subform = subform;
		this.keyField = keyField;
		this.valueField = valueField;
	}

	public String getSubform() {
		return subform;
	}

	public String getKeyField() {
		return keyField;
	}

	public String getValueField() {
		return valueField;
	}
	
	/**
	 * Only uses the subform field.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PivotInfo)) return false;
		final PivotInfo other = (PivotInfo) o;
		return subform.equals(other.subform);
	}
	
	/**
	 * Only uses the subform field.
	 */
	@Override
	public int hashCode() {
		return subform.hashCode() + 82781;
	}

	/**
	 * Only uses the subform field.
	 */
	@Override
	public int compareTo(PivotInfo o) {
		return subform.compareTo(o.subform);
	}

}
