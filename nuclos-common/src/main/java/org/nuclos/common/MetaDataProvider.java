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
import java.util.List;
import java.util.Map;

import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;

/**
 * Interface implemented by MetaDataProviders for accessing meta data information.
 */
public interface MetaDataProvider<S extends EntityMetaDataVO, T extends EntityFieldMetaDataVO> extends CommonMetaDataProvider<S, T> {

	public static final String NAMESPACE_NUCLOS = "NUC";

	public static final String NAMESPACE_DEFAULT = "DEF";

	Collection<S> getAllEntities();

	S getEntity(Long id);

	S getEntity(String entity);

	S getEntity(NuclosEntity entity);

	Map<String, T> getAllEntityFieldsByEntity(String entity);

    T getEntityField(String entity, String field);

	T getEntityField(String entity, Long fieldId);

	T getRefField(String baseEntity, String subform);
	
	Map<Long, LafParameterMap> getAllLafParameters();
	
	LafParameterMap getLafParameters(Long entityId);

	/**
	 * Get the base entity name of a dynamic entity.
	 *
	 * @param dynamicentityname The name of the dynamic entity.
	 * @return Returns the base entity name. Returns the original entity name if there is no dynamic entity with the given name.
	 */
	// String getBaseEntity(String dynamicentityname);

	/**
	 * Get entities by nuclet name
	 *
	 * @param nuclet The nuclet's acronym
	 * @return List of entity names that belong to the nuclet
	 */
	List<String> getEntities(String nuclet);
}
