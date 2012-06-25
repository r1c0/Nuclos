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

import org.nuclos.common.entityobject.CollectableEOEntityProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The default <code>CollectableEntityProvider</code> used by the framework for a specific application.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 * 
 * @deprecated Use spring injection of CollectableEOEntityProvider directly.
 */
// @Component
public class DefaultCollectableEntityProvider {
	/**
	 * Note that this class doesn't implement the original Singleton pattern, rather a variation of it...
	 */
	private static CollectableEntityProvider INSTANCE;

	/**
	 * this class has no instances.
	 */
	DefaultCollectableEntityProvider() {
	}

	/**
	 * sets the global instance of the <code>DefaultCollectableEntityProvider</code> that knows the application specific entities.
	 * @param provider
	 */
	// @Autowired
	public final void setCollectableEOEntityProvider(CollectableEOEntityProvider provider) {
		INSTANCE = provider;
	}

	/**
	 * @return the global instance of the <code>DefaultCollectableEntityProvider</code> that knows the application specific entities.
	 */
	public static CollectableEntityProvider getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}

}  // class DefaultCollectableEntityProvider
