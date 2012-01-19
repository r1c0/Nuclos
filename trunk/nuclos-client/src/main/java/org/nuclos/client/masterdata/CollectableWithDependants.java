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
package org.nuclos.client.masterdata;

import java.util.Collection;

import org.nuclos.common.collect.collectable.AbstractCollectable;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collection.Transformer;

/**
 * A <code>Collectable</code> with dependants.
 * The dependants are <code>CollectableMasterData</code>, so note that this class is Nucleus specific.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public interface CollectableWithDependants extends Collectable {

	/**
	 * @param sSubEntityName
	 * @return a copy of the contained dependants for the subentity with the given name.
	 * @postcondition result != null
	 * @todo refactor: The argument should be: <code>EntityAndFieldName sefk</code>
	 */
	Collection<? extends AbstractCollectable> getDependants(String sSubEntityName);

	/**
	 * inner class GetDependants
	 */
	public static class GetDependants implements Transformer<CollectableWithDependants, Collection<? extends AbstractCollectable>> {
		private final String sSubEntityName;

		public GetDependants(String sSubEntityName) {
			this.sSubEntityName = sSubEntityName;
		}

		@Override
		public Collection<? extends AbstractCollectable> transform(CollectableWithDependants clctlowd) {
			return clctlowd.getDependants(this.sSubEntityName);
		}
	}

}	// interface CollectableWithDependants
