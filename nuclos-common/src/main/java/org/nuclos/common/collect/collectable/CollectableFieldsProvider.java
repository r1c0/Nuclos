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

import java.util.List;

import org.nuclos.common2.exception.CommonBusinessException;

/**
 * Provides a list of <code>CollectableField</code>s.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public interface CollectableFieldsProvider {

	/**
	 * sets the parameter with the given name to the given value. Parameters that are unknown to a specific provider
	 * are ignored by this method.
	 * @param sName
	 * @param oValue
	 */
	void setParameter(String sName, Object oValue);

	/**
	 * @return List<CollectableField>
	 * @todo A Collection<? extends CollectableField> would be more appropriate here as the provider shouldn't have to
	 * 		care about a specific ordering. We make no assertions about ordering here anyway.
	 */
	List<CollectableField> getCollectableFields() throws CommonBusinessException;

}  // interface CollectableFieldsProvider
