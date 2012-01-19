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

import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common2.IdUtils;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import java.util.Collection;

/**
 * Provides <code>AttributeCVO</code>s by their ids or their names.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 * 
 * @deprecated AttributeCVO is deprecated, hence this is deprecated, too.
 */
public interface AttributeProvider {

	/**
	 * @param iAttributeId
	 * @return the AttributeCVO with the given id.
	 * @throws NuclosAttributeNotFoundException if there is no attribute with the given id.
	 * @postcondition result != null
	 */
	AttributeCVO getAttribute(int iAttributeId) throws NuclosAttributeNotFoundException;

	/**
	 * @param sAttributeName
	 * @return the attribute with the given name.
	 * @throws NuclosAttributeNotFoundException if there is no attribute with the given name
	 * @precondition sAttributeName != null
	 * @postcondition result != null
	 */
	AttributeCVO getAttribute(String sEntity, String sAttributeName) throws NuclosAttributeNotFoundException;
	AttributeCVO getAttribute(Integer iEntityId, String sAttributeName) throws NuclosAttributeNotFoundException;

	EntityFieldMetaDataVO getEntityField(String entity, String field) throws NuclosAttributeNotFoundException;
	
	/**
	 * get all attributes for this provider
	 * @return Collection<AttributeCVO> of attribute vos
	 */
	Collection<AttributeCVO> getAttributes();

	/**
	 * Transformer that gets an attribute by name.
	 * 
	 * @deprecated AttributeProvider is deprecated, hence this is deprecated, too.
	 */
	public static class GetAttributeByName implements Transformer<String, AttributeCVO> {
		private final AttributeProvider attrprovider;
		private final Integer iEntityId;

		/**
		 * @deprecated AttributeProvider is deprecated, hence this is deprecated, too.
		 */
		public GetAttributeByName(Integer iEntityId, AttributeProvider attrprovider) {
			this.attrprovider = attrprovider;
			this.iEntityId = iEntityId;
		}

		@Override
		public AttributeCVO transform(String sAttributeName) {
			return this.attrprovider.getAttribute(iEntityId, sAttributeName);
		}
	}

	/**
	 * Transformer that gets an attribute by id.
	 * 
	 * @deprecated AttributeProvider is deprecated, hence this is deprecated, too.
	 */
	public static class GetAttributeById implements Transformer<Integer, AttributeCVO> {
		private final AttributeProvider attrprovider;

		/**
		 * @deprecated AttributeProvider is deprecated, hence this is deprecated, too.
		 */
		public GetAttributeById(AttributeProvider attrprovider) {
			this.attrprovider = attrprovider;
		}

		@Override
		public AttributeCVO transform(Integer iAttributeId) {
			return this.attrprovider.getAttribute(iAttributeId);
		}
	}

	/**
	 * Transformer that gets an attribute id by name.
	 * 
	 * @deprecated AttributeProvider is deprecated, hence this is deprecated, too.
	 */
	public static class GetAttributeIdByName implements Transformer<String, Integer> {
		private final AttributeProvider attrprovider;
		private final String sEntity;

		/**
		 * @deprecated AttributeProvider is deprecated, hence this is deprecated, too.
		 */
		public GetAttributeIdByName(String sEntity, AttributeProvider attrprovider) {
			this.attrprovider = attrprovider;
			this.sEntity = sEntity;
		}

		@Override
		public Integer transform(String sAttributeName) {
			return this.attrprovider.getAttribute(sEntity, sAttributeName).getId();
		}
	}

	/**
	 * Transformer that gets an attribute id by name.
	 * 
	 * @deprecated AttributeProvider is deprecated, hence this is deprecated, too.
	 */
	public static class GetAttributeLongIdByName implements Transformer<String, Long> {
		
		private final AttributeProvider attrprovider;
		private final String sEntity;

		/**
		 * @deprecated AttributeProvider is deprecated, hence this is deprecated, too.
		 */
		public GetAttributeLongIdByName(String sEntity, AttributeProvider attrprovider) {
			this.attrprovider = attrprovider;
			this.sEntity = sEntity;
		}

		@Override
		public Long transform(String sAttributeName) {
			return IdUtils.toLongId(attrprovider.getAttribute(sEntity, sAttributeName).getId());
		}
	}

}	// interface AttributeProvider
