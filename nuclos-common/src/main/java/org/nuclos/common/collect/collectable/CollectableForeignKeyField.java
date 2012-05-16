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


/**
 * A CollectableField representing a foreign key to another entity.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public abstract class CollectableForeignKeyField extends AbstractCollectableField {

	protected final Object oValueId;
	protected final Object oValue;

	/**
	 * @param clctef
	 * @param oValueId
	 * @precondition clctef != null
	 */
	public CollectableForeignKeyField(CollectableEntityField clctef, Object oValueId) {
		this.oValueId = oValueId;
		this.oValue = (oValueId == null) ? null : this.readValue(clctef, oValueId);
	}

	@Override
	public int getFieldType() {
		return CollectableField.TYPE_VALUEIDFIELD;
	}

	@Override
	public Object getValueId() {
		return this.oValueId;
	}

	@Override
	public Object getValue() {
		return this.oValue;
	}

	/**
	 * reads the value for the given field corresponding to the given value id.
	 * @param clctef
	 * @param oValueId
	 * @return the read value.
	 * @precondition clctef != null
	 * @precondition oValueId != null
	 */
	protected abstract Object readValue(CollectableEntityField clctef, Object oValueId);

}	// class CollectableForeignKeyField
