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
import java.util.Map;

import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.PivotInfo;

/**
 * Interface implemented by MetaDataProviders for accessing meta data information.
 */
public interface MetaDataProvider extends CommonMetaDataProvider {

	Collection<EntityMetaDataVO> getAllEntities();

	EntityMetaDataVO getEntity(Long id);

	EntityMetaDataVO getEntity(String entity);

	EntityMetaDataVO getEntity(NuclosEntity entity);

	Map<String, EntityFieldMetaDataVO> getAllEntityFieldsByEntity(String entity);

    Map<String, EntityFieldMetaDataVO> getAllPivotEntityFields(PivotInfo info);

    EntityFieldMetaDataVO getEntityField(String entity, String field);

	EntityFieldMetaDataVO getEntityField(String entity, Long fieldId);

	EntityFieldMetaDataVO getRefField(String baseEntity, String subform);

	/**
	 * Get the base entity name of a dynamic entity.
	 *
	 * @param dynamicentityname The name of the dynamic entity.
	 * @return Returns the base entity name. Returns the original entity name if there is no dynamic entity with the given name.
	 */
	String getBaseEntity(String dynamicentityname);
}
