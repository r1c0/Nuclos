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
package org.nuclos.client.ui.collect.component.model;

import java.util.Collection;

/**
 * Provides <code>CollectableComponentModel</code>s.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public interface CollectableComponentModelProvider {

	/**
	 * @return all field names. Note that these could be calculated via {@link #getCollectableComponentModels()},
	 * but are provided here for convenience.
	 */
	Collection<String> getFieldNames();

	/**
	 * @return all <code>CollectableComponentModel</code>s.
	 */
	Collection<? extends CollectableComponentModel> getCollectableComponentModels();

	/**
	 * @param sFieldName
	 * @return the <code>CollectableComponentModel</code> with the given name, if any.
	 */
	CollectableComponentModel getCollectableComponentModelFor(String sFieldName);

}  // interface CollectableComponentModelProvider
