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
package org.nuclos.client.dbtransfer;

import org.nuclos.common.dal.vo.EntityObjectVO;

public class ParameterComparison extends EntityObjectVO {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final boolean isNew, isDeleted;
	private final String currentValue;
	protected ParameterComparison(EntityObjectVO eovo, boolean isNew, boolean isDeleted, String currentValue) {
		this.initFields(eovo.getFields().size(), 0);
		this.getFields().putAll(eovo.getFields());
		this.isNew = isNew;
		this.isDeleted = isDeleted;
		this.currentValue = currentValue;
	}
	public boolean isNew() {
		return isNew;
	}
	public boolean isDeleted() {
		return isDeleted;
	}
	public String getCurrentValue() {
		return currentValue;
	}
	public boolean isValueChanged() {
		return isNew() || isDeleted() || (super.getField("value", String.class) != null && !super.getField("value", String.class).equals(getCurrentValue())) 
		|| (super.getField("value", String.class) == null && getCurrentValue() != null);
	}
}
