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

import java.util.Set;

import org.nuclos.common.collection.Transformer;

/**
 * Contains the structural (meta) information about a <code>Collectable</code>.
 * This corresponds to the schema information in a relational database table.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public interface CollectableEntity {

	/**
	 * @return the number of fields
	 */
	int getFieldCount();

	/**
	 * @return the names of all fields
	 */
	Set<String> getFieldNames();

	/**
	 * @return the name of the field that contains the identifier for the collectable, if any.
	 * Note that this may be <code>null</code> in cases where there isn't a single identifying field
	 * (or column).
	 */
	String getIdentifierFieldName();

	/**
	 * @param sFieldName
	 * @return the meta information of the field with the given name
	 * @postcondition result != null
	 * @throws org.nuclos.common2.exception.CommonFatalException if a field with the given name is not contained in this entity.
	 */
	CollectableEntityField getEntityField(String sFieldName);

	/**
	 * @return the (internal) name of this entity
	 * @todo add postcondition result != null
	 */
	String getName();

	/**
	 * @return the label of this entity, as presented to the user.
	 */
	String getLabel();

	/**
	 * inner class <code>GetEntityField</code>: transforms a field name
	 * into the corresponding <code>CollectableEntityField</code> of the given <code>CollectableEntity</code>.
	 */
	public static class GetEntityField implements Transformer<String, CollectableEntityField> {
		private final CollectableEntity clcte;

		public GetEntityField(CollectableEntity clcte) {
			this.clcte = clcte;
		}

		@Override
		public CollectableEntityField transform(String sFieldName) {
			return this.clcte.getEntityField(sFieldName);
		}
	}

	/**
	 * inner class <code>GetName</code>: transforms a <code>CollectableEntity</code> into its name.
	 */
	public static class GetName implements Transformer<CollectableEntity, String> {
		@Override
		public String transform(CollectableEntity clcte) {
			return clcte.getName();
		}
	}

}  // class CollectableEntity
