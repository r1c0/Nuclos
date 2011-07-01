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
package org.nuclos.common2;

import org.nuclos.common.collection.Transformer;
import org.nuclos.common.NuclosEntity;

import java.io.Serializable;

/**
 * Holds an entity name and a field name.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 * @todo add assertions
 * @todo define equals/hashCode!
 */
public class EntityAndFieldName implements Serializable {

	private final String sEntityName;
	private final String sFieldName;

	public EntityAndFieldName(NuclosEntity entity, String fieldName) {
		this(entity.getEntityName(), fieldName);
	}
	
	public EntityAndFieldName(String sEntityName, String sFieldName) {
		this.sEntityName = sEntityName;
		this.sFieldName = sFieldName;
	}

	/**
	 * @return the subform's entity name
	 */
	public String getEntityName() {
		return this.sEntityName;
	}

	/**
	 * @return name of the foreign key field to the subform's parent entity. May be <code>null</code>.
	 */
	public String getFieldName() {
		return this.sFieldName;
	}

	public static class GetEntityName implements Transformer<EntityAndFieldName, String> {
		@Override
		public String transform(EntityAndFieldName eafn) {
			return eafn.getEntityName();
		}
	}

}  // class EntityAndFieldName
