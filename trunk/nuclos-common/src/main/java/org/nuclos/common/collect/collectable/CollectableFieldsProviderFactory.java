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
 * Factory that creates <code>CollectableFieldProvider</code>s.
 * @todo merge the three methods to one.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public interface CollectableFieldsProviderFactory {

	/**
	 * called for provider type="default"
	 * @param sEntityName may be <code>null</code> for fields in the form. Always non-null for subforms.
	 * @param sFieldName
	 * @return
	 * @todo add precondition sEntityName != null
	 */
	CollectableFieldsProvider newDefaultCollectableFieldsProvider(String sEntityName, String sFieldName);

	/**
	 * called for provider type="dependant"
	 * @param sEntityName may be <code>null</code> for fields in the form. Always non-null for subforms.
	 * @param sFieldName
	 * @return
	 * @todo add precondition sEntityName != null
	 */
	CollectableFieldsProvider newDependantCollectableFieldsProvider(String sEntityName, String sFieldName);

	/**
	 * called for provider type="custom"
	 * @param sCustomType
	 * @param sEntityName may be <code>null</code> for fields in the form. Always non-null for subforms.
	 * @param sFieldName
	 * @return
	 * @todo add precondition sEntityName != null
	 */
	CollectableFieldsProvider newCustomCollectableFieldsProvider(String sCustomType, String sEntityName, String sFieldName);

}  // interface CollectableFieldsProviderFactory
