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
package org.nuclos.client.ui.collect;

import java.util.Collection;

import org.nuclos.client.ui.collect.component.CollectableComponent;

/**
 * Provides <code>CollectableComponent</code>s.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public interface CollectableComponentsProvider {

	/**
	 * @return a collection containing all <code>CollectableComponent</code>s. There may be components sharing the same field names.
	 * @postcondition result != null
	 */
	Collection<CollectableComponent> getCollectableComponents();
	
	/**
	 * @return a collection containing all <code>CollectableComponent Labels</code>s. There may be components sharing the same field names.
	 * @postcondition result != null
	 * NUCLEUSINT-442
	 */
	Collection<CollectableComponent> getCollectableLabels();

	/**
	 * @param sFieldName the desired field name of the collectable components to receive.
	 * @return a possibly empty <code>Collection</code> containing the <code>CollectableComponent</code>s with the given field name.
	 * Note that there may be multiple components for a given field name, especially in different tabs in the view.<br>
	 * <i>Regarding the name: "getCollectableComponents(sFieldName)" is too easily mixed up with getCollectableComponents() as they both have the same
	 * return type. "getCollectableComponentsByName(sFieldName)" is misleading, as it's the field name and not the JComponent's name that counts,
	 * and "getCollectableComponentsByFieldName(sFieldName)" was considered too long a name.</i>
	 * @postcondition result != null
	 */
	Collection<CollectableComponent> getCollectableComponentsFor(String sFieldName);

}	// class CollectableComponentsProvider
