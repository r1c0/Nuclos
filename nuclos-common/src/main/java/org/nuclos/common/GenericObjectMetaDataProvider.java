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
package org.nuclos.common;

import java.util.Collection;
import java.util.Set;

import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.attribute.valueobject.AttributeCVO;

/**
 * provides meta information about leased objects (including attributes, layouts and modules).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public interface GenericObjectMetaDataProvider extends AttributeProvider {

	/**
	 * @param iModuleId may be <code>null</code>
	 * @param bSearchable
	 * @return Collection<AttributeCVO> all attributes used in the module with the given id.
	 * Note that layouts used for Details and Search are taken into account here.
	 * @postcondition result != null
	 */
	Collection<AttributeCVO> getAttributeCVOsByModuleId(Integer iModuleId, Boolean bSearchable);

	/**
	 * @param iModuleId may be <code>null</code>
	 * @param bSearchable may be <code>null</code>. Determines if only Search layouts (Boolean.TRUE) or only Details layouts (Boolean.FALSE)
	 * or both (null) are taken into account here.
	 * @return the names of all attributes used in the module with the given id.
	 * @postcondition result != null
	 */
	Set<String> getAttributeNamesByModuleId(Integer iModuleId, Boolean bSearchable);

	/**
	 * @param iLayoutId
	 * @return Collection<AttributeCVO> all attributes used in the layout with the given id.
	 * @precondition iLayoutId != null
	 * @postcondition result != null
	 */
//	Collection<AttributeCVO> getAttributeCVOsByLayoutId(int iLayoutId);

	/**
	 * @param iLayoutId
	 * @return Set<String>
	 * @postcondition result != null
	 */
	Set<String> getSubFormEntityNamesByLayoutId(int iLayoutId);

	/**
	 * @param iLayoutId
	 * @return Collection<EntityAndFieldName>
	 * @postcondition result != null
	 */
	Collection<EntityAndFieldName> getSubFormEntityAndForeignKeyFieldNamesByLayoutId(int iLayoutId);

	/**
	 * @param usagecriteria
	 * @return Set<String> the names of the attributes contained in the best matching layout for the given usagecriteria.
	 * @throws CommonFinderException
	 * @postcondition result != null
	 */
	Set<String> getBestMatchingLayoutAttributeNames(UsageCriteria usagecriteria) throws CommonFinderException;

	/**
	 * @param usagecriteria
	 * @param bSearchScreen
	 * @return the id of the best matching layout for the given usage criteria
	 * @throws CommonFinderException if there is no matching layout for the given usage criteria at all.
	 */
	int getBestMatchingLayoutId(UsageCriteria usagecriteria, boolean bSearchScreen) throws CommonFinderException;

	/**
	 * @param iLayoutId
	 * @return the LayoutML definition of the layout with the given id.
	 */
	String getLayoutML(int iLayoutId);
	
	Set<Integer> getLayoutIdsByModuleId(int iModuleId, boolean bSearchScreen);

}	// interface GenericObjectMetaDataProvider
