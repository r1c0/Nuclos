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

import java.util.NoSuchElementException;

/**
 * Provides CollectableEntities by name.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public interface CollectableEntityProvider {
	
	/**
	 * @param sEntityName name of the desired entity
	 * @return the <code>CollectableEntity</code> with the given name, if any.
	 * @throws NoSuchElementException if no entity with the given name exists.
	 * @todo adjust comment!
	 * @todo add precondition sEntityName != null
	 * @postcondition result != null
	 * @postcondition result.getName().equals(sEntityName)
	 */
	CollectableEntity getCollectableEntity(String sEntityName) throws NoSuchElementException;

	/**
	 * Is the entity with the given name displayable? If so, some CollectableComponents that display referencing fields
	 * provide the possibility to display the referenced object.
	 * @param sEntityName
	 * @return
	 * @throws NoSuchElementException
	 */
	boolean isEntityDisplayable(String sEntityName) throws NoSuchElementException;

}  // interface CollectableEntityProvider
