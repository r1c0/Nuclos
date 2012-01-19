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
 * The default <code>CollectableEntityProvider</code> used by the framework for a specific application.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class DefaultCollectableEntityProvider {
	/**
	 * Note that this class doesn't implement the original Singleton pattern, rather a variation of it...
	 */
	private static CollectableEntityProvider singleton;

	/**
	 * this class has no instances.
	 */
	private DefaultCollectableEntityProvider() {
		// do nothing
	}

	/**
	 * sets the global instance of the <code>DefaultCollectableEntityProvider</code> that knows the application specific entities.
	 * @param provider
	 */
	public static void setInstance(CollectableEntityProvider provider) {
		if(singleton != null) {
			throw new IllegalStateException("The DefaultCollectableEntityProvider has already been set.");
		}
		singleton = provider;
	}

	/**
	 * @return the global instance of the <code>DefaultCollectableEntityProvider</code> that knows the application specific entities.
	 */
	public static CollectableEntityProvider getInstance() {
		if (singleton == null) {
			throw new IllegalStateException("The DefaultCollectableEntityProvider must be set.");
		}
		return singleton;
	}

}  // class DefaultCollectableEntityProvider
